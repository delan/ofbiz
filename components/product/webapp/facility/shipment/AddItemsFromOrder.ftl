<#--
 *  Copyright (c) 2003-2004 The Open For Business Project - www.ofbiz.org
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
 *@author     Catherine.Heintz@nereide.biz (migration to UiLabel)
 *@version    $Rev$
 *@since      2.2
-->
<#if (requestAttributes.uiLabelMap)?exists><#assign uiLabelMap = requestAttributes.uiLabelMap></#if>
<#if security.hasEntityPermission("FACILITY", "_VIEW", session)>
${pages.get("/shipment/ShipmentTabBar.ftl")}

<#if shipment?exists>

<form action="<@ofbizUrl>/AddItemsFromOrder</@ofbizUrl>">
	<input type="hidden" name="shipmentId" value="${shipmentId}"/>
	<div class="tabletext">
        ${uiLabelMap.ProductOrderId} : <input type="text" class='inputBox' size="20" name="orderId" value="${orderId?if_exists}"/>
        ${uiLabelMap.ProductOrderShipGroupId} : <input type="text" class='inputBox' size="20" name="shipGroupSeqId" value="${shipGroupSeqId?if_exists}"/>
        <input type="submit" value="Select" class="smallSubmit"/>
    </div>
</form>

<div class="head2">${uiLabelMap.ProductAddItemsShipment}: [${shipmentId?if_exists}]; ${uiLabelMap.OrderFromOrder}: [${orderId?if_exists}], ${uiLabelMap.OrderShipGroup}: [${shipGroupSeqId?if_exists}]</div>
<#if orderId?has_content && !orderHeader?exists>
	<div class="head3" style="color: red;"><#assign uiLabelWithVar=uiLabelMap.ProductErrorOrderIdNotFound?interpret><@uiLabelWithVar/>.</div>
</#if>
<#if orderHeader?exists>
    <#if orderHeader.orderTypeId == "SALES_ORDER" && shipment.shipmentTypeId?if_exists != "SALES_SHIPMENT">
        <div class="head3" style="color: red;">${uiLabelMap.ProductWarningOrderType} ${(orderType.description)?default(orderHeader.orderTypeId?if_exists)}, ${uiLabelMap.ProductNotSalesShipment}.</div>
    <#elseif orderHeader.orderTypeId == "PURCHASE_ORDER" && shipment.shipmentTypeId?if_exists != "PURCHASE_SHIPMENT">
        <div class="head3" style="color: red;">${uiLabelMap.ProductWarningOrderType} ${(orderType.description)?default(orderHeader.orderTypeId?if_exists)}, ${uiLabelMap.ProductNotPurchaseShipment}.</div>
    <#else>
        <div class="head3">${uiLabelMap.ProductNoteOrderType} ${(orderType.description)?default(orderHeader.orderTypeId?if_exists)}.</div>
    </#if>
    <#if shipment.shipmentTypeId?if_exists == "SALES_SHIPMENT">
		<div class="head3">${uiLabelMap.ProductOriginFacilityIs}: <#if originFacility?exists>${originFacility.facilityName?if_exists} [${originFacility.facilityId}]<#else><span style="color: red;">${uiLabelMap.ProductNotSet}</span></#if></div>
    <#elseif shipment.shipmentTypeId?if_exists == "PURCHASE_SHIPMENT">
		<div class="head3">${uiLabelMap.ProductDestinationFacilityIs}: <#if destinationFacility?exists>${destinationFacility.facilityName?if_exists} [${destinationFacility.facilityId}]<#else><span style="color: red;">${uiLabelMap.ProductNotSet}</span></#if></div>
    </#if>
    <#if "ORDER_APPROVED" == orderHeader.statusId || "ORDER_BACKORDERED" == orderHeader.statusId>
        <div class="head3">${uiLabelMap.ProductNoteOrderStatus} ${(orderHeaderStatus.description)?default(orderHeader.statusId?if_exists)}.</div>
    <#elseif "ORDER_COMPLETED" == orderHeader.statusId>
        <div class="head3">${uiLabelMap.ProductNoteOrderStatus} ${(orderHeaderStatus.description)?default(orderHeader.statusId?if_exists)}, ${uiLabelMap.ProductNoItemsLeft}.</div>
    <#else>
        <div class="head3" style="color: red;">${uiLabelMap.ProductWarningOrderStatus} ${(orderHeaderStatus.description)?default(orderHeader.statusId?if_exists)}; ${uiLabelMap.ProductApprovedBeforeShipping}.</div>
    </#if>
</#if>
<#if orderItemDatas?exists>
    <#assign rowCount = 0>
    <#if isSalesOrder>
        <form action="<@ofbizUrl>/issueOrderItemShipGrpInvResToShipment</@ofbizUrl>" name="selectAllForm">
    <#else>
        <form action="<@ofbizUrl>/issueOrderItemToShipment</@ofbizUrl>" name="selectAllForm">
    </#if>
    <input type="hidden" name="shipmentId" value="${shipmentId}">
    <input type="hidden" name="_useRowSubmit" value="Y">
    <table width="100%" cellpadding="2" cellspacing="0" border="1">
        <tr>
            <td><div class="tableheadtext">${uiLabelMap.ProductOrderId}/Ship Group/${uiLabelMap.ProductOrderItem}</div></td>
            <td><div class="tableheadtext">${uiLabelMap.ProductProduct}</div></td>
            <#if isSalesOrder>
                <td><div class="tableheadtext">${uiLabelMap.ProductItemsIssuedReserved}</div></td>
                <td><div class="tableheadtext">${uiLabelMap.ProductIssuedReservedTotalOrdered}</div></td>
                <td><div class="tableheadtext">${uiLabelMap.ProductReserved}</div></td>
                <td><div class="tableheadtext">${uiLabelMap.ProductNotAvailable}</div></td>
            <#else>
                <td><div class="tableheadtext">${uiLabelMap.ProductItemsIssued}</div></td>
                <td><div class="tableheadtext">${uiLabelMap.ProductIssedOrdered}</div></td>
            </#if>
            <td><div class="tableheadtext">${uiLabelMap.ProductIssue}</div></td>
            <td align="right">
                <div class="tableheadtext">${uiLabelMap.CommonSubmit} ?</div>
                <div class="tableheadtext">${uiLabelMap.CommonAll}<input type="checkbox" name="selectAll" value="${uiLabelMap.CommonY}" onclick="javascript:toggleAll(this);"></div>
            </td>
        </tr>
        <#list orderItemDatas?if_exists as orderItemData>
            <#assign orderItemAndShipGroupAssoc = orderItemData.orderItemAndShipGroupAssoc>
            <#assign product = orderItemData.product?if_exists>
            <#assign itemIssuances = orderItemData.itemIssuances>
            <#assign totalQuantityIssued = orderItemData.totalQuantityIssued>
            <#assign orderItemShipGrpInvResDatas = orderItemData.orderItemShipGrpInvResDatas?if_exists>
            <#assign totalQuantityReserved = orderItemData.totalQuantityReserved?if_exists>
            <#assign totalQuantityIssuedAndReserved = orderItemData.totalQuantityIssuedAndReserved?if_exists>
            <tr>
                <td><div class="tabletext">${orderItemAndShipGroupAssoc.orderId} / ${orderItemAndShipGroupAssoc.shipGroupSeqId} / ${orderItemAndShipGroupAssoc.orderItemSeqId}</div></td>
                <td><div class="tabletext">${(product.internalName)?if_exists} [${orderItemAndShipGroupAssoc.productId?default("N/A")}]</div></td>
                <td>
                    <#if itemIssuances?has_content>
                        <#list itemIssuances as itemIssuance>
                            <div class="tabletext"><b>[${itemIssuance.quantity?if_exists}]</b>${itemIssuance.shipmentId?if_exists}:${itemIssuance.shipmentItemSeqId?if_exists} ${uiLabelMap.CommonOn} [${(itemIssuance.issuedDateTime.toString())?if_exists}] ${uiLabelMap.CommonBy} [${(itemIssuance.issuedByUserLoginId)?if_exists}]</div>
                        </#list>
                    <#else>
                        <div class="tabletext">&nbsp;</div>
                    </#if>
                </td>
                <td>
                    <div class="tabletext">
                        <#if isSalesOrder>
                            <#if (totalQuantityIssuedAndReserved != orderItemAndShipGroupAssoc.shipGroupQuantity)><span style="color: red;"><#else><span></#if>
                                [${totalQuantityIssued} + ${totalQuantityReserved} = ${totalQuantityIssuedAndReserved}]
                                <b>
                                    <#if (totalQuantityIssuedAndReserved > orderItemAndShipGroupAssoc.shipGroupQuantity)>&gt;<#else><#if (totalQuantityIssuedAndReserved < orderItemAndShipGroupAssoc.shipGroupQuantity)>&lt;<#else>=</#if></#if>
                                    ${orderItemAndShipGroupAssoc.shipGroupQuantity}
                                </b>
                            </span>
                        <#else>
                            <#if (totalQuantityIssued > orderItemAndShipGroupAssoc.shipGroupQuantity)><span style="color: red;"><#else><span></#if>
                                ${totalQuantityIssued}
                                <b>
                                    <#if (totalQuantityIssued > orderItemAndShipGroupAssoc.shipGroupQuantity)>&gt;<#else><#if (totalQuantityIssued < orderItemAndShipGroupAssoc.shipGroupQuantity)>&lt;<#else>=</#if></#if>
                                    ${orderItemAndShipGroupAssoc.shipGroupQuantity}
                                </b>
                            </span>
                        </#if>
                    </div>
                </td>
                <#if isSalesOrder>
                    <td><div class="tabletext">&nbsp;</div></td>
                    <td><div class="tabletext">&nbsp;</div></td>
                    <td><div class="tabletext">&nbsp;</div></td>
                    <td><div class="tabletext">&nbsp;</div></td>
                <#else>
                    <#assign quantityNotIssued = orderItemAndShipGroupAssoc.shipGroupQuantity - totalQuantityIssued>
                    <#if (quantityNotIssued > 0)>
                        <td>
                            <input type="hidden" name="shipmentId_o_${rowCount}" value="${shipmentId}"/>
                            <input type="hidden" name="orderId_o_${rowCount}" value="${orderItemAndShipGroupAssoc.orderId}"/>
                            <input type="hidden" name="shipGroupSeqId_o_${rowCount}" value="${orderItemAndShipGroupAssoc.shipGroupSeqId}"/>
                            <input type="hidden" name="orderItemSeqId_o_${rowCount}" value="${orderItemAndShipGroupAssoc.orderItemSeqId}"/>
                            <input type="text" class='inputBox' size="5" name="quantity_o_${rowCount}" value="${quantityNotIssued}"/>
                        </td>
                        <td align="right">              
                          <input type="checkbox" name="_rowSubmit_o_${rowCount}" value="Y" onclick="javascript:checkToggle(this);">
                        </td>
                        <#assign rowCount = rowCount + 1>   
                    <#else>
                        <td><div class="tabletext">&nbsp;</div></td>
                        <td><div class="tabletext">&nbsp;</div></td>
                    </#if>
                </#if>
            </tr>
            <#if isSalesOrder>
                <#list orderItemShipGrpInvResDatas as orderItemShipGrpInvResData>
                    <#assign orderItemShipGrpInvRes = orderItemShipGrpInvResData.orderItemShipGrpInvRes>
                    <#assign inventoryItem = orderItemShipGrpInvResData.inventoryItem>
                    <#assign inventoryItemFacility = orderItemShipGrpInvResData.inventoryItemFacility>
                    <#assign availableQuantity = orderItemShipGrpInvRes.quantity - (orderItemShipGrpInvRes.quantityNotAvailable?default(0))>
                    <#if availableQuantity < 0>
                        <#assign availableQuantity = 0>
                    </#if>
                    <tr>
                        <td><div class="tabletext">&nbsp;</div></td>
                        <td><div class="tabletext">&nbsp;</div></td>
                        <td>
                            <div class="tabletext">
                                ${orderItemShipGrpInvRes.inventoryItemId}
                                <#if inventoryItem.facilityId?has_content>
                                    <span<#if originFacility?exists && originFacility.facilityId != inventoryItem.facilityId> style="color: red;"</#if>>[${(inventoryItemFacility.facilityName)?default(inventoryItem.facilityId)}]</span>
                                <#else>
                                    <span style="color: red;">[${uiLabelMap.ProductNoFacility}]</span>
                                </#if>
                            </div>
                        </td>
                        <td><div class="tabletext">&nbsp;</div></td>
                        <td><div class="tabletext">${orderItemShipGrpInvRes.quantity}</div></td>
                        <td><div class="tabletext">${orderItemShipGrpInvRes.quantityNotAvailable?default("&nbsp;")}</div></td>
                        <#if originFacility?exists && originFacility.facilityId == inventoryItem.facilityId?if_exists>
                            <td>
                                <input type="hidden" name="shipmentId_o_${rowCount}" value="${shipmentId}"/>
                                <input type="hidden" name="orderId_o_${rowCount}" value="${orderItemShipGrpInvRes.orderId}"/>
                                <input type="hidden" name="shipGroupSeqId_o_${rowCount}" value="${orderItemShipGrpInvRes.shipGroupSeqId}"/>
                                <input type="hidden" name="orderItemSeqId_o_${rowCount}" value="${orderItemShipGrpInvRes.orderItemSeqId}"/>
                                <input type="hidden" name="inventoryItemId_o_${rowCount}" value="${orderItemShipGrpInvRes.inventoryItemId}"/>
                                <input type="text" class='inputBox' size="5" name="quantity_o_${rowCount}" value="${(orderItemShipGrpInvResData.shipmentPlanQuantity)?default(availableQuantity)}"/>
                            </td>
                            <td align="right">              
                              <input type="checkbox" name="_rowSubmit_o_${rowCount}" value="Y" onclick="javascript:checkToggle(this);">
                            </td>
                            <#assign rowCount = rowCount + 1>   
                        <#else>
                            <td><div class="tabletext">${uiLabelMap.ProductNotOriginFacility}</div></td>
                            <td><div class="tabletext">&nbsp;</div></td>
                        </#if>
                    </tr>
                </#list>
            </#if>
        </#list>
        <tr>
            <td colspan="8" align="right"><input type="submit" class="smallSubmit" value="${uiLabelMap.ProductIssueAll}"/></td>
        </tr>
    </table>
    <input type="hidden" name="_rowCount" value="${rowCount}">
    </form>
    <script language="JavaScript">selectAll();</script>
</#if>

<#else>
  <h3>${uiLabelMap.ProductShipmentNotFoundId}: [${shipmentId?if_exists}]</h3>
</#if>

<#else>
  <h3>${uiLabelMap.ProductFacilityViewPermissionError}</h3>
</#if>
