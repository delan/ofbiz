<#--
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
-->

<h1>${uiLabelMap.ProductProductSearch}, <span class="h2">${uiLabelMap.ProductYouSearchedFor}:</span></h1>
<#list searchConstraintStrings as searchConstraintString>
    <div class="tabletext">&nbsp;<a href="<@ofbizUrl>keywordsearch?removeConstraint=${searchConstraintString_index}&clearSearch=N</@ofbizUrl>" class="buttontext">X</a>&nbsp;${searchConstraintString}</div>
</#list>
<div class="tabletext">${uiLabelMap.CommonSortedBy}: ${searchSortOrderString}</div>
<div class="tabletext"><a href="<@ofbizUrl>advancedsearch?SEARCH_CATEGORY_ID=${(reqeustParameters.SEARCH_CATEGORY_ID)?if_exists}</@ofbizUrl>" class="buttontext">${uiLabelMap.ProductRefineSearch}</a></div>

<#if !productIds?has_content>
  <h2>&nbsp;${uiLabelMap.ProductNoResultsFound}.</h2>
</#if>

<#if productIds?has_content>
    <div class="product-prevnext">
        <#-- Start Page Select Drop-Down -->
        <#assign viewIndexMax = Static["java.lang.Math"].ceil((listSize - 1)?double / viewSize?double)>
        <select name="pageSelect" onchange="window.location=this[this.selectedIndex].value;">
          <option value="#">${uiLabelMap.CommonPage} ${viewIndex?int + 1} ${uiLabelMap.CommonOf} ${viewIndexMax + 1}</option>
          <#list 0..viewIndexMax as curViewNum>
            <option value="<@ofbizUrl>keywordsearch/~VIEW_SIZE=${viewSize}/~VIEW_INDEX=${curViewNum?int}/~clearSearch=N</@ofbizUrl>">${uiLabelMap.CommonGotoPage} ${curViewNum + 1}</option>
          </#list>
        </select>
        <#-- End Page Select Drop-Down -->
        <b>
        <#if (viewIndex?int > 0)>
          <a href="<@ofbizUrl>keywordsearch/~VIEW_SIZE=${viewSize}/~VIEW_INDEX=${viewIndex?int - 1}/~clearSearch=N</@ofbizUrl>" class="buttontext">${uiLabelMap.CommonPrevious}</a> |
        </#if>
        <#if (listSize?int > 0)>
          <span class="tabletext">${lowIndex+1} - ${highIndex} ${uiLabelMap.CommonOf} ${listSize}</span>
        </#if>
        <#if highIndex?int < listSize?int>
          | <a href="<@ofbizUrl>keywordsearch/~VIEW_SIZE=${viewSize}/~VIEW_INDEX=${viewIndex+1}/~clearSearch=N</@ofbizUrl>" class="buttontext">${uiLabelMap.CommonNext}</a>
        </#if>
        </b>
    </div>
</#if>

<#if productIds?has_content>
    <div class="productsummary-container">
        <#list productIds as productId> <#-- note that there is no boundary range because that is being done before the list is put in the content -->
            ${setRequestAttribute("optProductId", productId)}
            ${setRequestAttribute("listIndex", productId_index)}
            ${screens.render(productsummaryScreen)}
        </#list>
    </div>
</#if>

<#if productIds?has_content>
    <div class="product-prevnext">
        <#-- Start Page Select Drop-Down -->
        <#assign viewIndexMax = Static["java.lang.Math"].ceil((listSize - 1)?double / viewSize?double)>
        <select name="pageSelect" onchange="window.location=this[this.selectedIndex].value;">
          <option value="#">${uiLabelMap.CommonPage} ${viewIndex?int + 1} ${uiLabelMap.CommonOf} ${viewIndexMax + 1}</option>
          <#list 0..viewIndexMax as curViewNum>
            <option value="<@ofbizUrl>keywordsearch/~VIEW_SIZE=${viewSize}/~VIEW_INDEX=${curViewNum?int}/~clearSearch=N</@ofbizUrl>">${uiLabelMap.CommonGotoPage} ${curViewNum + 1}</option>
          </#list>
        </select>
        <#-- End Page Select Drop-Down -->
        <b>
        <#if (viewIndex?int > 0)>
          <a href="<@ofbizUrl>keywordsearch/~VIEW_SIZE=${viewSize}/~VIEW_INDEX=${viewIndex?int - 1}/~clearSearch=N</@ofbizUrl>" class="buttontext">${uiLabelMap.CommonPrevious}</a> |
        </#if>
        <#if (listSize?int > 0)>
          <span class="tabletext">${lowIndex+1} - ${highIndex} ${uiLabelMap.CommonOf} ${listSize}</span>
        </#if>
        <#if highIndex?int < listSize?int>
          | <a href="<@ofbizUrl>keywordsearch/~VIEW_SIZE=${viewSize}/~VIEW_INDEX=${viewIndex+1}/~clearSearch=N</@ofbizUrl>" class="buttontext">${uiLabelMap.CommonNext}</a>
        </#if>
        </b>
    </div>
</#if>
