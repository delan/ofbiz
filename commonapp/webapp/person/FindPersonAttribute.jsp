
<%
/**
 *  Title: Person Component - Person Attribute Entity
 *  Description: None
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
 *@created    Wed May 23 12:50:14 MDT 2001
 *@version    1.0
 */
%>

<%@ page import="org.ofbiz.commonapp.person.*" %>
<%@ page import="java.text.*" %>
<%@ page import="java.util.*" %>
<%@ page import="org.ofbiz.commonapp.common.*" %>
<%@ page import="org.ofbiz.commonapp.webevent.*" %>
<%@ page import="org.ofbiz.commonapp.security.*" %>

<%@ taglib uri="/WEB-INF/webevent.tld" prefix="webevent" %>
<webevent:dispatch loginRequired="true" />

<%pageContext.setAttribute("PageName", "FindPersonAttribute"); %>

<%@ include file="/includes/header.jsp" %>
<%@ include file="/includes/onecolumn.jsp" %>

<%boolean hasViewPermission=Security.hasEntityPermission("PERSON_ATTRIBUTE", "_VIEW", session);%>
<%boolean hasCreatePermission=Security.hasEntityPermission("PERSON_ATTRIBUTE", "_CREATE", session);%>
<%boolean hasUpdatePermission=Security.hasEntityPermission("PERSON_ATTRIBUTE", "_UPDATE", session);%>
<%boolean hasDeletePermission=Security.hasEntityPermission("PERSON_ATTRIBUTE", "_DELETE", session);%>
<%if(hasViewPermission){%>
<%
  String rowColorTop1 = "99CCFF";
  String rowColorTop2 = "CCFFFF";
  String rowColorTop = "";
  String rowColorResultIndex = "CCFFFF";
  String rowColorResultHeader = "99CCFF";
  String rowColorResult1 = "99FFCC";
  String rowColorResult2 = "CCFFCC";
  String rowColorResult = "";

  String searchType = request.getParameter("SEARCH_TYPE");
  String searchParam1 = UtilFormatOut.checkNull(request.getParameter("SEARCH_PARAMETER1"));
  String searchParam2 = UtilFormatOut.checkNull(request.getParameter("SEARCH_PARAMETER2"));
  String searchParam3 = UtilFormatOut.checkNull(request.getParameter("SEARCH_PARAMETER3"));
  if(searchType == null || searchType.length() <= 0) searchType = "all";
  String curFindString = "SEARCH_TYPE=" + searchType + "&SEARCH_PARAMETER1=" + searchParam1 + "&SEARCH_PARAMETER2=" + searchParam2 + "&SEARCH_PARAMETER3=" + searchParam3;
  curFindString = UtilFormatOut.encodeQuery(curFindString);

  Collection personAttributeCollection = null;
  Object[] personAttributeArray = (Object[])session.getAttribute("CACHE_SEARCH_RESULTS");
%>
<%
//--------------
  String viewIndexString = (String)request.getParameter("VIEW_INDEX");
  if (viewIndexString == null || viewIndexString.length() == 0) { viewIndexString = "0"; }
  int viewIndex = 0;
  try { viewIndex = Integer.valueOf(viewIndexString).intValue(); }
  catch (NumberFormatException nfe) { viewIndex = 0; }

  String viewSizeString = (String)request.getParameter("VIEW_SIZE");
  if (viewSizeString == null || viewSizeString.length() == 0) { viewSizeString = "10"; }
  int viewSize = 10;
  try { viewSize = Integer.valueOf(viewSizeString).intValue(); }
  catch (NumberFormatException nfe) { viewSize = 10; }

//--------------
  String personAttributeArrayName = (String)session.getAttribute("CACHE_SEARCH_RESULTS_NAME");
  if(personAttributeArray == null || personAttributeArrayName == null || curFindString.compareTo(personAttributeArrayName) != 0 || viewIndex == 0)
  {
    if(UtilProperties.propertyValueEqualsIgnoreCase("debug", "print.info", "true")) System.out.println("-=-=-=-=- Current Array not found in session, getting new one...");
    if(UtilProperties.propertyValueEqualsIgnoreCase("debug", "print.info", "true")) System.out.println("-=-=-=-=- curFindString:" + curFindString + " personAttributeArrayName:" + personAttributeArrayName);

    if(searchType.compareTo("all") == 0) personAttributeCollection = PersonAttributeHelper.findAll();

    else if(searchType.compareTo("Username") == 0) personAttributeCollection = PersonAttributeHelper.findByUsername(searchParam1);

    else if(searchType.compareTo("Name") == 0) personAttributeCollection = PersonAttributeHelper.findByName(searchParam1);

    else if(searchType.compareTo("primaryKey") == 0)
    {
      personAttributeCollection = new LinkedList();
      PersonAttribute personAttributeTemp = PersonAttributeHelper.findByPrimaryKey(searchParam1,searchParam2);
      if(personAttributeTemp != null) personAttributeCollection.add(personAttributeTemp);
    }
    if(personAttributeCollection != null) personAttributeArray = personAttributeCollection.toArray();

    if(personAttributeArray != null)
    {
      session.setAttribute("CACHE_SEARCH_RESULTS", personAttributeArray);
      session.setAttribute("CACHE_SEARCH_RESULTS_NAME", curFindString);
    }
  }
//--------------
  int lowIndex = viewIndex*viewSize+1;
  int highIndex = (viewIndex+1)*viewSize;
  int arraySize = 0;
  if(personAttributeArray!=null) arraySize = personAttributeArray.length;
  if(arraySize<highIndex) highIndex=arraySize;
  if(UtilProperties.propertyValueEqualsIgnoreCase("debug", "print.info", "true")) System.out.println("viewIndex=" + viewIndex + " lowIndex=" + lowIndex + " highIndex=" + highIndex + " arraySize=" + arraySize);
%>
<h3 style=margin:0;>Find PersonAttributes</h3>
Note: you may use the '%' character as a wildcard, to replace any other letters.
<table cellpadding="2" cellspacing="2" border="0">
  <%rowColorTop=(rowColorTop==rowColorTop1?rowColorTop2:rowColorTop1);%><tr bgcolor="<%=rowColorTop%>">
    <form method="post" action="FindPersonAttribute.jsp" style=margin:0;>
      <td valign="top">Primary Key:</td>
      <td valign="top">
          <input type="hidden" name="SEARCH_TYPE" value="primaryKey">

          <input type="text" name="SEARCH_PARAMETER1" value="" size="20">
          <input type="text" name="SEARCH_PARAMETER2" value="" size="20">
          (Must be exact)
      </td>
      <td valign="top">
          <input type="submit" value="Find">
      </td>
    </form>
  </tr>

  
  <%rowColorTop=(rowColorTop==rowColorTop1?rowColorTop2:rowColorTop1);%><tr bgcolor="<%=rowColorTop%>">
    <td valign="top">Username: </td>
    <form method="post" action="FindPersonAttribute.jsp" style=margin:0;>
      <td valign="top">
        <input type="hidden" name="SEARCH_TYPE" value="Username">

        <input type="text" name="SEARCH_PARAMETER1" value="" size="20">
      </td>
      <td valign="top">
        <input type="submit" value="Find">
      </td>
    </form>
  </tr>

  
  <%rowColorTop=(rowColorTop==rowColorTop1?rowColorTop2:rowColorTop1);%><tr bgcolor="<%=rowColorTop%>">
    <td valign="top">Name: </td>
    <form method="post" action="FindPersonAttribute.jsp" style=margin:0;>
      <td valign="top">
        <input type="hidden" name="SEARCH_TYPE" value="Name">

        <input type="text" name="SEARCH_PARAMETER1" value="" size="20">
      </td>
      <td valign="top">
        <input type="submit" value="Find">
      </td>
    </form>
  </tr>

  <%rowColorTop=(rowColorTop==rowColorTop1?rowColorTop2:rowColorTop1);%><tr bgcolor="<%=rowColorTop%>">
    <td valign="top">Display All: </td>
    <form method="post" action="FindPersonAttribute.jsp" style=margin:0;>
      <td valign="top">
        <input type="hidden" name="SEARCH_TYPE" value="all">
      </td>
      <td valign="top">
        <input type="submit" value="Find">
      </td>
    </form>
  </tr>
</table>
<b>PersonAttributes found by:&nbsp; <%=searchType%> : <%=UtilFormatOut.checkNull(searchParam1)%> : <%=UtilFormatOut.checkNull(searchParam2)%> : <%=UtilFormatOut.checkNull(searchParam3)%></b>
<br>
<%if(hasCreatePermission){%>
  <a href="EditPersonAttribute.jsp" class="buttontext">[Create PersonAttribute]</a>
<%}%>
<table border="0" width="100%" cellpadding="2">
<% if(arraySize > 0) { %>
    <tr bgcolor="<%=rowColorResultIndex%>">
      <td align="left">
        <b>
        <% if(viewIndex > 0) { %>
          <a href="FindPersonAttribute.jsp?<%=curFindString%>&VIEW_SIZE=<%=viewSize%>&VIEW_INDEX=<%=viewIndex-1%>" class="buttontext">[Previous]</a> |
        <% } %>
        <% if(arraySize > 0) { %>
          <%=lowIndex%> - <%=highIndex%> of <%=arraySize%>
        <% } %>
        <% if(arraySize>highIndex) { %>
          | <a href="FindPersonAttribute.jsp?<%=curFindString%>&VIEW_SIZE=<%=viewSize%>&VIEW_INDEX=<%=viewIndex+1%>" class="buttontext">[Next]</a>
        <% } %>
        </b>
      </td>
    </tr>
<% } %>
</table>

  <table width="100%" cellpadding="2" cellspacing="2" border="0">
    <tr bgcolor="<%=rowColorResultHeader%>">
  
      <td><div class="tabletext"><b><nobr>USERNAME</nobr></b></div></td>
      <td><div class="tabletext"><b><nobr>NAME</nobr></b></div></td>
      <td><div class="tabletext"><b><nobr>VALUE</nobr></b></div></td>
      <td>&nbsp;</td>
      <%if(hasUpdatePermission){%>
        <td>&nbsp;</td>
      <%}%>
      <%if(hasDeletePermission){%>
        <td>&nbsp;</td>
      <%}%>
    </tr>
<%
 if(personAttributeArray != null && personAttributeArray.length > 0)
 {
  int loopIndex;
  //for(loopIndex=personAttributeArray.length-1; loopIndex>=0 ; loopIndex--)
  for(loopIndex=lowIndex; loopIndex<=highIndex; loopIndex++)
  {
    PersonAttribute personAttribute = (PersonAttribute)personAttributeArray[loopIndex-1];
    if(personAttribute != null)
    {
%>
    <%rowColorResult=(rowColorResult==rowColorResult1?rowColorResult2:rowColorResult1);%><tr bgcolor="<%=rowColorResult%>">
  
      <td>
        <div class="tabletext">
    
      <%=UtilFormatOut.checkNull(personAttribute.getUsername())%>
    
        &nbsp;</div>
      </td>
  
      <td>
        <div class="tabletext">
    
      <%=UtilFormatOut.checkNull(personAttribute.getName())%>
    
        &nbsp;</div>
      </td>
  
      <td>
        <div class="tabletext">
    
      <%=UtilFormatOut.checkNull(personAttribute.getValue())%>
    
        &nbsp;</div>
      </td>
  
      <td>
        <a href="ViewPersonAttribute.jsp?PERSON_ATTRIBUTE_USERNAME=<%=personAttribute.getUsername()%>&PERSON_ATTRIBUTE_NAME=<%=personAttribute.getName()%>" class="buttontext">[View]</a>
      </td>
      <%if(hasUpdatePermission){%>
        <td>
          <a href="EditPersonAttribute.jsp?PERSON_ATTRIBUTE_USERNAME=<%=personAttribute.getUsername()%>&PERSON_ATTRIBUTE_NAME=<%=personAttribute.getName()%>" class="buttontext">[Edit]</a>
        </td>
      <%}%>
      <%if(hasDeletePermission){%>
        <td>
          <a href="FindPersonAttribute.jsp?WEBEVENT=UPDATE_PERSON_ATTRIBUTE&UPDATE_MODE=DELETE&PERSON_ATTRIBUTE_USERNAME=<%=personAttribute.getUsername()%>&PERSON_ATTRIBUTE_NAME=<%=personAttribute.getName()%>&<%=curFindString%>" class="buttontext">[Delete]</a>
        </td>
      <%}%>
    </tr>
  <%}%>
<%
   }
 }
 else
 {
%>
<%rowColorResult=(rowColorResult==rowColorResult1?rowColorResult2:rowColorResult1);%><tr bgcolor="<%=rowColorResult%>">
<td colspan="8">
<h3>No PersonAttributes Found.</h3>
</td>
</tr>
<%}%>
</table>

<table border="0" width="100%" cellpadding="2">
<%if(arraySize > 0){%>
  <tr bgcolor="<%=rowColorResultIndex%>">
    <td align="left">
      <b>
      <% if(viewIndex > 0) { %>
      <a href="FindPersonAttribute.jsp?<%=curFindString%>&VIEW_SIZE=<%=viewSize%>&VIEW_INDEX=<%=viewIndex-1%>" class="buttontext">[Previous]</a> |
      <% } %>
      <% if(arraySize > 0) { %>
      <%=lowIndex%> - <%=highIndex%> of <%=arraySize%>
      <% } %>
      <% if(arraySize>highIndex) { %>
      | <a href="FindPersonAttribute.jsp?<%=curFindString%>&VIEW_SIZE=<%=viewSize%>&VIEW_INDEX=<%=viewIndex+1%>" class="buttontext">[Next]</a>
      <% } %>
      </b>
    </td>
  </tr>
<%}%>
</table>
<%if(hasCreatePermission){%>
  <a href="EditPersonAttribute.jsp" class="buttontext">[Create PersonAttribute]</a>
<%}%>
<%}else{%>
  <h3>You do not have permission to view this page (PERSON_ATTRIBUTE_ADMIN, or PERSON_ATTRIBUTE_VIEW needed).</h3>
<%}%>

<%@ include file="/includes/onecolumnclose.jsp" %>
<%@ include file="/includes/footer.jsp" %>
