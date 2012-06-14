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
package org.ofbiz.minilang.method.envops;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.collections.FlexibleMapAccessor;
import org.ofbiz.minilang.MiniLangException;
import org.ofbiz.minilang.MiniLangRuntimeException;
import org.ofbiz.minilang.MiniLangValidate;
import org.ofbiz.minilang.SimpleMethod;
import org.ofbiz.minilang.artifact.ArtifactInfoContext;
import org.ofbiz.minilang.method.MethodContext;
import org.ofbiz.minilang.method.MethodOperation;
import org.ofbiz.minilang.method.envops.Break.BreakElementException;
import org.ofbiz.minilang.method.envops.Continue.ContinueElementException;
import org.w3c.dom.Element;

/**
 * Process sub-operations for each entry in the map
 */
public final class IterateMap extends MethodOperation {

    public static final String module = IterateMap.class.getName();

    private final FlexibleMapAccessor<Object> keyFma;
    private final FlexibleMapAccessor<Map<? extends Object, ? extends Object>> mapFma;
    private final List<MethodOperation> subOps;
    private final FlexibleMapAccessor<Object> valueFma;

    public IterateMap(Element element, SimpleMethod simpleMethod) throws MiniLangException {
        super(element, simpleMethod);
        if (MiniLangValidate.validationOn()) {
            MiniLangValidate.attributeNames(simpleMethod, element, "key", "map", "value");
            MiniLangValidate.requiredAttributes(simpleMethod, element, "key", "map", "value");
            MiniLangValidate.expressionAttributes(simpleMethod, element, "key", "map", "value");
        }
        this.keyFma = FlexibleMapAccessor.getInstance(element.getAttribute("key"));
        this.mapFma = FlexibleMapAccessor.getInstance(element.getAttribute("map"));
        this.valueFma = FlexibleMapAccessor.getInstance(element.getAttribute("value"));
        this.subOps = Collections.unmodifiableList(SimpleMethod.readOperations(element, simpleMethod));
    }

    @Override
    public boolean exec(MethodContext methodContext) throws MiniLangException {
        if (mapFma.isEmpty()) {
            throw new MiniLangRuntimeException("No map specified.", this);
        }
        Object oldKey = keyFma.get(methodContext.getEnvMap());
        Object oldValue = valueFma.get(methodContext.getEnvMap());
        if (oldKey != null) {
            if (Debug.verboseOn())
                Debug.logVerbose("In iterate-map the key had a non-null value before entering the loop for the operation: " + this, module);
        }
        if (oldValue != null) {
            if (Debug.verboseOn())
                Debug.logVerbose("In iterate-map the value had a non-null value before entering the loop for the operation: " + this, module);
        }
        Map<? extends Object, ? extends Object> theMap = mapFma.get(methodContext.getEnvMap());
        if (theMap == null) {
            if (Debug.verboseOn())
                Debug.logVerbose("Map not found with name " + mapFma + ", doing nothing: " + this, module);
            return true;
        }
        if (theMap.size() == 0) {
            if (Debug.verboseOn())
                Debug.logVerbose("Map with name " + mapFma + " has zero entries, doing nothing: " + this, module);
            return true;
        }
        for (Map.Entry<? extends Object, ? extends Object> theEntry : theMap.entrySet()) {
            keyFma.put(methodContext.getEnvMap(), theEntry.getKey());
            valueFma.put(methodContext.getEnvMap(), theEntry.getValue());
            try {
                for (MethodOperation methodOperation : subOps) {
                    if (!methodOperation.exec(methodContext)) {
                        return false;
                    }
                }
            } catch (MiniLangException e) {
                if (e instanceof BreakElementException) {
                    break;
                }
                if (e instanceof ContinueElementException) {
                    continue;
                }
                throw e;
            }
        }
        return true;
    }

    @Override
    public void gatherArtifactInfo(ArtifactInfoContext aic) {
        for (MethodOperation method : this.subOps) {
            method.gatherArtifactInfo(aic);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("<iterate-map ");
        if (!this.mapFma.isEmpty()) {
            sb.append("map=\"").append(this.mapFma).append("\" ");
        }
        if (!this.keyFma.isEmpty()) {
            sb.append("key=\"").append(this.keyFma).append("\" ");
        }
        if (!this.valueFma.isEmpty()) {
            sb.append("value=\"").append(this.valueFma).append("\" ");
        }
        sb.append("/>");
        return sb.toString();
    }

    public static final class IterateMapFactory implements Factory<IterateMap> {
        public IterateMap createMethodOperation(Element element, SimpleMethod simpleMethod) throws MiniLangException {
            return new IterateMap(element, simpleMethod);
        }

        public String getName() {
            return "iterate-map";
        }
    }
}
