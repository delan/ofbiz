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
 *@version    $Rev:$
 *@since      3.0
-->

<#assign uiLabelMap = requestAttributes.uiLabelMap>

<table border=0 width='100%' cellspacing='0' cellpadding='0' class='boxoutside'>
  <tr>
    <td width='100%'>
      <table width='100%' border='0' cellspacing='0' cellpadding='0' class='boxtop'>
        <tr>
          <td align='left'>
            <div class='boxhead'>&nbsp;${uiLabelMap.PartyBasicInformation}</div>
          </td>
          <td nowrap align="right">
            <div class="tabletext">
              ${pages.get("/order/anonymoustrail.ftl")}
            </div>
          </td>
        </tr>
      </table>
    </td>
  </tr>
  <tr>
    <td width='100%'>
      <table width='100%' border='0' cellspacing='0' cellpadding='0' class='boxbottom'>
        <form name="custsetupform" method="post" action="<@ofbizUrl>/setBasicInfo</@ofbizUrl>">
        <input type="hidden" name="finalizeMode" value="cust">
        <tr>
          <td>
            <table width="100%" border="0" cellpadding="1" cellspacing="0">
              <tr>
                <td width="26%" align="right"><div class="tabletext">${uiLabelMap.CommonTitle}</div></td>
                <td width="5">&nbsp;</td>
                <td width="74%">
                  <input type="text" class='inputBox' name="personalTitle" value="${requestParameters.personalTitle?if_exists}" size="10" maxlength="30">
                </td>
              </tr>
              <tr>
                <td width="26%" align="right"><div class="tabletext">${uiLabelMap.PartyFirstName}</div></td>
                <td width="5">&nbsp;</td>
                <td width="74%">
                  <input type="text" class='inputBox' name="firstName" value="${requestParameters.firstName?if_exists}" size="30" maxlength="30">
                *</td>
              </tr>
              <tr>
                <td width="26%" align="right"><div class="tabletext">${uiLabelMap.PartyMiddleInitial}</div></td>
                <td width="5">&nbsp;</td>
                <td width="74%">
                  <input type="text" class='inputBox' name="middleName" value="${requestParameters.middleName?if_exists}" size="4" maxlength="4">
                </td>
              </tr>
              <tr>
                <td width="26%" align="right"><div class="tabletext">${uiLabelMap.PartyLastName} </div></td>
                <td width="5">&nbsp;</td>
                <td width="74%">
                  <input type="text" class='inputBox' name="lastName" value="${requestParameters.lastName?if_exists}" size="30" maxlength="30">
                *</td>
              </tr>
              <tr>
                <td width="26%" align="right"><div class="tabletext">${uiLabelMap.PartySuffix}</div></td>
                <td width="5">&nbsp;</td>
                <td width="74%">
                  <input type="text" class='inputBox' name="suffix" value="${requestParameters.suffix?if_exists}" size="10" maxlength="30">
                </td>
              </tr>
              <tr>
                <td colspan="3">&nbsp;</td>
              </tr>
              <tr>
                <td width="26%" align="right"><div class="tabletext">${uiLabelMap.PartyHomePhone}<BR>(${uiLabelMap.PartyAllowSolicitation}?)</div></td>
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
                <td width="26%" align="right"><div class="tabletext">${uiLabelMap.PartyBusinessPhone}<BR>(${uiLabelMap.PartyAllowSolicitation}?)</div></td>
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
                <td width="26%" align="right"><div class="tabletext">${uiLabelMap.PartyEmailAddress}<BR>(${uiLabelMap.PartyAllowSolicitation}?)</div></td>
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
                <td colspan="3" align="center"><input type="submit" class="smallsubmit" value="${uiLabelMap.CommonContinue}"></td>
              </tr>
              </form>
            </table>
          </td>
        </tr>
      </table>
    </td>
  </tr>
</table>
