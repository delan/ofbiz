
<%
/**
 *  Title: Security Component - Security Group Permission Entity
 *  Description: Defines a permission available to a security group
 *  Copyright (c) 2001 The Open For Business Project - www.ofbiz.org
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
 *@created    Fri Jul 06 16:51:36 MDT 2001
 *@version    1.0
 */
%>

<%@ page import="java.util.*" %>
<%@ page import="org.ofbiz.commonapp.common.*" %>
<%@ page import="org.ofbiz.commonapp.webevent.*" %>
<%@ page import="org.ofbiz.commonapp.security.*" %>
<%@ page import="org.ofbiz.commonapp.security.securitygroup.*" %>


<%@ taglib uri="/WEB-INF/webevent.tld" prefix="webevent" %>
<webevent:dispatch loginRequired="true" />

<%pageContext.setAttribute("PageName", "EditSecurityGroupPermission"); %>

<%@ include file="/includes/header.jsp" %>
<%@ include file="/includes/onecolumn.jsp" %>

<%boolean hasViewPermission=Security.hasEntityPermission("SECURITY_GROUP_PERMISSION", "_VIEW", session);%>
<%boolean hasCreatePermission=Security.hasEntityPermission("SECURITY_GROUP_PERMISSION", "_CREATE", session);%>
<%boolean hasUpdatePermission=Security.hasEntityPermission("SECURITY_GROUP_PERMISSION", "_UPDATE", session);%>
<%boolean hasDeletePermission=Security.hasEntityPermission("SECURITY_GROUP_PERMISSION", "_DELETE", session);%>
<%if(hasViewPermission){%>

<%
  String rowClass1 = "viewOneTR1";
  String rowClass2 = "viewOneTR2";
  String rowClass = "";

  String groupId = request.getParameter("SECURITY_GROUP_PERMISSION_GROUP_ID");  
  String permissionId = request.getParameter("SECURITY_GROUP_PERMISSION_PERMISSION_ID");  

  
  

  SecurityGroupPermission securityGroupPermission = SecurityGroupPermissionHelper.findByPrimaryKey(groupId, permissionId);
%>

<br>
<div style='color: white; width: 100%; background-color: black; padding:3;'>
  <b>View Entity: SecurityGroupPermission with (GROUP_ID, PERMISSION_ID: <%=groupId%>, <%=permissionId%>).</b>
</div>

<a href="<%=response.encodeURL("FindSecurityGroupPermission.jsp")%>" class="buttontext">[Find SecurityGroupPermission]</a>
<%if(hasCreatePermission){%>
  <a href="<%=response.encodeURL("EditSecurityGroupPermission.jsp")%>" class="buttontext">[Create SecurityGroupPermission]</a>
<%}%>
<%if(securityGroupPermission != null){%>
  <%if(hasDeletePermission){%>
    <a href="<%=response.encodeURL("EditSecurityGroupPermission.jsp?WEBEVENT=UPDATE_SECURITY_GROUP_PERMISSION&UPDATE_MODE=DELETE&" + "SECURITY_GROUP_PERMISSION_GROUP_ID=" + groupId + "&" + "SECURITY_GROUP_PERMISSION_PERMISSION_ID=" + permissionId)%>" class="buttontext">[Delete this SecurityGroupPermission]</a>
  <%}%>
<%}%>
<%if(hasUpdatePermission){%>
  <%if(groupId != null && permissionId != null){%>
    <a href="<%=response.encodeURL("EditSecurityGroupPermission.jsp?" + "SECURITY_GROUP_PERMISSION_GROUP_ID=" + groupId + "&" + "SECURITY_GROUP_PERMISSION_PERMISSION_ID=" + permissionId)%>" class="buttontext">[Edit SecurityGroupPermission]</a>
  <%}%>
<%}%>

<table border="0" cellspacing="2" cellpadding="2">
<%if(securityGroupPermission == null){%>
<tr class="<%=rowClass1%>"><td><h3>Specified SecurityGroupPermission was not found.</h3></td></tr>
<%}else{%>

  <%rowClass=(rowClass==rowClass1?rowClass2:rowClass1);%>
  <tr class="<%=rowClass%>">
    <td><b>GROUP_ID</b></td>
    <td>
    
      <%=UtilFormatOut.checkNull(securityGroupPermission.getGroupId())%>
    
    </td>
  </tr>

  <%rowClass=(rowClass==rowClass1?rowClass2:rowClass1);%>
  <tr class="<%=rowClass%>">
    <td><b>PERMISSION_ID</b></td>
    <td>
    
      <%=UtilFormatOut.checkNull(securityGroupPermission.getPermissionId())%>
    
    </td>
  </tr>

<%} //end if securityGroupPermission == null %>
</table>

<a href="<%=response.encodeURL("FindSecurityGroupPermission.jsp")%>" class="buttontext">[Find SecurityGroupPermission]</a>
<%if(hasCreatePermission){%>
  <a href="<%=response.encodeURL("EditSecurityGroupPermission.jsp")%>" class="buttontext">[Create SecurityGroupPermission]</a>
<%}%>
<%if(securityGroupPermission != null){%>
  <%if(hasDeletePermission){%>
    <a href="<%=response.encodeURL("EditSecurityGroupPermission.jsp?WEBEVENT=UPDATE_SECURITY_GROUP_PERMISSION&UPDATE_MODE=DELETE&" + "SECURITY_GROUP_PERMISSION_GROUP_ID=" + groupId + "&" + "SECURITY_GROUP_PERMISSION_PERMISSION_ID=" + permissionId)%>" class="buttontext">[Delete this SecurityGroupPermission]</a>
  <%}%>
<%}%>
<%if(hasUpdatePermission){%>
  <%if(groupId != null && permissionId != null){%>
    <a href="<%=response.encodeURL("EditSecurityGroupPermission.jsp?" + "SECURITY_GROUP_PERMISSION_GROUP_ID=" + groupId + "&" + "SECURITY_GROUP_PERMISSION_PERMISSION_ID=" + permissionId)%>" class="buttontext">[Edit SecurityGroupPermission]</a>
  <%}%>
<%}%>
<br>
<br>
<SCRIPT language='JavaScript'>  
var numTabs=2;
function ShowTab(lname) 
{
  for(inc=1; inc <= numTabs; inc++)
  {
    document.all['tab' + inc].className = (lname == 'tab' + inc) ? 'ontab' : 'offtab';
    document.all['lnk' + inc].className = (lname == 'tab' + inc) ? 'onlnk' : 'offlnk';
    document.all['area' + inc].style.visibility = (lname == 'tab' + inc) ? 'visible' : 'hidden';
  }
}
</SCRIPT>
<%if(securityGroupPermission != null){%>
<table cellpadding='0' cellspacing='0'><tr>

  
    <%if(Security.hasEntityPermission("SECURITY_GROUP", "_VIEW", session)){%>
    <td id=tab1 class=ontab>
      <a href='javascript:ShowTab("tab1")' id=lnk1 class=onlnk> SecurityGroup</a>
    </td>
    <%}%>

  
    <%if(Security.hasEntityPermission("SECURITY_PERMISSION", "_VIEW", session)){%>
    <td id=tab2 class=offtab>
      <a href='javascript:ShowTab("tab2")' id=lnk2 class=offlnk> SecurityPermission</a>
    </td>
    <%}%>

</tr></table>
<%}%>
  

  
  
  
<%-- Start Relation for SecurityGroup, type: one --%>
<%if(securityGroupPermission != null){%>
  <%if(Security.hasEntityPermission("SECURITY_GROUP", "_VIEW", session)){%>
    <%SecurityGroup securityGroupRelated = SecurityGroupHelper.findByPrimaryKey(securityGroupPermission.getGroupId());%>
  <DIV id=area1 style="VISIBILITY: visible; POSITION: absolute" width="100%">
    <div class=areaheader>
     <b></b> Related Entity: <b>SecurityGroup</b> with (GROUP_ID: <%=securityGroupPermission.getGroupId()%>)
    </div>
    <%if(securityGroupPermission.getGroupId() != null){%>
      
      <a href="<%=response.encodeURL("/commonapp/security/securitygroup/ViewSecurityGroup.jsp?" + "SECURITY_GROUP_GROUP_ID=" + securityGroupPermission.getGroupId())%>" class="buttontext">[View SecurityGroup Details]</a>
      
    <%if(securityGroupRelated != null){%>
      <%if(Security.hasEntityPermission("SECURITY_GROUP", "_EDIT", session)){%>
        <a href="<%=response.encodeURL("/commonapp/security/securitygroup/EditSecurityGroup.jsp?" + "SECURITY_GROUP_GROUP_ID=" + securityGroupPermission.getGroupId())%>" class="buttontext">[Edit SecurityGroup]</a>
      <%}%>
    <%}else{%>
      <%if(Security.hasEntityPermission("SECURITY_GROUP", "_CREATE", session)){%>
        <a href="<%=response.encodeURL("/commonapp/security/securitygroup/EditSecurityGroup.jsp?" + "SECURITY_GROUP_GROUP_ID=" + securityGroupPermission.getGroupId())%>" class="buttontext">[Create SecurityGroup]</a>
      <%}%>
    <%}%>
    <%}%>
    <table border="0" cellspacing="2" cellpadding="2">
    <%if(securityGroupRelated == null){%>
    <tr class="<%=rowClass1%>"><td><h3>Specified SecurityGroup was not found.</h3></td></tr>
    <%}else{%>

  <%rowClass=(rowClass==rowClass1?rowClass2:rowClass1);%>
  <tr class="<%=rowClass%>">
    <td><b>GROUP_ID</b></td>
    <td>
    
      <%=UtilFormatOut.checkNull(securityGroupRelated.getGroupId())%>
    
    </td>
  </tr>

  <%rowClass=(rowClass==rowClass1?rowClass2:rowClass1);%>
  <tr class="<%=rowClass%>">
    <td><b>DESCRIPTION</b></td>
    <td>
    
      <%=UtilFormatOut.checkNull(securityGroupRelated.getDescription())%>
    
    </td>
  </tr>

    <%} //end if securityGroupRelated == null %>
    </table>
  </div>
  <%}%>
<%}%>
<%-- End Relation for SecurityGroup, type: one --%>
  

  
  
  
<%-- Start Relation for SecurityPermission, type: one --%>
<%if(securityGroupPermission != null){%>
  <%if(Security.hasEntityPermission("SECURITY_PERMISSION", "_VIEW", session)){%>
    <%SecurityPermission securityPermissionRelated = SecurityPermissionHelper.findByPrimaryKey(securityGroupPermission.getPermissionId());%>
  <DIV id=area2 style="VISIBILITY: hidden; POSITION: absolute" width="100%">
    <div class=areaheader>
     <b></b> Related Entity: <b>SecurityPermission</b> with (PERMISSION_ID: <%=securityGroupPermission.getPermissionId()%>)
    </div>
    <%if(securityGroupPermission.getPermissionId() != null){%>
      
      <a href="<%=response.encodeURL("/commonapp/security/securitygroup/ViewSecurityPermission.jsp?" + "SECURITY_PERMISSION_PERMISSION_ID=" + securityGroupPermission.getPermissionId())%>" class="buttontext">[View SecurityPermission Details]</a>
      
    <%if(securityPermissionRelated != null){%>
      <%if(Security.hasEntityPermission("SECURITY_PERMISSION", "_EDIT", session)){%>
        <a href="<%=response.encodeURL("/commonapp/security/securitygroup/EditSecurityPermission.jsp?" + "SECURITY_PERMISSION_PERMISSION_ID=" + securityGroupPermission.getPermissionId())%>" class="buttontext">[Edit SecurityPermission]</a>
      <%}%>
    <%}else{%>
      <%if(Security.hasEntityPermission("SECURITY_PERMISSION", "_CREATE", session)){%>
        <a href="<%=response.encodeURL("/commonapp/security/securitygroup/EditSecurityPermission.jsp?" + "SECURITY_PERMISSION_PERMISSION_ID=" + securityGroupPermission.getPermissionId())%>" class="buttontext">[Create SecurityPermission]</a>
      <%}%>
    <%}%>
    <%}%>
    <table border="0" cellspacing="2" cellpadding="2">
    <%if(securityPermissionRelated == null){%>
    <tr class="<%=rowClass1%>"><td><h3>Specified SecurityPermission was not found.</h3></td></tr>
    <%}else{%>

  <%rowClass=(rowClass==rowClass1?rowClass2:rowClass1);%>
  <tr class="<%=rowClass%>">
    <td><b>PERMISSION_ID</b></td>
    <td>
    
      <%=UtilFormatOut.checkNull(securityPermissionRelated.getPermissionId())%>
    
    </td>
  </tr>

  <%rowClass=(rowClass==rowClass1?rowClass2:rowClass1);%>
  <tr class="<%=rowClass%>">
    <td><b>DESCRIPTION</b></td>
    <td>
    
      <%=UtilFormatOut.checkNull(securityPermissionRelated.getDescription())%>
    
    </td>
  </tr>

    <%} //end if securityPermissionRelated == null %>
    </table>
  </div>
  <%}%>
<%}%>
<%-- End Relation for SecurityPermission, type: one --%>
  


<br>
<%}else{%>
  <h3>You do not have permission to view this page (SECURITY_GROUP_PERMISSION_ADMIN, or SECURITY_GROUP_PERMISSION_VIEW needed).</h3>
<%}%>

<%@ include file="/includes/onecolumnclose.jsp" %>
<%@ include file="/includes/footer.jsp" %>
