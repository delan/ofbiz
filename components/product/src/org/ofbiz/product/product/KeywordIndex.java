/*
 * $Id: KeywordIndex.java,v 1.10 2004/01/27 01:00:59 jonesde Exp $
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
package org.ofbiz.product.product;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.GeneralException;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilProperties;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.content.data.DataResourceWorker;
import org.ofbiz.entity.GenericDelegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.util.EntityUtil;

/**
 *  Does indexing in preparation for a keyword search.
 *
 * @author     <a href="mailto:jonesde@ofbiz.org">David E. Jones</a>
 * @version    $Revision: 1.10 $
 * @since      2.0
 */
public class KeywordIndex {
    
    public static final String module = KeywordIndex.class.getName();

    public static void indexKeywords(GenericValue product, boolean doAll) throws GenericEntityException {
        if (product == null) return;
        Timestamp nowTimestamp = UtilDateTime.nowTimestamp();
        
        if (!doAll) {
            if ("Y".equals(product.getString("isVariant"))) {
                return;
            }
            if ("N".equals(product.getString("autoCreateKeywords"))) {
                return;
            }
            Timestamp salesDiscontinuationDate = product.getTimestamp("salesDiscontinuationDate");
            if (salesDiscontinuationDate != null && salesDiscontinuationDate.before(nowTimestamp)) {
                return;
            }
        }
        
        GenericDelegator delegator = product.getDelegator();
        if (delegator == null) return;
        String productId = product.getString("productId");

        // String separators = ";: ,.!?\t\"\'\r\n\\/()[]{}*%<>-+_";
        String separators = UtilProperties.getPropertyValue("prodsearch", "index.keyword.separators", ";: ,.!?\t\"\'\r\n\\/()[]{}*%<>-+_");
        String stopWordBagOr = UtilProperties.getPropertyValue("prodsearch", "stop.word.bag.or");
        String stopWordBagAnd = UtilProperties.getPropertyValue("prodsearch", "stop.word.bag.and");

        String removeStemsStr = UtilProperties.getPropertyValue("prodsearch", "remove.stems");
        boolean removeStems = "true".equals(removeStemsStr);
        String stemBag = UtilProperties.getPropertyValue("prodsearch", "stem.bag");
        List stemList = new ArrayList(10);
        if (UtilValidate.isNotEmpty(stemBag)) {
            String curToken;
            StringTokenizer tokenizer = new StringTokenizer(stemBag, ": ");
            while (tokenizer.hasMoreTokens()) {
                curToken = tokenizer.nextToken();
                stemList.add(curToken);
            }
        }
        
        Map keywords = new TreeMap();
        List strings = new ArrayList(50);

        int pidWeight = 1;
        try {
            pidWeight = Integer.parseInt(UtilProperties.getPropertyValue("prodsearch", "index.weight.Product.productId", "1"));
        } catch (Exception e) {
            Debug.logWarning("Could not parse weight number: " + e.toString(), module);
        }
        keywords.put(product.getString("productId").toLowerCase(), new Long(pidWeight));

        addWeightedKeywordSourceString(product, "productName", strings);
        addWeightedKeywordSourceString(product, "brandName", strings);
        addWeightedKeywordSourceString(product, "description", strings);
        addWeightedKeywordSourceString(product, "longDescription", strings);

        if (!"0".equals(UtilProperties.getPropertyValue("prodsearch", "index.weight.ProductFeatureAndAppl.description", "1")) ||
            !"0".equals(UtilProperties.getPropertyValue("prodsearch", "index.weight.ProductFeatureAndAppl.abbrev", "1")) ||
            !"0".equals(UtilProperties.getPropertyValue("prodsearch", "index.weight.ProductFeatureAndAppl.idCode", "1"))) {
            // get strings from attributes and features
            Iterator productFeatureAndAppls = UtilMisc.toIterator(delegator.findByAnd("ProductFeatureAndAppl", UtilMisc.toMap("productId", productId)));
            while (productFeatureAndAppls != null && productFeatureAndAppls.hasNext()) {
                GenericValue productFeatureAndAppl = (GenericValue) productFeatureAndAppls.next();
                addWeightedKeywordSourceString(productFeatureAndAppl, "description", strings);
                addWeightedKeywordSourceString(productFeatureAndAppl, "abbrev", strings);
                addWeightedKeywordSourceString(productFeatureAndAppl, "idCode", strings);
            }
        }

        // ProductAttribute
        if (!"0".equals(UtilProperties.getPropertyValue("prodsearch", "index.weight.ProductAttribute.attrName", "1")) ||
                !"0".equals(UtilProperties.getPropertyValue("prodsearch", "index.weight.ProductAttribute.attrValue", "1"))) {
            Iterator productAttributes = UtilMisc.toIterator(delegator.findByAnd("ProductAttribute", UtilMisc.toMap("productId", productId)));
            while (productAttributes != null && productAttributes.hasNext()) {
                GenericValue productAttribute = (GenericValue) productAttributes.next();
                addWeightedKeywordSourceString(productAttribute, "attrName", strings);
                addWeightedKeywordSourceString(productAttribute, "attrValue", strings);
            }
        }

        // GoodIdentification
        if (!"0".equals(UtilProperties.getPropertyValue("prodsearch", "index.weight.GoodIdentification.idValue", "1"))) {
            Iterator goodIdentifications = UtilMisc.toIterator(delegator.findByAnd("GoodIdentification", UtilMisc.toMap("productId", productId)));
            while (goodIdentifications != null && goodIdentifications.hasNext()) {
                GenericValue goodIdentification = (GenericValue) goodIdentifications.next();
                addWeightedKeywordSourceString(goodIdentification, "idValue", strings);
            }
        }
        
        // Variant Product IDs
        if ("Y".equals(product.getString("isVirtual"))) {
            if (!"0".equals(UtilProperties.getPropertyValue("prodsearch", "index.weight.Variant.Product.productId", "1"))) {
                Iterator variantProducts = UtilMisc.toIterator(delegator.findByAnd("ProductAssoc", UtilMisc.toMap("productId", productId, "productAssocTypeId", "PRODUCT_VARIANT")));
                while (variantProducts != null && variantProducts.hasNext()) {
                    GenericValue variantProduct = (GenericValue) variantProducts.next();
                    int weight = 1;
                    try {
                        weight = Integer.parseInt(UtilProperties.getPropertyValue("prodsearch", "index.weight.Variant.Product.productId", "1"));
                    } catch (Exception e) {
                        Debug.logWarning("Could not parse weight number: " + e.toString(), module);
                    }
                    for (int i = 0; i < weight; i++) {
                        strings.add(variantProduct.getString("productId"));
                    }
                }
            }
        }
        
        String productContentTypes = UtilProperties.getPropertyValue("prodsearch", "index.include.ProductContentTypes");
        List productContentTypeList = Arrays.asList(productContentTypes.split(","));
        Iterator productContentTypeIter = productContentTypeList.iterator();
        while (productContentTypeIter.hasNext()) {
            String productContentTypeId = (String) productContentTypeIter.next();

            int weight = 1;
            try {
                weight = Integer.parseInt(UtilProperties.getPropertyValue("prodsearch", "index.weight.ProductContent." + productContentTypeId, "1"));
            } catch (Exception e) {
                Debug.logWarning("Could not parse weight number: " + e.toString(), module);
            }
            
            List productContentAndInfos = delegator.findByAnd("ProductContentAndInfo", UtilMisc.toMap("productId", productId, "productContentTypeId", productContentTypeId), null);
            Iterator productContentAndInfoIter = productContentAndInfos.iterator();
            while (productContentAndInfoIter.hasNext()) {
                GenericValue productContentAndInfo = (GenericValue) productContentAndInfoIter.next();
                addWeightedDataResourceString(productContentAndInfo, weight, strings, delegator);
                
                List alternateViews = productContentAndInfo.getRelated("ContentAssocDataResourceViewTo", UtilMisc.toMap("caContentAssocTypeId", "ALTERNATE_LOCALE"), UtilMisc.toList("-caFromDate"));
                alternateViews = EntityUtil.filterByDate(alternateViews, UtilDateTime.nowTimestamp(), "caFromDate", "caThruDate", true);
                Iterator alternateViewIter = alternateViews.iterator();
                while (alternateViewIter.hasNext()) {
                    GenericValue thisView = (GenericValue) alternateViewIter.next();
                    addWeightedDataResourceString(thisView, weight, strings, delegator);
                }
            }
        }
        
        Iterator strIter = strings.iterator();
        while (strIter.hasNext()) {
            String str = (String) strIter.next();

            if (str.length() > 0) {
                StringTokenizer tokener = new StringTokenizer(str, separators, false);

                while (tokener.hasMoreTokens()) {
                    // make sure it is lower case before doing anything else
                    String token = tokener.nextToken().toLowerCase();
                    
                    // when cleaning up the tokens the ordering is inportant: check stop words, remove stems, then get rid of 1 character tokens (1 digit okay)
                    
                    // check stop words
                    String colonToken = ":" + token + ":";
                    if (stopWordBagOr.indexOf(colonToken) >= 0 && stopWordBagAnd.indexOf(colonToken) >= 0) {
                        continue;
                    }
                    
                    // remove stems
                    if (removeStems) {
                        Iterator stemIter = stemList.iterator();
                        while (stemIter.hasNext()) {
                            String stem = (String) stemIter.next();
                            if (token.endsWith(stem)) {
                                token = token.substring(0, token.length() - stem.length());
                            }
                        }
                    }
                    
                    // get rid of all length 0 tokens now
                    if (token.length() == 0) {
                        continue;
                    }
                    
                    // get rid of all length 1 character only tokens, pretty much useless
                    if (token.length() == 1 && Character.isLetter(token.charAt(0))) {
                        continue;
                    }

                    // group by word, add up weight
                    Long curWeight = (Long) keywords.get(token);
                    if (curWeight == null) {
                        keywords.put(token, new Long(1));
                    } else {
                        keywords.put(token, new Long(curWeight.longValue() + 1));
                    }
                }
            }
        }

        List toBeStored = new LinkedList();
        Iterator kiter = keywords.entrySet().iterator();
        while (kiter.hasNext()) {
            Map.Entry entry = (Map.Entry) kiter.next();
            GenericValue productKeyword = delegator.makeValue("ProductKeyword", UtilMisc.toMap("productId", product.getString("productId"), "keyword", entry.getKey(), "relevancyWeight", entry.getValue()));
            toBeStored.add(productKeyword);
        }
        if (toBeStored.size() > 0) {
            if (Debug.verboseOn()) Debug.logVerbose("[KeywordSearch.induceKeywords] Storing " + toBeStored.size() + " keywords for productId " + product.getString("productId"), module);
            delegator.storeAll(toBeStored);
        }
    }
    
    public static void addWeightedDataResourceString(GenericValue drView, int weight, List strings, GenericDelegator delegator) {
        try {
            String contentText = DataResourceWorker.renderDataResourceAsText(delegator, drView.getString("dataResourceId"), null, drView, null, null);
            for (int i = 0; i < weight; i++) {
                strings.add(contentText);
            }
        } catch (IOException e1) {
            Debug.logError(e1, "Error getting content text to index", module);
        } catch (GeneralException e1) {
            Debug.logError(e1, "Error getting content text to index", module);
        }
    }

    public static void addWeightedKeywordSourceString(GenericValue value, String fieldName, List strings) {
        if (value.getString(fieldName) != null) {
            int weight = 1;

            try {
                weight = Integer.parseInt(UtilProperties.getPropertyValue("prodsearch", "index.weight." + value.getEntityName() + "." + fieldName, "1"));
            } catch (Exception e) {
                Debug.logWarning("Could not parse weight number: " + e.toString(), module);
            }

            for (int i = 0; i < weight; i++) {
                strings.add(value.getString(fieldName));
            }
        }
    }
}
