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
 *@author     Olivier.Heintz@nereide.biz
 *@version    $Revision: 1.1 $
 *@since      3.0
-->

<#assign uiLabelMap = requestAttributes.uiLabelMap>
<#assign unselectedClassName = "tabButton">
<#assign selectedClassMap = {page.tabButtonItem?default("void") : "tabButtonSelected"}>

<div class='tabContainer'>
        <a href="<@ofbizUrl>/findParts</@ofbizUrl>" class="${selectedClassMap.findParts?default(unselectedClassName)}">${uiLabelMap.ManufacturingParts}</a>
        <a href="<@ofbizUrl>/findBom</@ofbizUrl>" class="${selectedClassMap.findBom?default(unselectedClassName)}">${uiLabelMap.ManufacturingBillOfMaterials}</a>
        <a href="<@ofbizUrl>/EditProductBom</@ofbizUrl>" class="${selectedClassMap.EditProductBom?default(unselectedClassName)}">${uiLabelMap.ManufacturingEditProductBom}</a>
        <a href="<@ofbizUrl>/EditProductManufacturingRules</@ofbizUrl>" class="${selectedClassMap.productManufacturingRules?default(unselectedClassName)}">${uiLabelMap.ManufacturingManufacturingRules}</a>
</div>
