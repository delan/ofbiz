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
package org.ofbiz.minilang.method.callops;

import org.w3c.dom.*;

import org.ofbiz.minilang.*;
import org.ofbiz.minilang.method.*;

/**
 * An event operation that returns the given response code
 *
 * @author     <a href="mailto:jonesde@ofbiz.org">David E. Jones</a>
 * @version    $Rev$
 * @since      2.0
 */
public class Return extends MethodOperation {
    
    String responseCode;

    public Return(Element element, SimpleMethod simpleMethod) {
        super(element, simpleMethod);
        responseCode = element.getAttribute("response-code");
        if (responseCode == null || responseCode.length() == 0)
            responseCode = "success";
    }

    public boolean exec(MethodContext methodContext) {
        String responseCode = methodContext.expandString(this.responseCode);
        
        if (methodContext.getMethodType() == MethodContext.EVENT) {
            methodContext.putEnv(simpleMethod.getEventResponseCodeName(), responseCode);
            return false;
        } else if (methodContext.getMethodType() == MethodContext.SERVICE) {
            methodContext.putEnv(simpleMethod.getServiceResponseMessageName(), responseCode);
            return false;
        } else {
            return false;
        }
    }

    public String rawString() {
        // TODO: something more than the empty tag
        return "<return/>";
    }
    public String expandedString(MethodContext methodContext) {
        // TODO: something more than a stub/dummy
        return this.rawString();
    }
}
