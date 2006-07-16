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

<div class="screenlet">
    <div class="screenlet-header">
        <div class="boxhead">${uiLabelMap.RequestHistory}</div>
    </div>
    <div class="screenlet-body">
        <table width="100%" cellpadding="1" cellspacing="0" border="0">
            <tr>
                <td width="10%">
                    <div class="tabletext"><b><nobr>${uiLabelMap.OrderRequest} ${uiLabelMap.OrderNbr}</nobr></b></div>
                </td>
                <td width="10">&nbsp;</td>
                <td width="10%">
                    <div class="tabletext"><b><nobr>${uiLabelMap.CommonType}</nobr></b></div>
                </td>
                <td width="10">&nbsp;</td>
                <td width="20%">
                    <div class="tabletext"><b>${uiLabelMap.CommonName}</b></div>
                </td>
                <td width="10">&nbsp;</td>
                <td width="40%">
                    <div class="tabletext"><b>${uiLabelMap.CommonDescription}</b></div>
                </td>
                <td width="10">&nbsp;</td>
                <td width="10%">
                    <div class="tabletext"><b>${uiLabelMap.CommonStatus}</b></div>
                </td>
                <td width="10">&nbsp;</td>
                <td width="20%">
                    <div class="tabletext"><b>${uiLabelMap.OrderRequestDate}</b></div>
                    <div class="tabletext"><b>${uiLabelMap.OrderRequestCreatedDate}</b></div>
                    <div class="tabletext"><b>${uiLabelMap.OrderRequestLastModifiedDate}</b></div>
                </td>
                <td width="10">&nbsp;</td>
                <td width="10">&nbsp;</td>
            </tr>
            <#list requestList as custRequest>
                <#assign status = custRequest.getRelatedOneCache("StatusItem")>
                <#assign type = custRequest.getRelatedOneCache("CustRequestType")>
                <tr><td colspan="14"><hr class="sepbar"/></td></tr>
                <tr>
                    <td>
                        <div class="tabletext">${custRequest.custRequestId}</div>
                    </td>
                    <td width="10">&nbsp;</td>
                    <td>
                        <div class="tabletext">${type.get("description",locale)?if_exists}</div>
                    </td>
                    <td width="10">&nbsp;</td>
                    <td>
                        <div class="tabletext">${custRequest.custRequestName?if_exists}</div>
                    </td>
                    <td width="10">&nbsp;</td>
                    <td>
                        <div class="tabletext">${custRequest.description?if_exists}</div>
                    </td>
                    <td width="10">&nbsp;</td>
                    <td>
                        <div class="tabletext">${status.get("description",locale)}</div>
                    </td>
                    <td width="10">&nbsp;</td>
                    <td>
                        <div class="tabletext"><nobr>${custRequest.custRequestDate?if_exists}</nobr></div>
                        <div class="tabletext"><nobr>${custRequest.createdDate?if_exists}</nobr></div>
                        <div class="tabletext"><nobr>${custRequest.lastModifiedDate?if_exists}</nobr></div>
                    </td>
                    <td width="10">&nbsp;</td>
                    <td align="right">
                        <a href="<@ofbizUrl>/ViewRequest?custRequestId=${custRequest.custRequestId}</@ofbizUrl>" class="buttontext">${uiLabelMap.CommonView}</a>
                    </td>
                    <td width="10">&nbsp;</td>
                </tr>
            </#list>
            <#if !requestList?has_content>
                <tr><td colspan="9"><div class="head3">${uiLabelMap.OrderNoRequestFound}</div></td></tr>
            </#if>
        </table>
    </div>
</div>
