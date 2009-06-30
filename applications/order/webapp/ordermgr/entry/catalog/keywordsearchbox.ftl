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

<div id="keywordsearchbox" class="screenlet">
  <h3>${uiLabelMap.ProductSearchCatalog}</h3>
  <div class="screenlet-body">
    <form name="keywordsearchform" id="keywordsearchbox_keywordsearchform" method="post" action="<@ofbizUrl>keywordsearch</@ofbizUrl>">
      <fieldset>
        <input type="hidden" name="VIEW_SIZE" value="10"/>
        <input type="hidden" name="PAGING" value="Y"/>
        <div>
          <input type="text" name="SEARCH_STRING" size="14" maxlength="50" value="${requestParameters.SEARCH_STRING?if_exists}"/>
        </div>
        <#if 0 < otherSearchProdCatalogCategories?size>
          <div>
            <select name="SEARCH_CATEGORY_ID" size="1">
              <option value="${searchCategoryId?if_exists}">${uiLabelMap.ProductEntireCatalog}</option>
              <#list otherSearchProdCatalogCategories as otherSearchProdCatalogCategory>
                <#assign searchProductCategory = otherSearchProdCatalogCategory.getRelatedOneCache("ProductCategory")>
                <#if searchProductCategory?exists>
                  <option value="${searchProductCategory.productCategoryId}">${searchProductCategory.description?default("No Description " + searchProductCategory.productCategoryId)}</option>
                </#if>
              </#list>
            </select>
          </div>
        <#else>
          <input type="hidden" name="SEARCH_CATEGORY_ID" value="${searchCategoryId?if_exists}"/>
        </#if>
        <div>
          <input type="radio" name="SEARCH_OPERATOR" value="OR" <#if searchOperator == "OR">checked="checked"</#if>/>${uiLabelMap.CommonAny}
          <input type="radio" name="SEARCH_OPERATOR" value="AND" <#if searchOperator == "AND">checked="checked"</#if>/>${uiLabelMap.CommonAll}
          <input type="submit" value="${uiLabelMap.CommonFind}" class="button"/>
        </div>
      </fieldset>
    </form>
    <form name="advancedsearchform" id="keywordsearchbox_advancedsearchform" method="post" action="<@ofbizUrl>advancedsearch</@ofbizUrl>">
      <fieldset>
        <#if 0 < otherSearchProdCatalogCategories?size>
          <div>
            <label>${uiLabelMap.ProductAdvancedSearchIn}: </label>
            <select name="SEARCH_CATEGORY_ID" size="1">
              <option value="${searchCategoryId?if_exists}">${uiLabelMap.ProductEntireCatalog}</option>
              <#list otherSearchProdCatalogCategories as otherSearchProdCatalogCategory>
                <#assign searchProductCategory = otherSearchProdCatalogCategory.getRelatedOneCache("ProductCategory")>
                <#if searchProductCategory?exists>
                  <option value="${searchProductCategory.productCategoryId}">${searchProductCategory.description?default("No Description " + searchProductCategory.productCategoryId)}</option>
                </#if>
              </#list>
            </select>
          </div>
        <#else>
          <input type="hidden" name="SEARCH_CATEGORY_ID" value="${searchCategoryId?if_exists}"/>
        </#if>
        <div>
          <input type="submit" value="${uiLabelMap.ProductAdvancedSearch}" class="button"/>
        </div>
      </fieldset>
    </form>
  </div>
</div>
