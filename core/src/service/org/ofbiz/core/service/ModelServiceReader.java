/*                                                                      Debug
 * $Id$
 *
 * Copyright (c) 2001 The Open For Business Project - www.ofbiz.org
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

package org.ofbiz.core.service;

import java.io.*;
import java.util.*;
import java.net.*;
import javax.xml.parsers.*;

import org.xml.sax.*;
import org.w3c.dom.*;
import org.ofbiz.core.config.*;
import org.ofbiz.core.util.*;

/**
 * Generic Service - Service Definition Reader
 *
 *@author     <a href="mailto:jaz@zsolv.com">Andy Zeneski</a>
 *@author     <a href="mailto:jonesde@ofbiz.org">David E. Jones</a>
 *@created    October 20, 2001
 *@version    1.0
 */

public class ModelServiceReader {

    public static final String module = ModelServiceReader.class.getName();

    protected static UtilCache readersUrl = new UtilCache("service.ModelServiceReader.ByURL", 0, 0);
    protected static UtilCache readersLoader = new UtilCache("service.ModelServiceReader.ByResourceLoader", 0, 0);

    /** is either from a URL or from a ResourceLoader (through the ResourceHandler) */
    protected boolean isFromURL;
    protected URL readerURL = null;
    protected ResourceHandler handler = null;
    protected Map modelServices = null;

    public static ModelServiceReader getModelServiceReader(URL readerURL) {
        ModelServiceReader reader = null;
        //if ( readersUrl.containsKey(readerURL) ) <-- this is unnecessary as it will return null below if not found
        reader = (ModelServiceReader) readersUrl.get(readerURL);
        if (reader == null) { //don't want to block here
            synchronized (ModelServiceReader.class) {
                //must check if null again as one of the blocked threads can still enter
                reader = (ModelServiceReader) readersUrl.get(readerURL);
                if (reader == null) {
                    //if (Debug.infoOn()) Debug.logInfo("[Creating reader]: " + readerURL.toExternalForm(), module);
                    reader = new ModelServiceReader(readerURL);
                    readersUrl.put(readerURL, reader);
                }
            }
        }
        return reader;
    }

    public static ModelServiceReader getModelServiceReader(ResourceHandler handler) {
        ModelServiceReader reader = null;
        reader = (ModelServiceReader) readersLoader.get(handler);
        if (reader == null) { //don't want to block here
            synchronized (ModelServiceReader.class) {
                //must check if null again as one of the blocked threads can still enter
                reader = (ModelServiceReader) readersLoader.get(handler);
                if (reader == null) {
                    //if (Debug.infoOn()) Debug.logInfo("[Creating reader]: " + handler, module);
                    reader = new ModelServiceReader(handler);
                    readersLoader.put(handler, reader);
                }
            }
        }
        return reader;
    }

    protected ModelServiceReader(URL readerURL) {
        this.isFromURL = true;
        this.readerURL = readerURL;
        this.handler = null;
        //preload models...
        getModelServices();
    }

    protected ModelServiceReader(ResourceHandler handler) {
        this.isFromURL = false;
        this.readerURL = null;
        this.handler = handler;
        //preload models...
        getModelServices();
    }

    public Map getModelServices() {
        if (modelServices == null) { //don't want to block here
            synchronized (ModelServiceReader.class) {
                //must check if null again as one of the blocked threads can still enter
                if (modelServices == null) { //now it's safe
                    modelServices = new HashMap();

                    UtilTimer utilTimer = new UtilTimer();

                    Document document = null;
                    if (this.isFromURL) {
                        //utilTimer.timerString("Before getDocument in file " + readerURL);
                        document = getDocument(readerURL);
                        
                        if (document == null) {
                            modelServices = null;
                            return null;
                        }
                    } else {
                        //utilTimer.timerString("Before getDocument in " + handler);
                        try {
                            document = handler.getDocument();
                        } catch (GenericConfigException e) {
                            Debug.logError(e, "Error getting XML document from resource");
                            return null;
                        }
                    }
                    
                    if (this.isFromURL) {
                        //utilTimer.timerString("Before getDocumentElement in file " + readerURL);
                    } else {
                        //utilTimer.timerString("Before getDocumentElement in " + handler);
                    }
                    Element docElement = document.getDocumentElement();
                    if (docElement == null) {
                        modelServices = null;
                        return null;
                    }
                    docElement.normalize();
                    Node curChild = docElement.getFirstChild();

                    int i = 0;
                    if (curChild != null) {
                        if (this.isFromURL) {
                            utilTimer.timerString("Before start of service loop in file " + readerURL);
                        } else {
                            utilTimer.timerString("Before start of service loop in " + handler);
                        }
                        int servicesLoaded = 0;
                        do {
                            if (curChild.getNodeType() == Node.ELEMENT_NODE && "service".equals(curChild.getNodeName())) {
                                i++;
                                Element curService = (Element) curChild;
                                String serviceName = UtilXml.checkEmpty(curService.getAttribute("name"));

                                //check to see if service with same name has already been read
                                if (modelServices.containsKey(serviceName)) {
                                    Debug.logWarning("WARNING: Service " + serviceName + " is defined more than once, " +
                                                     "most recent will over-write previous definition(s)", module);
                                }

                                //utilTimer.timerString("  After serviceName -- " + i + " --");
                                ModelService service = createModelService(curService);
                                //utilTimer.timerString("  After createModelService -- " + i + " --");
                                if (service != null) {
                                    modelServices.put(serviceName, service);
                                    //utilTimer.timerString("  After modelServices.put -- " + i + " --");
                                    int reqIn = service.getParameterNames(ModelService.IN_PARAM, false).size();
                                    int optIn = service.getParameterNames(ModelService.IN_PARAM, true).size() - reqIn;
                                    int reqOut = service.getParameterNames(ModelService.OUT_PARAM, false).size();
                                    int optOut = service.getParameterNames(ModelService.OUT_PARAM, true).size() - reqOut;
                                    if (Debug.verboseOn()) {
				        String msg = "-- getModelService: # " + i + " Loaded service: " + serviceName +
                                                " (IN) " + reqIn + "/" + optIn + " (OUT) " + reqOut + "/" + optOut;
                                        Debug.logVerbose(msg, module);
				    }
                                } else {
                                    Debug.logWarning(
                                            "-- -- SERVICE ERROR:getModelService: Could not create service for serviceName: " +
                                            serviceName, module);
                                }

                            }
                        } while ((curChild = curChild.getNextSibling()) != null);
                    } else {
                        Debug.logWarning("No child nodes found.", module);
                    }
                    if (this.isFromURL) {
                        utilTimer.timerString("Finished file " + readerURL + " - Total Services: " + i + " FINISHED");
                        Debug.logImportant("Loaded " + i + " Service definitions from " + readerURL);
                    } else {
                        utilTimer.timerString("Finished document in " + handler + " - Total Services: " + i + " FINISHED");
                        Debug.logImportant("Loaded " + i + " Service definitions from " + handler.getLocation() + " in loader " + handler.getLoaderName());
                    }
                }
            }
        }
        return modelServices;
    }

    /** Gets an Service object based on a definition from the specified XML Service descriptor file.
     * @param serviceName The serviceName of the Service definition to use.
     * @return An Service object describing the specified service of the specified descriptor file.
     */
    public ModelService getModelService(String serviceName) {
        Map ec = getModelServices();
        if (ec != null)
            return (ModelService) ec.get(serviceName);
        else
            return null;
    }

    /** Creates a Iterator with the serviceName of each Service defined in the specified XML Service Descriptor file.
     * @return A Iterator of serviceName Strings
     */
    public Iterator getServiceNamesIterator() {
        Collection collection = getServiceNames();
        if (collection != null) {
            return collection.iterator();
        } else {
            return null;
        }
    }

    /** Creates a Collection with the serviceName of each Service defined in the specified XML Service Descriptor file.
     * @return A Collection of serviceName Strings
     */
    public Collection getServiceNames() {
        Map ec = getModelServices();
        return ec.keySet();
    }

    protected ModelService createModelService(Element serviceElement) {
        ModelService service = new ModelService();

        service.name = UtilXml.checkEmpty(serviceElement.getAttribute("name"));
        service.engineName = UtilXml.checkEmpty(serviceElement.getAttribute("engine"));
        service.location = UtilXml.checkEmpty(serviceElement.getAttribute("location"));
        service.invoke = UtilXml.checkEmpty(serviceElement.getAttribute("invoke"));
        service.auth = "true".equalsIgnoreCase(serviceElement.getAttribute("auth"));
        service.export = "true".equalsIgnoreCase(serviceElement.getAttribute("export"));
        //this defaults to true, so if anything but false, make it true
        service.validate = !"false".equalsIgnoreCase(serviceElement.getAttribute("validate"));
        service.description = getCDATADef(serviceElement, "description");
        service.nameSpace = getCDATADef(serviceElement, "namespace");
        service.contextInfo = new HashMap();

        createAttrDefs(serviceElement, service);
        return service;
    }

    protected String getCDATADef(Element baseElement, String tagName) {
        String value = "";
        NodeList nl = baseElement.getElementsByTagName(tagName);
        // if there are more then one decriptions we will use only the first one
        if (nl.getLength() > 0) {
            Node n = nl.item(0);
            NodeList childNodes = n.getChildNodes();
            if (childNodes.getLength() > 0) {
                Node cdata = childNodes.item(0);
                value = UtilXml.checkEmpty(cdata.getNodeValue());
            }
        }
        return value;
    }

    protected void createAttrDefs(Element baseElement, ModelService service) {
        // Add in the defined attributes (override the above defaults if specified)
        List paramElements = UtilXml.childElementList(baseElement, "attribute");
        Iterator paramIter = paramElements.iterator();
        while (paramIter.hasNext()) {
            Element attribute = (Element) paramIter.next();
            ModelParam param = new ModelParam();
            param.name = UtilXml.checkEmpty(attribute.getAttribute("name"));
            param.type = UtilXml.checkEmpty(attribute.getAttribute("type"));
            param.mode = UtilXml.checkEmpty(attribute.getAttribute("mode"));
            // defaults to false, if anything but true will be false
            param.optional = "true".equalsIgnoreCase(attribute.getAttribute("optional"));
            service.addParam(param);
        }

        // Add the default optional parameters
        ModelParam def = null;
        // responseMessage
        def = new ModelParam();
        def.name = ModelService.RESPONSE_MESSAGE;
        def.type = "String";
        def.mode = "OUT";
        def.optional = true;
        service.addParam(def);
        // errorMessage
        def = new ModelParam();
        def.name = ModelService.ERROR_MESSAGE;
        def.type = "String";
        def.mode = "OUT";
        def.optional = true;
        service.addParam(def);
        // errorMessageList
        def = new ModelParam();
        def.name = ModelService.ERROR_MESSAGE_LIST;
        def.type = "java.util.List";
        def.mode = "OUT";
        def.optional = true;
        service.addParam(def);
        // successMessage
        def = new ModelParam();
        def.name = ModelService.SUCCESS_MESSAGE;
        def.type = "String";
        def.mode = "OUT";
        def.optional = true;
        service.addParam(def);
        // successMessageList
        def = new ModelParam();
        def.name = ModelService.SUCCESS_MESSAGE_LIST;
        def.type = "java.util.List";
        def.mode = "OUT";
        def.optional = true;
        service.addParam(def);
        // userLogin
        def = new ModelParam();
        def.name = "userLogin";
        def.type = "org.ofbiz.core.entity.GenericValue";
        def.mode = "INOUT";
        def.optional = true;
        service.addParam(def);
    }

    protected Document getDocument(URL url) {
        if (url == null)
            return null;
        Document document = null;
        try {
            document = UtilXml.readXmlDocument(url, true);
        } catch (SAXException sxe) {
            // Error generated during parsing)
            Exception x = sxe;
            if (sxe.getException() != null)
                x = sxe.getException();
            x.printStackTrace();
        } catch (ParserConfigurationException pce) {
            // Parser with specified options can't be built
            pce.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        return document;
    }
}
