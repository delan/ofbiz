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
package org.ofbiz.service.config.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ofbiz.base.lang.ThreadSafe;
import org.ofbiz.base.util.UtilXml;
import org.ofbiz.service.config.ServiceConfigException;
import org.w3c.dom.Element;

/**
 * An object that models the <code>&lt;engine&gt;</code> element.
 */
@ThreadSafe
public final class Engine {

    private final String className;
    private final String name;
    private final List<Parameter> parameters;

    Engine(Element engineElement) throws ServiceConfigException {
        String name = engineElement.getAttribute("name").intern();
        if (name.isEmpty()) {
            throw new ServiceConfigException("<engine> element name attribute is empty");
        }
        this.name = name;
        String className = engineElement.getAttribute("class").intern();
        if (className.isEmpty()) {
            throw new ServiceConfigException("<engine> element class attribute is empty");
        }
        this.className = className;
        List<? extends Element> parameterElementList = UtilXml.childElementList(engineElement, "parameter");
        if (parameterElementList.isEmpty()) {
            this.parameters = Collections.emptyList();
        } else {
            List<Parameter> parameters = new ArrayList<Parameter>(parameterElementList.size());
            for (Element parameterElement : parameterElementList) {
                parameters.add(new Parameter(parameterElement));
            }
            this.parameters = Collections.unmodifiableList(parameters);
        }
    }

    public String getClassName() {
        return className;
    }

    public String getName() {
        return name;
    }

    public List<Parameter> getParameters() {
        return this.parameters;
    }
}
