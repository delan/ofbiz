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

package org.ofbiz.core.util;

import java.net.*;
import java.text.*;
import java.util.*;
import javax.servlet.*;

/**
 * Misc J2EE Compatibility Utility Functions
 *
 * @author     <a href="mailto:jonesde@ofbiz.org">David E. Jones</a>
 * @version    1.0
 * @created    15 August 2002
 */
public class UtilJ2eeCompat {

    public static final String TOMCAT = "Apache Tomcat";
    public static final String ORION = "Orion";
    public static final String RESIN = "Resin";
    public static final String REX_IP = "TradeCity";
    public static final String OC4J = "Oracle";
    
    protected static Boolean doFlushOnRenderValue = null;
    protected static Boolean useOutputStreamNotWriterValue = null;
    
    public static boolean doFlushOnRender(ServletContext context) {
        initCompatibilityOptions(context);
        return doFlushOnRenderValue.booleanValue();
    }
    
    public static boolean useOutputStreamNotWriter(ServletContext context) {
        initCompatibilityOptions(context);
        return useOutputStreamNotWriterValue.booleanValue();
    }
    
    protected static void initCompatibilityOptions(ServletContext context) {
        //this check to see if we should flush is done because on most servers this 
        // will just slow things down and not solve any problems, but on Tomcat, Orion, etc it is necessary
        if (useOutputStreamNotWriterValue == null || doFlushOnRenderValue == null) {
            boolean doflush = true;
            boolean usestream = true;
            //if context is null use an empty string here which will cause the defaults to be used
            String serverInfo = context == null ? "" : context.getServerInfo();
            Debug.logInfo("serverInfo: " + serverInfo);
            
            if (serverInfo.indexOf(RESIN) >= 0) {
                Debug.logImportant("Resin detected, disabling the flush on the region render from PageContext for better performance");
                doflush = false;
            } else if (serverInfo.indexOf(REX_IP) >= 0) {
                Debug.logImportant("Trade City RexIP detected, using response.getWriter to write text out instead of response.getOutputStream");
                usestream = false;
            } else if (serverInfo.indexOf(TOMCAT) >= 0) {
                Debug.logImportant("Apache Tomcat detected, using response.getWriter to write text out instead of response.getOutputStream");
                usestream = false;
            }
            
            doFlushOnRenderValue = new Boolean(doflush);
            useOutputStreamNotWriterValue = new Boolean(usestream);
        }
    }
}
