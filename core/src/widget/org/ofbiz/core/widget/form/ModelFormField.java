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
import org.ofbiz.core.entity.GenericDelegator;
import org.ofbiz.core.service.LocalDispatcher;
import org.ofbiz.core.util.*;

/**
 * Widget Library - Form model class
 *
 * @author     <a href="mailto:jonesde@ofbiz.org">David E. Jones</a>
 * @version    $Revision$
 * @since      2.2
 */
public class ModelFormField {
    
    public static final String module = ModelFormField.class.getName();

    protected ModelForm modelForm;

    protected String name;
    protected String mapName;
    protected String entityName;
    protected String serviceName;
    protected String entryName;
    protected String parameterName;
    protected String fieldName;
    protected String attributeName;
    protected FlexibleStringExpander title;
    protected String tooltip;
    protected String titleStyle;
    protected String widgetStyle;
    protected int position = 1;
    protected String redWhen;
    protected String useWhen;
    
    protected FieldInfo fieldInfo = null;

    // ===== CONSTRUCTORS =====
    /** Default Constructor */
    public ModelFormField(ModelForm modelForm) {
        this.modelForm = modelForm;
    }

    /** XML Constructor */
    public ModelFormField(Element fieldElement, ModelForm modelForm) {
        this.modelForm = modelForm;
        this.name = fieldElement.getAttribute("name");
        this.mapName = fieldElement.getAttribute("map-name");
        this.entityName = fieldElement.getAttribute("entity-name");
        this.serviceName = fieldElement.getAttribute("service-name");
        this.entryName = UtilXml.checkEmpty(fieldElement.getAttribute("entry-name"), this.name);
        this.parameterName = UtilXml.checkEmpty(fieldElement.getAttribute("parameter-name"), this.name);
        this.fieldName = UtilXml.checkEmpty(fieldElement.getAttribute("field-name"), this.name);
        this.attributeName = UtilXml.checkEmpty(fieldElement.getAttribute("attribute-name"), this.name);
        this.setTitle(fieldElement.getAttribute("title"));
        this.tooltip = fieldElement.getAttribute("tooltip");
        this.titleStyle = fieldElement.getAttribute("title-style");
        this.widgetStyle = fieldElement.getAttribute("widget-style");
        this.redWhen = fieldElement.getAttribute("red-when");
        this.useWhen = fieldElement.getAttribute("use-when");
        
        String positionStr = fieldElement.getAttribute("position");
        try {
            position = Integer.parseInt(positionStr);
        } catch (Exception e) {
            if (positionStr != null && positionStr.length() > 0) {
                Debug.logError(e, "Could not convert position attribute of the field element to an integer: [" + positionStr + "], using the default of " + position);
            }
        }
        
        // get sub-element and set fieldInfo
        Element subElement = UtilXml.firstChildElement(fieldElement, null);
        if (subElement != null) {
            String subElementName = subElement.getTagName();
            if (Debug.infoOn()) Debug.logInfo("Processing field " + this.name + " with type info tag " + subElementName);
            
            if ("display".equals(subElementName)) {
                this.fieldInfo = new DisplayField(subElement, this);
            } else if ("hyperlink".equals(subElementName)) {
                this.fieldInfo = new HyperlinkField(subElement, this);
            } else if ("text".equals(subElementName)) {
                this.fieldInfo = new TextField(subElement, this);
            } else if ("textarea".equals(subElementName)) {
                this.fieldInfo = new TextareaField(subElement, this);
            } else if ("date-time".equals(subElementName)) {
                this.fieldInfo = new DateTimeField(subElement, this);
            } else if ("drop-down".equals(subElementName)) {
                this.fieldInfo = new DropDownField(subElement, this);
            } else if ("check".equals(subElementName)) {
                this.fieldInfo = new CheckField(subElement, this);
            } else if ("radio".equals(subElementName)) {
                this.fieldInfo = new RadioField(subElement, this);
            } else if ("submit".equals(subElementName)) {
                this.fieldInfo = new SubmitField(subElement, this);
            } else if ("reset".equals(subElementName)) {
                this.fieldInfo = new ResetField(subElement, this);
            } else if ("hidden".equals(subElementName)) {
                this.fieldInfo = new HiddenField(subElement, this);
            } else if ("ignored".equals(subElementName)) {
                this.fieldInfo = new IgnoredField(subElement, this);
            } else {
                throw new IllegalArgumentException("The field sub-element with name " + subElementName + " is not supported");
            }
        }
    }
    
    public void mergeOverrideModelFormField(ModelFormField overrideFormField) {
        // TODO: incorporate updates for values that are not null empty in the overrideFormField
    }

    public void renderFieldString(StringBuffer buffer, Map context, FormStringRenderer formStringRenderer, GenericDelegator delegator, LocalDispatcher dispatcher) {
        this.fieldInfo.renderFieldString(buffer, context, formStringRenderer, delegator, dispatcher);
    }

    /**
     * @return
     */
    public FieldInfo getFieldInfo() {
        return fieldInfo;
    }

    /**
     * @return
     */
    public ModelForm getModelForm() {
        return modelForm;
    }

    /**
     * @param info
     */
    public void setModelForm(FieldInfo fieldInfo) {
        this.fieldInfo = fieldInfo;
    }
    
    /**
     * @return
     */
    public String getAttributeName() {
        return attributeName;
    }

    /**
     * @return
     */
    public String getEntityName() {
        return entityName;
    }

    /**
     * @return
     */
    public String getEntryName() {
        return entryName;
    }

    /**
     * @return
     */
    public String getFieldName() {
        return fieldName;
    }

    /**
     * @return
     */
    public String getMapName() {
        return mapName;
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
    public String getParameterName() {
        return parameterName;
    }

    /**
     * @return
     */
    public int getPosition() {
        return position;
    }

    /**
     * @return
     */
    public String getRedWhen() {
        return redWhen;
    }

    /**
     * @return
     */
    public String getServiceName() {
        return serviceName;
    }

    /**
     * @return
     */
    public String getTitle(Map context) {
        return title.expandString(context);
    }

    /**
     * @return
     */
    public String getTitleStyle() {
        return titleStyle;
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
    public String getUseWhen() {
        return useWhen;
    }

    /**
     * @return
     */
    public String getWidgetStyle() {
        return widgetStyle;
    }

    /**
     * @param string
     */
    public void setAttributeName(String string) {
        attributeName = string;
    }

    /**
     * @param string
     */
    public void setEntityName(String string) {
        entityName = string;
    }

    /**
     * @param string
     */
    public void setEntryName(String string) {
        entryName = string;
    }

    /**
     * @param string
     */
    public void setFieldName(String string) {
        fieldName = string;
    }

    /**
     * @param string
     */
    public void setMapName(String string) {
        mapName = string;
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
    public void setParameterName(String string) {
        parameterName = string;
    }

    /**
     * @param i
     */
    public void setPosition(int i) {
        position = i;
    }

    /**
     * @param string
     */
    public void setRedWhen(String string) {
        redWhen = string;
    }

    /**
     * @param string
     */
    public void setServiceName(String string) {
        serviceName = string;
    }

    /**
     * @param string
     */
    public void setTitle(String string) {
        this.title = new FlexibleStringExpander(string);
    }

    /**
     * @param string
     */
    public void setTitleStyle(String string) {
        titleStyle = string;
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
    public void setUseWhen(String string) {
        useWhen = string;
    }

    /**
     * @param string
     */
    public void setWidgetStyle(String string) {
        widgetStyle = string;
    }
    
    public static abstract class FieldInfo {
        protected String fieldTypeName;
        protected ModelFormField modelFormField;
        
        /** Don't allow the Default Constructor */
        protected FieldInfo() {}

        /** Value Constructor */        
        public FieldInfo(String fieldTypeName, ModelFormField modelFormField) {
            this.fieldTypeName = fieldTypeName;
            this.modelFormField = modelFormField;
        }
        
        /** XML Constructor */        
        public FieldInfo(Element element, ModelFormField modelFormField) {
            this.fieldTypeName = element.getTagName();
            this.modelFormField = modelFormField;
        }
        
        /**
         * @return
         */
        public String getFieldTypeName() {
            return fieldTypeName;
        }

        /**
         * @return
         */
        public ModelFormField getModelFormField() {
            return modelFormField;
        }

        /**
         * @param string
         */
        public void setFieldTypeName(String string) {
            fieldTypeName = string;
        }

        public abstract void renderFieldString(StringBuffer buffer, Map context, FormStringRenderer formStringRenderer, GenericDelegator delegator, LocalDispatcher dispatcher);
    }
    
    public static abstract class FieldInfoWithOptions extends FieldInfo {
        protected FieldInfoWithOptions() { super(); }
        
        protected List optionSources = new LinkedList();

        public FieldInfoWithOptions(String fieldTypeName, ModelFormField modelFormField) {
            super(fieldTypeName, modelFormField);
        }

        public FieldInfoWithOptions(Element element, ModelFormField modelFormField) {
            super(element, modelFormField);
            
            // TODO: read all option and entity-options sub-elements, maintaining order
        }

        public List getAllOptionValues(Map context, GenericDelegator delegator) {
            List optionValues = new LinkedList();
            
            Iterator optionSourceIter = this.optionSources.iterator();
            while (optionSourceIter.hasNext()) {
                OptionSource optionSource = (OptionSource) optionSourceIter.next();
                optionSource.addOptionValues(optionValues, context, delegator);
            }
                  
            return optionValues;
        }
    }
    
    public static class OptionValue {
        protected String key;
        protected String description;
        
        public OptionValue(String key, String description) {
            this.key = key;
            this.description = description;
        }

        public String getKey() {
            return key;
        }

        public String getDescription() {
            return description;
        }
    }
    
    public static abstract class OptionSource {
        public abstract void addOptionValues(List optionValues, Map context, GenericDelegator delegator);
    }
    
    public static class SingleOption extends OptionSource {
        protected FlexibleStringExpander key;
        protected FlexibleStringExpander description;
        
        public SingleOption(String key, String description) {
            this.key = new FlexibleStringExpander(key);
            this.description = new FlexibleStringExpander(UtilXml.checkEmpty(description, key));
        }
        
        public SingleOption(Element optionElement) {
            this.key = new FlexibleStringExpander(optionElement.getAttribute("key"));
            this.description = new FlexibleStringExpander(UtilXml.checkEmpty(optionElement.getAttribute("description"), optionElement.getAttribute("key")));
        }
        
        public void addOptionValues(List optionValues, Map context, GenericDelegator delegator) {
            optionValues.add(new OptionValue(key.expandString(context), description.expandString(context)));
        }
    }
    
    public static class EntityOptions extends OptionSource {
        /*
    entity-name CDATA #REQUIRED
    key-field-name CDATA #IMPLIED
    description CDATA #REQUIRED
    cache ( true | false ) "true"
         * 
         */
        protected String entityName;
        protected String keyFieldName;
        protected String description;
        protected boolean cache = true;
        
        public EntityOptions() {
        }
        
        public EntityOptions(Element entityOptionsElement) {
            // TODO: add all setup code for entity-options attributes
            // TODO: add all setup code for entity-options sub-elements
        }
        
        public void addOptionValues(List optionValues, Map context, GenericDelegator delegator) {
            // TODO: add key and description with string expansion, ie expanding ${} stuff
        }
    }
    
    public static class DisplayField extends FieldInfo {
        protected boolean alsoHidden = true;
        protected String description;
        
        protected DisplayField() { super(); }

        public DisplayField(String fieldTypeName, ModelFormField modelFormField) {
            super(fieldTypeName, modelFormField);
        }

        public DisplayField(Element element, ModelFormField modelFormField) {
            super(element, modelFormField);
            
            this.description = element.getAttribute("description");
            String alsoHiddenStr = element.getAttribute("also-hidden");
            try {
                this.alsoHidden = Boolean.getBoolean(alsoHiddenStr);
            } catch (Exception e) {
                if (alsoHiddenStr != null && alsoHiddenStr.length() > 0) {
                    Debug.logError("Could not parse the size value of the text element: [" + alsoHiddenStr + "], setting to default of " + alsoHidden);
                }
            }
        }

        public void renderFieldString(StringBuffer buffer, Map context, FormStringRenderer formStringRenderer, GenericDelegator delegator, LocalDispatcher dispatcher) {
            formStringRenderer.renderDisplayField(buffer, context, this, delegator, dispatcher);
        }
        
        /**
         * @return
         */
        public boolean getAlsoHidden() {
            return alsoHidden;
        }

        /**
         * @return
         */
        public String getDescription(Map context) {
            // TODO: put this logic in a method somewhere, ie out of this method, get the description
            if (UtilValidate.isNotEmpty(this.description)) {
                // TODO: expand the description
                return this.description;
            } else {
                // TODO: expand the map name
                // TODO: if map name not specified on field, look for default name on form
                Map dataMap = (Map) context.get(modelFormField.getMapName());
                Object retVal = dataMap.get(modelFormField.getEntryName());
                if (retVal != null) {
                    return retVal.toString();
                } else {
                    return "";
                }
            }
        }

        /**
         * @param b
         */
        public void setAlsoHidden(boolean b) {
            alsoHidden = b;
        }

        /**
         * @param string
         */
        public void setDescription(String string) {
            description = string;
        }
    }
    
    public static class HyperlinkField extends FieldInfo {
        protected boolean alsoHidden = true;
        protected String target;
        protected String description;
        
        protected HyperlinkField() { super(); }

        public HyperlinkField(String fieldTypeName, ModelFormField modelFormField) {
            super(fieldTypeName, modelFormField);
        }

        public HyperlinkField(Element element, ModelFormField modelFormField) {
            super(element, modelFormField);
            
            this.target = element.getAttribute("target");
            this.description = element.getAttribute("description");
            String alsoHiddenStr = element.getAttribute("also-hidden");
            try {
                this.alsoHidden = Boolean.getBoolean(alsoHiddenStr);
            } catch (Exception e) {
                if (alsoHiddenStr != null && alsoHiddenStr.length() > 0) {
                    Debug.logError("Could not parse the size value of the text element: [" + alsoHiddenStr + "], setting to default of " + alsoHidden);
                }
            }
        }

        public void renderFieldString(StringBuffer buffer, Map context, FormStringRenderer formStringRenderer, GenericDelegator delegator, LocalDispatcher dispatcher) {
            formStringRenderer.renderHyperlinkField(buffer, context, this, delegator, dispatcher);
        }
        
        /**
         * @return
         */
        public boolean isAlsoHidden() {
            return alsoHidden;
        }

        /**
         * @return
         */
        public String getDescription() {
            return description;
        }

        /**
         * @return
         */
        public String getTarget() {
            return target;
        }

        /**
         * @param b
         */
        public void setAlsoHidden(boolean b) {
            alsoHidden = b;
        }

        /**
         * @param string
         */
        public void setDescription(String string) {
            description = string;
        }

        /**
         * @param string
         */
        public void setTarget(String string) {
            target = string;
        }
    }
    
    public static class TextField extends FieldInfo {
        protected int size = 25;
        protected Integer maxlength;
        
        protected TextField() { super(); }

        public TextField(String fieldTypeName, ModelFormField modelFormField) {
            super(fieldTypeName, modelFormField);
        }

        public TextField(Element element, ModelFormField modelFormField) {
            super(element, modelFormField);
            
            String sizeStr = element.getAttribute("size");
            try {
                size = Integer.parseInt(sizeStr);
            } catch (Exception e) {
                if (sizeStr != null && sizeStr.length() > 0) {
                    Debug.logError("Could not parse the size value of the text element: [" + sizeStr + "], setting to the default of " + size);
                }
            }
            
            String maxlengthStr = element.getAttribute("maxlength");
            try {
                maxlength = Integer.valueOf(maxlengthStr);
            } catch (Exception e) {
                maxlength = null;
                if (maxlengthStr != null && maxlengthStr.length() > 0) {
                    Debug.logError("Could not parse the size value of the text element: [" + sizeStr + "], setting to null; default of no maxlength will be used");
                }
            }
        }

        public void renderFieldString(StringBuffer buffer, Map context, FormStringRenderer formStringRenderer, GenericDelegator delegator, LocalDispatcher dispatcher) {
            formStringRenderer.renderTextField(buffer, context, this, delegator, dispatcher);
        }
        
        /**
         * @return
         */
        public Integer getMaxlength() {
            return maxlength;
        }

        /**
         * @return
         */
        public int getSize() {
            return size;
        }

        /**
         * @param integer
         */
        public void setMaxlength(Integer integer) {
            maxlength = integer;
        }

        /**
         * @param i
         */
        public void setSize(int i) {
            size = i;
        }
    }
    
    public static class TextareaField extends FieldInfo {
        protected int cols = 60;
        protected int rows = 2;
        
        protected TextareaField() { super(); }

        public TextareaField(String fieldTypeName, ModelFormField modelFormField) {
            super(fieldTypeName, modelFormField);
        }

        public TextareaField(Element element, ModelFormField modelFormField) {
            super(element, modelFormField);
            
            String colsStr = element.getAttribute("cols");
            try {
                cols = Integer.parseInt(colsStr);
            } catch (Exception e) {
                if (colsStr != null && colsStr.length() > 0) {
                    Debug.logError("Could not parse the size value of the text element: [" + colsStr + "], setting to default of " + cols);
                }
            }
            
            String rowsStr = element.getAttribute("rows");
            try {
                rows = Integer.parseInt(rowsStr);
            } catch (Exception e) {
                if (rowsStr != null && rowsStr.length() > 0) {
                    Debug.logError("Could not parse the size value of the text element: [" + rowsStr + "], setting to default of " + rows);
                }
            }
        }

        public void renderFieldString(StringBuffer buffer, Map context, FormStringRenderer formStringRenderer, GenericDelegator delegator, LocalDispatcher dispatcher) {
            formStringRenderer.renderTextareaField(buffer, context, this, delegator, dispatcher);
        }
        
        /**
         * @return
         */
        public int getCols() {
            return cols;
        }

        /**
         * @return
         */
        public int getRows() {
            return rows;
        }

        /**
         * @param i
         */
        public void setCols(int i) {
            cols = i;
        }

        /**
         * @param i
         */
        public void setRows(int i) {
            rows = i;
        }
    }

    public static class DateTimeField extends FieldInfo {
        protected String type;
        
        protected DateTimeField() { super(); }

        public DateTimeField(String fieldTypeName, ModelFormField modelFormField) {
            super(fieldTypeName, modelFormField);
        }

        public DateTimeField(Element element, ModelFormField modelFormField) {
            super(element, modelFormField);
            type = element.getAttribute("type");
        }

        public void renderFieldString(StringBuffer buffer, Map context, FormStringRenderer formStringRenderer, GenericDelegator delegator, LocalDispatcher dispatcher) {
            formStringRenderer.renderDateTimeField(buffer, context, this, delegator, dispatcher);
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
        public void setType(String string) {
            type = string;
        }
    }

    public static class DropDownField extends FieldInfoWithOptions {
        protected boolean allowEmpty = false;
        protected String current;
        
        protected DropDownField() { super(); }

        public DropDownField(String fieldTypeName, ModelFormField modelFormField) {
            super(fieldTypeName, modelFormField);
        }

        public DropDownField(Element element, ModelFormField modelFormField) {
            super(element, modelFormField);
            
            this.current = element.getAttribute("current");
            String allowEmptyStr = element.getAttribute("allow-empty");
            try {
                this.allowEmpty = Boolean.getBoolean(allowEmptyStr);
            } catch (Exception e) {
                if (allowEmptyStr != null && allowEmptyStr.length() > 0) {
                    Debug.logError("Could not parse the size value of the text element: [" + allowEmptyStr + "], setting to default of " + allowEmpty);
                }
            }
        }

        public void renderFieldString(StringBuffer buffer, Map context, FormStringRenderer formStringRenderer, GenericDelegator delegator, LocalDispatcher dispatcher) {
            formStringRenderer.renderDropDownField(buffer, context, this, delegator, dispatcher);
        }
        
        /**
         * @return
         */
        public boolean isAllowEmpty() {
            return allowEmpty;
        }

        /**
         * @return
         */
        public String getCurrent() {
            return current;
        }

        /**
         * @param b
         */
        public void setAllowEmpty(boolean b) {
            allowEmpty = b;
        }

        /**
         * @param string
         */
        public void setCurrent(String string) {
            current = string;
        }
    }

    public static class CheckField extends FieldInfoWithOptions {
        protected CheckField() { super(); }

        public CheckField(String fieldTypeName, ModelFormField modelFormField) {
            super(fieldTypeName, modelFormField);
        }

        public CheckField(Element element, ModelFormField modelFormField) {
            super(element, modelFormField);
        }

        public void renderFieldString(StringBuffer buffer, Map context, FormStringRenderer formStringRenderer, GenericDelegator delegator, LocalDispatcher dispatcher) {
            formStringRenderer.renderCheckField(buffer, context, this, delegator, dispatcher);
        }
    }

    public static class RadioField extends FieldInfoWithOptions {
        protected RadioField() { super(); }

        public RadioField(String fieldTypeName, ModelFormField modelFormField) {
            super(fieldTypeName, modelFormField);
        }

        public RadioField(Element element, ModelFormField modelFormField) {
            super(element, modelFormField);
        }

        public void renderFieldString(StringBuffer buffer, Map context, FormStringRenderer formStringRenderer, GenericDelegator delegator, LocalDispatcher dispatcher) {
            formStringRenderer.renderRadioField(buffer, context, this, delegator, dispatcher);
        }
    }

    public static class SubmitField extends FieldInfo {
        protected String buttonType;
        protected String imageLocation;
        
        protected SubmitField() { super(); }

        public SubmitField(String fieldTypeName, ModelFormField modelFormField) {
            super(fieldTypeName, modelFormField);
        }

        public SubmitField(Element element, ModelFormField modelFormField) {
            super(element, modelFormField);
            this.buttonType = element.getAttribute("button-type");
            this.imageLocation = element.getAttribute("image-location");
        }

        public void renderFieldString(StringBuffer buffer, Map context, FormStringRenderer formStringRenderer, GenericDelegator delegator, LocalDispatcher dispatcher) {
            formStringRenderer.renderSubmitField(buffer, context, this, delegator, dispatcher);
        }
        
        /**
         * @return
         */
        public String getButtonType() {
            return buttonType;
        }

        /**
         * @return
         */
        public String getImageLocation() {
            return imageLocation;
        }

        /**
         * @param string
         */
        public void setButtonType(String string) {
            buttonType = string;
        }

        /**
         * @param string
         */
        public void setImageLocation(String string) {
            imageLocation = string;
        }
    }

    public static class ResetField extends FieldInfo {
        protected ResetField() { super(); }

        public ResetField(String fieldTypeName, ModelFormField modelFormField) {
            super(fieldTypeName, modelFormField);
        }

        public ResetField(Element element, ModelFormField modelFormField) {
            super(element, modelFormField);
        }

        public void renderFieldString(StringBuffer buffer, Map context, FormStringRenderer formStringRenderer, GenericDelegator delegator, LocalDispatcher dispatcher) {
            formStringRenderer.renderResetField(buffer, context, this, delegator, dispatcher);
        }
    }

    public static class HiddenField extends FieldInfo {
        protected HiddenField() { super(); }

        public HiddenField(String fieldTypeName, ModelFormField modelFormField) {
            super(fieldTypeName, modelFormField);
        }

        public HiddenField(Element element, ModelFormField modelFormField) {
            super(element, modelFormField);
        }

        public void renderFieldString(StringBuffer buffer, Map context, FormStringRenderer formStringRenderer, GenericDelegator delegator, LocalDispatcher dispatcher) {
            formStringRenderer.renderHiddenField(buffer, context, this, delegator, dispatcher);
        }
    }

    public static class IgnoredField extends FieldInfo {
        protected IgnoredField() { super(); }

        public IgnoredField(String fieldTypeName, ModelFormField modelFormField) {
            super(fieldTypeName, modelFormField);
        }

        public IgnoredField(Element element, ModelFormField modelFormField) {
            super(element, modelFormField);
        }

        public void renderFieldString(StringBuffer buffer, Map context, FormStringRenderer formStringRenderer, GenericDelegator delegator, LocalDispatcher dispatcher) {
            formStringRenderer.renderIgnoredField(buffer, context, this, delegator, dispatcher);
        }
    }
}
