<#--
$Id: $

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

<#-- TODO: probably some kind of permission checking to see that this userLogin can view such and such reports -->

<div class="tabletext">

<script language="javascript">
function submitReconcile(form) {
  form.action="<@ofbizUrl>ReconcileGlAccountOrganization</@ofbizUrl>";
  form.submit();
}

function submitViewBalance(form) {
  form.action="<@ofbizUrl>viewGlAccountBalance</@ofbizUrl>";
  form.submit();
}
</script>

<#if closedTimePeriods?has_content>
<p>The following time periods have been closed:
<ul type="circle">
<#list closedTimePeriods as timePeriod>
<li>${timePeriod.periodName?if_exists} ${timePeriod.periodNum?string("####")} (${timePeriod.fromDate} - ${timePeriod.thruDate})
</#list>
</ul></p>
<p>
<#else>
<p>There are currently no closed time periods.</p>
</#if>

<#if (openTimePeriods?has_content) && (openTimePeriods.size() > 0)>
<#assign timePeriod = openTimePeriods.get(0)>
<a href="<@ofbizUrl>closeAllTimePeriods?organizationPartyId=${organizationPartyId}&customTimePeriodId=${timePeriod.customTimePeriodId}</@ofbizUrl>">
Close time periods ending ${timePeriod.thruDate}
</a>
</p>
<#else>
<p>There are no time periods which can be closed.</p>
</#if>
</div>
