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

<div id="serverError_${contactMech.contactMechId}" class="errorMessage"></div>
<#assign postalAddress = delegator.findOne("PostalAddress", Static["org.ofbiz.base.util.UtilMisc"].toMap("contactMechId", contactMech.contactMechId), true)>

<form id="editPostalAddress_${contactMech.contactMechId}" name="editPostalAddress_${contactMech.contactMechId}" method="post" action="<@ofbizUrl></@ofbizUrl>">
  <input type="hidden" name="contactMechId" value="${postalAddress.contactMechId?if_exists}"/>
  <#assign productStoreId = Static["org.ofbiz.product.store.ProductStoreWorker"].getProductStoreId(request)/>
  <input type="hidden" name="productStoreId" value="${productStoreId?if_exists}"/>
  <#assign showSetShippingPurpose = "Y">
  <#assign showSetBillingPurpose = "Y">
  <#assign telecomNumber = "">
  <#list partyContactMechValueMap.partyContactMechPurposes?if_exists as partyContactMechPurpose> 
    <#assign contactMechPurposeType = partyContactMechPurpose.getRelatedOneCache("ContactMechPurposeType")>
    <#if contactMechPurposeType.contactMechPurposeTypeId == "SHIPPING_LOCATION">
      <#assign showSetShippingPurpose = "N">
      <#assign pcmps = Static["org.ofbiz.entity.util.EntityUtil"].filterByDate(party.getRelatedByAnd("PartyContactMechPurpose", Static["org.ofbiz.base.util.UtilMisc"].toMap("contactMechPurposeTypeId", "PHONE_SHIPPING")))>
      <#if pcmps?has_content>
        <#assign pcmp = Static["org.ofbiz.entity.util.EntityUtil"].getFirst(pcmps)/>
        <#assign telecomNumber = pcmp.getRelatedOne("TelecomNumber")/>
      </#if>
    </#if>
    <#if contactMechPurposeType.contactMechPurposeTypeId == "BILLING_LOCATION">
      <#assign showSetBillingPurpose = "N">
      <#assign pcmps = Static["org.ofbiz.entity.util.EntityUtil"].filterByDate(party.getRelatedByAnd("PartyContactMechPurpose", Static["org.ofbiz.base.util.UtilMisc"].toMap("contactMechPurposeTypeId", "PHONE_BILLING")))>
      <#if pcmps?has_content>
        <#assign pcmp = Static["org.ofbiz.entity.util.EntityUtil"].getFirst(pcmps)/>
        <#assign telecomNumber = pcmp.getRelatedOne("TelecomNumber")/>
      </#if>
    </#if>
  </#list>
  <div class="screenlet-body">
    <div class="form-row">
      <label>${uiLabelMap.PartyAddressLine1}*</label>
      <span>
        <input type="text" class="left required" name="address1" id="address1_${contactMech.contactMechId}" value="${postalAddress.address1?if_exists}" style="width: 50%" size="30" maxlength="30">
        <span id="advice-required-address1_${contactMech.contactMechId}" style="display: none" class="errorMessage">(required)</span>
      </span>
    </div>
    <div class="form-row">
      <label>${uiLabelMap.PartyAddressLine2}</label>
      <span>
        <input type="text" class="left" name="address2" value="${postalAddress.address2?if_exists}" style="width: 50%" size="30" maxlength="30">    
      </span>
    </div>
    <div class="form-row">
      <label>${uiLabelMap.PartyCity}*</label>
      <span>
        <input type="text" class="left required" name="city" id="city_${contactMech.contactMechId}" value="${postalAddress.city?if_exists}" size="30" maxlength="30">
        <span id="advice-required-city_${contactMech.contactMechId}" style="display: none" class="errorMessage">(required)</span>
      </span>
    </div>
    <div class="form-row">
      <label>${uiLabelMap.PartyZipCode}*</label>
      <span>
        <input type="text" class="left required" name="postalCode" id="postalCode_${contactMech.contactMechId}" value="${postalAddress.postalCode?if_exists}" size="12" maxlength="10">
        <span id="advice-required-postalCode_${contactMech.contactMechId}" style="display: none" class="errorMessage">(required)</span>
      </span>
    </div>
    <div class="form-row">
      <label>${uiLabelMap.PartyState}*</label>
      <span>
        <select name="stateProvinceGeoId" id="stateProvinceGeoId_${contactMech.contactMechId}" class="left required" style="width: 70%">
          <#if postalAddress.stateProvinceGeoId?exists>
            <option value='${postalAddress.stateProvinceGeoId}'>${selectedStateName?default(postalAddress.stateProvinceGeoId)}</option>
          </#if>
          <option value="">${uiLabelMap.PartyNoState}</option>
          ${screens.render("component://common/widget/CommonScreens.xml#states")}
        </select>
        <span id="advice-required-stateProvinceGeoId_${contactMech.contactMechId}" style="display: none" class="errorMessage">(required)</span>
      </span>
    </div>
    <div class="form-row">
      <label>${uiLabelMap.PartyCountry}*</label>
      <span>
        <select name="countryGeoId" id="countryGeoId_${contactMech.contactMechId}" class="left required" style="width: 70%">
          <#if postalAddress.countryGeoId?exists>
            <option value='${postalAddress.countryGeoId}'>${selectedCountryName?default(postalAddress.countryGeoId)}</option>
          </#if>
          ${screens.render("component://common/widget/CommonScreens.xml#countries")}
        </select>
        <span id="advice-required-countryGeoId_${contactMech.contactMechId}" style="display: none" class="errorMessage">(required)</span>
      </span>
    </div>

    <#if telecomNumber?has_content>
      <#assign pcm = Static["org.ofbiz.entity.util.EntityUtil"].getFirst(telecomNumber.getRelated("PartyContactMech"))/>
      <div class="form-row">
        <div class="field-label">
          <label for="phoneNumber_${telecomNumber.contactMechId}">${uiLabelMap.PartyPhoneNumber}*</label>
        </div>
        <div>
          <input type="hidden" name="phoneContactMechId" value="${telecomNumber.contactMechId?if_exists}"/>
          <input type="text" name="countryCode" id="countryCode_${telecomNumber.contactMechId}" class="required" value="${telecomNumber.countryCode?if_exists}" size="3" maxlength="3"/>
          - <input type="text" name="areaCode" id="areaCode_${telecomNumber.contactMechId}" class="required" value="${telecomNumber.areaCode?if_exists}" size="3" maxlength="3"/>
          - <input type="text" name="contactNumber" id="contactNumber_${telecomNumber.contactMechId}" class="required" value="${contactNumber?default("${telecomNumber.contactNumber?if_exists}")}" size="6" maxlength="7"/>
          - <input type="text" name="extension" id="extension_${telecomNumber.contactMechId}" value="${extension?default("${pcm.extension?if_exists}")}" size="3" maxlength="3"/>
        </div>
      </div>    
    </#if>
    <#if showSetShippingPurpose == "Y">
      <div class="form-row">
        <b>${uiLabelMap.EcommerceMyDefaultShippingAddress}</b>
        <input type="checkbox" name="setShippingPurpose" value="Y" <#if setShippingPurpose?exists>checked</#if>/>
      </div>
    <#else>
        <input type="hidden" name="setShippingPurpose" value="Y"/>
    </#if>
    <#if showSetBillingPurpose == "Y">
      <div class="form-row">
        <b>${uiLabelMap.EcommerceMyDefaultBillingAddress}</b>
        <input type="checkbox" name="setBillingPurpose" value="Y" <#if setBillingPurpose?exists>checked</#if>/>
      </div>
    <#else>
      <input type="hidden" name="setBillingPurpose" value="Y"/>
    </#if>

    <div class="form-row">
      <a name="submitEditPostalAddress_${contactMech.contactMechId}" id="submitEditPostalAddress_${contactMech.contactMechId}" class="buttontext" onclick="updatePartyPostalAddress('submitEditPostalAddress_${contactMech.contactMechId}')">${uiLabelMap.CommonSubmit}</a>
      <form action="">
        <input class="popup_closebox buttontext" type="button" value="${uiLabelMap.CommonClose}"/>
      </form>
    </div>
  </div>
</form>