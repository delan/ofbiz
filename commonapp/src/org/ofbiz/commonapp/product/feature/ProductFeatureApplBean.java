
package org.ofbiz.commonapp.product.feature;

import java.rmi.*;
import javax.ejb.*;
import java.math.*;
import java.util.*;

import org.ofbiz.commonapp.product.product.*;

/**
 * <p><b>Title:</b> Product Feature Applicability Entity
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
public class ProductFeatureApplBean implements EntityBean
{
  /** The variable for the PRODUCT_ID column of the PRODUCT_FEATURE_APPL table. */
  public String productId;
  /** The variable for the PRODUCT_FEATURE_ID column of the PRODUCT_FEATURE_APPL table. */
  public String productFeatureId;
  /** The variable for the PRODUCT_FEATURE_APPL_TYPE_ID column of the PRODUCT_FEATURE_APPL table. */
  public String productFeatureApplTypeId;
  /** The variable for the FROM_DATE column of the PRODUCT_FEATURE_APPL table. */
  public java.util.Date fromDate;
  /** The variable for the THRU_DATE column of the PRODUCT_FEATURE_APPL table. */
  public java.util.Date thruDate;

  EntityContext entityContext;
  boolean ejbIsModified = false;

  /** Sets the EntityContext attribute of the ProductFeatureApplBean object
   *@param  entityContext  The new EntityContext value
   */
  public void setEntityContext(EntityContext entityContext) { this.entityContext = entityContext; }

  /** Get the primary key PRODUCT_ID column of the PRODUCT_FEATURE_APPL table. */
  public String getProductId() { return productId; }

  /** Get the primary key PRODUCT_FEATURE_ID column of the PRODUCT_FEATURE_APPL table. */
  public String getProductFeatureId() { return productFeatureId; }

  /** Get the value of the PRODUCT_FEATURE_APPL_TYPE_ID column of the PRODUCT_FEATURE_APPL table. */
  public String getProductFeatureApplTypeId() { return productFeatureApplTypeId; }
  /** Set the value of the PRODUCT_FEATURE_APPL_TYPE_ID column of the PRODUCT_FEATURE_APPL table. */
  public void setProductFeatureApplTypeId(String productFeatureApplTypeId)
  {
    this.productFeatureApplTypeId = productFeatureApplTypeId;
    ejbIsModified = true;
  }

  /** Get the value of the FROM_DATE column of the PRODUCT_FEATURE_APPL table. */
  public java.util.Date getFromDate() { return fromDate; }
  /** Set the value of the FROM_DATE column of the PRODUCT_FEATURE_APPL table. */
  public void setFromDate(java.util.Date fromDate)
  {
    this.fromDate = fromDate;
    ejbIsModified = true;
  }

  /** Get the value of the THRU_DATE column of the PRODUCT_FEATURE_APPL table. */
  public java.util.Date getThruDate() { return thruDate; }
  /** Set the value of the THRU_DATE column of the PRODUCT_FEATURE_APPL table. */
  public void setThruDate(java.util.Date thruDate)
  {
    this.thruDate = thruDate;
    ejbIsModified = true;
  }

  /** Sets the values from ValueObject attribute of the ProductFeatureApplBean object
   *@param  valueObject  The new ValueObject value 
   */
  public void setValueObject(ProductFeatureAppl valueObject)
  {
    try
    {
      //check for null and if null do not set; this is the method for not setting certain fields while setting the rest quickly
      // to set a field to null, use the individual setters
      if(valueObject.getProductFeatureApplTypeId() != null)
      {
        this.productFeatureApplTypeId = valueObject.getProductFeatureApplTypeId();
        ejbIsModified = true;
      }
      if(valueObject.getFromDate() != null)
      {
        this.fromDate = valueObject.getFromDate();
        ejbIsModified = true;
      }
      if(valueObject.getThruDate() != null)
      {
        this.thruDate = valueObject.getThruDate();
        ejbIsModified = true;
      }
    }
    catch(java.rmi.RemoteException re)
    {
      //This should NEVER happen just calling getters on a value object, so do nothing.
      //The only reason these methods are declated to throw a RemoteException is to implement the corresponding EJBObject interface.
    }
  }

  /** Gets the ValueObject attribute of the ProductFeatureApplBean object
   *@return    The ValueObject value
   */
  public ProductFeatureAppl getValueObject()
  {
    if(this.entityContext != null)
    {
      return new ProductFeatureApplValue((ProductFeatureAppl)this.entityContext.getEJBObject(), productId, productFeatureId, productFeatureApplTypeId, fromDate, thruDate);
    }
    else { return null; }
  }


  /** Get the  ProductFeatureApplType entity corresponding to this entity. */
  public ProductFeatureApplType getProductFeatureApplType() { return ProductFeatureApplTypeHelper.findByPrimaryKey(productFeatureApplTypeId); }
  /** Remove the  ProductFeatureApplType entity corresponding to this entity. */
  public void removeProductFeatureApplType() { ProductFeatureApplTypeHelper.removeByPrimaryKey(productFeatureApplTypeId); }

  /** Get the  Product entity corresponding to this entity. */
  public Product getProduct() { return ProductHelper.findByPrimaryKey(productId); }
  /** Remove the  Product entity corresponding to this entity. */
  public void removeProduct() { ProductHelper.removeByPrimaryKey(productId); }

  /** Get the  ProductFeature entity corresponding to this entity. */
  public ProductFeature getProductFeature() { return ProductFeatureHelper.findByPrimaryKey(productFeatureId); }
  /** Remove the  ProductFeature entity corresponding to this entity. */
  public void removeProductFeature() { ProductFeatureHelper.removeByPrimaryKey(productFeatureId); }


  /** Description of the Method
   *@param  productId                  Field of the PRODUCT_ID column.
   *@param  productFeatureId                  Field of the PRODUCT_FEATURE_ID column.
   *@param  productFeatureApplTypeId                  Field of the PRODUCT_FEATURE_APPL_TYPE_ID column.
   *@param  fromDate                  Field of the FROM_DATE column.
   *@param  thruDate                  Field of the THRU_DATE column.
   *@return                      Description of the Returned Value
   *@exception  CreateException  Description of Exception
   */
  public org.ofbiz.commonapp.product.feature.ProductFeatureApplPK ejbCreate(String productId, String productFeatureId, String productFeatureApplTypeId, java.util.Date fromDate, java.util.Date thruDate) throws CreateException
  {
    this.productId = productId;
    this.productFeatureId = productFeatureId;
    this.productFeatureApplTypeId = productFeatureApplTypeId;
    this.fromDate = fromDate;
    this.thruDate = thruDate;
    return null;
  }

  /** Description of the Method
   *@param  productId                  Field of the PRODUCT_ID column.
   *@param  productFeatureId                  Field of the PRODUCT_FEATURE_ID column.
   *@return                      Description of the Returned Value
   *@exception  CreateException  Description of Exception
   */
  public org.ofbiz.commonapp.product.feature.ProductFeatureApplPK ejbCreate(String productId, String productFeatureId) throws CreateException
  {
    return ejbCreate(productId, productFeatureId, null, null, null);
  }

  /** Description of the Method
   *@param  productId                  Field of the PRODUCT_ID column.
   *@param  productFeatureId                  Field of the PRODUCT_FEATURE_ID column.
   *@param  productFeatureApplTypeId                  Field of the PRODUCT_FEATURE_APPL_TYPE_ID column.
   *@param  fromDate                  Field of the FROM_DATE column.
   *@param  thruDate                  Field of the THRU_DATE column.
   *@exception  CreateException  Description of Exception
   */
  public void ejbPostCreate(String productId, String productFeatureId, String productFeatureApplTypeId, java.util.Date fromDate, java.util.Date thruDate) throws CreateException {}

  /** Description of the Method
   *@param  productId                  Field of the PRODUCT_ID column.
   *@param  productFeatureId                  Field of the PRODUCT_FEATURE_ID column.
   *@exception  CreateException  Description of Exception
   */
  public void ejbPostCreate(String productId, String productFeatureId) throws CreateException
  {
    ejbPostCreate(productId, productFeatureId, null, null, null);
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
