
package org.ofbiz.commonapp.product.product;

import java.util.*;
import javax.naming.InitialContext;
import javax.ejb.*;

import org.ofbiz.core.util.*;

/**
 * <p><b>Title:</b> Product Type Entity
 * <p><b>Description:</b> None
 * <p>The Helper class from the ProductType Entity EJB; acts as a proxy for the Home interface
 *
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
 *@created    Fri Jul 27 01:18:25 MDT 2001
 *@version    1.0
 */
public class ProductTypeHelper
{

  /** A static variable to cache the Home object for the ProductType EJB */
  private static ProductTypeHome productTypeHome = null;

  /** Initializes the productTypeHome, from a JNDI lookup, with a cached result, checking for null each time. 
   *@return The ProductTypeHome instance for the default EJB server
   */
  public static ProductTypeHome getProductTypeHome()
  {
    if(productTypeHome == null) //don't want to block here
    {
      synchronized(ProductTypeHelper.class) 
      { 
        //must check if null again as one of the blocked threads can still enter 
        if(productTypeHome == null) //now it's safe
        {
          JNDIContext myJNDIContext = new JNDIContext();
          InitialContext initialContext = myJNDIContext.getInitialContext();
          try
          {
            Object homeObject = MyNarrow.lookup(initialContext, "org.ofbiz.commonapp.product.product.ProductTypeHome");
            productTypeHome = (ProductTypeHome)MyNarrow.narrow(homeObject, ProductTypeHome.class);
          }
          catch(Exception e1) { Debug.logError(e1); }
          Debug.logInfo("productType home obtained " + productTypeHome);
        }
      }
    }
    return productTypeHome;
  }




  /** Remove the ProductType corresponding to the primaryKey
   *@param  primaryKey  The primary key of the entity to remove.
   */
  public static void removeByPrimaryKey(java.lang.String primaryKey)
  {
    if(primaryKey == null) return;
    ProductType productType = findByPrimaryKey(primaryKey);
    try
    {
      if(productType != null)
      {
        productType.remove();
      }
    }
    catch(Exception e) { Debug.logWarning(e); }

  }


  /** Find a ProductType by its Primary Key
   *@param  primaryKey  The primary key to find by.
   *@return             The ProductType corresponding to the primaryKey
   */
  public static ProductType findByPrimaryKey(java.lang.String primaryKey)
  {
    ProductType productType = null;
    Debug.logInfo("ProductTypeHelper.findByPrimaryKey: Field is:" + primaryKey);

    if(primaryKey == null) { return null; }


    try
    {
      productType = (ProductType)MyNarrow.narrow(getProductTypeHome().findByPrimaryKey(primaryKey), ProductType.class);
      if(productType != null)
      {
        productType = productType.getValueObject();
      
      }
    }
    catch(ObjectNotFoundException onfe) { }
    catch(Exception fe) { Debug.logError(fe); }
    return productType;
  }

  /** Finds all ProductType entities
   *@return    Collection containing all ProductType entities
   */
  public static Collection findAll()
  {
    Collection collection = null;
    Debug.logInfo("ProductTypeHelper.findAll");

    try { collection = (Collection)MyNarrow.narrow(getProductTypeHome().findAll(), Collection.class); }
    catch(ObjectNotFoundException onfe) { }
    catch(Exception fe) { Debug.logError(fe); }
    return collection;
  }

  /** Creates a ProductType
   *@param  productTypeId                  Field of the PRODUCT_TYPE_ID column.
   *@param  parentTypeId                  Field of the PARENT_TYPE_ID column.
   *@param  hasTable                  Field of the HAS_TABLE column.
   *@param  description                  Field of the DESCRIPTION column.
   *@return                Description of the Returned Value
   */
  public static ProductType create(String productTypeId, String parentTypeId, String hasTable, String description)
  {
    ProductType productType = null;
    Debug.logInfo("ProductTypeHelper.create: productTypeId: " + productTypeId);
    if(productTypeId == null) { return null; }

    try { productType = (ProductType)MyNarrow.narrow(getProductTypeHome().create(productTypeId, parentTypeId, hasTable, description), ProductType.class); }
    catch(CreateException ce)
    {
      Debug.logError("Could not create productType with productTypeId: " + productTypeId);
      Debug.logError(ce);
      productType = null;
    }
    catch(Exception fe) { Debug.logError(fe); }
    return productType;
  }

  /** Updates the corresponding ProductType
   *@param  productTypeId                  Field of the PRODUCT_TYPE_ID column.
   *@param  parentTypeId                  Field of the PARENT_TYPE_ID column.
   *@param  hasTable                  Field of the HAS_TABLE column.
   *@param  description                  Field of the DESCRIPTION column.
   *@return                Description of the Returned Value
   */
  public static ProductType update(String productTypeId, String parentTypeId, String hasTable, String description) throws java.rmi.RemoteException
  {
    if(productTypeId == null) { return null; }
    ProductType productType = findByPrimaryKey(productTypeId);
    //Do not pass the value object to set on creation, we only want to populate it not attach it to the passed object
    ProductType productTypeValue = new ProductTypeValue();

    if(parentTypeId != null) { productTypeValue.setParentTypeId(parentTypeId); }
    if(hasTable != null) { productTypeValue.setHasTable(hasTable); }
    if(description != null) { productTypeValue.setDescription(description); }

    productType.setValueObject(productTypeValue);
    return productType;
  }

  /** Removes/deletes the specified  ProductType
   *@param  parentTypeId                  Field of the PARENT_TYPE_ID column.
   */
  public static void removeByParentTypeId(String parentTypeId)
  {
    if(parentTypeId == null) return;
    Iterator iterator = UtilMisc.toIterator(findByParentTypeId(parentTypeId));

    while(iterator.hasNext())
    {
      try
      {
        ProductType productType = (ProductType) iterator.next();
        Debug.logInfo("Removing productType with parentTypeId:" + parentTypeId);
        productType.remove();
      }
      catch(Exception e) { Debug.logError(e); }
    }
  }

  /** Finds ProductType records by the following parameters:
   *@param  parentTypeId                  Field of the PARENT_TYPE_ID column.
   *@return      Description of the Returned Value
   */
  public static Collection findByParentTypeId(String parentTypeId)
  {
    Debug.logInfo("findByParentTypeId: parentTypeId:" + parentTypeId);

    Collection collection = null;
    if(parentTypeId == null) { return null; }

    try { collection = (Collection) MyNarrow.narrow(getProductTypeHome().findByParentTypeId(parentTypeId), Collection.class); }
    catch(ObjectNotFoundException onfe) { }
    catch(Exception fe) { Debug.logError(fe); }

    return collection;
  }

  /** Removes/deletes the specified  ProductType
   *@param  hasTable                  Field of the HAS_TABLE column.
   */
  public static void removeByHasTable(String hasTable)
  {
    if(hasTable == null) return;
    Iterator iterator = UtilMisc.toIterator(findByHasTable(hasTable));

    while(iterator.hasNext())
    {
      try
      {
        ProductType productType = (ProductType) iterator.next();
        Debug.logInfo("Removing productType with hasTable:" + hasTable);
        productType.remove();
      }
      catch(Exception e) { Debug.logError(e); }
    }
  }

  /** Finds ProductType records by the following parameters:
   *@param  hasTable                  Field of the HAS_TABLE column.
   *@return      Description of the Returned Value
   */
  public static Collection findByHasTable(String hasTable)
  {
    Debug.logInfo("findByHasTable: hasTable:" + hasTable);

    Collection collection = null;
    if(hasTable == null) { return null; }

    try { collection = (Collection) MyNarrow.narrow(getProductTypeHome().findByHasTable(hasTable), Collection.class); }
    catch(ObjectNotFoundException onfe) { }
    catch(Exception fe) { Debug.logError(fe); }

    return collection;
  }


}
