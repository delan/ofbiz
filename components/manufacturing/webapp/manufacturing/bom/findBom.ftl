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
 *@author     Jacopo Cappellato (tiz@sastau.it)
 *@version    $Revision: 1.1 $
 *@since      3.0
-->

<#assign uiLabelMap = requestAttributes.uiLabelMap>

<script language="JavaScript">
<!-- //
function lookupBom() {
    document.lookupbom.submit();
}
// -->
</script>


<#if security.hasEntityPermission("MANUFACTURING", "_VIEW", session)>
<form method='post' name="lookupbom" action="<@ofbizUrl>/findBom</@ofbizUrl>">
<input type='hidden' name='lookupFlag' value='Y'>
<input type='hidden' name='hideFields' value='Y'>
<table border=0 width='100%' cellspacing='0' cellpadding='0' class='boxoutside'>
  <tr>
    <td width='100%'>
      <table width='100%' border='0' cellspacing='0' cellpadding='0' class='boxtop'>
        <tr>
          <td><div class='boxhead'>${uiLabelMap.ManufacturingFindBillOfMaterials}</div></td>
          <td align='right'>
            <div class="tabletext">
              <#if requestParameters.hideFields?default("N") == "Y">
                <a href="<@ofbizUrl>/findBom?hideFields=N${paramList}</@ofbizUrl>" class="submenutextright">${uiLabelMap.CommonShowLookupFields}</a>
              <#else>
                <#if tree?exists>
                    <a href="<@ofbizUrl>/findBom?hideFields=Y${paramList}</@ofbizUrl>" class="submenutext">${uiLabelMap.CommonHideFields}</a>
                </#if>
                <a href="javascript:lookupBom();" class="submenutextright">${uiLabelMap.CommonLookup}</a>                
              </#if>
            </div>
          </td>
        </tr>
      </table>
      <#if requestParameters.hideFields?default("N") != "Y">
      <table width='100%' border='0' cellspacing='0' cellpadding='2' class='boxbottom'>
        <tr>
          <td align='center' width='100%'>
            <table border='0' cellspacing='0' cellpadding='2'>
              <tr>
                <td width='25%' align='right'><div class='tableheadtext'>${uiLabelMap.ProductProductId}:</div></td>
                <td width='5%'>&nbsp;</td>
                <td>
                  <input type='text' size='25' class='inputBox' name='productId' value='${requestParameters.productId?if_exists}'>
                </td>
              </tr>
              <tr>
                <td width='25%' align='right'><div class='tableheadtext'>${uiLabelMap.ManufacturingPartId}:</div></td>
                <td width='5%'>&nbsp;</td>
                <td>
                  <input type='text' size='25' class='inputBox' name='partId' value='${requestParameters.partId?if_exists}'>
                  <span class='tabletext'><a href="<@ofbizUrl>/findParts</@ofbizUrl>" class="buttontext">${uiLabelMap.CommonLookup}</a></span>
                </td>
              </tr>
              <tr>
                <td width='25%' align='right'><div class='tableheadtext'>${uiLabelMap.ManufacturingPartBOMType}:</div></td>
                <td width='5%'>&nbsp;</td>
                <td>
                  <select name='partBomTypeId' class='selectBox'>
                    <#if currentPartBomTypeId?has_content>
                    <option value="${currentPartBomTypeId.productAssocTypeId}">${currentPartBomTypeId.description}</option>
                    <option value="${currentPartBomTypeId.productAssocTypeId}">---</option>
                    </#if>                                     
                    <#list bomTypes as oneBomType>
                      <option value="${oneBomType.productAssocTypeId}">${oneBomType.description}</option>
                    </#list>
                  </select>
                </td>
              </tr>              
              <tr>                      
                <td width='25%' align='right'><div class='tableheadtext'>${uiLabelMap.CommonFromDate}:</div></td>
                <td width='5%'>&nbsp;</td>
                <td>                        
                  <input type='text' size='25' class='inputBox' name='fromDate' value='${requestParameters.fromDate?if_exists}'>
                  <a href="javascript:call_cal(document.lookupbom.fromDate, '${fromDateStr}');"><img src='/images/cal.gif' width='16' height='16' border='0' alt='Calendar'></a>
                </td>
              </tr>
              <tr>
                <td width='25%' align='right'><div class='tableheadtext'>${uiLabelMap.ManufacturingQuantity}:</div></td>
                <td width='5%'>&nbsp;</td>
                <td>
                  <input type='text' size='25' class='inputBox' name='rootQuantity' value='${requestParameters.rootQuantity?default("1")}'>
                </td>
              </tr>
            </table>
          </td>
        </tr>
      </table>
      </#if>
    </td>
  </tr>
</table>
<input type="image" src="/images/spacer.gif" onClick="javascript:lookupBom();">
</form> 

<#if requestParameters.hideFields?default("N") != "Y">
<script language="JavaScript">
<!--//
document.lookupbom.partId.focus();
//-->
</script>
</#if>

<#if requestParameters.lookupFlag?default("N") == "Y">
<br>
<table border=0 width='100%' cellspacing='0' cellpadding='0' class='boxoutside'>
  <tr>
    <td width='100%'>
      <table width='100%' border='0' cellspacing='0' cellpadding='0' class='boxtop'>
        <tr>
          <td width="50%"><div class="boxhead">${uiLabelMap.ManufacturingPartBreakdown}</div></td>
          <td width="50%">
          </td>
        </tr>
      </table>
      <table width='100%' border='0' cellspacing='0' cellpadding='2' class='boxbottom'>
        <tr>
          <td width="10%" align="left"><div class="tableheadtext">${uiLabelMap.ManufacturingPartDepth}</div></td>
          <td width="30%" align="left"><div class="tableheadtext">${uiLabelMap.ManufacturingPartId}</div></td>
          <td width="40%" align="left"><div class="tableheadtext">${uiLabelMap.ManufacturingPartName}</div></td>
          <td width="20%" align="right"><div class="tableheadtext">${uiLabelMap.ManufacturingQuantity}</div></td>
        </tr>
        <tr>
          <td colspan='4'><hr class='sepbar'></td>
        </tr>
        <#if tree?has_content>
          <#assign rowClass = "viewManyTR2">
          <#list tree as node>            
            <tr class='${rowClass}'>
              <td><img src='/manufacturing/images/depth${node.depth}.gif' height='16' border='0' alt='Depth'></td>
              <td>${node.part.productId}</td>
              <td>${node.part.productName?default("&nbsp;")}</td>
              <td align="right">${node.quantity}</td>
            </tr>
            <#-- toggle the row color -->
            <#if rowClass == "viewManyTR2">
              <#assign rowClass = "viewManyTR1">
            <#else>
              <#assign rowClass = "viewManyTR2">
            </#if>
          </#list>          
        <#else>
          <tr>
            <td colspan='4'><div class='head3'>${uiLabelMap.CommonNoElementFound}.</div></td>
          </tr>        
        </#if>
        <#if lookupErrorMessage?exists>
          <tr>
            <td colspan='4'><div class="head3">${lookupErrorMessage}</div></td>
          </tr>
        </#if>
      </table>
    </td>
  </tr>
</table>

<br>
<table border=0 width='100%' cellspacing='0' cellpadding='0' class='boxoutside'>
  <tr>
    <td width='100%'>
      <table width='100%' border='0' cellspacing='0' cellpadding='0' class='boxtop'>
        <tr>
          <td width="50%"><div class="boxhead">${uiLabelMap.ManufacturingSummarizedPartBreakdown}</div></td>
          <td width="50%">
          </td>
        </tr>
      </table>
      <table width='100%' border='0' cellspacing='0' cellpadding='2' class='boxbottom'>
        <tr>
          <td width="30%" align="left"><div class="tableheadtext">${uiLabelMap.ManufacturingPartId}</div></td>
          <td width="40%" align="left"><div class="tableheadtext">${uiLabelMap.ManufacturingPartName}</div></td>
          <td width="40%" align="right"><div class="tableheadtext">${uiLabelMap.ManufacturingQuantity}</div></td>
        </tr>
        <tr>
          <td colspan='3'><hr class='sepbar'></td>
        </tr>
        <#if treeQty?has_content>
          <#assign rowClass = "viewManyTR2">
          <#list treeQty as nodeQty>            
            <tr class='${rowClass}'>
              <td>${nodeQty.part.productId}</td>
              <td>${nodeQty.part.productName?default("&nbsp;")}</td>
              <td align="right">${nodeQty.quantity}</td>
            </tr>
            <#-- toggle the row color -->
            <#if rowClass == "viewManyTR2">
              <#assign rowClass = "viewManyTR1">
            <#else>
              <#assign rowClass = "viewManyTR2">
            </#if>
          </#list>          
        <#else>
          <tr>
            <td colspan='4'><div class='head3'>${uiLabelMap.ManufacturingNoElementFound}.</div></td>
          </tr>        
        </#if>
        <#if lookupErrorMessage?exists>
          <tr>
            <td colspan='4'><div class="head3">${lookupErrorMessage}</div></td>
          </tr>
        </#if>
      </table>
    </td>
  </tr>
</table>
        
</#if>



<#else>
  <h3>${uiLabelMap.ManufacturingViewPermissionError}</h3>
</#if>
