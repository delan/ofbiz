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
 *@author     Andy Zeneski (jaz@ofbiz.org)
 *@version    $Rev$
 *@since      2.1
-->

<#assign uiLabelMap = requestAttributes.uiLabelMap>
<div class="head1">Product Search, <span class="head2">you searched for:</span></div>
<#list searchConstraintStrings as searchConstraintString>
    <div class="tabletext">&nbsp;<a href="<@ofbizUrl>/keywordsearch?removeConstraint=${searchConstraintString_index}&clearSearch=N</@ofbizUrl>" class="buttontext">[X]</a>&nbsp;${searchConstraintString}</div>
</#list>
<div class="tabletext">Sorted by: ${searchSortOrderString}</div>
<div class="tabletext"><a href="<@ofbizUrl>/advancedsearch?SEARCH_CATEGORY_ID=${(reqeustParameters.SEARCH_CATEGORY_ID)?if_exists}</@ofbizUrl>" class="buttontext">[Refine Search]</a></div>

<#if !productIds?has_content>
  <br><div class="head2">&nbsp;${uiLabelMap.ProductNoResultsFound}.</div>
</#if>

<#if productIds?has_content>
<table border="0" width="100%" cellpadding="2">
    <tr>
      <td align=right>
        <b>
        <#if 0 < viewIndex?int>
          <a href="<@ofbizUrl>/keywordsearch/~VIEW_INDEX=${viewIndex-1}/~VIEW_SIZE=${viewSize}/~clearSearch=N</@ofbizUrl>" class="buttontext">[${uiLabelMap.CommonPrevious}]</a> |
        </#if>
        <#if 0 < listSize?int>
          <span class="tabletext">${lowIndex+1} - ${highIndex} of ${listSize}</span>
        </#if>
        <#if highIndex?int < listSize?int>
          | <a href="<@ofbizUrl>/keywordsearch/~VIEW_INDEX=${viewIndex+1}/~VIEW_SIZE=${viewSize}/~clearSearch=N</@ofbizUrl>" class="buttontext">[${uiLabelMap.CommonNext}]</a>
        </#if>
        </b>
      </td>
    </tr>
</table>
</#if>

<#if productIds?has_content>
<script language="JavaScript">
<!--
    function addProductToBagTextArea(idValue) {
        document.forms["quickCreateVirtualWithVariants"].elements["variantProductIdsBag"].value+=idValue;
        document.forms["quickCreateVirtualWithVariants"].elements["variantProductIdsBag"].value+="\n";
    }
//-->
</script>
<center>
  <table width="100%" cellpadding="0" cellspacing="0">
    <#assign listIndex = lowIndex>
    <#list productIds as productId><#-- note that there is no boundary range because that is being done before the list is put in the content -->
      <#assign product = delegator.findByPrimaryKey("Product", Static["org.ofbiz.base.util.UtilMisc"].toMap("productId", productId))>
      <tr>
        <td>
          <input type="checkbox" name="selectResult${productId_index}" onChange="addProductToBagTextArea('${productId}');"/>
          <a href="<@ofbizUrl>/EditProduct?productId=${productId}</@ofbizUrl>" class="buttontext">[${productId}] ${(product.internalName)?if_exists}</a>
        </td>
      </tr>
    </#list>
  </table>
</center>
</#if>

<#if productIds?has_content>
<table border="0" width="100%" cellpadding="2">
    <tr>
      <td align=right>
        <b>
        <#if 0 < viewIndex?int>
          <a href="<@ofbizUrl>/keywordsearch/~VIEW_INDEX=${viewIndex-1}/~VIEW_SIZE=${viewSize}/~clearSearch=N</@ofbizUrl>" class="buttontext">[${uiLabelMap.CommonPrevious}]</a> |
        </#if>
        <#if 0 < listSize?int>
          <span class="tabletext">${lowIndex+1} - ${highIndex} of ${listSize}</span>
        </#if>
        <#if highIndex?int < listSize?int>
          | <a href="<@ofbizUrl>/keywordsearch/~VIEW_INDEX=${viewIndex+1}/~VIEW_SIZE=${viewSize}/~clearSearch=N</@ofbizUrl>" class="buttontext">[${uiLabelMap.CommonNext}]</a>
        </#if>
        </b>
      </td>
    </tr>
</table>

<hr class="sepbar"/>

<#include "/product/CreateVirtualWithVariantsForm.ftl"/>

<hr class="sepbar"/>

<div class="tabletext">
<form method="POST" action="<@ofbizUrl>/searchRemoveFromCategory</@ofbizUrl>"
  <b>Remove Results From </b> ${uiLabelMap.ProductCategory}:
    <select class="selectBox" name="SE_SEARCH_CATEGORY_ID">
       <option value="">- Any Category -</option>
       <#list productCategories as productCategory>
           <#assign displayDesc = productCategory.description?default("No Description")>
           <#if 28 < displayDesc?length>
               <#assign displayDesc = displayDesc[0..25] + "...">
           </#if>
           <option value="${productCategory.productCategoryId}">${displayDesc} [${productCategory.productCategoryId}]</option>
       </#list>
    </select>
  <input type="hidden" name="clearSearch" value="N">
  <input type="submit" value="Remove" class="smallSubmit"><br>
</form>
</div>

<hr class="sepbar"/>

<div class="tabletext">
<form method="POST" action="<@ofbizUrl>/searchExpireFromCategory</@ofbizUrl>" name="searchExpireFromCategory">
  <b>Expire Results From </b> ${uiLabelMap.ProductCategory}:
    <select class="selectBox" name="SE_SEARCH_CATEGORY_ID">
       <option value="">- Any Category -</option>
       <#list productCategories as productCategory>
           <#assign displayDesc = productCategory.description?default("No Description")>
           <#if 28 < displayDesc?length>
               <#assign displayDesc = displayDesc[0..25] + "...">
           </#if>
           <option value="${productCategory.productCategoryId}">${displayDesc} [${productCategory.productCategoryId}]</option>
       </#list>
    </select>
  Thru<input type="text" size="25" name="thruDate" class="inputBox"><a href="javascript:call_cal(document.searchExpireFromCategory.thruDate, null);"><img src="/images/cal.gif" width="16" height="16" border="0" alt="Calendar"></a>
  <input type="hidden" name="clearSearch" value="N">
  <input type="submit" value="Expire" class="smallSubmit"><br>
</form>
</div>

<hr class="sepbar"/>

<div class="tabletext">
<form method="POST" action="<@ofbizUrl>/searchAddToCategory</@ofbizUrl>" name="searchAddToCategory">
  <b>Add Results to </b> ${uiLabelMap.ProductCategory}:
    <select class="selectBox" name="SE_SEARCH_CATEGORY_ID">
       <#list productCategories as productCategory>
           <#assign displayDesc = productCategory.description?default("No Description")>
           <#if 28 < displayDesc?length>
               <#assign displayDesc = displayDesc[0..25] + "...">
           </#if>
           <option value="${productCategory.productCategoryId}">${displayDesc} [${productCategory.productCategoryId}]</option>
       </#list>
    </select>
  From<input type="text" size="25" name="fromDate" class="inputBox"><a href="javascript:call_cal(document.searchAddToCategory.fromDate, null);"><img src="/images/cal.gif" width="16" height="16" border="0" alt="Calendar"></a>
  <input type="hidden" name="clearSearch" value="N">
  <input type="submit" value="Add to Category" class="smallSubmit"><br>
</form>
</div>

<hr class="sepbar"/>

<div class="tabletext">
<form method="POST" action="<@ofbizUrl>/searchAddFeature</@ofbizUrl>" name="searchAddFeature">
  <b>Add Feature to Results:</b><br>
  Feature ID<input type="text" size="5" name="productFeatureId" value="" class="inputBox">
  From<input type="tex"t size="25" name="fromDate" class="inputBox"><a href="javascript:call_cal(document.searchAddFeature.fromDate, null);"><img src="/images/cal.gif" width="16" height="16" border="0" alt="Calendar"></a> 
  Thru<input type="text" size="25" name="thruDate" class="inputBox"><a href="javascript:call_cal(document.searchAddFeature.thruDate, null);"><img src="/images/cal.gif" width="16" height="16" border="0" alt="Calendar"></a>
  <br>
  Amount<input type="text" size="5" name="amount" value="" class="inputBox">
  Sequence<input type="text" size="5" name="sequenceNum" value="" class="inputBox">
  Application Type
    ${uiLabelMap.ProductCategoryId}:
    <select name='productFeatureApplTypeId' size='1' class='selectBox'>
       <#list applicationTypes as applicationType>
           <#assign displayDesc = applicationType.description?default("No Description")>
           <#if 18 < displayDesc?length>
               <#assign displayDesc = displayDesc[0..15] + "...">
           </#if>
           <option value="${applicationType.productFeatureApplTypeId}">${displayDesc}</option>
       </#list>
  </select>
  <input type="hidden" name="clearSearch" value="N">
  <input type="submit" value="Add Feature" class="smallSubmit"><br>
</form>
</div>

</#if>

