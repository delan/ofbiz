
package org.ofbiz.commonapp.product.supplier;

import java.rmi.*;
import javax.ejb.*;
import java.util.*;
import java.math.*;

/**
 * <p><b>Title:</b> Market Interest Entity
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

public interface MarketInterestHome extends EJBHome
{

  public MarketInterest create(String productCategoryId, String partyTypeId, java.util.Date fromDate, java.util.Date thruDate) throws RemoteException, CreateException;
  public MarketInterest create(String productCategoryId, String partyTypeId) throws RemoteException, CreateException;
  public MarketInterest findByPrimaryKey(org.ofbiz.commonapp.product.supplier.MarketInterestPK primaryKey) throws RemoteException, FinderException;
  public Collection findAll() throws RemoteException, FinderException;


  /**
   *  Finds MarketInterests by the following fields:
   *

   *@param  productCategoryId                  Field for the PRODUCT_CATEGORY_ID column.
   *@return      Collection containing the found MarketInterests
   */
  public Collection findByProductCategoryId(String productCategoryId) throws RemoteException, FinderException;

  /**
   *  Finds MarketInterests by the following fields:
   *

   *@param  partyTypeId                  Field for the PARTY_TYPE_ID column.
   *@return      Collection containing the found MarketInterests
   */
  public Collection findByPartyTypeId(String partyTypeId) throws RemoteException, FinderException;

}
