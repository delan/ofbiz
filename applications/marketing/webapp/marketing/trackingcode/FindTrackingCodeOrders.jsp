<%--
 *  Copyright (c) 2002 The Open For Business Project - www.ofbiz.org
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
 *@created    October 19, 2002
 *@version    1.0
--%>

<%@ page import="java.util.*, java.io.*" %>
<%@ page import="org.ofbiz.base.util.*, org.ofbiz.entity.*" %>

<%@ taglib uri="ofbizTags" prefix="ofbiz" %>
<jsp:useBean id="delegator" type="org.ofbiz.entity.GenericDelegator" scope="request" />
<jsp:useBean id="security" type="org.ofbiz.security.Security" scope="request" />

<%if (security.hasEntityPermission("MARKETING", "_VIEW", session)) {%>
<%
    String trackingCodeId = request.getParameter("trackingCodeId");
    GenericValue trackingCode = delegator.findByPrimaryKey("TrackingCode", UtilMisc.toMap("trackingCodeId", trackingCodeId));
    Collection trackingCodeOrders = delegator.findByAnd("TrackingCodeOrder", 
            UtilMisc.toMap("trackingCodeId", trackingCodeId), UtilMisc.toList("orderId"));
    if (trackingCodeOrders != null) pageContext.setAttribute("trackingCodeOrders", trackingCodeOrders);

    int viewIndex = 0;
    int viewSize = 20;
    int highIndex = 0;
    int lowIndex = 0;
    int listSize = 0;

    try {
        viewIndex = Integer.valueOf((String) pageContext.getRequest().getParameter("VIEW_INDEX")).intValue();
    } catch (Exception e) {
        viewIndex = 0;
    }
    try {
        viewSize = Integer.valueOf((String) pageContext.getRequest().getParameter("VIEW_SIZE")).intValue();
    } catch (Exception e) {
        viewSize = 20;
    }
    if (trackingCodeOrders != null) {
        listSize = trackingCodeOrders.size();
    }
    lowIndex = viewIndex * viewSize;
    highIndex = (viewIndex + 1) * viewSize;
    if (listSize < highIndex) {
        highIndex = listSize;
    }
%>

<%if(trackingCodeId != null && trackingCodeId.length() > 0){%>
  <div class='tabContainer'>
  <a href="<ofbiz:url>/EditTrackingCode?trackingCodeId=<%=trackingCodeId%></ofbiz:url>" class="tabButton">TrackingCode</a>
  <a href="<ofbiz:url>/FindTrackingCodeOrders?trackingCodeId=<%=trackingCodeId%></ofbiz:url>" class="tabButtonSelected">Orders</a>
  <a href="<ofbiz:url>/FindTrackingCodeVisits?trackingCodeId=<%=trackingCodeId%></ofbiz:url>" class="tabButton">Visits</a>
  </div>
<%}%>

<div class="head1">Orders <span class='head2'>for TrackingCode <%=UtilFormatOut.ifNotEmpty(trackingCode==null?null:trackingCode.getString("description"),"\"","\"")%> [ID:<%=UtilFormatOut.checkNull(trackingCodeId)%>]</span></div>

<a href="<ofbiz:url>/EditTrackingCode</ofbiz:url>" class="buttontext">[New TrackingCode]</a>

<ofbiz:if name="trackingCodeOrders" size="0">
  <table border="0" width="100%" cellpadding="2">
    <tr>
      <td align=right>
        <b>
        <%if (viewIndex > 0) {%>
          <a href="<ofbiz:url><%="/FindTrackingCodeOrders?trackingCodeId=" + trackingCodeId + "&VIEW_SIZE=" + viewSize + "&VIEW_INDEX=" + (viewIndex-1)%></ofbiz:url>" class="buttontext">[Previous]</a> |
        <%}%>
        <%if (listSize > 0) {%>
          <%=lowIndex+1%> - <%=highIndex%> of <%=listSize%>
        <%}%>
        <%if (listSize > highIndex) {%>
          | <a href="<ofbiz:url><%="/FindTrackingCodeOrders?trackingCodeId=" + trackingCodeId + "&VIEW_SIZE=" + viewSize + "&VIEW_INDEX=" + (viewIndex+1)%></ofbiz:url>" class="buttontext">[Next]</a>
        <%}%>
        </b>
      </td>
    </tr>
  </table>
</ofbiz:if>
<%if (trackingCodeId != null){%>
<table border="1" cellpadding='2' cellspacing='0' width='100%'>
  <tr>
    <td><div class="tabletext"><b>Order&nbsp;ID</b></div></td>
    <td><div class="tabletext"><b>Order&nbsp;Date</b></div></td>
    <td><div class="tabletext"><b>TrackingCode&nbsp;Type</b></div></td>
    <td><div class="tabletext"><b>Is&nbsp;Billable</b></div></td>
  </tr>
<ofbiz:iterator name="trackingCodeOrder" property="trackingCodeOrders" offset="<%=lowIndex%>" limit="<%=viewSize%>">
  <%GenericValue curTrackingCodeType = trackingCodeOrder.getRelatedOneCache("TrackingCodeType");%>
  <%if (curTrackingCodeType != null) pageContext.setAttribute("curTrackingCodeType", curTrackingCodeType);%>
  <%GenericValue orderHeader = trackingCodeOrder.getRelatedOne("OrderHeader");%>
  <%if (orderHeader != null) pageContext.setAttribute("orderHeader", orderHeader);%>
  <tr valign="middle">
    <td><div class='tabletext'>&nbsp;<a href='/ordermgr/control/orderview?order_id=<ofbiz:entityfield attribute="trackingCodeOrder" field="orderId"/>' target="ordermgr" class='buttontext'><ofbiz:inputvalue entityAttr="trackingCodeOrder" field="orderId"/></a></div></td>
    <td><div class='tabletext'>&nbsp;<ofbiz:inputvalue entityAttr="orderHeader" field="orderDate"/></div></td>
    <td><div class='tabletext'>&nbsp;<ofbiz:inputvalue entityAttr="curTrackingCodeType" field="description"/></div></td>
    <td><div class='tabletext'>&nbsp;<ofbiz:inputvalue entityAttr="trackingCodeOrder" field="isBillable"/></div></td>
  </tr>
</ofbiz:iterator>
</table>
<ofbiz:if name="trackingCodeOrders" size="0">
  <table border="0" width="100%" cellpadding="2">
    <tr>
      <td align=right>
        <b>
        <%if (viewIndex > 0) {%>
          <a href="<ofbiz:url><%="/FindTrackingCodeOrders?trackingCodeId=" + trackingCodeId + "&VIEW_SIZE=" + viewSize + "&VIEW_INDEX=" + (viewIndex-1)%></ofbiz:url>" class="buttontext">[Previous]</a> |
        <%}%>
        <%if (listSize > 0) {%>
          <%=lowIndex+1%> - <%=highIndex%> of <%=listSize%>
        <%}%>
        <%if (listSize > highIndex) {%>
          | <a href="<ofbiz:url><%="/FindTrackingCodeOrders?trackingCodeId=" + trackingCodeId + "&VIEW_SIZE=" + viewSize + "&VIEW_INDEX=" + (viewIndex+1)%></ofbiz:url>" class="buttontext">[Next]</a>
        <%}%>
        </b>
      </td>
    </tr>
  </table>
</ofbiz:if>
<br>
<%}%>
<br>

<%}else{%>
  <h3>You do not have permission to view this page. ("MARKETING_VIEW" or "MARKETING_ADMIN" needed)</h3>
<%}%>
