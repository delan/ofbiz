<%
// NOTE: This page is meant to be included, not called independently

/**
 *  Title: Order Information
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
 *@author     Eric Pabst
 *@author     David E. Jones
 *@created    May 22 2001
 *@version    1.0
 */
%>

    <%GenericValue localOrderHeader = null;%>
    <%OrderReadHelper localOrder = null;%>
    <ofbiz:if name="orderHeader">
        <%localOrderHeader = (GenericValue) pageContext.getAttribute("orderHeader");%>
        <%localOrder = new OrderReadHelper(localOrderHeader);%>
    </ofbiz:if>
    <%String distributorId = localOrder != null ? localOrder.getDistributorId() : (String) session.getAttribute(ThirdPartyEvents.DISTRIBUTOR_ID);%>
    <%if (distributorId != null) pageContext.setAttribute("distributorId", distributorId);%>
    <%if (creditCardInfo != null) pageContext.setAttribute("creditCardInfo", creditCardInfo);%>
    <%if (billingAccount != null) pageContext.setAttribute("billingAccount", billingAccount);%>
    <%--if (billingAddress != null) pageContext.setAttribute("billingAddress", billingAddress);--%>
    <%if (shippingAddress != null) pageContext.setAttribute("shippingAddress", shippingAddress);%>
    <%if (maySplit != null) pageContext.setAttribute("maySplit", maySplit);%>
    <%String shipMethDescription = "";%>
    <%GenericValue shipmentMethodType = delegator.findByPrimaryKey("ShipmentMethodType", UtilMisc.toMap("shipmentMethodTypeId", shipmentMethodTypeId));%>
    <%if(shipmentMethodType != null) shipMethDescription = shipmentMethodType.getString("description");%>

<br>
<table width="100%" border="0" cellpadding="0" cellspacing="0">
 <tr>
  <td width='50%' valign=top align=left>
<TABLE border=0 width='100%' cellpadding='<%EntityField.run("layoutSettings", "boxBorderWidth", pageContext);%>' cellspacing=0 bgcolor='<%EntityField.run("layoutSettings", "boxBorderColor", pageContext);%>'>
  <TR>
    <TD width='100%'>
      <table width='100%' border='0' cellpadding='<%EntityField.run("layoutSettings", "boxTopPadding", pageContext);%>' cellspacing='0' bgcolor='<%EntityField.run("layoutSettings", "boxTopColor", pageContext);%>'>
        <tr>
          <td valign="middle" align="left">
            <div class="boxhead">&nbsp;Order <ofbiz:if name="orderHeader">#<%EntityField.run("orderHeader", "orderId", pageContext);%> </ofbiz:if>Information</div>
          </td>
        </tr>
      </table>
    </TD>
  </TR>
  <TR>
    <TD width='100%'>
      <table width='100%' border='0' cellpadding='<%EntityField.run("layoutSettings", "boxBottomPadding", pageContext);%>' cellspacing='0' bgcolor='<%EntityField.run("layoutSettings", "boxBottomColor", pageContext);%>'>
        <tr>
          <td>
              <table width="100%" border="0" cellpadding="1">
               <ofbiz:if name="userLogin">
                <tr>
                  <td align="right" valign="top" width="15%">
                    <div class="tabletext">&nbsp;<b>Name</b></div>
                  </td>
                  <td width="5">&nbsp;</td>
                  <td align="left" valign="top" width="80%">
                    <div class="tabletext">
                    <%if(person!=null){%>
                      <%=PartyHelper.getPersonName(person)%>
                    <%}%>
                    <%EntityField.run("userLogin", "userLoginId", " (", ") ", pageContext);%>
                    </div>
                  </td>
                </tr>
                <tr><td colspan="7"><hr class='sepbar'></td></tr>
               </ofbiz:if>
                <tr>
                  <td align="right" valign="top" width="15%">
                    <div class="tabletext">&nbsp;<b>Status</b></div>
                  </td>
                  <td width="5">&nbsp;</td>
                  <td align="left" valign="top" width="80%">
                    <ofbiz:if name="orderHeader">
                      <div class="tabletext"><%=localOrder.getStatusString()%></div>
                    </ofbiz:if>
                    <ofbiz:unless name="orderHeader">
                      <div class="tabletext"><b>Not Yet Ordered</b></div>
                    </ofbiz:unless>
                  </td>
                </tr>
              <ofbiz:if name="orderHeader">
                <tr><td colspan="7"><hr class='sepbar'></td></tr>
                <tr>
                  <td align="right" valign="top" width="15%">
                    <div class="tabletext">&nbsp;<b>Date</b></div>
                  </td>
                  <td width="5">&nbsp;</td>
                  <td align="left" valign="top" width="80%">
                      <div class="tabletext"><%EntityField.run("orderHeader", "orderDate", pageContext);%></div>
                  </td>
                </tr>
              </ofbiz:if>
              <ofbiz:if name="distributorId">
                <tr><td colspan="7"><hr class='sepbar'></td></tr>
                <tr>
                  <td align="right" valign="top" width="15%">
                    <div class="tabletext">&nbsp;<b>Distributor</b></div>
                  </td>
                  <td width="5">&nbsp;</td>
                  <td align="left" valign="top" width="80%">
                      <div class="tabletext"><%=PartyHelper.formatPartyId(distributorId, delegator)%></div>
                  </td>
                </tr>
              </ofbiz:if>
              </table>
          </td>
        </tr>
      </table>
    </TD>
  </TR>
</TABLE>

    <br>

<TABLE border=0 width='100%' cellpadding='<%EntityField.run("layoutSettings", "boxBorderWidth", pageContext);%>' cellspacing=0 bgcolor='<%EntityField.run("layoutSettings", "boxBorderColor", pageContext);%>'>
  <TR>
    <TD width='100%'>
      <table width='100%' border='0' cellpadding='<%EntityField.run("layoutSettings", "boxTopPadding", pageContext);%>' cellspacing='0' bgcolor='<%EntityField.run("layoutSettings", "boxTopColor", pageContext);%>'>
        <tr>
          <td valign="middle" align="left">
            <div class="boxhead">&nbsp;Payment Information</div>
          </td>
        </tr>
      </table>
    </TD>
  </TR>
  <TR>
    <TD width='100%'>
      <table width='100%' border='0' cellpadding='<%EntityField.run("layoutSettings", "boxBottomPadding", pageContext);%>' cellspacing='0' bgcolor='<%EntityField.run("layoutSettings", "boxBottomColor", pageContext);%>'>
        <tr>
          <td>
              <table width="100%" border="0" cellpadding="1">
              <ofbiz:if name="creditCardInfo"> 
                <%pageContext.setAttribute("outputted", "true");%>
                <tr>
                  <td align="right" valign="top" width="15%">
                    <div class="tabletext">&nbsp;<b>Credit Card</b></div>
                  </td>
                  <td width="5">&nbsp;</td>
                  <td align="left" valign="top" width="80%">
                      <div class="tabletext">
                        <%EntityField.run("creditCardInfo", "nameOnCard", pageContext);%><br>
                        <%=ContactHelper.formatCreditCard(creditCardInfo)%>
                      </div>
                  </td>
                </tr>
              </ofbiz:if>
              <ofbiz:if name="billingAccount">
                <ofbiz:if name="outputted">
                <tr><td colspan="7"><hr class='sepbar'></td></tr>
                </ofbiz:if>
                <%pageContext.setAttribute("outputted", "true");%>
                <tr>
                  <td align="right" valign="top" width="15%">
                    <div class="tabletext">&nbsp;<b>Billing Account</b></div>
                  </td>
                  <td width="5">&nbsp;</td>
                  <td align="left" valign="top" width="80%">
                      <div class="tabletext">
                        #<%EntityField.run("billingAccount", "billingAccountId", pageContext);%> - <%EntityField.run("billingAccount", "description", pageContext);%>
                      </div>
                  </td>
                </tr>
                <tr><td colspan="7"><hr class='sepbar'></td></tr>
                <tr>
                  <td align="right" valign="top" width="15%">
                    <div class="tabletext">&nbsp;<b>Purchase Order Number</b></div>
                  </td>
                  <td width="5">&nbsp;</td>
                  <td align="left" valign="top" width="80%">
                      <div class="tabletext"><%=UtilFormatOut.checkNull(customerPoNumber)%></div>
                  </td>
                </tr>
              </ofbiz:if>
              <%--ofbiz:if name="billingAddress">
                <ofbiz:if name="outputted">
                <tr><td colspan="7"><hr class='sepbar'></td></tr>
                </ofbiz:if>
                <%pageContext.setAttribute("outputted", "true");%>
                <tr>
                  <td align="right" valign="top" width="15%">
                    <div class="tabletext">&nbsp;<b>Billing Address</b></div>
                  </td>
                  <td width="5">&nbsp;</td>
                  <td align="left" valign="top" width="80%">
                      <div class="tabletext">
                        <%EntityField.run("billingAddress", "toName", "<b>To:</b> ", "<br>", pageContext);%>
                        <%EntityField.run("billingAddress", "attnName", "<b>Attn:</b> ", "<br>", pageContext);%>
                        <%EntityField.run("billingAddress", "address1", pageContext);%><br>
                        <%EntityField.run("billingAddress", "address2", "", "<br>", pageContext);%>
                        <%EntityField.run("billingAddress", "city", pageContext);%>,
                        <%EntityField.run("billingAddress", "stateProvinceGeoId", pageContext);%>
                        <%EntityField.run("billingAddress", "postalCode", pageContext);%>
                        <%EntityField.run("billingAddress", "countryGeoId", "<br>", "", pageContext);%>
                      </div>
                  </td>
                </tr>
              </ofbiz:if> --%>
              </table>
          </td>
        </tr>
      </table>
    </TD>
  </TR>
</TABLE>

</td>
<td bgcolor="white" width="1">&nbsp;&nbsp;</td>
<td width='50%' valign=top align=left>

<TABLE border=0 width='100%' cellpadding='<%EntityField.run("layoutSettings", "boxBorderWidth", pageContext);%>' cellspacing=0 bgcolor='<%EntityField.run("layoutSettings", "boxBorderColor", pageContext);%>'>
  <TR>
    <TD width='100%'>
      <table width='100%' border='0' cellpadding='<%EntityField.run("layoutSettings", "boxTopPadding", pageContext);%>' cellspacing='0' bgcolor='<%EntityField.run("layoutSettings", "boxTopColor", pageContext);%>'>
        <tr>
          <td valign="middle" align="left">
            <div class="boxhead">&nbsp;Shipping Information</div>
          </td>
        </tr>
      </table>
    </TD>
  </TR>
  <TR>
    <TD width='100%'>
      <table width='100%' border='0' cellpadding='<%EntityField.run("layoutSettings", "boxBottomPadding", pageContext);%>' cellspacing='0' bgcolor='<%EntityField.run("layoutSettings", "boxBottomColor", pageContext);%>'>
        <tr>
          <td>
              <table width="100%" border="0" cellpadding="1">
                <ofbiz:if name="shippingAddress">
                <tr>
                  <td align="right" valign="top" width="15%">
                    <div class="tabletext">&nbsp;<b>Destination</b></div>
                  </td>
                  <td width="5">&nbsp;</td>
                  <td align="left" valign="top" width="80%">
                      <div class="tabletext">
                        <%EntityField.run("shippingAddress", "toName", "<b>To:</b> ", "<br>", pageContext);%>
                        <%EntityField.run("shippingAddress", "attnName", "<b>Attn:</b> ", "<br>", pageContext);%>
                        <%EntityField.run("shippingAddress", "address1", pageContext);%><br>
                        <%EntityField.run("shippingAddress", "address2", "", "<br>", pageContext);%>
                        <%EntityField.run("shippingAddress", "city", pageContext);%>,
                        <%EntityField.run("shippingAddress", "stateProvinceGeoId", pageContext);%>
                        <%EntityField.run("shippingAddress", "postalCode", pageContext);%>
                        <%EntityField.run("shippingAddress", "countryGeoId", "<br>", "", pageContext);%>
                      </div>
                  </td>
                </tr>
                <tr><td colspan="7"><hr class='sepbar'></td></tr>
                </ofbiz:if>
                <tr>
                  <td align="right" valign="top" width="15%">
                    <div class="tabletext">&nbsp;<b>Method</b></div>
                  </td>
                  <td width="5">&nbsp;</td>
                  <td align="left" valign="top" width="80%">
                      <div class="tabletext">
                      <%=UtilFormatOut.checkNull(carrierPartyId)%> 
                      <%=UtilFormatOut.checkNull(shipMethDescription)%>
                      <%--=UtilFormatOut.ifNotEmpty(shippingAccount, "<br>Use Account: ", "")--%>
                      </div>
                  </td>
                </tr>
                <tr><td colspan="7"><hr class='sepbar'></td></tr>
                <tr>
                  <td align="right" valign="top" width="15%">
                    <div class="tabletext">&nbsp;<b>Splitting Preference</b></div>
                  </td>
                  <td width="5">&nbsp;</td>
                  <td align="left" valign="top" width="80%">
                      <div class="tabletext">
                      <ofbiz:unless name="maySplit">
                      Please wait until the entire order is ready before shipping.
                      </ofbiz:unless>
                      <ofbiz:if name="maySplit">
                      Please ship items I ordered as they become available (may incur additional shipping charges).    
                      </ofbiz:if>
                      </div>
                  </td>
                </tr>
                <tr><td colspan="7"><hr class='sepbar'></td></tr>
                <tr>
                  <td align="right" valign="top" width="15%">
                    <div class="tabletext">&nbsp;<b>Instructions</b></div>
                  </td>
                  <td width="5">&nbsp;</td>
                  <td align="left" valign="top" width="80%">
                      <div class="tabletext">
                      <%=UtilFormatOut.checkNull(shippingInstructions)%>
                      </div>
                   </td>
                </tr>
              </table>
          </td>
        </tr>
      </table>
    </TD>
  </TR>
</TABLE>
  </td>
 </tr>
</table>
