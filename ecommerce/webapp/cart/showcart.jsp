<%@ taglib uri="ofbizTags" prefix="ofbiz" %>
<% pageContext.setAttribute("PageName", "showcart"); %>

<%@ include file="/includes/header.jsp" %>
<%@ include file="/includes/leftcolumn.jsp" %> 

<ofbiz:object name="cart" property="_SHOPPING_CART_" type="org.ofbiz.ecommerce.shoppingcart.ShoppingCart" />  

<BR>
<TABLE border=0 width='100%' cellpadding=1 cellspacing=0 bgcolor='black'>
  <TR>
    <TD width='100%'>
      <table width="100%" border="0" cellpadding="4" cellspacing="0" bgcolor="#678475">
        <tr>
          <td valign="middle" align="left">
            <div class="boxhead">&nbsp;Quick Add</div>
          </td>
          <td valign="middle" align="right">
            <div class='lightbuttontextdisabled'>
              <a href="<ofbiz:url>/main</ofbiz:url>" class="lightbuttontext">[Continue&nbsp;Shopping]</a>
              <%if(microCart != null && microCart.size() > 0){%>
                <a href="<ofbiz:url>/checkoutoptions</ofbiz:url>" class="lightbuttontext">[Checkout]</a>
              <%}else{%>
                [Checkout]
              <%}%>
            </div>
          </td>
        </tr>
      </table>
    </TD>
  </TR>
  <TR>
    <TD width='100%'>
      <table width='100%' border=0 cellpadding=4 cellspacing=0 bgcolor='white'>
        <tr>
          <td>
            <form method="POST" action="<ofbiz:url>/additem<%=UtilFormatOut.ifNotEmpty((String)request.getAttribute(SiteDefs.CURRENT_VIEW), "/", "")%></ofbiz:url>" name="quickaddform" style='margin: 0;'>
              <input type='text' name="product_id" value="<%=UtilFormatOut.checkNull(request.getParameter("product_id"))%>">
              <input type='text' size="5" name="quantity" value="<%=UtilFormatOut.checkNull(request.getParameter("quantity"), "1")%>">
              <input type='submit' value="Add To Cart">
              <%-- <a href="javascript:document.quickaddform.submit()" class="buttontext"><nobr>[Add to Cart]</nobr></a> --%>
            </form>      
          </td>
        </tr>
      </table>
    </TD>
  </TR>
</TABLE>

<script language="JavaScript">
<!--
  document.quickaddform.product_id.focus();
//-->
</script>
<BR>
<TABLE border=0 width='100%' cellpadding=1 cellspacing=0 bgcolor='black'>
  <TR>
    <TD width='100%'>
      <table width="100%" border="0" cellpadding="4" cellspacing="0" bgcolor="#678475">
        <tr>
          <td valign="middle" align="left">
            <div class="boxhead">&nbsp;Shopping Cart</div>
          </td>
          <td valign="middle" align="right">
            <div class='lightbuttontextdisabled'>
              <a href="<ofbiz:url>/main</ofbiz:url>" class="lightbuttontext">[Continue&nbsp;Shopping]</a>
              <a href="javascript:document.cartform.submit()" class="lightbuttontext">[Recalculate&nbsp;Cart]</a>
              <%if(microCart != null && microCart.size() > 0){%>
                <a href="<ofbiz:url>/checkoutoptions</ofbiz:url>" class="lightbuttontext">[Checkout]</a>
              <%}else{%>
                [Checkout]
              <%}%>
            </div>
          </td>
        </tr>
      </table>
    </TD>
  </TR>
  <TR>
    <TD width='100%'>
      <table width="100%" border="0" cellpadding="4" cellspacing="0" bgcolor="white">
        <tr>
          <td>
<ofbiz:if name="_SHOPPING_CART_">
    <FORM METHOD="POST" ACTION="<ofbiz:url>/modifycart</ofbiz:url>" name='cartform' style='margin: 0;'>
      <table width='100%' CELLSPACING="0" CELLPADDING="4" BORDER="0">
        <TR> 
          <TD NOWRAP><div class='tabletext'><b>Product</b></div></TD>
          <TD NOWRAP align=center><div class='tabletext'><b>Quantity</b></div></TD>
          <TD NOWRAP align=right><div class='tabletext'><b>Unit Price</b></div></TD>
          <TD NOWRAP align=right><div class='tabletext'><b>Total</b></div></TD>
          <%-- <TD NOWRAP align=center><div class='tabletext'><b>Remove</b></div></TD> --%>
        </TR>

        <ofbiz:iterator name="item" type="org.ofbiz.ecommerce.shoppingcart.ShoppingCartItem">  
          <tr><td colspan="7" height="1" bgcolor="#899ABC"></td></tr>
          <TR>
            <TD><div class='tabletext'><%-- <b><%= cart.getItemIndex(item)%></b> - --%><%= item.getProductId()%> - <%= item.getName()%> : <%= item.getDescription()%></div></TD>
            <TD NOWRAP ALIGN="center"><div class='tabletext'><input size="5" type="text" name="update_<%= cart.getItemIndex(item) %>" value="<ofbiz:format><%= item.getQuantity() %></ofbiz:format>"></div></TD>
            <TD NOWRAP ALIGN="right"><div class='tabletext'><ofbiz:format type="c"><%= item.getBasePrice() %></ofbiz:format></div></TD>
            <TD NOWRAP ALIGN="right"><div class='tabletext'><ofbiz:format type="c"><%= item.getTotalPrice() %></ofbiz:format></div></TD>
            <%-- <TD NOWRAP ALIGN="center"><div class='tabletext'><input type="checkbox" name="delete_<%= cart.getItemIndex(item) %>" value="0"></div></TD> --%>
          </TR>
        </ofbiz:iterator>

<%--
        <TR>
          <TD COLSPAN="3" ALIGN="right"><div class='tabletext'>Sales tax:</div></TD>
          <TD ALIGN="right"><div class='tabletext'>$0.00</div></TD>
        </TR> 
--%>

        <TR> 
          <TD COLSPAN="3" ALIGN="right" valign=bottom> 
             <!-- <HR SIZE=1> -->
            <div class='tabletext'><b>Cart Total:</b></div>
          </TD>
          <TD ALIGN="right" valign=bottom>
            <HR SIZE=1>
            <div class='tabletext'><b><ofbiz:format type="c"><%= cart.getGrandTotal() %></ofbiz:format></b></div>
          </TD>
        </TR>
      </table>
    </FORM>
<%--
      <CENTER>
        <input type="checkbox" name="always_showcart" <%= cart.viewCartOnAdd() ? "checked" : "" %>>&nbsp;Always view cart after adding an item.
    	<br><br>
        <input type="submit" value="Update Cart">
      </CENTER>
--%>
</ofbiz:if>
<ofbiz:unless name="_SHOPPING_CART_">
  <div class='head2'>Your shopping cart is empty.</div>
</ofbiz:unless>
          </td>
        </tr>
      </table>
    </TD>
  </TR>
  <TR>
    <TD width='100%'>
      <table width="100%" border="0" cellpadding="4" cellspacing="0" bgcolor="#678475">
        <tr>
          <td>
      <table width="100%" border="0" cellpadding="0" cellspacing="0">
        <tr>
          <td valign="middle" align="left">
            &nbsp;
          </td>
          <td valign="middle" align="right">
            <div class='lightbuttontextdisabled'>
              <a href="<ofbiz:url>/main</ofbiz:url>" class="lightbuttontext">[Continue&nbsp;Shopping]</a>
              <a href="javascript:document.cartform.submit()" class="lightbuttontext">[Recalculate&nbsp;Cart]</a>
              <%if(microCart != null && microCart.size() > 0){%>
                <a href="<ofbiz:url>/checkoutoptions</ofbiz:url>" class="lightbuttontext">[Checkout]</a>
              <%}else{%>
                [Checkout]
              <%}%>
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

<%@ include file="/includes/onecolumnclose.jsp" %>
<%@ include file="/includes/footer.jsp" %>
