<#--
 *  Copyright (c) 2001, 2002 The Open For Business Project - www.ofbiz.org
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
 *@author     Andy Zeneski (jaz@ofbiz.org)
 *@version    $Revision$
 *@since      2.1
-->

<div class='head1'>
    Search Results for "${requestAttributes.keywordString?if_exists}"
    where <#if requestParameters.SEARCH_OPERATOR?upper_case == "OR">any keyword<#else>all keywords</#if> matched.     
</div>

<#if !requestAttributes.searchProductList?has_content>
  <br><div class='head2'>&nbsp;No results found.</div>
</#if>

<#if requestAttributes.searchProductList?has_content>
<table border="0" width="100%" cellpadding="2">
    <tr>
      <td align=right>
        <b>
        <#if 0 < requestAttributes.viewIndex?int>
          <a href="<@ofbizUrl>/keywordsearch/${requestAttributes.prevStr}</@ofbizUrl>" class="buttontext">[Previous]</a> |
        </#if>
        <#if 0 < requestAttributes.listSize?int>
          <span class="tabletext">${requestAttributes.lowIndex} - ${requestAttributes.highIndex} of ${requestAttributes.listSize}</span>
        </#if>
        <#if requestAttributes.highIndex?int < requestAttributes.listSize?int>      
          | <a href="<@ofbizUrl>/keywordsearch/${requestAttributes.nextStr}</@ofbizUrl>" class="buttontext">[Next]</a>
        </#if>
        </b>
      </td>
    </tr>
</table>
</#if>

<#if requestAttributes.searchProductList?has_content>
<center>
  <table width='100%' cellpadding='0' cellspacing='0'>
    <#assign listIndex = requestAttributes.lowIndex>
    <#list requestAttributes.searchProductList as product>
    ${setRequestAttribute("optProductId", product.productId)}
    ${setRequestAttribute("listIndex", listIndex)}
      <tr><td colspan="2"><hr class='sepbar'></td></tr>
      <tr>
        <td>
          ${pages.get("/catalog/productsummary.ftl")}
        </td>
      </tr>
      <#assign listIndex = listIndex + 1>
    </#list>
  </table>
</center>
</#if>

<#if requestAttributes.searchProductList?has_content>
<table border="0" width="100%" cellpadding="2">
    <tr><td colspan="2"><hr class='sepbar'></td></tr>
    <tr>
      <td align=right>
        <b>
        <#if 0 < requestAttributes.viewIndex?int>
          <a href="<@ofbizUrl>/keywordsearch?${requestAttributes.prevStr}</@ofbizUrl>" class="buttontext">[Previous]</a> |
        </#if>
        <#if 0 < requestAttributes.listSize?int>
          <span class="tabletext">${requestAttributes.lowIndex} - ${requestAttributes.highIndex} of ${requestAttributes.listSize}</span>
        </#if>
        <#if requestAttributes.highIndex?int < requestAttributes.listSize?int>      
          | <a href="<@ofbizUrl>/keywordsearch?${requestAttributes.nextStr}</@ofbizUrl>" class="buttontext">[Next]</a>
        </#if>
        </b>
      </td>
    </tr>
</table>
</#if>

