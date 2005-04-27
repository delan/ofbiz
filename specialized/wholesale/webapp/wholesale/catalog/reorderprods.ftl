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
 *@author     Andy Zeneski (jaz@ofbiz.org)
 *@version    $Revision$
 *@since      2.1
-->
<#if (requestAttributes.uiLabelMap)?exists><#assign uiLabelMap = requestAttributes.uiLabelMap></#if>
<#if reorderProducts?has_content>
  <table border="0" cellspacing='0' cellpadding='0' class='boxoutside'>
    <tr>
      <td width='100%'>
        <table border='0' cellspacing='0' cellpadding='0' class='boxtop'>
          <tr>
            <td valign="middle" align="left">
              <div class="boxhead">${uiLabelMap.ProductQuickReorder}</div>
            </td>
          </tr>
        </table>
      </td>
    </tr>
    <tr>
      <td width='100%'>
        <table border='0' cellspacing='0' cellpadding='0' class='boxbottom'>
          <tr>
            <td>
              <table cellspacing="0" cellpadding="0" border="0">
                <tr>
                  <td valign="top">
                    <div class="tabletext"><b>
                      ${requestAttributes.uiLabelMap.CommonPicture}
                    </b></div>
                  </td>
                  <td valign="top">
                    <div class="tabletext"><b>
                      ${requestAttributes.uiLabelMap.EcommerceItem}
                    </b></div>
                  </td>
                  <td valign="top">
                    <div class="tabletext"><b>
                      ${requestAttributes.uiLabelMap.ProductInStock}
                    </b></div>
                  </td>
                  <td valign="top">
                    <div class="tabletext"><b>
                      ${requestAttributes.uiLabelMap.EcommercePrice}
                    </b></div>
                  </td>
                  <td valign="top">
                    <div class="tabletext"><b>
                      ${requestAttributes.uiLabelMap.CommonQuantity}
                    </b></div>
                  </td>
                </tr>
                <tr><td colspan="5"><hr class='sepbar'></td></tr>
                <#list reorderProducts as miniProduct>
                      ${setRequestAttribute("miniProdQuantity", reorderQuantities.get(miniProduct.productId))}
                      ${setRequestAttribute("miniProdFormName", "theminireorderprod" + miniProduct_index + "form")}
                      ${setRequestAttribute("optProductId", miniProduct.productId)}
                      ${setRequestAttribute("listIndex", miniProduct_index)}     
                      <#if pages?exists>${pages.get("/catalog/reordersummary.ftl")}</#if>
					  <#if screens?exists>${screens.render("component://wholesale/widget/CatalogScreens.xml#reordersummary")}</#if>
                  <#if miniProduct_has_next>
                    <tr><td colspan="5"><hr class='sepbar'></td></tr>
                  </#if>
                </#list>
              </table>
            </td>
          </tr>
        </table>
      </td>
    </tr>
  </table>
  <br/>
</#if>

