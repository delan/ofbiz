/*
 * $Id$
 *
 *  Copyright (c) 2001 The Open For Business Project - www.ofbiz.org
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

package org.ofbiz.commonapp.order.order;

import java.util.*;

import org.ofbiz.core.entity.*;
import org.ofbiz.core.service.*;
import org.ofbiz.core.util.*;
import org.ofbiz.commonapp.common.*;
import org.ofbiz.commonapp.product.catalog.*;

/**
 * Order Processing Services
 *
 *@author     <a href="mailto:jaz@zsolv.com">Andy Zeneski</a>
 *@author     <a href="mailto:cnelson@einnovation.com">Chris Nelson</a>
 *@author     <a href="mailto:jonesde@ofbiz.org">David E. Jones</a>
 *@created    December 11, 2001
 *@version    1.0
 */

public class OrderServices {

    /** Service for creating a new order */
    public static Map createOrder(DispatchContext ctx, Map context) {
        Map result = new HashMap();
        GenericDelegator delegator = ctx.getDelegator();
        LocalDispatcher dispatcher = ctx.getDispatcher();
        Collection toBeStored = new LinkedList();

        // check to make sure we have something to order
        List orderItems = (List) context.get("orderItems");
        if (orderItems.size() < 1) {
            result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_ERROR);
            result.put(ModelService.ERROR_MESSAGE, "ERROR: There are no items to order");
            return result;
        }

        // check inventory for each item
        String prodCatalogId = (String) context.get("prodCatalogId");
        List errorMessages = new LinkedList();
        Iterator itemIter = orderItems.iterator();
        while (itemIter.hasNext()) {
            //if the item is out of stock, return an error to that effect
            GenericValue orderItem = (GenericValue) itemIter.next();
            
            if (!CatalogWorker.isCatalogInventoryAvailable(prodCatalogId, orderItem.getString("productId"), orderItem.getDouble("quantity").doubleValue(), delegator, dispatcher)) {
                GenericValue product = null;
                try {
                    product = delegator.findByPrimaryKeyCache("Product", UtilMisc.toMap("productId", orderItem.get("productId")));
                } catch (GenericEntityException e) {
                    Debug.logError(e, "Error when looking up product in createOrder service, product failed inventory check");
                }
                String invErrMsg = "The product ";
                if (product != null) {
                    invErrMsg += product.getString("productName");
                }
                invErrMsg += " with ID " + orderItem.getString("productId") + " is no longer in stock. Please try reducing the quantity or removing the product from this order.";
                errorMessages.add(invErrMsg);
            }
        }
        
        if (errorMessages.size() > 0) {
            result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_ERROR);
            result.put(ModelService.ERROR_MESSAGE_LIST, errorMessages);
            return result;
        }
        
        // create the order object
        String orderId = delegator.getNextSeqId("OrderHeader").toString();
        String billingAccountId = (String) context.get("billingAccountId");
        GenericValue order = delegator.makeValue("OrderHeader",
                UtilMisc.toMap("orderId", orderId, "orderTypeId", "SALES_ORDER",
                        "orderDate", UtilDateTime.nowTimestamp(), "entryDate", UtilDateTime.nowTimestamp(),
                        "statusId", "ORDERED", "billingAccountId", billingAccountId));
        toBeStored.add(order);


        // add in discount adjustment
        Double cartDiscount = (Double) context.get("cartDiscount");
        if (cartDiscount.doubleValue() != 0.0) {
            toBeStored.add(delegator.makeValue("OrderAdjustment",
                    UtilMisc.toMap("orderAdjustmentId", delegator.getNextSeqId("OrderAdjustment").toString(),
                            "orderAdjustmentTypeId", "DISCOUNT_ADJUSTMENT", "orderId", orderId, "orderItemSeqId", DataModelConstants.SEQ_ID_NA,
                            "percentage", cartDiscount)));
        }

        List orderAdjustments = (List) context.get("orderAdjustments");
        Iterator iter = orderAdjustments.iterator();
        while (iter.hasNext()) {
            GenericValue orderAdjustment = (GenericValue) iter.next();
            orderAdjustment.set("orderId", orderId);
            toBeStored.add(orderAdjustment);
        }

        // set the shipping address
        String shippingContactMechId = (String) context.get("shippingContactMechId");
        toBeStored.add(delegator.makeValue("OrderContactMech",
                UtilMisc.toMap("contactMechId", shippingContactMechId,
                        "contactMechPurposeTypeId", "SHIPPING_LOCATION", "orderId", orderId)));

        // set the shipment preference
        String shipmentMethodTypeId = (String) context.get("shipmentMethodTypeId");
        String carrierPartyId = (String) context.get("carrierPartyId");
        GenericValue orderShipmentPreference = delegator.makeValue("OrderShipmentPreference",
                UtilMisc.toMap("orderId", orderId, "orderItemSeqId", DataModelConstants.SEQ_ID_NA,
                        "shipmentMethodTypeId", shipmentMethodTypeId,
                        "carrierPartyId", carrierPartyId, "carrierRoleTypeId", "CARRIER"));
        orderShipmentPreference.set("shippingInstructions", context.get("shippingInstructions"));
        orderShipmentPreference.set("maySplit", context.get("maySplit"));
        orderShipmentPreference.set("giftMessage", context.get("giftMessage"));
        orderShipmentPreference.set("isGift", context.get("isGift"));
        toBeStored.add(orderShipmentPreference);

        // set the order items
        Iterator oi = orderItems.iterator();
        //*cn*int seqId = 1;
        while (oi.hasNext()) {
            GenericValue orderItem = (GenericValue) oi.next();
            orderItem.set("orderId", orderId);
            toBeStored.add(orderItem);
        }

        // set the roles
        String partyId = (String) context.get("partyId");
        final String[] USER_ORDER_ROLE_TYPES = {"END_USER_CUSTOMER", "SHIP_TO_CUSTOMER",
                                                "BILL_TO_CUSTOMER", "PLACING_CUSTOMER"};

        for (int i = 0; i < USER_ORDER_ROLE_TYPES.length; i++) {
            //make sure the party is in the role before adding it...
            toBeStored.add(delegator.makeValue("PartyRole", UtilMisc.toMap(
                    "partyId", partyId,
                    "roleTypeId", USER_ORDER_ROLE_TYPES[i])));
            toBeStored.add(delegator.makeValue("OrderRole", UtilMisc.toMap(
                    "orderId", orderId,
                    "partyId", partyId,
                    "roleTypeId", USER_ORDER_ROLE_TYPES[i])));
        }

        // set the affiliate
        String affiliateId = (String) context.get("affiliateId");
        if (UtilValidate.isNotEmpty(affiliateId)) {
            toBeStored.add(delegator.makeValue("OrderRole", UtilMisc.toMap(
                    "orderId", orderId, "partyId", affiliateId, "roleTypeId", "AFFILIATE")));
        }

        // set the distributor
        String distributorId = (String) context.get("distributorId");
        if (UtilValidate.isNotEmpty(distributorId)) {
            toBeStored.add(delegator.makeValue("OrderRole", UtilMisc.toMap(
                    "orderId", orderId, "partyId", distributorId, "roleTypeId", "DISTRIBUTOR")));
        }

        // set the order status
        toBeStored.add(delegator.makeValue("OrderStatus",
                UtilMisc.toMap("orderStatusId", delegator.getNextSeqId("OrderStatus").toString(),
                        "statusId", "ORDERED", "orderId", orderId, "statusDatetime", UtilDateTime.nowTimestamp())));

        // set the order payment preference
        GenericValue paymentMethod = (GenericValue) context.get("paymentMethod");
        String paymentMethodId = (String) context.get("paymentMethodId");

        if (paymentMethod != null) {
            if (paymentMethodId != null) {
                toBeStored.add(delegator.makeValue("OrderPaymentPreference",
                        UtilMisc.toMap("orderPaymentPreferenceId", delegator.getNextSeqId("OrderPaymentPreference").toString(),
                                "orderId", orderId, "paymentMethodTypeId", paymentMethod.get("paymentMethodTypeId"),
                                "paymentMethodId", paymentMethodId)));
            } else {
                toBeStored.add(delegator.makeValue("OrderPaymentPreference",
                        UtilMisc.toMap("orderPaymentPreferenceId", delegator.getNextSeqId("OrderPaymentPreference").toString(),
                                "orderId", orderId, "paymentMethodTypeId", paymentMethod.get("paymentMethodTypeId"),
                                "paymentMethodId", paymentMethod.get("paymentMethodId"))));
            }
        } else {
            if (paymentMethodId != null && paymentMethodId.equals("_OFFLINE_")) {
                toBeStored.add(delegator.makeValue("OrderPaymentPreference",
                        UtilMisc.toMap("orderPaymentPreferenceId", delegator.getNextSeqId("OrderPaymentPreference").toString(),
                                "orderId", orderId, "paymentMethodTypeId", "OFFLINE")));
            }
        }

        try {
            boolean beganTransaction = TransactionUtil.begin();
            try {
                //store line items, etc so that they will be there for the foreign key checks
                delegator.storeAll(toBeStored);

                //START inventory reservation
                //decrement inventory available for each item, within the same transaction
                List resErrorMessages = new LinkedList();
                Iterator invDecItemIter = orderItems.iterator();
                while (invDecItemIter.hasNext()) {
                    GenericValue orderItem = (GenericValue) invDecItemIter.next();
                    
                    Double inventoryNotReserved = CatalogWorker.reserveCatalogInventory(prodCatalogId, 
                            orderItem.getString("productId"), orderItem.getDouble("quantity"),
                            orderItem.getString("orderId"), orderItem.getString("orderItemSeqId"),
                            delegator, dispatcher);
                    if (inventoryNotReserved != null) {
                        //if inventoryNotReserved is not 0.0 then that is the amount that it couldn't reserve
                        GenericValue product = null;
                        try {
                            product = delegator.findByPrimaryKeyCache("Product", UtilMisc.toMap("productId", orderItem.getString("productId")));
                        } catch (GenericEntityException e) {
                            Debug.logError(e, "Error when looking up product in createOrder service, product failed inventory reservation");
                        }
                        String invErrMsg = "The product ";
                        if (product != null) {
                            invErrMsg += product.getString("productName");
                        }
                        invErrMsg += " with ID " + orderItem.getString("productId") + " is no longer in stock. Please try reducing the quantity or removing the product from this order.";
                        errorMessages.add(invErrMsg);
                    }
                }
                
                if (resErrorMessages.size() > 0) {
                    TransactionUtil.rollback(beganTransaction);
                    result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_ERROR);
                    result.put(ModelService.ERROR_MESSAGE_LIST, resErrorMessages);
                    return result;
                }
                //END inventory reservation

                TransactionUtil.commit(beganTransaction);

                result.put("orderId", orderId);
                result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_SUCCESS);
            } catch (GenericEntityException e) {
                TransactionUtil.rollback(beganTransaction);
                Debug.logError(e);
                result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_ERROR);
                result.put(ModelService.ERROR_MESSAGE, "ERROR: Could not create order (write error: " + e.getMessage() + ").");
            }
        } catch (GenericTransactionException e) {
            Debug.logError(e);
            result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_ERROR);
            result.put(ModelService.ERROR_MESSAGE, "ERROR: Could not create order (transaction error on write: " + e.getMessage() + ").");
        }
        return result;
    }

    /** Service for changing the status on an order */
    public static Map setOrderStatus(DispatchContext ctx, Map context) {
        Map result = new HashMap();
        GenericDelegator delegator = ctx.getDelegator();
        String orderId = (String) context.get("orderId");
        String statusId = (String) context.get("statusId");
        try {
            GenericValue orderHeader = delegator.findByPrimaryKey("OrderHeader", UtilMisc.toMap("orderId", orderId));
            if (orderHeader == null) {
                result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_ERROR);
                result.put(ModelService.ERROR_MESSAGE, "ERROR: Could not change order status; order cannot be found.");
            }
            Debug.logInfo("[OrderServices.setOrderStatus] : From Status : " + orderHeader.getString("statusId"));
            Debug.logInfo("[OrderServices.setOrderStatus] : To Status : " + statusId);
            if (orderHeader.getString("statusId").equals(statusId)) {
                result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_SUCCESS);
                return result;
            }
            try {
                Map statusFields = UtilMisc.toMap("statusId", orderHeader.getString("statusId"), "statusIdTo", statusId);
                GenericValue statusChange = delegator.findByPrimaryKeyCache("StatusValidChange", statusFields);
                if (statusChange == null) {
                    result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_ERROR);
                    result.put(ModelService.ERROR_MESSAGE, "ERROR: Could not change order status; status is not a valid change.");
                    return result;
                }
            } catch (GenericEntityException e) {
                result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_ERROR);
                result.put(ModelService.ERROR_MESSAGE, "ERROR: Could not change order status (" + e.getMessage() + ").");
                return result;
            }
            orderHeader.set("statusId", statusId);
            Map fields = new HashMap();
            fields.put("orderStatusId", delegator.getNextSeqId("OrderStatus").toString());
            fields.put("statusId", statusId);
            fields.put("orderId", orderId);
            fields.put("statusDatetime", UtilDateTime.nowTimestamp());
            GenericValue orderStatus = delegator.makeValue("OrderStatus", fields);
            Collection c = new ArrayList();
            c.add(orderHeader);
            c.add(orderStatus);
            delegator.storeAll(c);
        } catch (GenericEntityException e) {
            result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_ERROR);
            result.put(ModelService.ERROR_MESSAGE, "ERROR: Could not change order status (" + e.getMessage() + ").");
            return result;
        }
        result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_SUCCESS);
        result.put("orderStatusId", statusId);
        return result;
    }

    /** Service to add a role type to an order */
    public static Map addRoleType(DispatchContext ctx, Map context) {
        Map result = new HashMap();
        GenericDelegator delegator = ctx.getDelegator();
        String orderId = (String) context.get("orderId");
        String partyId = (String) context.get("partyId");
        String roleTypeId = (String) context.get("roleTypeId");
        String removeOld = (String) context.get("removeOld");

        if (removeOld != null && removeOld.equalsIgnoreCase("true")) {
            try {
                delegator.removeByAnd("OrderRole", UtilMisc.toMap("orderId", orderId, "roleTypeId", roleTypeId));
            } catch (GenericEntityException e) {
                result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_ERROR);
                result.put(ModelService.ERROR_MESSAGE, "ERROR: Could not remove old roles (" + e.getMessage() + ").");
                return result;
            }
        }

        Map fields = UtilMisc.toMap("orderId", orderId, "partyId", partyId, "roleTypeId", roleTypeId);
        try {
            GenericValue value = delegator.makeValue("OrderRole", fields);
            delegator.create(value);
        } catch (GenericEntityException e) {
            result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_ERROR);
            result.put(ModelService.ERROR_MESSAGE, "ERROR: Could not add role to order (" + e.getMessage() + ").");
            return result;
        }
        result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_SUCCESS);
        return result;
    }

    /** Service to remove a role type from an order */
    public static Map removeRoleType(DispatchContext ctx, Map context) {
        Map result = new HashMap();
        GenericDelegator delegator = ctx.getDelegator();
        String orderId = (String) context.get("orderId");
        String partyId = (String) context.get("partyId");
        String roleTypeId = (String) context.get("roleTypeId");
        Map fields = UtilMisc.toMap("orderId", orderId, "partyId", partyId, "roleTypeId", roleTypeId);

        GenericValue testValue = null;
        try {
            testValue = delegator.findByPrimaryKey("OrderRole", fields);
        } catch (GenericEntityException e) {
            result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_ERROR);
            result.put(ModelService.ERROR_MESSAGE, "ERROR: Could not add role to order (" + e.getMessage() + ").");
            return result;
        }

        if (testValue == null) {
            result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_SUCCESS);
            return result;
        }

        try {
            GenericValue value = delegator.findByPrimaryKey("OrderRole", fields);
            value.remove();
        } catch (GenericEntityException e) {
            result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_ERROR);
            result.put(ModelService.ERROR_MESSAGE, "ERROR: Could not remove role from order (" + e.getMessage() + ").");
            return result;
        }
        result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_SUCCESS);
        return result;
    }

    /** Service to email a customer with order status */
    public static Map emailOrder(DispatchContext ctx, Map context) {
        Map result = new HashMap();
        GenericDelegator delegator = ctx.getDelegator();
        // This may be moved to a different place - email is a common task code this generic
        // implement me
        return result;
    }

    /** Service to create an order payment preference */
    public static Map createPaymentPreference(DispatchContext ctx, Map context) {
        Map result = new HashMap();
        GenericDelegator delegator = ctx.getDelegator();
        String orderId = (String) context.get("orderId");
        String paymentMethodTypeId = (String) context.get("paymentMethodTypeId");
        String paymentMethodId = (String) context.get("paymentMethodId");
        Double maxAmount = (Double) context.get("maxAmount");

        String prefId = null;
        Long newId = delegator.getNextSeqId("OrderPaymentPreference");
        if (newId == null)
            return ServiceUtil.returnError("ERROR: Could not create OrderPaymentPreference (id generation failure)");
        else
            prefId = newId.toString();

        Map fields = UtilMisc.toMap("orderPaymentPreferenceId", prefId, "orderId", orderId, "paymentMethodTypeId",
                paymentMethodTypeId, "paymentMethodId", paymentMethodId, "maxAmount", maxAmount);
        try {
            GenericValue v = delegator.makeValue("OrderPaymentPreference", fields);
            delegator.create(v);
        } catch (GenericEntityException e) {
            result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_ERROR);
            result.put(ModelService.ERROR_MESSAGE, "ERROR: Could not create OrderPaymentPreference (" + e.getMessage() + ").");
            return result;
        }
        result.put("orderPaymentPreferenceId", prefId);
        result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_SUCCESS);
        return result;
    }

    /** Service to get basic order information. */
    public static Map getOrderInformation(DispatchContext dctx, Map context) {
        Map result = new HashMap();
        GenericDelegator delegator = dctx.getDelegator();
        String orderId = (String) context.get("orderId");
        try {
            GenericValue orderHeader = delegator.findByPrimaryKeyCache("OrderHeader", UtilMisc.toMap("orderId", orderId));
            Collection orderItems = orderHeader.getRelatedCache("OrderItem");
            OrderReadHelper orh = new OrderReadHelper(orderHeader);
            String statusString = orh.getStatusString();
            Double totalItems = new Double(orh.getOrderItemsTotal());
            Double totalPrice = new Double(orh.getTotalPrice());
            GenericValue shipAddress = orh.getShippingAddress();
            GenericValue billAddress = orh.getBillingAddress();
            GenericValue billTo = orh.getBillToPerson();
            String affilId = orh.getAffiliateId();
            String distId = orh.getDistributorId();
            result.put("orderId", orderId);
            result.put("orderHeader", orderHeader);
            result.put("orderItems", orderItems);
            result.put("totalItems", totalItems);
            result.put("totalPrice", totalPrice);
            result.put("shippingAddress", shipAddress);
            result.put("billingAddress", billAddress);
            result.put("billToPerson", billTo);
            result.put("affiliateId", affilId);
            result.put("distributorId", distId);
            result.put("statusString", statusString);
        } catch (GenericEntityException e) {
            result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_ERROR);
            result.put(ModelService.ERROR_MESSAGE, "ERROR: Could not get order information (" + e.getMessage() + ").");
        }
        return result;
    }

    /** Service to get an order contact mech. */
    public static Map getOrderAddress(DispatchContext dctx, Map context) {
        Map result = new HashMap();
        GenericDelegator delegator = dctx.getDelegator();
        String orderId = (String) context.get("orderId");
        GenericValue v = null;
        String purpose[] = { "BILLING_LOCATION", "SHIPPING_LOCATION" };
        String outKey[] = { "billingAddress", "shippingAddress" };
        GenericValue orderHeader = null;
        try {
            orderHeader = delegator.findByPrimaryKeyCache("OrderHeader", UtilMisc.toMap("orderId", orderId));
            if (orderHeader != null)
                result.put("orderHeader", orderHeader);
        } catch (GenericEntityException e) {
            result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_ERROR);
            result.put(ModelService.ERROR_MESSAGE, "ERROR: Could not get OrderHeader (" + e.getMessage() + ").");
            return result;
        }
        if (orderHeader == null) {
            result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_ERROR);
            result.put(ModelService.ERROR_MESSAGE, "ERROR: Could get the OrderHeader.");
            return result;
        }
        for (int i = 0; i < purpose.length; i++) {
            try {
                GenericValue orderContactMech = EntityUtil.getFirst(orderHeader.getRelatedByAnd("OrderContactMech",
                        UtilMisc.toMap("contactMechPurposeTypeId", purpose[i])));
                GenericValue contactMech = orderContactMech.getRelatedOne("ContactMech");
                if (contactMech != null) {
                    result.put(outKey[i], contactMech.getRelatedOne("PostalAddress"));
                }
            } catch (GenericEntityException e) {
                result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_ERROR);
                result.put(ModelService.ERROR_MESSAGE, "ERROR: Problems getting contact mech (" + e.getMessage() + ").");
                return result;
            }
        }

        result.put("orderId", orderId);
        return result;
    }
}
