<#--
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
 *
 *@author     David E. Jones (jonesde@ofbiz.org)
 *@author     Catherine Heintz (catherine.heintz@nereide.biz)
 *@version    $Rev$
 *@since      2.1
-->

<#assign uiLabelMap = requestAttributes.uiLabelMap>
<#if hasPermission>

<#if prodCatalogId?has_content>
  <div class='tabContainer'>
  <a href="<@ofbizUrl>/EditProdCatalog?prodCatalogId=${prodCatalogId}</@ofbizUrl>" class="tabButton">${uiLabelMap.ProductCatalog}</a>
  <a href="<@ofbizUrl>/EditProdCatalogStores?prodCatalogId=${prodCatalogId}</@ofbizUrl>" class="tabButtonSelected">${uiLabelMap.ProductStores}</a>
  <a href="<@ofbizUrl>/EditProdCatalogParties?prodCatalogId=${prodCatalogId}</@ofbizUrl>" class="tabButton">${uiLabelMap.PartyParties}</a>
  <a href="<@ofbizUrl>/EditProdCatalogCategories?prodCatalogId=${prodCatalogId}</@ofbizUrl>" class="tabButton">${uiLabelMap.ProductCategories}</a>
  </div>
</#if>

<div class="head1">${uiLabelMap.ProductStoresFor} <span class='head2'> <#if (prodCatalog.catalogName)?has_content>"${prodCatalog.catalogName}"</#if> [${uiLabelMap.CommonId}:${prodCatalogId?if_exists}]</span></div>
<a href="<@ofbizUrl>/EditProdCatalog</@ofbizUrl>" class="buttontext">[${uiLabelMap.ProductNewProdCatalog}]</a>
<br>
<br>
<#if prodCatalogId?has_content>
${updateProductStoreCatalogWrapper.renderFormString()}
<br>
${createProductStoreCatalogWrapper.renderFormString()}
</#if>
<br>

<#else>
  <h3>${uiLabelMap.ProductCatalogViewPermissionError}</h3>
</#if>
