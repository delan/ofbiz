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
package org.ofbiz.webtools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;

import javolution.util.FastList;
import javolution.util.FastMap;
import javolution.util.FastSet;

import org.ofbiz.base.location.FlexibleLocation;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.GeneralException;
import org.ofbiz.base.util.StringUtil;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilHttp;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilPlist;
import org.ofbiz.base.util.UtilProperties;
import org.ofbiz.base.util.UtilURL;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.base.util.UtilProperties.UtilResourceBundle;
import org.ofbiz.entity.GenericDelegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.model.ModelEntity;
import org.ofbiz.entity.model.ModelField;
import org.ofbiz.entity.model.ModelFieldType;
import org.ofbiz.entity.model.ModelIndex;
import org.ofbiz.entity.model.ModelKeyMap;
import org.ofbiz.entity.model.ModelReader;
import org.ofbiz.entity.model.ModelRelation;
import org.ofbiz.entity.model.ModelUtil;
import org.ofbiz.entity.model.ModelViewEntity;
import org.ofbiz.entity.util.EntityDataAssert;
import org.ofbiz.entity.util.EntityDataLoader;
import org.ofbiz.entity.util.EntityListIterator;
import org.ofbiz.entity.util.EntitySaxReader;
import org.ofbiz.entityext.EntityGroupUtil;
import org.ofbiz.security.Security;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;
import org.ofbiz.webtools.artifactinfo.ArtifactInfoFactory;
import org.ofbiz.webtools.artifactinfo.ServiceArtifactInfo;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import freemarker.ext.beans.BeansWrapper;
import freemarker.ext.dom.NodeModel;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateHashModel;

/**
 * WebTools Services
 */

public class WebToolsServices {

    public static final String module = WebToolsServices.class.getName();

    public static Map<String, Object> entityImport(DispatchContext dctx, Map<String, ? extends Object> context) {
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        LocalDispatcher dispatcher = dctx.getDispatcher();

        List<String> messages = FastList.newInstance();

        String filename = (String)context.get("filename");
        String fmfilename = (String)context.get("fmfilename");
        String fulltext = (String)context.get("fulltext");
        boolean isUrl = (String)context.get("isUrl") != null;
        String mostlyInserts = (String)context.get("mostlyInserts");
        String maintainTimeStamps = (String)context.get("maintainTimeStamps");
        String createDummyFks = (String)context.get("createDummyFks");
        String checkDataOnly = (String) context.get("checkDataOnly");

        Integer txTimeout = (Integer)context.get("txTimeout");

        if (txTimeout == null) {
            txTimeout = Integer.valueOf(7200);
        }
        InputSource ins = null;
        URL url = null;

        // #############################
        // The filename to parse is prepared
        // #############################
        if (filename != null && filename.length() > 0) {
            try {
                url = isUrl?FlexibleLocation.resolveLocation(filename):UtilURL.fromFilename(filename);
                InputStream is = url.openStream();
                ins = new InputSource(is);
            } catch (MalformedURLException mue) {
                return ServiceUtil.returnError("ERROR: invalid file name (" + filename + "): " + mue.getMessage());
            } catch (IOException ioe) {
                return ServiceUtil.returnError("ERROR reading file name (" + filename + "): " + ioe.getMessage());
            } catch (Exception exc) {
                return ServiceUtil.returnError("ERROR: reading file name (" + filename + "): " + exc.getMessage());
            }
        }

        // #############################
        // The text to parse is prepared
        // #############################
        if (fulltext != null && fulltext.length() > 0) {
            StringReader sr = new StringReader(fulltext);
            ins = new InputSource(sr);
        }

        // #############################
        // FM Template
        // #############################
        String s = null;
        if (UtilValidate.isNotEmpty(fmfilename) && ins != null) {
            FileReader templateReader = null;
            try {
                templateReader = new FileReader(fmfilename);
            } catch (FileNotFoundException e) {
                return ServiceUtil.returnError("ERROR reading template file (" + fmfilename + "): " + e.getMessage());
            }

            StringWriter outWriter = new StringWriter();

            Template template = null;
            try {
                Configuration conf = org.ofbiz.base.util.template.FreeMarkerWorker.getDefaultOfbizConfig();
                template = new Template("FMImportFilter", templateReader, conf);
                Map<String, Object> fmcontext = FastMap.newInstance();

                NodeModel nodeModel = NodeModel.parse(ins);
                fmcontext.put("doc", nodeModel);
                BeansWrapper wrapper = BeansWrapper.getDefaultInstance();
                TemplateHashModel staticModels = wrapper.getStaticModels();
                fmcontext.put("Static", staticModels);

                template.process(fmcontext, outWriter);
                s = outWriter.toString();
            } catch (Exception ex) {
                return ServiceUtil.returnError("ERROR processing template file (" + fmfilename + "): " + ex.getMessage());
            }
        }

        // #############################
        // The parsing takes place
        // #############################
        if (s != null || fulltext != null || url != null) {
            try {
                Map<String, Object> inputMap = UtilMisc.toMap("mostlyInserts", mostlyInserts,
                                              "createDummyFks", createDummyFks,
                                              "checkDataOnly", checkDataOnly,
                                              "maintainTimeStamps", maintainTimeStamps,
                                              "txTimeout", txTimeout,
                                              "userLogin", userLogin);
                if (s != null) {
                    inputMap.put("xmltext", s);
                } else {
                    if (fulltext != null) {
                        inputMap.put("xmltext", fulltext);
                    } else {
                        inputMap.put("url", url);
                    }
                }
                Map<String, Object> outputMap = dispatcher.runSync("parseEntityXmlFile", inputMap);
                if (ServiceUtil.isError(outputMap)) {
                    return ServiceUtil.returnError("ERROR: " + ServiceUtil.getErrorMessage(outputMap));
                } else {
                    Long numberRead = (Long)outputMap.get("rowProcessed");
                    messages.add("Got " + numberRead.longValue() + " entities to write to the datasource.");
                }
            } catch (Exception ex) {
                return ServiceUtil.returnError("ERROR parsing Entity Xml file: " + ex.getMessage());
            }
        } else {
            messages.add("No filename/URL or complete XML document specified, doing nothing.");
        }

        // send the notification
        Map<String, Object> resp = UtilMisc.toMap("messages", (Object) messages);
        return resp;
    }

    public static Map<String, Object> entityImportDir(DispatchContext dctx, Map<String, ? extends Object> context) {
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        LocalDispatcher dispatcher = dctx.getDispatcher();

        List<String> messages = FastList.newInstance();

        String path = (String) context.get("path");
        String mostlyInserts = (String) context.get("mostlyInserts");
        String maintainTimeStamps = (String) context.get("maintainTimeStamps");
        String createDummyFks = (String) context.get("createDummyFks");
        boolean deleteFiles = (String) context.get("deleteFiles") != null;
        String checkDataOnly = (String) context.get("checkDataOnly");

        Integer txTimeout = (Integer)context.get("txTimeout");
        Long filePause = (Long)context.get("filePause");

        if (txTimeout == null) {
            txTimeout = Integer.valueOf(7200);
        }
        if (filePause == null) {
            filePause = Long.valueOf(0);
        }

        if (path != null && path.length() > 0) {
            long pauseLong = filePause != null ? filePause.longValue() : 0;
            File baseDir = new File(path);

            if (baseDir.isDirectory() && baseDir.canRead()) {
                File[] fileArray = baseDir.listFiles();
                FastList<File> files = FastList.newInstance();
                for (File file: fileArray) {
                    if (file.getName().toUpperCase().endsWith("XML")) {
                        files.add(file);
                    }
                }

                int passes=0;
                int initialListSize = files.size();
                int lastUnprocessedFilesCount = 0;
                FastList<File> unprocessedFiles = FastList.newInstance();
                while (files.size()>0 &&
                        files.size() != lastUnprocessedFilesCount) {
                    lastUnprocessedFilesCount = files.size();
                    unprocessedFiles = FastList.newInstance();
                    for (File f: files) {
                        Map<String, Object> parseEntityXmlFileArgs = UtilMisc.toMap("mostlyInserts", mostlyInserts,
                                "createDummyFks", createDummyFks,
                                "checkDataOnly", checkDataOnly,
                                "maintainTimeStamps", maintainTimeStamps,
                                "txTimeout", txTimeout,
                                "userLogin", userLogin);
 
                        try {
                            URL furl = f.toURI().toURL();
                            parseEntityXmlFileArgs.put("url", furl);
                            Map<String, Object> outputMap = dispatcher.runSync("parseEntityXmlFile", parseEntityXmlFileArgs);
                            Long numberRead = (Long) outputMap.get("rowProcessed");
                            messages.add("Got " + numberRead.longValue() + " entities from " + f);
                            if (deleteFiles) {
                                messages.add("Deleting " + f);
                                f.delete();
                            }
                        } catch (Exception e) {
                            unprocessedFiles.add(f);
                            messages.add("Failed " + f + " adding to retry list for next pass");
                        }
                        // pause in between files
                        if (pauseLong > 0) {
                            Debug.log("Pausing for [" + pauseLong + "] seconds - " + UtilDateTime.nowTimestamp());
                            try {
                                Thread.sleep((pauseLong * 1000));
                            } catch (InterruptedException ie) {
                                Debug.log("Pause finished - " + UtilDateTime.nowTimestamp());
                            }
                        }
                    }
                    files = unprocessedFiles;
                    passes++;
                    messages.add("Pass " + passes + " complete");
                    Debug.logInfo("Pass " + passes + " complete", module);
                }
                lastUnprocessedFilesCount=unprocessedFiles.size();
                messages.add("---------------------------------------");
                messages.add("Succeeded: " + (initialListSize-lastUnprocessedFilesCount) + " of " + initialListSize);
                messages.add("Failed:    " + lastUnprocessedFilesCount + " of " + initialListSize);
                messages.add("---------------------------------------");
                messages.add("Failed Files:");
                for (File file: unprocessedFiles) {
                    messages.add(file.toString());
                }
            } else {
                messages.add("path not found or can't be read");
            }
        } else {
            messages.add("No path specified, doing nothing.");
        }
        // send the notification
        Map<String, Object> resp = UtilMisc.toMap("messages", (Object) messages);
        return resp;
    }

    public static Map<String, Object> entityImportReaders(DispatchContext dctx, Map<String, Object> context) {
        String readers = (String) context.get("readers");
        String overrideDelegator = (String) context.get("overrideDelegator");
        String overrideGroup = (String) context.get("overrideGroup");
        boolean useDummyFks = "true".equals((String) context.get("createDummyFks"));
        boolean maintainTxs = "true".equals((String) context.get("maintainTimeStamps"));
        boolean tryInserts = "true".equals((String) context.get("mostlyInserts"));
        boolean checkDataOnly = "true".equals((String) context.get("checkDataOnly"));
 
        Integer txTimeoutInt = (Integer) context.get("txTimeout");
        int txTimeout = txTimeoutInt != null ? txTimeoutInt.intValue() : -1;

        List<Object> messages = FastList.newInstance();

        // parse the pass in list of readers to use
        List<String> readerNames = null;
        if (UtilValidate.isNotEmpty(readers) && !"none".equalsIgnoreCase(readers)) {
            if (readers.indexOf(",") == -1) {
                readerNames = FastList.newInstance();
                readerNames.add(readers);
            } else {
                readerNames = StringUtil.split(readers, ",");
            }
        }

        String groupNameToUse = overrideGroup != null ? overrideGroup : "org.ofbiz";
        GenericDelegator delegator = UtilValidate.isNotEmpty(overrideDelegator) ? GenericDelegator.getGenericDelegator(overrideDelegator) : dctx.getDelegator();

        String helperName = delegator.getGroupHelperName(groupNameToUse);
        if (helperName == null) {
            return ServiceUtil.returnError("Unable to locate the datasource helper for the group [" + groupNameToUse + "]");
        }

        // get the reader name URLs first
        List<URL> urlList = null;
        if (readerNames != null) {
            urlList = EntityDataLoader.getUrlList(helperName, readerNames);
        } else if (!"none".equalsIgnoreCase(readers)) {
            urlList = EntityDataLoader.getUrlList(helperName);
        }

        // need a list if it is empty
        if (urlList == null) {
            urlList = FastList.newInstance();
        }

        // process the list of files
        NumberFormat changedFormat = NumberFormat.getIntegerInstance();
        changedFormat.setMinimumIntegerDigits(5);
        changedFormat.setGroupingUsed(false);
 
        List<Object> errorMessages = FastList.newInstance();
        List<String> infoMessages = FastList.newInstance();
        int totalRowsChanged = 0;
        if (UtilValidate.isNotEmpty(urlList)) {
            messages.add("=-=-=-=-=-=-= Doing a data " + (checkDataOnly ? "check" : "load") + " with the following files:");
            for (URL dataUrl: urlList) {
                messages.add(dataUrl.toExternalForm());
            }

            messages.add("=-=-=-=-=-=-= Starting the data " + (checkDataOnly ? "check" : "load") + "...");

            for (URL dataUrl: urlList) {
                try {
                    int rowsChanged = 0;
                    if (checkDataOnly) {
                        try {
                            errorMessages.add("Checking data in [" + dataUrl.toExternalForm() + "]");
                            rowsChanged = EntityDataAssert.assertData(dataUrl, delegator, errorMessages);
                        } catch (SAXException e) {
                            errorMessages.add("Error checking data in [" + dataUrl.toExternalForm() + "]: " + e.toString());
                        } catch (ParserConfigurationException e) {
                            errorMessages.add("Error checking data in [" + dataUrl.toExternalForm() + "]: " + e.toString());
                        } catch (IOException e) {
                            errorMessages.add("Error checking data in [" + dataUrl.toExternalForm() + "]: " + e.toString());
                        }
                    } else {
                        rowsChanged = EntityDataLoader.loadData(dataUrl, helperName, delegator, errorMessages, txTimeout, useDummyFks, maintainTxs, tryInserts);
                    }
                    totalRowsChanged += rowsChanged;
                    infoMessages.add(changedFormat.format(rowsChanged) + " of " + changedFormat.format(totalRowsChanged) + " from " + dataUrl.toExternalForm());
                } catch (GenericEntityException e) {
                    Debug.logError(e, "Error loading data file: " + dataUrl.toExternalForm(), module);
                }
            }
        } else {
            messages.add("=-=-=-=-=-=-= No data " + (checkDataOnly ? "check" : "load") + " files found.");
        }

        if (infoMessages.size() > 0) {
            messages.add("=-=-=-=-=-=-= Here is a summary of the data " + (checkDataOnly ? "check" : "load") + ":");
            messages.addAll(infoMessages);
        }
 
        if (errorMessages.size() > 0) {
            messages.add("=-=-=-=-=-=-= The following errors occured in the data " + (checkDataOnly ? "check" : "load") + ":");
            messages.addAll(errorMessages);
        }

        messages.add("=-=-=-=-=-=-= Finished the data " + (checkDataOnly ? "check" : "load") + " with " + totalRowsChanged + " rows " + (checkDataOnly ? "checked" : "changed") + ".");
 
        Map<String, Object> resultMap = ServiceUtil.returnSuccess();
        resultMap.put("messages", messages);
        return resultMap;
    }
    
    public static Map<String, Object> parseEntityXmlFile(DispatchContext dctx, Map<String, ? extends Object> context) {
        GenericDelegator delegator = dctx.getDelegator();

        URL url = (URL) context.get("url");
        String xmltext = (String) context.get("xmltext");

        if (url == null && xmltext == null) {
            return ServiceUtil.returnError("No entity xml file or text specified");
        }
        boolean mostlyInserts = (String) context.get("mostlyInserts") != null;
        boolean maintainTimeStamps = (String) context.get("maintainTimeStamps") != null;
        boolean createDummyFks = (String) context.get("createDummyFks") != null;
        boolean checkDataOnly = (String) context.get("checkDataOnly") != null;
        Integer txTimeout = (Integer) context.get("txTimeout");

        if (txTimeout == null) {
            txTimeout = Integer.valueOf(7200);
        }

        long rowProcessed = 0;
        try {
            EntitySaxReader reader = new EntitySaxReader(delegator);
            reader.setUseTryInsertMethod(mostlyInserts);
            reader.setMaintainTxStamps(maintainTimeStamps);
            reader.setTransactionTimeout(txTimeout.intValue());
            reader.setCreateDummyFks(createDummyFks);
            reader.setCheckDataOnly(checkDataOnly);

            long numberRead = (url != null ? reader.parse(url) : reader.parse(xmltext));
            rowProcessed = numberRead;
        } catch (Exception ex) {
            return ServiceUtil.returnError("Error parsing entity xml file: " + ex.toString());
        }
        // send the notification
        Map<String, Object> resp = UtilMisc.<String, Object>toMap("rowProcessed", rowProcessed);
        return resp;
    }

    public static Map<String, Object> entityExportAll(DispatchContext dctx, Map<String, ? extends Object> context) {
        GenericDelegator delegator = dctx.getDelegator();

        String outpath = (String)context.get("outpath"); // mandatory
        Integer txTimeout = (Integer)context.get("txTimeout");
        if (txTimeout == null) {
            txTimeout = Integer.valueOf(7200);
        }

        List<String> results = FastList.newInstance();

        if (outpath != null && outpath.length() > 0) {
            File outdir = new File(outpath);
            if (!outdir.exists()) {
                outdir.mkdir();
            }
            if (outdir.isDirectory() && outdir.canWrite()) {
                Set<String> passedEntityNames;
                try {
                    ModelReader reader = delegator.getModelReader();
                    Collection<String> ec = reader.getEntityNames();
                    passedEntityNames = new TreeSet<String>(ec);
                } catch (Exception exc) {
                    return ServiceUtil.returnError("Error retrieving entity names.");
                }
                int fileNumber = 1;

                for (String curEntityName: passedEntityNames) {
                    long numberWritten = 0;
                    EntityListIterator values = null;

                    try {
                        ModelEntity me = delegator.getModelEntity(curEntityName);
                        if (me instanceof ModelViewEntity) {
                            results.add("["+fileNumber +"] [vvv] " + curEntityName + " skipping view entity");
                            continue;
                        }

                        // some databases don't support cursors, or other problems may happen, so if there is an error here log it and move on to get as much as possible
                        try {
                            values = delegator.find(curEntityName, null, null, null, me.getPkFieldNames(), null);
                        } catch (Exception entityEx) {
                            results.add("["+fileNumber +"] [xxx] Error when writing " + curEntityName + ": " + entityEx);
                            continue;
                        }

                        //Don't bother writing the file if there's nothing
                        //to put into it
                        GenericValue value = (GenericValue) values.next();
                        if (value != null) {
                            PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(outdir, curEntityName +".xml")), "UTF-8")));
                            writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
                            writer.println("<entity-engine-xml>");

                            do {
                                value.writeXmlText(writer, "");
                                numberWritten++;
                            } while ((value = (GenericValue) values.next()) != null);
                            writer.println("</entity-engine-xml>");
                            writer.close();
                            results.add("["+fileNumber +"] [" + numberWritten + "] " + curEntityName + " wrote " + numberWritten + " records");
                        } else {
                            results.add("["+fileNumber +"] [---] " + curEntityName + " has no records, not writing file");
                        }
                        values.close();
                    } catch (Exception ex) {
                        if (values != null) {
                            try {
                                values.close();
                            } catch (Exception exc) {
                                //Debug.warning();
                            }
                        }
                        results.add("["+fileNumber +"] [xxx] Error when writing " + curEntityName + ": " + ex);
                    }
                    fileNumber++;
                }
            } else {
                results.add("Path not found or no write access.");
            }
        } else {
            results.add("No path specified, doing nothing.");
        }
        // send the notification
        Map<String, Object> resp = UtilMisc.<String, Object>toMap("results", results);
        return resp;
    }
 
    /** Get entity reference data. Returns the number of entities in
     * <code>numberOfEntities</code> and a List of Maps -
     * <code>packagesList</code>.<br/> Each Map contains:<br/>
     * <ul><li><code>packageName</code> - the entity package name</li>
     * <li><code>entitiesList</code> - a list of Maps:
       <ul>
         <li><code>entityName</code></li>
         <li><code>helperName</code></li>
         <li><code>groupName</code></li>
         <li><code>plainTableName</code></li>
         <li><code>title</code></li>
         <li><code>description</code></li>
         <!-- <li><code>location</code></li> -->
         <li><code>javaNameList</code> - list of Maps:
           <ul>
             <li><code>isPk</code></li>
             <li><code>name</code></li>
             <li><code>colName</code></li>
             <li><code>description</code></li>
             <li><code>type</code></li>
             <li><code>javaType</code></li>
             <li><code>sqlType</code></li>
           </ul>
         </li>
         <li><code>relationsList</code> - list of Maps:
           <ul>
             <li><code>title</code></li>
             <!-- <li><code>description</code></li> -->
             <li><code>relEntity</code></li>
             <li><code>fkName</code></li>
             <li><code>type</code></li>
             <li><code>length</code></li>
             <li><code>keysList</code> - list of Maps:
               <ul>
                 <li><code>row</code></li>
                 <li><code>fieldName</code></li>
                 <li><code>relFieldName</code></li>
               </ul>
             </li>
           </ul>
         </li>
         <li><code>indexList</code> - list of Maps:
           <ul>
             <li><code>name</code></li>
             <!-- <li><code>description</code></li> -->
             <li><code>fieldNameList</code> - list of Strings</li>
           </ul>
         </li>
       </ul>
       </li></ul>
     * */
    public static Map<String, Object> getEntityRefData(DispatchContext dctx, Map<String, ? extends Object> context) {
        GenericDelegator delegator = dctx.getDelegator();
        Locale locale = (Locale) context.get("locale");
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        Map<String, Object> resultMap = ServiceUtil.returnSuccess();
 
        ModelReader reader = delegator.getModelReader();
        Map<String, TreeSet<String>> entitiesByPackage = FastMap.newInstance();
        TreeSet<String> packageNames = new TreeSet<String>();
        TreeSet<String> tableNames = new TreeSet<String>();
 
        //put the entityNames TreeSets in a HashMap by packageName
        try {
            Collection<String> ec = reader.getEntityNames();
            resultMap.put("numberOfEntities", ec.size());
            for (String eName: ec) {
                ModelEntity ent = reader.getModelEntity(eName);
                //make sure the table name is in the list of all table names, if not null
                if (UtilValidate.isNotEmpty(ent.getPlainTableName())) {
                    tableNames.add(ent.getPlainTableName());
                }
                TreeSet<String> entities = entitiesByPackage.get(ent.getPackageName());
                if (entities == null) {
                    entities = new TreeSet<String>();
                    entitiesByPackage.put(ent.getPackageName(), entities);
                    packageNames.add(ent.getPackageName());
                }
                entities.add(eName);
            }
        } catch (GenericEntityException e) {
            return ServiceUtil.returnError("ERROR: getting entity names: " + e.getMessage());
        }
 
        String search = (String) context.get("search");
        List<Map<String, Object>> packagesList = FastList.newInstance();
        Iterator piter = packageNames.iterator();
        try {
            while (piter.hasNext()) {
                Map<String, Object> packageMap = FastMap.newInstance();
                String pName = (String) piter.next();
                TreeSet<String> entities = entitiesByPackage.get(pName);
                List<Map<String, Object>> entitiesList = FastList.newInstance();
                for (String entityName: entities) {
                    Map<String, Object> entityMap = FastMap.newInstance();
                    String helperName = delegator.getEntityHelperName(entityName);
                    String groupName = delegator.getEntityGroupName(entityName);
                    if (search == null || entityName.toLowerCase().indexOf(search.toLowerCase()) != -1) {
                        ModelEntity entity = reader.getModelEntity(entityName);
                        ResourceBundle bundle = null;
                        if (UtilValidate.isNotEmpty(entity.getDefaultResourceName())) {
                            try {
                                bundle = UtilResourceBundle.getBundle(entity.getDefaultResourceName(), locale, loader);
                            } catch (Exception exception) {
                                Debug.logInfo(exception.getMessage(), module);
                            }
                        }

                        // fields list
                        List<Map<String, Object>> javaNameList = FastList.newInstance();
                        for (Iterator<ModelField> f = entity.getFieldsIterator(); f.hasNext();) {
                            Map<String, Object> javaNameMap = FastMap.newInstance();
                            ModelField field = f.next();
                            ModelFieldType type = delegator.getEntityFieldType(entity, field.getType());
                            javaNameMap.put("isPk", field.getIsPk());
                            javaNameMap.put("name", field.getName());
                            javaNameMap.put("colName", field.getColName());
                            String fieldDescription = null;
                            if (bundle != null) {
                                try {
                                    fieldDescription = bundle.getString("FieldDescription." + entity.getEntityName() + "." + field.getName());
                                } catch (Exception exception) {}
                            }
                            if (UtilValidate.isEmpty(fieldDescription)) {
                                fieldDescription = field.getDescription();
                            }
                            if (UtilValidate.isEmpty(fieldDescription) && bundle != null) {
                                try {
                                fieldDescription = bundle.getString("FieldDescription." + field.getName());
                                } catch (Exception exception) {}
                            }
                            if (UtilValidate.isEmpty(fieldDescription)) {
                                fieldDescription = ModelUtil.javaNameToDbName(field.getName()).toLowerCase();
                                fieldDescription = ModelUtil.upperFirstChar(fieldDescription.replace('_', ' '));
                            }
                            javaNameMap.put("description", fieldDescription);
                            javaNameMap.put("type", (field.getType()) != null ? field.getType() : null);
                            javaNameMap.put("javaType", (field.getType() != null && type != null) ? type.getJavaType() : "Undefined");
                            javaNameMap.put("sqlType", (type != null && type.getSqlType() != null) ? type.getSqlType() : "Undefined");
                            javaNameMap.put("encrypted", field.getEncrypt());
                            javaNameList.add(javaNameMap);
                        }

                        // relations list
                        List<Map<String, Object>> relationsList = FastList.newInstance();
                        for (int r = 0; r < entity.getRelationsSize(); r++) {
                            Map<String, Object> relationMap = FastMap.newInstance();
                            ModelRelation relation = entity.getRelation(r);
                            List<Map<String, Object>> keysList = FastList.newInstance();
                            for (int km = 0; km < relation.getKeyMapsSize(); km++) {
                                Map<String, Object> keysMap = FastMap.newInstance();
                                ModelKeyMap keyMap = relation.getKeyMap(km);
                                String fieldName = null;
                                String relFieldName = null;
                                if (keyMap.getFieldName().equals(keyMap.getRelFieldName())) {
                                    fieldName = keyMap.getFieldName();
                                    relFieldName = "aa";
                                } else {
                                    fieldName = keyMap.getFieldName();
                                    relFieldName = keyMap.getRelFieldName();
                                }
                                keysMap.put("row", km + 1);
                                keysMap.put("fieldName", fieldName);
                                keysMap.put("relFieldName", relFieldName);
                                keysList.add(keysMap);
                            }
                            relationMap.put("title", relation.getTitle());
                            relationMap.put("description", relation.getDescription());
                            relationMap.put("relEntity", relation.getRelEntityName());
                            relationMap.put("fkName", relation.getFkName());
                            relationMap.put("type", relation.getType());
                            relationMap.put("length", relation.getType().length());
                            relationMap.put("keysList", keysList);
                            relationsList.add(relationMap);
                        }

                        // index list
                        List<Map<String, Object>> indexList = FastList.newInstance();
                        for (int r = 0; r < entity.getIndexesSize(); r++) {
                            List<String> fieldNameList = FastList.newInstance();
 
                            ModelIndex index = entity.getIndex(r);
                            for (Iterator<String> fieldIterator = index.getIndexFieldsIterator(); fieldIterator.hasNext();) {
                                fieldNameList.add(fieldIterator.next());
                            }
 
                            Map<String, Object> indexMap = FastMap.newInstance();
                            indexMap.put("name", index.getName());
                            indexMap.put("description", index.getDescription());
                            indexMap.put("fieldNameList", fieldNameList);
                            indexList.add(indexMap);
                        }
 
                        entityMap.put("entityName", entityName);
                        entityMap.put("helperName", helperName);
                        entityMap.put("groupName", groupName);
                        entityMap.put("plainTableName", entity.getPlainTableName());
                        entityMap.put("title", entity.getTitle());
                        entityMap.put("description", entity.getDescription());
                        String entityLocation = entity.getLocation();
                        entityLocation = entityLocation.replaceFirst(System.getProperty("ofbiz.home") + "/", "");
                        entityMap.put("location", entityLocation);
                        entityMap.put("javaNameList", javaNameList);
                        entityMap.put("relationsList", relationsList);
                        entityMap.put("indexList", indexList);
                        entitiesList.add(entityMap);
                    }
                }
                packageMap.put("packageName", pName);
                packageMap.put("entitiesList", entitiesList);
                packagesList.add(packageMap);
            }
        } catch (GenericEntityException e) {
            return ServiceUtil.returnError("ERROR: getting entity info: " + e.getMessage());
        }
 
        resultMap.put("packagesList", packagesList);
        return resultMap;
    }
 
    public static Map<String, Object> exportEntityEoModelBundle(DispatchContext dctx, Map<String, ? extends Object> context) {
        String eomodeldFullPath = (String) context.get("eomodeldFullPath");
        String entityPackageNameOrig = (String) context.get("entityPackageName");
        String entityGroupId = (String) context.get("entityGroupId");
        String datasourceName = (String) context.get("datasourceName");
        String entityNamePrefix = (String) context.get("entityNamePrefix");
 
        if (datasourceName == null) datasourceName = "localderby";
 
        ModelReader reader = dctx.getDelegator().getModelReader();
 
        try {
            if (!eomodeldFullPath.endsWith(".eomodeld")) {
                eomodeldFullPath = eomodeldFullPath + ".eomodeld";
            }
 
            File outdir = new File(eomodeldFullPath);
            if (!outdir.exists()) {
                outdir.mkdir();
            }
            if (!outdir.isDirectory()) {
                return ServiceUtil.returnError("eomodel Full Path is not a directory: " + eomodeldFullPath);
            }
            if (!outdir.canWrite()) {
                return ServiceUtil.returnError("eomodel Full Path is not write-able: " + eomodeldFullPath);
            }
 
            Set<String> entityNames = new TreeSet<String>();
            if (UtilValidate.isNotEmpty(entityPackageNameOrig)) {
                Set<String> entityPackageNameSet = FastSet.newInstance();
                entityPackageNameSet.addAll(StringUtil.split(entityPackageNameOrig, ","));
 
                Debug.logInfo("Exporting with entityPackageNameSet: " + entityPackageNameSet, module);

                Map<String, TreeSet<String>> entitiesByPackage = reader.getEntitiesByPackage(entityPackageNameSet, null);
                for (Map.Entry<String, TreeSet<String>> entitiesByPackageMapEntry: entitiesByPackage.entrySet()) {
                    entityNames.addAll(entitiesByPackageMapEntry.getValue());
                }
            } else if (UtilValidate.isNotEmpty(entityGroupId)) {
                Debug.logInfo("Exporting entites from the Group: " + entityGroupId, module);
                entityNames.addAll(EntityGroupUtil.getEntityNamesByGroup(entityGroupId, dctx.getDelegator(), false));
            } else {
                entityNames.addAll(reader.getEntityNames());
            }
            Debug.logInfo("Exporting the following entities: " + entityNames, module);

            // remove all view-entity
            Iterator<String> filterEntityNameIter = entityNames.iterator();
            while (filterEntityNameIter.hasNext()) {
                String entityName = filterEntityNameIter.next();
                ModelEntity modelEntity = reader.getModelEntity(entityName);
                if (modelEntity instanceof ModelViewEntity) {
                    filterEntityNameIter.remove();
                }
            }
 
            // write the index.eomodeld file
            Map<String, Object> topLevelMap = FastMap.newInstance();
            topLevelMap.put("EOModelVersion", "\"2.1\"");
            List<Map<String, Object>> entitiesMapList = FastList.newInstance();
            topLevelMap.put("entities", entitiesMapList);
            for (String entityName: entityNames) {
                Map<String, Object> entitiesMap = FastMap.newInstance();
                entitiesMapList.add(entitiesMap);
                entitiesMap.put("className", "EOGenericRecord");
                entitiesMap.put("name", entityName);
            }
            UtilPlist.writePlistFile(topLevelMap, eomodeldFullPath, "index.eomodeld", true);
 
            // write each <EntityName>.plist file
            for (String curEntityName: entityNames) {
                ModelEntity modelEntity = reader.getModelEntity(curEntityName);
                UtilPlist.writePlistFile(modelEntity.createEoModelMap(entityNamePrefix, datasourceName, entityNames, reader), eomodeldFullPath, curEntityName +".plist", true);
            }
 
            return ServiceUtil.returnSuccess("Exported eomodeld file for " + entityNames.size() + " entities to: " + eomodeldFullPath);
        } catch (UnsupportedEncodingException e) {
            return ServiceUtil.returnError("ERROR saving file: " + e.toString());
        } catch (FileNotFoundException e) {
            return ServiceUtil.returnError("ERROR: file/directory not found: " + e.toString());
        } catch (GenericEntityException e) {
            return ServiceUtil.returnError("ERROR: getting entity names: " + e.toString());
        }
    }

    /** Performs an entity maintenance security check. Returns hasPermission=true
     * if the user has the ENTITY_MAINT permission.
     * @param dctx
     * @param context
     * @return
     */
    public static Map<String, Object> entityMaintPermCheck(DispatchContext dctx, Map<String, ? extends Object> context) {
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        Locale locale = (Locale) context.get("locale");
        Security security = dctx.getSecurity();
        Map<String, Object> resultMap = null;
        if (security.hasPermission("ENTITY_MAINT", userLogin)) {
            resultMap = ServiceUtil.returnSuccess();
            resultMap.put("hasPermission", true);
        } else {
            resultMap = ServiceUtil.returnFailure(UtilProperties.getMessage("WebtoolsUiLabels", "WebtoolsPermissionError", locale));
            resultMap.put("hasPermission", false);
        }
        return resultMap;
    }
 
 
    public static Map<String, Object> exportServiceEoModelBundle(DispatchContext dctx, Map<String, ? extends Object> context) {
        String eomodeldFullPath = (String) context.get("eomodeldFullPath");
        String serviceName = (String) context.get("serviceName");
 
        if (eomodeldFullPath.endsWith("/")) {
            eomodeldFullPath = eomodeldFullPath + serviceName + ".eomodeld";
        }
 
        if (!eomodeldFullPath.endsWith(".eomodeld")) {
            eomodeldFullPath = eomodeldFullPath + ".eomodeld";
        }
 
        File outdir = new File(eomodeldFullPath);
        if (!outdir.exists()) {
            outdir.mkdir();
        }
        if (!outdir.isDirectory()) {
            return ServiceUtil.returnError("eomodel Full Path is not a directory: " + eomodeldFullPath);
        }
        if (!outdir.canWrite()) {
            return ServiceUtil.returnError("eomodel Full Path is not write-able: " + eomodeldFullPath);
        }
 
        try {
            ArtifactInfoFactory aif = ArtifactInfoFactory.getArtifactInfoFactory("default");
            ServiceArtifactInfo serviceInfo = aif.getServiceArtifactInfo(serviceName);
            serviceInfo.writeServiceCallGraphEoModel(eomodeldFullPath);
        } catch (GeneralException e) {
            Debug.logError(e, module);
            return ServiceUtil.returnError("Error getting service info: " + e.toString());
        } catch (UnsupportedEncodingException e) {
            return ServiceUtil.returnError("ERROR saving file: " + e.toString());
        } catch (FileNotFoundException e) {
            return ServiceUtil.returnError("ERROR: file/directory not found: " + e.toString());
        }
 
        return ServiceUtil.returnSuccess();
    }
}
