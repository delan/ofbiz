/*
 * $Id: WfEventAuditImpl.java,v 1.2 2003/08/18 18:32:07 ajzeneski Exp $
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
package org.ofbiz.workflow.impl;

import java.sql.Timestamp;
import java.util.Date;

import org.ofbiz.base.util.ObjectType;
import org.ofbiz.workflow.SourceNotAvailable;
import org.ofbiz.workflow.WfActivity;
import org.ofbiz.workflow.WfEventAudit;
import org.ofbiz.workflow.WfException;
import org.ofbiz.workflow.WfExecutionObject;
import org.ofbiz.workflow.WfProcess;

/**
 * WfEventAuditImpl - Workflow Event Audit implementation
 *
 * @author     <a href="mailto:jaz@ofbiz.org">Andy Zeneski</a>
 * @version    $Revision: 1.2 $
 * @since      2.0
 */
public class WfEventAuditImpl implements WfEventAudit {

    private WfExecutionObject object = null;
    private String eventType = null;
    private Timestamp timeStamp = null;

    public WfEventAuditImpl(WfExecutionObject object, String eventType) {
        this.object = object;
        this.eventType = eventType;
        this.timeStamp = new Timestamp(new Date().getTime());
    }
    
    /**
     * @see org.ofbiz.core.workflow.WfEventAudit#source()
     */
    public WfExecutionObject source() throws WfException, SourceNotAvailable {
        return object;
    }
 
    /**
     * @see org.ofbiz.core.workflow.WfEventAudit#timeStamp()
     */
    public Timestamp timeStamp() throws WfException {
        return timeStamp;
    }

    /**
     * @see org.ofbiz.core.workflow.WfEventAudit#eventType()
     */
    public String eventType() throws WfException {
        return eventType;
    }

    /**
     * @see org.ofbiz.core.workflow.WfEventAudit#activityKey()
     */
    public String activityKey() throws WfException {
        try {
            if (ObjectType.instanceOf(object, "org.ofbiz.workflow.WfActivity"))
                return object.key();
        } catch (Exception e) {
            throw new WfException("Source is not a WfActivity object");
        }
        throw new WfException("Source is not a WfActivity object");
    }

    /**
     * @see org.ofbiz.core.workflow.WfEventAudit#activityName()
     */
    public String activityName() throws WfException {
        try {
            if (ObjectType.instanceOf(object, "org.ofbiz.workflow.WfActivity"))
                return object.name();
        } catch (Exception e) {}
        throw new WfException("Source is not a WfActivity object");

    }

    /**
     * @see org.ofbiz.core.workflow.WfEventAudit#processKey()
     */
    public String processKey() throws WfException {
        try {
            if (ObjectType.instanceOf(object, "org.ofbiz.workflow.WfProcess"))
                return object.key();
        } catch (Exception e) {}
        throw new WfException("Source is not a WfProcess object");

    }

    /**
     * @see org.ofbiz.core.workflow.WfEventAudit#processName()
     */
    public String processName() throws WfException {
        try {
            if (ObjectType.instanceOf(object, "org.ofbiz.workflow.WfProcess"))
                return object.name();
        } catch (Exception e) {}
        throw new WfException("Source is not a WfProcess object");

    }

    /**
     * @see org.ofbiz.core.workflow.WfEventAudit#processMgrName()
     */
    public String processMgrName() throws WfException {
        try {
            if (ObjectType.instanceOf(object, "org.ofbiz.workflow.WfProcess"))
                return ((WfProcess) object).manager().name();
            else if (ObjectType.instanceOf(object, "org.ofbiz.workflow.WfActivity"))
                return ((WfActivity) object).container().manager().name();
        } catch (Exception e) {}
        throw new WfException("Illegal source object");
    }

    /**
     * @see org.ofbiz.core.workflow.WfEventAudit#processMgrVersion()
     */
    public String processMgrVersion() throws WfException {
        try {
            if (ObjectType.instanceOf(object, "org.ofbiz.workflow.WfProcess"))
                return ((WfProcess) object).manager().version();
            else if (ObjectType.instanceOf(object, "org.ofbiz.workflow.WfActivity"))
                return ((WfActivity) object).container().manager().version();
        } catch (Exception e) {}
        throw new WfException("Illegal source object");
    }
}

