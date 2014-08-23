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

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.config.model.EntityConfig;
import org.ofbiz.entity.config.model.JdbcElement;
import org.ofbiz.entity.connection.ConnectionFactoryInterface;
import org.ofbiz.entity.datasource.GenericHelperInfo;
import org.ofbiz.entity.transaction.TransactionFactoryLoader;

/**
 * ConnectionFactory - central source for JDBC connections
 *
 */
public class ConnectionFactory {
    // Debug module name
    public static final String module = ConnectionFactory.class.getName();
    private static final AtomicReference<ConnectionFactoryInterface> connFactoryRef = new AtomicReference<ConnectionFactoryInterface>(null);

    private static ConnectionFactoryInterface createConnectionFactoryInterface() throws Exception {
        String className = EntityConfig.getInstance().getConnectionFactory().getClassName();
        if (className == null) {
            throw new IllegalStateException("Could not find connection factory class name definition");
        }
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        Class<?> tfClass = loader.loadClass(className);
        return (ConnectionFactoryInterface) tfClass.newInstance();
    }


    public static Connection getConnection(String driverName, String connectionUrl, Properties props, String userName, String password) throws SQLException {
        // first register the JDBC driver with the DriverManager
        if (driverName != null) {
            ConnectionFactory.loadDriver(driverName);
        }

        try {
            if (UtilValidate.isNotEmpty(userName))
                return DriverManager.getConnection(connectionUrl, userName, password);
            else if (props != null)
                return DriverManager.getConnection(connectionUrl, props);
            else
                return DriverManager.getConnection(connectionUrl);
        } catch (SQLException e) {
            Debug.logError(e, "SQL Error obtaining JDBC connection", module);
            throw e;
        }
    }

    public static Connection getConnection(String connectionUrl, String userName, String password) throws SQLException {
        return getConnection(null, connectionUrl, null, userName, password);
    }

    public static Connection getConnection(String connectionUrl, Properties props) throws SQLException {
        return getConnection(null, connectionUrl, props, null, null);
    }

    public static Connection getConnection(String helperName) throws SQLException, GenericEntityException {
        return getConnection(new GenericHelperInfo(null, helperName));
    }

    public static Connection getConnection(GenericHelperInfo helperInfo) throws SQLException, GenericEntityException {
        // Debug.logVerbose("Getting a connection", module);

        Connection con = TransactionFactoryLoader.getInstance().getConnection(helperInfo);
        if (con == null) {
            Debug.logError("******* ERROR: No database connection found for helperName \"" + helperInfo.getHelperFullName() + "\"", module);
        }
        return con;
    }

    private static ConnectionFactoryInterface getManagedConnectionFactory() {
        ConnectionFactoryInterface instance = connFactoryRef.get();
        if (instance == null) {
            try {
                instance = createConnectionFactoryInterface();
                if (!connFactoryRef.compareAndSet(null, instance)) {
                    instance = connFactoryRef.get();
                }
            } catch (Exception e) {
                Debug.logError(e, "Exception thrown while creating ConnectionFactoryInterface instance: ", module);
                throw new IllegalStateException("Error loading ConnectionFactoryInterface class: " + e);
            }
        }
        return instance;
    }

    public static Connection getManagedConnection(GenericHelperInfo helperInfo, JdbcElement jdbcElement) throws SQLException, GenericEntityException {
        return getManagedConnectionFactory().getConnection(helperInfo, jdbcElement);
    }

    public static void closeAllManagedConnections() {
        getManagedConnectionFactory().closeAll();
    }

    public static void loadDriver(String driverName) throws SQLException {
        if (DriverManager.getDriver(driverName) == null) {
            try {
                Driver driver = (Driver) Class.forName(driverName, true, Thread.currentThread().getContextClassLoader()).newInstance();
                DriverManager.registerDriver(driver);
            } catch (ClassNotFoundException e) {
                Debug.logWarning(e, "Unable to load driver [" + driverName + "]", module);
            } catch (InstantiationException e) {
                Debug.logWarning(e, "Unable to instantiate driver [" + driverName + "]", module);
            } catch (IllegalAccessException e) {
                Debug.logWarning(e, "Illegal access exception [" + driverName + "]", module);
            }
        }
    }
}
