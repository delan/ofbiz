/*
 * $Id$
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
package org.ofbiz.core.start;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

/**
 * Start - OFBiz/Jetty Startup Class
 *
 * @author     <a href="mailto:jaz@ofbiz.org">Andy Zeneski</a> 
  *@version    $Revision$
 * @since      2.1
 */
public class Start {

    public static final String config = "org/ofbiz/core/start/start.properties";
    private Configuration conf = new Configuration(Start.config);          
    private Classpath classPath = new Classpath(System.getProperty("java.class.path"));   
     
    public void run(String args[]) throws Exception {
        ArrayList xargs = new ArrayList();
        xargs.add(conf.configFile);
        for (int i = 0; i < args.length; i++) {
            xargs.add(args[i]);
            System.out.println("Arg " + i + ": " + args[i]);
        }
        ClassLoader cl = classPath.getClassLoader();
        
        System.out.println("");       
        System.out.println("java.home......: " + conf.javaHome);
        System.out.println("java.version...: " + conf.javaVersion);
        System.out.println("ofbiz.home.....: " + conf.ofbizHome);        
        System.out.println("server.class...: " + conf.serverClass);
        System.out.println("config.file....: " + conf.configFile);
        System.out.println("");                       
        
        System.setProperty("java.class.path", classPath.toString());
        Thread.currentThread().setContextClassLoader(cl);   
        
        // stat the log directory
        File logDir = new File(conf.jettyHome + "/logs");
        if (!logDir.exists())
            logDir.mkdir();
        
        // invoke the jetty server                            
        invokeMain(cl, conf.serverClass, (String[]) xargs.toArray(args));
    }
  
    private void loadLibs() throws IOException {  
        // load the OFB jars
        Iterator jarIt = conf.jarList.iterator();
        while (jarIt.hasNext()) {
            String pathStr = (String) jarIt.next(); 
            System.out.println("Loading " + pathStr + "...");           
            if (pathStr.endsWith(".zip") || pathStr.endsWith(".jar")) {             
                classPath.addComponent(pathStr);
            } else {
                File path = new File(pathStr);
                loadJarsFromPath(path, null);
            }
        }
       
        // load the OFB dirs
        Iterator dirIt = conf.dirList.iterator();
        while (dirIt.hasNext()) {
            String pathStr = (String) dirIt.next();
            classPath.addComponent(pathStr);                        
        }
        
        // try to load tools.jar      
        classPath.addComponent(conf.javaHome + "/lib/tools.jar");      
        classPath.addComponent(conf.javaHome + "/../lib/tools.jar");  
        
        // now load the required jetty lib based on java.version
        List excludes = new ArrayList();
        classPath.addClasspath(conf.ofbizHome + "/lib/jetty/lib/javax.servlet.jar");
        if (conf.javaVersion.startsWith("1.4")) {
            excludes.add("crimson.jar");
            excludes.add("javax.xml.jaxp.jar");
            classPath.addComponent(conf.ofbizHome + "/lib/jetty/lib/org.mortbay.jetty.jar");
        } else {
            classPath.addComponent(conf.ofbizHome + "/lib/jetty/lib/org.mortbay.jetty-jdk1.2.jar");
        }  
        loadJars(conf.ofbizHome + "/lib/jetty/ext", excludes);                                                   
    }
    
    private void loadJars(String parent, List excludes) throws IOException {  
        // note this is not recursive
        File libDir = new File(parent);  
        loadJarsFromPath(libDir, excludes);      
        String paths[] = libDir.list();
        for (int i = 0; i < paths.length; i++) {            
            File file = new File(libDir.getCanonicalPath() + "/" + paths[i]);           
            if (file.isDirectory() && !paths[i].equals("CVS") && !paths[i].equals("compile"))
                loadJarsFromPath(file, excludes);            
        }
    }
    
    private void loadJarsFromPath(File path, List excludes) {        
        File files[] = path.listFiles();
        for (int i = 0; i < files.length; i++) {
            String file = files[i].getName();
            if (file.endsWith(".jar") || file.endsWith(".zip")) {
                if (excludes != null && excludes.size() > 0 && excludes.contains(file)) {
                    // do not add these
                } else {
                    classPath.addComponent(files[i]);
                }
            }
        }        
    }
    
    public static void invokeMain(ClassLoader classloader, String classname, String[] args) throws Exception {                
        Class invoked_class = null;
        invoked_class = classloader.loadClass(classname);
        Class[] method_param_types = new Class[1];
        method_param_types[0] = args.getClass();
        Method main = null;
        main = invoked_class.getDeclaredMethod("main", method_param_types);
        Object[] method_params = new Object[1];
        method_params[0] = args;
        main.invoke(null, method_params);
    }

    public static void main(String[] args) throws Exception {
        Start start = new Start();        
        start.loadLibs();
        start.run(args);
    }
}

class Configuration {
        
    protected String serverClass;
    protected String configFile;
    protected String ofbizHome;
    protected String jettyHome;
    protected String javaHome;
    protected String javaVersion;
    protected List jarList;
    protected List dirList;
    
    protected Configuration(String config) {
        try {
            setConfig(config);
        } catch (IOException e) {
            e.printStackTrace();
        }        
    }
    
    protected void setConfig(String config) throws IOException {                 
        ofbizHome = System.getProperty("ofbiz.home");
        jettyHome = System.getProperty("jetty.home");
        javaHome = System.getProperty("java.home");
        javaVersion = System.getProperty("java.version");
        jarList = new ArrayList();
        dirList = new ArrayList();
        
        InputStream propsStream = getClass().getClassLoader().getResourceAsStream(config);
        Properties props = new Properties();
        props.load(propsStream); 
        
        if (ofbizHome == null)
            ofbizHome = props.getProperty("ofbiz.home", ".");                
        if (jettyHome == null)
            jettyHome = props.getProperty("jetty.home", ofbizHome);
        if (javaHome == null)
            javaHome = props.getProperty("java.home");
        
        System.setProperty("ofbiz.home", ofbizHome);
        System.setProperty("jetty.home", jettyHome);
        System.setProperty("java.home", javaHome);
        
        // set the property to tell Jetty to use 2.4 SessionListeners
        System.setProperty("org.mortbay.jetty.servlet.AbstractSessionManager.24SessionDestroyed", "true");
                
        serverClass = props.getProperty("server.class", "org.mortbay.jetty.Server.class");  
        configFile = props.getProperty("config.file", ofbizHome + "/setup/jetty/etc/ofbiz.xml");   
                
        // get the lib dir prefix names
        String prefixNames = props.getProperty("library.configs");
        StringTokenizer st = new StringTokenizer(prefixNames, ",");
        List configList = null;
        if (st != null && st.hasMoreTokens()) {
            configList = new ArrayList();

            while (st.hasMoreTokens())
                configList.add(st.nextToken());
        }
        
        // load the libs        
        if (configList != null) {
            Iterator configIter = configList.iterator();
            while (configIter.hasNext()) {
                int currentPosition;
                boolean looping;
                String configName = (String) configIter.next();
                String configPrefixEnv = props.getProperty(configName + ".prefix.env");
                
                // load the jars
                currentPosition = 1;
                looping = true;
                while (looping) {
                    StringBuffer path = new StringBuffer();                    
                    String suffix = props.getProperty(configName + ".loadjar" + currentPosition);                    
                    if (configPrefixEnv != null && configPrefixEnv.length() > 0) {
                        String prefixPath = System.getProperty(configPrefixEnv);
                        if (prefixPath != null && prefixPath.length() > 0) {
                            path.append(prefixPath);                            
                        }
                    }
                    if (suffix != null && suffix.length() > 0) {
                        path.append(suffix);
                        jarList.add(path.toString());
                        currentPosition++;
                    } else {
                        looping = false;
                    }                                  
                }
                
                // load the dirs
                currentPosition = 1;
                looping = true;
                while (looping) {
                    StringBuffer path = new StringBuffer();                    
                    String suffix = props.getProperty(configName + ".loaddir" + currentPosition);                    
                    if (configPrefixEnv != null && configPrefixEnv.length() > 0) {
                        String prefixPath = System.getProperty(configPrefixEnv);
                        if (prefixPath != null && prefixPath.length() > 0) {
                            path.append(prefixPath);                            
                        }
                    }
                    if (suffix != null && suffix.length() > 0) {
                        path.append(suffix);
                        dirList.add(path.toString());
                        currentPosition++;
                    } else {
                        looping = false;
                    }                                  
                }                
            }
        }        
        
        propsStream.close();            
    }               
}
