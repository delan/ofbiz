/*
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
 */

package org.ofbiz.core.entity;

import java.util.*;
import org.ofbiz.core.util.*;
import org.ofbiz.core.entity.model.*;

/**
 * Generic Entity Helper Class
 *
 *@author     <a href="mailto:jonesde@ofbiz.org">David E. Jones</a>
 *@author     <a href='mailto:chris_maurer@altavista.com'>Chris Maurer</a>
 *@created    Tue Aug 07 01:10:32 MDT 2001
 *@version    1.0
 */
public class GenericHelperDAO implements GenericHelper {
    GenericDAO genericDAO;
    String helperName;
    
    public GenericHelperDAO(String helperName) {
        this.helperName = helperName;
        genericDAO = GenericDAO.getGenericDAO(helperName);
    }
    
    public String getHelperName() {
        return helperName;
    }
    
    /** Creates a Entity in the form of a GenericValue and write it to the database
     *@return GenericValue instance containing the new instance
     */
    public GenericValue create(GenericValue value) throws GenericEntityException {
        if (value == null) {
            return null;
        }
        genericDAO.insert(value);
        return value;
    }
    
    /** Creates a Entity in the form of a GenericValue and write it to the database
     *@return GenericValue instance containing the new instance
     */
    public GenericValue create(GenericPK primaryKey) throws GenericEntityException {
        if (primaryKey == null) {
            return null;
        }
        GenericValue genericValue = new GenericValue(primaryKey);
        genericDAO.insert(genericValue);
        return genericValue;
    }
    
    /** Find a Generic Entity by its Primary Key
     *@param primaryKey The primary key to find by.
     *@return The GenericValue corresponding to the primaryKey
     */
    public GenericValue findByPrimaryKey(GenericPK primaryKey) throws GenericEntityException {
        if (primaryKey == null) {
            return null;
        }
        GenericValue genericValue = new GenericValue(primaryKey);
        genericDAO.select(genericValue);
        return genericValue;
    }
    
    /** Find a Generic Entity by its Primary Key and only returns the values requested by the passed keys (names)
     *@param primaryKey The primary key to find by.
     *@param keys The keys, or names, of the values to retrieve; only these values will be retrieved
     *@return The GenericValue corresponding to the primaryKey
     */
    public GenericValue findByPrimaryKeyPartial(GenericPK primaryKey, Set keys) throws GenericEntityException {
        if (primaryKey == null) {
            return null;
        }
        GenericValue genericValue = new GenericValue(primaryKey);
        genericDAO.partialSelect(genericValue, keys);
        return genericValue;
    }
    
    /** Find a number of Generic Value objects by their Primary Keys, all at once
     * This is done here for the DAO GenericHelper; for a client-server helper it
     * would be done on the server side to reduce network round trips.
     *@param primaryKeys A Collection of primary keys to find by.
     *@return Collection of GenericValue objects corresponding to the passed primaryKey objects
     */
    public Collection findAllByPrimaryKeys(Collection primaryKeys) throws GenericEntityException {
        if (primaryKeys == null) return null;
        Collection results = new LinkedList();
        
        Iterator pkiter = primaryKeys.iterator();
        while (pkiter.hasNext()) {
            GenericPK primaryKey = (GenericPK) pkiter.next();
            GenericValue result = this.findByPrimaryKey(primaryKey);
            if (result != null) results.add(result);
        }
        return results;
    }
    
    /** Remove a Generic Entity corresponding to the primaryKey
     *@param  primaryKey  The primary key of the entity to remove.
     */
    public void removeByPrimaryKey(GenericPK primaryKey) throws GenericEntityException {
        if (primaryKey == null) return;
        Debug.logInfo("Removing GenericPK: " + primaryKey.toString());
        genericDAO.delete(primaryKey);
    }
    
    /** Finds Generic Entity records by all of the specified fields (ie: combined using AND)
     *@param entityName The Name of the Entity as defined in the entity XML file
     *@param fields The fields of the named entity to query by with their corresponging values
     *@param order The fields of the named entity to order the query by; optionally add a " ASC" for ascending or " DESC" for descending
     *@return Collection of GenericValue instances that match the query
     */
    public Collection findByAnd(ModelEntity modelEntity, Map fields, List orderBy) throws GenericEntityException {
        return genericDAO.selectByAnd(modelEntity, fields, orderBy);
    }
    public Collection findByAnd(ModelEntity modelEntity, List expressions, List orderBy) throws GenericEntityException {
        return genericDAO.selectByAnd(modelEntity, expressions, orderBy);
    }
    public Collection findByLike(ModelEntity modelEntity, Map fields, List orderBy) throws GenericEntityException {
        return genericDAO.selectByLike(modelEntity, fields, orderBy);
    }
    public Collection findByClause(ModelEntity modelEntity, List entityClauses, Map fields, List orderBy) throws GenericEntityException {
        return genericDAO.selectByClause(modelEntity, entityClauses, fields, orderBy);
    }
    
    /** Finds Generic Entity records by all of the specified fields (ie: combined using OR)
     *@param entityName The Name of the Entity as defined in the entity XML file
     *@param fields The fields of the named entity to query by with their corresponging values
     *@param order The fields of the named entity to order the query by; optionally add a " ASC" for ascending or " DESC" for descending
     *@return Collection of GenericValue instances that match the query
     */
    public Collection findByOr(ModelEntity modelEntity, Map fields, List orderBy) throws GenericEntityException {
        return genericDAO.selectByOr(modelEntity, fields, orderBy);
    }
    public Collection findByOr(ModelEntity modelEntity, List expressions, List orderBy) throws GenericEntityException {
        return genericDAO.selectByOr(modelEntity, expressions, orderBy);
    }
    
    /** Removes/deletes Generic Entity records found by all of the specified fields (ie: combined using AND)
     *@param entityName The Name of the Entity as defined in the entity XML file
     *@param fields The fields of the named entity to query by with their corresponging values
     *@return Collection of GenericValue instances that match the query
     */
    public void removeByAnd(ModelEntity modelEntity, Map fields) throws GenericEntityException {
        if (modelEntity == null || fields == null) {
            return;
        }
        genericDAO.deleteByAnd(modelEntity, fields);
    }
    
    /** Store the Entity from the GenericValue to the persistent store
     *@param value GenericValue instance containing the entity
     */
    public void store(GenericValue value) throws GenericEntityException {
        if (value == null) {
            return;
        }
        genericDAO.update(value);
    }
    
    /** Store the Entities from the Collection GenericValue instances to the persistent store.
     *  This is different than the normal store method in that the store method only does
     *  an update, while the storeAll method checks to see if each entity exists, then
     *  either does an insert or an update as appropriate.
     *  These updates all happen in one transaction, so they will either all succeed or all fail,
     *  if the data source supports transactions. This is just like to othersToStore feature
     *  of the GenericEntity on a create or store.
     *@param values Collection of GenericValue instances containing the entities to store
     */
    public void storeAll(Collection values) throws GenericEntityException {
        genericDAO.storeAll(values);
    }
    
    /** Remove the Entities from the Collection from the persistent store.
     *  <br>The Collection contains GenericEntity objects, can be either GenericPK or GenericValue.
     *  <br>If a certain entity contains a complete primary key, the entity in the datasource corresponding
     *  to that primary key will be removed, this is like a removeByPrimary Key.
     *  <br>On the other hand, if a certain entity is an incomplete or non primary key,
     *  if will behave like the removeByAnd method.
     *  <br>These updates all happen in one transaction, so they will either all succeed or all fail,
     *  if the data source supports transactions.
     *@param dummyPKs Collection of GenericEntity instances containing the entities or by and fields to remove
     */
    public void removeAll(Collection dummyPKs) throws GenericEntityException {
        genericDAO.deleteAll(dummyPKs);
    }
    
    /** Check the datasource to make sure the entity definitions are correct, optionally adding missing entities or fields on the server
     *@param modelEntities Map of entityName names and ModelEntity values
     *@param messages Collection to put any result messages in
     *@param addMissing Flag indicating whether or not to add missing entities and fields on the server
     */
    public void checkDataSource(Map modelEntities, Collection messages, boolean addMissing) throws GenericEntityException {
        genericDAO.checkDb(modelEntities, messages, addMissing);
    }
}
