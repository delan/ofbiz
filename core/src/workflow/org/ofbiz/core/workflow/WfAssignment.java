/*
 * $Id$
 */

package org.ofbiz.core.workflow;

/**
 * <p><b>Title:</b> WfAssignment.java
 * <p><b>Description:</b> Workflow Assignment Interface
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
 *@created    October 29, 2001
 *@version    1.0
 */

public interface WfAssignment {
    
    /** Gets the activity object of this assignment.
     * @return WfActivity The activity object of this assignment
     * @throws WfException
     */
    public WfActivity activity() throws WfException;
    
    /** Gets the assignee (resource) of this assignment
     * @return WfResource The assignee of this assignment
     * @throws WfException
     */
    public WfResource assignee() throws WfException;
    
    /** Sets the assignee of this assignment
     * @param newValue
     * @throws WfException
     * @throws InvalidResource
     */
    public void setAssignee(WfResource newValue) throws WfException, InvalidResource;
    
    /** Mark this assignment as accepted
     *@throws WfException
     */
    public void accept() throws WfException;
    
    /** Mark this assignment as complete
     * @throws CannotComplete
     * @throws WfException
     */
    public void complete() throws WfException, CannotComplete;
    
} // interface WfAssignmentOperations
