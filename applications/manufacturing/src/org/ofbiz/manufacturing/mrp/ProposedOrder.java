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
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.manufacturing.jobshopmgt.ProductionRun;
import org.ofbiz.manufacturing.techdata.ProductHelper;
import org.ofbiz.manufacturing.techdata.TechDataServices;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;


/**
 * Proposed Order Object generated by the MRP process or other re-Order process
 *
 * @author     <a href="mailto:olivier.heintz@nereide.biz">Olivier Heintz</a>
 * @author     <a href=mailto:thierry.grauss@etu.univ-tours.fr">Thierry GRAUSS</a>
 * @version    $Rev$
 * @since      3.0
 */
public class ProposedOrder {
    
    public static final String module = ProposedOrder.class.getName();
    public static final String resource = "ManufacturingUiLabels";
    
    protected GenericValue product;
    protected boolean isbuild;
    protected String productId;
    protected String facilityId;
    protected String mrpName;
    protected Timestamp requiredByDate;
    protected Timestamp requirementStartDate;
    protected double quantity;
    
    
    public ProposedOrder(GenericValue product, String facilityId, boolean isbuild, Timestamp requiredByDate, double quantity) {
        this.product = product;
        this.productId = product.getString("productId");
        this.facilityId = facilityId;
        this.isbuild = isbuild;
        this.requiredByDate = requiredByDate;
        this.quantity = quantity;
        this.requirementStartDate = null;
    }
    /**
     * get the quantity property.
     * @return the quantity property
     **/
    public double getQuantity(){
        return quantity;
    }
    /**
     * get the requirementStartDate property.
     * @return the quantity property
     **/
    public Timestamp getRequirementStartDate(){
        return requirementStartDate;
    }
    /**
     * calculate the ProposedOrder requirementStartDate and update the requirementStartDate property.
     * <li>For the build product, <ul>
     *         <li>read the routing associated to the product,
     *         <li>read the routingTask associated to the routing
     *         <li> step by step calculate from the endDate the startDate</ul>
     * <li>For the bought product, the first ProductFacility.daysToShip is used to calculated the startDate
     * @return <ul>
     * <li>if ProposedOrder.isBuild a Map with all the routingTaskId as keys and estimatedStartDate as value.
     * <li>else null.
     **/
    public Map calculateStartDate(int daysToShip, GenericValue routing, LocalDispatcher dispatcher){
        Map result = null;
        Timestamp endDate = (Timestamp) requiredByDate.clone();
        Timestamp startDate = endDate;
        if (isbuild) {
            if (routing == null) {
                routing = ProductHelper.getRouting(product, quantity, requiredByDate, dispatcher);
            }
            if (routing != null) {
                //Looks for all the routingTask (ordered by inversed (begin from the end) sequence number)
                List listRoutingTaskAssoc = null;
                try{
                    listRoutingTaskAssoc = routing.getRelatedCache("FromWorkEffortAssoc",UtilMisc.toMap("workEffortAssocTypeId","ROUTING_COMPONENT"), UtilMisc.toList("sequenceNum DESC"));
                } catch (GenericEntityException e) {
                    Debug.logError("Error : routing.getRelatedCache('FromWorkEffort',UtilMisc.toMap('workEff..  error="+e, module);
                    listRoutingTaskAssoc = null;
                }
                // Iterate for all the routingTask, check if it's a valid routingTask, and step by step calculate the startDate
                Iterator  listIterRTA = listRoutingTaskAssoc.iterator();
                result = new HashMap();
                while (listIterRTA.hasNext()) {
                    GenericValue routingTaskAssoc = (GenericValue) listIterRTA.next();
                    if (EntityUtil.isValueActive(routingTaskAssoc,endDate)) {
                        GenericValue routingTask = null;
                        try {
                            routingTask = routingTaskAssoc.getRelatedOneCache("ToWorkEffort");
                        } catch (GenericEntityException e) {
                            Debug.logError(e.getMessage(),  module);
                        }
                        // Calculate the estimatedStartDate
                        long totalTime = ProductionRun.getEstimatedTaskTime(routingTask, quantity, dispatcher);
                        startDate = TechDataServices.addBackward(TechDataServices.getTechDataCalendar(routingTask),endDate, totalTime);
                        // record the routingTask with the startDate associated
                        result.put(routingTask.getString("workEffortId"),startDate);
                        endDate = startDate;
                    }
                }
            } else { 
                // routing is null
                // TODO : write an error message for the endUser to say "Build product without routing"
                Debug.logError("Build product without routing for product = "+product.getString("productId"), module);
            }
        } else {
            // the product is purchased
            // TODO: REVIEW this code
            long duringTime = daysToShip * 8 * 60 * 60 * 1000;
            try {
                GenericValue techDataCalendar = product.getDelegator().findByPrimaryKeyCache("TechDataCalendar",UtilMisc.toMap("calendarId", "SUPPLIER"));
                startDate = TechDataServices.addBackward(techDataCalendar, endDate, duringTime);
            } catch(GenericEntityException e) {
                Debug.logError(e, "Error : reading SUPPLIER TechDataCalendar"+"--"+e.getMessage(), module);
            }
        }
        requirementStartDate = startDate;
        return result;
    }
    
    
    /**
     * calculate the ProposedOrder quantity and update the quantity property.
     * Read the first ProductFacility.reorderQuantity and calculate the quantity : if (quantity < reorderQuantity) quantity = reorderQuantity;
     **/
    // FIXME: facilityId
    public void calculateQuantityToSupply(double reorderQuantity, ListIterator  listIterIEP){
        //      TODO : use a better algorithm using Order management cost et Product Stock cost to calculate the re-order quantity
        //                     the variable listIterIEP will be used for that
        if (quantity < reorderQuantity) {
            quantity = reorderQuantity;
        }
    }
    
    /**
     * create a ProposedOrder in the Requirement Entity calling the createRequirement service.
     * @param ctx The DispatchContext used to call service to create the Requirement Entity record.
     * @return String the requirementId
     **/
    public String create(DispatchContext ctx, GenericValue userLogin) {
        LocalDispatcher dispatcher = ctx.getDispatcher();
        Map parameters = UtilMisc.toMap("userLogin", userLogin);
        
        parameters.put("productId", productId);
        parameters.put("facilityId", facilityId);
        parameters.put("requiredByDate", requiredByDate);
        parameters.put("requirementStartDate", requirementStartDate);
        parameters.put("quantity", new Double(quantity));
        parameters.put("requirementTypeId", (isbuild? "MRP_PRO_PROD_ORDER" : "MRP_PRO_PURCH_ORDER"));
        if (mrpName != null) {
            parameters.put("description", "MRP_" + mrpName);
        } else {
            parameters.put("description", "Automatically generated by MRP");
        }
        try{
            Map result = dispatcher.runSync("createRequirement", parameters);
            return (String) result.get("requirementId");
        } catch (GenericServiceException e) {
            Debug.logError(e,"Error : createRequirement with parameters = "+parameters+"--"+e.getMessage(), module);
            return null;
        }
    }
    
    public void setMrpName(String mrpName) {
        this.mrpName = mrpName;
    }
}
