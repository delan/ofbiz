<#--
$Id$

Copyright 2004-2006 The Apache Software Foundation

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

<#assign countries = Static["org.ofbiz.common.CommonWorkers"].getCountryList(delegator)>
<#list countries as country>
    <option value='${country.geoId}'>${country.geoName?default(country.geoId)}</option>
</#list>
