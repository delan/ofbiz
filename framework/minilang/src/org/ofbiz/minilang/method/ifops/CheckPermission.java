/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/
package org.ofbiz.minilang.method.ifops;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javolution.util.FastList;

import org.ofbiz.base.util.UtilXml;
import org.ofbiz.base.util.collections.FlexibleMapAccessor;
import org.ofbiz.base.util.string.FlexibleStringExpander;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.minilang.MiniLangException;
import org.ofbiz.minilang.MiniLangValidate;
import org.ofbiz.minilang.SimpleMethod;
import org.ofbiz.minilang.method.MessageElement;
import org.ofbiz.minilang.method.MethodContext;
import org.ofbiz.minilang.method.MethodOperation;
import org.ofbiz.security.Security;
import org.ofbiz.security.authz.Authorization;
import org.w3c.dom.Element;

/**
 * Implements the &lt;check-permission&gt; element.
 */
public final class CheckPermission extends MethodOperation {

    private final List<PermissionInfo> altPermissionInfoList;
    private final FlexibleMapAccessor<List<String>> errorListFma;
    private final MessageElement messageElement;
    private final PermissionInfo primaryPermissionInfo;

    public CheckPermission(Element element, SimpleMethod simpleMethod) throws MiniLangException {
        super(element, simpleMethod);
        if (MiniLangValidate.validationOn()) {
            MiniLangValidate.attributeNames(simpleMethod, element, "permission", "action", "error-list-name");
            MiniLangValidate.constantAttributes(simpleMethod, element, "error-list-name");
            MiniLangValidate.childElements(simpleMethod, element, "alt-permission", "fail-message", "fail-property");
            MiniLangValidate.requireAnyChildElement(simpleMethod, element, "fail-message", "fail-property");
        }
        errorListFma = FlexibleMapAccessor.getInstance(MiniLangValidate.checkAttribute(element.getAttribute("error-list-name"), "error_list"));
        primaryPermissionInfo = new PermissionInfo(element);
        List<? extends Element> altPermElements = UtilXml.childElementList(element, "alt-permission");
        if (!altPermElements.isEmpty()) {
            List<PermissionInfo> permissionInfoList = new ArrayList<PermissionInfo>(altPermElements.size());
            for (Element altPermElement : altPermElements) {
                permissionInfoList.add(new PermissionInfo(altPermElement));
            }
            altPermissionInfoList = Collections.unmodifiableList(permissionInfoList);
        } else {
            altPermissionInfoList = null;
        }
        messageElement = MessageElement.fromParentElement(element, simpleMethod);
    }

    @Override
    public boolean exec(MethodContext methodContext) throws MiniLangException {
        boolean hasPermission = false;
        GenericValue userLogin = methodContext.getUserLogin();
        if (userLogin != null) {
            Authorization authz = methodContext.getAuthz();
            Security security = methodContext.getSecurity();
            hasPermission = this.primaryPermissionInfo.hasPermission(methodContext, userLogin, authz, security);
            if (!hasPermission && altPermissionInfoList != null) {
                for (PermissionInfo altPermInfo : altPermissionInfoList) {
                    if (altPermInfo.hasPermission(methodContext, userLogin, authz, security)) {
                        hasPermission = true;
                        break;
                    }
                }
            }
        }
        if (!hasPermission && messageElement != null) {
            List<String> messages = errorListFma.get(methodContext.getEnvMap());
            if (messages == null) {
                messages = FastList.newInstance();
                errorListFma.put(methodContext.getEnvMap(), messages);
            }
            messages.add(messageElement.getMessage(methodContext));
        }
        return true;
    }

    @Override
    public String expandedString(MethodContext methodContext) {
        return FlexibleStringExpander.expandString(toString(), methodContext.getEnvMap());
    }

    @Override
    public String rawString() {
        return toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("<check-permission ");
        sb.append("permission=\"").append(this.primaryPermissionInfo.permissionFse).append("\" ");
        if (!this.primaryPermissionInfo.actionFse.isEmpty()) {
            sb.append("action=\"").append(this.primaryPermissionInfo.actionFse).append("\" ");
        }
        if (!"error_list".equals(this.errorListFma.getOriginalName())) {
            sb.append("error-list-name=\"").append(this.errorListFma).append("\" ");
        }
        if (messageElement != null) {
            sb.append(">").append(messageElement).append("</check-permission>");
        } else {
            sb.append("/>");
        }
        return sb.toString();
    }

    /**
     * A &lt;check-permission&gt; element factory. 
     */
    public static final class CheckPermissionFactory implements Factory<CheckPermission> {
        @Override
        public CheckPermission createMethodOperation(Element element, SimpleMethod simpleMethod) throws MiniLangException {
            return new CheckPermission(element, simpleMethod);
        }

        @Override
        public String getName() {
            return "check-permission";
        }
    }

    private class PermissionInfo {
        private final FlexibleStringExpander actionFse;
        private final FlexibleStringExpander permissionFse;

        private PermissionInfo(Element element) throws MiniLangException {
            if (MiniLangValidate.validationOn()) {
                MiniLangValidate.attributeNames(simpleMethod, element, "permission", "action");
                MiniLangValidate.requiredAttributes(simpleMethod, element, "permission");
            }
            this.permissionFse = FlexibleStringExpander.getInstance(element.getAttribute("permission"));
            this.actionFse = FlexibleStringExpander.getInstance(element.getAttribute("action"));
        }

        private boolean hasPermission(MethodContext methodContext, GenericValue userLogin, Authorization authz, Security security) {
            String permission = permissionFse.expandString(methodContext.getEnvMap());
            String action = actionFse.expandString(methodContext.getEnvMap());
            if (!action.isEmpty()) {
                // run hasEntityPermission
                return security.hasEntityPermission(permission, action, userLogin);
            } else {
                // run hasPermission
                return authz.hasPermission(userLogin.getString("userLoginId"), permission, methodContext.getEnvMap());
            }
        }
    }
}
