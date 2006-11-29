<#--
Copyright 2001-2006 The Apache Software Foundation

Licensed under the Apache License, Version 2.0 (the "License"); you may not
use this file except in compliance with the License. You may obtain a copy of
the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
License for the specific language governing permissions and limitations
under the License.
-->

<div class="screenlet">
    <div class="screenlet-header">
        <div class='boxhead'>&nbsp;${uiLabelMap.OrderShippingInformation}</div>
    </div>
    <div class="screenlet-body">
      <form method="post" action="<@ofbizUrl>processShipSettings</@ofbizUrl>" name="${parameters.formNameValue}">
        <input type="hidden" name="contactMechId" value="${parameters.shippingContactMechId?if_exists}"/>
        <input type="hidden" name="partyId" value="${cart.getPartyId()?default("_NA_")}"/>

        <table width="100%" border="0" cellpadding="1" cellspacing="0">
          <tr>
            <td width="26%" align="right" valign="top"><div class="tableheadtext">${uiLabelMap.OrderShippingAddress}</div></td>
            <td width="5">&nbsp;</td>
            <td width="74%">&nbsp;</td>
          </tr>
            ${screens.render("component://ecommerce/widget/OrderScreens.xml#genericaddress")}
          <tr>
            <td colspan="3" align="center"><input type="submit" class="smallsubmit" value="${uiLabelMap.CommonContinue}"/></td>
          </tr>
        </table>
        </form>
    </div>
</div>
