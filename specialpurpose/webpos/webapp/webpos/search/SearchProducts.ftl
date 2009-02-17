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

<form name="SearchProducts" method="post" action="<@ofbizUrl>AddItem</@ofbizUrl>">
    <div>
        <input type="hidden" id="quantity" name="quantity" value="1">
        <input type="hidden" id="add_product_id" name="add_product_id" value="${parameters.add_product_id?if_exists}">
        <label for="productGoodIdentification"><b>&nbsp;${uiLabelMap.ProductGoodIdentification}</b></label>
        <input type="text" id="productGoodIdentification" name="productGoodIdentification" size="40" maxlength="100">
        <div id="productsIdent" class="autocomplete" style="display:none"></div>
        <input type="submit" id="submitButton" name="submitButton" value="${uiLabelMap.OrderAddToCart}" onclick="ajaxSubmitFormUpdateAreas('SearchProducts', 'CartScreen, webpos/control/ShowCart,')">
        <br/>
        <label for="searchBy"><b>&nbsp;${uiLabelMap.WebPosSearchBy}</b></label>
        <select id="searchBy" name="searchBy">
          <option value="productName" selected>${uiLabelMap.ProductProductName}</option>
          <option value="productDescription">${uiLabelMap.ProductProductDescription}</option>
        </select>
        <input type="text" id="productToSearch" name="productToSearch" size="40" maxlength="100">
        <div id="products" class="autocomplete" style="display:none"></div>
    </div>
</form>
<br/>
<script language="javascript" type="text/javascript">
    document.SearchProducts.productGoodIdentification.focus();
</script>