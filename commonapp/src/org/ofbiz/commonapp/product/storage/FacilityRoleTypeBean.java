
package org.ofbiz.commonapp.product.storage;

import java.rmi.*;
import javax.ejb.*;
import java.math.*;
import java.util.*;


/**
 * <p><b>Title:</b> Facility Role Type Entity
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
 *@created    Fri Jul 27 01:18:33 MDT 2001
 *@version    1.0
 */
public class FacilityRoleTypeBean implements EntityBean
{
  /** The variable for the FACILITY_ROLE_TYPE_ID column of the FACILITY_ROLE_TYPE table. */
  public String facilityRoleTypeId;
  /** The variable for the DESCRIPTION column of the FACILITY_ROLE_TYPE table. */
  public String description;

  EntityContext entityContext;
  boolean ejbIsModified = false;

  /** Sets the EntityContext attribute of the FacilityRoleTypeBean object
   *@param  entityContext  The new EntityContext value
   */
  public void setEntityContext(EntityContext entityContext) { this.entityContext = entityContext; }

  /** Get the primary key FACILITY_ROLE_TYPE_ID column of the FACILITY_ROLE_TYPE table. */
  public String getFacilityRoleTypeId() { return facilityRoleTypeId; }

  /** Get the value of the DESCRIPTION column of the FACILITY_ROLE_TYPE table. */
  public String getDescription() { return description; }
  /** Set the value of the DESCRIPTION column of the FACILITY_ROLE_TYPE table. */
  public void setDescription(String description)
  {
    this.description = description;
    ejbIsModified = true;
  }

  /** Sets the values from ValueObject attribute of the FacilityRoleTypeBean object
   *@param  valueObject  The new ValueObject value 
   */
  public void setValueObject(FacilityRoleType valueObject)
  {
    try
    {
      //check for null and if null do not set; this is the method for not setting certain fields while setting the rest quickly
      // to set a field to null, use the individual setters
      if(valueObject.getDescription() != null)
      {
        this.description = valueObject.getDescription();
        ejbIsModified = true;
      }
    }
    catch(java.rmi.RemoteException re)
    {
      //This should NEVER happen just calling getters on a value object, so do nothing.
      //The only reason these methods are declated to throw a RemoteException is to implement the corresponding EJBObject interface.
    }
  }

  /** Gets the ValueObject attribute of the FacilityRoleTypeBean object
   *@return    The ValueObject value
   */
  public FacilityRoleType getValueObject()
  {
    if(this.entityContext != null)
    {
      return new FacilityRoleTypeValue((FacilityRoleType)this.entityContext.getEJBObject(), facilityRoleTypeId, description);
    }
    else { return null; }
  }


  /** Get a collection of  PartyFacility related entities. */
  public Collection getPartyFacilitys() { return PartyFacilityHelper.findByFacilityRoleTypeId(facilityRoleTypeId); }
  /** Get the  PartyFacility keyed by member(s) of this class, and other passed parameters. */
  public PartyFacility getPartyFacility(String partyId, String facilityId) { return PartyFacilityHelper.findByPrimaryKey(partyId, facilityId); }
  /** Remove  PartyFacility related entities. */
  public void removePartyFacilitys() { PartyFacilityHelper.removeByFacilityRoleTypeId(facilityRoleTypeId); }
  /** Remove the  PartyFacility keyed by member(s) of this class, and other passed parameters. */
  public void removePartyFacility(String partyId, String facilityId) { PartyFacilityHelper.removeByPrimaryKey(partyId, facilityId); }


  /** Description of the Method
   *@param  facilityRoleTypeId                  Field of the FACILITY_ROLE_TYPE_ID column.
   *@param  description                  Field of the DESCRIPTION column.
   *@return                      Description of the Returned Value
   *@exception  CreateException  Description of Exception
   */
  public java.lang.String ejbCreate(String facilityRoleTypeId, String description) throws CreateException
  {
    this.facilityRoleTypeId = facilityRoleTypeId;
    this.description = description;
    return null;
  }

  /** Description of the Method
   *@param  facilityRoleTypeId                  Field of the FACILITY_ROLE_TYPE_ID column.
   *@return                      Description of the Returned Value
   *@exception  CreateException  Description of Exception
   */
  public java.lang.String ejbCreate(String facilityRoleTypeId) throws CreateException
  {
    return ejbCreate(facilityRoleTypeId, null);
  }

  /** Description of the Method
   *@param  facilityRoleTypeId                  Field of the FACILITY_ROLE_TYPE_ID column.
   *@param  description                  Field of the DESCRIPTION column.
   *@exception  CreateException  Description of Exception
   */
  public void ejbPostCreate(String facilityRoleTypeId, String description) throws CreateException {}

  /** Description of the Method
   *@param  facilityRoleTypeId                  Field of the FACILITY_ROLE_TYPE_ID column.
   *@exception  CreateException  Description of Exception
   */
  public void ejbPostCreate(String facilityRoleTypeId) throws CreateException
  {
    ejbPostCreate(facilityRoleTypeId, null);
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
