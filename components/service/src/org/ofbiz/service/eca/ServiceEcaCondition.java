/*
 * $Id$
 *
 * Copyright (c) 2002 The Open For Business Project - www.ofbiz.org
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
package org.ofbiz.service.eca;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.ObjectType;
import org.ofbiz.base.util.UtilMisc;

import org.w3c.dom.Element;

/**
 * ServiceEcaCondition
 *
 * @author     <a href="mailto:jaz@ofbiz.org">Andy Zeneski</a>
 * @version    $Rev$
 * @since      2.0
 */
public class ServiceEcaCondition implements java.io.Serializable {
    
    public static final String module = ServiceEcaCondition.class.getName();

    protected String conditionService = null;
    protected String lhsValueName = null;
    protected String rhsValueName = null;
    protected String lhsMapName = null;
    protected String rhsMapName = null;
    protected String operator = null;
    protected String compareType = null;
    protected String format = null;
    protected boolean isConstant = false;
    protected boolean isService = false;

    protected ServiceEcaCondition() {}

    public ServiceEcaCondition(Element condition, boolean isConstant, boolean isService) {
        if (isService) {
            this.isService = isService;
            this.conditionService = condition.getAttribute("service-name");
        } else {
            this.lhsValueName = condition.getAttribute("field-name");
            this.lhsMapName = condition.getAttribute("map-name");

            this.isConstant = isConstant;
            if (isConstant) {
                this.rhsValueName = condition.getAttribute("value");
                this.rhsMapName = null;
            } else {
                this.rhsValueName = condition.getAttribute("to-field-name");
                this.rhsMapName = condition.getAttribute("to-map-name");
            }

            this.operator = condition.getAttribute("operator");
            this.compareType = condition.getAttribute("type");
            this.format = condition.getAttribute("format");

            if (lhsValueName == null) {
                lhsValueName = "";
            }
            if (rhsValueName == null) {
                rhsValueName = "";
            }
        }
    }

    public boolean eval(String serviceName, DispatchContext dctx, Map context) throws GenericServiceException {
        if (serviceName == null || dctx == null || context == null || dctx.getClassLoader() == null) {
            throw new GenericServiceException("Cannot have null Service, Context or DispatchContext!");
        }

        if (Debug.verboseOn()) Debug.logVerbose(this.toString() + ", In the context: " + context, module);

        // condition-service; run the service and return the reply result
        if (isService) {
            LocalDispatcher dispatcher = dctx.getDispatcher();
            Map conditionServiceResult = dispatcher.runSync(conditionService,
                    UtilMisc.toMap("serviceContext", context, "serviceName", serviceName,
                            "userLogin", context.get("userLogin")));

            Boolean conditionReply = Boolean.FALSE;
            if (ServiceUtil.isError(conditionServiceResult)) {
                Debug.logError("Error in condition-service : " +
                        ServiceUtil.getErrorMessage(conditionServiceResult), module);
            } else {
                conditionReply = (Boolean) conditionServiceResult.get("conditionReply");
            }
            return conditionReply.booleanValue();
        }

        Object lhsValue = null;
        Object rhsValue = null;
        if (lhsMapName != null && lhsMapName.length() > 0) {
            try {
                if (context.containsKey(lhsMapName)) {
                    Map envMap = (Map) context.get(lhsMapName);
                    lhsValue = envMap.get(lhsValueName);
                } else {
                    Debug.logWarning("From Map (" + lhsMapName + ") not found in context, defaulting to null.", module);
                }
            } catch (ClassCastException e) {
                throw new GenericServiceException("From Map field [" + lhsMapName + "] is not a Map.", e);
            }
        } else {
            if (context.containsKey(lhsValueName)) {
                lhsValue = context.get(lhsValueName);
            } else {
                Debug.logWarning("From Field (" + lhsValueName + ") is not found in context for " + serviceName + ", defaulting to null.", module);
            }
        }

        if (isConstant) {
            rhsValue = rhsValueName;
        } else if (rhsMapName != null && rhsMapName.length() > 0) {
            try {
                if (context.containsKey(rhsMapName)) {
                    Map envMap = (Map) context.get(rhsMapName);
                    rhsValue = envMap.get(rhsValueName);
                } else {
                    Debug.logWarning("To Map (" + rhsMapName + ") not found in context for " + serviceName + ", defaulting to null.", module);
                }
            } catch (ClassCastException e) {
                throw new GenericServiceException("To Map field [" + rhsMapName + "] is not a Map.", e);
            }
        } else {
            if (context.containsKey(rhsValueName)) {
                rhsValue = context.get(rhsValueName);
            } else {
                Debug.logInfo("To Field (" + rhsValueName + ") is not found in context for " + serviceName + ", defaulting to null.", module);
            }
        }

        if (Debug.verboseOn()) Debug.logVerbose("Comparing : " + lhsValue + " " + operator + " " + rhsValue, module);

        // evaluate the condition & invoke the action(s)
        List messages = new LinkedList();
        Boolean cond = ObjectType.doRealCompare(lhsValue, rhsValue, operator, compareType, format, messages, null, dctx.getClassLoader());

        // if any messages were returned send them out
        if (messages.size() > 0) {
            Iterator m = messages.iterator();
            while (m.hasNext()) {
                Debug.logWarning((String) m.next(), module);
            }
        }
        if (cond != null) {
            return cond.booleanValue();
        } else {
            Debug.logWarning("doRealCompare returned null, returning false", module);
            return false;
        }
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();

        buf.append("[" + conditionService + "]");
        buf.append("[" + lhsMapName + "]");
        buf.append("[" + lhsValueName + "]");
        buf.append("[" + operator + "]");
        buf.append("[" + rhsMapName + "]");
        buf.append("[" + rhsValueName + "]");
        buf.append("[" + isConstant + "]");
        buf.append("[" + compareType + "]");
        buf.append("[" + format + "]");
        return buf.toString();
    }
}
