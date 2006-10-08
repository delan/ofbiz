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
package org.ofbiz.webapp.control;

import javax.servlet.http.HttpSessionActivationListener;
import javax.servlet.http.HttpSessionEvent;

import org.ofbiz.base.util.Debug;

/**
 * HttpSessionListener that gathers and tracks various information and statistics
 */
public class ControlActivationEventListener implements HttpSessionActivationListener {
    // Debug module name
    public static final String module = ControlActivationEventListener.class.getName();

    public ControlActivationEventListener() {}

    public void sessionWillPassivate(HttpSessionEvent event) {
        ControlEventListener.countPassivateSession();
        Debug.logInfo("Passivating session: " + event.getSession().getId(), module);
    }

    public void sessionDidActivate(HttpSessionEvent event) {
        ControlEventListener.countActivateSession();
        Debug.logInfo("Activating session: " + event.getSession().getId(), module);
    }
}
