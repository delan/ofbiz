/*
 * $Id$
 * $Log$
 * Revision 1.2  2001/08/16 01:24:19  azeneski
 * Updated property tags to use the EntityValue by default.
 *
 * Revision 1.1  2001/08/05 00:48:47  azeneski
 * Added new core JSP tag library. Non-application specific taglibs.
 *
 */

package org.ofbiz.core.taglib;

import javax.servlet.jsp.tagext.*;

/**
 * <p><b>Title:</b> ObjectTEI.java
 * <p><b>Description:</b> Extra-Info class for the ObjectTag.
 * <p>Copyright (c) 2001 The Open For Business Project and repected authors.
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
 * @author Andy Zeneski (jaz@zsolv.com)
 * @version 1.0
 * Created on August 4, 2001, 8:21 PM
 */
public class ObjectTEI extends TagExtraInfo {

    public ObjectTEI () { 
	super();
    }

    public VariableInfo[] getVariableInfo (TagData data) {
        String name = null;
        String className = null;
        
	name = data.getAttributeString("name");           
	className = data.getAttributeString("type");                                          
        if ( className == null )
            className = "org.ofbiz.core.entity.GenericValue";
        
	VariableInfo info = new VariableInfo(name, className, true, VariableInfo.AT_BEGIN);
	VariableInfo[] result = { info };
	return result;
    }

    public boolean isValid (TagData data) {	        
	return true;
    }         
}



