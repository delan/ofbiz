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
 *@author     Catherine.Heintz@nereide.biz (migration to UiLabel)
 *@version    $Revision: 1.16 $
 *@since      2.2
-->
<#assign uiLabelMap = requestAttributes.uiLabelMap>
<script language="JavaScript">
<!--
function setAssocFields(select) {
    var index = select.selectedIndex;
    var opt = select.options[index];
    var optStr = opt.value;
    var optLen = optStr.length;

    var shipmentMethodTypeId = "";
    var sequenceNumber = "";
    var roleTypeId = "";
    var partyId = "";
    var delIdx = 1;

    for (i=0; i<optLen; i++) {
        if (optStr.charAt(i) == '|') {
            delIdx++;
        } else {
            if (delIdx == 1) {
                partyId = partyId + optStr.charAt(i);
            } else if (delIdx == 2) {
                roleTypeId = roleTypeId + optStr.charAt(i);
            } else if (delIdx == 3) {
                shipmentMethodTypeId = shipmentMethodTypeId + optStr.charAt(i);
            } else if (delIdx == 4) {
                sequenceNumber = sequenceNumber + optStr.charAt(i);
            }
        }
    }

    document.addscarr.roleTypeId.value = roleTypeId;
    document.addscarr.partyId.value = partyId;
    document.addscarr.shipmentMethodTypeId.value = shipmentMethodTypeId;
    document.addscarr.sequenceNumber.value = sequenceNumber;
}
// -->
</script>

<#if hasPermission>
  ${pages.get("/store/ProductStoreTabBar.ftl")}
  <div class="head1">${uiLabelMap.ProductProductStoreShipmentSettings} <span class='head2'><#if (productStore.storeName)?has_content>"${productStore.storeName}"</#if> [${uiLabelMap.CommonId}:${productStoreId?if_exists}]</span></div>
  <a href="<@ofbizUrl>/EditProductStore</@ofbizUrl>" class="buttontext">[${uiLabelMap.ProductNewProductStore}]</a>
  <#if !requestParameters.createNew?exists || requestParameters.createNew != "Y">
    <a href="<@ofbizUrl>/EditProductStoreShipSetup?viewProductStoreId=${productStoreId}&createNew=Y</@ofbizUrl>" class="buttontext">[${uiLabelMap.ProductNewShipmentEstimate}]</a>
  </#if>
  <#if !requestParameters.newShipMethod?exists || requestParameters.newShipMethod != "Y">
    <a href="<@ofbizUrl>/EditProductStoreShipSetup?viewProductStoreId=${productStoreId}&newShipMethod=Y</@ofbizUrl>" class="buttontext">[${uiLabelMap.ProductNewShipmentMethod}]</a>
  </#if>
  <#if requestParameters.newShipMethod?default("N") == "Y" || requestParameters.createNew?default("N") == "Y">
    <a href="<@ofbizUrl>/EditProductStoreShipSetup?viewProductStoreId=${productStoreId}</@ofbizUrl>" class="buttontext">[${uiLabelMap.ProductViewEstimates}]</a>
  </#if>
  <br>
  <br>

  <#if !requestParameters.createNew?exists && !requestParameters.newShipMethod?exists>
    <table border="1" cellpadding="2" cellspacing="0" width="100%">
      <tr>
        <td nowrap><div class="tableheadtext">${uiLabelMap.ProductEstimateId}</div></td>
        <td nowrap><div class="tableheadtext">${uiLabelMap.ProductMethod}</div></td>
        <td nowrap><div class="tableheadtext">${uiLabelMap.CommonTo}</div></td>
        <td nowrap><div class="tableheadtext">${uiLabelMap.PartyParty}</div></td>
        <td nowrap><div class="tableheadtext">${uiLabelMap.PartyRole}</div></td>
        <td nowrap><div class="tableheadtext">${uiLabelMap.ProductBase}%</div></td>
        <td nowrap><div class="tableheadtext">${uiLabelMap.ProductBasePrc}</div></td>
        <td nowrap><div class="tableheadtext">${uiLabelMap.ProductItemPrc}</div></td>
        <td nowrap><div class="tableheadtext">&nbsp;</div></td>
      </tr>
      <#list estimates as estimate>
        <#assign weightValue = estimate.getRelatedOne("WeightQuantityBreak")?if_exists>
        <#assign quantityValue = estimate.getRelatedOne("QuantityQuantityBreak")?if_exists>
        <#assign priceValue = estimate.getRelatedOne("PriceQuantityBreak")?if_exists>
        <tr>
          <td><div class="tabletext">${estimate.shipmentCostEstimateId}</div></td>
          <td><div class="tabletext">${estimate.shipmentMethodTypeId}&nbsp;(${estimate.carrierPartyId})</div></td>
          <td><div class="tabletext">${estimate.geoIdTo?default("All")}</div></td>
          <td><div class="tabletext">${estimate.partyId?default("All")}</div></td>
          <td><div class="tabletext">${estimate.roleTypeId?default("All")}</div></td>
          <td><div class="tabletext">${estimate.orderPricePercent?default(0)?string.number}%</div></td>
          <td><div class="tabletext">${estimate.orderFlatPrice?default(0)?string.currency}</div></td>
          <td><div class="tabletext">${estimate.orderItemFlatPrice?default(0)?string.currency}</div></td>
          <td align="center">
            <div class="tabletext"><#if security.hasEntityPermission("SHIPRATE", "_DELETE", session)><a href="<@ofbizUrl>/storeRemoveShipRate?viewProductStoreId=${productStoreId}&shipmentCostEstimateId=${estimate.shipmentCostEstimateId}</@ofbizUrl>" class="buttontext">[${uiLabelMap.CommonDelete}]</a></#if> <a href="<@ofbizUrl>/EditProductStoreShipSetup?viewProductStoreId=${productStoreId}&shipmentCostEstimateId=${estimate.shipmentCostEstimateId}</@ofbizUrl>" class="buttontext">[${uiLabelMap.CommonView}]</a></div>
          </td>
        </tr>
      </#list>
    </table>
  </#if>

  <#if shipEstimate?has_content>
    <#assign estimate = shipEstimate>
    <#assign weightValue = estimate.getRelatedOne("WeightQuantityBreak")?if_exists>
    <#assign quantityValue = estimate.getRelatedOne("QuantityQuantityBreak")?if_exists>
    <#assign priceValue = estimate.getRelatedOne("PriceQuantityBreak")?if_exists>
    <br>
      <table cellspacing="2" cellpadding="2">
        <tr>
          <td align='right'><span class="tableheadtext">${uiLabelMap.ProductShipmentMethod}</span></td>
          <td><span class="tabletext">${estimate.shipmentMethodTypeId}&nbsp;(${estimate.carrierPartyId})</span></td>
          <td>&nbsp;</td>
        </tr>
        <tr>
          <td align='right'><span class="tableheadtext">${uiLabelMap.ProductFromGeo}</span></td>
          <td><span class="tabletext">${estimate.geoIdFrom?default("All")}</span></td>
          <td>&nbsp;</td>
        </tr>
        <tr>
          <td align='right'><span class="tableheadtext">${uiLabelMap.ProductToGeo}</span></td>
          <td><span class="tabletext">${estimate.geoIdTo?default("All")}</span></td>
          <td>&nbsp;</td>
        </tr>
        <tr>
          <td align='right'><span class="tableheadtext">${uiLabelMap.PartyParty}</span></td>
          <td><span class="tabletext">${estimate.partyId?default("All")}</span></td>
          <td>&nbsp;</td>
        </tr>
        <tr>
          <td align='right'><span class="tableheadtext">${uiLabelMap.PartyRole}</span></td>
          <td><span class="tabletext">${estimate.roleTypeId?default("All")}</span></td>
          <td>&nbsp;</td>
        </tr>
        <tr><td colspan="3"><hr class="sepbar"></td></tr>
        <tr>
          <td align='right'><span class="tableheadtext">${uiLabelMap.ProductFlatBasePercent}</span></td>
          <td>
            <span class="tabletext">${estimate.orderPricePercent?default(0)?string.number}%</span>
            <span class="tabletext"> - ${uiLabelMap.ProductShipamountOrderTotalPercent}</span>
          </td>
          <td>&nbsp;</td>
        </tr>
        <tr>
          <td align='right'><span class="tableheadtext">${uiLabelMap.ProductFlatBasePrice}</span></td>
          <td>
            <span class="tabletext">${estimate.orderFlatPrice?default(0)?string.currency}</span>
            <span class="tabletext"> - ${uiLabelMap.ProductShipamountPrice}</span>
          </td>
          <td>&nbsp;</td>
        </tr>
        <tr>
          <td align='right'><span class="tableheadtext">${uiLabelMap.ProductFlatItemPrice}</span></td>
          <td>
            <span class="tabletext">${estimate.orderItemFlatPrice?default(0)?string.currency}</span>
            <span class="tabletext"> - ${uiLabelMap.ProductShipamountTotalQuantityPrice}</span>
          </td>
          <td>&nbsp;</td>
        </tr>
        <tr><td colspan="3"><hr class="sepbar"></td></tr>
        <tr>
          <td align='right'><span class="tableheadtext">Feature Group</span></td>
          <td>
            <span class="tabletext">${estimate.productFeatureGroupId?default("N/A")}</span>
            <span class="tabletext"> - Below surcharge(s) will be added per-product * per-feature</span>
          </td>
          <td>&nbsp;</td>
        </tr>
        <tr>
          <td align='right'><span class="tableheadtext">Per-Feature Percent</span></td>
          <td>
            <span class="tabletext">${estimate.featurePercent?default(0)?string.number}%</span>
            <span class="tabletext"> - shipamount : shipamount + ((orderTotal * percent) * total feature(s) applied)</span>
          </td>
          <td>&nbsp;</td>
        </tr>
        <tr>
          <td align='right'><span class="tableheadtext">Per-Feature Price</span></td>
          <td>
            <span class="tabletext">${estimate.featurePrice?default(0)?string.currency}</span>
            <span class="tabletext"> - shipamount : shipamount + (price * total feature(s) applied)</span>
          </td>
          <td>&nbsp;</td>
        </tr>
        <tr><td colspan="3"><hr class="sepbar"></td></tr>
        <tr>
          <td align='right'><span class="tableheadtext">Oversize Unit</span></td>
          <td>
            <span class="tabletext">${estimate.oversizeUnit?default("N/A")}</span>
            <span class="tabletext"> - Each product (height + width + depth) >= this amount</span>
          </td>
          <td>&nbsp;</td>
        </tr>
        <tr>
          <td align='right'><span class="tableheadtext">Oversize Surcharge</span></td>
          <td>
            <span class="tabletext">${estimate.oversizePrice?default(0)?string.currency}</span>
            <span class="tabletext"> - shipamount : shipamount + (# oversize products * surcharge)</span>
          </td>
          <td>&nbsp;</td>
        </tr>
        <tr><td colspan="3"><hr class="sepbar"></td></tr>
        <tr>
          <td colspan="1"><span class="tableheadtext">${uiLabelMap.ProductWeight}</span></td>
          <td colspan="2"><span class="tabletext">${uiLabelMap.ProductMinMax}</span></td>
        </tr>
        <tr>
          <td align='right'><span class="tableheadtext">${uiLabelMap.ProductMinMaxSpan}</span></td>
          <td><span class="tabletext">${weightValue.fromQuantity?if_exists}-${weightValue.thruQuantity?if_exists}</span></td>
          <td>&nbsp;</td>
        </tr>
        <tr>
          <td align='right'><span class="tableheadtext">${uiLabelMap.ProductUnitOfMeasure}</span></td>
          <td><span class="tabletext">${estimate.weightUomId?if_exists}</span></td>
          <td>&nbsp;</td>
        </tr>
        <tr>
          <td align='right'><span class="tableheadtext">${uiLabelMap.ProductPerUnitPrice}</span></td>
          <td>
            <span class="tabletext">${estimate.weightUnitPrice?default(0)?string.currency}</span>
            <span class="tabletext"> -${uiLabelMap.ProductOnlyAppliesWithinSpan}</span>
          </td>
          <td>&nbsp;</td>
        </tr>
        <tr><td colspan="3"><hr class="sepbar"></td></tr>
        <tr>
          <td colspan="1"><span class="tableheadtext">${uiLabelMap.ProductQuantity}</span></td>
          <td colspan="2"><span class="tabletext">${uiLabelMap.ProductMinMax}</span></td>
        </tr>
        <tr>
          <td align='right'><span class="tableheadtext">${uiLabelMap.ProductMinMaxSpan}</span></td>
          <td><span class="tabletext">${quantityValue.fromQuantity?if_exists}-${quantityValue.thruQuantity?if_exists}</span></td>
          <td>&nbsp;</td>
        </tr>
        <tr>
          <td align='right'><span class="tableheadtext">${uiLabelMap.ProductUnitOfMeasure}</span></td>
          <td><span class="tabletext">${estimate.quantityUomId?if_exists}</span></td>
          <td>&nbsp;</td>
        </tr>
        <tr>
          <td align='right'><span class="tableheadtext">${uiLabelMap.ProductPerUnitPrice}</span></td>
          <td>
            <span class="tabletext">${estimate.quantityUnitPrice?default(0)?string.currency}</span>
            <span class="tabletext"> - ${uiLabelMap.ProductOnlyAppliesWithinSpan}</span>
          </td>
          <td>&nbsp;</td>
        </tr>
        <tr><td colspan="3"><hr class="sepbar"></td></tr>
        <tr>
          <td colspan="1"><span class="tableheadtext">${uiLabelMap.ProductPrice}</span></td>
          <td colspan="2"><span class="tabletext">${uiLabelMap.ProductMinMax}</span></td>
        </tr>
        <tr>
          <td align='right'><span class="tableheadtext">${uiLabelMap.ProductMinMaxSpan}</span></td>
          <td><span class="tabletext">${priceValue.fromQuantity?if_exists}-${priceValue.thruQuantity?if_exists}</span></td>
          <td>&nbsp;</td>
        </tr>
        <tr>
          <td align='right'><span class="tableheadtext">${uiLabelMap.ProductPerUnitPrice}</span></td>
          <td>
            <span class="tabletext">${estimate.priceUnitPrice?default(0)?string.currency}</span>
            <span class="tabletext"> - ${uiLabelMap.ProductOnlyAppliesWithinSpan}</span>
          </td>
          <td>&nbsp;</td>
        </tr>
      </table>
  </#if>

  <#if requestParameters.createNew?exists>
    <div class="head2">${uiLabelMap.ProductNewShipmentEstimate} :</div>
    <form name="addform" method="post" action="<@ofbizUrl>/storeCreateShipRate</@ofbizUrl>">
      <input type="hidden" name="viewProductStoreId" value="${productStoreId}">
      <input type="hidden" name="productStoreId" value="${productStoreId}">
      <table cellspacing="2" cellpadding="2">
        <tr>
          <td align='right'><span class="tableheadtext">${uiLabelMap.ProductShipmentMethod}</span></td>
          <td>
            <select name="shipMethod" class="selectBox">
              <#list storeShipMethods as shipmentMethod>
                <option value="${shipmentMethod.partyId}|${shipmentMethod.shipmentMethodTypeId}">${shipmentMethod.description} (${shipmentMethod.partyId})</option>
              </#list>
            </select>
          </td>
          <td>&nbsp;</td>
        </tr>
        <tr>
          <td align='right'><span class="tableheadtext">${uiLabelMap.ProductFromGeo}</span></td>
          <td>
            <select name="fromGeo" class="selectBox">
              <option value="">${uiLabelMap.CommonAll}</option>
              <#list geoList as geo>
                <option value="${geo.geoId}">${geo.geoName}</option>
              </#list>
            </select>
          </td>
          <td>&nbsp;</td>
        </tr>
        <tr>
          <td align='right'><span class="tableheadtext">${uiLabelMap.ProductToGeo}</span></td>
          <td>
            <select name="toGeo" class="selectBox">
              <option value="">${uiLabelMap.CommonAll}</option>
              <#list geoList as geo>
                <option value="${geo.geoId}">${geo.geoName}</option>
              </#list>
            </select>
          </td>
          <td>&nbsp;</td>
        </tr>
        <tr>
          <td align='right'><span class="tableheadtext">${uiLabelMap.PartyParty}</span></td>
          <td><input type="text" class="inputBox" name="partyId" size="6"></td>
          <td>&nbsp;</td>
        </tr>
        <tr>
          <td align='right'><span class="tableheadtext">${uiLabelMap.PartyRole}</span></td>
          <td><input type="text" class="inputBox" name="roleTyeId" size="6"></td>
          <td>&nbsp;</td>
        </tr>
        <tr><td colspan="3"><hr class="sepbar"></td></tr>
        <tr>
          <td align='right'><span class="tableheadtext">${uiLabelMap.ProductFlatBasePercent}</span></td>
          <td>
            <input type="text" class="inputBox" name="flatPercent" value="0" size="5">
            <span class="tabletext">${uiLabelMap.ProductShipamountOrderTotalPercent}</span>
          </td>
          <td>&nbsp;</td>
        </tr>
        <tr>
          <td align='right'><span class="tableheadtext">${uiLabelMap.ProductFlatBasePrice}</span></td>
          <td>
            <input type="text" class="inputBox" name="flatPrice" value="0.00" size="5">
            <span class="tabletext">${uiLabelMap.ProductShipamountPrice}</span>
          </td>
          <td>&nbsp;</td>
        </tr>
        <tr>
          <td align='right'><span class="tableheadtext">${uiLabelMap.ProductFlatItemPrice}</span></td>
          <td>
            <input type="text" class="inputBox" name="flatItemPrice" value="0.00" size="5">
            <span class="tabletext">${uiLabelMap.ProductShipamountTotalQuantityPrice}</span>
          </td>
          <td>&nbsp;</td>
        </tr>
        <tr><td colspan="3"><hr class="sepbar"></td></tr>
        <tr>
          <td align='right'><span class="tableheadtext">Feature Group</span></td>
          <td>
            <input type="text" class="inputBox" name="productFeatureGroupId" value="" size="15">
            <span class="tabletext">Below surcharge(s) will be added per-product * per-feature</span>
          </td>
          <td>&nbsp;</td>
        </tr>
        <tr>
          <td align='right'><span class="tableheadtext">Per-Feature Percent</span></td>
          <td>
            <input type="text" class="inputBox" name="featurePercent" value="0" size="5">
            <span class="tabletext">shipamount : shipamount + ((orderTotal * percent) * total feature(s) applied)</span>
          </td>
          <td>&nbsp;</td>
        </tr>
        <tr>
          <td align='right'><span class="tableheadtext">Per-Feature Price</span></td>
          <td>
            <input type="text" class="inputBox" name="featurePrice" value="0.00" size="5">
            <span class="tabletext">shipamount : shipamount + (price * total feature(s) applied)</span>
          </td>
          <td>&nbsp;</td>
        </tr>
        <tr><td colspan="3"><hr class="sepbar"></td></tr>
        <tr>
          <td align='right'><span class="tableheadtext">Oversize Unit</span></td>
          <td>
            <input type="text" class="inputBox" name="oversizeUnit" value="" size="5">
            <span class="tabletext">Each product (height + width + depth) >= this amount</span>
          </td>
          <td>&nbsp;</td>
        </tr>
        <tr>
          <td align='right'><span class="tableheadtext">Oversize Surcharge</span></td>
          <td>
            <input type="text" class="inputBox" name="oversizePrice" value="0.00" size="5">
            <span class="tabletext">shipamount : shipamount + (# oversize products * surcharge)</span>
          </td>
          <td>&nbsp;</td>
        </tr>
        <tr><td colspan="3"><hr class="sepbar"></td></tr>
        <tr>
          <td colspan="1"><span class="tableheadtext">${uiLabelMap.ProductWeight}</span></td>
          <td colspan="2"><span class="tabletext">${uiLabelMap.ProductMinMax}</span></td>
        </tr>
        <tr>
          <td align='right'><span class="tableheadtext">${uiLabelMap.ProductMinMaxSpan}</span></td>
          <td>
            <input type="text" class="inputBox" name="wmin" size="4"> - <input type="text" class="inputBox" name="wmax" size="4">
          </td>
          <td>&nbsp;</td>
        </tr>
        <tr>
          <td align='right'><span class="tableheadtext">${uiLabelMap.ProductUnitOfMeasure}</span></td>
          <td>
            <select name="wuom" class="selectBox">
              <#list weightUoms as uom>
                <option value="${uom.uomId}">${uom.description}</option>
              </#list>
            </select>
          </td>
          <td>&nbsp;</td>
        </tr>
        <tr>
          <td align='right'><span class="tableheadtext">${uiLabelMap.ProductPerUnitPrice}</span></td>
          <td>
            <input type="text" class='inputBox' name="wprice" size="5">
            <span class="tabletext">${uiLabelMap.ProductOnlyAppliesWithinSpan}</span>
          </td>
          <td>&nbsp;</td>
        </tr>
        <tr><td colspan="3"><hr class="sepbar"></td></tr>
        <tr>
          <td colspan="1"><span class="tableheadtext">${uiLabelMap.ProductQuantity}</span></td>
          <td colspan="2"><span class="tabletext">${uiLabelMap.ProductMinMax}</span></td>
        </tr>
        <tr>
          <td align='right'><span class="tableheadtext">${uiLabelMap.ProductMinMaxSpan}</span></td>
          <td><input type="text" class="inputBox" name="qmin" size="4"> - <input type="text" class="inputBox" name="qmax" size="4"></td>
          <td>&nbsp;</td>
        </tr>
        <tr>
          <td align='right'><span class="tableheadtext">${uiLabelMap.ProductUnitOfMeasure}</span></td>
          <td>
            <select name="quom" class="selectBox">
              <#list quantityUoms as uom>
                <option value="${uom.uomId}">${uom.description}</option>
              </#list>
            </select>
          </td>
          <td>&nbsp;</td>
        </tr>
        <tr>
          <td align='right'><span class="tableheadtext">${uiLabelMap.ProductPerUnitPrice}</span></td>
          <td>
            <input type="text" class='inputBox' name="qprice" size="5">
            <span class="tabletext">${uiLabelMap.ProductOnlyAppliesWithinSpan}</span>
          </td>
          <td>&nbsp;</td>
        </tr>
        <tr><td colspan="3"><hr class="sepbar"></td></tr>
        <tr>
          <td colspan="1"><span class="tableheadtext">${uiLabelMap.ProductPrice}</span></td>
          <td colspan="2"><span class="tabletext">${uiLabelMap.ProductMinMax}</span></td>
        <tr>
        <tr>
          <td align='right'><span class="tableheadtext">${uiLabelMap.ProductMinMaxSpan}</span></td>
          <td><input type="text" class="inputBox" name="pmin" size="4"> - <input type="text" class="inputBox" name="pmax" size="4"></td>
          <td>&nbsp;</td>
        </tr>
        <tr>
          <td align='right'><span class="tableheadtext">${uiLabelMap.ProductPerUnitPrice}</span></td>
          <td>
            <input type="text" class='inputBox' name="pprice" size="5">
            <span class="tabletext">${uiLabelMap.ProductOnlyAppliesWithinSpan}</span>
          </td>
          <td>&nbsp;</td>
        </tr>

        <tr>
          <td colspan="3">
            <input type="submit" class="smallSubmit" value="${uiLabelMap.CommonAdd}">
          </td>
        </tr>
      </table>
    </form>
  </#if>

  <#if requestParameters.newShipMethod?exists>
    <div class="head2">${uiLabelMap.ProductStoreShipmentMethodAssociations}</div>
    <table border="1" cellpadding="2" cellspacing="0" width="100%">
      <tr>
        <td><span class="tableheadtext">${uiLabelMap.ProductMethodType}</span></td>
        <td><span class="tableheadtext">${uiLabelMap.PartyParty}</span></td>
        <td><span class="tableheadtext">Min Sz</span></td>
        <td><span class="tableheadtext">Max Sz</span></td>
        <td><span class="tableheadtext">Min Wt</span></td>
        <td><span class="tableheadtext">Max Wt</span></td>
        <td><span class="tableheadtext">Min $</span></td>
        <td><span class="tableheadtext">Max $</span></td>
        <td><span class="tableheadtext">Allow USPS</span></td>
        <td><span class="tableheadtext">Req USPS</span></td>
        <td><span class="tableheadtext">Allow Co</span></td>
        <td><span class="tableheadtext">Req Co</span></td>
        <td><span class="tableheadtext">Inc FreeShip</span></td>
        <td><span class="tableheadtext">Inc Geo</span></td>
        <td><span class="tableheadtext">Exc Geo</span></td>
        <td><span class="tableheadtext">Inc Feature</span></td>
        <td><span class="tableheadtext">Exc Feature</span></td>
        <td><span class="tableheadtext">Seq</span></td>
        <td>&nbsp;</td>
      </tr>
      <#if storeShipMethods?has_content>
        <#assign idx = 0>
        <#list storeShipMethods as meth>
          <#assign idx = idx + 1>
          <form name="methUpdate${idx}" method="post" action="<@ofbizUrl>/storeUpdateShipMeth</@ofbizUrl>">
            <input type="hidden" name="shipmentMethodTypeId" value="${meth.shipmentMethodTypeId}">
            <input type="hidden" name="partyId" value="${meth.partyId}">
            <input type="hidden" name="roleTypeId" value="${meth.roleTypeId}">
            <input type="hidden" name="productStoreId" value="${meth.productStoreId}">
            <input type="hidden" name="viewProductStoreId" value="${productStoreId}">
            <input type="hidden" name="newShipMethod" value="Y">
            <tr>
              <td><span class="tabletext">${meth.description}</span></td>
              <td><span class="tabletext">${meth.partyId}</span></td>
              <td><span class="tabletext">${meth.minSize?if_exists}</span></td>
              <td><span class="tabletext">${meth.maxSize?if_exists}</span></td>
              <td><span class="tabletext">${meth.minWeight?if_exists}</span></td>
              <td><span class="tabletext">${meth.maxWeight?if_exists}</span></td>
              <td><span class="tabletext">${meth.minTotal?default(0)?string("##0.00")}</span></td>
              <td><span class="tabletext">${meth.maxTotal?default(0)?string("##0.00")}</span></td>
              <td><span class="tabletext">${meth.allowUspsAddr?default("N")}</span></td>
              <td><span class="tabletext">${meth.requireUspsAddr?default("N")}</span></td>
              <td><span class="tabletext">${meth.allowCompanyAddr?default("N")}</span></td>
              <td><span class="tabletext">${meth.requireCompanyAddr?default("N")}</span></td>
              <td><span class="tabletext">${meth.includeNoChargeItems?default("Y")}</span></td>
              <td><span class="tabletext">${meth.includeGeoId?if_exists}</span></td>
              <td><span class="tabletext">${meth.excludeGeoId?if_exists}</span></td>
              <td><span class="tabletext">${meth.includeFeatureGroup?if_exists}</span></td>
              <td><span class="tabletext">${meth.excludeFeatureGroup?if_exists}</span></td>
              <td><input type="text" size="5" class="inputBox" name="sequenceNumber" value="${meth.sequenceNumber?if_exists}"></td>
              <td width='1' align="right">
                <nobr>
                  <a href="javascript:document.methUpdate${idx}.submit();" class="buttontext">[${uiLabelMap.CommonUpdate}]</a>
                  <a href="<@ofbizUrl>/storeRemoveShipMeth?viewProductStoreId=${productStoreId}&productStoreId=${meth.productStoreId}&newShipMethod=Y&shipmentMethodTypeId=${meth.shipmentMethodTypeId}&partyId=${meth.partyId}&roleTypeId=${meth.roleTypeId}</@ofbizUrl>" class="buttontext">[${uiLabelMap.CommonRemove}]</a>
                </nobr>
              </td>
            </tr>
          </form>
        </#list>
      </#if>
    </table>
    <br>
    <table cellspacing="2" cellpadding="2">
      <form name="addscarr" method="post" action="<@ofbizUrl>/storeCreateShipMeth</@ofbizUrl>">
        <input type="hidden" name="viewProductStoreId" value="${productStoreId}">
        <input type="hidden" name="newShipMethod" value="Y">
        <input type="hidden" name="productStoreId" value="${productStoreId}">
        <input type="hidden" name="shipmentMethodTypeId">
        <input type="hidden" name="roleTypeId">
        <input type="hidden" name="partyId">
        <tr>
          <td align="right"><span class="tableheadtext">${uiLabelMap.ProductCarrierShipmentMethod}</span></td>
          <td>
            <select class="selectBox" name="carrierShipmentString" onChange="javascript:setAssocFields(this);">
              <option>${uiLabelMap.ProductSelectOne}</option>
              <#list shipmentMethods as shipmentMethod>
                <option value="${shipmentMethod.partyId}|${shipmentMethod.roleTypeId}|${shipmentMethod.shipmentMethodTypeId}|${shipmentMethod.sequenceNumber?default(1)}">${shipmentMethod.description} (${shipmentMethod.partyId}/${shipmentMethod.roleTypeId})</option>
              </#list>
            </select> *
          </td>
        </tr>
        <tr>
          <td align="right"><span class="tableheadtext">Min Size</span></td>
          <td>
            <input type="text" class="inputBox" name="minSize" size="5">
            <span class="tabletext">Displays only if smallest product size is equal/greater then this value</span>
          </td>
        </tr>
        <tr>
          <td align="right"><span class="tableheadtext">Max Size</span></td>
          <td>
            <input type="text" class="inputBox" name="maxSize" size="5">
            <span class="tabletext">Displays only if largest product size is equal/less then this value</span>
          </td>
        </tr>
        <tr>
          <td align="right"><span class="tableheadtext">Min Weight</span></td>
          <td>
            <input type="text" class="inputBox" name="minWeight" size="5">
            <span class="tabletext">Displays only if total weight is equal/greater then this value</span>
          </td>
        </tr>
        <tr>
          <td align="right"><span class="tableheadtext">Max Weight</span></td>
          <td>
            <input type="text" class="inputBox" name="maxWeight" size="5">
            <span class="tabletext">Displays only if total weight is equal/less then this value</span>
          </td>
        </tr>
        <tr>
          <td align="right"><span class="tableheadtext">Min Total</span></td>
          <td>
            <input type="text" class="inputBox" name="minTotal" size="5">
            <span class="tabletext">Displays only if total price is equal/greater then this value</span>
          </td>
        </tr>
        <tr>
          <td align="right"><span class="tableheadtext">Max Total</span></td>
          <td>
            <input type="text" class="inputBox" name="maxTotal" size="5">
            <span class="tabletext">Displays only if total price is equal/less then this value</span>
          </td>
        </tr>
        <tr>
          <td align="right"><span class="tableheadtext">Allow USPS Addr (PO Box, RR, etc)</span></td>
          <td>
            <select name="allowUspsAddr" class="selectBox">
              <option>N</option>
              <option>Y</option>
            </select>
          </td>
        </tr>
        <tr>
          <td align="right"><span class="tableheadtext">Require USPS Addr (PO Box, RR, etc)</span></td>
          <td>
            <select name="requireUspsAddr" class="selectBox">
              <option>N</option>
              <option>Y</option>
            </select>
            <span class="tabletext">Setting ignored if Allow is 'N'</span>
          </td>
        </tr>
        <tr>
          <td align="right"><span class="tableheadtext">Allow Company Addr</span></td>
          <td>
            <select name="allowCompanyAddr" class="selectBox">
              <option>N</option>
              <option>Y</option>
            </select>
          </td>
        </tr>
        <tr>
          <td align="right"><span class="tableheadtext">Require Company Addr</span></td>
          <td>
            <select name="requireCompanyAddr" class="selectBox">
              <option>N</option>
              <option>Y</option>
            </select>
            <span class="tabletext">Setting ignored if Allow is 'N'</span>
          </td>
        </tr>
        <tr>
          <td align="right"><span class="tableheadtext">Company Party ID</span></td>
          <td>
            <input type="text" class="inputBox" name="companyPartyId" size="20">
            <span class="tabletext">Used with allow company address</span>
          </td>
        </tr>
        <tr>
          <td align="right"><span class="tableheadtext">Include Free Ship Items</span></td>
          <td>
            <select name="includeNoChargeItems" class="selectBox">
              <option>Y</option>
              <option>N</option>
            </select>
            <span class="tabletext">Set to N to hide when the cart contains ONLY free shipping items</span>
          </td>
        </tr>
        <tr>
          <td align="right"><span class="tableheadtext">Include GeoId</span></td>
          <td>
            <select name="includeGeoId" class="selectBox">
              <option></option>
              <#list geoList as geo>
                <option value="${geo.geoId}">${geo.geoName}</option>
              </#list>
            </select>
            <span class="tabletext">Displays only if ship-to is in this geo</span>
          </td>
        </tr>
        <tr>
          <td align="right"><span class="tableheadtext">Exclude GeoId</span></td>
          <td>
            <select name="excludeGeoId" class="selectBox">
              <option></option>
              <#list geoList as geo>
                <option value="${geo.geoId}">${geo.geoName}</option>
              </#list>
            </select>
            <span class="tabletext">Displays only if ship-to is not in this geo</span>
          </td>
        </tr>
        <tr>
          <td align="right"><span class="tableheadtext">Include Feature Group</span></td>
          <td>
            <input type="text" class="inputBox" name="includeFeatureGroup" size="20">
            <span class="tabletext">Displays only if all items have all features in this group</span>
          </td>
        </tr>
        <tr>
          <td align="right"><span class="tableheadtext">Exclude Feature Group</span></td>
          <td>
            <input type="text" class="inputBox" name="excludeFeatureGroup" size="20">
            <span class="tabletext">Displays only if all items have no features in this group</span>
          </td>
        </tr>
        <tr>
          <td align="right"><span class="tableheadtext">Service Name</span></td>
          <td>
            <input type="text" class="inputBox" name="serviceName" size="25">
            <span class="tabletext"></span>
          </td>
        </tr>
        <tr>
          <td align="right"><span class="tableheadtext">Service Config</span></td>
          <td>
            <input type="text" class="inputBox" name="configProps" size="25">
            <span class="tabletext"></span>
          </td>
        </tr>
        <tr>
          <td align="right"><span class="tableheadtext">${uiLabelMap.ProductSequence}#</span></td>
          <td>
            <input type="text" class="inputBox" name="sequenceNumber" size="5">
            <span class="tabletext">${uiLabelMap.ProductUsedForDisplayOrdering}</span>
          </td>
        </tr>
        <tr>
          <td>
            <input type="submit" class="smallSubmit" value="${uiLabelMap.CommonAdd}">
          </td>
        </tr>
      </form>
    </table>
    <br>

    <div class="head2">${uiLabelMap.ProductShipmentMethodType} :</div>
    <table cellspacing="2" cellpadding="2">
      <form name="editmeth" method="post" action="<@ofbizUrl>/EditProductStoreShipSetup</@ofbizUrl>">
        <input type="hidden" name="viewProductStoreId" value="${productStoreId}">
        <input type="hidden" name="newShipMethod" value="Y">
        <tr>
          <td align="right"><span class="tableheadtext">${uiLabelMap.ProductSelectToEdit}</span></td>
          <td>
            <select class="selectBox" name="editShipmentMethodTypeId">
              <#list shipmentMethodTypes as shipmentMethodType>
                <option value="${shipmentMethodType.shipmentMethodTypeId}">${shipmentMethodType.description?default(shipmentMethodType.shipmentMethodTypeId)}</option>
              </#list>
            </select>
            <input type="submit" class="smallSubmit" value="${uiLabelMap.CommonEdit}">
          </td>
        </tr>
      </form>
      <#if shipmentMethodType?has_content>
        <#assign webRequest = "/updateShipmentMethodType">
        <#assign buttonText =uiLabelMap.CommonUpdate>
      <#else>
        <#assign webRequest = "/createShipmentMethodType">
        <#assign buttonText = uiLabelMap.CommonCreate>
      </#if>
      <form name="addmeth" method="post" action="<@ofbizUrl>${webRequest}</@ofbizUrl>">
        <input type="hidden" name="viewProductStoreId" value="${productStoreId}">
        <input type="hidden" name="newShipMethod" value="Y">
        <tr>
          <td align="right"><span class="tableheadtext">${uiLabelMap.ProductShipmentMethodType}</span></td>
          <td>
            <#if shipmentMethodType?has_content>
              <div class="tabletext">${shipmentMethodType.shipmentMethodTypeId}</div>
              <input type="hidden" name="shipmentMethodTypeId" value="${shipmentMethodType.shipmentMethodTypeId}">
            <#else>
              <input type="text" class="inputBox" name="shipmentMethodTypeId" size="20"> *</td>
            </#if>
          </td>
        </tr>
        <tr>
          <td align="right"><span class="tableheadtext">${uiLabelMap.ProductDescription}</span></td>
          <td><input type="text" class="inputBox" name="description" size="30" value="${shipmentMethodType.description?if_exists}"> *</td>
        </tr>
        <tr>
          <td>
            <input type="submit" class="smallSubmit" value="${buttonText}">
          </td>
        </tr>
      </form>
    </table>

    <br>

    <div class="head2">${uiLabelMap.ProductCarrierShipmentMethod} :</div>
    <table cellspacing="2" cellpadding="2">
      <form name="editcarr" method="post" action="<@ofbizUrl>/EditProductStoreShipSetup</@ofbizUrl>">
        <input type="hidden" name="viewProductStoreId" value="${productStoreId}">
        <input type="hidden" name="newShipMethod" value="Y">
        <tr>
          <td align="right"><span class="tableheadtext">${uiLabelMap.ProductSelectToEdit}</span></td>
          <td>
            <select class="selectBox" name="editCarrierShipmentMethodId">
              <#list shipmentMethods as shipmentMethod>
                <option value="${shipmentMethod.partyId}|${shipmentMethod.roleTypeId}|${shipmentMethod.shipmentMethodTypeId}">${shipmentMethod.description} (${shipmentMethod.partyId}/${shipmentMethod.roleTypeId})</option>
              </#list>
            </select>
            <input type="submit" class="smallSubmit" value="${uiLabelMap.CommonEdit}">
          </td>
        </tr>
      </form>
      <#if carrierShipmentMethod?has_content>
        <#assign webRequest = "/updateCarrierShipmentMethod">
        <#assign buttonText = uiLabelMap.CommonUpdate>
      <#else>
        <#assign webRequest = "/createCarrierShipmentMethod">
        <#assign buttonText = uiLabelMap.CommonCreate>
      </#if>
      <form name="addcarr" method="post" action="<@ofbizUrl>${webRequest}</@ofbizUrl>">
        <input type="hidden" name="viewProductStoreId" value="${productStoreId}">
        <#if carrierShipmentMethod?has_content>
          <input type="hidden" name="newShipMethod" value="Y">
        <#else>
          <input type="hidden" name="createNew" value="Y">
        </#if>
        <tr>
          <td align="right"><span class="tableheadtext">${uiLabelMap.ProductShipmentMethod}</span></td>
          <td>
            <#if carrierShipmentMethod?has_content>
              <input type="hidden" name="shipmentMethodTypeId" value="${carrierShipmentMethod.shipmentMethodTypeId}">
              <div class="tabletext">${carrierShipmentMethod.shipmentMethodTypeId}</div>
            <#else>
              <select class="selectBox" name="shipmentMethodTypeId">
                <#list shipmentMethodTypes as shipmentMethodType>
                  <option value="${shipmentMethodType.shipmentMethodTypeId}">${shipmentMethodType.description?default(shipmentMethodType.shipmentMethodTypeId)}</option>
                </#list>
              </select> *
            </#if>
          </td>
        </tr>
        <tr>
          <td align="right"><span class="tableheadtext">${uiLabelMap.PartyRoleType}</span></td>
          <td>
            <#if carrierShipmentMethod?has_content>
              <input type="hidden" name="roleTypeId" value="${carrierShipmentMethod.roleTypeId}">
              <div class="tabletext">${carrierShipmentMethod.roleTypeId}</div>
            <#else>
              <select class="selectBox" name="roleTypeId">
                <#list roleTypes as roleType>
                  <option value="${roleType.roleTypeId}" <#if roleType.roleTypeId == "CARRIER" && !carrierShipmentMethod?has_content>${uiLabelMap.ProductSelected}</#if>>${roleType.description?default(roleType.roleTypeId)}</option>
                </#list>
              </select> *
            </#if>
          </td>
        </tr>
        <tr>
          <td align="right"><span class="tableheadtext">${uiLabelMap.PartyParty}</span></td>
          <td>
            <#if carrierShipmentMethod?has_content>
              <input type="hidden" name="partyId" value="${carrierShipmentMethod.partyId}">
              <div class="tabletext">${carrierShipmentMethod.partyId}</div>
            <#else>
              <input type="text" class="inputBox" name="partyId" size="20" value="${carrierShipmentMethod.partyId?if_exists}"> *
            </#if>
          </td>
        </tr>
        <tr>
          <td align="right"><span class="tableheadtext">${uiLabelMap.ProductCarrierServiceCode}</span></td>
          <td><input type="text" class="inputBox" name="carrierServiceCode" size="20" value="${carrierShipmentMethod.carrierServiceCode?if_exists}"></td>
        </tr>
        <tr>
          <td align="right"><span class="tableheadtext">${uiLabelMap.ProductSequence} #</span></td>
          <td>
            <input type="text" class="inputBox" name="sequenceNumber" size="5" value="${carrierShipmentMethod.sequenceNumber?if_exists}">
            <span class="tabletext">${uiLabelMap.ProductUsedForDisplayOrdering}</span>
          </td>
        </tr>
        <tr>
          <td>
            <input type="submit" class="smallSubmit" value="${buttonText}">
          </td>
        </tr>
      </form>
    </table>
  </#if>
<#else>
  <h3>${uiLabelMap.ProductCatalogViewPermissionError}</h3>
</#if>
