/*
 * $Id$
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
package org.ofbiz.content.content;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;

import bsh.EvalError;
import freemarker.ext.dom.NodeModel;

import org.ofbiz.base.util.BshUtil;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.GeneralException;
import org.ofbiz.base.util.StringUtil;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.base.util.string.FlexibleStringExpander;
import org.ofbiz.content.data.DataResourceWorker;
import org.ofbiz.content.webapp.ftl.FreeMarkerWorker;
import org.ofbiz.entity.GenericDelegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.minilang.MiniLangException;
import org.ofbiz.minilang.SimpleMapProcessor;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;
import org.ofbiz.entity.model.ModelEntity;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
//import com.clarkware.profiler.Profiler;

/**
 * ContentWorker Class
 * 
 * @author <a href="mailto:byersa@automationgroups.com">Al Byers</a>
 * @version $Rev$
 * @since 2.2
 * 
 *  
 */
public class ContentWorker {

    public static final String module = ContentWorker.class.getName();

    public static GenericValue findAlternateLocaleContent(GenericDelegator delegator, GenericValue view, Locale locale) {
        GenericValue contentAssocDataResourceViewFrom = view;
        if (locale == null) {
            return contentAssocDataResourceViewFrom;
        }
        
        String localeStr = locale.toString();
        boolean isTwoLetterLocale = localeStr.length() == 2;

        List alternateViews = null;
        try {
            alternateViews = view.getRelated("ContentAssocDataResourceViewTo", UtilMisc.toMap("caContentAssocTypeId", "ALTERNATE_LOCALE"), UtilMisc.toList("-caFromDate"));
        } catch (GenericEntityException e) {
            Debug.logError(e, "Error finding alternate locale content: " + e.toString(), module);
            return contentAssocDataResourceViewFrom;
        }
        
        alternateViews = EntityUtil.filterByDate(alternateViews, UtilDateTime.nowTimestamp(), "caFromDate", "caThruDate", true);
        Iterator alternateViewIter = alternateViews.iterator();
        while (alternateViewIter.hasNext()) {
            GenericValue thisView = (GenericValue) alternateViewIter.next();
            String currentLocaleString = thisView.getString("localeString");
            if (UtilValidate.isEmpty(currentLocaleString)) {
                continue;
            }
            
            int currentLocaleLength = currentLocaleString.length();
            
            // could be a 2 letter or 5 letter code
            if (isTwoLetterLocale) {
                if (currentLocaleLength == 2) {
                    // if the currentLocaleString is only a two letter code and the current one is a two and it matches, we are done
                    if (localeStr.equals(currentLocaleString)) {
                        contentAssocDataResourceViewFrom = thisView;
                        break;
                    }
                } else if (currentLocaleLength == 5) {
                    // if the currentLocaleString is only a two letter code and the current one is a five, match up but keep going
                    if (localeStr.equals(currentLocaleString.substring(0, 2))) {
                        contentAssocDataResourceViewFrom = thisView;
                    }
                }
            } else {
                if (currentLocaleLength == 2) {
                    // if the currentLocaleString is a five letter code and the current one is a two and it matches, keep going
                    if (localeStr.substring(0, 2).equals(currentLocaleString)) {
                        contentAssocDataResourceViewFrom = thisView;
                    }
                } else if (currentLocaleLength == 5) {
                    // if the currentLocaleString is a five letter code and the current one is a five, if it matches we are done
                    if (localeStr.equals(currentLocaleString)) {
                        contentAssocDataResourceViewFrom = thisView;
                        break;
                    }
                }
            }
        }
        
        return contentAssocDataResourceViewFrom;
    }

    public static void traverse(GenericDelegator delegator, GenericValue content, Timestamp fromDate, Timestamp thruDate, Map whenMap, int depthIdx, Map masterNode, String contentAssocTypeId, List pickList, String direction) {
        
        //String startContentAssocTypeId = null;
        String contentTypeId = null;
        String contentId = null;
        try {
            if (contentAssocTypeId == null) {
                contentAssocTypeId = "";
            }
            contentId = (String) content.get("contentId");
            contentTypeId = (String) content.get("contentTypeId");
            List topicList = content.getRelatedByAnd("ToContentAssoc", UtilMisc.toMap("contentAssocTypeId", "TOPIC"));
            List topics = new ArrayList();
            for (int i = 0; i < topicList.size(); i++) {
                GenericValue assoc = (GenericValue) topicList.get(i);
                topics.add(assoc.get("contentId"));
            }
            List keywordList = content.getRelatedByAnd("ToContentAssoc", UtilMisc.toMap("contentAssocTypeId", "KEYWORD"));
            List keywords = new ArrayList();
            for (int i = 0; i < keywordList.size(); i++) {
                GenericValue assoc = (GenericValue) keywordList.get(i);
                keywords.add(assoc.get("contentId"));
            }
            List purposeValueList = content.getRelatedCache("ContentPurpose");
            List purposes = new ArrayList();
            for (int i = 0; i < purposeValueList.size(); i++) {
                GenericValue purposeValue = (GenericValue) purposeValueList.get(i);
                purposes.add(purposeValue.get("contentPurposeTypeId"));
            }
            List contentTypeAncestry = new ArrayList();
            getContentTypeAncestry(delegator, contentTypeId, contentTypeAncestry);

            Map context = new HashMap();
            context.put("content", content);
            context.put("contentAssocTypeId", contentAssocTypeId);
            //context.put("related", related);
            context.put("purposes", purposes);
            context.put("topics", topics);
            context.put("keywords", keywords);
            context.put("typeAncestry", contentTypeAncestry);
            boolean isPick = checkWhen(context, (String) whenMap.get("pickWhen"));
            boolean isReturnBefore = checkReturnWhen(context, (String) whenMap.get("returnBeforePickWhen"));
            Map thisNode = null;
            if (isPick || !isReturnBefore) {
                thisNode = new HashMap();
                thisNode.put("contentId", contentId);
                thisNode.put("contentTypeId", contentTypeId);
                thisNode.put("contentAssocTypeId", contentAssocTypeId);
                List kids = (List) masterNode.get("kids");
                if (kids == null) {
                    kids = new ArrayList();
                    masterNode.put("kids", kids);
                }
                kids.add(thisNode);
            }
            if (isPick) {
                pickList.add(content);
                thisNode.put("value", content);
            }
            boolean isReturnAfter = checkReturnWhen(context, (String) whenMap.get("returnAfterPickWhen"));
            if (!isReturnAfter) {

                List relatedAssocs = getContentAssocsWithId(delegator, contentId, fromDate, thruDate, direction, new ArrayList());
                Iterator it = relatedAssocs.iterator();
                Map assocContext = new HashMap();
                assocContext.put("related", relatedAssocs);
                while (it.hasNext()) {
                    GenericValue assocValue = (GenericValue) it.next();
                    contentAssocTypeId = (String) assocValue.get("contentAssocTypeId");
                    assocContext.put("contentAssocTypeId", contentAssocTypeId);
                    //assocContext.put("contentTypeId", assocValue.get("contentTypeId") );
                    assocContext.put("parentContent", content);
                    String assocRelation = null;
                    // This needs to be the opposite
                    String relatedDirection = null;
                    if (direction != null && direction.equalsIgnoreCase("From")) {
                        assocContext.put("contentIdFrom", assocValue.get("contentId"));
                        assocRelation = "ToContent";
                        relatedDirection = "From";
                    } else {
                        assocContext.put("contentIdTo", assocValue.get("contentId"));
                        assocRelation = "FromContent";
                        relatedDirection = "To";
                    }

                    boolean isFollow = checkWhen(assocContext, (String) whenMap.get("followWhen"));
                    if (isFollow) {
                        GenericValue thisContent = assocValue.getRelatedOne(assocRelation);
                        traverse(delegator, thisContent, fromDate, thruDate, whenMap, depthIdx + 1, thisNode, contentAssocTypeId, pickList, relatedDirection);
                    }
                }
            }
        } catch (GenericEntityException e) {
            Debug.logError("Entity Error:" + e.getMessage(), null);
        }
        return;
    }

    public static boolean traverseSubContent(Map ctx) {

        boolean inProgress = false;
        List nodeTrail = (List)ctx.get("nodeTrail");
               FreeMarkerWorker.traceNodeTrail("11",nodeTrail);
        int sz = nodeTrail.size();
        if (sz == 0) 
            return false;

        Map currentNode = (Map)nodeTrail.get(sz - 1);
        Boolean isReturnAfter = (Boolean)currentNode.get("isReturnAfter");
        if (isReturnAfter != null && isReturnAfter.booleanValue())
            return false;

        List kids = (List)currentNode.get("kids");
        if (kids != null && kids.size() > 0) {
            int idx = 0;
            while (idx < kids.size()) {
                currentNode = (Map)kids.get(idx);
                FreeMarkerWorker.traceNodeTrail("12",nodeTrail);
                Boolean isPick = (Boolean)currentNode.get("isPick");
               
                if (isPick != null && isPick.booleanValue()) {
                    nodeTrail.add(currentNode);
                    inProgress = true;
                    selectKids(currentNode, ctx);
                    FreeMarkerWorker.traceNodeTrail("14",nodeTrail);
                    break;
                } else {
                    Boolean isFollow = (Boolean)currentNode.get("isFollow");
                    if (isFollow != null || isFollow.booleanValue()) {
                        nodeTrail.add(currentNode);
                        boolean foundPick = traverseSubContent(ctx);
                        if (foundPick) {
                            inProgress = true;
                            break;
                        }
                    }
                }
                idx++;
            }
        } 

        if (!inProgress) {
            // look for next sibling
            while (sz > 1) {
                currentNode = (Map)nodeTrail.remove(--sz);
                FreeMarkerWorker.traceNodeTrail("15",nodeTrail);
                Map parentNode = (Map)nodeTrail.get(sz - 1);
                kids = (List)parentNode.get("kids");
                if (kids == null)
                    continue;

                int idx = kids.indexOf(currentNode);
                while (idx < (kids.size() - 1)) {
                    currentNode = (Map)kids.get(idx + 1);
                    Boolean isFollow = (Boolean)currentNode.get("isFollow");
                    if (isFollow == null || !isFollow.booleanValue()) {
                        idx++;
                        continue;
                    }
                    String contentAssocTypeId = (String)currentNode.get("contentAssocTypeId");
                    nodeTrail.add(currentNode);
               FreeMarkerWorker.traceNodeTrail("16",nodeTrail);
                    Boolean isPick = (Boolean)currentNode.get("isPick");
                    if (isPick == null || !isPick.booleanValue()) {
                        // If not a "pick" node, look at kids
                        inProgress = traverseSubContent(ctx);
               FreeMarkerWorker.traceNodeTrail("17",nodeTrail);
                        if (inProgress)
                            break;
                    } else {
                        inProgress = true;
                        break;
                    }
                    idx++;
                }
                if (inProgress)
                    break;
            }
        }
        return inProgress;
    }

    public static List getPurposes(GenericValue content) {
        List purposes = new ArrayList();
        try {
            List purposeValueList = content.getRelatedCache("ContentPurpose");
            for (int i = 0; i < purposeValueList.size(); i++) {
                GenericValue purposeValue = (GenericValue) purposeValueList.get(i);
                purposes.add(purposeValue.get("contentPurposeTypeId"));
            }
        } catch (GenericEntityException e) {
            Debug.logError("Entity Error:" + e.getMessage(), null);
        }
        return purposes;
    }

    public static List getSections(GenericValue content) {
        List sections = new ArrayList();
        try {
            List sectionValueList = content.getRelatedCache("FromContentAssoc");
            for (int i = 0; i < sectionValueList.size(); i++) {
                GenericValue sectionValue = (GenericValue) sectionValueList.get(i);
                String contentAssocPredicateId = (String)sectionValue.get("contentAssocPredicateId");
                if (contentAssocPredicateId != null && contentAssocPredicateId.equals("categorizes")) {
                    sections.add(sectionValue.get("contentIdTo"));
                }
            }
        } catch (GenericEntityException e) {
            Debug.logError("Entity Error:" + e.getMessage(), null);
        }
        return sections;
    }

    public static List getTopics(GenericValue content) {
        List topics = new ArrayList();
        try {
            List topicValueList = content.getRelatedCache("FromContentAssoc");
            for (int i = 0; i < topicValueList.size(); i++) {
                GenericValue topicValue = (GenericValue) topicValueList.get(i);
                String contentAssocPredicateId = (String)topicValue.get("contentAssocPredicateId");
                if (contentAssocPredicateId != null && contentAssocPredicateId.equals("topifies"))
                    topics.add(topicValue.get("contentIdTo"));
            }
        } catch (GenericEntityException e) {
            Debug.logError("Entity Error:" + e.getMessage(), null);
        }
        return topics;
    }

    public static void selectKids(Map currentNode, Map ctx) {
        
        GenericDelegator delegator = (GenericDelegator)ctx.get("delegator");
        GenericValue parentContent = (GenericValue)currentNode.get("value");
        String contentAssocTypeId = (String)ctx.get("contentAssocTypeId");
        String contentTypeId = (String)ctx.get("contentTypeId");
        String mapKey = (String)ctx.get("mapKey");
        String parentContentId = (String) parentContent.get("contentId");
        //if (Debug.infoOn()) Debug.logInfo("traverse, contentAssocTypeId:" + contentAssocTypeId,null);
        Map whenMap = (Map)ctx.get("whenMap");
        Map context = new HashMap();
        List kids = new ArrayList();
        currentNode.put("kids", kids);
        String direction = (String)ctx.get("direction");
        if (UtilValidate.isEmpty(direction))
            direction = "From";
        Timestamp fromDate = (Timestamp)ctx.get("fromDate");
        Timestamp thruDate = (Timestamp)ctx.get("thruDate");
        
        List assocTypeList = StringUtil.split(contentAssocTypeId, " ");
        List contentTypeList = StringUtil.split(contentTypeId, " ");
        String contentAssocPredicateId = null;
        Boolean nullThruDatesOnly = new Boolean(true);
        Map results = null;
        try {
            results = ContentServicesComplex.getAssocAndContentAndDataResourceCacheMethod(delegator, parentContentId, mapKey, direction, null, null, assocTypeList, contentTypeList, nullThruDatesOnly, contentAssocPredicateId);
        } catch (GenericEntityException e) {
            throw new RuntimeException(e.getMessage());
        } catch (MiniLangException e2) {
            throw new RuntimeException(e2.getMessage());
        }
        List relatedViews = (List)results.get("entityList");
        //if (Debug.infoOn()) Debug.logInfo("traverse, relatedViews:" + relatedViews,null);
        Iterator it = relatedViews.iterator();
        while (it.hasNext()) {
            GenericValue assocValue = (GenericValue) it.next();
            Map thisNode = FreeMarkerWorker.makeNode(assocValue);
            checkConditions(delegator, thisNode, null, whenMap);
            boolean isReturnBeforePick = booleanDataType(thisNode.get("isReturnBeforePick"));
            boolean isReturnAfterPick = booleanDataType(thisNode.get("isReturnAfterPick"));
            boolean isPick = booleanDataType(thisNode.get("isPick"));
            boolean isFollow = booleanDataType(thisNode.get("isFollow"));
            kids.add(thisNode);
            if (isPick) {
                    Integer count = (Integer) currentNode.get("count");
                    if (count == null) {
                        count = new Integer(1);
                    } else {
                        count = new Integer(count.intValue() + 1);
                    }
                    currentNode.put("count", count);
            }
        }
        return;
    }

    public static boolean checkWhen(Map context, String whenStr) {
   
        boolean isWhen = true; //opposite default from checkReturnWhen
        if (whenStr != null && whenStr.length() > 0) {
            FlexibleStringExpander fse = new FlexibleStringExpander(whenStr);
            String newWhen = fse.expandString(context);
            //if (Debug.infoOn()) Debug.logInfo("newWhen:" + newWhen,null);
            //if (Debug.infoOn()) Debug.logInfo("context:" + context,null);
            try {
                Boolean isWhenObj = (Boolean) BshUtil.eval(newWhen, context);
                isWhen = isWhenObj.booleanValue();
            } catch (EvalError e) {
                Debug.logError("Error in evaluating :" + whenStr + " : " + e.getMessage(), null);
                throw new RuntimeException(e.getMessage());

            }
        }
        //if (Debug.infoOn()) Debug.logInfo("isWhen:" + isWhen,null);
        return isWhen;
    }

    public static boolean checkReturnWhen(Map context, String whenStr) {
        boolean isWhen = false; //opposite default from checkWhen
        if (whenStr != null && whenStr.length() > 0) {
            FlexibleStringExpander fse = new FlexibleStringExpander(whenStr);
            String newWhen = fse.expandString(context);
            try {
                Boolean isWhenObj = (Boolean) BshUtil.eval(newWhen, context);
                isWhen = isWhenObj.booleanValue();
            } catch (EvalError e) {
                Debug.logError("Error in evaluating :" + whenStr + " : " + e.getMessage(), null);
                throw new RuntimeException(e.getMessage());
            }
        }
        return isWhen;
    }

    public static List getAssociatedContent(GenericValue currentContent, String linkDir, List assocTypes, List contentTypes, String fromDate, String thruDate)
        throws GenericEntityException {

        GenericDelegator delegator = currentContent.getDelegator();
        List assocList = getAssociations(currentContent, linkDir, assocTypes, fromDate, thruDate);
        if (assocList == null || assocList.size() == 0)
            return assocList;
        if (Debug.infoOn()) Debug.logInfo("assocList:" + assocList.size() + " contentId:" + currentContent.getString("contentId"), "");

        List contentList = new ArrayList();
        String contentIdName = "contentId";
        if (linkDir != null && linkDir.equalsIgnoreCase("TO")) {
            contentIdName.concat("To");
        }
        GenericValue assoc = null;
        GenericValue content = null;
        String contentTypeId = null;
        Iterator assocIt = assocList.iterator();
        while (assocIt.hasNext()) {
            assoc = (GenericValue) assocIt.next();
            String contentId = (String) assoc.get(contentIdName);
            if (Debug.infoOn()) Debug.logInfo("contentId:" + contentId, "");
            content = delegator.findByPrimaryKey("Content", UtilMisc.toMap("contentId", contentId));
            if (contentTypes != null && contentTypes.size() > 0) {
                contentTypeId = (String) content.get("contentTypeId");
                if (contentTypes.contains(contentTypeId)) {
                    contentList.add(content);
                }
            } else {
                contentList.add(content);
            }

        }
        if (Debug.infoOn()) Debug.logInfo("contentList:" + contentList.size() , "");
        return contentList;

    }

    public static List getAssociatedContentView(GenericValue currentContent, String linkDir, List assocTypes, List contentTypes, String fromDate, String thruDate) throws GenericEntityException {
        List contentList = new ArrayList();
        List exprListAnd = new ArrayList();

        String origContentId = (String) currentContent.get("contentId");
        String contentIdName = "contentId";
        String contentAssocViewName = "contentAssocView";
        if (linkDir != null && linkDir.equalsIgnoreCase("TO")) {
            contentIdName.concat("To");
            contentAssocViewName.concat("To");
        }
        EntityExpr expr = new EntityExpr(contentIdName, EntityOperator.EQUALS, origContentId);
        exprListAnd.add(expr);

        if (contentTypes.size() > 0) {
            List exprListOr = new ArrayList();
            Iterator it = contentTypes.iterator();
            while (it.hasNext()) {
                String contentType = (String) it.next();
                expr = new EntityExpr("contentTypeId", EntityOperator.EQUALS, contentType);
                exprListOr.add(expr);
            }
            EntityConditionList contentExprList = new EntityConditionList(exprListOr, EntityOperator.OR);
            exprListAnd.add(contentExprList);
        }
        if (assocTypes.size() > 0) {
            List exprListOr = new ArrayList();
            Iterator it = assocTypes.iterator();
            while (it.hasNext()) {
                String assocType = (String) it.next();
                expr = new EntityExpr("contentAssocTypeId", EntityOperator.EQUALS, assocType);
                exprListOr.add(expr);
            }
            EntityConditionList assocExprList = new EntityConditionList(exprListOr, EntityOperator.OR);
            exprListAnd.add(assocExprList);
        }

        if (fromDate != null) {
            Timestamp tsFrom = UtilDateTime.toTimestamp(fromDate);
            expr = new EntityExpr("fromDate", EntityOperator.GREATER_THAN_EQUAL_TO, tsFrom);
            exprListAnd.add(expr);
        }

        if (thruDate != null) {
            Timestamp tsThru = UtilDateTime.toTimestamp(thruDate);
            expr = new EntityExpr("thruDate", EntityOperator.LESS_THAN, tsThru);
            exprListAnd.add(expr);
        }
        EntityConditionList contentCondList = new EntityConditionList(exprListAnd, EntityOperator.AND);
        GenericDelegator delegator = currentContent.getDelegator();
        contentList = delegator.findByCondition(contentAssocViewName, contentCondList, null, null);
        return contentList;
    }

    public static List getAssociations(GenericValue currentContent, String linkDir, List assocTypes, String strFromDate, String strThruDate) throws GenericEntityException {
        GenericDelegator delegator = currentContent.getDelegator();
        String origContentId = (String) currentContent.get("contentId");
        Timestamp fromDate = null;
        if (strFromDate != null) {
            fromDate = UtilDateTime.toTimestamp(strFromDate);
        }
        Timestamp thruDate = null;
        if (strThruDate != null) {
            thruDate = UtilDateTime.toTimestamp(strThruDate);
        }
        List assocs = getContentAssocsWithId(delegator, origContentId, fromDate, thruDate, linkDir, assocTypes);
        //if (Debug.infoOn()) Debug.logInfo(" origContentId:" + origContentId + " linkDir:" + linkDir + " assocTypes:" + assocTypes, "");
        return assocs;
    }

    public static List getContentAssocsWithId(GenericDelegator delegator, String contentId, Timestamp fromDate, Timestamp thruDate, String direction, List assocTypes) throws GenericEntityException {
        List exprList = new ArrayList();
        EntityExpr joinExpr = null;
        EntityExpr expr = null;
        if (direction != null && direction.equalsIgnoreCase("From")) {
            joinExpr = new EntityExpr("contentIdTo", EntityOperator.EQUALS, contentId);
        } else {
            joinExpr = new EntityExpr("contentId", EntityOperator.EQUALS, contentId);
        }
        exprList.add(joinExpr);
        if (assocTypes != null && assocTypes.size() > 0) {
            List exprListOr = new ArrayList();
            Iterator it = assocTypes.iterator();
            while (it.hasNext()) {
                String assocType = (String) it.next();
                expr = new EntityExpr("contentAssocTypeId", EntityOperator.EQUALS, assocType);
                exprListOr.add(expr);
            }
            EntityConditionList assocExprList = new EntityConditionList(exprListOr, EntityOperator.OR);
            exprList.add(assocExprList);
        }
        if (fromDate != null) {
            EntityExpr fromExpr = new EntityExpr("fromDate", EntityOperator.GREATER_THAN_EQUAL_TO, fromDate);
            exprList.add(fromExpr);
        }
        if (thruDate != null) {
            List thruList = new ArrayList();
            //thruDate = UtilDateTime.getDayStart(thruDate, daysLater);

            EntityExpr thruExpr = new EntityExpr("thruDate", EntityOperator.LESS_THAN, thruDate);
            thruList.add(thruExpr);
            EntityExpr thruExpr2 = new EntityExpr("thruDate", EntityOperator.EQUALS, null);
            thruList.add(thruExpr2);
            EntityConditionList thruExprList = new EntityConditionList(thruList, EntityOperator.OR);
            exprList.add(thruExprList);
        } else if (fromDate != null) {
            List thruList = new ArrayList();

            EntityExpr thruExpr = new EntityExpr("thruDate", EntityOperator.GREATER_THAN, fromDate);
            thruList.add(thruExpr);
            EntityExpr thruExpr2 = new EntityExpr("thruDate", EntityOperator.EQUALS, null);
            thruList.add(thruExpr2);
            EntityConditionList thruExprList = new EntityConditionList(thruList, EntityOperator.OR);
            exprList.add(thruExprList);
        } else {
            EntityExpr thruExpr2 = new EntityExpr("thruDate", EntityOperator.EQUALS, null);
            exprList.add(thruExpr2);
        }
        EntityConditionList assocExprList = new EntityConditionList(exprList, EntityOperator.AND);
        //if (Debug.infoOn()) Debug.logInfo(" assocExprList:" + assocExprList , "");
        List relatedAssocs = delegator.findByCondition("ContentAssoc", assocExprList, new ArrayList(), UtilMisc.toList("-fromDate"));
        //if (Debug.infoOn()) Debug.logInfo(" relatedAssoc:" + relatedAssocs.size() , "");
        //for (int i = 0; i < relatedAssocs.size(); i++) {
            //GenericValue a = (GenericValue) relatedAssocs.get(i);
        //}
        return relatedAssocs;
    }

    public static void getContentTypeAncestry(GenericDelegator delegator, String contentTypeId, List contentTypes) throws GenericEntityException {
        contentTypes.add(contentTypeId);
        GenericValue contentTypeValue = delegator.findByPrimaryKey("ContentType", UtilMisc.toMap("contentTypeId", contentTypeId));
        if (contentTypeValue == null)
            return;
        String parentTypeId = (String) contentTypeValue.get("parentTypeId");
        if (parentTypeId != null) {
            getContentTypeAncestry(delegator, parentTypeId, contentTypes);
        }
        return;
    }

    public static void getContentAncestry(GenericDelegator delegator, String contentId, String contentAssocTypeId, String direction, List contentAncestorList) throws GenericEntityException {
        String contentIdField = null;
        String contentIdOtherField = null;
        if (direction != null && direction.equalsIgnoreCase("to")) {
            contentIdField = "contentId";
            contentIdOtherField = "contentIdTo";
        } else {
            contentIdField = "contentIdTo";
            contentIdOtherField = "contentId";
        }
        
        if (Debug.infoOn()) Debug.logInfo("getContentAncestry, contentId:" + contentId, "");
        if (Debug.infoOn()) Debug.logInfo("getContentAncestry, contentAssocTypeId:" + contentAssocTypeId, "");
        Map andMap = null;
        if (UtilValidate.isEmpty(contentAssocTypeId)) {
            andMap = UtilMisc.toMap(contentIdField, contentId);
        } else {
            andMap = UtilMisc.toMap(contentIdField, contentId, "contentAssocTypeId", contentAssocTypeId);
        }
        try {
            List lst = delegator.findByAndCache("ContentAssoc", andMap);
            //if (Debug.infoOn()) Debug.logInfo("getContentAncestry, lst:" + lst, "");
            List lst2 = EntityUtil.filterByDate(lst);
            //if (Debug.infoOn()) Debug.logInfo("getContentAncestry, lst2:" + lst2, "");
            if (lst2.size() > 0) {
                GenericValue contentAssoc = (GenericValue)lst2.get(0);
                getContentAncestry(delegator, contentAssoc.getString(contentIdOtherField), contentAssocTypeId, direction, contentAncestorList);
                contentAncestorList.add(contentAssoc.getString(contentIdOtherField));
            }
        } catch(GenericEntityException e) {
            Debug.logError(e,module); 
            return;
        }
        return;
    }
    
    public static void getEntityOwners(GenericDelegator delegator, GenericValue entity,  List contentOwnerList, String entityName, String ownerIdFieldName) throws GenericEntityException {

        String ownerContentId = entity.getString(ownerIdFieldName);
        if (UtilValidate.isNotEmpty(ownerContentId)) {
            contentOwnerList.add(ownerContentId);
            ModelEntity modelEntity = delegator.getModelEntity(entityName);
            String pkFieldName = getPkFieldName(entityName, modelEntity);
            GenericValue ownerContent = delegator.findByPrimaryKeyCache(entityName, UtilMisc.toMap(pkFieldName, ownerContentId));
            if (ownerContent != null) {
                getEntityOwners(delegator, ownerContent, contentOwnerList, entityName, ownerIdFieldName );
            }
        }
        return;
    }
    
    public static String getPkFieldName(String entityName, ModelEntity modelEntity) {
        List pkFieldNames = modelEntity.getPkFieldNames();
        String idFieldName = null;
        if (pkFieldNames.size() > 0) {
            idFieldName = (String)pkFieldNames.get(0);
        }
        return idFieldName;
    }
    
    public static int getPrivilegeEnumSeq(GenericDelegator delegator, String privilegeEnumId) throws GenericEntityException {
            int privilegeEnumSeq = -1;
            
            if ( UtilValidate.isNotEmpty(privilegeEnumId)) {
                GenericValue privEnum = delegator.findByPrimaryKeyCache("Enumeration", UtilMisc.toMap("enumId", privilegeEnumId));
                if (privEnum != null) {
                    String sequenceId = privEnum.getString("sequenceId");   
                    try {
                        privilegeEnumSeq = Integer.parseInt(sequenceId);
                    } catch(NumberFormatException e) {
                        // just leave it at -1   
                    }
                }
            }
            return privilegeEnumSeq;
    }
    
    public static List getUserRolesFromList(GenericDelegator delegator, List idList, String partyId, String entityIdFieldName, String partyIdFieldName, String roleTypeIdFieldName, String entityName) throws GenericEntityException {
        
        EntityExpr expr = new EntityExpr(entityIdFieldName, EntityOperator.IN, idList);
        EntityExpr expr2 = new EntityExpr(partyIdFieldName, EntityOperator.EQUALS, partyId);
        EntityConditionList condList = new EntityConditionList(UtilMisc.toList(expr, expr2), EntityOperator.AND);
        List roleList = delegator.findByConditionCache(entityName, condList, null, null);
        List roleListFiltered = EntityUtil.filterByDate(roleList);
        HashSet distinctSet = new HashSet();
        Iterator iter = roleListFiltered.iterator();
        while (iter.hasNext()) {
            GenericValue contentRole = (GenericValue)iter.next();
            String roleTypeId = contentRole.getString(roleTypeIdFieldName);
            distinctSet.add(roleTypeId);
        }
        List distinctList = Arrays.asList(distinctSet.toArray());
        return distinctList;
    }

    public static void getContentAncestryAll(GenericDelegator delegator, String contentId, String passedContentTypeId, String direction, List contentAncestorList) {
        String contentIdField = null;
        String contentIdOtherField = null;
        if (direction != null && direction.equalsIgnoreCase("to")) {
            contentIdField = "contentId";
            contentIdOtherField = "contentIdTo";
        } else {
            contentIdField = "contentIdTo";
            contentIdOtherField = "contentId";
        }
        
        if (Debug.infoOn()) Debug.logInfo("getContentAncestry, contentId:" + contentId, "");
        Map andMap = UtilMisc.toMap(contentIdField, contentId);
        try {
            List lst = delegator.findByAndCache("ContentAssoc", andMap);
            //if (Debug.infoOn()) Debug.logInfo("getContentAncestry, lst:" + lst, "");
            List lst2 = EntityUtil.filterByDate(lst);
            //if (Debug.infoOn()) Debug.logInfo("getContentAncestry, lst2:" + lst2, "");
            Iterator iter = lst2.iterator();
            while (iter.hasNext()) {
                GenericValue contentAssoc = (GenericValue)iter.next();
                String contentIdOther = contentAssoc.getString(contentIdOtherField);
                if (!contentAncestorList.contains(contentIdOther)) {
                    getContentAncestryAll(delegator, contentIdOther, passedContentTypeId, direction, contentAncestorList);
                    if (!contentAncestorList.contains(contentIdOther)) {
                        GenericValue contentTo = delegator.findByPrimaryKeyCache("Content", UtilMisc.toMap("contentId", contentIdOther));
                        String contentTypeId = contentTo.getString("contentTypeId");
                        if (contentTypeId != null && contentTypeId.equals(passedContentTypeId))
                            contentAncestorList.add(contentIdOther );
                    }
                }
            }
        } catch(GenericEntityException e) {
            Debug.logError(e,module); 
            return;
        }
        return;
    }

    public static List getContentAncestryNodeTrail(GenericDelegator delegator, String contentId, String contentAssocTypeId, String direction) throws GenericEntityException {

         List contentAncestorList = new ArrayList();
         List nodeTrail = new ArrayList();
         getContentAncestry(delegator, contentId, contentAssocTypeId, direction, contentAncestorList);
         Iterator contentAncestorListIter = contentAncestorList.iterator(); 
         while (contentAncestorListIter.hasNext()) {
             GenericValue value = (GenericValue) contentAncestorListIter.next();
             Map thisNode = FreeMarkerWorker.makeNode(value);
             nodeTrail.add(thisNode);
         }
         return nodeTrail;
    }

    public static String getContentAncestryNodeTrailCsv(GenericDelegator delegator, String contentId, String contentAssocTypeId, String direction) throws GenericEntityException {

         List contentAncestorList = new ArrayList();
         getContentAncestry(delegator, contentId, contentAssocTypeId, direction, contentAncestorList);
         String csv = StringUtil.join(contentAncestorList, ",");
         return csv;
    }

    public static void getContentAncestryValues(GenericDelegator delegator, String contentId, String contentAssocTypeId, String direction, List contentAncestorList) throws GenericEntityException {
        String contentIdField = null;
        String contentIdOtherField = null;
        if (direction != null && direction.equalsIgnoreCase("to")) {
            contentIdField = "contentId";
            contentIdOtherField = "contentIdTo";
        } else {
            contentIdField = "contentIdTo";
            contentIdOtherField = "contentId";
        }
        
            //if (Debug.infoOn()) Debug.logInfo("getContentAncestry, contentId:" + contentId, "");
        try {
            List lst = delegator.findByAndCache("ContentAssoc", UtilMisc.toMap(contentIdField, contentId, "contentAssocTypeId", contentAssocTypeId));
            //if (Debug.infoOn()) Debug.logInfo("getContentAncestry, lst:" + lst, "");
            List lst2 = EntityUtil.filterByDate(lst);
            //if (Debug.infoOn()) Debug.logInfo("getContentAncestry, lst2:" + lst2, "");
            if (lst2.size() > 0) {
                GenericValue contentAssoc = (GenericValue)lst2.get(0);
                getContentAncestryValues(delegator, contentAssoc.getString(contentIdOtherField), contentAssocTypeId, direction, contentAncestorList);
                GenericValue content = delegator.findByPrimaryKeyCache("Content", UtilMisc.toMap("contentId", contentAssoc.getString(contentIdOtherField)));
                
                contentAncestorList.add(content);
            }
        } catch(GenericEntityException e) {
            Debug.logError(e,module); 
            return;
        }
        return;
    }

    public static Map pullEntityValues(GenericDelegator delegator, String entityName, Map context) {
        GenericValue entOut = delegator.makeValue(entityName, null);
        entOut.setPKFields(context);
        entOut.setNonPKFields(context);
        return (Map) entOut;
    }

    /**
     * callContentPermissionCheck Formats data for a call to the checkContentPermission service.
     */
    public static String callContentPermissionCheck(GenericDelegator delegator, LocalDispatcher dispatcher, Map context) {
        Map permResults = callContentPermissionCheckResult(delegator, dispatcher, context);
        String permissionStatus = (String) permResults.get("permissionStatus");
        return permissionStatus;
    }

    public static Map callContentPermissionCheckResult(GenericDelegator delegator, LocalDispatcher dispatcher, Map context) {
        
        Map permResults = new HashMap();
        String skipPermissionCheck = (String) context.get("skipPermissionCheck");

        if (skipPermissionCheck == null
            || skipPermissionCheck.length() == 0
            || (!skipPermissionCheck.equalsIgnoreCase("true") && !skipPermissionCheck.equalsIgnoreCase("granted"))) {
            GenericValue userLogin = (GenericValue) context.get("userLogin");
            Map serviceInMap = new HashMap();
            serviceInMap.put("userLogin", userLogin);
            serviceInMap.put("targetOperationList", context.get("targetOperationList"));
            serviceInMap.put("contentPurposeList", context.get("contentPurposeList"));
            serviceInMap.put("targetOperationString", context.get("targetOperationString"));
            serviceInMap.put("contentPurposeString", context.get("contentPurposeString"));
            serviceInMap.put("entityOperation", context.get("entityOperation"));
            serviceInMap.put("currentContent", context.get("currentContent"));

            try {
                permResults = dispatcher.runSync("checkContentPermission", serviceInMap);
            } catch (GenericServiceException e) {
                Debug.logError(e, "Problem checking permissions", "ContentServices");
            }
        }
        return permResults;
    }

    public static GenericValue getSubContent(GenericDelegator delegator, String contentId, String mapKey, String subContentId, GenericValue userLogin, List assocTypes, Timestamp fromDate) throws IOException {
        //GenericValue content = null;
        GenericValue view = null;
        try {
            if (subContentId == null) {
                if (contentId == null) {
                    throw new GenericEntityException("contentId and subContentId are null.");
                }
                Map results = null;
                results = ContentServicesComplex.getAssocAndContentAndDataResourceMethod(delegator, contentId, mapKey, "From", fromDate, null, null, null, assocTypes, null);
                List entityList = (List) results.get("entityList");
                if (entityList == null || entityList.size() == 0) {
                    //throw new IOException("No subcontent found.");
                } else {
                    view = (GenericValue) entityList.get(0);
                }
            } else {
                List lst = delegator.findByAnd("ContentDataResourceView", UtilMisc.toMap("contentId", subContentId));
                if (lst == null || lst.size() == 0) {
                    throw new IOException("No subContent found for subContentId=." + subContentId);
                }
                view = (GenericValue) lst.get(0);
            }
        } catch (GenericEntityException e) {
            throw new IOException(e.getMessage());
        }
        return view;
    }

    public static GenericValue getSubContentCache(GenericDelegator delegator, String contentId, String mapKey, String subContentId, GenericValue userLogin, List assocTypes, Timestamp fromDate, Boolean nullThruDatesOnly, String contentAssocPredicateId) throws GenericEntityException {
        //GenericValue content = null;
        GenericValue view = null;
        if (UtilValidate.isEmpty(subContentId)) {
            view = getSubContentCache(delegator, contentId, mapKey, userLogin, assocTypes, fromDate, nullThruDatesOnly, contentAssocPredicateId);
        } else {
            view = getContentCache(delegator, subContentId);
        }
        return view;
    }

    public static GenericValue getSubContentCache(GenericDelegator delegator, String contentId, String mapKey,  GenericValue userLogin, List assocTypes, Timestamp fromDate, Boolean nullThruDatesOnly, String contentAssocPredicateId) throws GenericEntityException {
        //GenericValue content = null;
        GenericValue view = null;
        if (contentId == null) {
            throw new GenericEntityException("contentId and subContentId are null.");
        }
        Map results = null;
        List contentTypes = null;
        try {
            results = ContentServicesComplex.getAssocAndContentAndDataResourceCacheMethod(delegator, contentId, mapKey, "From", fromDate, null, assocTypes, contentTypes, nullThruDatesOnly, contentAssocPredicateId);
        } catch(MiniLangException e) {
            throw new RuntimeException(e.getMessage());
        }
        List entityList = (List) results.get("entityList");
        if (entityList == null || entityList.size() == 0) {
            //throw new IOException("No subcontent found.");
        } else {
            view = (GenericValue) entityList.get(0);
        }
        return view;
    }

    public static GenericValue getContentCache(GenericDelegator delegator, String contentId) throws GenericEntityException {

        GenericValue view = null;
        List lst = delegator.findByAndCache("ContentDataResourceView", UtilMisc.toMap("contentId", contentId));
            //if (Debug.infoOn()) Debug.logInfo("getContentCache, lst(2):" + lst, "");
        if (lst != null && lst.size() > 0) {
            view = (GenericValue) lst.get(0);
        }
        return view;
    }


    public static GenericValue getContentFromView(GenericValue view) {
        GenericValue content = null;
        if (view == null) {
            return content;
        }
        GenericDelegator delegator = view.getDelegator();
        content = delegator.makeValue("Content", null);
        content.setPKFields(view);
        content.setNonPKFields(view);
        String dataResourceId = null;
        try {
            dataResourceId = (String) view.get("drDataResourceId");
        } catch (Exception e) {
            dataResourceId = (String) view.get("dataResourceId");
        }
        content.set("dataResourceId", dataResourceId);
        return content;
    }

    public static Map renderSubContentAsText(GenericDelegator delegator, String contentId, Writer out, String mapKey, String subContentId, GenericValue subContentDataResourceView, 
            Map templateContext, Locale locale, String mimeTypeId, GenericValue userLogin, Timestamp fromDate) throws GeneralException, IOException {

        Map results = new HashMap();
        //GenericValue content = null;
        if (subContentDataResourceView == null) {
            subContentDataResourceView = getSubContentCache(delegator, contentId, mapKey, subContentId, userLogin, null, fromDate, new Boolean(false), null);
        }

        results.put("view", subContentDataResourceView);
        if (subContentDataResourceView == null) {
            //throw new IOException("SubContentDataResourceView is null.");
            return results;
        }

        //String dataResourceId = (String) subContentDataResourceView.get("drDataResourceId");
        contentId = (String) subContentDataResourceView.get("contentId");
        //GenericValue dataResourceContentView = null;

        if (templateContext == null) {
            templateContext = new HashMap();
        }

        renderContentAsText(delegator, contentId, out, templateContext, subContentDataResourceView, locale, mimeTypeId);

        return results;
    }

    public static String renderSubContentAsTextCache(GenericDelegator delegator, String contentId,  String mapKey,  GenericValue subContentDataResourceView, 
            Map templateRoot, Locale locale, String mimeTypeId, GenericValue userLogin, Timestamp fromDate) throws GeneralException, IOException {
        Boolean nullThruDatesOnly = new Boolean(false);
        Writer outWriter = new StringWriter();
        Map map = renderSubContentAsTextCache(delegator, contentId, outWriter, mapKey, subContentDataResourceView, templateRoot, locale, mimeTypeId, userLogin, fromDate, nullThruDatesOnly);
        return outWriter.toString();
    }

    public static Map renderSubContentAsTextCache(GenericDelegator delegator, String contentId, Writer out, String mapKey,  GenericValue subContentDataResourceView, 
            Map templateRoot, Locale locale, String mimeTypeId, GenericValue userLogin, Timestamp fromDate) throws GeneralException, IOException {
        Boolean nullThruDatesOnly = new Boolean(false);
        return renderSubContentAsTextCache(delegator, contentId, out, mapKey, subContentDataResourceView, 
            templateRoot, locale, mimeTypeId, userLogin, fromDate, nullThruDatesOnly);
    }

    /** 
     */
    public static Map renderSubContentAsTextCache(GenericDelegator delegator, String contentId, Writer out, String mapKey,  GenericValue subContentDataResourceView, 
            Map templateRoot, Locale locale, String mimeTypeId, GenericValue userLogin, Timestamp fromDate, Boolean nullThruDatesOnly) throws GeneralException, IOException {

        Map results = new HashMap();
        //GenericValue content = null;
        if (subContentDataResourceView == null) {
            String contentAssocPredicateId = null;
            try {
                subContentDataResourceView = getSubContentCache(delegator, contentId, mapKey, userLogin, null, fromDate, nullThruDatesOnly, contentAssocPredicateId );
            } catch(GenericEntityException e) {
                throw new GeneralException(e.getMessage());
            }
        }
        results.put("view", subContentDataResourceView);
        if (subContentDataResourceView == null) {
            //throw new IOException("SubContentDataResourceView is null.");
            return results;
        }

        String contentIdSub = (String) subContentDataResourceView.get("contentId");

        if (templateRoot == null) {
            templateRoot = new HashMap();
        }

        templateRoot.put("contentId", contentIdSub);
        templateRoot.put("subContentId", null);

        renderContentAsTextCache(delegator, contentIdSub, out, templateRoot, subContentDataResourceView, locale, mimeTypeId);

        return results;
    }

    public static Map renderContentAsText(GenericDelegator delegator, String contentId, Writer out, Map templateContext, GenericValue view, Locale locale, String mimeTypeId) throws GeneralException, IOException {
        //Map context = (Map) FreeMarkerWorker.get(templateContext, "context");
        Map results = new HashMap();
        GenericValue content = null;

        if (view == null) {
            if (contentId == null) {
                throw new IOException("ContentId is null");
            }
            try {
                List lst = delegator.findByAnd("SubContentDataResourceView", UtilMisc.toMap("contentId", contentId));
                if (lst != null && lst.size() > 0) {
                    view = (GenericValue) lst.get(0);
                } else {
                    throw new IOException("SubContentDataResourceView not found in renderSubContentAsText" + " for contentId=" + contentId);
                }
            } catch (GenericEntityException e) {
                throw new IOException(e.getMessage());
            }
        }
        if (view != null) {
            Map contentMap = new HashMap();
            try {
                SimpleMapProcessor.runSimpleMapProcessor("org/ofbiz/content/ContentManagementMapProcessors.xml", "contentIn", view, contentMap, new ArrayList(), locale);
            } catch (MiniLangException e) {
                throw new IOException(e.getMessage());
            }
            content = delegator.makeValue("Content", contentMap);
        }

        results.put("view", view);
        results.put("content", content);

        if (locale != null) {
            String targetLocaleString = locale.toString();
            String thisLocaleString = (String) view.get("localeString");
            thisLocaleString = (thisLocaleString != null) ? thisLocaleString : "";
            if (targetLocaleString != null && !targetLocaleString.equalsIgnoreCase(thisLocaleString)) {
                view = findAlternateLocaleContent(delegator, view, locale);
            }
        }

        //String contentTypeId = (String) view.get("contentTypeId");
        String dataResourceId = null;
        try {
            dataResourceId = (String) view.get("drDataResourceId");
        } catch (Exception e) {
            dataResourceId = (String) view.get("dataResourceId");
        }
        if (templateContext == null) {
            templateContext = new HashMap();
        }

        // TODO: what should we REALLY do here? looks like there is no decision between Java and Service style error handling...
        //try {
        if (UtilValidate.isNotEmpty(dataResourceId) || view != null)
            DataResourceWorker.renderDataResourceAsText(delegator, dataResourceId, out, templateContext, view, locale, mimeTypeId);
        //} catch (IOException e) {
        //    return ServiceUtil.returnError(e.getMessage());
        //}

        return results;
    }

    public static String renderContentAsTextCache(GenericDelegator delegator, String contentId,  Map templateContext, GenericValue view, Locale locale, String mimeTypeId) throws GeneralException, IOException {
        Writer outWriter = new StringWriter();
        renderContentAsTextCache(delegator, contentId, outWriter, templateContext, view, locale, mimeTypeId);
        return outWriter.toString();
    }

    public static Map renderContentAsTextCache(GenericDelegator delegator, String contentId, Writer out, Map templateContext, GenericValue view, Locale locale, String mimeTypeId) throws GeneralException, IOException {

        Map results = new HashMap();
 
        GenericValue content = null;

        if (view == null) {
            if (contentId == null) {
                throw new GeneralException("ContentId is null");
            }
            try {
                List lst = delegator.findByAndCache("SubContentDataResourceView", UtilMisc.toMap("contentId", contentId), null);
                if (lst != null && lst.size() > 0) {
                    view = (GenericValue) lst.get(0);
                } else {
                    throw new GeneralException("SubContentDataResourceView not found in renderSubContentAsText" + " for contentId=" + contentId);
                }
            } catch (GenericEntityException e) {
                throw new GeneralException(e.getMessage());
            }
        }
        if (view != null) {
            Map contentMap = new HashMap();
            try {
                SimpleMapProcessor.runSimpleMapProcessor("org/ofbiz/content/ContentManagementMapProcessors.xml", "contentIn", view, contentMap, new ArrayList(), locale);
            } catch (MiniLangException e) {
                throw new IOException(e.getMessage());
            }
            content = delegator.makeValue("Content", contentMap);
        }

        results.put("view", view);
        results.put("content", content);

        if (locale != null) {
            String targetLocaleString = locale.toString();
            String thisLocaleString = (String) view.get("localeString");
            thisLocaleString = (thisLocaleString != null) ? thisLocaleString : "";
            //if (Debug.infoOn()) Debug.logInfo("renderContentAsTextCache, thisLocaleString(2):" + thisLocaleString, "");
            //if (Debug.infoOn()) Debug.logInfo("renderContentAsTextCache, targetLocaleString(2):" + targetLocaleString, "");
            if (UtilValidate.isNotEmpty(targetLocaleString) && !targetLocaleString.equalsIgnoreCase(thisLocaleString)) {
                GenericValue localeView = findAlternateLocaleContent(delegator, view, locale);
                if (localeView != null)
                    view = localeView;
            }
        }

        String templateDataResourceId = (String)view.get("templateDataResourceId");

        //String contentTypeId = (String) view.get("contentTypeId");
        String dataResourceId = null;
        try {
            dataResourceId = (String) view.get("drDataResourceId");
        } catch (Exception e) {
            dataResourceId = (String) view.get("dataResourceId");
            view = null; // renderDataResourceAsText will expect DataResource values if not null
        }

        if (templateContext == null) {
            templateContext = new HashMap();
        }

        // TODO: what should we REALLY do here? looks like there is no decision between Java and Service style error handling...

        if (UtilValidate.isEmpty(templateDataResourceId)) {
            if (UtilValidate.isNotEmpty(dataResourceId) || view != null)
                DataResourceWorker.renderDataResourceAsTextCache(delegator, dataResourceId, out, templateContext, view, locale, mimeTypeId);

        } else {
            if (UtilValidate.isNotEmpty(dataResourceId) || view != null) {
                StringWriter sw = new StringWriter();
                DataResourceWorker.renderDataResourceAsTextCache(delegator, dataResourceId, sw, templateContext, view, locale, mimeTypeId);
                String s = sw.toString();
                if (UtilValidate.isNotEmpty(s))
                    s = s.trim();
                //if (Debug.infoOn()) Debug.logInfo("renderTextAsStringCache, s:" + s, "");
                
                if (Debug.infoOn()) Debug.logInfo("renderContentAsTextCache, dataResourceId(2):" + dataResourceId, "");
                //if (Debug.infoOn()) Debug.logInfo("renderTextAsStringCache, view(3):" + view, "");
                String reqdType = null;
                try {
                    reqdType = DataResourceWorker.getDataResourceMimeType(delegator, dataResourceId, view);
                } catch(GenericEntityException e) {
                    throw new GeneralException(e.getMessage());
                }
                if (Debug.infoOn()) Debug.logInfo("renderContentAsTextCache, reqdType(2):" + reqdType, "");
                if (UtilValidate.isNotEmpty(reqdType)) {
                    if (reqdType.toLowerCase().indexOf("xml") >= 0) {
                        StringReader sr = new StringReader(s);
                        try {
                            NodeModel nodeModel = NodeModel.parse(new InputSource(sr));
                            if (Debug.infoOn()) Debug.logInfo("renderTextAsStringCache, doc:" + nodeModel, "");
                            templateContext.put("doc", nodeModel);
                        } catch(SAXException e) {
                            throw new GeneralException(e.getMessage());
                        } catch(ParserConfigurationException e2) {
                            throw new GeneralException(e2.getMessage());
                        }
                    } else {
                        // must be text
                        templateContext.put("textData", sw.toString());
                    }
                } else {
                    templateContext.put("textData", sw.toString());
                }
            }
            DataResourceWorker.renderDataResourceAsTextCache(delegator, templateDataResourceId, out, templateContext, null, locale, mimeTypeId);
        }
        return results;
    }

    public static Map buildPickContext(GenericDelegator delegator, String contentAssocTypeId, String assocContentId, String direction, GenericValue thisContent) throws GenericEntityException {

        Map ctx = new HashMap();
        ctx.put("contentAssocTypeId", contentAssocTypeId);
        ctx.put("contentId", assocContentId);
        String assocRelation = null;
        // This needs to be the opposite
        if (direction != null && direction.equalsIgnoreCase("From")) {
            ctx.put("contentIdFrom", assocContentId);
            assocRelation = "FromContent";
        } else {
            ctx.put("contentIdTo", assocContentId);
            assocRelation = "ToContent";
        }
        if (thisContent == null)
            thisContent = delegator.findByPrimaryKeyCache("Content",
                                   UtilMisc.toMap("contentId", assocContentId));
        ctx.put("content", thisContent);
        List purposes = getPurposes(thisContent);
        ctx.put("purposes", purposes);
        List contentTypeAncestry = new ArrayList();
        String contentTypeId = (String)thisContent.get("contentTypeId");
        getContentTypeAncestry(delegator, contentTypeId, contentTypeAncestry);
        ctx.put("typeAncestry", contentTypeAncestry);
        List sections = getSections(thisContent);
        ctx.put("sections", sections);
        List topics = getTopics(thisContent);
        ctx.put("topics", topics);
        //Debug.logInfo("buildPickContext, ctx:" + ctx, "");
        return ctx;
    }

    public static void checkConditions(GenericDelegator delegator, Map trailNode, Map contentAssoc, Map whenMap) {

        Map context = new HashMap();
        GenericValue content = (GenericValue)trailNode.get("value");
        String contentId = (String)trailNode.get("contentId");
        if (contentAssoc == null && content != null && (content.getEntityName().indexOf("Assoc") >= 0)) {
            contentAssoc = delegator.makeValue("ContentAssoc", null);
            try {
                // TODO: locale needs to be gotten correctly
                SimpleMapProcessor.runSimpleMapProcessor("org/ofbiz/content/ContentManagementMapProcessors.xml", "contentAssocIn", content, contentAssoc, new ArrayList(), Locale.getDefault());
                context.put("contentAssocTypeId", contentAssoc.get("contentAssocTypeId"));
                context.put("contentAssocPredicateId", contentAssoc.get("contentAssocPredicateId"));
                context.put("mapKey", contentAssoc.get("mapKey"));
            } catch (MiniLangException e) {
                Debug.logError(e.getMessage(), module);
                //throw new GeneralException(e.getMessage());
            }
        } else {
                context.put("contentAssocTypeId", null);
                context.put("contentAssocPredicateId", null);
                context.put("mapKey", null);
        }
        context.put("content", content);
        List purposes = getPurposes(content);
        context.put("purposes", purposes);
        List sections = getSections(content);
        context.put("sections", sections);
        List topics = getTopics(content);
        context.put("topics", topics);
        String contentTypeId = (String)content.get("contentTypeId");
        List contentTypeAncestry = new ArrayList();
        try {
            getContentTypeAncestry(delegator, contentTypeId, contentTypeAncestry);
        } catch(GenericEntityException e) {
        }
        context.put("typeAncestry", contentTypeAncestry);
        boolean isReturnBefore = checkReturnWhen(context, (String)whenMap.get("returnBeforePickWhen"));
        trailNode.put("isReturnBefore", new Boolean(isReturnBefore));
        boolean isPick = checkWhen(context, (String)whenMap.get("pickWhen"));
        trailNode.put("isPick", new Boolean(isPick));
        boolean isFollow = checkWhen(context, (String)whenMap.get("followWhen"));
        trailNode.put("isFollow", new Boolean(isFollow));
        boolean isReturnAfter = checkReturnWhen(context, (String)whenMap.get("returnAfterPickWhen"));
        trailNode.put("isReturnAfter", new Boolean(isReturnAfter));
        trailNode.put("checked", new Boolean(true));

        return;
    }

    public static boolean booleanDataType( Object boolObj ) {
    
        boolean bool = false;
        if (boolObj != null && ((Boolean)boolObj).booleanValue()) {
            bool = true;
        }
        return bool;
    }
        

    public static List prepTargetOperationList(Map context, String md) {

        List targetOperationList = (List)context.get("targetOperationList");
        String targetOperationString = (String)context.get("targetOperationString");
        if (Debug.infoOn()) Debug.logInfo("in prepTargetOperationList, targetOperationString(0):" + targetOperationString, "");
        if (UtilValidate.isNotEmpty(targetOperationString) ) {
            List opsFromString = StringUtil.split(targetOperationString, "|");
            if (targetOperationList == null || targetOperationList.size() == 0) {
                targetOperationList = new ArrayList();
            }
            targetOperationList.addAll(opsFromString);
        }
        if (targetOperationList == null || targetOperationList.size() == 0) {
            targetOperationList = new ArrayList();
            if (UtilValidate.isEmpty(md))
                md ="_CREATE";
            targetOperationList.add("CONTENT" + md);
        }
        if (Debug.infoOn()) Debug.logInfo("in prepTargetOperationList, targetOperationList(0):" + targetOperationList, "");
        return targetOperationList;
    }

    public static List prepContentPurposeList(Map context) {

        List contentPurposeList = (List)context.get("contentPurposeList");
        String contentPurposeString = (String)context.get("contentPurposeString");
        if (Debug.infoOn()) Debug.logInfo("in prepContentPurposeList, contentPurposeString(0):" + contentPurposeString, "");
        if (UtilValidate.isNotEmpty(contentPurposeString) ) {
            List purposesFromString = StringUtil.split(contentPurposeString, "|");
            if (contentPurposeList == null || contentPurposeList.size() == 0) {
                contentPurposeList = new ArrayList();
            }
            contentPurposeList.addAll(purposesFromString);
        }
        if (contentPurposeList == null || contentPurposeList.size() == 0) {
            contentPurposeList = new ArrayList();
        }
        if (Debug.infoOn()) Debug.logInfo("in prepContentPurposeList, contentPurposeList(0):" + contentPurposeList, "");
        return contentPurposeList;
    }

    public static String prepPermissionErrorMsg(Map permResults) {

        String errorMessage = "Permission is denied."; 
        errorMessage += ServiceUtil.getErrorMessage(permResults);
        PermissionRecorder recorder = (PermissionRecorder)permResults.get("permissionRecorder");
            Debug.logInfo("recorder(0):" + recorder, "");
        if (recorder != null && recorder.isOn()) {
            String permissionMessage = recorder.toHtml();
            //Debug.logInfo("permissionMessage(0):" + permissionMessage, "");
            errorMessage += " \n " + permissionMessage;
        }
        return errorMessage;
    }

    public static List getContentAssocViewList(GenericDelegator delegator, String contentIdTo, String contentId, String contentAssocTypeId, String statusId, String privilegeEnumId) throws GenericEntityException {

        List exprListAnd = new ArrayList();

        if (UtilValidate.isNotEmpty(contentIdTo)) {
            EntityExpr expr = new EntityExpr("caContentIdTo", EntityOperator.EQUALS, contentIdTo);
            exprListAnd.add(expr);
        }

        if (UtilValidate.isNotEmpty(contentId)) {
            EntityExpr expr = new EntityExpr("contentId", EntityOperator.EQUALS, contentId);
            exprListAnd.add(expr);
        }

        if (UtilValidate.isNotEmpty(contentAssocTypeId)) {
            EntityExpr expr = new EntityExpr("caContentAssocTypeId", EntityOperator.EQUALS, contentAssocTypeId);
            exprListAnd.add(expr);
        }

        if (UtilValidate.isNotEmpty(statusId)) {
            EntityExpr expr = new EntityExpr("statusId", EntityOperator.EQUALS, statusId);
            exprListAnd.add(expr);
        }

        if (UtilValidate.isNotEmpty(privilegeEnumId)) {
            EntityExpr expr = new EntityExpr("privilegeEnumId", EntityOperator.EQUALS, privilegeEnumId);
            exprListAnd.add(expr);
        }

        EntityConditionList contentCondList = new EntityConditionList(exprListAnd, EntityOperator.AND);
        List contentList = delegator.findByCondition("ContentAssocDataResourceViewFrom", contentCondList, null, null);
        List filteredList = EntityUtil.filterByDate(contentList, UtilDateTime.nowTimestamp(), "caFromDate", "caThruDate", true);
        return filteredList;
    }

    public static GenericValue getContentAssocViewFrom(GenericDelegator delegator, String contentIdTo, String contentId, String contentAssocTypeId, String statusId, String privilegeEnumId) throws GenericEntityException {

        List filteredList = getContentAssocViewList(delegator, contentIdTo, contentId, contentAssocTypeId, statusId, privilegeEnumId);

        GenericValue val = null;
        if (filteredList.size() > 0 ) {
            val = (GenericValue)filteredList.get(0);
        }
        return val;
    }
}
