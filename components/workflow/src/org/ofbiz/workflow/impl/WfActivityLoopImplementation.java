/*
 * $Id: WfActivityLoopImplementation.java,v 1.2 2003/08/19 17:45:18 jonesde Exp $
 *
 * Copyright (c) 2001, 2002 The Open For Business Project - www.ofbiz.org
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
package org.ofbiz.workflow.impl;

/**
 * WfActivityLoopImplementation.java
 *
 * @author     <a href="mailto:jaz@ofbiz.org">Andy Zeneski</a> 
 * @author     Oswin Ondarza and Manuel Soto
 * @version    $Revision: 1.2 $
 * @since      2.0
 */
public class WfActivityLoopImplementation extends WfActivityAbstractImplementation {

    public static final String module = WfActivityLoopImplementation.class.getName();

    /**     
     * @see org.ofbiz.workflow.impl.WfActivityAbstractImplementation#WfActivityAbstractImplementation(WfActivityImpl)
     */
    public WfActivityLoopImplementation(WfActivityImpl wfActivity) {
        super(wfActivity);
    }

    /**
     * To be implemented.
     * @see org.ofbiz.workflow.impl.WfActivityAbstractImplementation#run()
     */
    public void run() {}
}
