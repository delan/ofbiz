<%@ page import="java.util.*" %>
<%@ page import="org.ofbiz.core.security.*" %>
<%@ page import="org.ofbiz.core.entity.*" %>
<%@ page import="org.ofbiz.core.util.*" %>

<%@ taglib uri="ofbizTags" prefix="ofbiz" %>

<jsp:useBean id="security" type="org.ofbiz.core.security.Security" scope="application" />
<jsp:useBean id="delegator" type="org.ofbiz.core.entity.GenericDelegator" scope="application" />
<%GenericValue userLogin = (GenericValue)session.getAttribute(SiteDefs.USER_LOGIN);%>
<%GenericValue person = userLogin==null?null:userLogin.getRelatedOne("Person");%>
<%String controlPath=(String)request.getAttribute(SiteDefs.CONTROL_PATH);%>

<%String pageName = (String)pageContext.getAttribute("PageName");%>
<html>
<head>

<title>Open For Commerce: <%=pageName%></title>

<%-- <link rel="stylesheet" href="<%=controlPath + "/view/includes/maincss.css"%>" type="text/css"> --%>
<link rel="stylesheet" href="/ecommerce/includes/maincss.css" type="text/css">
<%-- <%@ include file="/includes/maincss.jsp"%> --%>

<script language="javascript">
function mOvr(src,clrOver){ 
  if (!src.contains(event.fromElement)){ 
          src.style.cursor = 'hand';
          src.bgColor = clrOver; 
  } 
} 
function mOut(src,clrIn){ 
  if (!src.contains(event.toElement)){ 
    src.style.cursor = 'default'; 
    src.bgColor = clrIn; 
  } 
}
function mClk(src){ 
	if(event.srcElement.tagName=='TD')
		src.children.tags('A')[0].click();
}
</script>
<style>
 .headerButtonLeft { BORDER-RIGHT:solid white 1px; PADDING-RIGHT: 10px; PADDING-LEFT: 10px; }
 .headerButtonRight { BORDER-LEFT:solid white 1px; PADDING-RIGHT: 10px; PADDING-LEFT: 10px; }
 .headerCenter { PADDING-RIGHT:10px; PADDING-LEFT:10px; font-size: 10pt; font-weight: bold; COLOR: #000000; }
</style>
</head>

<body>

<TABLE border=0 width='100%' cellpadding=1 cellspacing=0 bgcolor='black'>
  <TR>
    <TD width='100%'>
      <table width="100%" border="0" cellpadding="4" cellspacing="0" bgcolor="#678475">
        <tr>
          <td align=left width='50%'>
            <div style="margin: 0; font-size: 24pt; font-weight: bold; color: white;">Open&nbsp;For&nbsp;Commerce</div>
            <div style="FONT-SIZE: 8pt; margin: 0; color: white;">Part&nbsp;of&nbsp;the&nbsp;Open&nbsp;For&nbsp;Business&nbsp;Family&nbsp;of&nbsp;Open&nbsp;Source&nbsp;Software</font></div>
          </TD>
          <TD align=right width='50%'>
<%@ include file="/cart/microcart.jsp"%>
          </td>
        </tr>
      </table>
    </TD>
  </TR>
  <TR>
    <TD width='100%'>
      <table width='100%' border=0 cellpadding=4 cellspacing=0 bgcolor='#cccc99'>
        <tr>
          <%
            String queryString = null;
            Enumeration parameterNames = request.getParameterNames();
            while(parameterNames != null && parameterNames.hasMoreElements())
            {
              String paramName = (String)parameterNames.nextElement();
              if(paramName != null)
              {
                if(queryString == null) queryString = paramName + "=" + request.getParameter(paramName);
                else queryString = queryString + "&" + paramName + "=" + request.getParameter(paramName);
              }
            }

            String loginUrl = controlPath + "/checkLogin/" + UtilFormatOut.checkNull((String)request.getAttribute(SiteDefs.CURRENT_VIEW));
            if(queryString != null) loginUrl = loginUrl  + "?" + UtilFormatOut.checkNull(queryString);
          %>
          <%if(userLogin==null){%>
            <td bgcolor="#cccc99" onmouseover='mOvr(this,"#eeeecc");' onmouseout='mOut(this,"#cccc99");' onclick="mClk(this);" class="headerButtonLeft"><a href="<%=response.encodeURL(loginUrl)%>" class="buttontext">Login</a></td>
          <%}else{%>
            <td bgcolor="#cccc99" onmouseover='mOvr(this,"#eeeecc");' onmouseout='mOut(this,"#cccc99");' onclick="mClk(this);" class="headerButtonLeft"><a href="<ofbiz:url>/logout/main</ofbiz:url>" class="buttontext">Logout</a></td>
          <%}%>
          <td bgcolor="#cccc99" onmouseover='mOvr(this,"#eeeecc");' onmouseout='mOut(this,"#cccc99");' onclick="mClk(this);" class="headerButtonLeft"><a href="<ofbiz:url>/main</ofbiz:url>" class="buttontext">Main</a></td>
          <%-- <td bgcolor="#cccc99" onmouseover='mOvr(this,"#eeeecc");' onmouseout='mOut(this,"#cccc99");' onclick="mClk(this);" class="headerButtonLeft"><a href="<ofbiz:url>/catalog?catalog_id=<%=UtilProperties.getPropertyValue("ecommerce", "catalog.id.default")%></ofbiz:url>" class="buttontext">Catalog</a></td> --%>
          <%if(person==null){%>
            <TD bgcolor="#cccc99" width="90%" align=center class='headerCenter'>Welcome!</TD>
          <%}else{%>
            <TD bgcolor="#cccc99" width="90%" align=center class='headerCenter'>Welcome<%=UtilFormatOut.ifNotEmpty(person.getString("firstName"),"&nbsp;","")%><%=UtilFormatOut.ifNotEmpty(person.getString("lastName"),"&nbsp;","")%>!</TD>
          <%}%>
          <td bgcolor="#cccc99" onmouseover='mOvr(this,"#eeeecc");' onmouseout='mOut(this,"#cccc99");' onclick="mClk(this);" class="headerButtonRight"><a href="<ofbiz:url>/orderhistory</ofbiz:url>" class="buttontext">Order&nbsp;History</a></td>
          <td bgcolor="#cccc99" onmouseover='mOvr(this,"#eeeecc");' onmouseout='mOut(this,"#cccc99");' onclick="mClk(this);" class="headerButtonRight"><a href="<ofbiz:url>/viewprofile</ofbiz:url>" class="buttontext">Profile</a></td>
          <%-- <td bgcolor="#cccc99" onmouseover='mOvr(this,"#eeeecc");' onmouseout='mOut(this,"#cccc99");' onclick="mClk(this);" class="headerButtonRight"><a href="<ofbiz:url>/view/showcart</ofbiz:url>" class="buttontext">ViewCart</a></td> --%>
          <%-- <td bgcolor="#cccc99" onmouseover='mOvr(this,"#eeeecc");' onmouseout='mOut(this,"#cccc99");' onclick="mClk(this);" class="headerButtonRight"><a href="<ofbiz:url>/checkoutoptions</ofbiz:url>" class="buttontext">CheckOut</a></td> --%>
          <%-- <td bgcolor="#cccc99" onmouseover='mOvr(this,"#eeeecc");' onmouseout='mOut(this,"#cccc99");' onclick="mClk(this);" class="headerButtonRight"><a href="<ofbiz:url>/sitemap</ofbiz:url>" class="buttontext">Site&nbsp;Map</a></td> --%>
        </TR>
      </TABLE>
    </TD>
  </TR>
</TABLE>
