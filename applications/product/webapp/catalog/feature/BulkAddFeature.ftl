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
-->

<div class="head1">${uiLabelMap.ProductAddProductFeatureInBulk} ${uiLabelMap.CommonFor} ${featureCategory.description}</div>

<table border="1" cellpadding='2' cellspacing='0'>
  <form method='POST' action='<@ofbizUrl>BulkAddProductFeatures</@ofbizUrl>' name="selectAllForm">
  <input type="hidden" name="_useRowSubmit" value="Y">
  <input type="hidden" name="_checkGlobalScope" value="N">
  <input type="hidden" name="productFeatureCategoryId" value="${productFeatureCategoryId}">
  <tr class='viewOneTR1'>
    <td><div class="tabletext"><b>${uiLabelMap.CommonDescription}</b></div></td>
    <td><div class="tabletext"><b>${uiLabelMap.ProductFeatureType}</b></div></td>
    <td><div class="tabletext"><b>${uiLabelMap.ProductIdSeqNum}</b></div></td>
    <td><div class="tabletext"><b>${uiLabelMap.ProductIdCode}</b></div></td>
    <td><div class="tabletext"><b>${uiLabelMap.CommonAll}<input type="checkbox" name="selectAll" value="${uiLabelMap.CommonY}" onclick="javascript:toggleAll(this, 'selectAllForm');"></div></td>
  </tr>
<#list 0..featureNum-1 as feature>
  <tr valign="middle" class='viewOneTR1'>
      <td><input type="text" class='inputBox' size='15' name="description_o_${feature_index}"></td>
      <td><select name='productFeatureTypeId_o_${feature_index}' size="1" class='selectBox'>
        <#list productFeatureTypes as productFeatureType>
          <option value='${productFeatureType.productFeatureTypeId}'>${productFeatureType.description?if_exists}</option>
        </#list>
      </select></td>
      <input name='productFeatureCategoryId_o_${feature_index}' type="hidden" value="${productFeatureCategoryId}">
      <td><input type="text" class='inputBox' size='5' name="defaultSequenceNum_o_${feature_index}""></td>
      <td><input type="text" class='inputBox' size='5' name="idCode_o_${feature_index}"></td>
      <td align="right"><input type="checkbox" name="_rowSubmit_o_${feature_index}" value="Y" onclick="javascript:checkToggle(this, 'selectAllForm');"></td>
  </tr>

</#list>
<input type="hidden" name="_rowCount" value="${featureNum}">
<tr><td colspan="11" align="center"><input type="submit" value='${uiLabelMap.CommonCreate}'/></td></tr>
</form>
</table>
