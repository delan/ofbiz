/*
 * $Id$
 * $Log$
 */

package org.ofbiz.core.taglib;

import java.text.NumberFormat;
import java.text.DateFormat;
import java.util.Date;

import javax.servlet.jsp.tagext.BodyTagSupport;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.JspException;
import java.io.IOException;

/**
 * <p><b>Title:</b> FormatTag.java
 * <p><b>Description:</b> Custom JSP Tag to format numbers and dates.
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
public class FormatTag extends BodyTagSupport {
     
    private String type = "N";
           
    public void setType(String type) {
        this.type = type;
    }
    
    public String getType() {
        return type;
    }
    
    public int doAfterBody() throws JspException {       
        NumberFormat nf = null;
        DateFormat df = null;
        BodyContent body = getBodyContent();
        String value = body.getString();
                
        if ( type.substring(0,1).equalsIgnoreCase("C") )       
            nf = NumberFormat.getCurrencyInstance();                            
        if ( type.substring(0,1).equalsIgnoreCase("N") )
            nf = NumberFormat.getNumberInstance();
        if ( type.substring(0,1).equalsIgnoreCase("D") )
            df = DateFormat.getDateInstance();
        
        try {
            if ( nf != null ) {
                // do the number formatting
                getPreviousOut().print(nf.format(Double.parseDouble(value)));
            }
            else if ( df != null ) {
                // do the date formatting
                getPreviousOut().print(df.format(df.parse(value)));
            }
            else {
                // just return the value
                getPreviousOut().print(value);
            }
        }
        catch ( Exception e ) {
            throw new JspException(e.getMessage());
        }
        
        return SKIP_BODY;                
    }
    
}
       