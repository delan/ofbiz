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

<script language="JavaScript" type="text/javascript">
    function clearLine(facilityId, orderId, orderItemSeqId, productId, shipGroupSeqId, inventoryItemId, packageSeqId) {
        document.clearPackLineForm.facilityId.value = facilityId;
        document.clearPackLineForm.orderId.value = orderId;
        document.clearPackLineForm.orderItemSeqId.value = orderItemSeqId;
        document.clearPackLineForm.productId.value = productId;
        document.clearPackLineForm.shipGroupSeqId.value = shipGroupSeqId;
        document.clearPackLineForm.inventoryItemId.value = inventoryItemId;
        document.clearPackLineForm.packageSeqId.value = packageSeqId;
        document.clearPackLineForm.submit();
    }
</script>
<#if security.hasEntityPermission("FACILITY", "_VIEW", session)>
    <#assign showInput = requestParameters.showInput?default("Y")/>
    <#assign hideGrid = requestParameters.hideGrid?default("N")/>
    <#assign showWeightPackageForm = requestParameters.showWeightPackageForm?default("N")/>
    <#assign showCompletePackForm = requestParameters.showCompletePackForm?default("Y")/>

    <#if (requestParameters.forceComplete?has_content && !invoiceIds?has_content)>
        <#assign forceComplete = "true">
        <#assign showInput = "Y">
    </#if>
    <#if !(showWarningForm)>
    <div class="screenlet">
        <div class="screenlet-title-bar">
            <ul>
                <li class="h3">${uiLabelMap.ProductPackOrder}&nbsp;in&nbsp;${facility.facilityName?if_exists} [${facilityId?if_exists}]</li>
            </ul>
            <br class="clear"/>
        </div>
        <div class="screenlet-body">
            <#if shipmentId?has_content && invoiceIds?exists && invoiceIds?has_content>
                <div>
                    ${uiLabelMap.CommonView} <a href="<@ofbizUrl>/PackingSlip.pdf?shipmentId=${shipmentId}</@ofbizUrl>" target="_blank" class="buttontext">${uiLabelMap.ProductPackingSlip}</a> ${uiLabelMap.CommonOr}
                    ${uiLabelMap.CommonView} <a href="<@ofbizUrl>/ShipmentBarCode.pdf?shipmentId=${shipmentId}</@ofbizUrl>" target="_blank" class="buttontext">${uiLabelMap.ProductBarcode}</a> ${uiLabelMap.CommonFor} ${uiLabelMap.ProductShipmentId} <a href="<@ofbizUrl>/ViewShipment?shipmentId=${shipmentId}</@ofbizUrl>" class="buttontext">${shipmentId}</a>
                </div>
                <div>
                    <p>${uiLabelMap.AccountingInvoices}:</p>
                    <ul>
                        <#list invoiceIds as invoiceId>
                            <li>
                                #<a href="/accounting/control/invoiceOverview?invoiceId=${invoiceId}&externalLoginKey=${externalLoginKey}" target="_blank" class="buttontext">${invoiceId}</a>
                                (<a href="/accounting/control/invoice.pdf?invoiceId=${invoiceId}&externalLoginKey=${externalLoginKey}" target="_blank" class="buttontext">PDF</a>)
                            </li>
                        </#list>
                    </ul>
                </div>
            </#if>
            <br/>

            <!-- select order form -->
            <form name="selectOrderForm" method="post" action="<@ofbizUrl>PackOrder</@ofbizUrl>">
                <input type="hidden" name="facilityId" value="${facilityId?if_exists}">
                <table cellspacing="0" class="basic-table">
                    <tr>
                        <td width="25%" align="right"><span class="label">${uiLabelMap.ProductOrderId}</span></td>
                        <td width="1">&nbsp;</td>
                        <td width="25%">
                            <input type="text" name="orderId" size="20" maxlength="20" value="${orderId?if_exists}"/>
                            /
                            <input type="text" name="shipGroupSeqId" size="6" maxlength="6" value="${shipGroupSeqId?default("00001")}"/>
                        </td>
                        <td><span class="label">${uiLabelMap.ProductHideGrid}</span>&nbsp;<input type="checkbox" name="hideGrid" value="Y" <#if (hideGrid == "Y")>checked=""</#if>></td>
                        <td>&nbsp;</td>
                    </tr>
                    <tr>
                        <td colspan="2">&nbsp;</td>
                        <td colspan="2">
                            <input type="image" src="<@ofbizContentUrl>/images/spacer.gif</@ofbizContentUrl>" onClick="javascript:document.selectOrderForm.submit();">
                            <a href="javascript:document.selectOrderForm.submit();" class="buttontext">${uiLabelMap.ProductPackOrder}</a>
                        </td>
                    </tr>
                </table>
            </form>
            <br/>

            <!-- select picklist bin form -->
            <form name="selectPicklistBinForm" method="post" action="<@ofbizUrl>PackOrder</@ofbizUrl>" style="margin: 0;">
                <input type="hidden" name="facilityId" value="${facilityId?if_exists}">
                <table cellspacing="0" class="basic-table">
                    <tr>
                        <td width="25%" align='right'><span class="label">${uiLabelMap.FormFieldTitle_picklistBinId}</span></td>
                        <td width="1">&nbsp;</td>
                        <td width="25%">
                            <input type="text" name="picklistBinId" size="29" maxlength="60" value="${picklistBinId?if_exists}"/>
                        </td>
                        <td><span class="label">${uiLabelMap.ProductHideGrid}</span>&nbsp;<input type="checkbox" name="hideGrid" value="Y" <#if (hideGrid == "Y")>checked=""</#if>></td>
                        <td>&nbsp;</td>
                    </tr>
                    <tr>
                        <td colspan="2">&nbsp;</td>
                        <td colspan="1">
                            <input type="image" src="<@ofbizContentUrl>/images/spacer.gif</@ofbizContentUrl>" onClick="javascript:document.selectPicklistBinForm.submit();">
                            <a href="javascript:document.selectPicklistBinForm.submit();" class="buttontext">${uiLabelMap.ProductPackOrder}</a>
                        </td>
                    </tr>
                </table>
            </form>
            <form name="clearPackForm" method="post" action="<@ofbizUrl>ClearPackAll</@ofbizUrl>">
                <input type="hidden" name="orderId" value="${orderId?if_exists}"/>
                <input type="hidden" name="shipGroupSeqId" value="${shipGroupSeqId?if_exists}"/>
                <input type="hidden" name="facilityId" value="${facilityId?if_exists}"/>
            </form>
            <form name="clearPackLineForm" method="post" action="<@ofbizUrl>ClearPackLine</@ofbizUrl>">
                <input type="hidden" name="facilityId"/>
                <input type="hidden" name="orderId"/>
                <input type="hidden" name="orderItemSeqId"/>
                <input type="hidden" name="productId"/>
                <input type="hidden" name="shipGroupSeqId"/>
                <input type="hidden" name="inventoryItemId"/>
                <input type="hidden" name="packageSeqId"/>
                <input type="hidden" name="showWeightPackageForm" value="${requestParameters.showWeightPackageForm?default("N")}"/>
                <input type="hidden" name="showCompletePackForm" value="${requestParameters.showCompletePackForm?default("Y")}"/>
                <input type="hidden" name="currentIndex" value="${currentIndex?default("0")}"/>
            </form>
            <form name="incPkgSeq" method="post" action="<@ofbizUrl>SetNextPackageSeq</@ofbizUrl>">
                <input type="hidden" name="orderId" value="${orderId?if_exists}"/>
                <input type="hidden" name="shipGroupSeqId" value="${shipGroupSeqId?if_exists}"/>
                <input type="hidden" name="facilityId" value="${facilityId?if_exists}"/>
            </form>
            <form name="weightPackageOnlyForm" method="post" action="<@ofbizUrl>WeightPackgeOnly</@ofbizUrl>">
              <input type="hidden" name="orderId" value="${orderId?if_exists}"/>
              <input type="hidden" name="shipGroupSeqId" value="${shipGroupSeqId?if_exists}"/>
              <input type="hidden" name="facilityId" value="${facilityId?if_exists}"/>
              <#assign packageSeqIds = packingSession.getPackageSeqIds()/>
              <#if packageSeqIds?has_content>
                <input type="hidden" name="showWeightPackageForm" value="Y"/>
                <input type="hidden" name="showCompletePackForm" value="N"/>
              </#if>
            </form>
        </div>
    </div>

    <#if ((shipment?has_content) && (shipment.shipmentId)?exists)>
        <#if showInput != "N" && ((orderHeader?exists && orderHeader?has_content))>
            <div class="screenlet">
                <div class="screenlet-title-bar">
                    <ul>
                        <li class="h3">${uiLabelMap.ProductOrderId} #<a href="/ordermgr/control/orderview?orderId=${orderId}">${orderId}</a> / ${uiLabelMap.ProductOrderShipGroupId} #${shipGroupSeqId}</li>
                    </ul>
                    <br class="clear"/>
                </div>
                <div class="screenlet-body">
                    <#if orderItemShipGroup?has_content>
                        <#assign postalAddress = orderItemShipGroup.getRelatedOne("PostalAddress")>
                        <#assign carrier = orderItemShipGroup.carrierPartyId?default("N/A")>
                        <table cellpadding="4" cellspacing="4" class="basic-table">
                            <tr>
                                <td valign="top">
                                    <span class="label">${uiLabelMap.ProductShipToAddress}</span>
                                    <br/>
                                    ${uiLabelMap.CommonTo}: ${postalAddress.toName?default("")}
                                    <br/>
                                    <#if postalAddress.attnName?has_content>
                                        ${uiLabelMap.CommonAttn}: ${postalAddress.attnName}
                                        <br/>
                                    </#if>
                                    ${postalAddress.address1}
                                    <br/>
                                    <#if postalAddress.address2?has_content>
                                        ${postalAddress.address2}
                                        <br/>
                                    </#if>
                                    ${postalAddress.city?if_exists}, ${postalAddress.stateProvinceGeoId?if_exists} ${postalAddress.postalCode?if_exists}
                                    <br/>
                                    ${postalAddress.countryGeoId}
                                    <br/>
                                </td>
                                <td>&nbsp;</td>
                                <td valign="top">
                                    <span class="label">${uiLabelMap.ProductCarrierShipmentMethod}</span>
                                    <br/>
                                    <#if carrier == "USPS">
                                        <#assign color = "red">
                                    <#elseif carrier == "UPS">
                                        <#assign color = "green">
                                    <#else>
                                        <#assign color = "black">
                                    </#if>
                                    <#if carrier != "_NA_">
                                        <font color="${color}">${carrier}</font>
                                        &nbsp;
                                    </#if>
                                    ${orderItemShipGroup.shipmentMethodTypeId?default("??")}
                                    <br/>
                                    <span class="label">${uiLabelMap.ProductEstimatedShipCostForShipGroup}</span>
                                    <br/>
                                    <#if shipmentCostEstimateForShipGroup?exists>
                                        <@ofbizCurrency amount=shipmentCostEstimateForShipGroup isoCode=orderReadHelper.getCurrency()?if_exists/>
                                        <br/>
                                    </#if>
                                </td>
                                <td>&nbsp;</td>
                                <td valign="top">
                                    <span class="label">${uiLabelMap.OrderInstructions}</span>
                                    <br/>
                                    ${orderItemShipGroup.shippingInstructions?default("(${uiLabelMap.CommonNone})")}
                                </td>
                            </tr>
                        </table>
                    </#if>

                    <!-- manual per item form -->
                    <#if showInput != "N">
                        <hr/>
                        <form name="singlePackForm" method="post" action="<@ofbizUrl>ProcessPackOrder</@ofbizUrl>">
                            <input type="hidden" name="packageSeq" value="${packingSession.getCurrentPackageSeq()}"/>
                            <input type="hidden" name="orderId" value="${orderId}"/>
                            <input type="hidden" name="shipGroupSeqId" value="${shipGroupSeqId}"/>
                            <input type="hidden" name="facilityId" value="${facilityId?if_exists}"/>
                            <input type="hidden" name="hideGrid" value="${hideGrid}"/>
                            <table cellpadding="2" cellspacing="0" class="basic-table">
                                <tr>
                                    <td>
                                        <div>
                                            <span class="label">${uiLabelMap.ProductProductNumber}</span>
                                            <input type="text" name="productId" size="20" maxlength="20" value=""/>
                                            @
                                            <input type="text" name="quantity" size="6" maxlength="6" value="1"/>
                                            <a href="javascript:document.singlePackForm.submit();" class="buttontext">${uiLabelMap.ProductPackItem}</a>
                                        </div>
                                    </td>
                                    <td>
                                        <span class="label">${uiLabelMap.ProductCurrentPackageSequence}</span>
                                        ${packingSession.getCurrentPackageSeq()}
                                        <input type="button" value="${uiLabelMap.ProductNextPackage}" onclick="javascript:document.incPkgSeq.submit();">
                                    </td>
                                </tr>
                            </table>
                        </form>
                    </#if>

                    <!-- auto grid form -->
                    <#if showInput != "N" && hideGrid != "Y" && orderItems?has_content>
                        <br/>
                        <form name="multiPackForm" method="post" action="<@ofbizUrl>ProcessBulkPackOrder</@ofbizUrl>">
                            <input type="hidden" name="facilityId" value="${facilityId?if_exists}">
                            <input type="hidden" name="orderId" value="${orderId?if_exists}">
                            <input type="hidden" name="shipGroupSeqId" value="${shipGroupSeqId?if_exists}">
                            <input type="hidden" name="originFacilityId" value="${facilityId?if_exists}">
                            <input type="hidden" name="hideGrid" value="${hideGrid}"/>
                            <table class="basic-table" cellspacing='0'>
                                <tr class="header-row">
                                    <td>&nbsp;</td>
                                    <td>${uiLabelMap.ProductItem} #</td>
                                    <td>${uiLabelMap.ProductProductId}</td>
                                    <td>${uiLabelMap.ProductInternalName}</td>
                                    <td align="right">${uiLabelMap.ProductOrderedQuantity}</td>
                                    <td align="right">${uiLabelMap.ProductQuantityShipped}</td>
                                    <td align="right">${uiLabelMap.ProductPackedQty}</td>
                                    <td>&nbsp;</td>
                                    <td align="center">${uiLabelMap.ProductPackQty}</td>
                                    <td align="center">${uiLabelMap.ProductPackedWeight}&nbsp;(${("uiLabelMap.ProductShipmentUomAbbreviation_" + defaultWeightUomId)?eval})</td>
                                    <td align="center">${uiLabelMap.ProductPackage}</td>
                                    <td align="right">&nbsp;<b>*</b>&nbsp;${uiLabelMap.ProductPackages}</td>
                                </tr>
                                <#if orderItems?has_content>
                                    <#assign rowKey = 1>
                                    <#list orderItems as orderItem>
                                    <#-- <#list itemInfos as orderItem>  -->
                                        <#assign shippedQuantity = 0.000000>
                                        <#assign orderItemQuantity = orderItem.getBigDecimal("quantity")/>
                                        <#assign shipments = delegator.findByAnd("Shipment", Static["org.ofbiz.base.util.UtilMisc"].toMap("primaryOrderId", orderItem.getString("orderId"), "statusId", "SHIPMENT_PACKED"))>
                                        <#if (shipments?has_content)>
                                          <#list shipments as shipment>
                                            <#assign itemIssuances = shipment.getRelatedByAnd("ItemIssuance", Static["org.ofbiz.base.util.UtilMisc"].toMap("orderId", "${orderId}", "orderItemSeqId", orderItemSeqId))>
                                            <#if itemIssuances?has_content>
                                              <#list itemIssuances as itemIssuance>
                                                <#assign shippedQuantity = shippedQuantity + itemIssuance.getBigDecimal("quantity")>
                                              </#list>
                                            </#if>
                                          </#list>
                                        </#if>
                                        <#assign orderItemQuantity = orderItemQuantity.subtract(shippedQuantity)>
                                        <#assign orderProduct = orderItem.getRelatedOne("Product")?if_exists/>
                                        <#assign product = Static["org.ofbiz.product.product.ProductWorker"].findProduct(delegator, orderItem.productId)?if_exists/>
                                    <#--
                                        <#if orderItem.cancelQuantity?exists>
                                            <#assign orderItemQuantity = orderItem.quantity - orderItem.cancelQuantity>
                                        <#else>
                                            <#assign orderItemQuantity = orderItem.quantity>
                                        </#if>
                                    -->
                                        <#assign inputQty = orderItemQuantity - packingSession.getPackedQuantity(orderId, orderItem.orderItemSeqId, shipGroupSeqId, orderItem.productId)>
                                        <tr>
                                            <td><input type="checkbox" name="sel_${rowKey}" value="Y" <#if (inputQty >0)>checked=""</#if>/></td>
                                            <td>${orderItem.orderItemSeqId}</td>
                                            <td>
                                                ${orderProduct.productId?default("N/A")}
                                                <#if orderProduct.productId != product.productId>
                                                    &nbsp;${product.productId?default("N/A")}
                                                </#if>
                                            </td>
                                            <td>
                                                <a href="/catalog/control/EditProduct?productId=${orderProduct.productId?if_exists}${externalKeyParam}" class="buttontext" target="_blank">${(orderProduct.internalName)?if_exists}</a>
                                                <#if orderProduct.productId != product.productId>
                                                    &nbsp;[<a href="/catalog/control/EditProduct?productId=${product.productId?if_exists}${externalKeyParam}" class="buttontext" target="_blank">${(product.internalName)?if_exists}</a>]
                                                </#if>
                                            </td>
                                            <td align="right">${orderItemQuantity}</td>
                                            <td align="right">${shippedQuantity}</td>
                                            <td align="right">${packingSession.getPackedQuantity(orderId, orderItem.orderItemSeqId, shipGroupSeqId, orderItem.productId)}</td>
                                            <td>&nbsp;</td>
                                            <td align="center">
                                                <input type="text" size="7" name="qty_${rowKey}" value="${inputQty}">
                                            </td>
                                            <td align="center">
                                                <input type="text" size="7" name="wgt_${rowKey}" value="">
                                            </td>
                                            <td align="center">
                                                <select name="pkg_${rowKey}">
                                                    <#if packingSession.getPackageSeqIds()?exists>
                                                        <#list packingSession.getPackageSeqIds() as packageSeqId>
                                                            <option value="${packageSeqId}">${uiLabelMap.ProductPackage} ${packageSeqId}</option>
                                                        </#list>
                                                        <#assign nextPackageSeqId = packingSession.getPackageSeqIds().size() + 1>
                                                        <option value="${nextPackageSeqId}">${uiLabelMap.ProductNextPackage}</option>
                                                    <#else>
                                                        <option value="1">${uiLabelMap.ProductPackage} 1</option>
                                                        <option value="2">${uiLabelMap.ProductPackage} 2</option>
                                                        <option value="3">${uiLabelMap.ProductPackage} 3</option>
                                                        <option value="4">${uiLabelMap.ProductPackage} 4</option>
                                                        <option value="5">${uiLabelMap.ProductPackage} 5</option>
                                                    </#if>
                                                </select>
                                            </td>
                                            <td align="right">
                                                <input type="text" size="7" name="numPackages_${rowKey}" value="1">
                                            </td>
                                            <input type="hidden" name="prd_${rowKey}" value="${orderItem.productId?if_exists}"/>
                                            <input type="hidden" name="ite_${rowKey}" value="${orderItem.orderItemSeqId}"/>
                                        </tr>
                                        <#assign rowKey = rowKey + 1>
                                    </#list>
                                </#if>
                                <tr><td colspan="10">&nbsp;</td></tr>
                                <tr>
                                    <td colspan="12" align="right">
                                        <a href="javascript:document.multiPackForm.submit()" class="buttontext">${uiLabelMap.ProductPackItem}</a>
                                        &nbsp;
                                        <a href="javascript:document.weightPackageOnlyForm.submit()" class="buttontext">${uiLabelMap.ProductWeightPackageOnly}</a>
                                        &nbsp;
                                        <a href="javascript:document.clearPackForm.submit()" class="buttontext">${uiLabelMap.CommonClear} (${uiLabelMap.CommonAll})</a>
                                    </td>
                                </tr>
                            </table>
                        </form>
                        <br/>
                    </#if>

                    <!-- weight package form -->
                    <#if showInput != "N">
                      <#if weightPackageSeqIds?exists && weightPackageSeqIds?has_content && "Y" == showWeightPackageForm>
                        <table class="basic-table" cellpadding="2" cellspacing='0'>
                          <hr>
                          <tr>
                            <th>
                              ${uiLabelMap.ProductPackedWeight} (${("uiLabelMap.ProductShipmentUomAbbreviation_" + defaultWeightUomId)?eval}):
                            </th>
                            <th>
                              ${uiLabelMap.CommonDimension} (${("uiLabelMap.ProductShipmentUomAbbreviation_" + defaultDimensionUomId)?eval}):
                            </th>
                            <th>
                              ${uiLabelMap.ProductPackageInputBox}:
                            </th>
                          </tr>
                          <#list weightPackageSeqIds as weightPackageSeqId>
                            <#if weightPackageSeqId != -1>
                              <form name="weightPackageForm_${weightPackageSeqId}" method="post" action="<@ofbizUrl>weightPackage</@ofbizUrl>">
                                <input type="hidden" name="shipGroupSeqId" value="${shipGroupSeqId?if_exists}"/>
                                <input type="hidden" name="facilityId" value ="${facilityId?if_exists}"/>
                                <input type="hidden" name="orderId" value ="${orderId?if_exists}"/>
                                <input type="hidden" name="showCompletePackForm" value="N"/>
                                <input type="hidden" name="showWeightPackageForm" value="Y"/>
                                <input type="hidden" name="currentIndex" value="${currentIndex?default("0")}"/>
                                <tr>
                                  <td>
                                    <span class="label">
                                      ${uiLabelMap.ProductPackage} ${weightPackageSeqId}
                                      <input type="text" size="7" name="packageWeight" value="${packingSession.getPackageWeight(weightPackageSeqId?int)?if_exists}">
                                    </span>
                                  </td>
                                  <td>
                                    <span class="label">${uiLabelMap.CommonLength}<input type="text" name="packageLength" value="${packingSession.getPackageLength(weightPackageSeqId?int)?if_exists}" size="5"/></span>
                                    <span class="label">${uiLabelMap.ProductWidth}<input type="text" name="packageWidth" value="${packingSession.getPackageWidth(weightPackageSeqId?int)?if_exists}" size="5"/></span>
                                    <span class="label">${uiLabelMap.PartyHeight}<input type="text" name="packageHeight" value="${packingSession.getPackageHeight(weightPackageSeqId?int)?if_exists}" size="5"/></span>
                                  </td>
                                  <td>
                                    <select name="shipmentBoxTypeId">
                                      <#if shipmentBoxTypes?has_content>
                                        <#assign shipmentBoxTypeId = "${packingSession.getShipmentBoxTypeId(weightPackageSeqId?int)?if_exists}"/>
                                        <#list shipmentBoxTypes as shipmentBoxType>
                                          <#if shipmentBoxTypeId == "${shipmentBoxType.shipmentBoxTypeId}">
                                            <option value="${shipmentBoxType.shipmentBoxTypeId}">${shipmentBoxType.description}</option>
                                          </#if>
                                        </#list>
                                        <option value=""></option>
                                        <#list shipmentBoxTypes as shipmentBoxType>
                                          <option value="${shipmentBoxType.shipmentBoxTypeId}">${shipmentBoxType.description}</option>
                                        </#list>
                                      </#if>
                                    </select>
                                  </td>
                                  <input type="hidden" name="packageSeqId" value="${weightPackageSeqId}" size="5"/>
                                  <td align="right"><a href="javascript:document.weightPackageForm_${weightPackageSeqId}.submit()" class="buttontext">${uiLabelMap.CommonUpdate}</a></td>
                                </tr>
                              </form>
                            </#if>
                          </#list>
                        </table>
                      </#if>
                      <table class="basic-table" cellpadding="2" cellspacing='0'>
                        <#assign packageSeqIds = packingSession.getPackageSeqIds()/>
                        <#if packageSeqIds?has_content>
                          <form name="weightPackageForm" method ="post" action="<@ofbizUrl>weightPackage</@ofbizUrl>">
                            <input type="hidden" name = "shipGroupSeqId" value = "${shipGroupSeqId?if_exists}"/>
                            <input type="hidden" name = "facilityId" value = "${facilityId?if_exists}"/>
                            <input type="hidden" name = "orderId" value = "${orderId?if_exists}"/>
                            <input type="hidden" name = "showWeightPackageForm" value = "Y"/>
                            <input type="hidden" name = "showCompletePackForm" value = "N"/>
                            <#assign currentPackageSeqId = Static["java.lang.Integer"].parseInt("${packageSequenceId}")/>
                            <#if "Y" == showWeightPackageForm && weightPackageSeqIds.size() != packageSeqIds.size()>
                              <hr>
                              <tr>
                                <#list packageSeqIds as packageSeqId>
                                  <#if packageSeqId == currentPackageSeqId>
                                    <td>
                                      <span class="label">${uiLabelMap.ProductPackedWeight} (${("uiLabelMap.ProductShipmentUomAbbreviation_" + defaultWeightUomId)?eval}):
                                        <br/>
                                        ${uiLabelMap.ProductPackage} ${packageSeqId}
                                        <input type="text" size="7" name="packageWeight" value="${packingSession.getPackageWeight(packageSeqId?int)?if_exists}"/>
                                      </span>
                                    </td>
                                    <td>
                                      <span class="label">${uiLabelMap.CommonDimension} (${("uiLabelMap.ProductShipmentUomAbbreviation_" + defaultDimensionUomId)?eval}):</span>
                                      <br/>
                                      <span class="label">${uiLabelMap.CommonLength}<input type="text" name="packageLength" value="${packingSession.getPackageLength(packageSeqId?int)?if_exists}" size="5"/></span>
                                      <span class="label">${uiLabelMap.ProductWidth}<input type="text" name="packageWidth" value="${packingSession.getPackageWidth(packageSeqId?int)?if_exists}" size="5"/></span>
                                      <span class="label">${uiLabelMap.PartyHeight}<input type="text" name="packageHeight" value="${packingSession.getPackageHeight(packageSeqId?int)?if_exists}" size="5"/></span>
                                    </td>
                                    <td>
                                      <span class="label">${uiLabelMap.ProductPackageInputBox}:</span>
                                      <br/>
                                      <select name="shipmentBoxTypeId">
                                        <#if shipmentBoxTypes?has_content>
                                          <option value=""></option>
                                          <#list shipmentBoxTypes as shipmentBoxType>
                                            <option value="${shipmentBoxType.shipmentBoxTypeId}">${shipmentBoxType.description}</option>
                                          </#list>
                                        </#if>
                                      </select>
                                    </td>
                                    <input type="hidden" name="packageSeqId" value="${packageSeqId}" size="5"/>
                                    <td align="right"><a href="javascript:document.weightPackageForm.submit()" class="buttontext">${uiLabelMap.ProductNextPackage}</a></td>
                                  </#if>
                                </#list>
                              </tr>
                            </#if>
                            <#assign currentIndex = Static["java.lang.Integer"].parseInt(currentIndex?default("0"))/>
                            <input type="hidden" name="currentIndex" value="${(currentIndex + 1)}"/>
                            <input type="hidden" name="weightPackageSeqId" value="${currentPackageSeqId}"/>
                            <#if weightPackageSeqIds.size() == packageSeqIds.size()>
                              <hr>
                              <div align="right">
                                <#assign buttonName = "${uiLabelMap.ProductComplete}"/>
                                <#if "true" == forceComplete?default("false")>
                                  <#assign buttonName = "${uiLabelMap.ProductCompleteForce}"/>
                                </#if>
                                <a href="javascript:document.completePackageForm.submit()" class="buttontext">${buttonName}</a>
                              </div>
                            </#if>
                          </form>
                        </table>
                      </#if>
                    </#if>
                    <form name="completePackageForm" method="post" action="<@ofbizUrl>completePackage</@ofbizUrl>">
                      <input type="hidden" name="orderId" value="${orderId?if_exists}"/>
                      <input type="hidden" name="showWeightPackageForm" value="Y"/>
                      <input type="hidden" name="shipGroupSeqId" value="${shipGroupSeqId?if_exists}"/>
                      <input type="hidden" name="facilityId" value="${facilityId?if_exists}"/>
                      <input type="hidden" name="forceComplete" value="${forceComplete?default('false')}"/>
                      <input type="hidden" name="dimensionUomId" value="${defaultDimensionUomId}"/>
                      <input type="hidden" name="weightUomId" value="${defaultWeightUomId}"/>
                      <input type="hidden" name="shipmentId" value="${(shipment.shipmentId)?default("")}"/>
                      <input type="hidden" name="invoiceId" value="${(invoice.invoiceId)?default("")}"/>
                      <input type="hidden" name="showInput" value="N"/>
                      <#if orderItemShipGroup?has_content>
                        <input type="hidden" name="shippingContactMechId" value="${orderItemShipGroup.contactMechId?if_exists}"/>
                        <input type="hidden" name="shipmentMethodTypeId" value="${orderItemShipGroup.shipmentMethodTypeId?if_exists}"/>
                        <input type="hidden" name="carrierPartyId" value="${orderItemShipGroup.carrierPartyId?if_exists}"/>
                        <input type="hidden" name="carrierRoleTypeId" value="${orderItemShipGroup.carrierRoleTypeId?if_exists}"/>
                        <input type="hidden" name="productStoreId" value="${productStoreId?if_exists}"/>
                      </#if>
                      <#if packageSeqIds?has_content>
                        <#list packageSeqIds as packageSeqId>
                          <input type="hidden" size="7" name="packageWeight_${packageSeqId}" value="${packingSession.getPackageWeight(packageSeqId?int)?if_exists}"/>
                        </#list>
                      </#if>
                      <#if shipmentCostEstimateForShipGroup?exists>
                        <input type="hidden" name="shipmentCostEstimateForShipGroup" value="${shipmentCostEstimateForShipGroup?if_exists}"/>
                      </#if>
                    </form>

                    <!-- complete form -->
                    <#if showInput != "N">
                    <#if "Y" == showCompletePackForm>
                        <form name="completePackForm" method="post" action="<@ofbizUrl>CompletePack</@ofbizUrl>">
                            <input type="hidden" name="orderId" value="${orderId?if_exists}"/>
                            <input type="hidden" name="showCompletePackForm" value="Y"/>
                            <input type="hidden" name="shipGroupSeqId" value="${shipGroupSeqId?if_exists}"/>
                            <input type="hidden" name="facilityId" value="${facilityId?if_exists}"/>
                            <input type="hidden" name="forceComplete" value="${forceComplete?default('false')}"/>
                            <input type="hidden" name="weightUomId" value="${defaultWeightUomId}"/>
                            <input type="hidden" name="shipmentId" value="${(shipment.shipmentId)?default("")}"/>
                            <input type="hidden" name="invoiceId" value="${(invoice.invoiceId)?default("")}"/>
                            <input type="hidden" name="showInput" value="N"/>
                            <hr>
                            <table class="basic-table" cellpadding="2" cellspacing='0'>
                                <tr>
                                    <#assign packageSeqIds = packingSession.getPackageSeqIds()/>
                                    <#if packageSeqIds?has_content>
                                        <td>
                                            <span class="label">${uiLabelMap.ProductPackedWeight} (${("uiLabelMap.ProductShipmentUomAbbreviation_" + defaultWeightUomId)?eval}):</span>
                                            <br/>
                                            <#list packageSeqIds as packageSeqId>
                                                ${uiLabelMap.ProductPackage} ${packageSeqId}
                                                <input type="text" size="7" name="packageWeight_${packageSeqId}" value="${packingSession.getPackageWeight(packageSeqId?int)?if_exists}">
                                                <br/>
                                            </#list>
                                            <#if orderItemShipGroup?has_content>
                                                <input type="hidden" name="shippingContactMechId" value="${orderItemShipGroup.contactMechId?if_exists}"/>
                                                <input type="hidden" name="shipmentMethodTypeId" value="${orderItemShipGroup.shipmentMethodTypeId?if_exists}"/>
                                                <input type="hidden" name="carrierPartyId" value="${orderItemShipGroup.carrierPartyId?if_exists}"/>
                                                <input type="hidden" name="carrierRoleTypeId" value="${orderItemShipGroup.carrierRoleTypeId?if_exists}"/>
                                                <input type="hidden" name="productStoreId" value="${productStoreId?if_exists}"/>
                                            </#if>
                                         </td>
                                    </#if>
                                    <td nowrap="nowrap">
                                        <span class="label">${uiLabelMap.ProductAdditionalShippingCharge}:</span>
                                        <br/>
                                        <input type="text" name="additionalShippingCharge" value="${packingSession.getAdditionalShippingCharge()?if_exists}" size="20"/>
                                        <#if packageSeqIds?has_content>
                                            <a href="javascript:document.completePackForm.action='<@ofbizUrl>calcPackSessionAdditionalShippingCharge</@ofbizUrl>';document.completePackForm.submit();" class="buttontext">${uiLabelMap.ProductEstimateShipCost}</a>
                                            <br/>
                                        </#if>
                                    </td>
                                    <td>
                                        <span class="label">${uiLabelMap.ProductHandlingInstructions}:</span>
                                        <br/>
                                        <textarea name="handlingInstructions" rows="2" cols="30">${packingSession.getHandlingInstructions()?if_exists}</textarea>
                                    </td>
                                    <td align="right">
                                        <div>
                                            <#assign buttonName = "${uiLabelMap.ProductComplete}">
                                            <#if forceComplete?default("false") == "true">
                                                <#assign buttonName = "${uiLabelMap.ProductCompleteForce}">
                                            </#if>
                                            <input type="button" value="${buttonName}" onclick="javascript:document.completePackForm.submit();"/>
                                        </div>
                                     </td>
                                 </tr>
                             </table>
                            <br/>
                        </form>
                    </#if>
                    </#if>
                </div>
            </div>

            <!-- display items in packages, per packed package and in order -->
            <#assign linesByPackageResultMap = packingSession.getPackingSessionLinesByPackage()?if_exists>
            <#assign packageMap = linesByPackageResultMap.get("packageMap")?if_exists>
            <#assign sortedKeys = linesByPackageResultMap.get("sortedKeys")?if_exists>
            <#if ((packageMap?has_content) && (sortedKeys?has_content))>
                <div class="screenlet">
                    <div class="screenlet-title-bar">
                        <ul>
                            <li class="h3">${uiLabelMap.ProductPackages} : ${sortedKeys.size()?if_exists}</li>
                        </ul>
                        <br class="clear"/>
                    </div>
                    <div class="screenlet-body">
                        <#list sortedKeys as key>
                            <#assign packedLines = packageMap.get(key)>
                            <#if packedLines?has_content>
                                <br/>
                                <#assign packedLine = packedLines.get(0)?if_exists>
                                <span class="label" style="font-size:1.2em">${uiLabelMap.ProductPackage}&nbsp;${packedLine.getPackageSeq()?if_exists}</span>
                                <br/>
                                <table class="basic-table" cellspacing='0'>
                                    <tr class="header-row">
                                        <td>${uiLabelMap.ProductItem} #</td>
                                        <td>${uiLabelMap.ProductProductId}</td>
                                        <td>${uiLabelMap.ProductProductDescription}</td>
                                        <td>${uiLabelMap.ProductInventoryItem} #</td>
                                        <td align="right">${uiLabelMap.ProductPackedQty}</td>
                                        <td align="right">${uiLabelMap.ProductPackedWeight}&nbsp;(${("uiLabelMap.ProductShipmentUomAbbreviation_" + defaultWeightUomId)?eval})&nbsp;(${uiLabelMap.ProductPackage})</td>
                                        <td align="right">${uiLabelMap.ProductPackage} #</td>
                                        <td>&nbsp;</td>
                                    </tr>
                                    <#list packedLines as line>
                                        <#assign product = Static["org.ofbiz.product.product.ProductWorker"].findProduct(delegator, line.getProductId())/>
                                        <tr>
                                            <td>${line.getOrderItemSeqId()}</td>
                                            <td>${line.getProductId()?default("N/A")}</td>
                                            <td>
                                                <a href="/catalog/control/EditProduct?productId=${line.getProductId()?if_exists}${externalKeyParam}" class="buttontext" target="_blank">${product.internalName?if_exists?default("[N/A]")}</a>
                                            </td>
                                            <td>${line.getInventoryItemId()}</td>
                                            <td align="right">${line.getQuantity()}</td>
                                            <td align="right">${line.getWeight()} (${packingSession.getPackageWeight(line.getPackageSeq()?int)?if_exists})</td>
                                            <td align="right">${line.getPackageSeq()}</td>
                                            <td align="right"><a href="javascript:clearLine('${facilityId}', '${line.getOrderId()}', '${line.getOrderItemSeqId()}', '${line.getProductId()?default("")}', '${line.getShipGroupSeqId()}', '${line.getInventoryItemId()}', '${line.getPackageSeq()}')" class="buttontext">${uiLabelMap.CommonClear}</a></td>
                                        </tr>
                                    </#list>
                                </table>
                            </#if>
                        </#list>
                    </div>
                </div>
            </#if>

            <!-- packed items display -->
            <#assign packedLines = packingSession.getLines()?if_exists>
            <#if packedLines?has_content>
                <div class="screenlet">
                    <div class="screenlet-title-bar">
                        <ul>
                            <li class="h3">${uiLabelMap.ProductItems} (${uiLabelMap.ProductPackages}): ${packedLines.size()?if_exists}</li>
                        </ul>
                        <br class="clear"/>
                    </div>
                    <div class="screenlet-body">
                        <table class="basic-table" cellspacing='0'>
                            <tr class="header-row">
                                <td>${uiLabelMap.ProductItem} #</td>
                                <td>${uiLabelMap.ProductProductId}</td>
                                <td>${uiLabelMap.ProductProductDescription}</td>
                                <td>${uiLabelMap.ProductInventoryItem} #</td>
                                <td align="right">${uiLabelMap.ProductPackedQty}</td>
                                <td align="right">${uiLabelMap.ProductPackedWeight}&nbsp;(${("uiLabelMap.ProductShipmentUomAbbreviation_" + defaultWeightUomId)?eval})&nbsp;(${uiLabelMap.ProductPackage})</td>
                                <td align="right">${uiLabelMap.ProductPackage} #</td>
                                <td>&nbsp;</td>
                            </tr>
                            <#list packedLines as line>
                                <#assign product = Static["org.ofbiz.product.product.ProductWorker"].findProduct(delegator, line.getProductId())/>
                                <tr>
                                    <td>${line.getOrderItemSeqId()}</td>
                                    <td>${line.getProductId()?default("N/A")}</td>
                                    <td>
                                        <a href="/catalog/control/EditProduct?productId=${line.getProductId()?if_exists}${externalKeyParam}" class="buttontext" target="_blank">${product.internalName?if_exists?default("[N/A]")}</a>
                                    </td>
                                    <td>${line.getInventoryItemId()}</td>
                                    <td align="right">${line.getQuantity()}</td>
                                    <td align="right">${line.getWeight()} (${packingSession.getPackageWeight(line.getPackageSeq()?int)?if_exists})</td>
                                    <td align="right">${line.getPackageSeq()}</td>
                                    <td align="right"><a href="javascript:clearLine('${facilityId}', '${line.getOrderId()}', '${line.getOrderItemSeqId()}', '${line.getProductId()?default("")}', '${line.getShipGroupSeqId()}', '${line.getInventoryItemId()}', '${line.getPackageSeq()}')" class="buttontext">${uiLabelMap.CommonClear}</a></td>
                                </tr>
                            </#list>
                        </table>
                    </div>
                </div>
            </#if>
        </#if>
    </#if>
    <#else>
      <div class="screenlet">
        <div class="screenlet-title-bar">
          <ul>
            <li class="h3">${uiLabelMap.WebtoolsWarningLogLevel}:</li>
          </ul>
          <br class="clear"/>
        </div>
        <div class="screenlet-body">
          <div>
           <h3>${uiLabelMap.FacilityWarningMessageThereIsMuchDifferenceInShippingCharges}</h3>
          </div>
          <form name="shipNowForm" method="post" action="<@ofbizUrl>shipNow</@ofbizUrl>">
            <input type="hidden" name="orderId" value="${orderId?if_exists}"/>
            <input type="hidden" name="shipGroupSeqId" value="${shipGroupSeqId?if_exists}"/>
            <input type="hidden" name="facilityId" value="${facilityId?if_exists}"/>
            <input type="hidden" name="dimensionUomId" value="${defaultDimensionUomId}"/>
            <input type="hidden" name="weightUomId" value="${defaultWeightUomId}"/>
            <input type="hidden" name="shipmentId" value="${(shipment.shipmentId)?default("")}"/>
            <input type="hidden" name="invoiceId" value="${(invoice.invoiceId)?default("")}"/>
            <input type="hidden" name="showInput" value="N"/>
          </form>
          <form name="holdShipmentForm" method="post" action="<@ofbizUrl>holdShipment</@ofbizUrl>">
            <input type="hidden" name="orderId" value="${orderId?if_exists}"/>
            <input type="hidden" name="shipGroupSeqId" value="${shipGroupSeqId?if_exists}"/>
            <input type="hidden" name="facilityId" value="${facilityId?if_exists}"/>
            <input type="hidden" name="shipmentId" value="${shipmentId?if_exists}"/>
            <input type="hidden" name="dimensionUomId" value="${defaultDimensionUomId}"/>
            <input type="hidden" name="weightUomId" value="${defaultWeightUomId}"/>
          </form>
          <div>
            <a href="javascript:document.shipNowForm.submit()" class="buttontext">${uiLabelMap.FacilityShip} ${uiLabelMap.CommonNow}</a>
            &nbsp;
            <a href="javascript:document.holdShipmentForm.submit()" class="buttontext">${uiLabelMap.FacilityHoldShipment}</a>
          </div>
        </div>
      </div>
    </#if>

    <#if orderId?has_content>
        <script language="javascript">
            document.singlePackForm.productId.focus();
        </script>
    <#else>
        <script language="javascript">
            document.selectOrderForm.orderId.focus();
        </script>
    </#if>
<#else>
    <h3>${uiLabelMap.ProductFacilityViewPermissionError}</h3>
</#if>