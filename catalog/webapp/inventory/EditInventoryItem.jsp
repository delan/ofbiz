<%
/**
 *  Title: Edit Inventory Item Page
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
 *@created    Sep 10 2001
 *@version    1.0
 */
%>
<%try {%>
<%@ page import="java.util.*, java.io.*" %>
<%@ page import="org.ofbiz.core.util.*, org.ofbiz.core.entity.*" %>

<%@ taglib uri="ofbizTags" prefix="ofbiz" %>
<jsp:useBean id="delegator" type="org.ofbiz.core.entity.GenericDelegator" scope="request" />
<jsp:useBean id="security" type="org.ofbiz.core.security.Security" scope="request" />

<table cellpadding=0 cellspacing=0 border=0 width="100%"><tr><td>&nbsp;&nbsp;</td><td>

<%if(security.hasEntityPermission("CATALOG", "_VIEW", session)) {%>
<%
    boolean tryEntity = true;
    if(request.getAttribute(SiteDefs.ERROR_MESSAGE) != null) tryEntity = false;

    String inventoryItemId = request.getParameter("inventoryItemId");
    GenericValue inventoryItem = delegator.findByPrimaryKey("InventoryItem", UtilMisc.toMap("inventoryItemId", inventoryItemId));
    GenericValue inventoryItemType = null;
    if(inventoryItem == null) {
        tryEntity = false;
    } else {
        inventoryItemType = inventoryItem.getRelatedOne("InventoryItemType");

        //statuses
        if ("NON_SERIAL_INV_ITEM".equals(inventoryItem.getString("inventoryItemTypeId"))) {
            //do nothing for non-serialized inventory
        } else if ("SERIALIZED_INV_ITEM".equals(inventoryItem.getString("inventoryItemTypeId"))) {
            if (UtilValidate.isNotEmpty(inventoryItem.getString("statusId"))) {
                Collection statusChange = delegator.findByAnd("StatusValidChange",UtilMisc.toMap("statusId",inventoryItem.getString("statusId")));
                if (statusChange != null) {
                    Collection statusItems = null;
                    Iterator statusChangeIter = statusChange.iterator();
                    while (statusChangeIter.hasNext()) {
                        GenericValue curStatusChange = (GenericValue) statusChangeIter.next();
                        GenericValue curStatusItem = delegator.findByPrimaryKey("StatusItem", UtilMisc.toMap("statusId", curStatusChange.get("statusIdTo")));
                        if (curStatusItem != null) statusItems.add(curStatusItem);
                    }
                    pageContext.setAttribute("statusItems", statusItems);
                }
            } else {
                //no status id, just get all statusItems
                Collection statusItems = delegator.findByAnd("StatusItem", UtilMisc.toMap("statusTypeId", "INV_SERIALIZED_STTS"));
                if (statusItems != null) pageContext.setAttribute("statusItems", statusItems);
            }
        }
    }

    //inv item types
    Collection inventoryItemTypes = delegator.findAll("InventoryItemType");
    if (inventoryItemTypes != null) pageContext.setAttribute("inventoryItemTypes", inventoryItemTypes);

    //facilities
    Collection facilities = delegator.findAll("Facility");
    if (facilities != null) pageContext.setAttribute("facilities", facilities);
%>

<br>
<a href="<ofbiz:url>/EditInventoryItem</ofbiz:url>" class="buttontext">[New InventoryItem]</a>
<%if(inventoryItemId != null && inventoryItemId.length() > 0){%>
  <a href="<ofbiz:url>/EditInventoryItem?inventoryItemId=<%=inventoryItemId%></ofbiz:url>" class="buttontextdisabled">[InventoryItem]</a>
<%}%>

<div class="head1">Edit InventoryItem with ID "<%=UtilFormatOut.checkNull(inventoryItemId)%>"</div>

<form action="<ofbiz:url>/UpdateInventoryItem</ofbiz:url>" method=POST style='margin: 0;'>
<table border='0' cellpadding='2' cellspacing='0'>

<%if(inventoryItem == null){%>
  <%if(inventoryItemId != null){%>
    <h3>Could not find inventoryItem with ID "<%=inventoryItemId%>".</h3>
    <input type=hidden name="UPDATE_MODE" value="CREATE">
    <tr>
      <td align=right><div class="tabletext">InventoryItem ID</div></td>
      <td>&nbsp;</td>
      <td>
        <input type="text" name="inventoryItemId" size="20" maxlength="20" value="<%=inventoryItemId%>">
      </td>
    </tr>
  <%}else{%>
    <input type=hidden name="UPDATE_MODE" value="CREATE">
    <tr>
      <td align=right><div class="tabletext">InventoryItem ID</div></td>
      <td>&nbsp;</td>
      <td>
        <input type="text" name="inventoryItemId" size="20" maxlength="20" value="">
      </td>
    </tr>
  <%}%>
<%}else{%>
  <input type=hidden name="UPDATE_MODE" value="UPDATE">
  <input type=hidden name="inventoryItemId" value="<%=inventoryItemId%>">
  <tr>
    <td align=right><div class="tabletext">InventoryItem ID</div></td>
    <td>&nbsp;</td>
    <td>
      <b><%=inventoryItemId%></b> (This cannot be changed without re-creating the inventoryItem.)
    </td>
  </tr>
<%}%>
      <tr>
        <td width="26%" align=right><div class="tabletext">InventoryItem Type Id</div></td>
        <td>&nbsp;</td>
        <td width="74%">
          <%-- <input type="text" name="<%=paramName%>" value="<%=UtilFormatOut.checkNull(tryEntity?inventoryItem.getString(fieldName):request.getParameter(paramName))%>" size="20" maxlength="20"> --%>
          <select name="inventoryItemTypeId" size=1>
            <%if(inventoryItemType != null) {%>
              <option selected value='<%=inventoryItemType.getString("inventoryItemTypeId")%>'><%=inventoryItemType.getString("description")%> [<%=inventoryItemType.getString("inventoryItemTypeId")%>]</option>
              <option value='<%=inventoryItemType.getString("inventoryItemTypeId")%>'>&nbsp;</option>
            <%} else {%>
              <option value=''>----</option>
            <%}%>
            <ofbiz:iterator name="nextInventoryItemType" property="inventoryItemTypes">
              <option value='<%=nextInventoryItemType.getString("inventoryItemTypeId")%>'><%=nextInventoryItemType.getString("description")%> [<%=nextInventoryItemType.getString("inventoryItemTypeId")%>]</option>
            </ofbiz:iterator>
          </select>
        </td>
      </tr>
      <tr>
        <td width="26%" align=right><div class="tabletext">Product Id</div></td>
        <td>&nbsp;</td>
        <td width="74%">
            <input type="text" <ofbiz:inputvalue entityAttr="inventoryItem" field="productId" fullattrs="true"/> size="20" maxlength="20">
            <%if (inventoryItem != null && UtilValidate.isNotEmpty(inventoryItem.getString("productId"))) {%>
                <a href='/catalog/control/EditProduct?PRODUCT_ID=<ofbiz:inputvalue entityAttr="inventoryItem" field="productId"/>' class='buttontext'>[Edit&nbsp;Product&nbsp;<ofbiz:inputvalue entityAttr="inventoryItem" field="productId"/>]</a>
            <%}%>
        </td>
      </tr>
      <tr>
        <td width="26%" align=right><div class="tabletext">Party Id</div></td>
        <td>&nbsp;</td>
        <td width="74%"><input type="text" <ofbiz:inputvalue entityAttr="inventoryItem" field="partyId" fullattrs="true"/> size="20" maxlength="20"></td>
      </tr>
      <tr>
        <td width="26%" align=right><div class="tabletext">Status</div></td>
        <td>&nbsp;</td>
        <td width="74%">
           <select name="statusId">
            <%if(inventoryItem != null) {%>
               <option value="<%=inventoryItem.getString("statusId")%>"><%=inventoryItem.getString("statusId")%></option>
               <option value="<%=inventoryItem.getString("statusId")%>">----</option>
            <%} else {%>
              <option value=''>----</option>
            <%}%>
             <ofbiz:iterator name="statusItem" property="statusItems">
               <option value="<%=statusItem.getString("statusId")%>"><%=statusItem.getString("description")%></option>               
             </ofbiz:iterator>
           </select>
         </td>
       </tr>
      <tr>
        <td width="26%" align=right><div class="tabletext">Facility/Container</div></td>
        <td>&nbsp;</td>
        <td width="74%">
           Select a Facility:
           <select name="facilityId">
            <%if(inventoryItem != null) {%>
                 <option value="<%=inventoryItem.getString("facilityId")%>"><%=inventoryItem.getString("facilityId")%></option>
                 <option value="<%=inventoryItem.getString("facilityId")%>">----</option>
            <%} else {%>
              <option value=''>----</option>
            <%}%>
             <ofbiz:iterator name="facility" property="facilities">
               <option value="<%=facility.getString("facilityId")%>"><%=facility.getString("description")%></option>
             </ofbiz:iterator>
           </select>
           OR enter a Container ID:
           <input type="text" <ofbiz:inputvalue entityAttr="inventoryItem" field="containerId" fullattrs="true"/> size="20" maxlength="20">
         </td>
       </tr>
      <tr>
        <td width="26%" align=right><div class="tabletext">Lot Id</div></td>
        <td>&nbsp;</td>
        <td width="74%"><input type="text" <ofbiz:inputvalue entityAttr="inventoryItem" field="lotId" fullattrs="true"/> size="20" maxlength="20"></td>
      </tr>
      <tr>
        <td width="26%" align=right><div class="tabletext">Uom Id</div></td>
        <td>&nbsp;</td>
        <td width="74%"><input type="text" <ofbiz:inputvalue entityAttr="inventoryItem" field="uomId" fullattrs="true"/> size="20" maxlength="20"></td>
      </tr>
    <%if (inventoryItem != null && "NON_SERIAL_INV_ITEM".equals(inventoryItem.getString("inventoryItemTypeId"))) {%>
      <tr>
        <td width="26%" align=right><div class="tabletext">Available To Promise / Quantity On Hand</div></td>
        <td>&nbsp;</td>
        <td width="74%">
            <input type=text size='5' <ofbiz:inputvalue entityAttr="inventoryItem" field="availableToPromise" fullattrs="true"/>>
            / <input type=text size='5' <ofbiz:inputvalue entityAttr="inventoryItem" field="quantityOnHand" fullattrs="true"/>>
        </td>
      </tr>
    <%} else if (inventoryItem != null && "SERIALIZED_INV_ITEM".equals(inventoryItem.getString("inventoryItemTypeId"))) {%>
      <tr>
        <td width="26%" align=right><div class="tabletext">Serial Number</div></td>
        <td>&nbsp;</td>
        <td width="74%"><input type="text" <ofbiz:inputvalue entityAttr="inventoryItem" field="serialNumber" fullattrs="true"/> size="30" maxlength="60"></td>
      </tr>
    <%} else if (inventoryItem != null) {%>
      <tr>
        <td width="26%" align=right><div class="tabletext">Serial#/ATP/QOH</div></td>
        <td>&nbsp;</td>
        <td width="74%"><div class='tabletext' style='color: red;'>Error: type <ofbiz:entityfield attribute="inventoryItem" field="inventoryItemTypeId"/> unknown; specify a type.</div></td>
      </tr>
    <%}%>

  <tr>
    <td colspan='1' align=right><input type="submit" name="Update" value="Update"></td>
    <td colspan='2'>&nbsp;</td>
  </tr>
</table>
</form>

<%}else{%>
  <h3>You do not have permission to view this page. ("CATALOG_VIEW" or "CATALOG_ADMIN" needed)</h3>
<%}%>
</td><td>&nbsp;&nbsp;</td></tr></table>
<%} catch (Exception e) { Debug.logError(e); } %>