
<%
/**
 *  Title: Person Component - Person Person Type Entity
 *  Description: Maps a Person to a Person Type; necessary so a person can be of multiple types.
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
 *@created    Wed May 23 12:53:34 MDT 2001
 *@version    1.0
 */
%>

<%@ page import="org.ofbiz.commonapp.person.*" %>
<%@ page import="java.util.*" %>
<%@ page import="org.ofbiz.commonapp.common.*" %>
<%@ page import="org.ofbiz.commonapp.webevent.*" %>
<%@ page import="org.ofbiz.commonapp.security.*" %>

<%@ taglib uri="/WEB-INF/webevent.tld" prefix="webevent" %>
<webevent:dispatch loginRequired="true" />

<%pageContext.setAttribute("PageName", "EditPersonPersonType"); %>

<%@ include file="/includes/header.jsp" %>
<%@ include file="/includes/onecolumn.jsp" %>

<%boolean hasViewPermission=Security.hasEntityPermission("PERSON_PERSON_TYPE", "_VIEW", session);%>
<%boolean hasCreatePermission=Security.hasEntityPermission("PERSON_PERSON_TYPE", "_CREATE", session);%>
<%boolean hasUpdatePermission=Security.hasEntityPermission("PERSON_PERSON_TYPE", "_UPDATE", session);%>
<%boolean hasDeletePermission=Security.hasEntityPermission("PERSON_PERSON_TYPE", "_DELETE", session);%>
<%if(hasViewPermission){%>

<%
  String rowColor1 = "99CCFF";
  String rowColor2 = "CCFFFF";
  String rowColor = "";

    String username = request.getParameter("PERSON_PERSON_TYPE_USERNAME");
    String typeId = request.getParameter("PERSON_PERSON_TYPE_TYPE_ID");

  
  

  PersonPersonType personPersonType = PersonPersonTypeHelper.findByPrimaryKey(username, typeId);
%>

<a href="FindPersonPersonType.jsp" class="buttontext">[Find PersonPersonType]</a>
<%if(hasCreatePermission){%>
  <a href="EditPersonPersonType.jsp" class="buttontext">[Create PersonPersonType]</a>
<%}%>
<%if(hasDeletePermission){%>
  <%if(personPersonType != null){%>
    <a href="EditPersonPersonType.jsp?WEBEVENT=UPDATE_PERSON_PERSON_TYPE&UPDATE_MODE=DELETE&PERSON_PERSON_TYPE_USERNAME=<%=username%>&PERSON_PERSON_TYPE_TYPE_ID=<%=typeId%>" class="buttontext">[Delete this PersonPersonType]</a>
  <%}%>
<%}%>
<%if(hasUpdatePermission){%>
  <%if(username != null && typeId != null){%>
    <a href="EditpersonPersonType.jsp?PERSON_PERSON_TYPE_USERNAME=<%=username%>&PERSON_PERSON_TYPE_TYPE_ID=<%=typeId%>" class="buttontext">[Edit PersonPersonType]</a>
  <%}%>
<%}%>

<table border="0" cellspacing="2" cellpadding="2">
<%if(personPersonType == null){%>
<tr bgcolor="<%=rowColor1%>"><td><h3>Specified PersonPersonType was not found.</h3></td></tr>
<%}else{%>
  <input type="hidden" name="WEBEVENT" value="UPDATE_PERSON_PERSON_TYPE">
  <input type="hidden" name="UPDATE_MODE" value="UPDATE">

  <%rowColor=(rowColor==rowColor1?rowColor2:rowColor1);%>
  <tr bgcolor="<%=rowColor%>">
    <td>USERNAME</td>
    <td>
    
      <%=UtilFormatOut.checkNull(personPersonType.getUsername())%>
    
    </td>
  </tr>

  <%rowColor=(rowColor==rowColor1?rowColor2:rowColor1);%>
  <tr bgcolor="<%=rowColor%>">
    <td>TYPE_ID</td>
    <td>
    
      <%=UtilFormatOut.checkNull(personPersonType.getTypeId())%>
    
    </td>
  </tr>

<%} //end if personPersonType == null %>
</table>

<a href="FindPersonPersonType.jsp" class="buttontext">[Find PersonPersonType]</a>
<%if(hasCreatePermission){%>
  <a href="EditPersonPersonType.jsp" class="buttontext">[Create PersonPersonType]</a>
<%}%>
<%if(hasDeletePermission){%>
  <%if(personPersonType != null){%>
    <a href="EditPersonPersonType.jsp?WEBEVENT=UPDATE_PERSON_PERSON_TYPE&UPDATE_MODE=DELETE&PERSON_PERSON_TYPE_USERNAME=<%=username%>&PERSON_PERSON_TYPE_TYPE_ID=<%=typeId%>" class="buttontext">[Delete this PersonPersonType]</a>
  <%}%>
<%}%>
<%if(hasUpdatePermission){%>
  <%if(username != null && typeId != null){%>
    <a href="EditpersonPersonType.jsp?PERSON_PERSON_TYPE_USERNAME=<%=username%>&PERSON_PERSON_TYPE_TYPE_ID=<%=typeId%>" class="buttontext">[Edit PersonPersonType]</a>
  <%}%>
<%}%>
<br>
<%}else{%>
  <h3>You do not have permission to view this page (PERSON_PERSON_TYPE_ADMIN, or PERSON_PERSON_TYPE_VIEW needed).</h3>
<%}%>

<%@ include file="/includes/onecolumnclose.jsp" %>
<%@ include file="/includes/footer.jsp" %>
