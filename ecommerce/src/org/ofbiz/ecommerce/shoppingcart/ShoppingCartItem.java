/*
 * $Id$
 */

package org.ofbiz.ecommerce.shoppingcart;

import java.util.*;
import org.ofbiz.core.entity.*;
import org.ofbiz.core.service.*;
import org.ofbiz.core.util.*;
import org.ofbiz.commonapp.order.order.OrderReadHelper;

/**
 * <p><b>Title:</b> ShoppingCartItem.java
 * <p><b>Description:</b> Shopping cart item object.
 * <p>Copyright (c) 2002 The Open For Business Project and repected authors.
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
 * @version    1.1
 * @created    August 4, 2001
 */
public class ShoppingCartItem implements java.io.Serializable {
    
    private transient GenericDelegator delegator = null;
    private String delegatorName = null;

    private transient GenericValue _product = null;
    private String prodCatalogId = null;
    private String productId = null;
    private String itemComment = null;
    private double quantity = 0.0;
    private Map features = null;
    private Map attributes = null;
    private String orderItemSeqId = null;
    
    private List itemAdjustments = new LinkedList();
    private boolean isPromo = false;

    /** makes a ShoppingCartItem and adds it to the cart at cartLocation, or at the end if cartLocation is null */
    public static ShoppingCartItem makeItem(Integer cartLocation, GenericValue product, double quantity, String prodCatalogId, LocalDispatcher dispatcher, ShoppingCart cart) throws CartItemModifyException {
        return makeItem(cartLocation, product, quantity, null, null, prodCatalogId, dispatcher, cart);
    }
    
    /** makes a ShoppingCartItem and adds it to the cart at cartLocation, or at the end if cartLocation is null */
    public static ShoppingCartItem makeItem(Integer cartLocation, GenericDelegator delegator, String productId, double quantity, HashMap features, HashMap attributes, String prodCatalogId, LocalDispatcher dispatcher, ShoppingCart cart) throws CartItemModifyException {
        GenericValue product = null;
        try {
            product = delegator.findByPrimaryKeyCache("Product", UtilMisc.toMap("productId", productId));
        } catch (GenericEntityException e) {
            Debug.logWarning(e.getMessage());
            product = null;
        }
        
        if (product == null) {
            String excMsg = "Product not found, not adding to cart. [productId: " + product.getString("productId") + "]";
            Debug.logWarning(excMsg);
            throw new CartItemModifyException(excMsg);
        }
        
        return makeItem(cartLocation, product, quantity, features, attributes, prodCatalogId, dispatcher, cart);
    }
    
    /** makes a ShoppingCartItem and adds it to the cart at cartLocation, or at the end if cartLocation is null */
    public static ShoppingCartItem makeItem(Integer cartLocation, GenericValue product, double quantity, HashMap features, HashMap attributes, String prodCatalogId, LocalDispatcher dispatcher, ShoppingCart cart) throws CartItemModifyException {
        return makeItem(cartLocation, product, quantity, features, attributes, prodCatalogId, dispatcher, cart, true);
    }
    
    /** makes a ShoppingCartItem and adds it to the cart at cartLocation, or at the end if cartLocation is null */
    public static ShoppingCartItem makeItem(Integer cartLocation, GenericValue product, double quantity, HashMap features, HashMap attributes, String prodCatalogId, LocalDispatcher dispatcher, ShoppingCart cart, boolean doPromotions) throws CartItemModifyException {
        ShoppingCartItem newItem = new ShoppingCartItem(product, features, attributes, prodCatalogId);

        //check to see if product is virtual
        if ("Y".equals(product.getString("isVirtual"))) {
            String excMsg = "Tried to add the Virtual Product " + product.getString("productName") + 
                    " (productId: " + product.getString("productId") + ") to the cart, not adding.";
            Debug.logWarning(excMsg);
            throw new CartItemModifyException(excMsg);
        }

        java.sql.Timestamp nowTimestamp = UtilDateTime.nowTimestamp();
        //check to see if introductionDate hasn't passed yet
        if (product.get("introductionDate") != null && nowTimestamp.before(product.getTimestamp("introductionDate"))) {
            String excMsg = "Tried to add the Product " + product.getString("productName") + 
                    " (productId: " + product.getString("productId") + ") to the cart. This product has not yet been made available for sale, so not adding.";
            Debug.logWarning(excMsg);
            throw new CartItemModifyException(excMsg);
        }
        
        //check to see if salesDiscontinuationDate has passed
        if (product.get("salesDiscontinuationDate") != null && nowTimestamp.after(product.getTimestamp("salesDiscontinuationDate"))) {
            String excMsg = "Tried to add the Product " + product.getString("productName") + 
                    " (productId: " + product.getString("productId") + ") to the cart. This product is no longer available for sale, so not adding.";
            Debug.logWarning(excMsg);
            throw new CartItemModifyException(excMsg);
        }
        
        //add to cart before setting quantity so that we can get order total, etc
        if (cartLocation == null) {
            cart.addItemToEnd(newItem);
        } else {
            cart.addItem(cartLocation.intValue(), newItem);
        }
        
        try {
            newItem.setQuantity(quantity, dispatcher, cart, doPromotions);
        } catch (CartItemModifyException e) {
            cart.removeEmptyCartItems();
            throw e;
        }
        return newItem;
    }
    
    /** can't create shopping cart item with no parameters */
    protected ShoppingCartItem() {}
    
    /** Creates new ShoppingCartItem object. */
    protected ShoppingCartItem(GenericValue product, HashMap features, HashMap attributes, String prodCatalogId) {
        this._product = product;
        this.productId = _product.getString("productId");
        this.prodCatalogId = prodCatalogId;
        this.itemComment = null;
        this.attributes = attributes;
        this.features = features;
        this.delegatorName = _product.getDelegator().getDelegatorName();
    }

    /** Sets the quantity for the item and validates the change in quantity, etc */
    public void setQuantity(double quantity, LocalDispatcher dispatcher, ShoppingCart cart) throws CartItemModifyException {
        setQuantity(quantity, dispatcher, cart, true);
    }
    
    /** Sets the quantity for the item and validates the change in quantity, etc */
    public void setQuantity(double quantity, LocalDispatcher dispatcher, ShoppingCart cart, boolean doPromotions) throws CartItemModifyException {
        if (this.quantity == quantity) {
            return;
        }

        if (this.isPromo) {
            throw new CartItemModifyException("Sorry, you can't change the quantity on the promotion item " + getProduct().getString("productName") + " (product ID: " + productId + "), not setting quantity.");
        }
        
        //check inventory if new quantity is greater than old quantity; don't worry about inventory getting pulled out from under, that will be handled at checkout time
        if (quantity > this.quantity) {
            if (!org.ofbiz.commonapp.product.catalog.CatalogWorker.isCatalogInventoryAvailable(this.prodCatalogId, productId, quantity, getDelegator(), dispatcher)) {
                String excMsg = "Sorry, we do not have enough (you tried " + UtilFormatOut.formatQuantity(quantity) + ") of the product " + getProduct().getString("productName") + " (product ID: " + productId + ") in stock, not adding to cart. Please try a lower quantity, try again later, or call customer service for more information.";
                Debug.logWarning(excMsg);
                throw new CartItemModifyException(excMsg);
            }
        }
        
        //set quantity before promos so order total, etc will be updated
        double oldQuantity = this.quantity;
        this.quantity = quantity;
        
        //apply/unapply promotions
        if (doPromotions) {
            org.ofbiz.ecommerce.catalog.ProductPromoWorker.doPromotions(prodCatalogId, cart, this, oldQuantity, getDelegator(), dispatcher);
        }
    }
    /** Returns the quantity. */
    public double getQuantity() {
        return quantity;
    }
    
    /** Sets the item comment. */
    public void setItemComment(String itemComment) {
        this.itemComment = itemComment;
    }
    /** Returns the item's comment. */
    public String getItemComment() {
        return itemComment;
    }

    public void setOrderItemSeqId(String orderItemSeqId) {
        this.orderItemSeqId = orderItemSeqId;
    }
    public String getOrderItemSeqId() {
        return orderItemSeqId;
    }
        
    /** Returns true if shipping charges apply to this item. */
    public boolean shippingApplies() {
        Boolean shipCharge = getProduct().getBoolean("chargeShipping");
        if (shipCharge == null) {
            return true;
        } else {
            return shipCharge.booleanValue();
        }
    }
    
    /** Returns true if tax charges apply to this item. */
    public boolean taxApplies() {
        Boolean taxable = getProduct().getBoolean("taxable");
        if (taxable == null) {
            return true;
        } else {
            return taxable.booleanValue();
        }
    }
    
    /** Returns the item's productId. */
    public String getProductId() {
        return productId;
    }
    /** Returns the item's description. */
    public String getName() {
        return getProduct().getString("productName");
    }
    /** Returns the item's description. */
    public String getDescription() {
        return getProduct().getString("description");
    }
    /** Returns the item's unit weight */
    public double getWeight() {
        Double weight = getProduct().getDouble("weight");
        if (weight == null) {
            return 0;
        } else {
            return weight.doubleValue();
        }
    }
                
    /** Returns the base price. */
    public double getBasePrice() {
        // todo calculate the price using price component.
        Double defaultPrice = getProduct().getDouble("defaultPrice");
        double defPrice = 0.0;
        if (defaultPrice != null) {
            defPrice = defaultPrice.doubleValue();
        }

        return defPrice;
    }
    /** Returns the "other" adjustments. */
    public double getOtherAdjustments() {
        return OrderReadHelper.calcItemAdjustments(new Double(quantity), new Double(getBasePrice()), this.getAdjustments(), true, false, false);
    }
    /** Returns the total line price. */
    public double getItemSubTotal() {
        return (getBasePrice() * quantity) + getOtherAdjustments();
    }

    /** Returns the tax adjustments. */
    public double getItemTax() {
        return OrderReadHelper.calcItemAdjustments(new Double(quantity), new Double(getBasePrice()), this.getAdjustments(), false, true, false);
    }
    /** Returns the shipping adjustments. */
    public double getItemShipping() {
        return OrderReadHelper.calcItemAdjustments(new Double(quantity), new Double(getBasePrice()), this.getAdjustments(), false, false, true);
    }
    
    /** Returns the features for the item. */
    public Map getFeatures() {
        return features;
    }
    /* * Returns a collection of attribute names. * /
    public Collection getFeatureNames() {
        if (features == null || features.size() < 1)
            return null;       
        return (Collection) features.keySet();
    }
    / * * Returns a collection of attribute values. * /
    public Collection getFeatureValues() {
        if (features == null || features.size() < 1)
            return null;
        return features.values();
    }
     */  

    /** Sets an item features. */
    public void setFeature(String name, String value) {
        if (features == null) features = new HashMap();
        features.put(name,value);
    }
    /** Return a specific features. */
    public String getFeature(String name) {
        if (features == null) return null;
        return (String) features.get(name);
    }        
    
    /** Sets an item attribute. */
    public void setAttribute(String name, String value) {
        if (attributes == null) attributes = new HashMap();
        attributes.put(name,value);
    }
    /** Return a specific attribute. */
    public String getAttribute(String name) {
        if (attributes == null) return null;
        return (String) attributes.get(name);
    }        
    /** Returns the attributes for the item. */
    public Map getAttributes() {
        return attributes;
    }
    
    /** Add an adjustment to the order item; don't worry about setting the orderId, orderItemSeqId or orderAdjustmentId; they will be set when the order is created */
    public void addAdjustment(GenericValue adjustment) {
        itemAdjustments.add(adjustment);
    }
    public void removeAdjustment(GenericValue adjustment) {
        itemAdjustments.remove(adjustment);
    }
    public void removeAdjustment(int index) {
        itemAdjustments.remove(index);
    }
    public Collection getAdjustments() {
        return itemAdjustments;
    }

    public void setIsPromo(boolean isPromo) {
        this.isPromo = isPromo;
    }
    public boolean getIsPromo() {
        return this.isPromo;
    }
    
    /** Compares the specified object with this cart item. */
    public boolean equals(ShoppingCartItem item) {
        if (item == null) return false;
        return this.equals(item.getProductId(), item.getFeatures(), item.getAttributes(), item.prodCatalogId, item.getIsPromo());
    }
    
    /** Compares the specified object with this cart item. Defaults isPromo to false. */
    public boolean equals(String productId, Map features, Map attributes, String prodCatalogId) {
        return equals(productId, features, attributes, prodCatalogId, false);
    }
    
    /** Compares the specified object with this cart item. */
    public boolean equals(String productId, Map features, Map attributes, String prodCatalogId, boolean isPromo) {
        if (!this.productId.equals(productId)) {
            return false;
        }
        
        if (!this.prodCatalogId.equals(prodCatalogId)) {
            return false;
        }

        if (this.isPromo != isPromo) {
            return false;
        }
        
        if (this.features != null && features == null ||
                this.features == null && features != null) {
            return false;
        }
        if (this.features != null && features != null) {
            if (!this.features.equals(features)) {
                return false;
            }
        }

        if (this.attributes != null && attributes == null ||
                this.attributes == null && attributes != null) {
            return false;
        }
        if (this.attributes != null && attributes != null) {
            if (!this.attributes.equals(attributes)) {
                return false;
            }
        }

        return true;
    }      
    
    // Gets the Product entity if its not there
    public GenericValue getProduct() {
        if (_product != null) {
            return _product;
        }
        if (delegatorName == null || productId == null) {
            throw new IllegalStateException("Bad delegator name or product id");
        }
        try {
            _product = this.getDelegator().findByPrimaryKeyCache("Product", UtilMisc.toMap("productId", productId));
        } catch (GenericEntityException e) {
            throw new RuntimeException("Error with Entity Engine ("+e.getMessage()+")");
        }
        return _product;
    }
    
    public GenericDelegator getDelegator() {
        if (delegator == null) {
            delegator = GenericDelegator.getGenericDelegator(delegatorName);
        }
        return delegator;
    }
}
