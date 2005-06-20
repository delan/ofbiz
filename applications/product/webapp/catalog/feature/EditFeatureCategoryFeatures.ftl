<#--
 *  Copyright (c) 2003-2005 The Open For Business Project - www.ofbiz.org
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
 *@since      2.2
-->

<div class="head1">${uiLabelMap.ProductEditFeaturesForFeatureCategory} "${(curProductFeatureCategory.description)?if_exists}"</div>
<a href="<@ofbizUrl>/EditFeature?productFeatureCategoryId=${productFeatureCategoryId?if_exists}</@ofbizUrl>" class="buttontext">[${uiLabelMap.ProductCreateNewFeature}]</a>

<br/>
<p class="head2">${uiLabelMap.ProductProductFeatureMaintenance}</p>
<#if (listSize > 0)>
    <#if productId?has_content>
      <#assign productString = "&productId=" + productId>
    </#if>
    <table border="0" width="100%" cellpadding="2">
        <tr>
        <td align="right">
            <span class="tabletext">
            <b>
            <#if (viewIndex > 0)>
            <a href="<@ofbizUrl>/EditFeatureCategoryFeatures?productFeatureCategoryId=${productFeatureCategoryId?if_exists}&VIEW_SIZE=${viewSize}&VIEW_INDEX=${viewIndex-1}${productString?if_exists}</@ofbizUrl>" class="buttontext">[${uiLabelMap.CommonPrevious}]</a> |
            </#if>
            ${lowIndex+1} - ${highIndex} of ${listSize}
            <#if (listSize > highIndex)>
            | <a href="<@ofbizUrl>/EditFeatureCategoryFeatures?productFeatureCategoryId=${productFeatureCategoryId?if_exists}&VIEW_SIZE=${viewSize}&VIEW_INDEX=${viewIndex+1}${productString?if_exists}</@ofbizUrl>" class="buttontext">[${uiLabelMap.CommonNext}]</a>
            </#if>
            </b>
            </span>
        </td>
        </tr>
    </table>
</#if>
<table border="1" cellpadding='2' cellspacing='0'>
    <form method='POST' action='<@ofbizUrl>/UpdateProductFeatureInCategory</@ofbizUrl>' name="selectAllForm">
    <input type="hidden" name="_useRowSubmit" value="Y">
    <input type="hidden" name="_checkGlobalScope" value="N">
    <input type="hidden" name="productFeatureCategoryId" value="${productFeatureCategoryId}">
  <tr class='viewOneTR1'>
    <td><div class="tabletext"><b>${uiLabelMap.CommonId}</b></div></td>
    <td><div class="tabletext"><b>${uiLabelMap.CommonDescription}</b></div></td>
    <td><div class="tabletext"><b>${uiLabelMap.ProductFeatureType}</b></div></td>
    <td><div class="tabletext"><b>${uiLabelMap.ProductFeatureCategory}</b></div></td>
    <td><div class="tabletext"><b>${uiLabelMap.ProductUnitOfMeasureId}</b></div></td>
    <td><div class="tabletext"><b>${uiLabelMap.ProductQuantity}</b></div></td>
    <td><div class="tabletext"><b>${uiLabelMap.ProductAmount}</b></div></td>
    <td><div class="tabletext"><b>${uiLabelMap.ProductIdSeqNum}</b></div></td>
    <td><div class="tabletext"><b>${uiLabelMap.ProductIdCode}</b></div></td>
    <td><div class="tabletext"><b>${uiLabelMap.ProductAbbrev}</b></div></td>
    <td><div class="tabletext"><b>${uiLabelMap.CommonAll}<input type="checkbox" name="selectAll" value="${uiLabelMap.CommonY}" onclick="javascript:toggleAll(this);"></div></td>
  </tr>
<#if (listSize > 0)>
    <#assign rowCount = 0>
<#list productFeatures as productFeature>
  <#assign curProductFeatureType = productFeature.getRelatedOneCache("ProductFeatureType")>
  <tr valign="middle" class='viewOneTR1'>
      <input type="hidden" name="productFeatureId_o_${rowCount}" value="${productFeature.productFeatureId}">
      <td><a href="<@ofbizUrl>/EditFeature?productFeatureId=${productFeature.productFeatureId}</@ofbizUrl>" class="buttontext">${productFeature.productFeatureId}</a></td>
      <td><input type="text" class='inputBox' size='15' name="description_o_${rowCount}" value="${productFeature.description}"></td>
      <td><select name='productFeatureTypeId_o_${rowCount}' size="1" class='selectBox'>
        <#if productFeature.productFeatureTypeId?has_content>
          <option value='${productFeature.productFeatureTypeId}'><#if curProductFeatureType?exists>${curProductFeatureType.description?if_exists}<#else> [${productFeature.productFeatureTypeId}]</#if></option>
          <option value='${productFeature.productFeatureTypeId}'>---</option>
        </#if>
        <#list productFeatureTypes as productFeatureType>
          <option value='${productFeatureType.productFeatureTypeId}'>${productFeatureType.description?if_exists}</option>
        </#list>
      </select></td>
      <td><select name='productFeatureCategoryId_o_${rowCount}' size="1" class='selectBox'>
        <#if productFeature.productFeatureCategoryId?has_content>
          <#assign curProdFeatCat = productFeature.getRelatedOne("ProductFeatureCategory")>
          <option value='${productFeature.productFeatureCategoryId}'>${(curProdFeatCat.description)?if_exists} [${productFeature.productFeatureCategoryId}]</option>
          <option value='${productFeature.productFeatureCategoryId}'>---</option>
        </#if>
        <#list productFeatureCategories as productFeatureCategory>
          <option value='${productFeatureCategory.productFeatureCategoryId}'>${productFeatureCategory.description} [${productFeatureCategory.productFeatureCategoryId}]</option>
        </#list>
      </select></td>
      <td><input type="text" class='inputBox' size='10' name="uomId_o_${rowCount}" value="${productFeature.uomId?if_exists}"></td>
      <td><input type="text" class='inputBox' size='5' name="numberSpecified_o_${rowCount}" value="${productFeature.numberSpecified?if_exists}"></td>
      <td><input type="text" class='inputBox' size='5' name="defaultAmount_o_${rowCount}" value="${productFeature.defaultAmount?if_exists}"></td>
      <td><input type="text" class='inputBox' size='5' name="defaultSequenceNum_o_${rowCount}" value="${productFeature.defaultSequenceNum?if_exists}"></td>
      <td><input type="text" class='inputBox' size='5' name="idCode_o_${rowCount}" value="${productFeature.idCode?if_exists}"></td>
      <td><input type="text" class='inputBox' size='5' name="abbrev_o_${rowCount}" value="${productFeature.abbrev?if_exists}"></td>
      <td align="right"><input type="checkbox" name="_rowSubmit_o_${rowCount}" value="Y" onclick="javascript:checkToggle(this);"></td>
  </tr>
<#assign rowCount = rowCount + 1>
</#list>
<input type="hidden" name="_rowCount" value="${rowCount}">
<tr><td colspan="11" align="center"><input type="submit" value='Update'/></td></tr>
</form>
</#if>
</table>
<br/>
