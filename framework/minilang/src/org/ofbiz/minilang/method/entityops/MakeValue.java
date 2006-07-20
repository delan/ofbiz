/*
 * Copyright 2001-2006 The Apache Software Foundation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.ofbiz.minilang.method.entityops;

import java.util.*;

import org.w3c.dom.*;
import org.ofbiz.minilang.*;
import org.ofbiz.minilang.method.*;

/**
 * Uses the delegator to find entity values by anding the map fields
 *
 * @author     <a href="mailto:jonesde@ofbiz.org">David E. Jones</a>
 * @version    $Rev$
 * @since      2.0
 */
public class MakeValue extends MethodOperation {
    
    ContextAccessor valueAcsr;
    String entityName;
    ContextAccessor mapAcsr;

    public MakeValue(Element element, SimpleMethod simpleMethod) {
        super(element, simpleMethod);
        valueAcsr = new ContextAccessor(element.getAttribute("value-name"));
        entityName = element.getAttribute("entity-name");
        mapAcsr = new ContextAccessor(element.getAttribute("map-name"));
    }

    public boolean exec(MethodContext methodContext) {
        String entityName = methodContext.expandString(this.entityName);
        Map ctxMap = (Map) (mapAcsr.isEmpty() ? null : mapAcsr.get(methodContext));
        valueAcsr.put(methodContext, methodContext.getDelegator().makeValidValue(entityName, ctxMap));
        return true;
    }

    public String rawString() {
        // TODO: something more than the empty tag
        return "<make-value/>";
    }
    public String expandedString(MethodContext methodContext) {
        // TODO: something more than a stub/dummy
        return this.rawString();
    }
}
