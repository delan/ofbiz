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
package org.ofbiz.minilang.method.callops;

import java.io.Serializable;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilProperties;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.base.util.UtilXml;
import org.ofbiz.minilang.method.MethodContext;
import org.w3c.dom.Element;

/**
 * Simple class to wrap messages that come either from a straight string or a properties file
 */
@SuppressWarnings("serial")
public class FlexibleMessage implements Serializable {

    public static final String module = FlexibleMessage.class.getName();

    boolean isProperty = false;
    String message = null;
    String propertyResource = null;

    public FlexibleMessage(Element element, String defaultProperty) {
        String resAttr = null;
        String propAttr = null;
        String elVal = null;
        if (element != null) {
            resAttr = element.getAttribute("resource");
            propAttr = element.getAttribute("property");
            elVal = UtilXml.elementValue(element);
        }
        if (UtilValidate.isNotEmpty(resAttr)) {
            propertyResource = resAttr;
            message = propAttr;
            isProperty = true;
        } else if (UtilValidate.isNotEmpty(elVal)) {
            message = elVal;
            isProperty = false;
        } else {
            // put in default property
            propertyResource = "DefaultMessages";
            message = defaultProperty;
            isProperty = true;
        }
    }

    public String getMessage(ClassLoader loader, MethodContext methodContext) {
        String message = methodContext.expandString(this.message);
        String propertyResource = methodContext.expandString(this.propertyResource);
        if (!isProperty && message != null) {
            return message;
        } else if (isProperty && propertyResource != null && message != null) {
            String propMsg = UtilProperties.getMessage(propertyResource, message, methodContext.getEnvMap(), methodContext.getLocale());
            if (propMsg == null) {
                return "In Simple Map Processing property message could not be found in resource [" + propertyResource + "] with name [" + message + "]. ";
            } else {
                return propMsg;
            }
        } else {
            Debug.logInfo("[FlexibleMessage.getMessage] No message found, returning empty string", module);
            return "";
        }
    }
}
