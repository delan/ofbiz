<#--
 *  Copyright (c) 2001, 2002 The Open For Business Project - www.ofbiz.org
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
 *@author     David E. Jones (jonesde@ofbiz.org)
 *@author     Andy Zeneski (jaz@ofbiz.org)
 *@version    $Revision$
 *@since      2.1
-->

<#assign layoutSettings = requestAttributes.layoutSettings>

<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>${layoutSettings.companyName?if_exists}: ${content.title?if_exists}</title>
    <link rel='stylesheet' href='<@ofbizContentUrl>/images/maincss.css</@ofbizContentUrl>' type='text/css'>
    
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
            <TD align=left width='1%'><IMG src='<@ofbizContentUrl>${layoutSettings.headerImageUrl}</@ofbizContentUrl>'></TD>
          </#if>
          <TD align=center width='98%' <#if layoutSettings.headerMiddleBackgroundUrl?has_content>background='<@ofbizContentUrl>${layoutSettings.headerMiddleBackgroundUrl}</@ofbizContentUrl>'</#if> >
              <#if layoutSettings.companyName?exists><span class='headerCompanyName'>${layoutSettings.companyName}</span></#if>
              <#if layoutSettings.companySubtitle?exists><br><span class='headerCompanySubtitle'>${layoutSettings.companySubtitle}</span></#if>
          </TD>
          <TD align=right width='1%' nowrap <#if layoutSettings.headerRightBackgroundUrl?has_content>background='<@ofbizContentUrl>${layoutSettings.headerRightBackgroundUrl}</@ofbizContentUrl>'</#if> >
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
            <td class="headerButtonLeft"><a href="<@ofbizUrl>/logout</@ofbizUrl>" class="headerbuttontext">Logout</a></td>
          <#else>
            <td class="headerButtonLeft"><a href='<@ofbizUrl>${Static["org.ofbiz.commonapp.common.CommonWorkers"].makeLoginUrl(request, "checkLogin")}</@ofbizUrl>' class='headerbuttontext'>Login</a></td>
          </#if>
          <td class="headerButtonLeft"><a href="<@ofbizUrl>/main</@ofbizUrl>" class="headerbuttontext">Main</a></td>

          <#if sessionAttributes.autoName?exists>
            <TD width="90%" align="center" class="headerCenter">
                Welcome&nbsp;${sessionAttributes.autoName}!
                (Not&nbsp;You?&nbsp;<a href="<@ofbizUrl>/autoLogout</@ofbizUrl>" class="buttontext">click&nbsp;here</a>)
            </TD>
          <#else>
              <TD width="90%" align=center class='headerCenter'>Welcome!</TD>
          </#if>

          <#if Static["org.ofbiz.commonapp.product.catalog.CatalogWorker"].getCatalogQuickaddUse(request)>
            <td class="headerButtonRight"><a href="<@ofbizUrl>/quickadd</@ofbizUrl>" class="headerbuttontext">Quick&nbsp;Add</a></td>
          </#if>
          <td class="headerButtonRight"><a href="<@ofbizUrl>/orderhistory</@ofbizUrl>" class="headerbuttontext">Order&nbsp;History</a></td>
          <td class="headerButtonRight"><a href="<@ofbizUrl>/viewprofile</@ofbizUrl>" class="headerbuttontext">Profile</a></td>
        </TR>
      </TABLE>
    </TD>
  </TR>
</TABLE>

<br>
