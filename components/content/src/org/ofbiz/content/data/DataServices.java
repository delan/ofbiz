package org.ofbiz.content.data;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.GenericDelegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ModelService;
import org.ofbiz.service.ServiceUtil;

/**
 * DataServices Class
 *
 * @author     <a href="mailto:byersa@automationgroups.com">Al Byers</a>
 * @version    $Revision: 1.3 $
 * @since      3.0
 *
 * 
 */
public class DataServices {

    public static final String module = DataServices.class.getName();


    /**
     * A top-level service for creating a DataResource and ElectronicText together.
     */
    public static Map createDataResourceAndText(DispatchContext dctx, Map context) {
        Map result = new HashMap();
        GenericDelegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        context.put("entityOperation", "_CREATE");
        List targetOperations = new ArrayList();
        targetOperations.add("CREATE_CONTENT");
        context.put("targetOperationList", targetOperations);
        context.put("skipPermissionCheck", null);
        String permissionStatus = DataResourceWorker.callDataResourcePermissionCheck( delegator, dispatcher,
                                      context);
        if (permissionStatus != null && permissionStatus.equalsIgnoreCase("granted") ) {
            context.put("skipPermissionCheck", "granted");
            Map thisResult = createDataResourceMethod(dctx, context );
            if (thisResult.get(ModelService.RESPONSE_MESSAGE) != null) {
                return ServiceUtil.returnError((String)thisResult.get(ModelService.ERROR_MESSAGE));
            }
            result.put("dataResourceId", thisResult.get("dataResourceId"));
            context.put("dataResourceId", thisResult.get("dataResourceId"));

            String dataResourceTypeId = (String)context.get("dataResourceTypeId");
            if (dataResourceTypeId != null && dataResourceTypeId.equals("ELECTRONIC_TEXT") ) {
                thisResult = createElectronicText(dctx, context );
                if (thisResult.get(ModelService.RESPONSE_MESSAGE) != null) {
                    return ServiceUtil.returnError((String)thisResult.get(ModelService.ERROR_MESSAGE));
                }
            }
        }

        return result;
    }

    /**
     * A service wrapper for the createDataResourceMethod method.
     * Forces permissions to be checked.
     */
    public static Map createDataResource(DispatchContext dctx, Map context) {
        context.put("entityOperation", "_CREATE");
        List targetOperations = new ArrayList();
        targetOperations.add("CREATE_CONTENT");
        context.put("targetOperationList", targetOperations);
        context.put("skipPermissionCheck", null);
        Map result = createDataResourceMethod(dctx, context);
        return result;
    }

    public static Map createDataResourceMethod(DispatchContext dctx, Map context) {
        Map result = new HashMap();
        GenericDelegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        String permissionStatus = DataResourceWorker.callDataResourcePermissionCheck( delegator, dispatcher,
                                      context);
        if (permissionStatus != null && permissionStatus.equalsIgnoreCase("granted") ) {
            GenericValue userLogin = (GenericValue) context.get("userLogin"); 
            String userLoginId = (String)userLogin.get("userLoginId");
            String createdByUserLogin = userLoginId;
            String lastModifiedByUserLogin = userLoginId;
            Timestamp createdDate = UtilDateTime.nowTimestamp();
            Timestamp lastModifiedDate = UtilDateTime.nowTimestamp();
    
            // If textData exists, then create DataResource and return dataResourceId
            String dataResourceId = (String)context.get("dataResourceId");
            String dataResourceTypeId = (String)context.get("dataResourceTypeId");
            if (dataResourceId == null) dataResourceId = delegator.getNextSeqId("DataResource").toString();
            GenericValue dataResource = delegator.makeValue("DataResource", 
                                    UtilMisc.toMap("dataResourceId", dataResourceId));
            dataResource.setNonPKFields(context);
            dataResource.put("createdByUserLogin", createdByUserLogin);
            dataResource.put("lastModifiedByUserLogin", lastModifiedByUserLogin);
            dataResource.put("createdDate", createdDate);
            dataResource.put("lastModifiedDate", lastModifiedDate);
            try {
                dataResource.create();
            } catch(GenericEntityException e) {
                return ServiceUtil.returnError(e.getMessage());
            }
            result.put("dataResourceId", dataResourceId);
        }
        return result;
    }

    /**
     * A service wrapper for the createElectronicTextMethod method.
     * Forces permissions to be checked.
     */
    public static Map createElectronicText(DispatchContext dctx, Map context) {
        context.put("entityOperation", "_CREATE");
        List targetOperations = new ArrayList();
        targetOperations.add("CREATE_CONTENT");
        context.put("targetOperationList", targetOperations);
        context.put("skipPermissionCheck", null);
        Map result = createElectronicTextMethod(dctx, context);
        return result;
    }

    public static Map createElectronicTextMethod(DispatchContext dctx, Map context) {
        HashMap result = new HashMap();
        GenericDelegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        String permissionStatus = DataResourceWorker.callDataResourcePermissionCheck( delegator, dispatcher,
                                      context );
        if (permissionStatus != null && permissionStatus.equalsIgnoreCase("granted") ) {
            String dataResourceId = (String)context.get("dataResourceId");
            String textData = (String)context.get("textData");
            if (textData != null && textData.length() > 0 ) {
                GenericValue electronicText = delegator.makeValue("ElectronicText", 
                       UtilMisc.toMap("dataResourceId", dataResourceId, "textData", textData));
                try {
                    electronicText.create();
                } catch(GenericEntityException e) {
                    return ServiceUtil.returnError(e.getMessage());
                }
            }
        }
        
        return result;
    }



    /**
     * A top-level service for updating a DataResource and ElectronicText together.
     */
    public static Map updateDataResourceAndText(DispatchContext dctx, Map context) {
        Map result = new HashMap();
        GenericDelegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        context.put("entityOperation", "_UPDATE");
        List targetOperations = new ArrayList();
        targetOperations.add("UPDATE_CONTENT");
        context.put("targetOperationList", targetOperations);
        context.put("skipPermissionCheck", null);
        String permissionStatus = DataResourceWorker.callDataResourcePermissionCheck( delegator, dispatcher,
                                      context);
        if (permissionStatus != null && permissionStatus.equalsIgnoreCase("granted") ) {
            context.put("skipPermissionCheck", "granted");
            Map thisResult = createDataResourceMethod(dctx, context );
            if (thisResult.get(ModelService.RESPONSE_MESSAGE) != null) {
                return ServiceUtil.returnError((String)thisResult.get(ModelService.ERROR_MESSAGE));
            }
            context.put("dataResourceId", thisResult.get("dataResourceId"));

            String dataResourceTypeId = (String)context.get("dataResourceTypeId");
            if (dataResourceTypeId != null && dataResourceTypeId.equals("ELECTRONIC_TEXT") ) {
                thisResult = updateElectronicText(dctx, context );
                if (thisResult.get(ModelService.RESPONSE_MESSAGE) != null) {
                    return ServiceUtil.returnError((String)thisResult.get(ModelService.ERROR_MESSAGE));
                }
            }
        }

        return result;
    }


    /**
     * A service wrapper for the updateDataResourceMethod method.
     * Forces permissions to be checked.
     */
    public static Map updateDataResource(DispatchContext dctx, Map context) {
        context.put("entityOperation", "_CREATE");
        List targetOperations = new ArrayList();
        targetOperations.add("CREATE_CONTENT");
        context.put("targetOperationList", targetOperations);
        context.put("skipPermissionCheck", null);
        Map result = updateDataResourceMethod(dctx, context);
        return result;
    }

    public static Map updateDataResourceMethod(DispatchContext dctx, Map context) {
        Map result = new HashMap();
        GenericDelegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        GenericValue dataResource = null;
        Locale locale = (Locale)context.get("locale");
        String permissionStatus = DataResourceWorker.callDataResourcePermissionCheck( delegator, dispatcher,
                                      context);
        if (permissionStatus != null && permissionStatus.equalsIgnoreCase("granted") ) {
            GenericValue userLogin = (GenericValue) context.get("userLogin"); 
            String userLoginId = (String)userLogin.get("userLoginId");
            String lastModifiedByUserLogin = userLoginId;
            Timestamp lastModifiedDate = UtilDateTime.nowTimestamp();
    
            // If textData exists, then create DataResource and return dataResourceId
            String dataResourceId = (String)context.get("dataResourceId");
            try {
                dataResource = delegator.findByPrimaryKey("DataResource", 
                                       UtilMisc.toMap("dataResourceId", dataResourceId));
            } catch (GenericEntityException e) {
                Debug.logWarning(e, module);
                return ServiceUtil.returnError( "dataResource.update.read_failure" + e.getMessage());
            }

            dataResource.setNonPKFields(context);
            dataResource.put("lastModifiedByUserLogin", lastModifiedByUserLogin);
            dataResource.put("lastModifiedDate", lastModifiedDate);
            try {
                dataResource.store();
            } catch(GenericEntityException e) {
                return ServiceUtil.returnError(e.getMessage());
            }
        }
        return result;
    }

    /**
     * A service wrapper for the updateElectronicTextMethod method.
     * Forces permissions to be checked.
     */
    public static Map updateElectronicText(DispatchContext dctx, Map context) {
        context.put("entityOperation", "_UPDATE");
        List targetOperations = new ArrayList();
        targetOperations.add("UPDATE_CONTENT");
        context.put("targetOperationList", targetOperations);
        context.put("skipPermissionCheck", null);
        Map result = updateElectronicTextMethod(dctx, context);
        return result;
    }

    public static Map updateElectronicTextMethod(DispatchContext dctx, Map context) {
        HashMap result = new HashMap();
        GenericDelegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        GenericValue electronicText = null;
        Locale locale = (Locale)context.get("locale");
        String permissionStatus = DataResourceWorker.callDataResourcePermissionCheck( delegator, dispatcher,
                                      context );
        if (permissionStatus != null && permissionStatus.equalsIgnoreCase("granted") ) {
            String dataResourceId = (String)context.get("dataResourceId");
            String textData = (String)context.get("textData");
            if (textData != null && textData.length() > 0 ) {
                try {
                    electronicText = delegator.findByPrimaryKey("ElectronicText", 
                                       UtilMisc.toMap("dataResourceId", dataResourceId));
                    electronicText.put("textData", textData);
                    electronicText.store();
                } catch (GenericEntityException e) {
                    Debug.logWarning(e, module);
                    return ServiceUtil.returnError( "electronicText.update.read_failure" + e.getMessage());
                }
            }
        }
        
        return result;
    }


}
