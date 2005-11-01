<#--
 *  Copyright (c) 2003-2005 The Open For Business Project - www.ofbiz.org
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
 *@author     Jean-Luc.Malet@nereide.biz (migration to uiLabelMap)
 *@version    $Rev$
 *@since      2.2
-->

<#if security.hasEntityPermission("ORDERMGR", "_CREATE", session) || security.hasEntityPermission("ORDERMGR", "_PURCHASE_CREATE", session)>
<table border="0" width='100%' cellspacing='0' cellpadding='0' class='boxoutside'>
  <tr>
    <td width='100%'>

      <#-- header table for company shipping addresses -->

      <br/>
      <table width="100%" border='0' cellspacing='0' cellpadding='0' class='boxtop'>
        <tr>
          <td><div class="boxhead">${uiLabelMap.OrderSelectAShippingAddress}</div></td>
          <td valign="middle" align="right">
            <a href="javascript:document.checkoutsetupform.submit();" class="submenutextright">${uiLabelMap.CommonContinue}</a>
          </td>
        </tr>
      </table>

      <table width='100%' border='0' cellspacing='0' cellpadding='0' class='boxbottom'>
        <tr>
          <td>
            <#if shippingContactMechListF?has_content>
            <table width="100%" border="0" cellpadding="1" cellspacing="0">
              <form method="post" action="<@ofbizUrl>finalizeOrder</@ofbizUrl>" name="checkoutsetupform">
                <input type="hidden" name="finalizeMode" value="ship">

                <tr><td colspan="4"><hr class='sepbar'></td></tr>

                <#-- company postal addresses -->
                
                <#list shippingContactMechListF as shippingContactMech>
                  <#if shippingContactMech.postalAddress?exists>
                  <#assign shippingAddress = shippingContactMech.postalAddress>
                  <tr>
                    <td valign="top" nowrap>
                      <input type="radio" name="shipping_contact_mech_id" value="${shippingAddress.contactMechId}" <#if cart.getShippingContactMechId()?default("") == shippingAddress.contactMechId>checked</#if>>
                    </td>
                    <td nowrap>&nbsp;&nbsp;&nbsp;&nbsp;</td>
                    <td align="left" valign="top" width="100%" nowrap>
                      <div class="tabletext">
                        <#if shippingAddress.toName?has_content><b>To:</b>&nbsp;${shippingAddress.toName}<br/></#if>
                        <#if shippingAddress.attnName?has_content><b>Attn:</b>&nbsp;${shippingAddress.attnName}<br/></#if>
                        <#if shippingAddress.address1?has_content>${shippingAddress.address1}<br/></#if>
                        <#if shippingAddress.address2?has_content>${shippingAddress.address2}<br/></#if>
                        <#if shippingAddress.city?has_content>${shippingAddress.city}</#if>
                        <#if shippingAddress.stateProvinceGeoId?has_content><br/>${shippingAddress.stateProvinceGeoId}</#if>
                        <#if shippingAddress.postalCode?has_content><br/>${shippingAddress.postalCode}</#if>
                        <#if shippingAddress.countryGeoId?has_content><br/>${shippingAddress.countryGeoId}</#if>
                      </div>
                    </td>
                    <td>
                      <div class="tabletext"><a href="/partymgr/control/editcontactmech?partyId=${orderParty.partyId}&contactMechId=${shippingAddress.contactMechId}" target="_blank" class="buttontext">${uiLabelMap.CommonUpdate}</a></div>
                    </td>
                  </tr>
                  <#if shippingContactMech_has_next>
                  <tr><td colspan="4"><hr class='sepbar'></td></tr>
                  </#if>
                  </#if>
                </#list>
              </form>
            </table>
            <#else>
            <#if shippingContactMechList?has_content && !requestParameters.createNew?exists>
            <table width="100%" border="0" cellpadding="1" cellspacing="0">
              <tr>
                <td colspan="3">
                  <a href="<@ofbizUrl>setShipping?createNew=Y</@ofbizUrl>" class="buttontext">${uiLabelMap.CommonCreateNew}</a>
                </td>
              </tr>
              <form method="post" action="<@ofbizUrl>finalizeOrder</@ofbizUrl>" name="checkoutsetupform"> 
                <input type="hidden" name="finalizeMode" value="ship">
                                
                <tr><td colspan="3"><hr class='sepbar'></td></tr>
                <#list shippingContactMechList as shippingContactMech>
                  <#assign shippingAddress = shippingContactMech.getRelatedOne("PostalAddress")>
                  <tr>
                    <td align="left" valign="top" width="1%" nowrap>
                      <input type="radio" name="shipping_contact_mech_id" value="${shippingAddress.contactMechId}" <#if cart.getShippingContactMechId()?default("") == shippingAddress.contactMechId>checked</#if>>        
                    </td>
                    <td align="left" valign="top" width="99%" nowrap>
                      <div class="tabletext">
                        <#if shippingAddress.toName?has_content><b>${uiLabelMap.CommonTo}:</b>&nbsp;${shippingAddress.toName}<br/></#if>
                        <#if shippingAddress.attnName?has_content><b>${uiLabelMap.CommonAttn}:</b>&nbsp;${shippingAddress.attnName}<br/></#if>
                        <#if shippingAddress.address1?has_content>${shippingAddress.address1}<br/></#if>
                        <#if shippingAddress.address2?has_content>${shippingAddress.address2}<br/></#if>
                        <#if shippingAddress.city?has_content>${shippingAddress.city}</#if>
                        <#if shippingAddress.stateProvinceGeoId?has_content><br/>${shippingAddress.stateProvinceGeoId}</#if>
                        <#if shippingAddress.postalCode?has_content><br/>${shippingAddress.postalCode}</#if>
                        <#if shippingAddress.countryGeoId?has_content><br/>${shippingAddress.countryGeoId}</#if>                                                                                     
                      </div>
                    </td>
                    <td>
                      <div class="tabletext"><a href="/partymgr/control/editcontactmech?partyId=${orderParty.partyId}&contactMechId=${shippingContactMech.contactMechId}" target="_blank" class="buttontext">${uiLabelMap.CommonUpdate}</a></div>
                    </td>                      
                  </tr>
                  <#if shippingContactMech_has_next>
                  <tr><td colspan="3"><hr class='sepbar'></td></tr>
                  </#if>
                </#list>
              </form>
            </table>  
            <#else>
              <#if postalAddress?has_content>            
              <form method="post" action="<@ofbizUrl>updatePostalAddressOrderEntry</@ofbizUrl>" name="checkoutsetupform">
                <input type="hidden" name="contactMechId" value="${shipContactMechId?if_exists}">
              <#else>
              <form method="post" action="<@ofbizUrl>createPostalAddress</@ofbizUrl>" name="checkoutsetupform">
                <input type="hidden" name="contactMechTypeId" value="POSTAL_ADDRESS">
                <input type="hidden" name="contactMechPurposeTypeId" value="SHIPPING_LOCATION">
              </#if>
                <input type="hidden" name="partyId" value="${cart.getPartyId()?default("_NA_")}">
                <input type="hidden" name="finalizeMode" value="ship">
                <#if orderPerson?exists && orderPerson?has_content>
                  <#assign toName = "">
                  <#if orderPerson.personalTitle?has_content><#assign toName = orderPerson.personalTitle + " "></#if>
                  <#assign toName = toName + orderPerson.firstName + " ">
                  <#if orderPerson.middleName?has_content><#assign toName = toName + orderPerson.middleName + " "></#if>
                  <#assign toName = toName + orderPerson.lastName>
                  <#if orderPerson.suffix?has_content><#assign toName = toName + " " + orderPerson.suffix></#if>
                <#else>
                  <#assign toName = "">
                </#if>
                <table width="100%" border="0" cellpadding="1" cellspacing="0">
                  <tr>
                    <td width="26%" align="right" valign="top"><div class="tabletext">${uiLabelMap.CommonToName}</div></td>
                    <td width="5">&nbsp;</td>
                    <td width="74%">
                      <input type="text" class="inputBox" size="30" maxlength="60" name="toName" value="${toName}">
                    </td>
                  </tr>
                  <tr>
                    <td width="26%" align="right" valign="top"><div class="tabletext">${uiLabelMap.CommonAttentionName}</div></td>
                    <td width="5">&nbsp;</td>
                    <td width="74%">
                      <input type="text" class="inputBox" size="30" maxlength="60" name="attnName" value="">
                    </td>
                  </tr>
                  <tr>
                    <td width="26%" align="right" valign="top"><div class="tabletext">${uiLabelMap.CommonAddressLine} 1</div></td>
                    <td width="5">&nbsp;</td>
                    <td width="74%">
                      <input type="text" class="inputBox" size="30" maxlength="30" name="address1" value="">
                    *</td>
                  </tr>
                  <tr>
                    <td width="26%" align="right" valign="top"><div class="tabletext">${uiLabelMap.CommonAddressLine} 2</div></td>
                    <td width="5">&nbsp;</td>
                    <td width="74%">
                      <input type="text" class="inputBox" size="30" maxlength="30" name="address2" value="">
                    </td>
                  </tr>
                  <tr>
                    <td width="26%" align="right" valign="top"><div class="tabletext">${uiLabelMap.CommonCity}</div></td>
                    <td width="5">&nbsp;</td>
                    <td width="74%">
                      <input type="text" class="inputBox" size="30" maxlength="30" name="city" value="">
                    *</td>
                  </tr>
                  <tr>
                    <td width="26%" align="right" valign="top"><div class="tabletext">${uiLabelMap.CommonStateProvince}</div></td>
                    <td width="5">&nbsp;</td>
                    <td width="74%">
                      <select name="stateProvinceGeoId" class="selectBox">
                        <option value=""></option>                       
                        ${screens.render("component://common/widget/CommonScreens.xml#states")}
                      </select>
                    </td>
                  </tr>
                  <tr>
                    <td width="26%" align="right" valign="top"><div class="tabletext">${uiLabelMap.CommonZipPostalCode}</div></td>
                    <td width="5">&nbsp;</td>
                    <td width="74%">
                      <input type="text" class="inputBox" size="12" maxlength="10" name="postalCode" value="">
                    *</td>
                  </tr>
                  <tr>
                    <td width="26%" align="right" valign="top"><div class="tabletext">${uiLabelMap.CommonCountry}</div></td>
                    <td width="5">&nbsp;</td>
                    <td width="74%">
                      <select name="countryGeoId" class="selectBox">                        
                        ${screens.render("component://common/widget/CommonScreens.xml#countries")}
                      </select>
                    *</td>
                  </tr>
                  <tr>
                    <td width="26%" align="right" valign="top"><div class="tabletext">${uiLabelMap.CommonAllowSolicitation}</div></td>
                    <td width="5">&nbsp;</td>
                    <td width="74%">
                      <select name="allowSolicitation" class='selectBox'>                       
                        <option></option><option>Y</option><option>N</option>
                      </select>
                    </td>
                  </tr>                                    
                </td>
                </table>
              </form>

            </#if>
            </#if>
          </td>
        </tr>
      </table>

      <#-- select a party id to ship to instead -->

      <br/>
      <form method="post" action="chooseOrderPartyAddress" name="partyshipform">

        <table width="100%" border='0' cellspacing='0' cellpadding='0' class='boxtop'>
          <tr>
           <td><div class="boxhead">${uiLabelMap.OrderShipToAnotherParty}</div></td>
            <td valign="middle" align="right">
              <a href="javascript:document.partyshipform.submit();" class="submenutextright">${uiLabelMap.CommonContinue}</a>
           </td>
          </tr>
        </table>

        <table width="100%" border="0" align="center" cellspacing='0' cellpadding='0' class='boxoutside'>
          <tr><td>
              <input type="hidden" name="contactMechPurposeTypeId" value="SHIPPING_LOCATION">
              <table width="100%" border='0' cellspacing='0' cellpadding='0' class='boxbottom'>
                <tr><td colspan="4">&nbsp;</td></tr>
                <tr>
                <td>&nbsp;</td>
                <td align='right' valign='middle' nowrap><div class='tableheadtext'>${uiLabelMap.PartyPartyId}</div></td>
                <td>&nbsp;</td>
                <td valign='middle'>
                  <div class='tabletext' valign='top'>
                    <input type='text' class='inputBox' name='partyId' value='${thisPartyId?if_exists}'>
                    <a href="javascript:call_fieldlookup2(document.partyshipform.partyId,'LookupPartyName');">
                    <img src='/images/fieldlookup.gif' width='15' height='14' border='0' alt='Click here For Field Lookup'>
                    </a>
                  </div>
                </td>
              </tr>
              <tr><td colspan="4">&nbsp;</td></tr>
            </table>
          </td></tr>
        </table>
      </form>

    </td>
  </tr>
</table>


    </td>
  </tr>
</table>

<br/>
<#else>
  <h3>${uiLabelMap.OrderViewPermissionError}</h3>
</#if>
