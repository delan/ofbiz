/*
 * $Id$
 *
 *  Copyright (c) 2001, 2002 The Open For Business Project - www.ofbiz.org
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a
 *  copy of this software and associated documentation files (the "Software"),
 *  to deal in the Software without restriction, including without limitation
 *  the rights to use, copy, modify, merge, publish, distribute, sublicense,
 *  and/or sell copies of the Software, and to permit persons to whom the
 *  Software is furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included
 *  in all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 *  OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 *  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 *  IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 *  CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT
 *  OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR
 *  THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.ofbiz.core.minilang.method.ifops;

import java.util.*;

import org.w3c.dom.*;
import org.ofbiz.core.util.*;
import org.ofbiz.core.minilang.*;
import org.ofbiz.core.minilang.method.*;

import org.ofbiz.core.minilang.operation.*;

/**
 * Iff the comparison between the constant and the specified field is true process sub-operations
 *
 * @author     <a href="mailto:jonesde@ofbiz.org">David E. Jones</a>
 * @version    $Revision$
 * @since      2.0
 */
public class IfCompare extends MethodOperation {

    List subOps = new LinkedList();
    List elseSubOps = null;

    String mapName;
    String fieldName;
    String value;

    String operator;
    String type;
    String format;

    public IfCompare(Element element, SimpleMethod simpleMethod) {
        super(element, simpleMethod);
        this.mapName = element.getAttribute("map-name");
        this.fieldName = element.getAttribute("field-name");
        this.value = element.getAttribute("value");

        this.operator = element.getAttribute("operator");
        this.type = element.getAttribute("type");
        this.format = element.getAttribute("format");

        SimpleMethod.readOperations(element, subOps, simpleMethod);

        Element elseElement = UtilXml.firstChildElement(element, "else");

        if (elseElement != null) {
            elseSubOps = new LinkedList();
            SimpleMethod.readOperations(elseElement, elseSubOps, simpleMethod);
        }
    }

    public boolean exec(MethodContext methodContext) {
        // if conditions fails, always return true; if a sub-op returns false 
        // return false and stop, otherwise return true

        Object fieldVal = null;

        if (mapName != null && mapName.length() > 0) {
            Map fromMap = (Map) methodContext.getEnv(mapName);

            if (fromMap == null) {
                if (Debug.infoOn()) Debug.logInfo("Map not found with name " + mapName + ", using empty string for comparison");
            } else {
                fieldVal = fromMap.get(fieldName);
            }
        } else {
            // no map name, try the env
            fieldVal = methodContext.getEnv(fieldName);
        }

        // always use an empty string by default
        if (fieldVal == null) {
            fieldVal = "";
        }

        List messages = new LinkedList();
        Boolean resultBool = BaseCompare.doRealCompare(fieldVal, value, this.operator, this.type, this.format, messages, null, methodContext.getLoader());

        if (messages.size() > 0) {
            if (methodContext.getMethodType() == MethodContext.EVENT) {
                StringBuffer fullString = new StringBuffer();

                fullString.append("Error with comparison: ");
                Iterator miter = messages.iterator();

                while (miter.hasNext()) {
                    fullString.append((String) miter.next());
                }
                Debug.logWarning(fullString.toString());

                methodContext.putEnv(simpleMethod.getEventErrorMessageName(), fullString.toString());
                methodContext.putEnv(simpleMethod.getEventResponseCodeName(), simpleMethod.getDefaultErrorCode());
            } else if (methodContext.getMethodType() == MethodContext.SERVICE) {
                methodContext.putEnv(simpleMethod.getServiceErrorMessageListName(), messages);
                methodContext.putEnv(simpleMethod.getServiceResponseMessageName(), simpleMethod.getDefaultErrorCode());
            }
            return false;
        }

        if (resultBool != null && resultBool.booleanValue()) {
            return SimpleMethod.runSubOps(subOps, methodContext);
        } else {
            if (elseSubOps != null) {
                return SimpleMethod.runSubOps(elseSubOps, methodContext);
            } else {
                return true;
            }
        }
    }
}
