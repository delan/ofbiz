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
package org.ofbiz.base.util;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.ofbiz.base.config.GenericConfigException;
import org.ofbiz.base.config.JNDIConfigUtil;
import org.ofbiz.base.util.cache.UtilCache;

/**
 * JNDIContextFactory - central source for JNDI Contexts by helper name
 *
 * @author     <a href="mailto:jonesde@ofbiz.org">David E. Jones</a>
 * @author     <a href="mailto:jaz@ofbiz.org">Andy Zeneski</a>
 * @version    $Rev$
 * @since      2.0
 */
public class JNDIContextFactory {
    
    public static final String module = JNDIContextFactory.class.getName();
    static UtilCache contexts = new UtilCache("entity.JNDIContexts", 0, 0);

    /** 
     * Return the initial context according to the entityengine.xml parameters that correspond to the given prefix
     * @return the JNDI initial context
     */
    public static InitialContext getInitialContext(String jndiServerName) throws GenericConfigException {
        InitialContext ic = (InitialContext) contexts.get(jndiServerName);

        if (ic == null) {
            synchronized (JNDIContextFactory.class) {
                ic = (InitialContext) contexts.get(jndiServerName);

                if (ic == null) {
                    JNDIConfigUtil.JndiServerInfo jndiServerInfo = JNDIConfigUtil.getJndiServerInfo(jndiServerName);

                    if (jndiServerInfo == null) {
                        throw new GenericConfigException("ERROR: no jndi-server definition was found with the name " + jndiServerName + " in jndiservers.xml");
                    }

                    try {
                        if (UtilValidate.isEmpty(jndiServerInfo.contextProviderUrl)) {
                            ic = new InitialContext();
                        } else {
                            Hashtable h = new Hashtable();

                            h.put(Context.INITIAL_CONTEXT_FACTORY, jndiServerInfo.initialContextFactory);
                            h.put(Context.PROVIDER_URL, jndiServerInfo.contextProviderUrl);
                            if (jndiServerInfo.urlPkgPrefixes != null && jndiServerInfo.urlPkgPrefixes.length() > 0)
                                h.put(Context.URL_PKG_PREFIXES, jndiServerInfo.urlPkgPrefixes);

                            if (jndiServerInfo.securityPrincipal != null && jndiServerInfo.securityPrincipal.length() > 0)
                                h.put(Context.SECURITY_PRINCIPAL, jndiServerInfo.securityPrincipal);
                            if (jndiServerInfo.securityCredentials != null && jndiServerInfo.securityCredentials.length() > 0)
                                h.put(Context.SECURITY_CREDENTIALS, jndiServerInfo.securityCredentials);

                            ic = new InitialContext(h);
                        }
                    } catch (Exception e) {
                        String errorMsg = "Error getting JNDI initial context for server name " + jndiServerName;

                        Debug.logError(e, errorMsg, module);
                        throw new GenericConfigException(errorMsg, e);
                    }

                    if (ic != null) {
                        contexts.put(jndiServerName, ic);
                    }
                }
            }
        }

        return ic;
    }
    /**
     * Removes an entry from the JNDI cache.
     * @param jndiServerName
     */
    public static void clearInitialContext(String jndiServerName) {
        InitialContext ic = (InitialContext) contexts.get(jndiServerName);
        if (ic != null) 
            contexts.remove(jndiServerName);
    }

}
