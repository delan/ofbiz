<#--
 *  Copyright (c) 2003-2005 The Open For Business Project - www.ofbiz.org
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
 *@author     Brad Steiner (bsteiner@thehungersite.com)
 *@author     Catherine.Heintz@nereide.biz (migration to UiLabel)
 *@version    $Rev$
 *@since      2.2
-->
  <#if activeOnly>
    <a href="<@ofbizUrl>EditProductStorePromos?productStoreId=${productStoreId}&userEntered=${userEntered?string}&activeOnly=false</@ofbizUrl>" class="buttontext">[${uiLabelMap.CommonActiveInactive}]</a>
  <#else>
    <a href="<@ofbizUrl>EditProductStorePromos?productStoreId=${productStoreId}&userEntered=${userEntered?string}</@ofbizUrl>" class="buttontext">[${uiLabelMap.CommonActiveOnly}]</a>
  </#if>
  <#if userEntered>
    <a href="<@ofbizUrl>EditProductStorePromos?productStoreId=${productStoreId}&activeOnly=${activeOnly?string}&userEntered=false</@ofbizUrl>" class="buttontext">[${uiLabelMap.CommonUserAutoEntered}]</a>
  <#else>
    <a href="<@ofbizUrl>EditProductStorePromos?productStoreId=${productStoreId}&activeOnly=${activeOnly?string}</@ofbizUrl>" class="buttontext">[${uiLabelMap.CommonUserEnteredOnly}]</a>
  </#if>
  <br/>
  <div class="head3">${uiLabelMap.CommonShowing}
    <#if activeOnly>${uiLabelMap.CommonActiveOnly}<#else>${uiLabelMap.CommonActiveInactive}</#if>
    ${uiLabelMap.CommonAnd}
    <#if userEntered>${uiLabelMap.CommonUserEnteredOnly}<#else>${uiLabelMap.CommonUserAutoEntered}</#if>
  </div>

    <#if productStoreId?exists && productStore?exists>
        <#if (listSize > 0)>
            <table border="0" cellpadding="2">
                <tr>
                <td align="right">
                    <b>
                    <#if (viewIndex > 0)>
                    <a href="<@ofbizUrl>EditProductStorePromos?productStoreId=${productStoreId}&VIEW_SIZE=${viewSize}&VIEW_INDEX=${viewIndex-1}&activeOnly=${activeOnly.toString()}&userEntered=${userEntered.toString()}</@ofbizUrl>" class="buttontext">[${uiLabelMap.CommonPrevious}]</a> |
                    </#if>
                    <#if (listSize > 0)>
                        <span class="tabletext">${lowIndex+1} - ${highIndex} ${uiLabelMap.CommonOf} ${listSize}</span>
                    </#if>
                    <#if (listSize > highIndex)>
                    | <a href="<@ofbizUrl>EditProductStorePromos?productStoreId=${productStoreId}&VIEW_SIZE=${viewSize}&VIEW_INDEX=${viewIndex+1}&activeOnly=${activeOnly.toString()}&userEntered=${userEntered.toString()}</@ofbizUrl>" class="buttontext">[${uiLabelMap.CommonNext}]</a>
                    </#if>
                    </b>
                </td>
                </tr>
            </table>
        </#if>
        
        <table border="1" cellpadding="2" cellspacing="0">
        <tr>
            <td><div class="tabletext"><b>${uiLabelMap.ProductPromoNameId}</b></div></td>
            <td><div class="tabletext"><b>${uiLabelMap.CommonFromDateTime}</b></div></td>
            <td align="center"><div class="tabletext"><b>${uiLabelMap.ProductThruDateTimeSequence}</b></div></td>
            <td><div class="tabletext"><b>&nbsp;</b></div></td>
        </tr>
        <#if (listSize > 0)>
	        <#assign line = 0>
	        <#list productStorePromoAndAppls[lowIndex..highIndex-1] as productStorePromoAndAppl>
	        <#assign line = line+1>
	        <tr valign="middle">
	            <td><a href="<@ofbizUrl>EditProductPromo?productPromoId=${(productStorePromoAndAppl.productPromoId)?if_exists}</@ofbizUrl>" class="buttontext">${(productStorePromoAndAppl.promoName)?if_exists} [${(productStorePromoAndAppl.productPromoId)?if_exists}]</a></td>
	            <#assign hasntStarted = false>
	            <#if productStorePromoAndAppl.getTimestamp("fromDate")?exists && Static["org.ofbiz.base.util.UtilDateTime"].nowTimestamp().before(productStorePromoAndAppl.getTimestamp("fromDate"))> <#assign hasntStarted = true> </#if>
	            <td><div class="tabletext" <#if hasntStarted> style="color: red;"</#if> >${productStorePromoAndAppl.getTimestamp("fromDate").toString()}</div></td>
	            <td align="center">
	                <#assign hasExpired = false>
	                <#if productStorePromoAndAppl.getTimestamp("thruDate")?exists && Static["org.ofbiz.base.util.UtilDateTime"].nowTimestamp().after(productStorePromoAndAppl.getTimestamp("thruDate"))> <#assign hasExpired = true></#if>
	                <FORM method="post" action="<@ofbizUrl>updateProductStorePromoAppl</@ofbizUrl>" name="lineForm${line}">
	                    <input type="hidden" name="productStoreId" value="${(productStorePromoAndAppl.productStoreId)?if_exists}">
	                    <input type="hidden" name="productPromoId" value="${(productStorePromoAndAppl.productPromoId)?if_exists}">
	                    <input type="hidden" name="fromDate" value="${(productStorePromoAndAppl.fromDate)?if_exists}">
	                    <input type="text" size="25" name="thruDate" value="${(productStorePromoAndAppl.thruDate)?if_exists}" class="inputBox" style="<#if (hasExpired) >color: red;</#if>">
	                    <a href="javascript:call_cal(document.lineForm${line}.thruDate, '${(productStorePromoAndAppl.thruDate)?default(nowTimestampString)}');"><img src="/images/cal.gif" width="16" height="16" border="0" alt="Calendar"></a>
	                    <input type="text" size="5" name="sequenceNum" value="${(productStorePromoAndAppl.sequenceNum)?if_exists}" class="inputBox">
	                    <INPUT type="submit" value="${uiLabelMap.CommonUpdate}" style="font-size: x-small;">
	                </FORM>
	            </td>
	            <td align="center">
	            <a href="<@ofbizUrl>deleteProductStorePromoAppl?productStoreId=${(productStorePromoAndAppl.productStoreId)?if_exists}&productPromoId=${(productStorePromoAndAppl.productPromoId)?if_exists}&fromDate=${Static["org.ofbiz.base.util.UtilFormatOut"].encodeQueryValue(productStorePromoAndAppl.getTimestamp("fromDate").toString())}</@ofbizUrl>" class="buttontext">
	            [${uiLabelMap.CommonDelete}]</a>
	            </td>
	        </tr>
	        </#list>
        </#if>
        </table>
        <#if (listSize > 0)>
            <table border="0" cellpadding="2">
                <tr>
                <td align="right">
                    <b>
                    <#if (viewIndex > 0)>
                    <a href="<@ofbizUrl>EditProductStorePromos?productStoreId=${productStoreId}&VIEW_SIZE=${viewSize}&VIEW_INDEX=${viewIndex-1}&activeOnly=${activeOnly.toString()}&userEntered=${userEntered.toString()}</@ofbizUrl>" class="buttontext">[${uiLabelMap.CommonPrevious}]</a> |
                    </#if>
                    <#if (listSize > 0)>
                        <span class="tabletext">${lowIndex+1} - ${highIndex} ${uiLabelMap.CommonOf} ${listSize}</span>
                    </#if>
                    <#if (listSize > highIndex)>
                    | <a href="<@ofbizUrl>EditProductStorePromos?productStoreId=${productStoreId}&VIEW_SIZE=${viewSize}&VIEW_INDEX=${viewIndex+1}&activeOnly=${activeOnly.toString()}&userEntered=${userEntered.toString()}</@ofbizUrl>" class="buttontext">[${uiLabelMap.CommonNext}]</a>
                    </#if>
                    </b>
                </td>
                </tr>
            </table>
        </#if>
        
        <br/>
        <form method="post" action="<@ofbizUrl>createProductStorePromoAppl</@ofbizUrl>" style="margin: 0;" name="addNewForm">
        <input type="hidden" name="productStoreId" value="${productStoreId?if_exists}"/>
        <input type="hidden" name="tryEntity" value="true"/>
        
        <div class="head2">${uiLabelMap.ProductAddStorePromoOptionalDate}:</div>
        <br/>
        <select name="productPromoId" class="selectBox">
        <#list productPromos as productPromo>
            <option value="${productPromo.productPromoId?if_exists}">${productPromo.promoName?if_exists} [${productPromo.productPromoId?if_exists}]</option>
        </#list>
        </select> <span class="tabletext">${uiLabelMap.ProductNoteUserPromotionEntered}</span> 
        <br/>
        <input type="text" size="25" name="fromDate" class="inputBox"/>
        <a href="javascript:call_cal(document.addNewForm.fromDate, '${nowTimestampString}');"><img src="/images/cal.gif" width="16" height="16" border="0" alt="Calendar"/></a>
        <input type="submit" value="${uiLabelMap.CommonAdd}"/>
        </form>
    </#if>
