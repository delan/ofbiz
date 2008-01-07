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
<#assign selected = tabButtonItem?default("void")>

<#if custRequest?exists>
    <div class="button-bar tab-bar">
        <ul>
            <li<#if selected="ViewRequest"> class="selected"</#if>><a href="<@ofbizUrl>ViewRequest?custRequestId=${custRequest.custRequestId}</@ofbizUrl>">${uiLabelMap.OrderViewRequest}</a></li>
            <li<#if selected="request"> class="selected"</#if>><a href="<@ofbizUrl>EditRequest?custRequestId=${custRequest.custRequestId}</@ofbizUrl>">${uiLabelMap.OrderRequest}</a></li>
            <li<#if selected="requestroles"> class="selected"</#if>><a href="<@ofbizUrl>requestroles?custRequestId=${custRequest.custRequestId}</@ofbizUrl>">${uiLabelMap.OrderRequestRoles}</a></li>
            <li<#if selected="requestitems"> class="selected"</#if>><a href="<@ofbizUrl>requestitems?custRequestId=${custRequest.custRequestId}</@ofbizUrl>">${uiLabelMap.OrderRequestItems}</a></li>
        </ul>
        <br/>
    </div>
    <#if custRequestItem?exists>
      <div class="tabContainer">
        <a href="<@ofbizUrl>EditRequestItem?custRequestId=${custRequest.custRequestId}&custRequestItemSeqId=${custRequestItem.custRequestItemSeqId}</@ofbizUrl>" class="${selectedClassMap.requestitem?default(unselectedClassName)}">${uiLabelMap.OrderRequestItem}</a>
        <a href="<@ofbizUrl>requestitemnotes?custRequestId=${custRequest.custRequestId}&custRequestItemSeqId=${custRequestItem.custRequestItemSeqId}</@ofbizUrl>" class="${selectedClassMap.requestitemnotes?default(unselectedClassName)}">${uiLabelMap.OrderNotes}</a>
        <#if custRequest.custRequestTypeId = "RF_QUOTE">
        <a href="<@ofbizUrl>RequestItemQuotes?custRequestId=${custRequest.custRequestId}&custRequestItemSeqId=${custRequestItem.custRequestItemSeqId}</@ofbizUrl>" class="${selectedClassMap.requestitemquotes?default(unselectedClassName)}">${uiLabelMap.OrderOrderQuotes}</a>
        </#if>
        <a href="<@ofbizUrl>requestitemrequirements?custRequestId=${custRequest.custRequestId}&custRequestItemSeqId=${custRequestItem.custRequestItemSeqId}</@ofbizUrl>" class="${selectedClassMap.requestitemrequirements?default(unselectedClassName)}">${uiLabelMap.OrderRequirements}</a>
        <a href="<@ofbizUrl>EditRequestItemWorkEfforts?custRequestId=${custRequest.custRequestId}&custRequestItemSeqId=${custRequestItem.custRequestItemSeqId}</@ofbizUrl>" class="${selectedClassMap.EditRequestItemWorkEfforts?default(unselectedClassName)}">${uiLabelMap.WorkEffortWorkEfforts}</a>
      </div>
    </#if>
</#if>
