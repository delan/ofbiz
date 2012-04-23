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

import java.util.List;

import javolution.util.FastList;

import org.ofbiz.base.util.Debug;
import org.ofbiz.minilang.MiniLangException;
import org.ofbiz.minilang.SimpleMethod;
import org.ofbiz.minilang.method.ContextAccessor;
import org.ofbiz.minilang.method.MethodContext;
import org.ofbiz.minilang.method.MethodOperation;
import org.ofbiz.minilang.method.envops.Break.BreakElementException;
import org.ofbiz.minilang.method.envops.Continue.ContinueElementException;
import org.w3c.dom.Element;

/**
 * Loop
 */
public class Loop extends MethodOperation {

    public static final String module = Loop.class.getName();

    protected String countStr;
    protected ContextAccessor<Integer> fieldAcsr;
    protected List<MethodOperation> subOps = FastList.newInstance();

    public Loop(Element element, SimpleMethod simpleMethod) throws MiniLangException {
        super(element, simpleMethod);
        this.fieldAcsr = new ContextAccessor<Integer>(element.getAttribute("field"));
        this.countStr = element.getAttribute("count");
        SimpleMethod.readOperations(element, subOps, simpleMethod);
    }

    @Override
    public boolean exec(MethodContext methodContext) throws MiniLangException {
        String countStrExp = methodContext.expandString(this.countStr);
        int count = 0;
        try {
            Double ctDbl = Double.valueOf(countStrExp);
            if (ctDbl != null) {
                count = ctDbl.intValue();
            }
        } catch (NumberFormatException e) {
            Debug.logError(e, module);
            return false;
        }
        if (count < 0) {
            Debug.logWarning("Unable to execute loop operation because the count variable is negative: " + rawString(), module);
            return false;
        }
        for (int i = 0; i < count; i++) {
            fieldAcsr.put(methodContext, i);
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
    public String expandedString(MethodContext methodContext) {
        return this.rawString();
    }

    public List<MethodOperation> getSubOps() {
        return this.subOps;
    }

    @Override
    public String rawString() {
        return "<loop field=\"" + this.fieldAcsr + "\" count=\"" + this.countStr + "\"/>";
    }

    public static final class LoopFactory implements Factory<Loop> {
        public Loop createMethodOperation(Element element, SimpleMethod simpleMethod) throws MiniLangException {
            return new Loop(element, simpleMethod);
        }

        public String getName() {
            return "loop";
        }
    }
}
