/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/
package org.ofbiz.order.order;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javolution.util.FastList;
import javolution.util.FastMap;
import javolution.util.FastSet;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilFormatOut;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilNumber;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.common.DataModelConstants;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntity;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.product.product.ProductWorker;
import org.ofbiz.security.Security;

/**
 * Utility class for easily extracting important information from orders
 *
 * <p>NOTE: in the current scheme order adjustments are never included in tax or shipping,
 * but order item adjustments ARE included in tax and shipping calcs unless they are
 * tax or shipping adjustments or the includeInTax or includeInShipping are set to N.</p>
 */
public class OrderReadHelper {

    public static final String module = OrderReadHelper.class.getName();

    // scales and rounding modes for BigDecimal math
    public static final int scale = UtilNumber.getBigDecimalScale("order.decimals");
    public static final int rounding = UtilNumber.getBigDecimalRoundingMode("order.rounding");
    public static final int taxCalcScale = UtilNumber.getBigDecimalScale("salestax.calc.decimals");
    public static final int taxFinalScale = UtilNumber.getBigDecimalScale("salestax.final.decimals");
    public static final int taxRounding = UtilNumber.getBigDecimalRoundingMode("salestax.rounding");
    public static final BigDecimal ZERO = (BigDecimal.ZERO).setScale(scale, rounding);
    public static final BigDecimal percentage = (new BigDecimal("0.01")).setScale(scale, rounding);

    protected GenericValue orderHeader = null;
    protected List<GenericValue> orderItemAndShipGrp = null;
    protected List<GenericValue> orderItems = null;
    protected List<GenericValue> adjustments = null;
    protected List<GenericValue> paymentPrefs = null;
    protected List<GenericValue> orderStatuses = null;
    protected List<GenericValue> orderItemPriceInfos = null;
    protected List<GenericValue> orderItemShipGrpInvResList = null;
    protected List<GenericValue> orderItemIssuances = null;
    protected List<GenericValue> orderReturnItems = null;
    protected BigDecimal totalPrice = null;

    protected OrderReadHelper() {}

    public OrderReadHelper(GenericValue orderHeader, List<GenericValue> adjustments, List<GenericValue> orderItems) {
        this.orderHeader = orderHeader;
        this.adjustments = adjustments;
        this.orderItems = orderItems;
        if (this.orderHeader != null && !this.orderHeader.getEntityName().equals("OrderHeader")) {
            try {
                this.orderHeader = orderHeader.getDelegator().findByPrimaryKey("OrderHeader", UtilMisc.toMap("orderId",
                        orderHeader.getString("orderId")));
            } catch (GenericEntityException e) {
                Debug.logError(e, module);
                this.orderHeader = null;
            }
        } else if (this.orderHeader == null && orderItems != null) {
            GenericValue firstItem = EntityUtil.getFirst(orderItems);
            try {
                this.orderHeader = firstItem.getRelatedOne("OrderHeader");
            } catch (GenericEntityException e) {
                Debug.logError(e, module);
                this.orderHeader = null;
            }
        }
        if (this.orderHeader == null) {
            if (orderHeader == null) {
                throw new IllegalArgumentException("Order header passed is null, or is otherwise invalid");
            } else {
                throw new IllegalArgumentException("Order header passed in is not valid for orderId [" + orderHeader.getString("orderId") + "]");
            }
        }
    }

    public OrderReadHelper(GenericValue orderHeader) {
        this(orderHeader, null, null);
    }

    public OrderReadHelper(List<GenericValue> adjustments, List<GenericValue> orderItems) {
        this.adjustments = adjustments;
        this.orderItems = orderItems;
    }

    public OrderReadHelper(Delegator delegator, String orderId) {
        try {
            this.orderHeader = delegator.findByPrimaryKey("OrderHeader", UtilMisc.toMap("orderId", orderId));
        } catch (GenericEntityException e) {
            String errMsg = "Error finding order with ID [" + orderId + "]: " + e.toString();
            Debug.logError(e, errMsg, module);
            throw new IllegalArgumentException(errMsg);
        }
        if (this.orderHeader == null) {
            throw new IllegalArgumentException("Order not found with orderId [" + orderId + "]");
        }
    }

    // ==========================================
    // ========== Order Header Methods ==========
    // ==========================================

    public String getOrderId() {
        return orderHeader.getString("orderId");
    }

    public String getWebSiteId() {
        return orderHeader.getString("webSiteId");
    }

    public String getProductStoreId() {
        return orderHeader.getString("productStoreId");
    }

    /**
     * Returns the ProductStore of this Order or null in case of Exception
     */
    public GenericValue getProductStore() {
        String productStoreId = orderHeader.getString("productStoreId");
        try {
            Delegator delegator = orderHeader.getDelegator();
            GenericValue productStore = delegator.findByPrimaryKeyCache("ProductStore", UtilMisc.toMap("productStoreId", productStoreId));
            return productStore;
        } catch (GenericEntityException ex) {
            Debug.logError(ex, "Failed to get product store for order header [" + orderHeader + "] due to exception "+ ex.getMessage(), module);
            return null;
        }
    }

    public String getOrderTypeId() {
        return orderHeader.getString("orderTypeId");
    }

    public String getCurrency() {
        return orderHeader.getString("currencyUom");
    }

    public String getOrderName() {
        return orderHeader.getString("orderName");
    }

    public List<GenericValue> getAdjustments() {
        if (adjustments == null) {
            try {
                adjustments = orderHeader.getRelated("OrderAdjustment");
            } catch (GenericEntityException e) {
                Debug.logError(e, module);
            }
            if (adjustments == null)
                adjustments = FastList.newInstance();
        }
        return adjustments;
    }

    public List<GenericValue> getPaymentPreferences() {
        if (paymentPrefs == null) {
            try {
                paymentPrefs = orderHeader.getRelated("OrderPaymentPreference", UtilMisc.toList("orderPaymentPreferenceId"));
            } catch (GenericEntityException e) {
                Debug.logError(e, module);
            }
        }
        return paymentPrefs;
    }

    /**
     * Returns a Map of paymentMethodId -> amount charged (BigDecimal) based on PaymentGatewayResponse.
     * @return returns a Map of paymentMethodId -> amount charged (BigDecimal) based on PaymentGatewayResponse.
     */
    public Map<String, BigDecimal> getReceivedPaymentTotalsByPaymentMethod() {
        Map<String, BigDecimal> paymentMethodAmounts = FastMap.newInstance();
        List<GenericValue> paymentPrefs = getPaymentPreferences();
        Iterator<GenericValue> ppit = paymentPrefs.iterator();
        while (ppit.hasNext()) {
            GenericValue paymentPref = ppit.next();
            List<GenericValue> payments = FastList.newInstance();
            try {
                List<EntityExpr> exprs = UtilMisc.toList(EntityCondition.makeCondition("statusId", EntityOperator.EQUALS, "PMNT_RECEIVED"),
                                            EntityCondition.makeCondition("statusId", EntityOperator.EQUALS, "PMNT_CONFIRMED"));
                payments = paymentPref.getRelated("Payment");
                payments = EntityUtil.filterByOr(payments, exprs);
                List<EntityExpr> conds = UtilMisc.toList(EntityCondition.makeCondition("paymentTypeId", EntityOperator.EQUALS, "CUSTOMER_PAYMENT"),
                                            EntityCondition.makeCondition("paymentTypeId", EntityOperator.EQUALS, "CUSTOMER_DEPOSIT"),
                                            EntityCondition.makeCondition("paymentTypeId", EntityOperator.EQUALS, "INTEREST_RECEIPT"),
                                            EntityCondition.makeCondition("paymentTypeId", EntityOperator.EQUALS, "GC_DEPOSIT"),
                                            EntityCondition.makeCondition("paymentTypeId", EntityOperator.EQUALS, "POS_PAID_IN"));
                payments = EntityUtil.filterByOr(payments, conds);
            } catch (GenericEntityException e) {
                Debug.logError(e, module);
            }

            BigDecimal chargedToPaymentPref = ZERO;
            Iterator<GenericValue> payit = payments.iterator();
            while (payit.hasNext()) {
                GenericValue payment = payit.next();
                if (payment.get("amount") != null) {
                    chargedToPaymentPref = chargedToPaymentPref.add(payment.getBigDecimal("amount")).setScale(scale+1, rounding);
                }
            }

            // if chargedToPaymentPref > 0
            if (chargedToPaymentPref.compareTo(ZERO) > 0) {
                // key of the resulting map is paymentMethodId or paymentMethodTypeId if the paymentMethodId is not available
                String paymentMethodKey = paymentPref.getString("paymentMethodId") != null ? paymentPref.getString("paymentMethodId") : paymentPref.getString("paymentMethodTypeId");
                if (paymentMethodAmounts.containsKey(paymentMethodKey)) {
                    BigDecimal value = paymentMethodAmounts.get(paymentMethodKey);
                    if (value != null) chargedToPaymentPref = chargedToPaymentPref.add(value);
                }
                paymentMethodAmounts.put(paymentMethodKey, chargedToPaymentPref.setScale(scale, rounding));
            }
        }
        return paymentMethodAmounts;
    }

    /**
     * Returns a Map of paymentMethodId -> amount refunded
     * @return returns a Map of paymentMethodId -> amount refunded
     */
    public Map<String, BigDecimal> getReturnedTotalsByPaymentMethod() {
        Map<String, BigDecimal> paymentMethodAmounts = FastMap.newInstance();
        List<GenericValue> paymentPrefs = getPaymentPreferences();
        Iterator<GenericValue> ppit = paymentPrefs.iterator();
        while (ppit.hasNext()) {
            GenericValue paymentPref = ppit.next();
            List<GenericValue> returnItemResponses = FastList.newInstance();
            try {
                returnItemResponses = orderHeader.getDelegator().findByAnd("ReturnItemResponse", UtilMisc.toMap("orderPaymentPreferenceId", paymentPref.getString("orderPaymentPreferenceId")));
            } catch (GenericEntityException e) {
                Debug.logError(e, module);
            }
            BigDecimal refundedToPaymentPref = ZERO;
            Iterator<GenericValue> ririt = returnItemResponses.iterator();
            while (ririt.hasNext()) {
                GenericValue returnItemResponse = ririt.next();
                refundedToPaymentPref = refundedToPaymentPref.add(returnItemResponse.getBigDecimal("responseAmount")).setScale(scale+1, rounding);
            }

            // if refundedToPaymentPref > 0
            if (refundedToPaymentPref.compareTo(ZERO) == 1) {
                String paymentMethodId = paymentPref.getString("paymentMethodId") != null ? paymentPref.getString("paymentMethodId") : paymentPref.getString("paymentMethodTypeId");
                paymentMethodAmounts.put(paymentMethodId, refundedToPaymentPref.setScale(scale, rounding));
            }
        }
        return paymentMethodAmounts;
    }

    public List<GenericValue> getOrderPayments() {
        return getOrderPayments(null);
    }

    public List<GenericValue> getOrderPayments(GenericValue orderPaymentPreference) {
        List<GenericValue> orderPayments = FastList.newInstance();
        List<GenericValue> prefs = null;

        if (orderPaymentPreference == null) {
            prefs = getPaymentPreferences();
        } else {
            prefs = UtilMisc.toList(orderPaymentPreference);
        }
        if (prefs != null) {
            Iterator<GenericValue> i = prefs.iterator();
            while (i.hasNext()) {
                GenericValue payPref = i.next();
                try {
                    orderPayments.addAll(payPref.getRelated("Payment"));
                } catch (GenericEntityException e) {
                    Debug.logError(e, module);
                    return null;
                }
            }
        }
        return orderPayments;
    }

    public List<GenericValue> getOrderStatuses() {
        if (orderStatuses == null) {
            try {
                orderStatuses = orderHeader.getRelated("OrderStatus");
            } catch (GenericEntityException e) {
                Debug.logError(e, module);
            }
        }
        return orderStatuses;
    }

    public List<GenericValue> getOrderTerms() {
        try {
           return orderHeader.getRelated("OrderTerm");
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
            return null;
        }
    }

    /**
     * Return the number of days from termDays of first FIN_PAYMENT_TERM
     * @return number of days from termDays of first FIN_PAYMENT_TERM
     */
    public Long getOrderTermNetDays() {
        List<GenericValue> orderTerms = EntityUtil.filterByAnd(getOrderTerms(), UtilMisc.toMap("termTypeId", "FIN_PAYMENT_TERM"));
        if (UtilValidate.isEmpty(orderTerms)) {
            return null;
        } else if (orderTerms.size() > 1) {
            Debug.logWarning("Found " + orderTerms.size() + " FIN_PAYMENT_TERM order terms for orderId [" + getOrderId() + "], using the first one ", module);
        }
        return orderTerms.get(0).getLong("termDays");
    }

    public String getShippingMethod(String shipGroupSeqId) {
        try {
            GenericValue shipGroup = orderHeader.getDelegator().findByPrimaryKey("OrderItemShipGroup",
                    UtilMisc.toMap("orderId", orderHeader.getString("orderId"), "shipGroupSeqId", shipGroupSeqId));

            if (shipGroup != null) {
                GenericValue carrierShipmentMethod = shipGroup.getRelatedOne("CarrierShipmentMethod");

                if (carrierShipmentMethod != null) {
                    GenericValue shipmentMethodType = carrierShipmentMethod.getRelatedOne("ShipmentMethodType");

                    if (shipmentMethodType != null) {
                        return UtilFormatOut.checkNull(shipGroup.getString("carrierPartyId")) + " " +
                                UtilFormatOut.checkNull(shipmentMethodType.getString("description"));
                    }
                }
                return UtilFormatOut.checkNull(shipGroup.getString("carrierPartyId"));
            }
        } catch (GenericEntityException e) {
            Debug.logWarning(e, module);
        }
        return "";
    }

    public String getShippingMethodCode(String shipGroupSeqId) {
        try {
            GenericValue shipGroup = orderHeader.getDelegator().findByPrimaryKey("OrderItemShipGroup",
                    UtilMisc.toMap("orderId", orderHeader.getString("orderId"), "shipGroupSeqId", shipGroupSeqId));

            if (shipGroup != null) {
                GenericValue carrierShipmentMethod = shipGroup.getRelatedOne("CarrierShipmentMethod");

                if (carrierShipmentMethod != null) {
                    GenericValue shipmentMethodType = carrierShipmentMethod.getRelatedOne("ShipmentMethodType");

                    if (shipmentMethodType != null) {
                        return UtilFormatOut.checkNull(shipmentMethodType.getString("shipmentMethodTypeId")) + "@" + UtilFormatOut.checkNull(shipGroup.getString("carrierPartyId"));
                    }
                }
                return UtilFormatOut.checkNull(shipGroup.getString("carrierPartyId"));
            }
        } catch (GenericEntityException e) {
            Debug.logWarning(e, module);
        }
        return "";
    }

    public boolean hasShippingAddress() {
        if (UtilValidate.isNotEmpty(this.getShippingLocations())) {
            return true;
        }
        return false;
    }

    public boolean hasPhysicalProductItems() throws GenericEntityException {
        Iterator<GenericValue> orderItemIter = this.getOrderItems().iterator();
        while (orderItemIter.hasNext()) {
            GenericValue orderItem = orderItemIter.next();
            GenericValue product = orderItem.getRelatedOneCache("Product");
            if (product != null) {
                GenericValue productType = product.getRelatedOneCache("ProductType");
                if ("Y".equals(productType.getString("isPhysical"))) {
                    return true;
                }
            }
        }
        return false;
    }

    public GenericValue getOrderItemShipGroup(String shipGroupSeqId) {
        try {
            return orderHeader.getDelegator().findByPrimaryKey("OrderItemShipGroup",
                    UtilMisc.toMap("orderId", orderHeader.getString("orderId"), "shipGroupSeqId", shipGroupSeqId));
        } catch (GenericEntityException e) {
            Debug.logWarning(e, module);
        }
        return null;
    }

    public List<GenericValue> getOrderItemShipGroups() {
        try {
            return orderHeader.getRelated("OrderItemShipGroup", UtilMisc.toList("shipGroupSeqId"));
        } catch (GenericEntityException e) {
            Debug.logWarning(e, module);
        }
        return null;
    }

    public List<GenericValue> getShippingLocations() {
        List<GenericValue> shippingLocations = FastList.newInstance();
        List<GenericValue> shippingCms = this.getOrderContactMechs("SHIPPING_LOCATION");
        if (shippingCms != null) {
            Iterator<GenericValue> i = shippingCms.iterator();
            while (i.hasNext()) {
                GenericValue ocm = i.next();
                if (ocm != null) {
                    try {
                        GenericValue addr = ocm.getDelegator().findByPrimaryKey("PostalAddress",
                                UtilMisc.toMap("contactMechId", ocm.getString("contactMechId")));
                        if (addr != null) {
                            shippingLocations.add(addr);
                        }
                    } catch (GenericEntityException e) {
                        Debug.logWarning(e, module);
                    }
                }
            }
        }
        return shippingLocations;
    }

    public GenericValue getShippingAddress(String shipGroupSeqId) {
        try {
            GenericValue shipGroup = orderHeader.getDelegator().findByPrimaryKey("OrderItemShipGroup",
                    UtilMisc.toMap("orderId", orderHeader.getString("orderId"), "shipGroupSeqId", shipGroupSeqId));

            if (shipGroup != null) {
                return shipGroup.getRelatedOne("PostalAddress");

            }
        } catch (GenericEntityException e) {
            Debug.logWarning(e, module);
        }
        return null;
    }

    /** @deprecated */
    @Deprecated
    public GenericValue getShippingAddress() {
        try {
            GenericValue orderContactMech = EntityUtil.getFirst(orderHeader.getRelatedByAnd("OrderContactMech", UtilMisc.toMap(
                            "contactMechPurposeTypeId", "SHIPPING_LOCATION")));

            if (orderContactMech != null) {
                GenericValue contactMech = orderContactMech.getRelatedOne("ContactMech");

                if (contactMech != null) {
                    return contactMech.getRelatedOne("PostalAddress");
                }
            }
        } catch (GenericEntityException e) {
            Debug.logWarning(e, module);
        }
        return null;
    }

    public List<GenericValue> getBillingLocations() {
        List<GenericValue> billingLocations = FastList.newInstance();
        List<GenericValue> billingCms = this.getOrderContactMechs("BILLING_LOCATION");
        if (billingCms != null) {
            Iterator<GenericValue> i = billingCms.iterator();
            while (i.hasNext()) {
                GenericValue ocm = i.next();
                if (ocm != null) {
                    try {
                        GenericValue addr = ocm.getDelegator().findByPrimaryKey("PostalAddress",
                                UtilMisc.toMap("contactMechId", ocm.getString("contactMechId")));
                        if (addr != null) {
                            billingLocations.add(addr);
                        }
                    } catch (GenericEntityException e) {
                        Debug.logWarning(e, module);
                    }
                }
            }
        }
        return billingLocations;
    }

    /** @deprecated */
    @Deprecated
    public GenericValue getBillingAddress() {
        GenericValue billingAddress = null;
        try {
            GenericValue orderContactMech = EntityUtil.getFirst(orderHeader.getRelatedByAnd("OrderContactMech", UtilMisc.toMap("contactMechPurposeTypeId", "BILLING_LOCATION")));

            if (orderContactMech != null) {
                GenericValue contactMech = orderContactMech.getRelatedOne("ContactMech");

                if (contactMech != null) {
                    billingAddress = contactMech.getRelatedOne("PostalAddress");
                }
            }
        } catch (GenericEntityException e) {
            Debug.logWarning(e, module);
        }

        if (billingAddress == null) {
            // get the address from the billing account
            GenericValue billingAccount = getBillingAccount();
            if (billingAccount != null) {
                try {
                    billingAddress = billingAccount.getRelatedOne("PostalAddress");
                } catch (GenericEntityException e) {
                    Debug.logWarning(e, module);
                }
            } else {
                // get the address from the first payment method
                GenericValue paymentPreference = EntityUtil.getFirst(getPaymentPreferences());
                if (paymentPreference != null) {
                    try {
                        GenericValue paymentMethod = paymentPreference.getRelatedOne("PaymentMethod");
                        if (paymentMethod != null) {
                            GenericValue creditCard = paymentMethod.getRelatedOne("CreditCard");
                            if (creditCard != null) {
                                billingAddress = creditCard.getRelatedOne("PostalAddress");
                            } else {
                                GenericValue eftAccount = paymentMethod.getRelatedOne("EftAccount");
                                if (eftAccount != null) {
                                    billingAddress = eftAccount.getRelatedOne("PostalAddress");
                                }
                            }
                        }
                    } catch (GenericEntityException e) {
                        Debug.logWarning(e, module);
                    }
                }
            }
        }
        return billingAddress;
    }

    public List<GenericValue> getOrderContactMechs(String purposeTypeId) {
        try {
            return orderHeader.getRelatedByAnd("OrderContactMech",
                    UtilMisc.toMap("contactMechPurposeTypeId", purposeTypeId));
        } catch (GenericEntityException e) {
            Debug.logWarning(e, module);
        }
        return null;
    }

    public Timestamp getEarliestShipByDate() {
        try {
            List<GenericValue> groups = orderHeader.getRelated("OrderItemShipGroup", UtilMisc.toList("shipByDate"));
            if (groups.size() > 0) {
                GenericValue group = groups.get(0);
                return group.getTimestamp("shipByDate");
            }
        } catch (GenericEntityException e) {
            Debug.logWarning(e, module);
        }
        return null;
    }

    public Timestamp getLatestShipAfterDate() {
        try {
            List<GenericValue> groups = orderHeader.getRelated("OrderItemShipGroup", UtilMisc.toList("shipAfterDate DESC"));
            if (groups.size() > 0) {
                GenericValue group = groups.get(0);
                return group.getTimestamp("shipAfterDate");
            }
        } catch (GenericEntityException e) {
            Debug.logWarning(e, module);
        }
        return null;
    }

    public String getCurrentStatusString() {
        GenericValue statusItem = null;
        try {
            statusItem = orderHeader.getRelatedOneCache("StatusItem");
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
        }
        if (statusItem != null) {
            return statusItem.getString("description");
        } else {
            return orderHeader.getString("statusId");
        }
    }

    public String getStatusString(Locale locale) {
        List<GenericValue> orderStatusList = this.getOrderHeaderStatuses();

        if (UtilValidate.isEmpty(orderStatusList)) return "";

        Iterator<GenericValue> orderStatusIter = orderStatusList.iterator();
        StringBuilder orderStatusString = new StringBuilder(50);

        try {
            boolean isCurrent = true;
            while (orderStatusIter.hasNext()) {
                GenericValue orderStatus = orderStatusIter.next();
                GenericValue statusItem = orderStatus.getRelatedOneCache("StatusItem");

                if (statusItem != null) {
                    orderStatusString.append(statusItem.get("description", locale));
                } else {
                    orderStatusString.append(orderStatus.getString("statusId"));
                }

                if (isCurrent && orderStatusIter.hasNext()) {
                    orderStatusString.append(" (");
                    isCurrent = false;
                } else {
                    if (orderStatusIter.hasNext()) {
                        orderStatusString.append("/");
                    } else {
                        if (!isCurrent) {
                            orderStatusString.append(")");
                        }
                    }
                }
            }
        } catch (GenericEntityException e) {
            Debug.logError(e, "Error getting Order Status information: " + e.toString(), module);
        }

        return orderStatusString.toString();
    }

    public GenericValue getBillingAccount() {
        GenericValue billingAccount = null;
        try {
            billingAccount = orderHeader.getRelatedOne("BillingAccount");
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
        }
        return billingAccount;
    }

    /**
     * Returns the OrderPaymentPreference.maxAmount for the billing account associated with the order, or 0 if there is no
     * billing account or no max amount set
     */
    public BigDecimal getBillingAccountMaxAmount() {
        if (getBillingAccount() == null) {
            return BigDecimal.ZERO;
        } else {
            List<GenericValue> paymentPreferences = null;
            try {
                Delegator delegator = orderHeader.getDelegator();
                paymentPreferences = delegator.findByAnd("OrderPurchasePaymentSummary", UtilMisc.toMap("orderId", orderHeader.getString("orderId")));
            } catch (GenericEntityException e) {
                Debug.logWarning(e, module);
            }
            List<EntityExpr> exprs = UtilMisc.toList(
                    EntityCondition.makeCondition("paymentMethodTypeId", "EXT_BILLACT"),
                    EntityCondition.makeCondition("preferenceStatusId", EntityOperator.NOT_EQUAL, "PAYMENT_CANCELLED"));
            GenericValue billingAccountPaymentPreference = EntityUtil.getFirst(EntityUtil.filterByAnd(paymentPreferences, exprs));
            if ((billingAccountPaymentPreference != null) && (billingAccountPaymentPreference.getBigDecimal("maxAmount") != null)) {
                return billingAccountPaymentPreference.getBigDecimal("maxAmount");
            } else {
                return BigDecimal.ZERO;
            }
        }
    }

    /**
     * Returns party from OrderRole of BILL_TO_CUSTOMER
     */
    public GenericValue getBillToParty() {
        return this.getPartyFromRole("BILL_TO_CUSTOMER");
    }

    /**
     * Returns party from OrderRole of BILL_FROM_VENDOR
     */
    public GenericValue getBillFromParty() {
        return this.getPartyFromRole("BILL_FROM_VENDOR");
    }

    /**
     * Returns party from OrderRole of SHIP_TO_CUSTOMER
     */
    public GenericValue getShipToParty() {
        return this.getPartyFromRole("SHIP_TO_CUSTOMER");
    }

    /**
     * Returns party from OrderRole of PLACING_CUSTOMER
     */
    public GenericValue getPlacingParty() {
        return this.getPartyFromRole("PLACING_CUSTOMER");
    }

    /**
     * Returns party from OrderRole of END_USER_CUSTOMER
     */
    public GenericValue getEndUserParty() {
        return this.getPartyFromRole("END_USER_CUSTOMER");
    }

    /**
     * Returns party from OrderRole of SUPPLIER_AGENT
     */
    public GenericValue getSupplierAgent() {
        return this.getPartyFromRole("SUPPLIER_AGENT");
    }

    public GenericValue getPartyFromRole(String roleTypeId) {
        Delegator delegator = orderHeader.getDelegator();
        GenericValue partyObject = null;
        try {
            GenericValue orderRole = EntityUtil.getFirst(orderHeader.getRelatedByAnd("OrderRole", UtilMisc.toMap("roleTypeId", roleTypeId)));

            if (orderRole != null) {
                partyObject = delegator.findByPrimaryKey("Person", UtilMisc.toMap("partyId", orderRole.getString("partyId")));

                if (partyObject == null) {
                    partyObject = delegator.findByPrimaryKey("PartyGroup", UtilMisc.toMap("partyId", orderRole.getString("partyId")));
                }
            }
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
        }
        return partyObject;
    }

    public String getDistributorId() {
        try {
            GenericEntity distributorRole = EntityUtil.getFirst(orderHeader.getRelatedByAnd("OrderRole", UtilMisc.toMap("roleTypeId", "DISTRIBUTOR")));

            return distributorRole == null ? null : distributorRole.getString("partyId");
        } catch (GenericEntityException e) {
            Debug.logWarning(e, module);
        }
        return null;
    }

    public String getAffiliateId() {
        try {
            GenericEntity distributorRole = EntityUtil.getFirst(orderHeader.getRelatedByAnd("OrderRole", UtilMisc.toMap("roleTypeId", "AFFILIATE")));

            return distributorRole == null ? null : distributorRole.getString("partyId");
        } catch (GenericEntityException e) {
            Debug.logWarning(e, module);
        }
        return null;
    }

    public BigDecimal getShippingTotal() {
        return OrderReadHelper.calcOrderAdjustments(getOrderHeaderAdjustments(), getOrderItemsSubTotal(), false, false, true);
    }

    public BigDecimal getHeaderTaxTotal() {
        return OrderReadHelper.calcOrderAdjustments(getOrderHeaderAdjustments(), getOrderItemsSubTotal(), false, true, false);
    }

    public BigDecimal getTaxTotal() {
        return OrderReadHelper.calcOrderAdjustments(getAdjustments(), getOrderItemsSubTotal(), false, true, false);
    }

    public Set<String> getItemFeatureSet(GenericValue item) {
        Set<String> featureSet = new LinkedHashSet<String>();
        List<GenericValue> featureAppls = null;
        if (item.get("productId") != null) {
            try {
                featureAppls = item.getDelegator().findByAndCache("ProductFeatureAppl", UtilMisc.toMap("productId", item.getString("productId")));
                List<EntityExpr> filterExprs = UtilMisc.toList(EntityCondition.makeCondition("productFeatureApplTypeId", EntityOperator.EQUALS, "STANDARD_FEATURE"));
                filterExprs.add(EntityCondition.makeCondition("productFeatureApplTypeId", EntityOperator.EQUALS, "REQUIRED_FEATURE"));
                featureAppls = EntityUtil.filterByOr(featureAppls, filterExprs);
            } catch (GenericEntityException e) {
                Debug.logError(e, "Unable to get ProductFeatureAppl for item : " + item, module);
            }
            if (featureAppls != null) {
                Iterator<GenericValue> fai = featureAppls.iterator();
                while (fai.hasNext()) {
                    GenericValue appl = fai.next();
                    featureSet.add(appl.getString("productFeatureId"));
                }
            }
        }

        // get the ADDITIONAL_FEATURE adjustments
        List<GenericValue> additionalFeatures = null;
        try {
            additionalFeatures = item.getRelatedByAnd("OrderAdjustment", UtilMisc.toMap("orderAdjustmentTypeId", "ADDITIONAL_FEATURE"));
        } catch (GenericEntityException e) {
            Debug.logError(e, "Unable to get OrderAdjustment from item : " + item, module);
        }
        if (additionalFeatures != null) {
            Iterator<GenericValue> afi = additionalFeatures.iterator();
            while (afi.hasNext()) {
                GenericValue adj = afi.next();
                String featureId = adj.getString("productFeatureId");
                if (featureId != null) {
                    featureSet.add(featureId);
                }
            }
        }

        return featureSet;
    }

    public Map<String, BigDecimal> getFeatureIdQtyMap(String shipGroupSeqId) {
        Map<String, BigDecimal> featureMap = FastMap.newInstance();
        List<GenericValue> validItems = getValidOrderItems(shipGroupSeqId);
        if (validItems != null) {
            Iterator<GenericValue> i = validItems.iterator();
            while (i.hasNext()) {
                GenericValue item = i.next();
                List<GenericValue> featureAppls = null;
                if (item.get("productId") != null) {
                    try {
                        featureAppls = item.getDelegator().findByAndCache("ProductFeatureAppl", UtilMisc.toMap("productId", item.getString("productId")));
                        List<EntityExpr> filterExprs = UtilMisc.toList(EntityCondition.makeCondition("productFeatureApplTypeId", EntityOperator.EQUALS, "STANDARD_FEATURE"));
                        filterExprs.add(EntityCondition.makeCondition("productFeatureApplTypeId", EntityOperator.EQUALS, "REQUIRED_FEATURE"));
                        featureAppls = EntityUtil.filterByOr(featureAppls, filterExprs);
                    } catch (GenericEntityException e) {
                        Debug.logError(e, "Unable to get ProductFeatureAppl for item : " + item, module);
                    }
                    if (featureAppls != null) {
                        Iterator<GenericValue> fai = featureAppls.iterator();
                        while (fai.hasNext()) {
                            GenericValue appl = fai.next();
                            BigDecimal lastQuantity = featureMap.get(appl.getString("productFeatureId"));
                            if (lastQuantity == null) {
                                lastQuantity = BigDecimal.ZERO;
                            }
                            BigDecimal newQuantity = lastQuantity.add(getOrderItemQuantity(item));
                            featureMap.put(appl.getString("productFeatureId"), newQuantity);
                        }
                    }
                }

                // get the ADDITIONAL_FEATURE adjustments
                List<GenericValue> additionalFeatures = null;
                try {
                    additionalFeatures = item.getRelatedByAnd("OrderAdjustment", UtilMisc.toMap("orderAdjustmentTypeId", "ADDITIONAL_FEATURE"));
                } catch (GenericEntityException e) {
                    Debug.logError(e, "Unable to get OrderAdjustment from item : " + item, module);
                }
                if (additionalFeatures != null) {
                    Iterator<GenericValue> afi = additionalFeatures.iterator();
                    while (afi.hasNext()) {
                        GenericValue adj = afi.next();
                        String featureId = adj.getString("productFeatureId");
                        if (featureId != null) {
                            BigDecimal lastQuantity = featureMap.get(featureId);
                            if (lastQuantity == null) {
                                lastQuantity = BigDecimal.ZERO;
                            }
                            BigDecimal newQuantity = lastQuantity.add(getOrderItemQuantity(item));
                            featureMap.put(featureId, newQuantity);
                        }
                    }
                }
            }
        }

        return featureMap;
    }

    public boolean shippingApplies() {
        boolean shippingApplies = false;
        List<GenericValue> validItems = this.getValidOrderItems();
        if (validItems != null) {
            Iterator<GenericValue> i = validItems.iterator();
            while (i.hasNext()) {
                GenericValue item = i.next();
                GenericValue product = null;
                try {
                    product = item.getRelatedOne("Product");
                } catch (GenericEntityException e) {
                    Debug.logError(e, "Problem getting Product from OrderItem; returning 0", module);
                }
                if (product != null) {
                    if (ProductWorker.shippingApplies(product)) {
                        shippingApplies = true;
                        break;
                    }
                }
            }
        }
        return shippingApplies;
    }

    public boolean taxApplies() {
        boolean taxApplies = false;
        List<GenericValue> validItems = this.getValidOrderItems();
        if (validItems != null) {
            Iterator<GenericValue> i = validItems.iterator();
            while (i.hasNext()) {
                GenericValue item = i.next();
                GenericValue product = null;
                try {
                    product = item.getRelatedOne("Product");
                } catch (GenericEntityException e) {
                    Debug.logError(e, "Problem getting Product from OrderItem; returning 0", module);
                }
                if (product != null) {
                    if (ProductWorker.taxApplies(product)) {
                        taxApplies = true;
                        break;
                    }
                }
            }
        }
        return taxApplies;
    }

    public BigDecimal getShippableTotal(String shipGroupSeqId) {
        BigDecimal shippableTotal = ZERO;
        List<GenericValue> validItems = getValidOrderItems(shipGroupSeqId);
        if (validItems != null) {
            Iterator<GenericValue> i = validItems.iterator();
            while (i.hasNext()) {
                GenericValue item = i.next();
                GenericValue product = null;
                try {
                    product = item.getRelatedOne("Product");
                } catch (GenericEntityException e) {
                    Debug.logError(e, "Problem getting Product from OrderItem; returning 0", module);
                    return ZERO;
                }
                if (product != null) {
                    if (ProductWorker.shippingApplies(product)) {
                        shippableTotal = shippableTotal.add(OrderReadHelper.getOrderItemSubTotal(item, getAdjustments(), false, true)).setScale(scale, rounding);
                    }
                }
            }
        }
        return shippableTotal.setScale(scale, rounding);
    }

    public BigDecimal getShippableQuantity() {
        BigDecimal shippableQuantity = ZERO;
        List<GenericValue> shipGroups = getOrderItemShipGroups();
        if (UtilValidate.isNotEmpty(shipGroups)) {
            Iterator<GenericValue> shipGroupsIt = shipGroups.iterator();
            while (shipGroupsIt.hasNext()) {
                GenericValue shipGroup = shipGroupsIt.next();
                shippableQuantity = shippableQuantity.add(getShippableQuantity(shipGroup.getString("shipGroupSeqId")));
            }
        }
        return shippableQuantity.setScale(scale, rounding);
    }

    public BigDecimal getShippableQuantity(String shipGroupSeqId) {
        BigDecimal shippableQuantity = ZERO;
        List<GenericValue> validItems = getValidOrderItems(shipGroupSeqId);
        if (validItems != null) {
            Iterator<GenericValue> i = validItems.iterator();
            while (i.hasNext()) {
                GenericValue item = i.next();
                GenericValue product = null;
                try {
                    product = item.getRelatedOne("Product");
                } catch (GenericEntityException e) {
                    Debug.logError(e, "Problem getting Product from OrderItem; returning 0", module);
                    return ZERO;
                }
                if (product != null) {
                    if (ProductWorker.shippingApplies(product)) {
                        shippableQuantity = shippableQuantity.add(getOrderItemQuantity(item)).setScale(scale, rounding);
                    }
                }
            }
        }
        return shippableQuantity.setScale(scale, rounding);
    }

    public BigDecimal getShippableWeight(String shipGroupSeqId) {
        BigDecimal shippableWeight = ZERO;
        List<GenericValue> validItems = getValidOrderItems(shipGroupSeqId);
        if (validItems != null) {
            Iterator<GenericValue> i = validItems.iterator();
            while (i.hasNext()) {
                GenericValue item = i.next();
                shippableWeight = shippableWeight.add(this.getItemWeight(item).multiply(getOrderItemQuantity(item))).setScale(scale, rounding);
            }
        }

        return shippableWeight.setScale(scale, rounding);
    }

    public BigDecimal getItemWeight(GenericValue item) {
        Delegator delegator = orderHeader.getDelegator();
        BigDecimal itemWeight = ZERO;

        GenericValue product = null;
        try {
            product = item.getRelatedOne("Product");
        } catch (GenericEntityException e) {
            Debug.logError(e, "Problem getting Product from OrderItem; returning 0", module);
            return BigDecimal.ZERO;
        }
        if (product != null) {
            if (ProductWorker.shippingApplies(product)) {
                BigDecimal weight = product.getBigDecimal("weight");
                String isVariant = product.getString("isVariant");
                if (weight == null && "Y".equals(isVariant)) {
                    // get the virtual product and check its weight
                    try {
                        String virtualId = ProductWorker.getVariantVirtualId(product);
                        if (UtilValidate.isNotEmpty(virtualId)) {
                            GenericValue virtual = delegator.findByPrimaryKeyCache("Product", UtilMisc.toMap("productId", virtualId));
                            if (virtual != null) {
                                weight = virtual.getBigDecimal("weight");
                            }
                        }
                    } catch (GenericEntityException e) {
                        Debug.logError(e, "Problem getting virtual product");
                    }
                }

                if (weight != null) {
                    itemWeight = weight;
                }
            }
        }

        return itemWeight;
    }

    public List<BigDecimal> getShippableSizes() {
        List<BigDecimal> shippableSizes = FastList.newInstance();

        List<GenericValue> validItems = getValidOrderItems();
        if (validItems != null) {
            Iterator<GenericValue> i = validItems.iterator();
            while (i.hasNext()) {
                GenericValue item = i.next();
                shippableSizes.add(this.getItemSize(item));
            }
        }
        return shippableSizes;
    }

    /**
     * Get the total payment preference amount by payment type.  Specify null to get amount
     * for all preference types.  TODO: filter by status as well?
     */
    public BigDecimal getOrderPaymentPreferenceTotalByType(String paymentMethodTypeId) {
        BigDecimal total = ZERO;
        for (Iterator<GenericValue> iter = getPaymentPreferences().iterator(); iter.hasNext();) {
            GenericValue preference = iter.next();
            if (preference.get("maxAmount") == null) continue;
            if (paymentMethodTypeId == null || paymentMethodTypeId.equals(preference.get("paymentMethodTypeId"))) {
                total = total.add(preference.getBigDecimal("maxAmount")).setScale(scale, rounding);
            }
        }
        return total;
    }

    public BigDecimal getCreditCardPaymentPreferenceTotal() {
        return getOrderPaymentPreferenceTotalByType("CREDIT_CARD");
    }

    public BigDecimal getBillingAccountPaymentPreferenceTotal() {
        return getOrderPaymentPreferenceTotalByType("EXT_BILLACT");
    }

    public BigDecimal getGiftCardPaymentPreferenceTotal() {
        return getOrderPaymentPreferenceTotalByType("GIFT_CARD");
    }

    /**
     * Get the total payment received amount by payment type.  Specify null to get amount
     * over all types. This method works by going through all the PaymentAndApplications
     * for all order Invoices that have status PMNT_RECEIVED.
     */
    public BigDecimal getOrderPaymentReceivedTotalByType(String paymentMethodTypeId) {
        BigDecimal total = ZERO;

        try {
            // get a set of invoice IDs that belong to the order
            List<GenericValue> orderItemBillings = orderHeader.getRelatedCache("OrderItemBilling");
            Set<String> invoiceIds = new HashSet<String>();
            for (Iterator<GenericValue> iter = orderItemBillings.iterator(); iter.hasNext();) {
                GenericValue orderItemBilling = iter.next();
                invoiceIds.add(orderItemBilling.getString("invoiceId"));
            }

            // get the payments of the desired type for these invoices TODO: in models where invoices can have many orders, this needs to be refined
            List<EntityExpr> conditions = UtilMisc.toList(
                    EntityCondition.makeCondition("statusId", EntityOperator.EQUALS, "PMNT_RECEIVED"),
                    EntityCondition.makeCondition("invoiceId", EntityOperator.IN, invoiceIds));
            if (paymentMethodTypeId != null) {
                conditions.add(EntityCondition.makeCondition("paymentMethodTypeId", EntityOperator.EQUALS, paymentMethodTypeId));
            }
            EntityConditionList<EntityExpr> ecl = EntityCondition.makeCondition(conditions, EntityOperator.AND);
            List<GenericValue> payments = orderHeader.getDelegator().findList("PaymentAndApplication", ecl, null, null, null, true);

            for (Iterator<GenericValue> iter = payments.iterator(); iter.hasNext();) {
                GenericValue payment = iter.next();
                if (payment.get("amountApplied") == null) continue;
                total = total.add(payment.getBigDecimal("amountApplied")).setScale(scale, rounding);
            }
        } catch (GenericEntityException e) {
            Debug.logError(e, e.getMessage(), module);
        }
        return total;
    }

    public BigDecimal getItemSize(GenericValue item) {
        Delegator delegator = orderHeader.getDelegator();
        BigDecimal size = BigDecimal.ZERO;

        GenericValue product = null;
        try {
            product = item.getRelatedOne("Product");
        } catch (GenericEntityException e) {
            Debug.logError(e, "Problem getting Product from OrderItem", module);
            return BigDecimal.ZERO;
        }
        if (product != null) {
            if (ProductWorker.shippingApplies(product)) {
                BigDecimal height = product.getBigDecimal("shippingHeight");
                BigDecimal width = product.getBigDecimal("shippingWidth");
                BigDecimal depth = product.getBigDecimal("shippingDepth");
                String isVariant = product.getString("isVariant");
                if ((height == null || width == null || depth == null) && "Y".equals(isVariant)) {
                    // get the virtual product and check its values
                    try {
                        String virtualId = ProductWorker.getVariantVirtualId(product);
                        if (UtilValidate.isNotEmpty(virtualId)) {
                            GenericValue virtual = delegator.findByPrimaryKeyCache("Product", UtilMisc.toMap("productId", virtualId));
                            if (virtual != null) {
                                if (height == null) height = virtual.getBigDecimal("shippingHeight");
                                if (width == null) width = virtual.getBigDecimal("shippingWidth");
                                if (depth == null) depth = virtual.getBigDecimal("shippingDepth");
                            }
                        }
                    } catch (GenericEntityException e) {
                        Debug.logError(e, "Problem getting virtual product");
                    }
                }

                if (height == null) height = BigDecimal.ZERO;
                if (width == null) width = BigDecimal.ZERO;
                if (depth == null) depth = BigDecimal.ZERO;

                // determine girth (longest field is length)
                BigDecimal[] sizeInfo = { height, width, depth };
                Arrays.sort(sizeInfo);

                size = sizeInfo[0].multiply(new BigDecimal("2")).add(sizeInfo[1].multiply(new BigDecimal("2"))).add(sizeInfo[2]);
            }
        }

        return size;
    }

    public long getItemPiecesIncluded(GenericValue item) {
        Delegator delegator = orderHeader.getDelegator();
        long piecesIncluded = 1;

        GenericValue product = null;
        try {
            product = item.getRelatedOne("Product");
        } catch (GenericEntityException e) {
            Debug.logError(e, "Problem getting Product from OrderItem; returning 1", module);
            return 1;
        }
        if (product != null) {
            if (ProductWorker.shippingApplies(product)) {
                Long pieces = product.getLong("piecesIncluded");
                String isVariant = product.getString("isVariant");
                if (pieces == null && isVariant != null && "Y".equals(isVariant)) {
                    // get the virtual product and check its weight
                    GenericValue virtual = null;
                    try {
                        List<GenericValue> virtuals = delegator.findByAnd("ProductAssoc", UtilMisc.toMap("productIdTo", product.getString("productId"), "productAssocTypeId", "PRODUCT_VARIANT"), UtilMisc.toList("-fromDate"));
                        virtuals = EntityUtil.filterByDate(virtuals);
                        virtual = EntityUtil.getFirst(virtuals);
                    } catch (GenericEntityException e) {
                        Debug.logError(e, "Problem getting virtual product");
                    }
                    if (virtual != null) {
                        try {
                            GenericValue virtualProduct = virtual.getRelatedOne("MainProduct");
                            pieces = virtualProduct.getLong("piecesIncluded");
                        } catch (GenericEntityException e) {
                            Debug.logError(e, "Problem getting virtual product");
                        }
                    }
                }

                if (pieces != null) {
                    piecesIncluded = pieces.longValue();
                }
            }
        }

        return piecesIncluded;
    }

   public List<Map<String, Object>> getShippableItemInfo(String shipGroupSeqId) {
        List<Map<String, Object>> shippableInfo = FastList.newInstance();

        List<GenericValue> validItems = getValidOrderItems(shipGroupSeqId);
        if (validItems != null) {
            Iterator<GenericValue> i = validItems.iterator();
            while (i.hasNext()) {
                GenericValue item = i.next();
                shippableInfo.add(this.getItemInfoMap(item));
            }
        }

        return shippableInfo;
    }

    public Map<String, Object> getItemInfoMap(GenericValue item) {
        Map<String, Object> itemInfo = FastMap.newInstance();
        itemInfo.put("productId", item.getString("productId"));
        itemInfo.put("quantity", getOrderItemQuantity(item));
        itemInfo.put("weight", this.getItemWeight(item));
        itemInfo.put("size",  this.getItemSize(item));
        itemInfo.put("piecesIncluded", Long.valueOf(this.getItemPiecesIncluded(item)));
        itemInfo.put("featureSet", this.getItemFeatureSet(item));
        return itemInfo;
    }

    public String getOrderEmailString() {
        Delegator delegator = orderHeader.getDelegator();
        // get the email addresses from the order contact mech(s)
        List<GenericValue> orderContactMechs = null;
        try {
            Map<String, Object> ocFields = UtilMisc.toMap("orderId", orderHeader.get("orderId"), "contactMechPurposeTypeId", "ORDER_EMAIL");
            orderContactMechs = delegator.findByAnd("OrderContactMech", ocFields);
        } catch (GenericEntityException e) {
            Debug.logWarning(e, "Problems getting order contact mechs", module);
        }

        StringBuilder emails = new StringBuilder();
        if (orderContactMechs != null) {
            Iterator<GenericValue> oci = orderContactMechs.iterator();
            while (oci.hasNext()) {
                try {
                    GenericValue orderContactMech = oci.next();
                    GenericValue contactMech = orderContactMech.getRelatedOne("ContactMech");
                    emails.append(emails.length() > 0 ? "," : "").append(contactMech.getString("infoString"));
                } catch (GenericEntityException e) {
                    Debug.logWarning(e, "Problems getting contact mech from order contact mech", module);
                }
            }
        }
        return emails.toString();
    }

    public BigDecimal getOrderGrandTotal() {
        if (totalPrice == null) {
            totalPrice = getOrderGrandTotal(getValidOrderItems(), getAdjustments());
        }// else already set
        return totalPrice;
    }

    /**
     * Gets the amount open on the order that is not covered by the relevant OrderPaymentPreferences.
     * This works by adding up the amount allocated to each unprocessed OrderPaymentPreference and the
     * amounts received and refunded as payments for the settled ones.
     */
    public BigDecimal getOrderOpenAmount() throws GenericEntityException {
        BigDecimal total = getOrderGrandTotal();
        BigDecimal openAmount = BigDecimal.ZERO;
        List<GenericValue> prefs = getPaymentPreferences();

        // add up the covered amount, but skip preferences which are declined or cancelled
        for (Iterator<GenericValue> iter = prefs.iterator(); iter.hasNext();) {
            GenericValue pref = iter.next();
            if ("PAYMENT_CANCELLED".equals(pref.get("statusId")) || "PAYMENT_DECLINED".equals(pref.get("statusId"))) {
                continue;
            } else if ("PAYMENT_SETTLED".equals(pref.get("statusId"))) {
                List<GenericValue> responses = pref.getRelatedByAnd("PaymentGatewayResponse", UtilMisc.toMap("transCodeEnumId", "PGT_CAPTURE"));
                for (Iterator<GenericValue> respIter = responses.iterator(); respIter.hasNext();) {
                    GenericValue response = respIter.next();
                    BigDecimal amount = response.getBigDecimal("amount");
                    if (amount != null) {
                        openAmount = openAmount.add(amount);
                    }
                }
                responses = pref.getRelatedByAnd("PaymentGatewayResponse", UtilMisc.toMap("transCodeEnumId", "PGT_REFUND"));
                for (Iterator<GenericValue> respIter = responses.iterator(); respIter.hasNext();) {
                    GenericValue response = respIter.next();
                    BigDecimal amount = response.getBigDecimal("amount");
                    if (amount != null) {
                        openAmount = openAmount.subtract(amount);
                    }
                }
            } else {
                // all others are currently "unprocessed" payment preferences
                BigDecimal maxAmount = pref.getBigDecimal("maxAmount");
                if (maxAmount != null) {
                    openAmount = openAmount.add(maxAmount);
                }
            }
        }
        openAmount = total.subtract(openAmount).setScale(scale, rounding);
        // return either a positive amount or positive zero
        return openAmount.compareTo(BigDecimal.ZERO) > 0 ? openAmount : BigDecimal.ZERO;
    }

    public List<GenericValue> getOrderHeaderAdjustments() {
        return getOrderHeaderAdjustments(getAdjustments(), null);
    }

    public List<GenericValue> getOrderHeaderAdjustments(String shipGroupSeqId) {
        return getOrderHeaderAdjustments(getAdjustments(), shipGroupSeqId);
    }

    public List<GenericValue> getOrderHeaderAdjustmentsTax(String shipGroupSeqId) {
        return filterOrderAdjustments(getOrderHeaderAdjustments(getAdjustments(), shipGroupSeqId), false, true, false, false, false);
    }

    public List<GenericValue> getOrderHeaderAdjustmentsToShow() {
        return filterOrderAdjustments(getOrderHeaderAdjustments(), true, false, false, false, false);
    }

    public List<GenericValue> getOrderHeaderStatuses() {
        return getOrderHeaderStatuses(getOrderStatuses());
    }

    public BigDecimal getOrderAdjustmentsTotal() {
        return getOrderAdjustmentsTotal(getValidOrderItems(), getAdjustments());
    }

    public BigDecimal getOrderAdjustmentTotal(GenericValue adjustment) {
        return calcOrderAdjustment(adjustment, getOrderItemsSubTotal());
    }

    public int hasSurvey() {
        Delegator delegator = orderHeader.getDelegator();
        List<GenericValue> surveys = null;
        try {
            surveys = delegator.findByAnd("SurveyResponse", UtilMisc.toMap("orderId", orderHeader.getString("orderId")));
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
        }
        int size = 0;
        if (surveys != null) {
            size = surveys.size();
        }

        return size;
    }

    // ========================================
    // ========== Order Item Methods ==========
    // ========================================

    public List<GenericValue> getOrderItems() {
        if (orderItems == null) {
            try {
                orderItems = orderHeader.getRelated("OrderItem", UtilMisc.toList("orderItemSeqId"));
            } catch (GenericEntityException e) {
                Debug.logWarning(e, module);
            }
        }
        return orderItems;
    }

    public List<GenericValue> getOrderItemAndShipGroupAssoc() {
        if (orderItemAndShipGrp == null) {
            try {
                orderItemAndShipGrp = orderHeader.getDelegator().findByAnd("OrderItemAndShipGroupAssoc",
                        UtilMisc.toMap("orderId", orderHeader.getString("orderId")));
            } catch (GenericEntityException e) {
                Debug.logWarning(e, module);
            }
        }
        return orderItemAndShipGrp;
    }

    public List<GenericValue> getOrderItemAndShipGroupAssoc(String shipGroupSeqId) {
        List<EntityExpr> exprs = UtilMisc.toList(EntityCondition.makeCondition("shipGroupSeqId", EntityOperator.EQUALS, shipGroupSeqId));
        return EntityUtil.filterByAnd(getOrderItemAndShipGroupAssoc(), exprs);
    }

    public List<GenericValue> getValidOrderItems() {
        List<EntityExpr> exprs = UtilMisc.toList(
                EntityCondition.makeCondition("statusId", EntityOperator.NOT_EQUAL, "ITEM_CANCELLED"),
                EntityCondition.makeCondition("statusId", EntityOperator.NOT_EQUAL, "ITEM_REJECTED"));
        return EntityUtil.filterByAnd(getOrderItems(), exprs);
    }

    public boolean getPastEtaOrderItems(String orderId) {
        /*List exprs = UtilMisc.toList(EntityCondition.makeCondition("statusId", EntityOperator.EQUALS, "ITEM_APPROVED"));
        List itemsApproved = EntityUtil.filterByAnd(getOrderItems(), exprs);
        Iterator i = itemsApproved.iterator();
        while (i.hasNext()) {
            GenericValue item = (GenericValue) i.next();
            Timestamp estimatedDeliveryDate = (Timestamp) item.get("estimatedDeliveryDate");
            if (estimatedDeliveryDate != null && UtilDateTime.nowTimestamp().after(estimatedDeliveryDate)) {
            return true;
            }
        }
        return false;
    }*/
        Delegator delegator = orderHeader.getDelegator();
        GenericValue orderDeliverySchedule = null;
        try {
            orderDeliverySchedule = delegator.findByPrimaryKey("OrderDeliverySchedule", UtilMisc.toMap("orderId", orderId, "orderItemSeqId", "_NA_"));
        } catch (GenericEntityException e) {
        }
        Timestamp estimatedShipDate = null;
        if (orderDeliverySchedule != null && orderDeliverySchedule.get("estimatedReadyDate") != null) {
            estimatedShipDate = orderDeliverySchedule.getTimestamp("estimatedReadyDate");
        }
        if (estimatedShipDate != null && UtilDateTime.nowTimestamp().after(estimatedShipDate)) {
            return true;
        }
        return false;
    }

    public boolean getRejectedOrderItems() {
        List<GenericValue> items = getOrderItems();
        Iterator<GenericValue> i = items.iterator();
        while (i.hasNext()) {
            GenericValue item = i.next();
            List<GenericValue> receipts = null;
            try {
                receipts = item.getRelated("ShipmentReceipt");
            } catch (GenericEntityException e) {
                Debug.logWarning(e, module);
            }
            if (UtilValidate.isNotEmpty(receipts)) {
                Iterator<GenericValue> recIter = receipts.iterator();
                while (recIter.hasNext()) {
                    GenericValue rec = recIter.next();
                    BigDecimal rejected = rec.getBigDecimal("quantityRejected");
                    if (rejected != null && rejected.compareTo(BigDecimal.ZERO) > 0) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean getPartiallyReceivedItems() {
        /*List exprs = UtilMisc.toList(EntityCondition.makeCondition("statusId", EntityOperator.EQUALS, "ITEM_APPROVED"));
        List itemsApproved = EntityUtil.filterByAnd(getOrderItems(), exprs);
        Iterator i = itemsApproved.iterator();
        while (i.hasNext()) {
            GenericValue item = (GenericValue) i.next();
            int shippedQuantity = (int) getItemShippedQuantity(item);
            BigDecimal orderedQuantity = (BigDecimal) item.get("quantity");
            if (shippedQuantity != orderedQuantity.intValue() && shippedQuantity > 0) {
            return true;
            }
        }
        return false;
    }*/
        List<GenericValue> items = getOrderItems();
        Iterator<GenericValue> i = items.iterator();
        while (i.hasNext()) {
            GenericValue item = i.next();
            List<GenericValue> receipts = null;
            try {
                receipts = item.getRelated("ShipmentReceipt");
            } catch (GenericEntityException e) {
                Debug.logWarning(e, module);
            }
            if (UtilValidate.isNotEmpty(receipts)) {
                Iterator<GenericValue> recIter = receipts.iterator();
                while (recIter.hasNext()) {
                    GenericValue rec = recIter.next();
                    BigDecimal acceptedQuantity = rec.getBigDecimal("quantityAccepted");
                    BigDecimal orderedQuantity = (BigDecimal) item.get("quantity");
                    if (acceptedQuantity.intValue() != orderedQuantity.intValue() && acceptedQuantity.intValue()  > 0) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public List<GenericValue> getValidOrderItems(String shipGroupSeqId) {
        if (shipGroupSeqId == null) return getValidOrderItems();
        List<EntityExpr> exprs = UtilMisc.toList(
                EntityCondition.makeCondition("statusId", EntityOperator.NOT_EQUAL, "ITEM_CANCELLED"),
                EntityCondition.makeCondition("statusId", EntityOperator.NOT_EQUAL, "ITEM_REJECTED"),
                EntityCondition.makeCondition("shipGroupSeqId", EntityOperator.EQUALS, shipGroupSeqId));
        return EntityUtil.filterByAnd(getOrderItemAndShipGroupAssoc(), exprs);
    }

    public GenericValue getOrderItem(String orderItemSeqId) {
        List<EntityExpr> exprs = UtilMisc.toList(EntityCondition.makeCondition("orderItemSeqId", EntityOperator.EQUALS, orderItemSeqId));
        return EntityUtil.getFirst(EntityUtil.filterByAnd(getOrderItems(), exprs));
    }

    public List<GenericValue> getValidDigitalItems() {
        List<GenericValue> digitalItems = FastList.newInstance();
        // only approved or complete items apply
        List<EntityExpr> exprs = UtilMisc.toList(
                EntityCondition.makeCondition("statusId", EntityOperator.EQUALS, "ITEM_APPROVED"),
                EntityCondition.makeCondition("statusId", EntityOperator.EQUALS, "ITEM_COMPLETED"));
        List<GenericValue> items = EntityUtil.filterByOr(getOrderItems(), exprs);
        Iterator<GenericValue> i = items.iterator();
        while (i.hasNext()) {
            GenericValue item = i.next();
            if (item.get("productId") != null) {
                GenericValue product = null;
                try {
                    product = item.getRelatedOne("Product");
                } catch (GenericEntityException e) {
                    Debug.logError(e, "Unable to get Product from OrderItem", module);
                }
                if (product != null) {
                    GenericValue productType = null;
                    try {
                        productType = product.getRelatedOne("ProductType");
                    } catch (GenericEntityException e) {
                        Debug.logError(e, "ERROR: Unable to get ProductType from Product", module);
                    }

                    if (productType != null) {
                        String isDigital = productType.getString("isDigital");

                        if (isDigital != null && "Y".equalsIgnoreCase(isDigital)) {
                            // make sure we have an OrderItemBilling record
                            List<GenericValue> orderItemBillings = null;
                            try {
                                orderItemBillings = item.getRelated("OrderItemBilling");
                            } catch (GenericEntityException e) {
                                Debug.logError(e, "Unable to get OrderItemBilling from OrderItem");
                            }

                            if (UtilValidate.isNotEmpty(orderItemBillings)) {
                                // get the ProductContent records
                                List<GenericValue> productContents = null;
                                try {
                                    productContents = product.getRelated("ProductContent");
                                } catch (GenericEntityException e) {
                                    Debug.logError("Unable to get ProductContent from Product", module);
                                }
                                List<EntityExpr> cExprs = UtilMisc.toList(
                                        EntityCondition.makeCondition("productContentTypeId", EntityOperator.EQUALS, "DIGITAL_DOWNLOAD"),
                                        EntityCondition.makeCondition("productContentTypeId", EntityOperator.EQUALS, "FULFILLMENT_EMAIL"),
                                        EntityCondition.makeCondition("productContentTypeId", EntityOperator.EQUALS, "FULFILLMENT_EXTERNAL"));
                                // add more as needed
                                productContents = EntityUtil.filterByDate(productContents);
                                productContents = EntityUtil.filterByOr(productContents, cExprs);

                                if (UtilValidate.isNotEmpty(productContents)) {
                                    // make sure we are still within the allowed timeframe and use limits
                                    Iterator<GenericValue> pci = productContents.iterator();
                                    while (pci.hasNext()) {
                                        GenericValue productContent = pci.next();
                                        Timestamp fromDate = productContent.getTimestamp("purchaseFromDate");
                                        Timestamp thruDate = productContent.getTimestamp("purchaseThruDate");
                                        if (fromDate == null || item.getTimestamp("orderDate").after(fromDate)) {
                                            if (thruDate == null || item.getTimestamp("orderDate").before(thruDate)) {
                                                // TODO: Implement use count and days
                                                digitalItems.add(item);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return digitalItems;
    }

    public List<GenericValue> getOrderItemAdjustments(GenericValue orderItem) {
        return getOrderItemAdjustmentList(orderItem, getAdjustments());
    }

    public String getCurrentOrderItemWorkEffort(GenericValue orderItem)    {
        String orderItemSeqId = orderItem.getString("orderItemSeqId");
        String orderId = orderItem.getString("orderId");
        Delegator delegator = orderItem.getDelegator();
        GenericValue workOrderItemFulFillment = null;
        GenericValue workEffort = null;
        try {
            List<GenericValue> workOrderItemFulFillments = delegator.findByAndCache("WorkOrderItemFulfillment", UtilMisc.toMap("orderId", orderId, "orderItemSeqId", orderItemSeqId));
            if (!UtilValidate.isEmpty(workOrderItemFulFillments)) {
                workOrderItemFulFillment = EntityUtil.getFirst(workOrderItemFulFillments);
                workEffort = workOrderItemFulFillment.getRelatedOne("WorkEffort");
            }
        } catch (GenericEntityException e) {
            return null;
        }
        if (workEffort != null) {
            return workEffort.getString("workEffortId");
        } else {
            return null;
        }
    }

    public String getCurrentItemStatus(GenericValue orderItem) {
        GenericValue statusItem = null;
        try {
            statusItem = orderItem.getRelatedOne("StatusItem");
        } catch (GenericEntityException e) {
            Debug.logError(e, "Trouble getting StatusItem : " + orderItem, module);
        }
        if (statusItem == null || statusItem.get("description") == null) {
            return "Not Available";
        } else {
            return statusItem.getString("description");
        }
    }

    public List<GenericValue> getOrderItemPriceInfos(GenericValue orderItem) {
        if (orderItem == null) return null;
        if (this.orderItemPriceInfos == null) {
            Delegator delegator = orderHeader.getDelegator();

            try {
                orderItemPriceInfos = delegator.findByAnd("OrderItemPriceInfo", UtilMisc.toMap("orderId", orderHeader.get("orderId")));
            } catch (GenericEntityException e) {
                Debug.logWarning(e, module);
            }
        }
        String orderItemSeqId = (String) orderItem.get("orderItemSeqId");

        return EntityUtil.filterByAnd(this.orderItemPriceInfos, UtilMisc.toMap("orderItemSeqId", orderItemSeqId));
    }

    public List<GenericValue> getOrderItemShipGroupAssocs(GenericValue orderItem) {
        if (orderItem == null) return null;
        try {
            return orderHeader.getDelegator().findByAnd("OrderItemShipGroupAssoc",
                    UtilMisc.toMap("orderId", orderItem.getString("orderId"), "orderItemSeqId", orderItem.getString("orderItemSeqId")), UtilMisc.toList("shipGroupSeqId"));
        } catch (GenericEntityException e) {
            Debug.logWarning(e, module);
        }
        return null;
    }

    public List<GenericValue> getOrderItemShipGrpInvResList(GenericValue orderItem) {
        if (orderItem == null) return null;
        if (this.orderItemShipGrpInvResList == null) {
            Delegator delegator = orderItem.getDelegator();
            try {
                orderItemShipGrpInvResList = delegator.findByAnd("OrderItemShipGrpInvRes", UtilMisc.toMap("orderId", orderItem.get("orderId")));
            } catch (GenericEntityException e) {
                Debug.logWarning(e, "Trouble getting OrderItemShipGrpInvRes List", module);
            }
        }
        return EntityUtil.filterByAnd(orderItemShipGrpInvResList, UtilMisc.toMap("orderItemSeqId", orderItem.getString("orderItemSeqId")));
    }

    public List<GenericValue> getOrderItemIssuances(GenericValue orderItem) {
        return this.getOrderItemIssuances(orderItem, null);
    }

    public List<GenericValue> getOrderItemIssuances(GenericValue orderItem, String shipmentId) {
        if (orderItem == null) return null;
        if (this.orderItemIssuances == null) {
            Delegator delegator = orderItem.getDelegator();

            try {
                orderItemIssuances = delegator.findByAnd("ItemIssuance", UtilMisc.toMap("orderId", orderItem.get("orderId")));
            } catch (GenericEntityException e) {
                Debug.logWarning(e, "Trouble getting ItemIssuance(s)", module);
            }
        }

        // filter the issuances
        Map<String, Object> filter = UtilMisc.toMap("orderItemSeqId", orderItem.get("orderItemSeqId"));
        if (shipmentId != null) {
            filter.put("shipmentId", shipmentId);
        }
        return EntityUtil.filterByAnd(orderItemIssuances, filter);
    }

    /** Get a set of productIds in the order. */
    public Collection<String> getOrderProductIds() {
        Set<String> productIds = FastSet.newInstance();
        for (GenericValue orderItem : getOrderItems()) {
            if (orderItem.get("productId") != null) {
                productIds.add(orderItem.getString("productId"));
            }
        }        
        return productIds;
    }

    public List<GenericValue> getOrderReturnItems() {
        Delegator delegator = orderHeader.getDelegator();
        if (this.orderReturnItems == null) {
            try {
                this.orderReturnItems = delegator.findByAnd("ReturnItem", UtilMisc.toMap("orderId", orderHeader.getString("orderId")));
            } catch (GenericEntityException e) {
                Debug.logError(e, "Problem getting ReturnItem from order", module);
                return null;
            }
        }
        return this.orderReturnItems;
    }

   /**
    * Get the quantity returned per order item.
    * In other words, this method will count the ReturnItems
    * related to each OrderItem.
    *
    * @return  Map of returned quantities as BigDecimals keyed to the orderItemSeqId
    */
   public Map<String, BigDecimal> getOrderItemReturnedQuantities() {
       List<GenericValue> returnItems = getOrderReturnItems();

       // since we don't have a handy grouped view entity, we'll have to group the return items by hand
       Map<String, BigDecimal> returnMap = FastMap.newInstance();
       for (Iterator<GenericValue> iter = this.getValidOrderItems().iterator(); iter.hasNext();) {
           GenericValue orderItem = iter.next();
           List<GenericValue> group = EntityUtil.filterByAnd(returnItems, UtilMisc.toList(
                                              EntityCondition.makeCondition("orderId", orderItem.get("orderId")),
                                              EntityCondition.makeCondition("orderItemSeqId", orderItem.get("orderItemSeqId")),
                                              EntityCondition.makeCondition("statusId", EntityOperator.NOT_EQUAL, "RETURN_CANCELLED")));

           // add up the returned quantities for this group TODO: received quantity should be used eventually
           BigDecimal returned = BigDecimal.ZERO;
           for (Iterator<GenericValue> groupiter = group.iterator(); groupiter.hasNext();) {
               GenericValue returnItem = groupiter.next();
               if (returnItem.getBigDecimal("returnQuantity") != null) {
                   returned = returned.add(returnItem.getBigDecimal("returnQuantity"));
               }
           }

           // the quantity returned per order item
           returnMap.put(orderItem.getString("orderItemSeqId"), returned);
       }
       return returnMap;
   }

   /**
    * Get the total quantity of returned items for an order. This will count
    * only the ReturnItems that are directly correlated to an OrderItem.
    */
    public BigDecimal getOrderReturnedQuantity() {
        List<GenericValue> returnedItemsBase = getOrderReturnItems();
        List<GenericValue> returnedItems = new ArrayList<GenericValue>(returnedItemsBase.size());

        // filter just order items
        List<EntityExpr> orderItemExprs = UtilMisc.toList(EntityCondition.makeCondition("returnItemTypeId", EntityOperator.EQUALS, "RET_PROD_ITEM"));
        orderItemExprs.add(EntityCondition.makeCondition("returnItemTypeId", EntityOperator.EQUALS, "RET_FPROD_ITEM"));
        orderItemExprs.add(EntityCondition.makeCondition("returnItemTypeId", EntityOperator.EQUALS, "RET_DPROD_ITEM"));
        orderItemExprs.add(EntityCondition.makeCondition("returnItemTypeId", EntityOperator.EQUALS, "RET_FDPROD_ITEM"));
        orderItemExprs.add(EntityCondition.makeCondition("returnItemTypeId", EntityOperator.EQUALS, "RET_PROD_FEATR_ITEM"));
        orderItemExprs.add(EntityCondition.makeCondition("returnItemTypeId", EntityOperator.EQUALS, "RET_SPROD_ITEM"));
        orderItemExprs.add(EntityCondition.makeCondition("returnItemTypeId", EntityOperator.EQUALS, "RET_WE_ITEM"));
        orderItemExprs.add(EntityCondition.makeCondition("returnItemTypeId", EntityOperator.EQUALS, "RET_TE_ITEM"));
        returnedItemsBase = EntityUtil.filterByOr(returnedItemsBase, orderItemExprs);

        // get only the RETURN_RECEIVED and RETURN_COMPLETED statusIds
        returnedItems.addAll(EntityUtil.filterByAnd(returnedItemsBase, UtilMisc.toMap("statusId", "RETURN_RECEIVED")));
        returnedItems.addAll(EntityUtil.filterByAnd(returnedItemsBase, UtilMisc.toMap("statusId", "RETURN_COMPLETED")));

        BigDecimal returnedQuantity = ZERO;
        Iterator<GenericValue> i = returnedItems.iterator();
        while (i.hasNext()) {
            GenericValue returnedItem = i.next();
            if (returnedItem.get("returnQuantity") != null) {
                returnedQuantity = returnedQuantity.add(returnedItem.getBigDecimal("returnQuantity")).setScale(scale, rounding);
            }
        }
        return returnedQuantity.setScale(scale, rounding);
    }

    /**
     * Get the returned total by return type (credit, refund, etc.).  Specify returnTypeId = null to get sum over all
     * return types.  Specify includeAll = true to sum up over all return statuses except cancelled.  Specify includeAll
     * = false to sum up over ACCEPTED,RECEIVED And COMPLETED returns.
     */
    public BigDecimal getOrderReturnedTotalByTypeBd(String returnTypeId, boolean includeAll) {
        List<GenericValue> returnedItemsBase = getOrderReturnItems();
        if (returnTypeId != null) {
            returnedItemsBase = EntityUtil.filterByAnd(returnedItemsBase, UtilMisc.toMap("returnTypeId", returnTypeId));
        }
        List<GenericValue> returnedItems = new ArrayList<GenericValue>(returnedItemsBase.size());

        // get only the RETURN_RECEIVED and RETURN_COMPLETED statusIds
        if (!includeAll) {
            returnedItems.addAll(EntityUtil.filterByAnd(returnedItemsBase, UtilMisc.toMap("statusId", "RETURN_ACCEPTED")));
            returnedItems.addAll(EntityUtil.filterByAnd(returnedItemsBase, UtilMisc.toMap("statusId", "RETURN_RECEIVED")));
            returnedItems.addAll(EntityUtil.filterByAnd(returnedItemsBase, UtilMisc.toMap("statusId", "RETURN_COMPLETED")));
        } else {
            // otherwise get all of them except cancelled ones
            returnedItems.addAll(EntityUtil.filterByAnd(returnedItemsBase,
                    UtilMisc.toList(EntityCondition.makeCondition("statusId", EntityOperator.NOT_EQUAL, "RETURN_CANCELLED"))));
        }
        BigDecimal returnedAmount = ZERO;
        Iterator<GenericValue> i = returnedItems.iterator();
        String orderId = orderHeader.getString("orderId");
        List<String> returnHeaderList = FastList.newInstance();
        while (i.hasNext()) {
            GenericValue returnedItem = i.next();
            if ((returnedItem.get("returnPrice") != null) && (returnedItem.get("returnQuantity") != null)) {
                returnedAmount = returnedAmount.add(returnedItem.getBigDecimal("returnPrice").multiply(returnedItem.getBigDecimal("returnQuantity")).setScale(scale, rounding));
            }
            Map<String, Object> itemAdjustmentCondition = UtilMisc.toMap("returnId", returnedItem.get("returnId"), "returnItemSeqId", returnedItem.get("returnItemSeqId"), "returnTypeId", returnTypeId);
            returnedAmount = returnedAmount.add(getReturnAdjustmentTotal(orderHeader.getDelegator(), itemAdjustmentCondition));
            if (orderId.equals(returnedItem.getString("orderId")) && (!returnHeaderList.contains(returnedItem.getString("returnId")))) {
                returnHeaderList.add(returnedItem.getString("returnId"));
            }
        }
        //get  returnedAmount from returnHeader adjustments whose orderId must equals to current orderHeader.orderId
        Iterator<String> returnHeaderIterator = returnHeaderList.iterator();
        while (returnHeaderIterator.hasNext()) {
            String returnId = returnHeaderIterator.next();
            Map<String, Object> returnHeaderAdjFilter = UtilMisc.<String, Object>toMap("returnId", returnId, "returnItemSeqId", "_NA_", "returnTypeId", returnTypeId);
            returnedAmount =returnedAmount.add(getReturnAdjustmentTotal(orderHeader.getDelegator(), returnHeaderAdjFilter)).setScale(scale, rounding);
        }
        return returnedAmount.setScale(scale, rounding);
    }

    /** Gets the total return credit for COMPLETED and RECEIVED returns. */
    public BigDecimal getOrderReturnedCreditTotalBd() {
        return getOrderReturnedTotalByTypeBd("RTN_CREDIT", false);
    }

    /** Gets the total return refunded for COMPLETED and RECEIVED returns. */
    public BigDecimal getOrderReturnedRefundTotalBd() {
        return getOrderReturnedTotalByTypeBd("RTN_REFUND", false);
    }

    /** Gets the total return amount (all return types) for COMPLETED and RECEIVED returns. */
    public BigDecimal getOrderReturnedTotal() {
        return getOrderReturnedTotalByTypeBd(null, false);
    }

    /**
     * Gets the total returned over all return types.  Specify true to include all return statuses
     * except cancelled.  Specify false to include only COMPLETED and RECEIVED returns.
     */
    public BigDecimal getOrderReturnedTotal(boolean includeAll) {
        return getOrderReturnedTotalByTypeBd(null, includeAll);
    }

    public BigDecimal getOrderNonReturnedTaxAndShipping() {
        // first make a Map of orderItemSeqId key, returnQuantity value
        List<GenericValue> returnedItemsBase = getOrderReturnItems();
        List<GenericValue> returnedItems = new ArrayList<GenericValue>(returnedItemsBase.size());

        // get only the RETURN_RECEIVED and RETURN_COMPLETED statusIds
        returnedItems.addAll(EntityUtil.filterByAnd(returnedItemsBase, UtilMisc.toMap("statusId", "RETURN_RECEIVED")));
        returnedItems.addAll(EntityUtil.filterByAnd(returnedItemsBase, UtilMisc.toMap("statusId", "RETURN_COMPLETED")));

        Map<String, BigDecimal> itemReturnedQuantities = FastMap.newInstance();
        Iterator<GenericValue> i = returnedItems.iterator();
        while (i.hasNext()) {
            GenericValue returnedItem = i.next();
            String orderItemSeqId = returnedItem.getString("orderItemSeqId");
            BigDecimal returnedQuantity = returnedItem.getBigDecimal("returnQuantity");
            if (orderItemSeqId != null && returnedQuantity != null) {
                BigDecimal existingQuantity =  itemReturnedQuantities.get(orderItemSeqId);
                if (existingQuantity == null) {
                    itemReturnedQuantities.put(orderItemSeqId, returnedQuantity);
                } else {
                    itemReturnedQuantities.put(orderItemSeqId, returnedQuantity.add(existingQuantity));
                }
            }
        }

        // then go through all order items and for the quantity not returned calculate it's portion of the item, and of the entire order
        BigDecimal totalSubTotalNotReturned = ZERO;
        BigDecimal totalTaxNotReturned = ZERO;
        BigDecimal totalShippingNotReturned = ZERO;

        Iterator<GenericValue> orderItems = this.getValidOrderItems().iterator();
        while (orderItems.hasNext()) {
            GenericValue orderItem = orderItems.next();

            BigDecimal itemQuantityDbl = orderItem.getBigDecimal("quantity");
            if (itemQuantityDbl == null || itemQuantityDbl.compareTo(ZERO) == 0) {
                continue;
            }
            BigDecimal itemQuantity = itemQuantityDbl;
            BigDecimal itemSubTotal = this.getOrderItemSubTotal(orderItem);
            BigDecimal itemTaxes = this.getOrderItemTax(orderItem);
            BigDecimal itemShipping = this.getOrderItemShipping(orderItem);

            BigDecimal quantityReturned = itemReturnedQuantities.get(orderItem.get("orderItemSeqId"));
            if (quantityReturned == null) {
                quantityReturned = BigDecimal.ZERO;
            }

            BigDecimal quantityNotReturned = itemQuantity.subtract(quantityReturned);

            // pro-rated factor (quantity not returned / total items ordered), which shouldn't be rounded to 2 decimals
            BigDecimal factorNotReturned = quantityNotReturned.divide(itemQuantity, 100, rounding);

            BigDecimal subTotalNotReturned = itemSubTotal.multiply(factorNotReturned).setScale(scale, rounding);

            // calculate tax and shipping adjustments for each item, add to accumulators
            BigDecimal itemTaxNotReturned = itemTaxes.multiply(factorNotReturned).setScale(scale, rounding);
            BigDecimal itemShippingNotReturned = itemShipping.multiply(factorNotReturned).setScale(scale, rounding);

            totalSubTotalNotReturned = totalSubTotalNotReturned.add(subTotalNotReturned);
            totalTaxNotReturned = totalTaxNotReturned.add(itemTaxNotReturned);
            totalShippingNotReturned = totalShippingNotReturned.add(itemShippingNotReturned);
        }

        // calculate tax and shipping adjustments for entire order, add to result
        BigDecimal orderItemsSubTotal = this.getOrderItemsSubTotal();
        BigDecimal orderFactorNotReturned = ZERO;
        if (orderItemsSubTotal.signum() != 0) {
            // pro-rated factor (subtotal not returned / item subtotal), which shouldn't be rounded to 2 decimals
            orderFactorNotReturned = totalSubTotalNotReturned.divide(orderItemsSubTotal, 100, rounding);
        }
        BigDecimal orderTaxNotReturned = this.getHeaderTaxTotal().multiply(orderFactorNotReturned).setScale(scale, rounding);
        BigDecimal orderShippingNotReturned = this.getShippingTotal().multiply(orderFactorNotReturned).setScale(scale, rounding);

        return totalTaxNotReturned.add(totalShippingNotReturned).add(orderTaxNotReturned).add(orderShippingNotReturned).setScale(scale, rounding);
    }

    /** Gets the total refunded to the order billing account by type.  Specify null to get total over all types. */
    public BigDecimal getBillingAccountReturnedTotalByTypeBd(String returnTypeId) {
        BigDecimal returnedAmount = ZERO;
        List<GenericValue> returnedItemsBase = getOrderReturnItems();
        if (returnTypeId != null) {
            returnedItemsBase = EntityUtil.filterByAnd(returnedItemsBase, UtilMisc.toMap("returnTypeId", returnTypeId));
        }
        List<GenericValue> returnedItems = new ArrayList<GenericValue>(returnedItemsBase.size());

        // get only the RETURN_RECEIVED and RETURN_COMPLETED statusIds
        returnedItems.addAll(EntityUtil.filterByAnd(returnedItemsBase, UtilMisc.toMap("statusId", "RETURN_RECEIVED")));
        returnedItems.addAll(EntityUtil.filterByAnd(returnedItemsBase, UtilMisc.toMap("statusId", "RETURN_COMPLETED")));

        // sum up the return items that have a return item response with a billing account defined
        try {
            for (Iterator<GenericValue> iter = returnedItems.iterator(); iter.hasNext();) {
                GenericValue returnItem = iter.next();
                GenericValue returnItemResponse = returnItem.getRelatedOne("ReturnItemResponse");
                if (returnItemResponse == null) continue;
                if (returnItemResponse.get("billingAccountId") == null) continue;

                // we can just add the response amounts
                returnedAmount = returnedAmount.add(returnItemResponse.getBigDecimal("responseAmount")).setScale(scale, rounding);
            }
        } catch (GenericEntityException e) {
            Debug.logError(e, e.getMessage(), module);
        }
        return returnedAmount;
    }

    /** Get the total return credited to the order billing accounts */
    public BigDecimal getBillingAccountReturnedCreditTotalBd() {
        return getBillingAccountReturnedTotalByTypeBd("RTN_CREDIT");
    }

    /** Get the total return refunded to the order billing accounts */
    public BigDecimal getBillingAccountReturnedRefundTotalBd() {
        return getBillingAccountReturnedTotalByTypeBd("RTN_REFUND");
    }

    /** Gets the total return credited amount with refunds and credits to the billing account figured in */
    public BigDecimal getReturnedCreditTotalWithBillingAccountBd() {
        return getOrderReturnedCreditTotalBd().add(getBillingAccountReturnedRefundTotalBd()).subtract(getBillingAccountReturnedCreditTotalBd());
    }

    /** Gets the total return refund amount with refunds and credits to the billing account figured in */
    public BigDecimal getReturnedRefundTotalWithBillingAccountBd() {
        return getOrderReturnedRefundTotalBd().add(getBillingAccountReturnedCreditTotalBd()).subtract(getBillingAccountReturnedRefundTotalBd());
    }

    public BigDecimal getOrderBackorderQuantity() {
        BigDecimal backorder = ZERO;
        List<GenericValue> items = this.getValidOrderItems();
        if (items != null) {
            Iterator<GenericValue> ii = items.iterator();
            while (ii.hasNext()) {
                GenericValue item = ii.next();
                List<GenericValue> reses = this.getOrderItemShipGrpInvResList(item);
                if (reses != null) {
                    Iterator<GenericValue> ri = reses.iterator();
                    while (ri.hasNext()) {
                        GenericValue res = ri.next();
                        BigDecimal nav = res.getBigDecimal("quantityNotAvailable");
                        if (nav != null) {
                            backorder = backorder.add(nav).setScale(scale, rounding);
                        }
                    }
                }
            }
        }
        return backorder.setScale(scale, rounding);
    }

    public BigDecimal getItemPickedQuantityBd(GenericValue orderItem) {
        BigDecimal quantityPicked = ZERO;
        EntityConditionList<EntityExpr> pickedConditions = EntityCondition.makeCondition(UtilMisc.toList(
                EntityCondition.makeCondition("orderId", EntityOperator.EQUALS, orderItem.get("orderId")),
                EntityCondition.makeCondition("orderItemSeqId", EntityOperator.EQUALS, orderItem.getString("orderItemSeqId")),
                EntityCondition.makeCondition("statusId", EntityOperator.NOT_EQUAL, "PICKLIST_CANCELLED")),
                EntityOperator.AND);

        List<GenericValue> picked = null;
        try {
            picked = orderHeader.getDelegator().findList("PicklistAndBinAndItem", pickedConditions, null, null, null, false);
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
            this.orderHeader = null;
        }

        if (picked != null) {
            Iterator<GenericValue> i = picked.iterator();
            while (i.hasNext()) {
                GenericValue pickedItem = i.next();
                BigDecimal issueQty = pickedItem.getBigDecimal("quantity");
                if (issueQty != null) {
                    quantityPicked = quantityPicked.add(issueQty).setScale(scale, rounding);
                }
            }
        }
        return quantityPicked.setScale(scale, rounding);
    }

    public BigDecimal getItemShippedQuantity(GenericValue orderItem) {
        BigDecimal quantityShipped = ZERO;
        List<GenericValue> issuance = getOrderItemIssuances(orderItem);
        if (issuance != null) {
            Iterator<GenericValue> i = issuance.iterator();
            while (i.hasNext()) {
                GenericValue issue = i.next();
                BigDecimal issueQty = issue.getBigDecimal("quantity");
                BigDecimal cancelQty = issue.getBigDecimal("cancelQuantity");
                if (cancelQty == null) {
                    cancelQty = ZERO;
                }
                if (issueQty == null) {
                    issueQty = ZERO;
                }
                quantityShipped = quantityShipped.add(issueQty.subtract(cancelQty)).setScale(scale, rounding);
            }
        }
        return quantityShipped.setScale(scale, rounding);
    }

    public BigDecimal getItemReservedQuantity(GenericValue orderItem) {
        BigDecimal reserved = ZERO;

        List<GenericValue> reses = getOrderItemShipGrpInvResList(orderItem);
        if (reses != null) {
            Iterator<GenericValue> i = reses.iterator();
            while (i.hasNext()) {
                GenericValue res = i.next();
                BigDecimal quantity = res.getBigDecimal("quantity");
                if (quantity != null) {
                    reserved = reserved.add(quantity).setScale(scale, rounding);
                }
            }
        }
        return reserved.setScale(scale, rounding);
    }

    public BigDecimal getItemBackorderedQuantity(GenericValue orderItem) {
        BigDecimal backOrdered = ZERO;

        Timestamp shipDate = orderItem.getTimestamp("estimatedShipDate");
        Timestamp autoCancel = orderItem.getTimestamp("autoCancelDate");

        List<GenericValue> reses = getOrderItemShipGrpInvResList(orderItem);
        if (reses != null) {
            Iterator<GenericValue> i = reses.iterator();
            while (i.hasNext()) {
                GenericValue res = i.next();
                Timestamp promised = res.getTimestamp("currentPromisedDate");
                if (promised == null) {
                    promised = res.getTimestamp("promisedDatetime");
                }
                if (autoCancel != null || (shipDate != null && shipDate.after(promised))) {
                    BigDecimal resQty = res.getBigDecimal("quantity");
                    if (resQty != null) {
                        backOrdered = backOrdered.add(resQty).setScale(scale, rounding);
                    }
                }
            }
        }
        return backOrdered;
    }

    public BigDecimal getItemPendingShipmentQuantity(GenericValue orderItem) {
        BigDecimal reservedQty = getItemReservedQuantity(orderItem);
        BigDecimal backordered = getItemBackorderedQuantity(orderItem);
        return reservedQty.subtract(backordered).setScale(scale, rounding);
    }

    public BigDecimal getItemCanceledQuantity(GenericValue orderItem) {
        BigDecimal cancelQty = orderItem.getBigDecimal("cancelQuantity");
        if (cancelQty == null) cancelQty = BigDecimal.ZERO;
        return cancelQty;
    }

    public BigDecimal getTotalOrderItemsQuantity() {
        List<GenericValue> orderItems = getValidOrderItems();
        BigDecimal totalItems = ZERO;

        for (int i = 0; i < orderItems.size(); i++) {
            GenericValue oi = orderItems.get(i);

            totalItems = totalItems.add(getOrderItemQuantity(oi)).setScale(scale, rounding);
        }
        return totalItems.setScale(scale, rounding);
    }

    public BigDecimal getTotalOrderItemsOrderedQuantity() {
        List<GenericValue> orderItems = getValidOrderItems();
        BigDecimal totalItems = ZERO;

        for (int i = 0; i < orderItems.size(); i++) {
            GenericValue oi = orderItems.get(i);

            totalItems = totalItems.add(oi.getBigDecimal("quantity")).setScale(scale, rounding);
        }
        return totalItems;
    }

    public BigDecimal getOrderItemsSubTotal() {
        return getOrderItemsSubTotal(getValidOrderItems(), getAdjustments());
    }

    public BigDecimal getOrderItemSubTotal(GenericValue orderItem) {
        return getOrderItemSubTotal(orderItem, getAdjustments());
    }

    public BigDecimal getOrderItemsTotal() {
        return getOrderItemsTotal(getValidOrderItems(), getAdjustments());
    }

    public BigDecimal getOrderItemTotal(GenericValue orderItem) {
        return getOrderItemTotal(orderItem, getAdjustments());
    }

    public BigDecimal getOrderItemTax(GenericValue orderItem) {
        return getOrderItemAdjustmentsTotal(orderItem, false, true, false);
    }

    public BigDecimal getOrderItemShipping(GenericValue orderItem) {
        return getOrderItemAdjustmentsTotal(orderItem, false, false, true);
    }

    public BigDecimal getOrderItemAdjustmentsTotal(GenericValue orderItem, boolean includeOther, boolean includeTax, boolean includeShipping) {
        return getOrderItemAdjustmentsTotal(orderItem, getAdjustments(), includeOther, includeTax, includeShipping);
    }

    public BigDecimal getOrderItemAdjustmentsTotal(GenericValue orderItem) {
        return getOrderItemAdjustmentsTotal(orderItem, true, false, false);
    }

    public BigDecimal getOrderItemAdjustmentTotal(GenericValue orderItem, GenericValue adjustment) {
        return calcItemAdjustment(adjustment, orderItem);
    }

    public String getAdjustmentType(GenericValue adjustment) {
        GenericValue adjustmentType = null;
        try {
            adjustmentType = adjustment.getRelatedOne("OrderAdjustmentType");
        } catch (GenericEntityException e) {
            Debug.logError(e, "Problems with order adjustment", module);
        }
        if (adjustmentType == null || adjustmentType.get("description") == null) {
            return "";
        } else {
            return adjustmentType.getString("description");
        }
    }

    public List<GenericValue> getOrderItemStatuses(GenericValue orderItem) {
        return getOrderItemStatuses(orderItem, getOrderStatuses());
    }

    public String getCurrentItemStatusString(GenericValue orderItem) {
        GenericValue statusItem = null;
        try {
            statusItem = orderItem.getRelatedOneCache("StatusItem");
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
        }
        if (statusItem != null) {
            return statusItem.getString("description");
        } else {
            return orderHeader.getString("statusId");
        }
    }

    /** Fetches the set of order items with the given EntityCondition. */
    public List<GenericValue> getOrderItemsByCondition(EntityCondition entityCondition) {
        return EntityUtil.filterByCondition(getOrderItems(), entityCondition);
    }

    public Set<String> getProductPromoCodesEntered() {
        Delegator delegator = orderHeader.getDelegator();
        Set<String> productPromoCodesEntered = FastSet.newInstance();
        try {
            for (GenericValue orderProductPromoCode: delegator.findByAndCache("OrderProductPromoCode", UtilMisc.toMap("orderId", orderHeader.get("orderId")))) {
                productPromoCodesEntered.add(orderProductPromoCode.getString("productPromoCodeId"));
            }
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
        }
        return productPromoCodesEntered;
    }

    public List<GenericValue> getProductPromoUse() {
        Delegator delegator = orderHeader.getDelegator();
        try {
            return delegator.findByAndCache("ProductPromoUse", UtilMisc.toMap("orderId", orderHeader.get("orderId")));
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
        }
        return FastList.newInstance();
    }

    /**
     * Checks to see if this user has read permission on this order
     * @param userLogin The UserLogin value object to check
     * @return boolean True if we have read permission
     */
    public boolean hasPermission(Security security, GenericValue userLogin) {
        return OrderReadHelper.hasPermission(security, userLogin, orderHeader);
    }

    /**
     * Getter for property orderHeader.
     * @return Value of property orderHeader.
     */
    public GenericValue getOrderHeader() {
        return orderHeader;
    }

    // ======================================================
    // =================== Static Methods ===================
    // ======================================================

    public static GenericValue getOrderHeader(Delegator delegator, String orderId) {
        GenericValue orderHeader = null;
        if (orderId != null && delegator != null) {
            try {
                orderHeader = delegator.findByPrimaryKey("OrderHeader", UtilMisc.toMap("orderId", orderId));
            } catch (GenericEntityException e) {
                Debug.logError(e, "Cannot get order header", module);
            }
        }
        return orderHeader;
    }

    public static BigDecimal getOrderItemQuantity(GenericValue orderItem) {

        BigDecimal cancelQty = orderItem.getBigDecimal("cancelQuantity");
        BigDecimal orderQty = orderItem.getBigDecimal("quantity");

        if (cancelQty == null) cancelQty = ZERO;
        if (orderQty == null) orderQty = ZERO;

        return orderQty.subtract(cancelQty).setScale(scale, rounding);
    }

    public static BigDecimal getOrderItemShipGroupQuantity(GenericValue shipGroupAssoc) {
        BigDecimal cancelQty = shipGroupAssoc.getBigDecimal("cancelQuantity");
        BigDecimal orderQty = shipGroupAssoc.getBigDecimal("quantity");

        if (cancelQty == null) cancelQty = BigDecimal.ZERO;
        if (orderQty == null) orderQty = BigDecimal.ZERO;

        return orderQty.subtract(cancelQty);
    }

    public static GenericValue getProductStoreFromOrder(Delegator delegator, String orderId) {
        GenericValue orderHeader = getOrderHeader(delegator, orderId);
        if (orderHeader == null) {
            Debug.logWarning("Could not find OrderHeader for orderId [" + orderId + "] in getProductStoreFromOrder, returning null", module);
        }
        return getProductStoreFromOrder(orderHeader);
    }

    public static GenericValue getProductStoreFromOrder(GenericValue orderHeader) {
        if (orderHeader == null) {
            return null;
        }
        Delegator delegator = orderHeader.getDelegator();
        GenericValue productStore = null;
        if (orderHeader.get("productStoreId") != null) {
            try {
                productStore = delegator.findByPrimaryKeyCache("ProductStore", UtilMisc.toMap("productStoreId", orderHeader.getString("productStoreId")));
            } catch (GenericEntityException e) {
                Debug.logError(e, "Cannot locate ProductStore from OrderHeader", module);
            }
        } else {
            Debug.logError("Null header or productStoreId", module);
        }
        return productStore;
    }

    public static BigDecimal getOrderGrandTotal(List<GenericValue> orderItems, List<GenericValue> adjustments) {
        Map<String, Object> orderTaxByTaxAuthGeoAndParty = getOrderTaxByTaxAuthGeoAndParty(adjustments);
        BigDecimal taxGrandTotal = (BigDecimal) orderTaxByTaxAuthGeoAndParty.get("taxGrandTotal");
        adjustments = EntityUtil.filterByAnd(adjustments, UtilMisc.toList(EntityCondition.makeCondition("orderAdjustmentTypeId", EntityOperator.NOT_EQUAL, "SALES_TAX")));
        BigDecimal total = getOrderItemsTotal(orderItems, adjustments);
        BigDecimal adj = getOrderAdjustmentsTotal(orderItems, adjustments);
        total = ((total.add(taxGrandTotal)).add(adj)).setScale(scale,rounding);
        return total;
    }

    public static List<GenericValue> getOrderHeaderAdjustments(List<GenericValue> adjustments, String shipGroupSeqId) {
        List<EntityExpr> contraints1 = UtilMisc.toList(EntityCondition.makeCondition("orderItemSeqId", EntityOperator.EQUALS, null));
        List<EntityExpr> contraints2 = UtilMisc.toList(EntityCondition.makeCondition("orderItemSeqId", EntityOperator.EQUALS, DataModelConstants.SEQ_ID_NA));
        List<EntityExpr> contraints3 = UtilMisc.toList(EntityCondition.makeCondition("orderItemSeqId", EntityOperator.EQUALS, ""));
        List<EntityExpr> contraints4 = FastList.newInstance();
        if (shipGroupSeqId != null) {
            contraints4.add(EntityCondition.makeCondition("shipGroupSeqId", EntityOperator.EQUALS, shipGroupSeqId));
        }
        List<GenericValue> toFilter = null;
        List<GenericValue> adj = FastList.newInstance();

        if (shipGroupSeqId != null) {
            toFilter = EntityUtil.filterByAnd(adjustments, contraints4);
        } else {
            toFilter = adjustments;
        }

        adj.addAll(EntityUtil.filterByAnd(toFilter, contraints1));
        adj.addAll(EntityUtil.filterByAnd(toFilter, contraints2));
        adj.addAll(EntityUtil.filterByAnd(toFilter, contraints3));
        return adj;
    }

    public static List<GenericValue> getOrderHeaderStatuses(List<GenericValue> orderStatuses) {
        List<EntityExpr> contraints1 = UtilMisc.toList(EntityCondition.makeCondition("orderItemSeqId", EntityOperator.EQUALS, null));
        contraints1.add(EntityCondition.makeCondition("orderItemSeqId", EntityOperator.EQUALS, DataModelConstants.SEQ_ID_NA));
        contraints1.add(EntityCondition.makeCondition("orderItemSeqId", EntityOperator.EQUALS, ""));

        List<EntityExpr> contraints2 = UtilMisc.toList(EntityCondition.makeCondition("orderPaymentPreferenceId", EntityOperator.EQUALS, null));
        contraints2.add(EntityCondition.makeCondition("orderPaymentPreferenceId", EntityOperator.EQUALS, DataModelConstants.SEQ_ID_NA));
        contraints2.add(EntityCondition.makeCondition("orderPaymentPreferenceId", EntityOperator.EQUALS, ""));

        List<GenericValue> newOrderStatuses = FastList.newInstance();
        newOrderStatuses.addAll(EntityUtil.filterByOr(orderStatuses, contraints1));
        return EntityUtil.orderBy(EntityUtil.filterByOr(newOrderStatuses, contraints2), UtilMisc.toList("-statusDatetime"));
    }

    public static BigDecimal getOrderAdjustmentsTotal(List<GenericValue> orderItems, List<GenericValue> adjustments) {
        return calcOrderAdjustments(getOrderHeaderAdjustments(adjustments, null), getOrderItemsSubTotal(orderItems, adjustments), true, true, true);
    }

    public static List<GenericValue> getOrderSurveyResponses(GenericValue orderHeader) {
        Delegator delegator = orderHeader.getDelegator();
        String orderId = orderHeader.getString("orderId");
        List<GenericValue> responses = null;
        try {
            responses = delegator.findByAnd("SurveyResponse", UtilMisc.toMap("orderId", orderId, "orderItemSeqId", "_NA_"));
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
        }

        if (responses == null) {
            responses = FastList.newInstance();
        }
        return responses;
    }

    public static List<GenericValue> getOrderItemSurveyResponse(GenericValue orderItem) {
        Delegator delegator = orderItem.getDelegator();
        String orderItemSeqId = orderItem.getString("orderItemSeqId");
        String orderId = orderItem.getString("orderId");
        List<GenericValue> responses = null;
        try {
            responses = delegator.findByAnd("SurveyResponse", UtilMisc.toMap("orderId", orderId, "orderItemSeqId", orderItemSeqId));
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
        }

        if (responses == null) {
            responses = FastList.newInstance();
        }
        return responses;
    }

    // ================= Order Adjustments =================

    public static BigDecimal calcOrderAdjustments(List<GenericValue> orderHeaderAdjustments, BigDecimal subTotal, boolean includeOther, boolean includeTax, boolean includeShipping) {
        BigDecimal adjTotal = ZERO;

        if (UtilValidate.isNotEmpty(orderHeaderAdjustments)) {
            List<GenericValue> filteredAdjs = filterOrderAdjustments(orderHeaderAdjustments, includeOther, includeTax, includeShipping, false, false);
            Iterator<GenericValue> adjIt = filteredAdjs.iterator();

            while (adjIt.hasNext()) {
                GenericValue orderAdjustment = adjIt.next();

                adjTotal = adjTotal.add(OrderReadHelper.calcOrderAdjustment(orderAdjustment, subTotal)).setScale(scale, rounding);
            }
        }
        return adjTotal.setScale(scale, rounding);
    }

    public static BigDecimal calcOrderAdjustment(GenericValue orderAdjustment, BigDecimal orderSubTotal) {
        BigDecimal adjustment = ZERO;

        if (orderAdjustment.get("amount") != null) {
            BigDecimal amount = orderAdjustment.getBigDecimal("amount");
            adjustment = adjustment.add(amount);
        } else if (orderAdjustment.get("sourcePercentage") != null) {
            BigDecimal percent = orderAdjustment.getBigDecimal("sourcePercentage");
            BigDecimal amount = orderSubTotal.multiply(percent).multiply(percentage);
            adjustment = adjustment.add(amount);
        }
        if ("SALES_TAX".equals(orderAdjustment.get("orderAdjustmentTypeId"))) {
            return adjustment.setScale(taxCalcScale, taxRounding);
        }
        return adjustment.setScale(scale, rounding);
    }

    // ================= Order Item Adjustments =================
    public static BigDecimal getOrderItemsSubTotal(List<GenericValue> orderItems, List<GenericValue> adjustments) {
        return getOrderItemsSubTotal(orderItems, adjustments, null);
    }

    public static BigDecimal getOrderItemsSubTotal(List<GenericValue> orderItems, List<GenericValue> adjustments, List<GenericValue> workEfforts) {
        BigDecimal result = ZERO;
        Iterator<GenericValue> itemIter = UtilMisc.toIterator(orderItems);

        while (itemIter != null && itemIter.hasNext()) {
            GenericValue orderItem = itemIter.next();
            BigDecimal itemTotal = getOrderItemSubTotal(orderItem, adjustments);
            // Debug.log("Item : " + orderItem.getString("orderId") + " / " + orderItem.getString("orderItemSeqId") + " = " + itemTotal, module);

            if (workEfforts != null && orderItem.getString("orderItemTypeId").compareTo("RENTAL_ORDER_ITEM") == 0) {
                Iterator<GenericValue> weIter = UtilMisc.toIterator(workEfforts);
                while (weIter != null && weIter.hasNext()) {
                    GenericValue workEffort = weIter.next();
                    if (workEffort.getString("workEffortId").compareTo(orderItem.getString("orderItemSeqId")) == 0)    {
                        itemTotal = itemTotal.multiply(getWorkEffortRentalQuantity(workEffort)).setScale(scale, rounding);
                        break;
                    }
//                    Debug.log("Item : " + orderItem.getString("orderId") + " / " + orderItem.getString("orderItemSeqId") + " = " + itemTotal, module);
                }
            }
            result = result.add(itemTotal).setScale(scale, rounding);

        }
        return result.setScale(scale, rounding);
    }

    /** The passed adjustments can be all adjustments for the order, ie for all line items */
    public static BigDecimal getOrderItemSubTotal(GenericValue orderItem, List<GenericValue> adjustments) {
        return getOrderItemSubTotal(orderItem, adjustments, false, false);
    }

    /** The passed adjustments can be all adjustments for the order, ie for all line items */
    public static BigDecimal getOrderItemSubTotal(GenericValue orderItem, List<GenericValue> adjustments, boolean forTax, boolean forShipping) {
        BigDecimal unitPrice = orderItem.getBigDecimal("unitPrice");
        BigDecimal quantity = getOrderItemQuantity(orderItem);
        BigDecimal result = ZERO;

        if (unitPrice == null || quantity == null) {
            Debug.logWarning("[getOrderItemTotal] unitPrice or quantity are null, using 0 for the item base price", module);
        } else {
            if (Debug.verboseOn()) Debug.logVerbose("Unit Price : " + unitPrice + " / " + "Quantity : " + quantity, module);
            result = unitPrice.multiply(quantity);

            if ("RENTAL_ORDER_ITEM".equals(orderItem.getString("orderItemTypeId"))) {
                // retrieve related work effort when required.
                List<GenericValue> workOrderItemFulfillments = null;
                try {
                    workOrderItemFulfillments = orderItem.getDelegator().findByAndCache("WorkOrderItemFulfillment", UtilMisc.toMap("orderId", orderItem.getString("orderId"), "orderItemSeqId", orderItem.getString("orderItemSeqId")));
                } catch (GenericEntityException e) {}
                if (workOrderItemFulfillments != null) {
                    Iterator<GenericValue> iter = workOrderItemFulfillments.iterator();
                    if (iter.hasNext())    {
                        GenericValue WorkOrderItemFulfillment = iter.next();
                        GenericValue workEffort = null;
                        try {
                            workEffort = WorkOrderItemFulfillment.getRelatedOneCache("WorkEffort");
                        } catch (GenericEntityException e) {}
                        result = result.multiply(getWorkEffortRentalQuantity(workEffort));
                    }
                }
            }
        }

        // subtotal also includes non tax and shipping adjustments; tax and shipping will be calculated using this adjusted value
        result = result.add(getOrderItemAdjustmentsTotal(orderItem, adjustments, true, false, false, forTax, forShipping));

        // Debug.logInfo("In getOrderItemSubTotal result=" + result + ", rounded result=" + result.setScale(scale, rounding), module);
        return result.setScale(scale, rounding);
    }

    public static BigDecimal getOrderItemsTotal(List<GenericValue> orderItems, List<GenericValue> adjustments) {
        BigDecimal result = ZERO;
        Iterator<GenericValue> itemIter = UtilMisc.toIterator(orderItems);

        while (itemIter != null && itemIter.hasNext()) {
            result = result.add(getOrderItemTotal(itemIter.next(), adjustments));
        }
        return result.setScale(scale,  rounding);
    }

    public static BigDecimal getOrderItemTotal(GenericValue orderItem, List<GenericValue> adjustments) {
        // add tax and shipping to subtotal
        return getOrderItemSubTotal(orderItem, adjustments).add(getOrderItemAdjustmentsTotal(orderItem, adjustments, false, true, true));
    }

    public static BigDecimal calcOrderPromoAdjustmentsBd(List<GenericValue> allOrderAdjustments) {
        BigDecimal promoAdjTotal = ZERO;

        List<GenericValue> promoAdjustments = EntityUtil.filterByAnd(allOrderAdjustments, UtilMisc.toMap("orderAdjustmentTypeId", "PROMOTION_ADJUSTMENT"));
        
        if (UtilValidate.isNotEmpty(promoAdjustments)) {
            Iterator<GenericValue> promoAdjIter = promoAdjustments.iterator();
            while (promoAdjIter.hasNext()) {
                GenericValue promoAdjustment = promoAdjIter.next();
                if (promoAdjustment != null) {
                    BigDecimal amount = promoAdjustment.getBigDecimal("amount").setScale(taxCalcScale, taxRounding);
                    promoAdjTotal = promoAdjTotal.add(amount);
                }
            }
        }
        return promoAdjTotal.setScale(scale, rounding);
    }

    public static BigDecimal getWorkEffortRentalLength(GenericValue workEffort) {
        BigDecimal length = null;
        if (workEffort.get("estimatedStartDate") != null && workEffort.get("estimatedCompletionDate") != null) {
            length = new BigDecimal(UtilDateTime.getInterval(workEffort.getTimestamp("estimatedStartDate"),workEffort.getTimestamp("estimatedCompletionDate"))/86400000);
        }
        return length;
    }

    public static BigDecimal getWorkEffortRentalQuantity(GenericValue workEffort) {
        BigDecimal persons = BigDecimal.ONE;
        if (workEffort.get("reservPersons") != null)
            persons = workEffort.getBigDecimal("reservPersons");
        BigDecimal secondPersonPerc = ZERO;
        if (workEffort.get("reserv2ndPPPerc") != null)
            secondPersonPerc = workEffort.getBigDecimal("reserv2ndPPPerc");
        BigDecimal nthPersonPerc = ZERO;
        if (workEffort.get("reservNthPPPerc") != null)
            nthPersonPerc = workEffort.getBigDecimal("reservNthPPPerc");
        long length = 1;
        if (workEffort.get("estimatedStartDate") != null && workEffort.get("estimatedCompletionDate") != null)
            length = (workEffort.getTimestamp("estimatedCompletionDate").getTime() - workEffort.getTimestamp("estimatedStartDate").getTime()) / 86400000;

        BigDecimal rentalAdjustment = ZERO;
        if (persons.compareTo(BigDecimal.ONE) == 1)    {
            if (persons.compareTo(new BigDecimal(2)) == 1) {
                persons = persons.subtract(new BigDecimal(2));
                if (nthPersonPerc.signum() == 1)
                    rentalAdjustment = persons.multiply(nthPersonPerc);
                else
                    rentalAdjustment = persons.multiply(secondPersonPerc);
                persons = new BigDecimal("2");
            }
            if (persons.compareTo(new BigDecimal("2")) == 0)
                rentalAdjustment = rentalAdjustment.add(secondPersonPerc);
        }
        rentalAdjustment = rentalAdjustment.add(new BigDecimal(100));  // add final 100 percent for first person
        rentalAdjustment = rentalAdjustment.divide(new BigDecimal(100), scale, rounding).multiply(new BigDecimal(String.valueOf(length)));
//        Debug.logInfo("rental parameters....Nbr of persons:" + persons + " extra% 2nd person:" + secondPersonPerc + " extra% Nth person:" + nthPersonPerc + " Length: " + length + "  total rental adjustment:" + rentalAdjustment ,module);
        return rentalAdjustment; // return total rental adjustment
        }

    public static BigDecimal getAllOrderItemsAdjustmentsTotal(List<GenericValue> orderItems, List<GenericValue> adjustments, boolean includeOther, boolean includeTax, boolean includeShipping) {
        BigDecimal result = ZERO;
        Iterator<GenericValue> itemIter = UtilMisc.toIterator(orderItems);

        while (itemIter != null && itemIter.hasNext()) {
            result = result.add(getOrderItemAdjustmentsTotal(itemIter.next(), adjustments, includeOther, includeTax, includeShipping));
        }
        return result.setScale(scale, rounding);
    }

    /** The passed adjustments can be all adjustments for the order, ie for all line items */
    public static BigDecimal getOrderItemAdjustmentsTotal(GenericValue orderItem, List<GenericValue> adjustments, boolean includeOther, boolean includeTax, boolean includeShipping) {
        return getOrderItemAdjustmentsTotal(orderItem, adjustments, includeOther, includeTax, includeShipping, false, false);
    }

    /** The passed adjustments can be all adjustments for the order, ie for all line items */
    public static BigDecimal getOrderItemAdjustmentsTotal(GenericValue orderItem, List<GenericValue> adjustments, boolean includeOther, boolean includeTax, boolean includeShipping, boolean forTax, boolean forShipping) {
        return calcItemAdjustments(getOrderItemQuantity(orderItem), orderItem.getBigDecimal("unitPrice"),
                getOrderItemAdjustmentList(orderItem, adjustments),
                includeOther, includeTax, includeShipping, forTax, forShipping);
    }

    public static List<GenericValue> getOrderItemAdjustmentList(GenericValue orderItem, List<GenericValue> adjustments) {
        return EntityUtil.filterByAnd(adjustments, UtilMisc.toMap("orderItemSeqId", orderItem.get("orderItemSeqId")));
    }

    public static List<GenericValue> getOrderItemStatuses(GenericValue orderItem, List<GenericValue> orderStatuses) {
        List<EntityExpr> contraints1 = UtilMisc.toList(EntityCondition.makeCondition("orderItemSeqId", EntityOperator.EQUALS, orderItem.get("orderItemSeqId")));
        List<EntityExpr> contraints2 = UtilMisc.toList(EntityCondition.makeCondition("orderPaymentPreferenceId", EntityOperator.EQUALS, null));
        contraints2.add(EntityCondition.makeCondition("orderPaymentPreferenceId", EntityOperator.EQUALS, DataModelConstants.SEQ_ID_NA));
        contraints2.add(EntityCondition.makeCondition("orderPaymentPreferenceId", EntityOperator.EQUALS, ""));

        List<GenericValue> newOrderStatuses = FastList.newInstance();
        newOrderStatuses.addAll(EntityUtil.filterByAnd(orderStatuses, contraints1));
        return EntityUtil.orderBy(EntityUtil.filterByOr(newOrderStatuses, contraints2), UtilMisc.toList("-statusDatetime"));
    }


    // Order Item Adjs Utility Methods

    public static BigDecimal calcItemAdjustments(BigDecimal quantity, BigDecimal unitPrice, List<GenericValue> adjustments, boolean includeOther, boolean includeTax, boolean includeShipping, boolean forTax, boolean forShipping) {
        BigDecimal adjTotal = ZERO;

        if (UtilValidate.isNotEmpty(adjustments)) {
            List<GenericValue> filteredAdjs = filterOrderAdjustments(adjustments, includeOther, includeTax, includeShipping, forTax, forShipping);
            Iterator<GenericValue> adjIt = filteredAdjs.iterator();

            while (adjIt.hasNext()) {
                GenericValue orderAdjustment = adjIt.next();

                adjTotal = adjTotal.add(OrderReadHelper.calcItemAdjustment(orderAdjustment, quantity, unitPrice));
            }
        }
        return adjTotal;
    }

    public static BigDecimal calcItemAdjustmentsRecurringBd(BigDecimal quantity, BigDecimal unitPrice, List<GenericValue> adjustments, boolean includeOther, boolean includeTax, boolean includeShipping, boolean forTax, boolean forShipping) {
        BigDecimal adjTotal = ZERO;

        if (UtilValidate.isNotEmpty(adjustments)) {
            List<GenericValue> filteredAdjs = filterOrderAdjustments(adjustments, includeOther, includeTax, includeShipping, forTax, forShipping);
            Iterator<GenericValue> adjIt = filteredAdjs.iterator();

            while (adjIt.hasNext()) {
                GenericValue orderAdjustment = adjIt.next();

                adjTotal = adjTotal.add(OrderReadHelper.calcItemAdjustmentRecurringBd(orderAdjustment, quantity, unitPrice)).setScale(scale, rounding);
            }
        }
        return adjTotal;
    }

    public static BigDecimal calcItemAdjustment(GenericValue itemAdjustment, GenericValue item) {
        return calcItemAdjustment(itemAdjustment, getOrderItemQuantity(item), item.getBigDecimal("unitPrice"));
    }

    public static BigDecimal calcItemAdjustment(GenericValue itemAdjustment, BigDecimal quantity, BigDecimal unitPrice) {
        BigDecimal adjustment = ZERO;
        if (itemAdjustment.get("amount") != null) {
            //shouldn't round amounts here, wait until item total is added up otherwise incremental errors are introduced, and there is code that calls this method that does that already: adjustment = adjustment.add(setScaleByType("SALES_TAX".equals(itemAdjustment.get("orderAdjustmentTypeId")), itemAdjustment.getBigDecimal("amount")));
            adjustment = adjustment.add(itemAdjustment.getBigDecimal("amount"));
        } else if (itemAdjustment.get("sourcePercentage") != null) {
            // see comment above about rounding: adjustment = adjustment.add(setScaleByType("SALES_TAX".equals(itemAdjustment.get("orderAdjustmentTypeId")), itemAdjustment.getBigDecimal("sourcePercentage").multiply(quantity).multiply(unitPrice).multiply(percentage)));
            adjustment = adjustment.add(itemAdjustment.getBigDecimal("sourcePercentage").multiply(quantity).multiply(unitPrice).multiply(percentage));
        }
        if (Debug.verboseOn()) Debug.logVerbose("calcItemAdjustment: " + itemAdjustment + ", quantity=" + quantity + ", unitPrice=" + unitPrice + ", adjustment=" + adjustment, module);
        return adjustment;
    }

    public static BigDecimal calcItemAdjustmentRecurringBd(GenericValue itemAdjustment, BigDecimal quantity, BigDecimal unitPrice) {
        BigDecimal adjustmentRecurring = ZERO;
        if (itemAdjustment.get("recurringAmount") != null) {
            adjustmentRecurring = adjustmentRecurring.add(setScaleByType("SALES_TAX".equals(itemAdjustment.get("orderAdjustmentTypeId")), itemAdjustment.getBigDecimal("recurringAmount")));
        }
        if (Debug.verboseOn()) Debug.logVerbose("calcItemAdjustmentRecurring: " + itemAdjustment + ", quantity=" + quantity + ", unitPrice=" + unitPrice + ", adjustmentRecurring=" + adjustmentRecurring, module);
        return adjustmentRecurring.setScale(scale, rounding);
    }

    public static List<GenericValue> filterOrderAdjustments(List<GenericValue> adjustments, boolean includeOther, boolean includeTax, boolean includeShipping, boolean forTax, boolean forShipping) {
        List<GenericValue> newOrderAdjustmentsList = FastList.newInstance();

        if (UtilValidate.isNotEmpty(adjustments)) {
            Iterator<GenericValue> adjIt = adjustments.iterator();

            while (adjIt.hasNext()) {
                GenericValue orderAdjustment = adjIt.next();

                boolean includeAdjustment = false;

                if ("SALES_TAX".equals(orderAdjustment.getString("orderAdjustmentTypeId")) ||
                        "VAT_TAX".equals(orderAdjustment.getString("orderAdjustmentTypeId")) ||
                        "VAT_PRICE_CORRECT".equals(orderAdjustment.getString("orderAdjustmentTypeId"))) {
                    if (includeTax) includeAdjustment = true;
                } else if ("SHIPPING_CHARGES".equals(orderAdjustment.getString("orderAdjustmentTypeId"))) {
                    if (includeShipping) includeAdjustment = true;
                } else {
                    if (includeOther) includeAdjustment = true;
                }

                // default to yes, include for shipping; so only exclude if includeInShipping is N, or false; if Y or null or anything else it will be included
                if (forTax && "N".equals(orderAdjustment.getString("includeInTax"))) {
                    includeAdjustment = false;
                }

                // default to yes, include for shipping; so only exclude if includeInShipping is N, or false; if Y or null or anything else it will be included
                if (forShipping && "N".equals(orderAdjustment.getString("includeInShipping"))) {
                    includeAdjustment = false;
                }

                if (includeAdjustment) {
                    newOrderAdjustmentsList.add(orderAdjustment);
                }
            }
        }
        return newOrderAdjustmentsList;
    }

    public static BigDecimal getQuantityOnOrder(Delegator delegator, String productId) {
        BigDecimal quantity = BigDecimal.ZERO;

        // first find all open purchase orders
        List<EntityExpr> openOrdersExprs = UtilMisc.toList(EntityCondition.makeCondition("orderTypeId", EntityOperator.EQUALS, "PURCHASE_ORDER"));
        openOrdersExprs.add(EntityCondition.makeCondition("itemStatusId", EntityOperator.NOT_EQUAL, "ITEM_CANCELLED"));
        openOrdersExprs.add(EntityCondition.makeCondition("itemStatusId", EntityOperator.NOT_EQUAL, "ITEM_REJECTED"));
        openOrdersExprs.add(EntityCondition.makeCondition("itemStatusId", EntityOperator.NOT_EQUAL, "ITEM_COMPLETED"));
        openOrdersExprs.add(EntityCondition.makeCondition("productId", EntityOperator.EQUALS, productId));
        EntityCondition openOrdersCond = EntityCondition.makeCondition(openOrdersExprs, EntityOperator.AND);
        List<GenericValue> openOrders = null;
        try {
            openOrders = delegator.findList("OrderHeaderAndItems", openOrdersCond, null, null, null, false);
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
        }

        if (UtilValidate.isNotEmpty(openOrders)) {
            Iterator<GenericValue> i = openOrders.iterator();
            while (i.hasNext()) {
                GenericValue order = i.next();
                BigDecimal thisQty = order.getBigDecimal("quantity");
                if (thisQty == null) {
                    thisQty = BigDecimal.ZERO;
                }
                quantity = quantity.add(thisQty);
            }
        }

        return quantity;
    }

    /**
     * Checks to see if this user has read permission on the specified order
     * @param userLogin The UserLogin value object to check
     * @param orderHeader The OrderHeader for the specified order
     * @return boolean True if we have read permission
     */
    public static boolean hasPermission(Security security, GenericValue userLogin, GenericValue orderHeader) {
        if (userLogin == null || orderHeader == null)
            return false;

        if (security.hasEntityPermission("ORDERMGR", "_VIEW", userLogin)) {
            return true;
        } else if (security.hasEntityPermission("ORDERMGR", "_ROLEVIEW", userLogin)) {
            List<GenericValue> orderRoles = null;
            try {
                orderRoles = orderHeader.getRelatedByAnd("OrderRole",
                        UtilMisc.toMap("partyId", userLogin.getString("partyId")));
            } catch (GenericEntityException e) {
                Debug.logError(e, "Cannot get OrderRole from OrderHeader", module);
            }

            if (UtilValidate.isNotEmpty(orderRoles)) {
                // we are in at least one role
                return true;
            }
        }

        return false;
    }

    public static OrderReadHelper getHelper(GenericValue orderHeader) {
        return new OrderReadHelper(orderHeader);
    }

    /**
     * Get orderAdjustments that have no corresponding returnAdjustment
     * @return return the order adjustments that have no corresponding with return adjustment
     */
    public List<GenericValue> getAvailableOrderHeaderAdjustments() {
        List<GenericValue> orderHeaderAdjustments = this.getOrderHeaderAdjustments();
        List<GenericValue> filteredAdjustments = FastList.newInstance();
        if (orderHeaderAdjustments != null) {
            Iterator<GenericValue> orderAdjIterator = orderHeaderAdjustments.iterator();
            while (orderAdjIterator.hasNext()) {
                GenericValue orderAdjustment = orderAdjIterator.next();
                long count = 0;
                try {
                    count = orderHeader.getDelegator().findCountByCondition("ReturnAdjustment", EntityCondition.makeCondition("orderAdjustmentId", EntityOperator.EQUALS, orderAdjustment.get("orderAdjustmentId")), null, null);
                } catch (GenericEntityException e) {
                    Debug.logError(e, module);
                }
                if (count == 0) {
                    filteredAdjustments.add(orderAdjustment);
                }
            }
        }
        return filteredAdjustments;
    }

    /**
     * Get the total return adjustments for a set of key -> value condition pairs.  Done for code efficiency.
     * @param delegator the delegator
     * @param condition a map of the conditions to use
     * @return Get the total return adjustments
     */
    public static BigDecimal getReturnAdjustmentTotal(Delegator delegator, Map<String, Object> condition) {
        BigDecimal total = ZERO;
        List<GenericValue> adjustments;
        try {
            // TODO: find on a view-entity with a sum is probably more efficient
            adjustments = delegator.findByAnd("ReturnAdjustment", condition);
            if (adjustments != null) {
                Iterator<GenericValue> adjustmentIterator = adjustments.iterator();
                while (adjustmentIterator.hasNext()) {
                    GenericValue returnAdjustment = adjustmentIterator.next();
                    total = total.add(setScaleByType("RET_SALES_TAX_ADJ".equals(returnAdjustment.get("returnAdjustmentTypeId")),returnAdjustment.getBigDecimal("amount")));
                }
            }
        } catch (GenericEntityException e) {
            Debug.logError(e, OrderReturnServices.module);
        }
        return total;
    }

    // little helper method to set the scale according to tax type
    public static BigDecimal setScaleByType(boolean isTax, BigDecimal value) {
        return isTax ? value.setScale(taxCalcScale, taxRounding) : value.setScale(scale, rounding);
    }

   /** Get the quantity of order items that have been invoiced */
   public static BigDecimal getOrderItemInvoicedQuantity(GenericValue orderItem) {
       BigDecimal invoiced = BigDecimal.ZERO;
       try {
           // this is simply the sum of quantity billed in all related OrderItemBillings
           List<GenericValue> billings = orderItem.getRelated("OrderItemBilling");
           for (Iterator<GenericValue> iter = billings.iterator(); iter.hasNext();) {
               GenericValue billing = iter.next();
               BigDecimal quantity = billing.getBigDecimal("quantity");
               if (quantity != null) {
                   invoiced = invoiced.add(quantity);
               }
           }
       } catch (GenericEntityException e) {
           Debug.logError(e, e.getMessage(), module);
       }
       return invoiced;
   }

   public List<GenericValue> getOrderPaymentStatuses() {
       return getOrderPaymentStatuses(getOrderStatuses());
   }

   public static List<GenericValue> getOrderPaymentStatuses(List<GenericValue> orderStatuses) {
       List<EntityExpr> contraints1 = UtilMisc.toList(EntityCondition.makeCondition("orderItemSeqId", EntityOperator.EQUALS, null));
       contraints1.add(EntityCondition.makeCondition("orderItemSeqId", EntityOperator.EQUALS, DataModelConstants.SEQ_ID_NA));
       contraints1.add(EntityCondition.makeCondition("orderItemSeqId", EntityOperator.EQUALS, ""));

       List<EntityExpr> contraints2 = UtilMisc.toList(EntityCondition.makeCondition("orderPaymentPreferenceId", EntityOperator.NOT_EQUAL, null));
       List<GenericValue> newOrderStatuses = FastList.newInstance();
       newOrderStatuses.addAll(EntityUtil.filterByOr(orderStatuses, contraints1));

       return EntityUtil.orderBy(EntityUtil.filterByAnd(newOrderStatuses, contraints2), UtilMisc.toList("-statusDatetime"));
   }

    public static String getOrderItemAttribute(GenericValue orderItem, String attributeName) {
        String attributeValue = null;
        if (orderItem != null) {
            try {
                GenericValue orderItemAttribute = EntityUtil.getFirst(orderItem.getRelatedByAnd("OrderItemAttribute", UtilMisc.toMap("attrName", attributeName)));
                if (orderItemAttribute != null) {
                    attributeValue = orderItemAttribute.getString("attrValue");
                }
            } catch (GenericEntityException e) {
                Debug.logError(e, module);
            }
        }
        return attributeValue;
    }

    public String getOrderAttribute(String attributeName) {
        String attributeValue = null;
        if (orderHeader != null) {
            try {
                GenericValue orderAttribute = EntityUtil.getFirst(orderHeader.getRelatedByAnd("OrderAttribute", UtilMisc.toMap("attrName", attributeName)));
                if (orderAttribute != null) {
                    attributeValue = orderAttribute.getString("attrValue");
                }
            } catch (GenericEntityException e) {
                Debug.logError(e, module);
            }
        }
        return attributeValue;
    }
    

   public static Map<String, Object> getOrderTaxByTaxAuthGeoAndParty(List<GenericValue> orderAdjustments) {
       BigDecimal taxGrandTotal = BigDecimal.ZERO;
       List<Map<String, Object>> taxByTaxAuthGeoAndPartyList = FastList.newInstance();
       if (UtilValidate.isNotEmpty(orderAdjustments)) {
           // get orderAdjustment where orderAdjustmentTypeId is SALES_TAX.
           orderAdjustments = EntityUtil.filterByAnd(orderAdjustments, UtilMisc.toMap("orderAdjustmentTypeId","SALES_TAX"));
           orderAdjustments = EntityUtil.orderBy(orderAdjustments, UtilMisc.toList("taxAuthGeoId","taxAuthPartyId"));

           // get the list of all distinct taxAuthGeoId and taxAuthPartyId. It is for getting the number of taxAuthGeo and taxAuthPartyId in adjustments.
           List<String> distinctTaxAuthGeoIdList = EntityUtil.getFieldListFromEntityList(orderAdjustments, "taxAuthGeoId", true);
           List<String> distinctTaxAuthPartyIdList = EntityUtil.getFieldListFromEntityList(orderAdjustments, "taxAuthPartyId", true);

           // Keep a list of amount that have been added to make sure none are missed (if taxAuth* information is missing)
           List<GenericValue> processedAdjustments = FastList.newInstance();
           // For each taxAuthGeoId get and add amount from orderAdjustment
           for (String taxAuthGeoId : distinctTaxAuthGeoIdList) {
               for (String taxAuthPartyId : distinctTaxAuthPartyIdList) {
                   //get all records for orderAdjustments filtered by taxAuthGeoId and taxAurhPartyId
                   List<GenericValue> orderAdjByTaxAuthGeoAndPartyIds = EntityUtil.filterByAnd(orderAdjustments, UtilMisc.toMap("taxAuthGeoId", taxAuthGeoId, "taxAuthPartyId", taxAuthPartyId));
                   if (UtilValidate.isNotEmpty(orderAdjByTaxAuthGeoAndPartyIds)) {
                       BigDecimal totalAmount = BigDecimal.ZERO;
                       //Now for each orderAdjustment record get and add amount.
                       for (GenericValue orderAdjustment : orderAdjByTaxAuthGeoAndPartyIds) {
                           BigDecimal amount = orderAdjustment.getBigDecimal("amount");
                           if (amount == null) {
                               amount = ZERO;
                           }
                           totalAmount = totalAmount.add(amount).setScale(taxCalcScale, taxRounding);
                           processedAdjustments.add(orderAdjustment);
                       }
                       totalAmount = totalAmount.setScale(taxFinalScale, taxRounding);
                       taxByTaxAuthGeoAndPartyList.add(UtilMisc.<String, Object>toMap("taxAuthPartyId", taxAuthPartyId, "taxAuthGeoId", taxAuthGeoId, "totalAmount", totalAmount));
                       taxGrandTotal = taxGrandTotal.add(totalAmount);
                   }
               }
           }
           // Process any adjustments that got missed
           List<GenericValue> missedAdjustments = FastList.newInstance();
           missedAdjustments.addAll(orderAdjustments);
           missedAdjustments.removeAll(processedAdjustments);
           for (GenericValue orderAdjustment : missedAdjustments) {
               taxGrandTotal = taxGrandTotal.add(orderAdjustment.getBigDecimal("amount").setScale(taxCalcScale, taxRounding));
           }
           taxGrandTotal = taxGrandTotal.setScale(taxFinalScale, taxRounding);
       }
       Map<String, Object> result = FastMap.newInstance();
       result.put("taxByTaxAuthGeoAndPartyList", taxByTaxAuthGeoAndPartyList);
       result.put("taxGrandTotal", taxGrandTotal);
       return result;
   }

   public static Map<String, Object> getOrderItemTaxByTaxAuthGeoAndPartyForDisplay(GenericValue orderItem, List<GenericValue> orderAdjustmentsOriginal) {
       return getOrderTaxByTaxAuthGeoAndPartyForDisplay(getOrderItemAdjustmentList(orderItem, orderAdjustmentsOriginal));
   }

   public static Map<String, Object> getOrderTaxByTaxAuthGeoAndPartyForDisplay(List<GenericValue> orderAdjustmentsOriginal) {
       BigDecimal taxGrandTotal = BigDecimal.ZERO;
       List<Map<String, Object>> taxByTaxAuthGeoAndPartyList = FastList.newInstance();
       List<GenericValue> orderAdjustmentsToUse = FastList.newInstance();
       if (UtilValidate.isNotEmpty(orderAdjustmentsOriginal)) {
           // get orderAdjustment where orderAdjustmentTypeId is SALES_TAX.
           orderAdjustmentsToUse.addAll(EntityUtil.filterByAnd(orderAdjustmentsOriginal, UtilMisc.toMap("orderAdjustmentTypeId", "SALES_TAX")));
           orderAdjustmentsToUse.addAll(EntityUtil.filterByAnd(orderAdjustmentsOriginal, UtilMisc.toMap("orderAdjustmentTypeId", "VAT_TAX")));
           orderAdjustmentsToUse = EntityUtil.orderBy(orderAdjustmentsToUse, UtilMisc.toList("taxAuthGeoId","taxAuthPartyId"));

           // get the list of all distinct taxAuthGeoId and taxAuthPartyId. It is for getting the number of taxAuthGeo and taxAuthPartyId in adjustments.
           List<String> distinctTaxAuthGeoIdList = EntityUtil.getFieldListFromEntityList(orderAdjustmentsToUse, "taxAuthGeoId", true);
           List<String> distinctTaxAuthPartyIdList = EntityUtil.getFieldListFromEntityList(orderAdjustmentsToUse, "taxAuthPartyId", true);

           // Keep a list of amount that have been added to make sure none are missed (if taxAuth* information is missing)
           List<GenericValue> processedAdjustments = FastList.newInstance();
           // For each taxAuthGeoId get and add amount from orderAdjustment
           for (String taxAuthGeoId : distinctTaxAuthGeoIdList) {
               for (String taxAuthPartyId : distinctTaxAuthPartyIdList) {
                   //get all records for orderAdjustments filtered by taxAuthGeoId and taxAurhPartyId
                   List<GenericValue> orderAdjByTaxAuthGeoAndPartyIds = EntityUtil.filterByAnd(orderAdjustmentsToUse, UtilMisc.toMap("taxAuthGeoId", taxAuthGeoId, "taxAuthPartyId", taxAuthPartyId));
                   if (UtilValidate.isNotEmpty(orderAdjByTaxAuthGeoAndPartyIds)) {
                       BigDecimal totalAmount = BigDecimal.ZERO;
                       //Now for each orderAdjustment record get and add amount.
                       for (GenericValue orderAdjustment : orderAdjByTaxAuthGeoAndPartyIds) {
                           BigDecimal amount = orderAdjustment.getBigDecimal("amount");
                           if (amount != null) {
                               totalAmount = totalAmount.add(amount);
                           }
                           if ("VAT_TAX".equals(orderAdjustment.getString("orderAdjustmentTypeId")) && 
                                   orderAdjustment.get("amountAlreadyIncluded") != null) {
                               // this is the only case where the VAT_TAX amountAlreadyIncluded should be added in, and should just be for display and not to calculate the order grandTotal
                               totalAmount = totalAmount.add(orderAdjustment.getBigDecimal("amountAlreadyIncluded"));
                           }
                           totalAmount = totalAmount.setScale(taxCalcScale, taxRounding);
                           processedAdjustments.add(orderAdjustment);
                       }
                       totalAmount = totalAmount.setScale(taxFinalScale, taxRounding);
                       taxByTaxAuthGeoAndPartyList.add(UtilMisc.<String, Object>toMap("taxAuthPartyId", taxAuthPartyId, "taxAuthGeoId", taxAuthGeoId, "totalAmount", totalAmount));
                       taxGrandTotal = taxGrandTotal.add(totalAmount);
                   }
               }
           }
           // Process any adjustments that got missed
           List<GenericValue> missedAdjustments = FastList.newInstance();
           missedAdjustments.addAll(orderAdjustmentsToUse);
           missedAdjustments.removeAll(processedAdjustments);
           for (GenericValue orderAdjustment : missedAdjustments) {
               taxGrandTotal = taxGrandTotal.add(orderAdjustment.getBigDecimal("amount").setScale(taxCalcScale, taxRounding));
           }
           taxGrandTotal = taxGrandTotal.setScale(taxFinalScale, taxRounding);
       }
       Map<String, Object> result = FastMap.newInstance();
       result.put("taxByTaxAuthGeoAndPartyList", taxByTaxAuthGeoAndPartyList);
       result.put("taxGrandTotal", taxGrandTotal);
       return result;
   }
}
