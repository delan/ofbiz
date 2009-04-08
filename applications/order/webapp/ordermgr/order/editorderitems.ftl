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

<#if orderHeader?has_content>

<#-- Order Items changes to basic-table style  -->
<style type="text/css">
.order-items .header-row td {
border-bottom: none;
}
.order-items tr .button-col {
float: right;
}
</style>

<#-- price change rules -->
<#assign allowPriceChange = false/>
<#if (orderHeader.orderTypeId == 'PURCHASE_ORDER' || security.hasEntityPermission("ORDERMGR", "_SALES_PRICEMOD", session))>
    <#assign allowPriceChange = true/>
</#if>

<div class="screenlet">
    <div class="screenlet-title-bar">
        <ul>
          <li class="h3">&nbsp;${uiLabelMap.OrderOrderItems}</li>
          <#if security.hasEntityPermission("ORDERMGR", "_UPDATE", session) || security.hasRolePermission("ORDERMGR", "_UPDATE", "", "", session)>
              <#if orderHeader?has_content && orderHeader.statusId != "ORDER_CANCELLED" && orderHeader.statusId != "ORDER_COMPLETED">
                  <li><a href="javascript:document.updateItemInfo.action='<@ofbizUrl>cancelOrderItem?${paramString}</@ofbizUrl>';document.updateItemInfo.submit()">${uiLabelMap.OrderCancelAllItems}</a></li>
                  <li><a href="<@ofbizUrl>orderview?${paramString}</@ofbizUrl>">${uiLabelMap.OrderViewOrder}</a></li>
              </#if>
          </#if>
        </ul>
        <br class="clear"/>
    </div>
    <div class="screenlet-body">
        <#if !orderItemList?has_content>
            <span class="alert">${uiLabelMap.checkhelper_sales_order_lines_lookup_failed}</span>
        <#else>
            <form name="updateItemInfo" method="post" action="<@ofbizUrl>updateOrderItems</@ofbizUrl>">
            <input type="hidden" name="orderId" value="${orderId}"/>
            <#if (orderHeader.orderTypeId == 'PURCHASE_ORDER')>
              <input type="hidden" name="supplierPartyId" value="${partyId}"/>
              <input type="hidden" name="orderTypeId" value="PURCHASE_ORDER"/>
            </#if>
            <table class="basic-table order-items" cellspacing="0">
                <tr class="header-row">
                    <td width="30%">${uiLabelMap.ProductProduct}</td>
                    <td width="30%">${uiLabelMap.CommonStatus}</td>
                    <td width="5%" class="align-text">${uiLabelMap.OrderQuantity}</td>
                    <td width="10%" class="align-text">${uiLabelMap.OrderUnitPrice}</td>
                    <td width="10%" class="align-text">${uiLabelMap.OrderAdjustments}</td>
                    <td width="10%" class="align-text">${uiLabelMap.OrderSubTotal}</td>
                    <td width="2%">&nbsp;</td>
                    <td width="3%">&nbsp;</td>
                </tr>
                <#list orderItemList as orderItem>
                    <#if orderItem.productId?exists> <#-- a null product may come from a quote -->
                      <#assign orderItemContentWrapper = Static["org.ofbiz.order.order.OrderContentWrapper"].makeOrderContentWrapper(orderItem, request)>
                      <tr><td colspan="8"><hr/></td></tr>
                      <tr>
                          <#assign orderItemType = orderItem.getRelatedOne("OrderItemType")?if_exists>
                          <#assign productId = orderItem.productId?if_exists>
                          <#if productId?exists && productId == "shoppingcart.CommentLine">
                              <td colspan="8" valign="top">
                                  <span class="label">&gt;&gt; ${orderItem.itemDescription}</span>
                              </td>
                          <#else>
                              <td valign="top">
                                  <div>
                                      <#if orderHeader.statusId = "ORDER_CANCELLED" || orderHeader.statusId = "ORDER_COMPLETED">
                                      <#if productId?exists>
                                      ${orderItem.productId?default("N/A")} - ${orderItem.itemDescription?if_exists}
                                      <#elseif orderItemType?exists>
                                      ${orderItemType.description} - ${orderItem.itemDescription?if_exists}
                                      <#else>
                                      ${orderItem.itemDescription?if_exists}
                                      </#if>
                                      <#else>
                                      <#if productId?exists>
                                      <#assign orderItemName = orderItem.productId?default("N/A")/>
                                      <#elseif orderItemType?exists>
                                      <#assign orderItemName = orderItemType.description/>
                                      </#if>
                                      <p>${uiLabelMap.ProductProduct}&nbsp;${orderItemName}</p>
                                      <#if productId?exists>
                                          <#assign product = orderItem.getRelatedOneCache("Product")>
                                          <#if product.salesDiscontinuationDate?exists && Static["org.ofbiz.base.util.UtilDateTime"].nowTimestamp().after(product.salesDiscontinuationDate)>
                                              <span class="alert">${uiLabelMap.OrderItemDiscontinued}: ${product.salesDiscontinuationDate}</span>
                                          </#if>
                                      </#if>
                                      ${uiLabelMap.CommonDescription}<br/>
                                      <input type="text" size="20" name="idm_${orderItem.orderItemSeqId}" value="${orderItem.itemDescription?if_exists}"/>
                                      </#if>
                                  </div>
                                  <#if productId?exists>
                                  <div>
                                      <a href="/catalog/control/EditProduct?productId=${productId}" class="buttontext" target="_blank">${uiLabelMap.ProductCatalog}</a>
                                      <a href="/ecommerce/control/product?product_id=${productId}" class="buttontext" target="_blank">${uiLabelMap.OrderEcommerce}</a>
                                      <#if orderItemContentWrapper.get("IMAGE_URL")?has_content>
                                      <a href="<@ofbizUrl>viewimage?orderId=${orderId}&orderItemSeqId=${orderItem.orderItemSeqId}&orderContentTypeId=IMAGE_URL</@ofbizUrl>" target="_orderImage" class="buttontext">${uiLabelMap.OrderViewImage}</a>
                                      </#if>
                                  </div>
                                  </#if>
                              </td>

                              <#-- now show status details per line item -->
                              <#assign currentItemStatus = orderItem.getRelatedOne("StatusItem")>
                              <td>
                                  ${uiLabelMap.CommonCurrent}&nbsp;${currentItemStatus.get("description",locale)?default(currentItemStatus.statusId)}<br/>
                                  <#assign orderItemStatuses = orderReadHelper.getOrderItemStatuses(orderItem)>
                                  <#list orderItemStatuses as orderItemStatus>
                                  <#assign loopStatusItem = orderItemStatus.getRelatedOne("StatusItem")>
                                  ${orderItemStatus.statusDatetime.toString()}&nbsp;${loopStatusItem.get("description",locale)?default(orderItemStatus.statusId)}<br/>
                                  </#list>
                                  <#assign returns = orderItem.getRelated("ReturnItem")?if_exists>
                                  <#if returns?has_content>
                                  <#list returns as returnItem>
                                  <#assign returnHeader = returnItem.getRelatedOne("ReturnHeader")>
                                  <#if returnHeader.statusId != "RETURN_CANCELLED">
                                  <div class="alert">
                                      <span class="label">${uiLabelMap.OrderReturned}</span> #<a href="<@ofbizUrl>returnMain?returnId=${returnItem.returnId}</@ofbizUrl>" class="buttontext">${returnItem.returnId}</a>
                                  </div>
                                  </#if>
                                  </#list>
                                  </#if>
                              </td>
                              <td class="align-text" valign="top" nowrap="nowrap">
                                  <#assign remainingQuantity = (orderItem.quantity?default(0) - orderItem.cancelQuantity?default(0))>
                                  ${uiLabelMap.OrderOrdered}&nbsp;${orderItem.quantity?default(0)?string.number}&nbsp;&nbsp;<br/>
                                  ${uiLabelMap.OrderCancelled}:&nbsp;${orderItem.cancelQuantity?default(0)?string.number}&nbsp;&nbsp;<br/>
                                  ${uiLabelMap.OrderRemaining}:&nbsp;${remainingQuantity}&nbsp;&nbsp;<br/>
                              </td>
                              <td class="align-text" valign="top" nowrap="nowrap">
                                  <#-- check for permission to modify price -->
                                  <#if (allowPriceChange)>
                                      <input type="text" size="8" name="ipm_${orderItem.orderItemSeqId}" value="<@ofbizAmount amount=orderItem.unitPrice/>"/>
                                      &nbsp;<input type="checkbox" name="opm_${orderItem.orderItemSeqId}" value="Y"/>
                                  <#else>
                                      <div><@ofbizCurrency amount=orderItem.unitPrice isoCode=currencyUomId/> / <@ofbizCurrency amount=orderItem.unitListPrice isoCode=currencyUomId/></div>
                                  </#if>
                              </td>
                              <td class="align-text" valign="top" nowrap="nowrap">
                                  <@ofbizCurrency amount=Static["org.ofbiz.order.order.OrderReadHelper"].getOrderItemAdjustmentsTotal(orderItem, orderAdjustments, true, false, false) isoCode=currencyUomId/>
                              </td>
                              <td class="align-text" valign="top" nowrap="nowrap">
                                  <#if orderItem.statusId != "ITEM_CANCELLED">
                                  <@ofbizCurrency amount=Static["org.ofbiz.order.order.OrderReadHelper"].getOrderItemSubTotal(orderItem, orderAdjustments) isoCode=currencyUomId/>
                                  <#else>
                                  <@ofbizCurrency amount=0.00 isoCode=currencyUomId/>
                                  </#if>
                              </td>
                              <td>&nbsp;</td>
                              <td>
                                  <#if ("Y" != orderItem.isPromo?if_exists) && ((security.hasEntityPermission("ORDERMGR", "_ADMIN", session) && orderItem.statusId != "ITEM_CANCELLED" && orderItem.statusId != "ITEM_COMPLETED") || (security.hasEntityPermission("ORDERMGR", "_UPDATE", session) && orderItem.statusId != "ITEM_CANCELLED" && orderItem.statusId != "ITEM_COMPLETED" && orderHeader.statusId != "ORDER_SENT"))>
                                      <a href="javascript:document.updateItemInfo.action='<@ofbizUrl>cancelOrderItem?orderItemSeqId=${orderItem.orderItemSeqId}&amp;${paramString}</@ofbizUrl>';document.updateItemInfo.submit()" class="buttontext">${uiLabelMap.CommonCancelAll}</a>
                                  <#else>
                                      &nbsp;
                                  </#if>
                              </td>
                          </#if>
                      </tr>

                      <#-- now update/cancel reason and comment field -->
                      <#if orderItem.statusId != "ITEM_CANCELLED" && orderItem.statusId != "ITEM_COMPLETED" && ("Y" != orderItem.isPromo?if_exists)>
                        <tr><td colspan="8"><span class="label">${uiLabelMap.OrderReturnReason}</span>
  	                      <select name="irm_${orderItem.orderItemSeqId}">
  	                        <option value="">&nbsp;</option>
  	                        <#list orderItemChangeReasons as reason>
  	                          <option value="${reason.enumId}">${reason.get("description",locale)?default(reason.enumId)}</option>
  	                        </#list>
  	                      </select>
                            <span class="label">${uiLabelMap.CommonComments}</span>
                            <input type="text" name="icm_${orderItem.orderItemSeqId}" value="" size="30" maxlength="60"/>
                            <#if (orderHeader.orderTypeId == 'PURCHASE_ORDER')>
                              <span class="label">${uiLabelMap.OrderEstimatedShipDate}</span>
                              <input type="text" name="isdm_${orderItem.orderItemSeqId}" value="${orderItem.estimatedShipDate?if_exists}" size="25" maxlength="30"/>
                              <a href="javascript:call_cal(document.updateItemInfo.isdm_${orderItem.orderItemSeqId},'${todayDate} 00:00:00.0');"><img src="<@ofbizContentUrl>/images/cal.gif</@ofbizContentUrl>" width="16" height="16" border="0" alt="${uiLabelMap.OrderCalendarClickHereForCalendar}"/></a>
                              <span class="label">${uiLabelMap.OrderOrderQuoteEstimatedDeliveryDate}</span>
                              <input type="text" name="iddm_${orderItem.orderItemSeqId}" value="${orderItem.estimatedDeliveryDate?if_exists}" size="25" maxlength="30"/>
                              <a href="javascript:call_cal(document.updateItemInfo.iddm_${orderItem.orderItemSeqId},'${todayDate} 00:00:00.0');"><img src="<@ofbizContentUrl>/images/cal.gif</@ofbizContentUrl>" width="16" height="16" border="0" alt="${uiLabelMap.OrderCalendarClickHereForCalendar}"/></a>
                            </#if>
                        </tr>
                      </#if>
                      <#-- now show adjustment details per line item -->
                      <#assign orderItemAdjustments = Static["org.ofbiz.order.order.OrderReadHelper"].getOrderItemAdjustmentList(orderItem, orderAdjustments)>
                      <#if orderItemAdjustments?exists && orderItemAdjustments?has_content>
                          <#list orderItemAdjustments as orderItemAdjustment>
                              <#assign adjustmentType = orderItemAdjustment.getRelatedOneCache("OrderAdjustmentType")>
                              <tr>
                                  <td class="align-text" colspan="2">
                                      <span class="label">${uiLabelMap.OrderAdjustment}</span>&nbsp;${adjustmentType.get("description",locale)}&nbsp;
                                      ${orderItemAdjustment.get("description",locale)?if_exists} (${orderItemAdjustment.comments?default("")})

                                      <#if orderItemAdjustment.orderAdjustmentTypeId == "SALES_TAX">
                                      <#if orderItemAdjustment.primaryGeoId?has_content>
                                      <#assign primaryGeo = orderItemAdjustment.getRelatedOneCache("PrimaryGeo")/>
                                      <span class="label">${uiLabelMap.OrderJurisdiction}</span>&nbsp;${primaryGeo.geoName} [${primaryGeo.abbreviation?if_exists}]
                                      <#if orderItemAdjustment.secondaryGeoId?has_content>
                                      <#assign secondaryGeo = orderItemAdjustment.getRelatedOneCache("SecondaryGeo")/>
                                      (<span class="label">${uiLabelMap.CommonIn}</span>&nbsp;${secondaryGeo.geoName} [${secondaryGeo.abbreviation?if_exists}])
                                      </#if>
                                      </#if>
                                      <#if orderItemAdjustment.sourcePercentage?exists><span class="label">Rate</span>&nbsp;${orderItemAdjustment.sourcePercentage}</#if>
                                      <#if orderItemAdjustment.customerReferenceId?has_content><span class="label">Customer Tax ID</span>&nbsp;${orderItemAdjustment.customerReferenceId}</#if>
                                      <#if orderItemAdjustment.exemptAmount?exists><span class="label">Exempt Amount</span>&nbsp;${orderItemAdjustment.exemptAmount}</#if>
                                      </#if>
                                  </td>
                                  <td>&nbsp;</td>
                                  <td>&nbsp;</td>
                                  <td class="align-text">
                                      <@ofbizCurrency amount=Static["org.ofbiz.order.order.OrderReadHelper"].calcItemAdjustment(orderItemAdjustment, orderItem) isoCode=currencyUomId/>
                                  </td>
                                  <td colspan="3">&nbsp;</td>
                              </tr>
                          </#list>
                      </#if>

                      <#-- now show ship group info per line item -->
                      <#assign orderItemShipGroupAssocs = orderItem.getRelated("OrderItemShipGroupAssoc")?if_exists>
                      <#if orderItemShipGroupAssocs?has_content>
                          <tr><td colspan="8">&nbsp;</td></tr>
                          <#list orderItemShipGroupAssocs as shipGroupAssoc>
                              <#assign shipGroup = shipGroupAssoc.getRelatedOne("OrderItemShipGroup")>
                              <#assign shipGroupAddress = shipGroup.getRelatedOne("PostalAddress")?if_exists>
                              <tr>
                                  <td class="align-text" colspan="2">
                                      <span class="label">${uiLabelMap.OrderShipGroup}</span>&nbsp;[${shipGroup.shipGroupSeqId}] ${shipGroupAddress.address1?default("${uiLabelMap.OrderNotShipped}")}
                                  </td>
                                  <td align="center">
                                      <input type="text" name="iqm_${shipGroupAssoc.orderItemSeqId}:${shipGroupAssoc.shipGroupSeqId}" size="6" value="${shipGroupAssoc.quantity?string.number}"/>
                                  </td>
                                  <td colspan="4">&nbsp;</td>
                                  <td>
                                      <#assign itemStatusOkay = (orderItem.statusId != "ITEM_CANCELLED" && orderItem.statusId != "ITEM_COMPLETED" && (shipGroupAssoc.cancelQuantity?default(0) < shipGroupAssoc.quantity?default(0)) && ("Y" != orderItem.isPromo?if_exists))>
                                      <#if (security.hasEntityPermission("ORDERMGR", "_ADMIN", session) && itemStatusOkay) || (security.hasEntityPermission("ORDERMGR", "_UPDATE", session) && itemStatusOkay && orderHeader.statusId != "ORDER_SENT")>
                                          <a href="javascript:document.updateItemInfo.action='<@ofbizUrl>cancelOrderItem?orderItemSeqId=${orderItem.orderItemSeqId}&amp;shipGroupSeqId=${shipGroup.shipGroupSeqId}&amp;${paramString}</@ofbizUrl>';document.updateItemInfo.submit()" class="buttontext">${uiLabelMap.CommonCancel}</a>
                                      <#else>
                                          &nbsp;
                                      </#if>
                                  </td>
                              </tr>
                          </#list>
                      </#if>
                    </#if>
                </#list>
                <tr>
                    <td colspan="7">&nbsp;</td>
                    <td>
                        <input type="submit" value="${uiLabelMap.OrderUpdateItems}" class="buttontext"/>
                    </td>
                </tr>
                <tr><td colspan="8"><hr/></td></tr>
            </table>
            </form>
        </#if>
        <#list orderHeaderAdjustments as orderHeaderAdjustment>
            <#assign adjustmentType = orderHeaderAdjustment.getRelatedOne("OrderAdjustmentType")>
            <#assign adjustmentAmount = Static["org.ofbiz.order.order.OrderReadHelper"].calcOrderAdjustment(orderHeaderAdjustment, orderSubTotal)>
            <#assign orderAdjustmentId = orderHeaderAdjustment.get("orderAdjustmentId")>
            <#if adjustmentAmount != 0>
                <form name="updateOrderAdjustmentForm${orderAdjustmentId}" method="post" action="<@ofbizUrl>updateOrderAdjustment?orderAdjustmentId=${orderAdjustmentId?if_exists}&amp;orderId=${orderId?if_exists}</@ofbizUrl>">
                    <table class="basic-table" cellspacing="0">
                        <tr>
                            <td class="align-text" width="55%">
                                <span class="label">${adjustmentType.get("description",locale)}</span>&nbsp;${orderHeaderAdjustment.comments?if_exists}
                            </td>
                            <td nowrap="nowrap" width="30%">
                                <#if (allowPriceChange)>
                                    <input type="text" name="description" value="${orderHeaderAdjustment.get("description")?if_exists}" size="30" maxlength="60"/>
                                <#else>
                                    ${orderHeaderAdjustment.get("description")?if_exists}
                                </#if>
                            </td>
                            <td nowrap="nowrap" width="15%">
                                <#if (allowPriceChange)>
                                    <input type="text" name="amount" size="6" value="<@ofbizAmount amount=adjustmentAmount/>"/>
                                    <input class="smallSubmit" type="submit" value="${uiLabelMap.CommonUpdate}"/><a href="<@ofbizUrl>deleteOrderAdjustment?orderAdjustmentId=${orderAdjustmentId?if_exists}&amp;orderId=${orderId?if_exists}</@ofbizUrl>" class="buttontext">${uiLabelMap.CommonDelete}</a>
                                <#else>
                                    <@ofbizAmount amount=adjustmentAmount/>
                                </#if>
                            </td>
                        </tr>
                    </table>
                </form>
            </#if>
        </#list>

        <#-- add new adjustment -->
        <#if (security.hasEntityPermission("ORDERMGR", "_UPDATE", session) || security.hasRolePermission("ORDERMGR", "_UPDATE", "", "", session)) && orderHeader.statusId != "ORDER_COMPLETED" && orderHeader.statusId != "ORDER_CANCELLED" && orderHeader.statusId != "ORDER_REJECTED">
            <form name="addAdjustmentForm" method="post" action="<@ofbizUrl>createOrderAdjustment?${paramString}</@ofbizUrl>">
                <input type="hidden" name="comments" value="Added manually by [${userLogin.userLoginId}]"/>
                <table class="basic-table" cellspacing="0">
                    <tr><td colspan="3"><hr/></td></tr>
                    <tr>
                        <td class="align-text" width="55%">
                            <span class="label">${uiLabelMap.OrderAdjustment}</span>&nbsp;
                            <select name="orderAdjustmentTypeId">
                                <#list orderAdjustmentTypes as type>
                                <option value="${type.orderAdjustmentTypeId}">${type.get("description",locale)?default(type.orderAdjustmentTypeId)}</option>
                                </#list>
                            </select>
                            <select name="shipGroupSeqId">
                                <option value="_NA_"></option>
                                <#list shipGroups as shipGroup>
                                <option value="${shipGroup.shipGroupSeqId}">${uiLabelMap.OrderShipGroup} ${shipGroup.shipGroupSeqId}</option>
                                </#list>
                            </select>
                        </td>
                        <td width="30%"><input type="text" name="description" value="" size="30" maxlength="60"/></td>
                        <td width="15%">
                            <input type="text" name="amount" size="6" value="<@ofbizAmount amount=0.00/>"/>
                            <input class="smallSubmit" type="submit" value="${uiLabelMap.CommonAdd}"/>
                        </td>
                    </tr>
                </table>
            </form>
        </#if>

        <#-- subtotal -->
        <table class="basic-table" cellspacing="0">
            <tr><td colspan="4"><hr/></td></tr>
            <tr class="align-text">
              <td width="80%"><span class="label">${uiLabelMap.OrderItemsSubTotal}</span></td>
              <td width="10%" nowrap="nowrap"><@ofbizCurrency amount=orderSubTotal isoCode=currencyUomId/></td>
              <td width="10%" colspan="2">&nbsp;</td>
            </tr>

            <#-- other adjustments -->
            <tr class="align-text">
              <td><span class="label">${uiLabelMap.OrderTotalOtherOrderAdjustments}</span></td>
              <td nowrap="nowrap"><@ofbizCurrency amount=otherAdjAmount isoCode=currencyUomId/></td>
              <td colspan="2">&nbsp;</td>
            </tr>

            <#-- shipping adjustments -->
            <tr class="align-text">
              <td><span class="label">${uiLabelMap.OrderTotalShippingAndHandling}</span></td>
              <td nowrap="nowrap"><@ofbizCurrency amount=shippingAmount isoCode=currencyUomId/></td>
              <td colspan="2">&nbsp;</td>
            </tr>

            <#-- tax adjustments -->
            <tr class="align-text">
              <td><span class="label">${uiLabelMap.OrderTotalSalesTax}</span></td>
              <td nowrap="nowrap"><@ofbizCurrency amount=taxAmount isoCode=currencyUomId/></td>
              <td colspan="2">&nbsp;</td>
            </tr>

            <#-- grand total -->
            <tr class="align-text">
              <td><span class="label">${uiLabelMap.OrderTotalDue}</span></td>
              <td nowrap="nowrap"><@ofbizCurrency amount=grandTotal isoCode=currencyUomId/></td>
              <td colspan="2">&nbsp;</td>
            </tr>
        </table>
    </div>
</div>
</#if>