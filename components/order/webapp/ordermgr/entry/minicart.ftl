<#--
 *  Copyright (c) 2003 The Open For Business Project - www.ofbiz.org
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
 *@author     David E. Jones (jonesde@ofbiz.org)
 *@author     Andy Zeneski (jaz@ofbiz.org)
 *@version    $Revision: 1.2 $
 *@since      2.1
-->

<#assign shoppingCart = sessionAttributes.shoppingCart?if_exists>
<#assign currencyUomId = shoppingCart.getCurrency()>
<#if shoppingCart?has_content>
    <#assign shoppingCartSize = shoppingCart.size()>
<#else>
    <#assign shoppingCartSize = 0>
</#if>
    
<table border=0 width='100%' cellspacing='0' cellpadding='0' class='boxoutside'>
  <tr>
    <td width='100%'>
      <table width='100%' border='0' cellspacing='0' cellpadding='0' class='boxtop'>
        <tr>
          <td valign=middle align=center>
      <div class='boxhead'><b>Order&nbsp;Summary</b></div>
          </td>
        </tr>
      </table>
    </td>
  </tr>
  <tr>
    <td width='100%'>
      <table width='100%' border='0' cellspacing='0' cellpadding='0' class='boxbottom'>
        <tr>
          <td>
            <table width="100%" border="0" cellpadding="2" cellspacing="0">
              <#if (shoppingCartSize > 0)>             
                <tr>
                  <td valign="bottom"><div class="tabletext"><b>#<b></div></td>
                  <td valign="bottom"><div class="tabletext"><b>Item<b></div></td>
                  <td valign="bottom"><div class="tabletext"><b>Subtotal<b></div></td>
                </tr>
                <#list shoppingCart.items() as cartLine>
                  <tr>
                    <td valign="top"><div class="tabletext" nowrap>${cartLine.getQuantity()?string.number}</div></td>                    
                    <td valign="top">
                      <#if cartLine.getProductId()?exists>
                        <div><a href="<@ofbizUrl>/product?product_id=${cartLine.getProductId()}</@ofbizUrl>" class="buttontext">${cartLine.getName()}</a></div>
                      <#else>
                        <div class="tabletext"><b>${cartLine.getItemTypeDescription()?if_exists}</b></div>
                      </#if>
                    </td>
                    <td align="right" valign="top"><div class="tabletext" nowrap><@ofbizCurrency amount=cartLine.getItemSubTotal() isoCode=currencyUomId/></div></td>
                  </tr>
                </#list>
                <tr>
                  <td colspan="3" align="right">
                    <div class="tabletext"><b>Total: <@ofbizCurrency amount=shoppingCart.getGrandTotal() isoCode=currencyUomId/></b></div>
                  </td>
                </tr>                
              <#else>
                <tr>
                  <td nowrap colspan="3"><div class="tabletext">No order items selected.</div></td>
                </tr>
              </#if>
            </table>
          </td>
        </tr>
      </table>
    </td>
  </tr>
</table>

