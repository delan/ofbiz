
package org.ofbiz.commonapp.product.price;

import java.rmi.*;
import javax.ejb.*;
import java.math.*;
import java.util.*;


/**
 * <p><b>Title:</b> Order Value Break Entity
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
 *@created    Fri Jul 27 01:18:30 MDT 2001
 *@version    1.0
 */

public interface OrderValueBreak extends EJBObject
{
  /** Get the primary key of the ORDER_VALUE_BREAK_ID column of the ORDER_VALUE_BREAK table. */
  public String getOrderValueBreakId() throws RemoteException;
  
  /** Get the value of the FROM_AMOUNT column of the ORDER_VALUE_BREAK table. */
  public Double getFromAmount() throws RemoteException;
  /** Set the value of the FROM_AMOUNT column of the ORDER_VALUE_BREAK table. */
  public void setFromAmount(Double fromAmount) throws RemoteException;
  
  /** Get the value of the THRU_AMOUNT column of the ORDER_VALUE_BREAK table. */
  public Double getThruAmount() throws RemoteException;
  /** Set the value of the THRU_AMOUNT column of the ORDER_VALUE_BREAK table. */
  public void setThruAmount(Double thruAmount) throws RemoteException;
  

  /** Get the value object of this OrderValueBreak class. */
  public OrderValueBreak getValueObject() throws RemoteException;
  /** Set the values in the value object of this OrderValueBreak class. */
  public void setValueObject(OrderValueBreak orderValueBreakValue) throws RemoteException;


  /** Get a collection of  PriceComponent related entities. */
  public Collection getPriceComponents() throws RemoteException;
  /** Get the  PriceComponent keyed by member(s) of this class, and other passed parameters. */
  public PriceComponent getPriceComponent(String priceComponentId) throws RemoteException;
  /** Remove  PriceComponent related entities. */
  public void removePriceComponents() throws RemoteException;
  /** Remove the  PriceComponent keyed by member(s) of this class, and other passed parameters. */
  public void removePriceComponent(String priceComponentId) throws RemoteException;

}
