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

<#if (requestAttributes.uiLabelMap)?exists><#assign uiLabelMap = requestAttributes.uiLabelMap></#if>
<#if (requestAttributes.security)?exists><#assign security = requestAttributes.security></#if>
<#if (requestAttributes.userLogin)?exists><#assign userLogin = requestAttributes.userLogin></#if>
<#assign unselectedLeftClassName = "headerButtonLeft">
<#assign unselectedRightClassName = "headerButtonRight">
<#assign selectedLeftClassMap = {page.headerItem?default("void") : "headerButtonLeftSelected"}>
<#assign selectedRightClassMap = {page.headerItem?default("void") : "headerButtonRightSelected"}>

<div class="apptitle">${uiLabelMap.AccountingManagerApplication}</div>
<div class="row">
  <div class="col"><a href="<@ofbizUrl>main</@ofbizUrl>" class="${selectedLeftClassMap.main?default(unselectedLeftClassName)}">${uiLabelMap.AccountingMainMenu}</a></div>  
  <div class="col"><a href="<@ofbizUrl>FindAgreement</@ofbizUrl>" class="${selectedLeftClassMap.agreement?default(unselectedLeftClassName)}">${uiLabelMap.AccountingAgreements}</a></div>
  <div class="col"><a href="<@ofbizUrl>FindBillingAccount</@ofbizUrl>" class="${selectedLeftClassMap.billingaccount?default(unselectedLeftClassName)}">${uiLabelMap.AccountingBillingMenu}</a></div>
  <div class="col"><a href="<@ofbizUrl>findInvoices</@ofbizUrl>" class="${selectedLeftClassMap.invoices?default(unselectedLeftClassName)}">${uiLabelMap.AccountingInvoicesMenu}</a></div>
  <div class="col"><a href="<@ofbizUrl>findPayments</@ofbizUrl>" class="${selectedLeftClassMap.payments?default(unselectedLeftClassName)}">${uiLabelMap.AccountingPaymentsMenu}</a></div>
  <#if security.hasEntityPermission("MANUAL", "_PAYMENT", session)>
    <div class="col"><a href="<@ofbizUrl>FindGatewayResponses</@ofbizUrl>" class="${selectedLeftClassMap.transaction?default(unselectedLeftClassName)}">${uiLabelMap.AccountingTransactions}</a></div>
  </#if>
  <div class="col"><a href="<@ofbizUrl>FindGlobalGlAccount</@ofbizUrl>" class="${selectedLeftClassMap.chartofaccounts?default(unselectedLeftClassName)}">${uiLabelMap.AccountingChartOfAcctsMenu}</a></div>
  <div class="col"><a href="<@ofbizUrl>ListFixedAssets</@ofbizUrl>" class="${selectedLeftClassMap.ListFixedAssets?default(unselectedLeftClassName)}">${uiLabelMap.AccountingFixedAssets}</a></div>
  <div class="col"><a href="<@ofbizUrl>FindTaxAuthority</@ofbizUrl>" class="${selectedLeftClassMap.TaxAuthorities?default(unselectedLeftClassName)}">${uiLabelMap.AccountingTaxAuthorities}</a></div>
  <div class="col"><a href="<@ofbizUrl>ListCompanies</@ofbizUrl>" class="${selectedLeftClassMap.companies?default(unselectedLeftClassName)}">${uiLabelMap.AccountingCompanies}</a></div>
  <div class="col"><a href="<@ofbizUrl>FindFinAccount</@ofbizUrl>" class="${selectedLeftClassMap.FindFinAccount?default(unselectedLeftClassName)}">${uiLabelMap.AccountingFinAccount}</a></div>
  <#if userLogin?has_content>
    <div class="col-right"><a href="<@ofbizUrl>logout</@ofbizUrl>" class="${selectedRightClassMap.logout?default(unselectedRightClassName)}">${uiLabelMap.CommonLogout}</a></div>
  <#else>
    <div class="col-right"><a href='<@ofbizUrl>${checkLoginUrl?if_exists}</@ofbizUrl>' class='${selectedRightClassMap.login?default(unselectedRightClassName)}'>${uiLabelMap.CommonLogin}</a></div>
  </#if>
  <div class="col-fill">&nbsp;</div>
</div>
