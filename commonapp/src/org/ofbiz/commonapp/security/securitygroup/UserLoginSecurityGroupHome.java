
package org.ofbiz.commonapp.security.securitygroup;

import java.rmi.*;
import javax.ejb.*;
import java.util.*;
import java.math.*;

/**
 * <p><b>Title:</b> Security Component - User Login Security Group Entity
 * <p><b>Description:</b> Defines a permission available to a security group
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
 *@created    Sun Jul 08 01:14:09 MDT 2001
 *@version    1.0
 */

public interface UserLoginSecurityGroupHome extends EJBHome
{

  public UserLoginSecurityGroup create(String userLoginId, String groupId) throws RemoteException, CreateException;
  public UserLoginSecurityGroup findByPrimaryKey(org.ofbiz.commonapp.security.securitygroup.UserLoginSecurityGroupPK primaryKey) throws RemoteException, FinderException;
  public Collection findAll() throws RemoteException, FinderException;


  /**
   *  Finds UserLoginSecurityGroups by the following fields:
   *

   *@param  userLoginId                  Field for the USER_LOGIN_ID column.
   *@return      Collection containing the found UserLoginSecurityGroups
   */
  public Collection findByUserLoginId(String userLoginId) throws RemoteException, FinderException;

  /**
   *  Finds UserLoginSecurityGroups by the following fields:
   *

   *@param  groupId                  Field for the GROUP_ID column.
   *@return      Collection containing the found UserLoginSecurityGroups
   */
  public Collection findByGroupId(String groupId) throws RemoteException, FinderException;

}
