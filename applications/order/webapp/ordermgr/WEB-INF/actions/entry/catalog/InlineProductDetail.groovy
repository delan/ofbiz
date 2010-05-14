/*
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
 */

import java.lang.*;
import java.util.*;
import java.text.NumberFormat;
import org.ofbiz.base.util.*;
import org.ofbiz.service.*;
import org.ofbiz.entity.*;
import org.ofbiz.entity.condition.*;
import org.ofbiz.entity.util.*;
import org.ofbiz.webapp.taglib.*;
import org.ofbiz.webapp.stats.VisitHandler;
import org.ofbiz.order.shoppingcart.ShoppingCartEvents;
import org.ofbiz.product.catalog.*;
import org.ofbiz.product.category.*;
import org.ofbiz.product.product.ProductWorker;
import org.ofbiz.product.product.ProductContentWrapper;
import org.ofbiz.product.product.ProductSearch;
import org.ofbiz.product.product.ProductSearchSession;
import org.ofbiz.product.store.*;

inlineProductId = request.getAttribute("inlineProductId");
inlineCounter = request.getAttribute("inlineCounter");
context.inlineCounter = inlineCounter;
context.inlineProductId = inlineProductId;

contentPathPrefix = CatalogWorker.getContentPathPrefix(request);
catalogName = CatalogWorker.getCatalogName(request);
currentCatalogId = CatalogWorker.getCurrentCatalogId(request);

if (inlineProductId) {
    inlineProduct = delegator.findByPrimaryKeyCache("Product", [productId : inlineProductId]);
    if (inlineProduct) {
        context.product = inlineProduct;
        contentWrapper = new ProductContentWrapper(inlineProduct, request);
        context.put("title", contentWrapper.get("PRODUCT_NAME"));
        context.put("metaDescription", contentWrapper.get("DESCRIPTION"));
        productTemplate = product.detailScreen;
        if (productTemplate) {
            detailScreen = productTemplate;
        }
    }
}

templatePathPrefix = CatalogWorker.getTemplatePathPrefix(request);
if (templatePathPrefix) {
    detailScreen = templatePathPrefix + detailScreen;
}
context.detailScreen = detailScreen;

String buildNext(Map map, List order, String current, String prefix, Map featureTypes) {
    def ct = 0;
    def buf = new StringBuffer();
    buf.append("function listFT" + inlineCounter + current + prefix + "() { ");
    buf.append("document.forms[\"configform\"].elements[\"FT" + inlineCounter + current + "\"].options.length = 1;");
    buf.append("document.forms[\"configform\"].elements[\"FT" + inlineCounter + current + "\"].options[0] = new Option(\"" + featureTypes[current] + "\",\"\",true,true);");
    map.each { key, value ->
        def optValue = null;

        if (order.indexOf(current) == (order.size()-1)) {
            optValue = value.iterator().next();
        } else {
            optValue = prefix + "_" + ct;
        }

        buf.append("document.forms[\"configform\"].elements[\"FT" + inlineCounter + current + "\"].options[" + (ct + 1) + "] = new Option(\"" + key + "\",\"" + optValue + "\");");
        ct++;
    }
    buf.append(" }");
    if (order.indexOf(current) < (order.size()-1)) {
        ct = 0;
        map.each { key, value ->
            def nextOrder = order.get(order.indexOf(current)+1);
            def newPrefix = prefix + "_" + ct;
            buf.append(buildNext(value, order, nextOrder, newPrefix, featureTypes));
            ct++;
        }
    }
    return buf.toString();
}

cart = ShoppingCartEvents.getCartObject(request);

// set the content path prefix
contentPathPrefix = CatalogWorker.getContentPathPrefix(request);
context.contentPathPrefix = contentPathPrefix;

// get the product detail information
if (inlineProduct) {
    inlineProductId = inlineProduct.productId;
    productTypeId = inlineProduct.productTypeId;
    featureTypes = [:];
    featureOrder = [];

    // make the productContentWrapper
    productContentWrapper = new ProductContentWrapper(inlineProduct, request);
    context.productContentWrapper = productContentWrapper;

    // get the main detail image (virtual or single product)
    mainDetailImage = productContentWrapper.get("DETAIL_IMAGE_URL");
    if (mainDetailImage) {
        mainDetailImageUrl = ContentUrlTag.getContentPrefix(request) + mainDetailImage;
        context.mainDetailImageUrl = mainDetailImageUrl.toString();
    }


    // get the product price
    webSiteId = CatalogWorker.getWebSiteId(request);
    autoUserLogin = request.getSession().getAttribute("autoUserLogin");
    if (cart.isSalesOrder()) {
        // sales order: run the "calculateProductPrice" service
        priceContext = [product : inlineProduct, prodCatalogId : currentCatalogId,
            currencyUomId : cart.getCurrency(), autoUserLogin : autoUserLogin];
        priceContext.webSiteId = webSiteId;
        priceContext.productStoreId = productStoreId;
        priceContext.checkIncludeVat = "Y";
        priceContext.agreementId = cart.getAgreementId();
        priceContext.partyId = cart.getPartyId();  // IMPORTANT: must put this in, or price will be calculated for the CSR instead of the customer
        priceMap = dispatcher.runSync("calculateProductPrice", priceContext);
        context.priceMap = priceMap;
    } else {
        // purchase order: run the "calculatePurchasePrice" service
        priceContext = [product : inlineProduct, currencyUomId : cart.getCurrency(),
                partyId : cart.getPartyId(), userLogin : userLogin];
        priceMap = dispatcher.runSync("calculatePurchasePrice", priceContext);
        context.priceMap = priceMap;
    }


    context.variantTree = null;
    context.variantTreeSize = null;
    context.variantSample = null;
    context.variantSampleKeys = null;
    context.variantSampleSize = null;
    if ("Y".equals(inlineProduct.isVirtual)) {
        if ("VV_FEATURETREE".equals(ProductWorker.getProductVirtualVariantMethod(delegator, inlineProductId))) {
            context.featureLists = ProductWorker.getSelectableProductFeaturesByTypesAndSeq(inlineProduct);
        } else {
            featureMap = dispatcher.runSync("getProductFeatureSet", [productId : inlineProductId]);
            featureSet = featureMap.featureSet;
            if (featureSet) {
                variantTreeMap = dispatcher.runSync("getProductVariantTree", [productId : inlineProductId, featureOrder : featureSet, productStoreId : productStoreId]);
                variantTree = variantTreeMap.variantTree;
                imageMap = variantTreeMap.variantSample;
                virtualVariant = variantTreeMap.virtualVariant;
                context.virtualVariant = virtualVariant;
                if (variantTree) {
                    context.variantTree = variantTree;
                    context.variantTreeSize = variantTree.size();
                }
                if (imageMap) {
                    context.variantSample = imageMap;
                    context.variantSampleKeys = imageMap.keySet();
                    context.variantSampleSize = imageMap.size();
                }
                context.featureSet = featureSet;

                if (variantTree) {
                    featureOrder = new LinkedList(featureSet);
                    featureOrder.each { featureKey ->
                        featureValue = delegator.findByPrimaryKeyCache("ProductFeatureType", [productFeatureTypeId : featureKey]);
                        fValue = featureValue.get("description") ?: featureValue.productFeatureTypeId;
                        featureTypes[featureKey] = fValue;
                    }
                }
                context.featureTypes = featureTypes;
                context.featureOrder = featureOrder;
                if (featureOrder) {
                    context.featureOrderFirst = featureOrder[0];
                }

                if (variantTree && imageMap) {
                    jsBuf = new StringBuffer();
                    jsBuf.append("<script language=\"JavaScript\" type=\"text/javascript\">");
                    jsBuf.append("var DET" + inlineCounter + "= new Array(" + variantTree.size() + ");");
                    jsBuf.append("var IMG" + inlineCounter + " = new Array(" + variantTree.size() + ");");
                    jsBuf.append("var OPT" + inlineCounter + " = new Array(" + featureOrder.size() + ");");
                    jsBuf.append("var VIR" + inlineCounter + " = new Array(" + virtualVariant.size() + ");");
                    jsBuf.append("var detailImageUrl" + inlineCounter + " = null;");
                    featureOrder.eachWithIndex { feature, i ->
                        jsBuf.append("OPT" + inlineCounter + "[" + i + "] = \"FT" + inlineCounter + feature + "\";");
                    }
                    virtualVariant.eachWithIndex { variant, i ->
                        jsBuf.append("VIR" + inlineCounter + "[" + i + "] = \"" + variant + "\";");
                    }

                    // build the top level
                    topLevelName = featureOrder[0];
                    jsBuf.append("function list" + inlineCounter + topLevelName + "() {");
                    jsBuf.append("document.forms[\"configform\"].elements[\"FT" + inlineCounter + topLevelName + "\"].options.length = 1;");
                    jsBuf.append("document.forms[\"configform\"].elements[\"FT" + inlineCounter + topLevelName + "\"].options[0] = new Option(\"" + featureTypes[topLevelName] + "\",\"\",true,true);");
                    if (variantTree) {
                        featureOrder.each { featureKey ->
                            jsBuf.append("document.forms[\"configform\"].elements[\"FT" + inlineCounter + featureKey + "\"].options.length = 1;");
                        }
                        firstDetailImage = null;
                        firstLargeImage = null;
                        counter = 0;
                        variantTree.each { key, value ->
                            opt = null;
                            if (featureOrder.size() == 1) {
                                opt = value.iterator().next();
                            } else {
                                opt = counter as String;
                            }
                            // create the variant content wrapper
                            contentWrapper = new ProductContentWrapper(imageMap[key], request);

                            // initial image paths
                            detailImage = contentWrapper.get("DETAIL_IMAGE_URL") ?: productContentWrapper.get("DETAIL_IMAGE_URL");
                            largeImage = contentWrapper.get("LARGE_IMAGE_URL") ?: productContentWrapper.get("LARGE_IMAGE_URL");

                            // full image URLs
                            detailImageUrl = null;
                            largeImageUrl = null;

                            // append the content prefix
                            if (detailImage) {
                                detailImageUrl = ContentUrlTag.getContentPrefix(request) + detailImage;
                            }
                            if (largeImage) {
                                largeImageUrl = ContentUrlTag.getContentPrefix(request) + largeImage;
                            }

                            jsBuf.append("document.forms[\"configform\"].elements[\"FT" + inlineCounter + topLevelName + "\"].options[" + (counter+1) + "] = new Option(\"" + key + "\",\"" + opt + "\");");
                            jsBuf.append("DET" + inlineCounter + "[" + counter + "] = \"" + detailImageUrl +"\";");
                            jsBuf.append("IMG" + inlineCounter + "[" + counter + "] = \"" + largeImageUrl +"\";");

                            if (!firstDetailImage) {
                                firstDetailImage = detailImageUrl;
                            }
                            if (!firstLargeImage) {
                                firstLargeImage = largeImage;
                            }
                            counter++;
                        }
                        context.firstDetailImage = firstDetailImage;
                        context.firstLargeImage = firstLargeImage;
                    }
                    jsBuf.append("}");

                    // build dynamic lists
                    if (variantTree) {
                        variantTree.values().eachWithIndex { varTree, topLevelKeysCt ->
                            cnt = "" + topLevelKeysCt;
                            if (varTree instanceof Map) {
                                jsBuf.append(buildNext(varTree, featureOrder, featureOrder[1], cnt, featureTypes));
                            }
                        }
                    }

                    // make a list of variant sku with requireAmount
                    variantsRes = dispatcher.runSync("getAssociatedProducts", [productId : inlineProductId, type : "PRODUCT_VARIANT", checkViewAllow : true, prodCatalogId : currentCatalogId]);
                    variants = variantsRes.assocProducts;
                    if (variants) {
                        amt = new StringBuffer();
                        amt.append("function checkAmtReq" + inlineCounter + "(sku) { ");
                        // Create the javascript to return the price for each variant
                        variantPriceJS = new StringBuffer();
                        variantPriceJS.append("function getVariantPrice" + inlineCounter + "(sku) { ");
                        // Format to apply the currency code to the variant price in the javascript
                        productStore = ProductStoreWorker.getProductStore(request);
                        localeString = productStore.defaultLocaleString;
                        if (localeString) {
                            locale = UtilMisc.parseLocale(localeString);
                        }
                        numberFormat = NumberFormat.getCurrencyInstance(locale);
                        variants.each { variantAssoc ->
                            variant = variantAssoc.getRelatedOne("AssocProduct");
                            // Get the price for each variant. Reuse the priceContext already setup for virtual product above and replace the product
                            if (cart.isSalesOrder()) {
                                // sales order: run the "calculateProductPrice" service
                                priceContext.product = variant;
                                variantPriceMap = dispatcher.runSync("calculateProductPrice", priceContext);
                            }
                            amt.append(" if (sku == \"" + variant.productId + "\") return \"" + (variant.requireAmount ?: "N") + "\"; ");
                            variantPriceJS.append("  if (sku == \"" + variant.productId + "\") return \"" + numberFormat.format(variantPriceMap.basePrice) + "\"; ");
                        }
                        amt.append(" } ");
                        variantPriceJS.append(" } ");
                    }
                    jsBuf.append(amt.toString());
                    jsBuf.append(variantPriceJS.toString());
                    jsBuf.append("</script>");

                    context.virtualJavaScript = jsBuf;
                }
            }
        }
    }
}
