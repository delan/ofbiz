/*
 * $Id: EntityFinderUtil.java,v 1.2 2004/07/24 09:43:27 jonesde Exp $
 *
 *  Copyright (c) 2004 The Open For Business Project - www.ofbiz.org
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
package org.ofbiz.entity.finder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilFormatOut;
import org.ofbiz.base.util.UtilXml;
import org.ofbiz.base.util.collections.FlexibleMapAccessor;
import org.ofbiz.base.util.string.FlexibleStringExpander;
import org.ofbiz.entity.GenericDelegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.condition.EntityComparisonOperator;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityJoinOperator;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.model.ModelEntity;
import org.ofbiz.entity.util.EntityListIterator;
import org.w3c.dom.Element;

/**
 * Uses the delegator to find entity values by a condition
 *
 * @author     <a href="mailto:jonesde@ofbiz.org">David E. Jones</a>
 * @version    $Revision: 1.2 $
 * @since      3.1
 */
public class EntityFinderUtil {
    
    public static final String module = EntityFinderUtil.class.getName();         
    
    public static Map makeFieldMap(Element element) {
        Map fieldMap = null;
        List fieldMapElementList = UtilXml.childElementList(element, "field-map");
        if (fieldMapElementList.size() > 0) {
            fieldMap = new HashMap();
            Iterator fieldMapElementIter = fieldMapElementList.iterator();
            while (fieldMapElementIter.hasNext()) {
                Element fieldMapElement = (Element) fieldMapElementIter.next();
                // set the env-name for each field-name, noting that if no field-name is specified it defaults to the env-name
                fieldMap.put(
                        new FlexibleMapAccessor(UtilFormatOut.checkEmpty(fieldMapElement.getAttribute("field-name"), fieldMapElement.getAttribute("env-name"))), 
                        new FlexibleMapAccessor(fieldMapElement.getAttribute("env-name")));
            }
        }
        return fieldMap;
    }

    public static void expandFieldMapToContext(Map fieldMap, Map context, Map outContext) {
        if (fieldMap != null) {
            Iterator fieldMapEntryIter = fieldMap.entrySet().iterator();
            while (fieldMapEntryIter.hasNext()) {
                Map.Entry entry = (Map.Entry) fieldMapEntryIter.next();
                FlexibleMapAccessor serviceContextFieldAcsr = (FlexibleMapAccessor) entry.getKey();
                FlexibleMapAccessor contextEnvAcsr = (FlexibleMapAccessor) entry.getValue();
                serviceContextFieldAcsr.put(outContext, contextEnvAcsr.get(context));
            }
        }
    }
    
    public static List makeSelectFieldExpanderList(Element element) {
        List selectFieldExpanderList = null;
        List selectFieldElementList = UtilXml.childElementList(element, "select-field");
        if (selectFieldElementList.size() > 0) {
            selectFieldExpanderList = new LinkedList();
            Iterator selectFieldElementIter = selectFieldElementList.iterator();
            while (selectFieldElementIter.hasNext()) {
                Element selectFieldElement = (Element) selectFieldElementIter.next();
                selectFieldExpanderList.add(new FlexibleStringExpander(selectFieldElement.getAttribute("field-name")));
            }
        }
        return selectFieldExpanderList;
    }
    
    public static Set makeFieldsToSelect(List selectFieldExpanderList, Map context) {
        Set fieldsToSelect = null;
        if (selectFieldExpanderList != null && selectFieldExpanderList.size() > 0) {
            fieldsToSelect = new HashSet();
            Iterator selectFieldExpanderIter = selectFieldExpanderList.iterator();
            while (selectFieldExpanderIter.hasNext()) {
                FlexibleStringExpander selectFieldExpander = (FlexibleStringExpander) selectFieldExpanderIter.next();
                fieldsToSelect.add(selectFieldExpander.expandString(context));
            }
        }
        return fieldsToSelect;
    }
    
    public static List makeOrderByFieldList(List orderByExpanderList, Map context) {
        List orderByFields = null;
        if (orderByExpanderList != null && orderByExpanderList.size() > 0) {
            orderByFields = new LinkedList();
            Iterator orderByExpanderIter = orderByExpanderList.iterator();
            while (orderByExpanderIter.hasNext()) {
                FlexibleStringExpander orderByExpander = (FlexibleStringExpander) orderByExpanderIter.next();
                orderByFields.add(orderByExpander.expandString(context));
            }
        }
        return orderByFields;
    }
    
    public static interface Condition {
        public EntityCondition createCondition(Map context, String entityName, GenericDelegator delegator);
    }
    public static class ConditionExpr implements Condition {
        FlexibleStringExpander fieldNameExdr;
        FlexibleStringExpander operatorExdr;
        FlexibleMapAccessor envNameAcsr;
        FlexibleStringExpander valueExdr;
        
        public ConditionExpr(Element conditionExprElement) {
            this.fieldNameExdr = new FlexibleStringExpander(conditionExprElement.getAttribute("field-name"));
            this.operatorExdr = new FlexibleStringExpander(conditionExprElement.getAttribute("operator"));
            this.envNameAcsr = new FlexibleMapAccessor(conditionExprElement.getAttribute("env-name"));
            this.valueExdr = new FlexibleStringExpander(conditionExprElement.getAttribute("value"));
        }
        
        public EntityCondition createCondition(Map context, String entityName, GenericDelegator delegator) {
            ModelEntity modelEntity = delegator.getModelEntity(entityName);
            String fieldName = fieldNameExdr.expandString(context);
            
            Object value = null;
            // start with the environment variable, will override if exists and a value is specified
            if (envNameAcsr != null) {
                value = envNameAcsr.get(context);
            }
            // no value so far, and a string value is specified, use that
            if (value == null && valueExdr != null) {
                value = valueExdr.expandString(context);
            }
            // now to a type conversion for the target fieldName
            value = modelEntity.convertFieldValue(fieldName, value, delegator);
            
            String operatorName = operatorExdr.expandString(context);
            EntityOperator operator = EntityOperator.lookup(operatorName);
            if (operator == null) {
                throw new IllegalArgumentException("Could not find an entity operator for the name: " + operatorName);
            }
            
            return new EntityExpr(fieldName, (EntityComparisonOperator) operator, value);
        }
    }
    public static class ConditionList implements Condition {
        List conditionList = new LinkedList();
        FlexibleStringExpander combineExdr;
        
        public ConditionList(Element conditionListElement) {
            this.combineExdr = new FlexibleStringExpander(conditionListElement.getAttribute("combine"));
            
            List subElements = UtilXml.childElementList(conditionListElement);
            Iterator subElementIter = subElements.iterator();
            while (subElementIter.hasNext()) {
                Element subElement = (Element) subElementIter.next();
                if ("condition-expr".equals(subElement.getNodeName())) {
                    conditionList.add(new ConditionExpr(subElement));
                } else if ("condition-list".equals(subElement.getNodeName())) {
                    conditionList.add(new ConditionList(subElement));
                } else {
                    throw new IllegalArgumentException("Invalid element with name [" + subElement.getNodeName() + "] found under a condition-list element.");
                }
            }
        }
        
        public EntityCondition createCondition(Map context, String entityName, GenericDelegator delegator) {
            if (this.conditionList.size() == 0) {
                return null;
            }
            if (this.conditionList.size() == 1) {
                Condition condition = (Condition) this.conditionList.get(0);
                return condition.createCondition(context, entityName, delegator);
            }
            
            List entityConditionList = new LinkedList();
            Iterator conditionIter = conditionList.iterator();
            while (conditionIter.hasNext()) {
                Condition curCondition = (Condition) conditionIter.next();
                entityConditionList.add(curCondition.createCondition(context, entityName, delegator));
            }
            
            String operatorName = combineExdr.expandString(context);
            EntityOperator operator = EntityOperator.lookup(operatorName);
            if (operator == null) {
                throw new IllegalArgumentException("Could not find an entity operator for the name: " + operatorName);
            }
            
            return new EntityConditionList(entityConditionList, (EntityJoinOperator) operator);
        }
    }
    
    public static interface OutputHandler {
        public void handleOutput(EntityListIterator eli, Map context, FlexibleMapAccessor listAcsr);
        public void handleOutput(List results, Map context, FlexibleMapAccessor listAcsr);
    }
    public static class LimitRange implements OutputHandler {
        FlexibleStringExpander startExdr;
        FlexibleStringExpander sizeExdr;
        
        public LimitRange(Element limitRangeElement) {
            this.startExdr = new FlexibleStringExpander(limitRangeElement.getAttribute("start"));
            this.sizeExdr = new FlexibleStringExpander(limitRangeElement.getAttribute("size"));
        }
        
        int getStart(Map context) {
            String startStr = this.startExdr.expandString(context);
            try {
                return Integer.parseInt(startStr);
            } catch (NumberFormatException e) {
                String errMsg = "The limit-range start number \"" + startStr + "\" was not valid: " + e.toString();
                Debug.logError(e, errMsg, module);
                throw new IllegalArgumentException(errMsg);
            }
        }
        
        int getSize(Map context) {
            String sizeStr = this.sizeExdr.expandString(context);
            try {
                return Integer.parseInt(sizeStr);
            } catch (NumberFormatException e) {
                String errMsg = "The limit-range size number \"" + sizeStr + "\" was not valid: " + e.toString();
                Debug.logError(e, errMsg, module);
                throw new IllegalArgumentException(errMsg);
            }
        }
        
        public void handleOutput(EntityListIterator eli, Map context, FlexibleMapAccessor listAcsr) {
            int start = getStart(context);
            int size = getSize(context);
            try {
                listAcsr.put(context, eli.getPartialList(start, size));
            } catch (GenericEntityException e) {
                String errMsg = "Error getting partial list in limit-range with start=" + start + " and size=" + size + ": " + e.toString();
                Debug.logError(e, errMsg, module);
                throw new IllegalArgumentException(errMsg);
            }
        }

        public void handleOutput(List results, Map context, FlexibleMapAccessor listAcsr) {
            int start = getStart(context);
            int size = getSize(context);
            
            int end = start + size;
            if (end > results.size()) end = results.size();
            
            listAcsr.put(context, results.subList(start, end));
        }
    }
    public static class LimitView implements OutputHandler {
        FlexibleStringExpander viewIndexExdr;
        FlexibleStringExpander viewSizeExdr;
        
        public LimitView(Element limitViewElement) {
            this.viewIndexExdr = new FlexibleStringExpander(limitViewElement.getAttribute("view-index"));
            this.viewSizeExdr = new FlexibleStringExpander(limitViewElement.getAttribute("view-size"));
        }
        
        int getIndex(Map context) {
            String viewIndexStr = this.viewIndexExdr.expandString(context);
            try {
                return Integer.parseInt(viewIndexStr);
            } catch (NumberFormatException e) {
                String errMsg = "The limit-view view-index number \"" + viewIndexStr + "\" was not valid: " + e.toString();
                Debug.logError(e, errMsg, module);
                throw new IllegalArgumentException(errMsg);
            }
        }
        
        int getSize(Map context) {
            String viewSizeStr = this.viewSizeExdr.expandString(context);
            try {
                return Integer.parseInt(viewSizeStr);
            } catch (NumberFormatException e) {
                String errMsg = "The limit-view view-size number \"" + viewSizeStr + "\" was not valid: " + e.toString();
                Debug.logError(e, errMsg, module);
                throw new IllegalArgumentException(errMsg);
            }
        }
        
        public void handleOutput(EntityListIterator eli, Map context, FlexibleMapAccessor listAcsr) {
            int index = this.getIndex(context);
            int size = this.getSize(context);
            
            try {
                listAcsr.put(context, eli.getPartialList(index * size, size));
            } catch (GenericEntityException e) {
                String errMsg = "Error getting partial list in limit-view with index=" + index + " and size=" + size + ": " + e.toString();
                Debug.logError(e, errMsg, module);
                throw new IllegalArgumentException(errMsg);
            }
        }

        public void handleOutput(List results, Map context, FlexibleMapAccessor listAcsr) {
            int index = this.getIndex(context);
            int size = this.getSize(context);
            
            int begin = index * size;
            int end = index * size + size;
            if (end > results.size()) end = results.size();
            
            listAcsr.put(context, results.subList(begin, end));
        }
    }
    public static class UseIterator implements OutputHandler {
        public UseIterator(Element useIteratorElement) {
            // no parameters, nothing to do
        }
        
        public void handleOutput(EntityListIterator eli, Map context, FlexibleMapAccessor listAcsr) {
            listAcsr.put(context, eli);
        }

        public void handleOutput(List results, Map context, FlexibleMapAccessor listAcsr) {
            throw new IllegalArgumentException("Cannot handle output with use-iterator when the query is cached, or the result in general is not an EntityListIterator");
        }
    }
}

