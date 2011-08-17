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

import java.lang.*;
import java.util.*;
import org.ofbiz.base.util.*;
import org.ofbiz.entity.*;
import org.ofbiz.entity.util.*;
import org.ofbiz.entity.condition.*;
import org.ofbiz.party.contact.ContactMechWorker;
import org.ofbiz.product.store.ProductStoreWorker;
import org.ofbiz.webapp.website.WebSiteWorker;
import org.ofbiz.accounting.payment.PaymentWorker;

/*publicEmailContactLists = delegator.findByAnd("ContactList", [isPublic : "Y", contactMechTypeId : "EMAIL_ADDRESS"], ["contactListName"]);
context.publicEmailContactLists = publicEmailContactLists;*/

webSiteId = WebSiteWorker.getWebSiteId(request);
webSiteContactList = delegator.findByAnd("WebSiteContactList", [webSiteId: webSiteId]);
publicEmailContactLists = [];
webSiteContactList.each { webSiteContactList ->
    temp = webSiteContactList.getRelatedOne("ContactList");
    publicEmailContactLists.add(temp);
}
context.publicEmailContactLists = publicEmailContactLists;

if (userLogin) {
    partyAndContactMechList = delegator.findByAnd("PartyAndContactMech", [partyId : partyId, contactMechTypeId : "EMAIL_ADDRESS"], ["-fromDate"]);
    partyAndContactMechList = EntityUtil.filterByDate(partyAndContactMechList);
    context.partyAndContactMechList = partyAndContactMechList;
}


