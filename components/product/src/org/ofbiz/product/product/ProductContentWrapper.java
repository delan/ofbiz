/*
 * $Id: ProductContentWrapper.java,v 1.2 2003/12/20 01:17:22 jonesde Exp $
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
package org.ofbiz.product.product;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilHttp;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.content.content.ContentWorker;
import org.ofbiz.entity.GenericDelegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.model.ModelEntity;
import org.ofbiz.entity.model.ModelUtil;
import org.ofbiz.entity.util.EntityUtil;

/**
 * Product Content Worker: gets product content to display
 *
 * @author     <a href="mailto:jonesde@ofbiz.org">David E. Jones</a>
 * @version    $Revision: 1.2 $
 * @since      3.0
 */
public class ProductContentWrapper {
    
    public static final String module = ProductContentWrapper.class.getName();
    
    protected GenericValue product;
    protected Locale locale;
    protected String mimeTypeId;
    
    public ProductContentWrapper(GenericValue product, Locale locale, String mimeTypeId) {
        this.product = product;
        this.locale = locale;
        this.mimeTypeId = mimeTypeId;
    }
    
    public ProductContentWrapper(GenericValue product, HttpServletRequest request) {
        this.product = product;
        this.locale = UtilHttp.getLocale(request);
        this.mimeTypeId = "text/html";
    }
    
    public String get(String productContentTypeId) {
        try {
            return getProductContentAsText(product, productContentTypeId, locale, mimeTypeId, product.getDelegator());
        } catch (GenericEntityException e) {
            Debug.logError(e, "Error rendering ProductContent, inserting empty String", module);
            return "";
        } catch (IOException e) {
            Debug.logError(e, "Error rendering ProductContent, inserting empty String", module);
            return "";
        }
    }
    
    public static String getProductContentAsText(GenericValue product, String productContentTypeId, HttpServletRequest request) throws GenericEntityException, IOException {
        return getProductContentAsText(product, productContentTypeId, UtilHttp.getLocale(request), "text/html", product.getDelegator());
    }

    public static String getProductContentAsText(GenericValue product, String productContentTypeId, Locale locale, String mimeTypeId, GenericDelegator delegator) throws GenericEntityException, IOException {
        Writer outWriter = new StringWriter();
        getProductContentAsText(null, product, productContentTypeId, locale, mimeTypeId, delegator, outWriter);
        return outWriter.toString();
    }
    
    public static void getProductContentAsText(String productId, GenericValue product, String productContentTypeId, Locale locale, String mimeTypeId, GenericDelegator delegator, Writer outWriter) throws GenericEntityException, IOException {
        if (productId == null && product != null) {
            productId = product.getString("productId");
        }
        
        if (UtilValidate.isEmpty(mimeTypeId)) {
            mimeTypeId = "text/html";
        }
        
        String candidateFieldName = ModelUtil.dbNameToVarName(productContentTypeId);
        //Debug.logInfo("candidateFieldName=" + candidateFieldName, module);
        ModelEntity productModel = delegator.getModelEntity("Product");
        if (productModel.isField(candidateFieldName)) {
            if (product == null) {
                product = delegator.findByPrimaryKeyCache("Product", UtilMisc.toMap("productId", productId));
            }
            if (product != null) {
                String candidateValue = product.getString(candidateFieldName);
                if (UtilValidate.isNotEmpty(candidateValue)) {
                    outWriter.write(candidateValue);
                }
            }
        }
        
        List productContentList = delegator.findByAndCache("ProductContent", UtilMisc.toMap("productId", productId, "productContentTypeId", productContentTypeId), UtilMisc.toList("-fromDate"));
        productContentList = EntityUtil.filterByDate(productContentList);
        GenericValue productContent = EntityUtil.getFirst(productContentList);
        
        if (productContent != null) {
            ContentWorker.renderContentAsText(delegator, productContent.getString("contentId"), outWriter, null, null, locale, mimeTypeId);
        }
    }
}

