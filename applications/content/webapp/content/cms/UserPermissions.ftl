<script language="javascript1.2">
function call_fieldlookup3(view_name) {
        var webSitePublishPoint = document.userform.webSitePublishPoint.value;
	var obj_lookupwindow = window.open(view_name + "?webSitePublishPoint=" + webSitePublishPoint,'FieldLookup', 'width=700,height=550,scrollbars=yes,status=no,top='+my+',left='+mx+',dependent=yes,alwaysRaised=yes');
	obj_lookupwindow.opener = window;
	obj_lookupwindow.focus();
}
    function submitRows(rowCount) {
        var rowCountElement = document.createElement("input");
        rowCountElement.setAttribute("name", "_rowCount");
        rowCountElement.setAttribute("type", "hidden");
        rowCountElement.setAttribute("value", rowCount);
        document.forms.siteRoleForm.appendChild(rowCountElement);

        rowCountElement = document.createElement("input");
        rowCountElement.setAttribute("name", "partyId");
        rowCountElement.setAttribute("type", "hidden");
        rowCountElement.setAttribute("value", "${partyId?if_exists}");
        document.forms.siteRoleForm.appendChild(rowCountElement);

        rowCountElement = document.createElement("input");
        rowCountElement.setAttribute("name", "userLoginId");
        rowCountElement.setAttribute("type", "hidden");
        rowCountElement.setAttribute("value", "${userLoginId?if_exists}");
        document.forms.siteRoleForm.appendChild(rowCountElement);

        rowCountElement = document.createElement("input");
        rowCountElement.setAttribute("name", "webSitePublishPoint");
        rowCountElement.setAttribute("type", "hidden");
        rowCountElement.setAttribute("value", "${webSitePublishPoint?if_exists}");
        document.forms.siteRoleForm.appendChild(rowCountElement);

        document.forms.siteRoleForm.submit();
    }

</script>
${menuWrapper.renderMenuString()}

<#-- ============================================================= -->
<#assign partyUserLoginId=""/>
<#if partyAndUserLoginAndPerson?has_content>
    <#assign partyUserLoginId=partyAndUserLoginAndPerson.userLoginId?if_exists/>
</#if>
<#assign partyUserLoginId=""/>
<@checkPermission entityOperation="_ADMIN" targetOperation="CONTENT_ADMIN" subContentId=partyUserLoginId>
<br/>
<TABLE border="0" width='100%' cellspacing='0' cellpadding='0' class='boxoutside'>
  <TR>
    <TD width='100%'>
      <form name="userform" mode="POST" action="<@ofbizUrl>UserPermissions</@ofbizUrl>" >
      <table width='100%' border='0' cellspacing='0' cellpadding='0' class='appTitle'>
        <tr>
          <td colspan="1" valign="middle" align="right">
            <div class="boxhead">&nbsp; WebSitePublishPoint&nbsp;&nbsp; </div>
          </td>
          <td valign="middle" align="left">
            <div class="boxhead">
             <input type="text" name="webSitePublishPoint" size="20" value="${webSitePublishPoint?if_exists}">
             <input type="submit" value="ReGet"/>
             <input type="hidden" name="partyId" value="${partyId?if_exists}"/>
             <input type="hidden" name="userLoginId" value="${userLoginId?if_exists}"/>
            </div>
          </td>
        </tr>
        <tr>
          <td valign="middle" align="right">
            <div class="boxhead">&nbsp; User &nbsp;&nbsp;</div>
          </td>
          <td valign="middle" align="left">
            <div class="boxhead"><#if partyAndUserLoginAndPerson?has_content>${partyAndUserLoginAndPerson.firstName?if_exists}&nbsp;${partyAndUserLoginAndPerson.lastName?if_exists}[${partyAndUserLoginAndPerson.partyId?if_exists}]<#else>(No current user)</#if>
<a href="javascript:call_fieldlookup3('<@ofbizUrl>LookupPartyAndUserLoginAndPerson</@ofbizUrl>')"><img src="<@ofbizContentUrl>/content/images/fieldlookup.gif</@ofbizContentUrl>" width="16" height="16" border="0" alt="Lookup"></a></div>
          </td>
        </tr>
      </table>
      </form>
    </TD>
  </TR>
<#if partyAndUserLoginAndPerson?has_content>
  <TR>
    <TD width='100%'>
      <form name="siteRoleForm" mode="POST" action="<@ofbizUrl>updateSiteRoles</@ofbizUrl>">
      <table width='100%' border='0' cellspacing='0' cellpadding='4' class='boxoutside'>
        <tr>
            <td class="">Site</td>
            <#list blogRoleIdList as roleTypeId>
              <td class="">${roleTypeId}</td>
            </#list>
        </tr>

      <#assign rowCount=0/>
        <#list siteList as siteRoleMap>
          <tr>
            <td class="">${siteRoleMap.contentName}</td>
            <#list blogRoleIdList as roleTypeId>
              <#assign cappedSiteRole= Static["org.ofbiz.entity.model.ModelUtil"].dbNameToVarName(roleTypeId) />
              <td align="center">
              <input type="checkbox" name="${cappedSiteRole}_o_${rowCount}" value="Y" <#if siteRoleMap[cappedSiteRole] == "Y">checked</#if>/>
              </td>
            </#list>
          </tr>
          <input type="hidden" name="contentId_o_${rowCount}" value="${siteRoleMap.contentId}"/>
          <input type="hidden" name="partyId_o_${rowCount}" value="${siteRoleMap.partyId}"/>
          <#assign rowCount=rowCount + 1/>
        </#list>
          <tr>
            <td>
<div class="smallSubmit" ><a href="javascript:submitRows('${rowCount?if_exists}')">Update</a></div>
            </td>
          </tr>
      </table>
      </form>



                </td>
              </tr>
            </table>
          </td>
        </tr>
      </table>
    </TD>
  </TR>
</#if>
</TABLE>
</@checkPermission>
