
package org.ofbiz.commonapp.product.product;

import java.rmi.*;
import javax.ejb.*;
import java.util.*;
import java.math.*;

/**
 * <p><b>Title:</b> Product Data Object Entity
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
 *@created    Fri Jul 27 01:18:26 MDT 2001
 *@version    1.0
 */

public interface ProductDataObjectHome extends EJBHome
{

  public ProductDataObject create(String dataObjectId, String productId) throws RemoteException, CreateException;
  public ProductDataObject findByPrimaryKey(org.ofbiz.commonapp.product.product.ProductDataObjectPK primaryKey) throws RemoteException, FinderException;
  public Collection findAll() throws RemoteException, FinderException;


  /**
   *  Finds ProductDataObjects by the following fields:
   *

   *@param  dataObjectId                  Field for the DATA_OBJECT_ID column.
   *@return      Collection containing the found ProductDataObjects
   */
  public Collection findByDataObjectId(String dataObjectId) throws RemoteException, FinderException;

  /**
   *  Finds ProductDataObjects by the following fields:
   *

   *@param  productId                  Field for the PRODUCT_ID column.
   *@return      Collection containing the found ProductDataObjects
   */
  public Collection findByProductId(String productId) throws RemoteException, FinderException;

}
