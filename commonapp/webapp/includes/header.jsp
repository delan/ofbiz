<%@ page import="java.util.*" %>
<%@ page import="org.ofbiz.commonapp.person.*" %>

<%String pageName = (String)pageContext.getAttribute("PageName"); %>
<html>
<head>

<title>Open For Business: <%=pageName%></title>

<%@ include file="/includes/maincss.jsp"%>
</head>

<body>
<%Person headerPerson = (Person)session.getAttribute("PERSON");%>

<TABLE width='100%' cellpadding='0' cellspacing='0' border='0' bgcolor='CCCCCC'>
  <TR>
    <TD>
      <H1 style=margin:0;>&nbsp;The Open For Business Project</H1>
    </TD>
  </TR>
</TABLE>
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
<TABLE width='100%' cellpadding='0' cellspacing='0' border='0' bgcolor='CCCCCC'>
  <TR>
    <%
      String queryString = null;
      Enumeration parameterNames = request.getParameterNames();
      while(parameterNames != null && parameterNames.hasMoreElements())
      {
        String paramName = (String)parameterNames.nextElement();
        if(paramName != null && paramName.compareTo("WEBEVENT") != 0)
        {
          if(queryString == null)
            queryString = paramName + "=" + request.getParameter(paramName);
          else
            queryString = queryString + "&" + paramName + "=" + request.getParameter(paramName);
        }
      }
      if(queryString == null) queryString = "WEBEVENT=forceLogin";
      else queryString = queryString + "&WEBEVENT=forceLogin";

      String loginUrl = request.getRequestURI() + "?" + queryString;
    %>
    <td bgcolor="#cccc99" onmouseover='mOvr(this,"#eeeecc");' onmouseout='mOut(this,"#cccc99");' onclick="mClk(this);" style="border-right:solid white 2px;padding-right:10px;padding-left:10px;"><a href="/commonapp/main.jsp" class="buttontext">Main</a></td>
    <%if(headerPerson==null){%>
      <td bgcolor="#cccc99" onmouseover='mOvr(this,"#eeeecc");' onmouseout='mOut(this,"#cccc99");' onclick="mClk(this);" style="border-right:solid white 2px;padding-right:10px;padding-left:10px;"><a href="<%=loginUrl%>" class="buttontext">Login</a></td>
    <%}else{%>
      <td bgcolor="#cccc99" onmouseover='mOvr(this,"#eeeecc");' onmouseout='mOut(this,"#cccc99");' onclick="mClk(this);" style="border-right:solid white 2px;padding-right:10px;padding-left:10px;"><a href="/commonapp/main.jsp?WEBPREEVENT=logout" class="buttontext">Logout</a></td>
    <%}%>
    <TD bgcolor="#cccc99" width="90%">&nbsp;</TD>
    <td bgcolor="#cccc99" onmouseover='mOvr(this,"#eeeecc");' onmouseout='mOut(this,"#cccc99");' onclick="mClk(this);" style="border-left:solid white 2px;padding-right:10px;padding-left:10px;"><a href="/commonapp/sitemap.jsp" class="buttontext">Site&nbsp;Map</a></td>
  </TR>
</TABLE>
