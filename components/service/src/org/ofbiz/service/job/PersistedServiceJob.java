/*
 * $Id: PersistedServiceJob.java,v 1.11 2004/08/09 23:52:26 jonesde Exp $
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
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilProperties;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.GenericDelegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.serialize.SerializeException;
import org.ofbiz.entity.serialize.XmlSerializer;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericRequester;
import org.ofbiz.service.calendar.RecurrenceInfo;
import org.ofbiz.service.config.ServiceConfigUtil;
import org.xml.sax.SAXException;

/**
 * Entity Service Job - Store => Schedule => Run
 *
 * @author     <a href="mailto:jaz@ofbiz.org">Andy Zeneski</a>
 * @version    $Revision: 1.11 $
 * @since      2.0
 */
public class PersistedServiceJob extends GenericServiceJob {

    public static final String module = PersistedServiceJob.class.getName();

    private transient GenericDelegator delegator = null;
    private Timestamp storedDate = null;
    private long nextRecurrence = -1;
    private long maxRetry = -1;

    /**
     * Creates a new PersistedServiceJob
     * @param dctx
     * @param jobValue
     * @param req
     */
    public PersistedServiceJob(DispatchContext dctx, GenericValue jobValue, GenericRequester req) {
        super(jobValue.getString("jobId"), jobValue.getString("jobName"));
        this.delegator = dctx.getDelegator();
        this.requester = req;
        this.dctx = dctx;
        this.storedDate = jobValue.getTimestamp("runTime");
        this.runtime = storedDate.getTime();
        this.maxRetry = jobValue.get("maxRetry") != null ? jobValue.getLong("maxRetry").longValue() : -1;

        // set the start time to now
        String instanceId = UtilProperties.getPropertyValue("general.properties", "unique.instanceId", "ofbiz0");
        jobValue.set("startDateTime", UtilDateTime.nowTimestamp());
        jobValue.set("statusId", "SERVICE_RUNNING");
        jobValue.set("runByInstanceId", instanceId);
        try {
            jobValue.store();
        } catch (GenericEntityException e) {
            Debug.logError(e, "Unable to set the startDateTime on the current job [" + getJobId() + "]; not running!");
            runtime = -1;
        }
    }

    /**
     * @see org.ofbiz.service.job.GenericServiceJob#init()
     */
    protected void init() {
        super.init();

        // configure any addition recurrences
        GenericValue job = this.getJob();
        RecurrenceInfo recurrence = JobManager.getRecurrenceInfo(job);

        try {
            if (recurrence != null) {
                recurrence.incrementCurrentCount();
                long next = recurrence.next();
                createRecurrence(job, next);
            }
        } catch (GenericEntityException e) {
            throw new RuntimeException(e.getMessage());
        }
        if (Debug.infoOn()) Debug.logInfo(this.toString() + "[" + getJobId() + "] -- Next runtime: " + nextRecurrence, module);
    }

    private void createRecurrence(GenericValue job, long next) throws GenericEntityException {
        if (Debug.verboseOn()) Debug.logVerbose("Next runtime returned: " + next, module);

        if (next > runtime) {
            String newJobId = job.getDelegator().getNextSeqId("JobSandbox").toString();
            String pJobId = job.getString("parentJobId");
            if (pJobId == null) {
                pJobId = job.getString("jobId");
            }
            GenericValue newJob = new GenericValue(job);
            newJob.set("jobId", newJobId);
            newJob.set("previousJobId", job.getString("jobId"));
            newJob.set("parentJobId", pJobId);
            newJob.set("statusId", "SERVICE_PENDING");
            newJob.set("startDateTime", null);
            newJob.set("runTime", new java.sql.Timestamp(next));
            nextRecurrence = next;
            delegator.create(newJob);
            if (Debug.verboseOn()) Debug.logVerbose("Created next job entry: " + newJob, module);
        }
    }

    /**
     * @see org.ofbiz.service.job.GenericServiceJob#finish()
     */
    protected void finish() {
        super.finish();

        // set the finish date
        GenericValue job = getJob();
        String status = job.getString("statusId");
        if (status == null || "SERVICE_RUNNING".equals(status)) {
            job.set("statusId", "SERVICE_FINISHED");
        }
        job.set("finishDateTime", UtilDateTime.nowTimestamp());
        try {
            job.store();
        } catch (GenericEntityException e) {
            Debug.logError(e, "Cannot update the job [" + getJobId() + "] sandbox", module);
        }
    }

    /**
     * @see org.ofbiz.service.job.GenericServiceJob#failed(Throwable)
     */
    protected void failed(Throwable t) {
        super.failed(t);

        // if the job has not been re-scheduled; we need to re-schedule and run again
        if (nextRecurrence == -1) {
            if (this.canRetry()) {
                // create a recurrence
                Calendar cal = Calendar.getInstance();
                cal.setTime(new Date());
                cal.add(Calendar.MINUTE, ServiceConfigUtil.getFailedRetryMin());
                long next = cal.getTimeInMillis();
                GenericValue job = getJob();
                try {
                    createRecurrence(job, next);
                } catch (GenericEntityException gee) {
                    Debug.logError(gee, "ERROR: Unable to re-schedule job [" + getJobId() + "] to re-run : " + job, module);
                }

                // set the failed status
                job.set("statusId", "SERVICE_FAILED");
                try {
                    job.store();
                } catch (GenericEntityException e) {
                    Debug.logError(e, "Cannot update the job sandbox", module);
                }
                Debug.log("Persisted Job [" + getJobId() + "] Failed Re-Scheduling : " + next, module);
            } else {
                Debug.logWarning("Persisted Job [" + getJobId() + "] Failed - Max Retry Hit; not re-scheduling", module);
            }
        }
    }

    /**
     * @see org.ofbiz.service.job.GenericServiceJob#getServiceName()
     */
    protected String getServiceName() {
        GenericValue jobObj = getJob();
        if (jobObj == null || jobObj.get("serviceName") == null) {
            return null;
        }
        return jobObj.getString("serviceName");
    }

    /**
     * @see org.ofbiz.service.job.GenericServiceJob#getContext()
     */
    protected Map getContext() {
        Map context = null;
        try {
            GenericValue jobObj = getJob();
            if (!UtilValidate.isEmpty(jobObj.getString("runtimeDataId"))) {
                GenericValue contextObj = jobObj.getRelatedOne("RuntimeData");
                if (contextObj != null) {
                    context = (Map) XmlSerializer.deserialize(contextObj.getString("runtimeInfo"), delegator);
                }
            }

            if (context == null) {
                context = new HashMap();
            }

            // check the runAsUser
            if (!UtilValidate.isEmpty(jobObj.getString("runAsUser"))) {
                GenericValue runAsUser = jobObj.getRelatedOne("RunAsUserLogin");
                if (runAsUser != null) {
                    context.put("userLogin", runAsUser);
                }
            }
        } catch (GenericEntityException e) {
            Debug.logError(e, "PersistedServiceJob.getContext(): Entity Exception", module);
        } catch (SerializeException e) {
            Debug.logError(e, "PersistedServiceJob.getContext(): Serialize Exception", module);
        } catch (ParserConfigurationException e) {
            Debug.logError(e, "PersistedServiceJob.getContext(): Parse Exception", module);
        } catch (SAXException e) {
            Debug.logError(e, "PersistedServiceJob.getContext(): SAXException", module);
        } catch (IOException e) {
            Debug.logError(e, "PersistedServiceJob.getContext(): IOException", module);
        }
        if (context == null) {
            Debug.logError("Job context is null", module);
        }
        
        return context;
    }

    // gets the job value object
    private GenericValue getJob() {
        try {
            Map fields = UtilMisc.toMap("jobId", getJobId());
            GenericValue jobObj = delegator.findByPrimaryKey("JobSandbox", fields);

            if (jobObj == null)
                Debug.logError("Job [" + getJobId() + "]came back null from datasource", module);
            return jobObj;
        } catch (GenericEntityException e) {
            Debug.logError(e, "Cannot get job definition [" + getJobId() + "] from entity", module);
            //e.printStackTrace();
        }
        return null;
    }

    // returns the number of current retries
    private long getRetries() {
        GenericValue job = this.getJob();
        String pJobId = job.getString("parentJobId");
        if (pJobId == null) {
            return 0;
        }

        Map fields = UtilMisc.toMap("parentJobId", pJobId, "statusId", "SERVICE_FAILED");
        long count = 0;
        try {
            count = delegator.findCountByAnd("JobSandbox", fields);
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
        }

        return count + 1; // add one for the parent
    }

    private boolean canRetry() {
        if (maxRetry == -1) {
            return true;
        }
        if (this.getRetries() < maxRetry) {
            return true;
        }
        return false;
    }
}
