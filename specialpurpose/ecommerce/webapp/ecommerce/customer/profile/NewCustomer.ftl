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

<div class="screenlet">
  <h3>${uiLabelMap.EcommerceMyAccount}</h3>
  <div class="screenlet-body">
    <form id="newUserForm" method="post" action="<@ofbizUrl>createCustomerProfile</@ofbizUrl>">
      <fieldset>
        <input type="hidden" name="roleTypeId" value="CUSTOMER" />
        <input type="hidden" name="emailContactMechPurposeTypeId" value="PRIMARY_EMAIL" />
        <#assign productStoreId = Static["org.ofbiz.product.store.ProductStoreWorker"].getProductStoreId(request) />
        <input type="hidden" name="productStoreId" value="${productStoreId?if_exists}" />
        <div class="left center">
          <h3>${uiLabelMap.PartyContactInformation}</h3>
          <div>
            <label for="firstName">${uiLabelMap.PartyFirstName}* <span id="advice-required-firstName" style="display: none" class="errorMessage">(required)</span></label>
            <input type="text" name="firstName" id="firstName" class="required" value="${parameters.firstName?if_exists}" size="30" maxlength="30" />
          </div>
          <div>
            <label for="lastName">${uiLabelMap.PartyLastName}* <span id="advice-required-lastName" style="display: none" class="errorMessage">(required)</span></label>
            <input type="text" name="lastName" id="lastName" class="required" value="${parameters.lastName?if_exists}" size="30" maxlength="30" />
          </div>
          <div>
            <label for="emailAddress">${uiLabelMap.CommonEmail}*
              <span id="advice-required-emailAddress" style="display: none" class="errorMessage">(required)</span>
            </label>
            <input type="text" class="required validate-email" name="emailAddress" id="emailAddress" value="${parameters.emailAddress?if_exists}" size="30" maxlength="255" />
          </div>
        </div>
      </fieldset>
      <fieldset>
        <div class="center right">
          <h3>${uiLabelMap.EcommerceAccountInformation}</h3>
          <div id="userNameAndPasswordPanel">
            <div>
              <label for="username">${uiLabelMap.CommonUsername}* <span id="advice-required-username" style="display: none" class="errorMessage">(required)</span></label>
              <input type="text" name="username" id="username" class="required" value="${parameters.username?if_exists}" size="30" maxlength="255" />
            </div>
            <div>
              <label for="password">${uiLabelMap.CommonPassword}* <span id="advice-required-password" style="display: none" class="errorMessage">(required)</span></label>
              <input type="password" name="password" id="password" class="required validate-password" value="${parameters.password?if_exists}" maxlength="16" />
              <span id="advice-validate-password-password" class="errorMessage" style="display:none">${uiLabelMap["loginservices.password_may_not_equal_username"]}</span>
            </div>
            <div>
              <label for="passwordVerify">${uiLabelMap.PartyRepeatPassword}* <span id="advice-required-passwordVerify" style="display: none" class="errorMessage">(required)</span></label>
              <input type="password" name="passwordVerify" id="passwordVerify" class="required validate-passwordVerify" value="${parameters.passwordVerify?if_exists}" maxlength="16" />
              <span id="advice-validate-passwordVerify-passwordVerify" class="errorMessage" style="display:none">${uiLabelMap["loginservices.password_did_not_match_verify_password"]}</span>
            </div>
          </div>
        </div>
        <span id="advice-validate-email-emailAddress" class="errorMessage" style="display:none">${uiLabelMap.PartyEmailAddressNotFormattedCorrectly}</span>
      </fieldset>
      <fieldset>
        <div class="left center">
          <h3>${uiLabelMap.OrderShippingInformation}</h3>
          <div>
            <label for="shipToAddress1">${uiLabelMap.PartyAddressLine1}* <span id="advice-required-shipToAddress1" style="display: none" class="errorMessage">(required)</span></label>
            <input type="text" name="shipToAddress1" id="shipToAddress1" class="required" value="${parameters.shipToAddress1?if_exists}" />
          </div>
          <div>
            <label for="shipToAddress2">${uiLabelMap.PartyAddressLine2}</label>
            <input type="text" name="shipToAddress2" id="shipToAddress2" value="${parameters.shipToAddress2?if_exists}" />
          </div>
          <div>
            <label for="shipToCity">${uiLabelMap.CommonCity}* <span id="advice-required-shipToCity" style="display: none" class="errorMessage">(required)</span></label>
            <input type="text" name="shipToCity" id="shipToCity" class="required" value="${parameters.shipToCity?if_exists}" />
          </div>
          <div>
            <label for="shipToPostalCode">${uiLabelMap.PartyZipCode}* <span id="advice-required-shipToPostalCode" style="display: none" class="errorMessage">(required)</span></label>
            <input type="text" name="shipToPostalCode" id="shipToPostalCode" class="required" value="${parameters.shipToPostalCode?if_exists}" maxlength="10" />
          </div>
          <div>
            <label for="shipToCountryGeoId">${uiLabelMap.PartyCountry}* <span id="advice-required-shipToCountryGeoId" style="display: none" class="errorMessage">(required)</span></label>
            <div>
              <select name="shipToCountryGeoId" id="shipToCountryGeoId">
                <#if shipToCountryGeoId??>
                  <option value="${shipToCountryGeoId!}">${shipToCountryProvinceGeo!(shipToCountryGeoId!)}</option>
                </#if>
                ${screens.render("component://common/widget/CommonScreens.xml#countries")}
              </select>
            </div>
          </div>
          <div id='shipToStates'>
            <label for="shipToStateProvinceGeoId">${uiLabelMap.CommonState}*<span id="advice-required-shipToStateProvinceGeoId" style="display: none" class="errorMessage">(required)</span></label>
            <div>
              <select id="shipToStateProvinceGeoId" name="shipToStateProvinceGeoId">
                <#if shipToStateProvinceGeoId?has_content>
                  <option value='${shipToStateProvinceGeoId!}'>${shipToStateProvinceGeo!(shipToStateProvinceGeoId!)}</option>
                <#else>
                  <option value="_NA_">${uiLabelMap.PartyNoState}</option>
                </#if>
              </select>
            </div>
          </div>
          <div>
            <label>${uiLabelMap.PartyPhoneNumber}*</label>
            <span id="advice-required-shipToCountryCode" style="display:none" class="errorMessage"></span>
            <span id="advice-required-shipToAreaCode" style="display:none" class="errorMessage"></span>
            <span id="advice-required-shipToContactNumber" style="display:none" class="errorMessage"></span>
            <span id="shipToPhoneRequired" style="display: none;" class="errorMessage">(required)</span>
            <input type="text" name="shipToCountryCode" id="shipToCountryCode" class="required" value="${parameters.shipToCountryCode?if_exists}" size="3" maxlength="3" />
            - <input type="text" name="shipToAreaCode" id="shipToAreaCode" class="required" value="${parameters.shipToAreaCode?if_exists}" size="3" maxlength="3" />
            - <input type="text" name="shipToContactNumber" id="shipToContactNumber" class="required" value="${contactNumber?default("${parameters.shipToContactNumber?if_exists}")}" size="6" maxlength="7" />
            - <input type="text" name="shipToExtension" id="shipToExtension" value="${extension?default("${parameters.shipToExtension?if_exists}")}" size="3" maxlength="3" />
          </div>
          <div>
            <input type="checkbox" class="checkbox" name="useShippingAddressForBilling" id="useShippingAddressForBilling" value="Y" <#if parameters.useShippingAddressForBilling?has_content && parameters.useShippingAddressForBilling?default("")=="Y">checked="checked"</#if> />
            <label for="useShippingAddressForBilling">${uiLabelMap.FacilityBillingAddressSameShipping}</label>
          </div>
        </div>
      </fieldset>
      <fieldset>
        <div class="center right">
          <h3>${uiLabelMap.PageTitleBillingInformation}</h3>
          <div id="billingAddress">
            <div>
              <label for="billToAddress1">${uiLabelMap.PartyAddressLine1}* <span id="advice-required-billToAddress1" style="display: none" class="errorMessage">(required)</span></label>
              <input type="text" name="billToAddress1" id="billToAddress1" class="required" value="${parameters.billToAddress1?if_exists}" />
            </div>
            <div>
              <label for="billToAddress2">${uiLabelMap.PartyAddressLine2}</label>
              <input type="text" name="billToAddress2" id="billToAddress2" value="${parameters.billToAddress2?if_exists}" />
            </div>
            <div>
              <label for="billToCity">${uiLabelMap.CommonCity}*<span id="advice-required-billToCity" style="display: none" class="errorMessage">(required)</span></label>
              <input type="text" name="billToCity" id="billToCity" class="required" value="${parameters.billToCity?if_exists}" />
            </div>
            <div>
              <label for="billToPostalCode">${uiLabelMap.PartyZipCode}* <span id="advice-required-billToPostalCode" style="display: none" class="errorMessage">(required)</span></label>
              <input type="text" name="billToPostalCode" id="billToPostalCode" class="required" value="${parameters.billToPostalCode?if_exists}" maxlength="10" />
            </div>
            <div>
              <label for="billToCountryGeoId">${uiLabelMap.PartyCountry}* <span id="advice-required-billToCountryGeoId" style="display: none" class="errorMessage">(required)</span></label>
              <select name="billToCountryGeoId" id="billToCountryGeoId" class='required selectBox'>
              <#if billToCountryGeoId??>
                <option value='${billToCountryGeoId!}'>${billToCountryProvinceGeo!(billToCountryGeoId!)}</option>
              </#if>
                ${screens.render("component://common/widget/CommonScreens.xml#countries")}
              </select>
            </div>
            <div id='billToStates'>
              <label for="billToStateProvinceGeoId">${uiLabelMap.CommonState}*<span id="advice-required-billToStateProvinceGeoId" style="display: none" class="errorMessage">(required)</span></label>
              <div>
                <select id="billToStateProvinceGeoId" name="billToStateProvinceGeoId">
                <#if billToStateProvinceGeoId?has_content>
                  <option value='${billToStateProvinceGeoId!}'>${billToStateProvinceGeo!(billToStateProvinceGeoId!)}</option>
                <#else>
                  <option value="_NA_">${uiLabelMap.PartyNoState}</option>
                </#if>
                </select>
              </div>
            </div>
            <div>
              <label>${uiLabelMap.PartyPhoneNumber}*</label>
              <span id="advice-required-billToCountryCode" style="display:none" class="errorMessage"></span>
              <span id="advice-required-billToAreaCode" style="display:none" class="errorMessage"></span>
              <span id="advice-required-billToContactNumber" style="display:none" class="errorMessage"></span>
              <span id="billToPhoneRequired" style="display: none;" class="errorMessage">(required)</span>
              <input type="text" name="billToCountryCode" id="billToCountryCode" class="required" value="${parameters.billToCountryCode?if_exists}" size="3" maxlength="3"/>
              - <input type="text" name="billToAreaCode" id="billToAreaCode" class="required" value="${parameters.billToAreaCode?if_exists}" size="3" maxlength="3"/>
              - <input type="text" name="billToContactNumber" id="billToContactNumber" class="required" value="${contactNumber?default("${parameters.billToContactNumber?if_exists}")}" size="6" maxlength="7"/>
              - <input type="text" name="billToExtension" id="billToExtension" value="${extension?default("${parameters.billToExtension?if_exists}")}" size="3" maxlength="3"/>
            </div>
          </div>
        </div>
      </fieldset>
      <div><a id="submitNewUserForm" href="javascript:void(0);" class="button">${uiLabelMap.CommonSubmit}</a></div>
    </form>
  </div>
</div>