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
 *@version    $Revision: 1.2 $
 *@since      2.1
-->
<#assign uiLabelMap = requestAttributes.uiLabelMap>
<#if canNotView>
  <p><h3>${uiLabelMap.CustomerContactInfoNotBelongToYou}.</h3></p>
  &nbsp;<a href="<@ofbizUrl>/authview/${donePage}</@ofbizUrl>" class="buttontext">[${uiLabelMap.CustomerBack}]</a>
<#else>

  <#if !contactMech?exists>
    <#-- When creating a new contact mech, first select the type, then actually create -->
    <#if !requestParameters.preContactMechTypeId?exists && !preContactMechTypeId?exists>
    <p class="head1">${uiLabelMap.CustomerCreateNewContactInfo}</p>
    <form method="post" action='<@ofbizUrl>/editcontactmech?DONE_PAGE=${donePage}</@ofbizUrl>' name="createcontactmechform">
      <table width="90%" border="0" cellpadding="2" cellspacing="0">
        <tr>
          <td width="26%"><div class="tabletext">${uiLabelMap.CustomerSelectContactType}:</div></td>
          <td width="74%">
            <select name="preContactMechTypeId" class='selectBox'>
              <#list contactMechTypes as contactMechType>
                <option value='${contactMechType.contactMechTypeId}'>${contactMechType.description}</option>
              </#list>
            </select>&nbsp;<a href="javascript:document.createcontactmechform.submit()" class="buttontext">[${uiLabelMap.CustomerCreate}]</a>
          </td>
        </tr>
      </table>
    </form>
    <#-- <p><h3>ERROR: Contact information with ID "${contactMechId}" not found!</h3></p> -->
    </#if>
  </#if>

  <#if contactMechTypeId?exists>
    <#if !contactMech?exists>
      <div class="head1">${uiLabelMap.CustomerCreateNewContactInfo}</div>
      &nbsp;<a href='<@ofbizUrl>/authview/${donePage}</@ofbizUrl>' class="buttontext">[${uiLabelMap.CustomerGoBack}]</a>
      &nbsp;<a href="javascript:document.editcontactmechform.submit()" class="buttontext">[${uiLabelMap.CustomerSave}]</a>
      <table width="90%" border="0" cellpadding="2" cellspacing="0">
        <form method="post" action='<@ofbizUrl>/${requestName}</@ofbizUrl>' name="editcontactmechform">
        <input type='hidden' name='DONE_PAGE' value='${donePage}'>
        <input type='hidden' name='contactMechTypeId' value='${contactMechTypeId}'>
        <#if contactMechPurposeType?exists>
            <div>(${uiLabelMap.CustomerNote} <b>"${contactMechPurposeType.description?if_exists}"</b>)</div>
        </#if>
        <#if cmNewPurposeTypeId?has_content><input type='hidden' name='contactMechPurposeTypeId' value='${cmNewPurposeTypeId}'></#if>
        <#if preContactMechTypeId?has_content><input type='hidden' name='preContactMechTypeId' value='${preContactMechTypeId}'></#if>
        <#if paymentMethodId?has_content><input type='hidden' name='paymentMethodId' value='${paymentMethodId}'></#if>
    <#else>
      <p class="head1">${uiLabelMap.CustomerEditContactInfo}</p>
      &nbsp;<a href='<@ofbizUrl>/authview/${donePage}</@ofbizUrl>' class='buttontext'>[${uiLabelMap.CustomerGoBack}]</a>
      &nbsp;<a href="javascript:document.editcontactmechform.submit()" class="buttontext">[${uiLabelMap.CustomerSave}]</a>
      <table width="90%" border="0" cellpadding="2" cellspacing="0">
        <tr>
          <td width="26%" align=right valign=top><div class="tabletext">${uiLabelMap.CustomerConactPurposes}</div></td>
          <td width="5">&nbsp;</td>
          <td width="74%">
            <table border='0' cellspacing='1' bgcolor='black'>
              <#list partyContactMechPurposes?if_exists as partyContactMechPurpose>
                <#assign contactMechPurposeType = partyContactMechPurpose.getRelatedOneCache("ContactMechPurposeType")>
                <tr>
                  <td bgcolor='white'>
                    <div class="tabletext">&nbsp;
                      <#if contactMechPurposeType?exists>
                        <b>${contactMechPurposeType.description}</b>
                      <#else>
                        <b>${uiLabelMap.CustomerPurposeTypeNotFound}: "${partyContactMechPurpose.contactMechPurposeTypeId}"</b>
                      </#if>
                      (Since:${partyContactMechPurpose.fromDate.toString()})
                      <#if partyContactMechPurpose.thruDate?exists>(${uiLabelMap.CustomerExpires}:${partyContactMechPurpose.thruDate.toString()})</#if>
                    &nbsp;</div></td>
                  <td bgcolor='white'><div><a href='<@ofbizUrl>/deletePartyContactMechPurpose?contactMechId=${contactMechId}&contactMechPurposeTypeId=${partyContactMechPurpose.contactMechPurposeTypeId}&fromDate=${partyContactMechPurpose.fromDate.toString()?html}&DONE_PAGE=${donePage}&useValues=true</@ofbizUrl>' class='buttontext'>&nbsp;${uiLabelMap.CustomerDelete}&nbsp;</a></div></td>
                </tr>
              </#list>
              <#if purposeTypes?has_content>
              <tr>
                <form method=POST action='<@ofbizUrl>/createPartyContactMechPurpose?contactMechId=${contactMechId}&DONE_PAGE=${donePage}&useValues=true</@ofbizUrl>' name='newpurposeform'>
                  <td bgcolor='white'>
                    <select name='contactMechPurposeTypeId' class='selectBox'>
                      <option></option>
                      <#list purposeTypes as contactMechPurposeType>
                        <OPTION value='${contactMechPurposeType.contactMechPurposeTypeId}'>${contactMechPurposeType.description}</OPTION>
                      </#list>
                    </select>
                  </td>
                </form>
                <td bgcolor='white'><div><a href='javascript:document.newpurposeform.submit()' class='buttontext'>${uiLabelMap.CustomerAddPurpose}&nbsp;</a></div></td>
              </tr>
              </#if>
            </table>
          </td>
        </tr>
        <form method="post" action='<@ofbizUrl>/${requestName}</@ofbizUrl>' name="editcontactmechform">
        <input type=hidden name="DONE_PAGE" value='${donePage}'>
        <input type=hidden name="contactMechId" value='${contactMechId}'>
    </#if>

  <#if contactMechTypeId = "POSTAL_ADDRESS">
    <tr>
      <td width="26%" align=right valign=top><div class="tabletext">${uiLabelMap.CustomerToName}</div></td>
      <td width="5">&nbsp;</td>
      <td width="74%">
        <input type="text" class='inputBox' size="30" maxlength="60" name="toName" value="${postalAddressData.toName?if_exists}">
      </td>
    </tr>
    <tr>
      <td width="26%" align=right valign=top><div class="tabletext">${uiLabelMap.CustomerAttentionName}</div></td>
      <td width="5">&nbsp;</td>
      <td width="74%">
        <input type="text" class='inputBox' size="30" maxlength="60" name="attnName" value="${postalAddressData.attnName?if_exists}">
      </td>
    </tr>
    <tr>
      <td width="26%" align=right valign=top><div class="tabletext">${uiLabelMap.CustomerAddressLine1}</div></td>
      <td width="5">&nbsp;</td>
      <td width="74%">
        <input type="text" class='inputBox' size="30" maxlength="30" name="address1" value="${postalAddressData.address1?if_exists}">
      *</td>
    </tr>
    <tr>
      <td width="26%" align=right valign=top><div class="tabletext">${uiLabelMap.CustomerAddressLine2}</div></td>
      <td width="5">&nbsp;</td>
      <td width="74%">
          <input type="text" class='inputBox' size="30" maxlength="30" name="address2" value="${postalAddressData.address2?if_exists}">
      </td>
    </tr>
    <tr>
      <td width="26%" align=right valign=top><div class="tabletext">${uiLabelMap.CustomerCity}</div></td>
      <td width="5">&nbsp;</td>
      <td width="74%">
          <input type="text" class='inputBox' size="30" maxlength="30" name="city" value="${postalAddressData.city?if_exists}">
      *</td>
    </tr>
    <tr>
      <td width="26%" align=right valign=top><div class="tabletext">${uiLabelMap.CustomerState}</div></td>
      <td width="5">&nbsp;</td>
      <td width="74%">
        <select name="stateProvinceGeoId" class='selectBox'>
          <#if postalAddressData.stateProvinceGeoId?exists><option value='${postalAddressData.stateProvinceGeoId}'>${selectedStateName?default(postalAddressData.stateProvinceGeoId)}</option></#if>
          <option value="">${uiLabelMap.CustomerNoState}</option>          
          <#include "../includes/states.ftl">          
        </select>
      *</td>
    </tr>
    <tr>
      <td width="26%" align=right valign=top><div class="tabletext">${uiLabelMap.CustomerPostalCode}</div></td>
      <td width="5">&nbsp;</td>
      <td width="74%">
        <input type="text" class='inputBox' size="12" maxlength="10" name="postalCode" value="${postalAddressData.postalCode?if_exists}">
      </td>
    </tr>
    <tr>
      <td width="26%" align=right valign=top><div class="tabletext">${uiLabelMap.CustomerCountry}</div></td>
      <td width="5">&nbsp;</td>
      <td width="74%">
        <select name="countryGeoId" class='selectBox'>
          <#if postalAddressData.countryGeoId?exists><option value='${postalAddressData.countryGeoId}'>${selectedCountryName?default(postalAddressData.countryGeoId)}</option></#if>
          <#include "../includes/countries.ftl">
        </select>
      *</td>
    </tr>
  <#elseif contactMechTypeId = "TELECOM_NUMBER">
    <tr>
      <td width="26%" align=right valign=top><div class="tabletext">${uiLabelMap.CustomerPhoneNumber}</div></td>
      <td width="5">&nbsp;</td>
      <td width="74%">
        <div class="tabletext">
        <input type="text" class='inputBox' size="4" maxlength="10" name="countryCode" value="${telecomNumberData.countryCode?if_exists}">
        -&nbsp;<input type="text" class='inputBox' size="4" maxlength="10" name="areaCode" value="${telecomNumberData.areaCode?if_exists}">
        -&nbsp;<input type="text" class='inputBox' size="15" maxlength="15" name="contactNumber" value="${telecomNumberData.contactNumber?if_exists}">
        &nbsp;ext&nbsp;<input type="text" class='inputBox' size="6" maxlength="10" name="extension" value="${partyContactMechData.extension?if_exists}">
        </div>
      </td>
    </tr>
    <tr>
      <td width="26%" align=right valign=top><div class="tabletext"></div></td>
      <td width="5">&nbsp;</td>
      <td><div class="tabletext">[${uiLabelMap.CustomerCountryCode}] [${uiLabelMap.CustomerAreaCode}] [${uiLabelMap.CustomerContactNumber}] [${uiLabelMap.CustomerExtension}]</div></td>
    </tr>
  <#elseif contactMechTypeId = "EMAIL_ADDRESS">
    <tr>
      <td width="26%" align=right valign=top><div class="tabletext">${uiLabelMap.CustomerEmailAddress}</div></td>
      <td width="5">&nbsp;</td>
      <td width="74%">
          <input type="text" class='inputBox' size="60" maxlength="255" name="emailAddress" value="<#if tryEntity>${contactMech.infoString?if_exists}<#else>${requestParameters.emailAddress?if_exists}</#if>">
      *</td>
    </tr>
  <#else>
    <tr>
      <td width="26%" align=right valign=top><div class="tabletext">${contactMechType.description?if_exists}</div></td>
      <td width="5">&nbsp;</td>
      <td width="74%">
          <input type="text" class='inputBox' size="60" maxlength="255" name="infoString" value="${contactMechData.infoString?if_exists}">
      *</td>
    </tr>
  </#if>
    <tr>
      <td width="26%" align=right valign=top><div class="tabletext">${uiLabelMap.CustomerAllowSolicitation}?</div></td>
      <td width="5">&nbsp;</td>
      <td width="74%">
        <select name="allowSolicitation" class='selectBox'>
          <option>${partyContactMechData.allowSolicitation?if_exists}</option>
          <option></option><option>Y</option><option>N</option>
        </select>
      </td>
    </tr>
  </form>
  </table>

    &nbsp;<a href='<@ofbizUrl>/authview/${donePage}</@ofbizUrl>' class="buttontext">[${uiLabelMap.CustomerGoBack}]</a>
    &nbsp;<a href="javascript:document.editcontactmechform.submit()" class="buttontext">[${uiLabelMap.CustomerSave}]</a>
  <#else>
    &nbsp;<a href='<@ofbizUrl>/authview/${donePage}</@ofbizUrl>' class="buttontext">[${uiLabelMap.CustomerGoBack}]</a>
  </#if>
</#if>

