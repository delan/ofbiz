/*
 * $Id: ModelFormField.java,v 1.15 2004/07/31 12:17:39 jonesde Exp $
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
package org.ofbiz.content.widget.form;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.ofbiz.base.util.BshUtil;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.base.util.UtilXml;
import org.ofbiz.base.util.collections.FlexibleMapAccessor;
import org.ofbiz.base.util.string.FlexibleStringExpander;
import org.ofbiz.entity.GenericDelegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.model.ModelEntity;
import org.ofbiz.entity.model.ModelField;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ModelParam;
import org.ofbiz.service.ModelService;
import org.w3c.dom.Element;

import bsh.EvalError;
import bsh.Interpreter;

/**
 * Widget Library - Form model class
 *
 * @author     <a href="mailto:jonesde@ofbiz.org">David E. Jones</a>
 * @author     <a href="mailto:byersa@automationgroups.com">Al Byers</a>
 * @version    $Revision: 1.15 $
 * @since      2.2
 */
public class ModelFormField {

    public static final String module = ModelFormField.class.getName();

    protected ModelForm modelForm;

    protected String name;
    protected FlexibleMapAccessor mapAcsr;
    protected String entityName;
    protected String serviceName;
    protected FlexibleMapAccessor entryAcsr;
    protected String parameterName;
    protected String fieldName;
    protected String attributeName;
    protected FlexibleStringExpander title;
    protected FlexibleStringExpander tooltip;
    protected String titleStyle;
    protected String widgetStyle;
    protected String tooltipStyle;
    protected Integer position = null;
    protected String redWhen;
    protected FlexibleStringExpander useWhen;

    protected FieldInfo fieldInfo = null;
    protected String idName;
    protected boolean separateColumn = false;

    // ===== CONSTRUCTORS =====
    /** Default Constructor */
    public ModelFormField(ModelForm modelForm) {
        this.modelForm = modelForm;
    }

    /** XML Constructor */
    public ModelFormField(Element fieldElement, ModelForm modelForm) {
        this.modelForm = modelForm;
        this.name = fieldElement.getAttribute("name");
        this.setMapName(fieldElement.getAttribute("map-name"));
        this.entityName = fieldElement.getAttribute("entity-name");
        this.serviceName = fieldElement.getAttribute("service-name");
        this.setEntryName(UtilXml.checkEmpty(fieldElement.getAttribute("entry-name"), this.name));
        this.parameterName = UtilXml.checkEmpty(fieldElement.getAttribute("parameter-name"), this.name);
        this.fieldName = UtilXml.checkEmpty(fieldElement.getAttribute("field-name"), this.name);
        this.attributeName = UtilXml.checkEmpty(fieldElement.getAttribute("attribute-name"), this.name);
        this.setTitle(fieldElement.getAttribute("title"));
        this.setTooltip(fieldElement.getAttribute("tooltip"));
        this.titleStyle = fieldElement.getAttribute("title-style");
        this.widgetStyle = fieldElement.getAttribute("widget-style");
        this.tooltipStyle = fieldElement.getAttribute("tooltip-style");
        this.redWhen = fieldElement.getAttribute("red-when");
        this.setUseWhen(fieldElement.getAttribute("use-when"));
        this.idName = fieldElement.getAttribute("id-name");
        String sepColumns = fieldElement.getAttribute("separate-column");
        if (sepColumns != null && sepColumns.equalsIgnoreCase("true"))
            separateColumn = true;




        String positionStr = fieldElement.getAttribute("position");
        try {
            if (positionStr != null && positionStr.length() > 0) {
                position = Integer.valueOf(positionStr);
            }
        } catch (Exception e) {
            Debug.logError(
                e,
                "Could not convert position attribute of the field element to an integer: [" + positionStr + "], using the default of the form renderer",
                module);
        }

        // get sub-element and set fieldInfo
        Element subElement = UtilXml.firstChildElement(fieldElement);
        if (subElement != null) {
            String subElementName = subElement.getTagName();
            if (Debug.verboseOn())
                Debug.logVerbose("Processing field " + this.name + " with type info tag " + subElementName, module);

            if (UtilValidate.isEmpty(subElementName)) {
                this.fieldInfo = null;
                this.induceFieldInfo(null); //no defaultFieldType specified here, will default to edit
            } else if ("display".equals(subElementName)) {
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
            } else if ("text-find".equals(subElementName)) {
                this.fieldInfo = new TextFindField(subElement, this);
            } else if ("date-find".equals(subElementName)) {
                this.fieldInfo = new DateFindField(subElement, this);
            } else if ("range-find".equals(subElementName)) {
                this.fieldInfo = new RangeFindField(subElement, this);
            } else if ("lookup".equals(subElementName)) {
                this.fieldInfo = new LookupField(subElement, this);
            } else if ("file".equals(subElementName)) {
                this.fieldInfo = new FileField(subElement, this);
            } else if ("password".equals(subElementName)) {
                this.fieldInfo = new PasswordField(subElement, this);
            } else if ("image".equals(subElementName)) {
                this.fieldInfo = new ImageField(subElement, this);
            } else {
                throw new IllegalArgumentException("The field sub-element with name " + subElementName + " is not supported");
            }
        }
    }

    public void mergeOverrideModelFormField(ModelFormField overrideFormField) {
        if (overrideFormField == null)
            return;
        // incorporate updates for values that are not empty in the overrideFormField
        if (UtilValidate.isNotEmpty(overrideFormField.name))
            this.name = overrideFormField.name;
        if (overrideFormField.mapAcsr != null && !overrideFormField.mapAcsr.isEmpty()) {
            //Debug.logInfo("overriding mapAcsr, old=" + (this.mapAcsr==null?"null":this.mapAcsr.getOriginalName()) + ", new=" + overrideFormField.mapAcsr.getOriginalName(), module);
            this.mapAcsr = overrideFormField.mapAcsr;
        }
        if (UtilValidate.isNotEmpty(overrideFormField.entityName))
            this.entityName = overrideFormField.entityName;
        if (UtilValidate.isNotEmpty(overrideFormField.serviceName))
            this.serviceName = overrideFormField.serviceName;
        if (overrideFormField.entryAcsr != null && !overrideFormField.entryAcsr.isEmpty())
            this.entryAcsr = overrideFormField.entryAcsr;
        if (UtilValidate.isNotEmpty(overrideFormField.parameterName))
            this.parameterName = overrideFormField.parameterName;
        if (UtilValidate.isNotEmpty(overrideFormField.fieldName))
            this.fieldName = overrideFormField.fieldName;
        if (UtilValidate.isNotEmpty(overrideFormField.attributeName))
            this.attributeName = overrideFormField.attributeName;
        if (overrideFormField.title != null && !overrideFormField.title.isEmpty())
            this.title = overrideFormField.title;
        if (overrideFormField.tooltip != null && !overrideFormField.tooltip.isEmpty())
            this.tooltip = overrideFormField.tooltip;
        if (UtilValidate.isNotEmpty(overrideFormField.titleStyle))
            this.titleStyle = overrideFormField.titleStyle;
        if (UtilValidate.isNotEmpty(overrideFormField.widgetStyle))
            this.widgetStyle = overrideFormField.widgetStyle;
        if (overrideFormField.position != null)
            this.position = overrideFormField.position;
        if (UtilValidate.isNotEmpty(overrideFormField.redWhen))
            this.redWhen = overrideFormField.redWhen;
        if (overrideFormField.useWhen != null && !overrideFormField.useWhen.isEmpty())
            this.useWhen = overrideFormField.useWhen;
        if (overrideFormField.fieldInfo != null) {
            this.setFieldInfo(overrideFormField.fieldInfo);
        }
        if (UtilValidate.isNotEmpty(overrideFormField.idName))
            this.idName = overrideFormField.idName;
    }

    public boolean induceFieldInfo(String defaultFieldType) {
        if (this.induceFieldInfoFromEntityField(defaultFieldType)) {
            return true;
        }
        if (this.induceFieldInfoFromServiceParam(defaultFieldType)) {
            return true;
        }
        return false;
    }

    public boolean induceFieldInfoFromServiceParam(String defaultFieldType) {
        if (UtilValidate.isEmpty(this.getServiceName()) || UtilValidate.isEmpty(this.getAttributeName())) {
            return false;
        }
        LocalDispatcher dispatcher = this.getModelForm().getDispacher();
        try {
            ModelService modelService = dispatcher.getDispatchContext().getModelService(this.getServiceName());
            if (modelService != null) {
                ModelParam modelParam = modelService.getParam(this.getAttributeName());
                if (modelParam != null) {
                    if (UtilValidate.isNotEmpty(modelParam.entityName) && UtilValidate.isNotEmpty(modelParam.fieldName)) {
                        this.entityName = modelParam.entityName;
                        this.fieldName = modelParam.fieldName;
                        if (this.induceFieldInfoFromEntityField(defaultFieldType)) {
                            return true;
                        }
                    }

                    this.induceFieldInfoFromServiceParam(modelService, modelParam, defaultFieldType);
                    return true;
                }
            }
        } catch (GenericServiceException e) {
            Debug.logError(e, "error getting service parameter definition for auto-field with serviceName: " + this.getServiceName() + ", and attributeName: " + this.getAttributeName(), module);
        }
        return false;
    }

    public boolean induceFieldInfoFromServiceParam(ModelService modelService, ModelParam modelParam, String defaultFieldType) {
        if (modelService == null || modelParam == null) {
            return false;
        }

        this.serviceName = modelService.name;
        this.attributeName = modelParam.name;

        if ("find".equals(defaultFieldType)) {
            if (modelParam.type.indexOf("Double") != -1
                || modelParam.type.indexOf("Float") != -1
                || modelParam.type.indexOf("Long") != -1
                || modelParam.type.indexOf("Integer") != -1) {
                ModelFormField.RangeFindField textField = new ModelFormField.RangeFindField(ModelFormField.FieldInfo.SOURCE_AUTO_SERVICE, this);
                textField.setSize(6);
                this.setFieldInfo(textField);
            } else if (modelParam.type.indexOf("Timestamp") != -1) {
                ModelFormField.DateFindField dateTimeField = new ModelFormField.DateFindField(ModelFormField.FieldInfo.SOURCE_AUTO_SERVICE, this);
                dateTimeField.setType("timestamp");
                this.setFieldInfo(dateTimeField);
            } else if (modelParam.type.indexOf("Date") != -1) {
                ModelFormField.DateFindField dateTimeField = new ModelFormField.DateFindField(ModelFormField.FieldInfo.SOURCE_AUTO_SERVICE, this);
                dateTimeField.setType("date");
                this.setFieldInfo(dateTimeField);
            } else if (modelParam.type.indexOf("Time") != -1) {
                ModelFormField.DateFindField dateTimeField = new ModelFormField.DateFindField(ModelFormField.FieldInfo.SOURCE_AUTO_SERVICE, this);
                dateTimeField.setType("time");
                this.setFieldInfo(dateTimeField);
            } else {
                ModelFormField.TextFindField textField = new ModelFormField.TextFindField(ModelFormField.FieldInfo.SOURCE_AUTO_SERVICE, this);
                this.setFieldInfo(textField);
            }
        } else if ("display".equals(defaultFieldType)) {
            ModelFormField.DisplayField displayField = new ModelFormField.DisplayField(ModelFormField.FieldInfo.SOURCE_AUTO_SERVICE, this);
            this.setFieldInfo(displayField);
        } else {
            // default to "edit"
            if (modelParam.type.indexOf("Double") != -1
                || modelParam.type.indexOf("Float") != -1
                || modelParam.type.indexOf("Long") != -1
                || modelParam.type.indexOf("Integer") != -1) {
                ModelFormField.TextField textField = new ModelFormField.TextField(ModelFormField.FieldInfo.SOURCE_AUTO_SERVICE, this);
                textField.setSize(6);
                this.setFieldInfo(textField);
            } else if (modelParam.type.indexOf("Timestamp") != -1) {
                ModelFormField.DateTimeField dateTimeField = new ModelFormField.DateTimeField(ModelFormField.FieldInfo.SOURCE_AUTO_SERVICE, this);
                dateTimeField.setType("timestamp");
                this.setFieldInfo(dateTimeField);
            } else if (modelParam.type.indexOf("Date") != -1) {
                ModelFormField.DateTimeField dateTimeField = new ModelFormField.DateTimeField(ModelFormField.FieldInfo.SOURCE_AUTO_SERVICE, this);
                dateTimeField.setType("date");
                this.setFieldInfo(dateTimeField);
            } else if (modelParam.type.indexOf("Time") != -1) {
                ModelFormField.DateTimeField dateTimeField = new ModelFormField.DateTimeField(ModelFormField.FieldInfo.SOURCE_AUTO_SERVICE, this);
                dateTimeField.setType("time");
                this.setFieldInfo(dateTimeField);
            } else {
                ModelFormField.TextField textField = new ModelFormField.TextField(ModelFormField.FieldInfo.SOURCE_AUTO_SERVICE, this);
                this.setFieldInfo(textField);
            }
        }

        return true;
    }

    public boolean induceFieldInfoFromEntityField(String defaultFieldType) {
        if (UtilValidate.isEmpty(this.getEntityName()) || UtilValidate.isEmpty(this.getFieldName())) {
            return false;
        }
        GenericDelegator delegator = this.getModelForm().getDelegator();
        ModelEntity modelEntity = delegator.getModelEntity(this.getEntityName());
        if (modelEntity != null) {
            ModelField modelField = modelEntity.getField(this.getFieldName());
            if (modelField != null) {
                // okay, populate using the entity field info...
                this.induceFieldInfoFromEntityField(modelEntity, modelField, defaultFieldType);
                return true;
            }
        }
        return false;
    }

    public boolean induceFieldInfoFromEntityField(ModelEntity modelEntity, ModelField modelField, String defaultFieldType) {
        if (modelEntity == null || modelField == null) {
            return false;
        }

        this.entityName = modelEntity.getEntityName();
        this.fieldName = modelField.getName();

        if ("find".equals(defaultFieldType)) {
            if ("id".equals(modelField.getType()) || "id-ne".equals(modelField.getType())) {
                ModelFormField.TextFindField textField = new ModelFormField.TextFindField(ModelFormField.FieldInfo.SOURCE_AUTO_ENTITY, this);
                textField.setSize(20);
                textField.setMaxlength(new Integer(20));
                this.setFieldInfo(textField);
            } else if ("id-long".equals(modelField.getType()) || "id-long-ne".equals(modelField.getType())) {
                ModelFormField.TextFindField textField = new ModelFormField.TextFindField(ModelFormField.FieldInfo.SOURCE_AUTO_ENTITY, this);
                textField.setSize(40);
                textField.setMaxlength(new Integer(60));
                this.setFieldInfo(textField);
            } else if ("id-vlong".equals(modelField.getType()) || "id-vlong-ne".equals(modelField.getType())) {
                ModelFormField.TextFindField textField = new ModelFormField.TextFindField(ModelFormField.FieldInfo.SOURCE_AUTO_ENTITY, this);
                textField.setSize(60);
                textField.setMaxlength(new Integer(250));
                this.setFieldInfo(textField);
            } else if ("very-short".equals(modelField.getType())) {
                ModelFormField.TextField textField = new ModelFormField.TextField(ModelFormField.FieldInfo.SOURCE_AUTO_ENTITY, this);
                textField.setSize(6);
                textField.setMaxlength(new Integer(10));
                this.setFieldInfo(textField);
            } else if ("name".equals(modelField.getType()) || "short-varchar".equals(modelField.getType())) {
                ModelFormField.TextFindField textField = new ModelFormField.TextFindField(ModelFormField.FieldInfo.SOURCE_AUTO_ENTITY, this);
                textField.setSize(40);
                textField.setMaxlength(new Integer(60));
                this.setFieldInfo(textField);
            } else if (
                "value".equals(modelField.getType())
                    || "comment".equals(modelField.getType())
                    || "description".equals(modelField.getType())
                    || "long-varchar".equals(modelField.getType())
                    || "url".equals(modelField.getType())
                    || "email".equals(modelField.getType())) {
                ModelFormField.TextFindField textField = new ModelFormField.TextFindField(ModelFormField.FieldInfo.SOURCE_AUTO_ENTITY, this);
                textField.setSize(60);
                textField.setMaxlength(new Integer(250));
                this.setFieldInfo(textField);
            } else if (
                "floating-point".equals(modelField.getType()) || "currency-amount".equals(modelField.getType()) || "numeric".equals(modelField.getType())) {
                ModelFormField.RangeFindField textField = new ModelFormField.RangeFindField(ModelFormField.FieldInfo.SOURCE_AUTO_ENTITY, this);
                textField.setSize(6);
                this.setFieldInfo(textField);
            } else if ("date-time".equals(modelField.getType()) || "date".equals(modelField.getType()) || "time".equals(modelField.getType())) {
                ModelFormField.DateFindField dateTimeField = new ModelFormField.DateFindField(ModelFormField.FieldInfo.SOURCE_AUTO_ENTITY, this);
                if ("date-time".equals(modelField.getType())) {
                    dateTimeField.setType("timestamp");
                } else if ("date".equals(modelField.getType())) {
                    dateTimeField.setType("date");
                } else if ("time".equals(modelField.getType())) {
                    dateTimeField.setType("time");
                }
                this.setFieldInfo(dateTimeField);
            } else {
                ModelFormField.TextFindField textField = new ModelFormField.TextFindField(ModelFormField.FieldInfo.SOURCE_AUTO_ENTITY, this);
                this.setFieldInfo(textField);
            }
        } else if ("display".equals(defaultFieldType)) {
            ModelFormField.DisplayField displayField = new ModelFormField.DisplayField(ModelFormField.FieldInfo.SOURCE_AUTO_SERVICE, this);
            this.setFieldInfo(displayField);
        } else if ("hidden".equals(defaultFieldType)) {
        	ModelFormField.HiddenField hiddenField = new ModelFormField.HiddenField(ModelFormField.FieldInfo.SOURCE_AUTO_SERVICE, this);
        	this.setFieldInfo(hiddenField);
        } else {
            if ("id".equals(modelField.getType()) || "id-ne".equals(modelField.getType())) {
                ModelFormField.TextField textField = new ModelFormField.TextField(ModelFormField.FieldInfo.SOURCE_AUTO_ENTITY, this);
                textField.setSize(20);
                textField.setMaxlength(new Integer(20));
                this.setFieldInfo(textField);
            } else if ("id-long".equals(modelField.getType()) || "id-long-ne".equals(modelField.getType())) {
                ModelFormField.TextField textField = new ModelFormField.TextField(ModelFormField.FieldInfo.SOURCE_AUTO_ENTITY, this);
                textField.setSize(40);
                textField.setMaxlength(new Integer(60));
                this.setFieldInfo(textField);
            } else if ("id-vlong".equals(modelField.getType()) || "id-vlong-ne".equals(modelField.getType())) {
                ModelFormField.TextField textField = new ModelFormField.TextField(ModelFormField.FieldInfo.SOURCE_AUTO_ENTITY, this);
                textField.setSize(60);
                textField.setMaxlength(new Integer(250));
                this.setFieldInfo(textField);
            } else if ("indicator".equals(modelField.getType())) {
                ModelFormField.DropDownField dropDownField = new ModelFormField.DropDownField(ModelFormField.FieldInfo.SOURCE_AUTO_ENTITY, this);
                dropDownField.setAllowEmpty(false);
                dropDownField.addOptionSource(new ModelFormField.SingleOption("Y", null, dropDownField));
                dropDownField.addOptionSource(new ModelFormField.SingleOption("N", null, dropDownField));
                this.setFieldInfo(dropDownField);
                //ModelFormField.TextField textField = new ModelFormField.TextField(ModelFormField.FieldInfo.SOURCE_AUTO_ENTITY, this);
                //textField.setSize(1);
                //textField.setMaxlength(new Integer(1));
                //this.setFieldInfo(textField);
            } else if ("very-short".equals(modelField.getType())) {
                ModelFormField.TextField textField = new ModelFormField.TextField(ModelFormField.FieldInfo.SOURCE_AUTO_ENTITY, this);
                textField.setSize(6);
                textField.setMaxlength(new Integer(10));
                this.setFieldInfo(textField);
            } else if ("very-long".equals(modelField.getType())) {
                ModelFormField.TextareaField textareaField = new ModelFormField.TextareaField(ModelFormField.FieldInfo.SOURCE_AUTO_ENTITY, this);
                textareaField.setCols(60);
                textareaField.setRows(2);
                this.setFieldInfo(textareaField);
            } else if ("name".equals(modelField.getType()) || "short-varchar".equals(modelField.getType())) {
                ModelFormField.TextField textField = new ModelFormField.TextField(ModelFormField.FieldInfo.SOURCE_AUTO_ENTITY, this);
                textField.setSize(40);
                textField.setMaxlength(new Integer(60));
                this.setFieldInfo(textField);
            } else if (
                "value".equals(modelField.getType())
                    || "comment".equals(modelField.getType())
                    || "description".equals(modelField.getType())
                    || "long-varchar".equals(modelField.getType())
                    || "url".equals(modelField.getType())
                    || "email".equals(modelField.getType())) {
                ModelFormField.TextField textField = new ModelFormField.TextField(ModelFormField.FieldInfo.SOURCE_AUTO_ENTITY, this);
                textField.setSize(60);
                textField.setMaxlength(new Integer(250));
                this.setFieldInfo(textField);
            } else if (
                "floating-point".equals(modelField.getType()) || "currency-amount".equals(modelField.getType()) || "numeric".equals(modelField.getType())) {
                ModelFormField.TextField textField = new ModelFormField.TextField(ModelFormField.FieldInfo.SOURCE_AUTO_ENTITY, this);
                textField.setSize(6);
                this.setFieldInfo(textField);
            } else if ("date-time".equals(modelField.getType()) || "date".equals(modelField.getType()) || "time".equals(modelField.getType())) {
                ModelFormField.DateTimeField dateTimeField = new ModelFormField.DateTimeField(ModelFormField.FieldInfo.SOURCE_AUTO_ENTITY, this);
                if ("date-time".equals(modelField.getType())) {
                    dateTimeField.setType("timestamp");
                } else if ("date".equals(modelField.getType())) {
                    dateTimeField.setType("date");
                } else if ("time".equals(modelField.getType())) {
                    dateTimeField.setType("time");
                }
                this.setFieldInfo(dateTimeField);
            } else {
                ModelFormField.TextField textField = new ModelFormField.TextField(ModelFormField.FieldInfo.SOURCE_AUTO_ENTITY, this);
                this.setFieldInfo(textField);
            }
        }

        return true;
    }

    public void renderFieldString(StringBuffer buffer, Map context, FormStringRenderer formStringRenderer) {
        this.fieldInfo.renderFieldString(buffer, context, formStringRenderer);
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
     * @param fieldInfo
     */
    public void setFieldInfo(FieldInfo fieldInfo) {
        if (fieldInfo == null)
            return;

        // field info is a little different, check source for priority
        if (this.fieldInfo == null || (fieldInfo.getFieldSource() <= this.fieldInfo.getFieldSource())) {
            this.fieldInfo = fieldInfo;
            this.fieldInfo.modelFormField = this;
        }
    }

    /**
     * Gets the name of the Service Attribute (aka Parameter) that corresponds
     * with this field. This can be used to get additional information about the field.
     * Use the getServiceName() method to get the Entity name that the field is in.
     *
     * @return
     */
    public String getAttributeName() {
        if (UtilValidate.isNotEmpty(this.attributeName)) {
            return this.attributeName;
        } else {
            return this.name;
        }
    }

    /**
     * @return
     */
    public String getEntityName() {
        if (UtilValidate.isNotEmpty(this.entityName)) {
            return this.entityName;
        } else {
            return this.modelForm.getDefaultEntityName();
        }
    }

    /**
     * @return
     */
    public String getEntryName() {
        if (this.entryAcsr != null && !this.entryAcsr.isEmpty()) {
            return this.entryAcsr.getOriginalName();
        } else {
            return this.name;
        }
    }

    /**
     * Gets the entry from the context that corresponds to this field; if this
     * form is being rendered in an error condition (ie isError in the context
     * is true) then the value will be retreived from the parameters Map in
     * the context.
     *
     * @param context
     * @return
     */
    public String getEntry(Map context) {
        return this.getEntry(context, "");
    }

    public String getEntry(Map context, String defaultValue) {
        Boolean isError = (Boolean) context.get("isError");
        Boolean useRequestParameters = (Boolean) context.get("useRequestParameters");
        // if useRequestParameters is TRUE then parameters will always be used, if FALSE then parameters will never be used
        // if isError is TRUE and useRequestParameters is not FALSE (ie is null or TRUE) then parameters will be used
        if ((Boolean.TRUE.equals(isError) && !Boolean.FALSE.equals(useRequestParameters)) || (Boolean.TRUE.equals(useRequestParameters))) {
            //Debug.logInfo("Getting entry, isError true so getting from parameters for field " + this.getName() + " of form " + this.modelForm.getName(), module);
            Map parameters = (Map) context.get("parameters");
            if (parameters != null && parameters.get(this.getParameterName(context)) != null) {
                return (String) parameters.get(this.getParameterName(context));
            } else {
                return defaultValue;
            }
        } else {
            //Debug.logInfo("Getting entry, isError false so getting from Map in context for field " + this.getName() + " of form " + this.modelForm.getName(), module);
            Map dataMap = this.getMap(context);
            if (dataMap == null) {
                //Debug.logInfo("Getting entry, no Map found with name " + this.getMapName() + ", using context for field " + this.getName() + " of form " + this.modelForm.getName(), module);
                dataMap = context;
            }
            Object retVal = null;
            if (this.entryAcsr != null && !this.entryAcsr.isEmpty()) {
                //Debug.logInfo("Getting entry, using entryAcsr for field " + this.getName() + " of form " + this.modelForm.getName(), module);
                retVal = this.entryAcsr.get(dataMap);
            } else {
                //Debug.logInfo("Getting entry, no entryAcsr so using field name " + this.name + " for field " + this.getName() + " of form " + this.modelForm.getName(), module);
                // if no entry name was specified, use the field's name
                retVal = dataMap.get(this.name);
            }

            if (retVal != null) {
            	// format number in the default way for the server because that is how it will parse it; may want to change this to be based on the user's locale instead of the server's sometime in the future
            	if (retVal instanceof Double || retVal instanceof Float) {
            	    return NumberFormat.getInstance().format(retVal);
            	}
            	
                return retVal.toString();
            } else {
                return defaultValue;
            }
        }
    }

    public Map getMap(Map context) {
        if (this.mapAcsr == null || this.mapAcsr.isEmpty()) {
            //Debug.logInfo("Getting Map from default of the form because of no mapAcsr for field " + this.getName(), module);
            return this.modelForm.getDefaultMap(context);
        } else {
            //Debug.logInfo("Getting Map from mapAcsr for field " + this.getName(), module);
            return (Map) mapAcsr.get(context);
        }
    }

    /**
     * Gets the name of the Entity Field that corresponds
     * with this field. This can be used to get additional information about the field.
     * Use the getEntityName() method to get the Entity name that the field is in.
     *
     * @return
     */
    public String getFieldName() {
        if (UtilValidate.isNotEmpty(this.fieldName)) {
            return this.fieldName;
        } else {
            return this.name;
        }
    }

    /** Get the name of the Map in the form context that contains the entry,
     * available from the getEntryName() method. This entry is used to
     * pre-populate the field widget when not in an error condition. In an
     * error condition the parameter name is used to get the value from the
     * parameters Map.
     *
     * @return
     */
    public String getMapName() {
        if (this.mapAcsr != null && !this.mapAcsr.isEmpty()) {
            return this.mapAcsr.getOriginalName();
        } else {
            return this.modelForm.getDefaultMapName();
        }
    }

    /**
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * Get the name to use for the parameter for this field in the form interpreter.
     * For HTML forms this is the request parameter name.
     *
     * @return
     */
    public String getParameterName(Map context) {
        String baseName;
        if (UtilValidate.isNotEmpty(this.parameterName)) {
            baseName = this.parameterName;
        } else {
            baseName = this.name;
        }

        Integer itemIndex = (Integer) context.get("itemIndex");
        if (itemIndex != null && "multi".equals(this.modelForm.getType())) {
            return baseName + this.modelForm.getItemIndexSeparator() + itemIndex.intValue();
        } else {
            return baseName;
        }
    }

    /**
     * @return
     */
    public int getPosition() {
        if (this.position == null) {
            return 1;
        } else {
            return position.intValue();
        }
    }

    /**
     * @return
     */
    public String getRedWhen() {
        return redWhen;
    }

    /**
     * the widget/interaction part will be red if the date value is
     *  before-now (for ex. thruDate), after-now (for ex. fromDate), or by-name (if the
     *  field's name or entry-name or fromDate or thruDate the corresponding
     *  action will be done); only applicable when the field is a timestamp
     *
     * @param context
     * @return
     */
    public boolean shouldBeRed(Map context) {
        // red-when ( never | before-now | after-now | by-name ) "by-name"

        String redCondition = this.redWhen;

        if ("never".equals(redCondition)) {
            return false;
        }

        // for performance resaons we check this first, most fields will be eliminated here and the valueOfs will not be necessary
        if (UtilValidate.isEmpty(redCondition) || "by-name".equals(redCondition)) {
            if ("fromDate".equals(this.name) || (this.entryAcsr != null && "fromDate".equals(this.entryAcsr.getOriginalName()))) {
                redCondition = "after-now";
            } else if ("thruDate".equals(this.name) || (this.entryAcsr != null && "thruDate".equals(this.entryAcsr.getOriginalName()))) {
                redCondition = "before-now";
            } else {
                return false;
            }
        }

        boolean isBeforeNow = false;
        if ("before-now".equals(redCondition)) {
            isBeforeNow = true;
        } else if ("after-now".equals(redCondition)) {
            isBeforeNow = false;
        } else {
            return false;
        }

        java.sql.Date dateVal = null;
        java.sql.Time timeVal = null;
        java.sql.Timestamp timestampVal = null;

        //now before going on, check to see if the current entry is a valid date and/or time and get the value
        String value = this.getEntry(context);
        try {
            timestampVal = java.sql.Timestamp.valueOf(value);
        } catch (Exception e) {
            // okay, not a timestamp...
        }

        if (timestampVal == null) {
            try {
                dateVal = java.sql.Date.valueOf(value);
            } catch (Exception e) {
                // okay, not a date...
            }
        }

        if (timestampVal == null && dateVal == null) {
            try {
                timeVal = java.sql.Time.valueOf(value);
            } catch (Exception e) {
                // okay, not a time...
            }
        }

        if (timestampVal == null && dateVal == null && timeVal == null) {
            return false;
        }

        long nowMillis = System.currentTimeMillis();
        if (timestampVal != null) {
            java.sql.Timestamp nowStamp = new java.sql.Timestamp(nowMillis);
            if (!timestampVal.equals(nowStamp)) {
                if (isBeforeNow) {
                    if (timestampVal.before(nowStamp)) {
                        return true;
                    }
                } else {
                    if (timestampVal.after(nowStamp)) {
                        return true;
                    }
                }
            }
        } else if (dateVal != null) {
            java.sql.Date nowDate = new java.sql.Date(nowMillis);
            if (!dateVal.equals(nowDate)) {
                if (isBeforeNow) {
                    if (dateVal.before(nowDate)) {
                        return true;
                    }
                } else {
                    if (dateVal.after(nowDate)) {
                        return true;
                    }
                }
            }
        } else if (timeVal != null) {
            java.sql.Time nowTime = new java.sql.Time(nowMillis);
            if (!timeVal.equals(nowTime)) {
                if (isBeforeNow) {
                    if (timeVal.before(nowTime)) {
                        return true;
                    }
                } else {
                    if (timeVal.after(nowTime)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * @return
     */
    public String getServiceName() {
        if (UtilValidate.isNotEmpty(this.serviceName)) {
            return this.serviceName;
        } else {
            return this.modelForm.getDefaultServiceName();
        }
    }

    /**
     * @return
     */
    public String getTitle(Map context) {
        if (this.title != null && !this.title.isEmpty()) {
            return title.expandString(context);
        } else {
            // create a title from the name of this field; expecting a Java method/field style name, ie productName or productCategoryId
            if (this.name == null || this.name.length() == 0) {
                // this should never happen, ie name is required
                return "";
            }

            StringBuffer autoTitleBuffer = new StringBuffer();

            // always use upper case first letter...
            autoTitleBuffer.append(Character.toUpperCase(this.name.charAt(0)));

            // just put spaces before the upper case letters
            for (int i = 1; i < this.name.length(); i++) {
                char curChar = this.name.charAt(i);
                if (Character.isUpperCase(curChar)) {
                    autoTitleBuffer.append(' ');
                }
                autoTitleBuffer.append(curChar);
            }

            return autoTitleBuffer.toString();
        }
    }

    /**
     * @return
     */
    public String getTitleStyle() {
        if (UtilValidate.isNotEmpty(this.titleStyle)) {
            return this.titleStyle;
        } else {
            return this.modelForm.getDefaultTitleStyle();
        }
    }

    /**
     * @return
     */
    public String getTooltip(Map context) {
        if (tooltip != null && !tooltip.isEmpty()) {
            return tooltip.expandString(context);
        } else {
            return "";
        }
    }

    /**
     * @return
     */
    public String getUseWhen(Map context) {
        if (useWhen != null && !useWhen.isEmpty()) {
            return useWhen.expandString(context);
        } else {
            return "";
        }
    }

    /**
     * @return
     */
    public String getIdName() {
        return idName;
    }

    /**
     * @param string
     */
    public void setIdName(String string) {
        idName = string;
    }


    public boolean isUseWhenEmpty() {
        if (this.useWhen == null) {
            return true;
        }

        return this.useWhen.isEmpty();
    }

    public boolean shouldUse(Map context) {
        String useWhenStr = this.getUseWhen(context);
        if (UtilValidate.isEmpty(useWhenStr)) {
            return true;
        } else {
            try {
                Interpreter bsh = this.modelForm.getBshInterpreter(context);
                Object retVal = bsh.eval(useWhenStr);
                boolean condTrue = false;
                // retVal should be a Boolean, if not something weird is up...
                if (retVal instanceof Boolean) {
                    Boolean boolVal = (Boolean) retVal;
                    condTrue = boolVal.booleanValue();
                } else {
                    throw new IllegalArgumentException("Return value from use-when condition eval was not a Boolean: "
                            + retVal.getClass().getName() + " [" + retVal + "] on the field " + this.name + " of form " + this.modelForm.name);
                }

                return condTrue;
            } catch (EvalError e) {
                String errMsg = "Error evaluating BeanShell use-when condition [" + this.useWhen + "] on the field "
                        + this.name + " of form " + this.modelForm.name + ": " + e.toString();
                Debug.logError(e, errMsg, module);
                throw new IllegalArgumentException(errMsg);
            }
        }
    }

    /**
     * @return
     */
    public String getWidgetStyle() {
        if (UtilValidate.isNotEmpty(this.widgetStyle)) {
            return this.widgetStyle;
        } else {
            return this.modelForm.getDefaultWidgetStyle();
        }
    }

    /**
     * @return
     */
    public String getTooltipStyle() {
        if (UtilValidate.isNotEmpty(this.tooltipStyle)) {
            return this.tooltipStyle;
        } else {
            return this.modelForm.getDefaultTooltipStyle();
        }
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
        entryAcsr = new FlexibleMapAccessor(string);
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
        this.mapAcsr = new FlexibleMapAccessor(string);
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
        position = new Integer(i);
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
        this.titleStyle = string;
    }

    /**
     * @param string
     */
    public void setTooltip(String string) {
        this.tooltip = new FlexibleStringExpander(string);
    }

    /**
     * @param string
     */
    public void setUseWhen(String string) {
        this.useWhen = new FlexibleStringExpander(string);
    }

    /**
     * @param string
     */
    public void setWidgetStyle(String string) {
        this.widgetStyle = string;
    }

    /**
     * @param string
     */
    public void setTooltipStyle(String string) {
        this.tooltipStyle = string;
    }

    /**
     * @return
     */
    public boolean getSeparateColumn() {
        return this.separateColumn;
    }

    public static abstract class FieldInfo {

        public static final int DISPLAY = 1;
        public static final int HYPERLINK = 2;
        public static final int TEXT = 3;
        public static final int TEXTAREA = 4;
        public static final int DATE_TIME = 5;
        public static final int DROP_DOWN = 6;
        public static final int CHECK = 7;
        public static final int RADIO = 8;
        public static final int SUBMIT = 9;
        public static final int RESET = 10;
        public static final int HIDDEN = 11;
        public static final int IGNORED = 12;
        public static final int TEXTQBE = 13;
        public static final int DATEQBE = 14;
        public static final int RANGEQBE = 15;
        public static final int LOOKUP = 16;
        public static final int FILE = 17;
        public static final int PASSWORD = 18;
        public static final int IMAGE = 19;

        // the numbering here represents the priority of the source;
        //when setting a new fieldInfo on a modelFormField it will only set
        //the new one if the fieldSource is less than or equal to the existing
        //fieldSource, which should always be passed as one of the following...
        public static final int SOURCE_EXPLICIT = 1;
        public static final int SOURCE_AUTO_ENTITY = 2;
        public static final int SOURCE_AUTO_SERVICE = 3;

        public static Map fieldTypeByName = new HashMap();

        static {
            fieldTypeByName.put("display", new Integer(1));
            fieldTypeByName.put("hyperlink", new Integer(2));
            fieldTypeByName.put("text", new Integer(3));
            fieldTypeByName.put("textarea", new Integer(4));
            fieldTypeByName.put("date-time", new Integer(5));
            fieldTypeByName.put("drop-down", new Integer(6));
            fieldTypeByName.put("check", new Integer(7));
            fieldTypeByName.put("radio", new Integer(8));
            fieldTypeByName.put("submit", new Integer(9));
            fieldTypeByName.put("reset", new Integer(10));
            fieldTypeByName.put("hidden", new Integer(11));
            fieldTypeByName.put("ignored", new Integer(12));
            fieldTypeByName.put("text-find", new Integer(13));
            fieldTypeByName.put("date-find", new Integer(14));
            fieldTypeByName.put("range-find", new Integer(15));
            fieldTypeByName.put("lookup", new Integer(16));
            fieldTypeByName.put("file", new Integer(17));
            fieldTypeByName.put("password", new Integer(18));
            fieldTypeByName.put("image", new Integer(19));
        }

        protected int fieldType;
        protected int fieldSource;
        protected ModelFormField modelFormField;

        /** Don't allow the Default Constructor */
        protected FieldInfo() {}

        /** Value Constructor */
        public FieldInfo(int fieldSource, int fieldType, ModelFormField modelFormField) {
            this.fieldType = fieldType;
            this.fieldSource = fieldSource;
            this.modelFormField = modelFormField;
        }

        /** XML Constructor */
        public FieldInfo(Element element, ModelFormField modelFormField) {
            this.fieldSource = FieldInfo.SOURCE_EXPLICIT;
            this.fieldType = findFieldTypeFromName(element.getTagName());
            this.modelFormField = modelFormField;
        }

        /**
         * @return
         */
        public ModelFormField getModelFormField() {
            return modelFormField;
        }

        /**
         * @return
         */
        public int getFieldType() {
            return fieldType;
        }

        /**
         * @return
         */
        public int getFieldSource() {
            return this.fieldSource;
        }

        public static int findFieldTypeFromName(String name) {
            Integer fieldTypeInt = (Integer) FieldInfo.fieldTypeByName.get(name);
            if (fieldTypeInt != null) {
                return fieldTypeInt.intValue();
            } else {
                throw new IllegalArgumentException("Could not get fieldType for field type name " + name);
            }
        }

        public abstract void renderFieldString(StringBuffer buffer, Map context, FormStringRenderer formStringRenderer);
    }

    public static abstract class FieldInfoWithOptions extends FieldInfo {
        protected FieldInfoWithOptions() {
            super();
        }

        protected FlexibleStringExpander noCurrentSelectedKey;
        protected List optionSources = new LinkedList();

        public FieldInfoWithOptions(int fieldSource, int fieldType, ModelFormField modelFormField) {
            super(fieldSource, fieldType, modelFormField);
        }

        public FieldInfoWithOptions(Element element, ModelFormField modelFormField) {
            super(element, modelFormField);

            noCurrentSelectedKey = new FlexibleStringExpander(element.getAttribute("no-current-selected-key"));

            // read all option and entity-options sub-elements, maintaining order
            List childElements = UtilXml.childElementList(element);
            Iterator childElementIter = childElements.iterator();
            while (childElementIter.hasNext()) {
                Element childElement = (Element) childElementIter.next();
                if ("option".equals(childElement.getTagName())) {
                    this.addOptionSource(new SingleOption(childElement, this));
                } else if ("list-options".equals(childElement.getTagName())) {
                    this.addOptionSource(new ListOptions(childElement, this));
                } else if ("entity-options".equals(childElement.getTagName())) {
                    this.addOptionSource(new EntityOptions(childElement, this));
                }
            }
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

        public static String getDescriptionForOptionKey(String key, List allOptionValues) {
            if (UtilValidate.isEmpty(key)) {
                return "";
            }

            if (UtilValidate.isEmpty(allOptionValues)) {
                return key;
            }

            Iterator optionValueIter = allOptionValues.iterator();
            while (optionValueIter.hasNext()) {
                OptionValue optionValue = (OptionValue) optionValueIter.next();
                if (key.equals(optionValue.getKey())) {
                    return optionValue.getDescription();
                }
            }

            // if we get here we didn't find a match, just return the key
            return key;
        }

        public String getNoCurrentSelectedKey(Map context) {
            return this.noCurrentSelectedKey.expandString(context);
        }

        public void setNoCurrentSelectedKey(String string) {
            this.noCurrentSelectedKey = new FlexibleStringExpander(string);
        }

        public void addOptionSource(OptionSource optionSource) {
            this.optionSources.add(optionSource);
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
        protected FieldInfo fieldInfo;

        public abstract void addOptionValues(List optionValues, Map context, GenericDelegator delegator);
    }

    public static class SingleOption extends OptionSource {
        protected FlexibleStringExpander key;
        protected FlexibleStringExpander description;

        public SingleOption(String key, String description, FieldInfo fieldInfo) {
            this.key = new FlexibleStringExpander(key);
            this.description = new FlexibleStringExpander(UtilXml.checkEmpty(description, key));
            this.fieldInfo = fieldInfo;
        }

        public SingleOption(Element optionElement, FieldInfo fieldInfo) {
            this.key = new FlexibleStringExpander(optionElement.getAttribute("key"));
            this.description = new FlexibleStringExpander(UtilXml.checkEmpty(optionElement.getAttribute("description"), optionElement.getAttribute("key")));
            this.fieldInfo = fieldInfo;
        }

        public void addOptionValues(List optionValues, Map context, GenericDelegator delegator) {
            optionValues.add(new OptionValue(key.expandString(context), description.expandString(context)));
        }
    }

    public static class ListOptions extends OptionSource {
        protected FlexibleMapAccessor listAcsr;
        protected String listEntryName;
        protected FlexibleMapAccessor keyAcsr;
        protected FlexibleStringExpander description;

        public ListOptions(String listName, String listEntryName, String keyName, String description, FieldInfo fieldInfo) {
            this.listAcsr = new FlexibleMapAccessor(listName);
            this.listEntryName = listEntryName;
            this.keyAcsr = new FlexibleMapAccessor(keyName);
            this.description = new FlexibleStringExpander(description);
            this.fieldInfo = fieldInfo;
        }

        public ListOptions(Element optionElement, FieldInfo fieldInfo) {
            this.listEntryName = optionElement.getAttribute("list-entry-name");
            this.listAcsr = new FlexibleMapAccessor(optionElement.getAttribute("list-name"));
            this.keyAcsr = new FlexibleMapAccessor(optionElement.getAttribute("key-name"));
            this.listAcsr = new FlexibleMapAccessor(optionElement.getAttribute("list-name"));
            this.listEntryName = optionElement.getAttribute("list-entry-name");
            this.description = new FlexibleStringExpander(optionElement.getAttribute("description"));
            this.fieldInfo = fieldInfo;
        }

        public void addOptionValues(List optionValues, Map context, GenericDelegator delegator) {
            List dataList = (List) this.listAcsr.get(context);
            if (dataList != null && dataList.size() != 0) {
                Iterator dataIter = dataList.iterator();
                while (dataIter.hasNext()) {
                    Object data = dataIter.next();
                    Map localContext = new HashMap(context);
                    if (UtilValidate.isNotEmpty(this.listEntryName)) {
                        localContext.put(this.listEntryName, data);
                    } else {
                        localContext.putAll((Map) data);
                    }
                    optionValues.add(new OptionValue((String) keyAcsr.get(localContext), description.expandString(localContext)));
                }
            }
        }
    }

    public static class EntityOptions extends OptionSource {
        protected String entityName;
        protected String keyFieldName;
        protected FlexibleStringExpander description;
        protected boolean cache = true;
        protected String filterByDate;

        protected Map constraintMap = null;
        protected List orderByList = null;

        public EntityOptions(FieldInfo fieldInfo) {
            this.fieldInfo = fieldInfo;
        }

        public EntityOptions(Element entityOptionsElement, FieldInfo fieldInfo) {
            this.entityName = entityOptionsElement.getAttribute("entity-name");
            this.keyFieldName = entityOptionsElement.getAttribute("key-field-name");
            this.description = new FlexibleStringExpander(entityOptionsElement.getAttribute("description"));
            this.cache = !"false".equals(entityOptionsElement.getAttribute("cache"));
            this.filterByDate = entityOptionsElement.getAttribute("filter-by-date");

            List constraintElements = UtilXml.childElementList(entityOptionsElement, "entity-constraint");
            if (constraintElements != null && constraintElements.size() > 0) {
                this.constraintMap = new HashMap();
                Iterator constraintElementIter = constraintElements.iterator();
                while (constraintElementIter.hasNext()) {
                    Element constraintElement = (Element) constraintElementIter.next();
                    constraintMap.put(constraintElement.getAttribute("name"), new FlexibleStringExpander(constraintElement.getAttribute("value")));
                }
            }

            List orderByElements = UtilXml.childElementList(entityOptionsElement, "entity-order-by");
            if (orderByElements != null && orderByElements.size() > 0) {
                this.orderByList = new LinkedList();
                Iterator orderByElementIter = orderByElements.iterator();
                while (orderByElementIter.hasNext()) {
                    Element orderByElement = (Element) orderByElementIter.next();
                    orderByList.add(orderByElement.getAttribute("field-name"));
                }
            }

            this.fieldInfo = fieldInfo;
        }

        public String getKeyFieldName() {
            if (UtilValidate.isNotEmpty(this.keyFieldName)) {
                return this.keyFieldName;
            } else {
                // get the modelFormField fieldName
                return this.fieldInfo.getModelFormField().getFieldName();
            }
        }

        public void addOptionValues(List optionValues, Map context, GenericDelegator delegator) {
            // first expand any conditions that need expanding based on the current context
            Map expandedConstraintMap = null;
            if (this.constraintMap != null) {
                expandedConstraintMap = new HashMap();
                Iterator constraintMapIter = this.constraintMap.entrySet().iterator();
                while (constraintMapIter.hasNext()) {
                    Map.Entry entry = (Map.Entry) constraintMapIter.next();
                    expandedConstraintMap.put(entry.getKey(), ((FlexibleStringExpander) entry.getValue()).expandString(context));
                }
            }

            try {
                List values = null;
                if (this.cache) {
                    values = delegator.findByAndCache(this.entityName, expandedConstraintMap, this.orderByList);
                } else {
                    values = delegator.findByAnd(this.entityName, expandedConstraintMap, this.orderByList);
                }

                // filter-by-date if requested
                if ("true".equals(this.filterByDate)) {
                    values = EntityUtil.filterByDate(values, true);
                } else if (!"false".equals(this.filterByDate)) {
                    // not explicitly true or false, check to see if has fromDate and thruDate, if so do the filter
                    ModelEntity modelEntity = delegator.getModelEntity(this.entityName);
                    if (modelEntity != null && modelEntity.isField("fromDate") && modelEntity.isField("thruDate")) {
                        values = EntityUtil.filterByDate(values, true);
                    }
                }

                Iterator valueIter = values.iterator();
                while (valueIter.hasNext()) {
                    GenericValue value = (GenericValue) valueIter.next();
                    // add key and description with string expansion, ie expanding ${} stuff, passing locale explicitly to expand value stirng because it won't be found in the Entity
                    String optionDesc = this.description.expandString(value, UtilMisc.ensureLocale(context.get("locale")));
                    Object keyFieldObject = value.get(this.getKeyFieldName());
                    if (keyFieldObject == null) {
                    	throw new IllegalArgumentException("The value found for key-name [" + this.getKeyFieldName() + "], may not be a valid key field name.");
                    }
                    String keyFieldValue = keyFieldObject.toString();
                    optionValues.add(new OptionValue(keyFieldValue, optionDesc));
                }
            } catch (GenericEntityException e) {
                Debug.logError(e, "Error getting entity options in form", module);
            }
        }
    }

    public static class DisplayField extends FieldInfo {
        protected boolean alsoHidden = true;
        protected FlexibleStringExpander description;

        protected DisplayField() {
            super();
        }

        public DisplayField(ModelFormField modelFormField) {
            super(FieldInfo.SOURCE_EXPLICIT, FieldInfo.DISPLAY, modelFormField);
        }

        public DisplayField(int fieldSource, ModelFormField modelFormField) {
            super(fieldSource, FieldInfo.DISPLAY, modelFormField);
        }

        public DisplayField(Element element, ModelFormField modelFormField) {
            super(element, modelFormField);
            this.setDescription(element.getAttribute("description"));
            this.alsoHidden = !"false".equals(element.getAttribute("also-hidden"));
        }

        public void renderFieldString(StringBuffer buffer, Map context, FormStringRenderer formStringRenderer) {
            formStringRenderer.renderDisplayField(buffer, context, this);
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
            String retVal = null;
            if (this.description != null && !this.description.isEmpty()) {
                retVal = this.description.expandString(context);
            } else {
                retVal = modelFormField.getEntry(context);
            }
            if (retVal == null || retVal.length() == 0)
                retVal = "&nbsp;";
            return retVal;
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
            description = new FlexibleStringExpander(string);
        }
    }

    public static class HyperlinkField extends FieldInfo {
        public static String DEFAULT_TARGET_TYPE = "intra-app";

        protected boolean alsoHidden = true;
        protected String targetType;
        protected FlexibleStringExpander target;
        protected FlexibleStringExpander description;

        protected HyperlinkField() {
            super();
        }

        public HyperlinkField(ModelFormField modelFormField) {
            super(FieldInfo.SOURCE_EXPLICIT, FieldInfo.HYPERLINK, modelFormField);
        }

        public HyperlinkField(int fieldSource, ModelFormField modelFormField) {
            super(fieldSource, FieldInfo.HYPERLINK, modelFormField);
        }

        public HyperlinkField(Element element, ModelFormField modelFormField) {
            super(element, modelFormField);

            this.setDescription(element.getAttribute("description"));
            this.setTarget(element.getAttribute("target"));
            this.alsoHidden = !"false".equals(element.getAttribute("also-hidden"));
            this.targetType = element.getAttribute("target-type");
        }

        public void renderFieldString(StringBuffer buffer, Map context, FormStringRenderer formStringRenderer) {
            formStringRenderer.renderHyperlinkField(buffer, context, this);
        }

        /**
         * @return
         */
        public boolean getAlsoHidden() {
            return this.alsoHidden;
        }

        /**
         * @return
         */
        public String getTargetType() {
            if (UtilValidate.isNotEmpty(this.targetType)) {
                return this.targetType;
            } else {
                return HyperlinkField.DEFAULT_TARGET_TYPE;
            }
        }

        /**
         * @return
         */
        public String getDescription(Map context) {
            return this.description.expandString(context);
        }

        /**
         * @return
         */
        public String getTarget(Map context) {
            return this.target.expandString(context);
        }

        /**
         * @param b
         */
        public void setAlsoHidden(boolean b) {
            this.alsoHidden = b;
        }

        /**
         * @param string
         */
        public void setTargetType(String string) {
            this.targetType = string;
        }

        /**
         * @param string
         */
        public void setDescription(String string) {
            this.description = new FlexibleStringExpander(string);
        }

        /**
         * @param string
         */
        public void setTarget(String string) {
            this.target = new FlexibleStringExpander(string);
        }
    }

    public static class SubHyperlink {
        protected FlexibleStringExpander useWhen;
        protected String linkStyle;
        protected String targetType;
        protected FlexibleStringExpander target;
        protected FlexibleStringExpander description;

        public SubHyperlink(Element element) {
            this.setDescription(element.getAttribute("description"));
            this.setTarget(element.getAttribute("target"));
            this.setUseWhen(element.getAttribute("use-when"));
            this.linkStyle = element.getAttribute("link-style");
            this.targetType = element.getAttribute("target-type");
        }

        /**
         * @return
         */
        public String getLinkStyle() {
            return this.linkStyle;
        }

        /**
         * @return
         */
        public String getTargetType() {
            if (UtilValidate.isNotEmpty(this.targetType)) {
                return this.targetType;
            } else {
                return HyperlinkField.DEFAULT_TARGET_TYPE;
            }
        }

        /**
         * @return
         */
        public String getDescription(Map context) {
            if (this.description != null) {
                return this.description.expandString(context);
            } else {
                return "";
            }
        }

        /**
         * @return
         */
        public String getTarget(Map context) {
            if (this.target != null) {
                return this.target.expandString(context);
            } else {
                return "";
            }
        }

        /**
         * @return
         */
        public String getUseWhen(Map context) {
            if (this.useWhen != null) {
                return this.useWhen.expandString(context);
            } else {
                return "";
            }
        }

        public boolean shouldUse(Map context) {
            boolean shouldUse = true;
            String useWhen = this.getUseWhen(context);
            if (UtilValidate.isNotEmpty(useWhen)) {
                try {
                    Interpreter bsh = (Interpreter) context.get("bshInterpreter");
                    if (bsh == null) {
                        bsh = BshUtil.makeInterpreter(context);
                        context.put("bshInterpreter", bsh);
                    }

                    Object retVal = bsh.eval(useWhen);

                    // retVal should be a Boolean, if not something weird is up...
                    if (retVal instanceof Boolean) {
                        Boolean boolVal = (Boolean) retVal;
                        shouldUse = boolVal.booleanValue();
                    } else {
                        throw new IllegalArgumentException(
                            "Return value from target condition eval was not a Boolean: " + retVal.getClass().getName() + " [" + retVal + "]");
                    }
                } catch (EvalError e) {
                    String errmsg = "Error evaluating BeanShell target conditions";
                    Debug.logError(e, errmsg, module);
                    throw new IllegalArgumentException(errmsg);
                }
            }
            return shouldUse;
        }

        /**
         * @param string
         */
        public void setLinkStyle(String string) {
            this.linkStyle = string;
        }

        /**
         * @param string
         */
        public void setTargetType(String string) {
            this.targetType = string;
        }

        /**
         * @param string
         */
        public void setDescription(String string) {
            this.description = new FlexibleStringExpander(string);
        }

        /**
         * @param string
         */
        public void setTarget(String string) {
            this.target = new FlexibleStringExpander(string);
        }

        /**
         * @param string
         */
        public void setUseWhen(String string) {
            this.useWhen = new FlexibleStringExpander(string);
        }
    }

    public static class TextField extends FieldInfo {
        protected int size = 25;
        protected Integer maxlength;
        protected FlexibleStringExpander defaultValue;
        protected SubHyperlink subHyperlink;

        protected TextField() {
            super();
        }

        public TextField(ModelFormField modelFormField) {
            super(FieldInfo.SOURCE_EXPLICIT, FieldInfo.TEXT, modelFormField);
        }

        public TextField(int fieldSource, ModelFormField modelFormField) {
            super(fieldSource, FieldInfo.TEXT, modelFormField);
        }

        public TextField(Element element, ModelFormField modelFormField) {
            super(element, modelFormField);
            this.setDefaultValue(element.getAttribute("default-value"));

            String sizeStr = element.getAttribute("size");
            try {
                size = Integer.parseInt(sizeStr);
            } catch (Exception e) {
                if (sizeStr != null && sizeStr.length() > 0) {
                    Debug.logError("Could not parse the size value of the text element: [" + sizeStr + "], setting to the default of " + size, module);
                }
            }

            String maxlengthStr = element.getAttribute("maxlength");
            try {
                maxlength = Integer.valueOf(maxlengthStr);
            } catch (Exception e) {
                maxlength = null;
                if (maxlengthStr != null && maxlengthStr.length() > 0) {
                    Debug.logError(
                        "Could not parse the size value of the text element: [" + sizeStr + "], setting to null; default of no maxlength will be used",
                        module);
                }
            }

            Element subHyperlinkElement = UtilXml.firstChildElement(element, "sub-hyperlink");
            if (subHyperlinkElement != null) {
                this.subHyperlink = new SubHyperlink(subHyperlinkElement);
            }
        }

        public void renderFieldString(StringBuffer buffer, Map context, FormStringRenderer formStringRenderer) {
            formStringRenderer.renderTextField(buffer, context, this);
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
         * @return
         */
        public String getDefaultValue(Map context) {
            if (this.defaultValue != null) {
                return this.defaultValue.expandString(context);
            } else {
                return "";
            }
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

        /**
         * @param str
         */
        public void setDefaultValue(String str) {
            this.defaultValue = new FlexibleStringExpander(str);
        }

        public SubHyperlink getSubHyperlink() {
            return this.subHyperlink;
        }
        public void setSubHyperlink(SubHyperlink newSubHyperlink) {
            this.subHyperlink = newSubHyperlink;
        }
    }

    public static class TextareaField extends FieldInfo {
        protected int cols = 60;
        protected int rows = 2;
        protected FlexibleStringExpander defaultValue;

        protected TextareaField() {
            super();
        }

        public TextareaField(ModelFormField modelFormField) {
            super(FieldInfo.SOURCE_EXPLICIT, FieldInfo.TEXTAREA, modelFormField);
        }

        public TextareaField(int fieldSource, ModelFormField modelFormField) {
            super(fieldSource, FieldInfo.TEXTAREA, modelFormField);
        }

        public TextareaField(Element element, ModelFormField modelFormField) {
            super(element, modelFormField);
            this.setDefaultValue(element.getAttribute("default-value"));

            String colsStr = element.getAttribute("cols");
            try {
                cols = Integer.parseInt(colsStr);
            } catch (Exception e) {
                if (colsStr != null && colsStr.length() > 0) {
                    Debug.logError("Could not parse the size value of the text element: [" + colsStr + "], setting to default of " + cols, module);
                }
            }

            String rowsStr = element.getAttribute("rows");
            try {
                rows = Integer.parseInt(rowsStr);
            } catch (Exception e) {
                if (rowsStr != null && rowsStr.length() > 0) {
                    Debug.logError("Could not parse the size value of the text element: [" + rowsStr + "], setting to default of " + rows, module);
                }
            }
        }

        public void renderFieldString(StringBuffer buffer, Map context, FormStringRenderer formStringRenderer) {
            formStringRenderer.renderTextareaField(buffer, context, this);
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
         * @return
         */
        public String getDefaultValue(Map context) {
            if (this.defaultValue != null) {
                return this.defaultValue.expandString(context);
            } else {
                return "";
            }
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

        /**
         * @param str
         */
        public void setDefaultValue(String str) {
            this.defaultValue = new FlexibleStringExpander(str);
        }
    }

    public static class DateTimeField extends FieldInfo {
        protected String type;
        protected FlexibleStringExpander defaultValue;

        protected DateTimeField() {
            super();
        }

        public DateTimeField(ModelFormField modelFormField) {
            super(FieldInfo.SOURCE_EXPLICIT, FieldInfo.DATE_TIME, modelFormField);
        }

        public DateTimeField(int fieldSource, ModelFormField modelFormField) {
            super(fieldSource, FieldInfo.DATE_TIME, modelFormField);
        }

        public DateTimeField(Element element, ModelFormField modelFormField) {
            super(element, modelFormField);
            this.setDefaultValue(element.getAttribute("default-value"));
            type = element.getAttribute("type");
        }

        public void renderFieldString(StringBuffer buffer, Map context, FormStringRenderer formStringRenderer) {
            formStringRenderer.renderDateTimeField(buffer, context, this);
        }

        /**
         * @return
         */
        public String getType() {
            return type;
        }

        /**
         * @return
         */
        public String getDefaultValue(Map context) {
            if (this.defaultValue != null) {
                return this.defaultValue.expandString(context);
            } else {
                return "";
            }
        }

        /**
         * @param string
         */
        public void setType(String string) {
            type = string;
        }

        /**
         * @param str
         */
        public void setDefaultValue(String str) {
            this.defaultValue = new FlexibleStringExpander(str);
        }

        /**
         * Returns the default-value if specified, otherwise the current date, time or timestamp
         *
         * @param context Context Map
         * @return Default value string for date-time
         */
        public String getDefaultDateTimeString(Map context) {
            if (this.defaultValue != null && !this.defaultValue.isEmpty()) {
                return this.getDefaultValue(context);
            }

            if ("date".equals(this.type)) {
                return (new java.sql.Date(System.currentTimeMillis())).toString();
            } else if ("time".equals(this.type)) {
                return (new java.sql.Time(System.currentTimeMillis())).toString();
            } else {
                return UtilDateTime.nowTimestamp().toString();
            }
        }
    }

    public static class DropDownField extends FieldInfoWithOptions {
        protected boolean allowEmpty = false;
        protected String current;
        protected FlexibleStringExpander currentDescription;
        protected SubHyperlink subHyperlink;

        protected DropDownField() {
            super();
        }

        public DropDownField(ModelFormField modelFormField) {
            super(FieldInfo.SOURCE_EXPLICIT, FieldInfo.DROP_DOWN, modelFormField);
        }

        public DropDownField(int fieldSource, ModelFormField modelFormField) {
            super(fieldSource, FieldInfo.DROP_DOWN, modelFormField);
        }

        public DropDownField(Element element, ModelFormField modelFormField) {
            super(element, modelFormField);

            this.current = element.getAttribute("current");
            this.allowEmpty = "true".equals(element.getAttribute("allow-empty"));
            this.currentDescription = new FlexibleStringExpander(element.getAttribute("current-description"));

            Element subHyperlinkElement = UtilXml.firstChildElement(element, "sub-hyperlink");
            if (subHyperlinkElement != null) {
                this.subHyperlink = new SubHyperlink(subHyperlinkElement);
            }
        }

        public void renderFieldString(StringBuffer buffer, Map context, FormStringRenderer formStringRenderer) {
            formStringRenderer.renderDropDownField(buffer, context, this);
        }

        public boolean isAllowEmpty() {
            return this.allowEmpty;
        }

        public String getCurrent() {
            if (UtilValidate.isEmpty(this.current)) {
                return "first-in-list";
            } else {
                return this.current;
            }
        }

        public String getCurrentDescription(Map context) {
            if (this.currentDescription == null)
                return null;
            else
                return this.currentDescription.expandString(context);
        }

        public void setAllowEmpty(boolean b) {
            this.allowEmpty = b;
        }

        public void setCurrent(String string) {
            this.current = string;
        }

        public void setCurrentDescription(String string) {
            this.currentDescription = new FlexibleStringExpander(string);
        }

        public SubHyperlink getSubHyperlink() {
            return this.subHyperlink;
        }
        public void setSubHyperlink(SubHyperlink newSubHyperlink) {
            this.subHyperlink = newSubHyperlink;
        }
    }

    public static class RadioField extends FieldInfoWithOptions {
        protected RadioField() {
            super();
        }

        public RadioField(ModelFormField modelFormField) {
            super(FieldInfo.SOURCE_EXPLICIT, FieldInfo.RADIO, modelFormField);
        }

        public RadioField(int fieldSource, ModelFormField modelFormField) {
            super(fieldSource, FieldInfo.RADIO, modelFormField);
        }

        public RadioField(Element element, ModelFormField modelFormField) {
            super(element, modelFormField);
        }

        public void renderFieldString(StringBuffer buffer, Map context, FormStringRenderer formStringRenderer) {
            formStringRenderer.renderRadioField(buffer, context, this);
        }
    }

    public static class CheckField extends FieldInfo {
        protected CheckField() {
            super();
        }

        public CheckField(ModelFormField modelFormField) {
            super(FieldInfo.SOURCE_EXPLICIT, FieldInfo.CHECK, modelFormField);
        }

        public CheckField(int fieldSource, ModelFormField modelFormField) {
            super(fieldSource, FieldInfo.CHECK, modelFormField);
        }

        public CheckField(Element element, ModelFormField modelFormField) {
            super(element, modelFormField);
        }

        public void renderFieldString(StringBuffer buffer, Map context, FormStringRenderer formStringRenderer) {
            formStringRenderer.renderCheckField(buffer, context, this);
        }
    }

    public static class SubmitField extends FieldInfo {
        protected String buttonType;
        protected String imageLocation;

        protected SubmitField() {
            super();
        }

        public SubmitField(ModelFormField modelFormField) {
            super(FieldInfo.SOURCE_EXPLICIT, FieldInfo.SUBMIT, modelFormField);
        }

        public SubmitField(int fieldInfo, ModelFormField modelFormField) {
            super(fieldInfo, FieldInfo.SUBMIT, modelFormField);
        }

        public SubmitField(Element element, ModelFormField modelFormField) {
            super(element, modelFormField);
            this.buttonType = element.getAttribute("button-type");
            this.imageLocation = element.getAttribute("image-location");
        }

        public void renderFieldString(StringBuffer buffer, Map context, FormStringRenderer formStringRenderer) {
            formStringRenderer.renderSubmitField(buffer, context, this);
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
        protected ResetField() {
            super();
        }

        public ResetField(ModelFormField modelFormField) {
            super(FieldInfo.SOURCE_EXPLICIT, FieldInfo.RESET, modelFormField);
        }

        public ResetField(int fieldSource, ModelFormField modelFormField) {
            super(fieldSource, FieldInfo.RESET, modelFormField);
        }

        public ResetField(Element element, ModelFormField modelFormField) {
            super(element, modelFormField);
        }

        public void renderFieldString(StringBuffer buffer, Map context, FormStringRenderer formStringRenderer) {
            formStringRenderer.renderResetField(buffer, context, this);
        }
    }

    public static class HiddenField extends FieldInfo {
        protected FlexibleStringExpander value;

        protected HiddenField() {
            super();
        }

        public HiddenField(ModelFormField modelFormField) {
            super(FieldInfo.SOURCE_EXPLICIT, FieldInfo.HIDDEN, modelFormField);
        }

        public HiddenField(int fieldSource, ModelFormField modelFormField) {
            super(fieldSource, FieldInfo.HIDDEN, modelFormField);
        }

        public HiddenField(Element element, ModelFormField modelFormField) {
            super(element, modelFormField);
            this.setValue(element.getAttribute("value"));
        }

        public void renderFieldString(StringBuffer buffer, Map context, FormStringRenderer formStringRenderer) {
            formStringRenderer.renderHiddenField(buffer, context, this);
        }

        public String getValue(Map context) {
            if (this.value != null && !this.value.isEmpty()) {
                return this.value.expandString(context);
            } else {
                return modelFormField.getEntry(context);
            }
        }

        public void setValue(String string) {
            this.value = new FlexibleStringExpander(string);
        }
    }

    public static class IgnoredField extends FieldInfo {
        protected IgnoredField() {
            super();
        }

        public IgnoredField(ModelFormField modelFormField) {
            super(FieldInfo.SOURCE_EXPLICIT, FieldInfo.IGNORED, modelFormField);
        }

        public IgnoredField(int fieldSource, ModelFormField modelFormField) {
            super(fieldSource, FieldInfo.IGNORED, modelFormField);
        }

        public IgnoredField(Element element, ModelFormField modelFormField) {
            super(element, modelFormField);
        }

        public void renderFieldString(StringBuffer buffer, Map context, FormStringRenderer formStringRenderer) {
            formStringRenderer.renderIgnoredField(buffer, context, this);
        }
    }

    public static class TextFindField extends TextField {
        public TextFindField(Element element, ModelFormField modelFormField) {
            super(element, modelFormField);
        }

        public TextFindField(int fieldSource, ModelFormField modelFormField) {
            super(fieldSource, modelFormField);
        }

        public void renderFieldString(StringBuffer buffer, Map context, FormStringRenderer formStringRenderer) {
            formStringRenderer.renderTextFindField(buffer, context, this);
        }
    }

    public static class DateFindField extends DateTimeField {
        public DateFindField(Element element, ModelFormField modelFormField) {
            super(element, modelFormField);
        }

        public DateFindField(int fieldSource, ModelFormField modelFormField) {
            super(fieldSource, modelFormField);
        }

        public void renderFieldString(StringBuffer buffer, Map context, FormStringRenderer formStringRenderer) {
            formStringRenderer.renderDateFindField(buffer, context, this);
        }
    }

    public static class RangeFindField extends TextField {
        public RangeFindField(Element element, ModelFormField modelFormField) {
            super(element, modelFormField);
        }

        public RangeFindField(int fieldSource, ModelFormField modelFormField) {
            super(fieldSource, modelFormField);
        }

        public void renderFieldString(StringBuffer buffer, Map context, FormStringRenderer formStringRenderer) {
            formStringRenderer.renderRangeFindField(buffer, context, this);
        }
    }

    public static class LookupField extends TextField {
        protected String formName;

        public LookupField(Element element, ModelFormField modelFormField) {
            super(element, modelFormField);
            this.formName = element.getAttribute("target-form-name");
        }

        public LookupField(int fieldSource, ModelFormField modelFormField) {
            super(fieldSource, modelFormField);
        }

        public void renderFieldString(StringBuffer buffer, Map context, FormStringRenderer formStringRenderer) {
            formStringRenderer.renderLookupField(buffer, context, this);
        }

        public String getFormName() {
            return this.formName;
        }

        public void setFormName(String str) {
            this.formName = str;
        }
    }

    public static class FileField extends TextField {

        public FileField(Element element, ModelFormField modelFormField) {
            super(element, modelFormField);
        }

        public FileField(int fieldSource, ModelFormField modelFormField) {
            super(fieldSource, modelFormField);
        }

        public void renderFieldString(StringBuffer buffer, Map context, FormStringRenderer formStringRenderer) {
            formStringRenderer.renderFileField(buffer, context, this);
        }
    }

    public static class PasswordField extends TextField {

        public PasswordField(Element element, ModelFormField modelFormField) {
            super(element, modelFormField);
        }

        public PasswordField(int fieldSource, ModelFormField modelFormField) {
            super(fieldSource, modelFormField);
        }

        public void renderFieldString(StringBuffer buffer, Map context, FormStringRenderer formStringRenderer) {
            formStringRenderer.renderPasswordField(buffer, context, this);
        }
    }

    public static class ImageField extends FieldInfo {
        protected int border = 0;
        protected Integer width;
        protected Integer height;
        protected FlexibleStringExpander defaultValue;
        protected FlexibleStringExpander value;
        protected SubHyperlink subHyperlink;

        protected ImageField() {
            super();
        }

        public ImageField(ModelFormField modelFormField) {
            super(FieldInfo.SOURCE_EXPLICIT, FieldInfo.IMAGE, modelFormField);
        }

        public ImageField(int fieldSource, ModelFormField modelFormField) {
            super(fieldSource, FieldInfo.IMAGE, modelFormField);
        }

        public ImageField(Element element, ModelFormField modelFormField) {
            super(element, modelFormField);
            this.setValue(element.getAttribute("value"));

            String borderStr = element.getAttribute("border");
            try {
                border = Integer.parseInt(borderStr);
            } catch (Exception e) {
                if (borderStr != null && borderStr.length() > 0) {
                    Debug.logError("Could not parse the border value of the text element: [" + borderStr + "], setting to the default of " + border, module);
                }
            }

            String widthStr = element.getAttribute("width");
            try {
                width = Integer.valueOf(widthStr);
            } catch (Exception e) {
                width = null;
                if (widthStr != null && widthStr.length() > 0) {
                    Debug.logError(
                        "Could not parse the size value of the text element: [" + widthStr + "], setting to null; default of no width will be used",
                        module);
                }
            }

            String heightStr = element.getAttribute("height");
            try {
                height = Integer.valueOf(heightStr);
            } catch (Exception e) {
                height = null;
                if (heightStr != null && heightStr.length() > 0) {
                    Debug.logError(
                        "Could not parse the size value of the text element: [" + heightStr + "], setting to null; default of no height will be used",
                        module);
                }
            }

            Element subHyperlinkElement = UtilXml.firstChildElement(element, "sub-hyperlink");
            if (subHyperlinkElement != null) {
                this.subHyperlink = new SubHyperlink(subHyperlinkElement);
            }
        }

        public void renderFieldString(StringBuffer buffer, Map context, FormStringRenderer formStringRenderer) {
            formStringRenderer.renderImageField(buffer, context, this);
        }


        /**
         * @param str
         */
        public void setDefaultValue(String str) {
            this.defaultValue = new FlexibleStringExpander(str);
        }

        public SubHyperlink getSubHyperlink() {
            return this.subHyperlink;
        }
        public void setSubHyperlink(SubHyperlink newSubHyperlink) {
            this.subHyperlink = newSubHyperlink;
        }
        /**
         * @return
         */
        public Integer getWidth() {
            return width;
        }
        /**
         * @return
         */
        public Integer getHeight() {
            return height;
        }

        /**
         * @return
         */
        public int getBorder() {
            return border;
        }

        /**
         * @return
         */
        public String getDefaultValue(Map context) {
            if (this.defaultValue != null) {
                return this.defaultValue.expandString(context);
            } else {
                return "";
            }
        }

        public String getValue(Map context) {
            if (this.value != null && !this.value.isEmpty()) {
                return this.value.expandString(context);
            } else {
                return modelFormField.getEntry(context);
            }
        }

        public void setValue(String string) {
            this.value = new FlexibleStringExpander(string);
        }

    }

}
