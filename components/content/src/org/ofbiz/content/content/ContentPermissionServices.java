/*
 * $Id: ContentPermissionServices.java,v 1.4 2003/11/08 22:58:55 byersa Exp $
 *
 * Copyright (c) 2001-2003 The Open For Business Project - www.ofbiz.org
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
 *
 */
package org.ofbiz.content.content;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.GenericDelegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.security.Security;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.ServiceUtil;

/**
 * ContentPermissionServices Class
 *
 * @author     <a href="mailto:byersa@automationgroups.com">Al Byers</a>
 * @version    $Revision: 1.4 $
 * @since      2.2
 * 
 * Services for granting operation permissions on Content entities in a data-driven manner.
 */
public class ContentPermissionServices {

    public static final String module = ContentPermissionServices.class.getName();


    public ContentPermissionServices() {}

    /**
     * checkContentPermission
     *
     *@param dctx The DispatchContext that this service is operating in
     *@param context Map containing the input parameters
     *@return Map with the result of the service, the output parameters
     *
     * This service goes thru a series of test to determine if the user has
     * authority to performed anyone of the passed in target operations.
     *
     * It expects a Content entity in "currentContent" 
     * It expects a list of contentOperationIds in "targetOperationList" rather
     * than a scalar because it is thought that sometimes more than one operation
     * would fit the situation.
     * Similarly, it expects a list of contentPurposeTypeIds in "contentPurposeList".
     * Again, normally there will just be one, but it is possible that a Content 
     * entity could have multiple purposes associated with it.
     * The userLogin GenericValue is also required.
     * A list of roleTypeIds is also possible.
     *
     * The basic sequence of testing events is:
     * First the ContentPurposeOperation table is checked to see if there are any 
     * entries with matching purposes (and operations) with no roleTypeId (ie. _NA_).
     * This is done because it would be the most common scenario and is quick to check.
     *
     * Secondly, the CONTENTMGR permission is checked.
     *
     * Thirdly, the ContentPurposeOperation table is rechecked to see if there are 
     * any conditions with roleTypeIds that match associated ContentRoles tied to the
     * user. 
     * If a Party of "PARTY_GROUP" type is found, the PartyRelationship table is checked
     * to see if the current user is linked to that group.
     *
     * If no match is found to this point and the current Content entity has a value for
     * ownerContentId, then the last step is recusively applied, using the ContentRoles
     * associated with the ownerContent entity.
     */

    public static Map checkContentPermission(DispatchContext dctx, Map context) {

        Security security = dctx.getSecurity();
        GenericDelegator delegator = dctx.getDelegator();
        String statusId = (String) context.get("statusId");
        GenericValue content = (GenericValue) context.get("currentContent"); 
        GenericValue userLogin = (GenericValue) context.get("userLogin"); 
        List passedPurposes = (List) context.get("contentPurposeList"); 
        List targetOperations = (List) context.get("targetOperationList"); 
        List passedRoles = (List) context.get("roleTypeList"); 
        if (passedRoles == null) passedRoles = new ArrayList();
        // If the current user created the content, then add "_OWNER_" as one of
        //   the contentRoles that is in effect.
        if (content != null && content.get("createdByUserLogin") != null 
            && userLogin != null) {
            String userLoginId = (String)userLogin.get("userLoginId");
            String userLoginIdCB = (String)content.get("createdByUserLogin");
            if (userLoginIdCB.equals(userLoginId)) {
                passedRoles.add("_OWNER_");
            }
        }
        String entityAction = (String) context.get("entityOperation");
        if (entityAction == null) entityAction = "_ADMIN";

//Debug.logInfo("targetOperations(0):" + targetOperations, null);
//Debug.logInfo("content:" + content, null);

        Map results = checkPermission( content, statusId,
                                      userLogin, passedPurposes,
                                      targetOperations, passedRoles,
                                      delegator, security, entityAction);
        return results;
    }

    public static Map checkPermission(GenericValue content, String statusId,
                                      GenericValue userLogin, List passedPurposes,
                                      List targetOperations, List passedRoles,
                                      GenericDelegator delegator ,
                                      Security security, String entityAction
        ) {

	List roleIds = null;
        Map result = new HashMap();
        String permissionStatus = null;
        result.put("roleTypeList", passedRoles);

        // Get the ContentPurposeOperation table and save the result to be reused.
        List purposeOperations = null;
        try {
            purposeOperations = delegator.findAllCache("ContentPurposeOperation");
        } catch (GenericEntityException e) {
            return ServiceUtil.returnError("Error in retrieving ContentPurposeOperations. " + e.getMessage());
        }
//Debug.logInfo("purposeOperations:" + purposeOperations, null);
//Debug.logInfo("targetOperations:" + targetOperations, null);


        // Combine any passed purposes with those linked to the Content entity
        // Note that purposeIds is a list of contentPurposeTypeIds, not GenericValues
        List purposeIds = getRelatedPurposes(content, passedPurposes );
//Debug.logInfo("purposeIds:" + purposeIds, null);

        // Do check of non-RoleType conditions
        boolean isMatch = publicMatches(purposeOperations, targetOperations, purposeIds, passedRoles, statusId);
        
        if( isMatch ) {
            result.put("permissionStatus", "granted");
            return result;
        }

        if (userLogin != null ) {
            isMatch = security.hasEntityPermission("CONTENTMGR", entityAction, userLogin);
        }

        if( isMatch ) {
            result.put("permissionStatus", "granted");
            return result;
        }


        if (content == null || content.isEmpty() ) {
            return result;
        }

//Debug.logInfo("userLogin:" + userLogin, null);
        if (userLogin != null ) {

            // Get all roles associated with this Content and the user,
            // including groups.
//Debug.logInfo("before getUserRoles, content(1):" + content, null);
            roleIds = getUserRoles(content, userLogin, passedRoles, delegator);
//Debug.logInfo("roleIds:" + roleIds, null);
		if (passedRoles == null) {
                    passedRoles = roleIds;
                } else {
                    passedRoles.addAll(roleIds);
                }
                result.put("roleTypeList", passedRoles);

            // This is a recursive query that looks for any "owner" content in the 
            // ancestoral path that might have ContentRole associations that
            // make a ContentPurposeOperation condition match.
            Map thisResult = checkPermissionWithRoles(content, purposeIds, passedRoles, 
                             targetOperations, purposeOperations, userLogin, delegator, statusId );
            result.put("roleTypeList", thisResult.get("roleTypeList"));
            result.put("permissionStatus", thisResult.get("permissionStatus"));
        }
        return result;

    }

    /**
     * checkContentPermission
     *
     *@param content The content GenericValue to be checked
     *@param passedPurposes The list of contentPurposeTypeIds to be used in the test 
     *@param passedRoles The list of roleTypeIds to be used in the test 
     *@param targetOperatons The list of contentOperationIds that must be matched
     *@param purposeOperations The list of contentPurposeOperation GenericValues that will
     *                           be used to find matches
     *@param userLogin
     *@param delegator 
     *@return boolean True if a match is found, else false.
     *
     */
    public static Map checkPermissionWithRoles( GenericValue content, List passedPurposes, 
                                           List passedRoles, 
                                           List targetOperations, List purposeOperations,
                                           GenericValue userLogin, GenericDelegator delegator, 
                                           String statusId){ 

        String permissionStatus = null;
        Map result = new HashMap();
        result.put("permissionStatus", permissionStatus);
        result.put("roleTypeList", passedRoles);
        List roleIds = null;
        boolean isMatch = publicMatches(purposeOperations, targetOperations, 
                                        passedPurposes, passedRoles, statusId);
        if (isMatch) {
            result.put("permissionStatus", "granted");
            return result;
        }

        // recursively try if the "owner" Content has ContentRoles that allow a match
        String ownerContentId = (String)content.get("ownerContentId");
//Debug.logInfo("ownerContentId:" + ownerContentId, null);
        if (ownerContentId != null && ownerContentId.length() > 0 ) {
            GenericValue ownerContent = null;
            try {
                ownerContent = delegator.findByPrimaryKeyCache("Content", 
                                                 UtilMisc.toMap("contentId", ownerContentId) );
//Debug.logInfo("ownerContent:" + ownerContent, null);
            } catch (GenericEntityException e) {
                Debug.logError(e, "Owner content not found. ", module);
            }
            if (ownerContent != null) {
//Debug.logInfo("before getUserRoles, ownerContent(2):" + ownerContent, null);
                roleIds = getUserRoles(ownerContent, userLogin, null, delegator);
		if (passedRoles == null) {
                    passedRoles = roleIds;
                } else {
                    passedRoles.addAll(roleIds);
                }
//Debug.logInfo("after getUserRoles, passedRoles(2):" + passedRoles, null);
                Map result2 = checkPermissionWithRoles(ownerContent, passedPurposes, roleIds, 
                             targetOperations, purposeOperations, userLogin,  delegator, statusId );
                result.put("roleTypeList", result2.get("roleTypeList"));
                result.put("permissionStatus", result2.get("permissionStatus"));
            }
        }
        return result;

    }



    /**
     * getUserRoles
     * Queries for the ContentRoles associated with a Content entity
     * and returns the ones that match the user.
     * Follows group parties to see if the user is a member.
     */
    public static List getUserRoles(GenericValue content, GenericValue userLogin, 
                                    List passedRoles, GenericDelegator delegator) {

        if(content == null) return passedRoles;

        ArrayList roles = null;
        if (passedRoles == null) {
            roles = new ArrayList( );
        } else {
            roles = new ArrayList( passedRoles );
        }
        String partyId = (String)userLogin.get("partyId");
	List relatedRoles = null;
        try {
            relatedRoles = content.getRelatedCache("ContentRole");
        } catch (GenericEntityException e) {
            Debug.logError(e, "No related roles found. ", module);
        }
        if(relatedRoles != null ) {
            Iterator rolesIter = relatedRoles.iterator();
            while (rolesIter.hasNext() ) {
                GenericValue contentRole = (GenericValue)rolesIter.next();
                String roleTypeId = (String)contentRole.get("roleTypeId");
                String targPartyId = (String)contentRole.get("partyId");
                if (targPartyId.equals(partyId)) {
                    roles.add(roleTypeId);
                } else {
                    GenericValue party = null;
                    String partyTypeId = null;
                    try {
                        party = contentRole.getRelatedOneCache("Party");
                        partyTypeId = (String)party.get("partyTypeId");
                        if ( partyTypeId != null && partyTypeId.equals("PARTY_GROUP") ) {
                           HashMap map = new HashMap();
                         
                           // At some point from/thru date will need to be added
                           map.put("partyIdFrom", partyId);
                           map.put("partyIdTo", targPartyId);
                           if ( isGroupMember( map, delegator ) ) {
                               roles.add(roleTypeId);
                           }
                        }
                    } catch (GenericEntityException e) {
                        Debug.logError(e, "Error in finding related party. " + e.getMessage(), module);
                    }
                }
            }
        }
        return roles;
    }



    /**
     * publicMatches
     * Takes all the criteria and performs a check to see if there is a match.
     */
    public static boolean publicMatches(List purposeOperations, List targetOperations, 
                   List purposes, List roles, String targStatusId) {
        boolean isMatch = false;
        Iterator purposeOpsIter = purposeOperations.iterator();
        while (purposeOpsIter.hasNext() ) {
            GenericValue purposeOp = (GenericValue)purposeOpsIter.next();
            String roleTypeId = (String)purposeOp.get("roleTypeId");
            String contentPurposeTypeId = (String)purposeOp.get("contentPurposeTypeId");
            String contentOperationId = (String)purposeOp.get("contentOperationId");
            String testStatusId = (String)purposeOp.get("statusId");
//Debug.logInfo("purposeOp:" + purposeOp, null);
//Debug.logInfo("purposes:" + purposes, null);
//Debug.logInfo("roles:" + roles, null);
//Debug.logInfo("targetOperations:" + targetOperations, null);
 
            if ( targetOperations != null && targetOperations.contains(contentOperationId)           
                 && ( (purposes != null && purposes.contains(contentPurposeTypeId) )
                     || contentPurposeTypeId.equals("_NA_") ) 
                 && (testStatusId == null || testStatusId.equals("_NA_")
                     || testStatusId.equals(targStatusId) ) 
               ) {
                if ( roleTypeId == null 
                    || roleTypeId.equals("_NA_") 
                    || (roles != null && roles.contains(roleTypeId) ) ){
                
                    isMatch = true;
                    break;
                }
            }
        }
        return isMatch;
    }



    /**
     * isGroupMember
     * Tests to see if the user belongs to a group
     */
    public static boolean isGroupMember( Map partyRelationshipValues, GenericDelegator delegator ) {
        boolean isMember = false;
        String partyIdFrom = (String)partyRelationshipValues.get("partyIdFrom") ;
        String partyIdTo = (String)partyRelationshipValues.get("partyIdTo") ;
        String roleTypeIdFrom = "CONTENT_PERMISSION_GROUP_MEMBER";
        String roleTypeIdTo = "CONTENT_PERMISSION_GROUP";
        Timestamp fromDate = UtilDateTime.nowTimestamp();
        Timestamp thruDate = UtilDateTime.getDayStart(UtilDateTime.nowTimestamp(), 1);

        if (partyRelationshipValues.get("roleTypeIdFrom") != null ) {
            roleTypeIdFrom = (String)partyRelationshipValues.get("roleTypeIdFrom") ;
        }
        if (partyRelationshipValues.get("roleTypeIdTo") != null ) {
            roleTypeIdTo = (String)partyRelationshipValues.get("roleTypeIdTo") ;
        }
        if (partyRelationshipValues.get("fromDate") != null ) {
            fromDate = (Timestamp)partyRelationshipValues.get("fromDate") ;
        }
        if (partyRelationshipValues.get("thruDate") != null ) {
            thruDate = (Timestamp)partyRelationshipValues.get("thruDate") ;
        }

        EntityExpr partyFromExpr = new EntityExpr("partyIdFrom", EntityOperator.EQUALS, partyIdFrom);
        EntityExpr partyToExpr = new EntityExpr("partyIdTo", EntityOperator.EQUALS, partyIdTo);
       
        EntityExpr relationExpr = new EntityExpr("partyRelationshipTypeId", EntityOperator.EQUALS,
                                                       "CONTENT_PERMISSION");
        //EntityExpr roleTypeIdFromExpr = new EntityExpr("roleTypeIdFrom", EntityOperator.EQUALS, "CONTENT_PERMISSION_GROUP_MEMBER");
        //EntityExpr roleTypeIdToExpr = new EntityExpr("roleTypeIdTo", EntityOperator.EQUALS, "CONTENT_PERMISSION_GROUP");
        EntityExpr fromExpr = new EntityExpr("fromDate", EntityOperator.LESS_THAN_EQUAL_TO,
                                                       fromDate);
        EntityCondition thruCond = new EntityConditionList(
                        UtilMisc.toList(
                            new EntityExpr("thruDate", EntityOperator.EQUALS, null),
                            new EntityExpr("thruDate", EntityOperator.GREATER_THAN, thruDate) ),
                        EntityOperator.OR);

        // This method is simplified to make it work, these conditions need to be added back in.
        //List joinList = UtilMisc.toList(fromExpr, thruCond, partyFromExpr, partyToExpr, relationExpr);
        List joinList = UtilMisc.toList( partyFromExpr, partyToExpr);
        EntityCondition condition = new EntityConditionList(joinList, EntityOperator.AND);

        List partyRelationships = null;
        try {
            partyRelationships = delegator.findByCondition("PartyRelationship", condition, null, null);
        } catch (GenericEntityException e) {
            Debug.logError(e, "Problem finding PartyRelationships. ", module);
            return false;
        }
        if (partyRelationships.size() > 0) {
           isMember = true;
        }

        return isMember;
    }

    /**
     * getRelatedPurposes
     */
    public static List getRelatedPurposes(GenericValue content, List passedPurposes) {

        if(content == null) return passedPurposes;

        List purposeIds = null;
        if (passedPurposes == null) {
            purposeIds = new ArrayList( );
        } else {
            purposeIds = new ArrayList( passedPurposes );
        }


        if (content == null || content.get("contentId") == null ) {
            return purposeIds;
        }

        List purposes = null;
        try {
            purposes = content.getRelatedCache("ContentPurpose");
        } catch (GenericEntityException e) {
            Debug.logError(e, "No associated purposes found. ", module);
        }

        Iterator purposesIter = purposes.iterator();
        while (purposesIter.hasNext() ) {
            GenericValue val = (GenericValue)purposesIter.next();
            purposeIds.add(val.get("contentPurposeTypeId"));
        }
        

        return purposeIds;
    }



    public static Map checkAssocPermission(DispatchContext dctx, Map context) {

//Debug.logInfo("checkAssoc", null);
        Map results = new HashMap();
        Security security = dctx.getSecurity();
        GenericDelegator delegator = dctx.getDelegator();
//Debug.logInfo("checkAssoc, delegator:" + delegator, null);
        String contentIdFrom = (String) context.get("contentIdFrom");
        String contentIdTo = (String) context.get("contentIdTo");
        String statusId = (String) context.get("statusId");
        GenericValue content = (GenericValue) context.get("currentContent"); 
        GenericValue userLogin = (GenericValue) context.get("userLogin"); 
        List purposeList = (List) context.get("contentPurposeList"); 
        List targetOperations = (List) context.get("targetOperationList"); 
        List roleList = (List) context.get("roleTypeList"); 
        if (roleList == null) roleList = new ArrayList();
        String entityAction = (String) context.get("entityOperation");
        if (entityAction == null) entityAction = "_ADMIN";
	List roleIds = null;

//Debug.logInfo("contentIdTo:" + contentIdTo, null);
//Debug.logInfo("contentIdFrom:" + contentIdFrom, null);
        GenericValue contentTo = null;
        GenericValue contentFrom = null;
        try {
                contentTo = delegator.findByPrimaryKeyCache("Content", 
                                                 UtilMisc.toMap("contentId", contentIdTo) );
                contentFrom = delegator.findByPrimaryKeyCache("Content", 
                                                 UtilMisc.toMap("contentId", contentIdFrom) );
//Debug.logInfo("contentTo:" + contentTo, null);
//Debug.logInfo("contentFrom:" + contentFrom, null);
        } catch (GenericEntityException e) {
            Debug.logError(e, " content To or From not found. ", module);
            return ServiceUtil.returnError("Error in retrieving content To or From. " + e.getMessage());
        }
        String creatorLoginTo = (String)contentTo.get("createdByUserLogin");
        String creatorLoginFrom = (String)contentFrom.get("createdByUserLogin");
        if(creatorLoginTo != null && creatorLoginFrom != null 
           && creatorLoginTo.equals(creatorLoginFrom) ) {
            roleList.add("_OWNER_");
        }
    
        Map resultsMap = checkPermission( null, statusId,
                                      userLogin, purposeList,
                                      targetOperations, roleList,
                                      delegator, security, entityAction);
        boolean isMatch = false;
        if(resultsMap.get("permissionStatus").equals("granted") ) isMatch = true;

        boolean isMatchTo = false;
        boolean isMatchFrom = false;
        if(!isMatch){
            roleList = (List)resultsMap.get("roleTypeList");
            resultsMap = checkPermission( contentTo, statusId,
                                      userLogin, purposeList,
                                      targetOperations, roleList,
                                      delegator, security, entityAction);
            if(resultsMap.get("permissionStatus").equals("granted") ) isMatchTo = true;

            resultsMap = checkPermission( contentFrom, statusId,
                                      userLogin, purposeList,
                                      targetOperations, roleList,
                                      delegator, security, entityAction);
            if(resultsMap.get("permissionStatus").equals("granted") ) isMatchFrom = true;
            results.put("roleTypeList", resultsMap.get("roleTypeList"));

            if(isMatchTo && isMatchFrom) isMatch = true;
        }

        String permissionStatus = null;
        if( isMatch ) permissionStatus = "granted";
        results.put("permissionStatus", permissionStatus);
        return results;
    }
}
