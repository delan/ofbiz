
<%
/**
 *  Title: Party Entity
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
 *@created    Wed Jul 04 01:03:15 MDT 2001
 *@version    1.0
 */
%>

<%@ page import="java.util.*" %>
<%@ page import="org.ofbiz.commonapp.common.*" %>
<%@ page import="org.ofbiz.commonapp.webevent.*" %>
<%@ page import="org.ofbiz.commonapp.security.*" %>
<%@ page import="org.ofbiz.commonapp.party.party.*" %>

<%@ page import="org.ofbiz.commonapp.security.login.*" %>

<%@ taglib uri="/WEB-INF/webevent.tld" prefix="webevent" %>
<webevent:dispatch loginRequired="true" />

<%pageContext.setAttribute("PageName", "EditParty"); %>

<%@ include file="/includes/header.jsp" %>
<%@ include file="/includes/onecolumn.jsp" %>

<%boolean hasViewPermission=Security.hasEntityPermission("PARTY", "_VIEW", session);%>
<%boolean hasCreatePermission=Security.hasEntityPermission("PARTY", "_CREATE", session);%>
<%boolean hasUpdatePermission=Security.hasEntityPermission("PARTY", "_UPDATE", session);%>
<%boolean hasDeletePermission=Security.hasEntityPermission("PARTY", "_DELETE", session);%>
<%if(hasViewPermission){%>

<%
  String rowColor1 = "99CCFF";
  String rowColor2 = "CCFFFF";
  String rowColor = "";

  String partyId = request.getParameter("PARTY_PARTY_ID");  

  

  Party party = PartyHelper.findByPrimaryKey(partyId);
%>

<br>
<div style='color:yellow;width:100%;background-color:#330033;padding:3;'>
  <b>View Entity: Party with (PARTY_ID: <%=partyId%>).</b>
</div>

<a href="<%=response.encodeURL("FindParty.jsp")%>" class="buttontext">[Find Party]</a>
<%if(hasCreatePermission){%>
  <a href="<%=response.encodeURL("EditParty.jsp")%>" class="buttontext">[Create Party]</a>
<%}%>
<%if(party != null){%>
  <%if(hasDeletePermission){%>
    <a href="<%=response.encodeURL("EditParty.jsp?WEBEVENT=UPDATE_PARTY&UPDATE_MODE=DELETE&" + "PARTY_PARTY_ID=" + partyId)%>" class="buttontext">[Delete this Party]</a>
  <%}%>
<%}%>
<%if(hasUpdatePermission){%>
  <%if(partyId != null){%>
    <a href="<%=response.encodeURL("EditParty.jsp?" + "PARTY_PARTY_ID=" + partyId)%>" class="buttontext">[Edit Party]</a>
  <%}%>
<%}%>

<table border="0" cellspacing="2" cellpadding="2">
<%if(party == null){%>
<tr bgcolor="<%=rowColor1%>"><td><h3>Specified Party was not found.</h3></td></tr>
<%}else{%>

  <%rowColor=(rowColor==rowColor1?rowColor2:rowColor1);%>
  <tr bgcolor="<%=rowColor%>">
    <td><b>PARTY_ID</b></td>
    <td>
    
      <%=UtilFormatOut.checkNull(party.getPartyId())%>
    
    </td>
  </tr>

<%} //end if party == null %>
</table>

<a href="<%=response.encodeURL("FindParty.jsp")%>" class="buttontext">[Find Party]</a>
<%if(hasCreatePermission){%>
  <a href="<%=response.encodeURL("EditParty.jsp")%>" class="buttontext">[Create Party]</a>
<%}%>
<%if(party != null){%>
  <%if(hasDeletePermission){%>
    <a href="<%=response.encodeURL("EditParty.jsp?WEBEVENT=UPDATE_PARTY&UPDATE_MODE=DELETE&" + "PARTY_PARTY_ID=" + partyId)%>" class="buttontext">[Delete this Party]</a>
  <%}%>
<%}%>
<%if(hasUpdatePermission){%>
  <%if(partyId != null){%>
    <a href="<%=response.encodeURL("EditParty.jsp?" + "PARTY_PARTY_ID=" + partyId)%>" class="buttontext">[Edit Party]</a>
  <%}%>
<%}%>
<br>

  
  
  
<%-- Start Relation for PartyClassification, type: many --%>
<%if(party != null){%>
  <%if(Security.hasEntityPermission("PARTY_CLASSIFICATION", "_VIEW", session)){%>    
    <%Iterator relatedIterator = PartyClassificationHelper.findByPartyIdIterator(party.getPartyId());%>
    <br>
    <div style='color:yellow;width:100%;background-color:#660066;padding:2;'>
      <b></b> Related Entities: <b>PartyClassification</b> with (PARTY_ID: <%=party.getPartyId()%>)
    </div>
    <%boolean relatedCreatePerm = Security.hasEntityPermission("PARTY_CLASSIFICATION", "_CREATE", session);%>
    <%boolean relatedUpdatePerm = Security.hasEntityPermission("PARTY_CLASSIFICATION", "_UPDATE", session);%>
    <%boolean relatedDeletePerm = Security.hasEntityPermission("PARTY_CLASSIFICATION", "_DELETE", session);%>
    <%
      String rowColorResultHeader = "99CCFF";
      String rowColorResult1 = "99FFCC";
      String rowColorResult2 = "CCFFCC"; 
      String rowColorResult = "";
    %>
      
    <%if(relatedCreatePerm){%>
      <a href="<%=response.encodeURL("/commonapp/party/party/EditPartyClassification.jsp?" + "PARTY_CLASSIFICATION_PARTY_ID=" + party.getPartyId())%>" class="buttontext">[Create PartyClassification]</a>
    <%}%>
    
    <%String curFindString = "SEARCH_TYPE=PartyId";%>
    <%curFindString = curFindString + "&SEARCH_PARAMETER1=" + party.getPartyId();%>
    <a href="<%=response.encodeURL("/commonapp/party/party/FindParty.jsp?" + UtilFormatOut.encodeQuery(curFindString))%>" class="buttontext">[Find PartyClassification]</a>

  <table width="100%" cellpadding="2" cellspacing="2" border="0">
    <tr bgcolor="<%=rowColorResultHeader%>">
  
      <td><div class="tabletext"><b><nobr>PARTY_ID</nobr></b></div></td>
      <td><div class="tabletext"><b><nobr>PARTY_TYPE_ID</nobr></b></div></td>
      <td><div class="tabletext"><b><nobr>PARTY_CLASSIFICATION_TYPE_ID</nobr></b></div></td>
      <td><div class="tabletext"><b><nobr>FROM_DATE</nobr></b></div></td>
      <td><div class="tabletext"><b><nobr>THRU_DATE</nobr></b></div></td>
      <td>&nbsp;</td>
      <%if(relatedUpdatePerm){%>
        <td>&nbsp;</td>
      <%}%>
      <%if(relatedDeletePerm){%>
        <td>&nbsp;</td>
      <%}%>
    </tr>
    <%
     if(relatedIterator != null && relatedIterator.hasNext())
     {
      int relatedLoopCount = 0;
      while(relatedIterator != null && relatedIterator.hasNext())
      {
        relatedLoopCount++; if(relatedLoopCount > 10) break;
        PartyClassification partyClassificationRelated = (PartyClassification)relatedIterator.next();
        if(partyClassificationRelated != null)
        {
    %>
    <%rowColorResult=(rowColorResult==rowColorResult1?rowColorResult2:rowColorResult1);%><tr bgcolor="<%=rowColorResult%>">
  
      <td>
        <div class="tabletext">
    
      <%=UtilFormatOut.checkNull(partyClassificationRelated.getPartyId())%>
    
        &nbsp;</div>
      </td>
  
      <td>
        <div class="tabletext">
    
      <%=UtilFormatOut.checkNull(partyClassificationRelated.getPartyTypeId())%>
    
        &nbsp;</div>
      </td>
  
      <td>
        <div class="tabletext">
    
      <%=UtilFormatOut.checkNull(partyClassificationRelated.getPartyClassificationTypeId())%>
    
        &nbsp;</div>
      </td>
  
      <td>
        <div class="tabletext">
    
      <%{
        String dateString = null;
        String timeString = null;
        if(partyClassificationRelated != null)
        {
          java.util.Date date = partyClassificationRelated.getFromDate();
          if(date  != null)
          {
            dateString = UtilDateTime.toDateString(date);
            timeString = UtilDateTime.toTimeString(date);
          }
        }
      %>
      <%=UtilFormatOut.checkNull(dateString)%>&nbsp;<%=UtilFormatOut.checkNull(timeString)%>
      <%}%>
    
        &nbsp;</div>
      </td>
  
      <td>
        <div class="tabletext">
    
      <%{
        String dateString = null;
        String timeString = null;
        if(partyClassificationRelated != null)
        {
          java.util.Date date = partyClassificationRelated.getThruDate();
          if(date  != null)
          {
            dateString = UtilDateTime.toDateString(date);
            timeString = UtilDateTime.toTimeString(date);
          }
        }
      %>
      <%=UtilFormatOut.checkNull(dateString)%>&nbsp;<%=UtilFormatOut.checkNull(timeString)%>
      <%}%>
    
        &nbsp;</div>
      </td>
  
      <td>
        <a href="<%=response.encodeURL("/commonapp/party/party/ViewPartyClassification.jsp?" + "PARTY_CLASSIFICATION_PARTY_ID=" + partyClassificationRelated.getPartyId() + "&" + "PARTY_CLASSIFICATION_PARTY_TYPE_ID=" + partyClassificationRelated.getPartyTypeId())%>" class="buttontext">[View]</a>
      </td>
      <%if(relatedUpdatePerm){%>
        <td>
          <a href="<%=response.encodeURL("/commonapp/party/party/EditPartyClassification.jsp?" + "PARTY_CLASSIFICATION_PARTY_ID=" + partyClassificationRelated.getPartyId() + "&" + "PARTY_CLASSIFICATION_PARTY_TYPE_ID=" + partyClassificationRelated.getPartyTypeId())%>" class="buttontext">[Edit]</a>
        </td>
      <%}%>
      <%if(relatedDeletePerm){%>
        <td>
          <%-- <a href="<%=response.encodeURL("ViewPersonSecurityGroup.jsp?" + "PERSON_SECURITY_GROUP_USERNAME=" + username + "&" + "PERSON_SECURITY_GROUP_GROUP_ID=" + groupId + "&" + "WEBEVENT=UPDATE_SECURITY_GROUP_PERMISSION&UPDATE_MODE=DELETE&" + "SECURITY_GROUP_PERMISSION_GROUP_ID=" + securityGroupPermission.getGroupId() + "&" + "SECURITY_GROUP_PERMISSION_PERMISSION_ID=" + securityGroupPermission.getPermissionId())%>" class="buttontext">[Delete]</a> --%>
          <a href="<%=response.encodeURL("ViewPartyClassification.jsp?" + "PARTY_CLASSIFICATION_PARTY_ID=" + partyClassificationRelated.getPartyId() + "&" + "PARTY_CLASSIFICATION_PARTY_TYPE_ID=" + partyClassificationRelated.getPartyTypeId() + "&" + "PARTY_PARTY_ID=" + partyId + "&WEBEVENT=UPDATE_PARTY_CLASSIFICATION&UPDATE_MODE=DELETE")%>" class="buttontext">[Delete]</a>
        </td>
      <%}%>
    </tr>
    <%}%>
  <%}%>
<%}else{%>
<%rowColorResult=(rowColorResult==rowColorResult1?rowColorResult2:rowColorResult1);%><tr bgcolor="<%=rowColorResult%>">
<td colspan="8">
<h3>No PartyClassifications Found.</h3>
</td>
</tr>
<%}%>
</table>
  <%}%>
<%}%>
<%-- End Relation for PartyClassification, type: many --%>
  

  
  
  
<%-- Start Relation for PartyAttribute, type: many --%>
<%if(party != null){%>
  <%if(Security.hasEntityPermission("PARTY_ATTRIBUTE", "_VIEW", session)){%>    
    <%Iterator relatedIterator = PartyAttributeHelper.findByPartyIdIterator(party.getPartyId());%>
    <br>
    <div style='color:yellow;width:100%;background-color:#660066;padding:2;'>
      <b></b> Related Entities: <b>PartyAttribute</b> with (PARTY_ID: <%=party.getPartyId()%>)
    </div>
    <%boolean relatedCreatePerm = Security.hasEntityPermission("PARTY_ATTRIBUTE", "_CREATE", session);%>
    <%boolean relatedUpdatePerm = Security.hasEntityPermission("PARTY_ATTRIBUTE", "_UPDATE", session);%>
    <%boolean relatedDeletePerm = Security.hasEntityPermission("PARTY_ATTRIBUTE", "_DELETE", session);%>
    <%
      String rowColorResultHeader = "99CCFF";
      String rowColorResult1 = "99FFCC";
      String rowColorResult2 = "CCFFCC"; 
      String rowColorResult = "";
    %>
      
    <%if(relatedCreatePerm){%>
      <a href="<%=response.encodeURL("/commonapp/party/party/EditPartyAttribute.jsp?" + "PARTY_ATTRIBUTE_PARTY_ID=" + party.getPartyId())%>" class="buttontext">[Create PartyAttribute]</a>
    <%}%>
    
    <%String curFindString = "SEARCH_TYPE=PartyId";%>
    <%curFindString = curFindString + "&SEARCH_PARAMETER1=" + party.getPartyId();%>
    <a href="<%=response.encodeURL("/commonapp/party/party/FindParty.jsp?" + UtilFormatOut.encodeQuery(curFindString))%>" class="buttontext">[Find PartyAttribute]</a>

  <table width="100%" cellpadding="2" cellspacing="2" border="0">
    <tr bgcolor="<%=rowColorResultHeader%>">
  
      <td><div class="tabletext"><b><nobr>PARTY_ID</nobr></b></div></td>
      <td><div class="tabletext"><b><nobr>NAME</nobr></b></div></td>
      <td><div class="tabletext"><b><nobr>VALUE</nobr></b></div></td>
      <td>&nbsp;</td>
      <%if(relatedUpdatePerm){%>
        <td>&nbsp;</td>
      <%}%>
      <%if(relatedDeletePerm){%>
        <td>&nbsp;</td>
      <%}%>
    </tr>
    <%
     if(relatedIterator != null && relatedIterator.hasNext())
     {
      int relatedLoopCount = 0;
      while(relatedIterator != null && relatedIterator.hasNext())
      {
        relatedLoopCount++; if(relatedLoopCount > 10) break;
        PartyAttribute partyAttributeRelated = (PartyAttribute)relatedIterator.next();
        if(partyAttributeRelated != null)
        {
    %>
    <%rowColorResult=(rowColorResult==rowColorResult1?rowColorResult2:rowColorResult1);%><tr bgcolor="<%=rowColorResult%>">
  
      <td>
        <div class="tabletext">
    
      <%=UtilFormatOut.checkNull(partyAttributeRelated.getPartyId())%>
    
        &nbsp;</div>
      </td>
  
      <td>
        <div class="tabletext">
    
      <%=UtilFormatOut.checkNull(partyAttributeRelated.getName())%>
    
        &nbsp;</div>
      </td>
  
      <td>
        <div class="tabletext">
    
      <%=UtilFormatOut.checkNull(partyAttributeRelated.getValue())%>
    
        &nbsp;</div>
      </td>
  
      <td>
        <a href="<%=response.encodeURL("/commonapp/party/party/ViewPartyAttribute.jsp?" + "PARTY_ATTRIBUTE_PARTY_ID=" + partyAttributeRelated.getPartyId() + "&" + "PARTY_ATTRIBUTE_NAME=" + partyAttributeRelated.getName())%>" class="buttontext">[View]</a>
      </td>
      <%if(relatedUpdatePerm){%>
        <td>
          <a href="<%=response.encodeURL("/commonapp/party/party/EditPartyAttribute.jsp?" + "PARTY_ATTRIBUTE_PARTY_ID=" + partyAttributeRelated.getPartyId() + "&" + "PARTY_ATTRIBUTE_NAME=" + partyAttributeRelated.getName())%>" class="buttontext">[Edit]</a>
        </td>
      <%}%>
      <%if(relatedDeletePerm){%>
        <td>
          <%-- <a href="<%=response.encodeURL("ViewPersonSecurityGroup.jsp?" + "PERSON_SECURITY_GROUP_USERNAME=" + username + "&" + "PERSON_SECURITY_GROUP_GROUP_ID=" + groupId + "&" + "WEBEVENT=UPDATE_SECURITY_GROUP_PERMISSION&UPDATE_MODE=DELETE&" + "SECURITY_GROUP_PERMISSION_GROUP_ID=" + securityGroupPermission.getGroupId() + "&" + "SECURITY_GROUP_PERMISSION_PERMISSION_ID=" + securityGroupPermission.getPermissionId())%>" class="buttontext">[Delete]</a> --%>
          <a href="<%=response.encodeURL("ViewPartyAttribute.jsp?" + "PARTY_ATTRIBUTE_PARTY_ID=" + partyAttributeRelated.getPartyId() + "&" + "PARTY_ATTRIBUTE_NAME=" + partyAttributeRelated.getName() + "&" + "PARTY_PARTY_ID=" + partyId + "&WEBEVENT=UPDATE_PARTY_ATTRIBUTE&UPDATE_MODE=DELETE")%>" class="buttontext">[Delete]</a>
        </td>
      <%}%>
    </tr>
    <%}%>
  <%}%>
<%}else{%>
<%rowColorResult=(rowColorResult==rowColorResult1?rowColorResult2:rowColorResult1);%><tr bgcolor="<%=rowColorResult%>">
<td colspan="8">
<h3>No PartyAttributes Found.</h3>
</td>
</tr>
<%}%>
</table>
  <%}%>
<%}%>
<%-- End Relation for PartyAttribute, type: many --%>
  

  
  
  
<%-- Start Relation for UserLogin, type: many --%>
<%if(party != null){%>
  <%if(Security.hasEntityPermission("USER_LOGIN", "_VIEW", session)){%>    
    <%Iterator relatedIterator = UserLoginHelper.findByPartyIdIterator(party.getPartyId());%>
    <br>
    <div style='color:yellow;width:100%;background-color:#660066;padding:2;'>
      <b></b> Related Entities: <b>UserLogin</b> with (PARTY_ID: <%=party.getPartyId()%>)
    </div>
    <%boolean relatedCreatePerm = Security.hasEntityPermission("USER_LOGIN", "_CREATE", session);%>
    <%boolean relatedUpdatePerm = Security.hasEntityPermission("USER_LOGIN", "_UPDATE", session);%>
    <%boolean relatedDeletePerm = Security.hasEntityPermission("USER_LOGIN", "_DELETE", session);%>
    <%
      String rowColorResultHeader = "99CCFF";
      String rowColorResult1 = "99FFCC";
      String rowColorResult2 = "CCFFCC"; 
      String rowColorResult = "";
    %>
      
    <%if(relatedCreatePerm){%>
      <a href="<%=response.encodeURL("/commonapp/security/login/EditUserLogin.jsp?" + "USER_LOGIN_PARTY_ID=" + party.getPartyId())%>" class="buttontext">[Create UserLogin]</a>
    <%}%>
    
    <%String curFindString = "SEARCH_TYPE=PartyId";%>
    <%curFindString = curFindString + "&SEARCH_PARAMETER1=" + party.getPartyId();%>
    <a href="<%=response.encodeURL("/commonapp/security/login/FindParty.jsp?" + UtilFormatOut.encodeQuery(curFindString))%>" class="buttontext">[Find UserLogin]</a>

  <table width="100%" cellpadding="2" cellspacing="2" border="0">
    <tr bgcolor="<%=rowColorResultHeader%>">
  
      <td><div class="tabletext"><b><nobr>USER_LOGIN_ID</nobr></b></div></td>
      <td><div class="tabletext"><b><nobr>PARTY_ID</nobr></b></div></td>
      <td><div class="tabletext"><b><nobr>CONTACT_MECHANISM_ID</nobr></b></div></td>
      <td><div class="tabletext"><b><nobr>CURRENT_USER_ID</nobr></b></div></td>
      <td><div class="tabletext"><b><nobr>CURRENT_PASSWORD</nobr></b></div></td>
      <td>&nbsp;</td>
      <%if(relatedUpdatePerm){%>
        <td>&nbsp;</td>
      <%}%>
      <%if(relatedDeletePerm){%>
        <td>&nbsp;</td>
      <%}%>
    </tr>
    <%
     if(relatedIterator != null && relatedIterator.hasNext())
     {
      int relatedLoopCount = 0;
      while(relatedIterator != null && relatedIterator.hasNext())
      {
        relatedLoopCount++; if(relatedLoopCount > 10) break;
        UserLogin userLoginRelated = (UserLogin)relatedIterator.next();
        if(userLoginRelated != null)
        {
    %>
    <%rowColorResult=(rowColorResult==rowColorResult1?rowColorResult2:rowColorResult1);%><tr bgcolor="<%=rowColorResult%>">
  
      <td>
        <div class="tabletext">
    
      <%=UtilFormatOut.checkNull(userLoginRelated.getUserLoginId())%>
    
        &nbsp;</div>
      </td>
  
      <td>
        <div class="tabletext">
    
      <%=UtilFormatOut.checkNull(userLoginRelated.getPartyId())%>
    
        &nbsp;</div>
      </td>
  
      <td>
        <div class="tabletext">
    
      <%=UtilFormatOut.checkNull(userLoginRelated.getContactMechanismId())%>
    
        &nbsp;</div>
      </td>
  
      <td>
        <div class="tabletext">
    
      <%=UtilFormatOut.checkNull(userLoginRelated.getCurrentUserId())%>
    
        &nbsp;</div>
      </td>
  
      <td>
        <div class="tabletext">
    
      <%=UtilFormatOut.checkNull(userLoginRelated.getCurrentPassword())%>
    
        &nbsp;</div>
      </td>
  
      <td>
        <a href="<%=response.encodeURL("/commonapp/security/login/ViewUserLogin.jsp?" + "USER_LOGIN_USER_LOGIN_ID=" + userLoginRelated.getUserLoginId())%>" class="buttontext">[View]</a>
      </td>
      <%if(relatedUpdatePerm){%>
        <td>
          <a href="<%=response.encodeURL("/commonapp/security/login/EditUserLogin.jsp?" + "USER_LOGIN_USER_LOGIN_ID=" + userLoginRelated.getUserLoginId())%>" class="buttontext">[Edit]</a>
        </td>
      <%}%>
      <%if(relatedDeletePerm){%>
        <td>
          <%-- <a href="<%=response.encodeURL("ViewPersonSecurityGroup.jsp?" + "PERSON_SECURITY_GROUP_USERNAME=" + username + "&" + "PERSON_SECURITY_GROUP_GROUP_ID=" + groupId + "&" + "WEBEVENT=UPDATE_SECURITY_GROUP_PERMISSION&UPDATE_MODE=DELETE&" + "SECURITY_GROUP_PERMISSION_GROUP_ID=" + securityGroupPermission.getGroupId() + "&" + "SECURITY_GROUP_PERMISSION_PERMISSION_ID=" + securityGroupPermission.getPermissionId())%>" class="buttontext">[Delete]</a> --%>
          <a href="<%=response.encodeURL("ViewUserLogin.jsp?" + "USER_LOGIN_USER_LOGIN_ID=" + userLoginRelated.getUserLoginId() + "&" + "PARTY_PARTY_ID=" + partyId + "&WEBEVENT=UPDATE_USER_LOGIN&UPDATE_MODE=DELETE")%>" class="buttontext">[Delete]</a>
        </td>
      <%}%>
    </tr>
    <%}%>
  <%}%>
<%}else{%>
<%rowColorResult=(rowColorResult==rowColorResult1?rowColorResult2:rowColorResult1);%><tr bgcolor="<%=rowColorResult%>">
<td colspan="8">
<h3>No UserLogins Found.</h3>
</td>
</tr>
<%}%>
</table>
  <%}%>
<%}%>
<%-- End Relation for UserLogin, type: many --%>
  


<br>
<%}else{%>
  <h3>You do not have permission to view this page (PARTY_ADMIN, or PARTY_VIEW needed).</h3>
<%}%>

<%@ include file="/includes/onecolumnclose.jsp" %>
<%@ include file="/includes/footer.jsp" %>
