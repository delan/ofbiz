<%
/**
 *  Title: Checkout Page
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
 *@created    May 22 2001
 *@version    1.0
 */
%>
<%@ taglib uri="ofbizTags" prefix="ofbiz" %>

<%@ page import="java.util.*" %>

<%@ page import="org.ofbiz.ecommerce.catalog.*" %>
<%@ page import="org.ofbiz.ecommerce.shoppingcart.*" %>
<%@ page import="org.ofbiz.commonapp.party.contact.ContactHelper" %>
<%@ page import="org.ofbiz.commonapp.party.party.PartyHelper" %>
<%@ page import="org.ofbiz.core.entity.*" %>

<% pageContext.setAttribute("PageName", "checkout"); %>

<%@ include file="/includes/header.jsp" %>
<%@ include file="/includes/onecolumn.jsp" %>

<p class="head1">Final Checkout Review</p>
<p>NOTE: This is a DEMO store-front.  Orders placed here will NOT be billed, and will NOT be fulfilled.</p>

<%ShoppingCart cart = (ShoppingCart)session.getAttribute(SiteDefs.SHOPPING_CART); %>
<%pageContext.setAttribute("cart", cart);%>
<ofbiz:if name="cart" size="0">
<%
  GenericValue shippingAddress = cart.getShippingAddress(helper);
  GenericValue creditCardInfo = cart.getCreditCardInfo(helper);
  GenericValue  billingAddress = cart.getBillingAddress(helper);
  String customerPoNumber = cart.getPoNumber();
//  if (creditCardInfo == null) {

  if (shippingAddress != null) pageContext.setAttribute("shippingAddress", shippingAddress);
  if (creditCardInfo != null) pageContext.setAttribute("creditCardInfo", creditCardInfo);
  if (billingAddress != null) pageContext.setAttribute("billingAddress", billingAddress);
  pageContext.setAttribute("cartItems", cart.items()); 
%>

<ofbiz:unless name="shippingAddress">
  <h3>ERROR: Shipping address must be specified, please go back or start over.</h3>
</ofbiz:unless>
<ofbiz:if name="shippingAddress">
<table width="100%" cellpadding="3" cellspacing="0" border="0">
  <%-- row to set cell widths --%>
  <tr>
    <td width="5%"></td>
    <td width="95%"></td>
  </tr>
 <tr>
    <td colspan="2" align="left"><div class="head2">Shipping Destination</div></td>
 </tr>

 <tr>
    <td>&nbsp;</td>
    <td align="left" valign="top" nowrap>
      <div class="tabletext">
        <%=UtilFormatOut.ifNotEmpty(shippingAddress.getString("toName"), "<b>To:</b> ", "<br>")%>
        <%=UtilFormatOut.ifNotEmpty(shippingAddress.getString("attnName"), "<b>Attn:</b> ", "<br>")%>
        <%=UtilFormatOut.ifNotEmpty(shippingAddress.getString("address1"), "", "<br>")%>
        <%=UtilFormatOut.ifNotEmpty(shippingAddress.getString("address2"), "", "<br>")%>
        <%=UtilFormatOut.ifNotEmpty(shippingAddress.getString("city"), "", "<br>")%>
        <%=UtilFormatOut.ifNotEmpty(shippingAddress.getString("stateProvinceGeoId"), "", "&nbsp;")%> <%=UtilFormatOut.checkNull(shippingAddress.getString("postalCode"))%><br>
        <%=UtilFormatOut.ifNotEmpty(shippingAddress.getString("countryGeoId"), "", "<br>")%>
      </div>
    </td>
  </tr>
<%--  <tr>
    <td align="left" colspan="2"><div class="head2">Shipment Splitting Preference</div></td>
  </tr>
  <tr>
      <td>&nbsp;</td>
      <td align="left">
        <div class="tabletext"><%=splittingPreferenceCode%></div>
      </td>
  </tr>  --%>



  <tr>
    <td align="left" colspan="2"><div class="head2">Shipping Instructions</div></td>
  </tr>
  <tr>
      <td>&nbsp;</td>
      <td align="left">
  <div class="tabletext"><%=UtilFormatOut.checkNull(cart.getShippingInstructions())%></div>
      </td>
  </tr>

  <tr>
    <td align="left" colspan="5"><div class="head2">Shipping Method</div></td>
  </tr>
  <tr>
    <td>&nbsp;</td>
    <td align="left">
        <div class="tabletext">
          <%=UtilFormatOut.checkNull(cart.getCarrierPartyId())%> 
          <%=UtilFormatOut.checkNull(helper.findByPrimaryKey("ShipmentMethodType", UtilMisc.toMap("shipmentMethodTypeId", cart.getShipmentMethodTypeId())).getString("description"))%>
          <%--=UtilFormatOut.ifNotEmpty(shippingAccount, "<br>Use Account: ", "")--%>
        </div>
    </td>
  </tr> 
      <tr>
        <td align="left" colspan="5"><div class="head2">Payment Information</div></td>
      </tr>
      <tr>
        <td>&nbsp;</td>
        <td align="left" valign="top" nowrap>
<ofbiz:if name="creditCardInfo"> 
        <div class="tabletext">
          <b>Name on Card:</b> <%=creditCardInfo.getString("nameOnCard")%><br>
        </div>
        <div class="tabletext"><b><%=creditCardInfo.getString("cardType")%>: <%=creditCardInfo.getString("cardNumber").substring(creditCardInfo.getString("cardNumber").length()-4)%> <%=creditCardInfo.getString("expireDate")%></b></div>
</ofbiz:if>
<ofbiz:if name="billingAddress">
        <div class="tabletext"><b>Purchase Order Number: <%=UtilFormatOut.checkNull(customerPoNumber)%></b></div>
        <div class="tabletext">
          <%=UtilFormatOut.ifNotEmpty(billingAddress.getString("toName"), "<b>To:</b> ", "<br>")%>
          <%=UtilFormatOut.ifNotEmpty(billingAddress.getString("attnName"), "<b>Attn:</b> ", "<br>")%>
          <%=UtilFormatOut.checkNull(billingAddress.getString("address1"))%><br>
          <%=UtilFormatOut.ifNotEmpty(billingAddress.getString("address2"),  "", "<br>")%>
          <%=UtilFormatOut.ifNotEmpty(billingAddress.getString("city"), "", "<br>")%>
          <%=UtilFormatOut.checkNull(billingAddress.getString("stateProvinceGeoId"))%> &nbsp; <%=UtilFormatOut.checkNull(billingAddress.getString("postalCode"))%><br>
          <%=UtilFormatOut.ifNotEmpty(billingAddress.getString("countryGeoId"), "", "")%>
        </div>
</ofbiz:if>
        </td>
      </tr>
</table>
</ofbiz:if> <%-- shippingAddress --%>
<%-- table for shoppingcartline items --%>
  <table border="0" cellpadding="1" width="100%">
    <%-- use this row to set the widths on all the columns --%>
    <tr>
      <td width="15%"><img src="../images/shim.gif" width="2" height="1"></td>
      <td width="50%"><img src="../images/shim.gif" width="2" height="1"></td>
      <td width="5%"><img src="../images/shim.gif" width="2" height="1"></td>
      <td width="15%"><img src="../images/shim.gif" width="2" height="1"></td>
      <td width="15%"><img src="../images/shim.gif" width="2" height="1"></td>
    </tr>

    <tr>
      <td colspan="5" bgcolor="#678475">
      <div class="tabletext"><font color="white"><b>Order</b></font></div>
      </td>
    </tr>

    <tr>
      <td align="left" valign="bottom"><div class="tabletext"><b>ID</b></div></td>
      <td align="left" valign="bottom"><div class="tabletext"><b>Description</b></div></td>
      <td align="center" valign="bottom"><div class="tabletext"><b>Quantity</b></div></td>
      <td align="center" valign="bottom"><div class="tabletext"><b>Our Price</b></div></td>
      <td align="center" valign="bottom"><div class="tabletext"><b>SubTotal</b></div></td>
    </tr>
    <tr>
      <td colspan="5"><hr size="1"></td>
    </tr>

 <ofbiz:iterator name="cartItem" type="org.ofbiz.ecommerce.shoppingcart.ShoppingCartItem" property="cartItems">
<% pageContext.setAttribute("productId", cartItem.getProductId()); %>
  <ofbiz:if name="productId" value="shoppingcart.CommentLine">
    <tr>
      <td valign="middle" colspan="5">
        <b><div class="tabletext"> >> <%=cartItem.getDescription()%></div></b>
      </td>
    </tr>
  </ofbiz:if>
  <ofbiz:unless name="productId" value="shoppingcart.CommentLine">
    <tr>
      <td nowrap valign="top">
        <div class="tabletext"><%=cartItem.getProductId()%></div>
      </td>
      <td valign="top">
        <div class="tabletext"><%=cartItem.getName()%> : <%=cartItem.getDescription()%>
        </div>
      </td>
      <td align="center" valign="top">
        <div class="tabletext"><%=UtilFormatOut.formatQuantity(cartItem.getQuantity())%></div>
      </td>
      <td align="right" nowrap valign="top">
        <div class="tabletext"><%-- cartItem.getUnitPrice().getCurrency() --%>
        <%=UtilFormatOut.formatPrice(cartItem.getPrice())%></div>
      </td>
      <td align="right" nowrap valign="top">
        <div class="tabletext"><%-- lineTotal.getCurrency() --%>
        <%=UtilFormatOut.formatPrice(cartItem.getTotalPrice())%></div>
      </td>
    </tr>
  </ofbiz:unless>
    <tr>
      <td colspan="5" height="1"><hr size="1"></td>
    </tr>
</ofbiz:iterator>

<%String prevPage = "/checkoutoptions"; %>
    <tr>
      <td colspan="4" align="right"><div class="tabletext">Item Total</div></td>
      <td align="right" nowrap>
  <div class="tabletext"><%=UtilFormatOut.formatPrice(cart.getItemTotal())%></div>
      </td>
    </tr>
    <tr>
      <td colspan="4" align="right"><div class="tabletext">Shipping & handling</div></td>
      <td align="right" nowrap>
  <div class="tabletext"><%=UtilFormatOut.formatPrice(cart.getShipping())%></div>
      </td>
    </tr>

    <tr>
      <td colspan="4" align="right"><div class="tabletext">Total tax</div></td>
      <td align="right" nowrap>
  <div class="tabletext"><%=UtilFormatOut.formatPrice(cart.getSalesTax())%></div>
      </td>
    </tr>

    <tr>
      <td colspan="4" align="right"><div class="tabletext"><b>Total due</b></div></td>
     <td align="right" bgcolor="#99BBAA" nowrap>
  <div class="tabletext"><b><%-- total.getCurrency() --%>
  <%=UtilFormatOut.formatPrice(cart.getGrandTotal())%></b></div>
      </td>
    </tr>
    <tr>
      <td colspan="5"><hr size="1"></td>
    </tr>
   <tr>
      <td colspan="4" align="left">
      <a href="<ofbiz:url><%=prevPage%></ofbiz:url>" class="buttontextbig">&nbsp;[Back]</a></td>
      <td align="right">
        <a href="<ofbiz:url>/createorder</ofbiz:url>" class="buttontextbig">[Submit&nbsp;Order]&nbsp;</a>
      </td>
    </tr>
  </table>
</ofbiz:if><%-- Shipping address check --%>

</ofbiz:if><%-- cart check --%>
<ofbiz:unless name="cart" size="0">
<h3>ERROR: Shopping cart empty, please start over.</h3>
</ofbiz:unless>

<%@ include file="/includes/onecolumnclose.jsp" %>
<%@ include file="/includes/footer.jsp" %>
