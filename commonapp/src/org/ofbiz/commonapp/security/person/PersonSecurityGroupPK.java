
package org.ofbiz.commonapp.security.person;

import java.io.*;

/**
 * <p><b>Title:</b> Security Component - Person Security Group Entity
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
 *@created    Wed May 23 02:34:33 MDT 2001
 *@version    1.0
 */
public class PersonSecurityGroupPK implements Serializable
{


  /**
   *  The variable of the USERNAME column of the PERSON_SECURITY_GROUP table.
   */
  public String username;

  /**
   *  The variable of the GROUP_ID column of the PERSON_SECURITY_GROUP table.
   */
  public String groupId;


  /**
   *  Constructor for the PersonSecurityGroupPK object
   */
  public PersonSecurityGroupPK()
  {
  }

  /**
   *  Constructor for the PersonSecurityGroupPK object
   *

   *@param  username                  Field of the USERNAME column.
   *@param  groupId                  Field of the GROUP_ID column.
   */
  public PersonSecurityGroupPK(String username, String groupId)
  {

    this.username = username;
    this.groupId = groupId;
  }

  /**
   *  Description of the Method
   *
   *@param  obj  Description of Field
   *@return      Description of the Returned Value
   */
  public boolean equals(Object obj)
  {
    if(this.getClass().equals(obj.getClass()))
    {
      PersonSecurityGroupPK that = (PersonSecurityGroupPK)obj;
      return

            this.username.equals(that.username) &&
            this.groupId.equals(that.groupId) &&
            true; //This "true" is a dummy thing to take care of the last &&, just for laziness sake.
    }
    return false;
  }

  /**
   *  Description of the Method
   *
   *@return    Description of the Returned Value
   */
  public int hashCode()
  {
    return (username + groupId).hashCode();
  }
}
