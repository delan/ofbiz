/*
 * $Id: CheckOutEvents.java,v 1.33 2004/07/19 02:41:37 ajzeneski Exp $
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
package org.ofbiz.order.shoppingcart;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.GeneralException;
import org.ofbiz.base.util.GeneralRuntimeException;
import org.ofbiz.base.util.UtilHttp;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilProperties;
import org.ofbiz.content.stats.VisitHandler;
import org.ofbiz.entity.GenericDelegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.marketing.tracking.TrackingCodeEvents;
import org.ofbiz.product.catalog.CatalogWorker;
import org.ofbiz.product.store.ProductStoreWorker;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ModelService;
import org.ofbiz.service.ServiceUtil;

/**
 * Events used for processing checkout and orders.
 *
 * @author <a href="mailto:jaz@ofbiz.org">Andy Zeneski</a>
 * @author <a href="mailto:cnelson@einnovation.com">Chris Nelson</a>
 * @author <a href="mailto:jonesde@ofbiz.org">David E. Jones</a>
 * @author <a href="mailto:tristana@twibble.org">Tristan Austin</a>
 * @version $Revision: 1.33 $
 * @since 2.0
 */
public class CheckOutEvents {

    public static final String module = CheckOutEvents.class.getName();
    public static final String resource = "OrderUiLabels";

    public static String cartNotEmpty(HttpServletRequest request, HttpServletResponse response) {
        ShoppingCart cart = (ShoppingCart) request.getSession().getAttribute("shoppingCart");
        Locale locale = UtilHttp.getLocale(request);
        String errMsg = null;

        if (cart != null && cart.size() > 0) {
            return "success";
        } else {
            errMsg = UtilProperties.getMessage(resource, "checkevents.cart_empty", (cart != null ? cart.getLocale() : Locale.getDefault()));
            request.setAttribute("_ERROR_MESSAGE_", errMsg);
            return "error";
        }
    }

    public static String cancelOrderItem(HttpServletRequest request, HttpServletResponse response) {
        ShoppingCart cart = (ShoppingCart) request.getSession().getAttribute("shoppingCart");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        GenericValue userLogin = (GenericValue) request.getSession().getAttribute("userLogin");
        String orderId = request.getParameter("order_id");
        String itemSeqId = request.getParameter("item_seq");
        Locale locale = UtilHttp.getLocale(request);

        Map fields = UtilMisc.toMap("orderId", orderId, "orderItemSeqId", itemSeqId, "userLogin", userLogin);
        Map result = null;
        String errMsg = null;

        try {
            result = dispatcher.runSync("cancelOrderItem", fields);
        } catch (GenericServiceException e) {
            Debug.logError(e, module);
            errMsg = UtilProperties.getMessage(resource, "checkevents.cannot_cancel_item", (cart != null ? cart.getLocale() : Locale.getDefault()));
            request.setAttribute("_ERROR_MESSAGE_", errMsg);
            return "error";
        }

        if (result.containsKey(ModelService.ERROR_MESSAGE)) {
            request.setAttribute("_ERROR_MESSAGE_", result.get(ModelService.ERROR_MESSAGE));
            return "error";
        }

        return "success";
    }

    public static String setCheckOutPages(HttpServletRequest request, HttpServletResponse response) {
        if ("error".equals(CheckOutEvents.cartNotEmpty(request, response)) == true) {
            return "error";
        }

        Locale locale = UtilHttp.getLocale(request);
        String curPage = request.getParameter("checkoutpage");
        Debug.logInfo("CheckoutPage: " + curPage, module);

        Map callResult = null;
        String errMsg = null;

        ShoppingCart cart = (ShoppingCart) request.getSession().getAttribute("shoppingCart");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        GenericDelegator delegator = (GenericDelegator) request.getAttribute("delegator");
        CheckOutHelper checkOutHelper = new CheckOutHelper(dispatcher, delegator, cart);

        if ("shippingaddress".equals(curPage) == true) {
            // Set the shipping address options
            String shippingContactMechId = request.getParameter("shipping_contact_mech_id");
            callResult = checkOutHelper.setCheckOutShippingAddress(shippingContactMechId);

            ServiceUtil.getMessages(request, callResult, null);

            if (!(callResult.get(ModelService.RESPONSE_MESSAGE).equals(ModelService.RESPOND_ERROR))) {
                // No errors so push the user onto the next page
                curPage = "shippingoptions";
            }
        } else if ("shippingoptions".equals(curPage) == true) {
            // Set the general shipping options
            String shippingMethod = request.getParameter("shipping_method");
            String correspondingPoId = request.getParameter("corresponding_po_id");
            String shippingInstructions = request.getParameter("shipping_instructions");
            String orderAdditionalEmails = request.getParameter("order_additional_emails");
            String maySplit = request.getParameter("may_split");
            String giftMessage = request.getParameter("gift_message");
            String isGift = request.getParameter("is_gift");
            callResult = checkOutHelper.setCheckOutShippingOptions(shippingMethod, correspondingPoId,
                    shippingInstructions, orderAdditionalEmails, maySplit, giftMessage, isGift);

            ServiceUtil.getMessages(request, callResult, null);

            if (!(callResult.get(ModelService.RESPONSE_MESSAGE).equals(ModelService.RESPOND_ERROR))) {
                // No errors so push the user onto the next page
                curPage = "payment";
            }
        } else if ("payment".equals(curPage) == true) {
            // get the currency format
            String currencyFormat = UtilProperties.getPropertyValue("general.properties", "currency.decimal.format", "##0.00");
            DecimalFormat formatter = new DecimalFormat(currencyFormat);

            // Set the payment options
            Map selectedPaymentMethods = getSelectedPaymentMethods(request);
            if (selectedPaymentMethods == null) {
                return "error";
            }

            String billingAccountId = request.getParameter("billingAccountId");
            String billingAcctAmtStr = request.getParameter("amount_" + billingAccountId);
            Double billingAccountAmt = null;
            // parse the amount to a decimal
            if (billingAcctAmtStr != null) {
                try {
                    billingAccountAmt = new Double(formatter.parse(billingAcctAmtStr).doubleValue());
                } catch (ParseException e) {
                    Debug.logError(e, module);
                    Map messageMap = UtilMisc.toMap("billingAccountId", billingAccountId);
                    errMsg = UtilProperties.getMessage(resource, "checkevents.invalid_amount_set_for_billing_account", messageMap, (cart != null ? cart.getLocale() : Locale.getDefault()));
                    request.setAttribute("_ERROR_MESSAGE_", errMsg);
                    return "error";
                }
            }

            List singleUsePayments = new ArrayList();

            // check for gift card not on file
            Map params = UtilHttp.getParameterMap(request);
            Map gcResult = checkOutHelper.checkGiftCard(params, selectedPaymentMethods);
            ServiceUtil.getMessages(request, gcResult, null);
            if (gcResult.get(ModelService.RESPONSE_MESSAGE).equals(ModelService.RESPOND_ERROR)) {
                return "error";
            } else {
                String gcPaymentMethodId = (String) gcResult.get("paymentMethodId");
                Double gcAmount = (Double) gcResult.get("amount");
                if (gcPaymentMethodId != null) {
                    selectedPaymentMethods.put(gcPaymentMethodId, gcAmount);
                    if ("Y".equalsIgnoreCase(request.getParameter("singleUseGiftCard"))) {
                        singleUsePayments.add(gcPaymentMethodId);
                    }
                }
            }

            callResult = checkOutHelper.setCheckOutPayment(selectedPaymentMethods, singleUsePayments, billingAccountId, billingAccountAmt);
            ServiceUtil.getMessages(request, callResult, null);

            if (!(callResult.get(ModelService.RESPONSE_MESSAGE).equals(ModelService.RESPOND_ERROR))) {
                // No errors so push the user onto the next page
                curPage = "confirm";
            }
        } else {
            curPage = "shippingaddress";
        }

        return curPage;
    }

    public static String setCheckOutError(HttpServletRequest request, HttpServletResponse response) {
        String currentPage = request.getParameter("checkoutpage");
        if (currentPage == null || currentPage.length() == 0) {
            return "error";
        } else {
            return currentPage;
        }
    }

    public static String setPartialCheckOutOptions(HttpServletRequest request, HttpServletResponse response) {
        String resp = setCheckOutOptions(request, response);
        request.setAttribute("_ERROR_MESSAGE_", null);
        return "success";
    }

    public static String checkPaymentMethods(HttpServletRequest request, HttpServletResponse response) {
        ShoppingCart cart = (ShoppingCart) request.getSession().getAttribute("shoppingCart");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        GenericDelegator delegator = (GenericDelegator) request.getAttribute("delegator");
        Locale locale = UtilHttp.getLocale(request);

        String errMsg = null;

        CheckOutHelper checkOutHelper = new CheckOutHelper(dispatcher, delegator, cart);
        String billingAccountId = cart.getBillingAccountId();
        double billingAccountAmt = cart.getBillingAccountAmount();
        double availableAmount = checkOutHelper.availableAccountBalance(billingAccountId);
        if (billingAccountAmt > availableAmount) {
            Map messageMap = UtilMisc.toMap("billingAccountId", billingAccountId);
            errMsg = UtilProperties.getMessage(resource, "checkevents.not_enough_available_on_account", messageMap, (cart != null ? cart.getLocale() : Locale.getDefault()));
            request.setAttribute("_ERROR_MESSAGE_", errMsg);
            return "error";
        }

        // payment by billing account only requires more checking
        List paymentMethods = cart.getPaymentMethodIds();
        List paymentTypes = cart.getPaymentMethodTypeIds();
        if (paymentTypes.contains("EXT_BILLACT") && paymentTypes.size() == 1 && paymentMethods.size() == 0) {
            if (cart.getGrandTotal() > availableAmount) {
                errMsg = UtilProperties.getMessage(resource, "checkevents.insufficient_credit_available_on_account", (cart != null ? cart.getLocale() : Locale.getDefault()));
                request.setAttribute("_ERROR_MESSAGE_", errMsg);
                return "error";
            }
        }

        // validate any gift card balances
        CheckOutEvents.validateGiftCardAmounts(request);

        // update the selected payment methods amount with valid numbers
        if (paymentMethods != null) {
            List nullPaymentIds = new ArrayList();
            Iterator i = paymentMethods.iterator();
            while (i.hasNext()) {
                String paymentMethodId = (String) i.next();
                Double paymentAmount = cart.getPaymentAmount(paymentMethodId);
                if (paymentAmount == null || paymentAmount.doubleValue() == 0) {
                    nullPaymentIds.add(paymentMethodId);
                }
            }
            Iterator npi = nullPaymentIds.iterator();
            while (npi.hasNext()) {
                String paymentMethodId = (String) npi.next();
                Double paymentTotal = cart.getPaymentTotal();
                double selectedPaymentTotal = paymentTotal != null ? paymentTotal.doubleValue() : 0.00;
                double requiredAmount = cart.getGrandTotal() - cart.getBillingAccountAmount();
                double nullAmount = requiredAmount - selectedPaymentTotal;
                if (nullAmount > 0) {
                    cart.addPaymentAmount(paymentMethodId, nullAmount);
                }
            }
        }

        // verify the selected payment method amounts will cover the total
        Double paymentTotal = cart.getPaymentTotal();
        double selectedPaymentTotal = paymentTotal != null ? paymentTotal.doubleValue() : 0.00;
        double requiredAmount = cart.getGrandTotal() - cart.getBillingAccountAmount();
        if (paymentMethods != null && paymentMethods.size() > 0 && requiredAmount > selectedPaymentTotal) {
            Debug.logError("Required Amount : " + requiredAmount + " / Selected Amount : " + selectedPaymentTotal, module);
            errMsg = UtilProperties.getMessage(resource, "checkevents.payment_not_cover_this_order", (cart != null ? cart.getLocale() : Locale.getDefault()));
            request.setAttribute("_ERROR_MESSAGE_", errMsg);
            return "error";
        }

        return "success";
    }

    public static Map getSelectedPaymentMethods(HttpServletRequest request) {
        ShoppingCart cart = (ShoppingCart) request.getSession().getAttribute("shoppingCart");
        Locale locale = UtilHttp.getLocale(request);
        String currencyFormat = UtilProperties.getPropertyValue("general.properties", "currency.decimal.format", "##0.00");
        DecimalFormat formatter = new DecimalFormat(currencyFormat);
        Map selectedPaymentMethods = new HashMap();
        String[] paymentMethods = request.getParameterValues("checkOutPaymentId");
        String errMsg = null;

        if (paymentMethods != null) {
            for (int i = 0; i < paymentMethods.length; i++) {
                String amountStr = request.getParameter("amount_" + paymentMethods[i]);
                Double amount = null;
                if (amountStr != null && amountStr.length() > 0 && !"REMAINING".equals(amountStr)) {
                    try {
                        amount = new Double(formatter.parse(amountStr).doubleValue());
                    } catch (ParseException e) {
                        Debug.logError(e, module);
                        errMsg = UtilProperties.getMessage(resource, "checkevents.invalid_amount_set_for_payment_method", (cart != null ? cart.getLocale() : Locale.getDefault()));
                        request.setAttribute("_ERROR_MESSAGE_", errMsg);
                        return null;
                    }
                }
                selectedPaymentMethods.put(paymentMethods[i], amount);
            }
        }
        Debug.logInfo("Selected Payment Methods : " + selectedPaymentMethods, module);
        return selectedPaymentMethods;
    }

    private static void validateGiftCardAmounts(HttpServletRequest request) {
        ShoppingCart cart = (ShoppingCart) request.getSession().getAttribute("shoppingCart");
        GenericDelegator delegator = (GenericDelegator) request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");

        // get the product store
        GenericValue productStore = ProductStoreWorker.getProductStore(cart.getProductStoreId(), delegator);
        if (productStore != null && !"Y".equalsIgnoreCase(productStore.getString("checkGcBalance"))) {
            return;
        }

        // get the payment config
        String paymentConfig = ProductStoreWorker.getProductStorePaymentProperties(delegator, cart.getProductStoreId(), "GIFT_CARD", null, true);

        // get the gift card objects to check
        Iterator i = cart.getGiftCards().iterator();
        while (i.hasNext()) {
            GenericValue gc = (GenericValue) i.next();
            Map gcBalanceMap = null;
            double gcBalance = 0.00;
            try {
                Map ctx = UtilMisc.toMap("paymentConfig", paymentConfig);
                ctx.put("currency", cart.getCurrency());
                ctx.put("cardNumber", gc.getString("cardNumber"));
                ctx.put("pin", gc.getString("pinNumber"));
                gcBalanceMap = dispatcher.runSync("balanceInquireGiftCard", ctx);
            } catch (GenericServiceException e) {
                Debug.logError(e, module);
            }
            if (gcBalanceMap != null) {
                Double bal = (Double) gcBalanceMap.get("balance");
                if (bal != null) {
                    gcBalance = bal.doubleValue();
                }
            }

            // get the bill-up to amount
            Double billUpTo = cart.getPaymentAmount(gc.getString("paymentMethodId"));

            // null bill-up to means use the full balance || update the bill-up to with the balance
            if (billUpTo == null || billUpTo.doubleValue() == 0 || gcBalance < billUpTo.doubleValue()) {
                cart.addPaymentAmount(gc.getString("paymentMethodId"), new Double(gcBalance));
            }
        }
    }

    public static String setCheckOutOptions(HttpServletRequest request, HttpServletResponse response) {
        ShoppingCart cart = (ShoppingCart) request.getSession().getAttribute("shoppingCart");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        GenericDelegator delegator = (GenericDelegator) request.getAttribute("delegator");

        String errMsg = null;

        // get the currency format
        String currencyFormat = UtilProperties.getPropertyValue("general.properties", "currency.decimal.format", "##0.00");
        DecimalFormat formatter = new DecimalFormat(currencyFormat);

        // Set the payment options
        Map selectedPaymentMethods = getSelectedPaymentMethods(request);
        if (selectedPaymentMethods == null) {
            return "error";
        }

        String shippingMethod = request.getParameter("shipping_method");
        String shippingContactMechId = request.getParameter("shipping_contact_mech_id");
        String correspondingPoId = request.getParameter("corresponding_po_id");
        String shippingInstructions = request.getParameter("shipping_instructions");
        String orderAdditionalEmails = request.getParameter("order_additional_emails");
        String maySplit = request.getParameter("may_split");
        String giftMessage = request.getParameter("gift_message");
        String isGift = request.getParameter("is_gift");
        List singleUsePayments = new ArrayList();

        // get the billing account and amount
        String billingAccountId = request.getParameter("billingAccountId");
        String billingAcctAmtStr = request.getParameter("amount_" + billingAccountId);
        Double billingAccountAmt = null;
        // parse the amount to a decimal
        if (billingAcctAmtStr != null) {
            try {
                billingAccountAmt = new Double(formatter.parse(billingAcctAmtStr).doubleValue());
            } catch (ParseException e) {
                Debug.logError(e, module);
                Map messageMap = UtilMisc.toMap("billingAccountId", billingAccountId);
                errMsg = UtilProperties.getMessage(resource, "checkevents.invalid_amount_set_for_billing_account", messageMap, (cart != null ? cart.getLocale() : Locale.getDefault()));
                request.setAttribute("_ERROR_MESSAGE_", errMsg);
                return "error";
            }
        }

        // get a request map of parameters
        Map params = UtilHttp.getParameterMap(request);
        CheckOutHelper checkOutHelper = new CheckOutHelper(dispatcher, delegator, cart);

        // check for gift card not on file
        Map gcResult = checkOutHelper.checkGiftCard(params, selectedPaymentMethods);
        ServiceUtil.getMessages(request, gcResult, null);
        if (gcResult.get(ModelService.RESPONSE_MESSAGE).equals(ModelService.RESPOND_ERROR)) {
            return "error";
        } else {
            String gcPaymentMethodId = (String) gcResult.get("paymentMethodId");
            Double gcAmount = (Double) gcResult.get("amount");
            if (gcPaymentMethodId != null) {
                selectedPaymentMethods.put(gcPaymentMethodId, gcAmount);
                if ("Y".equalsIgnoreCase(request.getParameter("singleUseGiftCard"))) {
                    singleUsePayments.add(gcPaymentMethodId);
                }
            }
        }

        Map optResult = checkOutHelper.setCheckOutOptions(shippingMethod, shippingContactMechId, selectedPaymentMethods,
                singleUsePayments, billingAccountId, billingAccountAmt, correspondingPoId, shippingInstructions,
                orderAdditionalEmails, maySplit, giftMessage, isGift);

        ServiceUtil.getMessages(request, optResult, null);
        if (optResult.get(ModelService.RESPONSE_MESSAGE).equals(ModelService.RESPOND_ERROR)) {
            return "error";
        } else {
            return "success";
        }
    }

    // Create order event - uses createOrder service for processing
    public static String createOrder(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession();
        ServletContext application = ((ServletContext) request.getAttribute("servletContext"));
        ShoppingCart cart = ShoppingCartEvents.getCartObject(request);
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        GenericDelegator delegator = (GenericDelegator) request.getAttribute("delegator");
        GenericValue userLogin = (GenericValue) session.getAttribute("userLogin");
        CheckOutHelper checkOutHelper = new CheckOutHelper(dispatcher, delegator, cart);
        Map callResult;

        // remove this whenever creating an order so quick reorder cache will refresh/recalc
        session.removeAttribute("_QUICK_REORDER_PRODUCTS_");

        boolean areOrderItemsExploded = explodeOrderItems(delegator, cart);

        //get the TrackingCodeOrder List
        List trackingCodeOrders = TrackingCodeEvents.makeTrackingCodeOrders(request);
        String distributorId = (String) session.getAttribute("_DISTRIBUTOR_ID_");
        String affiliateId = (String) session.getAttribute("_AFFILIATE_ID_");
        String visitId = VisitHandler.getVisitId(session);
        String webSiteId = CatalogWorker.getWebSiteId(request);

        callResult = checkOutHelper.createOrder(userLogin, distributorId, affiliateId, trackingCodeOrders, areOrderItemsExploded, visitId, webSiteId);
        if (callResult != null) {
            ServiceUtil.getMessages(request, callResult, null);
            if (ServiceUtil.isError(callResult)) {
                // messages already setup with the getMessages call, just return the error response code
                return "error";
            }
            if (callResult.get(ModelService.RESPONSE_MESSAGE).equals(ModelService.RESPOND_SUCCESS)) {
                // set the orderId for use by chained events
                String orderId = cart.getOrderId();
                request.setAttribute("order_id", orderId);
                request.setAttribute("orderId", orderId);
                request.setAttribute("orderAdditionalEmails", cart.getOrderAdditionalEmails());
            }
        }

        return cart.getOrderType().toLowerCase();
    }

    // Event wrapper for the tax calc.
    public static String calcTax(HttpServletRequest request, HttpServletResponse response) {
        try {
            calcTax(request);
        } catch (GeneralException e) {
            request.setAttribute("_ERROR_MESSAGE_", e.getMessage());
            return "error";
        }
        return "success";
    }

    // Invoke the taxCalc
    private static void calcTax(HttpServletRequest request) throws GeneralException {
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        GenericDelegator delegator = (GenericDelegator) request.getAttribute("delegator");
        ShoppingCart cart = (ShoppingCart) request.getSession().getAttribute("shoppingCart");
        CheckOutHelper checkOutHelper = new CheckOutHelper(dispatcher, delegator, cart);

        //Calculate and add the tax adjustments
        checkOutHelper.calcAndAddTax();
    }

    public static boolean explodeOrderItems(GenericDelegator delegator, ShoppingCart cart) {
        if (cart == null) return false;
        GenericValue productStore = ProductStoreWorker.getProductStore(cart.getProductStoreId(), delegator);
        if (productStore == null || productStore.get("explodeOrderItems") == null) {
            return false;
        }
        return productStore.getBoolean("explodeOrderItems").booleanValue();
    }

    // Event wrapper for processPayment.
    public static String processPayment(HttpServletRequest request, HttpServletResponse response) {
        // run the process payment process + approve order when complete; may also run sync fulfillments
        int failureCode = 0;
        try {
            if (!processPayment(request)) {
                failureCode = 1;
            }
        } catch (GeneralException e) {
            Debug.logError(e, module);
            ServiceUtil.setMessages(request, e.getMessage(), null, null);
            failureCode = 2;
        } catch (GeneralRuntimeException e) {
            Debug.logError(e, module);
            ServiceUtil.setMessages(request, e.getMessage(), null, null);
        }

        // event return based on failureCode
        switch (failureCode) {
            case 0:
                return "success";
            case 1:
                return "fail";
            default:
                return "error";
        }
    }

    private static boolean processPayment(HttpServletRequest request) throws GeneralException {
        HttpSession session = request.getSession();
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        GenericDelegator delegator = (GenericDelegator) request.getAttribute("delegator");
        ShoppingCart cart = (ShoppingCart) request.getSession().getAttribute("shoppingCart");
        GenericValue userLogin = (GenericValue) session.getAttribute("userLogin");
        CheckOutHelper checkOutHelper = new CheckOutHelper(dispatcher, delegator, cart);

        // load the ProductStore settings
        GenericValue productStore = ProductStoreWorker.getProductStore(cart.getProductStoreId(), delegator);
        Map callResult = checkOutHelper.processPayment(productStore, userLogin);

        // generate any messages required
        ServiceUtil.getMessages(request, callResult, null);

        // check for customer message(s)
        List messages = (List) callResult.get("authResultMsgs");
        if (messages != null && messages.size() > 0) {
            request.setAttribute("_EVENT_MESSAGE_LIST_", messages);
        }

        // determine whether it was a success or failure
        return (callResult.get(ModelService.RESPONSE_MESSAGE).equals(ModelService.RESPOND_SUCCESS));
    }

    public static String checkOrderBlacklist(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession();
        ShoppingCart cart = (ShoppingCart) session.getAttribute("shoppingCart");
        GenericDelegator delegator = (GenericDelegator) request.getAttribute("delegator");
        GenericValue userLogin = (GenericValue) session.getAttribute("userLogin");
        CheckOutHelper checkOutHelper = new CheckOutHelper(null, delegator, cart);
        String result;

        Map callResult = checkOutHelper.checkOrderBlacklist(userLogin);
        if (callResult.get(ModelService.RESPONSE_MESSAGE).equals(ModelService.RESPOND_ERROR)) {
            result = (String) callResult.get(ModelService.ERROR_MESSAGE);
        } else {
            result = (String) callResult.get(ModelService.SUCCESS_MESSAGE);
        }

        return result;
    }

    public static String failedBlacklistCheck(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession();
        ShoppingCart cart = (ShoppingCart) session.getAttribute("shoppingCart");
        GenericDelegator delegator = (GenericDelegator) request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        GenericValue userLogin = (GenericValue) session.getAttribute("userLogin");
        String result;

        // Load the properties store
        GenericValue productStore = ProductStoreWorker.getProductStore(cart.getProductStoreId(), delegator);
        CheckOutHelper checkOutHelper = new CheckOutHelper(dispatcher, delegator, cart);
        Map callResult = checkOutHelper.failedBlacklistCheck(userLogin, productStore);

        //Generate any messages required
        ServiceUtil.getMessages(request, callResult, null);

        // wipe the session
        session.invalidate();

        //Determine whether it was a success or not
        if (callResult.get(ModelService.RESPONSE_MESSAGE).equals(ModelService.RESPOND_ERROR)) {
            result = (String) callResult.get(ModelService.ERROR_MESSAGE);
            request.setAttribute("_ERROR_MESSAGE_", result);
            result = "error";
        } else {
            result = (String) callResult.get(ModelService.ERROR_MESSAGE);
            request.setAttribute("_ERROR_MESSAGE_", result);
            result = "success";
        }
        return result;
    }

    public static String checkExternalPayment(HttpServletRequest request, HttpServletResponse response) {
        // warning there can only be ONE payment preference for this to work
        // you cannot accept multiple payment type when using an external gateway
        GenericDelegator delegator = (GenericDelegator) request.getAttribute("delegator");
        String result;

        String orderId = (String) request.getAttribute("order_id");
        CheckOutHelper checkOutHelper = new CheckOutHelper(null, delegator, null);
        Map callResult = checkOutHelper.checkExternalPayment(orderId);

        //Generate any messages required
        ServiceUtil.getMessages(request, callResult, null);

        // any error messages have prepared for display, return the type ('error' if failed)
        result = (String) callResult.get("type");
        return result;
    }

    public static String finalizeOrderEntry(HttpServletRequest request, HttpServletResponse response) {
        ShoppingCart cart = (ShoppingCart) request.getSession().getAttribute("shoppingCart");
        GenericDelegator delegator = (GenericDelegator) request.getAttribute("delegator");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        Map paramMap = UtilHttp.getParameterMap(request);
        Boolean offlinePayments;
        String shippingContactMechId = null;
        String shippingMethod = null;
        String shippingInstructions = null;
        String maySplit = null;
        String giftMessage = null;
        String isGift = null;
        String methodType = null;
        String checkOutPaymentId = null;
        String singleUsePayment = null;
        String appendPayment = null;

        String mode = request.getParameter("finalizeMode");
        Debug.logInfo("FinalizeMode: " + mode, module);

        // check the userLogin object
        GenericValue userLogin = (GenericValue) request.getSession().getAttribute("userLogin");

        // if null then we must be an anonymous shopper
        if (userLogin == null) {
            // remove auto-login fields
            request.getSession().removeAttribute("autoUserLogin");
            request.getSession().removeAttribute("autoName");
        }

        // set the customer info
        if (mode != null && mode.equals("cust")) {
            String partyId = (String) request.getAttribute("partyId");
            if (partyId != null) {
                request.getSession().setAttribute("orderPartyId", partyId);
                // no userLogin means we are an anonymous shopper; fake the UL for service calls
                if (userLogin == null) {
                    try {
                        userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", "anonymous"));
                    } catch (GenericEntityException e) {
                        Debug.logError(e, module);
                    }
                    if (userLogin != null) {
                        userLogin.set("partyId", partyId);
                    }
                    request.getSession().setAttribute("userLogin", userLogin);
                }
            }
        }

        // get the shipping method
        shippingContactMechId = request.getParameter("shipping_contact_mech_id");
        if (shippingContactMechId == null) {
            shippingContactMechId = (String) request.getAttribute("contactMechId");
        }

        // get the options
        shippingMethod = request.getParameter("shipping_method");
        shippingInstructions = request.getParameter("shipping_instructions");
        maySplit = request.getParameter("may_split");
        giftMessage = request.getParameter("gift_message");
        isGift = request.getParameter("is_gift");

        // payment option; if offline we skip the payment screen
        methodType = request.getParameter("paymentMethodType");

        // get the payment
        checkOutPaymentId = request.getParameter("checkOutPaymentId");
        if (checkOutPaymentId == null) {
            checkOutPaymentId = (String) request.getAttribute("paymentMethodId");
        }
        singleUsePayment = request.getParameter("singleUsePayment");
        appendPayment = request.getParameter("appendPayment");
        boolean isSingleUsePayment = singleUsePayment != null && "Y".equalsIgnoreCase(singleUsePayment) ? true : false;
        boolean doAppendPayment = appendPayment != null && "Y".equalsIgnoreCase(appendPayment) ? true : false;

        CheckOutHelper checkOutHelper = new CheckOutHelper(dispatcher, delegator, cart);

        // get the currency format
        String currencyFormat = UtilProperties.getPropertyValue("general.properties", "currency.decimal.format", "##0.00");
        DecimalFormat formatter = new DecimalFormat(currencyFormat);

        // Set the payment options
        Map selectedPaymentMethods = getSelectedPaymentMethods(request);
        if (selectedPaymentMethods == null) {
            return "error";
        }

        String billingAccountId = request.getParameter("billingAccountId");
        String billingAcctAmtStr = request.getParameter("amount_" + billingAccountId);
        Double billingAccountAmt = null;

        // parse the amount to a decimal
        if (billingAcctAmtStr != null) {
            try {
                billingAccountAmt = new Double(formatter.parse(billingAcctAmtStr).doubleValue());
            } catch (ParseException e) {
                Debug.logError(e, module);
                request.setAttribute("_ERROR_MESSAGE_", "Invalid amount set for Billing Account #" + billingAccountId);
                return "error";
            }
        }

        checkOutHelper.setCheckOutPayment(selectedPaymentMethods, null, billingAccountId, billingAccountAmt);

        Map callResult = checkOutHelper.finalizeOrderEntry(mode, shippingContactMechId, shippingMethod, shippingInstructions,
                maySplit, giftMessage, isGift, methodType, checkOutPaymentId, isSingleUsePayment, doAppendPayment, paramMap);

        // generate any messages required
        ServiceUtil.getMessages(request, callResult, null);

        // determine whether it was a success or not
        if (callResult.get(ModelService.RESPONSE_MESSAGE).equals(ModelService.RESPOND_ERROR)) {
            return "error";
        } else {
            // seems a bit suspicious that these properties have slightly different names
            offlinePayments = (Boolean) callResult.get("OFFLINE_PAYMENT");
            request.setAttribute("OFFLINE_PAYMENT", offlinePayments);
            offlinePayments = (Boolean) callResult.get("OFFLINE_PAYMENTS");
            request.getSession().setAttribute("OFFLINE_PAYMENTS", offlinePayments);
        }

        // determine where to direct the browser
        String requireCustomer = null;
        String requireShipping = null;
        String requireOptions = null;
        String requirePayment = null;

        // these options are not available to anonymous shoppers (security)
        if (userLogin != null && !"anonymous".equals(userLogin.getString("userLoginId"))) {
            requireCustomer = request.getParameter("finalizeReqCustInfo");
            requireShipping = request.getParameter("finalizeReqShipInfo");
            requireOptions = request.getParameter("finalizeReqOptions");
            requirePayment = request.getParameter("finalizeReqPayInfo");
        }

        // these are the default values
        if (requireCustomer == null)
            requireCustomer = "true";
        if (requireShipping == null)
            requireShipping = "true";
        if (requireOptions == null)
            requireOptions = "true";
        if (requirePayment == null)
            requirePayment = "true";

        String shipContactMechId = cart.getShippingContactMechId();
        String customerPartyId = cart.getPartyId();
        String shipmentMethodTypeId = cart.getShipmentMethodTypeId();
        List paymentMethodIds = cart.getPaymentMethodIds();
        List paymentMethodTypeIds = cart.getPaymentMethodTypeIds();

        if (requireCustomer.equalsIgnoreCase("true") && (customerPartyId == null || customerPartyId.equals("_NA_"))) {
            return "customer";
        }

        if (requireShipping.equalsIgnoreCase("true") && shipContactMechId == null) {
            return "shipping";
        }

        if (requireOptions.equalsIgnoreCase("true") && shipmentMethodTypeId == null) {
            return "options";
        }

        if (requirePayment.equalsIgnoreCase("true")) {
            if (paymentMethodIds == null || paymentMethodIds.size() == 0) {
                if (paymentMethodTypeIds == null || paymentMethodTypeIds.size() == 0) {
                    return "payment";
                }
            }
        }

        if (isSingleUsePayment) {
            return "paysplit";
        }

        if ("SALES_ORDER".equals(cart.getOrderType())) {
            return "sales";
        } else {
            return "po";
        }
    }

    public static String finalizeOrderEntryError(HttpServletRequest request, HttpServletResponse response) {
        String finalizePage = request.getParameter("finalizeMode");
        if (finalizePage == null || finalizePage.length() == 0) {
            return "error";
        } else {
            return finalizePage;
        }
    }
}
