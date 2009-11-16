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
package org.ofbiz.sql;

import java.util.Iterator;
import java.util.List;

import org.ofbiz.base.util.StringUtil;

public final class MathValue extends StaticValue implements Iterable<StaticValue> {
    private final String op;
    private final List<StaticValue> values;

    public MathValue(String op, List<StaticValue> values) {
        this.op = op;
        this.values = values;
    }

    public String getOp() {
        return op;
    }

    public String getDefaultName() {
        return null;
    }

    public Iterator<StaticValue> iterator() {
        return values.iterator();
    }

    public StringBuilder appendTo(StringBuilder sb) {
        sb.append('(');
        StringUtil.appendTo(sb, values, " ", null, op);
        sb.append(')');
        return sb;
    }
}
