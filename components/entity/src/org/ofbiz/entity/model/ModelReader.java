/*
 * $Id: ModelReader.java,v 1.15 2004/08/09 23:52:24 jonesde Exp $
 *
 *  Copyright (c) 2001-2004 The Open For Business Project - www.ofbiz.org
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a
 *  copy of this software and associated documentation files (the "Software"),
 *  to deal in the Software without restriction, including without limitation
 *  the rights to use, copy, modify, merge, publish, distribute, sublicense,
 *  and/or sell copies of the Software, and to permit persons to whom the
 *  Software is furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included
 *  in all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 *  OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 *  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 *  IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 *  CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT
 *  OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR
 *  THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.ofbiz.entity.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.ofbiz.base.component.ComponentConfig;
import org.ofbiz.base.config.GenericConfigException;
import org.ofbiz.base.config.MainResourceHandler;
import org.ofbiz.base.config.ResourceHandler;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilCache;
import org.ofbiz.base.util.UtilTimer;
import org.ofbiz.base.util.UtilXml;
import org.ofbiz.entity.GenericEntityConfException;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericModelException;
import org.ofbiz.entity.config.DelegatorInfo;
import org.ofbiz.entity.config.EntityConfigUtil;
import org.ofbiz.entity.config.EntityModelReaderInfo;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Generic Entity - Entity Definition Reader
 *
 * @author     <a href="mailto:jonesde@ofbiz.org">David E. Jones</a>
 * @author     <a href="mailto:jaz@ofbiz.org">Andy Zeneski</a>
 * @version    $Revision: 1.15 $
 * @since      2.0
 */
public class ModelReader {

    public static final String module = ModelReader.class.getName();
    public static UtilCache readers = new UtilCache("entity.ModelReader", 0, 0);

    protected Map entityCache = null;

    protected int numEntities = 0;
    protected int numViewEntities = 0;
    protected int numFields = 0;
    protected int numRelations = 0;
    protected int numAutoRelations = 0;

    protected String modelName;

    /** collection of filenames for entity definitions */
    protected Collection entityResourceHandlers;

    /** contains a collection of entity names for each ResourceHandler, populated as they are loaded */
    protected Map resourceHandlerEntities;

    /** for each entity contains a map to the ResourceHandler that the entity came from */
    protected Map entityResourceHandlerMap;

    public static ModelReader getModelReader(String delegatorName) throws GenericEntityException {
        DelegatorInfo delegatorInfo = EntityConfigUtil.getDelegatorInfo(delegatorName);

        if (delegatorInfo == null) {
            throw new GenericEntityConfException("Could not find a delegator with the name " + delegatorName);
        }

        String tempModelName = delegatorInfo.entityModelReader;
        ModelReader reader = (ModelReader) readers.get(tempModelName);

        if (reader == null) { // don't want to block here
            synchronized (ModelReader.class) {
                // must check if null again as one of the blocked threads can still enter
                reader = (ModelReader) readers.get(tempModelName);
                if (reader == null) {
                    reader = new ModelReader(tempModelName);
                    // preload caches...
                    reader.getEntityCache();
                    readers.put(tempModelName, reader);
                }
            }
        }
        return reader;
    }

    public ModelReader(String modelName) throws GenericEntityException {
        this.modelName = modelName;
        entityResourceHandlers = new LinkedList();
        resourceHandlerEntities = new HashMap();
        entityResourceHandlerMap = new HashMap();

        EntityModelReaderInfo entityModelReaderInfo = EntityConfigUtil.getEntityModelReaderInfo(modelName);

        if (entityModelReaderInfo == null) {
            throw new GenericEntityConfException("Cound not find an entity-model-reader with the name " + modelName);
        }

        // get all of the main resource model stuff, ie specified in the entityengine.xml file
        List resourceElements = entityModelReaderInfo.resourceElements;
        Iterator resIter = resourceElements.iterator();
        while (resIter.hasNext()) {
            Element resourceElement = (Element) resIter.next();
            ResourceHandler handler = new MainResourceHandler(EntityConfigUtil.ENTITY_ENGINE_XML_FILENAME, resourceElement);
            entityResourceHandlers.add(handler);
        }
        
        // get all of the component resource model stuff, ie specified in each ofbiz-component.xml file
        List componentResourceInfos = ComponentConfig.getAllEntityResourceInfos("model");
        Iterator componentResourceInfoIter = componentResourceInfos.iterator();
        while (componentResourceInfoIter.hasNext()) {
            ComponentConfig.EntityResourceInfo componentResourceInfo = (ComponentConfig.EntityResourceInfo) componentResourceInfoIter.next();
            if (modelName.equals(componentResourceInfo.readerName)) {
                entityResourceHandlers.add(componentResourceInfo.createResourceHandler());
            }
        }
    }

    public Map getEntityCache() throws GenericEntityException {
        if (entityCache == null) { // don't want to block here
            synchronized (ModelReader.class) {
                // must check if null again as one of the blocked threads can still enter
                if (entityCache == null) { // now it's safe
                    numEntities = 0;
                    numViewEntities = 0;
                    numFields = 0;
                    numRelations = 0;
                    numAutoRelations = 0;

                    entityCache = new HashMap();
                    List tempViewEntityList = new LinkedList();

                    UtilTimer utilTimer = new UtilTimer();
                    
                    Iterator rhIter = entityResourceHandlers.iterator();
                    while (rhIter.hasNext()) {
                        ResourceHandler entityResourceHandler = (ResourceHandler) rhIter.next();

                        // utilTimer.timerString("Before getDocument in file " + entityFileName);
                        Document document = null;

                        try {
                            document = entityResourceHandler.getDocument();
                        } catch (GenericConfigException e) {
                            throw new GenericEntityConfException("Error getting document from resource handler", e);
                        }
                        if (document == null) {
                            throw new GenericEntityConfException("Could not get document for " + entityResourceHandler.toString());
                        }

                        // utilTimer.timerString("Before getDocumentElement in " + entityResourceHandler.toString());
                        Element docElement = document.getDocumentElement();

                        if (docElement == null) {
                            entityCache = null;
                            return null;
                        }
                        docElement.normalize();
                        Node curChild = docElement.getFirstChild();

                        ModelInfo def = new ModelInfo();
                        def.populateFromElements(docElement);
                        int i = 0;

                        if (curChild != null) {
                            utilTimer.timerString("Before start of entity loop in " + entityResourceHandler.toString());
                            do {
                                boolean isEntity = "entity".equals(curChild.getNodeName());
                                boolean isViewEntity = "view-entity".equals(curChild.getNodeName());

                                if ((isEntity || isViewEntity) && curChild.getNodeType() == Node.ELEMENT_NODE) {
                                    i++;
                                    Element curEntity = (Element) curChild;
                                    String entityName = UtilXml.checkEmpty(curEntity.getAttribute("entity-name"));

                                    // add entityName to appropriate resourceHandlerEntities collection
                                    Collection resourceHandlerEntityNames = (Collection) resourceHandlerEntities.get(entityResourceHandler);

                                    if (resourceHandlerEntityNames == null) {
                                        resourceHandlerEntityNames = new LinkedList();
                                        resourceHandlerEntities.put(entityResourceHandler, resourceHandlerEntityNames);
                                    }
                                    resourceHandlerEntityNames.add(entityName);

                                    // check to see if entity with same name has already been read
                                    if (entityCache.containsKey(entityName)) {
                                        Debug.logWarning("WARNING: Entity " + entityName +
                                            " is defined more than once, most recent will over-write " +
                                            "previous definition(s)", module);
                                        Debug.logWarning("WARNING: Entity " + entityName + " was found in " +
                                            entityResourceHandler + ", but was already defined in " +
                                            entityResourceHandlerMap.get(entityName).toString(), module);
                                    }

                                    // add entityName, entityFileName pair to entityResourceHandlerMap map
                                    entityResourceHandlerMap.put(entityName, entityResourceHandler);

                                    // utilTimer.timerString("  After entityEntityName -- " + i + " --");
                                    // ModelEntity entity = createModelEntity(curEntity, utilTimer);

                                    ModelEntity entity = null;

                                    if (isEntity) {
                                        entity = createModelEntity(curEntity, null, def);
                                    } else {
                                        entity = createModelViewEntity(curEntity, null, def);
                                        // put the view entity in a list to get ready for the second pass to populate fields...
                                        tempViewEntityList.add(entity);
                                    }

                                    // utilTimer.timerString("  After createModelEntity -- " + i + " --");
                                    if (entity != null) {
                                        entityCache.put(entityName, entity);
                                        // utilTimer.timerString("  After entityCache.put -- " + i + " --");
                                        if (isEntity) {
                                            if (Debug.verboseOn()) Debug.logVerbose("-- [Entity]: #" + i + ": " + entityName, module);
                                        } else {
                                            if (Debug.verboseOn()) Debug.logVerbose("-- [ViewEntity]: #" + i + ": " + entityName, module);
                                        }
                                    } else {
                                        Debug.logWarning("-- -- ENTITYGEN ERROR:getModelEntity: Could not create " +
                                            "entity for entityName: " + entityName, module);
                                    }

                                }
                            } while ((curChild = curChild.getNextSibling()) != null);
                        } else {
                            Debug.logWarning("No child nodes found.", module);
                        }
                        utilTimer.timerString("Finished " + entityResourceHandler.toString() + " - Total Entities: " + i + " FINISHED");
                    }

                    // do a pass on all of the view entities now that all of the entities have
                    // loaded and populate the fields
                    for (int velInd = 0; velInd < tempViewEntityList.size(); velInd++) {
                        ModelViewEntity curViewEntity = (ModelViewEntity) tempViewEntityList.get(velInd);
                        curViewEntity.populateFields(this);
                        List memberEntities = curViewEntity.getAllModelMemberEntities();
                        for (int j = 0; j < memberEntities.size(); j++) {
                            ModelViewEntity.ModelMemberEntity mve = (ModelViewEntity.ModelMemberEntity) memberEntities.get(j);
                            ModelEntity me = (ModelEntity) entityCache.get(mve.getEntityName());
                            if (me == null) throw new GenericEntityConfException("View " + curViewEntity.getEntityName() + " references non-existant entity: " + mve.getEntityName());
                            me.addViewEntity(curViewEntity);
                        }
                    }
                    
                    // auto-create relationships
                    TreeSet orderedMessages = new TreeSet();
                    Iterator entityNamesIter = new TreeSet(this.getEntityNames()).iterator();
                    while (entityNamesIter.hasNext()) {
                        String curEntityName = (String) entityNamesIter.next();
                        ModelEntity curModelEntity = this.getModelEntity(curEntityName);
                        if (curModelEntity instanceof ModelViewEntity) {
                            // for view-entities auto-create relationships for all member-entity relationships that have all corresponding fields in the view-entity
                            
                        } else {
                            // for entities auto-create many relationships for all type one relationships
                            
                            // just in case we add a new relation to the same entity, keep in a separate list and add them at the end
                            List newSameEntityRelations = new LinkedList();
                            
                            Iterator relationsIter = curModelEntity.getRelationsIterator();
                            while (relationsIter.hasNext()) {
                                ModelRelation modelRelation = (ModelRelation) relationsIter.next();
                                if (("one".equals(modelRelation.getType()) || "one-nofk".equals(modelRelation.getType())) && !modelRelation.isAutoRelation()) {
                                    ModelEntity relatedEnt = this.getModelEntity(modelRelation.getRelEntityName());
                                    if (relatedEnt != null) {
                                        // don't do relationship to the same entity, unless title is "Parent", then do a "Child" automatically
                                        String targetTitle = modelRelation.getTitle();
                                        if (curModelEntity.getEntityName().equals(relatedEnt.getEntityName()) && "Parent".equals(targetTitle)) {
                                            targetTitle = "Child";
                                        }
                                        
                                        // create the new relationship even if one exists so we can show what we are looking for in the info message
                                        ModelRelation newRel = new ModelRelation();
                                        newRel.setModelEntity(relatedEnt);
                                        newRel.setRelEntityName(curModelEntity.getEntityName());
                                        newRel.setTitle(targetTitle);
                                        newRel.setAutoRelation(true);
                                        Set curEntityKeyFields = new HashSet();
                                        for (int kmn = 0; kmn < modelRelation.getKeyMapsSize(); kmn++) {
                                            ModelKeyMap curkm = modelRelation.getKeyMap(kmn);
                                            ModelKeyMap newkm = new ModelKeyMap();
                                            newRel.addKeyMap(newkm);
                                            newkm.setFieldName(curkm.getRelFieldName());
                                            newkm.setRelFieldName(curkm.getFieldName());
                                            curEntityKeyFields.add(curkm.getFieldName());
                                        }
                                        // decide whether it should be one or many by seeing if the key map represents the complete pk of the relEntity
                                        if (curModelEntity.containsAllPkFieldNames(curEntityKeyFields)) {
                                            // always use one-nofk, we don't want auto-fks getting in for these automatic ones
                                            newRel.setType("one-nofk");
                                            
                                            // to keep it clean, remove any additional keys that aren't part of the PK
                                            List curPkFieldNames = curModelEntity.getPkFieldNames();
                                            Iterator nrkmIter = newRel.getKeyMapsIterator();
                                            while (nrkmIter.hasNext()) {
                                                ModelKeyMap nrkm = (ModelKeyMap) nrkmIter.next();
                                                String checkField = nrkm.getRelFieldName();
                                                if (!curPkFieldNames.contains(checkField)) {
                                                    nrkmIter.remove();
                                                }
                                            }
                                        } else {
                                            newRel.setType("many");
                                        }
                                        
                                        ModelRelation existingRelation = relatedEnt.getRelation(targetTitle + curModelEntity.getEntityName());
                                        if (existingRelation == null) {
                                            numAutoRelations++;
                                            if (curModelEntity.getEntityName().equals(relatedEnt.getEntityName())) {
                                                newSameEntityRelations.add(newRel);
                                            } else {
                                                relatedEnt.addRelation(newRel);
                                            }
                                        } else {
                                            if (newRel.equals(existingRelation)) {
                                                // don't warn if the target title+entity = current title+entity
                                                if (!(targetTitle + curModelEntity.getEntityName()).equals(modelRelation.getTitle() + modelRelation.getRelEntityName())) {
                                                    //String errorMsg = "Relation already exists to entity [] with title [" + targetTitle + "],from entity []";
                                                    String message = "Entity [" + relatedEnt.getPackageName() + ":" + relatedEnt.getEntityName() + "] already has identical relationship to entity [" + 
                                                            curModelEntity.getEntityName() + "] title [" + targetTitle + "]; would auto-create: type [" + 
                                                            newRel.getType() + "] and fields [" + newRel.keyMapString(",", "") + "]";
                                                    orderedMessages.add(message);
                                                }
                                            } else {
                                                String message = "Existing relationship with the same name, but different specs found from what would be auto-created for Entity [" + relatedEnt.getEntityName() + "] ant relationship to entity [" + 
                                                        curModelEntity.getEntityName() + "] title [" + targetTitle + "]; would auto-create: type [" + 
                                                        newRel.getType() + "] and fields [" + newRel.keyMapString(",", "") + "]";
                                                //Debug.logInfo(message, module);
                                            }
                                        }
                                    } else {
                                        String errorMsg = "Could not find related entity ["
                                                + modelRelation.getRelEntityName() + "], no reverse relation added.";
                                        Debug.logWarning(errorMsg, module);
                                    }
                                }
                            }
                            
                            if (newSameEntityRelations.size() > 0) {
                                Iterator newRelsIter = newSameEntityRelations.iterator();
                                while (newRelsIter.hasNext()) {
                                    ModelRelation newRel = (ModelRelation) newRelsIter.next();
                                    curModelEntity.addRelation(newRel);
                                }
                            }
                        }
                    }
                    
                    Iterator omIter = orderedMessages.iterator();
                    while (omIter.hasNext()) {
                        Debug.logInfo((String) omIter.next(), module);
                    }

                    Debug.log("FINISHED LOADING ENTITIES - ALL FILES; #Entities=" + numEntities + " #ViewEntities=" +
                        numViewEntities + " #Fields=" + numFields + " #Relationships=" + numRelations + " #AutoRelationships=" + numAutoRelations, module);
                }
            }
        }
        return entityCache;
    }

    /** rebuilds the resourceHandlerEntities Map of Collections based on the current
     *  entityResourceHandlerMap Map, must be done whenever a manual change is made to the
     *  entityResourceHandlerMap Map after the initial load to make them consistent again.
     */
    public void rebuildResourceHandlerEntities() {
        resourceHandlerEntities = new HashMap();
        Iterator entityResourceIter = entityResourceHandlerMap.entrySet().iterator();

        while (entityResourceIter.hasNext()) {
            Map.Entry entry = (Map.Entry) entityResourceIter.next();
            // add entityName to appropriate resourceHandlerEntities collection
            Collection resourceHandlerEntityNames = (Collection) resourceHandlerEntities.get(entry.getValue());

            if (resourceHandlerEntityNames == null) {
                resourceHandlerEntityNames = new LinkedList();
                resourceHandlerEntities.put(entry.getValue(), resourceHandlerEntityNames);
            }
            resourceHandlerEntityNames.add(entry.getKey());
        }
    }

    public Iterator getResourceHandlerEntitiesKeyIterator() {
        if (resourceHandlerEntities == null) return null;
        return resourceHandlerEntities.keySet().iterator();
    }

    public Collection getResourceHandlerEntities(ResourceHandler resourceHandler) {
        if (resourceHandlerEntities == null) return null;
        return (Collection) resourceHandlerEntities.get(resourceHandler);
    }

    public void addEntityToResourceHandler(String entityName, String loaderName, String location) {
        entityResourceHandlerMap.put(entityName, new MainResourceHandler(EntityConfigUtil.ENTITY_ENGINE_XML_FILENAME, loaderName, location));
    }

    public ResourceHandler getEntityResourceHandler(String entityName) {
        return (ResourceHandler) entityResourceHandlerMap.get(entityName);
    }

    /** Gets an Entity object based on a definition from the specified XML Entity descriptor file.
     * @param entityName The entityName of the Entity definition to use.
     * @return An Entity object describing the specified entity of the specified descriptor file.
     */
    public ModelEntity getModelEntity(String entityName) throws GenericEntityException {
        Map ec = getEntityCache();
        if (ec == null) {
            throw new GenericEntityConfException("ERROR: Unable to load Entity Cache");
        }
        ModelEntity modelEntity = (ModelEntity) ec.get(entityName);
        if (modelEntity == null) {
            throw new GenericModelException("Could not find definition for entity name " + entityName);
        }
        return modelEntity;
    }

    public ModelEntity getModelEntityNoCheck(String entityName) {
        Map ec = null;
        try {
            ec = getEntityCache();
        } catch (GenericEntityException e) {
            Debug.logError(e, "Error getting entity cache", module);
        }
        if (ec == null) {
            return null;
        }
        ModelEntity modelEntity = (ModelEntity) ec.get(entityName);
        return modelEntity;
    }

    /** Creates a Iterator with the entityName of each Entity defined in the specified XML Entity Descriptor file.
     * @return A Iterator of entityName Strings
     */
    public Iterator getEntityNamesIterator() throws GenericEntityException {
        Collection collection = getEntityNames();
        if (collection != null) {
            return collection.iterator();
        } else {
            return null;
        }
    }

    /** Creates a Collection with the entityName of each Entity defined in the specified XML Entity Descriptor file.
     * @return A Collection of entityName Strings
     */
    public Collection getEntityNames() throws GenericEntityException {
        Map ec = getEntityCache();
        if (ec == null) {
            throw new GenericEntityConfException("ERROR: Unable to load Entity Cache");
        }
        return ec.keySet();
    }

    ModelEntity createModelEntity(Element entityElement, UtilTimer utilTimer, ModelInfo def) {
        if (entityElement == null) return null;
        this.numEntities++;
        ModelEntity entity = new ModelEntity(this, entityElement, utilTimer, def);
        return entity;
    }

    ModelEntity createModelViewEntity(Element entityElement, UtilTimer utilTimer, ModelInfo def) {
        if (entityElement == null) return null;
        this.numViewEntities++;
        ModelViewEntity entity = new ModelViewEntity(this, entityElement, utilTimer, def);
        return entity;
    }

    public ModelRelation createRelation(ModelEntity entity, Element relationElement) {
        this.numRelations++;
        ModelRelation relation = new ModelRelation(entity, relationElement);
        return relation;
    }

    public ModelField findModelField(ModelEntity entity, String fieldName) {
        for (int i = 0; i < entity.fields.size(); i++) {
            ModelField field = (ModelField) entity.fields.get(i);
            if (field.name.compareTo(fieldName) == 0) {
                return field;
            }
        }
        return null;
    }

    public ModelField createModelField(String name, String type, String colName, boolean isPk) {
        this.numFields++;
        ModelField field = new ModelField(name, type, colName, isPk);
        return field;
    }
    
    public ModelField createModelField(Element fieldElement) {
        if (fieldElement == null) {
            return null;
        }

        this.numFields++;
        ModelField field = new ModelField(fieldElement);
        return field;
    }
}
