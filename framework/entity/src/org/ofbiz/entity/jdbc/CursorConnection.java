/*******************************************************************************
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
 *******************************************************************************/
package org.ofbiz.entity.jdbc;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;


public class CursorConnection extends AbstractCursorHandler {

    public static Connection newCursorConnection(Connection con, String cursorName, int pageSize) throws Exception {
        return newHandler(new CursorConnection(con, cursorName, pageSize), Connection.class);
    }

    protected Connection con;

    protected CursorConnection(Connection con, String cursorName, int fetchSize) {
        super(cursorName, fetchSize);
        this.con = con;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getName().equals("prepareStatement")) {
            System.err.println("prepareStatement");
            args[0] = "DECLARE " + cursorName + " CURSOR FOR " + args[0];
            PreparedStatement pstmt = (PreparedStatement) method.invoke(con, args);
            return CursorStatement.newCursorPreparedStatement(pstmt, cursorName, fetchSize);
        } else if (method.getName().equals("createStatement")) {
            System.err.println("createStatement");
            Statement stmt = (Statement) method.invoke(con, args);
            return CursorStatement.newCursorStatement(stmt, cursorName, fetchSize);
        }
        return super.invoke(con, proxy, method, args);
    }
}
