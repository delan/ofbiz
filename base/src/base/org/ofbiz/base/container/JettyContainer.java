/*
 * $Id: JettyContainer.java,v 1.9 2003/08/20 02:33:13 ajzeneski Exp $
 *
 * Copyright (c) 2003 The Open For Business Project - www.ofbiz.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT
 * OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR
 * THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */
package org.ofbiz.base.container;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.mortbay.http.NCSARequestLog;
import org.mortbay.http.SocketListener;
import org.mortbay.http.SunJsseListener;
import org.mortbay.http.ajp.AJP13Listener;
import org.mortbay.jetty.Server;
import org.mortbay.util.Frame;
import org.mortbay.util.Log;
import org.mortbay.util.LogSink;
import org.mortbay.util.MultiException;
import org.mortbay.util.ThreadedServer;
import org.ofbiz.base.component.ComponentConfig;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilURL;

/**
 * JettyContainer - Container implementation for Jetty
 * This container depends on the ComponentContainer as well.
 *
 * @author     <a href="mailto:jaz@ofbiz.org">Andy Zeneski</a> 
  *@version    $Revision: 1.9 $
 * @since      3.0
 */
public class JettyContainer implements Container {
    
    public static final String module = JettyContainer.class.getName();    
    private Map servers = new HashMap();
    
    private void init(String configFile) throws ContainerException {
        // configure jetty logging        
        Log log = Log.instance();
        log.disableLog();
        Log4jSink sink = new Log4jSink();
        log.add(sink);
        sink.setOptions(UtilURL.fromResource("debug.properties").toExternalForm());        
        try {
            sink.start();
        } catch (Exception e) {
            Debug.logWarning(e, module);            
        }

        // get the container        
        ContainerConfig.Container jc = ContainerConfig.getContainer("jetty-container", configFile);
                        
        // create the servers
        Iterator sci = jc.properties.values().iterator();
        while (sci.hasNext()) {
            ContainerConfig.Container.Property prop = (ContainerConfig.Container.Property) sci.next();
            servers.put(prop.name, createServer(prop));                                   
        }
        
        // load the applications
        Collection componentConfigs = ComponentConfig.getAllComponents();
        if (componentConfigs != null) {
            Iterator components = componentConfigs.iterator();
            while (components.hasNext()) {
                ComponentConfig component = (ComponentConfig) components.next();                
                Iterator appInfos = component.getWebappInfos().iterator();
                while (appInfos.hasNext()) {
                    ComponentConfig.WebappInfo appInfo = (ComponentConfig.WebappInfo) appInfos.next();
                    Server server = (Server) servers.get(appInfo.server);
                    if (server == null) {
                        Debug.logWarning("Server with name [" + appInfo.server + "] not found; not mounting [" + appInfo.name + "]", module);
                    } else {
                        try {                            
                            String location = component.getRootLocation() + appInfo.location;
                            location = location.replace('\\', '/');
                            if (!location.endsWith("/")) {
                                location = location + "/";
                            }
                            server.addWebApplication(appInfo.mountPoint, location);
                        } catch (IOException e) {
                            Debug.logError(e, "Problem mounting application [" + appInfo.name + " / " + appInfo.location + "]", module);                        
                        }
                    }                    
                }                        
            }
        }                
    }
    
    private Server createServer(ContainerConfig.Container.Property serverConfig) throws ContainerException {
        Server server = new Server();        
        
        // configure the listeners/loggers
        int listeners = 0;
        Iterator properties = serverConfig.properties.values().iterator();
        while (properties.hasNext()) {
            ContainerConfig.Container.Property props = 
                    (ContainerConfig.Container.Property) properties.next();
                                
            if ("listener".equals(props.value)) {                        
                if ("default".equals(props.getProperty("type").value)) {
                    SocketListener listener = new SocketListener();
                    setListenerOptions(listener, props);
                    if (props.getProperty("low-resource-persist-time") != null) {
                        int value = 0;
                        try {
                            value = Integer.parseInt(props.getProperty("low-resource-persist-time").value);
                        } catch (NumberFormatException e) {
                            value = 0;
                        }
                        if (value > 0) {
                            listener.setLowResourcePersistTimeMs(value);
                        }
                    }                
                    server.addListener(listener);                                               
                } else if ("sun-jsse".equals(props.getProperty("type").value)) {
                    SunJsseListener listener = new SunJsseListener();
                    setListenerOptions(listener, props);
                    if (props.getProperty("keystore") != null) {
                        listener.setKeystore(props.getProperty("keystore").value);    
                    }
                    if (props.getProperty("password") != null) {
                        listener.setKeystore(props.getProperty("password").value);    
                    }                
                    if (props.getProperty("key-password") != null) {
                        listener.setKeystore(props.getProperty("key-password").value);    
                    }
                    if (props.getProperty("low-resource-persist-time") != null) {
                        int value = 0;
                        try {
                            value = Integer.parseInt(props.getProperty("low-resource-persist-time").value);
                        } catch (NumberFormatException e) {
                            value = 0;
                        }
                        if (value > 0) {
                            listener.setLowResourcePersistTimeMs(value);
                        }
                    }                                               
                    server.addListener(listener);
                } else if ("ibm-jsse".equals(props.getProperty("type").value)) {
                    throw new ContainerException("Listener not supported yet [" + props.getProperty("type").value + "]");
                } else if ("nio".equals(props.getProperty("type").value)) {
                    throw new ContainerException("Listener not supported yet [" + props.getProperty("type").value + "]");
                } else if ("ajp13".equals(props.getProperty("type").value)) {
                    AJP13Listener listener = new AJP13Listener();
                    setListenerOptions(listener, props);
                    server.addListener(listener);                
                }
            } else if ("request-log".equals(props.value)) {
                NCSARequestLog rl = new NCSARequestLog();
                
                if (props.getProperty("filename") != null) {
                    rl.setFilename(props.getProperty("filename").value);
                }
                
                if (props.getProperty("append") != null) {
                    rl.setAppend("true".equalsIgnoreCase(props.getProperty("append").value));
                }
                
                if (props.getProperty("buffered") != null) {
                    rl.setBuffered("true".equalsIgnoreCase(props.getProperty("buffered").value));
                }
                
                if (props.getProperty("extended") != null) {
                    rl.setExtended("true".equalsIgnoreCase(props.getProperty("extended").value));
                }
                
                if (props.getProperty("timezone") != null) {
                    rl.setLogTimeZone(props.getProperty("timezone").value);
                }
                
                if (props.getProperty("date-format") != null) {
                    rl.setLogDateFormat(props.getProperty("date-format").value);
                }
                
                if (props.getProperty("retain-days") != null) {
                    int days = 90;
                    try {
                        days = Integer.parseInt(props.getProperty("retain-days").value);
                    } catch (NumberFormatException e) {
                        days = 90;
                    }
                    rl.setRetainDays(days);                   
                }
                server.setRequestLog(rl);               
            }                       
        }
        return server;
    }  
    
    private void setListenerOptions(ThreadedServer listener, ContainerConfig.Container.Property listenerProps) throws ContainerException {
        if (listenerProps.getProperty("host") != null) {
            try {
                listener.setHost(listenerProps.getProperty("host").value);
            } catch (UnknownHostException e) {
                throw new ContainerException(e);                       
            }
        } else {
            try {
                listener.setHost("0.0.0.0");
            } catch (UnknownHostException e) {
                throw new ContainerException(e);
            }          
        }
        
        if (listenerProps.getProperty("port") != null) {
            int value = 8080;
            try {
                value = Integer.parseInt(listenerProps.getProperty("port").value);
            } catch (NumberFormatException e) {
                value = 8080;
            }
            if (value == 0) value = 8080;
            
            listener.setPort(value);
        } else {
            listener.setPort(8080);
        }
        
        if (listenerProps.getProperty("min-threads") != null) {
            int value = 0;
            try {
                value = Integer.parseInt(listenerProps.getProperty("min-threads").value);
            } catch (NumberFormatException e) {
                value = 0;
            }
            if (value > 0) {
                listener.setMinThreads(value);
            }
        }
        
        if (listenerProps.getProperty("max-threads") != null) {
            int value = 0;
            try {
                value = Integer.parseInt(listenerProps.getProperty("max-threads").value);
            } catch (NumberFormatException e) {
                value = 0;
            }
            if (value > 0) {
                listener.setMaxThreads(value);
            }
        }
        
        if (listenerProps.getProperty("max-idle-time") != null) {
            int value = 0;
            try {
                value = Integer.parseInt(listenerProps.getProperty("max-idle-time").value);
            } catch (NumberFormatException e) {
                value = 0;
            }
            if (value > 0) {
                listener.setMaxIdleTimeMs(value);
            }
        }                                   
    }  
    
    /**
     * @see org.ofbiz.base.start.StartupContainer#start(java.lang.String)
     */
    public boolean start(String configFile) throws ContainerException {        
        // start the server(s)
        this.init(configFile);
        if (servers != null) {
            Iterator i = servers.values().iterator();
            while (i.hasNext()) {
                Server server = (Server) i.next();
                try {                    
                    server.start();                                                                              
                } catch (MultiException e) {                    
                    throw new ContainerException(e);
                }
            }
        }                                        
        return true;
    }
        
    /**
     * @see org.ofbiz.base.start.StartupContainer#stop()
     */
    public void stop() throws ContainerException {
        if (servers != null) {
            Iterator i = servers.values().iterator();
            while(i.hasNext()) {
                Server server = (Server) i.next();
                try {
                    server.stop();
                } catch (InterruptedException e) {
                    Debug.logWarning(e, module);                    
                }
            }
        }
    }              
}

// taken from JettyPlus
class Log4jSink implements LogSink {

    private String _options;
    private transient boolean _started;
        
    public void setOptions(String filename) {
        _options=filename;
    }
       
    public String getOptions() {
        return _options;
    }
       
    public void start() throws Exception {
        _started=true;
    }
       
    public void stop() {    
        _started=false;
    }
   
    public boolean isStarted() {    
        return _started;
    }
       
    public void log(String tag, Object msg, Frame frame, long time) {    
        String method = frame.getMethod();
        int lb = method.indexOf('(');
        int ld = (lb > 0) ? method.lastIndexOf('.', lb) : method.lastIndexOf('.');
        if (ld < 0) ld = lb;
        String class_name = (ld > 0) ? method.substring(0,ld) : method;
        
        Logger log = Logger.getLogger(class_name);

        Priority priority = Priority.INFO;

        if (Log.DEBUG.equals(tag)) {
            priority = Priority.DEBUG;
        } else if (Log.WARN.equals(tag) || Log.ASSERT.equals(tag)) {
            priority = Priority.ERROR;
        } else if (Log.FAIL.equals(tag)) {
            priority = Priority.FATAL;
        }
        
        if (!log.isEnabledFor(priority)) {
            return;
        }

        log.log(Log4jSink.class.getName(), priority, "" + msg, null);
    }
    
    public synchronized void log(String s) {
        Logger.getRootLogger().log("jetty.log4jSink", Priority.INFO, s, null);
    }    
}
