<#-- *  Copyright (c) 2003 The Open For Business Project - www.ofbiz.org * *  Permission is hereby granted, free of charge, to any person obtaining a  *  copy of this software and associated documentation files (the "Software"),  *  to deal in the Software without restriction, including without limitation  *  the rights to use, copy, modify, merge, publish, distribute, sublicense,  *  and/or sell copies of the Software, and to permit persons to whom the  *  Software is furnished to do so, subject to the following conditions: * *  The above copyright notice and this permission notice shall be included  *  in all copies or substantial portions of the Software. * *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS  *  OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF  *  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  *  IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY  *  CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT  *  OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR  *  THE USE OR OTHER DEALINGS IN THE SOFTWARE. * *@author     Jacopo Cappellato (tiz@sastau.it) *@author     Olivier.Heintz@nereide.biz *@version    $Rev:$ *@since      3.0--><#assign uiLabelMap = requestAttributes.uiLabelMap><#assign locale = requestAttributes.locale><script language="JavaScript"><!-- //function lookupRoutingTask() {    document.lookupRouTask.submit();}// --></script>${pages.get("/routing/RoutingTabBar.ftl")}<#if security.hasEntityPermission("MANUFACTURING", "_CREATE", session)><div><a href="<@ofbizUrl>/FindRoutingTask?addRecord=Y&hideFields=Y${paramList}</@ofbizUrl>" class="buttontext">[${uiLabelMap.ManufacturingNewRoutingTask}]</a></div><#--   Add RoutingTask  --><#if requestParameters.addRecord?default("N") == "Y">   <#assign formSuite ="Y">   <#assign buttonEnr = uiLabelMap.CommonAdd>		   <form name="routingTaskform" method="post" action="<@ofbizUrl>/CreateRoutingTask?hideFields=Y${paramList}</@ofbizUrl>">    	<input type="hidden" name="workEffortTypeId" value="ROU_TASK">    	<input type="hidden" name="currentStatusId" value="ROU_ACTIVE"><#elseif routingTask?has_content && ! (requestParameters.fromEdit?default("N") == "Y")>   <#assign formSuite ="Y">		    <#assign buttonEnr = uiLabelMap.CommonUpdate>		   <form name="routingTaskform" method="post" action="<@ofbizUrl>/UpdateRoutingTask?hideFields=Y${paramList}</@ofbizUrl>">    	<input type="hidden" name="workEffortId" value="${routingTask.workEffortId}"></#if><#if formSuite?default("N") == "Y">     	<input type="hidden" name="VIEW_SIZE" value="${viewSize}">     	<input type="hidden" name="VIEW_INDEX" value="${viewIndex}">     	<input type="hidden" name="fromEdit" value="Y">  <br>  <table width="90%" border="0" cellpadding="2" cellspacing="0">    <tr>      <td width='26%' align='right' valign='top'><div class="tabletext">${uiLabelMap.ManufacturingTaskName}</div></td>      <td width="5">&nbsp;</td>      <td width="74%"><input type="text" class="inputBox" size="30" name="workEffortName" value="${routingTask.workEffortName?if_exists}"></td>    </tr>    <tr>      <td width='26%' align='right' valign='top'><div class="tabletext">${uiLabelMap.ManufacturingTaskPurpose}</div></td>      <td width="5">&nbsp;</td>      <td width="74%">         <select class="selectBox" name="workEffortPurposeTypeId">          <#list allTaskPurposeTypes as allTaskPurposeType>          <option value="${allTaskPurposeType.workEffortPurposeTypeId}" <#if routingTask?has_content && routingTask.workEffortPurposeTypeId?default("") == allTaskPurposeType.workEffortPurposeTypeId>SELECTED</#if>>${(allTaskPurposeType.get("description", locale))?if_exists}</option>          </#list>        </select>    </tr>    <tr>      <td width='26%' align='right' valign='top'><div class="tabletext">${uiLabelMap.CommonDescription}</div></td>      <td width="5">&nbsp;</td>      <td width="74%"><input type="text" class="inputBox" size="40" name="description" value="${routingTask.description?if_exists}"></td>    </tr>    <tr>      <td width='26%' align='right' valign='top'><div class="tabletext">${uiLabelMap.ManufacturingMachineGroup}</div></td>      <td width="5">&nbsp;</td>      <td width="74%">         <select class="selectBox" name="fixedAssetId">		  <option></option>          <#list machineGroups as machineGroup>          <option value="${machineGroup.fixedAssetId}" <#if routingTask?has_content && routingTask.fixedAssetId?default("") == machineGroup.fixedAssetId>SELECTED</#if>>${(machineGroup.get("fixedAssetName", locale))?if_exists}</option>          </#list>        </select>    </tr>    <tr>      <td width='26%' align='right' valign='top'><div class="tabletext">${uiLabelMap.ManufacturingTaskEstimatedSetupMillis}</div></td>      <td width="5">&nbsp;</td>      <td width="74%"><input type="text" class="inputBox" size="10" name="estimatedSetupMillis" value="${routingTask.estimatedSetupMillis?default(0)}"></td>    </tr>    <tr>      <td width='26%' align='right' valign='top'><div class="tabletext">${uiLabelMap.ManufacturingTaskEstimatedMilliSeconds}</div></td>      <td width="5">&nbsp;</td>      <td width="74%"><input type="text" class="inputBox" size="10" name="estimatedMilliSeconds" value="${routingTask.estimatedMilliSeconds?default(0)}"></td>    </tr>    <tr>      <td width="26%" align="right" valign="top">      <td width="5">&nbsp;</td>      <td width="74%"><input type="submit" value="${buttonEnr}" class="smallSubmit"></td>    </tr>  </table></form></#if> <#--  formSuite?default("N") == "Y" --></#if> <#-- security.hasEntityPermission("MANUFACTURING", "_CREATE", session) --><#--   End of Add RoutingTask  --><#if security.hasEntityPermission("MANUFACTURING", "_VIEW", session)><form method="post" name="lookupRouTask" action="<@ofbizUrl>/FindRoutingTask</@ofbizUrl>"><input type="hidden" name="lookupFlag" value="Y"><input type="hidden" name="hideFields" value="Y"><table border=0 width="100%" cellspacing="0" cellpadding="0" class="boxoutside">  <tr>    <td width="100%">      <table width="100%" border="0" cellspacing="0" cellpadding="0" class="boxtop">        <tr>          <td><div class="boxhead">${uiLabelMap.ManufacturingFindRoutingTasks}</div></td>          <td align="right">            <div class="tabletext">              <#if requestParameters.hideFields?default("N") == "Y">                <a href="<@ofbizUrl>/FindRoutingTask?hideFields=N${paramList}</@ofbizUrl>" class="submenutextright">${uiLabelMap.CommonShowLookupFields}</a>              <#else>                <#if partList?exists>                    <a href="<@ofbizUrl>/FindRoutingTask?hideFields=Y${paramList}</@ofbizUrl>" class="submenutext">${uiLabelMap.CommonHideFields}</a>                </#if>                <a href="javascript:lookupRoutingTask();" class="submenutextright">${uiLabelMap.CommonLookup}</a>                              </#if>            </div>          </td>        </tr>      </table>      <#if requestParameters.hideFields?default("N") != "Y">      <table width="100%" border="0" cellspacing="0" cellpadding="2" class="boxbottom">        <tr>          <td align="center" width="100%">            <table border="0" cellspacing="0" cellpadding="2">              <tr>                <td width="25%" align="right"><div class="tableheadtext">${uiLabelMap.ManufacturingTaskName}:</div></td>                <td width="5%">&nbsp;</td>                <td>                    <input type="text" size="25" class="inputBox" name="taskName" value="${requestParameters.taskName?if_exists}">                </td>              </tr>              <tr>                <td width="25%" align="right"><div class="tableheadtext">${uiLabelMap.ManufacturingMachineGroup}:</div></td>                <td width="5%">&nbsp;</td>                <td>                  <select name="machineGroup" class="selectBox">                     <option value="ANY">${uiLabelMap.ManufacturingAnyMachineGroup}</option>                                       <#list machineGroups as oneMachineGroup>                      <option value="${oneMachineGroup.fixedAssetId}">${oneMachineGroup.fixedAssetName}</option>                    </#list>                  </select>                </td>              </tr>                             <tr>                  <td width="26%" align="center" valign="top">                  <td width="5">&nbsp;</td>                  <td width="74%"><input type="submit" value="${uiLabelMap.CommonFind}" class="smallSubmit"></td>              </tr>            </table>          </td>        </tr>      </table>      </#if>    </td>  </tr></table></form> <#if requestParameters.hideFields?default("N") != "Y"><script language="JavaScript"> <#-- A tester --><!--//document.lookupRouTask.machineGroup.focus();//--></script></#if><br><#if routingTaskList?exists><table border=0 width="100%" cellspacing="0" cellpadding="0" class="boxoutside">  <tr>    <td width="100%">      <table width="100%" border="0" cellspacing="0" cellpadding="0" class="boxtop">        <tr>          <td width="50%"><div class="boxhead">${uiLabelMap.CommonElementsFound}</div></td>          <td width="50%">            <div class="boxhead" align=right>              <#if 0 < routingTaskList?size>                             <#if 0 < viewIndex>                  <a href="<@ofbizUrl>/FindRoutingTask?VIEW_SIZE=${viewSize}&VIEW_INDEX=${viewIndex-1}&hideFields=${requestParameters.hideFields?default("N")}${paramList}</@ofbizUrl>" class="submenutext">${uiLabelMap.CommonPrevious}</a>                <#else>                  <span class="submenutextdisabled">${uiLabelMap.CommonPrevious}</span>                </#if>                <#if 0 < listSize>                  <span class="submenutextinfo">${lowIndex+1} - ${highIndex} ${uiLabelMap.CommonOf} ${listSize}</span>                </#if>                <#if highIndex < listSize>                  <a href="<@ofbizUrl>/FindRoutingTask?VIEW_SIZE=${viewSize}&VIEW_INDEX=${viewIndex+1}&hideFields=${requestParameters.hideFields?default("N")}${paramList}</@ofbizUrl>" class="submenutextright">${uiLabelMap.CommonNext}</a>                <#else>                  <span class="submenutextrightdisabled">${uiLabelMap.CommonNext}</span>                </#if>              </#if>              &nbsp;            </div>          </td>        </tr>      </table>      <table width="100%" border="0" cellspacing="0" cellpadding="2" class="boxbottom">        <tr>          <td width="10%" align="left"><div class="tableheadtext">${uiLabelMap.ManufacturingTaskId}</div></td>          <td width="10%" align="left"><div class="tableheadtext">${uiLabelMap.ManufacturingTaskName}</div></td>          <td width="30%" align="left"><div class="tableheadtext">${uiLabelMap.CommonDescription}</div></td>          <td width="10%" align="left"><div class="tableheadtext">${uiLabelMap.ManufacturingTaskPurpose}</div></td>          <td width="20%" align="left"><div class="tableheadtext">${uiLabelMap.ManufacturingMachineGroup}</div></td>          <td width="10%" align="left"><div class="tableheadtext">${uiLabelMap.ManufacturingTaskEstimatedSetupMillis}</div></td>          <td width="10%" align="left"><div class="tableheadtext">${uiLabelMap.ManufacturingTaskEstimatedMilliSeconds}</div></td>        </tr>        <tr>          <td colspan="7"><hr class="sepbar"></td>        </tr>        <#if routingTaskList?has_content>          <#assign rowClass = "viewManyTR2">          <#list routingTaskList[lowIndex..highIndex-1] as routingTask>          <#if routingTask.fixedAssetId?exists>           			<#assign machineGroup = routingTask.getRelatedOneCache("FixedAsset")> 		  <#else>		  			<#assign machineGroup = {"",""}>          </#if>            <#assign taskPurpose = routingTask.getRelatedOneCache("WorkEffortPurposeType")>            <tr class="${rowClass}">            <div class="tabletext">              <td>${routingTask.workEffortId}</td>              <td>                  <a href="<@ofbizUrl>/FindRoutingTask?workEffortId=${routingTask.workEffortId}&VIEW_SIZE=${viewSize}&VIEW_INDEX=${viewIndex}&hideFields=Y${paramList}</@ofbizUrl>" class="buttontext">${routingTask.workEffortName}</a>              </td>              <td>${routingTask.get("description",locale)?default("&nbsp;")}</td>              <td>${taskPurpose.get("description",locale)?default("&nbsp;")}</td>              <td>${machineGroup.fixedAssetName?default(machineGroup.fixedAssetId?default("&nbsp;"))}</td>              <td>${routingTask.estimatedSetupMillis?default("&nbsp;")}</td>              <td>${routingTask.estimatedMilliSeconds?default("&nbsp;")}</td>            </div>            </tr>            <#-- toggle the row color -->            <#if rowClass == "viewManyTR2">              <#assign rowClass = "viewManyTR1">            <#else>              <#assign rowClass = "viewManyTR2">            </#if>          </#list>                  <#else>          <tr>            <td colspan="7"><div class="head3">${uiLabelMap.CommonNoElementFound}.</div></td>          </tr>                </#if>        <#if lookupErrorMessage?exists>          <tr>            <td colspan="7"><div class="head3">${lookupErrorMessage}</div></td>          </tr>        </#if>      </table>    </td>  </tr></table></#if>  <#-- security.hasEntityPermission("MANUFACTURING", "_VIEW", session) --><#else>  <h3>${uiLabelMap.ManufacturingViewPermissionError}</h3></#if>