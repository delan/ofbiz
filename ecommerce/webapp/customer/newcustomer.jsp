<%
/**
 *  Title: Login Page
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
 *@created    May 22 2001
 *@version    1.0
 */
%>

<%@ page import="javax.servlet.*" %>
<%@ page import="javax.servlet.http.*" %>
<%@ page import="org.ofbiz.core.util.*" %>

<%pageContext.setAttribute("PageName", "newuser");%>
<%@ include file="/includes/header.jsp" %>
<%@ include file="/includes/onecolumn.jsp" %>
<%String previousParams=(String)session.getAttribute(SiteDefs.PREVIOUS_PARAMS);%>
<%String createFormUrl=controlPath + "/createcustomer"; if(previousParams != null) createFormUrl=createFormUrl + "?" + previousParams;%>

<p class="head1">Request a New Account</p>

<%String fontColor = "Black";%>

<p>If you already have an account, use your browser's Back button to return to the Login page and log in from there.</p>

<form method="post" action="<%=response.encodeURL(controlPath + "/createcustomer")%>" name="newuserform" style='margin:0;'>
<table width="100%" border="0" cellpadding="2" cellspacing="0">
  <tr>
    <td width="26%"><div class="tabletext"><font color='<%=fontColor%>'>First name</font></div></td>
    <td width="74%">
      <input type="text" name="USER_FIRST_NAME" value="<%=UtilFormatOut.checkNull(request.getParameter("USER_FIRST_NAME"))%>" size="30" maxlength="30">
    * </td>
  </tr>
  <tr>
    <td width="26%"><div class="tabletext"><font color='<%=fontColor%>'>Middle initial</font></div></td>
    <td width="74%">
        <input type="text" name="USER_MIDDLE_NAME" value="<%=UtilFormatOut.checkNull(request.getParameter("USER_MIDDLE_NAME"))%>" size="4" maxlength="4">
    </td>
  </tr>
  <tr>
    <td width="26%"><div class="tabletext"><font color='<%=fontColor%>'>Last name </font></div></td>
    <td width="74%">
      <input type="text" name="USER_LAST_NAME" value="<%=UtilFormatOut.checkNull(request.getParameter("USER_LAST_NAME"))%>" size="30" maxlength="30">
    * </td>
  </tr>
  <tr>
    <td width="26%"><div class="tabletext"><font color='<%=fontColor%>'>Address</font></div></td>
    <td width="74%">
      <input type="text" name="CUSTOMER_ADDRESS1" value="<%=UtilFormatOut.checkNull(request.getParameter("CUSTOMER_ADDRESS1"))%>" size="30" maxlength="30">
    *</td>
  </tr>
  <tr>
    <td width="26%"><div class="tabletext"><font color='<%=fontColor%>'>Address 2</font></div></td>
    <td width="74%">
        <input type="text" name="CUSTOMER_ADDRESS2" value="<%=UtilFormatOut.checkNull(request.getParameter("CUSTOMER_ADDRESS2"))%>" size="30" maxlength="30">
    </td>
  </tr>
  <tr>
    <td width="26%"><div class="tabletext"><font color='<%=fontColor%>'>City</font></div></td>
    <td width="74%">
        <input type="text" name="CUSTOMER_CITY" value="<%=UtilFormatOut.checkNull(request.getParameter("CUSTOMER_CITY"))%>" size="30" maxlength="30">
    * </td>
  </tr>
  <tr>
    <td width="26%"><div class="tabletext"><font color='<%=fontColor%>'>State/Province</font></div></td>
    <td width="74%">
      <select name="CUSTOMER_STATE">
          <option><%=UtilFormatOut.checkNull(request.getParameter("CUSTOMER_STATE"))%></option>
          <%@ include file="/includes/states.jsp" %>
      </select>
      * </td>
  </tr>
  <tr>
    <td width="26%"><div class="tabletext"><font color='<%=fontColor%>'>Zip/Postal Code</font></div></td>
    <td width="74%">
        <input type="text" name="CUSTOMER_POSTAL_CODE" value="<%=UtilFormatOut.checkNull(request.getParameter("CUSTOMER_POSTAL_CODE"))%>" size="12" maxlength="10">
    * </td>
  </tr>
  <tr>
      <td width="26%"><div class="tabletext"><font color='<%=fontColor%>'>Country</font></div></td>
      <td width="74%">
          <select name="CUSTOMER_COUNTRY" >
            <%@ include file="/includes/countries.jsp" %>
          </select>
      * </td>
  </tr>
  <tr>
    <td width="26%"><div class="tabletext"><font color='<%=fontColor%>'>Home phone </font></div></td>
    <td width="74%">
        <input type="text" name="CUSTOMER_HOME_PHONE" value="<%=UtilFormatOut.checkNull(request.getParameter("CUSTOMER_HOME_PHONE"))%>" size="30" maxlength="15">
    </td>
  </tr>
  <tr>
    <td width="26%"><div class="tabletext"><font color='<%=fontColor%>'>Business phone</font></div></td>
    <td width="74%">
        <input type="text" name="CUSTOMER_WORK_PHONE" value="<%=UtilFormatOut.checkNull(request.getParameter("CUSTOMER_WORK_PHONE"))%>" size="30" maxlength="15">
    </td>
  </tr>
  <tr>
    <td width="26%"><div class="tabletext"><font color='<%=fontColor%>'>Fax number</font></div></td>
    <td width="74%">
        <input type="text" name="CUSTOMER_FAX_PHONE" value="<%=UtilFormatOut.checkNull(request.getParameter("CUSTOMER_FAX_PHONE"))%>" size="30" maxlength="15">
    </td>
  </tr>
  <tr>
    <td width="26%"><div class="tabletext"><font color='<%=fontColor%>'>Mobile phone</font></div></td>
    <td width="74%">
        <input type="text" name="CUSTOMER_MOBILE_PHONE" value="<%=UtilFormatOut.checkNull(request.getParameter("CUSTOMER_MOBILE_PHONE"))%>" size="30" maxlength="15">
    </td>
  </tr>
  <tr>
    <td width="26%"><div class="tabletext"><font color='<%=fontColor%>'>Email address</font></div></td>
    <td width="74%">
        <input type="text" name="CUSTOMER_EMAIL" value="<%=UtilFormatOut.checkNull(request.getParameter("CUSTOMER_EMAIL"))%>" size="40" maxlength="80">
    * </td>
  </tr>
  <tr>
    <td width="26%">
        <div class="tabletext"><font color='<%=fontColor%>'>Order Email addresses (comma separated)</font></div>
    </td>
    <td width="74%">
        <input type="text" name="CUSTOMER_ORDER_EMAIL" value="<%=UtilFormatOut.checkNull(request.getParameter("CUSTOMER_ORDER_EMAIL"))%>" size="40" maxlength="80">
    </td>
  </tr>
</table>
<br>
<table width="100%" border="0" bgcolor="#678475" cellpadding="2" cellspacing="0">
<tr bgcolor="#678475">
    <td bgcolor="#678475" valign="middle" align="left">
      <p class="head2"><font color="white">&nbsp;Username and Password</font>
    </td>
    <td bgcolor="#678475">
      &nbsp;
    </td>
</tr>
</table>

<p>&nbsp;</p>

<table width="100%" border="0" cellpadding="2" cellspacing="0">
  <tr>
    <td width="27%"><div class="tabletext"><font color='<%=fontColor%>'>Username</font></div></td>
    <td width="73%">
        <input type="text" name="USERNAME" value="<%=UtilFormatOut.checkNull(request.getParameter("USERNAME"))%>" size="20" maxlength="50">
    * </td>
  </tr>
  <% if(UtilProperties.propertyValueEqualsIgnoreCase("ecommerce", "create.allow.password", "true")) { %>
    <tr>
      <td width="27%">
          <div class="tabletext"><font color='<%=fontColor%>'>Password</font></div>
      </td>
      <td width="73%">
          <input type="password" name="PASSWORD" value="" size="20" maxlength="50">
        * </td>
    </tr>
    <tr>
      <td width="27%">
          <div class="tabletext"><font color='<%=fontColor%>'>Repeat password to confirm</font></div>
      </td>
      <td width="73%">
          <input type="password" name="CONFIRM_PASSWORD" value="" size="20" maxlength="50">
      * </td>
    </tr>
  <% } else { %>
    <tr>
      <td width="27%">
          <div class="tabletext"><font color='<%=fontColor%>'>Password</font></div>
      </td>
      <td>
         <div class="commentary">You will receive a password by email when your new account is approved.</div>
      </td>
    </tr>
  <% } %>
  <tr><td colspan="2"><div class="commentary"><br>Fields marked with (*) are required.</div></td></tr>
</table>
</form>

<a href="/login" class="buttonlink">&nbsp;&nbsp;[Back]</a>
<a href="javascript:document.newuserform.submit()" class="buttonlink">&nbsp;&nbsp;[Save]</a>
<br>
<br>
<%@ include file="/includes/onecolumnclose.jsp" %>
<%@ include file="/includes/footer.jsp" %>
