<#--
 *  Copyright (c) 2003 The Open For Business Project - www.ofbiz.org
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a 
 *  copy of this software and associated documentation files (the "Software"), 
 *  to deal in the Software without restriction, including without limitation 
 *  the rights to use, copy, modify, merge, publish, distribute, sublicense, 
 *  and/or sell copies of the Software, and to permit persons to whom the 
 *  Software is furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included 
 *  in all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS 
 *  OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
 *  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
 *  IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY 
 *  CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT 
 *  OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR 
 *  THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 *@author     David E. Jones
 *@author     Brad Steiner
 *@version    $Revision$
 *@since      2.2
-->

<#if hasPermission>

${pages.get("/group/FacilityGroupTabBar.ftl")}
    
    <div class="head1">Facility Group<span class="head2">&nbsp;<#if facilityGroup?exists>${(facilityGroup.facilityGroupName)?if_exists}</#if> [ID:${facilityGroupId?if_exists}]</span></div>
    <a href="<@ofbizUrl>/EditFacilityGroup</@ofbizUrl>" class="buttontext">[New Group]</a>
    
    <#if !(facilityGroup?exists)>
        <#if facilityGroupId?exists>
            <form action="<@ofbizUrl>/CreateFacilityGroup</@ofbizUrl>" method=POST style="margin: 0;">
            <table border="0" cellpadding="2" cellspacing="0">
            <h3>Could not find facility with ID "${facilityGroupId}".</h3>
        <#else>
            <form action="<@ofbizUrl>/CreateFacilityGroup</@ofbizUrl>" method=POST style="margin: 0;">
            <table border="0" cellpadding="2" cellspacing="0">
        </#if>
    <#else>
        <form action="<@ofbizUrl>/UpdateFacilityGroup</@ofbizUrl>" method=POST style="margin: 0;">
        <table border="0" cellpadding="2" cellspacing="0">
        <input type=hidden name="facilityGroupId" value="${facilityGroupId?if_exists}">
        <tr>
            <td align=right><div class="tabletext">Facility Group ID</div></td>
            <td>&nbsp;</td>
            <td>
            <b>${facilityGroupId?if_exists}</b> (This cannot be changed without re-creating the facility group.)
            </td>
        </tr>
    </#if>
    <tr>
        <td width="26%" align=right><div class="tabletext">Facility Group Type</div></td>
        <td>&nbsp;</td>
        <td width="74%">
        <select name="facilityGroupTypeId" size=1 class="selectBox">
            <option selected value="${(facilityGroupType.facilityGroupTypeId)?if_exists}">${(facilityGroupType.description)?if_exists}</option>
            <#list facilityGroupTypes as nextFacilityGroupType>
            <option value="${(nextFacilityGroupType.facilityGroupTypeId)?if_exists}">${(nextFacilityGroupType.description)?if_exists}</option>
            </#list>
        </select>
        </td>
    </tr>

    <tr>
        <td width="26%" align=right><div class="tabletext">Primary Parent Group</div></td>
        <td>&nbsp;</td>
        <td width="74%">
        <select name="primaryParentGroupId" size=1 class="selectBox">
            <#if facilityGroup?exists> 
                <#if (facilityGroup.getRelatedOne("PrimaryParentFacilityGroup"))?exists>
                    <#assign currentPrimaryParent = facilityGroup.getRelatedOne("PrimaryParentFacilityGroup")>
                    <option selected value="${(facilityGroup.primaryParentGroupId)?if_exists}">${(currentPrimaryParent.description)?if_exists}</option>
                </#if>
            </#if> 
            <option value="${(facilityGroup.primaryParentGroupId)?if_exists}"></option>            
            <#list facilityGroups as nextFacilityGroup>
            <#if !(nextFacilityGroup.facilityGroupId.equals("_NA_"))>
                <option value="${(nextFacilityGroup.facilityGroupId)?if_exists}">${(nextFacilityGroup.facilityGroupName)?if_exists}</option>
            </#if>
            </#list>
        </select>
        </td>
    </tr>

    <tr>
        <td width="26%" align=right><div class="tabletext">Name</div></td>
        <td>&nbsp;</td>
        <td width="74%"><input type="text" class="inputBox" name="facilityGroupName" value="${(facilityGroup.facilityGroupName)?if_exists}" size="30" maxlength="60"></td>
    </tr>    
    <tr>
        <td width="26%" align=right><div class="tabletext">Description</div></td>
        <td>&nbsp;</td>
        <td width="74%"><input type="text" class="inputBox" name="description" value="${(facilityGroup.description)?if_exists}" size="60" maxlength="250"></td>
    </tr>
    <tr>
        <td colspan="2">&nbsp;</td>
        <td colspan="1" align=left><input type="submit" name="Update" value="Update"></td>
    </tr>
    </table>
    </form>
<#else>
  <h3>You do not have permission to view this page. ("FACILITY_VIEW" or "FACILITY_ADMIN" needed)</h3>
</#if>
