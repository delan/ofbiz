/*
 * $Id$
 * $Log$ 
 */

package org.ofbiz.core.control;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.ofbiz.core.util.SiteDefs;
import org.ofbiz.core.util.Debug;

/**
 * <p><b>Title:</b> ContextSecurityFilter.java
 * <p><b>Description:</b> Security Filter to restrict access to raw JSP pages.
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
 * Created on September 23, 2001, 10:37 AM
 */
public class ContextSecurityFilter implements Filter {
    
    public FilterConfig config;
    
    public void init(FilterConfig config) {
        this.config = config;
    }
    
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        ArrayList allowList = new ArrayList();
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponseWrapper wrapper = new HttpServletResponseWrapper((HttpServletResponse)response);
        
        String allowedPath = config.getInitParameter("allowedPaths");
        String redirectPath = config.getInitParameter("redirectPath");
        String errorCode = config.getInitParameter("errorCode");
               
        String allowedPathString = allowedPath;
        while ( allowedPathString.lastIndexOf(":") > -1 ) {            
            String newPath = allowedPathString.substring(0,allowedPathString.indexOf(":"));
            allowedPathString = allowedPathString.substring(allowedPathString.indexOf(":") + 1);
            allowList.add(newPath);
            if ( allowedPathString.lastIndexOf(":") == -1 && allowedPathString.length() > 0 )
                allowList.add(allowedPathString);
        }
        
        allowList.add("");    // No path is allowed.
        allowList.add("/");  // No path is allowed.
                
        String requestPath = httpRequest.getServletPath();
        if ( requestPath.lastIndexOf("/") > 0 ) {
            if ( requestPath.indexOf("/") == 0 )                 
                requestPath = "/" + requestPath.substring(1,requestPath.indexOf("/",1));
            else
                requestPath = requestPath.substring(1,requestPath.indexOf("/"));
        }
        
        StringBuffer contextUriBuffer = new StringBuffer();
        if ( httpRequest.getContextPath() != null )
            contextUriBuffer.append(httpRequest.getContextPath());
        if ( httpRequest.getServletPath() != null )
            contextUriBuffer.append(httpRequest.getServletPath());
        if ( httpRequest.getPathInfo() != null )
            contextUriBuffer.append(httpRequest.getPathInfo());        
        String contextUri = contextUriBuffer.toString();        
                
        if ( !allowList.contains(requestPath) && !allowList.contains(httpRequest.getServletPath()) ) {    
            String filterMessage = "[ContextSecurityFilter] : Filtered request - " + contextUri;
            if ( redirectPath == null ) {
                int error;
                try {
                    error = Integer.parseInt(errorCode);
                }
                catch ( NumberFormatException nfe ) {
                    error = 404;
                }                
                filterMessage = filterMessage + " (" + error + ")";
                wrapper.sendError(error,contextUri);
            } else {       
                filterMessage = filterMessage + " (" + redirectPath + ")";
                wrapper.sendRedirect(redirectPath);
            }
            Debug.logInfo(filterMessage);
            return;
        }
        
        chain.doFilter(request,response);
    }
        
    public void destroy() { 
        config = null;
    }
    
}
