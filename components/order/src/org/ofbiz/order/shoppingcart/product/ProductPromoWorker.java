/*
 * $Id: ProductPromoWorker.java,v 1.5 2003/11/12 23:45:27 jonesde Exp $
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
package org.ofbiz.order.shoppingcart.product;

import java.util.*;
import java.sql.Timestamp;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.entity.GenericDelegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.order.shoppingcart.CartItemModifyException;
import org.ofbiz.order.shoppingcart.ShoppingCart;
import org.ofbiz.order.shoppingcart.ShoppingCartEvents;
import org.ofbiz.order.shoppingcart.ShoppingCartItem;
import org.ofbiz.product.store.ProductStoreWorker;
import org.ofbiz.product.product.ProductSearch;
import org.ofbiz.service.LocalDispatcher;

/**
 * ProductPromoWorker - Worker class for catalog/product promotion related functionality
 *
 * @author     <a href="mailto:jonesde@ofbiz.org">David E. Jones</a>
 * @author     <a href="mailto:jaz@ofbiz.org">Andy Zeneski</a>
 * @version    $Revision: 1.5 $
 * @since      2.0
 */
public class ProductPromoWorker {

    public static final String module = ProductPromoWorker.class.getName();

    public static List getStoreProductPromos(GenericDelegator delegator, ServletRequest request) {
        List productPromos = new LinkedList();
        Timestamp nowTimestamp = UtilDateTime.nowTimestamp();

        try {
            GenericValue productStore = ProductStoreWorker.getProductStore(request);

            if (productStore != null) {
                String productStoreId = productStore.getString("productStoreId");
                Iterator productStorePromoAppls = UtilMisc.toIterator(EntityUtil.filterByDate(productStore.getRelatedCache("ProductStorePromoAppl", UtilMisc.toMap("productStoreId", productStoreId), UtilMisc.toList("sequenceNum")), true));

                while (productStorePromoAppls != null && productStorePromoAppls.hasNext()) {
                    GenericValue productStorePromoAppl = (GenericValue) productStorePromoAppls.next();
                    GenericValue productPromo = productStorePromoAppl.getRelatedOneCache("ProductPromo");
                    List productPromoRules = productPromo.getRelatedCache("ProductPromoRule", null, null);

                    // get the ShoppingCart out of the session.
                    HttpServletRequest req = null;
                    ShoppingCart cart = null;

                    try {
                        req = (HttpServletRequest) request;
                        cart = ShoppingCartEvents.getCartObject(req);
                    } catch (ClassCastException cce) {
                        Debug.logInfo("Not a HttpServletRequest, no shopping cart found.", module);
                    }

                    boolean condResult = true;

                    if (productPromoRules != null) {
                        Iterator promoRulesItr = productPromoRules.iterator();

                        while (condResult && promoRulesItr != null && promoRulesItr.hasNext()) {
                            GenericValue promoRule = (GenericValue) promoRulesItr.next();
                            Iterator productPromoConds = UtilMisc.toIterator(promoRule.getRelatedCache("ProductPromoCond", null, UtilMisc.toList("productPromoCondSeqId")));

                            while (condResult && productPromoConds != null && productPromoConds.hasNext()) {
                                GenericValue productPromoCond = (GenericValue) productPromoConds.next();

                                // evaluate the party related conditions; so we don't show the promo if it doesn't apply.
                                if ("PPIP_PARTY_ID".equals(productPromoCond.getString("inputParamEnumId"))) {
                                    condResult = checkCondition(productPromoCond, cart, delegator, nowTimestamp);
                                } else if ("PRIP_PARTY_GRP_MEM".equals(productPromoCond.getString("inputParamEnumId"))) {
                                    condResult = checkCondition(productPromoCond, cart, delegator, nowTimestamp);
                                } else if ("PRIP_PARTY_CLASS".equals(productPromoCond.getString("inputParamEnumId"))) {
                                    condResult = checkCondition(productPromoCond, cart, delegator, nowTimestamp);
                                } else if ("PPIP_ROLE_TYPE".equals(productPromoCond.getString("inputParamEnumId"))) {
                                    condResult = checkCondition(productPromoCond, cart, delegator, nowTimestamp);
                                }
                            }
                        }
                        if (!condResult) productPromo = null;
                    }
                    if (productPromo != null) productPromos.add(productPromo);
                }
            }
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
        }
        return productPromos;
    }

    public static void doPromotions(ShoppingCart cart, GenericDelegator delegator, LocalDispatcher dispatcher) {
        Timestamp nowTimestamp = UtilDateTime.nowTimestamp();

        // start out by clearing all existing promotions, then we can just add all that apply
        clearAllPromotions(cart);

        // this is our safety net; we should never need to loop through the rules more than a certain number of times, this is that number and may have to be changed for insanely large promo sets...
        int maxIterations = 1000;

        String productStoreId = cart.getProductStoreId();
        GenericValue productStore = null;

        try {
            productStore = delegator.findByPrimaryKeyCache("ProductStore", UtilMisc.toMap("productStoreId", productStoreId));
        } catch (GenericEntityException e) {
            Debug.logError(e, "Error looking up store with id " + productStoreId, module);
        }
        if (productStore == null) {
            Debug.logWarning("No store found with id " + productStoreId + ", not doing promotions", module);
            return;
        }

        // there will be a ton of db access, so just do a big catch entity exception block
        try {
            // loop through promotions and get a list of all of the rules...
            Collection productStorePromoApplsCol = productStore.getRelatedCache("ProductStorePromoAppl", null, UtilMisc.toList("sequenceNum"));
            productStorePromoApplsCol = EntityUtil.filterByDate((List) productStorePromoApplsCol, true);

            if (productStorePromoApplsCol == null || productStorePromoApplsCol.size() == 0) {
                if (Debug.infoOn()) Debug.logInfo("Not doing promotions, none applied to store with ID " + productStoreId, module);
            }

            List allPromoRules = new LinkedList();
            Iterator prodCatalogPromoAppls = UtilMisc.toIterator(productStorePromoApplsCol);

            while (prodCatalogPromoAppls != null && prodCatalogPromoAppls.hasNext()) {
                GenericValue prodCatalogPromoAppl = (GenericValue) prodCatalogPromoAppls.next();
                GenericValue productPromo = prodCatalogPromoAppl.getRelatedOneCache("ProductPromo");

                Collection productPromoRules = productPromo.getRelatedCache("ProductPromoRule", null, null);
                if (productPromoRules != null) {
                    allPromoRules.addAll(productPromoRules);
                }
            }

            // This isn't necessary, just removing run rules from list: Set firedRules = new HashSet();

            // part of the safety net to avoid infinite iteration
            int numberOfIterations = 0;
            // repeat until no more rules to run: either all rules are run, or no changes to the cart in a loop
            boolean cartChanged = true;

            while (cartChanged) {
                cartChanged = false;

                numberOfIterations++;
                if (numberOfIterations > maxIterations) {
                    Debug.logError("ERROR: While calculating promotions the promotion rules where run more than " + maxIterations + " times, so the calculation has been ended. This should generally never happen unless you have bad rule definitions (or LOTS of promos/rules).", module);
                    break;
                }

                Iterator allPromoRulesIter = allPromoRules.iterator();

                while (allPromoRulesIter != null && allPromoRulesIter.hasNext()) {
                    GenericValue productPromoRule = (GenericValue) allPromoRulesIter.next();

                    // if apply then performActions when no conditions are false, so default to true
                    boolean performActions = true;

                    // loop through conditions for rule, if any false, set allConditionsTrue to false
                    Iterator productPromoConds = UtilMisc.toIterator(productPromoRule.getRelatedCache("ProductPromoCond", null, UtilMisc.toList("productPromoCondSeqId")));
                    while (productPromoConds != null && productPromoConds.hasNext()) {
                        GenericValue productPromoCond = (GenericValue) productPromoConds.next();

                        boolean condResult = checkCondition(productPromoCond, cart, delegator, nowTimestamp);

                        // any false condition will cause it to NOT perform the action
                        if (condResult == false) {
                            performActions = false;
                            break;
                        }
                    }

                    if (performActions) {
                        // perform all actions, either apply or unapply
                        if (Debug.verboseOn()) Debug.logVerbose("Performing" + " actions for rule " + productPromoRule, module);

                        /* This isn't necessary, just removing run rules from list:
                         //rule done, add to list so it won't get done again
                         firedRules.add(productPromoRulePK);*/

                        // rule done, remove from list so it won't get done again
                        allPromoRulesIter.remove();

                        Iterator productPromoActions = UtilMisc.toIterator(productPromoRule.getRelatedCache("ProductPromoAction", null, UtilMisc.toList("productPromoActionSeqId")));
                        while (productPromoActions != null && productPromoActions.hasNext()) {
                            GenericValue productPromoAction = (GenericValue) productPromoActions.next();

                            // Debug.logInfo("Doing action: " + productPromoAction, module);

                            try {
                                boolean actionChangedCart = performAction(productPromoAction, cart, delegator, dispatcher);

                                // if cartChanged is already true then don't set it again: implements OR logic (ie if ANY actions change content, redo loop)
                                if (!cartChanged) {
                                    cartChanged = actionChangedCart;
                                }
                            } catch (CartItemModifyException e) {
                                Debug.logError("Error modifying the cart in perform promotion action: " + e.toString(), module);
                            }
                        }
                    }
                }
            }
        } catch (NumberFormatException e) {
            Debug.logError(e, "Number not formatted correctly in promotion rules, not completed...", module);
        } catch (GenericEntityException e) {
            Debug.logError(e, "Error looking up promotion data while doing promotions", module);
        }
    }

    protected static boolean checkCondition(GenericValue productPromoCond, ShoppingCart cart, GenericDelegator delegator, Timestamp nowTimestamp) throws GenericEntityException {
        GenericValue userLogin = null;
        String partyId = null;

        if (cart != null) userLogin = cart.getUserLogin();
        if (cart != null && userLogin == null) userLogin = cart.getAutoUserLogin();
        if (userLogin != null && userLogin.get("partyId") != null)
            partyId = userLogin.getString("partyId");

        if (Debug.verboseOn()) Debug.logVerbose("Checking promotion condition: " + productPromoCond, module);
        int compare = 0;

        if ("PPIP_PRODUCT_ID_IC".equals(productPromoCond.getString("inputParamEnumId"))) {
            String candidateProductId = productPromoCond.getString("condValue");

            if (candidateProductId == null) {
                // if null, then it's not in the cart
                compare = 1;
            } else {
                // Debug.logInfo("Testing to see if productId \"" + candidateProductId + "\" is in the cart", module);
                List productCartItems = cart.findAllCartItems(candidateProductId);

                // don't count promotion items in this count...
                Iterator pciIter = productCartItems.iterator();
                while (pciIter.hasNext()) {
                    ShoppingCartItem productCartItem = (ShoppingCartItem) pciIter.next();
                    if (productCartItem.getIsPromo()) pciIter.remove();
                }

                if (productCartItems.size() > 0) {
                    //Debug.logError("Item with productId \"" + candidateProductId + "\" IS in the cart", module);
                    compare = 0;
                } else {
                    //Debug.logError("Item with productId \"" + candidateProductId + "\" IS NOT in the cart", module);
                    compare = 1;
                }
            }
        } else if ("PPIP_CATEGORY_ID_IC".equals(productPromoCond.getString("inputParamEnumId"))) {
            String productCategoryId = productPromoCond.getString("condValue");
            Set productIds = new HashSet();

            Iterator cartItemIter = cart.iterator();
            while (cartItemIter.hasNext()) {
                ShoppingCartItem cartItem = (ShoppingCartItem) cartItemIter.next();
                if (!cartItem.getIsPromo()) {
                    productIds.add(cartItem.getProductId());
                }
            }

            compare = 1;
            // NOTE: this technique is efficient for a smaller number of items in the cart, if there are a lot of lines
            //in the cart then a non-cached query with a set of productIds using the IN operator would be better
            Iterator productIdIter = productIds.iterator();
            while (productIdIter.hasNext()) {
                String productId = (String) productIdIter.next();

                // if a ProductCategoryMember exists for this productId and the specified productCategoryId
                List productCategoryMembers = delegator.findByAndCache("ProductCategoryMember", UtilMisc.toMap("productId", productId, "productCategoryId", productCategoryId));
                // and from/thru date within range
                productCategoryMembers = EntityUtil.filterByDate(productCategoryMembers, nowTimestamp);
                if (productCategoryMembers != null && productCategoryMembers.size() > 0) {
                    // if any product is in category, set true and break
                    // then 0 (equals), otherwise 1 (not equals)
                    compare = 0;
                    break;
                }
            }
        } else if ("PPIP_PARTY_ID".equals(productPromoCond.getString("inputParamEnumId"))) {
            if (partyId != null) {
                compare = partyId.compareTo(productPromoCond.getString("condValue"));
            } else {
                compare = 1;
            }

            /* These aren't supported yet, ie TODO
             } else if ("PRIP_PARTY_GRP_MEM".equals(productPriceCond.getString("inputParamEnumId"))) {
             } else if ("PRIP_PARTY_CLASS".equals(productPriceCond.getString("inputParamEnumId"))) {
             */
        } else if ("PPIP_ROLE_TYPE".equals(productPromoCond.getString("inputParamEnumId"))) {
            if (partyId != null) {
                // if a PartyRole exists for this partyId and the specified roleTypeId
                GenericValue partyRole = delegator.findByPrimaryKeyCache("PartyRole",
                        UtilMisc.toMap("partyId", partyId, "roleTypeId", productPromoCond.getString("condValue")));

                // then 0 (equals), otherwise 1 (not equals)
                if (partyRole != null) {
                    compare = 0;
                } else {
                    compare = 1;
                }
            } else {
                compare = 1;
            }
        } else if ("PPIP_ORDER_TOTAL".equals(productPromoCond.getString("inputParamEnumId"))) {
            Double orderSubTotal = new Double(cart.getSubTotal());

            if (Debug.verboseOn()) Debug.logVerbose("Doing order total compare: orderSubTotal=" + orderSubTotal, module);
            compare = orderSubTotal.compareTo(Double.valueOf(productPromoCond.getString("condValue")));
        } else {
            Debug.logWarning("An un-supported productPromoCond input parameter (lhs) was used: " + productPromoCond.getString("inputParamEnumId") + ", returning false, ie check failed", module);
            return false;
        }

        if (Debug.verboseOn()) Debug.logVerbose("Condition compare done, compare=" + compare, module);

        if ("PPC_EQ".equals(productPromoCond.getString("operatorEnumId"))) {
            if (compare == 0) return true;
        } else if ("PPC_NEQ".equals(productPromoCond.getString("operatorEnumId"))) {
            if (compare != 0) return true;
        } else if ("PPC_LT".equals(productPromoCond.getString("operatorEnumId"))) {
            if (compare < 0) return true;
        } else if ("PPC_LTE".equals(productPromoCond.getString("operatorEnumId"))) {
            if (compare <= 0) return true;
        } else if ("PPC_GT".equals(productPromoCond.getString("operatorEnumId"))) {
            if (compare > 0) return true;
        } else if ("PPC_GTE".equals(productPromoCond.getString("operatorEnumId"))) {
            if (compare >= 0) return true;
        } else {
            Debug.logWarning("An un-supported productPromoCond condition was used: " + productPromoCond.getString("operatorEnumId") + ", returning false, ie check failed", module);
            return false;
        }
        return false;
    }

    /** returns true if the cart was changed and rules need to be re-evaluted */
    protected static boolean performAction(GenericValue productPromoAction, ShoppingCart cart, GenericDelegator delegator, LocalDispatcher dispatcher) throws GenericEntityException, CartItemModifyException {
        if ("PROMO_GWP".equals(productPromoAction.getString("productPromoActionEnumId"))) {
            Integer itemLoc = findPromoItem(productPromoAction, cart);

            if (itemLoc != null) {
                if (Debug.verboseOn()) Debug.logVerbose("Not adding promo item, already there; action: " + productPromoAction, module);
                return false;
            }

            GenericValue product = delegator.findByPrimaryKeyCache("Product", UtilMisc.toMap("productId", productPromoAction.get("productId")));
            double quantity = productPromoAction.get("quantity") == null ? 0.0 : productPromoAction.getDouble("quantity").doubleValue();

            // pass null for cartLocation to add to end of cart, pass false for doPromotions to avoid infinite recursion
            ShoppingCartItem gwpItem = null;
            try {
                // TODO: where should we REALLY get the prodCatalogId?
                String prodCatalogId = null;
                gwpItem = ShoppingCartItem.makeItem(null, product, quantity, null, null, prodCatalogId, dispatcher, cart, false);
            } catch (CartItemModifyException e) {
                int gwpItemIndex = cart.getItemIndex(gwpItem);
                cart.removeCartItem(gwpItemIndex, dispatcher);
                throw e;
            }

            double discountAmount = quantity * gwpItem.getBasePrice();
            GenericValue orderAdjustment = delegator.makeValue("OrderAdjustment",
                    UtilMisc.toMap("orderAdjustmentTypeId", "PROMOTION_ADJUSTMENT", "amount", new Double(-discountAmount),
                        "productPromoId", productPromoAction.get("productPromoId"),
                        "productPromoRuleId", productPromoAction.get("productPromoRuleId"),
                        "productPromoActionSeqId", productPromoAction.get("productPromoActionSeqId")));

            // if an orderAdjustmentTypeId was included, override the default
            if (UtilValidate.isNotEmpty(productPromoAction.getString("orderAdjustmentTypeId"))) {
                orderAdjustment.set("orderAdjustmentTypeId", productPromoAction.get("orderAdjustmentTypeId"));
            }

            // set promo after create; note that to setQuantity we must clear this flag, setQuantity, then re-set the flag
            gwpItem.setIsPromo(true);
            gwpItem.addAdjustment(orderAdjustment);
            if (Debug.verboseOn()) Debug.logVerbose("gwpItem adjustments: " + gwpItem.getAdjustments(), module);

            // ProductPromoWorker.doPromotions(prodCatalogId, cart, gwpItem, 0, delegator, dispatcher);
            return true;
        } else if ("PROMO_FREE_SHIPPING".equals(productPromoAction.getString("productPromoActionEnumId"))) {
            // this may look a bit funny: on each pass all rules that do free shipping will set their own rule id for it,
            // and on unapply if the promo and rule ids are the same then it will clear it; essentially on any pass
            // through the promos and rules if any free shipping should be there, it will be there
            cart.addFreeShippingProductPromoAction(productPromoAction);
            // don't consider this as a cart change...
            return false;
        } else if ("PROMO_ITEM_PERCENT".equals(productPromoAction.getString("productPromoActionEnumId"))) {
            // TODO: re-implement this: return doItemPromoAction(productPromoAction, cartItem, "percentage", delegator);
            return false;
        } else if ("PROMO_ITEM_AMOUNT".equals(productPromoAction.getString("productPromoActionEnumId"))) {
            // TODO: re-implement this: return doItemPromoAction(productPromoAction, cartItem, "amount", delegator);
            return false;
        } else if ("PROMO_ORDER_PERCENT".equals(productPromoAction.getString("productPromoActionEnumId"))) {
            return doOrderPromoAction(productPromoAction, cart, "percentage", delegator);
        } else if ("PROMO_ORDER_AMOUNT".equals(productPromoAction.getString("productPromoActionEnumId"))) {
            return doOrderPromoAction(productPromoAction, cart, "amount", delegator);
        } else {
            Debug.logError("An un-supported productPromoActionType was used: " + productPromoAction.getString("productPromoActionEnumId") + ", not performing any action", module);
            return false;
        }
    }

    protected static Integer findPromoItem(GenericValue productPromoAction, ShoppingCart cart) {
        List cartItems = cart.items();

        for (int i = 0; i < cartItems.size(); i++) {
            ShoppingCartItem checkItem = (ShoppingCartItem) cartItems.get(i);

            if (checkItem.getIsPromo() && checkItem.getProductId().equals(productPromoAction.get("productId"))) {
                // found a promo item with the productId, see if it has a matching adjustment on it
                Iterator checkOrderAdjustments = UtilMisc.toIterator(checkItem.getAdjustments());

                while (checkOrderAdjustments != null && checkOrderAdjustments.hasNext()) {
                    GenericValue checkOrderAdjustment = (GenericValue) checkOrderAdjustments.next();

                    if (productPromoAction.getString("productPromoId").equals(checkOrderAdjustment.get("productPromoId")) &&
                        productPromoAction.getString("productPromoRuleId").equals(checkOrderAdjustment.get("productPromoRuleId")) &&
                        productPromoAction.getString("productPromoActionSeqId").equals(checkOrderAdjustment.get("productPromoActionSeqId"))) {
                        return new Integer(i);
                    }
                }
            }
        }
        return null;
    }

    protected static void clearAllPromotions(ShoppingCart cart) {
        // remove cart adjustments from promo actions
        List cartAdjustments = cart.getAdjustments();
        if (cartAdjustments != null) {
            Iterator cartAdjustmentIter = cartAdjustments.iterator();
            while (cartAdjustmentIter.hasNext()) {
                GenericValue checkOrderAdjustment = (GenericValue) cartAdjustmentIter.next();
                if (UtilValidate.isNotEmpty(checkOrderAdjustment.getString("productPromoId")) &&
                        UtilValidate.isNotEmpty(checkOrderAdjustment.getString("productPromoRuleId")) &&
                        UtilValidate.isNotEmpty(checkOrderAdjustment.getString("productPromoActionSeqId"))) {
                    cartAdjustmentIter.remove();
                }
            }
        }

        // remove cart lines that are promos (ie GWPs) and cart line adjustments from promo actions
        Iterator cartItemIter = cart.items().iterator();
        while (cartItemIter.hasNext()) {
            ShoppingCartItem checkItem = (ShoppingCartItem) cartItemIter.next();
            if (checkItem.getIsPromo()) {
                cartItemIter.remove();
            } else {
                // found a promo item with the productId, see if it has a matching adjustment on it
                Iterator checkOrderAdjustments = UtilMisc.toIterator(checkItem.getAdjustments());
                while (checkOrderAdjustments != null && checkOrderAdjustments.hasNext()) {
                    GenericValue checkOrderAdjustment = (GenericValue) checkOrderAdjustments.next();
                    if (UtilValidate.isNotEmpty(checkOrderAdjustment.getString("productPromoId")) &&
                            UtilValidate.isNotEmpty(checkOrderAdjustment.getString("productPromoRuleId")) &&
                            UtilValidate.isNotEmpty(checkOrderAdjustment.getString("productPromoActionSeqId"))) {
                        checkOrderAdjustments.remove();
                    }
                }
            }
        }

        // remove all free shipping promo actions
        cart.removeAllFreeShippingProductPromoActions();

        // clear promo uses & reset promo code uses
        cart.clearProductPromoUses();
        cart.resetProductPromoCodeUses();
    }

    protected static boolean doItemPromoAction(GenericValue productPromoAction, ShoppingCartItem cartItem, String quantityField, GenericDelegator delegator) {
        Integer adjLoc = findAdjustment(productPromoAction, cartItem.getAdjustments());

        if (adjLoc != null) {
            if (Debug.verboseOn()) Debug.logVerbose("Not adding promo adjustment, already there; action: " + productPromoAction, module);
            return false;
        }

        double quantity = productPromoAction.get("quantity") == null ? 0.0 : productPromoAction.getDouble("quantity").doubleValue();
        GenericValue itemAdjustment = delegator.makeValue("OrderAdjustment",
                UtilMisc.toMap("orderAdjustmentTypeId", "PROMOTION_ADJUSTMENT", quantityField, new Double(quantity),
                    "productPromoId", productPromoAction.get("productPromoId"),
                    "productPromoRuleId", productPromoAction.get("productPromoRuleId"),
                    "productPromoActionSeqId", productPromoAction.get("productPromoActionSeqId")));

        // if an orderAdjustmentTypeId was included, override the default
        if (UtilValidate.isNotEmpty(productPromoAction.getString("orderAdjustmentTypeId"))) {
            itemAdjustment.set("orderAdjustmentTypeId", productPromoAction.get("orderAdjustmentTypeId"));
        }

        cartItem.addAdjustment(itemAdjustment);
        return true;
    }

    public static boolean doOrderPromoAction(GenericValue productPromoAction, ShoppingCart cart, String quantityField, GenericDelegator delegator) {
        // Debug.logInfo("Starting doOrderPromoAction: productPromoAction=" + productPromoAction, module);
        Integer adjLoc = findAdjustment(productPromoAction, cart.getAdjustments());

        if (adjLoc != null) {
            if (Debug.infoOn()) Debug.logInfo("Not adding promo adjustment, already there; action: " + productPromoAction, module);
            return false;
        }

        double quantity = productPromoAction.get("quantity") == null ? 0.0 : productPromoAction.getDouble("quantity").doubleValue();
        GenericValue orderAdjustment = delegator.makeValue("OrderAdjustment",
                UtilMisc.toMap("orderAdjustmentTypeId", "PROMOTION_ADJUSTMENT", quantityField, new Double(quantity),
                    "productPromoId", productPromoAction.get("productPromoId"),
                    "productPromoRuleId", productPromoAction.get("productPromoRuleId"),
                    "productPromoActionSeqId", productPromoAction.get("productPromoActionSeqId")));

        // if an orderAdjustmentTypeId was included, override the default
        if (UtilValidate.isNotEmpty(productPromoAction.getString("orderAdjustmentTypeId"))) {
            orderAdjustment.set("orderAdjustmentTypeId", productPromoAction.get("orderAdjustmentTypeId"));
        }

        cart.addAdjustment(orderAdjustment);
        return true;
    }

    protected static Integer findAdjustment(GenericValue productPromoAction, List adjustments) {
        for (int i = 0; i < adjustments.size(); i++) {
            GenericValue checkOrderAdjustment = (GenericValue) adjustments.get(i);

            if (productPromoAction.getString("productPromoId").equals(checkOrderAdjustment.get("productPromoId")) &&
                productPromoAction.getString("productPromoRuleId").equals(checkOrderAdjustment.get("productPromoRuleId")) &&
                productPromoAction.getString("productPromoActionSeqId").equals(checkOrderAdjustment.get("productPromoActionSeqId"))) {
                return new Integer(i);
            }
        }
        return null;
    }

    protected static Set getPromoRuleCondProductIds(GenericValue productPromoCond, GenericDelegator delegator, Timestamp nowTimestamp) throws GenericEntityException {
        // get a cached list for the whole promo and filter it as needed, this for better efficiency in caching
        List productPromoCategoriesAll = delegator.findByAndCache("ProductPromoCategory", UtilMisc.toMap("productPromoId", productPromoCond.get("productPromoId")));
        List productPromoCategories = EntityUtil.filterByAnd(productPromoCategoriesAll, UtilMisc.toMap("productPromoRuleId", "_NA_", "productPromoCondId", "_NA_"));
        productPromoCategories.addAll(EntityUtil.filterByAnd(productPromoCategoriesAll, UtilMisc.toMap("productPromoRuleId", productPromoCond.get("productPromoRuleId"), "productPromoCondId", productPromoCond.get("productPromoCondId"))));

        List productPromoProductsAll = delegator.findByAndCache("ProductPromoProduct", UtilMisc.toMap("productPromoId", productPromoCond.get("productPromoId")));
        List productPromoProducts = EntityUtil.filterByAnd(productPromoProductsAll, UtilMisc.toMap("productPromoRuleId", "_NA_", "productPromoCondId", "_NA_"));
        productPromoProducts.addAll(EntityUtil.filterByAnd(productPromoProductsAll, UtilMisc.toMap("productPromoRuleId", productPromoCond.get("productPromoRuleId"), "productPromoCondId", productPromoCond.get("productPromoCondId"))));

        Set productIds = new HashSet();

        // do the includes
        handleProductPromoCategories(productIds, productPromoCategories, "PPPA_INCLUDE", delegator, nowTimestamp);
        handleProductPromoProducts(productIds, productPromoProducts, "PPPA_INCLUDE");

        // do the excludes
        handleProductPromoCategories(productIds, productPromoCategories, "PPPA_EXCLUDE", delegator, nowTimestamp);
        handleProductPromoProducts(productIds, productPromoProducts, "PPPA_EXCLUDE");

        // do the always includes
        handleProductPromoCategories(productIds, productPromoCategories, "PPPA_ALWAYS", delegator, nowTimestamp);
        handleProductPromoProducts(productIds, productPromoProducts, "PPPA_ALWAYS");

        return productIds;
    }

    protected static void handleProductPromoCategories(Set productIds, List productPromoCategories, String productPromoApplEnumId, GenericDelegator delegator, Timestamp nowTimestamp) throws GenericEntityException {
        boolean include = !"PPPA_EXCLUDE".equals(productPromoApplEnumId);
        Set productCategoryIds = new HashSet();
        Iterator productPromoCategoryIter = productPromoCategories.iterator();
        while (productPromoCategoryIter.hasNext()) {
            GenericValue productPromoCategory = (GenericValue) productPromoCategoryIter.next();
            if (productPromoApplEnumId.equals(productPromoCategory.getString("productPromoApplEnumId"))) {
                if ("Y".equals(productPromoCategory.getString("includeSubCategories"))) {
                    ProductSearch.getAllSubCategoryIds(productPromoCategory.getString("productCategoryId"), productCategoryIds, delegator, nowTimestamp);
                } else {
                    productCategoryIds.add(productPromoCategory.getString("productCategoryId"));
                }
            }
        }

        Iterator productCategoryIdIter = productCategoryIds.iterator();
        while (productCategoryIdIter.hasNext()) {
            String productCategoryId = (String) productCategoryIdIter.next();
            // get all product category memebers, filter by date
            List productCategoryMembers = delegator.findByAndCache("ProductCategoryMember", UtilMisc.toMap("productCategoryId", productCategoryId));
            productCategoryMembers = EntityUtil.filterByDate(productCategoryMembers, nowTimestamp);
            Iterator productCategoryMemberIter = productCategoryMembers.iterator();
            while (productCategoryMemberIter.hasNext()) {
                GenericValue productCategoryMember = (GenericValue) productCategoryMemberIter.next();
                String productId = productCategoryMember.getString("productId");
                if (include) {
                    productIds.add(productId);
                } else {
                    productIds.remove(productId);
                }
            }
        }
    }

    protected static void handleProductPromoProducts(Set productIds, List productPromoProducts, String productPromoApplEnumId) {
        boolean include = !"PPPA_EXCLUDE".equals(productPromoApplEnumId);
        Iterator productPromoProductIter = productPromoProducts.iterator();
        while (productPromoProductIter.hasNext()) {
            GenericValue productPromoProduct = (GenericValue) productPromoProductIter.next();
            if (productPromoApplEnumId.equals(productPromoProduct.getString("productPromoApplEnumId"))) {
                String productId = productPromoProduct.getString("productId");
                if (include) {
                    productIds.add(productId);
                } else {
                    productIds.remove(productId);
                }
            }
        }
    }
}
