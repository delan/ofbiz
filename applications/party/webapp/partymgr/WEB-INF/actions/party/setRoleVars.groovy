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
 import org.ofbiz.entity.*;
 import org.ofbiz.entity.util.EntityUtil;
 import org.ofbiz.base.util.*;
 
 roleTypeAndParty = delegator.findByAnd("RoleTypeAndParty", ['partyId': parameters.partyId, 'roleTypeId': 'ACCOUNT']); 
 if (UtilValidate.isNotEmpty(roleTypeAndParty)) {
	 context.put("accountDescription", roleTypeAndParty.get(0).getString("description"));
 }
 roleTypeAndParty = delegator.findByAnd("RoleTypeAndParty", ['partyId': parameters.partyId, 'roleTypeId': 'CONTACT']); 
 if (UtilValidate.isNotEmpty(roleTypeAndParty)) {
	 context.put("contactDescription", roleTypeAndParty.get(0).getString("description"));
 }
 roleTypeAndParty = delegator.findByAnd("RoleTypeAndParty", ['partyId': parameters.partyId, 'roleTypeId': 'LEAD']); 
 if (UtilValidate.isNotEmpty(roleTypeAndParty)) {
	 context.put("leadDescription", roleTypeAndParty.get(0).getString("description"));
	 partyRelationships = EntityUtil.filterByDate(delegator.findByAnd("PartyRelationship", ["partyIdTo": parameters.partyId, "roleTypeIdFrom": "ACCOUNT_LEAD", "roleTypeIdTo": "LEAD", "partyRelationshipTypeId": "LEAD_REL"]));
	 context.put("partyGroupId", partyRelationships.get(0).partyIdFrom); 
     context.put("partyId", parameters.partyId); 
 }
 roleTypeAndParty = delegator.findByAnd("RoleTypeAndParty", ['partyId': parameters.partyId, 'roleTypeId': 'ACCOUNT_LEAD']); 
 if (UtilValidate.isNotEmpty(roleTypeAndParty)) {
	 context.put("leadDescription", roleTypeAndParty.get(0).getString("description"));
	 partyRelationships = EntityUtil.filterByDate(delegator.findByAnd("PartyRelationship", ["partyIdFrom": parameters.partyId, "roleTypeIdFrom": "ACCOUNT_LEAD", "roleTypeIdTo": "LEAD", "partyRelationshipTypeId": "LEAD_REL"]));
	 context.put("partyGroupId", parameters.partyId); 
     context.put("partyId", partyRelationships.get(0).partyIdTo); 
 }

