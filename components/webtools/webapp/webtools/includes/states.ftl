
<#assign geoFindMap = Static["org.ofbiz.core.util.UtilMisc"].toMap("geoTypeId", "STATE")>
<#assign geoOrderList = Static["org.ofbiz.core.util.UtilMisc"].toList("geoName")>
<#assign states = delegator.findByAndCache("Geo", geoFindMap, geoOrderList)>
<#list states as state>
    <option value='${state.geoId}'>${state.geoName?default(state.geoId)}</option>
</#list>

