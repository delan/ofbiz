/*
 *
 * Copyright 2001-2006 The Apache Software Foundation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.ofbiz.order.shoppingcart.product;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilHttp;
import org.ofbiz.entity.GenericDelegator;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.order.shoppingcart.ShoppingCart;
import org.ofbiz.order.shoppingcart.ShoppingCartEvents;
import org.ofbiz.order.shoppingcart.WebShoppingCart;
import org.ofbiz.product.store.ProductStoreWorker;

/**
 * ProductStoreWorker - Worker class for store related functionality
 */
public class ProductStoreCartAwareEvents {

    public static final String module = ProductStoreCartAwareEvents.class.getName();

    public static String setSessionProductStore(HttpServletRequest request, HttpServletResponse response) {
        Map parameters = UtilHttp.getParameterMap(request);
        String productStoreId = (String) parameters.get("productStoreId");
        
        try {
            ProductStoreCartAwareEvents.setSessionProductStore(productStoreId, request);
        } catch (Exception e) {
            String errMsg = "Problem setting new store: " + e.toString();
            Debug.logError(e, errMsg, module);
            request.setAttribute("_ERROR_MESSAGE_", errMsg);
            return "error";
        }
        
        return "success";
    }

    public static void setSessionProductStore(String productStoreId, HttpServletRequest request) {
        if (productStoreId == null) {
            return;
        }
        
        HttpSession session = request.getSession();
        String oldProductStoreId = (String) session.getAttribute("productStoreId");
        
        if (productStoreId.equals(oldProductStoreId)) {
            // great, nothing to do, bye bye
            return;
        }
        
        GenericDelegator delegator = (GenericDelegator) request.getAttribute("delegator");
        
        // get the ProductStore record, make sure it's valid
        GenericValue productStore = ProductStoreWorker.getProductStore(productStoreId, delegator);
        if (productStore == null) {
            throw new IllegalArgumentException("Cannot set session ProductStore, passed productStoreId [" + productStoreId + "] is not valid/not found.");
        }
        
        // set the productStoreId in the session (we know is different by this point)
        session.setAttribute("productStoreId", productStoreId);
        
        // have set the new store, now need to clear out the current catalog so the default for the new store will be used
        session.removeAttribute("CURRENT_CATALOG_ID");
        
        // if there is no locale or currencyUom in the session, set the defaults from the store, but don't do so through the CommonEvents methods setSessionLocale and setSessionCurrencyUom because we don't want these to be put on the UserLogin entity
        // note that this is different from the normal default setting process because these will now override the settings on the UserLogin; this is desired when changing stores and the user should be given a chance to change their personal settings after the store change
        UtilHttp.setCurrencyUomIfNone(session, productStore.getString("defaultCurrencyUomId"));
        UtilHttp.setLocaleIfNone(session, productStore.getString("defaultLocaleString"));
        
        // if a shoppingCart exists in the session and the productStoreId on it is different, 
        // - leave the old cart as-is (don't clear it, want to leave the auto-save list intact)
        // - but create a new cart (which will load from auto-save list if applicable) and put it in the session
        
        ShoppingCart cart = ShoppingCartEvents.getCartObject(request);
        // this should always be different given the previous session productStoreId check, but just in case...
        if (!productStoreId.equals(cart.getProductStoreId())) {
            // this is a really simple operation now that we have prepared all of the data, as done above in this method
            cart = new WebShoppingCart(request);
            session.setAttribute("shoppingCart", cart);
        }
    }
}
