/*
 * $Id$
 * $Log$
 * Revision 1.1  2001/09/28 22:56:44  jonesde
 * Big update for fromDate PK use, organization stuff
 *
 * Revision 1.4  2001/07/23 19:16:15  azeneski
 * Fixed up finalize() method to not debug everytime.
 *
 * Revision 1.3  2001/07/23 18:38:14  azeneski
 * Added in new HttpClient. Makes behind the scenes HTTP request (GET/POST)
 * and returns the output as a string.
 *
 * Revision 1.2  2001/07/23 18:05:00  azeneski
 * Fixed runaway thread in the job scheduler.
 *
 * Revision 1.1  2001/07/16 14:45:48  azeneski
 * Added the missing 'core' directory into the module.
 *
 * Revision 1.1  2001/07/15 16:36:42  azeneski
 * Initial Import
 *
 */

package org.ofbiz.core.event;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletContext;

import org.ofbiz.core.service.scheduler.JobManager;
import org.ofbiz.core.util.HttpClient;
import org.ofbiz.core.util.SiteDefs;
import org.ofbiz.core.util.Debug;

/**
 * <p><b>Title:</b> TestEvent.java
 * <p><b>Description:</b> Test Event Class
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
 * Created on July 14, 2001, 6:41 PM
 */
public class TestEvent {

    
    public static String test (HttpServletRequest request, HttpServletResponse response ) {
        request.setAttribute("MESSAGE","Test Event Ran Fine.");
        Debug.log("Test Event Ran Fine.");
        return "success";
    }
        
    public static String httpClientTest(HttpServletRequest request, HttpServletResponse response ) {
        try {            
            HttpClient http = new HttpClient("http://www.ofbiz.org/cgi-bin/http_test.pl");
            http.setHeader("Cookie","name=value,value=name");
            http.setHeader("User-Agent", "Mozilla/4.0");
            http.setParameter("testId","testing");
            Debug.log(http.post());
        } 
        catch ( Exception e ) {
            Debug.log(e,"HttpClientException Caught.");
        }
        return "success";
    }
}
