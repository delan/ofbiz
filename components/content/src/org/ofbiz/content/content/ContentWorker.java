/*
 * $Id: ContentWorker.java,v 1.16 2004/01/11 06:27:04 byersa Exp $
 *
 *  Copyright (c) 2001, 2002 The Open For Business Project - www.ofbiz.org
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
import java.io.Writer;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.ofbiz.base.util.BshUtil;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.FlexibleStringExpander;
import org.ofbiz.base.util.GeneralException;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.content.data.DataResourceWorker;
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
import org.ofbiz.content.webapp.ftl.FreeMarkerWorker;

import bsh.EvalError;

/**
 * ContentWorker Class
 * 
 * @author <a href="mailto:byersa@automationgroups.com">Al Byers</a>
 * @version $Revision: 1.16 $
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
        //if (Debug.verboseOn()) Debug.logVerbose("contentId(traverse - 0):" + content.get("contentId") + " depth:" + depthIdx,null);
        //if (Debug.verboseOn()) Debug.logVerbose("masterNode(traverse -0):" + masterNode,null);
        //if (Debug.verboseOn()) Debug.logVerbose("traverse, fromDate:" + fromDate,null);
        //if (Debug.verboseOn()) Debug.logVerbose("traverse, thruDate:" + thruDate,null);
        
        //String startContentAssocTypeId = null;
        String contentTypeId = null;
        String contentId = null;
        try {
            if (contentAssocTypeId == null) {
                contentAssocTypeId = "";
            }
            contentId = (String) content.get("contentId");
            contentTypeId = (String) content.get("contentTypeId");
            //if (Debug.verboseOn()) Debug.logVerbose("contentTypeId(traverse):" + contentTypeId,null);
            List topicList = content.getRelatedByAnd("ToContentAssoc", UtilMisc.toMap("contentAssocTypeId", "TOPIC"));
            List topics = new ArrayList();
            for (int i = 0; i < topicList.size(); i++) {
                GenericValue assoc = (GenericValue) topicList.get(i);
                topics.add(assoc.get("contentId"));
            }
            //if (Debug.verboseOn()) Debug.logVerbose("topics(traverse):" + topics,null);
            List keywordList = content.getRelatedByAnd("ToContentAssoc", UtilMisc.toMap("contentAssocTypeId", "KEYWORD"));
            List keywords = new ArrayList();
            for (int i = 0; i < keywordList.size(); i++) {
                GenericValue assoc = (GenericValue) keywordList.get(i);
                keywords.add(assoc.get("contentId"));
            }
            //if (Debug.verboseOn()) Debug.logVerbose("keywords(traverse):" + keywords,null);
            List purposeValueList = content.getRelatedCache("ContentPurpose");
            List purposes = new ArrayList();
            for (int i = 0; i < purposeValueList.size(); i++) {
                GenericValue purposeValue = (GenericValue) purposeValueList.get(i);
                purposes.add(purposeValue.get("contentPurposeTypeId"));
            }
            //if (Debug.verboseOn()) Debug.logVerbose("purposes(traverse):" + purposes,null);
            List contentTypeAncestry = new ArrayList();
            getContentTypeAncestry(delegator, contentTypeId, contentTypeAncestry);

            Map context = new HashMap();
            context.put("content", content);
            context.put("contentAssocTypeId", contentAssocTypeId);
            //if (Debug.verboseOn()) Debug.logVerbose("contentAssocTypeId(traverse):" + contentAssocTypeId,null);
            //context.put("related", related);
            context.put("purposes", purposes);
            context.put("topics", topics);
            context.put("keywords", keywords);
            context.put("typeAncestry", contentTypeAncestry);
            //if (Debug.verboseOn()) Debug.logVerbose("context(traverse):" + context,null);
            boolean isPick = checkWhen(context, (String) whenMap.get("pickWhen"));
            //if (Debug.verboseOn()) Debug.logVerbose("isPick(traverse):" + isPick,null);
            boolean isReturnBefore = checkReturnWhen(context, (String) whenMap.get("returnBeforePickWhen"));
            //if (Debug.verboseOn()) Debug.logVerbose("isReturnBefore:" + isReturnBefore,null);
            Map thisNode = null;
            if (isPick || !isReturnBefore) {
                //if (Debug.verboseOn()) Debug.logVerbose("masterNode(traverse -1):" + masterNode,null);
                thisNode = new HashMap();
                thisNode.put("contentId", contentId);
                thisNode.put("contentTypeId", contentTypeId);
                thisNode.put("contentAssocTypeId", contentAssocTypeId);
                //if (Debug.verboseOn()) Debug.logVerbose("thisNode(traverse):" + thisNode,null);
                List kids = (List) masterNode.get("kids");
                if (kids == null) {
                    kids = new ArrayList();
                    masterNode.put("kids", kids);
                }
                kids.add(thisNode);
                //if (Debug.verboseOn()) Debug.logVerbose("masterNode(traverse -2):" + masterNode,null);
            }
            if (isPick) {
                pickList.add(content);
                thisNode.put("value", content);
                //if (Debug.verboseOn()) Debug.logVerbose("thisNode2(traverse):" + thisNode,null);
                //if (Debug.verboseOn()) Debug.logVerbose("masterNode(traverse -3):" + masterNode,null);
            }
            boolean isReturnAfter = checkReturnWhen(context, (String) whenMap.get("returnAfterPickWhen"));
            //if (Debug.verboseOn()) Debug.logVerbose("isReturnAfter:" + isReturnAfter,null);
            if (!isReturnAfter) {

                //if (Debug.verboseOn()) Debug.logVerbose("traverse, getContentAssocs, contentId:" + contentId,null);
                List relatedAssocs = getContentAssocsWithId(delegator, contentId, fromDate, thruDate, direction, new ArrayList());
                //if (Debug.verboseOn()) Debug.logVerbose("traverse, relatedAssocs:" + relatedAssocs,null);
                Iterator it = relatedAssocs.iterator();
                Map assocContext = new HashMap();
                assocContext.put("related", relatedAssocs);
                while (it.hasNext()) {
                    GenericValue assocValue = (GenericValue) it.next();
                    if (Debug.verboseOn()) Debug.logVerbose("assocValue, Id:" + assocValue.get("contentId") + " To:" + assocValue.get("contentIdTo") + " AssocTypeId:" + assocValue.get("contentAssocTypeId"), null);
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

                    //if (Debug.verboseOn()) Debug.logVerbose("assocContext(traverse - 2):" + assocContext,null);
                    boolean isFollow = checkWhen(assocContext, (String) whenMap.get("followWhen"));
                    //if (Debug.verboseOn()) Debug.logVerbose("isFollow:" + isFollow,null);
                    //if (Debug.verboseOn()) Debug.logVerbose("assocRelation:" + assocRelation,null);
                    //if (Debug.verboseOn()) Debug.logVerbose("relatedDirection:" + relatedDirection,null);
                    if (isFollow) {
                        GenericValue thisContent = assocValue.getRelatedOne(assocRelation);
                        //if (Debug.verboseOn()) Debug.logVerbose("thisContent, id:" + thisContent.get("contentId"),null);
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
        if (Debug.verboseOn()) Debug.logVerbose("sz(traverseSubContent):" + sz,null);
        if (sz == 0) 
            return false;

        Map currentNode = (Map)nodeTrail.get(sz - 1);
        List kids = (List)currentNode.get("kids");
        //if (Debug.verboseOn()) Debug.logVerbose("currentNode(traverseSubContent):" + currentNode,null);
        if (kids != null && kids.size() > 0) {
            int idx = 0;
            while (idx < kids.size()) {
                currentNode = (Map)kids.get(idx);
                Boolean isFollow = (Boolean)currentNode.get("isFollow");
                if (isFollow == null || !isFollow.booleanValue()) {
                    idx++;
                    continue;
                }
                nodeTrail.add(currentNode);
                FreeMarkerWorker.traceNodeTrail("12",nodeTrail);
                Boolean isPick = (Boolean)currentNode.get("isPick");
                inProgress = true;
                selectKids(currentNode, ctx);
                FreeMarkerWorker.traceNodeTrail("14",nodeTrail);
                break;
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
                if (Debug.verboseOn()) Debug.logVerbose("idx(traverseSubContent):" + idx,null);
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

    public static void selectKids(Map currentNode, Map ctx) {
        
        GenericValue parentContent = (GenericValue)currentNode.get("value");
        String parentContentAssocTypeId = (String)currentNode.get("assocContentTypeId");
        String parentContentTypeId = (String)currentNode.get("contentTypeId");
        String parentContentId = (String) parentContent.get("contentId");
        if (Debug.verboseOn()) Debug.logVerbose("traverse, parentContentId:" + parentContentId,null);
        if (Debug.verboseOn()) Debug.logVerbose("traverse, parentContentAssocTypeId:" + parentContentAssocTypeId,null);
        Map whenMap = (Map)ctx.get("whenMap");
        Map context = new HashMap();
        context.put("content", parentContent);
        context.put("contentAssocTypeId", parentContentAssocTypeId);
        List purposes = getPurposes(parentContent);
        context.put("purposes", purposes);
        List contentTypeAncestry = new ArrayList();
        GenericDelegator delegator = (GenericDelegator)ctx.get("delegator");
        try {
            getContentTypeAncestry(delegator, parentContentTypeId, contentTypeAncestry);
        } catch(GenericEntityException e) {
            if (Debug.verboseOn()) Debug.logVerbose("Error in getting contentTypeAncestry:" + e.getMessage(), "");
        }
        context.put("typeAncestry", contentTypeAncestry);
        boolean isReturnAfter = checkReturnWhen(context, (String)whenMap.get("returnAfterPickWhen"));
        if (isReturnAfter) 
            return;

        List kids = new ArrayList();
        currentNode.put("kids", kids);
        try {
            String direction = (String)ctx.get("direction");
            if (UtilValidate.isEmpty(direction))
                direction = "From";
            Timestamp fromDate = (Timestamp)ctx.get("fromDate");
            Timestamp thruDate = (Timestamp)ctx.get("thruDate");
            
            List relatedAssocs = getContentAssocsWithId(delegator, parentContentId, fromDate, thruDate, direction, new ArrayList());
            if (Debug.verboseOn()) Debug.logVerbose("traverse, relatedAssocs:" + relatedAssocs,null);
            Iterator it = relatedAssocs.iterator();
            while (it.hasNext()) {
                GenericValue assocValue = (GenericValue) it.next();
                if (Debug.verboseOn()) Debug.logVerbose("assocValue, Id:" + assocValue.get("contentId") + " To:" + assocValue.get("contentIdTo") + " AssocTypeId:" + assocValue.get("contentAssocTypeId"), null);
                String contentAssocTypeId = (String) assocValue.get("contentAssocTypeId");
                String assocContentId = null;
              
                if (direction != null && direction.equalsIgnoreCase("From")) {
                    assocContentId = (String) assocValue.get("contentId");
                } else {
                    assocContentId = (String) assocValue.get("contentIdTo");
                }
                Map assocContext = buildPickContext(delegator, contentAssocTypeId, assocContentId, direction, null);
                assocContext.put("related", relatedAssocs);
                boolean isReturnBefore = checkReturnWhen(assocContext, (String)whenMap.get("returnBeforePickWhen"));
                if (Debug.verboseOn()) Debug.logVerbose("isReturnBefore(traverse):" + isReturnBefore,null);
                if (!isReturnBefore) {
                    Map thisNode = new HashMap();
                    GenericValue thisContent = (GenericValue)assocContext.get("content");
                    thisNode.put("value", thisContent);
                    thisNode.put("contentId", thisContent.get("contentId"));
                    thisNode.put("contentTypeId", thisContent.get("contentTypeId"));
                    thisNode.put("contentAssocTypeId", contentAssocTypeId);
                    thisNode.put("direction", direction);
                    if (Debug.verboseOn()) Debug.logVerbose("thisNode(traverse):" + thisNode,null);
                    kids = (List) currentNode.get("kids");
                    if (kids == null) {
                        kids = new ArrayList();
                        currentNode.put("kids", kids);
                    }
                    kids.add(thisNode);
                    String pickWhen = (String)whenMap.get("pickWhen");
                    if (Debug.verboseOn()) Debug.logVerbose("pickWhen(traverse):" + pickWhen,null);
                    if (Debug.verboseOn()) Debug.logVerbose("assocContext(traverse):" + assocContext,null);
                    boolean isPick = checkWhen(assocContext, (String)whenMap.get("pickWhen"));
                    if (Debug.verboseOn()) Debug.logVerbose("isPick(selectKids):" + isPick,null);
                    if (isPick) {
                        thisNode.put("isPick", new Boolean(true));
                        Integer count = (Integer) currentNode.get("count");
                        if (count == null) {
                            count = new Integer(1);
                        } else {
                            count = new Integer(count.intValue() + 1);
                        }
                        currentNode.put("count", count);
                    }
                    String followWhen = (String)whenMap.get("followWhen");
                    if (Debug.verboseOn()) Debug.logVerbose("followWhen(traverse):" + followWhen,null);
                    boolean isFollow = checkWhen(assocContext, (String)whenMap.get("followWhen"));
                    if (Debug.verboseOn()) Debug.logVerbose("isFollow(traverse):" + isFollow,null);
                    if (isFollow) {
                        thisNode.put("isFollow", new Boolean(true));
                    }
                }
            }
        } catch (GenericEntityException e) {
            Debug.logError("Entity Error:" + e.getMessage(), null);
        }
        return;
    }

    public static boolean checkWhen(Map context, String whenStr) {
        //if (Debug.verboseOn()) Debug.logVerbose("whenStr:" + whenStr,null);
        boolean isWhen = true; //opposite default from checkReturnWhen
        if (whenStr != null && whenStr.length() > 0) {
            FlexibleStringExpander fse = new FlexibleStringExpander(whenStr);
            String newWhen = fse.expandString(context);
            try {
                Boolean isWhenObj = (Boolean) BshUtil.eval(newWhen, context);
                isWhen = isWhenObj.booleanValue();
            } catch (EvalError e) {
                Debug.logError("Error in evaluating :" + whenStr + " : " + e.getMessage(), null);
            }
        }
        return isWhen;
    }

    public static boolean checkReturnWhen(Map context, String whenStr) {
        //if (Debug.verboseOn()) Debug.logVerbose("checkReturnWhen:" + whenStr,null);
        boolean isWhen = false; //opposite default from checkWhen
        if (whenStr != null && whenStr.length() > 0) {
            FlexibleStringExpander fse = new FlexibleStringExpander(whenStr);
            String newWhen = fse.expandString(context);
            try {
                Boolean isWhenObj = (Boolean) BshUtil.eval(newWhen, context);
                isWhen = isWhenObj.booleanValue();
            } catch (EvalError e) {
                Debug.logError("Error in evaluating :" + whenStr + " : " + e.getMessage(), null);
            }
        }
        return isWhen;
    }

    public static List getAssociatedContent(GenericValue currentContent, String linkDir, List assocTypes, List contentTypes, String fromDate, String thruDate)
        throws GenericEntityException {

        GenericDelegator delegator = currentContent.getDelegator();
        List assocList = getAssociations(currentContent, linkDir, assocTypes, fromDate, thruDate);

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
        return assocs;
    }

    public static List getContentAssocsWithId(GenericDelegator delegator, String contentId, Timestamp fromDate, Timestamp thruDate, String direction, List assocTypes) throws GenericEntityException {
        //if (Debug.verboseOn()) Debug.logVerbose("getContentAssocs, direction:" + direction,null);
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
        }
        EntityConditionList assocExprList = new EntityConditionList(exprList, EntityOperator.AND);
        List relatedAssocs = delegator.findByCondition("ContentAssoc", assocExprList, new ArrayList(), new ArrayList());
        //if (Debug.verboseOn()) Debug.logVerbose("relatedAssocs:", null);
        for (int i = 0; i < relatedAssocs.size(); i++) {
            GenericValue a = (GenericValue) relatedAssocs.get(i);
            if (Debug.verboseOn()) Debug.logVerbose(" contentId:" + a.get("contentId") + " To:" + a.get("contentIdTo") + " AssocTypeId:" + a.get("contentAssocTypeId"), null);

        }
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
        String permissionStatus = "granted";
        String skipPermissionCheck = (String) context.get("skipPermissionCheck");

        if (skipPermissionCheck == null
            || skipPermissionCheck.length() == 0
            || (!skipPermissionCheck.equalsIgnoreCase("true") && !skipPermissionCheck.equalsIgnoreCase("granted"))) {
            GenericValue userLogin = (GenericValue) context.get("userLogin");
            Map serviceInMap = new HashMap();
            serviceInMap.put("userLogin", userLogin);
            serviceInMap.put("targetOperationList", context.get("targetOperationList"));
            serviceInMap.put("contentPurposeList", context.get("contentPurposeList"));
            serviceInMap.put("entityOperation", context.get("entityOperation"));

            try {
                Map permResults = dispatcher.runSync("checkContentPermission", serviceInMap);
                permissionStatus = (String) permResults.get("permissionStatus");
            } catch (GenericServiceException e) {
                Debug.logError(e, "Problem checking permissions", "ContentServices");
            }
        }
        return permissionStatus;
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

    public static GenericValue getSubContentCache(GenericDelegator delegator, String contentId, String mapKey, String subContentId, GenericValue userLogin, List assocTypes, Timestamp fromDate) throws GenericEntityException, MiniLangException, GeneralException {
        //GenericValue content = null;
        GenericValue view = null;
        if (subContentId == null) {
            if (contentId == null) {
                throw new GenericEntityException("contentId and subContentId are null.");
            }
            Map results = null;
            List contentTypes = null;
            results = ContentServicesComplex.getAssocAndContentAndDataResourceCacheMethod(delegator, contentId, mapKey, "From", fromDate, null, assocTypes, contentTypes);
            List entityList = (List) results.get("entityList");
            if (entityList == null || entityList.size() == 0) {
                //throw new IOException("No subcontent found.");
            } else {
                view = (GenericValue) entityList.get(0);
            }
        } else {
            List lst = delegator.findByAnd("ContentDataResourceView", UtilMisc.toMap("contentId", subContentId));
            if (lst == null || lst.size() == 0) {
                throw new GeneralException("No subContent found for subContentId=." + subContentId);
            }
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

        //Map context = (Map) FreeMarkerWorker.get(templateContext, "context");
        //if (Debug.verboseOn()) Debug.logVerbose(" in renderSubContentAsText, mimeTypeId:" + mimeTypeId, module);
        Map results = new HashMap();
        //GenericValue content = null;
        if (subContentDataResourceView == null) {
            subContentDataResourceView = ContentWorker.getSubContent(delegator, contentId, mapKey, subContentId, userLogin, null, fromDate);
        }
        results.put("view", subContentDataResourceView);
        if (subContentDataResourceView == null) {
            //throw new IOException("SubContentDataResourceView is null.");
            if (Debug.verboseOn()) Debug.logVerbose(" in renderSubContentAsText, SubContentDataResourceView is null", module);
            return results;
        }

        //String dataResourceId = (String) subContentDataResourceView.get("drDataResourceId");
        subContentId = (String) subContentDataResourceView.get("contentId");
        //GenericValue dataResourceContentView = null;

        if (templateContext == null) {
            templateContext = new HashMap();
        }

        renderContentAsText(delegator, subContentId, out, templateContext, subContentDataResourceView, locale, mimeTypeId);

        return results;
    }

    public static Map renderSubContentAsTextCache(GenericDelegator delegator, String contentId, Writer out, String mapKey, String subContentId, GenericValue subContentDataResourceView, 
            Map templateContext, Locale locale, String mimeTypeId, GenericValue userLogin, Timestamp fromDate) throws GeneralException, IOException {

        //Map context = (Map) FreeMarkerWorker.get(templateContext, "context");
        //if (Debug.verboseOn()) Debug.logVerbose(" in renderSubContentAsText, mimeTypeId:" + mimeTypeId, module);
        Map results = new HashMap();
        //GenericValue content = null;
        if (subContentDataResourceView == null) 
                subContentDataResourceView = ContentWorker.getSubContent(delegator, contentId, mapKey, subContentId, userLogin, null, fromDate);
        results.put("view", subContentDataResourceView);
        if (subContentDataResourceView == null) {
            //throw new IOException("SubContentDataResourceView is null.");
            if (Debug.verboseOn()) Debug.logVerbose(" in renderSubContentAsText, SubContentDataResourceView is null", module);
            return results;
        }

        //String dataResourceId = (String) subContentDataResourceView.get("drDataResourceId");
        subContentId = (String) subContentDataResourceView.get("contentId");
        //GenericValue dataResourceContentView = null;

        if (templateContext == null) {
            templateContext = new HashMap();
        }

        renderContentAsTextCache(delegator, subContentId, out, templateContext, subContentDataResourceView, locale, mimeTypeId);

        return results;
    }

    public static Map renderContentAsText(GenericDelegator delegator, String contentId, Writer out, Map templateContext, GenericValue view, Locale locale, String mimeTypeId) throws GeneralException, IOException {
        //Map context = (Map) FreeMarkerWorker.get(templateContext, "context");
        //if (Debug.verboseOn()) Debug.logVerbose(" in renderContentAsText, mimeTypeId:" + mimeTypeId, module);
        Map results = new HashMap();
        GenericValue content = null;

        if (view == null) {
            if (contentId == null) {
                throw new IOException("ContentId is null");
            }
            try {
                List lst = delegator.findByAnd("SubContentDataResourceView", UtilMisc.toMap("contentId", contentId), UtilMisc.toList("-fromDate"));
                if (lst != null && lst.size() > 0) {
                    view = (GenericValue) lst.get(0);
                } else {
                    if (Debug.verboseOn()) Debug.logVerbose(" in renderContentAsText, no SubContentDataResourceView found.", module);
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
            //if (Debug.verboseOn()) Debug.logVerbose("thisLocaleString" + thisLocaleString, "");
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
        if (Debug.verboseOn()) Debug.logVerbose("in renderContentAsText, view" + view, "");
        if (Debug.verboseOn()) Debug.logVerbose("in renderContentAsText, dataResourceId" + dataResourceId, "");
        if (UtilValidate.isNotEmpty(dataResourceId) || view != null)
            DataResourceWorker.renderDataResourceAsText(delegator, dataResourceId, out, templateContext, view, locale, mimeTypeId);
        //} catch (IOException e) {
        //    return ServiceUtil.returnError(e.getMessage());
        //}

        return results;
    }

    public static Map renderContentAsTextCache(GenericDelegator delegator, String contentId, Writer out, Map templateContext, GenericValue view, Locale locale, String mimeTypeId) throws GeneralException, IOException {
        //Map context = (Map) FreeMarkerWorker.get(templateContext, "context");
        //if (Debug.verboseOn()) Debug.logVerbose(" in renderContentAsText, mimeTypeId:" + mimeTypeId, module);
        Map results = new HashMap();
        GenericValue content = null;

        if (view == null) {
            if (contentId == null) {
                throw new IOException("ContentId is null");
            }
            try {
                List lst = delegator.findByAndCache("SubContentDataResourceView", UtilMisc.toMap("contentId", contentId), UtilMisc.toList("-fromDate"));
                if (lst != null && lst.size() > 0) {
                    view = (GenericValue) lst.get(0);
                } else {
                    if (Debug.verboseOn()) Debug.logVerbose(" in renderContentAsText, no SubContentDataResourceView found.", module);
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
            //if (Debug.verboseOn()) Debug.logVerbose("thisLocaleString" + thisLocaleString, "");
            if (targetLocaleString != null && !targetLocaleString.equalsIgnoreCase(thisLocaleString)) {
                view = findAlternateLocaleContent(delegator, view, locale);
            }
        }
        if (Debug.verboseOn()) Debug.logVerbose(" in renderContentAsText, content." + content, module);

        //String contentTypeId = (String) view.get("contentTypeId");
        String dataResourceId = null;
        try {
            dataResourceId = (String) view.get("drDataResourceId");
        } catch (Exception e) {
            dataResourceId = (String) view.get("dataResourceId");
            view = null; // renderDataResourceAsText will expect DataResource values if not null
        }
        if (Debug.verboseOn()) Debug.logVerbose(" in renderContentAsText, dataResourceId." + dataResourceId, module);
        if (Debug.verboseOn()) Debug.logVerbose(" in renderContentAsText, view." + view, module);

        if (templateContext == null) {
            templateContext = new HashMap();
        }

        // TODO: what should we REALLY do here? looks like there is no decision between Java and Service style error handling...
        //try {
        if (Debug.verboseOn()) Debug.logVerbose("in renderContentAsText, view" + view, "");
        if (Debug.verboseOn()) Debug.logVerbose("in renderContentAsText, dataResourceId" + dataResourceId, "");
        if (UtilValidate.isNotEmpty(dataResourceId) || view != null)
            DataResourceWorker.renderDataResourceAsTextCache(delegator, dataResourceId, out, templateContext, view, locale, mimeTypeId);
        //} catch (IOException e) {
        //    return ServiceUtil.returnError(e.getMessage());
        //}

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
        return ctx;
    }
}
