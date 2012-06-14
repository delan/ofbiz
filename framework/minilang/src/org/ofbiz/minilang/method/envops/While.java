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

import org.ofbiz.base.util.UtilXml;
import org.ofbiz.minilang.MiniLangException;
import org.ofbiz.minilang.MiniLangValidate;
import org.ofbiz.minilang.SimpleMethod;
import org.ofbiz.minilang.artifact.ArtifactInfoContext;
import org.ofbiz.minilang.method.MethodContext;
import org.ofbiz.minilang.method.MethodOperation;
import org.ofbiz.minilang.method.conditional.Conditional;
import org.ofbiz.minilang.method.conditional.ConditionalFactory;
import org.ofbiz.minilang.method.envops.Break.BreakElementException;
import org.ofbiz.minilang.method.envops.Continue.ContinueElementException;
import org.w3c.dom.Element;

/**
 * Continually processes sub-ops while the condition remains true
 */
public final class While extends MethodOperation {

    private final Conditional condition;
    private final List<MethodOperation> thenSubOps;

    public While(Element element, SimpleMethod simpleMethod) throws MiniLangException {
        super(element, simpleMethod);
        if (MiniLangValidate.validationOn()) {
            MiniLangValidate.childElements(simpleMethod, element, "condition", "then");
            MiniLangValidate.requiredChildElements(simpleMethod, element, "condition", "then");
        }
        Element conditionElement = UtilXml.firstChildElement(element, "condition");
        Element conditionChildElement = UtilXml.firstChildElement(conditionElement);
        this.condition = ConditionalFactory.makeConditional(conditionChildElement, simpleMethod);
        Element thenElement = UtilXml.firstChildElement(element, "then");
        this.thenSubOps = Collections.unmodifiableList(SimpleMethod.readOperations(thenElement, simpleMethod));
    }

    @Override
    public boolean exec(MethodContext methodContext) throws MiniLangException {
        while (condition.checkCondition(methodContext)) {
            try {
                for (MethodOperation methodOperation : thenSubOps) {
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
        for (MethodOperation method : this.thenSubOps) {
            method.gatherArtifactInfo(aic);
        }
    }

    @Override
    public String toString() {
        StringBuilder messageBuf = new StringBuilder();
        this.condition.prettyPrint(messageBuf, null);
        return "<while><condition>" + messageBuf + "</condition></while>";
    }

    public static final class WhileFactory implements Factory<While> {
        public While createMethodOperation(Element element, SimpleMethod simpleMethod) throws MiniLangException {
            return new While(element, simpleMethod);
        }

        public String getName() {
            return "while";
        }
    }
}
