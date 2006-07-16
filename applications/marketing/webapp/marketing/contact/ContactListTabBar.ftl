<#--

Copyright 2001-2006 The Apache Software Foundation

Licensed under the Apache License, Version 2.0 (the "License"); you may not
use this file except in compliance with the License. You may obtain a copy of
the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
License for the specific language governing permissions and limitations
under the License.
-->
<#if (requestAttributes.uiLabelMap)?exists><#assign uiLabelMap = requestAttributes.uiLabelMap></#if>

<#assign unselectedClassName = "tabButton">
<#assign selectedClassMap = {page.tabButtonItem?default("void") : "tabButtonSelected"}>

<#if security.hasEntityPermission("MARKETING", "_VIEW", session)>
<#if segmentGroup?has_content>
<#-- Main Heading -->
<table width="100%" cellpadding="0" cellspacing="0" border="0">
  <tr>
    <td align="right">
      <div class="tabContainer">
        <a href="<@ofbizUrl>EditContactList?contactListId=${contactListId}</@ofbizUrl>" class="${selectedClassMap.EditContactList?default(unselectedClassName)}">${uiLabelMap.ContactList}</a>
        <a href="<@ofbizUrl>ListContactListParty?contactListId=${contactListId}</@ofbizUrl>" class="${selectedClassMap.ContactListParty?default(unselectedClassName)}">${uiLabelMap.ContactListParty}</a>
        <a href="<@ofbizUrl>ListContactListCommEvent?contactListId=${contactListId}</@ofbizUrl>" class="${selectedClassMap.ContactListCommEvent?default(unselectedClassName)}">${uiLabelMap.ContactListCommEvent}</a>
      </div>
    </td>
  </tr>
 </table>

<#else>
  <div class="head2">${uiLabelMap.SegmentGroupNoSegmentGroupFoundWithId}: ${segmentGroupIdId?if_exists}</div>
</#if>
<#else>
  <div class="head2">${uiLabelMap.MarketingViewPermissionError}</div>
</#if>
