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
package org.ofbiz.content.search;

import java.lang.Object;
import java.lang.String;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilProperties;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.ServiceUtil;
import org.ofbiz.service.LocalDispatcher;

/**
 * SearchServices Class
 */
public class SearchServices {

    public static final String module = SearchServices.class.getName();
    public static final String resource = "ContentUiLabels";

    public static Map<String, Object> indexTree(DispatchContext dctx, Map<String, ? extends Object> context) {
        Date start = new Date();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        Delegator delegator = dctx.getDelegator();
        String siteId = (String) context.get("contentId");
        Locale locale = (Locale) context.get("locale");
        Map<String, Object> envContext = new HashMap<String, Object>();

        if (Debug.infoOn()) Debug.logInfo("in indexTree, siteId:" + siteId, module);
        List<String> badIndexList = new ArrayList<String>();
        envContext.put("badIndexList", badIndexList);
        envContext.put("goodIndexCount", Integer.valueOf(0));

        Map<String, Object> results;
        try {
            results = SearchWorker.indexTree(dispatcher, delegator, siteId, envContext);
        } catch (Exception e) {
            Debug.logError(e, module);
            return ServiceUtil.returnError(UtilProperties.getMessage(resource,
                    "ContentIndexingTreeError", UtilMisc.toMap("errorString", e.toString()), locale));
        }
        Date end = new Date();
        if (Debug.infoOn()) Debug.logInfo("in indexTree, results:" + results, module);
        if (Debug.infoOn()) Debug.logInfo("Indexing done in: " + (end.getTime()-start.getTime()) + " ms", module);
        return results;
    }

    public static Map<String, Object> indexProduct(DispatchContext dctx, Map<String, ? extends Object> context) {
        Delegator delegator = dctx.getDelegator();
        String productId = (String) context.get("productId");
        ProductIndexer indexer = ProductIndexer.getInstance(delegator);
        indexer.queue(productId);
        return ServiceUtil.returnSuccess();
    }

    public static Map<String, Object> indexProductsFromFeature(DispatchContext dctx, Map<String, ? extends Object> context) {
        Delegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        try {
            List<GenericValue> productFeatureAppls = delegator.findByAnd("ProductFeatureAppl", UtilMisc.toMap("productFeatureId", context.get("productFeatureId")), null, false);
            // Only re-index the active appls, future dated ones will get picked up on that product's re-index date
            productFeatureAppls = EntityUtil.filterByDate(productFeatureAppls);

            for (GenericValue productFeatureAppl : productFeatureAppls) {
                try {
                    dispatcher.runSync("indexProduct", UtilMisc.toMap("productId", productFeatureAppl.get("productId")));
                } catch (GenericServiceException e) {
                    Debug.logError(e, module);
                }
            }

        } catch (GenericEntityException e) {
            Debug.logError(e, module);
        }
        return ServiceUtil.returnSuccess();
    }

    public static Map<String, Object> indexProductsFromProductAssoc(DispatchContext dctx, Map<String, ? extends Object> context) {
        LocalDispatcher dispatcher = dctx.getDispatcher();
        try {
            dispatcher.runSync("indexProduct", UtilMisc.toMap("productId", context.get("productId")));
            dispatcher.runSync("indexProduct", UtilMisc.toMap("productId", context.get("productIdTo")));
        } catch (GenericServiceException e) {
            Debug.logError(e, module);
        }
        return ServiceUtil.returnSuccess();
    }

    public static Map<String, Object> indexProductsFromDataResource(DispatchContext dctx, Map<String, ? extends Object> context) {
        Delegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        try {
            List<GenericValue> contents = delegator.findByAnd("Content", UtilMisc.toMap("dataResourceId", context.get("dataResourceId")), null, false);
            for (GenericValue content : contents) {
                dispatcher.runSync("indexProductsFromContent",
                        UtilMisc.toMap(
                                "userLogin", context.get("userLogin"),
                                "contentId", content.get("contentId")));
            }
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
        } catch (GenericServiceException e) {
            Debug.logError(e, module);
        }
        return ServiceUtil.returnSuccess();
    }
    public static Map<String, Object> indexProductsFromContent(DispatchContext dctx, Map<String, ? extends Object> context) {
        Delegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        try {
            List<GenericValue> productContents = delegator.findByAnd("ProductContent", UtilMisc.toMap("contentId", context.get("contentId")), null, false);
            for (GenericValue productContent : productContents) {
                try {
                    dispatcher.runSync("indexProduct", UtilMisc.toMap("productId", productContent.get("productId")));
                } catch (GenericServiceException e) {
                    Debug.logError(e, module);
                }
            }
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
        }
        return ServiceUtil.returnSuccess();
    }
    public static Map<String, Object> indexProductsFromCategory(DispatchContext dctx, Map<String, Object> context) {
        Delegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        try {
            String productCategoryId = (String) context.get("productCategoryId");
            indexProductCategoryMembers(productCategoryId, delegator, dispatcher);
            indexProductCategoryRollup(productCategoryId, delegator, dispatcher, UtilMisc.<String>toSet(productCategoryId));
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
        }
        return ServiceUtil.returnSuccess();
    }
    private static void indexProductCategoryRollup(String parentProductCategoryId, Delegator delegator, LocalDispatcher dispatcher, Set<String> excludeProductCategoryIds) throws GenericEntityException {
        List<GenericValue> productCategoryRollups = delegator.findByAnd("ProductCategoryRollup", UtilMisc.toMap("parentProductCategoryId", parentProductCategoryId), null, false);
        for (GenericValue productCategoryRollup : productCategoryRollups) {
            String productCategoryId = productCategoryRollup.getString("productCategoryId");
            // Avoid infinite recursion
            if (!excludeProductCategoryIds.add(productCategoryId)) {
                continue;
            }
            indexProductCategoryMembers(productCategoryId, delegator, dispatcher);
            indexProductCategoryRollup(productCategoryId, delegator, dispatcher, excludeProductCategoryIds);
        }
    }

    private static void indexProductCategoryMembers(String productCategoryId, Delegator delegator, LocalDispatcher dispatcher) throws GenericEntityException {
        List<GenericValue> productCategoryMembers = delegator.findByAnd("ProductCategoryMember", UtilMisc.toMap("productCategoryId", productCategoryId), null, false);
        for (GenericValue productCategoryMember : productCategoryMembers) {
            try {
                dispatcher.runSync("indexProduct", UtilMisc.toMap("productId", productCategoryMember.get("productId")));
            } catch (GenericServiceException e) {
                Debug.logError(e, module);
            }
        }
    }

}
