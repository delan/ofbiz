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

import java.util.List;
import java.util.Map;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.collections.FlexibleMapAccessor;
import org.ofbiz.base.util.string.FlexibleStringExpander;
import org.ofbiz.minilang.MiniLangException;
import org.ofbiz.minilang.MiniLangRuntimeException;
import org.ofbiz.minilang.MiniLangUtil;
import org.ofbiz.minilang.MiniLangValidate;
import org.ofbiz.minilang.SimpleMethod;
import org.ofbiz.minilang.method.MethodContext;
import org.ofbiz.minilang.method.MethodOperation;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.ModelService;
import org.w3c.dom.Element;

/**
 * Sets all Service parameters/attributes in the to-map using the map as a source.
 */
public final class SetServiceFields extends MethodOperation {

    public static final String module = SetServiceFields.class.getName();

    // This method is needed only during the v1 to v2 transition
    private static boolean autoCorrect(Element element) {
        String errorListAttr = element.getAttribute("error-list-name");
        if (!errorListAttr.isEmpty()) {
            element.removeAttribute("error-list-name");
            return true;
        }
        return false;
    }

    private final FlexibleMapAccessor<Map<String, ? extends Object>> mapFma;
    private final FlexibleStringExpander serviceNameFse;
    private final FlexibleMapAccessor<Map<String, Object>> toMapFma;

    public SetServiceFields(Element element, SimpleMethod simpleMethod) throws MiniLangException {
        super(element, simpleMethod);
        if (MiniLangValidate.validationOn()) {
            MiniLangValidate.attributeNames(simpleMethod, element, "service-name", "map", "to-map");
            MiniLangValidate.requiredAttributes(simpleMethod, element, "service-name", "map", "to-map");
            MiniLangValidate.constantPlusExpressionAttributes(simpleMethod, element, "service-name");
            MiniLangValidate.expressionAttributes(simpleMethod, element, "map", "to-map");
            MiniLangValidate.noChildElements(simpleMethod, element);
        }
        boolean elementModified = autoCorrect(element);
        if (elementModified && MiniLangUtil.autoCorrectOn()) {
            MiniLangUtil.flagDocumentAsCorrected(element);
        }
        serviceNameFse = FlexibleStringExpander.getInstance(element.getAttribute("service-name"));
        mapFma = FlexibleMapAccessor.getInstance(element.getAttribute("map"));
        toMapFma = FlexibleMapAccessor.getInstance(element.getAttribute("to-map"));
    }

    @Override
    public boolean exec(MethodContext methodContext) throws MiniLangException {
        String serviceName = serviceNameFse.expandString(methodContext.getEnvMap());
        ModelService modelService = null;
        try {
            modelService = methodContext.getDispatcher().getDispatchContext().getModelService(serviceName);
        } catch (GenericServiceException e) {
            throw new MiniLangRuntimeException("Could not get service definition for service name \"" + serviceName + "\": " + e.getMessage(), this);
        }
        Map<String, Object> toMap = toMapFma.get(methodContext.getEnvMap());
        if (toMap == null) {
            toMap = FastMap.newInstance();
            toMapFma.put(methodContext.getEnvMap(), toMap);
        }
        Map<String, ? extends Object> fromMap = mapFma.get(methodContext.getEnvMap());
        if (fromMap == null) {
            if (Debug.verboseOn()) {
                Debug.logVerbose("The from map in set-service-field was not found with name: " + mapFma, module);
            }
            return true;
        }
        List<Object> errorMessages = FastList.newInstance();
        Map<String, Object> validAttributes = modelService.makeValid(fromMap, "IN", true, errorMessages, methodContext.getTimeZone(), methodContext.getLocale());
        if (errorMessages.size() > 0) {
            for (Object obj : errorMessages) {
                simpleMethod.addErrorMessage(methodContext, (String) obj);
            }
            throw new MiniLangRuntimeException("Errors encountered while setting service attributes for service name \"" + serviceName + "\"", this);
        }
        toMap.putAll(validAttributes);
        return true;
    }

    @Override
    public String expandedString(MethodContext methodContext) {
        return FlexibleStringExpander.expandString(toString(), methodContext.getEnvMap());
    }

    public String getServiceName() {
        return this.serviceNameFse.getOriginal();
    }

    @Override
    public String rawString() {
        return toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("<set-service-fields ");
        if (!this.serviceNameFse.isEmpty()) {
            sb.append("service-name=\"").append(this.serviceNameFse).append("\" ");
        }
        if (!this.mapFma.isEmpty()) {
            sb.append("map=\"").append(this.mapFma).append("\" ");
        }
        if (!this.toMapFma.isEmpty()) {
            sb.append("to-map=\"").append(this.toMapFma).append("\" ");
        }
        sb.append("/>");
        return sb.toString();
    }

    public static final class SetServiceFieldsFactory implements Factory<SetServiceFields> {
        public SetServiceFields createMethodOperation(Element element, SimpleMethod simpleMethod) throws MiniLangException {
            return new SetServiceFields(element, simpleMethod);
        }

        public String getName() {
            return "set-service-fields";
        }
    }
}
