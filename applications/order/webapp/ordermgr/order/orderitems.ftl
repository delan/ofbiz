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

<#if orderHeader?has_content>

<div class="screenlet">
    <div class="screenlet-header">
        <div style="float: right;">
            <#if security.hasEntityPermission("ORDERMGR", "_UPDATE", session)>
                <#if orderHeader?has_content && orderHeader.statusId != "ORDER_CANCELLED" && orderHeader.statusId != "ORDER_COMPLETED">
                    <div class="tabletext">
                      <#--
                      <a href="<@ofbizUrl>changeOrderItemStatus?statusId=ITEM_CANCELLED&${paramString}</@ofbizUrl>" class="submenutext">${uiLabelMap.OrderCancelAllItems}</a>
                      -->
                      <a href="<@ofbizUrl>editOrderItems?${paramString}</@ofbizUrl>" class="submenutextright">${uiLabelMap.OrderEditItems}</a>
                    </div>
                </#if>
            </#if>
        </div>
        <div class="boxhead">&nbsp;${uiLabelMap.OrderOrderItems}</div>
    </div>
    <div class="screenlet-body">
        <table width="100%" border="0" cellpadding="0" cellspacing="0">
          <tr align="left" valign=bottom>
            <td width="30%" align="left"><div class="tableheadtext">${uiLabelMap.ProductProduct}</div></td>
            <td width="30%" align="left"><div class="tableheadtext">${uiLabelMap.CommonStatus}</div></td>
            <td width="5%" align="right"><div class="tableheadtext">${uiLabelMap.OrderQuantity}</div></td>
            <td width="10%" align="right"><div class="tableheadtext">${uiLabelMap.OrderUnitList}</div></td>
            <td width="10%" align="right"><div class="tableheadtext">${uiLabelMap.OrderAdjustments}</div></td>
            <td width="10%" align="right"><div class="tableheadtext">${uiLabelMap.OrderSubTotal}</div></td>
            <td width="5%">&nbsp;</td>
          </tr>
          <#if !orderItemList?has_content>
            <tr><td><font color="red">${uiLabelMap.checkhelper_sales_order_lines_lookup_failed}</font></td></tr>
          <#else>
            <#list orderItemList as orderItem>
              <#assign orderItemContentWrapper = Static["org.ofbiz.order.order.OrderContentWrapper"].makeOrderContentWrapper(orderItem, request)>
              <tr><td colspan="8"><hr class="sepbar"></td></tr>
              <tr>
                <#assign orderItemType = orderItem.getRelatedOne("OrderItemType")?if_exists>
                <#assign productId = orderItem.productId?if_exists>
                <#if productId?exists && productId == "shoppingcart.CommentLine">
                  <td colspan="1" valign="top">
                    <b><div class="tabletext"> &gt;&gt; ${orderItem.itemDescription}</div></b>
                  </td>
                <#else>
                  <td valign="top">
                    <div class="tabletext">
                      <#if productId?exists>
                        ${orderItem.productId?default("N/A")} - ${orderItem.itemDescription?if_exists}
                      <#elseif orderItemType?exists>
                        ${orderItemType.description} - ${orderItem.itemDescription?if_exists}
                      <#else>
                        ${orderItem.itemDescription?if_exists}
                      </#if>
                    </div>
                    <#if productId?exists>
                      <div class="tabletext">
                        <a href="/catalog/control/EditProduct?productId=${productId}" class="buttontext" target="_blank">${uiLabelMap.ProductCatalog}</a>
                        <a href="/ecommerce/control/product?product_id=${productId}" class="buttontext" target="_blank">${uiLabelMap.EcommerceEcommerce}</a>
                        <#if orderItemContentWrapper.get("IMAGE_URL")?has_content>
                          <a href="<@ofbizUrl>viewimage?orderId=${orderId}&orderItemSeqId=${orderItem.orderItemSeqId}&orderContentTypeId=IMAGE_URL</@ofbizUrl>" target="_orderImage" class="buttontext">${uiLabelMap.OrderViewImage}</a>
                        </#if>
                      </div>
                      <#if (orderHeader.statusId != "ORDER_COMPLETED") && availableToPromiseMap.get(productId)?exists && quantityOnHandMap.get(productId)?exists>
                        <div class="tabletext">
                            <a href="/catalog/control/EditProductInventoryItems?productId=${productId}" target="_blank">${uiLabelMap.ProductInventory}:</a>
                            ATP = ${availableToPromiseMap.get(productId)}, QOH = ${quantityOnHandMap.get(productId)}</a>
                        </div>
                      </#if>
                    </#if>
                  </td>

                  <#-- now show status details per line item -->
                  <#assign currentItemStatus = orderItem.getRelatedOne("StatusItem")>
                  <td align="left" colspan="1">
                    <div class="tabletext">${uiLabelMap.CommonCurrent}: ${currentItemStatus.description?default(currentItemStatus.statusId)}</div>
                    <#assign orderItemStatuses = orderReadHelper.getOrderItemStatuses(orderItem)>
                    <#list orderItemStatuses as orderItemStatus>
                      <#assign loopStatusItem = orderItemStatus.getRelatedOne("StatusItem")>
                      <div class="tabletext">
                        ${orderItemStatus.statusDatetime.toString()} : ${loopStatusItem.description?default(orderItemStatus.statusId)}
                      </div>
                    </#list>
                    <#assign returns = orderItem.getRelated("ReturnItem")?if_exists>
                    <#if returns?has_content>
                      <#list returns as returnItem>
                        <#assign returnHeader = returnItem.getRelatedOne("ReturnHeader")>
                        <#if returnHeader.statusId != "RETURN_CANCELLED">
                          <div class="tabletext">
                            <font color="red"><b>${uiLabelMap.OrderReturned}</b></font> #<a href="<@ofbizUrl>returnMain?returnId=${returnItem.returnId}</@ofbizUrl>" class="buttontext">${returnItem.returnId}</a>
                          </div>
                        </#if>
                      </#list>
                    </#if>
                  </td>
                  <td align="right" valign="top" nowrap>
                    <#assign remainingQuantity = (orderItem.quantity?default(0) - orderItem.cancelQuantity?default(0))>
                    <div class="tabletext">${uiLabelMap.OrderOrdered}:&nbsp;${orderItem.quantity?default(0)?string.number}&nbsp;&nbsp;</div>
                    <div class="tabletext">${uiLabelMap.OrderCancelled}:&nbsp;${orderItem.cancelQuantity?default(0)?string.number}&nbsp;&nbsp;</div>
                    <div class="tabletext">${uiLabelMap.OrderRemaining}:&nbsp;${remainingQuantity}&nbsp;&nbsp;</div>
                  </td>
                  <td align="right" valign="top" nowrap>
                    <div class="tabletext" nowrap><@ofbizCurrency amount=orderItem.unitPrice isoCode=currencyUomId/> / <@ofbizCurrency amount=orderItem.unitListPrice isoCode=currencyUomId/></div>
                  </td>
                  <td align="right" valign="top" nowrap>
                    <div class="tabletext" nowrap><@ofbizCurrency amount=Static["org.ofbiz.order.order.OrderReadHelper"].getOrderItemAdjustmentsTotal(orderItem, orderAdjustments, true, false, false) isoCode=currencyUomId/></div>
                  </td>
                  <td align="right" valign="top" nowrap>
                    <#if orderItem.statusId != "ITEM_CANCELLED">
                      <div class="tabletext" nowrap><@ofbizCurrency amount=Static["org.ofbiz.order.order.OrderReadHelper"].getOrderItemSubTotal(orderItem, orderAdjustments) isoCode=currencyUomId/></div>
                    <#else>
                      <div class="tabletext" nowrap><@ofbizCurrency amount=0.00 isoCode=currencyUomId/></div>
                    </#if>
                  </td>
                  <td>&nbsp;</td>
                  <td align="right" valign="top" nowrap>
                    &nbsp;
                  </td>
                </#if>
              </tr>
              <#-- show info from workeffort -->
              <#assign workOrderItemFulfillments = orderItem.getRelated("WorkOrderItemFulfillment")?if_exists>
              <#if workOrderItemFulfillments?has_content>
                  <#list workOrderItemFulfillments as workOrderItemFulfillment>
                      <#assign workEffort = workOrderItemFulfillment.getRelatedOneCache("WorkEffort")>
                      <tr>
                        <td>&nbsp;</td>
                        <td colspan="9">
                          <div class="tabletext">
                            <#if orderItem.orderItemTypeId != "RENTAL_ORDER_ITEM">
                              <b><i>Production Run</i>:</b>
                              <a href="/manufacturing/control/ShowProductionRun?productionRunId=${workEffort.workEffortId}&externalLoginKey=${externalLoginKey}" class="buttontext" style="font-size: xx-small;">${workEffort.workEffortId}</a>&nbsp;
                            </#if>
                            From: ${workEffort.estimatedStartDate?string("yyyy-MM-dd")} to: ${workEffort.estimatedCompletionDate?string("yyyy-MM-dd")} Number of persons: ${workEffort.reservPersons?default("")}
                          </div>
                        </td>
                      </tr>
                      <#break><#-- need only the first one -->
                  </#list>
              </#if>
              <#-- show linked order lines -->
              <#if orderHeader?has_content && orderHeader.orderTypeId = "SALES_ORDER">
                <#assign linkedOrderItems = orderItem.getRelated("SalesOrderItemAssociation")?if_exists>
              <#else>
                <#assign linkedOrderItems = orderItem.getRelated("PurchaseOrderItemAssociation")?if_exists>
              </#if>

              <#if linkedOrderItems?has_content>
                <#list linkedOrderItems as linkedOrderItem>
                  <#if orderHeader?has_content && orderHeader.orderTypeId = "SALES_ORDER">
                    <#assign linkedOrderId = linkedOrderItem.purchaseOrderId>
                    <#assign linkedOrderItemSeqId = linkedOrderItem.poItemSeqId>
                  <#else>
                    <#assign linkedOrderId = linkedOrderItem.salesOrderId>
                    <#assign linkedOrderItemSeqId = linkedOrderItem.soItemSeqId>
                  </#if>
                  <tr>
                    <td>&nbsp;</td>
                    <td colspan="9">
                      <div class="tabletext">
                        <b><i>Linked to order item</i>:</b>
                        <a href="/ordermgr/control/orderview?orderId=${linkedOrderId}" class="buttontext" style="font-size: xx-small;">${linkedOrderId}/${linkedOrderItemSeqId}</a>&nbsp;
                      </div>
                    </td>
                  </tr>
                </#list>
              </#if>
              <#-- show linked requirements -->
              <#assign linkedRequirements = orderItem.getRelated("OrderRequirementCommitment")?if_exists>

              <#if linkedRequirements?has_content>
                <#list linkedRequirements as linkedRequirement>
                  <tr>
                    <td>&nbsp;</td>
                    <td colspan="9">
                      <div class="tabletext">
                        <b><i>Linked to requirement</i>:</b>
                        <a href="<@ofbizUrl>EditRequirement?requirementId=${linkedRequirement.requirementId}</@ofbizUrl>" class="buttontext" style="font-size: xx-small;">${linkedRequirement.requirementId}</a>&nbsp;
                      </div>
                    </td>
                  </tr>
                </#list>
              </#if>

              <#-- show linked quote -->
              <#assign linkedQuote = orderItem.getRelatedOneCache("QuoteItem")?if_exists>

              <#if linkedQuote?has_content>
                <tr>
                  <td>&nbsp;</td>
                  <td colspan="9">
                    <div class="tabletext">
                      <b><i>Linked to quote</i>:</b>
                      <a href="<@ofbizUrl>EditQuoteItem?quoteId=${linkedQuote.quoteId}&quoteItemSeqId=${linkedQuote.quoteItemSeqId}</@ofbizUrl>" class="buttontext" style="font-size: xx-small;">${linkedQuote.quoteId}-${linkedQuote.quoteItemSeqId}</a>&nbsp;
                    </div>
                  </td>
                </tr>
              </#if>

              <#-- now show adjustment details per line item -->
              <#assign orderItemAdjustments = Static["org.ofbiz.order.order.OrderReadHelper"].getOrderItemAdjustmentList(orderItem, orderAdjustments)>
              <#if orderItemAdjustments?exists && orderItemAdjustments?has_content>
                <#list orderItemAdjustments as orderItemAdjustment>
                  <#assign adjustmentType = orderItemAdjustment.getRelatedOneCache("OrderAdjustmentType")>
                  <tr>
                    <td align="right" colspan="2">
                      <div class="tabletext" style="font-size: xx-small;">
                        <b><i>${uiLabelMap.OrderAdjustment}</i>:</b> <b>${adjustmentType.description}</b>:
                        ${orderItemAdjustment.description?if_exists} (${orderItemAdjustment.comments?default("")})

                        <#if orderItemAdjustment.orderAdjustmentTypeId == "SALES_TAX">
                          <#if orderItemAdjustment.primaryGeoId?has_content>
                            <#assign primaryGeo = orderItemAdjustment.getRelatedOneCache("PrimaryGeo")/>
                            <b>${uiLabelMap.OrderJurisdiction}:</b> ${primaryGeo.geoName} [${primaryGeo.abbreviation?if_exists}]
                            <#if orderItemAdjustment.secondaryGeoId?has_content>
                              <#assign secondaryGeo = orderItemAdjustment.getRelatedOneCache("SecondaryGeo")/>
                              (<b>in:</b> ${secondaryGeo.geoName} [${secondaryGeo.abbreviation?if_exists}])
                            </#if>
                          </#if>
                          <#if orderItemAdjustment.sourcePercentage?exists><b>Rate:</b> ${orderItemAdjustment.sourcePercentage?string("0.######")}</#if>
                          <#if orderItemAdjustment.customerReferenceId?has_content><b>Customer Tax ID:</b> ${orderItemAdjustment.customerReferenceId}</#if>
                          <#if orderItemAdjustment.exemptAmount?exists><b>Exempt Amount:</b> ${orderItemAdjustment.exemptAmount}</#if>
                        </#if>
                      </div>
                    </td>
                    <td>&nbsp;</td>
                    <td>&nbsp;</td>
                    <td align="right">
                      <div class="tabletext" style="font-size: xx-small;">
                        <@ofbizCurrency amount=Static["org.ofbiz.order.order.OrderReadHelper"].calcItemAdjustment(orderItemAdjustment, orderItem) isoCode=currencyUomId/>
                      </div>
                     </td>
                    <td>&nbsp;</td>
                  </tr>
                </#list>
              </#if>

              <#-- now show price info per line item -->
              <#assign orderItemPriceInfos = orderReadHelper.getOrderItemPriceInfos(orderItem)>
              <#if orderItemPriceInfos?exists && orderItemPriceInfos?has_content>
                <tr><td>&nbsp;</td></tr>
                <#list orderItemPriceInfos as orderItemPriceInfo>
                  <tr>
                    <td align="right" colspan="2">
                      <div class="tabletext" style="font-size: xx-small;"><b><i>Price Rule</i>:</b> [${orderItemPriceInfo.productPriceRuleId}:${orderItemPriceInfo.productPriceActionSeqId}] ${orderItemPriceInfo.description?if_exists}</div>
                    </td>
                    <td>&nbsp;</td>
                    <td align="right">
                      <div class="tabletext" style="font-size: xx-small;">
                        <@ofbizCurrency amount=orderItemPriceInfo.modifyAmount isoCode=currencyUomId/>
                      </div>
                    </td>
                    <td>&nbsp;</td>
                    <td>&nbsp;</td>
                  </tr>
                </#list>
              </#if>

              <#-- now show survey information per line item -->
              <#assign orderItemSurveyResponses = Static["org.ofbiz.order.order.OrderReadHelper"].getOrderItemSurveyResponse(orderItem)>
              <#if orderItemSurveyResponses?exists && orderItemSurveyResponses?has_content>
                <#list orderItemSurveyResponses as survey>
                  <tr>
                    <td align="right" colspan="2">
                      <div class="tabletext" style="font-size: xx-small;">
                        <b><i>Survey</i>:</b>
                          <a href="/content/control/ViewSurveyResponse?surveyResponseId=${survey.surveyResponseId}&surveyId=${survey.surveyId}<#if survey.partyId?exists>&partyId=${survey.partyId}</#if>&externalLoginKey=${externalLoginKey}" class="buttontext" style="font-size: xx-small;">${survey.surveyId}</a>
                      </div>
                    </td>
                    <td>&nbsp;</td>
                    <td>&nbsp;</td>
                    <td>&nbsp;</td>
                    <td>&nbsp;</td>
                  </tr>
                </#list>
              </#if>

              <#-- now show ship group info per line item -->
              <#assign orderItemShipGroupAssocs = orderItem.getRelated("OrderItemShipGroupAssoc")?if_exists>
              <#if orderItemShipGroupAssocs?has_content>
                <tr><td>&nbsp;</td></tr>
                <#list orderItemShipGroupAssocs as shipGroupAssoc>
                  <#assign shipGroup = shipGroupAssoc.getRelatedOne("OrderItemShipGroup")>
                  <#assign shipGroupAddress = shipGroup.getRelatedOne("PostalAddress")?if_exists>
                  <tr>
                    <td align="right" colspan="2">
                      <div class="tabletext" style="font-size: xx-small;"><b><i>${uiLabelMap.OrderShipGroup}</i>:</b> [${shipGroup.shipGroupSeqId}] ${shipGroupAddress.address1?default("${uiLabelMap.OrderNotShipped}")}</div>
                    </td>
                    <td align="center">
                      <div class="tabletext" style="font-size: xx-small;">${shipGroupAssoc.quantity?string.number}&nbsp;</div>
                    </td>
                    <td>&nbsp;</td>
                    <td>&nbsp;</td>
                    <td>&nbsp;</td>
                    <td>&nbsp;</td>
                    <td align="right" valign="top" nowrap>
                      &nbsp;
                    </td>
                  </tr>
                </#list>
              </#if>

              <#-- now show inventory reservation info per line item -->
              <#assign orderItemShipGrpInvResList = orderReadHelper.getOrderItemShipGrpInvResList(orderItem)>
              <#if orderItemShipGrpInvResList?exists && orderItemShipGrpInvResList?has_content>
                <#list orderItemShipGrpInvResList as orderItemShipGrpInvRes>
                  <tr>
                    <td align="right" colspan="2">
                      <div class="tabletext" style="font-size: xx-small;">
                        <b><i>${uiLabelMap.FacilityInventory}</i>:</b>
                          <a href="/facility/control/EditInventoryItem?inventoryItemId=${orderItemShipGrpInvRes.inventoryItemId}&externalLoginKey=${externalLoginKey}" class="buttontext" style="font-size: xx-small;">${orderItemShipGrpInvRes.inventoryItemId}</a>
                        <b><i>${uiLabelMap.OrderShipGroup}</i>:</b> ${orderItemShipGrpInvRes.shipGroupSeqId}
                      </div>
                    </td>
                    <td align="center">
                      <div class="tabletext" style="font-size: xx-small;">${orderItemShipGrpInvRes.quantity?string.number}&nbsp;</div>
                    </td>
                    <td>
                      <#if (orderItemShipGrpInvRes.quantityNotAvailable?has_content && orderItemShipGrpInvRes.quantityNotAvailable > 0)>
                        <font color="red">[${orderItemShipGrpInvRes.quantityNotAvailable?string.number}&nbsp;Backordered]</font>
                      </#if>
                      &nbsp;
                    </td>
                    <td>&nbsp;</td>
                    <td>&nbsp;</td>
                  </tr>
                </#list>
              </#if>

              <#-- now show planned shipment info per line item -->
              <#assign orderShipments = orderItem.getRelated("OrderShipment")?if_exists>
              <#if orderShipments?has_content>
                <#list orderShipments as orderShipment>
                  <tr>
                    <td align="right" colspan="2">
                      <div class="tabletext" style="font-size: xx-small;"><b><i>Planned in Shipment</i>: </b><a target="facility" href="/facility/control/ViewShipment?shipmentId=${orderShipment.shipmentId}&externalLoginKey=${externalLoginKey}" class="buttontext" style="font-size: xx-small;">${orderShipment.shipmentId}</a>: ${orderShipment.shipmentItemSeqId}</div>
                    </td>
                    <td align="center">
                      <div class="tabletext" style="font-size: xx-small;">${orderShipment.quantity?string.number}&nbsp;</div>
                    </td>
                    <td>&nbsp;</td>
                    <td>&nbsp;</td>
                    <td>&nbsp;</td>
                    <td>&nbsp;</td>
                    <td>&nbsp;</td>
                  </tr>
                </#list>
              </#if>

              <#-- now show item issuances per line item -->
              <#assign itemIssuances = itemIssuancesPerItem.get(orderItem.get("orderItemSeqId"))?if_exists>
              <#if itemIssuances?has_content>
                <#list itemIssuances as itemIssuance>
                  <tr>
                    <td align="right" colspan="2">
                      <div class="tabletext" style="font-size: xx-small;">
                        <#if itemIssuance.shipmentId?has_content>
                          <b><i>${uiLabelMap.OrderIssuedToShipmentItem}</i>:</b>
                          <a target="facility" href="/facility/control/ViewShipment?shipmentId=${itemIssuance.shipmentId}&externalLoginKey=${externalLoginKey}" class="buttontext" style="font-size: xx-small;">${itemIssuance.shipmentId}</a>:${itemIssuance.shipmentItemSeqId?if_exists}
                        <#else>
                          <b><i>Issued Without a Shipment (Immediate, Physical Store)</i></b>
                        </#if>
                      </div>
                    </td>
                    <td align="center">
                      <div class="tabletext" style="font-size: xx-small;">${itemIssuance.quantity?string.number}&nbsp;</div>
                    </td>
                    <td>&nbsp;</td>
                    <td>&nbsp;</td>
                    <td>&nbsp;</td>
                  </tr>
                </#list>
              </#if>
            </#list>
          </#if>
          <tr><td colspan="8"><hr class="sepbar"></td></tr>
          <#list orderHeaderAdjustments as orderHeaderAdjustment>
            <#assign adjustmentType = orderHeaderAdjustment.getRelatedOne("OrderAdjustmentType")>
            <#assign adjustmentAmount = Static["org.ofbiz.order.order.OrderReadHelper"].calcOrderAdjustment(orderHeaderAdjustment, orderSubTotal)>
            <#if adjustmentAmount != 0>
              <tr>
                <td align="right" colspan="5">
                  <div class="tabletext"><b>${adjustmentType.description}</b> : ${orderHeaderAdjustment.comments?if_exists}</div>
                </td>
                <td align="right" nowrap>
                  <div class="tabletext"><@ofbizCurrency amount=adjustmentAmount isoCode=currencyUomId/></div>
                </td>
                <td>&nbsp;</td>
              </tr>
            </#if>
          </#list>

          <#-- subtotal -->
          <tr><td colspan=1></td><td colspan="8"><hr class="sepbar"></td></tr>
          <tr>
            <td align="right" colspan="5"><div class="tabletext"><b>${uiLabelMap.OrderItemsSubTotal}</b></div></td>
            <td align="right" nowrap><div class="tabletext"><@ofbizCurrency amount=orderSubTotal isoCode=currencyUomId/></div></td>
          </tr>

          <#-- other adjustments -->
          <tr>
            <td align="right" colspan="5"><div class="tabletext"><b>${uiLabelMap.OrderTotalOtherOrderAdjustments}</b></div></td>
            <td align="right" nowrap><div class="tabletext"><@ofbizCurrency amount=otherAdjAmount isoCode=currencyUomId/></div></td>
          </tr>

          <#-- shipping adjustments -->
          <tr>
            <td align="right" colspan="5"><div class="tabletext"><b>${uiLabelMap.OrderTotalShippingAndHandling}</b></div></td>
            <td align="right" nowrap><div class="tabletext"><@ofbizCurrency amount=shippingAmount isoCode=currencyUomId/></div></td>
          </tr>

              <#-- tax adjustments -->
          <tr>
            <td align="right" colspan="5"><div class="tabletext"><b>${uiLabelMap.OrderTotalSalesTax}</b></div></td>
            <td align="right" nowrap><div class="tabletext"><@ofbizCurrency amount=taxAmount isoCode=currencyUomId/></div></td>
          </tr>

          <#-- grand total -->
          <tr>
            <td align="right" colspan="5"><div class="tabletext"><b>${uiLabelMap.OrderTotalDue}</b></div></td>
            <td align="right" nowrap>
              <div class="tabletext"><@ofbizCurrency amount=grandTotal isoCode=currencyUomId/></div>
            </td>
          </tr>
        </table>
    </div>
</div>

</#if>
