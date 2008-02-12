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

<#if security.hasEntityPermission("ORDERMGR", "_VIEW", session)>
<div class="screenlet">
    <div class="screenlet-title-bar">
      <ul>
        <li class="h3">${uiLabelMap.OrderAddNote}</li>
      </ul>
      <br class="clear"/>
    </div>
    <div class="screenlet-body">      
        <a href="<@ofbizUrl>authview/${donePage}</@ofbizUrl>" class="buttontext">${uiLabelMap.CommonGoBack}</a>
        <a href="javascript:document.createnoteform.submit()" class="buttontext">${uiLabelMap.CommonSave}</a>

        <form method="post" action="<@ofbizUrl>createordernote/${donePage}</@ofbizUrl>" name="createnoteform">
            <table class="basic-table" cellspacing='0'>
              <tr>
                <td width="26%" align="right"><span class="label">${uiLabelMap.OrderNote}</span></td>
                <td width="54%">
                  <textarea name="note" rows="5" cols="70"></textarea>
                </td>
              </tr>
              <tr>
                 <td>&nbsp;</td>
                 <td><span class="label">${uiLabelMap.OrderInternalNote}</span>
                    <select name="internalNote" size="1">
                    <option value=""></option>
                    <option value="Y" selected>${uiLabelMap.CommonYes}</option>
                    <option value="N">${uiLabelMap.CommonNo}</option>
                    </select>
                    <span class="tooltip">${uiLabelMap.OrderInternalNoteMessage}</span>
                 </td>
              </tr>
            </table>
        </form>

        <a href="<@ofbizUrl>authview/${donePage}</@ofbizUrl>" class="buttontext">${uiLabelMap.CommonGoBack}</a>
        <a href="javascript:document.createnoteform.submit()" class="buttontext">${uiLabelMap.CommonSave}</a>      
    </div>
</div>
<#else>
  <h3>${uiLabelMap.OrderViewPermissionError}</h3>
</#if>