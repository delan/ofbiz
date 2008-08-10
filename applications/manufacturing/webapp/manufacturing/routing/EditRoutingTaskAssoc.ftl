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
<!-- //
function copyAndAddRoutingTask() {
    document.addtaskassocform.copyTask.value = "Y";
    document.addtaskassocform.submit();
}
function addRoutingTask() {
    document.addtaskassocform.copyTask.value = "N";
    document.addtaskassocform.submit();
}
// -->
</script>

<#if security.hasEntityPermission("MANUFACTURING", "_CREATE", session)>
<form method="post" action="<@ofbizUrl>AddRoutingTaskAssoc</@ofbizUrl>" name="addtaskassocform">
    <input type="hidden" name="workEffortId" value="${workEffortId}"/>
    <input type="hidden" name="workEffortIdFrom" value="${workEffortId}"/>
    <input type="hidden" name="workEffortAssocTypeId" value="ROUTING_COMPONENT"/>
    <input type="hidden" name="copyTask" value="N"/>
    <table class="basic-table" cellspacing="0">
        <tr>
            <th align="right">
                ${uiLabelMap.ManufacturingRoutingTaskId}
            </th>
            <td>
                <input type="text" name="workEffortIdTo" size="20"/>
                <a href="javascript:call_fieldlookup(document.addtaskassocform.workEffortIdTo,'<@ofbizUrl>LookupRoutingTask</@ofbizUrl>', 'vide',540,450);"><img src='/images/fieldlookup.gif' width='15' height='14' border='0' alt='Click here For Field Lookup'></a>
            </td>
            <th align="right">
                ${uiLabelMap.CommonFromDate}
            </th>
            <td>
                <input type="text" name="fromDate" size="25"/>
                <a href="javascript:call_cal(document.addtaskassocform.fromDate, null);"><img src="<@ofbizContentUrl>/images/cal.gif</@ofbizContentUrl>" width="16" height="16" border="0" alt="Click here For Calendar"></a>
            </td>
            <td align="center" width="40%">&nbsp;</td>
        </tr>
        <tr>
            <th align="right">
                ${uiLabelMap.CommonSequenceNum}
            </th>
            <td>
                <input type="text" name="sequenceNum" size="10"/>
            </td>
            <th align="right">
                ${uiLabelMap.CommonThruDate}
            </th>
            <td>
                <input type="text" name="thruDate" size="25"/>
                <a href="javascript:call_cal(document.addtaskassocform.thruDate, null);"><img src="<@ofbizContentUrl>/images/cal.gif</@ofbizContentUrl>" width="16" height="16" border="0" alt="Click here For Calendar"></a>
            </td>
            <td>&nbsp;</td>
        </tr>

        <tr>
            <td >&nbsp;</td>
            <td colspan="3">
                <a href="javascript:addRoutingTask();" class="buttontext">${uiLabelMap.ManufacturingAddExistingRoutingTask}</a>
                &nbsp;-&nbsp;
                <a href="javascript:copyAndAddRoutingTask();" class="buttontext">${uiLabelMap.ManufacturingCopyAndAddRoutingTask}</a>
            </td>
            <td>&nbsp;</td>
        </tr>
    </table>
</form>
</#if>