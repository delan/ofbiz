/*
 * $Id$
 *
 *  Copyright (c) 2003 The Open For Business Project - www.ofbiz.org
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
package org.ofbiz.commonapp.order.shoppinglist;

import java.util.*;
import javax.servlet.http.*;

import org.ofbiz.core.entity.*;
import org.ofbiz.core.service.*;
import org.ofbiz.core.util.*;

import org.ofbiz.commonapp.order.shoppingcart.*;

/**
 * Shopping cart events.
 *
 * @author     <a href="mailto:jaz@ofbiz.org">Andy Zeneski</a>
 * @version    $Revision$
 * @since      2.2
 */
public class ShoppingListEvents {
    
    public static final String module = ShoppingListEvents.class.getName();
    
    public static String addBulkFromCart(HttpServletRequest request, HttpServletResponse response) {
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        ShoppingCart cart = (ShoppingCart) request.getSession().getAttribute(SiteDefs.SHOPPING_CART);
        GenericValue userLogin = (GenericValue) request.getSession().getAttribute("userLogin");        
        
        String shoppingListId = request.getParameter("shoppingListId");
        String selectedCartItems[] = request.getParameterValues("selectedItem");
        
        if (selectedCartItems == null || selectedCartItems.length == 0) {
            request.setAttribute(SiteDefs.ERROR_MESSAGE, "<li>Please select item(s) to add to the shopping list.");
            return "error";
        }
                
        if (shoppingListId == null || shoppingListId.equals("")) {
            // create a new shopping list
            Map newListResult = null;
            try {
                newListResult = dispatcher.runSync("createShoppingList", UtilMisc.toMap("userLogin", userLogin));    
            } catch (GenericServiceException e) {
                Debug.logError(e, "Problems creating new ShoppingList", module);
                request.setAttribute(SiteDefs.ERROR_MESSAGE, "<li>Cannot create new shopping list.");
                return "error";
            }
            
            // get the new list id
            if (newListResult != null) {
                shoppingListId = (String) newListResult.get("shoppingListId");
            }
            
            // if no list was created throw an error
            if (shoppingListId == null || shoppingListId.equals("")) {            
                request.setAttribute(SiteDefs.ERROR_MESSAGE, "<li>shoppingListId is a required parameter.");
                return "error";
            }
        }
                
        for (int i = 0; i < selectedCartItems.length; i++) {
            Integer cartIdInt = null;
            try {            
                cartIdInt = new Integer(selectedCartItems[i]);
            } catch (Exception e) {
                Debug.logWarning(e, "Illegal character in selectedItem field", module);
            }
            if (cartIdInt != null) {            
                ShoppingCartItem item = cart.findCartItem(cartIdInt.intValue());
                Map serviceResult = null;
                try {
                    Map ctx = UtilMisc.toMap("userLogin", userLogin, "shoppingListId", shoppingListId, "productId", item.getProductId(), "quantity", new Double(item.getQuantity()));
                    serviceResult = dispatcher.runSync("createShoppingListItem", ctx);                    
                } catch (GenericServiceException e) {
                    Debug.logError(e, "Problems creating ShoppingList item entity", module);
                    request.setAttribute(SiteDefs.ERROR_MESSAGE, "Error adding item to shopping list");
                    return "error";
                }
            }           
        }
        
        // return the shoppinglist id 
        request.setAttribute("shoppingListId", shoppingListId);                
        return "success";
    }

}
