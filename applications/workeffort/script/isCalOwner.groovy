/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.util.*;
import org.ofbiz.entity.util.*;

boolean isCalOwner = false;
List partyAssignments = EntityUtil.filterByDate(delegator.findByAnd("WorkEffortPartyAssignment", ["workEffortId" : parameters.workEffortId, "partyId" : parameters.userLogin.partyId]));
for (partyAssign in partyAssignments) {
    if ("CAL_OWNER".equals(partyAssign.roleTypeId) || "CAL_DELEGATE".equals(partyAssign.roleTypeId)) {
        isCalOwner = true;
        break;
    }
}
context.isCalOwner = isCalOwner;
