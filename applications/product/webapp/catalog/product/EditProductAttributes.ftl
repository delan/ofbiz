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
 *@author     Brad Steiner (bsteiner@thehungersite.com)
 *@author     Catherine.Heintz@nereide.biz (migration to UiLabel)
 *@version    $Rev$
 *@since      2.2
-->
<#assign uiLabelMap = requestAttributes.uiLabelMap>
<#if hasPermission>

${pages.get("/product/ProductTabBar.ftl")}
    
    <div class="head1">${uiLabelMap.ProductAttributes} <span class="head2">${uiLabelMap.CommonFor} <#if product?exists>${(product.internalName)?if_exists}</#if> [${uiLabelMap.CommonId}:${productId?if_exists}]</span></div>
    
    <a href="<@ofbizUrl>/EditProduct</@ofbizUrl>" class="buttontext">[${uiLabelMap.ProductNewProduct}]</a>
    <#if productId?has_content>
    <a href="/ecommerce/control/product?product_id=${productId}" class="buttontext" target="_blank">[${uiLabelMap.ProductProductPage}]</a>
    </#if>
    <p>    
    <#if productId?exists && product?exists>
        <table border="1" cellpadding="2" cellspacing="0">
        <tr>
            <td><div class="tabletext"><b>${uiLabelMap.ProductName}</b></div></td>
            <td><div class="tabletext"><b>${uiLabelMap.ProductValueType}</b></div></td>
        </tr>
        <#list productAttributes as productAttribute>
        <tr valign="middle">
            <td><div class="tabletext">${(productAttribute.attrName)?if_exists}</div></td>
            <td>
                <FORM method=POST action="<@ofbizUrl>/UpdateProductAttribute?UPDATE_MODE=UPDATE</@ofbizUrl>">
                    <input type=hidden name="PRODUCT_ID" value="${(productAttribute.productId)?if_exists}">
                    <input type=hidden name="ATTRIBUTE_NAME" value="${(productAttribute.attrName)?if_exists}">
                    <input type="text" class="inputBox" size="50" name="ATTRIBUTE_VALUE" value="${(productAttribute.attrValue)?if_exists}">
                    <input type="text" class="inputBox" size="15" name="ATTRIBUTE_TYPE" value="${(productAttribute.attrType)?if_exists}">
                    <INPUT type=submit value="Update">
                </FORM>
            </td>
            <td>
            <a href="<@ofbizUrl>/UpdateProductAttribute?UPDATE_MODE=DELETE&PRODUCT_ID=${(productAttribute.productId)?if_exists}&ATTRIBUTE_NAME=${(productAttribute.attrName)?if_exists}</@ofbizUrl>" class="buttontext">
            [${uiLabelMap.CommonDelete}]</a>
            </td>
        </tr>
        </#list>
        </table>
        <br>
        <form method="POST" action="<@ofbizUrl>/UpdateProductAttribute</@ofbizUrl>" style="margin: 0;">
        <input type="hidden" name="PRODUCT_ID" value="${productId}">
        <input type="hidden" name="UPDATE_MODE" value="CREATE">
        <input type="hidden" name="useValues" value="true">
        
        <div class="head2">${uiLabelMap.ProductAddProductAttributeNameValueType}:</div>
        <br>
        <input type="text" class="inputBox" name="ATTRIBUTE_NAME" size="15">&nbsp;
        <input type="text" class="inputBox" name="ATTRIBUTE_VALUE" size="50">&nbsp;
        <input type="text" class="inputBox" name="ATTRIBUTE_TYPE" size="15">&nbsp;
        <input type="submit" value="Add">
        </form>
    </#if>
<#else>
  <h3>${uiLabelMap.ProductCatalogViewPermissionError}</h3>
</#if>
