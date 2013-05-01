/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/
package org.ofbiz.entity.model;

import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.ofbiz.base.lang.ThreadSafe;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilXml;


/**
 * An object that models the <code>&lt;key-map&gt;</code> element.
 *
 */
@ThreadSafe
@SuppressWarnings("serial")
public final class ModelKeyMap implements java.io.Serializable {

    /*
     * Developers - this is an immutable class. Once constructed, the object should not change state.
     * Therefore, 'setter' methods are not allowed. If client code needs to modify the object's
     * state, then it can create a new copy with the changed values.
     */

    /** name of the field in this entity */
    private final String fieldName;

    /** name of the field in related entity */
    private final String relFieldName;

    /** Data Constructor, if relFieldName is null defaults to fieldName */
    public ModelKeyMap(String fieldName, String relFieldName) {
        this.fieldName = fieldName;
        this.relFieldName = UtilXml.checkEmpty(relFieldName, this.fieldName);
    }

    /** XML Constructor */
    public ModelKeyMap(Element keyMapElement) {
        this.fieldName = UtilXml.checkEmpty(keyMapElement.getAttribute("field-name")).intern();
        // if no relFieldName is specified, use the fieldName; this is convenient for when they are named the same, which is often the case
        this.relFieldName = UtilXml.checkEmpty(keyMapElement.getAttribute("rel-field-name"), this.fieldName).intern();
    }

    /** Returns the field name. */
    public String getFieldName() {
        return this.fieldName;
    }

    /** Returns the related entity field name. */
    public String getRelFieldName() {
        return this.relFieldName;
    }

    // ======= Some Convenience Oriented Factory Methods =======
    public static List<ModelKeyMap> makeKeyMapList(String fieldName1) {
        return UtilMisc.toList(new ModelKeyMap(fieldName1, null));
    }
    public static List<ModelKeyMap> makeKeyMapList(String fieldName1, String relFieldName1) {
        return UtilMisc.toList(new ModelKeyMap(fieldName1, relFieldName1));
    }
    public static List<ModelKeyMap> makeKeyMapList(String fieldName1, String relFieldName1, String fieldName2, String relFieldName2) {
        return UtilMisc.toList(new ModelKeyMap(fieldName1, relFieldName1), new ModelKeyMap(fieldName2, relFieldName2));
    }
    public static List<ModelKeyMap> makeKeyMapList(String fieldName1, String relFieldName1, String fieldName2, String relFieldName2, String fieldName3, String relFieldName3) {
        return UtilMisc.toList(new ModelKeyMap(fieldName1, relFieldName1), new ModelKeyMap(fieldName2, relFieldName2), new ModelKeyMap(fieldName3, relFieldName3));
    }

    @Override
    public int hashCode() {
        return this.fieldName.hashCode() + this.relFieldName.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof ModelKeyMap)) return false;
        ModelKeyMap otherKeyMap = (ModelKeyMap) other;

        if (!otherKeyMap.fieldName.equals(this.fieldName)) return false;
        if (!otherKeyMap.relFieldName.equals(this.relFieldName)) return false;

        return true;
    }

    // TODO: Externalize this.
    public Element toXmlElement(Document document) {
        Element root = document.createElement("key-map");
        root.setAttribute("field-name", this.getFieldName());
        if (!this.getFieldName().equals(this.getRelFieldName())) {
            root.setAttribute("rel-field-name", this.getRelFieldName());
        }
        return root;
    }
}
