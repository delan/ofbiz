
package org.ofbiz.commonapp.security.login;

import java.rmi.*;
import javax.ejb.*;
import java.math.*;
import java.util.*;

import org.ofbiz.commonapp.party.party.*;
import org.ofbiz.commonapp.security.securitygroup.*;

/**
 * <p><b>Title:</b> User Login Entity
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
 *@created    Fri Jul 27 01:18:34 MDT 2001
 *@version    1.0
 */
public class UserLoginBean implements EntityBean
{
  /** The variable for the USER_LOGIN_ID column of the USER_LOGIN table. */
  public String userLoginId;
  /** The variable for the PARTY_ID column of the USER_LOGIN table. */
  public String partyId;
  /** The variable for the CONTACT_MECHANISM_ID column of the USER_LOGIN table. */
  public String contactMechanismId;
  /** The variable for the CURRENT_USER_ID column of the USER_LOGIN table. */
  public String currentUserId;
  /** The variable for the CURRENT_PASSWORD column of the USER_LOGIN table. */
  public String currentPassword;

  EntityContext entityContext;
  boolean ejbIsModified = false;

  /** Sets the EntityContext attribute of the UserLoginBean object
   *@param  entityContext  The new EntityContext value
   */
  public void setEntityContext(EntityContext entityContext) { this.entityContext = entityContext; }

  /** Get the primary key USER_LOGIN_ID column of the USER_LOGIN table. */
  public String getUserLoginId() { return userLoginId; }

  /** Get the value of the PARTY_ID column of the USER_LOGIN table. */
  public String getPartyId() { return partyId; }
  /** Set the value of the PARTY_ID column of the USER_LOGIN table. */
  public void setPartyId(String partyId)
  {
    this.partyId = partyId;
    ejbIsModified = true;
  }

  /** Get the value of the CONTACT_MECHANISM_ID column of the USER_LOGIN table. */
  public String getContactMechanismId() { return contactMechanismId; }
  /** Set the value of the CONTACT_MECHANISM_ID column of the USER_LOGIN table. */
  public void setContactMechanismId(String contactMechanismId)
  {
    this.contactMechanismId = contactMechanismId;
    ejbIsModified = true;
  }

  /** Get the value of the CURRENT_USER_ID column of the USER_LOGIN table. */
  public String getCurrentUserId() { return currentUserId; }
  /** Set the value of the CURRENT_USER_ID column of the USER_LOGIN table. */
  public void setCurrentUserId(String currentUserId)
  {
    this.currentUserId = currentUserId;
    ejbIsModified = true;
  }

  /** Get the value of the CURRENT_PASSWORD column of the USER_LOGIN table. */
  public String getCurrentPassword() { return currentPassword; }
  /** Set the value of the CURRENT_PASSWORD column of the USER_LOGIN table. */
  public void setCurrentPassword(String currentPassword)
  {
    this.currentPassword = currentPassword;
    ejbIsModified = true;
  }

  /** Sets the values from ValueObject attribute of the UserLoginBean object
   *@param  valueObject  The new ValueObject value 
   */
  public void setValueObject(UserLogin valueObject)
  {
    try
    {
      //check for null and if null do not set; this is the method for not setting certain fields while setting the rest quickly
      // to set a field to null, use the individual setters
      if(valueObject.getPartyId() != null)
      {
        this.partyId = valueObject.getPartyId();
        ejbIsModified = true;
      }
      if(valueObject.getContactMechanismId() != null)
      {
        this.contactMechanismId = valueObject.getContactMechanismId();
        ejbIsModified = true;
      }
      if(valueObject.getCurrentUserId() != null)
      {
        this.currentUserId = valueObject.getCurrentUserId();
        ejbIsModified = true;
      }
      if(valueObject.getCurrentPassword() != null)
      {
        this.currentPassword = valueObject.getCurrentPassword();
        ejbIsModified = true;
      }
    }
    catch(java.rmi.RemoteException re)
    {
      //This should NEVER happen just calling getters on a value object, so do nothing.
      //The only reason these methods are declated to throw a RemoteException is to implement the corresponding EJBObject interface.
    }
  }

  /** Gets the ValueObject attribute of the UserLoginBean object
   *@return    The ValueObject value
   */
  public UserLogin getValueObject()
  {
    if(this.entityContext != null)
    {
      return new UserLoginValue((UserLogin)this.entityContext.getEJBObject(), userLoginId, partyId, contactMechanismId, currentUserId, currentPassword);
    }
    else { return null; }
  }


  /** Get the  Party entity corresponding to this entity. */
  public Party getParty() { return PartyHelper.findByPrimaryKey(partyId); }
  /** Remove the  Party entity corresponding to this entity. */
  public void removeParty() { PartyHelper.removeByPrimaryKey(partyId); }

  /** Get a collection of  UserLoginSecurityGroup related entities. */
  public Collection getUserLoginSecurityGroups() { return UserLoginSecurityGroupHelper.findByUserLoginId(userLoginId); }
  /** Get the  UserLoginSecurityGroup keyed by member(s) of this class, and other passed parameters. */
  public UserLoginSecurityGroup getUserLoginSecurityGroup(String groupId) { return UserLoginSecurityGroupHelper.findByPrimaryKey(userLoginId, groupId); }
  /** Remove  UserLoginSecurityGroup related entities. */
  public void removeUserLoginSecurityGroups() { UserLoginSecurityGroupHelper.removeByUserLoginId(userLoginId); }
  /** Remove the  UserLoginSecurityGroup keyed by member(s) of this class, and other passed parameters. */
  public void removeUserLoginSecurityGroup(String groupId) { UserLoginSecurityGroupHelper.removeByPrimaryKey(userLoginId, groupId); }

  /** Get a collection of  LoginAccountHistory related entities. */
  public Collection getLoginAccountHistorys() { return LoginAccountHistoryHelper.findByUserLoginId(userLoginId); }
  /** Get the  LoginAccountHistory keyed by member(s) of this class, and other passed parameters. */
  public LoginAccountHistory getLoginAccountHistory(String userLoginSeqId) { return LoginAccountHistoryHelper.findByPrimaryKey(userLoginId, userLoginSeqId); }
  /** Remove  LoginAccountHistory related entities. */
  public void removeLoginAccountHistorys() { LoginAccountHistoryHelper.removeByUserLoginId(userLoginId); }
  /** Remove the  LoginAccountHistory keyed by member(s) of this class, and other passed parameters. */
  public void removeLoginAccountHistory(String userLoginSeqId) { LoginAccountHistoryHelper.removeByPrimaryKey(userLoginId, userLoginSeqId); }


  /** Description of the Method
   *@param  userLoginId                  Field of the USER_LOGIN_ID column.
   *@param  partyId                  Field of the PARTY_ID column.
   *@param  contactMechanismId                  Field of the CONTACT_MECHANISM_ID column.
   *@param  currentUserId                  Field of the CURRENT_USER_ID column.
   *@param  currentPassword                  Field of the CURRENT_PASSWORD column.
   *@return                      Description of the Returned Value
   *@exception  CreateException  Description of Exception
   */
  public java.lang.String ejbCreate(String userLoginId, String partyId, String contactMechanismId, String currentUserId, String currentPassword) throws CreateException
  {
    this.userLoginId = userLoginId;
    this.partyId = partyId;
    this.contactMechanismId = contactMechanismId;
    this.currentUserId = currentUserId;
    this.currentPassword = currentPassword;
    return null;
  }

  /** Description of the Method
   *@param  userLoginId                  Field of the USER_LOGIN_ID column.
   *@return                      Description of the Returned Value
   *@exception  CreateException  Description of Exception
   */
  public java.lang.String ejbCreate(String userLoginId) throws CreateException
  {
    return ejbCreate(userLoginId, null, null, null, null);
  }

  /** Description of the Method
   *@param  userLoginId                  Field of the USER_LOGIN_ID column.
   *@param  partyId                  Field of the PARTY_ID column.
   *@param  contactMechanismId                  Field of the CONTACT_MECHANISM_ID column.
   *@param  currentUserId                  Field of the CURRENT_USER_ID column.
   *@param  currentPassword                  Field of the CURRENT_PASSWORD column.
   *@exception  CreateException  Description of Exception
   */
  public void ejbPostCreate(String userLoginId, String partyId, String contactMechanismId, String currentUserId, String currentPassword) throws CreateException {}

  /** Description of the Method
   *@param  userLoginId                  Field of the USER_LOGIN_ID column.
   *@exception  CreateException  Description of Exception
   */
  public void ejbPostCreate(String userLoginId) throws CreateException
  {
    ejbPostCreate(userLoginId, null, null, null, null);
  }

  /** Called when the entity bean is removed.
   *@exception  RemoveException  Description of Exception
   */
  public void ejbRemove() throws RemoveException {}

  /** Called when the entity bean is activated. */
  public void ejbActivate() {}

  /** Called when the entity bean is passivated. */
  public void ejbPassivate() {}

  /** Called when the entity bean is loaded. */
  public void ejbLoad() { ejbIsModified = false; }

  /** Called when the entity bean is stored. */
  public void ejbStore() { ejbIsModified = false; }

  /** Called to check if the entity bean needs to be stored. */
  public boolean isModified() { return ejbIsModified; }

  /** Unsets the EntityContext, ie sets it to null. */
  public void unsetEntityContext() { entityContext = null; }
}
