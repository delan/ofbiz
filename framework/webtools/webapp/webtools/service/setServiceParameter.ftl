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

<form name="scheduleForm" method="POST" action="<@ofbizUrl>scheduleService</@ofbizUrl>">
    <#list scheduleOptions as scheduleOption>
	<input type="hidden" name="${scheduleOption.name}" value="${scheduleOption.value}"/>
    </#list>

    <table border="0">
        
    <#list serviceParameters as serviceParameter>
      <tr>
        <td align="right"><div class="tabletext">${serviceParameter.name} (${serviceParameter.type})</div></td>
        <td>
          <input type="text" class="inputBox" size="20" name="${serviceParameter.name}"/>
          <span class="tabletext"><#if serviceParameter.optional == "N">(required)<#else>(optional)</#if></span>
        </td>
      </tr>
    </#list>
      <tr>
        <td colspan="2" align="center"><input type="submit" class="smallSubmit" value="Submit"></td>
      </tr>      
    </table>
</form>	
