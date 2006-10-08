/*
 * Copyright 2001-2006 The Apache Software Foundation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package org.ofbiz.service.rmi.socket.ssl;

import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;
import java.rmi.server.RMIClientSocketFactory;
import java.security.GeneralSecurityException;
import javax.net.ssl.SSLSocketFactory;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.SSLUtil;

/**
 * RMI SSL Client Socket Factory
 */
public class SSLClientSocketFactory implements RMIClientSocketFactory, Serializable {

    public static final String module = SSLClientSocketFactory.class.getName();

    public Socket createSocket(String host, int port) throws IOException {
        try {
            SSLSocketFactory factory = SSLUtil.getSSLSocketFactory();
            return factory.createSocket(host, port);
        } catch (GeneralSecurityException e) {
            Debug.logError(e, module);
            throw new IOException(e.getMessage());
        }
    }
}
