/*
 * $Id$
 */

package org.ofbiz.core.service;

import java.io.*;
import java.util.*;
import java.net.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import org.w3c.dom.Document;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.ofbiz.core.util.*;

/**
 * <p><b>Title:</b> Generic Service - Service Definition Reader
 * <p><b>Description:</b> None
 * <p>Copyright (c) 2001 The Open For Business Project - www.ofbiz.org
 *
 * <p>Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * <p>The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * <p>THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT
 * OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR
 * THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * @author <a href="mailto:jonesde@ofbiz.org">David E. Jones</a>
 * @created May 15, 2001
 * @version 1.0
 */

public class ModelServiceReader {
    public static Map readers = new Hashtable();
    
    public URL readerURL = null;
    public Map modelServices =  null;
    
    public static ModelServiceReader getModelServiceReader(URL readerURL) {
        ModelServiceReader reader = (ModelServiceReader)readers.get(readerURL);
        if(reader == null) { //don't want to block here
            synchronized(ModelServiceReader.class) {
                //must check if null again as one of the blocked threads can still enter
                reader = (ModelServiceReader)readers.get(readerURL);
                if(reader == null) {
                    reader = new ModelServiceReader(readerURL);
                    readers.put(readerURL, reader);
                }
            }
        }
        return reader;
    }
    
    public ModelServiceReader(URL readerURL) {
        this.readerURL = readerURL;
        
        //preload models...
        getModelServices();
    }
    
    public Map getModelServices() {
        if(modelServices == null) { //don't want to block here
            synchronized(ModelServiceReader.class) {
                //must check if null again as one of the blocked threads can still enter
                if(modelServices == null) { //now it's safe
                    modelServices = new HashMap();
                    
                    UtilTimer utilTimer = new UtilTimer();
                    
                    utilTimer.timerString("Before getDocument in file " + readerURL);
                    Document document = getDocument(readerURL);
                    if(document == null) { modelServices = null; return null; }
                    
                    utilTimer.timerString("Before getDocumentElement in file " + readerURL);
                    Element docElement = document.getDocumentElement();
                    if(docElement == null) { modelServices = null; return null; }
                    docElement.normalize();
                    Node curChild = docElement.getFirstChild();
                    
                    int i=0;
                    if(curChild != null) {
                        utilTimer.timerString("Before start of service loop in file " + readerURL);
                        do {
                            if(curChild.getNodeType() == Node.ELEMENT_NODE && "service".equals(curChild.getNodeName())) {
                                i++;
                                Element curService = (Element)curChild;
                                String serviceName = checkEmpty(curService.getAttribute("name"));
                                
                                //check to see if service with same name has already been read
                                if(modelServices.containsKey(serviceName)) {
                                    Debug.logWarning("WARNING: Service " + serviceName + " is defined more than once, most recent will over-write previous definition(s)");
                                }
                                
                                //utilTimer.timerString("  After serviceName -- " + i + " --");
                                ModelService service = createModelService(curService);
                                //utilTimer.timerString("  After createModelService -- " + i + " --");
                                if(service != null) {
                                    modelServices.put(serviceName, service);
                                    //utilTimer.timerString("  After modelServices.put -- " + i + " --");
                                    Debug.logInfo("-- getModelService: #" + i + " Loaded service: " + serviceName);
                                }
                                else Debug.logWarning("-- -- ENTITYGEN ERROR:getModelService: Could not create service for serviceName: " + serviceName);
                                
                            }
                        } while((curChild = curChild.getNextSibling()) != null);
                    }
                    else Debug.logWarning("No child nodes found.");
                    utilTimer.timerString("Finished file " + readerURL + " - Total Entities: " + i + " FINISHED");
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
        if(ec != null) return (ModelService)ec.get(serviceName);
        else return null;
    }
    
    /** Creates a Iterator with the serviceName of each Service defined in the specified XML Service Descriptor file.
     * @return A Iterator of serviceName Strings
     */
    public Iterator getServiceNamesIterator() {
        Collection collection = getServiceNames();
        if(collection != null) return collection.iterator();
        else return null;
    }
    
    /** Creates a Collection with the serviceName of each Service defined in the specified XML Service Descriptor file.
     * @return A Collection of serviceName Strings
     */
    public Collection getServiceNames() {
        Map ec = getModelServices();
        return ec.keySet();
    }
    
    ModelService createModelService(Element serviceElement) {
        ModelService service = new ModelService();
        
        service.name = checkEmpty(serviceElement.getAttribute("name"));
        service.engineName = checkEmpty(serviceElement.getAttribute("engine-name"));
        service.location = checkEmpty(serviceElement.getAttribute("location"));
        service.invoke = checkEmpty(serviceElement.getAttribute("invoke"));
        
        service.contextInfo = new HashMap();
        createAttrDefs(serviceElement, "context-info", service.contextInfo);
        service.resultInfo = new HashMap();
        createAttrDefs(serviceElement, "result-info", service.resultInfo);
        
        return service;
    }
    
    void createAttrDefs(Element baseElement, String parentNodeName, Map attrMap) {
        NodeList tempList = baseElement.getElementsByTagName(parentNodeName);
        Element tempElement = (Element)tempList.item(0);
        if(tempElement != null) {
            NodeList attrDefList = tempElement.getElementsByTagName("attr-def");
            for(int i=0; i<attrDefList.getLength(); i++) {
                Element attrDefElement = (Element)attrDefList.item(i);
                String name = checkEmpty(attrDefElement.getAttribute("name"));
                String type = checkEmpty(attrDefElement.getAttribute("type"));
                attrMap.put(name, type);
            }
        }
    }
    
    String childElementValue(Element element, String childElementName) {
        if(element == null || childElementName == null) return null;
        //get the value of the first element with the given name
        Node node = element.getFirstChild();
        if(node != null) {
            do {
                if(node.getNodeType() == Node.ELEMENT_NODE && childElementName.equals(node.getNodeName())) {
                    Element childElement = (Element)node;
                    return elementValue(childElement);
                }
            } while((node = node.getNextSibling()) != null);
        }
        return null;
    }
    
    String elementValue(Element element) {
        Node textNode = element.getFirstChild();
        if(textNode == null) return null;
        //should be of type text
        return textNode.getNodeValue();
    }
    
    String checkEmpty(String string) {
        if(string != null && string.length() > 0) return string;
        else return "";
    }
    
    String checkEmpty(String string1, String string2) {
        if(string1 != null && string1.length() > 0) return string1;
        else if(string2 != null && string2.length() > 0) return string2;
        else return "";
    }
    String checkEmpty(String string1, String string2, String string3) {
        if(string1 != null && string1.length() > 0) return string1;
        else if(string2 != null && string2.length() > 0) return string2;
        else if(string3 != null && string3.length() > 0) return string3;
        else return "";
    }
    
    Document getDocument(URL url) {
        if(url == null) return null;
        Document document = null;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(true);
        //factory.setNamespaceAware(true);
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            document = builder.parse(url.openStream());
        }
        catch (SAXException sxe) {
            // Error generated during parsing)
            Exception  x = sxe;
            if(sxe.getException() != null) x = sxe.getException();
            x.printStackTrace();
        }
        catch(ParserConfigurationException pce) {
            // Parser with specified options can't be built
            pce.printStackTrace();
        }
        catch(IOException ioe) { ioe.printStackTrace(); }
        
        return document;
    }
}
