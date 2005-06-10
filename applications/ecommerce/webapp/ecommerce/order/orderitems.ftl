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
 *@author     Andy Zeneski (jaz@ofbiz.org)
 *@author     David E. Jones (jonesde@ofbiz.org)
 *@version    $Rev$
 *@since      2.1
-->

<#-- NOTE: this template is used for the orderstatus screen in ecommerce AND for order notification emails through the OrderNoticeEmail.ftl file -->
<#-- the "urlPrefix" value will be prepended to URLs by the ofbizUrl transform if/when there is no "request" object in the context -->
<#if baseEcommerceSecureUrl?exists><#assign urlPrefix = baseEcommerceSecureUrl/></#if>

<table border="0" cellspacing="0" cellpadding="0" class="boxoutside">
  <tr>
    <td width="100%">
      <table width="100%" border="0" cellspacing="0" cellpadding="0" class="boxtop">
        <tr>
          <td valign="middle" align="left">
            <div class="boxhead">&nbsp; ${uiLabelMap.OrderOrderItems}</div>
          </td>
          <#if maySelectItems?default("N") == "Y">
            <td valign="middle" align="right" nowrap>
              <a href="javascript:document.addCommonToCartForm.add_all.value='true';document.addCommonToCartForm.submit()" class="submenutext">${uiLabelMap.EcommerceAddAlltoCart}</a><a href="javascript:document.addCommonToCartForm.add_all.value='false';document.addCommonToCartForm.submit()" class="submenutext">${uiLabelMap.EcommerceAddCheckedToCart}</a><a href="<@ofbizUrl>createShoppingListFromOrder?order_id=${orderHeader.orderId}&orderId=${orderHeader.orderId}&frequency=6&intervalNumber=1&shoppingListTypeId=SLT_AUTO_REODR</@ofbizUrl>" class="submenutextright">${uiLabelMap.OrderSendMeThisEveryMonth}</a>
            </td>
          </#if>
        </tr>
      </table>
    </td>
  </tr>
  <tr>
    <td width="100%">
      <table width="100%" border="0" cellspacing="0" cellpadding="0" class="boxbottom">
        <tr>
          <td>
            <table width="100%" border="0" cellpadding="0">
              <tr align="left" valign="bottom">
                <td width="35%" align="left"><span class="tableheadtext"><b>${uiLabelMap.EcommerceProduct}</b></span></td>               
                <#if maySelectItems?default("N") == "Y">
                  <td width="10%" align="right"><span class="tableheadtext"><b>${uiLabelMap.OrderQtyOrdered}</b></span></td>
                  <td width="10%" align="right"><span class="tableheadtext"><b>${uiLabelMap.OrderQtyShipped}</b></span></td>
                  <td width="10%" align="right"><span class="tableheadtext"><b>${uiLabelMap.OrderQtyCanceled}</b></span></td>
                <#else>
                  <td width="10%" align="right">&nbsp;</td>
                  <td width="10%" align="right">&nbsp;</td>
                  <td width="10%" align="right"><span class="tableheadtext"><b>${uiLabelMap.OrderQtyOrdered}</b></span></td>
                </#if>
                <td width="10%" align="right"><span class="tableheadtext"><b>${uiLabelMap.EcommerceUnitPrice}</b></span></td>
                <td width="10%" align="right"><span class="tableheadtext"><b>${uiLabelMap.OrderAdjustments}</b></span></td>
                <td width="10%" align="right"><span class="tableheadtext"><b>${uiLabelMap.CommonSubtotal}</b></span></td>
                <#if maySelectItems?default("N") == "Y">
                  <td width="5%" align="right">&nbsp;</td>
                </#if>
              </tr>
              <#list orderItems as orderItem>
                <#-- get info from workeffort and calculate rental quantity, if it was a rental item -->
                <#assign rentalQuantity = 1> <#-- no change if no rental item -->
                <#if orderItem.orderItemTypeId == "RENTAL_ORDER_ITEM" && workEfforts?exists>
                    <#list workEfforts as workEffort>
                        <#if workEffort.workEffortId == orderItem.orderItemSeqId>
                            <#assign rentalQuantity = localOrderReadHelper.getWorkEffortRentalQuantity(workEffort)>
                            <#assign workEffortSave = workEffort>
                          <#break>
                          </#if>
                      </#list>
                  <#else> 
                      <#assign WorkOrderItemFulfillments = orderItem.getRelatedCache("WorkOrderItemFulfillment")?if_exists>
                      <#if WorkOrderItemFulfillments?has_content>
                        <#list WorkOrderItemFulfillments as WorkOrderItemFulfillment>
                          <#assign workEffortSave = WorkOrderItemFulfillment.getRelatedOneCache("WorkEffort")?if_exists>
                          <#break>
                         </#list>
                      </#if>
                </#if>
                <tr><td colspan="10"><hr class="sepbar"/></td></tr>
                <tr>
                  <#if orderItem.productId == "_?_">
                    <td colspan="1" valign="top">
                      <b><div class="tabletext"> &gt;&gt; ${orderItem.itemDescription}</div></b>
                    </td>
                  <#else>
                    <td valign="top">
                      <div class="tabletext">
                        <a href="<@ofbizUrl>product?product_id=${orderItem.productId}</@ofbizUrl>" class="buttontext">${orderItem.productId} - ${orderItem.itemDescription}</a>
                      </div>
                      <#if maySelectItems?default("N") == "Y">
                        <#assign returns = orderItem.getRelated("ReturnItem")?if_exists>
                        <#if returns?has_content>
                          <#list returns as return>
                            <#assign returnHeader = return.getRelatedOne("ReturnHeader")>
                            <#if returnHeader.statusId != "RETURN_CANCELLED">
                              <#if returnHeader.statusId == "RETURN_REQUESTED" || returnHeader.statusId == "RETURN_APPROVED">
                                <#assign displayState = "Return Pending">
                              <#else>
                                <#assign displayState = "Returned">
                              </#if>
                              <div class="tabletext"><font color="red"><b>${displayState}</b></font> (#${return.returnId})</div>
                            </#if>
                          </#list>
                        </#if>
                      </#if>
                    </td>
                    <#if !(maySelectItems?default("N") == "Y")>
                      <td>&nbsp;</td>
                      <td>&nbsp;</td>
                    </#if>
                    <td align="right" valign="top">
                      <div class="tabletext">${orderItem.quantity?string.number}</div>                        
                    </td>
                    <#if maySelectItems?default("N") == "Y">
                    <td align="right" valign="top">
                      <#assign shippedQty = localOrderReadHelper.getItemShippedQuantity(orderItem)>
                      <div class="tabletext">${shippedQty?default(0)?string.number}</div>
                    </td>
                    <td align="right" valign="top">
                      <#assign canceledQty = localOrderReadHelper.getItemCanceledQuantity(orderItem)>
                      <div class="tabletext">${canceledQty?default(0)?string.number}</div>
                    </td>
                    </#if>
                    <td align="right" valign="top">
                      <div class="tabletext"><@ofbizCurrency amount=orderItem.unitPrice isoCode=currencyUomId/></div>
                    </td>
                    <td align="right" valign="top">
                      <div class="tabletext"><@ofbizCurrency amount=localOrderReadHelper.getOrderItemAdjustmentsTotal(orderItem) isoCode=currencyUomId/></div>
                    </td>
                    <td align="right" valign="top">
                    <#if workEfforts?exists>
                       <div class="tabletext"><@ofbizCurrency amount=localOrderReadHelper.getOrderItemTotal(orderItem)*rentalQuantity isoCode=currencyUomId/></div>
                    <#else>                                          
                      <div class="tabletext"><@ofbizCurrency amount=localOrderReadHelper.getOrderItemTotal(orderItem) isoCode=currencyUomId/></div>
                      </#if>
                    </td>                    
                    <#if maySelectItems?default("N") == "Y">
                      <td>&nbsp;</td>
                      <#if (orderHeader.statusId != "ORDER_SENT" && orderItem.statusId != "ITEM_COMPLETED" && orderItem.statusId != "ITEM_CANCELLED")>
                        <td><a href="<@ofbizUrl>cancelOrderItem?order_id=${orderItem.orderId}&item_seq=${orderItem.orderItemSeqId}</@ofbizUrl>" class="buttontext">${uiLabelMap.CommonCancel}</a></td>
                      <#else>
                        <td>&nbsp;</td>
                      </#if>
                      <td>
                        <input name="item_id" value="${orderItem.orderItemSeqId}" type="checkbox">
                      </td>
                    </#if>
                  </#if>
                </tr>
                <#-- show info from workeffort if it was a rental item -->
                <#if orderItem.orderItemTypeId == "RENTAL_ORDER_ITEM">
                    <#if workEffortSave?exists>
                          <tr><td>&nbsp;</td><td colspan="8"><div class="tabletext">${uiLabelMap..CommonFrom}: ${workEffortSave.estimatedStartDate?string("yyyy-MM-dd")} ${uiLabelMap.CommonUntil} ${workEffortSave.estimatedCompletionDate?string("yyyy-MM-dd")} ${uiLabelMap.CommonFor} ${workEffortSave.reservPersons} ${uiLabelMap.CommonPerson}(s).</div></td></tr>
                      </#if>
                </#if>
                <#-- now show adjustment details per line item -->
                <#assign itemAdjustments = localOrderReadHelper.getOrderItemAdjustments(orderItem)>
                <#list itemAdjustments as orderItemAdjustment>
                  <tr>
                    <td align="right">
                      <div class="tabletext" style="font-size: xx-small;">
                        <b><i>${uiLabelMap.EcommerceAdjustment}</i>:</b> <b>${localOrderReadHelper.getAdjustmentType(orderItemAdjustment)}</b>&nbsp;
                        <#if orderItemAdjustment.description?has_content>: ${orderItemAdjustment.description}</#if>

                        <#if orderItemAdjustment.orderAdjustmentTypeId == "SALES_TAX">
                          <#if orderItemAdjustment.primaryGeoId?has_content>
                            <#assign primaryGeo = orderItemAdjustment.getRelatedOneCache("PrimaryGeo")/>
                            <b>${uiLabelMap.EcommerceJurisdiction}:</b> ${primaryGeo.geoName} [${primaryGeo.abbreviation?if_exists}]
                            <#if orderItemAdjustment.secondaryGeoId?has_content>
                              <#assign secondaryGeo = orderItemAdjustment.getRelatedOneCache("SecondaryGeo")/>
                              (<b>${uiLabelMap.CommonIn}:</b> ${secondaryGeo.geoName} [${secondaryGeo.abbreviation?if_exists}])
                            </#if>
                          </#if>
                          <#if orderItemAdjustment.sourcePercentage?exists><b>${uiLabelMap.EcommerceRate}:</b> ${orderItemAdjustment.sourcePercentage}</#if>
                          <#if orderItemAdjustment.customerReferenceId?has_content><b>${uiLabelMap.CustomerTaxID}:</b> ${orderItemAdjustment.customerReferenceId}</#if>
                          <#if orderItemAdjustment.exemptAmount?exists><b>${uiLabelMap.EcommerceExemptAmount}:</b> ${orderItemAdjustment.exemptAmount}</#if>
                        </#if>
                      </div>
                    </td>
                    <td>&nbsp;</td>
                    <td>&nbsp;</td>
                    <td>&nbsp;</td>
                    <td align="right">
                      <div class="tabletext" style="font-size: xx-small;"><@ofbizCurrency amount=localOrderReadHelper.getOrderItemAdjustmentTotal(orderItem, orderItemAdjustment) isoCode=currencyUomId/></div>
                    </td>
                    <td>&nbsp;</td>
                    <#if maySelectItems?default("N") == "Y"><td>&nbsp;</td></#if>
                  </tr>
                </#list>

                <#-- show the order item ship group info -->
                <#assign orderItemShipGroupAssocs = orderItem.getRelated("OrderItemShipGroupAssoc")?if_exists>
                <#if orderItemShipGroupAssocs?has_content>
                  <#list orderItemShipGroupAssocs as shipGroupAssoc>
                    <#assign shipGroup = shipGroupAssoc.getRelatedOne("OrderItemShipGroup")?if_exists>
                    <#assign shipGroupAddress = (shipGroup.getRelatedOne("PostalAddress"))?if_exists>
                    <tr>
                      <td align="right">
                        <div class="tabletext" style="font-size: xx-small;"><b><i>${uiLabelMap.OrderShipGroup}</i>:</b> [${shipGroup.shipGroupSeqId}] ${shipGroupAddress.address1?default("N/A")}</div>
                      </td>
                      <td align="right">
                        <div class="tabletext" style="font-size: xx-small;">${shipGroupAssoc.quantity?string.number}</div>
                      </td>
                      <td>&nbsp;</td>
                      <td>&nbsp;</td>
                      <td>&nbsp;</td>
                      <td>&nbsp;</td>
                      <td>&nbsp;</td>
                    </tr>
                  </#list>
                </#if>

               </#list>
               <#if orderItems?size == 0 || !orderItems?has_content>
                 <tr><td><font color="red">${uiLabelMap.OrderSalesOrderLookupFailed}.</font></td></tr>
               </#if>

              <tr><td colspan="10"><hr class="sepbar"/></td></tr>
              <tr>
                <td align="right" colspan="6"><div class="tabletext"><b>${uiLabelMap.CommonSubtotal}</b></div></td>
                <td align="right"><div class="tabletext"><@ofbizCurrency amount=orderSubTotal isoCode=currencyUomId/></div></td>
              </tr>
              <#list headerAdjustmentsToShow as orderHeaderAdjustment>
                <tr>
                  <td align="right" colspan="6"><div class="tabletext"><b>${localOrderReadHelper.getAdjustmentType(orderHeaderAdjustment)}</b></div></td>
                  <td align="right"><div class="tabletext"><@ofbizCurrency amount=localOrderReadHelper.getOrderAdjustmentTotal(orderHeaderAdjustment) isoCode=currencyUomId/></div></td>
                </tr>
              </#list>
              <tr>
                <td align="right" colspan="6"><div class="tabletext"><b>${uiLabelMap.OrderShippingAndHandling}</b></div></td>
                <td align="right"><div class="tabletext"><@ofbizCurrency amount=orderShippingTotal isoCode=currencyUomId/></div></td>
              </tr>
              <tr>
                <td align="right" colspan="6"><div class="tabletext"><b>${uiLabelMap.OrderSalesTax}</b></div></td>
                <td align="right"><div class="tabletext"><@ofbizCurrency amount=orderTaxTotal isoCode=currencyUomId/></div></td>
              </tr>

              <tr><td colspan="2"></td><td colspan="9"><hr class="sepbar"/></td></tr>
              <tr>
                <td align="right" colspan="6"><div class="tabletext"><b>${uiLabelMap.OrderGrandTotal}</b></div></td>
                <td align="right">
                  <div class="tabletext"><@ofbizCurrency amount=orderGrandTotal isoCode=currencyUomId/></div>
                </td>
              </tr>
            </table>
          </td>
        </tr>
      </table>
    </td>
  </tr>
</table>
