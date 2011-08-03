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
<html xmlns="http://www.w3.org/1999/xhtml">
  <body>
    <p class="tabletext">Hello ${partyName.firstName?if_exists} ${partyName.lastName?if_exists} ${partyName.groupName?if_exists}!</p>
    <p class="tabletext">Successfully unsubscribed from ${contactList.contactListName} contact list.</p>

    <#--assign verifyUrl = baseEcommerceSecureUrl +'updateContactListPartyNoUserLogin?contactListId='+contactListParty.contactListId+'&amp;partyId='+contactListParty.partyId+'&amp;fromDate='+contactListParty.fromDate+'&amp;statusId=CLPT_SUBS_PENDING&amp;optInVerifyCode='+contactListPartyStatus.optInVerifyCode+'&amp;baseLocation='+baseLocation?if_exists>
    <#if (contactListParty.preferredContactMechId)?exists>
        <#assign verifyUrl= verifyUrl+"&amp;preferredContactMechId="+contactListParty.preferredContactMechId>
    </#if>
    <a href="${verifyUrl}">If this was by mistake, click here subscribe again.</a-->



  </body>
</html>
