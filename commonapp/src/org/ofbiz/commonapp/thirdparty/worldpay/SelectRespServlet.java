/*
 * $Id$
 *
 * Copyright (c) 2001, 2002 The Open For Business Project - www.ofbiz.org
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
package org.ofbiz.commonapp.thirdparty.worldpay;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.servlet.*;

import org.ofbiz.core.entity.*;
import org.ofbiz.core.service.*;
import org.ofbiz.core.util.*;
import org.ofbiz.commonapp.order.shoppingcart.*;

import com.worldpay.select.*;
import com.worldpay.select.merchant.*;

/**
 * WorldPay Select Pro Response Servlet
 *
 * @author     <a href="mailto:jaz@jflow.net">Andy Zeneski</a>
 * @version    $Revision$
 * @since      2.0
 */
public class SelectRespServlet extends SelectServlet implements SelectDefs {
    
    public static final String module = SelectRespServlet.class.getName();
    
    private ServletContext sctx = null;
    private GenericDelegator delegator = null;
    private LocalDispatcher dispatcher = null;
    private URL orderPropertiesUrl = null;
    private GenericValue userLogin = null;   
    
    protected void doRequest(SelectServletRequest request, SelectServletResponse response) throws ServletException, IOException {
        Debug.logInfo("Request receive from worldpay..", module);
        
        String webSiteId = request.getParameter("M_webSiteId");
        String delegatorName = request.getParameter("M_delegatorName");
        String dispatchName = request.getParameter("M_dispatchName");
        
        Debug.logInfo("Got websiteId: " + webSiteId, module);
        Debug.logInfo("Got delegatorName:" + delegatorName, module);
        Debug.logInfo("Got dispatchName:" + dispatchName, module);
        
        // get the delegator
        delegator = GenericDelegator.getGenericDelegator(delegatorName);
        
        // get the dispatcher
        ServiceDispatcher serviceDisp = ServiceDispatcher.getInstance(dispatchName, delegator);
        DispatchContext dctx = serviceDisp.getLocalContext(dispatchName);
        dispatcher = dctx.getDispatcher();        
                
        // get the properties file
        //String webSiteId = sctx.getInitParameter("webSiteId");
        String configString = null;
        try {
            GenericValue webSitePayment = delegator.findByPrimaryKey("WebSitePaymentSetting", UtilMisc.toMap("webSiteId", webSiteId, "paymentMethodTypeId", "EXT_WORLDPAY"));
            if (webSitePayment != null)
                configString = webSitePayment.getString("paymentConfiguration");
        } catch (GenericEntityException e) {
            Debug.logWarning(e, "Cannot find webSitePayment Settings", module);
        }
        if (configString == null)
        configString = "payment.properties";    
        Debug.logInfo("Got the payment configuration", module);    
        
        String orderId = request.getParameter(SelectDefs.SEL_cartId);
        String transStatus = request.getParameter(SelectDefs.SEL_transStatus);
        
        // store some stuff for calling existing events
        request.setAttribute("servletContext", sctx);
        request.setAttribute("delegator", delegator);
        request.setAttribute("dispatcher", dispatcher);
        request.setAttribute("order_id", orderId);
        request.setAttribute(SiteDefs.CONTROL_PATH, UtilProperties.getPropertyValue(configString, "payment.general.controlpath", "/control"));
        
        // load the order.properties file.        
        try {
            orderPropertiesUrl = sctx.getResource("/WEB-INF/order.properties");
        } catch (MalformedURLException e) {
            Debug.logWarning(e, "Problems loading order.properties", module);
        }    
        
        if (transStatus.equalsIgnoreCase("Y")) {
            // order was approved
            Debug.logInfo("Order #" + orderId + " approved", module);
            approveOrder(orderId);                       
        } else {
            // order was cancelled
            Debug.logInfo("Order #" + orderId + " cancelled", module);
            cancelOrder(orderId);
        }
        
        // set the payment preference
        setPaymentPreferences(orderId, request);
        
        // call the existing confirm order events (calling direct)
        String confirm = CheckOutEvents.renderConfirmOrder(request, response);
        String email = CheckOutEvents.emailOrder(request, response);
        
        // set up the output stream for the page
        response.setContentType("text/html");
        ServletOutputStream out = response.getOutputStream();
        String content = (String) request.getAttribute("confirmorder");
        if (content != null)
            out.println(content);                                           
    }
        
    private void approveOrder(String orderId) {        
        // get some payment related strings from order.properties.
        final String HEADER_APPROVE_STATUS = UtilProperties.getPropertyValue(orderPropertiesUrl, "order.header.payment.approved.status", "ORDER_APPROVED");
        final String ITEM_APPROVE_STATUS = UtilProperties.getPropertyValue(orderPropertiesUrl, "order.item.payment.approved.status", "ITEM_APPROVED");
        
        try {
            // set the status on the order header
            Map statusFields = UtilMisc.toMap("orderId", orderId, "statusId", HEADER_APPROVE_STATUS, "userLogin", userLogin);
            Map statusResult = dispatcher.runSync("changeOrderStatus", statusFields);                               
            if (statusResult.containsKey(ModelService.ERROR_MESSAGE)) {
                Debug.logError("Problems adjusting order header status for order #" + orderId, module);                            
            }
                        
            // set the status on the order item(s)
            Map itemStatusFields = UtilMisc.toMap("orderId", orderId, "statusId", ITEM_APPROVE_STATUS, "userLogin", userLogin);
            Map itemStatusResult = dispatcher.runSync("changeOrderItemStatus", itemStatusFields);                        
            if (itemStatusResult.containsKey(ModelService.ERROR_MESSAGE)) {
                Debug.logError("Problems adjusting order item status for order #" + orderId, module);
            }
                                                                                                                
        } catch (GenericServiceException e) {
            Debug.logError(e, "Service invocation error, status changes were not updated for order #" + orderId, module);
        }
    }
    
    private void cancelOrder(String orderId) {
        // get some payment related strings from order.properties.
        final String HEADER_DECLINE_STATUS = UtilProperties.getPropertyValue(orderPropertiesUrl, "order.header.payment.declined.status", "ORDER_REJECTED");
        final String ITEM_DECLINE_STATUS = UtilProperties.getPropertyValue(orderPropertiesUrl, "order.item.payment.declined.status", "ITEM_REJECTED");
        
        try {
            // set the status on the order header
            Map statusFields = UtilMisc.toMap("orderId", orderId, "statusId", HEADER_DECLINE_STATUS, "userLogin", userLogin);
            Map statusResult = dispatcher.runSync("changeOrderStatus", statusFields);                               
            if (statusResult.containsKey(ModelService.ERROR_MESSAGE)) {
                Debug.logError("Problems adjusting order header status for order #" + orderId, module);                            
            }
                        
            // set the status on the order item(s)
            Map itemStatusFields = UtilMisc.toMap("orderId", orderId, "statusId", ITEM_DECLINE_STATUS, "userLogin", userLogin);
            Map itemStatusResult = dispatcher.runSync("changeOrderItemStatus", itemStatusFields);                        
            if (itemStatusResult.containsKey(ModelService.ERROR_MESSAGE)) {
                Debug.logError("Problems adjusting order item status for order #" + orderId, module);
            }
                        
            // cancel the inventory reservations
            Map cancelInvFields = UtilMisc.toMap("orderId", orderId, "userLogin", userLogin);
            Map cancelInvResult = dispatcher.runSync("cancelOrderInventoryReservation", cancelInvFields);
            if (ModelService.RESPOND_ERROR.equals((String) cancelInvResult.get(ModelService.RESPONSE_MESSAGE))) {
                Debug.logError("Problems reversing inventory reservations for order #" + orderId, module);
            }                                                         
                                           
        } catch (GenericServiceException e) {
            Debug.logError(e, "Service invocation error, status/reservations were not updated for order #" + orderId, module);
        }
    }
    
    private void setPaymentPreferences(String orderId, ServletRequest request) {
        List paymentPrefs = null;
        try {
            Map paymentFields = UtilMisc.toMap("orderId", orderId, "statusId", "PAYMENT_NOT_RECEIVED");
            paymentPrefs = delegator.findByAnd("OrderPaymentPreference", paymentFields);
        } catch (GenericEntityException e) {
            Debug.logError(e, "Cannot get payment preferences for order #" + orderId, module);
        }
        if (paymentPrefs != null && paymentPrefs.size() > 0) {
            Iterator i = paymentPrefs.iterator();
            while (i.hasNext()) {
                GenericValue pref = (GenericValue) i.next();
                setPaymentPreference(pref, request);
            }
        }
    }
        
    private void setPaymentPreference(GenericValue paymentPreference, ServletRequest request) {
        String transId = request.getParameter(SelectDefs.SEL_transId);       
        String transTime = request.getParameter(SelectDefs.SEL_transTime);
        String transStatus = request.getParameter(SelectDefs.SEL_transStatus);
        String avsCode = request.getParameter("AVS");  // why is this not in SelectDefs??
        String authCode = request.getParameter(SelectDefs.SEL_authCode);
        String authAmount = request.getParameter(SelectDefs.SEL_authAmount); 
        String rawAuthMessage = request.getParameter(SelectDefs.SEL_rawAuthMessage);
        
        if (transStatus.equalsIgnoreCase("Y")) {
            paymentPreference.set("authCode", authCode);
            paymentPreference.set("statusId", "PAYMENT_RECEIVED");
        } else {
            paymentPreference.set("statusId", "PAYMENT_CANCELLED");
        }
        paymentPreference.set("avsCode", avsCode);
        paymentPreference.set("authRefNum", transId);
        paymentPreference.set("authDate", transTime);
        paymentPreference.set("authFlag", transStatus);
        paymentPreference.set("authMessage", rawAuthMessage);
        
        try {
            paymentPreference.store();
        } catch (GenericEntityException e) {
            Debug.logError(e, "Cannot set payment preference info", module);
        }                   
    }    
}
