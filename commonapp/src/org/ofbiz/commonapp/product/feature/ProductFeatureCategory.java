
package org.ofbiz.commonapp.product.feature;

import java.rmi.*;
import javax.ejb.*;
import java.math.*;
import java.util.*;


/**
 * <p><b>Title:</b> Product Feature Category Entity
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
 *@created    Fri Jul 27 01:18:28 MDT 2001
 *@version    1.0
 */

public interface ProductFeatureCategory extends EJBObject
{
  /** Get the primary key of the PRODUCT_FEATURE_CATEGORY_ID column of the PRODUCT_FEATURE_CATEGORY table. */
  public String getProductFeatureCategoryId() throws RemoteException;
  
  /** Get the value of the PARENT_CATEGORY_ID column of the PRODUCT_FEATURE_CATEGORY table. */
  public String getParentCategoryId() throws RemoteException;
  /** Set the value of the PARENT_CATEGORY_ID column of the PRODUCT_FEATURE_CATEGORY table. */
  public void setParentCategoryId(String parentCategoryId) throws RemoteException;
  
  /** Get the value of the DESCRIPTION column of the PRODUCT_FEATURE_CATEGORY table. */
  public String getDescription() throws RemoteException;
  /** Set the value of the DESCRIPTION column of the PRODUCT_FEATURE_CATEGORY table. */
  public void setDescription(String description) throws RemoteException;
  

  /** Get the value object of this ProductFeatureCategory class. */
  public ProductFeatureCategory getValueObject() throws RemoteException;
  /** Set the values in the value object of this ProductFeatureCategory class. */
  public void setValueObject(ProductFeatureCategory productFeatureCategoryValue) throws RemoteException;


  /** Get the  ProductFeatureCategory entity corresponding to this entity. */
  public ProductFeatureCategory getProductFeatureCategory() throws RemoteException;
  /** Remove the  ProductFeatureCategory entity corresponding to this entity. */
  public void removeProductFeatureCategory() throws RemoteException;  

  /** Get a collection of  ProductFeature related entities. */
  public Collection getProductFeatures() throws RemoteException;
  /** Get the  ProductFeature keyed by member(s) of this class, and other passed parameters. */
  public ProductFeature getProductFeature(String productFeatureId) throws RemoteException;
  /** Remove  ProductFeature related entities. */
  public void removeProductFeatures() throws RemoteException;
  /** Remove the  ProductFeature keyed by member(s) of this class, and other passed parameters. */
  public void removeProductFeature(String productFeatureId) throws RemoteException;

}
