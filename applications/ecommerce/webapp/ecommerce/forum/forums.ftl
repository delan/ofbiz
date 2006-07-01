<#--
$Id: forums.ftl 7615 2006-05-16 03:15:25Z jonesde $

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
        <div class="boxhead">${uiLabelMap.ProductBrowseForums}</div>
    </div>
    <div class="screenlet-body">
            <#list forums as forum>
            <div class="browsecategorytext" style="margin-left: 10px">
               -&nbsp;<a href="<@ofbizUrl>showforum?forumId=${forum.contentId}</@ofbizUrl>" class="browsecategorybutton">${forum.contentName}</a>
            </div>
            </#list>   
    </div>
</div>
