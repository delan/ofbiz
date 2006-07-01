/*
 * $Id: WfFactory.java 5462 2005-08-05 18:35:48Z jonesde $
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
package org.ofbiz.workflow;

import java.sql.Timestamp;
import java.util.Date;

import org.ofbiz.base.util.cache.UtilCache;
import org.ofbiz.entity.GenericDelegator;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.workflow.client.WorkflowClient;
import org.ofbiz.workflow.impl.WfActivityImpl;
import org.ofbiz.workflow.impl.WfAssignmentImpl;
import org.ofbiz.workflow.impl.WfEventAuditImpl;
import org.ofbiz.workflow.impl.WfProcessImpl;
import org.ofbiz.workflow.impl.WfProcessMgrImpl;
import org.ofbiz.workflow.impl.WfRequesterImpl;
import org.ofbiz.workflow.impl.WfResourceImpl;

/**
 * WfFactory - Workflow Factory Class
 *
 * @author     <a href="mailto:jaz@ofbiz.org">Andy Zeneski</a>
 * @version    $Rev$
 * @since      2.0
 */
public class WfFactory {
        
    public static final String module = WfFactory.class.getName();
    
    protected static UtilCache wfProcessMgrCache = new UtilCache("workflow.processmgr");
    protected static UtilCache wfClientCache = new UtilCache("workflow.client");
  
    /**
     * Creates a new {@link WfActivity} instance.
     * @param value GenericValue object defining this activity.
     * @param process The WorkEffort key of the parent process
     * @return An instance of the WfActivify Interface
     * @throws WfException
     */
    public static WfActivity getWfActivity(GenericValue value, String process) throws WfException {
        if (value == null) throw new WfException("Activity definition value object cannot be null");
        if (process == null) throw new WfException("Parent process WorkEffort key cannot be null");
        return new WfActivityImpl(value, process);                      
    }

    public static WfActivity getWfActivity(GenericDelegator delegator, String workEffortId) throws WfException {
        if (delegator == null) throw new WfException("The delegator object cannot be null");
        if (workEffortId == null) throw new WfException("The WorkEffort key cannot be null");
        return new WfActivityImpl(delegator, workEffortId);
    }

    /**
     * Creates a new {@link WfAssignment} instance.
     * @return An instance of the WfAssignment Interface
     * @throws WfException
     */
    public static WfAssignment getWfAssignment(WfActivity activity, WfResource resource, Timestamp fromDate, boolean create) throws WfException {            
        if (activity == null) throw new WfException("WfActivity cannot be null");
        if (resource == null) throw new WfException("WfResource cannot be null");
        if (fromDate == null) fromDate = new Timestamp(new Date().getTime());        
        return new WfAssignmentImpl(activity, resource, fromDate, create);        
    }

    public static WfAssignment getWfAssignment(GenericDelegator delegator, String work, String party, String role, Timestamp from) throws WfException {
        WfActivity act = getWfActivity(delegator, work);
        WfResource res = getWfResource(delegator, null, null, party, role);
        return getWfAssignment(act, res, from, false);
    }

    /** 
     * Creates a new {@link WfProcess} instance.
     * @param value The GenericValue object for the process definition.
     * @param mgr The WfProcessMgr which is managing this process.
     * @return An instance of the WfProcess Interface.
     * @throws WfException
     */
    public static WfProcess getWfProcess(GenericValue value, WfProcessMgr mgr) throws WfException {
        if (value == null) throw new WfException("Process definition value object cannot be null");
        if (mgr == null) throw new WfException("WfProcessMgr cannot be null");
        return new WfProcessImpl(value, mgr);        
    }

    public static WfProcess getWfProcess(GenericDelegator delegator, String workEffortId) throws WfException {
        if (delegator == null) throw new WfException("The delegator object cannot be null");
        if (workEffortId == null) throw new WfException("The WorkEffort key cannot be null");
        WfProcess process = null;
        try {
            process = new WfProcessImpl(delegator, workEffortId);
        } catch (WfException e) {           
            try {
                WfActivity act = WfFactory.getWfActivity(delegator, workEffortId);
                if (act != null) {
                    process = act.container();
                } else {                    
                    throw e;
                }
            } catch (WfException e2) {
                throw e;
            }
            if (process == null) {
                throw e;
            }
        }
        if (process == null) {
            throw new WfException("No process object found");
        }
        return process;        
    }

    /** 
     * Creates a new {@link WfProcessMgr} instance.
     * @param delegator The GenericDelegator to use for this manager.
     * @param pkg The Workflow Package ID.
     * @param pkver The Workflow Package Version.
     * @param pid The Workflow Process ID.
     * @param pver The Workflow Process Version.
     * @return An instance of the WfProcessMgr Interface.
     * @throws WfException
     */
    public static WfProcessMgr getWfProcessMgr(GenericDelegator delegator, String pkg, String pkver, String pid, String pver) throws WfException {
        if (delegator == null) throw new WfException("Delegator cannot be null");
        if (pkg == null) throw new WfException("Workflow package id cannot be null.");
        if (pid == null) throw new WfException("Workflow process id cannot be null");
        
        String key = delegator.getDelegatorName() + ":" + pkg + ":" + pkver + ":" + pid + ":" + pver;
        if (!wfProcessMgrCache.containsKey(key)) {
            synchronized (WfFactory.class) {
                if (!wfProcessMgrCache.containsKey(key)) {                
                    wfProcessMgrCache.put(key, new WfProcessMgrImpl(delegator, pkg, pkver, pid, pver));
                }
            }
        }
        return (WfProcessMgr) wfProcessMgrCache.get(key);                
    }

    /** 
     * Creates a new {@link WfRequester} instance.
     * @return An instance of the WfRequester Interface.
     * @throws WfException
     */
    public static WfRequester getWfRequester() throws WfException {
        return new WfRequesterImpl();
    }

    /** 
     * Creates a new {@link WfResource} instance.
     * @param value The GenericValue object of the WorkflowParticipant
     * @throws WfException
     * @return An instance of the WfResource Interface.
     */
    public static WfResource getWfResource(GenericValue value) throws WfException {
        if (value == null) throw new WfException("Value object for WfResource definition cannot be null");
        return new WfResourceImpl(value);        
    }

    /** 
     * Creates a new {@link WfResource} instance.
     * @param delegator The GenericDelegator for this instance
     * @param key The key for the resource
     * @param name The name of the resource
     * @param party The partyId of the resource
     * @param role The roleTypeId of the resource
     * @return An instance of the WfResource Interface.
     * @throws WfException
     */
    public static WfResource getWfResource(GenericDelegator delegator, String key, String name, String party, String role) throws WfException {
        if (delegator == null) throw new WfException("Delegator cannot be null");        
        if (party == null) party = "_NA_";
        if (role == null) role = "_NA_";
        return new WfResourceImpl(delegator, key, name, party, role);        
    }

    /** 
     * Creates a new {@link WfEventAudit} instance.
     * @return An instance of the WfEventAudit Interface.
     * @throws WfException
     */
    public static WfEventAudit getWfEventAudit(WfExecutionObject object, String type) throws WfException {
        return new WfEventAuditImpl(object, type);
    }

    public static WorkflowClient getClient(DispatchContext dctx) {
        if (!wfClientCache.containsKey(dctx)) {
            synchronized (WfFactory.class) {
                if (!wfClientCache.containsKey(dctx))
                wfClientCache.put(dctx, new WorkflowClient(dctx));
            }
        }
        return (WorkflowClient) wfClientCache.get(dctx);
    }

}
