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
package org.ofbiz.product.category.ftl;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.StringUtil;
import org.ofbiz.base.util.StringUtil.StringWrapper;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.common.UrlServletHelper;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.product.category.CatalogUrlServlet;
import org.ofbiz.product.category.CategoryContentWrapper;
import org.ofbiz.product.category.CategoryWorker;
import org.ofbiz.product.category.UrlRegexpConfigUtil;
import org.ofbiz.product.category.UrlUtil;
import org.ofbiz.product.product.ProductContentWrapper;

import freemarker.core.Environment;
import freemarker.ext.beans.BeanModel;
import freemarker.ext.beans.StringModel;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateTransformModel;

public class CatalogUrlSeoTransform implements TemplateTransformModel {
    public final static String module = CatalogUrlSeoTransform.class.getName();
    
    private static Map<String, String> m_categoryNameIdMap = null;
    private static Map<String, String> m_categoryIdNameMap = null;
    private static boolean m_categoryMapInitialed = false;
    private static final String m_asciiRegexp = "^[0-9-_a-zA-Z]*$";
    private static Perl5Compiler m_perlCompiler = new Perl5Compiler();
    private static Pattern m_asciiPattern = null;
    public static final String URL_HYPHEN = "-";

    static {
        if (!UrlRegexpConfigUtil.isInitialed()) {
            UrlRegexpConfigUtil.init();
        }
        try {
            m_asciiPattern = m_perlCompiler.compile(m_asciiRegexp, Perl5Compiler.DEFAULT_MASK);
        } catch (MalformedPatternException e1) {
            // do nothing
        }
    }
    
    @SuppressWarnings("unchecked")
    public String getStringArg(Map args, String key) {
        Object o = args.get(key);
        if (o instanceof SimpleScalar) {
            return ((SimpleScalar) o).getAsString();
        } else if (o instanceof StringModel) {
            return ((StringModel) o).getAsString();
        }
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Writer getWriter(final Writer out, final Map args)
            throws TemplateModelException, IOException {
        final StringBuilder buf = new StringBuilder();
        
        return new Writer(out) {
            
            @Override
            public void write(char[] cbuf, int off, int len) throws IOException {
                buf.append(cbuf, off, len);
            }
            
            @Override
            public void flush() throws IOException {
                out.flush();
            }
            
            @Override
            public void close() throws IOException {
                try {
                    Environment env = Environment.getCurrentEnvironment();
                    BeanModel req = (BeanModel) env.getVariable("request");
                    if (req != null) {
                        String productId = getStringArg(args, "productId");
                        String currentCategoryId = getStringArg(args, "currentCategoryId");
                        String previousCategoryId = getStringArg(args, "previousCategoryId");
                        HttpServletRequest request = (HttpServletRequest) req.getWrappedObject();
                        
                        if (!isCategoryMapInitialed()) {
                            initCategoryMap(request);
                        }

                        String catalogUrl = "";
                        if (UrlRegexpConfigUtil.isCategoryUrlEnabled(request.getContextPath())) {
                            if (UtilValidate.isEmpty(productId)) {
                                catalogUrl = makeCategoryUrl(request, currentCategoryId, previousCategoryId, null, null, null, null);
                            } else {
                                catalogUrl = makeProductUrl(request, productId, currentCategoryId, previousCategoryId);
                            }
                        } else {
                            catalogUrl = CatalogUrlServlet.makeCatalogUrl(request, productId, currentCategoryId, previousCategoryId);
                        }
                        out.write(catalogUrl);
                    }
                } catch (TemplateModelException e) {
                    throw new IOException(e.getMessage());
                }
            }
        };
    }
    
    /**
     * Check whether the category map is initialed.
     * 
     * @return a boolean value to indicate whether the category map has been initialized.
     */
    public static boolean isCategoryMapInitialed() {
        return m_categoryMapInitialed;
    }
    
    /**
     * Get the category name/id map.
     * 
     * @return the category name/id map
     */
    public static Map<String, String> getCategoryNameIdMap() {
        return m_categoryNameIdMap;
    }
    
    /**
     * Get the category id/name map.
     * 
     * @return the category id/name map
     */
    public static Map<String, String> getCategoryIdNameMap() {
        return m_categoryIdNameMap;
    }
    
    /**
     * Initial category-name/category-id map.
     * Note: as a key, the category-name should be:
     *         1. ascii
     *         2. lower cased and use hyphen between the words.
     *       If not, the category id will be used.
     * 
     */
    public static synchronized void initCategoryMap(HttpServletRequest request) {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        initCategoryMap(request, delegator);
    }
    
    public static synchronized void initCategoryMap(HttpServletRequest request, Delegator delegator) {
        if (UrlRegexpConfigUtil.checkCategoryUrl()) {
            m_categoryNameIdMap = FastMap.newInstance();
            m_categoryIdNameMap = FastMap.newInstance();
            Perl5Matcher matcher = new Perl5Matcher();

            try {
                Collection<GenericValue> allCategories = delegator.findList("ProductCategory", null, UtilMisc.toSet("productCategoryId", "categoryName"), null, null, false);
                for (GenericValue category : allCategories) {
                    String categoryName = category.getString("categoryName");
                    String categoryNameId = null;
                    String categoryIdName = null;
                    String categoryId = category.getString("productCategoryId");
                    if (UtilValidate.isNotEmpty(categoryName)) {
                        categoryName = UrlUtil.replaceSpecialCharsUrl(categoryName.trim().toLowerCase());
                        if (matcher.matches(categoryName, m_asciiPattern)) {
                            categoryIdName = categoryName.toLowerCase().replaceAll(" ", URL_HYPHEN);
                            categoryNameId = categoryIdName + URL_HYPHEN + categoryId.trim().toLowerCase().replaceAll(" ", URL_HYPHEN);
                        } else {
                            categoryIdName = categoryId.trim().toLowerCase().replaceAll(" ", URL_HYPHEN);
                            categoryNameId = categoryIdName;
                        }
                    } else {
                        GenericValue productCategory = delegator.findOne("ProductCategory", UtilMisc.toMap("productCategoryId", categoryId), true);
                        CategoryContentWrapper wrapper = new CategoryContentWrapper(productCategory, request);
                        StringWrapper alternativeUrl = wrapper.get("ALTERNATIVE_URL");
                        if (UtilValidate.isNotEmpty(alternativeUrl) && UtilValidate.isNotEmpty(alternativeUrl.toString())) {
                            categoryIdName = UrlUtil.replaceSpecialCharsUrl(alternativeUrl.toString());
                            categoryNameId = categoryIdName + URL_HYPHEN + categoryId.trim().toLowerCase().replaceAll(" ", URL_HYPHEN);
                        } else {
                            categoryNameId = categoryId.trim().toLowerCase().replaceAll(" ", URL_HYPHEN);
                            categoryIdName = categoryNameId;
                        }
                    }
                    if (m_categoryNameIdMap.containsKey(categoryNameId)) {
                        categoryNameId = categoryId.trim().toLowerCase().replaceAll(" ", URL_HYPHEN);
                        categoryIdName = categoryNameId;
                    }
                    if (!matcher.matches(categoryNameId, m_asciiPattern) || m_categoryNameIdMap.containsKey(categoryNameId)) {
                        continue;
                    }
                    m_categoryNameIdMap.put(categoryNameId, categoryId);
                    m_categoryIdNameMap.put(categoryId, categoryIdName);
                }
            } catch (GenericEntityException e) {
                Debug.logError(e, module);
            }
        }
        m_categoryMapInitialed = true;
    }

    /**
     * Make product url according to the configurations.
     * 
     * @return String a catalog url
     */
    public static String makeProductUrl(HttpServletRequest request, String productId, String currentCategoryId, String previousCategoryId) {
        Delegator delegator = (Delegator) request.getAttribute("delegator");
        if (!isCategoryMapInitialed()) {
            initCategoryMap(request);
        }

        String contextPath = request.getContextPath();
        StringBuilder urlBuilder = new StringBuilder();
        GenericValue product = null;
        urlBuilder.append((request.getSession().getServletContext()).getContextPath());
        if (urlBuilder.charAt(urlBuilder.length() - 1) != '/') {
            urlBuilder.append("/");
        }
        if (UtilValidate.isNotEmpty(productId)) {
            try {
                product = delegator.findOne("Product", UtilMisc.toMap("productId", productId), true);
            } catch (GenericEntityException e) {
                Debug.logError(e, "Error looking up product info for productId [" + productId + "]: " + e.toString(), module);
            }
        }
        if (product != null) {
            urlBuilder.append(CatalogUrlServlet.PRODUCT_REQUEST + "/");
        }

        if (UtilValidate.isNotEmpty(currentCategoryId)) {
            List<String> trail = CategoryWorker.getTrail(request);
            trail = CategoryWorker.adjustTrail(trail, currentCategoryId, previousCategoryId);
            if (!UrlRegexpConfigUtil.isCategoryUrlEnabled(contextPath)) {
                for (String trailCategoryId: trail) {
                    if ("TOP".equals(trailCategoryId)) continue;
                    urlBuilder.append("/");
                    urlBuilder.append(trailCategoryId);
                }
            } else {
                if (trail.size() > 1) {
                    String lastCategoryId = trail.get(trail.size() - 1);
                    if (!"TOP".equals(lastCategoryId)) {
                        if (UrlRegexpConfigUtil.isCategoryNameEnabled()) {
                            String categoryName = CatalogUrlSeoTransform.getCategoryIdNameMap().get(lastCategoryId);
                            if (UtilValidate.isNotEmpty(categoryName)) {
                                urlBuilder.append(categoryName);
                                if (product != null) {
                                    urlBuilder.append(URL_HYPHEN);
                                }
                            }
                        }
                    }
                }
            }
        }

        if (UtilValidate.isNotEmpty(productId)) {
            if (product != null) {
                String productName = product.getString("productName");
                productName = UrlUtil.replaceSpecialCharsUrl(productName);
                if (UtilValidate.isNotEmpty(productName)) {
                    urlBuilder.append(productName + URL_HYPHEN);
                } else {
                    ProductContentWrapper wrapper = new ProductContentWrapper(product, request);
                    StringWrapper alternativeUrl = wrapper.get("ALTERNATIVE_URL");
                    if (UtilValidate.isNotEmpty(alternativeUrl) && UtilValidate.isNotEmpty(alternativeUrl.toString())) {
                        productName = UrlUtil.replaceSpecialCharsUrl(alternativeUrl.toString());
                        if (UtilValidate.isNotEmpty(productName)) {
                            urlBuilder.append(productName + URL_HYPHEN);
                        }
                    }
                }
            }
            try {
                UrlRegexpConfigUtil.addSpecialProductId(productId);
                urlBuilder.append(productId.toLowerCase());
            } catch (Exception e) {
                urlBuilder.append(productId);
            }
        }
        
        if (!urlBuilder.toString().endsWith("/") && UtilValidate.isNotEmpty(UrlRegexpConfigUtil.getCategoryUrlSuffix())) {
            urlBuilder.append(UrlRegexpConfigUtil.getCategoryUrlSuffix());
        }
        
        return urlBuilder.toString();
    }

    /**
     * Make category url according to the configurations.
     * 
     * @return String a category url
     */
    public static String makeCategoryUrl(HttpServletRequest request, String currentCategoryId, String previousCategoryId, String viewSize, String viewIndex, String viewSort, String searchString) {

        if (!isCategoryMapInitialed()) {
            initCategoryMap(request);
        }

        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append((request.getSession().getServletContext()).getContextPath());
        if (urlBuilder.charAt(urlBuilder.length() - 1) != '/') {
            urlBuilder.append("/");
        }
        urlBuilder.append(CatalogUrlServlet.CATEGORY_REQUEST + "/");

        if (UtilValidate.isNotEmpty(currentCategoryId)) {
            List<String> trail = CategoryWorker.getTrail(request);
            trail = CategoryWorker.adjustTrail(trail, currentCategoryId, previousCategoryId);
            if (trail.size() > 1) {
                String lastCategoryId = trail.get(trail.size() - 1);
                if (!"TOP".equals(lastCategoryId)) {
                    String categoryName = CatalogUrlSeoTransform.getCategoryIdNameMap().get(lastCategoryId);
                    if (UtilValidate.isNotEmpty(categoryName)) {
                        urlBuilder.append(categoryName);
                        urlBuilder.append(URL_HYPHEN);
                        urlBuilder.append(lastCategoryId.trim().toLowerCase().replaceAll(" ", URL_HYPHEN));
                    } else {
                        urlBuilder.append(lastCategoryId.trim().toLowerCase().replaceAll(" ", URL_HYPHEN));
                    }
                }
            }
        }

        if (!urlBuilder.toString().endsWith("/") && UtilValidate.isNotEmpty(UrlRegexpConfigUtil.getCategoryUrlSuffix())) {
            urlBuilder.append(UrlRegexpConfigUtil.getCategoryUrlSuffix());
        }
        
        // append view index
        if (UtilValidate.isNotEmpty(viewIndex)) {
            if (!urlBuilder.toString().endsWith("?") && !urlBuilder.toString().endsWith("&")) {
                urlBuilder.append("?");
            }
            urlBuilder.append("viewIndex=" + viewIndex + "&");
        }
        // append view size
        if (UtilValidate.isNotEmpty(viewSize)) {
            if (!urlBuilder.toString().endsWith("?") && !urlBuilder.toString().endsWith("&")) {
                urlBuilder.append("?");
            }
            urlBuilder.append("viewSize=" + viewSize + "&");
        }
        // append view sort
        if (UtilValidate.isNotEmpty(viewSort)) {
            if (!urlBuilder.toString().endsWith("?") && !urlBuilder.toString().endsWith("&")) {
                urlBuilder.append("?");
            }
            urlBuilder.append("viewSort=" + viewSort + "&");
        }
        // append search string
        if (UtilValidate.isNotEmpty(searchString)) {
            if (!urlBuilder.toString().endsWith("?") && !urlBuilder.toString().endsWith("&")) {
                urlBuilder.append("?");
            }
            urlBuilder.append("searchString=" + searchString + "&");
        }
        if (urlBuilder.toString().endsWith("&")) {
            return urlBuilder.toString().substring(0, urlBuilder.toString().length()-1);
        }
        
        return urlBuilder.toString();
    }

    /**
     * Make product url according to the configurations.
     * 
     * @return String a catalog url
     */
    public static String makeProductUrl(String contextPath, List<String> trail, String productId, String productName, String currentCategoryId, String previousCategoryId) {
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(contextPath);
        if (urlBuilder.charAt(urlBuilder.length() - 1) != '/') {
            urlBuilder.append("/");
        }
        if (!UrlRegexpConfigUtil.isCategoryUrlEnabled(contextPath)) {
            urlBuilder.append(CatalogUrlServlet.CATALOG_URL_MOUNT_POINT);
        } else {
            urlBuilder.append(CatalogUrlServlet.PRODUCT_REQUEST + "/");
        }

        if (UtilValidate.isNotEmpty(currentCategoryId)) {
            trail = CategoryWorker.adjustTrail(trail, currentCategoryId, previousCategoryId);
            if (!UrlRegexpConfigUtil.isCategoryUrlEnabled(contextPath)) {
                for (String trailCategoryId: trail) {
                    if ("TOP".equals(trailCategoryId)) continue;
                    urlBuilder.append("/");
                    urlBuilder.append(trailCategoryId);
                }
            } else {
                if (trail.size() > 1) {
                    String lastCategoryId = trail.get(trail.size() - 1);
                    if (!"TOP".equals(lastCategoryId)) {
                        if (UrlRegexpConfigUtil.isCategoryNameEnabled()) {
                            String categoryName = CatalogUrlSeoTransform.getCategoryIdNameMap().get(lastCategoryId);
                            if (UtilValidate.isNotEmpty(categoryName)) {
                                urlBuilder.append(categoryName + URL_HYPHEN);
                            }
                        }
                    }
                }
            }
        }

        if (UtilValidate.isNotEmpty(productId)) {
            if (!UrlRegexpConfigUtil.isCategoryUrlEnabled(contextPath)) {
                urlBuilder.append("/p_");
            } else {
                productName = UrlUtil.replaceSpecialCharsUrl(productName);
                if (UtilValidate.isNotEmpty(productName)) {
                    urlBuilder.append(productName + URL_HYPHEN);
                }
            }
            urlBuilder.append(productId);
        }
        
        if (!urlBuilder.toString().endsWith("/") && UtilValidate.isNotEmpty(UrlRegexpConfigUtil.getCategoryUrlSuffix())) {
            urlBuilder.append(UrlRegexpConfigUtil.getCategoryUrlSuffix());
        }
        
        return urlBuilder.toString();
    }

    /**
     * Get a string lower cased and hyphen connected.
     * 
     * @param name a String to be transformed
     * @return String nice name
     */
    protected static String getNiceName(String name) {
        Perl5Matcher matcher = new Perl5Matcher();
        String niceName = null;
        if (UtilValidate.isNotEmpty(name)) {
            name = name.trim().toLowerCase().replaceAll(" ", URL_HYPHEN);
            if (UtilValidate.isNotEmpty(name) && matcher.matches(name, m_asciiPattern)) {
                niceName = name;
            }
        }
        return niceName;
    }
    
    public static boolean forwardProductUri(HttpServletRequest request, HttpServletResponse response, Delegator delegator) throws ServletException, IOException {
        return forwardProductUri(request, response, delegator, null);
    }

    public static boolean forwardProductUri(HttpServletRequest request, HttpServletResponse response, Delegator delegator, String controlServlet) throws ServletException, IOException {
        return forwardUri(request, response, delegator, controlServlet);
    }

    /**
     * Forward a uri according to forward pattern regular expressions.
     * 
     * @param uri
     *            String to reverse transform
     * @return boolean to indicate whether the uri is forwarded.
     * @throws IOException 
     * @throws ServletException 
     */
    public static boolean forwardUri(HttpServletRequest request, HttpServletResponse response, Delegator delegator, String controlServlet) throws ServletException, IOException {
        String pathInfo = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (!isCategoryMapInitialed()) {
            initCategoryMap(request, delegator);
        }

        if (!UrlRegexpConfigUtil.isCategoryUrlEnabled(contextPath)) {
            return false;
        }
        List<String> pathElements = StringUtil.split(pathInfo, "/");
        if (UtilValidate.isEmpty(pathElements)) {
            return false;
        }
        if (pathInfo.startsWith("/" + CatalogUrlServlet.CATEGORY_REQUEST + "/")) {
            return forwardCategoryUri(request, response, delegator, controlServlet);
        }
        
        String lastPathElement = pathElements.get(pathElements.size() - 1);
        String categoryId = null;
        String productId = null;
        if (UtilValidate.isNotEmpty(lastPathElement)) {
            if (UtilValidate.isNotEmpty(UrlRegexpConfigUtil.getCategoryUrlSuffix())) {
                if (lastPathElement.endsWith(UrlRegexpConfigUtil.getCategoryUrlSuffix())) {
                    lastPathElement = lastPathElement.substring(0, lastPathElement.length() - UrlRegexpConfigUtil.getCategoryUrlSuffix().length());
                } else {
                    return false;
                }
            }
            if (UrlRegexpConfigUtil.isCategoryNameEnabled() || pathInfo.startsWith("/" + CatalogUrlServlet.CATEGORY_REQUEST + "/")) {
                for (String categoryName : m_categoryNameIdMap.keySet()) {
                    if (lastPathElement.startsWith(categoryName)) {
                        categoryId = m_categoryNameIdMap.get(categoryName);
                        if (!lastPathElement.equals(categoryName)) {
                            lastPathElement = lastPathElement.substring(categoryName.length() + URL_HYPHEN.length());
                        }
                        break;
                    }
                }
                if (UtilValidate.isEmpty(categoryId)) {
                    categoryId = lastPathElement;
                }
            }

            if (UtilValidate.isNotEmpty(lastPathElement)) {
                List<String> urlElements = StringUtil.split(lastPathElement, URL_HYPHEN);
                if (UtilValidate.isEmpty(urlElements)) {
                    try {
                        if (delegator.findOne("Product", UtilMisc.toMap("productId", lastPathElement), true) != null) {
                            productId = lastPathElement;
                        }
                    } catch (GenericEntityException e) {
                        Debug.logError(e, "Error looking up product info for ProductUrl with path info [" + pathInfo + "]: " + e.toString(), module);
                    }
                } else {
                    int i = urlElements.size() - 1;
                    String tempProductId = urlElements.get(i);
                    while (i >= 0) {
                        try {
                            List<EntityExpr> exprs = FastList.newInstance();
                            exprs.add(EntityCondition.makeCondition("productId", EntityOperator.EQUALS, lastPathElement));
                            if (UrlRegexpConfigUtil.isSpecialProductId(tempProductId)) {
                                exprs.add(EntityCondition.makeCondition("productId", EntityOperator.EQUALS, UrlRegexpConfigUtil.getSpecialProductId(tempProductId)));
                            } else {
                                exprs.add(EntityCondition.makeCondition("productId", EntityOperator.EQUALS, tempProductId.toUpperCase()));
                            }
                            List<GenericValue> products = delegator.findList("Product", EntityCondition.makeCondition(exprs, EntityOperator.OR), UtilMisc.toSet("productId", "productName"), null, null, true);
                            
                            if (products != null && products.size() > 0) {
                                if (products.size() == 1) {
                                    productId = products.get(0).getString("productId");
                                    break;
                                } else {
                                    productId = tempProductId;
                                    break;
                                }
                            } else if (i > 0) {
                                tempProductId = urlElements.get(i - 1) + URL_HYPHEN + tempProductId;
                            }
                        } catch (GenericEntityException e) {
                            Debug.logError(e, "Error looking up product info for ProductUrl with path info [" + pathInfo + "]: " + e.toString(), module);
                        }
                        i--;
                    }
                }
            }
        }

        if (UtilValidate.isNotEmpty(productId) || UtilValidate.isNotEmpty(categoryId)) {
            if (categoryId != null) {
                request.setAttribute("productCategoryId", categoryId);
            }

            if (productId != null) {
                request.setAttribute("product_id", productId);
                request.setAttribute("productId", productId);
            }

            StringBuilder urlBuilder = new StringBuilder();
            if (UtilValidate.isNotEmpty(controlServlet)) {
                urlBuilder.append("/" + controlServlet);
            }
            urlBuilder.append("/" + (productId != null ? CatalogUrlServlet.PRODUCT_REQUEST : CatalogUrlServlet.CATEGORY_REQUEST));
            UrlServletHelper.setViewQueryParameters(request, urlBuilder);
            Debug.logInfo("[Filtered request]: " + pathInfo + " (" + urlBuilder + ")", module);
            RequestDispatcher rd = request.getRequestDispatcher(urlBuilder.toString());
            rd.forward(request, response);
            return true;
        }
        return false;
    }

    /**
     * Forward a category uri according to forward pattern regular expressions.
     * 
     * @param uri
     *            String to reverse transform
     * @return String
     * @throws IOException 
     * @throws ServletException 
     */
    public static boolean forwardCategoryUri(HttpServletRequest request, HttpServletResponse response, Delegator delegator, String controlServlet) throws ServletException, IOException {
        String pathInfo = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (!isCategoryMapInitialed()) {
            initCategoryMap(request);
        }
        if (!UrlRegexpConfigUtil.isCategoryUrlEnabled(contextPath)) {
            return false;
        }
        List<String> pathElements = StringUtil.split(pathInfo, "/");
        if (UtilValidate.isEmpty(pathElements)) {
            return false;
        }
        String lastPathElement = pathElements.get(pathElements.size() - 1);
        String categoryId = null;
        if (UtilValidate.isNotEmpty(lastPathElement)) {
            if (UtilValidate.isNotEmpty(UrlRegexpConfigUtil.getCategoryUrlSuffix())) {
                if (lastPathElement.endsWith(UrlRegexpConfigUtil.getCategoryUrlSuffix())) {
                    lastPathElement = lastPathElement.substring(0, lastPathElement.length() - UrlRegexpConfigUtil.getCategoryUrlSuffix().length());
                } else {
                    return false;
                }
            }
            for (String categoryName : m_categoryNameIdMap.keySet()) {
                if (lastPathElement.startsWith(categoryName)) {
                    categoryId = m_categoryNameIdMap.get(categoryName);
                    break;
                }
            }
            if (UtilValidate.isEmpty(categoryId)) {
                categoryId = lastPathElement.trim();
            }
        }
        if (UtilValidate.isNotEmpty(categoryId)) {
            request.setAttribute("productCategoryId", categoryId);
            StringBuilder urlBuilder = new StringBuilder();
            if (UtilValidate.isNotEmpty(controlServlet)) {
                urlBuilder.append("/" + controlServlet);
            }
            urlBuilder.append("/" + CatalogUrlServlet.CATEGORY_REQUEST);
            UrlServletHelper.setViewQueryParameters(request, urlBuilder);
            Debug.logInfo("[Filtered request]: " + pathInfo + " (" + urlBuilder + ")", module);
            RequestDispatcher rd = request.getRequestDispatcher(urlBuilder.toString());
            rd.forward(request, response);
            return true;
        }
        return false;
    }

}