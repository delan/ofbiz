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

import org.ofbiz.minilang.MiniLangException;
import org.ofbiz.minilang.SimpleMethod;
import org.ofbiz.minilang.method.MethodContext;
import org.ofbiz.minilang.method.MethodOperation;
import org.w3c.dom.Element;

/**
 * Causes script execution to return to the beginning of the nearest enclosing loop element.
 */
public class Continue extends MethodOperation {

    public Continue(Element element, SimpleMethod simpleMethod) throws MiniLangException {
        super(element, simpleMethod);
    }

    @Override
    public boolean exec(MethodContext methodContext) throws MiniLangException {
        throw new ContinueElementException();
    }

    @Override
    public String toString() {
        return "<continue/>";
    }

    @SuppressWarnings("serial")
    public class ContinueElementException extends MiniLangException {

        public ContinueElementException() {
            super("<continue> element encountered without enclosing loop");
        }

        @Override
        public String getMessage() {
            StringBuilder sb = new StringBuilder(super.getMessage());
            SimpleMethod method = getSimpleMethod();
            sb.append(" Method = ").append(method.getMethodName()).append(", File = ").append(method.getFromLocation());
            sb.append(", Element = <continue>, Line ").append(getLineNumber());
            return sb.toString();
        }
    }

    public static final class ContinueFactory implements Factory<Continue> {
        public Continue createMethodOperation(Element element, SimpleMethod simpleMethod) throws MiniLangException {
            return new Continue(element, simpleMethod);
        }

        public String getName() {
            return "continue";
        }
    }
}
