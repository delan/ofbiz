package org.ofbiz.core.entity;

import java.util.*;
import org.ofbiz.core.util.*;
import org.ofbiz.core.entity.model.*;

/**
 * <p><b>Title:</b> Generic Entity Helper Class
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
 *@created    Tue Aug 07 01:10:32 MDT 2001
 *@version    1.0
 */
public class GenericHelperDAO extends GenericHelperAbstract
{
  GenericDAO genericDAO;
  
  public GenericHelperDAO(String serverName)
  { 
    this.serverName = serverName;
    
    genericDAO = GenericDAO.getGenericDAO(serverName);
    primaryKeyCache = new UtilCache("FindByPrimaryKeyDAO-" + serverName);
    allCache = new UtilCache("FindAllDAO-" + serverName);
    andCache = new UtilCache("FindByAndDAO-" + serverName);

    modelReader = ModelReader.getModelReader(serverName);
  }

  /** Creates a Entity in the form of a GenericValue and write it to the database
   *@return GenericValue instance containing the new instance
   */
  public GenericValue create(String entityName, Map fields)
  {
    if(entityName == null || fields == null) { return null; }
    GenericValue genericValue = new GenericValue(modelReader.getModelEntity(entityName), fields);
    if(!genericDAO.insert(genericValue)) return null;
    genericValue.helper = this;
    return genericValue;
  }

  /** Creates a Entity in the form of a GenericValue and write it to the database
   *@return GenericValue instance containing the new instance
   */
  public GenericValue create(GenericValue value)
  {
    if(value == null) { return null; }
    if(!genericDAO.insert(value)) return null;
    value.helper = this;
    return value;
  }

  /** Creates a Entity in the form of a GenericValue and write it to the database
   *@return GenericValue instance containing the new instance
   */
  public GenericValue create(GenericPK primaryKey)
  {
    if(primaryKey == null) { return null; }
    GenericValue genericValue = new GenericValue(primaryKey);
    if(!genericDAO.insert(genericValue)) return null;
    genericValue.helper = this;
    return genericValue;
  }

  /** Find a Generic Entity by its Primary Key
   *@param primaryKey The primary key to find by.
   *@return The GenericValue corresponding to the primaryKey
   */
  public GenericValue findByPrimaryKey(GenericPK primaryKey)
  {
    if(primaryKey == null) { return null; }
    GenericValue genericValue = new GenericValue(primaryKey);
    if(!genericDAO.select(genericValue)) return null;
    genericValue.helper = this;
    return genericValue;
  }

  /** Remove a Generic Entity corresponding to the primaryKey
   *@param  primaryKey  The primary key of the entity to remove.
   */
  public void removeByPrimaryKey(GenericPK primaryKey)
  {
    if(primaryKey == null) return;
    try 
    { 
      Debug.logInfo("Removing GenericPK: " + primaryKey.toString());
      genericDAO.delete(primaryKey);
    }
    catch(Exception e) { Debug.logWarning(e); }
  }

  /** Finds all Generic entities
   *@param entityName The Name of the Entity as defined in the entity XML file
   *@param order The fields of the named entity to order the query by; optionall add a " ASC" for ascending or " DESC" for descending
   *@return    Collection containing all Generic entities
   */
  public Collection findAll(String entityName, List orderBy)
  {
    Collection collection = null;
    collection = genericDAO.selectByAnd(entityName, null, orderBy);
    absorbCollection(collection);
    return collection;
  }

  /** Finds Generic Entity records by all of the specified fields (ie: combined using AND)
   *@param entityName The Name of the Entity as defined in the entity XML file
   *@param fields The fields of the named entity to query by with their corresponging values
   *@param order The fields of the named entity to order the query by; optionall add a " ASC" for ascending or " DESC" for descending
   *@return Collection of GenericValue instances that match the query
   */
  public Collection findByAnd(String entityName, Map fields, List orderBy)
  {
    Collection collection = null;
    collection = genericDAO.selectByAnd(entityName, fields, orderBy);
    absorbCollection(collection);
    return collection;
  }
  
  /** Removes/deletes Generic Entity records found by all of the specified fields (ie: combined using AND)
   *@param entityName The Name of the Entity as defined in the entity XML file
   *@param fields The fields of the named entity to query by with their corresponging values
   *@return Collection of GenericValue instances that match the query
   */
  public void removeByAnd(String entityName, Map fields)
  {
    if(entityName == null || fields == null) { return; }
    Iterator iterator = UtilMisc.toIterator(findByAnd(entityName, fields, null));

    while(iterator != null && iterator.hasNext())
    {
      try
      {
        GenericValue generic = (GenericValue)iterator.next();
        Debug.logInfo("Removing GenericValue: " + generic.toString());
        generic.remove();
      }
      catch(Exception e) { Debug.logError(e); }
    }
  }
  
  /** Store the Entity from the GenericValue to the persistent store
   *@param value GenericValue instance containing the entity
   */
  public void store(GenericValue value)
  {
    if(value == null) { return; }
    if(!genericDAO.update(value)) Debug.logError("[GenericHelperDAO.store] Could not store GenericValue: " + value.toString());
    value.helper = this;
  }
  
  /** Get the named Related Entity for the GenericValue from the persistent store
   *@param relationName String containing the relation name which is the combination of relation.title and relation.rel-entity-name as specified in the entity XML definition file
   *@param value GenericValue instance containing the entity
   *@return Collection of GenericValue instances as specified in the relation definition
   */
  public Collection getRelated(String relationName, GenericValue value)
  {
    Collection col = genericDAO.selectRelated(relationName, value);
    absorbCollection(col);
    return col;    
  }  
  
  /** Remove the named Related Entity for the GenericValue from the persistent store
   * @param relationName String containing the relation name which is the combination of relation.title and relation.rel-entity-name as specified in the entity XML definition file
   * @param value GenericValue instance containing the entity
   */
  public void removeRelated(String relationName, GenericValue value)
  {
    genericDAO.deleteRelated(relationName, value);
  }
  
  private void absorbCollection(Collection col)
  {
    if(col == null) return;
    Iterator iter = col.iterator();
    while(iter.hasNext())
    {
      GenericValue value = (GenericValue)iter.next();
      value.helper = this;
    }
  }
}
