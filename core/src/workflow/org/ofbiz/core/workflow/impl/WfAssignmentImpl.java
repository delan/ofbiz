/*
 * $Id$
 *
 * Copyright (c) 2001 The Open For Business Project - www.ofbiz.org
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

package org.ofbiz.core.workflow.impl;

import java.util.*;
import java.sql.Timestamp;

import org.ofbiz.core.entity.*;
import org.ofbiz.core.util.*;
import org.ofbiz.core.workflow.*;

/**
 * WfAssignmentImpl - Workflow Assignment Object implementation
 *
 *@author     <a href="mailto:jaz@zsolv.com">Andy Zeneski</a>
 *@created    December 19, 2001
 *@version    1.2
 */
public class WfAssignmentImpl implements WfAssignment {

    public static final String module = WfAssignmentImpl.class.getName();

    protected WfActivity activity;
    protected WfResource resource;
    protected Timestamp fromDate;

    /** Creates new WfAssignment
     *@param activity Sets the activity object for this assignment
     *@param resource The WfResource object this is assigned to
     *@throws WfException
     */
    public WfAssignmentImpl(WfActivity activity, WfResource resource, Timestamp fromDate) throws WfException {
        this.activity = activity;
        this.resource = resource;
        this.fromDate = fromDate;
        checkAssignment();
    }

    // makes the assignment entity
    private void checkAssignment() throws WfException {
        String workEffortId = activity.runtimeKey();
        String partyId = resource.resourcePartyId();
        String roleTypeId = resource.resourceRoleId();

        if (workEffortId == null)
            throw new WfException("WorkEffort could not be found for assignment");
        if (partyId == null && roleTypeId == null)
            throw new WfException("Both party and role type IDs cannot be null");
        if (fromDate == null)
            throw new WfException("From date cannot be null");

        GenericValue value = null;
        Map fields = new HashMap();
        fields.put("workEffortId", workEffortId);
        fields.put("partyId", partyId);
        fields.put("roleTypeId", roleTypeId);
        fields.put("fromDate", fromDate);
        fields.put("statusId", "CAL_SENT");

        // check if one exists
        try {
            if (valueObject() != null) {
                Debug.logVerbose("[WfAssignment.checkAssignment] : found existing assignment.", module);
                return;
            }
        } catch (WfException e) {
            Debug.logVerbose("[WfAssignment.checkAssignment] : no existing assignment.", module);
        }

        // check the state of the activity
        if (!activity.state().equals("open.not_running.not_started"))
            throw new WfException("Activity already running");

        // none exist; create a new one
        try {
            GenericValue v = activity.getDelegator().makeValue("WorkEffortPartyAssignment", fields);
            value = activity.getDelegator().create(v);
            Debug.logVerbose("[WfAssignment.checkAssignment] : created new party assignment.", module);
        } catch (GenericEntityException e) {
            throw new WfException(e.getMessage(), e);
        }
        if (value == null)
            throw new WfException("Could not create the assignement!");
    }

    /** Mark this assignment as accepted
     *@throws WfException
     */
    public void accept() throws WfException {
        // remove other assignments
        boolean acceptAll = activity.getDefinitionObject().get("acceptAllAssignments") != null ?
                activity.getDefinitionObject().getBoolean("acceptAllAssignments").booleanValue() : false;
        if (!acceptAll) {
            Debug.logInfo("[WfAssignment.accept] : setting other assignments to delegated status.", module);
            Iterator ai = activity.getIteratorAssignment();
            while (ai.hasNext()) {
                WfAssignment a = (WfAssignment) ai.next();
                if (!a.equals(this)) a.changeStatus("CAL_DELEGATED");
            }
        }
        changeStatus("CAL_ACCEPTED");
        try {
            activity.activate();
        } catch (CannotStart cs) {
            throw new WfException("Assignment was accepted, but activity cannot start yet.", cs);
        } catch (AlreadyRunning ar) {
            throw new WfException("Cannot accept assignment; activity has already started.", ar);
        }
        try {
            GenericValue v = activity.getRuntimeObject();
            v.set("estimatedStartDate", UtilDateTime.nowTimestamp());
            v.store();
        } catch (GenericEntityException e) {
            e.printStackTrace();
            Debug.logWarning("[WfAssignment.accept] : could not set startdate", module);
        }
    }

    /** Set the results of this assignment
     * @param Map The results of the assignement
     * @throws WfException
     */
    public void setResult(Map results) throws WfException {
        activity.setResult(results);
    }

    /** Mark this assignment as complete
     * @throws WfException
     */
    public void complete() throws WfException {
        changeStatus("CAL_COMPLETE");
        try {
            activity.complete();
        } catch (CannotComplete e) {
            throw new WfException(e.getMessage(), e);
        }
    }

    /** Change the status of this assignment
     * @param status The new status
     * @throws WfException
     */
    public void changeStatus(String status) throws WfException {
        GenericValue valueObject = valueObject();
        try {
            valueObject.set("statusId", status);
            valueObject.store();
            Debug.logInfo("[WfAssignment.changeStatus] : changed status to " + status, module);
        } catch (GenericEntityException e) {
            e.printStackTrace();
            throw new WfException(e.getMessage(), e);
        }
    }

    /** Gets the activity object of this assignment.
     * @return WfActivity The activity object of this assignment
     * @throws WfException
     */
    public WfActivity activity() throws WfException {
        return activity;
    }

    /** Gets the assignee (resource) of this assignment
     * @return WfResource The assignee of this assignment
     * @throws WfException
     */
    public WfResource assignee() throws WfException {
        return resource;
    }

    /** Sets the assignee of this assignment
     * @param newValue
     * @throws WfException
     * @throws InvalidResource
     */
    public void setAssignee(WfResource newValue) throws WfException, InvalidResource {
        remove();
        this.resource = newValue;
        this.fromDate = new Timestamp(new Date().getTime());
        checkAssignment();
    }

    /** Removes the stored data for this object
     * @throws WfException
     */
    public void remove() throws WfException {
        try {
            valueObject().remove();
        } catch (GenericEntityException e) {
            throw new WfException(e.getMessage(), e);
        }
    }

    /** Gets the status of this assignment
     * @return String status code for this assignment
     * @throws WfException
     */
    public String status() throws WfException {
        return valueObject().getString("statusId");
    }

    private GenericValue valueObject() throws WfException {
        GenericValue value = null;
        Map fields = new HashMap();
        fields.put("workEffortId", activity.runtimeKey());
        fields.put("partyId", resource.resourcePartyId());
        fields.put("roleTypeId", resource.resourceRoleId());
        fields.put("fromDate", fromDate);
        try {
            value = activity.getDelegator().findByPrimaryKey("WorkEffortPartyAssignment", fields);
        } catch (GenericEntityException e) {
            throw new WfException(e.getMessage(), e);
        }
        if (value == null)
            throw new WfException("Invalid assignment runtime entity");
        return value;
    }
}

