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
 *@author     Peter Goron (peter.goron@nereide.biz)
 *@version    $Revision: 1.1 $
 *@since      3.1
-->

<#-- ==================== Party Selection dialog box ========================= -->
<table border="0" width="100%" cellspacing="0" cellpadding="0" class="boxoutside">
  <tr>
    <td width="100%">
      <table width="100%" border="0" cellspacing="0" cellpadding="0" class="boxtop">
        <tr>
	      <td valign="middle" align="left">
	        <div class="boxhead">&nbsp;Additional Party Entry</div>
	      </td>
	      <td nowrap align="right">
	        <div class="tabletext">
              <#--<a href="/ordermgr/control/setAdditionalParty" class="submenutext">Refresh</a>-->
	          <a href="/ordermgr/control/orderentry" class="submenutext">Items</a>
	          <a href="/ordermgr/control/setShipping" class="submenutext">Shipping</a>
	          <a href="/ordermgr/control/setOptions" class="submenutext">Options</a>
              <a href="/ordermgr/control/setBilling" class="submenutext">Payment</a>
	          <a href="<@ofbizUrl>/finalizeOrder?finalizeReqAdditionalParty=false</@ofbizUrl>" class="submenutextright">Continue</a>
	        </div>
	      </td>
	    </tr>
      </table>
    </td>
  </tr>
  
  <form method="post" action="<@ofbizUrl>/setAdditionalParty</@ofbizUrl>" name="quickAddPartyForm">
  
    <tr>
      <td><div class="tableheadtext">1) Select type of party to associate to order :</div></td>
    </tr>
  
    <tr>
      <td width="100%">
        <table border="0" cellspacing="0" cellpadding="0" class="boxbottom">
          <tr>
	        <td align="right">
	          <input type="radio" name="additionalPartyType" value="Person" onchange="<#if additionalPartyType?exists>javascript:document.quickAddPartyForm.additionalPartyId.value='';</#if>document.quickAddPartyForm.submit()"<#if (additionalPartyType?exists && additionalPartyType == "Person")> checked</#if>>
	        </td>
	        <td>
              <div class="tabletext">Person</div>
	        </td>
	      </tr>
          <tr>
	        <td align="right">
	          <input type="radio" name="additionalPartyType" value="Group" onchange="<#if additionalPartyType?exists>javascript:document.quickAddPartyForm.additionalPartyId.value='';</#if>document.quickAddPartyForm.submit()"<#if additionalPartyType?exists && additionalPartyType == "Group"> checked</#if>>
	        </td>
	        <td>
              <div class="tabletext">Group</div>
	        </td>
	      </tr>
        </table>
      </td>
    </tr>

    <tr>
      <td>&nbsp;</td>
    </tr>

    <#if additionalPartyType?exists && additionalPartyType != "">
      <#if additionalPartyType == "Person">
        <#assign lookupPartyView="LookupPerson">
      <#else>
        <#assign lookupPartyView="LookupPartyGroup">
      </#if>
  
      <tr>
        <td><div class="tableheadtext">2) Find a party :</div></td>
      </tr>
  
      <tr>
        <td width="100%">
          <table border="0" cellspacing="0" cellpadding="0" class="boxbottom">
            <tr>
	          <td>
	            <div class="tableheadtext">Identifier :</div>
	          </td>
	          <td>
	            <input type="text" class="inputBox" name="additionalPartyId" value="${additionalPartyId?if_exists}" onchange="javascript:document.quickAddPartyForm.submit()">
	         </td>
	         <td>
	           <a href="javascript:call_fieldlookup2(document.quickAddPartyForm.additionalPartyId, '${lookupPartyView}');document.quickAddPartyForm.additionalPartyId.focus();"><img src="/content/images/fieldlookup.gif" width="16" height="16" border="0" alt="Lookup"></a>
	         </td>
	         <#-- <#if !additionalPartyId?has_content </#if> -->
             <td>
	           &nbsp;<a href="javascript:document.quickAddPartyForm.submit()" class="buttontext">[Apply]</a>
	         </td>
           </tr>
        </table>
      </td>
    </tr>

    <tr>
      <td>&nbsp;</td>
    </tr>

    </form>

    <#if roles?has_content>
      <tr>
        <td><div class="tableheadtext">3) Select role(s) for this party :</div></td>
      </tr>

      <tr>
        <td width="100%">
          <table border="0" cellspacing="0" cellpadding="0" class="boxbottom">
            <form method="post" action="<@ofbizUrl>/addAdditionalParty</@ofbizUrl>">
              <tr>
                <td>&nbsp;</td>
	            <td>
	              <select name="additionalRoleTypeId" size="5" multiple>
	                <#list roles as role>
	                  <option value="${role.roleTypeId}" class="tabletext">${role.description}</option>
	                </#list>
	              </select>
	            </td>
	  
	            <td>&nbsp;</td>
	            <td align="left">
	              <input type="hidden" name="additionalPartyId" value="${additionalPartyId}">
	              <input type="submit" class="smallSubmit" value="Add">
	            </td>
	          </tr>
	        </form>
          </table>
        </td>
      </tr>
    </#if> <#-- roles?has_content -->
  <#else>
    </form>
  </#if> <#-- additionalPartyType?has_content -->
</table>
<br>

${pages.get("/entry/additionalPartyListing.ftl")}
