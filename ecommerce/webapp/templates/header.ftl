
<#assign layoutSettings = requestAttributes.layoutSettings>

<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>${layoutSettings.companyName?if_exists}: ${content.title?if_exists}</title>
    <link rel='stylesheet' href='<transform ofbizContentUrl>/images/maincss.css</transform>' type='text/css'>
    
    <#-- Append CSS for catalog -->
    <#if requestAttributes.catalogStyleSheet?exists>
    <link rel='stylesheet' href='${requestAttribute.catalogStyleSheet}' type="text/css">
    </#if>
     
    <#-- Append CSS for tracking codes -->
    <#if sessionAttributes.overrideCss?exists>
	<link rel='stylesheet' href='${sessionAttributes.overrideCss}' type="text/css">
    </#if>
</head>
<body>

<TABLE border=0 width='100%' cellspacing='0' cellpadding='0' class='headerboxoutside'>
  <TR>
    <TD width='100%'>
      <table width='100%' border='0' cellspacing='0' cellpadding='0' class='headerboxtop'>
        <tr>
          <#if sessionAttributes.overrideLogo?exists>
            <TD align=left width='1%'><IMG src='${sessionAttributes.overrideLogo}'></TD> 
          <#elseif requestAttributes.catalogHeaderLogo?exists>
            <TD align=left width='1%'><IMG src='${requestAttributes.catalogHeaderLogo}'></TD> 
          <#elseif layoutSettings.headerImageUrl?has_content>
            <TD align=left width='1%'><IMG src='<transform ofbizContentUrl>${layoutSettings.headerImageUrl}</transform>'></TD>
          </#if>
          <TD align=center width='98%' <#if layoutSettings.headerMiddleBackgroundUrl?has_content>background='<transform ofbizContentUrl>${layoutSettings.headerMiddleBackgroundUrl}</transform>'</#if> >
              <#if layoutSettings.companyName?exists><span class='headerCompanyName'>${layoutSettings.companyName}</span></#if>
              <#if layoutSettings.companySubtitle?exists><br><span class='headerCompanySubtitle'>${layoutSettings.companySubtitle}</span></#if>
          </TD>
          <TD align=right width='1%' nowrap <#if layoutSettings.headerRightBackgroundUrl?has_content>background='<transform ofbizContentUrl>${layoutSettings.headerRightBackgroundUrl}</transform>'</#if> >
            ${pages.get("/cart/microcart.ftl")}
          </td>
        </tr>
      </table>
    </TD>
  </TR>
  <TR>
    <TD width='100%'>
      <table width='100%' border='0' cellspacing='0' cellpadding='0' class='headerboxbottom'>
        <tr>
          <#if userLogin?exists>
            <td class="headerButtonLeft"><a href="<transform ofbizUrl>/logout</transform>" class="headerbuttontext">Logout</a></td>
          <#else>
            <td class="headerButtonLeft"><a href='<transform ofbizUrl>${Static["org.ofbiz.commonapp.common.CommonWorkers"].makeLoginUrl(request, "checkLogin")}</transform>' class='headerbuttontext'>Login</a></td>
          </#if>
          <td class="headerButtonLeft"><a href="<transform ofbizUrl>/main</transform>" class="headerbuttontext">Main</a></td>

          <#if sessionAttributes.autoName?exists>
            <TD width="90%" align="center" class="headerCenter">
                Welcome&nbsp;${sessionAttributes.autoName}!
                (Not&nbsp;You?&nbsp;<a href="<transform ofbizUrl>/autoLogout</transform>" class="buttontext">click&nbsp;here</a>)
            </TD>
          <#else>
              <TD width="90%" align=center class='headerCenter'>Welcome!</TD>
          </#if>

          <#if Static["org.ofbiz.commonapp.product.catalog.CatalogWorker"].getCatalogQuickaddUse(request)>
            <td class="headerButtonRight"><a href="<transform ofbizUrl>/quickadd</transform>" class="headerbuttontext">Quick&nbsp;Add</a></td>
          </#if>
          <td class="headerButtonRight"><a href="<transform ofbizUrl>/orderhistory</transform>" class="headerbuttontext">Order&nbsp;History</a></td>
          <td class="headerButtonRight"><a href="<transform ofbizUrl>/viewprofile</transform>" class="headerbuttontext">Profile</a></td>
        </TR>
      </TABLE>
    </TD>
  </TR>
</TABLE>

<br>
