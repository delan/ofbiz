/*
 * $Id$
 */

package org.ofbiz.core.pseudotag;

import java.io.*;
import java.text.*;
import java.util.*;
import javax.servlet.jsp.*;
import org.ofbiz.core.entity.*;
import org.ofbiz.core.entity.model.*;
import org.ofbiz.core.util.*;

/**
 * <p><b>Title:</b> InputValue Pseudo-Tag
 * <p><b>Description:</b> Outputs a string for an input box from either an entity field or
 *     a request parameter. Decides which to use by checking to see if the entityattr exist and
 *     using the specified field if it does. If the Boolean object referred to by the tryentityattr
 *     attribute is false, always tries to use the request parameter and ignores the entity field.
 * <p>Copyright (c) 2002 The Open For Business Project - www.ofbiz.org
 * <p>Permission is hereby granted, free of charge, to any person obtaining a
 *  copy of this software and associated documentation files (the "Software"),
 *  to deal in the Software without restriction, including without limitation
 *  the rights to use, copy, modify, merge, publish, distribute, sublicense,
 *  and/or sell copies of the Software, and to permit persons to whom the
 *  Software is furnished to do so, subject to the following conditions:
 *
 * <p>The above copyright notice and this permission notice shall be included
 *  in all copies or substantial portions of the Software.
 *
 * <p>THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 *  OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 *  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 *  IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 *  CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT
 *  OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR
 *  THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * @author     <a href="mailto:jonesde@ofbiz.org">David E. Jones</a>
 * @version    1.0
 * @created February 1, 2002
 */
public class InputValue {

    PageContext pageContextInternal = null;

    public InputValue(PageContext pageContextInternal) {
        this.pageContextInternal = pageContextInternal;
    }
    
    public void run(String field, String entityAttr)
            throws IOException {
        run(field, null, entityAttr, null, null, null, pageContextInternal);
    }
    
    public void run(String field, String entityAttr, String tryEntityAttr)
            throws IOException {
        run(field, null, entityAttr, tryEntityAttr, null, null, pageContextInternal);
    }
    
    public void run(String field, String entityAttr, String tryEntityAttr, 
            String fullattrsStr) throws IOException {
        run(field, null, entityAttr, tryEntityAttr, null, fullattrsStr, pageContextInternal);
    }
    
    /** Run the InputValue Pseudo-Tag, all fields except field, and entityAttr can be null */
    public void run(String field, String param, String entityAttr, String tryEntityAttr, 
            String defaultStr, String fullattrsStr) throws IOException {
        run(field, param, entityAttr, tryEntityAttr, defaultStr, fullattrsStr, pageContextInternal);
    }
    
    /* --- STATIC METHODS --- */
    
    public static void run(String field, String entityAttr, 
            PageContext pageContext) throws IOException {
        run(field, null, entityAttr, null, null, null, pageContext);
    }
    
    public static void run(String field, String entityAttr, String tryEntityAttr, 
            PageContext pageContext) throws IOException {
        run(field, null, entityAttr, tryEntityAttr, null, null, pageContext);
    }
    
    public static void run(String field, String entityAttr, String tryEntityAttr, 
            String fullattrsStr, PageContext pageContext) throws IOException {
        run(field, null, entityAttr, tryEntityAttr, null, fullattrsStr, pageContext);
    }
    
    /** Run the InputValue Pseudo-Tag, all fields except field, entityAttr, and pageContext can be null */
    public static void run(String field, String param, String entityAttr, String tryEntityAttr, 
            String defaultStr, String fullattrsStr, PageContext pageContext) throws IOException {
        if (field == null || entityAttr == null || pageContext == null) {
            throw new RuntimeException("Required parameter (attribute || field || pageContext) missing");
        }
        
        if (defaultStr == null) defaultStr = "";
        String inputValue = null;
        boolean tryEntity = true;
        boolean fullattrs = false;

        String paramName = param;
        if (paramName == null || paramName.length() == 0)
            paramName = field;

        Boolean tempBool = null;
        if (tryEntityAttr != null)
            tempBool = (Boolean) pageContext.getAttribute(tryEntityAttr);
        if (tempBool != null)
            tryEntity = tempBool.booleanValue();

        //if anything but true, it will be false, ie default is false
        fullattrs = "true".equals(fullattrsStr);

        if (tryEntity) {
            Object entTemp = pageContext.getAttribute(entityAttr);
            if (entTemp != null) {
                if (entTemp instanceof GenericValue) {
                    GenericValue entity = (GenericValue) entTemp;
                    Object fieldVal = entity.get(field);
                    if (fieldVal != null)
                        inputValue = fieldVal.toString();
                } else if (entTemp instanceof Map) {
                    Map map = (Map) entTemp;
                    Object fieldVal = map.get(field);
                    if (fieldVal != null)
                        inputValue = fieldVal.toString();
                }
            }
        }
        //if nothing found in entity, or if not checked, try a parameter
        if (inputValue == null) {
            inputValue = pageContext.getRequest().getParameter(paramName);
        }

        if (inputValue == null || inputValue.length() == 0)
            inputValue = defaultStr;

        if (fullattrs) {
            pageContext.getOut().print("name='" + paramName + "' value='" +
                    inputValue + "'");
        } else {
            pageContext.getOut().print(inputValue);
        }
    }
}
