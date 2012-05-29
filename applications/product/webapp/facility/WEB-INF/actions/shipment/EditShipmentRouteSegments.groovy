/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.ofbiz.base.util.*
import org.ofbiz.entity.condition.EntityCondition;

shipmentId = request.getParameter("shipmentId");
if (!shipmentId) {
    shipmentId = context.shipmentId;
}

shipment = null;
if (shipmentId) {
    shipment = delegator.findOne("Shipment", [shipmentId : shipmentId], false);
}

if (shipment) {
    shipmentRouteSegments = shipment.getRelated("ShipmentRouteSegment", null, ['shipmentRouteSegmentId']);
    shipmentRouteSegmentDatas = [] as LinkedList;
    if (shipmentRouteSegments) {
        shipmentRouteSegments.each { shipmentRouteSegment ->
            shipmentRouteSegmentData = [:];
            shipmentRouteSegmentData.shipmentRouteSegment = shipmentRouteSegment;
            shipmentRouteSegmentData.originFacility = shipmentRouteSegment.getRelatedOne("OriginFacility", false);
            shipmentRouteSegmentData.destFacility = shipmentRouteSegment.getRelatedOne("DestFacility", false);
            shipmentRouteSegmentData.originPostalAddress = shipmentRouteSegment.getRelatedOne("OriginPostalAddress", false);
            shipmentRouteSegmentData.originTelecomNumber = shipmentRouteSegment.getRelatedOne("OriginTelecomNumber", false);
            shipmentRouteSegmentData.destPostalAddress = shipmentRouteSegment.getRelatedOne("DestPostalAddress", false);
            shipmentRouteSegmentData.destTelecomNumber = shipmentRouteSegment.getRelatedOne("DestTelecomNumber", false);
            shipmentRouteSegmentData.shipmentMethodType = shipmentRouteSegment.getRelatedOne("ShipmentMethodType", false);
            shipmentRouteSegmentData.carrierPerson = shipmentRouteSegment.getRelatedOne("CarrierPerson", false);
            shipmentRouteSegmentData.carrierPartyGroup = shipmentRouteSegment.getRelatedOne("CarrierPartyGroup", false);
            shipmentRouteSegmentData.shipmentPackageRouteSegs = shipmentRouteSegment.getRelated("ShipmentPackageRouteSeg");
            shipmentRouteSegmentData.carrierServiceStatusItem = shipmentRouteSegment.getRelatedOne("CarrierServiceStatusItem", false);
            shipmentRouteSegmentData.currencyUom = shipmentRouteSegment.getRelatedOne("CurrencyUom", false);
            shipmentRouteSegmentData.billingWeightUom = shipmentRouteSegment.getRelatedOne("BillingWeightUom", false);
            if (shipmentRouteSegment.carrierServiceStatusId) {
                shipmentRouteSegmentData.carrierServiceStatusValidChangeToDetails = delegator.findList("StatusValidChangeToDetail", EntityCondition.makeCondition([statusId : shipmentRouteSegment.carrierServiceStatusId]), null, ['sequenceId'], null, false);
            } else {
                shipmentRouteSegmentData.carrierServiceStatusValidChangeToDetails = delegator.findList("StatusValidChangeToDetail", EntityCondition.makeCondition([statusId : 'SHRSCS_NOT_STARTED']), null, ['sequenceId'], null, false);
            }
            shipmentRouteSegmentDatas.add(shipmentRouteSegmentData);
        }
    }

    shipmentPackages = shipment.getRelated("ShipmentPackage", null, ['shipmentPackageSeqId']);
    facilities = delegator.findList("Facility", null, null, ['facilityName'], null, false);
    shipmentMethodTypes = delegator.findList("ShipmentMethodType", null, null, ['description'], null, false);
    weightUoms = delegator.findList("Uom", EntityCondition.makeCondition([uomTypeId : 'WEIGHT_MEASURE']), null, null, null, false);
    currencyUoms = delegator.findList("Uom", EntityCondition.makeCondition([uomTypeId : 'CURRENCY_MEASURE']), null, null, null, false);

    carrierPartyRoles = delegator.findList("PartyRole", EntityCondition.makeCondition([roleTypeId : 'CARRIER']), null, null, null, false);
    carrierPartyDatas = [] as LinkedList;
    carrierPartyRoles.each { carrierPartyRole ->
        party = carrierPartyRole.getRelatedOne("Party", false);
        carrierPartyData = [:];
        carrierPartyData.party = party;
        carrierPartyData.person = party.getRelatedOne("Person", false);
        carrierPartyData.partyGroup = party.getRelatedOne("PartyGroup", false);
        carrierPartyDatas.add(carrierPartyData);
    }

    context.shipment = shipment;
    context.shipmentRouteSegmentDatas = shipmentRouteSegmentDatas;
    context.shipmentPackages = shipmentPackages;
    context.facilities = facilities;
    context.shipmentMethodTypes = shipmentMethodTypes;
    context.weightUoms = weightUoms;
    context.currencyUoms = currencyUoms;
    context.carrierPartyDatas = carrierPartyDatas;
}
context.shipmentId = shipmentId;
context.nowTimestampString = UtilDateTime.nowTimestamp().toString();
