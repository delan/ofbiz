<%
    /**
     *  Title: Edit Product Page
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
     *@author     Andy Zeneski
     *@author     David E. Jones
     *@created    March 10, 2002
     *@version    1.0
     */
%>

<%@ page import="java.util.*, java.io.*" %>
<%@ page import="org.ofbiz.core.util.*, org.ofbiz.core.entity.*" %>

<%@ taglib uri="ofbizTags" prefix="ofbiz" %>
<jsp:useBean id="delegator" type="org.ofbiz.core.entity.GenericDelegator" scope="request" />
<jsp:useBean id="security" type="org.ofbiz.core.security.Security" scope="request" />

<%if (security.hasEntityPermission("CATALOG", "_VIEW", session)) {%>
<%
        boolean useValues = true;
        if (request.getAttribute(SiteDefs.ERROR_MESSAGE) != null) useValues = false;

        String productId = request.getParameter("PRODUCT_ID");
        GenericValue product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", productId));
        if (product == null) useValues = false;
        Collection productAttributes = product.getRelated("ProductAttribute", null, UtilMisc.toList("attrType", "attrName"));
        if (productAttributes != null)
            pageContext.setAttribute("productAttributes", productAttributes);

        if ("true".equalsIgnoreCase((String)request.getParameter("useValues"))) useValues = true;
%>
<br>

<div class="head1">Edit Attributes for Product
  <%=UtilFormatOut.ifNotEmpty(product==null?null:product.getString("productName"),"\"","\"")%>
  with ID "<%=UtilFormatOut.checkNull(productId)%>"</div>

<a href="<ofbiz:url>/EditProduct</ofbiz:url>" class="buttontext">[Create New Product]</a>
<%-- <%if(product != null){%><a href="<ofbiz:url>UpdateProduct?UPDATE_MODE=DELETE&PRODUCT_ID=<%=product.getSku()%></ofbiz:url>" class="buttontext">[Delete this Product]</a><%}%> --%>
<%if(productId != null && productId.length() > 0){%>
  <a href="/ecommerce/control/product?product_id=<%=productId%>" class='buttontext' target='_blank'>[View Product Page]</a>
  <a href="<ofbiz:url>/EditProduct?PRODUCT_ID=<%=productId%></ofbiz:url>" class="buttontext">[Edit Product]</a>
  <a href="<ofbiz:url>/EditProductCategories?PRODUCT_ID=<%=productId%></ofbiz:url>" class="buttontext">[Edit Category Members]</a>
  <a href="<ofbiz:url>/EditProductKeyword?PRODUCT_ID=<%=productId%></ofbiz:url>" class="buttontext">[Edit Keywords]</a>
  <a href="<ofbiz:url>/EditProductAssoc?PRODUCT_ID=<%=productId%></ofbiz:url>" class="buttontext">[Edit Associations]</a>
  <a href="<ofbiz:url>/EditProductAttributes?PRODUCT_ID=<%=productId%></ofbiz:url>" class="buttontext">[Edit Attributes]</a>
<%}%>
<br>
<br>
<%-- Edit 'ProductCategoryMember's --%>
<%if(productId!=null && product!=null){%>
<p class="head2">Product-Category Member Maintenance</p>

<table border="1" cellpadding='2' cellspacing='0'>
  <tr>
    <td><div class="tabletext"><b>Attribute Name</b></div></td>
    <td><div class="tabletext"><b>Attribute Value, Type</b></div></td>
  </tr>
<ofbiz:iterator name="productAttribute" property="productAttributes">
  <tr valign="middle">
    <td><div class='tabletext'><ofbiz:entityfield attribute="productAttribute" field="attrName"/></div></td>
    <td>
        <FORM method=POST action='<ofbiz:url>/UpdateProductAttribute?UPDATE_MODE=UPDATE</ofbiz:url>'>
            <input type=hidden name='PRODUCT_ID' value='<ofbiz:entityfield attribute="productAttribute" field="productId"/>'>
            <input type=hidden name='ATTRIBUTE_NAME' value='<ofbiz:entityfield attribute="productAttribute" field="attrName"/>'>
            <input type=text size='60' name='ATTRIBUTE_VALUE' value='<ofbiz:inputvalue entityAttr="productAttribute" field="attrValue"/>'>
            <input type=text size='20' name='ATTRIBUTE_TYPE' value='<ofbiz:inputvalue entityAttr="productAttribute" field="attrType"/>'>
            <INPUT type=submit value='Update'>
        </FORM>
    </td>
    <td>
      <a href='<ofbiz:url>/UpdateProductAttribute?UPDATE_MODE=DELETE&PRODUCT_ID=<ofbiz:entityfield attribute="productCategoryMember" field="productId"/>&ATTRIBUTE_NAME=<ofbiz:entityfield attribute="productAttribute" field="attrName"/></ofbiz:url>' class="buttontext">
      [Delete]</a>
    </td>
  </tr>
</ofbiz:iterator>
</table>
<br>
<form method="POST" action="<ofbiz:url>/UpdateProductAttribute</ofbiz:url>" style='margin: 0;'>
  <input type="hidden" name="PRODUCT_ID" value="<%=productId%>">
  <input type="hidden" name="UPDATE_MODE" value="CREATE">
  <input type="hidden" name="useValues" value="true">

  <div class='head2'>Add ProductAttribute (enter Name, Value and Type):</div>
  <br>
  <input type="text" name="ATTRIBUTE_NAME" size="20">&nbsp;
  <input type="text" name="ATTRIBUTE_VALUE" size="60">&nbsp;
  <input type="text" name="ATTRIBUTE_TYPE" size="20">&nbsp;
  <input type="submit" value="Add">
</form>
<%}%>
<br>
<a href="<ofbiz:url>/EditProduct</ofbiz:url>" class="buttontext">[Create New Product]</a>
<%-- <%if(product != null){%><a href="<ofbiz:url>UpdateProduct?UPDATE_MODE=DELETE&PRODUCT_ID=<%=product.getSku()%></ofbiz:url>" class="buttontext">[Delete this Product]</a><%}%> --%>
<%if(productId != null && productId.length() > 0){%>
  <a href="/ecommerce/control/product?product_id=<%=productId%>" class='buttontext' target='_blank'>[View Product Page]</a>
  <a href="<ofbiz:url>/EditProduct?PRODUCT_ID=<%=productId%></ofbiz:url>" class="buttontext">[Edit Product]</a>
  <a href="<ofbiz:url>/EditProductCategories?PRODUCT_ID=<%=productId%></ofbiz:url>" class="buttontext">[Edit Category Members]</a>
  <a href="<ofbiz:url>/EditProductKeyword?PRODUCT_ID=<%=productId%></ofbiz:url>" class="buttontext">[Edit Keywords]</a>
  <a href="<ofbiz:url>/EditProductAssoc?PRODUCT_ID=<%=productId%></ofbiz:url>" class="buttontext">[Edit Associations]</a>
  <a href="<ofbiz:url>/EditProductAttributes?PRODUCT_ID=<%=productId%></ofbiz:url>" class="buttontext">[Edit Attributes]</a>
<%}%>
<br>

<%}else{%>
  <h3>You do not have permission to view this page. ("CATALOG_VIEW" or "CATALOG_ADMIN" needed)</h3>
<%}%>
