package org.ofbiz.oagis;

/**
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
**/
import java.util.Map;

import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.ServiceUtil;

public class OagisServices {
    
    public static final String module = OagisServices.class.getName();
    
    public static Map sendConfirmBod(DispatchContext ctx, Map context) {
        return ServiceUtil.returnError("Service not Implemented");
    }

    public static Map receiveConfirmBod(DispatchContext ctx, Map context) {
        return ServiceUtil.returnError("Service not Implemented");
    }
}