/*
 * $Id$
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
import java.util.ListIterator;
import java.util.Map;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.StringUtil;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.GenericDelegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.model.ModelEntity;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.security.Security;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;
import org.ofbiz.service.ModelService;
import org.ofbiz.service.GenericServiceException;
import org.w3c.dom.Element;

/**
 * ContentPermissionServices Class
 *
 * @author     <a href="mailto:byersa@automationgroups.com">Al Byers</a>
 * @version    $Rev$
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
        String privilegeEnumId = (String) context.get("privilegeEnumId");
        GenericValue content = (GenericValue) context.get("currentContent"); 
        Boolean bDisplayFailCond = (Boolean)context.get("displayFailCond");
        boolean displayFailCond = false;
        if (bDisplayFailCond != null && bDisplayFailCond.booleanValue()) {
             displayFailCond = true;   
        }
                Debug.logInfo("displayFailCond(0):" + displayFailCond, "");
        Boolean bDisplayPassCond = (Boolean)context.get("displayPassCond");
        boolean displayPassCond = false;
        if (bDisplayPassCond != null && bDisplayPassCond.booleanValue()) {
             displayPassCond = true;   
        }
                Debug.logInfo("displayPassCond(0):" + displayPassCond, "");
        Map results  = new HashMap();
        String contentId = null;
        if (content != null)
            contentId = content.getString("contentId");
        GenericValue userLogin = (GenericValue) context.get("userLogin"); 
        String partyId = (String) context.get("partyId");
        if (UtilValidate.isEmpty(partyId)) {
            String passedUserLoginId = (String)context.get("userLoginId");
            if (UtilValidate.isNotEmpty(passedUserLoginId)) {
                try {
                    userLogin = delegator.findByPrimaryKeyCache("UserLogin", UtilMisc.toMap("userLoginId", passedUserLoginId));
                    if (userLogin != null) {
                        partyId = userLogin.getString("partyId");   
                    }
                } catch(GenericEntityException e) {
                    ServiceUtil.returnError(e.getMessage());
                }
            }
        }
        if (UtilValidate.isEmpty(partyId) && userLogin != null) {
            partyId = userLogin.getString("partyId");
        }

 
        // Do entity permission check. This will pass users with administrative permissions.
        boolean passed = false;
        // I realized, belatedly, that I wanted to be able to pass parameters in as
        // strings so this service could be used in an action event directly,
        // so I had to write this code to handle both list and strings
        List passedPurposes = (List) context.get("contentPurposeList"); 
        String contentPurposeString = (String) context.get("contentPurposeString"); 
        //Debug.logInfo("contentPurposeString(b):" + contentPurposeString, "");
        if (UtilValidate.isNotEmpty(contentPurposeString)) {
            List purposesFromString = StringUtil.split(contentPurposeString, "|");
            if (passedPurposes == null) {
                passedPurposes = new ArrayList();
            }
            passedPurposes.addAll(purposesFromString);
        }
        
        StdAuxiliaryValueGetter auxGetter = new StdAuxiliaryValueGetter( "ContentPurpose",  "contentPurposeTypeId", "contentId");
        // Sometimes permissions need to be checked before an entity is created, so 
        // there needs to be a method for setting a purpose list
        auxGetter.setList(passedPurposes);
        //Debug.logInfo("passedPurposes(b):" + passedPurposes, "");
        List targetOperations = (List) context.get("targetOperationList"); 
        //Debug.logInfo("targetOperations(b):" + targetOperations, "");
        String targetOperationString = (String) context.get("targetOperationString"); 
        //Debug.logInfo("targetOperationString(b):" + targetOperationString, "");
        if (UtilValidate.isNotEmpty(targetOperationString)) {
            List operationsFromString = StringUtil.split(targetOperationString, "|");
            if (targetOperations == null) {
                targetOperations = new ArrayList();
            }
            targetOperations.addAll(operationsFromString);
        }
        //Debug.logInfo("targetOperations(c):" + targetOperations, "");
        StdPermissionConditionGetter permCondGetter = new StdPermissionConditionGetter ( "ContentPurposeOperation",  "contentOperationId", "roleTypeId", "statusId", "contentPurposeTypeId", "privilegeEnumId");
        permCondGetter.setOperationList(targetOperations);
        
        StdRelatedRoleGetter roleGetter = new StdRelatedRoleGetter ( "Content",  "roleTypeId", "contentId", "partyId", "ownerContentId", "ContentRole");
        //Debug.logInfo("targetOperations(b):" + targetOperations, "");
        List passedRoles = (List) context.get("roleTypeList"); 
        if (passedRoles == null) passedRoles = new ArrayList();
        String roleTypeString = (String) context.get("roleTypeString"); 
        if (UtilValidate.isNotEmpty(roleTypeString)) {
            List rolesFromString = StringUtil.split(roleTypeString, "|");
            passedRoles.addAll(rolesFromString);
        }
        roleGetter.setList(passedRoles);
        
        String entityAction = (String) context.get("entityOperation");
        if (entityAction == null) entityAction = "_ADMIN";
        if (userLogin != null && entityAction != null) {
            passed = security.hasEntityPermission("CONTENTMGR", entityAction, userLogin);
        }
        
        StringBuffer errBuf = new StringBuffer();
        String permissionStatus = null;
        List entityIds = new ArrayList();
        if (passed) {
            results.put("permissionStatus", "granted");   
            permissionStatus = "granted";
            if (displayPassCond) {
                 errBuf.append("\n    hasEntityPermission(" + entityAction + "): PASSED" );
            } 
                
        } else {
            if (displayFailCond || displayPassCond) {
                 errBuf.append("\n    hasEntityPermission(" + entityAction + "): FAILED" );
            } 

            if (content != null)
                entityIds.add(content);
            String quickCheckContentId = (String) context.get("quickCheckContentId");
            if (UtilValidate.isNotEmpty(quickCheckContentId)) {
               List quickList = StringUtil.split(quickCheckContentId, "|"); 
               if (UtilValidate.isNotEmpty(quickList)) entityIds.addAll(quickList);
            }
            try {
                boolean check = checkPermissionMethod(delegator, partyId,  "Content", entityIds, auxGetter, roleGetter, permCondGetter);
                if (check) {
                    results.put("permissionStatus", "granted");
                } else {
                    results.put("permissionStatus", "rejected");
                }
            } catch (GenericEntityException e) {
                ServiceUtil.returnError(e.getMessage());   
            }
            permissionStatus = (String)results.get("permissionStatus");
        }
            
        if ((permissionStatus.equals("granted") && displayPassCond)
            || (permissionStatus.equals("rejected") && displayFailCond)) {
            // Don't show this if passed on 'hasEntityPermission'
            if (displayFailCond || displayPassCond) {
              if (!passed) {
                 errBuf.append("\n    permissionStatus:" );
                 errBuf.append(permissionStatus);
                 errBuf.append("\n    targetOperations:" );
                 errBuf.append(targetOperations);

                 String errMsg = permCondGetter.dumpAsText();
                 errBuf.append("\n" );
                 errBuf.append(errMsg);
                 errBuf.append("\n    partyId:" );
                 errBuf.append(partyId);
                 errBuf.append("\n    entityIds:" );
                 errBuf.append(entityIds);
                 
                 if (auxGetter != null) {
                 	 errBuf.append("\n    auxList:" );
                     errBuf.append(auxGetter.getList());
                 }
                 
                 if (roleGetter != null) {
                 	 errBuf.append("\n    roleList:" );
                     errBuf.append(roleGetter.getList());
                 }
              }
                 
                 Debug.logInfo("displayPass/FailCond(0), errBuf:" + errBuf.toString(), "");
                 results.put(ModelService.ERROR_MESSAGE, errBuf.toString());
            }
        }
        return results;
    }

    public static Map checkPermission(GenericValue content, String statusId, GenericValue userLogin, List passedPurposes, List targetOperations, List passedRoles, GenericDelegator delegator , Security security, String entityAction
        ) {
             String privilegeEnumId = null;
             return checkPermission( content, statusId,
                                      userLogin, passedPurposes,
                                      targetOperations, passedRoles,
                                      delegator, security, entityAction, privilegeEnumId, null);
        }

    public static Map checkPermission(GenericValue content, String statusId,
                                      GenericValue userLogin, List passedPurposes,
                                      List targetOperations, List passedRoles,
                                      GenericDelegator delegator ,
                                      Security security, String entityAction,
                                      String privilegeEnumId, String quickCheckContentId
        ) {
             List statusList = null;
             if (statusId != null) {
                 statusList = StringUtil.split(statusId, "|");
             }
             return checkPermission( content, statusList,
                                      userLogin, passedPurposes,
                                      targetOperations, passedRoles,
                                      delegator, security, entityAction, privilegeEnumId, quickCheckContentId);
        }

    public static Map checkPermission(GenericValue content, List statusList,
                                      GenericValue userLogin, List passedPurposes,
                                      List targetOperations, List passedRoles,
                                      GenericDelegator delegator ,
                                      Security security, String entityAction,
                                      String privilegeEnumId
        ) {
             return checkPermission( content, statusList,
                                      userLogin, passedPurposes,
                                      targetOperations, passedRoles,
                                      delegator, security, entityAction, privilegeEnumId, null);
     }
     
    public static Map checkPermission(GenericValue content, List statusList,
                                      GenericValue userLogin, List passedPurposes,
                                      List targetOperations, List passedRoles,
                                      GenericDelegator delegator ,
                                      Security security, String entityAction,
                                      String privilegeEnumId, String quickCheckContentId
        ) {

        String contentId = null;
        if (content != null)
            contentId = content.getString("contentId");
        List entityIds = new ArrayList();
        if (content != null)
            entityIds.add(content);
        if (UtilValidate.isNotEmpty(quickCheckContentId)) {
            List quickList = StringUtil.split(quickCheckContentId, "|"); 
           if (UtilValidate.isNotEmpty(quickList))
                   entityIds.addAll(quickList);
        }
        Map results  = new HashMap();
        boolean passed = false;
        if (userLogin != null && entityAction != null) {
            passed = security.hasEntityPermission("CONTENTMGR", entityAction, userLogin);
        }
        if (passed) {
            results.put("permissionStatus", "granted");   
            return results;
        }
        try {
            boolean check  = checkPermissionMethod( delegator, userLogin, targetOperations, "Content", entityIds, passedPurposes, null, privilegeEnumId);
            if (check)
                results.put("permissionStatus", "granted");
            else
                results.put("permissionStatus", "rejected");
        } catch (GenericEntityException e) {
            ServiceUtil.returnError(e.getMessage());   
        }
        return results;
    }


    public static boolean checkPermissionMethod(GenericDelegator delegator, GenericValue userLogin, List targetOperationList, String entityName, List entityIdList, List purposeList, List roleList, String privilegeEnumId) throws GenericEntityException {

        boolean passed = false;

        String lcEntityName = entityName.toLowerCase();
        String userLoginId = null;
        String partyId = null;
        if (userLogin != null) {
            userLoginId = userLogin.getString("userLoginId");
            partyId = userLogin.getString("partyId");
        }
        boolean hasRoleOperation = false;
        if (!(targetOperationList == null) && userLoginId != null) {
            hasRoleOperation = checkHasRoleOperations(partyId, targetOperationList, delegator);
        }
        if( hasRoleOperation ) {
            return true;
        }
        ModelEntity modelEntity = delegator.getModelEntity(entityName);
        boolean hasStatusField = false;
        if (modelEntity.getField("statusId") != null)
            hasStatusField = true;
  
        boolean hasPrivilegeField = false;
        if (modelEntity.getField("privilegeEnumId") != null)
            hasPrivilegeField = true;
  
        List operationEntities = null;
        ModelEntity modelOperationEntity = delegator.getModelEntity(entityName + "PurposeOperation");
        if (modelOperationEntity == null) {
            modelOperationEntity = delegator.getModelEntity(entityName + "Operation");            
        }
        
        if (modelOperationEntity == null) {
            Debug.logError("No operation entity found for " + entityName, module);
            throw new RuntimeException("No operation entity found for " + entityName);
        }
        
        boolean hasPurposeOp = false;
        if (modelOperationEntity.getField(lcEntityName + "PurposeTypeId") != null)
            hasPurposeOp = true;
        boolean hasStatusOp = false;
        if (modelOperationEntity.getField("statusId") != null)
            hasStatusOp = true;
        boolean hasPrivilegeOp = false;
        if (modelOperationEntity.getField("privilegeEnumId") != null)
             hasPrivilegeOp = true;
        
        // Get all the condition operations that could apply, rather than having to go thru 
        // entire table each time.
        //List condList = new ArrayList();
        //Iterator iterType = targetOperationList.iterator();
        //while (iterType.hasNext()) {
        //    String op = (String)iterType.next();
        //    condList.add(new EntityExpr(lcEntityName + "OperationId", EntityOperator.EQUALS, op));
        //}
        //EntityCondition opCond = new EntityConditionList(condList, EntityOperator.OR);
        
        EntityCondition opCond = new EntityExpr(lcEntityName + "OperationId", EntityOperator.IN, targetOperationList);
        
        List targetOperationEntityList = delegator.findByConditionCache(modelOperationEntity.getEntityName(), opCond, null, null);
        Map entities = new HashMap();
        String pkFieldName = ContentWorker.getPkFieldName(entityName, modelEntity);

        //TODO: privilegeEnumId test
        /*
        if (hasPrivilegeOp && hasPrivilegeField) {
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
            boolean thisPassed = true; 
            Iterator iter = entityIdList.iterator();
            while (iter.hasNext()) {
                GenericValue entity = getNextEntity(delegator, entityName, pkFieldName, iter.next(), entities);
                if (entity == null) continue;
                   
                String entityId = entity.getString(pkFieldName);
                String targetPrivilegeEnumId = entity.getString("privilegeEnumId");
                if (UtilValidate.isNotEmpty(targetPrivilegeEnumId)) {
                    int targetPrivilegeEnumSeq = -1;
                    GenericValue privEnum = delegator.findByPrimaryKeyCache("Enumeration", UtilMisc.toMap("enumId", privilegeEnumId));
                    if (privEnum != null) {
                        String sequenceId = privEnum.getString("sequenceId");   
                        try {
                            targetPrivilegeEnumSeq = Integer.parseInt(sequenceId);
                        } catch(NumberFormatException e) {
                            // just leave it at -1   
                        }
                        if (targetPrivilegeEnumSeq > privilegeEnumSeq) {
                            return false;   
                        }
                    }
                }
                entities.put(entityId, entity);
            }
        }
        */
        
        // check permission for each id in passed list until success.
        // Note that "quickCheck" id come first in the list
        // Check with no roles or purposes on the chance that the permission fields contain _NA_ s.
        List alreadyCheckedIds = new ArrayList();
        Map purposes = new HashMap();
        Map roles = new HashMap();
        Iterator iter = entityIdList.iterator();
        //List purposeList = null;
        //List roleList = null;
        while (iter.hasNext()) {
               GenericValue entity = getNextEntity(delegator, entityName, pkFieldName, iter.next(), entities);
             if (entity == null) continue;
                   
            String statusId = null;
            if (hasStatusOp && hasStatusField) {
                statusId = entity.getString("statusId");
            }
            
            int privilegeEnumSeq = -1;
            if (hasPrivilegeOp && hasPrivilegeField) {
                privilegeEnumId = entity.getString("privilegeEnumId");
                privilegeEnumSeq = ContentWorker.getPrivilegeEnumSeq(delegator, privilegeEnumId);
            }
               
            passed = hasMatch(entityName, targetOperationEntityList, roleList, hasPurposeOp, purposeList, hasStatusOp, statusId);
            if (passed) {
                break;
            }
       }
        
        if (passed) {
            return true;
        }
        
        if (hasPurposeOp) {
            // Check with just purposes next.
            iter = entityIdList.iterator();
            while (iter.hasNext()) {
                GenericValue entity = getNextEntity(delegator, entityName, pkFieldName, iter.next(), entities);
                if (entity == null) continue;
                 
                String entityId = entity.getString(pkFieldName);
                purposeList = getRelatedPurposes(entity, null);
                String statusId = null;
                if (hasStatusOp && hasStatusField) {
                    statusId = entity.getString("statusId");
                }
                
                if (purposeList.size() > 0) {
                    passed = hasMatch(entityName, targetOperationEntityList, roleList, hasPurposeOp, purposeList, hasStatusOp, statusId);
                }
                if (passed){
                    break;
                }
                purposes.put(entityId, purposeList);
            }
        }
        
        if (passed)
            return true;
        
        if (userLogin == null)
            return false;

        // Check with roles.
        iter = entityIdList.iterator();
        while (iter.hasNext()) {
            GenericValue entity = getNextEntity(delegator, entityName, pkFieldName, iter.next(), entities);
            if (entity == null) continue;
            String entityId = entity.getString(pkFieldName);
            List tmpPurposeList = (List)purposes.get(entityId);
            if (purposeList != null ) {
                if (tmpPurposeList != null) {
                    purposeList.addAll(tmpPurposeList);
                }
            } else { 
                purposeList = tmpPurposeList;
            }
                
            List tmpRoleList = getUserRoles(entity, userLogin, delegator);
            if (roleList != null ) {
                if (tmpRoleList != null) {
                    roleList.addAll(tmpRoleList);
                }
            } else { 
                roleList = tmpRoleList;
            }

            String statusId = null;
            if (hasStatusOp && hasStatusField) {
                statusId = entity.getString("statusId");
            }
               
            passed = hasMatch(entityName, targetOperationEntityList, roleList, hasPurposeOp, purposeList, hasStatusOp, statusId);
            if (passed) {
                break;
            }
            roles.put(entityId, roleList);
        }
        
        if (passed)
            return true;
        
        // Follow ownedEntityIds
        if (modelEntity.getField("owner" + entityName + "Id") != null) {
            iter = entityIdList.iterator();
            while (iter.hasNext()) {
                GenericValue entity = getNextEntity(delegator, entityName, pkFieldName, iter.next(), entities);
                if (entity == null) continue;
                
                String entityId = entity.getString(pkFieldName);
                List ownedContentIdList = new ArrayList();
                ContentWorker.getEntityOwners(delegator, entity, ownedContentIdList, "Content", "ownerContentId");

                List ownedContentRoleIds = ContentWorker.getUserRolesFromList(delegator, ownedContentIdList, partyId, "contentId", "partyId", "roleTypeId", "ContentRole");
                String statusId = null;
                if (hasStatusOp && hasStatusField) {
                    statusId = entity.getString("statusId");
                }
                   
                purposeList = (List)purposes.get(entityId);
                passed = hasMatch(entityName, targetOperationEntityList, ownedContentRoleIds, hasPurposeOp, purposeList, hasStatusOp, statusId);
                if (passed) break;
               
                /* 
                   String ownedEntityId = entity.getString("owner" + entityName + "Id");
                   GenericValue ownedEntity = delegator.findByPrimaryKeyCache(entityName,UtilMisc.toMap(pkFieldName, ownedEntityId));
                   while (ownedEntity != null) {
                       if (!alreadyCheckedIds.contains(ownedEntityId)) {
                        // Decided to let the original purposes only be used in permission checking
                        // 
                        //purposeList = (List)purposes.get(entityId);
                        //purposeList = getRelatedPurposes(ownedEntity, purposeList);
                        roleList = getUserRoles(ownedEntity, userLogin, delegator);
            
                        String statusId = null;
                        if (hasStatusOp && hasStatusField) {
                            statusId = entity.getString("statusId");
                        }
                           
                        passed = hasMatch(entityName, targetOperationEntityList, roleList, hasPurposeOp, purposeList, hasStatusOp, statusId);
                        if (passed)
                            break;
                        alreadyCheckedIds.add(ownedEntityId);
                       //purposes.put(ownedEntityId, purposeList);
                        //roles.put(ownedEntityId, roleList);
                           ownedEntityId = ownedEntity.getString("owner" + entityName + "Id");
                           ownedEntity = delegator.findByPrimaryKeyCache(entityName,UtilMisc.toMap(pkFieldName, ownedEntityId));
                       } else {
                          ownedEntity = null;
                       }
                   }
                   if (passed)
                       break;
                       */
            }
        }
        
        
        /* seems like repeat
        // Check parents
        iter = entityIdList.iterator();
        while (iter.hasNext()) {
            String entityId = (String)iter.next();
               GenericValue entity = (GenericValue)entities.get(entityId);
            purposeList = (List)purposes.get(entityId);
            roleList = getUserRoles(entity, userLogin, delegator);

            String statusId = null;
            if (hasStatusOp && hasStatusField) {
                statusId = entity.getString("statusId");
            }
            String targetPrivilegeEnumId = null;
            if (hasPrivilegeOp && hasPrivilegeField) {
                targetPrivilegeEnumId = entity.getString("privilegeEnumId");
            }
               
            passed = hasMatch(entityName, targetOperationEntityList, roleList, hasPurposeOp, purposeList, hasStatusOp, statusId);
            if (passed)
                break;
            alreadyCheckedIds.add(entityId);
        }
        */
        
        return passed;
    }
    public static boolean checkPermissionMethod(GenericDelegator delegator, String partyId,  String entityName, List entityIdList, AuxiliaryValueGetter auxiliaryValueGetter, RelatedRoleGetter relatedRoleGetter, PermissionConditionGetter permissionConditionGetter) throws GenericEntityException {

        permissionConditionGetter.init(delegator);
        if (Debug.verboseOn()) Debug.logVerbose(permissionConditionGetter.dumpAsText(), module);
        boolean passed = false;

        String lcEntityName = entityName.toLowerCase();
        String userLoginId = null;
        boolean checkAncestors = false;
        boolean hasRoleOperation =  checkHasRoleOperations(partyId, permissionConditionGetter, delegator);
        if( hasRoleOperation ) {
            return true;
        }
        ModelEntity modelEntity = delegator.getModelEntity(entityName);
        
        // check permission for each id in passed list until success.
        // Note that "quickCheck" id come first in the list
        // Check with no roles or purposes on the chance that the permission fields contain _NA_ s.
        String pkFieldName = ContentWorker.getPkFieldName(entityName, modelEntity);
        if (Debug.infoOn()) {
        String entityIdString = "ENTITIES: ";
        for (int i=0; i < entityIdList.size(); i++) {
            Object obj = entityIdList.get(i);
            if (obj instanceof GenericValue) {
                String s = ((GenericValue)obj).getString(pkFieldName);
                entityIdString += s + "  ";
            } else {
                entityIdString += obj + "  ";
            }
        }
            //if (Debug.infoOn()) Debug.logInfo(entityIdString, module);
        }
        
        List alreadyCheckedIds = new ArrayList();
        Map entities = new HashMap();
        Iterator iter = entityIdList.iterator();
        //List purposeList = null;
        //List roleList = null;
        while (iter.hasNext()) {
            GenericValue entity = getNextEntity(delegator, entityName, pkFieldName, iter.next(), entities);
            if (entity == null) continue;
            checkAncestors = false;
            passed = hasMatch(entity, permissionConditionGetter, null, null, partyId, checkAncestors);
            if (passed) {
                break;
            }
       }
        
        if (passed) {
            return true;
        }
        
        if (auxiliaryValueGetter != null) {
            //if (Debug.infoOn()) Debug.logInfo(auxiliaryValueGetter.dumpAsText(), module);
            // Check with just purposes next.
            iter = entityIdList.iterator();
            while (iter.hasNext()) {
                GenericValue entity = getNextEntity(delegator, entityName, pkFieldName, iter.next(), entities);
                if (entity == null) continue;
                checkAncestors = false;
                passed = hasMatch(entity, permissionConditionGetter, null, auxiliaryValueGetter, partyId, checkAncestors);
                 
                if (passed){
                    break;
                }
            }
        }
        
        if (passed) return true;
        
        // TODO: need to return some information here about why it failed
        if (partyId == null) return false;

        // Check with roles.
        if (relatedRoleGetter != null) {
            iter = entityIdList.iterator();
            while (iter.hasNext()) {
                GenericValue entity = getNextEntity(delegator, entityName, pkFieldName, iter.next(), entities);
                if (entity == null) continue;
                checkAncestors = false;
                passed = hasMatch(entity, permissionConditionGetter, relatedRoleGetter, auxiliaryValueGetter, partyId, checkAncestors);
                 
                if (passed){
                    break;
                }
            }
        }
        
        if (passed)
            return true;
        
        if (relatedRoleGetter != null) {
            iter = entityIdList.iterator();
            while (iter.hasNext()) {
                GenericValue entity = getNextEntity(delegator, entityName, pkFieldName, iter.next(), entities);
                if (entity == null) continue;
                String entityId = entity.getString(pkFieldName);
                checkAncestors = true;
                passed = hasMatch(entity, permissionConditionGetter, relatedRoleGetter, auxiliaryValueGetter, partyId, checkAncestors);
                 
                if (passed){
                    break;
                }
            }
        }
        
        
        return passed;
    }
    

    public static GenericValue getNextEntity(GenericDelegator delegator, String entityName, String pkFieldName, Object obj, Map entities) throws GenericEntityException {
           
        GenericValue entity = null;
        if (obj instanceof String) {
            String entityId  = (String)obj; 
            if (entities != null)
               entity = (GenericValue)entities.get(entityId);
            
            if (entity == null)
                entity = delegator.findByPrimaryKeyCache(entityName,UtilMisc.toMap(pkFieldName, entityId));
        } else if (obj instanceof GenericValue) {
            entity = (GenericValue)obj;
        }
        return entity;
    }
 
    public static boolean checkHasRoleOperations(String partyId,  ContentPermissionServices.PermissionConditionGetter permissionConditionGetter , GenericDelegator delegator) {
    
        List targetOperations = permissionConditionGetter.getOperationList();
        return checkHasRoleOperations(partyId, targetOperations, delegator);
    }
    
    public static boolean checkHasRoleOperations(String partyId,  List targetOperations, GenericDelegator delegator) {

        //if (Debug.infoOn()) Debug.logInfo("targetOperations:" + targetOperations, module);
        //if (Debug.infoOn()) Debug.logInfo("userLoginId:" + userLoginId, module);
        if (targetOperations == null)
            return false;

        if (partyId != null && targetOperations.contains("HAS_USER_ROLE"))
            return true;

        boolean hasRoleOperation = false;
        Iterator targOpIter = targetOperations.iterator();
        boolean hasNeed = false;
        List newHasRoleList = new ArrayList();
        while (targOpIter.hasNext()) {
            String roleOp = (String)targOpIter.next();
            int idx1 = roleOp.indexOf("HAS_");
            if (idx1 == 0) {
                String roleOp1 = roleOp.substring(4); // lop off "HAS_"
                int idx2 = roleOp1.indexOf("_ROLE");
                if (idx2 == (roleOp1.length() - 5)) {
                    String roleOp2 = roleOp1.substring(0, roleOp1.indexOf("_ROLE") - 1); // lop off "_ROLE"
                    //if (Debug.infoOn()) Debug.logInfo("roleOp2:" + roleOp2, module);
                    newHasRoleList.add(roleOp2);
                    hasNeed = true;
                }
            }
        }

        if (hasNeed) {
            GenericValue uLogin = null;
            try {
                if (UtilValidate.isNotEmpty(partyId)) {
                    List partyRoleList = delegator.findByAndCache("PartyRole", UtilMisc.toMap("partyId", partyId));
                    Iterator partyRoleIter = partyRoleList.iterator();
                    while (partyRoleIter.hasNext()) {
                        GenericValue partyRole = (GenericValue)partyRoleIter.next();
                        String roleTypeId = partyRole.getString("roleTypeId");
                        targOpIter = newHasRoleList.iterator();
                        while (targOpIter.hasNext()) {
                            String thisRole = (String)targOpIter.next();
                            if (roleTypeId.indexOf(thisRole) >= 0) {
                                hasRoleOperation = true;
                                break;
                            }
                        }
                        if (hasRoleOperation)
                            break;
                    }
                }
            } catch (GenericEntityException e) {
                Debug.logError(e, module);
                return hasRoleOperation;
            }
        }
        return hasRoleOperation;
    }
    
    public static boolean hasMatch(String entityName, List targetOperations, List roles, boolean hasPurposeOp, List purposes, boolean hasStatusOp, String targStatusId) {
        boolean isMatch = false;
        int targPrivilegeSeq = 0;
//        if (UtilValidate.isNotEmpty(targPrivilegeEnumId) && !targPrivilegeEnumId.equals("_NA_") && !targPrivilegeEnumId.equals("_00_") ) {
            // need to do a lookup here to find the seq value of targPrivilegeEnumId.
            // The lookup could be a static store or it could be done on Enumeration entity.
//        }
        String lcEntityName = entityName.toLowerCase();
        Iterator targetOpsIter = targetOperations.iterator();
        while (targetOpsIter.hasNext() ) {
            GenericValue targetOp = (GenericValue)targetOpsIter.next();
            String testRoleTypeId = (String)targetOp.get("roleTypeId");
            String testContentPurposeTypeId = null;
            if (hasPurposeOp)
                testContentPurposeTypeId = (String)targetOp.get(lcEntityName + "PurposeTypeId");
            String testStatusId = null;
            if (hasStatusOp)
                testStatusId = (String)targetOp.get("statusId");
            //String testPrivilegeEnumId = null;
            //if (hasPrivilegeOp)
                //testPrivilegeEnumId = (String)targetOp.get("privilegeEnumId");
            //int testPrivilegeSeq = 0;

            boolean purposesCond = ( !hasPurposeOp || (purposes != null && purposes.contains(testContentPurposeTypeId) ) || testContentPurposeTypeId.equals("_NA_") ); 
            boolean statusCond = ( !hasStatusOp || testStatusId.equals("_NA_") || (targStatusId != null && targStatusId.equals(testStatusId) ) ); 
            //boolean privilegeCond = ( !hasPrivilegeOp || testPrivilegeEnumId.equals("_NA_") || testPrivilegeSeq <= targPrivilegeSeq || testPrivilegeEnumId.equals(targPrivilegeEnumId) ); 
            boolean roleCond = ( testRoleTypeId.equals("_NA_") || (roles != null && roles.contains(testRoleTypeId) ) );

 
            if (purposesCond && statusCond && roleCond) {
                
                    isMatch = true;
                    break;
            }
        }
        return isMatch;
    }
    
    public static boolean hasMatch(GenericValue entity, PermissionConditionGetter permissionConditionGetter, RelatedRoleGetter relatedRoleGetter, AuxiliaryValueGetter auxiliaryValueGetter, String partyId, boolean checkAncestors) throws GenericEntityException {
    
        String entityName = entity.getEntityName();
        ModelEntity modelEntity = entity.getModelEntity();
        GenericDelegator delegator = entity.getDelegator();
        String pkFieldName = ContentWorker.getPkFieldName(entityName, modelEntity);
        String entityId = entity.getString(pkFieldName);
        if (Debug.verboseOn()) Debug.logVerbose("\n\nIN hasMatch: entityId:" + entityId + " partyId:" + partyId + " checkAncestors:" + checkAncestors, module);
        boolean isMatch = false;
        permissionConditionGetter.restart();
        List auxiliaryValueList = null;
        if (auxiliaryValueGetter != null) {
           auxiliaryValueGetter.init(delegator, entityId);
           auxiliaryValueList =   auxiliaryValueGetter.getList();
            if (Debug.verboseOn()) Debug.logVerbose(auxiliaryValueGetter.dumpAsText(), module);
        } else {
            if (Debug.verboseOn()) Debug.logVerbose("NO AUX GETTER", module);
        }
        List roleValueList = null;
        if (relatedRoleGetter != null) {
            if (checkAncestors) {
                relatedRoleGetter.initWithAncestors(delegator, entity, partyId);
            } else {
                relatedRoleGetter.init(delegator, entityId, partyId, entity);
            }
            roleValueList =   relatedRoleGetter.getList();
            if (Debug.verboseOn()) Debug.logVerbose(relatedRoleGetter.dumpAsText(), module);
        } else {
            if (Debug.verboseOn()) Debug.logVerbose("NO ROLE GETTER", module);
        }
        
        String targStatusId = null;
        if (modelEntity.getField("statusId") != null) {
            targStatusId = entity.getString("statusId");   
        }
            if (Debug.verboseOn()) Debug.logVerbose("STATUS:" + targStatusId, module);
        
        while (permissionConditionGetter.getNext() ) {
            String roleConditionId = permissionConditionGetter.getRoleValue();
            String auxiliaryConditionId = permissionConditionGetter.getAuxiliaryValue();
            String statusConditionId = permissionConditionGetter.getStatusValue();

            boolean auxiliaryCond = ( auxiliaryConditionId == null ||  auxiliaryConditionId.equals("_NA_") || (auxiliaryValueList != null && auxiliaryValueList.contains(auxiliaryConditionId) )  ); 
            boolean statusCond = ( statusConditionId == null || statusConditionId.equals("_NA_") || (targStatusId != null && targStatusId.equals(statusConditionId) ) ); 
            boolean roleCond = ( roleConditionId == null || roleConditionId.equals("_NA_") || (roleValueList != null && roleValueList.contains(roleConditionId) ) );
 
            if (auxiliaryCond && statusCond && roleCond) {
                if (Debug.verboseOn()) Debug.logVerbose("MATCHED: role:" + roleConditionId + " status:" + statusConditionId + " aux:" + auxiliaryConditionId, module);
                    isMatch = true;
                    break;
            }
        }
        return isMatch;
    }
    
    /**
     * getRelatedPurposes
     */
    public static List getRelatedPurposes(GenericValue entity, List passedPurposes) {

        if(entity == null) return passedPurposes;

        List purposeIds = null;
        if (purposeIds == null) {
            purposeIds = new ArrayList( );
        } else {
            purposeIds = new ArrayList( passedPurposes );
        }

        String entityName = entity.getEntityName();
        String lcEntityName = entityName.toLowerCase();

        List purposes = null;
        try {
            purposes = entity.getRelatedCache(entityName + "Purpose");
        } catch (GenericEntityException e) {
            Debug.logError(e, "No associated purposes found. ", module);
            return purposeIds;
        }

        Iterator purposesIter = purposes.iterator();
        while (purposesIter.hasNext() ) {
            GenericValue val = (GenericValue)purposesIter.next();
            purposeIds.add(val.get(lcEntityName + "PurposeTypeId"));
        }
        

        return purposeIds;
    }
 

    /**
     * getUserRoles
     * Queries for the ContentRoles associated with a Content entity
     * and returns the ones that match the user.
     * Follows group parties to see if the user is a member.
     */
    public static List getUserRoles(GenericValue entity, GenericValue userLogin, GenericDelegator delegator) throws GenericEntityException {

        String entityName = entity.getEntityName();
        List roles = new ArrayList();
        if(entity == null) return roles;
            // TODO: Need to use ContentManagementWorker.getAuthorContent first

 
        roles.remove("OWNER"); // always test with the owner of the current content
        if ( entity.get("createdByUserLogin") != null && userLogin != null) {
            String userLoginId = (String)userLogin.get("userLoginId");
            String userLoginIdCB = (String)entity.get("createdByUserLogin");
            //if (Debug.infoOn()) Debug.logInfo("userLoginId:" + userLoginId + ": userLoginIdCB:" + userLoginIdCB + ":", null);
            if (userLoginIdCB.equals(userLoginId)) {
                roles.add("OWNER");
                //if (Debug.infoOn()) Debug.logInfo("in getUserRoles, passedRoles(0):" + passedRoles, null);
            }
        }
        
        String partyId = (String)userLogin.get("partyId");
        List relatedRoles = null;
        List tmpRelatedRoles = entity.getRelatedCache(entityName + "Role");
        relatedRoles = EntityUtil.filterByDate(tmpRelatedRoles);
        if(relatedRoles != null ) {
            Iterator rolesIter = relatedRoles.iterator();
            while (rolesIter.hasNext() ) {
                GenericValue contentRole = (GenericValue)rolesIter.next();
                String roleTypeId = (String)contentRole.get("roleTypeId");
                String targPartyId = (String)contentRole.get("partyId");
                if (targPartyId.equals(partyId)) {
                    if (!roles.contains(roleTypeId))
                        roles.add(roleTypeId);
                    if (roleTypeId.equals("AUTHOR") && !roles.contains("OWNER"))
                        roles.add("OWNER");
                } else { // Party may be of "PARTY_GROUP" type, in which case the userLogin may still possess this role
                    GenericValue party = null;
                    String partyTypeId = null;
                    try {
                        party = contentRole.getRelatedOne("Party");
                        partyTypeId = (String)party.get("partyTypeId");
                        if ( partyTypeId != null && partyTypeId.equals("PARTY_GROUP") ) {
                           HashMap map = new HashMap();
                         
                           // At some point from/thru date will need to be added
                           map.put("partyIdFrom", partyId);
                           map.put("partyIdTo", targPartyId);
                           if ( isGroupMember( map, delegator ) ) {
                               if (!roles.contains(roleTypeId))
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
     * Tests to see if the user belongs to a group
     */
    public static boolean isGroupMember( Map partyRelationshipValues, GenericDelegator delegator ) {
        boolean isMember = false;
        String partyIdFrom = (String)partyRelationshipValues.get("partyIdFrom") ;
        String partyIdTo = (String)partyRelationshipValues.get("partyIdTo") ;
        String roleTypeIdFrom = "PERMISSION_GROUP_MBR";
        String roleTypeIdTo = "PERMISSION_GROUP";
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
    

    public static Map checkAssocPermission(DispatchContext dctx, Map context) {

        Map results = new HashMap();
        Security security = dctx.getSecurity();
        GenericDelegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        Boolean bDisplayFailCond = (Boolean)context.get("displayFailCond");
        String contentIdFrom = (String) context.get("contentIdFrom");
        String contentIdTo = (String) context.get("contentIdTo");
        GenericValue userLogin = (GenericValue) context.get("userLogin"); 
        String entityAction = (String) context.get("entityOperation");
        if (entityAction == null) entityAction = "_ADMIN";
        List roleIds = null;
        String permissionStatus = null;

        GenericValue contentTo = null;
        GenericValue contentFrom = null;
        try {
            contentTo = delegator.findByPrimaryKeyCache("Content", UtilMisc.toMap("contentId", contentIdTo) );
            contentFrom = delegator.findByPrimaryKeyCache("Content",  UtilMisc.toMap("contentId", contentIdFrom) );
        } catch (GenericEntityException e) {
            return ServiceUtil.returnError("Error in retrieving content To or From. " + e.getMessage());
        }
        if (contentTo == null || contentFrom == null) {
            return ServiceUtil.returnError("contentTo[" + contentTo + "]/From[" + contentFrom + "] is null. ");
        }
        Map resultsMap = null;
        boolean isMatch = false;

        boolean isMatchTo = false;
        boolean isMatchFrom = false;
        Map permResults = new HashMap();
        String skipPermissionCheck = null;

        if (skipPermissionCheck == null
            || skipPermissionCheck.length() == 0
            || (!skipPermissionCheck.equalsIgnoreCase("true") && !skipPermissionCheck.equalsIgnoreCase("granted"))) {
            List relatedPurposes = getRelatedPurposes(contentFrom, null);
            Map serviceInMap = new HashMap();
            serviceInMap.put("userLogin", userLogin);
            serviceInMap.put("targetOperationList", UtilMisc.toList("CONTENT_LINK_TO"));
            serviceInMap.put("contentPurposeList", relatedPurposes);
            serviceInMap.put("currentContent", contentTo);
            serviceInMap.put("displayFailCond", bDisplayFailCond);

            try {
                permResults = dispatcher.runSync("checkContentPermission", serviceInMap);
            } catch (GenericServiceException e) {
                Debug.logError(e, "Problem checking permissions", "ContentServices");
            }
            permissionStatus = (String)permResults.get("permissionStatus");
            if(permissionStatus == null || !permissionStatus.equals("granted") ) {
                if (bDisplayFailCond != null && bDisplayFailCond.booleanValue()) {
                     String errMsg = (String)permResults.get(ModelService.ERROR_MESSAGE);
                     results.put(ModelService.ERROR_MESSAGE, errMsg);
                }
                return results;
            }
            serviceInMap.put("currentContent", contentFrom);
            serviceInMap.put("targetOperationList", UtilMisc.toList("CONTENT_LINK_FROM"));
            try {
                permResults = dispatcher.runSync("checkContentPermission", serviceInMap);
            } catch (GenericServiceException e) {
                Debug.logError(e, "Problem checking permissions", "ContentServices");
            }
            permissionStatus = (String)permResults.get("permissionStatus");
            if(permissionStatus != null && permissionStatus.equals("granted") ) {
                results.put("permissionStatus", "granted");   
            } else {
                if (bDisplayFailCond != null && bDisplayFailCond.booleanValue()) {
                     String errMsg = (String)permResults.get(ModelService.ERROR_MESSAGE);
                     results.put(ModelService.ERROR_MESSAGE, errMsg);
                }
            }
        } else {
            results.put("permissionStatus", "granted");   
        }
        return results;
    }


    public interface PermissionConditionGetter {
        
    	public boolean getNext();
        public String getRoleValue();
        public String getOperationValue();
        public String getStatusValue();
        public int getPrivilegeValue() throws GenericEntityException;
        public String getAuxiliaryValue();
        public void init(GenericDelegator delegator) throws GenericEntityException;
        public void restart();
        public void setOperationList(String operationIdString);
        public void setOperationList(List opList);
        public List getOperationList();
        public String dumpAsText();
        public void clearList();
    }
    
    public static class StdPermissionConditionGetter implements PermissionConditionGetter {
    
        protected List entityList;
        protected List operationList;
        protected ListIterator iter;
        protected GenericValue currentValue;
        protected String operationFieldName;
        protected String roleFieldName;
        protected String statusFieldName;
        protected String privilegeFieldName;
        protected String auxiliaryFieldName;
        protected String entityName;
        
        public StdPermissionConditionGetter () {
            
        	this.operationFieldName = "contentOperationId";
        	this.roleFieldName = "roleTypeId";
        	this.statusFieldName = "statusId";
        	this.privilegeFieldName = "privilegeEnumId";
        	this.auxiliaryFieldName = "contentPurposeTypeId";
        	this.entityName = "ContentPurposeOperation";
        }
        
        public StdPermissionConditionGetter ( String entityName, String operationFieldName, String roleFieldName, String statusFieldName, String auxiliaryFieldName, String privilegeFieldName) {
            
        	this.operationFieldName = operationFieldName;
        	this.roleFieldName = roleFieldName ;
        	this.statusFieldName = statusFieldName ;
        	this.privilegeFieldName = privilegeFieldName ;
        	this.auxiliaryFieldName = auxiliaryFieldName ;
        	this.entityName = entityName;
        }
        
        public StdPermissionConditionGetter ( Element getterElement) {
            this.operationFieldName = getterElement.getAttribute("operation-field-name");
            this.roleFieldName = getterElement.getAttribute("role-field-name");
            this.statusFieldName = getterElement.getAttribute("status-field-name");
            this.privilegeFieldName = getterElement.getAttribute("privilege-field-name");
            this.auxiliaryFieldName = getterElement.getAttribute("auxiliary-field-name");
            this.entityName = getterElement.getAttribute("entity-name");
        }
        
    	public boolean getNext() {
    		boolean hasNext = false;
    		if (iter != null && iter.hasNext()) {
                currentValue = (GenericValue)iter.next();
                hasNext = true;
            }
            return hasNext;
        }
        
        public String getRoleValue() {
            return this.currentValue.getString(this.roleFieldName);
        }

        public String getOperationValue() {
            return this.currentValue.getString(this.operationFieldName);
        }
        public String getStatusValue() {
            return this.currentValue.getString(this.statusFieldName);
            
        }
        public int getPrivilegeValue() throws GenericEntityException {
            int privilegeEnumSeq = -1;
            String privilegeEnumId = null;
            GenericDelegator delegator = currentValue.getDelegator();
            
            if (UtilValidate.isNotEmpty(privilegeFieldName)) {
                privilegeEnumId = currentValue.getString(this.privilegeFieldName);   
            }
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
        
        public String getAuxiliaryValue() {
            return this.currentValue.getString(this.auxiliaryFieldName);
        }
        
        public void setOperationList(String operationIdString) {
            
            this.operationList = null;
        	if (UtilValidate.isNotEmpty(operationIdString)) {
                this.operationList = StringUtil.split(operationIdString, "|");
            }
        }
        
        public void setOperationList(List operationList) {
            this.operationList = operationList;
        }
        
        public List getOperationList() {
            return this.operationList;
        }
        
    	public void clearList() {
            this.entityList = new ArrayList();
        }
        
        public void init( GenericDelegator delegator) throws GenericEntityException {
            EntityCondition opCond = new EntityExpr(operationFieldName, EntityOperator.IN, this.operationList);
            this.entityList = delegator.findByConditionCache(this.entityName, opCond, null, null);
        }
        
        public void restart() {
            this.iter = null;
            if (this.entityList != null) {
                this.iter = this.entityList.listIterator();   
            }
        }
    
        public String dumpAsText() {
             List fieldNames = UtilMisc.toList("roleFieldName",  "auxiliaryFieldName",  "statusFieldName");
             Map widths = UtilMisc.toMap("roleFieldName", new Integer(24), "auxiliaryFieldName", new Integer(24), "statusFieldName", new Integer(24));
             StringBuffer buf = new StringBuffer();
             Integer wid = null;
             
             buf.append("Dump for ");
             buf.append(this.entityName);
             buf.append(" ops:");
             buf.append(StringUtil.join(this.operationList, ","));
             buf.append("\n");
             Iterator itFields = fieldNames.iterator();
             while (itFields.hasNext()) {
                 String fld = (String)itFields.next();
                 wid = (Integer)widths.get(fld);
                 buf.append(fld);  
                 for (int i=0; i < (wid.intValue() - fld.length()); i++) buf.append("^");
                 buf.append("  ");
             }
                     buf.append("\n");
             itFields = fieldNames.iterator();
             while (itFields.hasNext()) {
                 String fld = (String)itFields.next();
                 wid = (Integer)widths.get(fld);
                 for (int i=0; i < wid.intValue(); i++) buf.append("-");
                 buf.append("  ");
             }
                     buf.append("\n");
             if (entityList != null) {
                 Iterator it = this.entityList.iterator();
                 while (it.hasNext()) {
                     GenericValue contentPurposeOperation = (GenericValue)it.next();  
                     /*
                     String contentOperationId = contentPurposeOperation.getString(this.operationFieldName);
                     if (UtilValidate.isEmpty(contentOperationId)) {
                         contentOperationId = "";
                     }
                     wid = (Integer)widths.get("operationFieldName");
                     buf.append(contentOperationId);  
                     for (int i=0; i < (wid.intValue() - contentOperationId.length()); i++) buf.append("^");
                     buf.append("  ");
                     */
                     
                     String roleTypeId = contentPurposeOperation.getString(this.roleFieldName);
                     if (UtilValidate.isEmpty(roleTypeId)) {
                         roleTypeId = "";
                     }
                     wid = (Integer)widths.get("roleFieldName");
                     buf.append(roleTypeId);  
                     for (int i=0; i < (wid.intValue() - roleTypeId.length()); i++) buf.append("^");
                     buf.append("  ");
                     
                     String  auxiliaryFieldValue = contentPurposeOperation.getString(this.auxiliaryFieldName);
                     if (UtilValidate.isEmpty(auxiliaryFieldValue)) {
                         auxiliaryFieldValue = "";
                     }
                     wid = (Integer)widths.get("auxiliaryFieldName");
                     buf.append(auxiliaryFieldValue);  
                     for (int i=0; i < (wid.intValue() - auxiliaryFieldValue.length()); i++) buf.append("^");
                     buf.append("  ");
                     
                     String statusId = contentPurposeOperation.getString(this.statusFieldName);
                     if (UtilValidate.isEmpty(statusId)) {
                         statusId = "";
                     }
                     buf.append(statusId);  
                     /*
                     wid = (Integer)widths.get("statusFieldName");
                     for (int i=0; i < (wid.intValue() - statusId.length()); i++) buf.append(" ");
                     */
                     buf.append("  ");
                     
                     buf.append("\n");
                 }
             }
             return buf.toString();
        }
    }
    
    public interface AuxiliaryValueGetter {
        public void init(GenericDelegator delegator, String entityId) throws GenericEntityException;
        public List getList();
        public void clearList();
        public String dumpAsText();
    }
    
    public static class StdAuxiliaryValueGetter implements AuxiliaryValueGetter {
    
        protected List entityList = new ArrayList();
        protected String auxiliaryFieldName;
        protected String entityName;
        protected String entityIdName;
        
        public StdAuxiliaryValueGetter () {
            
        	this.auxiliaryFieldName = "contentPurposeTypeId";
        	this.entityName = "ContentPurpose";
        	this.entityIdName = "contentId";
        }
        
        public StdAuxiliaryValueGetter ( String entityName,  String auxiliaryFieldName, String entityIdName) {
            
        	this.auxiliaryFieldName = auxiliaryFieldName ;
        	this.entityName = entityName;
        	this.entityIdName = entityIdName;
        }
        
        public StdAuxiliaryValueGetter ( Element getterElement) {
        
            this.auxiliaryFieldName = getterElement.getAttribute("auxiliary-field-name");
            this.entityName = getterElement.getAttribute("entity-name");
            this.entityIdName = getterElement.getAttribute("entity-id-name");
        }
        
    	public List getList() {
            return entityList;
        }
        
    	public void clearList() {
            this.entityList = new ArrayList();
        }
        
    	public void setList(List lst) {
            this.entityList = lst;
        }
        
        public void init(GenericDelegator delegator, String entityId) throws GenericEntityException {
            
            if (this.entityList == null) {
               this.entityList = new ArrayList(); 
            }
            if (UtilValidate.isEmpty(this.entityName)) {
                return;   
            }
            List values = delegator.findByAndCache(this.entityName, UtilMisc.toMap(this.entityIdName, entityId));
            Iterator iter = values.iterator();
            while (iter.hasNext()) {
                GenericValue entity = (GenericValue)iter.next();
                this.entityList.add(entity.getString(this.auxiliaryFieldName)); 
            }
        }
        
        public String dumpAsText() {
             StringBuffer buf = new StringBuffer();
             buf.append("AUXILIARY: ");
             if (entityList != null) {
                for (int i=0; i < entityList.size(); i++) {
                    String val = (String)entityList.get(i);
                    buf.append(val);
                    buf.append("  ");
                }
             }
             return buf.toString();
        }
    }
    
    public interface RelatedRoleGetter {
        public void init(GenericDelegator delegator, String entityId, String partyId, GenericValue entity) throws GenericEntityException;
        public void initWithAncestors(GenericDelegator delegator, GenericValue entity, String partyId) throws GenericEntityException;
        public List getList();
        public void clearList();
        public void setList(List lst);
        public String dumpAsText();
        public boolean isOwner(GenericValue entity, String targetPartyId);
    }
    
    public static class StdRelatedRoleGetter implements RelatedRoleGetter {
    
        protected List roleIdList = new ArrayList();
        protected String roleTypeFieldName;
        protected String partyFieldName;
        protected String entityName;
        protected String roleEntityIdName;
        protected String roleEntityName;
        protected String ownerEntityFieldName;
        
        public StdRelatedRoleGetter () {
            
        	this.roleTypeFieldName = "roleTypeId";
        	this.partyFieldName = "partyId";
        	this.ownerEntityFieldName = "ownerContentId";
        	this.entityName = "Content";
        	this.roleEntityName = "ContentRole";
        	this.roleEntityIdName = "contentId";
        }
        
        public StdRelatedRoleGetter ( String entityName,  String roleTypeFieldName, String roleEntityIdName, String partyFieldName, String ownerEntityFieldName, String roleEntityName) {
            
        	this.roleTypeFieldName = roleTypeFieldName ;
        	this.partyFieldName = partyFieldName ;
        	this.ownerEntityFieldName = ownerEntityFieldName ;
        	this.entityName = entityName;
        	this.roleEntityName = roleEntityName;
        	this.roleEntityIdName = roleEntityIdName;
        }
        
        public StdRelatedRoleGetter ( Element getterElement) {
        
            this.roleTypeFieldName = getterElement.getAttribute("role-type-field-name");
            this.partyFieldName = getterElement.getAttribute("party-field-name");
            this.ownerEntityFieldName = getterElement.getAttribute("owner-entity-field-name");
            this.entityName = getterElement.getAttribute("entity-name");
            this.roleEntityName = getterElement.getAttribute("role-entity-name");
            this.roleEntityIdName = getterElement.getAttribute("entity-id-name");
        }
        
    	public List getList() {
            return this.roleIdList;
        }
        
    	public void clearList() {
            this.roleIdList = new ArrayList();
        }
        
    	public void setList(List lst) {
            this.roleIdList = lst;
        }
        
        public void init(GenericDelegator delegator, String entityId, String partyId, GenericValue entity) throws GenericEntityException {
            
            List lst = ContentWorker.getUserRolesFromList(delegator, UtilMisc.toList(entityId), partyId, this.roleEntityIdName, 
                                               this.partyFieldName, this.roleTypeFieldName, this.roleEntityName);
            this.roleIdList.addAll(lst);
            if (isOwner(entity, partyId)) {
                this.roleIdList.add("OWNER");   
            }
           return;
        }
        
        public void initWithAncestors(GenericDelegator delegator, GenericValue entity, String partyId) throws GenericEntityException {
            
           List ownedContentIdList = new ArrayList();
           ContentWorker.getEntityOwners(delegator, entity, ownedContentIdList, this.entityName, this.ownerEntityFieldName);
           if (ownedContentIdList.size() > 0) {
               List lst = ContentWorker.getUserRolesFromList(delegator, ownedContentIdList, partyId, this.roleEntityIdName, this.partyFieldName, this.roleTypeFieldName, this.roleEntityName);
               this.roleIdList.addAll(lst);
           }
           return;
        }
        
        public boolean isOwner( GenericValue entity, String targetPartyId) {
            boolean isOwner = false;
            if (entity == null || targetPartyId == null) {
                return false;   
            }
            GenericDelegator delegator = entity.getDelegator();
            ModelEntity modelEntity = delegator.getModelEntity(entityName);
            if (modelEntity.getField("createdByUserLogin") == null) {
                return false;
            }
            if ( entity.get("createdByUserLogin") != null) {
                String userLoginIdCB = (String)entity.get("createdByUserLogin");
                try {
                    GenericValue userLogin = delegator.findByPrimaryKeyCache("UserLogin", UtilMisc.toMap("userLoginId", userLoginIdCB ));
                    if (userLogin != null) {
                        String partyIdCB = userLogin.getString("partyId");
                        if (partyIdCB != null) {
                            if (partyIdCB.equals(targetPartyId)) {
                                isOwner = true;   
                            }
                        }
                    }
                } catch(GenericEntityException e) {
                	Debug.logInfo(e.getMessage() + " Returning false for 'isOwner'.", module);
                       
                }
            }
            return isOwner;
        }
        
        public String dumpAsText() {
             StringBuffer buf = new StringBuffer();
             buf.append("ROLES: ");
             if (roleIdList != null) {
                for (int i=0; i < roleIdList.size(); i++) {
                    String val = (String)roleIdList.get(i);
                    buf.append(val);
                    buf.append("  ");
                }
             }
             return buf.toString();
        }
    }
    
}
