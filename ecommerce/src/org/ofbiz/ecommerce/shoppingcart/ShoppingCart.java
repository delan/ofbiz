/*
 * $Id$
 */

package org.ofbiz.ecommerce.shoppingcart;

import java.text.*;
import java.util.*;

import org.ofbiz.core.entity.*;
import org.ofbiz.core.service.*;
import org.ofbiz.core.util.*;
import org.ofbiz.commonapp.order.order.OrderReadHelper;

/**
 * <p><b>Title:</b> ShoppingCart.java
 * <p><b>Description:</b> Shopping Cart Object.
 * <p>Copyright (c) 2001 The Open For Business Project and repected authors.
 * <p>Permission is hereby granted, free of charge, to any person obtaining a
 *  copy of this software and associated documentation files (the "Software"),
 *  to deal in the Software without restriction, including without limitation
 *  the rights to use, copy, modify, merge, publish, distribute, sublicense,
 *  and/or sell copies of the Software, and to permit persons to whom the
 *  Software is furnished to do so, subject to the following conditions:
 *
 * <p>The above copyright notice and this permission notice shall be included
 *  in all copies or substantial portions of the Software.
 *
 * <p>THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 *  OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 *  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 *  IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 *  CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT
 *  OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR
 *  THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * @author     <a href="mailto:jaz@zsolv.com">Andy Zeneski</a>
 * @author     <a href="mailto:cnelson@einnovation.com">Chris Nelson</a>
 * @author     <a href="mailto:jonesde@ofbiz.org">David E. Jones</a>
 * @version    1.0
 * @created    August 4, 2001
 */
public class ShoppingCart implements java.io.Serializable {

    //either paymentMethodId or poNumber must be null (use one or the other)
    private List paymentMethodIds = new LinkedList();
    private List paymentMethodTypeIds = new LinkedList();
    private String poNumber = null;
    private String orderId = null;

    private String shippingContactMechId = null;
    private String billingAccountId = null;
    private String shippingInstructions = null;
    private Boolean maySplit = null;
    private String giftMessage = null;
    private Boolean isGift = null;

    private String shipmentMethodTypeId = "";
    private String carrierPartyId = "";
    private String orderAdditionalEmails = null;
    private Map freeShippingInfo = null;
    private boolean viewCartOnAdd = true;

    /** Holds value of order adjustments. */
    private List adjustments = new LinkedList();
    private List cartLines = new ArrayList();

    private GenericDelegator delegator;

    /** don't allow empty constructor */
    protected ShoppingCart() {}
    
    /** Creates new empty ShoppingCart object. */
    public ShoppingCart(GenericDelegator delegator) {
        this.delegator = delegator;
    }

    // =======================================================================
    // Methods for cart items
    // =======================================================================

    /** Add an item to the shopping cart, or if already there, increase the quantity.
     *  @return the new/increased item index
     */
    public int addOrIncreaseItem(GenericDelegator delegator, String productId, double quantity, HashMap features, HashMap attributes, String prodCatalogId, LocalDispatcher dispatcher) throws CartItemModifyException {
    //public int addOrIncreaseItem(GenericValue product, double quantity, HashMap features) {

        // Check for existing cart item.
        for (int i = 0; i < this.cartLines.size(); i++) {
            ShoppingCartItem sci = (ShoppingCartItem) cartLines.get(i);
            if (sci.equals(productId, features, attributes, prodCatalogId)) {
                double newQuantity = sci.getQuantity() + quantity;
                Debug.logVerbose("Found a match for id " + productId + " on line " + i + ", updating quantity to " + newQuantity);
                sci.setQuantity(newQuantity, dispatcher, this);
                return i;
            }
        }

        // Add the new item to the shopping cart if it wasn't found.
        return this.addItem(0, ShoppingCartItem.makeItem(new Integer(0), delegator, productId, quantity, features, attributes, prodCatalogId, dispatcher, this));
    }
    /** Add an item to the shopping cart. */
    public int addItem(int index, ShoppingCartItem item) {
        if (!cartLines.contains(item)) {
            cartLines.add(index, item);
            return index;
        } else {
            return this.getItemIndex(item);
        }
    }

    /** Add an item to the shopping cart. */
    public int addItemToEnd(GenericDelegator delegator, String productId, double quantity, HashMap features, HashMap attributes, String prodCatalogId, LocalDispatcher dispatcher) throws CartItemModifyException {
        return addItemToEnd(ShoppingCartItem.makeItem(null, delegator, productId, quantity, features, attributes, prodCatalogId, dispatcher, this));
    }
    /** Add an item to the shopping cart. */
    public int addItemToEnd(ShoppingCartItem item) {
        if (!cartLines.contains(item)) {
            cartLines.add(item);
            return cartLines.size() - 1;
        } else {
            return this.getItemIndex(item);
        }
    }

    /** Get a ShoppingCartItem from the cart object. */
    public ShoppingCartItem findCartItem(String productId, HashMap features, HashMap attributes, String prodCatalogId) {
        // Check for existing cart item.
        for (int i = 0; i < this.cartLines.size(); i++) {
            ShoppingCartItem cartItem = (ShoppingCartItem) cartLines.get(i);
            if (cartItem.equals(productId, features, attributes, prodCatalogId)) {
                return cartItem;
            }
        }
        return null;
    }

    /** Remove quantity 0 ShoppingCartItems from the cart object. */
    public void removeEmptyCartItems() {
        // Check for existing cart item.
        for (int i = 0; i < this.cartLines.size(); ) {
            ShoppingCartItem cartItem = (ShoppingCartItem) cartLines.get(i);
            if (cartItem.getQuantity() == 0.0) {
                cartLines.remove(i);
            } else {
                i++;
            }
        }
    }

    /** Returns this item's index. */
    public int getItemIndex(ShoppingCartItem item) {
        return cartLines.indexOf(item);
    }

    /** Get a ShoppingCartItem from the cart object. */
    public ShoppingCartItem findCartItem(int index) {
        if (cartLines.size() <= index)
            return null;
        return (ShoppingCartItem) cartLines.get(index);
    }

    /** Remove an item from the cart object. */
    public void removeCartItem(int index, LocalDispatcher dispatcher) throws CartItemModifyException {
        if (cartLines.size() <= index) return;
        ShoppingCartItem item = (ShoppingCartItem) cartLines.remove(index);
        //set quantity to 0 to trigger necessary events
        item.setQuantity(0.0, dispatcher, this);
    }

    /** Moves a line item to a differnt index. */
    public void moveCartItem(int fromIndex, int toIndex) {
        if (toIndex < fromIndex) {
            cartLines.add(toIndex, cartLines.remove(fromIndex));
        } else if (toIndex > fromIndex) {
            cartLines.add(toIndex - 1, cartLines.remove(fromIndex));
        }
    }

    /** Returns the number of items in the cart object. */
    public int size() {
        return cartLines.size();
    }
    /** Returns a Collection of items in the cart object. */
    public List items() {
        return cartLines;
    }
    /** Returns an iterator of cart items. */
    public Iterator iterator() {
        return cartLines.iterator();
    }

    // =======================================================================
    // Methods for cart fields
    // =======================================================================

    /** Clears out the cart. */
    public void clear() {
        poNumber = null;
        orderId = null;

        shippingInstructions = null;
        maySplit = null;
        giftMessage = null;
        isGift = null;

        orderAdditionalEmails = null;
        freeShippingInfo = null;

        paymentMethodIds.clear();
        paymentMethodTypeIds.clear();
        adjustments.clear();
        cartLines.clear();
    }

    /** Sets the PO Number in the cart. */
    public void setPoNumber(String poNumber) {
        this.poNumber = poNumber;
    }
    /** Returns the po number. */
    public String getPoNumber() {
        return poNumber;
    }

    /** Add the Payment Method Id to the cart. */
    public void addPaymentMethodId(String paymentMethodId) {
        this.paymentMethodIds.add(paymentMethodId);
    }
    /** Returns the Payment Method Ids. */
    public List getPaymentMethodIds() {
        return paymentMethodIds;
    }

    /** Add the Payment Method Type Id to the cart. */
    public void addPaymentMethodTypeId(String paymentMethodTypeId) {
        this.paymentMethodTypeIds.add(paymentMethodTypeId);
    }
    /** Returns the Payment Method Ids. */
    public List getPaymentMethodTypeIds() {
        return paymentMethodTypeIds;
    }

    /** Sets the billing account id string. */
    public void setBillingAccountId(String billingAccountId) {
        this.billingAccountId = billingAccountId;
    }
    /** Returns the billing message string. */
    public String getBillingAccountId() {
        return billingAccountId;
    }

    /** Sets the shipping contact mech id. */
    public void setShippingContactMechId(String shippingContactMechId) {
        this.shippingContactMechId = shippingContactMechId;
    }
    /** Returns the shipping message string. */
    public String getShippingContactMechId() {
        return shippingContactMechId;
    }

    /** Sets the shipment method type. */
    public void setShipmentMethodTypeId(String shipmentMethodTypeId) {
        this.shipmentMethodTypeId = shipmentMethodTypeId;
    }
    /** Returns the shipment method type */
    public String getShipmentMethodTypeId() {
        return shipmentMethodTypeId;
    }

    /** Returns the order level shipping amount */
    public double getOrderShipping() {
        return OrderReadHelper.calcOrderAdjustments(this.getAdjustments(), this.getSubTotal(), false, false, true);
    }

    /** Sets the shipping instructions. */
    public void setShippingInstructions(String shippingInstructions) {
        this.shippingInstructions = shippingInstructions;
    }
    /** Returns the shipping instructions. */
    public String getShippingInstructions() {
        return shippingInstructions;
    }

    public void setMaySplit(Boolean maySplit) {
        this.maySplit = maySplit;
    }
    /** Returns Boolean.TRUE if the order may be shipped (null if unspecified) */
    public Boolean getMaySplit() {
        return maySplit;
    }

    public void setGiftMessage(String giftMessage) {
        this.giftMessage = giftMessage;
    }
    public String getGiftMessage() {
        return giftMessage;
    }

    public void setIsGift(Boolean isGift) {
        this.isGift = isGift;
    }
    public Boolean getIsGift() {
        return isGift;
    }

    public void setCarrierPartyId(String carrierPartyId) {
        this.carrierPartyId = carrierPartyId;
    }
    public String getCarrierPartyId() {
        return carrierPartyId;
    }

    public void setOrderAdditionalEmails(String orderAdditionalEmails) {
        this.orderAdditionalEmails = orderAdditionalEmails;
    }
    public String getOrderAdditionalEmails() {
        return orderAdditionalEmails;
    }

    public List getPaymentMethods(GenericDelegator delegator) {
        List paymentMethods = new LinkedList();
        if (paymentMethodIds != null && paymentMethodIds.size() > 0) {
            Iterator pmIdsIter = paymentMethodIds.iterator();
            while (pmIdsIter.hasNext()) {
                String paymentMethodId = (String) pmIdsIter.next();
                try {
                    paymentMethods.add(delegator.findByPrimaryKey("PaymentMethod", UtilMisc.toMap("paymentMethodId", paymentMethodId)));
                } catch (GenericEntityException e) {
                    Debug.logError(e);
                }
            }
        }
        return paymentMethods;
    }

    public GenericValue getShippingAddress(GenericDelegator delegator) {
        if (this.shippingContactMechId != null) {
            try {
                return delegator.findByPrimaryKey("PostalAddress", UtilMisc.toMap("contactMechId", this.shippingContactMechId));
            } catch (GenericEntityException e) {
                Debug.logWarning(e.toString());
                return null;
            }
        } else {
            return null;
        }
    }

    /** Returns the tax amount from the cart object. */
    public double getTotalSalesTax() {
        double tempTax = 0.0;
        Iterator i = iterator();
        while (i.hasNext()) {
            tempTax += ((ShoppingCartItem) i.next()).getItemTax();
        }
        
        tempTax += OrderReadHelper.calcOrderAdjustments(this.getAdjustments(), getSubTotal(), false, true, false);

        return tempTax;
    }
    /** Returns the shipping amount from the cart object. */
    public double getTotalShipping() {
        double tempShipping = 0.0;
        Iterator i = iterator();
        while (i.hasNext()) {
            tempShipping += ((ShoppingCartItem) i.next()).getItemShipping();
        }
        
        tempShipping += OrderReadHelper.calcOrderAdjustments(this.getAdjustments(), getSubTotal(), false, false, true);

        return tempShipping;
    }

    /** Returns the item-total in the cart (not including discount/tax/shipping). */
    public double getItemTotal() {
        double itemTotal = 0.00;
        Iterator i = iterator();
        while (i.hasNext()) {
            itemTotal += ((ShoppingCartItem) i.next()).getBasePrice();
        }
        return itemTotal;
    }

    /** Returns the sub-total in the cart (item-total - discount). */
    public double getSubTotal() {
        double itemTotal = 0.00;
        Iterator i = iterator();
        while (i.hasNext()) {
            itemTotal += ((ShoppingCartItem) i.next()).getItemSubTotal();
        }
        return itemTotal;
    }

    /** Get a List of adjustments on the order (ie cart) */
    public List getAdjustments() {
        return adjustments;
    }
    /** Add an adjustment to the order; don't worry about setting the orderId, orderItemSeqId or orderAdjustmentId; they will be set when the order is created */
    public void addAdjustment(GenericValue adjustment) {
        adjustments.add(adjustment);
    }
    public void removeAdjustment(int index) {
        adjustments.remove(index);
    }
    /** go through the order adjustments and remove all adjustments with the given type */
    public void removeAdjustmentByType(String orderAdjustmentTypeId) {
        if (orderAdjustmentTypeId == null) return;
        List adjs = this.getAdjustments();
        if (adjs != null) {
            for (int i = 0; i < adjs.size(); ) {
                GenericValue orderAdjustment = (GenericValue) adjs.get(i);
                if (orderAdjustmentTypeId.equals(orderAdjustment.getString("orderAdjustmentTypeId"))) {
                    adjs.remove(i);
                } else {
                    i++;
                }
            }
        }
    }
    public double getOrderOtherAdjustmentTotal() {
        return OrderReadHelper.calcOrderAdjustments(this.getAdjustments(), getSubTotal(), true, false, false);
    }

    /** Returns the total from the cart, including tax/shipping. */
    public double getGrandTotal() {
        return (getSubTotal() + getOrderOtherAdjustmentTotal() + getTotalShipping() + getTotalSalesTax());
    }

    /** Returns the SHIPPABLE item-total in the cart. */
    public double getShippableTotal() {
        double itemTotal = 0.0;
        Iterator i = iterator();
        while (i.hasNext()) {
            ShoppingCartItem item = (ShoppingCartItem) i.next();
            if (item.shippingApplies())
                itemTotal += item.getItemSubTotal();
        }
        return itemTotal;
    }

    /** Returns the total quantity in the cart. */
    public double getTotalQuantity() {
        double count = 0.0;
        Iterator i = iterator();
        while (i.hasNext()) {
            count += ((ShoppingCartItem) i.next()).getQuantity();
        }
        return count;
    }

    /** Returns the total SHIPPABLE quantity in the cart. */
    public double getShippableQuantity() {
        double count = 0.0;
        Iterator i = iterator();
        while (i.hasNext()) {
            ShoppingCartItem item = (ShoppingCartItem) i.next();
            if (item.shippingApplies()) {
                count += item.getQuantity();
            }
        }
        return count;
    }

    /** Returns the total SHIPPABLE weight in the cart. */
    public double getShippableWeight() {
        double weight = 0.0;
        Iterator i = iterator();
        while (i.hasNext()) {
            ShoppingCartItem item = (ShoppingCartItem) i.next();
            if (item.shippingApplies()) {
                weight += (item.getWeight() * item.getQuantity());
            }
        }
        return weight;
    }


    /** Returns the total weight in the cart. */
    public double getTotalWeight() {
        double weight = 0.0;
        Iterator i = iterator();
        while (i.hasNext()) {
            ShoppingCartItem item = (ShoppingCartItem) i.next();
            weight += (item.getWeight() * item.getQuantity());
        }
        return weight;
    }

    /** Returns true if the user wishes to view the cart everytime an item is added. */
    public boolean viewCartOnAdd() {
        return viewCartOnAdd;
    }
    /** Returns true if the user wishes to view the cart everytime an item is added. */
    public void setViewCartOnAdd(boolean viewCartOnAdd) {
        this.viewCartOnAdd = viewCartOnAdd;
    }

    /** Returns the order ID associated with this cart or null if no order has been created yet. */
    public String getOrderId() {
        return this.orderId;
    }
    /** Sets the orderId associated with this cart. */
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    /** Returns the productPromoId used for free shipping or null if free shipping is not used. */
    public Map getFreeShippingInfo() {
        return this.freeShippingInfo;
    }
    /** Sets the productPromoId used for free shipping. */
    public void setFreeShippingInfo(Map freeShippingInfo) {
        this.freeShippingInfo = freeShippingInfo;
    }

    // =======================================================================
    // Methods used for order creation
    // =======================================================================
    
    /** Returns an collection of order items. */
    public List makeOrderItems(GenericDelegator delegator) {
        synchronized (cartLines) {
            List result = new LinkedList();
            Iterator itemIter = cartLines.iterator();
            long cartLineSize = cartLines.size();
            long seqId = 1;
            while (itemIter.hasNext()) {
                ShoppingCartItem item = (ShoppingCartItem) itemIter.next();
                
                //format the string with enough leading zeroes for the number of cartLines
                NumberFormat nf = NumberFormat.getNumberInstance();
                if (cartLineSize > 9) {
                    nf.setMinimumIntegerDigits(2);
                } else if (cartLineSize > 99) {
                    nf.setMinimumIntegerDigits(3);
                } else if (cartLineSize > 999) {
                    nf.setMinimumIntegerDigits(4);
                } else if (cartLineSize > 9999) {
                    //if it's more than 9999, something's up... hit the sky
                    nf.setMinimumIntegerDigits(18);
                }
                String orderItemSeqId = nf.format(seqId);
                seqId++;
                item.setOrderItemSeqId(orderItemSeqId);
                
                GenericValue orderItem = delegator.makeValue("OrderItem", null);
                orderItem.set("orderItemSeqId", orderItemSeqId);
                orderItem.set("orderItemTypeId", "SALES_ORDER_ITEM");
                orderItem.set("productId", item.getProductId());
                orderItem.set("quantity", new Double(item.getQuantity()));
                orderItem.set("unitPrice", new Double(item.getBasePrice()));
                
                orderItem.set("itemDescription", item.getName());
                orderItem.set("comments", item.getItemComment());
                orderItem.set("correspondingPoId", this.getPoNumber());
                orderItem.set("statusId", "ORDER_ORDERED");
                result.add(orderItem);
                //don't do anything with adjustments here, those will be added below in makeAllAdjustments
            }
            return result;
        }
    }
    /** make a list of all adjustments including order adjustments, order line adjustments, and special adjustments (shipping and tax if applicable) */
    public List makeAllAdjustments(GenericDelegator delegator) {
        List allAdjs = new LinkedList(this.getAdjustments());
        
        if (this.freeShippingInfo != null) {
            GenericValue fsOrderAdjustment = delegator.makeValue("OrderAdjustment",
                    UtilMisc.toMap("orderAdjustmentTypeId", "PROMOTION_ADJUSTMENT", "amount", new Double(-this.getTotalShipping()),
                    "productPromoId", freeShippingInfo.get("productPromoId"), "productPromoRuleId", freeShippingInfo.get("productPromoRuleId")));
            
            //if an orderAdjustmentTypeId was passed, override the default
            if (UtilValidate.isNotEmpty((String) freeShippingInfo.get("orderAdjustmentTypeId"))) {
                fsOrderAdjustment.set("orderAdjustmentTypeId", freeShippingInfo.get("orderAdjustmentTypeId"));
            }
            allAdjs.add(fsOrderAdjustment);
        }
        
        //add all of the item adjustments to this list too
        Iterator itemIter = cartLines.iterator();
        int seqId = 1;
        while (itemIter.hasNext()) {
            ShoppingCartItem item = (ShoppingCartItem) itemIter.next();
            Collection adjs = item.getAdjustments();
            if (adjs != null) {
                Iterator adjIter = adjs.iterator();
                while (adjIter.hasNext()) {
                    GenericValue orderAdjustment = (GenericValue) adjIter.next();
                    orderAdjustment.set("orderItemSeqId", item.getOrderItemSeqId());
                    allAdjs.add(orderAdjustment);
                }
            }
        }
        
        return allAdjs;
    }
    /** Returns a Map of cart values */
    public Map makeCartMap(GenericDelegator delegator) {
        Map result = new HashMap();
        result.put("orderItems", makeOrderItems(delegator));
        result.put("orderAdjustments", makeAllAdjustments(delegator));

        result.put("billingAccountId", getBillingAccountId());
        result.put("shippingContactMechId", getShippingContactMechId());
        result.put("shipmentMethodTypeId", getShipmentMethodTypeId());
        result.put("carrierPartyId", getCarrierPartyId());
        result.put("shippingInstructions", getShippingInstructions());
        result.put("maySplit", getMaySplit());
        result.put("giftMessage", getGiftMessage());
        result.put("isGift", getIsGift());
        result.put("paymentMethods", getPaymentMethods(delegator));
        result.put("paymentMethodTypeIds", getPaymentMethodTypeIds());
        return result;
    }    
}
