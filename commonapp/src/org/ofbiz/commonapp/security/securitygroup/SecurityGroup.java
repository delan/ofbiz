
package org.ofbiz.commonapp.security.securitygroup;

import java.rmi.*;
import javax.ejb.*;
import java.math.*;

/**
 * <p><b>Title:</b> Security Component - Security Group Entity
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
 *@author     David E. Jones
 *@created    Tue Jul 03 01:11:49 MDT 2001
 *@version    1.0
 */

public interface SecurityGroup extends EJBObject
{

  
  /**
   *  Get the primary key of the GROUP_ID column of the SECURITY_GROUP table.
   */
  public String getGroupId() throws RemoteException;
  

  
  /**
   *  Get the value of the DESCRIPTION column of the SECURITY_GROUP table.
   */
  public String getDescription() throws RemoteException;
  /**
   *  Set the value of the DESCRIPTION column of the SECURITY_GROUP table.
   */
  public void setDescription(String description) throws RemoteException;
  


  /**
   *  Get the value object of this SecurityGroup class.
   */
  public SecurityGroup getValueObject() throws RemoteException;
  /**
   *  Set the values in the value object of this SecurityGroup class.
   */
  public void setValueObject(SecurityGroup securityGroupValue) throws RemoteException;
}
