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
 *@author     David E. Jones
 *@version    1.0
-->

<p class="head1">Request a New Account</p>
<br>
<p class='tabletext'>If you already have an account, <a href='<@ofbizUrl>/checkLogin/main</@ofbizUrl>' class='buttontext'>log in here</a>.</p>

<form method="post" action="<@ofbizUrl>/createcustomer${previousParams}</@ofbizUrl>" name="newuserform" style='margin:0;'>
	
<TABLE border=0 width='100%' cellspacing='0' cellpadding='0' class='boxoutside'>
  <TR>
    <TD width='100%'>
      <table width='100%' border='0' cellspacing='0' cellpadding='0' class='boxtop'>
        <tr>
          <td valign="middle" align="left">
            <div class='boxhead'>&nbsp;Name and Shipping Address</div>
          </td>
        </tr>
      </table>
    </TD>
  </TR>
  <TR>
    <TD width='100%'>
      <table width='100%' border='0' cellspacing='0' cellpadding='0' class='boxbottom'>
        <tr>
          <td>
<table width="100%" border="0" cellpadding="2" cellspacing="0">
  <tr>
    <td width="26%"><div class="tabletext">Title</div></td>
    <td width="74%">
      <input type="text" class='inputBox' name="USER_TITLE" value="${requestParameters.USER_TITLE?if_exists}" size="10" maxlength="30">
    </td>
  </tr>
  <tr>
    <td width="26%"><div class="tabletext">First name</div></td>
    <td width="74%">
      <input type="text" class='inputBox' name="USER_FIRST_NAME" value="${requestParameters.USER_FIRST_NAME?if_exists}" size="30" maxlength="30">
    * </td>
  </tr>
  <tr>
    <td width="26%"><div class="tabletext">Middle initial</div></td>
    <td width="74%">
        <input type="text" class='inputBox' name="USER_MIDDLE_NAME" value="${requestParameters.USER_MIDDLE_NAME?if_exists}" size="4" maxlength="4">
    </td>
  </tr>
  <tr>
    <td width="26%"><div class="tabletext">Last name </div></td>
    <td width="74%">
      <input type="text" class='inputBox' name="USER_LAST_NAME" value="${requestParameters.USER_LAST_NAME?if_exists}" size="30" maxlength="30">
    * </td>
  </tr>
  <tr>
    <td width="26%"><div class="tabletext">Suffix</div></td>
    <td width="74%">
      <input type="text" class='inputBox' name="USER_SUFFIX" value="${requestParameters.USER_SUFFIX?if_exists}" size="10" maxlength="30">
    </td>
  </tr>
  <tr>
    <td width="26%"><div class="tabletext">Address Line 1</div></td>
    <td width="74%">
      <input type="text" class='inputBox' name="CUSTOMER_ADDRESS1" value="${requestParameters.CUSTOMER_ADDRESS1?if_exists}" size="30" maxlength="30">
    *</td>
  </tr>
  <tr>
    <td width="26%"><div class="tabletext">Address Line 2</div></td>
    <td width="74%">
        <input type="text" class='inputBox' name="CUSTOMER_ADDRESS2" value="${requestParameters.CUSTOMER_ADDRESS2?if_exists}" size="30" maxlength="30">
    </td>
  </tr>
  <tr>
    <td width="26%"><div class="tabletext">City</div></td>
    <td width="74%">
        <input type="text" class='inputBox' name="CUSTOMER_CITY" value="${requestParameters.CUSTOMER_CITY?if_exists}" size="30" maxlength="30">
    * </td>
  </tr>
  <tr>
    <td width="26%"><div class="tabletext">State/Province</div></td>
    <td width="74%">
      <select name="CUSTOMER_STATE" class='selectBox'>
          <#if requestParameters.CUSTOMER_STATE?exists><option value='${requestParameters.CUSTOMER_STATE}'>${selectedStateName?default(requestParameters.CUSTOMER_STATE)}</option></#if>
          <option value="">No State/Province</option>          
          <#include "../includes/states.ftl">
      </select>
    * </td>
  </tr>
  <tr>
    <td width="26%"><div class="tabletext">Zip/Postal Code</div></td>
    <td width="74%">
        <input type="text" class='inputBox' name="CUSTOMER_POSTAL_CODE" value="${requestParameters.CUSTOMER_POSTAL_CODE?if_exists}" size="12" maxlength="10">
    * </td>
  </tr>
  <tr>
      <td width="26%"><div class="tabletext">Country</div></td>
      <td width="74%">
          <select name="CUSTOMER_COUNTRY" class='selectBox'>
            <#if requestParameters.CUSTOMER_COUNTRY?exists><option value='${requestParameters.CUSTOMER_COUNTRY}'>${selectedCountryName?default(requestParameters.CUSTOMER_COUNTRY)}</option></#if>
            <#include "../includes/countries.ftl">
          </select>
      * </td>
  </tr>
  <tr>
    <td width="26%"><div class="tabletext">Allow Address Solicitation?</div></td>
    <td width="74%">
      <select name="CUSTOMER_ADDRESS_ALLOW_SOL" class='selectBox'>
        <option>${requestParameters.CUSTOMER_ADDRESS_ALLOW_SOL?default("Y")}</option>
        <option></option><option>Y</option><option>N</option>
      </select>
    </td>
  </tr>
</table>
          </td>
        </tr>
      </table>
    </TD>
  </TR>
</TABLE>

<br>

<TABLE border=0 width='100%' cellspacing='0' cellpadding='0' class='boxoutside'>
  <TR>
    <TD width='100%'>
      <table width='100%' border='0' cellspacing='0' cellpadding='0' class='boxtop'>
        <tr>
          <td valign="middle" align="left">
            <div class='boxhead'>&nbsp;Phone Numbers</div>
          </td>
        </tr>
      </table>
    </TD>
  </TR>
  <TR>
    <TD width='100%'>
      <table width='100%' border='0' cellspacing='0' cellpadding='0' class='boxbottom'>
        <tr>
          <td>
<table width="100%">
  <tr>
    <td width="26%"><div class="tabletext">All phone numbers:</div></td>
    <td width="74%"><div class="tabletext">[Country] [Area Code] [Contact Number] [Extension]</div></td>
  </tr>
  <tr>
    <td width="26%"><div class="tabletext">Home phone<BR>(allow solicitation?)</div></td>
    <td width="74%">
        <input type="text" class='inputBox' name="CUSTOMER_HOME_COUNTRY" value="${requestParameters.CUSTOMER_HOME_COUNTRY?if_exists}" size="4" maxlength="10">
        -&nbsp;<input type="text" class='inputBox' name="CUSTOMER_HOME_AREA" value="${requestParameters.CUSTOMER_HOME_AREA?if_exists}" size="4" maxlength="10">
        -&nbsp;<input type="text" class='inputBox' name="CUSTOMER_HOME_CONTACT" value="${requestParameters.CUSTOMER_HOME_CONTACT?if_exists}" size="15" maxlength="15">
        &nbsp;ext&nbsp;<input type="text" class='inputBox' name="CUSTOMER_HOME_EXT" value="${requestParameters.CUSTOMER_HOME_EXT?if_exists}" size="6" maxlength="10">
        <BR>
        <select name="CUSTOMER_HOME_ALLOW_SOL" class='selectBox'>
          <option>${requestParameters.CUSTOMER_HOME_ALLOW_SOL?default("Y")}</option>
          <option></option><option>Y</option><option>N</option>
        </select>
    </td>
  </tr>
  <tr>
    <td width="26%"><div class="tabletext">Business phone<BR>(allow solicitation?)</div></td>
    <td width="74%">
        <input type="text" class='inputBox' name="CUSTOMER_WORK_COUNTRY" value="${requestParameters.CUSTOMER_WORK_COUNTRY?if_exists}" size="4" maxlength="10">
        -&nbsp;<input type="text" class='inputBox' name="CUSTOMER_WORK_AREA" value="${requestParameters.CUSTOMER_WORK_AREA?if_exists}" size="4" maxlength="10">
        -&nbsp;<input type="text" class='inputBox' name="CUSTOMER_WORK_CONTACT" value="${requestParameters.CUSTOMER_WORK_CONTACT?if_exists}" size="15" maxlength="15">
        &nbsp;ext&nbsp;<input type="text" class='inputBox' name="CUSTOMER_WORK_EXT" value="${requestParameters.CUSTOMER_WORK_EXT?if_exists}" size="6" maxlength="10">
        <BR>
        <select name="CUSTOMER_WORK_ALLOW_SOL" class='selectBox'>
          <option>${requestParameters.CUSTOMER_WORK_ALLOW_SOL?default("Y")}</option>
          <option></option><option>Y</option><option>N</option>
        </select>
    </td>
  </tr>
  <tr>
    <td width="26%"><div class="tabletext">Fax number<BR>(allow solicitation?)</div></td>
    <td width="74%">
        <input type="text" class='inputBox' name="CUSTOMER_FAX_COUNTRY" value="${requestParameters.CUSTOMER_FAX_COUNTRY?if_exists}" size="4" maxlength="10">
        -&nbsp;<input type="text" class='inputBox' name="CUSTOMER_FAX_AREA" value="${requestParameters.CUSTOMER_FAX_AREA?if_exists}" size="4" maxlength="10">
        -&nbsp;<input type="text" class='inputBox' name="CUSTOMER_FAX_CONTACT" value="${requestParameters.CUSTOMER_FAX_CONTACT?if_exists}" size="15" maxlength="15">
        <BR>
        <select name="CUSTOMER_FAX_ALLOW_SOL" class='selectBox'>
          <option>${requestParameters.CUSTOMER_FAX_ALLOW_SOL?default("Y")}</option>
          <option></option><option>Y</option><option>N</option>
        </select>
    </td>
  </tr>
  <tr>
    <td width="26%"><div class="tabletext">Mobile phone<BR>(allow solicitation?)</div></td>
    <td width="74%">
        <input type="text" class='inputBox' name="CUSTOMER_MOBILE_COUNTRY" value="${requestParameters.CUSTOMER_MOBILE_COUNTRY?if_exists}" size="4" maxlength="10">
        -&nbsp;<input type="text" class='inputBox' name="CUSTOMER_MOBILE_AREA" value="${requestParameters.CUSTOMER_MOBILE_AREA?if_exists}" size="4" maxlength="10">
        -&nbsp;<input type="text" class='inputBox' name="CUSTOMER_MOBILE_CONTACT" value="${requestParameters.CUSTOMER_MOBILE_CONTACT?if_exists}" size="15" maxlength="15">
        <BR>
        <select name="CUSTOMER_MOBILE_ALLOW_SOL" class='selectBox'>
          <option>${requestParameters.CUSTOMER_MOBILE_ALLOW_SOL?default("Y")}</option>
          <option></option><option>Y</option><option>N</option>
        </select>
    </td>
  </tr>
</table>
          </td>
        </tr>
      </table>
    </TD>
  </TR>
</TABLE>

<br>

<TABLE border=0 width='100%' cellspacing='0' cellpadding='0' class='boxoutside'>
  <TR>
    <TD width='100%'>
      <table width='100%' border='0' cellspacing='0' cellpadding='0' class='boxtop'>
        <tr>
          <td valign="middle" align="left">
            <div class='boxhead'>&nbsp;Email Address</div>
          </td>
        </tr>
      </table>
    </TD>
  </TR>
  <TR>
    <TD width='100%'>
      <table width='100%' border='0' cellspacing='0' cellpadding='0' class='boxbottom'>
        <tr>
          <td>
<table width="100%">
  <tr>
    <td width="26%"><div class="tabletext">Email address<BR>(allow solicitation?)</div></td>
    <td width="74%">
        <input type="text" class='inputBox' name="CUSTOMER_EMAIL" value="${requestParameters.CUSTOMER_EMAIL?if_exists}" size="60" maxlength="255"> *
        <BR>
        <select name="CUSTOMER_EMAIL_ALLOW_SOL" class='selectBox'>
          <option>${requestParameters.CUSTOMER_EMAIL_ALLOW_SOL?default("Y")}</option>
          <option></option><option>Y</option><option>N</option>
        </select>
    </td>
  </tr>
<#--
  <tr>
    <td width="26%">
        <div class="tabletext">Order Email addresses (comma separated)</div>
    </td>
    <td width="74%">
        <input type="text" name="CUSTOMER_ORDER_EMAIL" value="${requestParameters.CUSTOMER_ORDER_EMAIL?if_exists}" size="40" maxlength="80">
    </td>
  </tr>
-->
</table>
          </td>
        </tr>
      </table>
    </TD>
  </TR>
</TABLE>

<br>

<TABLE border=0 width='100%' cellspacing='0' cellpadding='0' class='boxoutside'>
  <TR>
    <TD width='100%'>
      <table width='100%' border='0' cellspacing='0' cellpadding='0' class='boxtop'>
        <tr>
          <td valign="middle" align="left">
            <div class='boxhead'>&nbsp;Username and Password</div>
          </td>          
        </tr>
      </table>
    </TD>
  </TR>
  <TR>
    <TD width='100%'>
      <table width='100%' border='0' cellspacing='0' cellpadding='0' class='boxbottom'>
        <tr>
          <td>
  <table width="100%" border="0" cellpadding="2" cellspacing="0">
    <tr>
      <td width="26%"><div class="tabletext">Username</div></td>
      <td width="74%">
          <input type="text" class='inputBox' name="USERNAME" value="${requestParameters.USERNAME?if_exists}" size="20" maxlength="50">
      * </td>
    </tr>
    <#if createAllowPassword>
      <tr>
        <td width="26%">
            <div class="tabletext">Password</div>
        </td>
        <td width="74%">
            <input type="password" class='inputBox' name="PASSWORD" value="" size="20" maxlength="50">
          * </td>
      </tr>
      <tr>
        <td width="26%">
            <div class="tabletext">Repeat password to confirm</div>
        </td>
        <td width="74%">
            <input type="password" class='inputBox' name="CONFIRM_PASSWORD" value="" size="20" maxlength="50">
        * </td>
      </tr>
      <tr>
        <td width="26%">
            <div class="tabletext">Password Hint</div>
        </td>
        <td width="74%">
            <input type="text" class='inputBox' name="PASSWORD_HINT" value="${requestParameters.PASSWORD_HINT?if_exists}" size="40" maxlength="100">
        </td>
      </tr>
    <#else>
      <tr>
        <td width="26%">
            <div class="tabletext">Password</div>
        </td>
        <td>
           <div class="commentary">You will receive a password by email when your new account is approved.</div>
        </td>
      </tr>
    </#if>
  </table>
          </td>
        </tr>
      </table>
    </TD>
  </TR>
</TABLE>
<input type="image" src="/images/spacer.gif" onClick="javascript:document.newuserform.submit();">
</form>

<br><div class="commentary">Fields marked with (*) are required.</div>

&nbsp;&nbsp;<a href="<@ofbizUrl>/checkLogin/main</@ofbizUrl>" class="buttontext">[Back]</a>
&nbsp;&nbsp;<a href="javascript:document.newuserform.submit()" class="buttontext">[Save]</a>
<br>
<br>
