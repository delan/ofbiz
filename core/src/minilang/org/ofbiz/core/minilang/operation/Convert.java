/*
 * $Id$
 *
 *  Copyright (c) 2001 The Open For Business Project - www.ofbiz.org
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

package org.ofbiz.core.minilang.operation;

import java.net.*;
import java.text.*;
import java.util.*;
import javax.servlet.http.*;

import org.w3c.dom.*;
import org.ofbiz.core.util.*;
import org.ofbiz.core.minilang.*;

/**
 * Convert the current field from the in-map and place it in the out-map
 *
 *@author     <a href="mailto:jonesde@ofbiz.org">David E. Jones</a>
 *@created    December 29, 2001
 *@version    1.0
 */
public class Convert extends SimpleMapOperation {
    String toField;
    String type;
    boolean replace = true;
    boolean setIfNull = false;
    String format;

    public Convert(Element element, SimpleMapProcess simpleMapProcess) {
        super(element, simpleMapProcess);
        this.toField = element.getAttribute("to-field");
        if (this.toField == null || this.toField.length() == 0) {
            this.toField = this.fieldName;
        }

        type = element.getAttribute("type");
        //if anything but false it will be true
        replace = !"false".equals(element.getAttribute("replace"));
        //if anything but true it will be false
        setIfNull = "true".equals(element.getAttribute("set-if-null"));

        format = element.getAttribute("format");
    }

    public void exec(Map inMap, Map results, List messages, Locale locale, ClassLoader loader) {
        Object fieldObject = inMap.get(fieldName);

        if (fieldObject == null) {
            if (setIfNull && (replace || !results.containsKey(toField)))
                results.put(toField, null);
            return;
        }

        //if an incoming string is empty,
        // set to null if setIfNull is true, otherwise do nothing, ie treat as if null
        if (fieldObject instanceof java.lang.String) {
            if (((String) fieldObject).length() == 0) {
                if (setIfNull && (replace || !results.containsKey(toField)))
                    results.put(toField, null);
                return;
            }
        }

        Object convertedObject = null;
        try {
            convertedObject = ObjectType.simpleTypeConvert(fieldObject, type, format, locale);
        } catch (GeneralException e) {
            messages.add(e.getMessage());
            return;
        }

        if (convertedObject == null)
            return;

        if (replace) {
            results.put(toField, convertedObject);
            //Debug.logInfo("[SimpleMapProcessor.Converted.exec] Put converted value \"" + convertedObject + "\" in field \"" + toField + "\"");
        } else {
            if (results.containsKey(toField)) {
                //do nothing
            } else {
                results.put(toField, convertedObject);
                //Debug.logInfo("[SimpleMapProcessor.Converted.exec] Put converted value \"" + convertedObject + "\" in field \"" + toField + "\"");
            }
        }
    }
}
