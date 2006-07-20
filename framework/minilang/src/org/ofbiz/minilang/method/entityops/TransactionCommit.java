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

import org.ofbiz.base.util.Debug;
import org.ofbiz.entity.transaction.GenericTransactionException;
import org.ofbiz.entity.transaction.TransactionUtil;
import org.ofbiz.minilang.SimpleMethod;
import org.ofbiz.minilang.method.ContextAccessor;
import org.ofbiz.minilang.method.MethodContext;
import org.ofbiz.minilang.method.MethodOperation;
import org.w3c.dom.Element;

/**
 * Commits a transaction if beganTransaction is true, otherwise does nothing.
 *
 * @author     <a href="mailto:jonesde@ofbiz.org">David E. Jones</a>
 * @version    $Rev$
 * @since      2.0
 */
public class TransactionCommit extends MethodOperation {
    
    public static final String module = TransactionCommit.class.getName();
    
    ContextAccessor beganTransactionAcsr;

    public TransactionCommit(Element element, SimpleMethod simpleMethod) {
        super(element, simpleMethod);
        beganTransactionAcsr = new ContextAccessor(element.getAttribute("began-transaction-name"), "beganTransaction");
    }

    public boolean exec(MethodContext methodContext) {
        boolean beganTransaction = false;
        
        Boolean beganTransactionBoolean = (Boolean) beganTransactionAcsr.get(methodContext);
        if (beganTransactionBoolean != null) {
            beganTransaction = beganTransactionBoolean.booleanValue();
        }
        
        try {
            TransactionUtil.commit(beganTransaction);
        } catch (GenericTransactionException e) {
            Debug.logError(e, "Could not commit transaction in simple-method, returning error.", module);
            
            String errMsg = "ERROR: Could not complete the " + simpleMethod.getShortDescription() + " process [error committing a transaction: " + e.getMessage() + "]";
            methodContext.setErrorReturn(errMsg, simpleMethod);
            return false;
        }
        
        beganTransactionAcsr.remove(methodContext);
        return true;
    }

    public String rawString() {
        // TODO: something more than the empty tag
        return "<transaction-commit/>";
    }
    public String expandedString(MethodContext methodContext) {
        // TODO: something more than a stub/dummy
        return this.rawString();
    }
}
