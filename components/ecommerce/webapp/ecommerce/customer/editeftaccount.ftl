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
 *@version    $Revision: 1.1 $
 *@since      2.1
-->


<#if canNotView>
  <p><h3>The EFT Account specified does not belong to you, you may not view or edit it.</h3></p>
&nbsp;<a href='<@ofbizUrl>/authview/${donePage}</@ofbizUrl>' class="buttontext">[Back]</a>
<#else>
    <#if !eftAccount?exists>
      <p class="head1">Add New EFT Account</p>
      &nbsp;<a href='<@ofbizUrl>/authview/${donePage}</@ofbizUrl>' class="buttontext">[Go&nbsp;Back]</a>
      &nbsp;<a href="javascript:document.editeftaccountform.submit()" class="buttontext">[Save]</a>
      <form method="post" action='<@ofbizUrl>/createEftAccount?DONE_PAGE=${donePage}</@ofbizUrl>' name="editeftaccountform" style='margin: 0;'>
      <table width="90%" border="0" cellpadding="2" cellspacing="0">
    <#else>
      <p class="head1">Edit EFT Account</p>
      &nbsp;<a href='<@ofbizUrl>/authview/${donePage}</@ofbizUrl>' class="buttontext">[Go&nbsp;Back]</a>
      &nbsp;<a href="javascript:document.editeftaccountform.submit()" class="buttontext">[Save]</a>
      <form method="post" action='<@ofbizUrl>/updateEftAccount?DONE_PAGE=${donePage}</@ofbizUrl>' name="editeftaccountform" style='margin: 0;'>
      <table width="90%" border="0" cellpadding="2" cellspacing="0">
        <input type=hidden name='paymentMethodId' value='${paymentMethodId}'>
    </#if>

    <tr>
      <td width="26%" align=right valign=top><div class="tabletext">Name on Account</div></td>
      <td width="5">&nbsp;</td>
      <td width="74%">
        <input type="text" class='inputBox' size="30" maxlength="60" name="nameOnAccount" value="${eftAccountData.nameOnAccount?if_exists}">
      *</td>
    </tr>
    <tr>
      <td width="26%" align=right valign=top><div class="tabletext">Company Name on Account</div></td>
      <td width="5">&nbsp;</td>
      <td width="74%">
        <input type="text" class='inputBox' size="30" maxlength="60" name="companyNameOnAccount" value="${eftAccountData.companyNameOnAccount?if_exists}">
      </td>
    </tr>
    <tr>
      <td width="26%" align=right valign=top><div class="tabletext">Bank Name</div></td>
      <td width="5">&nbsp;</td>
      <td width="74%">
        <input type="text" class='inputBox' size="30" maxlength="60" name="bankName" value="${eftAccountData.bankName?if_exists}">
      *</td>
    </tr>
    <tr>
      <td width="26%" align=right valign=top><div class="tabletext">Routing Number</div></td>
      <td width="5">&nbsp;</td>
      <td width="74%">
        <input type="text" class='inputBox' size="10" maxlength="30" name="routingNumber" value="${eftAccountData.routingNumber?if_exists}">
      *</td>
    </tr>
    <tr>
      <td width="26%" align=right valign=top><div class="tabletext">Account Type</div></td>
      <td width="5">&nbsp;</td>
      <td width="74%">
        <select name="accountType" class='selectBox'>
          <option>${eftAccountData.accountType?if_exists}</option>
          <option></option>
          <option>Checking</option>
          <option>Savings</option>
        </select>
      *</td>
    </tr>
    <tr>
      <td width="26%" align=right valign=top><div class="tabletext">Account Number</div></td>
      <td width="5">&nbsp;</td>
      <td width="74%">
        <input type="text" class='inputBox' size="20" maxlength="40" name="accountNumber" value="${eftAccountData.accountNumber?if_exists}">
      *</td>
    </tr>

    <tr>
      <td width="26%" align=right valign=top><div class="tabletext">Billing Address</div></td>
      <td width="5">&nbsp;</td>
      <td width="74%">
        <#-- Removed because is confusing, can add but would have to come back here with all data populated as before...
        <a href="<@ofbizUrl>/editcontactmech</@ofbizUrl>" class="buttontext">
          [Create New Address]</a>&nbsp;&nbsp;
        -->
        <table width="100%" border="0" cellpadding="1">
        <#if curPostalAddress?exists>
          <tr>
            <td align="right" valign="top" width="1%">
              <input type="radio" name="contactMechId" value="${curContactMechId}" checked>
            </td>
            <td align="left" valign="top" width="80%">
              <div class="tabletext"><b>Use Current Address:</b></div>
              <#list curPartyContactMechPurposes as curPartyContactMechPurpose> 
                <#assign curContactMechPurposeType = curPartyContactMechPurpose.getRelatedOneCache("ContactMechPurposeType")>
                <div class="tabletext">
                  <b>${curContactMechPurposeType.description?if_exists}</b>
                  <#if curPartyContactMechPurpose.thruDate?exists>
                    (Expire:${curPartyContactMechPurpose.thruDate.toString()})
                  </#if>
                </div>
              </#list>
              <div class="tabletext">
                <#if curPostalAddress.toName?exists><b>To:</b> ${curPostalAddress.toName}<br></#if>
                <#if curPostalAddress.attnName?exists><b>Attn:</b> ${curPostalAddress.attnName}<br></#if>
                ${curPostalAddress.address1?if_exists}<br>
                <#if curPostalAddress.address2?exists>${curPostalAddress.address2}<br></#if>
                ${curPostalAddress.city}<#if curPostalAddress.stateProvinceGeoId?has_content>,&nbsp;${curPostalAddress.stateProvinceGeoId}</#if>&nbsp;${curPostalAddress.postalCode} 
                <#if curPostalAddress.countryGeoId?exists><br>${curPostalAddress.countryGeoId}</#if>
              </div>
              <div class="tabletext">(Updated:&nbsp;${(curPartyContactMech.fromDate.toString())?if_exists})</div>
              <#if curPartyContactMech.thruDate?exists><div class='tabletext'><b>Delete:&nbsp;${curPartyContactMech.thruDate.toString()}</b></div></#if>
            </td>
          </tr>
        <#else>
           <#-- <tr>
            <td align="left" valign="top" colspan='2'>
              <div class="tabletext">No Billing Address Selected</div>
            </td>
          </tr> -->
        </#if>
          <#-- is confusing
          <tr>
            <td align="left" valign="top" colspan='2'>
              <div class="tabletext"><b>Select a New Billing Address:</b></div>
            </td>
          </tr>
          -->
          <#list postalAddressInfos as postalAddressInfo>
            <#assign contactMech = postalAddressInfo.contactMech>
            <#assign partyContactMechPurposes = postalAddressInfo.partyContactMechPurposes>
            <#assign postalAddress = postalAddressInfo.postalAddress>
            <#assign partyContactMech = postalAddressInfo.partyContactMech>
            <tr>
              <td align="right" valign="top" width="1%">
                <input type='radio' name='contactMechId' value='${contactMech.contactMechId}'>
              </td>
              <td align="left" valign="top" width="80%">
                <#list partyContactMechPurposes as partyContactMechPurpose>
                    <#assign contactMechPurposeType = partyContactMechPurpose.getRelatedOneCache("ContactMechPurposeType")>
                    <div class="tabletext">
                      <b>${contactMechPurposeType.description?if_exists}</b>
                      <#if partyContactMechPurpose.thruDate?exists>(Expire:${partyContactMechPurpose.thruDate})</#if>
                    </div>
                </#list>
                <div class="tabletext">
                  <#if postalAddress.toName?exists><b>To:</b> ${postalAddress.toName}<br></#if>
                  <#if postalAddress.attnName?exists><b>Attn:</b> ${postalAddress.attnName}<br></#if>
                  ${postalAddress.address1?if_exists}<br>
                  <#if postalAddress.address2?exists>${postalAddress.address2}<br></#if>
                  ${postalAddress.city}<#if postalAddress.stateProvinceGeoId?has_content>,&nbsp;${postalAddress.stateProvinceGeoId}</#if>&nbsp;${postalAddress.postalCode} 
                  <#if postalAddress.countryGeoId?exists><br>${postalAddress.countryGeoId}</#if>
                </div>
                <div class="tabletext">(Updated:&nbsp;${(partyContactMech.fromDate.toString())?if_exists})</div>
                <#if partyContactMech.thruDate?exists><div class='tabletext'><b>Delete:&nbsp;${partyContactMech.thruDate.toString()}</b></div></#if>
              </td>
            </tr>
          </#list>
          <#if !postalAddressInfos?has_content && !curContactMech?exists>
              <tr><td colspan='2'><div class="tabletext">No contact information on file.</div></td></tr>
          </#if>
        </table>
      </td>
    </tr>
  </table>
  </form>

  &nbsp;<a href='<@ofbizUrl>/authview/${donePage}</@ofbizUrl>' class="buttontext">[Go&nbsp;Back]</a>
  &nbsp;<a href="javascript:document.editeftaccountform.submit()" class="buttontext">[Save]</a>
</#if>

