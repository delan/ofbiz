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

<h3>${uiLabelMap.EcommerceYourNamePhoneAndEmail}</h3>
<form id="editCustomerNamePhoneAndEmail" name="${parameters.formNameValue}" method="post" action="<@ofbizUrl>processCustomerSettings</@ofbizUrl>">
  <input type="hidden" name="partyId" value="${parameters.partyId?if_exists}"/>
  <fieldset>
    <div>
    <span>
      <label for="personalTitle">${uiLabelMap.CommonTitle}</label>
      <select name="personalTitle">
          <#if requestParameters.personalTitle?has_content >
            <option>${parameters.personalTitle}</option>
            <option value="${parameters.personalTitle}"> -- </option>
          <#else>
            <option value="">${uiLabelMap.CommonSelectOne}</option>
          </#if>
          <option>${uiLabelMap.CommonTitleMr}</option>
          <option>${uiLabelMap.CommonTitleMrs}</option>
          <option>${uiLabelMap.CommonTitleMs}</option>
          <option>${uiLabelMap.CommonTitleDr}</option>
      </select>
    </span>
    <span>
      <lable for="firstName">${uiLabelMap.PartyFirstName}</label>
      <input type="text" name="firstName" value="${parameters.firstName?if_exists}"> *
    </span>
    <span>
      <lable for="middleName">${uiLabelMap.PartyMiddleInitial}</label>
      <input type="text" name="middleName" value="${parameters.middleName?if_exists}">
    </span>
    <span>
      <lable for="lastName">${uiLabelMap.PartyLastName}</label>
      <input type="text" name="lastName" value="${parameters.lastName?if_exists}"> *
    </span>
    <span>
      <lable for="suffix">${uiLabelMap.PartySuffix}</label>
      <input type="text" class='inputBox' name="suffix" value="${parameters.suffix?if_exists}">
    </span>
    </div>
  </fieldset>

  <table summary="Tabular form for entering multiple telecom numbers for different purposes. Each row allows user to enter telecom number for a purpose">
  <caption>${uiLabelMap.PartyPhoneNumbers}</caption>
    <tr>
      <th></th>
      <th scope="col">${uiLabelMap.PartyCountry}</th>
      <th scope="col">${uiLabelMap.PartyAreaCode}</th>
      <th scope="col">${uiLabelMap.PartyContactNumber}</th>
      <th scope="col">${uiLabelMap.PartyExtension}</th>
      <th scope="col">${uiLabelMap.PartyAllowSolicitation}</th>
    </tr>
    <tr>
    <th scope="row">${uiLabelMap.PartyHomePhone}</th>
    <input type="hidden" name="homePhoneContactMechId" value="${parameters.homePhoneContactMechId?if_exists}"/>
    <td><input type="text" name="homeCountryCode" value="${parameters.homeCountryCode?if_exists}"></td>
    <td><input type="text" name="homeAreaCode" value="${parameters.homeAreaCode?if_exists}"></td>
    <td><input type="text" name="homeContactNumber" value="${parameters.homeContactNumber?if_exists}"></td>
    <td><input type="text" name="homeExt" value="${parameters.homeExt?if_exists}"></td>
    <td>
      <select name="homeSol">
        <#if (((parameters.homeSol)!"") == "Y")><option value="Y">${uiLabelMap.CommonY}</option></#if>
        <#if (((parameters.homeSol)!"") == "N")><option value="N">${uiLabelMap.CommonN}</option></#if>
        <option></option>
        <option value="Y">${uiLabelMap.CommonY}</option>
        <option value="N">${uiLabelMap.CommonN}</option>
      </select>
    </td>
  </tr>
  <tr>
    <th scope="row">${uiLabelMap.PartyBusinessPhone}</th>
    <input type="hidden" name="workPhoneContactMechId" value="${parameters.workPhoneContactMechId?if_exists}"/>
    <td><input type="text" name="workCountryCode" value="${parameters.workCountryCode?if_exists}"></td>
    <td><input type="text" name="workAreaCode" value="${parameters.workAreaCode?if_exists}"></td>
    <td><input type="text" name="workContactNumber" value="${parameters.workContactNumber?if_exists}"></td>
    <td><input type="text" name="workExt" value="${parameters.workExt?if_exists}"></td>
    <td>
      <select name="workSol">
        <#if (((parameters.workSol)!"") == "Y")><option value="Y">${uiLabelMap.CommonY}</option></#if>
        <#if (((parameters.workSol)!"") == "N")><option value="N">${uiLabelMap.CommonN}</option></#if>
        <option></option>
        <option value="Y">${uiLabelMap.CommonY}</option>
        <option value="N">${uiLabelMap.CommonN}</option>
      </select>
    </td>
  </tr>
</table>
  <fieldset>
    <div>
    <span>
      <label for="emailAddress">${uiLabelMap.PartyEmailAddress}</label>
      <input type="hidden" name="emailContactMechId" value="${parameters.emailContactMechId?if_exists}"/>
      <input type="text" class="inputBox" name="emailAddress" value="${parameters.emailAddress?if_exists}"/> *
    </span>
    <span>
      <label for="emailSol">${uiLabelMap.PartyAllowSolicitation}</label>
      <select name="emailSol" class="selectBox">
        <#if (((parameters.emailSol)!"") == "Y")><option value="Y">${uiLabelMap.CommonY}</option></#if>
        <#if (((parameters.emailSol)!"") == "N")><option value="N">${uiLabelMap.CommonN}</option></#if>
        <option></option>
        <option value="Y">${uiLabelMap.CommonY}</option>
        <option value="N">${uiLabelMap.CommonN}</option>
      </select>
    </span>
   </div>
  </fieldset>
  <div class="buttons">
    <input type="submit" value="${uiLabelMap.CommonContinue}"/>
  </div>
</form>
