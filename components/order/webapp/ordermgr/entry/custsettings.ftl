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
 *@version    $Revision: 1.1 $
 *@since      2.2
-->

<#if security.hasEntityPermission("ORDERMGR", "_CREATE", session)>
<table border=0 width='100%' cellspacing='0' cellpadding='0' class='boxoutside'>
  <tr>
    <td width='100%'>
      <table width='100%' border='0' cellspacing='0' cellpadding='0' class='boxtop'>
        <tr>
          <td align='left'>
            <div class='boxhead'>&nbsp;Order Entry Customer Info</div>
          </td> 
          <td nowrap align="right">
            <div class="tabletext">
              <a href="<@ofbizUrl>/setCustomer</@ofbizUrl>" class="submenutext">Refresh</a><a href="<@ofbizUrl>/orderentry</@ofbizUrl>" class="submenutext">Items</a><a href="javascript:document.custsetupform.submit();" class="submenutextright">Continue</a>
            </div>
          </td>         
        </tr>
      </table>
    </td>
  </tr>
  <tr>
    <td width='100%'>
      <table width='100%' border='0' cellspacing='0' cellpadding='0' class='boxbottom'>
        <form name="custsetupform" method="post" action="<@ofbizUrl>/createCustomer</@ofbizUrl>">
        <input type="hidden" name="finalizeMode" value="cust">
        <tr>
          <td>
            <table width="100%" border="0" cellpadding="1" cellspacing="0">              
              <tr>
                <td width="26%" align="right"><div class="tabletext">Title</div></td>
                <td width="5">&nbsp;</td>
                <td width="74%">
                  <input type="text" class='inputBox' name="personalTitle" value="${requestParameters.personalTitle?if_exists}" size="10" maxlength="30">
                </td>
              </tr>
              <tr>
                <td width="26%" align="right"><div class="tabletext">First Name</div></td>
                <td width="5">&nbsp;</td>
                <td width="74%">
                  <input type="text" class='inputBox' name="firstName" value="${requestParameters.firstName?if_exists}" size="30" maxlength="30">
                *</td>
              </tr>
              <tr>
                <td width="26%" align="right"><div class="tabletext">Middle Initial</div></td>
                <td width="5">&nbsp;</td>
                <td width="74%">
                  <input type="text" class='inputBox' name="middleName" value="${requestParameters.middleName?if_exists}" size="4" maxlength="4">
                </td>
              </tr>
              <tr>
                <td width="26%" align="right"><div class="tabletext">Last Name </div></td>
                <td width="5">&nbsp;</td>
                <td width="74%">
                  <input type="text" class='inputBox' name="lastName" value="${requestParameters.lastName?if_exists}" size="30" maxlength="30">
                *</td>
              </tr>
              <tr>
                <td width="26%" align="right"><div class="tabletext">Suffix</div></td>
                <td width="5">&nbsp;</td>
                <td width="74%">
                  <input type="text" class='inputBox' name="suffix" value="${requestParameters.suffix?if_exists}" size="10" maxlength="30">
                </td>
              </tr>                          
              <tr>
                <td colspan="3">&nbsp;</td>
              </tr>              
              <tr>
                <td width="26%" align="right"><div class="tabletext">Home phone<BR>(allow solicitation?)</div></td>
                <td width="5">&nbsp;</td>
                <td width="74%">
                  <input type="text" class='inputBox' name="homeCountryCode" value="${requestParameters.homeCountryCode?if_exists}" size="4" maxlength="10">
                  -&nbsp;<input type="text" class='inputBox' name="homeAreaCode" value="${requestParameters.homeAreaCode?if_exists}" size="4" maxlength="10">
                  -&nbsp;<input type="text" class='inputBox' name="homeContactNumber" value="${requestParameters.homeContactNumber?if_exists}" size="15" maxlength="15">
                  &nbsp;ext&nbsp;<input type="text" class='inputBox' name="homeExt" value="${requestParameters.homeExt?if_exists}" size="6" maxlength="10">
                  <BR>
                  <select name="homeSol" class='selectBox'>
                    <option>${requestParameters.homeSol?default("Y")}</option>
                    <option></option><option>Y</option><option>N</option>
                  </select>
                </td>
              </tr>
              <tr>
                <td width="26%" align="right"><div class="tabletext">Business phone<BR>(allow solicitation?)</div></td>
                <td width="5">&nbsp;</td>
                <td width="74%">
                  <input type="text" class='inputBox' name="workCountryCode" value="${requestParameters.CUSTOMER_WORK_COUNTRY?if_exists}" size="4" maxlength="10">
                  -&nbsp;<input type="text" class='inputBox' name="workAreaCode" value="${requestParameters.CUSTOMER_WORK_AREA?if_exists}" size="4" maxlength="10">
                  -&nbsp;<input type="text" class='inputBox' name="workContactNumber" value="${requestParameters.CUSTOMER_WORK_CONTACT?if_exists}" size="15" maxlength="15">
                  &nbsp;ext&nbsp;<input type="text" class='inputBox' name="workExt" value="${requestParameters.CUSTOMER_WORK_EXT?if_exists}" size="6" maxlength="10">
                  <BR>
                  <select name="workSol" class='selectBox'>
                    <option>${requestParameters.workSol?default("Y")}</option>
                    <option></option><option>Y</option><option>N</option>
                  </select>
                </td>
              </tr>                            
              <tr>
                <td colspan="3">&nbsp;</td>
              </tr>                            
              <tr>
                <td width="26%" align="right"><div class="tabletext">Email address<BR>(allow solicitation?)</div></td>
                <td width="5">&nbsp;</td>
                <td width="74%">
                  <input type="text" class='inputBox' name="emailAddress" value="" size="60" maxlength="255"> *
                  <BR>
                  <select name="emailSol" class='selectBox'>
                    <option>${requestParameters.emailSol?default("Y")}</option>
                    <option></option><option>Y</option><option>N</option>
                  </select>
                </td>
              </tr>               
              <tr>
                <td colspan="3">&nbsp;</td>
              </tr>              
              <tr>
                <td width="26%" align="right"><div class="tabletext">Username</div></td>
                <td width="5">&nbsp;</td>
                <td width="74%">
                  <input type="text" class='inputBox' name="userLoginId" value="${requestParameters.USERNAME?if_exists}" size="20" maxlength="50">
                </td>
              </tr> 
              <#--  
              <tr>
                <td width="26%" align="right"><div class="tabletext">Password</div></td>
                <td width="5">&nbsp;</td>
                <td width="74%">
                  <input type="password" class='inputBox' name="PASSWORD" value="" size="20" maxlength="50">
                *</td>
              </tr>
              <tr>
                <td width="26%" align="right"><div class="tabletext">Confirm Password</div></td>
                <td width="5">&nbsp;</td>
                <td width="74%">
                  <input type="password" class='inputBox' name="CONFIRM_PASSWORD" value="" size="20" maxlength="50">
                *</td>
              </tr>
              <tr>
                <td width="26%" align="right"><div class="tabletext">Password Hint</div></td>
                <td width="5">&nbsp;</td>
                <td width="74%">
                  <input type="text" class='inputBox' name="PASSWORD_HINT" value="${requestParameters.PASSWORD_HINT?if_exists}" size="40" maxlength="100">
                </td>
              </tr> 
              --> 
              </form>                                       
            </table>        
          </td>
        </tr>
      </table>
    </td>
  </tr>
</table>

<br>
<#else>
  <h3>You do not have permission to view this page. ("ORDERMGR_CREATE" or "ORDERMGR_ADMIN" needed)</h3>
</#if>          