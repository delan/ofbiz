
package org.ofbiz.commonapp.party.party;

import java.rmi.*;
import javax.ejb.*;
import java.math.*;

/**
 * <p><b>Title:</b> Party Classification Entity
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
 *@created    Tue Jul 03 01:11:42 MDT 2001
 *@version    1.0
 */

public interface PartyClassification extends EJBObject
{

  
  /**
   *  Get the primary key of the PARTY_ID column of the PARTY_CLASSIFICATION table.
   */
  public String getPartyId() throws RemoteException;
  

  
  /**
   *  Get the primary key of the PARTY_TYPE_ID column of the PARTY_CLASSIFICATION table.
   */
  public String getPartyTypeId() throws RemoteException;
  

  
  /**
   *  Get the value of the PARTY_CLASSIFICATION_TYPE_ID column of the PARTY_CLASSIFICATION table.
   */
  public String getPartyClassificationTypeId() throws RemoteException;
  /**
   *  Set the value of the PARTY_CLASSIFICATION_TYPE_ID column of the PARTY_CLASSIFICATION table.
   */
  public void setPartyClassificationTypeId(String partyClassificationTypeId) throws RemoteException;
  

  
  /**
   *  Get the value of the FROM_DATE column of the PARTY_CLASSIFICATION table.
   */
  public java.util.Date getFromDate() throws RemoteException;
  /**
   *  Set the value of the FROM_DATE column of the PARTY_CLASSIFICATION table.
   */
  public void setFromDate(java.util.Date fromDate) throws RemoteException;
  

  
  /**
   *  Get the value of the THRU_DATE column of the PARTY_CLASSIFICATION table.
   */
  public java.util.Date getThruDate() throws RemoteException;
  /**
   *  Set the value of the THRU_DATE column of the PARTY_CLASSIFICATION table.
   */
  public void setThruDate(java.util.Date thruDate) throws RemoteException;
  


  /**
   *  Get the value object of this PartyClassification class.
   */
  public PartyClassification getValueObject() throws RemoteException;
  /**
   *  Set the values in the value object of this PartyClassification class.
   */
  public void setValueObject(PartyClassification partyClassificationValue) throws RemoteException;
}
