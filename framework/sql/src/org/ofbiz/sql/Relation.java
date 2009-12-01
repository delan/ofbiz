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

public final class Relation extends Atom implements Iterable<KeyMap> {
    private final String type;
    private final String title;
    private final String entityName;
    private final List<KeyMap> keyMaps;

    public Relation(String type, String title, String entityName, List<KeyMap> keyMaps) {
        this.type = type;
        this.title = title;
        this.entityName = entityName;
        this.keyMaps = keyMaps;
    }

    public String getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getEntityName() {
        return entityName;
    }

    public Iterator<KeyMap> iterator() {
        return keyMaps.iterator();
    }

    public StringBuilder appendTo(StringBuilder sb) {
        sb.append("RELATION");
        if (type != null) {
            sb.append(' ').append(type);
        }
        if (title != null) {
            sb.append(' ').append(title);
        }
        sb.append(' ').append(entityName);
        sb.append(" ON");
        for (int i = 0; i < keyMaps.size(); i++) {
            sb.append(' ');
            keyMaps.get(i).appendTo("cur", "other", sb);
        }
        return sb;
    }
}
