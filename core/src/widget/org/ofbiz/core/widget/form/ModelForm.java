/*
 * $Id$
 *
 * Copyright (c) 2003 The Open For Business Project - www.ofbiz.org
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
package org.ofbiz.core.widget.form;

import java.util.*;
import org.w3c.dom.*;
import org.ofbiz.core.util.*;

/**
 * Widget Library - Form model class
 *
 * @author     <a href="mailto:jonesde@ofbiz.org">David E. Jones</a>
 * @version    $Revision$
 * @since      2.2
 */
public class ModelForm {
    
    public static final String module = ModelForm.class.getName();

    protected String name;
    protected String type;
    protected String target;
    protected String title;
    protected String tooltip;
    protected String listName;
    protected String listEntryName;
    protected String defaultMapName;
    protected String defaultTitleStyle;
    protected String defaultWidgetStyle;
    
    protected List altTargets;
    protected List autoFieldsServices;
    protected List autoFieldsEntities;

    /** This List will contain one copy of each field for each field name in the order
     * they were encountered in the service, entity, or form definition; field definitions
     * with constraints will also be in this list but may appear multiple times for the same
     * field name.
     * 
     * When rendering the form the order in this list should be following and it should not be
     * necessary to use the Map. The Map is used when loading the form definition to keep the
     * list clean and implement the override features for field definitions.
     */    
    protected List fieldList;
    
    /** This Map is keyed with the field name and has a ModelFormField for the value; fields
     * with conditions will not be put in this Map so field definition overrides for fields
     * with conditions is not possible.
     */ 
    protected Map fieldMap;

    // ===== CONSTRUCTORS =====
    /** Default Constructor */
    public ModelForm() {}

    /** XML Constructor */
    public ModelForm(Element formElement) {
        this.name = formElement.getAttribute("name");
        this.type = formElement.getAttribute("type");
        this.target = formElement.getAttribute("target");
        this.title = formElement.getAttribute("title");
        this.tooltip = formElement.getAttribute("tooltip");
        this.listName = formElement.getAttribute("list-name");
        this.listEntryName = formElement.getAttribute("list-entry-name");
        this.defaultMapName = formElement.getAttribute("default-map-name");
        this.defaultTitleStyle = formElement.getAttribute("default-title-style");
        this.defaultWidgetStyle = formElement.getAttribute("default-widget-style");
        
        // alt-target
        List altTargetElements = UtilXml.childElementList(formElement, "alt-target");
        Iterator altTargetElementIter = altTargetElements.iterator();
        while (altTargetElementIter.hasNext()) {
            Element altTargetElement = (Element) altTargetElementIter.next();
            altTargets.add(new AltTarget(altTargetElement));
        }

        // auto-fields-service
        List autoFieldsServiceElements = UtilXml.childElementList(formElement, "auto-fields-service");
        Iterator autoFieldsServiceElementIter = autoFieldsServiceElements.iterator();
        while (autoFieldsServiceElementIter.hasNext()) {
            Element autoFieldsServiceElement = (Element) autoFieldsServiceElementIter.next();
            autoFieldsServices.add(new AutoFieldsService(autoFieldsServiceElement));
        }

        // auto-fields-entity
        List autoFieldsEntityElements = UtilXml.childElementList(formElement, "auto-fields-entity");
        Iterator autoFieldsEntityElementIter = autoFieldsEntityElements.iterator();
        while (autoFieldsEntityElementIter.hasNext()) {
            Element autoFieldsEntityElement = (Element) autoFieldsEntityElementIter.next();
            autoFieldsEntities.add(new AutoFieldsEntity(autoFieldsEntityElement));
        }

    }

    /**
     * @return
     */
    public String getDefaultMapName() {
        return defaultMapName;
    }

    /**
     * @return
     */
    public String getDefaultTitleStyle() {
        return defaultTitleStyle;
    }

    /**
     * @return
     */
    public String getDefaultWidgetStyle() {
        return defaultWidgetStyle;
    }

    /**
     * @return
     */
    public String getListEntryName() {
        return listEntryName;
    }

    /**
     * @return
     */
    public String getListName() {
        return listName;
    }

    /**
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * @return
     */
    public String getTarget() {
        return target;
    }

    /**
     * @return
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return
     */
    public String getTooltip() {
        return tooltip;
    }

    /**
     * @return
     */
    public String getType() {
        return type;
    }

    /**
     * @param string
     */
    public void setDefaultMapName(String string) {
        defaultMapName = string;
    }

    /**
     * @param string
     */
    public void setDefaultTitleStyle(String string) {
        defaultTitleStyle = string;
    }

    /**
     * @param string
     */
    public void setDefaultWidgetStyle(String string) {
        defaultWidgetStyle = string;
    }

    /**
     * @param string
     */
    public void setListEntryName(String string) {
        listEntryName = string;
    }

    /**
     * @param string
     */
    public void setListName(String string) {
        listName = string;
    }

    /**
     * @param string
     */
    public void setName(String string) {
        name = string;
    }

    /**
     * @param string
     */
    public void setTarget(String string) {
        target = string;
    }

    /**
     * @param string
     */
    public void setTitle(String string) {
        title = string;
    }

    /**
     * @param string
     */
    public void setTooltip(String string) {
        tooltip = string;
    }

    /**
     * @param string
     */
    public void setType(String string) {
        type = string;
    }
    
    public static class AltTarget {
        public String useWhen;
        public String target;
        public AltTarget(Element altTargetElement) {
            this.useWhen = altTargetElement.getAttribute("use-when");
            this.target = altTargetElement.getAttribute("target");
        }
    }
    
    public static class AutoFieldsService {
        public String serviceName;
        public String mapName;
        public AutoFieldsService(Element element) {
            this.serviceName = element.getAttribute("service-name");
            this.mapName = element.getAttribute("map-name");
        }
    }
    
    public static class AutoFieldsEntity {
        public String entityName;
        public String mapName;
        public AutoFieldsEntity(Element element) {
            this.entityName = element.getAttribute("entity-name");
            this.mapName = element.getAttribute("map-name");
        }
    }
}
