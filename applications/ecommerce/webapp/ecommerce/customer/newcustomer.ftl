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
 *@author     David E. Jones
 *@version    1.0
 *@since      2.1
-->

<#if getUsername>
<script language="JavaScript">
     lastFocusedName = null;
     function setLastFocused(formElement) {
         lastFocusedName = formElement.name;
     }
     function clickUsername() {
         if (document.forms["newuserform"].elements["UNUSEEMAIL"].checked) {
             if (lastFocusedName == "UNUSEEMAIL") {
                 document.forms["newuserform"].elements["PASSWORD"].focus();
             } else if (lastFocusedName == "PASSWORD") {
                 document.forms["newuserform"].elements["UNUSEEMAIL"].focus();
             } else {
                 document.forms["newuserform"].elements["PASSWORD"].focus();
             }
         }
     }
     function changeEmail() {
         if (document.forms["newuserform"].elements["UNUSEEMAIL"].checked) {
             document.forms["newuserform"].elements["USERNAME"].value=document.forms["newuserform"].elements["CUSTOMER_EMAIL"].value;
         }
     }
     function setEmailUsername() {
         if (document.forms["newuserform"].elements["UNUSEEMAIL"].checked) {
             document.forms["newuserform"].elements["USERNAME"].value=document.forms["newuserform"].elements["CUSTOMER_EMAIL"].value;
             // don't disable, make the browser not submit the field: document.forms["newuserform"].elements["USERNAME"].disabled=true;
         } else {
             document.forms["newuserform"].elements["USERNAME"].value='';
             // document.forms["newuserform"].elements["USERNAME"].disabled=false;
         }
     }
</script>
</#if>

<p class="head1">${uiLabelMap.PartyRequestNewAccount}</p>
<p class='tabletext'>${uiLabelMap.PartyAlreadyHaveAccount}, <a href='<@ofbizUrl>/checkLogin/main</@ofbizUrl>' class='buttontext'>${uiLabelMap.CommonLoginHere}</a>.</p>

<#macro fieldErrors fieldName>
  <#if requestAttributes.errorMsgListReq?has_content>
    <#assign fieldMessages = Static["org.ofbiz.base.util.MessageString"].getMessagesForField(fieldName, true, requestAttributes.errorMsgListReq)>
    <ul>
      <#list fieldMessages as errorMsg>
        <li class="errorMessage">${errorMsg}</li>
      </#list>
    </ul>
  </#if>
</#macro>
<#macro fieldErrorsMulti fieldName1 fieldName2 fieldName3 fieldName4>
  <#if requestAttributes.errorMsgListReq?has_content>
    <#assign fieldMessages = Static["org.ofbiz.base.util.MessageString"].getMessagesForField(fieldName1, fieldName2, fieldName3, fieldName4, true, requestAttributes.errorMsgListReq)>
    <ul>
      <#list fieldMessages as errorMsg>
        <li class="errorMessage">${errorMsg}</li>
      </#list>
    </ul>
  </#if>
</#macro>

<form method="post" action="<@ofbizUrl>createcustomer${previousParams}</@ofbizUrl>" name="newuserform" style="margin: 0;">
<input type="hidden" name="productStoreId" value="${productStoreId}">

<div class="ecom-screenlet">
    <div class="ecom-screenlet-header">
        <div class='boxhead'>&nbsp;${uiLabelMap.PartyNameAndShippingAddress}</div>
    </div>
    <div class="ecom-screenlet-body">
        <div class="ecom-form-row">
            <div class="ecom-form-label">${uiLabelMap.CommonTitle}</div>
            <div class="ecom-form-field">
                <@fieldErrors fieldName="USER_TITLE"/>
                <input type="text" class='inputBox' name="USER_TITLE" value="${requestParameters.USER_TITLE?if_exists}" size="10" maxlength="30"/>
            </div>
        </div>
        <div class="ecom-form-row">
            <div class="ecom-form-label">${uiLabelMap.PartyFirstName}</div>
            <div class="ecom-form-field">
                <@fieldErrors fieldName="USER_FIRST_NAME"/>
                <input type="text" class='inputBox' name="USER_FIRST_NAME" value="${requestParameters.USER_FIRST_NAME?if_exists}" size="30" maxlength="30"> *
            </div>
        </div>
        <div class="ecom-form-row">
            <div class="ecom-form-label">${uiLabelMap.PartyMiddleInitial}</div>
            <div class="ecom-form-field">
                <@fieldErrors fieldName="USER_MIDDLE_NAME"/>
                <input type="text" class='inputBox' name="USER_MIDDLE_NAME" value="${requestParameters.USER_MIDDLE_NAME?if_exists}" size="4" maxlength="4">
            </div>
        </div>
        <div class="ecom-form-row">
            <div class="ecom-form-label">${uiLabelMap.PartyLastName}</div>
            <div class="ecom-form-field">
                <@fieldErrors fieldName="USER_LAST_NAME"/>
                <input type="text" class='inputBox' name="USER_LAST_NAME" value="${requestParameters.USER_LAST_NAME?if_exists}" size="30" maxlength="30"> *
            </div>
        </div>
        <div class="ecom-form-row">
            <div class="ecom-form-label">${uiLabelMap.PartySuffix}</div>
            <div class="ecom-form-field">
                <@fieldErrors fieldName="USER_SUFFIX"/>
                <input type="text" class='inputBox' name="USER_SUFFIX" value="${requestParameters.USER_SUFFIX?if_exists}" size="10" maxlength="30">
            </div>
        </div>
        <div class="ecom-form-row">
            <div class="ecom-form-label">${uiLabelMap.PartyAddressLine1}</div>
            <div class="ecom-form-field">
                <@fieldErrors fieldName="CUSTOMER_ADDRESS1"/>
                <input type="text" class='inputBox' name="CUSTOMER_ADDRESS1" value="${requestParameters.CUSTOMER_ADDRESS1?if_exists}" size="30" maxlength="30"> *
            </div>
        </div>
        <div class="ecom-form-row">
            <div class="ecom-form-label">${uiLabelMap.PartyAddressLine2}</div>
            <div class="ecom-form-field">
                <@fieldErrors fieldName="CUSTOMER_ADDRESS2"/>
                <input type="text" class='inputBox' name="CUSTOMER_ADDRESS2" value="${requestParameters.CUSTOMER_ADDRESS2?if_exists}" size="30" maxlength="30">
            </div>
        </div>
        <div class="ecom-form-row">
            <div class="ecom-form-label">${uiLabelMap.PartyCity}</div>
            <div class="ecom-form-field">
                <@fieldErrors fieldName="CUSTOMER_CITY"/>
                <input type="text" class='inputBox' name="CUSTOMER_CITY" value="${requestParameters.CUSTOMER_CITY?if_exists}" size="30" maxlength="30"> *
            </div>
        </div>
        <div class="ecom-form-row">
            <div class="ecom-form-label">${uiLabelMap.PartyState}</div>
            <div class="ecom-form-field">
                <@fieldErrors fieldName="CUSTOMER_STATE"/>
                <select name="CUSTOMER_STATE" class='selectBox'>
                    <#if requestParameters.CUSTOMER_STATE?exists><option value='${requestParameters.CUSTOMER_STATE}'>${selectedStateName?default(requestParameters.CUSTOMER_STATE)}</option></#if>
                    <option value="">${uiLabelMap.PartyNoState}</option>
                    ${screens.render("component://common/widget/CommonScreens.xml#states")}
                </select> *
            </div>
        </div>
        <div class="ecom-form-row">
            <div class="ecom-form-label">${uiLabelMap.PartyZipCode}</div>
            <div class="ecom-form-field">
                <@fieldErrors fieldName="CUSTOMER_POSTAL_CODE"/>
                <input type="text" class='inputBox' name="CUSTOMER_POSTAL_CODE" value="${requestParameters.CUSTOMER_POSTAL_CODE?if_exists}" size="12" maxlength="10"> *
            </div>
        </div>
        <div class="ecom-form-row">
            <div class="ecom-form-label">${uiLabelMap.PartyCountry}</div>
            <div class="ecom-form-field">
                <@fieldErrors fieldName="CUSTOMER_COUNTRY"/>
                <select name="CUSTOMER_COUNTRY" class='selectBox'>
                    <#if requestParameters.CUSTOMER_COUNTRY?exists><option value='${requestParameters.CUSTOMER_COUNTRY}'>${selectedCountryName?default(requestParameters.CUSTOMER_COUNTRY)}</option></#if>
                    ${screens.render("component://common/widget/CommonScreens.xml#countries")}
                </select> *
            </div>
        </div>
        <div class="ecom-form-row">
            <div class="ecom-form-label">${uiLabelMap.PartyAllowAddressSolicitation}</div>
            <div class="ecom-form-field">
                <select name="CUSTOMER_ADDRESS_ALLOW_SOL" class='selectBox'>
                    <option>${requestParameters.CUSTOMER_ADDRESS_ALLOW_SOL?default("Y")}</option>
                    <option></option><option>Y</option><option>N</option>
                </select>
            </div>
        </div>
        <div class="ecom-endcolumns"><span></span></div>
    </div>
</div>

<div class="ecom-screenlet">
    <div class="ecom-screenlet-header">
        <div class='boxhead'>&nbsp;${uiLabelMap.PartyPhoneNumbers}</div>
    </div>
    <div class="ecom-screenlet-body">
        <div class="ecom-form-row">
            <div class="ecom-form-label">${uiLabelMap.PartyAllPhoneNumbers}</div>
            <div class="ecom-form-field">
                [${uiLabelMap.PartyCountry}] [${uiLabelMap.PartyAreaCode}] [${uiLabelMap.PartyContactNumber}] [${uiLabelMap.PartyExtension}]</div>
            </div>
        </div>
        <div class="ecom-form-row">
            <div class="ecom-form-label"><div>${uiLabelMap.PartyHomePhone}</div><div>(${uiLabelMap.PartyAllowSolicitation}?)</div></div>
            <div class="ecom-form-field">
                <@fieldErrorsMulti fieldName1="CUSTOMER_HOME_COUNTRY" fieldName2="CUSTOMER_HOME_AREA" fieldName3="CUSTOMER_HOME_CONTACT" fieldName4="CUSTOMER_HOME_EXT"/>
                <input type="text" class='inputBox' name="CUSTOMER_HOME_COUNTRY" value="${requestParameters.CUSTOMER_HOME_COUNTRY?if_exists}" size="4" maxlength="10">
                -&nbsp;<input type="text" class='inputBox' name="CUSTOMER_HOME_AREA" value="${requestParameters.CUSTOMER_HOME_AREA?if_exists}" size="4" maxlength="10">
                -&nbsp;<input type="text" class='inputBox' name="CUSTOMER_HOME_CONTACT" value="${requestParameters.CUSTOMER_HOME_CONTACT?if_exists}" size="15" maxlength="15">
                &nbsp;ext&nbsp;<input type="text" class='inputBox' name="CUSTOMER_HOME_EXT" value="${requestParameters.CUSTOMER_HOME_EXT?if_exists}" size="6" maxlength="10">
                <br/>
                <select name="CUSTOMER_HOME_ALLOW_SOL" class='selectBox'>
                    <option>${requestParameters.CUSTOMER_HOME_ALLOW_SOL?default("Y")}</option>
                    <option></option><option>Y</option><option>N</option>
                </select>
            </div>
        </div>
        <div class="ecom-form-row">
            <div class="ecom-form-label"><div>${uiLabelMap.PartyBusinessPhone}</div><div>(${uiLabelMap.PartyAllowSolicitation}?)</div></div>
            <div class="ecom-form-field">
                <@fieldErrorsMulti fieldName1="CUSTOMER_WORK_COUNTRY" fieldName2="CUSTOMER_WORK_AREA" fieldName3="CUSTOMER_WORK_CONTACT" fieldName4="CUSTOMER_WORK_EXT"/>
                <input type="text" class='inputBox' name="CUSTOMER_WORK_COUNTRY" value="${requestParameters.CUSTOMER_WORK_COUNTRY?if_exists}" size="4" maxlength="10">
                -&nbsp;<input type="text" class='inputBox' name="CUSTOMER_WORK_AREA" value="${requestParameters.CUSTOMER_WORK_AREA?if_exists}" size="4" maxlength="10">
                -&nbsp;<input type="text" class='inputBox' name="CUSTOMER_WORK_CONTACT" value="${requestParameters.CUSTOMER_WORK_CONTACT?if_exists}" size="15" maxlength="15">
                &nbsp;ext&nbsp;<input type="text" class='inputBox' name="CUSTOMER_WORK_EXT" value="${requestParameters.CUSTOMER_WORK_EXT?if_exists}" size="6" maxlength="10">
                <br/>
                <select name="CUSTOMER_WORK_ALLOW_SOL" class='selectBox'>
                    <option>${requestParameters.CUSTOMER_WORK_ALLOW_SOL?default("Y")}</option>
                    <option></option><option>Y</option><option>N</option>
                </select>
            </div>
        </div>
        <div class="ecom-form-row">
            <div class="ecom-form-label"><div>${uiLabelMap.PartyFaxNumber}</div><div>(${uiLabelMap.PartyAllowSolicitation}?)</div></div>
            <div class="ecom-form-field">
                <@fieldErrorsMulti fieldName1="CUSTOMER_FAX_COUNTRY" fieldName2="CUSTOMER_FAX_AREA" fieldName3="CUSTOMER_FAX_CONTACT" fieldName4=""/>
                <input type="text" class='inputBox' name="CUSTOMER_FAX_COUNTRY" value="${requestParameters.CUSTOMER_FAX_COUNTRY?if_exists}" size="4" maxlength="10">
                -&nbsp;<input type="text" class='inputBox' name="CUSTOMER_FAX_AREA" value="${requestParameters.CUSTOMER_FAX_AREA?if_exists}" size="4" maxlength="10">
                -&nbsp;<input type="text" class='inputBox' name="CUSTOMER_FAX_CONTACT" value="${requestParameters.CUSTOMER_FAX_CONTACT?if_exists}" size="15" maxlength="15">
                <br/>
                <select name="CUSTOMER_FAX_ALLOW_SOL" class='selectBox'>
                    <option>${requestParameters.CUSTOMER_FAX_ALLOW_SOL?default("Y")}</option>
                    <option></option><option>Y</option><option>N</option>
                </select>
            </div>
        </div>
        <div class="ecom-form-row">
            <div class="ecom-form-label"><div>${uiLabelMap.PartyMobilePhone}</div><div>(${uiLabelMap.PartyAllowSolicitation}?)</div></div>
            <div class="ecom-form-field">
                <@fieldErrorsMulti fieldName1="CUSTOMER_MOBILE_COUNTRY" fieldName2="CUSTOMER_MOBILE_AREA" fieldName3="CUSTOMER_MOBILE_CONTACT" fieldName4=""/>
                <input type="text" class='inputBox' name="CUSTOMER_MOBILE_COUNTRY" value="${requestParameters.CUSTOMER_MOBILE_COUNTRY?if_exists}" size="4" maxlength="10">
                -&nbsp;<input type="text" class='inputBox' name="CUSTOMER_MOBILE_AREA" value="${requestParameters.CUSTOMER_MOBILE_AREA?if_exists}" size="4" maxlength="10">
                -&nbsp;<input type="text" class='inputBox' name="CUSTOMER_MOBILE_CONTACT" value="${requestParameters.CUSTOMER_MOBILE_CONTACT?if_exists}" size="15" maxlength="15">
                <br/>
                <select name="CUSTOMER_MOBILE_ALLOW_SOL" class='selectBox'>
                    <option>${requestParameters.CUSTOMER_MOBILE_ALLOW_SOL?default("Y")}</option>
                    <option></option><option>Y</option><option>N</option>
                </select>
            </div>
        </div>
        <div class="ecom-endcolumns"><span></span></div>
    </div>
</div>

<div class="ecom-screenlet">
    <div class="ecom-screenlet-header">
        <div class='boxhead'>&nbsp;${uiLabelMap.PartyEmailAddress}</div>
    </div>
    <div class="ecom-screenlet-body">
        <div class="ecom-form-row">
            <div class="ecom-form-label"><div>${uiLabelMap.PartyEmailAddress}</div><div>(${uiLabelMap.PartyAllowSolicitation}?)</div></div>
            <div class="ecom-form-field">
                <@fieldErrors fieldName="CUSTOMER_EMAIL"/>
                <div><input type="text" class='inputBox' name="CUSTOMER_EMAIL" value="${requestParameters.CUSTOMER_EMAIL?if_exists}" size="40" maxlength="255" onchange="changeEmail()" onkeyup="changeEmail()"> *</div>
                <div>
                    <select name="CUSTOMER_EMAIL_ALLOW_SOL" class='selectBox'>
                        <option>${requestParameters.CUSTOMER_EMAIL_ALLOW_SOL?default("Y")}</option>
                        <option></option><option>Y</option><option>N</option>
                    </select>
                </div>
            </div>
        </div>
<#--
        <div>Order Email addresses (comma separated)</div>
        <input type="text" name="CUSTOMER_ORDER_EMAIL" value="${requestParameters.CUSTOMER_ORDER_EMAIL?if_exists}" size="40" maxlength="80">
-->
        <div class="ecom-endcolumns"><span></span></div>
    </div>
</div>

<div class="ecom-screenlet">
    <div class="ecom-screenlet-header">
        <div class='boxhead'>&nbsp;<#if getUsername>${uiLabelMap.CommonUsername} & </#if>${uiLabelMap.CommonPassword}</div>
    </div>
    <div class="ecom-screenlet-body">
        <#if getUsername>
            <div class="ecom-form-row">
                <div class="ecom-form-label"><span class="tabletext">${uiLabelMap.CommonUsername}</span></div>
                <div class="ecom-form-field">
                    <@fieldErrors fieldName="USERNAME"/>
                    <div>Use Email Address: <input type="CHECKBOX" name="UNUSEEMAIL" value="on" onClick="setEmailUsername();" onFocus="setLastFocused(this);"/></div>
                    <div><input type="text" class='inputBox' name="USERNAME" value="${requestParameters.USERNAME?if_exists}" size="20" maxlength="50" onFocus="clickUsername();" onchange="changeEmail();"/> *</div>
                </div>
            </div>
        </#if>
        <#if createAllowPassword>
            <div class="ecom-form-row">
                <div class="ecom-form-label">${uiLabelMap.CommonPassword}</div>
                <div class="ecom-form-field">
                    <@fieldErrors fieldName="PASSWORD"/>
                    <input type="password" class='inputBox' name="PASSWORD" value="" size="20" maxlength="50" onFocus="setLastFocused(this);"/> *
                </div>
            </div>
            <div class="ecom-form-row">
                <div class="ecom-form-label">${uiLabelMap.PartyRepeatPassword}</div>
                <div class="ecom-form-field">
                    <@fieldErrors fieldName="CONFIRM_PASSWORD"/>
                    <input type="password" class='inputBox' name="CONFIRM_PASSWORD" value="" size="20" maxlength="50"/> *
                </div>
            </div>
            <div class="ecom-form-row">
                <div class="ecom-form-label"><span class="tabletext">${uiLabelMap.PartyPasswordHint}</span></div>
                <div class="ecom-form-field">
                    <@fieldErrors fieldName="PASSWORD_HINT"/>
                    <input type="text" class='inputBox' name="PASSWORD_HINT" value="${requestParameters.PASSWORD_HINT?if_exists}" size="30" maxlength="100"/>
                </div>
            </div>
        <#else/>
            <div class="ecom-form-row">
                <div class="ecom-form-label">${uiLabelMap.CommonPassword}</div>
                <div class="ecom-form-field">
                    ${uiLabelMap.PartyRecievePasswordByEmail}.
                </div>
            </div>
        </#if>
        <div class="ecom-endcolumns"><span></span></div>
    </div>
</div>

<input type="image" src="<@ofbizContentUrl>/images/spacer.gif</@ofbizContentUrl>" onClick="javascript:document.newuserform.submit();">
</form>

<div class="tabletext">${uiLabelMap.CommonFieldsMarkedAreRequired}</div>

<div>
&nbsp;&nbsp;<a href="<@ofbizUrl>/checkLogin/main</@ofbizUrl>" class="buttontextbig">[${uiLabelMap.CommonBack}]</a>
&nbsp;&nbsp;<a href="javascript:document.newuserform.submit()" class="buttontextbig">[${uiLabelMap.CommonSave}]</a>
</div>

<br/>
