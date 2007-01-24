<#--
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
-->

<#-- 
Generates PDF of multiple checks in two styles: one check per page, multiple checks per page 
Note that this must be customized to fit specific check layouts. The layout here is copied
by hand from a real template using a ruler.
-->
<#escape x as x?xml>

<fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">

  <fo:layout-master-set>

    <#-- define the margins of the check layout here -->
    <fo:simple-page-master master-name="checks"
      page-height="27.9cm" page-width="21.6cm">
      <fo:region-body/>
    </fo:simple-page-master>

  </fo:layout-master-set>

  <fo:page-sequence master-reference="checks">
    <fo:flow flow-name="xsl-region-body">
      <#if !security.hasEntityPermission("ACCOUNTING", "_PRINT_CHECKS", session)>
      <fo:block padding="20pt">${uiLabelMap.AccountingPrintChecksPermissionError}</fo:block>
      <#else>
      <#if payments.size() == 0>
        <fo:block padding="20pt">${uiLabelMap.AccountingPaymentCheckMessage1}</fo:block>
      </#if>

      <#list payments as payment>
      <#assign paymentApplications = payment.getRelated("PaymentApplication")>
      <fo:block font-size="10pt" break-before="page"> <#-- this produces a page break if this block cannot fit on the current page -->

        <#-- the check: note that the format is fairly precise -->

        <fo:table height="8.85cm">
          <fo:table-column column-width="17.7cm"/>
          <fo:table-column/>
          <fo:table-body>
            <fo:table-row>
              <fo:table-cell/>
              <fo:table-cell>
                <fo:block padding-before="2.2cm">${payment.effectiveDate?date?string.short}</fo:block>
              </fo:table-cell>
            </fo:table-row>
            <fo:table-row>
              <fo:table-cell padding-before="0.8cm">
                <fo:block margin-left="3.0cm">
                  <#assign toPartyNameResult = dispatcher.runSync("getPartyNameForDate", Static["org.ofbiz.base.util.UtilMisc"].toMap("partyId", payment.partyIdTo, "compareDate", payment.effectiveDate, "userLogin", userLogin))/>
                  ${toPartyNameResult.fullName?default("Name Not Found")}
                </fo:block>
              </fo:table-cell>
              <fo:table-cell padding-before="0.8cm">
                <fo:block>**${payment.getBigDecimal("amount").setScale(decimals, rounding).toString()}</fo:block>
              </fo:table-cell>
            </fo:table-row>
            <fo:table-row>
              <fo:table-cell number-columns-spanned="2">
                <#assign amount = Static["org.ofbiz.base.util.UtilNumber"].formatRuleBasedAmount(payment.getDouble("amount"), "%dollars-and-hundredths", locale).toUpperCase()>
                <fo:block padding-before="0.4cm" margin-left="1.3cm">${amount}<#list 1..(100-amount.length()) as x>*</#list></fo:block>
              </fo:table-cell>
            </fo:table-row>
          </fo:table-body>
        </fo:table>

        <#-- payment applications (twice: both blocks are exactly the same) -->

        <fo:table height="9.3cm" margin="10pt">
          <fo:table-column/>
          <fo:table-column/>
          <fo:table-column/>
          <fo:table-column/>
          <fo:table-column/>
          <fo:table-column/>
          <fo:table-column/>
          <fo:table-header>
            <fo:table-row>
              <fo:table-cell padding="3pt" number-columns-spanned="3" text-align="center">
                <fo:block text-align="center">
                  <#assign toPartyNameResult = dispatcher.runSync("getPartyNameForDate", Static["org.ofbiz.base.util.UtilMisc"].toMap("partyId", payment.partyIdTo, "compareDate", payment.effectiveDate, "userLogin", userLogin))/>
                  ${toPartyNameResult.fullName?default("Name Not Found")}
                </fo:block>
              </fo:table-cell>
              <fo:table-cell padding="3pt" number-columns-spanned="4" text-align="center">
                <fo:block text-align="center">${payment.effectiveDate?date?string.short}</fo:block>
              </fo:table-cell>
            </fo:table-row>
            <fo:table-row>
              <fo:table-cell padding="3pt">
                <fo:block font-weight="bold">${uiLabelMap.CommonDate}</fo:block>
              </fo:table-cell>
              <fo:table-cell padding="3pt">
                <fo:block font-weight="bold">${uiLabelMap.CommonType}</fo:block>
              </fo:table-cell>
              <fo:table-cell padding="3pt">
                <fo:block font-weight="bold">${uiLabelMap.AccountingReferenceNumber}</fo:block>
              </fo:table-cell>
              <fo:table-cell padding="3pt"><fo:block/></fo:table-cell>
              <fo:table-cell padding="3pt">
                <fo:block font-weight="bold" text-align="right">${uiLabelMap.AccountingPaymentOriginalAmount}</fo:block>
              </fo:table-cell>
              <fo:table-cell padding="3pt">
                <fo:block font-weight="bold" text-align="right">${uiLabelMap.AccountingBalanceDue}</fo:block>
              </fo:table-cell>
              <fo:table-cell padding="3pt">
                <fo:block font-weight="bold" text-align="right">${uiLabelMap.AccountingPayment}</fo:block>
              </fo:table-cell>
            </fo:table-row>
          </fo:table-header>
          <fo:table-body>

            <#list paymentApplications as paymentApplication>
            <#assign invoice = paymentApplication.getRelatedOne("Invoice")?if_exists>
            <fo:table-row>
              <fo:table-cell padding="3pt">
                <fo:block>${payment.effectiveDate?date?string.short}</fo:block>
              </fo:table-cell>
              <fo:table-cell padding="3pt">
                <fo:block><#if invoice?exists>${uiLabelMap.AccountingInvoice}</#if></fo:block>
              </fo:table-cell>
              <fo:table-cell padding="3pt">
                <fo:block>
                  <#if invoice?exists>${invoice.referenceNumber?if_exists}</#if>
                  ${paymentApplication.taxAuthGeoId?if_exists}
                </fo:block>
              </fo:table-cell>
              <fo:table-cell padding="3pt"><fo:block/></fo:table-cell>
              <fo:table-cell padding="3pt"><fo:block/></fo:table-cell>
              <fo:table-cell padding="3pt"><fo:block/></fo:table-cell>
              <fo:table-cell padding="3pt">
                <fo:block text-align="end">${paymentApplication.getBigDecimal("amountApplied").setScale(decimals, rounding).toString()}</fo:block>
              </fo:table-cell>
            </fo:table-row>
            </#list>

            <fo:table-row>
              <fo:table-cell padding="3pt" number-columns-spanned="6">
                <fo:block text-align="end">${uiLabelMap.AccountingCheckAmount}</fo:block>
              </fo:table-cell>
              <fo:table-cell padding="3pt">
                <fo:block text-align="end">${payment.getBigDecimal("amount").setScale(decimals, rounding).toString()}</fo:block>
              </fo:table-cell>
            </fo:table-row>
          </fo:table-body>
        </fo:table>

        <#-- copy of above -->

        <fo:table height="9.3cm" margin="10pt">
          <fo:table-column/>
          <fo:table-column/>
          <fo:table-column/>
          <fo:table-column/>
          <fo:table-column/>
          <fo:table-column/>
          <fo:table-column/>
          <fo:table-header>
            <fo:table-row>
              <fo:table-cell padding="3pt" number-columns-spanned="3" text-align="center">
                <fo:block text-align="center">
                  ${Static["org.ofbiz.party.party.PartyHelper"].getPartyName(delegator, payment.partyIdTo, false)}
                </fo:block>
              </fo:table-cell>
              <fo:table-cell padding="3pt" number-columns-spanned="4" text-align="center">
                <fo:block text-align="center">${payment.effectiveDate?date?string.short}</fo:block>
              </fo:table-cell>
            </fo:table-row>
            <fo:table-row>
              <fo:table-cell padding="3pt">
                <fo:block font-weight="bold">${uiLabelMap.CommonDate}</fo:block>
              </fo:table-cell>
              <fo:table-cell padding="3pt">
                <fo:block font-weight="bold">${uiLabelMap.CommonType}</fo:block>
              </fo:table-cell>
              <fo:table-cell padding="3pt">
                <fo:block font-weight="bold">${uiLabelMap.AccountingReferenceNumber}</fo:block>
              </fo:table-cell>
              <fo:table-cell padding="3pt"><fo:block/></fo:table-cell>
              <fo:table-cell padding="3pt">
                <fo:block font-weight="bold" text-align="right">${uiLabelMap.AccountingPaymentOriginalAmount}</fo:block>
              </fo:table-cell>
              <fo:table-cell padding="3pt">
                <fo:block font-weight="bold" text-align="right">${uiLabelMap.AccountingBalanceDue}</fo:block>
              </fo:table-cell>
              <fo:table-cell padding="3pt">
                <fo:block font-weight="bold" text-align="right">${uiLabelMap.AccountingPayment}</fo:block>
              </fo:table-cell>
            </fo:table-row>
          </fo:table-header>
          <fo:table-body>

            <#list paymentApplications as paymentApplication>
            <#assign invoice = paymentApplication.getRelatedOne("Invoice")?if_exists>
            <fo:table-row>
              <fo:table-cell padding="3pt">
                <fo:block>${payment.effectiveDate?date?string.short}</fo:block>
              </fo:table-cell>
              <fo:table-cell padding="3pt">
                <fo:block><#if invoice?exists>${uiLabelMap.AccountingInvoice}</#if></fo:block>
              </fo:table-cell>
              <fo:table-cell padding="3pt">
                <fo:block>
                  <#if invoice?exists>${invoice.referenceNumber?if_exists}</#if>
                  ${paymentApplication.taxAuthGeoId?if_exists}
                </fo:block>
              </fo:table-cell>
              <fo:table-cell padding="3pt"><fo:block/></fo:table-cell>
              <fo:table-cell padding="3pt"><fo:block/></fo:table-cell>
              <fo:table-cell padding="3pt"><fo:block/></fo:table-cell>
              <fo:table-cell padding="3pt">
                <fo:block text-align="end">${paymentApplication.getBigDecimal("amountApplied").setScale(decimals, rounding).toString()}</fo:block>
              </fo:table-cell>
            </fo:table-row>
            </#list>

            <fo:table-row>
              <fo:table-cell padding="3pt" number-columns-spanned="6">
                <fo:block text-align="end">${uiLabelMap.AccountingCheckAmount}</fo:block>
              </fo:table-cell>
              <fo:table-cell padding="3pt">
                <fo:block text-align="end">${payment.getBigDecimal("amount").setScale(decimals, rounding).toString()}</fo:block>
              </fo:table-cell>
            </fo:table-row>
          </fo:table-body>
        </fo:table>

      </fo:block>
      </#list>
      </#if> <#-- security if -->
    </fo:flow>
  </fo:page-sequence>
</fo:root>
</#escape>
