/*
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
 *
 *@author     Andy Zeneski
 *@version    $Revision$
 *@since      2.1
*/

importPackage(Packages.java.lang);
importPackage(Packages.org.ofbiz.core.util);
importPackage(Packages.org.ofbiz.core.entity);
importPackage(Packages.org.ofbiz.core.service);
importPackage(Packages.org.ofbiz.commonapp.product.catalog);

var dispatcher = request.getAttribute("dispatcher");
var delegator = request.getAttribute("delegator");
var contentPathPrefix = CatalogWorker.getContentPathPrefix(request);

var detailTemplate = "/catalog/productdetail.ftl";
var productId = request.getParameter("product_id");
if (productId != null) {
    request.setAttribute("productId", productId);
}

// get the product entity
if (productId != null) {
    var product = delegator.findByPrimaryKeyCache("Product", UtilMisc.toMap("productId", productId));
    if (product != null) {
        request.setAttribute("product", product);    
        var productTemplate = product.getString("detailTemplate");
        if (productTemplate != null && productTemplate.length() > 0) {
            detailTempalte = productTemplate;
        }
    }
}
       
//  check the catalog's template path and update
var templatePathPrefix = CatalogWorker.getTemplatePathPrefix(request);
if (templatePathPrefix != null) {
    detailTemplate = templatePathPrefix + detailTemplate;
}    
    
// set the template for the view
request.setAttribute("detailTemplate", detailTemplate);
    