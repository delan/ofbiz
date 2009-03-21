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
package org.ofbiz.accounting.thirdparty.paypal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilFormatOut;
import org.ofbiz.base.util.UtilHttp;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilProperties;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.GenericDelegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.transaction.GenericTransactionException;
import org.ofbiz.entity.transaction.TransactionUtil;
import org.ofbiz.order.order.OrderChangeHelper;
import org.ofbiz.product.store.ProductStoreWorker;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.ModelService;
import org.ofbiz.service.LocalDispatcher;


public class PayPalEvents {
 
    public static final String resource = "AccountingUiLabels";
    public static final String resourceErr = "AccountingErrorUiLabels";
    public static final String commonResource = "CommonUiLabels";
    public static final String module = PayPalEvents.class.getName();
 
    /** Initiate PayPal Request */
    public static String callPayPal(HttpServletRequest request, HttpServletResponse response) {
        Locale locale = UtilHttp.getLocale(request);
        GenericDelegator delegator = (GenericDelegator) request.getAttribute("delegator");
        GenericValue userLogin = (GenericValue) request.getSession().getAttribute("userLogin");
 
        // get the orderId
        String orderId = (String) request.getAttribute("orderId");
 
        // get the order header
        GenericValue orderHeader = null;
        try {
            orderHeader = delegator.findByPrimaryKey("OrderHeader", UtilMisc.toMap("orderId", orderId));
        } catch (GenericEntityException e) {
            Debug.logError(e, "Cannot get the order header for order: " + orderId, module);
            request.setAttribute("_ERROR_MESSAGE_", UtilProperties.getMessage(resourceErr, "payPalEvents.problemsGettingOrderHeader", locale));
            return "error";
        }
 
        // get the order total
        String orderTotal = orderHeader.getBigDecimal("grandTotal").toPlainString();
 
        // get the product store
        GenericValue productStore = ProductStoreWorker.getProductStore(request);

        if (productStore == null) {
            Debug.logError("ProductStore is null", module);
            request.setAttribute("_ERROR_MESSAGE_", UtilProperties.getMessage(resourceErr, "payPalEvents.problemsGettingMerchantConfiguration", locale));
            return "error";
        }
 
        // get the payment properties file
        GenericValue paymentConfig = ProductStoreWorker.getProductStorePaymentSetting(delegator, productStore.getString("productStoreId"), "EXT_PAYPAL", null, true);
        String configString = null;
        if (paymentConfig != null) {
            configString = paymentConfig.getString("paymentPropertiesPath");
        }
 
        if (configString == null) {
            configString = "payment.properties";
        }
 
        // get the company name
        String company = UtilFormatOut.checkEmpty(productStore.getString("companyName"), "");
 
        // create the item name
        String itemName = UtilProperties.getMessage(resource, "AccountingOrderNr", locale) + orderId + " " +
                                 (company != null ? UtilProperties.getMessage(commonResource, "CommonFrom", locale) + " "+ company : "");
        String itemNumber = "0";
 
        // get the redirect url
        String redirectUrl = UtilProperties.getPropertyValue(configString, "payment.paypal.redirect");
 
        // get the notify url
        String notifyUrl = UtilProperties.getPropertyValue(configString, "payment.paypal.notify");
 
        // get the return urls
        String returnUrl = UtilProperties.getPropertyValue(configString, "payment.paypal.return");
 
        // get the cancel return urls
        String cancelReturnUrl = UtilProperties.getPropertyValue(configString, "payment.paypal.cancelReturn");
 
        // get the image url
        String imageUrl = UtilProperties.getPropertyValue(configString, "payment.paypal.image");
 
        // get the paypal account
        String payPalAccount = UtilProperties.getPropertyValue(configString, "payment.paypal.business");
 
        if (UtilValidate.isEmpty(redirectUrl)
            || UtilValidate.isEmpty(notifyUrl)
            || UtilValidate.isEmpty(returnUrl)
            || UtilValidate.isEmpty(cancelReturnUrl)
            || UtilValidate.isEmpty(imageUrl)
            || UtilValidate.isEmpty(payPalAccount) ) {
            Debug.logError("Payment properties is not configured properly, some notify URL from PayPal is not correctly defined!", module);
            request.setAttribute("_ERROR_MESSAGE_", UtilProperties.getMessage(resourceErr, "payPalEvents.problemsGettingMerchantConfiguration", locale));
            return "error";
        }
 
        // create the redirect string
        Map <String, Object> parameters = new LinkedHashMap <String, Object>();
        parameters.put("cmd", "_xclick");
        parameters.put("business", payPalAccount);
        parameters.put("item_name", itemName);
        parameters.put("item_number", itemNumber);
        parameters.put("invoice", orderId);
        parameters.put("custom", userLogin.getString("userLoginId"));
        parameters.put("amount", orderTotal);
        parameters.put("return", returnUrl);
        parameters.put("cancel_return", cancelReturnUrl);
        parameters.put("notify_url", notifyUrl);
        parameters.put("image_url", imageUrl);
        parameters.put("no_note", "1");        // no notes allowed in paypal (not passed back)
        parameters.put("no_shipping", "1");    // no shipping address required (local shipping used)
 
        String encodedParameters = UtilHttp.urlEncodeArgs(parameters, false);
        String redirectString = redirectUrl + "?" + encodedParameters;
 
        // set the order in the session for cancelled orders
        request.getSession().setAttribute("PAYPAL_ORDER", orderId);
 
        // redirect to paypal
        try {
            response.sendRedirect(redirectString);
        } catch (IOException e) {
            Debug.logError(e, "Problems redirecting to PayPal", module);
            request.setAttribute("_ERROR_MESSAGE_", UtilProperties.getMessage(resourceErr, "payPalEvents.problemsConnectingWithPayPal", locale));
            return "error";
        }
 
        return "success";
    }
 
    /** PayPal Call-Back Event */
    public static String payPalIPN(HttpServletRequest request, HttpServletResponse response) {
        Locale locale = UtilHttp.getLocale(request);
        GenericDelegator delegator = (GenericDelegator) request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
 
        // get the product store
        GenericValue productStore = ProductStoreWorker.getProductStore(request);
        if (productStore == null) {
            Debug.logError("ProductStore is null", module);
            request.setAttribute("_ERROR_MESSAGE_", UtilProperties.getMessage(resourceErr, "payPalEvents.problemsGettingMerchantConfiguration", locale));
            return "error";
        }
 
        // get the payment properties file
        GenericValue paymentConfig = ProductStoreWorker.getProductStorePaymentSetting(delegator, productStore.getString("productStoreId"), "EXT_PAYPAL", null, true);
 
        String configString = null;
        if (paymentConfig != null) {
            configString = paymentConfig.getString("paymentPropertiesPath");
        }

        if (configString == null) {
            configString = "payment.properties";
        }
 
        // get the confirm URL
        String confirmUrl = UtilProperties.getPropertyValue(configString, "payment.paypal.confirm");
 
        // get the redirect URL
        String redirectUrl = UtilProperties.getPropertyValue(configString, "payment.paypal.redirect");
 
        if (confirmUrl == null || redirectUrl == null) {
            Debug.logError("Payment properties is not configured properly, no confirm URL defined!", module);
            request.setAttribute("_ERROR_MESSAGE_", UtilProperties.getMessage(resourceErr, "payPalEvents.problemsGettingMerchantConfiguration", locale));
            return "error";
        }
 
        // first verify this is valid from PayPal
        Map <String, Object> parametersMap = UtilHttp.getParameterMap(request);
        parametersMap.put("cmd", "_notify-validate");
 
        // send off the confirm request
        String confirmResp = null;

        try {
            String str = UtilHttp.urlEncodeArgs(parametersMap);
            URL u = new URL(redirectUrl);
            URLConnection uc = u.openConnection();
            uc.setDoOutput(true);
            uc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            PrintWriter pw = new PrintWriter(uc.getOutputStream());
            pw.println(str);
            pw.close();

            BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream()));
            confirmResp = in.readLine();
            in.close();
            Debug.logError("PayPal Verification Response: " + confirmResp, module);
        } catch (IOException e) {
            Debug.logError(e, "Problems sending verification message", module);
        }

        if (confirmResp.trim().equals("VERIFIED")) {
            // we passed verification
            Debug.logInfo("Got verification from PayPal, processing..", module);
        } else {
            Debug.logError("###### PayPal did not verify this request, need investigation!", module);
            Set <String> keySet = parametersMap.keySet();
            Iterator <String> i = keySet.iterator();
            while (i.hasNext()) {
                String name = (String) i.next();
                String value = request.getParameter(name);
                Debug.logError("### Param: " + name + " => " + value, module);
            }
        }
 
        // get the user
        GenericValue userLogin = null;
        String userLoginId = request.getParameter("custom");
        if (userLoginId == null)
            userLoginId = "admin";
        try {
            userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", userLoginId));
        } catch (GenericEntityException e) {
            Debug.logError(e, "Cannot get UserLogin for: " + userLoginId + "; cannot continue", module);
            request.setAttribute("_ERROR_MESSAGE_", UtilProperties.getMessage(resourceErr, "payPalEvents.problemsGettingAuthenticationUser", locale));
            return "error";
        }
 
        // get the orderId
        String orderId = request.getParameter("invoice");

        // get the order header
        GenericValue orderHeader = null;
        if (UtilValidate.isNotEmpty(orderId)) {
            try {
                orderHeader = delegator.findByPrimaryKey("OrderHeader", UtilMisc.toMap("orderId", orderId));
            } catch (GenericEntityException e) {
                Debug.logError(e, "Cannot get the order header for order: " + orderId, module);
                request.setAttribute("_ERROR_MESSAGE_", UtilProperties.getMessage(resourceErr, "payPalEvents.problemsGettingOrderHeader", locale));
                return "error";
            }
        } else {
            Debug.logError("PayPal did not callback with a valid orderId!", module);
            request.setAttribute("_ERROR_MESSAGE_", UtilProperties.getMessage(resourceErr, "payPalEvents.noValidOrderIdReturned", locale));
            return "error";
        }

        if (orderHeader == null) {
            Debug.logError("Cannot get the order header for order: " + orderId, module);
            request.setAttribute("_ERROR_MESSAGE_", UtilProperties.getMessage(resourceErr, "payPalEvents.problemsGettingOrderHeader", locale));
            return "error";
        }

        /*  get payment data
        String paymentCurrency = request.getParameter("mc_currency");
        String paymentAmount = request.getParameter("mc_gross");
        String paymentFee = request.getParameter("mc_fee");
        String transactionId = request.getParameter("txn_id");
        */

        // get the transaction status
        String paymentStatus = request.getParameter("payment_status");

        // attempt to start a transaction
        boolean okay = true;
        boolean beganTransaction = false;
        try {
            beganTransaction = TransactionUtil.begin();

            if (paymentStatus.equals("Completed")) {
                okay = OrderChangeHelper.approveOrder(dispatcher, userLogin, orderId);
            } else if (paymentStatus.equals("Failed") || paymentStatus.equals("Denied")) {
                okay = OrderChangeHelper.cancelOrder(dispatcher, userLogin, orderId);
            }

            if (okay) {
                // set the payment preference
                okay = setPaymentPreferences(delegator, dispatcher, userLogin, orderId, request);
            }
        } catch (Exception e) {
            String errMsg = "Error handling PayPal notification";
            Debug.logError(e, errMsg, module);
            try {
                TransactionUtil.rollback(beganTransaction, errMsg, e);
            } catch (GenericTransactionException gte2) {
                Debug.logError(gte2, "Unable to rollback transaction", module);
            }
        } finally {
            if (!okay) {
                try {
                    TransactionUtil.rollback(beganTransaction, "Failure in processing PayPal callback", null);
                } catch (GenericTransactionException gte) {
                    Debug.logError(gte, "Unable to rollback transaction", module);
                }
            } else {
                try {
                    TransactionUtil.commit(beganTransaction);
                } catch (GenericTransactionException gte) {
                    Debug.logError(gte, "Unable to commit transaction", module);
                }
            }
        }


        if (okay) {
            // attempt to release the offline hold on the order (workflow)
            OrderChangeHelper.releaseInitialOrderHold(dispatcher, orderId);

            // call the email confirm service
            Map <String, String> emailContext = UtilMisc.toMap("orderId", orderId);
            try {
                dispatcher.runSync("sendOrderConfirmation", emailContext);
            } catch (GenericServiceException e) {
                Debug.logError(e, "Problems sending email confirmation", module);
            }
        }

        return "success";
    }
 
    /** Event called when customer cancels a paypal order */
    public static String cancelPayPalOrder(HttpServletRequest request, HttpServletResponse response) {
        Locale locale = UtilHttp.getLocale(request);
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        GenericValue userLogin = (GenericValue) request.getSession().getAttribute("userLogin");
 
        // get the stored order id from the session
        String orderId = (String) request.getSession().getAttribute("PAYPAL_ORDER");
 
        // attempt to start a transaction
        boolean beganTransaction = false;
        try {
            beganTransaction = TransactionUtil.begin();
        } catch (GenericTransactionException gte) {
            Debug.logError(gte, "Unable to begin transaction", module);
        }
 
        // cancel the order
        boolean okay = OrderChangeHelper.cancelOrder(dispatcher, userLogin, orderId);
 
        if (okay) {
            try {
                TransactionUtil.commit(beganTransaction);
            } catch (GenericTransactionException gte) {
                Debug.logError(gte, "Unable to commit transaction", module);
            }
        } else {
            try {
                TransactionUtil.rollback(beganTransaction, "Failure in processing PayPal cancel callback", null);
            } catch (GenericTransactionException gte) {
                Debug.logError(gte, "Unable to rollback transaction", module);
            }
        }
 
        // attempt to release the offline hold on the order (workflow)
        if (okay)
            OrderChangeHelper.releaseInitialOrderHold(dispatcher, orderId);
 
        request.setAttribute("_EVENT_MESSAGE_", UtilProperties.getMessage(resourceErr, "payPalEvents.previousPayPalOrderHasBeenCancelled", locale));
        return "success";
    }
 
    private static boolean setPaymentPreferences(GenericDelegator delegator, LocalDispatcher dispatcher, GenericValue userLogin, String orderId, HttpServletRequest request) {
        Debug.logVerbose("Setting payment prefrences..", module);
        List <GenericValue> paymentPrefs = null;
        try {
            Map <String, String> paymentFields = UtilMisc.toMap("orderId", orderId, "statusId", "PAYMENT_NOT_RECEIVED");
            paymentPrefs = delegator.findByAnd("OrderPaymentPreference", paymentFields);
        } catch (GenericEntityException e) {
            Debug.logError(e, "Cannot get payment preferences for order #" + orderId, module);
            return false;
        }
        if (paymentPrefs.size() > 0) {
            Iterator <GenericValue> i = paymentPrefs.iterator();
            while (i.hasNext()) {
                GenericValue pref = (GenericValue) i.next();
                boolean okay = setPaymentPreference(dispatcher, userLogin, pref, request);
                if (!okay)
                    return false;
            }
        }
        return true;
    }
 
    private static boolean setPaymentPreference(LocalDispatcher dispatcher, GenericValue userLogin, GenericValue paymentPreference, HttpServletRequest request) {
        Locale locale = UtilHttp.getLocale(request);
        String paymentDate = request.getParameter("payment_date");
        String paymentType = request.getParameter("payment_type");
        String paymentAmount = request.getParameter("mc_gross");
        String paymentStatus = request.getParameter("payment_status");
        String transactionId = request.getParameter("txn_id");

        List <GenericValue> toStore = new LinkedList <GenericValue> ();

        // PayPal returns the timestamp in the format 'hh:mm:ss Jan 1, 2000 PST'
        // Parse this into a valid Timestamp Object
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss MMM d, yyyy z");
        java.sql.Timestamp authDate = null;
        try {
            authDate = new java.sql.Timestamp(sdf.parse(paymentDate).getTime());
        } catch (ParseException e) {
            Debug.logError(e, "Cannot parse date string: " + paymentDate, module);
            authDate = UtilDateTime.nowTimestamp();
        } catch (NullPointerException e) {
            Debug.logError(e, "Cannot parse date string: " + paymentDate, module);
            authDate = UtilDateTime.nowTimestamp();
        }

        paymentPreference.set("maxAmount", new BigDecimal(paymentAmount));
        if (paymentStatus.equals("Completed")) {
            paymentPreference.set("statusId", "PAYMENT_RECEIVED");
        } else if (paymentStatus.equals("Pending")) {
            paymentPreference.set("statusId", "PAYMENT_NOT_RECEIVED");
        } else {
            paymentPreference.set("statusId", "PAYMENT_CANCELLED");
        }
        toStore.add(paymentPreference);


        GenericDelegator delegator = paymentPreference.getDelegator();

        // create the PaymentGatewayResponse
        String responseId = delegator.getNextSeqId("PaymentGatewayResponse");
        GenericValue response = delegator.makeValue("PaymentGatewayResponse");
        response.set("paymentGatewayResponseId", responseId);
        response.set("paymentServiceTypeEnumId", "PRDS_PAY_EXTERNAL");
        response.set("orderPaymentPreferenceId", paymentPreference.get("orderPaymentPreferenceId"));
        response.set("paymentMethodTypeId", paymentPreference.get("paymentMethodTypeId"));
        response.set("paymentMethodId", paymentPreference.get("paymentMethodId"));

        // set the auth info
        response.set("amount", new BigDecimal(paymentAmount));
        response.set("referenceNum", transactionId);
        response.set("gatewayCode", paymentStatus);
        response.set("gatewayFlag", paymentStatus.substring(0,1));
        response.set("gatewayMessage", paymentType);
        response.set("transactionDate", authDate);
        toStore.add(response);

        try {
            delegator.storeAll(toStore);
        } catch (GenericEntityException e) {
            Debug.logError(e, "Cannot set payment preference/payment info", module);
            return false;
        }
 
        // create a payment record too
        Map <String, Object> results = null;
        try {
            String comment = UtilProperties.getMessage(resource, "AccountingPaymentReceiveViaPayPal", locale);
            results = dispatcher.runSync("createPaymentFromPreference", UtilMisc.toMap("userLogin", userLogin,
                    "orderPaymentPreferenceId", paymentPreference.get("orderPaymentPreferenceId"), "comments", comment));
        } catch (GenericServiceException e) {
            Debug.logError(e, "Failed to execute service createPaymentFromPreference", module);
            request.setAttribute("_ERROR_MESSAGE_", UtilProperties.getMessage(resourceErr, "payPalEvents.failedToExecuteServiceCreatePaymentFromPreference", locale));
            return false;
        }

        if ((results == null) || (results.get(ModelService.RESPONSE_MESSAGE).equals(ModelService.RESPOND_ERROR))) {
            Debug.logError((String) results.get(ModelService.ERROR_MESSAGE), module);
            request.setAttribute("_ERROR_MESSAGE_", (String) results.get(ModelService.ERROR_MESSAGE));
            return false;
        }

        return true;
    }
}