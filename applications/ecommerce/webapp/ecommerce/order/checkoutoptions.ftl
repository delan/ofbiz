<#--
 *  Copyright (c) 2001-2005 The Open For Business Project - www.ofbiz.org
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
 *@author     Andy Zeneski (jaz@ofbiz.org)
 *@version    $Rev$
 *@since      2.1
-->

<script language="javascript" type="text/javascript">
function submitForm(form, mode, value) {
    if (mode == "DN") {
        // done action; checkout
        form.action="<@ofbizUrl>checkout</@ofbizUrl>";
        form.submit();
    } else if (mode == "CS") {
        // continue shopping
        form.action="<@ofbizUrl>updateCheckoutOptions/showcart</@ofbizUrl>";
        form.submit();
    } else if (mode == "NA") {
        // new address
        form.action="<@ofbizUrl>updateCheckoutOptions/editcontactmech?DONE_PAGE=quickcheckout&preContactMechTypeId=POSTAL_ADDRESS&contactMechPurposeTypeId=SHIPPING_LOCATION</@ofbizUrl>";
        form.submit();
    } else if (mode == "EA") {
        // edit address
        form.action="<@ofbizUrl>updateCheckoutOptions/editcontactmech?DONE_PAGE=quickcheckout&contactMechId="+value+"</@ofbizUrl>";
        form.submit();
    } else if (mode == "NC") {
        // new credit card
        form.action="<@ofbizUrl>updateCheckoutOptions/editcreditcard?DONE_PAGE=quickcheckout</@ofbizUrl>";
        form.submit();
    } else if (mode == "EC") {
        // edit credit card
        form.action="<@ofbizUrl>updateCheckoutOptions/editcreditcard?DONE_PAGE=quickcheckout&paymentMethodId="+value+"</@ofbizUrl>";
        form.submit();
    } else if (mode == "GC") {
        // edit gift card
        form.action="<@ofbizUrl>updateCheckoutOptions/editgiftcard?DONE_PAGE=quickcheckout&paymentMethodId="+value+"</@ofbizUrl>";
        form.submit();
    } else if (mode == "NE") {
        // new eft account
        form.action="<@ofbizUrl>updateCheckoutOptions/editeftaccount?DONE_PAGE=quickcheckout</@ofbizUrl>";
        form.submit();
    } else if (mode == "EE") {
        // edit eft account
        form.action="<@ofbizUrl>updateCheckoutOptions/editeftaccount?DONE_PAGE=quickcheckout&paymentMethodId="+value+"</@ofbizUrl>";
        form.submit();
    } else if (mode == "SP") {
        // split payment
        form.action="<@ofbizUrl>updateCheckoutOptions/checkoutpayment</@ofbizUrl>";
        form.submit();
    } else if (mode == "SA") {
        // selected shipping address
        form.action="<@ofbizUrl>updateCheckoutOptions/quickcheckout</@ofbizUrl>";
        form.submit();
    }
}

function toggleBillingAccount(box) {
    var amountName = "amount_" + box.value;
    box.checked = true;
    box.form.elements[amountName].disabled = false;

    for (var i = 0; i < box.form.elements[box.name].length; i++) {
        if (!box.form.elements[box.name][i].checked) {
            box.form.elements["amount_" + box.form.elements[box.name][i].value].disabled = true;
        }
    }
}
</script>

<#assign shipping = !shoppingCart.containAllWorkEffortCartItems()> <#-- contains items which need shipping? -->
<form method="post" name="checkoutInfoForm" style="margin:0;">
  <input type="hidden" name="checkoutpage" value="quick"/>
  <input type="hidden" name="DONE_PAGE" value="quickcheckout"/>
  <input type="hidden" name="BACK_PAGE" value="quickcheckout"/>

  <table width="100%" border="0" cellpadding="0" cellspacing="0">
    <tr valign="top" align="left">
      <td height="100%">
        <div class="screenlet" style="height: 100%;">
            <div class="screenlet-header">
                <#if shipping == true>
                    <div class="boxhead">1)&nbsp;${uiLabelMap.OrderWhereShallWeShipIt}?</div>
                <#else>
                    <div class="boxhead">1)&nbsp;${uiLabelMap.OrderInformationAboutYou}</div>
                </#if>
            </div>
            <div class="screenlet-body" style="height: 100%;">
                <table width="100%" border="0" cellpadding="1" cellspacing="0">
                  <tr>
                    <td colspan="2">
                      <span class="tabletext">${uiLabelMap.CommonAdd}:</span>
                      <a href="javascript:submitForm(document.checkoutInfoForm, 'NA', '');" class="buttontext">[${uiLabelMap.PartyAddNewAddress}]</a>
                    </td>
                  </tr>
                  <#if (shoppingCart.getTotalQuantity() > 1) && !shoppingCart.containAllWorkEffortCartItems()> <#-- no splitting when only rental items -->
                    <tr><td colspan="2"><hr class="sepbar"/></td></tr>
                    <tr>
                      <td colspan="2" align="center">
                        <a href="<@ofbizUrl>splitship</@ofbizUrl>" class="buttontext">[${uiLabelMap.OrderSplitIntoMultipleShipments}]</a>
                        <#if (shoppingCart.getShipGroupSize() > 1)>
                          <div class="tabletext" style="color: red;">${uiLabelMap.OrderNOTEMultipleShipmentsExist}.</div>
                        </#if>
                      </td>
                    </tr>
                  </#if>
                   <#if shippingContactMechList?has_content>
                     <tr><td colspan="2"><hr class="sepbar"/></td></tr>
                     <#list shippingContactMechList as shippingContactMech>
                       <#assign shippingAddress = shippingContactMech.getRelatedOne("PostalAddress")>
                       <tr>
                         <td align="left" valign="top" width="1%">
                           <input type="radio" name="shipping_contact_mech_id" value="${shippingAddress.contactMechId}" onclick="javascript:submitForm(document.checkoutInfoForm, 'SA', null);"<#if shoppingCart.getShippingContactMechId()?default("") == shippingAddress.contactMechId> checked="checked"</#if>/>
                         </td>
                         <td align="left" valign="top" width="99%">
                           <div class="tabletext">
                             <#if shippingAddress.toName?has_content><b>${uiLabelMap.CommonTo}:</b>&nbsp;${shippingAddress.toName}<br/></#if>
                             <#if shippingAddress.attnName?has_content><b>${uiLabelMap.PartyAddrAttnName}:</b>&nbsp;${shippingAddress.attnName}<br/></#if>
                             <#if shippingAddress.address1?has_content>${shippingAddress.address1}<br/></#if>
                             <#if shippingAddress.address2?has_content>${shippingAddress.address2}<br/></#if>
                             <#if shippingAddress.city?has_content>${shippingAddress.city}</#if>
                             <#if shippingAddress.stateProvinceGeoId?has_content><br/>${shippingAddress.stateProvinceGeoId}</#if>
                             <#if shippingAddress.postalCode?has_content><br/>${shippingAddress.postalCode}</#if>
                             <#if shippingAddress.countryGeoId?has_content><br/>${shippingAddress.countryGeoId}</#if>
                             <a href="javascript:submitForm(document.checkoutInfoForm, 'EA', '${shippingAddress.contactMechId}');" class="buttontext">[${uiLabelMap.CommonUpdate}]</a>
                           </div>
                         </td>
                       </tr>
                       <#if shippingContactMech_has_next>
                         <tr><td colspan="2"><hr class="sepbar"/></td></tr>
                       </#if>
                     </#list>
                   </#if>
                 </table>
            </div>
        </div>
      </td>
      <td bgcolor="white" width="1">&nbsp;&nbsp;</td>
      <td height="100%">
        <div class="screenlet" style="height: 100%;">
            <div class="screenlet-header">
                <#if shipping == true>
                    <div class="boxhead">2)&nbsp;${uiLabelMap.OrderHowShallWeShipIt}?</div>
                <#else>
                    <div class="boxhead">2)&nbsp;${uiLabelMap.OrderOptions}?</div>
                </#if>
            </div>
            <div class="screenlet-body" style="height: 100%;">
                <table width="100%" cellpadding="1" border="0" cellpadding="0" cellspacing="0">
                 <#if shipping == true>
                  <#list carrierShipmentMethodList as carrierShipmentMethod>
                    <#assign shippingMethod = carrierShipmentMethod.shipmentMethodTypeId + "@" + carrierShipmentMethod.partyId>
                    <tr>
                      <td width="1%" valign="top">
                        <input type="radio" name="shipping_method" value="${shippingMethod}" <#if shippingMethod == chosenShippingMethod?default("N@A")>checked="checked"</#if>/>
                      </td>
                      <td valign="top">
                        <div class="tabletext">
                          <#if shoppingCart.getShippingContactMechId()?exists>
                            <#assign shippingEst = shippingEstWpr.getShippingEstimate(carrierShipmentMethod)?default(-1)>
                          </#if>
                          <#if carrierShipmentMethod.partyId != "_NA_">${carrierShipmentMethod.partyId?if_exists}&nbsp;</#if>${carrierShipmentMethod.description?if_exists}
                          <#if shippingEst?has_content> - <#if (shippingEst > -1)?exists><@ofbizCurrency amount=shippingEst isoCode=shoppingCart.getCurrency()/><#else>${uiLabelMap.OrderCalculatedOffline}</#if></#if>
                        </div>
                      </td>
                    </tr>
                  </#list>
                  <#if !carrierShipmentMethodList?exists || carrierShipmentMethodList?size == 0>
                    <tr>
                      <td width="1%" valign="top">
                        <input type="radio" name="shipping_method" value="Default" checked="checked"/>
                      </td>
                      <td valign="top">
                        <div class="tabletext">${uiLabelMap.OrderUseDefault}.</div>
                      </td>
                    </tr>
                  </#if>
                  <tr><td colspan="2"><hr class="sepbar"/></td></tr>
                  <tr>
                    <td colspan="2">
                      <div class="head2"><b>${uiLabelMap.OrderShipAllAtOnce}?</b></div>
                    </td>
                  </tr>
                  <tr>
                    <td valign="top">
                      <input type="radio" <#if shoppingCart.getMaySplit()?default("N") == "N">checked="checked"</#if> name="may_split" value="false"/>
                    </td>
                    <td valign="top">
                      <div class="tabletext">${uiLabelMap.OrderPleaseWaitUntilBeforeShipping}.</div>
                    </td>
                  </tr>
                  <tr>
                    <td valign="top">
                      <input <#if shoppingCart.getMaySplit()?default("N") == "Y">checked="checked"</#if> type="radio" name="may_split" value="true"/>
                    </td>
                    <td valign="top">
                      <div class="tabletext">${uiLabelMap.OrderPleaseShipItemsBecomeAvailable}.</div>
                    </td>
                  </tr>
                  <tr><td colspan="2"><hr class="sepbar"/></td></tr>
                 <#else/>
                    <input type="hidden" name="shipping_method" value="NO_SHIPPING@_NA_"/>
                    <input type="hidden" name="may_split" value="false"/>
                    <input type="hidden" name="is_gift" value="false"/>
                 </#if>
                  <tr>
                    <td colspan="2">
                      <div class="head2"><b>${uiLabelMap.OrderSpecialInstructions}</b></div>
                    </td>
                  </tr>
                  <tr>
                    <td colspan="2">
                      <textarea class="textAreaBox" cols="30" rows="3" wrap="hard" name="shipping_instructions">${shoppingCart.getShippingInstructions()?if_exists}</textarea>
                    </td>
                  </tr>
                  <tr><td colspan="2"><hr class="sepbar"/></td></tr>
                  <tr>
                    <td colspan="2">
                      <span class="head2"><b>${uiLabelMap.OrderPoNumber}</b></span>&nbsp;
                      <#if shoppingCart.getPoNumber()?exists && shoppingCart.getPoNumber() != "(none)">
                        <#assign currentPoNumber = shoppingCart.getPoNumber()>
                      </#if>
                      <input type="text" class="inputBox" name="corresponding_po_id" size="15" value="${currentPoNumber?if_exists}">
                    </td>
                  </tr>
                 <#if shipping == true>
                  <#if productStore.showCheckoutGiftOptions?if_exists != "N">
                  <tr><td colspan="2"><hr class="sepbar"/></td></tr>
                  <tr>
                    <td colspan="2">
                      <div>
                        <span class="head2"><b>${uiLabelMap.OrderIsThisGift}?</b></span>
                        <input type="radio" <#if shoppingCart.getIsGift()?default("Y") == "Y">checked="checked"</#if> name="is_gift" value="true"><span class="tabletext">${uiLabelMap.CommonYes}</span>
                        <input type="radio" <#if shoppingCart.getIsGift()?default("N") == "N">checked="checked"</#if> name="is_gift" value="false"><span class="tabletext">${uiLabelMap.CommonNo}</span>
                      </div>
                    </td>
                  </tr>
                  <tr><td colspan="2"><hr class="sepbar"/></td></tr>
                  <tr>
                    <td colspan="2">
                      <div class="head2"><b>${uiLabelMap.OrderGiftMessage}</b></div>
                    </td>
                  </tr>
                  <tr>
                    <td colspan="2">
                      <textarea class="textAreaBox" cols="30" rows="3" wrap="hard" name="gift_message">${shoppingCart.getGiftMessage()?if_exists}</textarea>
                    </td>
                  </tr>
                  <#else/>
                  <input type="hidden" name="is_gift" value="false"/>
                  </#if>
                 </#if>
                  <tr><td colspan="2"><hr class="sepbar"/></td></tr>
                  <tr>
                    <td colspan="2">
                      <div class="head2"><b>${uiLabelMap.PartyEmailAddresses}</b></div>
                    </td>
                  </tr>
                  <tr>
                    <td colspan="2">
                      <div class="tabletext">${uiLabelMap.OrderEmailSentToFollowingAddresses}:</div>
                      <div class="tabletext">
                      <b>
                      <#list emailList as email>
                        ${email.infoString?if_exists}<#if email_has_next>,</#if>
                      </#list>
                      </b>
                      </div>
                      <div class="tabletext">${uiLabelMap.OrderUpdateEmailAddress} <a href="<@ofbizUrl>viewprofile?DONE_PAGE=quickcheckout</@ofbizUrl>" class="buttontext">${uiLabelMap.PartyProfile}</a>.</div>
                      <br/>
                      <div class="tabletext">${uiLabelMap.OrderCommaSeperatedEmailAddresses}:</div>
                      <input type="text" class="inputBox" size="30" name="order_additional_emails" value="${shoppingCart.getOrderAdditionalEmails()?if_exists}"/>
                    </td>
                  </tr>
                </table>
            </div>
        </div>

      </td>
      <td bgcolor="white" width="1">&nbsp;&nbsp;</td>
      <td height="100%">
          <#-- Payment Method Selection -->

        <div class="screenlet" style="height: 100%;">
            <div class="screenlet-header">
                <div class="boxhead">3)&nbsp;${uiLabelMap.OrderHowShallYouPay}?</div>
            </div>
            <div class="screenlet-body" style="height: 100%;">
                <table width="100%" cellpadding="1" cellspacing="0" border="0">
                  <tr>
                    <td colspan="2">
                      <span class="tabletext">${uiLabelMap.CommonAdd}:</span>
                      <#if productStorePaymentMethodTypeIdMap.CREDIT_CARD?exists>
                        <a href="javascript:submitForm(document.checkoutInfoForm, 'NC', '');" class="buttontext">[${uiLabelMap.AccountingCreditCard}]</a>
                      </#if>
                      <#if productStorePaymentMethodTypeIdMap.EFT_ACCOUNT?exists>
                        <a href="javascript:submitForm(document.checkoutInfoForm, 'NE', '');" class="buttontext">[${uiLabelMap.AccountingEftAccount}]</a>
                      </#if>
                    </td>
                  </tr>
                  <tr><td colspan="2"><hr class="sepbar"/></td></tr>
                  <tr>
                    <td colspan="2" align="center">
                      <a href="javascript:submitForm(document.checkoutInfoForm, 'SP', '');" class="buttontext">[${uiLabelMap.AccountingSplitPayment}]</a>
                    </td>
                  </tr>
                  <tr><td colspan="2"><hr class="sepbar"/></td></tr>
                  <#if productStorePaymentMethodTypeIdMap.EXT_OFFLINE?exists>
                  <tr>
                    <td width="1%">
                      <input type="radio" name="checkOutPaymentId" value="EXT_OFFLINE" <#if "EXT_OFFLINE" == checkOutPaymentId>checked="checked"</#if>/>
                    </td>
                    <td width="50%">
                      <span class="tabletext">${uiLabelMap.OrderMoneyOrder}</span>
                    </td>
                  </tr>
                  </#if>
                  <#if productStorePaymentMethodTypeIdMap.EXT_COD?exists>
                  <tr>
                    <td width="1%">
                      <input type="radio" name="checkOutPaymentId" value="EXT_COD" <#if "EXT_COD" == checkOutPaymentId>checked="checked"</#if>/>
                    </td>
                    <td width="50%">
                      <span class="tabletext">${uiLabelMap.OrderCOD}</span>
                    </td>
                  </tr>
                  </#if>
                  <#if productStorePaymentMethodTypeIdMap.EXT_WORLDPAY?exists>
                  <tr>
                    <td width="1%">
                      <input type="radio" name="checkOutPaymentId" value="EXT_WORLDPAY" <#if "EXT_WORLDPAY" == checkOutPaymentId>checked="checked"</#if>/>
                    </td>
                    <td width="50%">
                      <span class="tabletext">${uiLabelMap.AccountingPayWithWorldPay}</span>
                    </td>
                  </tr>
                  </#if>
                  <#if productStorePaymentMethodTypeIdMap.EXT_PAYPAL?exists>
                  <tr>
                    <td width="1%">
                      <input type="radio" name="checkOutPaymentId" value="EXT_PAYPAL" <#if "EXT_PAYPAL" == checkOutPaymentId>checked="checked"</#if>/>
                    </td>
                    <td width="50%">
                      <span class="tabletext">${uiLabelMap.AccountingPayWithPayPal}</span>
                    </td>
                  </tr>
                  </#if>
                  <tr><td colspan="2"><hr class="sepbar"/></td></tr>

                  <#if !paymentMethodList?has_content>
                      <tr>
                        <td colspan="2">
                          <div class="tabletext"><b>${uiLabelMap.AccountingNoPaymentMethodsOnFile}.</b></div>
                        </td>
                      </tr>
                  <#else>
                  <#list paymentMethodList as paymentMethod>
                    <#if paymentMethod.paymentMethodTypeId == "CREDIT_CARD">
                     <#if productStorePaymentMethodTypeIdMap.CREDIT_CARD?exists>
                      <#assign creditCard = paymentMethod.getRelatedOne("CreditCard")>
                      <tr>
                        <td width="1%">
                          <input type="radio" name="checkOutPaymentId" value="${paymentMethod.paymentMethodId}" <#if shoppingCart.isPaymentSelected(paymentMethod.paymentMethodId)>checked="checked"</#if>/>
                        </td>
                        <td width="50%">
                          <span class="tabletext">CC:&nbsp;${Static["org.ofbiz.party.contact.ContactHelper"].formatCreditCard(creditCard)}</span>
                          <a href="javascript:submitForm(document.checkoutInfoForm, 'EC', '${paymentMethod.paymentMethodId}');" class="buttontext">[${uiLabelMap.CommonUpdate}]</a>
                        </td>
                      </tr>
                     </#if>
                    <#elseif paymentMethod.paymentMethodTypeId == "EFT_ACCOUNT">
                     <#if productStorePaymentMethodTypeIdMap.EFT_ACCOUNT?exists>
                      <#assign eftAccount = paymentMethod.getRelatedOne("EftAccount")>
                      <tr>
                        <td width="1%">
                          <input type="radio" name="checkOutPaymentId" value="${paymentMethod.paymentMethodId}" <#if shoppingCart.isPaymentSelected(paymentMethod.paymentMethodId)>checked="checked"</#if>/>
                        </td>
                        <td width="50%">
                          <span class="tabletext">EFT:&nbsp;${eftAccount.bankName?if_exists}: ${eftAccount.accountNumber?if_exists}</span>
                          <a href="javascript:submitForm(document.checkoutInfoForm, 'EE', '${paymentMethod.paymentMethodId}');" class="buttontext">[${uiLabelMap.CommonUpdate}]</a>
                        </td>
                      </tr>
                     </#if>
                    <#elseif paymentMethod.paymentMethodTypeId == "GIFT_CARD">
                     <#if productStorePaymentMethodTypeIdMap.GIFT_CARD?exists>
                      <#assign giftCard = paymentMethod.getRelatedOne("GiftCard")>

                      <#if giftCard?has_content && giftCard.cardNumber?has_content>
                        <#assign giftCardNumber = "">
                        <#assign pcardNumber = giftCard.cardNumber>
                        <#if pcardNumber?has_content>
                          <#assign psize = pcardNumber?length - 4>
                          <#if 0 < psize>
                            <#list 0 .. psize-1 as foo>
                              <#assign giftCardNumber = giftCardNumber + "*">
                            </#list>
                            <#assign giftCardNumber = giftCardNumber + pcardNumber[psize .. psize + 3]>
                          <#else>
                            <#assign giftCardNumber = pcardNumber>
                          </#if>
                        </#if>
                      </#if>

                      <tr>
                        <td width="1%">
                          <input type="radio" name="checkOutPaymentId" value="${paymentMethod.paymentMethodId}" <#if shoppingCart.isPaymentSelected(paymentMethod.paymentMethodId)>checked="checked"</#if>/>
                        </td>
                        <td width="50%">
                          <span class="tabletext">${uiLabelMap.AccountingGift}:&nbsp;${giftCardNumber}</span>
                          <a href="javascript:submitForm(document.checkoutInfoForm, 'EG', '${paymentMethod.paymentMethodId}');" class="buttontext">[${uiLabelMap.CommonUpdate}]</a>
                        </td>
                      </tr>
                     </#if>
                    </#if>
                  </#list>
                  </#if>

                <#-- special billing account functionality to allow use w/ a payment method -->
                <#if productStorePaymentMethodTypeIdMap.EXT_BILLACT?exists>
                  <#if billingAccountList?has_content>
                    <tr><td colspan="2"><hr class="sepbar"/></td></tr>
                    <tr>
                      <td width="1%">
                        <input type="radio" name="checkOutPaymentId" value="EXT_BILLACT" <#if "EXT_BILLACT" == checkOutPaymentId>checked="checked"</#if>/></hr>
                      </td>
                      <td width="50%">
                        <span class="tabletext">${uiLabelMap.AccountingPayOnlyWithBillingAccount}</span>
                      </td>
                    </tr>
                    <tr><td colspan="2"><hr class="sepbar"/></td></tr>
                    <#list billingAccountList as billingAccount>
                      <#assign availableAmount = billingAccount.accountLimit?double - billingAccount.accountBalance?double>
                      <tr>
                        <td align="left" valign="top" width="1%">
                          <input type="radio" onClick="javascript:toggleBillingAccount(this);" name="billingAccountId" value="${billingAccount.billingAccountId}" <#if (billingAccount.billingAccountId == selectedBillingAccount?default(""))>checked="checked"</#if>/>
                        </td>
                        <td align="left" valign="top" width="99%">
                          <div class="tabletext">
                           ${billingAccount.description?default("Bill Account")} [${uiLabelMap.OrderNbr}<b>${billingAccount.billingAccountId}</b>]&nbsp;(<@ofbizCurrency amount=availableAmount isoCode=billingAccount.accountCurrencyUomId?default(shoppingCart.getCurrency())/>)<br/>
                           <b>${uiLabelMap.OrderBillUpTo}:</b> <input type="text" size="8" class="inputBox" name="amount_${billingAccount.billingAccountId}" value="${availableAmount?double?string("##0.00")}" <#if !(billingAccount.billingAccountId == selectedBillingAccount?default(""))>disabled</#if>>
                          </div>
                        </td>
                      </tr>
                    </#list>
                    <tr>
                      <td align="left" valign="top" width="1%">
                        <input type="radio" onClick="javascript:toggleBillingAccount(this);" name="billingAccountId" value="_NA" <#if (selectedBillingAccount?default("") == "N")>checked="checked"</#if>/>
                        <input type="hidden" name="_NA_amount" value="0.00"/>
                      </td>
                      <td align="left" valign="top" width="99%">
                        <div class="tabletext">${uiLabelMap.AccountingNoBillingAccount}</div>
                       </td>
                    </tr>
                  </#if>
                </#if>
                <#-- end of special billing account functionality -->

                <#if productStorePaymentMethodTypeIdMap.GIFT_CARD?exists>
                  <tr><td colspan="2"><hr class="sepbar"/></td></tr>
                  <tr>
                    <td width="1%">
                      <input type="checkbox" name="addGiftCard" value="Y"/>
                    </td>
                    <td width="50%">
                      <span class="tabletext">${uiLabelMap.AccountingUseGiftCardNotOnFile}</span>
                    </td>
                  </tr>
                  <tr>
                    <td width="1%">
                      <div class="tabletext">${uiLabelMap.AccountingNumber}</div>
                    </td>
                    <td width="50%">
                      <input type="text" size="15" class="inputBox" name="giftCardNumber" value="${(requestParameters.giftCardNumber)?if_exists}" onFocus="document.checkoutInfoForm.addGiftCard.checked=true;"/>
                    </td>
                  </tr>
                  <tr>
                    <td width="1%">
                      <div class="tabletext">${uiLabelMap.AccountingPIN}</div>
                    </td>
                    <td width="50%">
                      <input type="text" size="10" class="inputBox" name="giftCardPin" value="${(requestParameters.giftCardPin)?if_exists}" onFocus="document.checkoutInfoForm.addGiftCard.checked=true;"/>
                    </td>
                  </tr>
                  <tr>
                    <td width="1%">
                      <div class="tabletext">${uiLabelMap.AccountingAmount}</div>
                    </td>
                    <td width="50%">
                      <input type="text" size="6" class="inputBox" name="giftCardAmount" value="${(requestParameters.giftCardAmount)?if_exists}" onFocus="document.checkoutInfoForm.addGiftCard.checked=true;"/>
                    </td>
                  </tr>
                </#if>
                </table>
            </div>
        </div>
        <#-- End Payment Method Selection -->
      </td>
    </tr>
  </table>
</form>

<table width="100%">
  <tr valign="top">
    <td align="left">
      &nbsp;<a href="javascript:submitForm(document.checkoutInfoForm, 'CS', '');" class="buttontextbig">[${uiLabelMap.OrderBacktoShoppingCart}]</a>
    </td>
    <td align="right">
      <a href="javascript:submitForm(document.checkoutInfoForm, 'DN', '');" class="buttontextbig">[${uiLabelMap.OrderContinueToFinalOrderReview}]</a>
    </td>
  </tr>
</table>
