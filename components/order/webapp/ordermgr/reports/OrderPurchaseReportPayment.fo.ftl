<?xml version="1.0" encoding="UTF-8" ?>
<fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">

<#--
 *  Copyright (c) 2004 The Open For Business Project - www.ofbiz.org
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
 *@version    $Rev$
 *@since      3.2
-->

<#if (requestAttributes.uiLabelMap)?exists><#assign uiLabelMap = requestAttributes.uiLabelMap></#if>

<#-- do not display columns associated with values specified in the request, ie constraint values -->
<#assign showProductStore = !parameters.productStoreId?has_content>
<#assign showOriginFacility = !parameters.originFacilityId?has_content>
<#assign showTerminal = !parameters.terminalId?has_content>
<#assign showStatus = !parameters.statusId?has_content>

<fo:layout-master-set>
    <fo:simple-page-master master-name="main" page-height="11in" page-width="8.5in"
            margin-top="0.5in" margin-bottom="1in" margin-left="0.5in" margin-right="0.5in">
        <fo:region-body margin-top="1in"/>
        <fo:region-before extent="1in"/>
        <fo:region-after extent="1in"/>
    </fo:simple-page-master>
</fo:layout-master-set>
<#if security.hasEntityPermission("ORDERMGR", "_VIEW", session)>

<#if orderPurchasePaymentSummaryList?has_content>
        <fo:page-sequence master-reference="main">
        <fo:flow flow-name="xsl-region-body" font-family="Helvetica">
            <fo:block font-size="14pt">Purchase by Product Summary</fo:block>
            <#if !showProductStore><fo:block font-size="10pt">For Product Store: ${parameters.productStoreId}</fo:block></#if>
            <#if !showOriginFacility><fo:block font-size="10pt">For Origin Facility: ${parameters.originFacilityId}</fo:block></#if>
            <#if !showTerminal><fo:block font-size="10pt">For Terminal: ${parameters.terminalId}</fo:block></#if>
            <#if !showStatus><fo:block font-size="10pt">For Status: ${parameters.statusId}</fo:block></#if>
            <#if parameters.fromOrderDate?has_content><fo:block font-size="10pt">From Date: ${parameters.fromOrderDate} (orderDate &gt;= from)</fo:block></#if>
            <#if parameters.thruOrderDate?has_content><fo:block font-size="10pt">Thru Date: ${parameters.thruOrderDate} (orderDate &lt; from)</fo:block></#if>
            <fo:block space-after.optimum="10pt" font-size="10pt">
            <fo:table>
                <#if showProductStore><fo:table-column column-width="50pt"/></#if>
                <#if showOriginFacility><fo:table-column column-width="80pt"/></#if>
                <#if showTerminal><fo:table-column column-width="50pt"/></#if>
                <#if showStatus><fo:table-column column-width="110pt"/></#if>
                <fo:table-column column-width="100pt"/>
                <fo:table-column column-width="60pt"/>
                <fo:table-header>
                    <fo:table-row font-weight="bold">
                        <#if showProductStore><fo:table-cell border-bottom="thin solid grey"><fo:block>Store ID</fo:block></fo:table-cell></#if>
                        <#if showOriginFacility><fo:table-cell border-bottom="thin solid grey"><fo:block>Facility ID</fo:block></fo:table-cell></#if>
                        <#if showTerminal><fo:table-cell border-bottom="thin solid grey"><fo:block>Terminal ID</fo:block></fo:table-cell></#if>
                        <#if showStatus><fo:table-cell border-bottom="thin solid grey"><fo:block>Status ID</fo:block></fo:table-cell></#if>
                        <fo:table-cell border-bottom="thin solid grey"><fo:block>Payment Method</fo:block></fo:table-cell>
                        <fo:table-cell border-bottom="thin solid grey"><fo:block>Amount</fo:block></fo:table-cell>
                    </fo:table-row>
                </fo:table-header>
                <fo:table-body>
                    <#assign rowColor = "white">
                    <#list orderPurchasePaymentSummaryList as orderPurchasePaymentSummary>
                        <fo:table-row>
                            <#if showProductStore>
                                <fo:table-cell padding="2pt" background-color="${rowColor}">
                                    <fo:block>${orderPurchasePaymentSummary.productStoreId?if_exists}</fo:block>
                                </fo:table-cell>
                            </#if>
                            <#if showOriginFacility>
                                <fo:table-cell padding="2pt" background-color="${rowColor}">
                                    <fo:block>${orderPurchasePaymentSummary.originFacilityId?if_exists}</fo:block>
                                </fo:table-cell>
                            </#if>
                            <#if showTerminal>
                                <fo:table-cell padding="2pt" background-color="${rowColor}">
                                    <fo:block>${orderPurchasePaymentSummary.terminalId?if_exists}</fo:block>
                                </fo:table-cell>
                            </#if>
                            <#if showStatus>
                                <fo:table-cell padding="2pt" background-color="${rowColor}">
                                    <fo:block>${orderPurchasePaymentSummary.statusId?if_exists}</fo:block>
                                </fo:table-cell>
                            </#if>
                            <fo:table-cell padding="2pt" background-color="${rowColor}">
                                <fo:block>${orderPurchasePaymentSummary.description?if_exists}</fo:block>
                            </fo:table-cell>
                            <fo:table-cell padding="2pt" background-color="${rowColor}">
                                <fo:block>${orderPurchasePaymentSummary.maxAmount?if_exists}</fo:block>
                            </fo:table-cell>
                        </fo:table-row>
                        <#-- toggle the row color -->
                        <#if rowColor == "white">
                            <#assign rowColor = "#D4D0C8">
                        <#else>
                            <#assign rowColor = "white">
                        </#if>        
                    </#list>          
                </fo:table-body>
            </fo:table>
            </fo:block>
        </fo:flow>
        </fo:page-sequence>
<#else>
    <fo:page-sequence master-reference="main">
    <fo:flow flow-name="xsl-region-body" font-family="Helvetica">
        <fo:block font-size="14pt">
            No Purchase Payment Method Information Found.
        </fo:block>
    </fo:flow>
    </fo:page-sequence>
</#if>

<#else>
    <fo:block font-size="14pt">
        ${uiLabelMap.OrderViewPermissionError}
    </fo:block>
</#if>

</fo:root>
