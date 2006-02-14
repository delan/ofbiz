/*
 * $Id$
 * Copyright (c) 2003, 2004 The Open For Business Project - www.ofbiz.org
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

package org.ofbiz.manufacturing.mrp;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.GenericDelegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.manufacturing.bom.BOMNode;
import org.ofbiz.security.Security;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ModelService;
import org.ofbiz.service.ServiceUtil;

// TODO: 
//  Verificare il metodo: ProductHelper.isBuild(product)
//  Verificare il metodo ProposedOrder.calculateQuantityToSupply()
//  il metodo findProductMrpQoh() deve richiamare internamente un servizio
//  
/**
 * Services for running MRP
 *
 * @author     <a href=mailto:thierry.grauss@etu.univ-tours.fr">Thierry GRAUSS</a>
 * @version    $Rev$
 * @since      0.1
 */
public class MrpServices {
    
    public static final String module = MrpServices.class.getName();
    public static final String resource = "ManufacturingUiLabels";
    
    
    
    /**
     * Initialize the InventoryEventPlanned table.
     * <li>PreConditions : none</li>
     * <li>Result : The table InventoryEventPlannedForMRP is initialized</li>
     * <li>INPUT : Parameter to get from the context :</li><ul>
     * <li>Boolean reInitialize<br/>
     * if true : we must reinitialize the table, else we synchronize the table (not for the moment)</li></ul>
     *
     * <li>OUTPUT : Result to put in the map :</li><ul>
     * <li>none</li></ul>
     *
     * @param ctx The DispatchContext that this service is operating in.
     * @param context Map containing the input parameters.
     * @return Map with the result of the service, the output parameters.
     */
    
    public static Map initInventoryEventPlanned(DispatchContext ctx, Map context) {
        GenericDelegator delegator = ctx.getDelegator();
        LocalDispatcher dispatcher = ctx.getDispatcher();
        Security security = ctx.getSecurity();
        Timestamp now = UtilDateTime.nowTimestamp();
        Locale locale = (Locale) context.get("locale");
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        
        //Erases the old table 	for the moment and initializes it with the new orders,
        //Does not modify the old one now.
        Debug.logInfo("initInventoryEventPlanned called", module);
        
        List listResult = null;
        try{
            listResult = delegator.findAll("InventoryEventPlanned");
        } catch(GenericEntityException e) {
            Debug.logError(e,"Error : delegator.findAll(\"InventoryEventPlanned\")", module);
            return ServiceUtil.returnError("Problem, we can not find all the items of InventoryEventPlanned, for more detail look at the log");
        }
        if(listResult != null){
            try{
                delegator.removeAll(listResult);
            } catch(GenericEntityException e) {
                Debug.logError(e,"Error : delegator.removeAll(listResult), listResult ="+listResult, module);
                return ServiceUtil.returnError("Problem, we can not remove the InventoryEventPlanned items, for more detail look at the log");
            }
        }

        // MRP proposed requirements are deleted
        // TODO: is it correct?
        
        listResult = null;
        try{
            listResult = delegator.findByAnd("Requirement", UtilMisc.toMap("requirementTypeId", "MRP_PRO_PURCH_ORDER", "statusId", "REQ_CREATED"));
        } catch(GenericEntityException e) {
            return ServiceUtil.returnError("Problem, we can not find all the items of InventoryEventPlanned, for more detail look at the log");
        }
        if(listResult != null){
            try{
                delegator.removeAll(listResult);
            } catch(GenericEntityException e) {
                return ServiceUtil.returnError("Problem, we can not remove the InventoryEventPlanned items, for more detail look at the log");
            }
        }
        listResult = null;
        try{
            listResult = delegator.findByAnd("Requirement", UtilMisc.toMap("requirementTypeId", "MRP_PRO_PROD_ORDER", "statusId", "REQ_CREATED"));
        } catch(GenericEntityException e) {
            return ServiceUtil.returnError("Problem, we can not find all the items of InventoryEventPlanned, for more detail look at the log");
        }
        if(listResult != null){
            try{
                delegator.removeAll(listResult);
            } catch(GenericEntityException e) {
                return ServiceUtil.returnError("Problem, we can not remove the InventoryEventPlanned items, for more detail look at the log");
            }
        }

        GenericValue genericResult = null;
        Map parameters = null;
        List resultList = null;
        Iterator iteratorResult = null;
        // ----------------------------------------
        // Loads all the approved sales order items and purchase order items
        // ----------------------------------------
        resultList = null;
        iteratorResult = null;
        parameters = UtilMisc.toMap("orderTypeId", "SALES_ORDER", "itemStatusId", "ITEM_APPROVED");
        try {
            resultList = delegator.findByAnd("OrderHeaderAndItems", parameters);
        } catch(GenericEntityException e) {
            Debug.logError(e, "Error : delegator.findByAnd(\"OrderItem\", parameters\")", module);
            Debug.logError(e, "Error : parameters = "+parameters,module);
            return ServiceUtil.returnError("Problem, we can not find the order items, for more detail look at the log");
        }
        iteratorResult = resultList.iterator();
        while(iteratorResult.hasNext()){
            genericResult = (GenericValue) iteratorResult.next();
            String productId =  genericResult.getString("productId");
            Double eventQuantityTmp = new Double(-1.0 * genericResult.getDouble("quantity").doubleValue());
            Timestamp estimatedShipDate = genericResult.getTimestamp("estimatedDeliveryDate");
            if (estimatedShipDate == null) {
                estimatedShipDate = now;
            }
            parameters = UtilMisc.toMap("productId", productId, "eventDate", estimatedShipDate, "inventoryEventPlanTypeId", "SALE_ORDER_SHIP");
            try {
                InventoryEventPlannedServices.createOrUpdateInventoryEventPlanned(parameters, eventQuantityTmp, delegator);
            } catch (GenericEntityException e) {
                return ServiceUtil.returnError("Problem initializing the InventoryEventPlanned entity (SALE_ORDER_SHIP)");
            }
        }
        
        // ----------------------------------------
        // Loads all the approved purchase order items
        // ----------------------------------------
        resultList = null;
        iteratorResult = null;
        parameters = UtilMisc.toMap("orderTypeId", "PURCHASE_ORDER", "itemStatusId", "ITEM_APPROVED");
        try {
            resultList = delegator.findByAnd("OrderHeaderAndItems", parameters);
        } catch(GenericEntityException e) {
            Debug.logError(e, "Error : delegator.findByAnd(\"OrderItem\", parameters\")", module);
            Debug.logError(e, "Error : parameters = "+parameters,module);
            return ServiceUtil.returnError("Problem, we can not find the order items, for more detail look at the log");
        }
        iteratorResult = resultList.iterator();
        while(iteratorResult.hasNext()){
            genericResult = (GenericValue) iteratorResult.next();
            String productId =  genericResult.getString("productId");
            Double eventQuantityTmp = new Double(genericResult.getDouble("quantity").doubleValue());
            Timestamp estimatedShipDate = genericResult.getTimestamp("estimatedShipDate");
            if (estimatedShipDate == null) {
                estimatedShipDate = now;
            }
            
            parameters = UtilMisc.toMap("productId", productId, "eventDate", estimatedShipDate, "inventoryEventPlanTypeId", "PUR_ORDER_RECP");
            try {
                InventoryEventPlannedServices.createOrUpdateInventoryEventPlanned(parameters, eventQuantityTmp, delegator);
            } catch (GenericEntityException e) {
                return ServiceUtil.returnError("Problem initializing the InventoryEventPlanned entity (PUR_ORDER_RECP)");
            }
        }
        // ----------------------------------------
        // PRODUCTION Run: components
        // ----------------------------------------
        resultList = null;
        iteratorResult = null;
        parameters = UtilMisc.toMap("workEffortGoodStdTypeId", "PRUNT_PROD_NEEDED", "statusId", "WEGS_CREATED");
        try {
            resultList = delegator.findByAnd("WorkEffortAndGoods", parameters);
        } catch(GenericEntityException e) {
            Debug.logError(e, "Error : delegator.findByAnd(\"OrderItem\", parameters\")", module);
            Debug.logError(e, "Error : parameters = "+parameters,module);
            return ServiceUtil.returnError("Problem, we can not find the order items, for more detail look at the log");
        }
        iteratorResult = resultList.iterator();
        while(iteratorResult.hasNext()){
            genericResult = (GenericValue) iteratorResult.next();
            String productId =  genericResult.getString("productId");
            Double eventQuantityTmp = new Double(-1.0 * genericResult.getDouble("estimatedQuantity").doubleValue());
            Timestamp estimatedShipDate = genericResult.getTimestamp("estimatedStartDate");
            if (estimatedShipDate == null) {
                estimatedShipDate = now;
            }
            
            parameters = UtilMisc.toMap("productId", productId, "eventDate", estimatedShipDate, "inventoryEventPlanTypeId", "MANUF_ORDER_REQ");
            try {
                InventoryEventPlannedServices.createOrUpdateInventoryEventPlanned(parameters, eventQuantityTmp, delegator);
            } catch (GenericEntityException e) {
                return ServiceUtil.returnError("Problem initializing the InventoryEventPlanned entity (MRP_REQUIREMENT)");
            }
        }
        
        // ----------------------------------------
        // PRODUCTION Run: product produced
        // ----------------------------------------
        resultList = null;
        iteratorResult = null;
        parameters = UtilMisc.toMap("workEffortGoodStdTypeId", "PRUN_PROD_DELIV", "statusId", "WEGS_CREATED", "workEffortTypeId", "PROD_ORDER_HEADER");
        try {
            resultList = delegator.findByAnd("WorkEffortAndGoods", parameters);
        } catch(GenericEntityException e) {
            Debug.logError(e, "Error : delegator.findByAnd(\"OrderItem\", parameters\")", module);
            Debug.logError(e, "Error : parameters = "+parameters,module);
            return ServiceUtil.returnError("Problem, we can not find the order items, for more detail look at the log");
        }
        iteratorResult = resultList.iterator();
        while(iteratorResult.hasNext()){
            genericResult = (GenericValue) iteratorResult.next();
            if ("PRUN_CLOSED".equals(genericResult.getString("currentStatusId"))) {
                continue;
            }
            Double qtyToProduce = genericResult.getDouble("quantityToProduce");
            if (qtyToProduce == null) {
                qtyToProduce = new Double(0);
            }
            Double qtyProduced = genericResult.getDouble("quantityProduced");
            if (qtyProduced == null) {
                qtyProduced = new Double(0);
            }
            if (qtyProduced.compareTo(qtyToProduce) >= 0) {
                continue;
            }
            double qtyDiff = qtyToProduce.doubleValue() - qtyProduced.doubleValue();
            String productId =  genericResult.getString("productId");
            Double eventQuantityTmp = new Double(qtyDiff);
            Timestamp estimatedShipDate = genericResult.getTimestamp("estimatedCompletionDate");
            if (estimatedShipDate == null) {
                estimatedShipDate = now;
            }
            
            parameters = UtilMisc.toMap("productId", productId, "eventDate", estimatedShipDate, "inventoryEventPlanTypeId", "MANUF_ORDER_RECP");
            try {
                InventoryEventPlannedServices.createOrUpdateInventoryEventPlanned(parameters, eventQuantityTmp, delegator);
            } catch (GenericEntityException e) {
                return ServiceUtil.returnError("Problem initializing the InventoryEventPlanned entity (MANUF_ORDER_RECP)");
            }
        }

        
        Map result = new HashMap();
        result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_SUCCESS);
        Debug.logInfo("return from initInventoryEventPlanned", module);
        return result;
    }
    
/*    
    public static Map initInventoryEventPlanned(DispatchContext ctx, Map context) {
        GenericDelegator delegator = ctx.getDelegator();
        LocalDispatcher dispatcher = ctx.getDispatcher();
        Security security = ctx.getSecurity();
        Timestamp now = UtilDateTime.nowTimestamp();
        Locale locale = (Locale) context.get("locale");
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        
        //Erases the old table 	for the moment and initializes it with the new orders,
        //Does not modify the old one now.
        Debug.logInfo("initInventoryEventPlanned called", module);
        
        Map parameters = null;
        List listResult = null;
        try{
            listResult = delegator.findAll("InventoryEventPlanned");
        } catch(GenericEntityException e) {
            Debug.logError(e,"Error : delegator.findAll(\"InventoryEventPlanned\")", module);
            return ServiceUtil.returnError("Problem, we can not find all the items of InventoryEventPlanned, for more detail look at the log");
        }
        if(listResult != null){
            try{
                delegator.removeAll(listResult);
            } catch(GenericEntityException e) {
                Debug.logError(e,"Error : delegator.removeAll(listResult), listResult ="+listResult, module);
                return ServiceUtil.returnError("Problem, we can not remove the InventoryEventPlanned items, for more detail look at the log");
            }
        }
        
        GenericValue genericResult = null;
        List resultList = null;
        parameters = UtilMisc.toMap("statusId", "ITEM_APPROVED");
        try {
            resultList = delegator.findByAnd("OrderItem", parameters);
        } catch(GenericEntityException e) {
            Debug.logError(e, "Error : delegator.findByAnd(\"OrderItem\", parameters\")", module);
            Debug.logError(e, "Error : parameters = "+parameters,module);
            return ServiceUtil.returnError("Problem, we can not find the order items, for more detail look at the log");
        }
        Iterator iteratorResult = resultList.iterator();
        while(iteratorResult.hasNext()){
            genericResult = (GenericValue) iteratorResult.next();
            String productId =  genericResult.getString("productId");
            Double eventQuantityTmp = new Double(-1.0 * genericResult.getDouble("quantity").doubleValue());
            Timestamp estimatedShipDate = genericResult.getTimestamp("estimatedShipDate");
            if (estimatedShipDate == null) estimatedShipDate =now;
            
            
            parameters = UtilMisc.toMap("productId",productId,"eventDate",estimatedShipDate,"inventoryEventPlanTypeId","SALE_ORDER_SHIP");
            //-------------------------------------------------------
            GenericValue tempInventoryEventPlanned = delegator.makeValue("InventoryEventPlanned", parameters);
            //tempInventoryEventPlanned.setNonPKFields(context);
            
            genericResult = null;
            try {
                genericResult = delegator.findByPrimaryKey("InventoryEventPlanned", parameters);
            } catch (GenericEntityException e) {
                Debug.logError(e,"Error : delegator.findByPrimaryKey(\"InventoryEventPlanned\", parameters)", module);
                Debug.logError(e,"Error : parameters = "+parameters,module);
                return ServiceUtil.returnError("Problem, we can not find the products, for more detail look at the log");
            }
            if(genericResult==null){
                tempInventoryEventPlanned.put("eventQuantity", eventQuantityTmp);
                try {
                    tempInventoryEventPlanned.create();
                } catch (GenericEntityException e) {
                    Debug.logError(e,"Error : InventoryEventPlanned.create()", module);
                    Debug.logError(e,"Error : parameters = "+tempInventoryEventPlanned, module);
                    return ServiceUtil.returnError("Problem, we can not create the product in InventoryEventPlanned, for more detail look at the log");
                }
            } else{
                double qties = eventQuantityTmp.doubleValue() - ((Double)genericResult.get("eventQuantity")).doubleValue();
                tempInventoryEventPlanned.put("eventQuantity", new Double(qties));
                try {
                    tempInventoryEventPlanned.store();
                } catch (GenericEntityException e) {
                    Debug.logError(e,"Error : InventoryEventPlanned.store()", module);
                    Debug.logError(e,"Error : parameters = "+tempInventoryEventPlanned, module);
                    return ServiceUtil.returnError("Problem, we can not update the product in InventoryEventPlanned, for more detail look at the log");
                }
            }
            //------------------------------------------------
        }
        
        Map result = new HashMap();
        result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_SUCCESS);
        Debug.logInfo("return from initInventoryEventPlanned", module);
        return result;
    }
*/    
    /**
     * Create a List  with all the event of InventotyEventPlanned for one billOfMaterialLevel, sorted by productId and eventDate.
     *
     * <li>INPUT : Parameter to get from the context : </li><ul>
     * <li>Integer billOfMaterialLevel : 0 for root for more detail see BomHelper.getMaxDepth</li></ul>
     *
     * <li>OUTPUT : Result to put in the map :</li><ul>
     * <li>List listInventoryEventForMRP : all the event of InventotyEventPlanned for one billOfMaterialLevel, sorted by productId and eventDate<br/>
     * @param ctx The DispatchContext that this service is operating in.
     * @param context Map containing the input parameters.
     * @return Map with the result of the service, the output parameters.
     */
    public static Map listProductForMrp(DispatchContext ctx, Map context) {
        Debug.logInfo("listProductForMrp called", module);
        // read parameters from context
        GenericDelegator delegator = ctx.getDelegator();
        Security security = ctx.getSecurity();
        Locale locale = (Locale) context.get("locale");
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        Long billOfMaterialLevel = (Long) context.get("billOfMaterialLevel");
        
        // Find all products in MrpInventoryEventPlanned, ordered by bom and eventDate
        List listResult = null;
        // If billOfMaterialLevel == 0 the search must be done with (billOfMaterialLevel == 0 || billOfMaterialLevel == null)
        EntityCondition parameters = null;
        if (billOfMaterialLevel.intValue() == 0) {
            parameters = new EntityExpr(new EntityExpr("billOfMaterialLevel", EntityOperator.EQUALS, null),
                                        EntityOperator.OR,
                                        new EntityExpr("billOfMaterialLevel", EntityOperator.EQUALS, billOfMaterialLevel));
        } else {
            parameters = new EntityExpr("billOfMaterialLevel", EntityOperator.EQUALS, billOfMaterialLevel);
        }

        List orderBy = UtilMisc.toList("productId", "eventDate");
        try{
            //listResult = delegator.findByAnd("MrpInventoryEventPlanned", parameters, orderBy);
            listResult = delegator.findByCondition("MrpInventoryEventPlanned", parameters, null, orderBy);
        } catch(GenericEntityException e) {
            Debug.logError(e, "Error : delegator.findByCondition(\"MrpInventoryEventPlanned\", parameters, null, orderBy)", module);
            Debug.logError(e, "Error : parameters = "+parameters,module);
            Debug.logError(e, "Error : orderBy = "+orderBy,module);
            return ServiceUtil.returnError("Problem, we can not find the products, for more detail look at the log");
        }
        Map result = new HashMap();
        result.put("listInventoryEventForMrp",listResult);
        result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_SUCCESS);
        Debug.logInfo("return from listProductForMrp "+billOfMaterialLevel, module);
        return result;
    }
    
    /**
     * Find the quantity on hand of products for MRP.
     * <li>PreConditions :	none</li>
     * <li>Result : We get the quantity of product available in the stocks.</li>
     *
     * @param product the product for which the Quantity Available is required
     * @return the sum of all the totalAvailableToPromise of the inventoryItem related to the product, if the related facility is Mrp available (not yet implemented!!)
     */
    public static double findProductMrpQoh(GenericValue product, LocalDispatcher dispatcher) {
        List orderBy = UtilMisc.toList("facilityId", "-receivedDate", "-inventoryItemId");
        Map resultMap = null;
        try{
            resultMap = dispatcher.runSync("getProductInventoryAvailable", UtilMisc.toMap("productId", product.getString("productId")));
            // TODO: aggiungere facilityId come argomento ed usare il seguente
            //resultMap = dispatcher.runSync("getProductInventoryAvailableByFacility", UtilMisc.toMap("productId", product.getString("productId"), "facilityId", facilityId));
        } catch (GenericServiceException e) {
            Debug.logError(e, "Error calling getProductInventoryAvailableByFacility service", module);
            return 0;
        }
        return ((Double)resultMap.get("quantityOnHandTotal")).doubleValue();
    }
    /*
    public static double findProductMrpQoh(GenericValue product) {
        Debug.logInfo("findProductMrpQoh called", module);
        
        //TODO : verify if the facility is really MRP available !!!!!!!!!!!!!!!
        List productInventoryItems = null;
        //Lists all stocks where the product is defined
        List orderBy = UtilMisc.toList("facilityId", "-receivedDate", "-inventoryItemId");
        try{
            productInventoryItems = product.getRelated("InventoryItem", null, orderBy);
        } catch (GenericEntityException e) {
            Debug.logError(e, "Error : product.getRelated(\"InventoryItem\")", module);
            Debug.logError(e, "Error : orderBy = "+orderBy,module);
            return 0;
        }
        //loop through the inventory items and get totals of available to promise per facility
        Iterator productInventoryItemIter = productInventoryItems.iterator();
        double totalAvailableToPromise = 0.0;
        while (productInventoryItemIter.hasNext()) {
            GenericValue productInventoryItem = (GenericValue) productInventoryItemIter.next();
            GenericValue facility = null;
            try{
                facility = productInventoryItem.getRelatedOneCache("Facility");
            } catch (GenericEntityException e) {
                Debug.logError(e, "Error : productInventoryItem.getRelatedCache(\"Facility\")", module);
                return 0;
            }
            if(facility!=null){
                if ("SERIALIZED_INV_ITEM".equals(productInventoryItem.getString("inventoryItemTypeId"))) {
                    if ("INV_PROMISED".equals(productInventoryItem.getString("statusId"))) {
                        totalAvailableToPromise += 1;
                    }
                } else if ("NON_SERIAL_INV_ITEM".equals(productInventoryItem.getString("inventoryItemTypeId"))) {
                    if (productInventoryItem.get("availableToPromiseTotal") != null) {
                        totalAvailableToPromise += productInventoryItem.getDouble("availableToPromiseTotal").doubleValue();
                    }
                }
            }
        }
        Debug.logInfo("return from findProductMrpQoh "+product.getString("productId"), module);
        return totalAvailableToPromise;
    }
    */
    
    /**
     * Process the bill of material (bom) of the product  to insert components in the InventoryEventPlanned table.
     *   Before inserting in the entity, test if there is the record already existing to add quantity rather to create a new one.
     *
     * @param product
     * @param eventQuantity the product quantity needed
     *  @param startDate the startDate of the productionRun which will used to produce the product
     *  @param routingTaskStartDate Map with all the routingTask as keys and startDate of each of them
     * @return None
     */
    
    public static void processBomComponent(GenericValue product, double eventQuantity, Timestamp startDate, Map routingTaskStartDate, List listComponent) {
        // TODO : change the return type to boolean to be able to test if all is ok or if it have had a exception
        GenericDelegator delegator = product.getDelegator();

        if (listComponent != null && listComponent.size() >0) {
            Iterator listComponentIter = listComponent.iterator();
            while (listComponentIter.hasNext()) {
                BOMNode node = (BOMNode) listComponentIter.next();
                GenericValue productComponent = node.getProductAssoc();
                // read the startDate for the component
                String routingTask = node.getProductAssoc().getString("routingWorkEffortId");
                Timestamp eventDate = (routingTask == null || !routingTaskStartDate.containsKey(routingTask)) ? startDate : (Timestamp) routingTaskStartDate.get(routingTask);
                // if the components is valid at the event Date create the Mrp requirement in the InventoryEventPlanned entity
                if (EntityUtil.isValueActive(productComponent, eventDate)) {
                    //Map parameters = UtilMisc.toMap("productId", productComponent.getString("productIdTo"));
                    Map parameters = UtilMisc.toMap("productId", node.getProduct().getString("productId"));
                    parameters.put("eventDate", eventDate);
                    parameters.put("inventoryEventPlanTypeId", "MRP_REQUIREMENT");
                    double componentEventQuantity = node.getQuantity();
                    try {
                        InventoryEventPlannedServices.createOrUpdateInventoryEventPlanned(parameters, new Double(-1.0 * componentEventQuantity), delegator);
                    } catch (GenericEntityException e) {
                        Debug.logError("Error : delegator.findByPrimaryKey(\"InventoryEventPlanned\", parameters) ="+parameters+"--"+e.getMessage(), module);
                    }
                }
            }
        }
        return;
    }
    
    /**
     * Launch the MRP.
     * <li>PreConditions : none</li>
     * <li>Result : The date when we must order or begin to build the products and subproducts we need are calclated</li>
     *
     * <li>INPUT : parameters to get from the context :</li><ul>
     * <li>String mrpName</li></ul>
     *
     * <li>OUTPUT : Result to put in the map :</li><ul>
     * <li>none</li></ul>
     * @param ctx The DispatchContext that this service is operating in.
     * @param context Map containing the input parameters, productId routingId, quantity, startDate.
     * @return Map with the result of the service, the output parameters.
     */
    public static Map runningMrp(DispatchContext ctx, Map context) {
        Debug.logInfo("runningMrp called", module);
        //Context
        GenericDelegator delegator = ctx.getDelegator();
        LocalDispatcher dispatcher = ctx.getDispatcher();
        Security security = ctx.getSecurity();
        Locale locale = (Locale) context.get("locale");
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        
        String mrpName = (String)context.get("mrpName");
        String facilityId = (String)context.get("facilityId");
        // Variable declaration
        int bomLevelWithNoEvent = 0;
        double stockTmp = 0;
        String oldProductId = null;
        String productId = null;
        GenericValue product = null;
        GenericValue productFacility = null;
        double eventQuantity = 0;
        Timestamp eventDate = null;
        boolean isNegative = false;
        double quantityNeeded = 0;
        double reorderQuantity = 0;
        double minimumStock = 0;
        int daysToShip = 0;
        List components = null;
        boolean isbuild = false;
        GenericValue routing = null;
        
        Map result = null;
        Map parameters = null;
        List listInventoryEventForMRP = null;
        ListIterator iteratorListInventoryEventForMRP = null;
        GenericValue inventoryEventForMRP = null;
        
        // Initialisation of the InventoryEventPlanned table, This table will contain the products we want to buy or build.
        parameters = UtilMisc.toMap("reInitialize",new Boolean(true),"userLogin", userLogin);
        try {
            result = dispatcher.runSync("initInventoryEventPlanned", parameters);
        } catch (GenericServiceException e) {
            Debug.logError("Error : initInventoryEventPlanned", module);
            Debug.logError("Error : parameters = "+parameters,module);
            return ServiceUtil.returnError("Problem, can not initialise the table InventoryEventPlanned, for more detail look at the log");
        }
        // TODO : modifier le jeux d'essai de TGR pour mettre 0 au niveau pf
        long bomLevel = 0;
        // iteration for the bomLevel for which there are some events
        do {
            //get the products from the InventoryEventPlanned table for the current billOfMaterialLevel (ie. BOM)
            parameters = UtilMisc.toMap("billOfMaterialLevel", new Long(bomLevel), "userLogin", userLogin);
            try {
                result = dispatcher.runSync("listProductForMrp", parameters);
            } catch (GenericServiceException e) {
                Debug.logError("Error : listProductForMrp, parameters ="+parameters, module);
                return ServiceUtil.returnError("Problem, can not list the products for the MRP, for more detail look at the log");
            }
            listInventoryEventForMRP = (List) result.get("listInventoryEventForMrp");
            
            if (listInventoryEventForMRP != null && listInventoryEventForMRP.size()>0) {
                bomLevelWithNoEvent = 0;
                iteratorListInventoryEventForMRP = listInventoryEventForMRP.listIterator();
                
                oldProductId = "";
                while (iteratorListInventoryEventForMRP.hasNext()) {
                    inventoryEventForMRP = (GenericValue) iteratorListInventoryEventForMRP.next();
                    productId = (String) inventoryEventForMRP.getString("productId");
                    eventQuantity = inventoryEventForMRP.getDouble("eventQuantity").doubleValue();

                    if (!productId.equals(oldProductId)) {
                        double positiveEventQuantity = (eventQuantity > 0? eventQuantity: -1 * eventQuantity);
                        // It's a new product, so it's necessary to  read the MrpQoh
                        try {
                            product = inventoryEventForMRP.getRelatedOneCache("Product");
                            productFacility = (GenericValue)EntityUtil.getFirst(product.getRelatedByAndCache("ProductFacility", UtilMisc.toMap("facilityId", facilityId)));
                        } catch (GenericEntityException e) {
                            return ServiceUtil.returnError("Problem, can not find the product for a event, for more detail look at the log");
                        }
                        stockTmp = findProductMrpQoh(product, dispatcher);
                        if (productFacility != null) {
                            reorderQuantity = (productFacility.getDouble("reorderQuantity") != null? productFacility.getDouble("reorderQuantity").doubleValue(): -1);
                            minimumStock = (productFacility.getDouble("minimumStock") != null? productFacility.getDouble("minimumStock").doubleValue(): 0);
                            daysToShip = (productFacility.getLong("daysToShip") != null? productFacility.getLong("daysToShip").intValue(): 0);
                        } else {
                            minimumStock = 0;
                            daysToShip = 0;
                            reorderQuantity = -1;
                        }
                        // -----------------------------------------------------
                        // The components are also loaded thru the configurator
                        Map serviceResponse = null;
                        try {
                            serviceResponse = dispatcher.runSync("getManufacturingComponents", UtilMisc.toMap("productId", product.getString("productId"), "quantity", new Double(positiveEventQuantity), "userLogin", userLogin));
                        } catch (Exception e) {
                            return ServiceUtil.returnError("Problem, can not find the product for a event, for more detail look at the log");
                        }
                        components = (List)serviceResponse.get("components");
                        if (components != null && components.size() > 0) {
                            BOMNode node = ((BOMNode)components.get(0)).getParentNode();
                            isbuild = node.isManufactured();
                        } else {
                            isbuild = false;
                        }
                        // #####################################################

                        oldProductId = productId;
                    }
                    
                    stockTmp = stockTmp + eventQuantity;
                    if(stockTmp < minimumStock){
                        double qtyToStock = minimumStock - stockTmp;
                        //need to buy or build the product as we have not enough stock
                        eventDate = inventoryEventForMRP.getTimestamp("eventDate");
                        // to be just before the requirement
                        eventDate.setTime(eventDate.getTime()-1);
                        ProposedOrder proposedOrder = new ProposedOrder(product, facilityId, isbuild, eventDate, qtyToStock);
                        proposedOrder.setMrpName(mrpName);
                        // calculate the ProposedOrder quantity and update the quantity object property.
                        proposedOrder.calculateQuantityToSupply(reorderQuantity, iteratorListInventoryEventForMRP);
                        
                        // -----------------------------------------------------
                        // The components are also loaded thru the configurator
                        Map serviceResponse = null;
                        try {
                            serviceResponse = dispatcher.runSync("getManufacturingComponents", UtilMisc.toMap("productId", product.getString("productId"), "quantity", new Double(proposedOrder.getQuantity())));
                        } catch (Exception e) {
                            return ServiceUtil.returnError("Problem, can not find the product for a event, for more detail look at the log");
                        }
                        components = (List)serviceResponse.get("components");
                        String routingId = (String)serviceResponse.get("workEffortId");
                        if (routingId != null) {
                            try {
                                routing = delegator.findByPrimaryKey("WorkEffort", UtilMisc.toMap("workEffortId", routingId));
                            } catch (GenericEntityException e) {
                                return ServiceUtil.returnError("Problem, can not find the product for a event, for more detail look at the log");
                            }
                        } else {
                            routing = null;
                        }
                        if (components != null && components.size() > 0) {
                            BOMNode node = ((BOMNode)components.get(0)).getParentNode();
                            isbuild = node.isManufactured();
                        } else {
                            isbuild = false;
                        }
                        // #####################################################
                        
                        // calculate the ProposedOrder requirementStartDate and update the requirementStartDate object property.
                        Map routingTaskStartDate = proposedOrder.calculateStartDate(daysToShip, routing, delegator, dispatcher, userLogin);
                        if (isbuild) {
                            // process the product components
                            processBomComponent(product, proposedOrder.getQuantity(), proposedOrder.getRequirementStartDate(), routingTaskStartDate, components);
                        }
                        // create the  ProposedOrder (only if the product is warehouse managed), and the InventoryEventPlanned associated
                        if (productFacility != null) {
                            String proposedOrderId = proposedOrder.create(ctx, userLogin);
                        }
                        Map eventMap = UtilMisc.toMap("productId", product.getString("productId"),
                                                      "eventDate", eventDate,
                                                      "inventoryEventPlanTypeId", (isbuild? "PROP_MANUF_O_RECP" : "PROP_PUR_O_RECP"));
                        try {
                            InventoryEventPlannedServices.createOrUpdateInventoryEventPlanned(eventMap, new Double(proposedOrder.getQuantity()), delegator);
                        } catch (GenericEntityException e) {
                            return ServiceUtil.returnError("Problem running createOrUpdateInventoryEventPlanned");
                        }
                        //
                        stockTmp = stockTmp + proposedOrder.getQuantity();
                    }
                }
            } else {
                bomLevelWithNoEvent += 1;
            }
            
            bomLevel += 1;
            // if there are 3 levels with no inventoryEvenPanned we stop
        } while (bomLevelWithNoEvent < 3);
        
        result =  new HashMap();
        List msgResult = new LinkedList();
        result.put("msgResult",msgResult);
        result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_SUCCESS);
        Debug.logInfo("return from runningMrp", module);
        return result;
    }
}
