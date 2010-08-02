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

public final class NumberValue<N extends Number> extends ConstantValue {
    private final N number;

    public static NumberValue<Long> valueOf(long v) {
        return new NumberValue<Long>(v);
    }

    public static NumberValue<Double> valueOf(double v) {
        return new NumberValue<Double>(v);
    }

    public NumberValue(N number) {
        this.number = number;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public N getNumber() {
        return number;
    }

    public boolean equals(Object o) {
        if (o instanceof NumberValue) {
            NumberValue other = (NumberValue) o;
            return number.equals(other.number);
        } else {
            return false;
        }
    }

    public StringBuilder appendTo(StringBuilder sb) {
        sb.append(number);
        return sb;
    }
}
