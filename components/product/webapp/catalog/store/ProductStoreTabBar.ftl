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
 *@author     Andy Zeneski (jaz@ofbiz.org)
 *@version    $Revision: 1.2 $
 *@since      2.2
-->

  <#assign unselectedClassName = "tabButton">
  <#assign selectedClassMap = {page.tabButtonItem?default("void") : "tabButtonSelected"}>
  <#if productStoreId?has_content>
    <div class='tabContainer'>
      <a href="<@ofbizUrl>/EditProductStore?productStoreId=${productStoreId}</@ofbizUrl>" class="${selectedClassMap.EditProductStore?default(unselectedClassName)}">Store</a>
      <a href="<@ofbizUrl>/EditProductStoreRoles?productStoreId=${productStoreId}</@ofbizUrl>" class="${selectedClassMap.EditProductStoreRoles?default(unselectedClassName)}">Roles</a>
      <a href="<@ofbizUrl>/EditProductStorePromos?productStoreId=${productStoreId}</@ofbizUrl>" class="${selectedClassMap.EditProductStorePromos?default(unselectedClassName)}">Promos</a>
	  <a href="<@ofbizUrl>/EditProductStoreCatalogs?productStoreId=${productStoreId}</@ofbizUrl>" class="${selectedClassMap.EditProductStoreCatalogs?default(unselectedClassName)}">Catalogs</a>
	  <a href="<@ofbizUrl>/EditProductStoreWebSites?viewProductStoreId=${productStoreId}</@ofbizUrl>" class="${selectedClassMap.EditProductStoreWebSites?default(unselectedClassName)}">WebSites</a>
	  <a href="<@ofbizUrl>/EditProductStoreTaxSetup?productStoreId=${productStoreId}</@ofbizUrl>" class="${selectedClassMap.EditProductStoreTaxSetup?default(unselectedClassName)}">Sales Tax</a>
	  <a href="<@ofbizUrl>/EditProductStoreShipSetup?viewProductStoreId=${productStoreId}</@ofbizUrl>" class="${selectedClassMap.EditProductStoreShipSetup?default(unselectedClassName)}">Shipping</a>
	  <a href="<@ofbizUrl>/EditProductStorePaySetup?productStoreId=${productStoreId}</@ofbizUrl>" class="${selectedClassMap.EditProductStorePaySetup?default(unselectedClassName)}">Payments</a>
	  <a href="<@ofbizUrl>/EditProductStoreEmails?productStoreId=${productStoreId}</@ofbizUrl>" class="${selectedClassMap.EditProductStoreEmails?default(unselectedClassName)}">Emails</a>
	  <a href="<@ofbizUrl>/editProductStoreKeywordOvrd?productStoreId=${productStoreId}</@ofbizUrl>" class="${selectedClassMap.EditProductStoreKeywordOvrd?default(unselectedClassName)}">Override</a>
    </div>
  </#if>  
