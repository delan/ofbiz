/*
 * $Id$
 *
 * Copyright (c) 2001, 2002 The Open For Business Project - www.ofbiz.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT
 * OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR
 * THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */
package org.ofbiz.service.job;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.sql.Timestamp;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilProperties;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.GenericDelegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.serialize.SerializeException;
import org.ofbiz.entity.serialize.XmlSerializer;
import org.ofbiz.entity.transaction.GenericTransactionException;
import org.ofbiz.entity.transaction.TransactionUtil;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericDispatcher;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.calendar.RecurrenceInfo;
import org.ofbiz.service.calendar.RecurrenceInfoException;
import org.ofbiz.service.config.ServiceConfigUtil;

/**
 * JobManager
 *
 * @author     <a href="mailto:jaz@ofbiz.org">Andy Zeneski</a>
 * @version    $Rev$
 * @since      2.0
 */
public class JobManager {

    public static final String module = JobManager.class.getName();
    public static final String dispatcherName = "JobDispatcher";

    protected GenericDelegator delegator;
    protected JobPoller jp;

    /** Creates a new JobManager object. */
    public JobManager(GenericDelegator delegator) {
        this.delegator = delegator;
        jp = new JobPoller(this);
    }

    /** Queues a Job to run now. */
    public void runJob(Job job) throws JobManagerException {
        if (job.isValid())
            jp.queueNow(job);
    }

    /** Returns the ServiceDispatcher. */
    public LocalDispatcher getDispatcher() {
        LocalDispatcher thisDispatcher = null;
        try {
            thisDispatcher = GenericDispatcher.getLocalDispatcher(dispatcherName, delegator);
        } catch (GenericServiceException e) {
            Debug.logError(e, module);
        }
        return thisDispatcher;
    }

    /** Returns the GenericDelegator. */
    public GenericDelegator getDelegator() {
        return this.delegator;
    }

    public synchronized Iterator poll() {
        List poll = new ArrayList();
        Collection jobEnt = null;

        // sort the results by time
        List order = UtilMisc.toList("runTime");

        // basic query
        List expressions = UtilMisc.toList(new EntityExpr("runTime", EntityOperator.LESS_THAN_EQUAL_TO,
                UtilDateTime.nowTimestamp()), new EntityExpr("startDateTime", EntityOperator.EQUALS, null),
                new EntityExpr("cancelDateTime", EntityOperator.EQUALS, null));

        // limit to just defined pools
        List pools = ServiceConfigUtil.getRunPools();
        List poolsExpr = UtilMisc.toList(new EntityExpr("poolId", EntityOperator.EQUALS, null));
        if (pools != null) {
            Iterator poolsIter = pools.iterator();
            while (poolsIter.hasNext()) {
                String poolName = (String) poolsIter.next();
                poolsExpr.add(new EntityExpr("poolId", EntityOperator.EQUALS, poolName));
            }
        }

        // make the conditions
        EntityCondition baseCondition = new EntityConditionList(expressions, EntityOperator.AND);
        EntityCondition poolCondition = new EntityConditionList(poolsExpr, EntityOperator.OR);
        EntityCondition mainCondition = new EntityConditionList(UtilMisc.toList(baseCondition, poolCondition), EntityOperator.AND);

        // we will loop until we have no more to do
        boolean pollDone = false;

        while (!pollDone) {
            boolean beganTransaction;
            try {
                beganTransaction = TransactionUtil.begin();
            } catch (GenericTransactionException e) {
                Debug.logError(e, "Unable to start transaction; not polling for jobs", module);
                return null;
            }
            if (!beganTransaction) {
                Debug.logError("Unable to poll for jobs; transaction was not started by this process", module);
                return null;
            }

            try {
                jobEnt = delegator.findByCondition("JobSandbox", mainCondition, null, order);
            } catch (GenericEntityException ee) {
                Debug.logError(ee, "Cannot load jobs from datasource.", module);
            } catch (Exception e) {
                Debug.logError(e, "Unknown error.", module);
            }

            if (jobEnt != null && jobEnt.size() > 0) {
                Iterator i = jobEnt.iterator();

                while (i.hasNext()) {
                    GenericValue v = (GenericValue) i.next();
                    DispatchContext dctx = getDispatcher().getDispatchContext();

                    if (dctx == null) {
                        Debug.logError("Unable to locate DispatchContext object; not running job!", module);
                        continue;
                    }
                    Job job = new PersistedServiceJob(dctx, v, null); // todo fix the requester
                    poll.add(job);
                }
            } else {
                pollDone = true;
            }

            // finished this run; commit the transaction
            try {
                TransactionUtil.commit(beganTransaction);
            } catch (GenericTransactionException e) {
                Debug.logError(e, module);
            }

        }
        return poll.iterator();
    }

    public synchronized void reloadCrashedJobs() {
        String instanceId = UtilProperties.getPropertyValue("general.properties", "unique.instanceId", "ofbiz0");
        List toStore = new ArrayList();
        List crashed = null;

        List exprs = UtilMisc.toList(new EntityExpr("startDateTime", EntityOperator.NOT_EQUAL, null));
        exprs.add(new EntityExpr("finishDateTime", EntityOperator.EQUALS, null));
        exprs.add(new EntityExpr("cancelDateTime", EntityOperator.EQUALS, null));
        exprs.add(new EntityExpr("runByInstanceId", EntityOperator.EQUALS, instanceId));
        try {
            crashed = delegator.findByAnd("JobSandbox", exprs, UtilMisc.toList("startDateTime"));
        } catch (GenericEntityException e) {
            Debug.logError(e, "Unable to load crashed jobs", module);
        }

        if (crashed != null && crashed.size() > 0) {
            Iterator i = crashed.iterator();
            while (i.hasNext()) {
                GenericValue job = (GenericValue) i.next();
                long runtime = job.getTimestamp("runTime").getTime();
                RecurrenceInfo ri = JobManager.getRecurrenceInfo(job);
                if (ri != null) {
                    long next = ri.next();
                    if (next <= runtime) {
                        Timestamp now = UtilDateTime.nowTimestamp();
                        // only re-schedule if there is no new recurrences since last run
                        Debug.log("Scheduling Job : " + job, module);

                        String newJobId = job.getDelegator().getNextSeqId("JobSandbox").toString();
                        String pJobId = job.getString("parentJobId");
                        if (pJobId == null) {
                            pJobId = job.getString("jobId");
                        }
                        GenericValue newJob = new GenericValue(job);
                        newJob.set("statusId", "SERVICE_PENDING");
                        newJob.set("runTime", now);
                        newJob.set("jobId", newJobId);
                        newJob.set("previousJobId", job.getString("jobId"));
                        newJob.set("parentJobId", pJobId);
                        newJob.set("startDateTime", null);
                        toStore.add(newJob);

                        // set the cancel time on the old job to the same as the re-schedule time
                        job.set("statusId", "SERVICE_CRASHED");
                        job.set("cancelDateTime", now);
                        toStore.add(job);
                    }
                }
            }

            if (toStore.size() > 0) {
                try {
                    delegator.storeAll(toStore);
                } catch (GenericEntityException e) {
                    Debug.logError(e, module);
                }
                if (Debug.infoOn()) Debug.logInfo("-- " + toStore.size() + " jobs re-scheduled", module);
            }

        } else {
            if (Debug.infoOn()) Debug.logInfo("No crashed jobs to re-schedule", module);
        }
    }

    /**
     * Schedule a job to start at a specific time with specific recurrence info
     *@param serviceName The name of the service to invoke
     *@param context The context for the service
     *@param startTime The time in milliseconds the service should run
     *@param frequency The frequency of the recurrence (HOURLY,DAILY,MONTHLY,etc)
     *@param interval The interval of the frequency recurrence
     *@param count The number of times to repeat
     */
    public void schedule(String serviceName, Map context, long startTime, int frequency, int interval, int count) throws JobManagerException {
        schedule(serviceName, context, startTime, frequency, interval, count, 0);
    }

    /**
     * Schedule a job to start at a specific time with specific recurrence info
     *@param serviceName The name of the service to invoke
     *@param context The context for the service
     *@param startTime The time in milliseconds the service should run
     *@param frequency The frequency of the recurrence (HOURLY,DAILY,MONTHLY,etc)
     *@param interval The interval of the frequency recurrence
     *@param endTime The time in milliseconds the service should expire
     */
    public void schedule(String serviceName, Map context, long startTime, int frequency, int interval, long endTime) throws JobManagerException {
        schedule(serviceName, context, startTime, frequency, interval, -1, endTime);
    }

    /**
     * Schedule a job to start at a specific time with specific recurrence info
     *@param serviceName The name of the service to invoke
     *@param context The context for the service
     *@param startTime The time in milliseconds the service should run
     *@param frequency The frequency of the recurrence (HOURLY,DAILY,MONTHLY,etc)
     *@param interval The interval of the frequency recurrence
     *@param count The number of times to repeat
     *@param endTime The time in milliseconds the service should expire
     */
    public void schedule(String serviceName, Map context, long startTime, int frequency, int interval, int count, long endTime) throws JobManagerException {
        schedule(null, serviceName, context, startTime, frequency, interval, count, endTime);
    }

    /**
     * Schedule a job to start at a specific time with specific recurrence info
     *@param poolName The name of the pool to run the service from
     *@param serviceName The name of the service to invoke
     *@param context The context for the service
     *@param startTime The time in milliseconds the service should run
     *@param frequency The frequency of the recurrence (HOURLY,DAILY,MONTHLY,etc)
     *@param interval The interval of the frequency recurrence
     *@param count The number of times to repeat
     *@param endTime The time in milliseconds the service should expire
     */
    public void schedule(String poolName, String serviceName, Map context, long startTime, int frequency, int interval, int count, long endTime) throws JobManagerException {
        schedule(null, serviceName, context, startTime, frequency, interval, count, endTime, -1);
    }

    /**
     * Schedule a job to start at a specific time with specific recurrence info
     *@param poolName The name of the pool to run the service from
     *@param serviceName The name of the service to invoke
     *@param context The context for the service
     *@param startTime The time in milliseconds the service should run
     *@param frequency The frequency of the recurrence (HOURLY,DAILY,MONTHLY,etc)
     *@param interval The interval of the frequency recurrence
     *@param count The number of times to repeat
     *@param endTime The time in milliseconds the service should expire
     *@param maxRetry The max number of retries on failure (-1 for no max)
     */
    public void schedule(String poolName, String serviceName, Map context, long startTime, int frequency, int interval, int count, long endTime, int maxRetry) throws JobManagerException {
        if (delegator == null) {
            Debug.logWarning("No delegator referenced; cannot schedule job.", module);
            return;
        }

        // persist the context
        String dataId = null;
        try {
            dataId = delegator.getNextSeqId("RuntimeData").toString();
            GenericValue runtimeData = delegator.makeValue("RuntimeData", UtilMisc.toMap("runtimeDataId", dataId));

            runtimeData.set("runtimeInfo", XmlSerializer.serialize(context));
            delegator.create(runtimeData);
        } catch (GenericEntityException ee) {
            throw new JobManagerException(ee.getMessage(), ee);
        } catch (SerializeException se) {
            throw new JobManagerException(se.getMessage(), se);
        } catch (IOException ioe) {
            throw new JobManagerException(ioe.getMessage(), ioe);
        }

        // schedule the job
        schedule(poolName, serviceName, dataId, startTime, frequency, interval, count, endTime, maxRetry);
    }

    /**
     * Schedule a job to start at a specific time with specific recurrence info
     *@param poolName The name of the pool to run the service from
     *@param serviceName The name of the service to invoke
     *@param dataId The persisted context (RuntimeData.runtimeDataId)
     *@param startTime The time in milliseconds the service should run
     */
    public void schedule(String poolName, String serviceName, String dataId, long startTime) throws JobManagerException {
        schedule(poolName, serviceName, dataId, startTime, -1, 0, 1, 0, -1);
    }

    /**
     * Schedule a job to start at a specific time with specific recurrence info
     *@param poolName The name of the pool to run the service from
     *@param serviceName The name of the service to invoke
     *@param dataId The persisted context (RuntimeData.runtimeDataId)
     *@param startTime The time in milliseconds the service should run
     *@param frequency The frequency of the recurrence (HOURLY,DAILY,MONTHLY,etc)
     *@param interval The interval of the frequency recurrence
     *@param count The number of times to repeat
     *@param endTime The time in milliseconds the service should expire
     *@param maxRetry The max number of retries on failure (-1 for no max)
     */
    public void schedule(String poolName, String serviceName, String dataId, long startTime, int frequency, int interval, int count, long endTime, int maxRetry) throws JobManagerException {
        if (delegator == null) {
            Debug.logWarning("No delegator referenced; cannot schedule job.", module);
            return;
        }

        // create the recurrence
        String infoId = null;
        if (frequency > -1 && count != 0) {
            try {
                RecurrenceInfo info = RecurrenceInfo.makeInfo(delegator, startTime, frequency, interval, count);
                infoId = info.primaryKey();
            } catch (RecurrenceInfoException e) {
                throw new JobManagerException(e.getMessage(), e);
            }
        }

        // set the persisted fields
        String jobName = new String(new Long((new Date().getTime())).toString());
        String jobId = delegator.getNextSeqId("JobSandbox").toString();
        Map jFields = UtilMisc.toMap("jobId", jobId, "jobName", jobName, "runTime", new java.sql.Timestamp(startTime),
                "serviceName", serviceName, "recurrenceInfoId", infoId, "runtimeDataId", dataId);

        // set the pool ID
        if (poolName != null && poolName.length() > 0) {
            jFields.put("poolId", poolName);
        } else {
            jFields.put("poolId", ServiceConfigUtil.getSendPool());
        }

        // set the loader name
        jFields.put("loaderName", dispatcherName);

        // set the max retry
        jFields.put("maxRetry", new Long(maxRetry));

        // create the value and store
        GenericValue jobV = null;
        try {
            jobV = delegator.makeValue("JobSandbox", jFields);
            delegator.create(jobV);
        } catch (GenericEntityException e) {
            throw new JobManagerException(e.getMessage(), e);
        }
    }

    /**
     * Kill a JobInvoker Thread.
     * @param threadName Name of the JobInvoker Thread to kill.
     */
    public void killThread(String threadName) {
        jp.killThread(threadName);
    }

    /**
     * Get a List of each threads current state.
     * @return List containing a Map of each thread's state.
     */
    public List processList() {
        return jp.getPoolState();
    }

    /** Close out the scheduler thread. */
    public void finalize() {
        if (jp != null) {
            jp.stop();
            jp = null;
            Debug.logInfo("JobManager: Stopped Scheduler Thread.", module);
        }
    }

    /** gets the recurrence info object for a job. */
    public static RecurrenceInfo getRecurrenceInfo(GenericValue job) {
        try {
            if (job != null && !UtilValidate.isEmpty(job.getString("recurrenceInfoId"))) {
                if (job.get("cancelDateTime") != null) {
                    // cancel has been flagged, no more recurrence
                    return null;
                }
                GenericValue ri = job.getRelatedOne("RecurrenceInfo");

                if (ri != null) {
                    return new RecurrenceInfo(ri);
                } else {
                    return null;
                }
            } else {
                return null;
            }
        } catch (GenericEntityException e) {
            e.printStackTrace();
            Debug.logError(e, "Problem getting RecurrenceInfo entity from JobSandbox", module);
        } catch (RecurrenceInfoException re) {
            re.printStackTrace();
            Debug.logError(re, "Problem creating RecurrenceInfo instance: " + re.getMessage(), module);
        }
        return null;
    }

}
