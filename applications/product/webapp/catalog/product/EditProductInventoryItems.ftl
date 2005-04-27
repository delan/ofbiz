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
 *@author     David E. Jones (jonesde@ofbiz.org)
 *@author     Brad Steiner (bsteiner@thehungersite.com)
 *@author     Catherine.Heintz@nereide.biz (migration to UiLabel)
 *@version    $Rev$
 *@since      2.2
-->
<#assign externalKeyParam = "&externalLoginKey=" + requestAttributes.externalLoginKey?if_exists>

<table border="1" cellpadding="2" cellspacing="0">
    <tr>
        <td><div class="tabletext"><b>${uiLabelMap.ProductFacility}</b></div></td>
        <td><div class="tabletext"><b>${uiLabelMap.ProductAtp}</b></div></td>
        <td><div class="tabletext"><b>${uiLabelMap.ProductQoh}</b></div></td>
        <td><div class="tabletext"><b>${uiLabelMap.ProductIncomingShipments}</b></div></td>
        <td><div class="tabletext"><b>${uiLabelMap.ProductIncomingProductionRuns}</b></div></td>
        <td><div class="tabletext"><b>${uiLabelMap.ProductOutgoingProductionRuns}</b></div></td>
    </tr>
    <#list quantitySummaryByFacility.values() as quantitySummary>
        <#assign facilityId = quantitySummary.facilityId?if_exists>
        <#assign facility = quantitySummary.facility?if_exists>
        <#assign totalQuantityOnHand = quantitySummary.totalQuantityOnHand?if_exists>
        <#assign totalAvailableToPromise = quantitySummary.totalAvailableToPromise?if_exists>
        <#assign incomingShipmentAndItemList = quantitySummary.incomingShipmentAndItemList?if_exists>
        <#assign incomingProductionRunList = quantitySummary.incomingProductionRunList?if_exists>
        <#assign outgoingProductionRunList = quantitySummary.outgoingProductionRunList?if_exists>

        <tr>
            <td><div class="tabletext">${(facility.facilityName)?if_exists} [${facilityId?default("[No Facility]")}]</div></td>
            <td><div class="tabletext"><#if totalAvailableToPromise?exists>${totalAvailableToPromise}<#else>&nbsp;</#if></div></td>
            <td><div class="tabletext"><#if totalQuantityOnHand?exists>${totalQuantityOnHand}<#else>&nbsp;</#if></div></td>
            <td>
                <#if incomingShipmentAndItemList?has_content>
                    <#list incomingShipmentAndItemList as incomingShipmentAndItem>
                        <div class="tabletext">${incomingShipmentAndItem.shipmentId}:${incomingShipmentAndItem.shipmentItemSeqId}-${(incomingShipmentAndItem.estimatedArrivalDate.toString())?if_exists}-<#if incomingShipmentAndItem.quantity?exists>${incomingShipmentAndItem.quantity?string.number}<#else>[${uiLabelMap.ProductQuantityNotSet}]</#if></div>
                    </#list>
                <#else>
                    <div class="tabletext">&nbsp;</div>
                </#if>
            </td>
            <td>
                <#if incomingProductionRunList?has_content>
                    <#list incomingProductionRunList as incomingProductionRun>
                        <div class="tabletext">${incomingProductionRun.workEffortId}-${(incomingProductionRun.estimatedCompletionDate.toString())?if_exists}-<#if incomingProductionRun.estimatedQuantity?exists>${incomingProductionRun.estimatedQuantity?string.number}<#else>[${uiLabelMap.ProductQuantityNotSet}]</#if></div>
                    </#list>
                <#else>
                    <div class="tabletext">&nbsp;</div>
                </#if>
            </td>
            <td>
                <#if outgoingProductionRunList?has_content>
                    <#list outgoingProductionRunList as outgoingProductionRun>
                        <div class="tabletext">${outgoingProductionRun.workEffortParentId}:${outgoingProductionRun.workEffortId}-${(outgoingProductionRun.estimatedStartDate.toString())?if_exists}-<#if outgoingProductionRun.estimatedQuantity?exists>${outgoingProductionRun.estimatedQuantity?string.number}<#else>[${uiLabelMap.ProductQuantityNotSet}]</#if></div>
                    </#list>
                <#else>
                    <div class="tabletext">&nbsp;</div>
                </#if>
            </td>
        </tr>
    </#list>
</table>

<hr class="sepbar"/>

<div class="head1">${uiLabelMap.ProductInventoryItems} <span class="head2">${uiLabelMap.CommonFor} <#if product?exists>${(product.internalName)?if_exists} </#if> [${uiLabelMap.CommonId}:${productId?if_exists}]</span></div>
<#if productId?has_content>
    <a href="/facility/control/EditInventoryItem?productId=${productId}${externalKeyParam}" class="buttontext">[${uiLabelMap.ProductCreateNewInventoryItemProduct}]</a>
    <#if showEmpty>
        <a href="<@ofbizUrl>/EditProductInventoryItems?productId=${productId}</@ofbizUrl>" class="buttontext">[${uiLabelMap.ProductHideEmptyItems}]</a>
    <#else>
        <a href="<@ofbizUrl>/EditProductInventoryItems?productId=${productId}&showEmpty=true</@ofbizUrl>" class="buttontext">[${uiLabelMap.ProductShowEmptyItems}]</a>
    </#if>
</#if>
<br/>

<#if (product.isVirtual)?if_exists == "Y">
    <div class="head3">${uiLabelMap.ProductWarningVirtualProduct}.</div>
</#if>

<br/>
<#if productId?exists>
    <table border="1" cellpadding="2" cellspacing="0">
    <tr>
        <td><div class="tabletext"><b>${uiLabelMap.ProductItemId}</b></div></td>
        <td><div class="tabletext"><b>${uiLabelMap.ProductItemType}</b></div></td>
        <td><div class="tabletext"><b>${uiLabelMap.ProductStatus}</b></div></td>
        <td><div class="tabletext"><b>${uiLabelMap.CommonReceived}</b></div></td>
        <td><div class="tabletext"><b>${uiLabelMap.CommonExpire}</b></div></td>
        <td><div class="tabletext"><b>${uiLabelMap.ProductFacilityContainerId}</b></div></td>
        <td><div class="tabletext"><b>${uiLabelMap.ProductLocation}</b></div></td>
        <td><div class="tabletext"><b>${uiLabelMap.ProductLotId}</b></div></td>
        <td><div class="tabletext"><b>${uiLabelMap.ProductBinNum}</b></div></td>
        <td><div class="tabletext"><b>${uiLabelMap.ProductAtpQohSerial}</b></div></td>
        <td><div class="tabletext">&nbsp;</div></td>
        <td><div class="tabletext">&nbsp;</div></td>
    </tr>
    <#list productInventoryItems as inventoryItem>
       <#if showEmpty || (inventoryItem.inventoryItemTypeId?if_exists == "SERIALIZED_INV_ITEM" && inventoryItem.statusId?if_exists != "INV_DELIVERED")
                       || (inventoryItem.inventoryItemTypeId?if_exists == "NON_SERIAL_INV_ITEM" && ((inventoryItem.availableToPromiseTotal?exists && inventoryItem.availableToPromiseTotal > 0) || (inventoryItem.quantityOnHandTotal?exists && inventoryItem.quantityOnHandTotal > 0)))>
            <#assign curInventoryItemType = inventoryItem.getRelatedOne("InventoryItemType")>
            <#if inventoryItem.inventoryItemTypeId?if_exists == "SERIALIZED_INV_ITEM">
                <#assign curStatusItem = inventoryItem.getRelatedOneCache("StatusItem")?if_exists>
            </#if>
            <#assign facilityLocation = inventoryItem.getRelatedOne("FacilityLocation")?if_exists>
            <#assign facilityLocationTypeEnum = (facilityLocation.getRelatedOneCache("TypeEnumeration"))?if_exists>
            <#if curInventoryItemType?exists>
                <tr valign="middle">
                    <td><a href="/facility/control/EditInventoryItem?inventoryItemId=${(inventoryItem.inventoryItemId)?if_exists}${externalKeyParam}" class="buttontext">${(inventoryItem.inventoryItemId)?if_exists}</a></td>
                    <td><div class="tabletext">&nbsp;${(curInventoryItemType.description)?if_exists}</div></td>
                    <td>
                        <div class="tabletext">
                            <#if inventoryItem.inventoryItemTypeId?if_exists == "SERIALIZED_INV_ITEM">
                                <#if curStatusItem?has_content>
                                    ${(curStatusItem.description)?if_exists}
                                <#elseif inventoryItem.statusId?has_content>
                                    [${inventoryItem.statusId}]
                                <#else>
                                    ${uiLabelMap.CommonNotSet}&nbsp;
                                </#if>
                            <#else>
                                &nbsp;
                            </#if>
                        </div>
                    </td>
                    <td><div class="tabletext">&nbsp;${(inventoryItem.datetimeReceived)?if_exists}</div></td>
                    <td><div class="tabletext">&nbsp;${(inventoryItem.expireDate)?if_exists}</div></td>
                    <#if inventoryItem.facilityId?exists && inventoryItem.containerId?exists>
                        <td><div class="tabletext" style="color: red;">${uiLabelMap.ProductErrorFacility} (${inventoryItem.facilityId})
                            ${uiLabelMap.ProductAndContainer} (${inventoryItem.containerId}) ${uiLabelMap.CommonSpecified}</div></td>
                    <#elseif inventoryItem.facilityId?exists>
                        <td><span class="tabletext">${uiLabelMap.ProductFacilityLetter}:&nbsp;</span><a href="/facility/control/EditFacility?facilityId=${inventoryItem.facilityId}${externalKeyParam}" class="buttontext">
                            ${inventoryItem.facilityId}</a></td>
                    <#elseif (inventoryItem.containerId)?exists>
                        <td><span class="tabletext">${uiLabelMap.ProductContainerLetter}:&nbsp;</span><a href="<@ofbizUrl>/EditContainer?containerId=${inventoryItem.containerId }</@ofbizUrl>" class="buttontext">
                            ${inventoryItem.containerId}</a></td>
                    <#else>
                        <td>&nbsp;</td>
                    </#if>
                    <td><div class="tabletext"><a href="/facility/control/EditFacilityLocation?facilityId=${(inventoryItem.facilityId)?if_exists}&locationSeqId=${(inventoryItem.locationSeqId)?if_exists}${externalKeyParam}" class="buttontext"><#if facilityLocation?exists>${facilityLocation.areaId?if_exists}:${facilityLocation.aisleId?if_exists}:${facilityLocation.sectionId?if_exists}:${facilityLocation.levelId?if_exists}:${facilityLocation.positionId?if_exists}</#if><#if facilityLocationTypeEnum?has_content> (${facilityLocationTypeEnum.description})</#if> [${(inventoryItem.locationSeqId)?if_exists}]</a></div></td>
                    <td><div class="tabletext">&nbsp;${(inventoryItem.lotId)?if_exists}</div></td>
                    <td><div class="tabletext">&nbsp;${(inventoryItem.binNumber)?if_exists}</div></td>
                    <#if inventoryItem.inventoryItemTypeId?if_exists == "NON_SERIAL_INV_ITEM">
                        <td>
                            <div class="tabletext">${(inventoryItem.availableToPromiseTotal)?default("NA")}
                            / ${(inventoryItem.quantityOnHandTotal)?default("NA")}</div>
                        </td>
                    <#elseif inventoryItem.inventoryItemTypeId?if_exists == "SERIALIZED_INV_ITEM">
                        <td><div class="tabletext">&nbsp;${(inventoryItem.serialNumber)?if_exists}</div></td>
                    <#else>
                        <td><div class="tabletext" style="color: red;">${uiLabelMap.ProductErrorType} ${(inventoryItem.inventoryItemTypeId)?if_exists} ${uiLabelMap.ProductUnknownSerialNumber} (${(inventoryItem.serialNumber)?if_exists})
                            ${uiLabelMap.ProductAndQuantityOnHand} (${(inventoryItem.quantityOnHandTotal)?if_exists} ${uiLabelMap.CommonSpecified}</div></td>
                        <td>&nbsp;</td>
                    </#if>
                    <td>
                    <a href="/facility/control/EditInventoryItem?inventoryItemId=${(inventoryItem.inventoryItemId)?if_exists}${externalKeyParam}" class="buttontext">
                    [${uiLabelMap.CommonEdit}]</a>
                    </td>
                    <td>
                    <a href="<@ofbizUrl>/DeleteProductInventoryItem?productId=${productId}&inventoryItemId=${(inventoryItem.inventoryItemId)?if_exists}</@ofbizUrl>" class="buttontext">
                    [${uiLabelMap.CommonDelete}]</a>
                    </td>
                </tr>
            </#if>
        </#if>
    </#list>
    </table>
</#if>
