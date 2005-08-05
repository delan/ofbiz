/*
 * $Id$
 *
 * Copyright (c) 2004 The Open For Business Project - www.ofbiz.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT
 * OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR
 * THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */
package org.ofbiz.base.location;

import java.net.MalformedURLException;
import java.net.URL;

import org.ofbiz.base.util.UtilURL;

/**
 * A special location resolver that uses Strings like URLs, but with more options 
 *
 *@author     <a href="mailto:jonesde@ofbiz.org">David E. Jones</a>
 *@version    $Rev$
 *@since      3.1
 */

public class ClasspathLocationResolver implements LocationResolver {
    public URL resolveLocation(String location) throws MalformedURLException {
        return resolveLocation(location, null);
    }
    
    public URL resolveLocation(String location, ClassLoader loader) throws MalformedURLException {
        String baseLocation = FlexibleLocation.stripLocationType(location);
        // if there is a leading forward slash, remove it
        if (baseLocation.charAt(0) == '/') {
            baseLocation = baseLocation.substring(1);
        }
        return UtilURL.fromResource(baseLocation, loader);
    }
}
