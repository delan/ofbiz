/*
 * $Id$
 */

package org.ofbiz.core.taglib;

import java.io.*;
import java.text.*;
import java.util.*;
import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;

import org.ofbiz.core.entity.*;
import org.ofbiz.core.entity.model.*;
import org.ofbiz.core.pseudotag.*;
import org.ofbiz.core.util.*;

/**
 * <p><b>Title:</b> Tag to Print Localized Entity Fields
 * <p><b>Description:</b> None
 * <p>Copyright (c) 2002 The Open For Business Project (www.ofbiz.org) and repected authors.
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
 * @author <a href="mailto:jaz@zsolv.com">Andy Zeneski</a>
 * @author <a href="mailto:jonesde@ofbiz.org">David E. Jones</a>
 * @version 1.0
 * @created October 2, 2001
 */
public class EntityFieldTag extends TagSupport {

    String field = null;
    String type = null;
    String attribute = null;
    String defaultStr = "";
    String prefix = null;
    String suffix = null;

    //public EntityFieldTag() {
    //    super();
    //    Debug.logInfo("Creating new EntityFieldTag");
    //}

    public String getAttribute() {
        return attribute;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public String getDefault() {
        return defaultStr;
    }

    public void setDefault(String defaultStr) {
        this.defaultStr = defaultStr;
    }

    public int doStartTag() throws JspTagException {
        try {
            EntityField.run(attribute, field, prefix, suffix, defaultStr, type, pageContext);
        } catch (IOException e) {
            throw new JspTagException(e.getMessage());
        } catch (GenericEntityException e) {
            throw new JspTagException("Entity Engine error: " + e.getMessage());
        }

        return (SKIP_BODY);
    }
}
