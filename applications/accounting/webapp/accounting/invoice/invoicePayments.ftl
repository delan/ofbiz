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
 *@author     Andy Zeneski (jaz@ofbiz.org)
 *@author     Eric.Barbier@nereide.biz (migration to uiLabelMap)
 *@version    $Rev$
 *@since      2.2
-->
<#assign uiLabelMap = requestAttributes.uiLabelMap>

<div class="head1">${uiLabelMap.AccountingInvoicePayments} - <a href="<@ofbizUrl>/viewInvoice?invoiceId=${invoiceId}</@ofbizUrl>" class="buttontext">#${invoiceId}</div>

<br>
<table width="100%" border="0" cellpadding="0" cellspacing="0">
  <tr>
    <td><div class="tableheadtext">${uiLabelMap.AccountingPayment} #</div></td>
    <td><div class="tableheadtext">${uiLabelMap.AccountingType}</div></td>
    <td><div class="tableheadtext">${uiLabelMap.AccountingStatus}</div></td>
    <td><div class="tableheadtext">${uiLabelMap.AccountingInvoice} #</div></td>
    <td><div class="tableheadtext">${uiLabelMap.AccountingInvoiceItem}</div></td>
    <td><div class="tableheadtext">${uiLabelMap.AccountingBillingAcct}</div></td>
    <td><div class="tableheadtext">${uiLabelMap.AccountingPaymentDate}</div></td>
    <td><div class="tableheadtext">Ref #</div></td>
    <td align="right"><div class="tableheadtext">${uiLabelMap.AccountingAmount}</div></td>
  </tr>
  <tr><td colspan="9"><hr class="sepbar"></td></tr>
  <#list payments as payment>
  <#assign paymentMethodType = payment.getRelatedOne("PaymentMethodType")>
  <#assign statusItem = payment.getRelatedOne("StatusItem")?if_exists>
  <tr>
    <td><div class="tabletext">${payment.paymentId?if_exists}</div></td>
    <td><div class="tabletext">${paymentMethodType.description?default("N/A")}</div></td>
    <td><div class="tabletext">${(statusItem.description)?if_exists}</div></td>
    <td><div class="tabletext">${payment.invoiceId}</div></td>
    <td><div class="tabletext">${payment.invoiceItemSeqId?default("N/A")}</div></td>
    <td><div class="tabletext">${payment.billingAccountId?default("N/A")}</div></td>
    <td><div class="tabletext">${payment.effectiveDate?string?if_exists}</div></td>
    <td><div class="tabletext">${payment.paymentRefNum?if_exists}</div></td>
    <td align="right"><div class="tabletext"><@ofbizCurrency amount=payment.amountApplied isoCode=payment.currencyUomId?if_exists/> of <@ofbizCurrency amount=payment.amount isoCode=payment.currencyUomId?if_exists/></div></td>
  </tr>
  </#list>
</table>
