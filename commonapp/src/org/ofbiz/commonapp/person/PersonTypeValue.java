
package org.ofbiz.commonapp.person;

import java.rmi.*;
import javax.ejb.*;
import org.ofbiz.commonapp.common.*;

/**
 * <p><b>Title:</b> Person Component - Person Type Entity
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
 *@created    Wed May 23 12:51:33 MDT 2001
 *@version    1.0
 */

public class PersonTypeValue implements PersonType
{

  /**
   *  The variable of the TYPE_ID column of the PERSON_TYPE table.
   */
  private String typeId;

  /**
   *  The variable of the DESCRIPTION column of the PERSON_TYPE table.
   */
  private String description;


  private PersonType personType;

  public PersonTypeValue()
  {

    this.typeId = null;
    this.description = null;

    this.personType = null;
  }

  public PersonTypeValue(PersonType personType) throws RemoteException
  {
    if(personType == null) return;


    this.typeId = personType.getTypeId();
    this.description = personType.getDescription();

    this.personType = personType;
  }

  public PersonTypeValue(PersonType personType, String typeId, String description)
  {
    if(personType == null) return;


    this.typeId = typeId;
    this.description = description;

    this.personType = personType;
  }


  /**
   *  Get the primary key of the TYPE_ID column of the PERSON_TYPE table.
   */
  public String getTypeId()  throws RemoteException
  {
    return typeId;
  }
  
  /**
   *  Get the value of the DESCRIPTION column of the PERSON_TYPE table.
   */
  public String getDescription() throws RemoteException
  {
    return description;
  }
  /**
   *  Set the value of the DESCRIPTION column of the PERSON_TYPE table.
   */
  public void setDescription(String description) throws RemoteException
  {
    this.description = description;
    if(personType!=null) personType.setDescription(description);
  }
  

  /**
   *  Get the value object of the PersonType class.
   */
  public PersonType getValueObject() throws RemoteException { return this; }
  /**
   *  Set the value object of the PersonType class.
   */
  public void setValueObject(PersonType valueObject) throws RemoteException
  {
    if(valueObject == null) return;

    if(personType!=null) personType.setValueObject(valueObject);

    if(typeId == null) typeId = valueObject.getTypeId();
  
  
    description = valueObject.getDescription();
  
  }

  //These are from the EJBObject interface, and must at least have thrower implementations, although we do more if the EJBObject is set...
  public EJBHome getEJBHome() throws RemoteException { if(personType!=null) return personType.getEJBHome(); else throw new ValueException("Cannot call getEJBHome, EJBObject is null."); }
  public Handle getHandle() throws RemoteException { if(personType!=null) return personType.getHandle(); else throw new ValueException("Cannot call getHandle, EJBObject is null."); }
  public Object getPrimaryKey() throws RemoteException { if(personType!=null) return personType.getEJBHome(); else throw new ValueException("Cannot call getPrimaryKey, EJBObject is null."); }
  public boolean isIdentical(EJBObject p0) throws RemoteException { if(personType!=null) return personType.isIdentical(p0); else throw new ValueException("Cannot call isIdentical(EJBObject p0), EJBObject is null."); }
  public void remove() throws RemoteException, RemoveException { if(personType!=null) personType.remove(); else throw new ValueException("Cannot call getPrimaryKey, remove is null."); }
}
