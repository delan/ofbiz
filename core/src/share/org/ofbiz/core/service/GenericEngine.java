/*
 * $Id$
 */

package org.ofbiz.core.service;

import java.util.*;
import org.ofbiz.core.util.*;

/**
 * <p><b>Title:</b> Generic Engine Interface
 * <p><b>Description:</b> None
 * <p>Copyright (c) 2001 The Open For Business Project - www.ofbiz.org
 *
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
 *@author     <a href="mailto:jaz@zsolv.com">Andy Zeneski</a>
 *@author     <a href="mailto:jonesde@ofbiz.org">David E. Jones</a>
 *@created    Oct 20 2001
 *@version    1.0
 */
public interface GenericEngine {
    
    /** Run the service synchronously and return the result
     *@param context Map of name, value pairs composing the context
     *@return Map of name, value pairs composing the result
     */
    public Map runSync(ModelService modelService, Map context) throws GenericServiceException;
    
    /** Run the service synchronously and IGNORE the result
     *@param context Map of name, value pairs composing the context
     */
    public void runSyncIgnore(ModelService modelService, Map context) throws GenericServiceException;
    
    /** Run the service asynchronously, passing an instance of GenericRequester that will receive the result
     *@param context Map of name, value pairs composing the context
     *@param requester Object implementing GenericRequester interface which will receive the result
     */
    public void runAsync(ModelService modelService, Map context, GenericRequester requester) throws GenericServiceException;
    
    /** Run the service asynchronously and IGNORE the result
     *@param context Map of name, value pairs composing the context
     */
    public void runAsync(ModelService modelService, Map context) throws GenericServiceException;
    
    /** Set the name of the local dispatcher
     *@param Name of the local dispatcher
     */
    public void setLoader(String name);
}
