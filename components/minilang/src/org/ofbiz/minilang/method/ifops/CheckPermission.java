/*
 * $Id: CheckPermission.java,v 1.3 2003/11/25 06:05:36 jonesde Exp $
 *
 *  Copyright (c) 2001, 2002 The Open For Business Project - www.ofbiz.org
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a
 *  copy of this software and associated documentation files (the "Software"),
 *  to deal in the Software without restriction, including without limitation
 *  the rights to use, copy, modify, merge, publish, distribute, sublicense,
 *  and/or sell copies of the Software, and to permit persons to whom the
 *  Software is furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included
 *  in all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 *  OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 *  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 *  IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 *  CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT
 *  OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR
 *  THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.ofbiz.minilang.method.ifops;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.ofbiz.base.util.UtilProperties;
import org.ofbiz.base.util.UtilXml;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.minilang.SimpleMethod;
import org.ofbiz.minilang.method.ContextAccessor;
import org.ofbiz.minilang.method.MethodContext;
import org.ofbiz.minilang.method.MethodOperation;
import org.ofbiz.security.Security;
import org.w3c.dom.Element;

/**
 * Iff the user does not have the specified permission the fail-message 
 * or fail-property sub-elements are used to add a message to the error-list.
 *
 * @author     <a href="mailto:jonesde@ofbiz.org">David E. Jones</a>
 * @version    $Revision: 1.3 $
 * @since      2.0
 */
public class CheckPermission extends MethodOperation {
    
    String message = null;
    String propertyResource = null;
    boolean isProperty = false;

    PermissionInfo permissionInfo;
    ContextAccessor errorListAcsr;
    List altPermissions = null;

    public CheckPermission(Element element, SimpleMethod simpleMethod) {
        super(element, simpleMethod);
        permissionInfo = new PermissionInfo(element);
        this.errorListAcsr = new ContextAccessor(element.getAttribute("error-list-name"), "error_list");

        List altPermElements = UtilXml.childElementList(element, "alt-permission");
        Iterator apeIter = altPermElements.iterator();
        if (apeIter.hasNext()) {
            altPermissions = new LinkedList();
        }
        while (apeIter.hasNext()) {
            Element altPermElement = (Element) apeIter.next();
            altPermissions.add(new PermissionInfo(altPermElement));
        }

        Element failMessage = UtilXml.firstChildElement(element, "fail-message");
        Element failProperty = UtilXml.firstChildElement(element, "fail-property");
        if (failMessage != null) {
            this.message = failMessage.getAttribute("message");
            this.isProperty = false;
        } else if (failProperty != null) {
            this.propertyResource = failProperty.getAttribute("resource");
            this.message = failProperty.getAttribute("property");
            this.isProperty = true;
        }
    }

    public boolean exec(MethodContext methodContext) {
        boolean hasPermission = false;

        List messages = (List) errorListAcsr.get(methodContext);
        if (messages == null) {
            messages = new LinkedList();
            errorListAcsr.put(methodContext, messages);
        }

        // if no user is logged in, treat as if the user does not have permission: do not run subops
        GenericValue userLogin = methodContext.getUserLogin();
        if (userLogin != null) {
            Security security = methodContext.getSecurity();
            if (this.permissionInfo.hasPermission(methodContext, userLogin, security)) {
                hasPermission = true;
            }
            
            // if failed, check alternate permissions
            if (!hasPermission && altPermissions != null) {
                Iterator altPermIter = altPermissions.iterator();
                while (altPermIter.hasNext()) {
                    PermissionInfo altPermInfo = (PermissionInfo) altPermIter.next();
                    if (altPermInfo.hasPermission(methodContext, userLogin, security)) {
                        hasPermission = true;
                        break;
                    }
                }
            }
        }
        
        if (!hasPermission) {
            this.addMessage(messages, methodContext);
        }

        return true;
    }

    public void addMessage(List messages, MethodContext methodContext) {
        ClassLoader loader = methodContext.getLoader();
        
        String message = methodContext.expandString(this.message);
        String propertyResource = methodContext.expandString(this.propertyResource);        
        
        if (!isProperty && message != null) {
            messages.add(message);
            // if (Debug.infoOn()) Debug.logInfo("[SimpleMapOperation.addMessage] Adding message: " + message, module);
        } else if (isProperty && propertyResource != null && message != null) {
            //String propMsg = UtilProperties.getPropertyValue(UtilURL.fromResource(propertyResource, loader), message);
            String propMsg = UtilProperties.getMessage(propertyResource, message, methodContext.getEnvMap(), methodContext.getLocale());
            if (propMsg == null || propMsg.length() == 0) {
                messages.add("Simple Method Permission error occurred, but no message was found, sorry.");
            } else {
                messages.add(methodContext.expandString(propMsg));
            }
            // if (Debug.infoOn()) Debug.logInfo("[SimpleMapOperation.addMessage] Adding property message: " + propMsg, module);
        } else {
            messages.add("Simple Method Permission error occurred, but no message was found, sorry.");
            // if (Debug.infoOn()) Debug.logInfo("[SimpleMapOperation.addMessage] ERROR: No message found", module);
        }
    }
    
    public static class PermissionInfo {
        String permission;
        String action;
        
        public PermissionInfo(Element altPermissionElement) {
            this.permission = altPermissionElement.getAttribute("permission");
            this.action = altPermissionElement.getAttribute("action");
        }
        
        public boolean hasPermission(MethodContext methodContext, GenericValue userLogin, Security security) {
            String permission = methodContext.expandString(this.permission);
            String action = methodContext.expandString(this.action);
            
            if (action != null && action.length() > 0) {
                // run hasEntityPermission
                return security.hasEntityPermission(permission, action, userLogin);
            } else {
                // run hasPermission
                return security.hasPermission(permission, userLogin);
            }
        }
    }
}
