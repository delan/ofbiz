/*
 * $Id$
 *
 * Copyright (c) 2001 The Open For Business Project - www.ofbiz.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT
 * OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR
 * THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package org.ofbiz.core.entity.model;

import java.util.*;
import org.ofbiz.core.util.*;

/**
 * This class extends ModelEntity and provides additional information appropriate to view entities
 *
 *@author     <a href="mailto:jonesde@ofbiz.org">David E. Jones</a>
 *@created    November 9, 2001
 *@version    1.0
 */
public class ModelViewEntity extends ModelEntity {

    /** Contains member-entity alias name definitions: key is alias, value is entity-name */
    protected Map memberEntityNames = new HashMap();
    /** Contains member-entity ModelEntities: key is alias, value is ModelEntity; populated with fields */
    protected Map memberModelEntities = null;
    /** List of aliases with information in addition to what is in the standard field list */
    public Vector aliases = new Vector();
    /** List of view links to define how entities are connected (or "joined") */
    public Vector viewLinks = new Vector();
    
    public Map getMemberEntityNames() {
        return this.memberEntityNames;
    }
    
    public ModelEntity getMemberModelEntity(String alias) {
        if (this.memberModelEntities == null) {
            this.memberModelEntities = new HashMap();
            populateFields(this.getModelReader().entityCache);
        }
        return (ModelEntity) this.memberModelEntities.get(alias);
    }

    public void addMemberEntityName(String alias, String aliasedEntityName) {
        this.memberEntityNames.put(alias, aliasedEntityName);
    }
    
    public void populateFields(Map entityCache) {
        if (this.memberModelEntities == null) {
            this.memberModelEntities = new HashMap();
        }
        Iterator meIter = memberEntityNames.entrySet().iterator();
        while (meIter.hasNext()) {
            Map.Entry entry = (Map.Entry) meIter.next();

            String aliasedEntityName = (String) entry.getValue();
            ModelEntity aliasedEntity = (ModelEntity) entityCache.get(aliasedEntityName);
            if (aliasedEntity == null) {
                Debug.logError("[ModelViewEntity.populateFields] ERROR: could not find ModelEntity for entity name: " +
                               aliasedEntityName);
                continue;
            }
            memberModelEntities.put(entry.getKey(), aliasedEntity);
        }

        for (int i = 0; i < aliases.size(); i++) {
            ModelAlias alias = (ModelAlias) aliases.get(i);

            String aliasedEntityName = (String) memberEntityNames.get(alias.entityAlias);
            ModelEntity aliasedEntity = (ModelEntity) entityCache.get(aliasedEntityName);
            if (aliasedEntity == null) {
                Debug.logError("[ModelViewEntity.populateFields] ERROR: could not find ModelEntity for entity name: " +
                               aliasedEntityName);
                continue;
            }

            ModelField field = new ModelField();
            field.name = alias.name;
            field.isPk = alias.isPk;

            this.fields.add(field);
            if (field.isPk)
                this.pks.add(field);
            else
                this.nopks.add(field);

            ModelField aliasedField = aliasedEntity.getField(alias.field);
            if (aliasedField == null) {
                Debug.logError("[ModelViewEntity.populateFields] ERROR: could not find ModelField for field name \"" +
                               alias.field + "\" on entity with name: " + aliasedEntityName);
                continue;
            }
            field.type = aliasedField.type;
            field.colName = alias.entityAlias + "." + aliasedField.colName;
            field.validators = aliasedField.validators;
        }
    }

    public ModelAlias makeModelAlias() {
        return new ModelAlias();
    }

    public ModelViewLink makeModelViewLink() {
        return new ModelViewLink();
    }

    public class ModelAlias {

        public String entityAlias = "";
        public String name = "";
        public String field = "";
        public boolean isPk = false;
    }

    public class ModelViewLink {

        public String entityAlias = "";
        public String relEntityAlias = "";
        public Vector keyMaps = new Vector();
    }
}

