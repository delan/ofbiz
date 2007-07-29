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
    
<style type="text/css">
.button-col form a,
.button-col form input {
float: left;
}
</style>

    <span class="head1">${uiLabelMap.ProductLocationFor}</span> <span class="head2"><#if facility?exists>${(facility.facilityName)?if_exists}</#if> [${uiLabelMap.CommonId}: ${facilityId?if_exists}]</span>
    <div class="button-bar">
      <a href="<@ofbizUrl>EditFacility</@ofbizUrl>" class="buttontext">${uiLabelMap.ProductNewFacility}</a>
      <a href="<@ofbizUrl>EditFacilityLocation?facilityId=${facilityId?if_exists}</@ofbizUrl>" class="buttontext">${uiLabelMap.ProductNewFacilityLocation}</a>
      <#if facilityId?exists && locationSeqId?exists>
          <a href="<@ofbizUrl>EditInventoryItem?facilityId=${facilityId}&locationSeqId=${locationSeqId}</@ofbizUrl>" class="buttontext">${uiLabelMap.ProductNewInventoryItem}</a>
      </#if>
    </div>
    
    <#if facilityId?exists && !(facilityLocation?exists)> 
        <form action="<@ofbizUrl>CreateFacilityLocation</@ofbizUrl>" method="post" style="margin: 0;">
        <input type="hidden" name="facilityId" value="${facilityId}">  
        <table class="basic-table" cellspacing="0">
    <#elseif facilityLocation?exists>
        <form action="<@ofbizUrl>UpdateFacilityLocation</@ofbizUrl>" method="post" style="margin: 0;">
        <input type="hidden" name="facilityId" value="${facilityId?if_exists}">
        <input type="hidden" name="locationSeqId" value="${locationSeqId}">
        <table class="basic-table" cellspacing="0">
        <tr>
            <td class="label">${uiLabelMap.ProductFacilityId}</td>
            <td>${facilityId?if_exists}</td>
        </tr>
        <tr>
            <td class="label">${uiLabelMap.ProductLocationSeqId}</td>
            <td>${locationSeqId}</td>
        </tr>
    <#else>
        <h1>${uiLabelMap.ProductNotCreateLocationFacilityId}</h1>
    </#if>
    
    <#if facilityId?exists>      
        <tr>
            <td class="label">${uiLabelMap.ProductType}</td>
            <td>
                <select name="locationTypeEnumId">
                    <#if (facilityLocation.locationTypeEnumId)?has_content>
                        <#assign locationTypeEnum = facilityLocation.getRelatedOneCache("TypeEnumeration")?if_exists>
                        <option value="${facilityLocation.locationTypeEnumId}">${(locationTypeEnum.get("description",locale))?default(facilityLocation.locationTypeEnumId)}</option>
                        <option value="${facilityLocation.locationTypeEnumId}">----</option>
                    </#if>
                    <#list locationTypeEnums as locationTypeEnum>
                        <option value="${locationTypeEnum.enumId}">${locationTypeEnum.get("description",locale)}</option>
                    </#list>
                </select>
            </td>
        </tr>
        <tr>
            <td class="label">${uiLabelMap.CommonArea}</td>
            <td><input type="text" name="areaId" value="${(facilityLocation.areaId)?if_exists}" size="19" maxlength="20"></td>
        </tr>
        <tr>
            <td class="label">${uiLabelMap.ProductAisle}</td>
            <td><input type="text" name="aisleId" value="${(facilityLocation.aisleId)?if_exists}" size="19" maxlength="20"></td>
        </tr>
        <tr>
            <td class="label">${uiLabelMap.ProductSection}</td>
            <td><input type="text" name="sectionId" value="${(facilityLocation.sectionId)?if_exists}" size="19" maxlength="20"></td>
        </tr>
        <tr>
            <td class="label">${uiLabelMap.ProductLevel}</td>
            <td><input type="text" name="levelId" value="${(facilityLocation.levelId)?if_exists}" size="19" maxlength="20"></td>
        </tr>
        <tr>
            <td class="label">${uiLabelMap.ProductPosition}</td>
            <td><input type="text" name="positionId" value="${(facilityLocation.positionId)?if_exists}" size="19" maxlength="20"></td>
        </tr>    
        <tr>
            <td>&nbsp;</td>
            <td><input type="submit" value="${uiLabelMap.CommonUpdate}"></td>
        </tr>
    </table>
    </form>
    
    <hr/>
    
        <#-- ProductFacilityLocation stuff -->
        <h2>${uiLabelMap.ProductLocationProduct}:</h2>
        <table class="basic-table" cellspacing="0">
        <tr class="header-row">
            <td>${uiLabelMap.ProductProduct}</td>
            <td>${uiLabelMap.ProductMinimumStockAndMoveQuantity}</td>
        </tr>
        <#list productFacilityLocations?if_exists as productFacilityLocation>
            <#assign product = productFacilityLocation.getRelatedOne("Product")?if_exists>
            <tr>
                <td><#if product?exists>${(product.internalName)?if_exists}</#if>[${productFacilityLocation.productId}]</td>
                <td class="button-col">
                    <form method="post" action="<@ofbizUrl>updateProductFacilityLocation</@ofbizUrl>" name="lineForm${productFacilityLocation_index}">
                        <input type="hidden" name="productId" value="${(productFacilityLocation.productId)?if_exists}"/>
                        <input type="hidden" name="facilityId" value="${(productFacilityLocation.facilityId)?if_exists}"/>
                        <input type="hidden" name="locationSeqId" value="${(productFacilityLocation.locationSeqId)?if_exists}"/>
                        <input type="text" size="10" name="minimumStock" value="${(productFacilityLocation.minimumStock)?if_exists}"/>
                        <input type="text" size="10" name="moveQuantity" value="${(productFacilityLocation.moveQuantity)?if_exists}"/>
                        <input type="submit" value="${uiLabelMap.CommonUpdate}"/>
                        <a href="<@ofbizUrl>deleteProductFacilityLocation?productId=${(productFacilityLocation.productId)?if_exists}&facilityId=${(productFacilityLocation.facilityId)?if_exists}&locationSeqId=${(productFacilityLocation.locationSeqId)?if_exists}</@ofbizUrl>">${uiLabelMap.CommonDelete}</a>
                    </form>
                </td>
            </tr>
        </#list>
        </table>
        <br/>
        <h2>${uiLabelMap.ProductAddProduct}:</h2>
        <form method="post" action="<@ofbizUrl>createProductFacilityLocation</@ofbizUrl>" style="margin: 0;" name="createProductFacilityLocationForm">
            <input type="hidden" name="facilityId" value="${facilityId?if_exists}">
            <input type="hidden" name="locationSeqId" value="${locationSeqId?if_exists}">
            <input type="hidden" name="useValues" value="true">
            ${uiLabelMap.ProductProductId}:&nbsp;<input type="text" size="10" name="productId">
            ${uiLabelMap.ProductMinimumStock}:&nbsp;<input type="text" size="10" name="minimumStock">
            ${uiLabelMap.ProductMoveQuantity}:&nbsp;<input type="text" size="10" name="moveQuantity">
            <input type="submit" value="${uiLabelMap.CommonAdd}">
        </form>
    </#if>
