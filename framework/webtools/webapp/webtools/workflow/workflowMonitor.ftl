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

<h1>${uiLabelMap.WebtoolsActiveWorkflowMonitor}</h1>
<br />
<#if security.hasPermission("WORKFLOW_MAINT", session)>

  <#-- list all running processes -->
  <#if !parameters.workflow?exists>
    <#if runningProcesses?exists>
      <p>${uiLabelMap.WebtoolsMessage23}</p>
      <table class="basic-table dark-grid" cellspacing="0">
        <tr class="header-row">
          <td>${uiLabelMap.WebtoolsWorkflowPackageVersion}</td>
          <td>${uiLabelMap.WebtoolsWorkflowProcessVersion}</td>
          <td>${uiLabelMap.WebtoolsWorkflowCurrentStatus}</td>
          <td>${uiLabelMap.WebtoolsPriority}</div></td>
          <td>${uiLabelMap.WebtoolsWorkflowActualStartDate}</div></td>
          <td>${uiLabelMap.WebtoolsWorkflowSourceReferenceId}</div></td>
          <td>&nbsp;</td>
        </tr>
        <#list runningProcesses as runningProcess>
          <tr>
            <td>${runningProcess.workflowPackageId} / ${runningProcess.workflowPackageVersion}</td>
            <td>${runningProcess.workflowProcessId} / ${runningProcess.workflowProcessVersion}</td>
            <td>${WfUtil.getOMGStatus(runningProcess.getString("currentStatusId"))}</td>
            <td>${runningProcess.priority?default("&nbsp;")}</td>
            <td>${runningProcess.actualStartDate?default("N/A")}</td>
            <td>${runningProcess.sourceReferenceId?default("&nbsp;")}</td>
            <td class="button-col"><a href="<@ofbizUrl>workflowMonitor?workflow=${runningProcess.workEffortId?if_exists}</@ofbizUrl>">${uiLabelMap.CommonView}</a></td>
          </tr>
        </#list>
      </table>
    <else>
      <h3>${uiLabelMap.WebtoolsNoRunningProcesses}</h3>
    </#if>
  <#else>
    <#-- list all steps in the process -->
    <#if activities?exists>
      <div class="button-bar"><a href="<@ofbizUrl>workflowMonitor</@ofbizUrl>">${uiLabelMap.WebtoolsWorkflows}</a></div>
      <br />
      <div class="screenlet">
        <div class="screenlet-title-bar">
          <h3>${uiLabelMap.WebtoolsWorkflowActivityListFor}: ${workflowDefworkflowPackageId} / ${workflowDef.workflowProcessId}</h3> 
        </div>
        <table class="basic-table dark-grid" cellspacing="0">
          <tr class="header-row">
            <td>${uiLabelMap.WebtoolsWorkflowActivityId}</td>
            <td>${uiLabelMap.Priority}</td>
            <td>${uiLabelMap.WebtoolsWorkflowCurrentStatus}</td>
            <td>${uiLabelMap.WebtoolsWorkflowActualStartDate}</td>
            <td>${uiLabelMap.WebtoolsWorkflowActualCompleteDate}</td>
            <td>${uiLabelMap.WebtoolsWorkflowAssignments}</td>
          </tr>
          <#list activities as step>
            <#assign assignments = step.getRelated("WorkEffortPartyAssignment")>
			<#assign assignments = EntityUtil.filterByDate(assignments)>
            <tr>
              <#-- TODO: add external login ID to external links -->
              <td class="button-col"><a href="/workeffort/control/activity?workEffortId=${step.workEffortId}" target="workeffort">${step.workflowActivityId}</a></td>
              <td>${step.priority?default("&nbsp;")}</td>
              <td>${WfUtil.getOMGStatus(step.getString("currentStatusId"))}</td>
              <td>${step.actualStartDate?default("N/A")}</td>
              <td>${step.actualCompletionDate?default("N/A")}</td>
              <td class="button-col">
                <#if assignments?has_content>
                  <#list assignments as assignment>
                    <a href="/partymgr/control/viewprofile?party_id=${assignment.partyId?if_exists}" target="partymgr" >${assignment.partyId}</a>
                  </#list>
                <#else>
                  N/A
                </#if>
              </td>                                                                    
            </tr>
          </#list>
        </table>
      </div>
    <#else>
      <h3>${uiLabelMap.WebtoolsMessage24}: ${workflow}</h3>
    </#if>
  </#if>
<#else>
  <h3>${uiLabelMap.WebtoolsPermissionWorkflow}</h3>
</#if>
