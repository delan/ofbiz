/*
 * $Id$
 *
 * Copyright (c) 2001-2005 The Open For Business Project - www.ofbiz.org
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
 */
package org.ofbiz.entity.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.ofbiz.base.util.Base64;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilURL;
import org.ofbiz.base.util.UtilXml;
import org.ofbiz.content.webapp.ftl.FreeMarkerWorker;
import org.ofbiz.entity.GenericDelegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.model.ModelEntity;
import org.ofbiz.entity.model.ModelField;
import org.ofbiz.entity.transaction.GenericTransactionException;
import org.ofbiz.entity.transaction.TransactionUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import freemarker.ext.beans.BeansWrapper;
import freemarker.ext.dom.NodeModel;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateHashModel;

/**
 * SAX XML Parser Content Handler for Entity Engine XML files
 *
 * @author     <a href="mailto:jonesde@ofbiz.org">David E. Jones</a>
 * @author     <a href="mailto:jaz@ofbiz.org">Andy Zeneski</a>
 * @version    $Rev:$
 * @since      2.0
 */
public class EntitySaxReader implements org.xml.sax.ContentHandler, ErrorHandler {

    public static final String module = EntitySaxReader.class.getName();
    public static final int DEFAULT_TX_TIMEOUT = 7200;

    protected org.xml.sax.Locator locator;
    protected GenericDelegator delegator;
    protected GenericValue currentValue = null;
    protected String currentFieldName = null;
    protected String currentFieldValue = null;
    protected long numberRead = 0;

    protected int valuesPerWrite = 100;
    protected int valuesPerMessage = 1000;
    protected int transactionTimeout = 7200;
    protected boolean useTryInsertMethod = false;
    protected boolean maintainTxStamps = false;
    protected boolean createDummyFks = false;
    protected boolean doCacheClear = true;

    protected List valuesToWrite = new ArrayList(valuesPerWrite);

    protected boolean isParseForTemplate = false;
    protected String templatePath = null;
    protected Node rootNodeForTemplate = null;
    protected Node currentNodeForTemplate = null;
    protected Document documentForTemplate = null;

    protected EntitySaxReader() {}

    public EntitySaxReader(GenericDelegator delegator, int transactionTimeout) {
        this.delegator = delegator;
        this.transactionTimeout = transactionTimeout;
    }

    public EntitySaxReader(GenericDelegator delegator) {
        this(delegator, DEFAULT_TX_TIMEOUT);
    }

    public int getValuesPerWrite() {
        return this.valuesPerWrite;
    }

    public void setValuesPerWrite(int valuesPerWrite) {
        this.valuesPerWrite = valuesPerWrite;
    }

    public int getValuesPerMessage() {
        return this.valuesPerMessage;
    }

    public void setValuesPerMessage(int valuesPerMessage) {
        this.valuesPerMessage = valuesPerMessage;
    }

    public int getTransactionTimeout() {
        return this.transactionTimeout;
    }
    
    public void setUseTryInsertMethod(boolean value) {
        this.useTryInsertMethod = value;
    }

    public void setTransactionTimeout(int transactionTimeout) throws GenericTransactionException {
        if (this.transactionTimeout != transactionTimeout) {
            TransactionUtil.setTransactionTimeout(transactionTimeout);
            this.transactionTimeout = transactionTimeout;
        }
    }

    public boolean getMaintainTxStamps() {
        return this.maintainTxStamps;
    }

    public void setMaintainTxStamps(boolean maintainTxStamps) {
        this.maintainTxStamps = maintainTxStamps;
    }

    public boolean getCreateDummyFks() {
        return this.createDummyFks;
    }

    public void setCreateDummyFks(boolean createDummyFks) {
        this.createDummyFks = createDummyFks;
    }

    public boolean getDoCacheClear() {
        return this.doCacheClear;
    }

    public void setDoCacheClear(boolean doCacheClear) {
        this.doCacheClear = doCacheClear;
    }

    public long parse(String content) throws SAXException, java.io.IOException {
        if (content == null) {
            Debug.logWarning("content was null, doing nothing", module);
            return 0;
        }
        ByteArrayInputStream bis = new ByteArrayInputStream(content.getBytes());

        return this.parse(bis, "Internal Content");
    }

    public long parse(URL location) throws SAXException, java.io.IOException {
        if (location == null) {
            Debug.logWarning("location URL was null, doing nothing", module);
            return 0;
        }
        Debug.logImportant("Beginning import from URL: " + location.toExternalForm(), module);
        return this.parse(location.openStream(), location.toString());
    }

    public long parse(InputStream is, String docDescription) throws SAXException, java.io.IOException {

        /* NOTE: this method is not used because it doesn't work with various parsers...
         String orgXmlSaxDriver = System.getProperty("org.xml.sax.driver");
         if (UtilValidate.isEmpty(orgXmlSaxDriver)) orgXmlSaxDriver = "org.apache.xerces.parsers.SAXParser";
         XMLReader reader = XMLReaderFactory.createXMLReader(orgXmlSaxDriver);
         */

        XMLReader reader = null;

        try {
            SAXParserFactory parserFactory = SAXParserFactory.newInstance();
            SAXParser parser = parserFactory.newSAXParser();

            reader = parser.getXMLReader();
        } catch (javax.xml.parsers.ParserConfigurationException e) {
            Debug.logError(e, "Failed to get a SAX XML parser", module);
            throw new IllegalStateException("Failed to get a SAX XML parser");
        }

        reader.setContentHandler(this);
        reader.setErrorHandler(this);
        // LocalResolver lr = new UtilXml.LocalResolver(new DefaultHandler());
        // reader.setEntityResolver(lr);

        numberRead = 0;
        try {
            boolean beganTransaction = false;
            if (transactionTimeout > -1) {
                beganTransaction = TransactionUtil.begin(transactionTimeout);
                Debug.logImportant("Transaction Timeout set to " + transactionTimeout / 3600 + " hours (" + transactionTimeout + " seconds)", module);
            }
            try {
                reader.parse(new InputSource(is));
                // make sure all of the values to write got written...
                if (valuesToWrite.size() > 0) {
                    writeValues(valuesToWrite);
                    valuesToWrite.clear();
                }
                TransactionUtil.commit(beganTransaction);
            } catch (Exception e) {
                Debug.logError(e, "An error occurred saving the data, rolling back transaction (" + beganTransaction + ")", module);
                TransactionUtil.rollback(beganTransaction);
                throw new SAXException("A transaction error occurred reading data", e);
            }
        } catch (GenericTransactionException e) {
            throw new SAXException("A transaction error occurred reading data", e);
        }
        Debug.logImportant("Finished " + numberRead + " values from " + docDescription, module);
        return numberRead;
    }

    protected void writeValues(List valuesToWrite) throws GenericEntityException {
        delegator.storeAll(valuesToWrite, doCacheClear, createDummyFks);
    }

    public void characters(char[] values, int offset, int count) throws org.xml.sax.SAXException {
        if (isParseForTemplate) {
            // if null, don't worry about it
            if (this.currentNodeForTemplate != null) {
                Node newNode = this.documentForTemplate.createTextNode(new String(values, offset, count));
                this.currentNodeForTemplate.appendChild(newNode);
            }
            return;
        }
        
        if (currentValue != null && currentFieldName != null) {
            String value = new String(values, offset, count);

            // Debug.logInfo("characters: value=" + value, module);
            if (currentFieldValue == null) {
                currentFieldValue = value;
            } else {
                currentFieldValue += value;
            }
        }
    }

    public void endDocument() throws org.xml.sax.SAXException {}

    public void endElement(String namespaceURI, String localName, String fullName) throws org.xml.sax.SAXException {
        if (Debug.verboseOn()) Debug.logVerbose("endElement: localName=" + localName + ", fullName=" + fullName + ", numberRead=" + numberRead, module);
        if ("entity-engine-xml".equals(fullName)) {
            return;
        }
        if ("entity-engine-transform-xml".equals(fullName)) {
            // transform file & parse it, then return
            URL templateUrl = UtilURL.fromResource(templatePath);
            
            if (templateUrl == null) {
                throw new SAXException("Could not find transform template with resource path: " + templatePath);
            } else {
                try {
                    Reader templateReader = new InputStreamReader(templateUrl.openStream());
                    
                    StringWriter outWriter = new StringWriter();
                    Configuration config = FreeMarkerWorker.makeDefaultOfbizConfig();
                    
                    Template template = new Template("FMImportFilter", templateReader, config);
                    NodeModel nodeModel = NodeModel.wrap(this.rootNodeForTemplate);
                        
                    Map context = new HashMap();
                    BeansWrapper wrapper = BeansWrapper.getDefaultInstance();
                    TemplateHashModel staticModels = wrapper.getStaticModels();
                    context.put("Static", staticModels);
                    
                    context.put("doc", nodeModel);
                    template.process(context, outWriter);
                    String s = outWriter.toString();
                    if (Debug.verboseOn()) Debug.logVerbose("transformed xml: " + s, module);

                    EntitySaxReader reader = new EntitySaxReader(delegator);
                    reader.setUseTryInsertMethod(this.useTryInsertMethod);
                    try {
                        reader.setTransactionTimeout(this.transactionTimeout);
                    } catch (GenericTransactionException e1) {
                        // couldn't set tx timeout, shouldn't be a big deal
                    }
                    
                    numberRead += reader.parse(s);
                } catch (TemplateException e) {
                    throw new SAXException("Error storing value", e);
                } catch(IOException e) {
                    throw new SAXException("Error storing value", e);
                }
            }
            
            return;
        }

        if (isParseForTemplate) {
            this.currentNodeForTemplate = this.currentNodeForTemplate.getParentNode();
            return;
        }

        if (currentValue != null) {
            if (currentFieldName != null) {
                if (currentFieldValue != null && currentFieldValue.length() > 0) {
                    if (currentValue.getModelEntity().isField(currentFieldName)) {
                        ModelEntity modelEntity = currentValue.getModelEntity();
                        ModelField modelField = modelEntity.getField(currentFieldName);
                        String type = modelField.getType();
                        if (type != null && type.equals("blob")) {
                            byte strData[] = new byte[currentFieldValue.length()];
                            strData = currentFieldValue.getBytes();
                            byte binData[] = new byte[currentFieldValue.length()];
                            binData = Base64.base64Decode(strData);
                            currentValue.setBytes(currentFieldName, binData);
                        } else {
                            currentValue.setString(currentFieldName, currentFieldValue);
                        }
                    } else {
                        Debug.logWarning("Ignoring invalid field name [" + currentFieldName + "] found for the entity: " + currentValue.getEntityName() + " with value=" + currentFieldValue, module);
                    }
                    currentFieldValue = null;
                }
                currentFieldName = null;
            } else {
                // before we write currentValue check to see if PK is there, if not and it is one field, generate it from a sequence using the entity name
                if (!currentValue.containsPrimaryKey()) {
                    if (currentValue.getModelEntity().getPksSize() == 1) {
                        ModelField modelField = currentValue.getModelEntity().getPk(0);
                        String newSeq = delegator.getNextSeqId(currentValue.getEntityName());
                        currentValue.setString(modelField.getName(), newSeq);
                    } else {
                        throw new SAXException("Cannot store value with incomplete primary key with more than 1 primary key field: " + currentValue);
                    }
                }
                
                try {
                    if (useTryInsertMethod) {
                        // this technique is faster for data sets where most, if not all, values do not already exist in the database
                        try {
                            currentValue.create();
                        } catch (GenericEntityException e1) {
                            // create failed, try a store, if that fails too we have a real error and the catch outside of this should handle it
                            currentValue.store();
                        }
                    } else {
                        valuesToWrite.add(currentValue);
                        if (valuesToWrite.size() >= valuesPerWrite) {
                            writeValues(valuesToWrite);
                            valuesToWrite.clear();
                        }
                    }
                    numberRead++;
                    if ((numberRead % valuesPerMessage) == 0) {
                        Debug.logImportant("Another " + valuesPerMessage + " values imported: now up to " + numberRead, module);
                    }
                    currentValue = null;
                } catch (GenericEntityException e) {
                    String errMsg = "Error storing value";
                    Debug.logError(e, errMsg, module);
                    throw new SAXException(errMsg, e);
                }
            }
        }
    }

    public void endPrefixMapping(String prefix) throws org.xml.sax.SAXException {}

    public void ignorableWhitespace(char[] values, int offset, int count) throws org.xml.sax.SAXException {
        // String value = new String(values, offset, count);
        // Debug.logInfo("ignorableWhitespace: value=" + value, module);
    }

    public void processingInstruction(String target, String instruction) throws org.xml.sax.SAXException {}

    public void setDocumentLocator(org.xml.sax.Locator locator) {
        this.locator = locator;
    }

    public void skippedEntity(String name) throws org.xml.sax.SAXException {}

    public void startDocument() throws org.xml.sax.SAXException {}

    public void startElement(String namepsaceURI, String localName, String fullName, org.xml.sax.Attributes attributes) throws org.xml.sax.SAXException {
        if (Debug.verboseOn()) Debug.logVerbose("startElement: localName=" + localName + ", fullName=" + fullName + ", attributes=" + attributes, module);
        if ("entity-engine-xml".equals(fullName)) {
            return;
        }
        
        if ("entity-engine-transform-xml".equals(fullName)) {
            templatePath = attributes.getValue("template");
            isParseForTemplate = true;
            documentForTemplate = UtilXml.makeEmptyXmlDocument();
            return;
        }
        
        if (isParseForTemplate) {
            Element newElement = this.documentForTemplate.createElement(fullName);
            int length = attributes.getLength();
            for (int i = 0; i < length; i++) {
                String name = attributes.getLocalName(i);
                String value = attributes.getValue(i);

                if (name == null || name.length() == 0) {
                    name = attributes.getQName(i);
                }
                newElement.setAttribute(name, value);
            }
            
            if (this.currentNodeForTemplate == null) {
                this.currentNodeForTemplate = newElement;
                this.rootNodeForTemplate = newElement;
            } else {
                this.currentNodeForTemplate.appendChild(newElement);
                this.currentNodeForTemplate = newElement;
            }
            return;
        }
        
        if (currentValue != null) {
            // we have a nested value/CDATA element
            currentFieldName = fullName;
        } else {
            String entityName = fullName;

            // if a dash or colon is in the tag name, grab what is after it
            if (entityName.indexOf('-') > 0) {
                entityName = entityName.substring(entityName.indexOf('-') + 1);
            }
            if (entityName.indexOf(':') > 0) {
                entityName = entityName.substring(entityName.indexOf(':') + 1);
            }

            try {
                currentValue = delegator.makeValue(entityName, null);
                // TODO: do we really want this? it makes it so none of the values imported have create/update timestamps set 
                // DEJ 10/16/04 I think they should all be stamped, so commenting this out
                // JAZ 12/10/04 I think it should be specified when creating the reader
                if (this.maintainTxStamps) {
                    currentValue.setIsFromEntitySync(true);
                }                
            } catch (Exception e) {
                Debug.logError(e, module);
            }

            if (currentValue != null) {
                int length = attributes.getLength();

                for (int i = 0; i < length; i++) {
                    String name = attributes.getLocalName(i);
                    String value = attributes.getValue(i);

                    if (name == null || name.length() == 0) {
                        name = attributes.getQName(i);
                    }
                    try {
                        // treat empty strings as nulls
                        if (value != null && value.length() > 0) {
                        	if (currentValue.getModelEntity().isField(name)) {
                                currentValue.setString(name, value);
                        	} else {
                                Debug.logWarning("Ignoring invalid field name [" + name + "] found for the entity: " + currentValue.getEntityName() + " with value=" + value, module);
                        	}
                        }
                    } catch (Exception e) {
                        Debug.logWarning(e, "Could not set field " + name + " to the value " + value, module);
                    }
                }
            }
        }
    }

    public void startPrefixMapping(String prefix, String uri) throws org.xml.sax.SAXException {}

    // ======== ErrorHandler interface implementations ========

    public void error(org.xml.sax.SAXParseException exception) throws org.xml.sax.SAXException {
        Debug.logWarning(exception, "Error reading XML on line " + exception.getLineNumber() + ", column " + exception.getColumnNumber(), module);
    }

    public void fatalError(org.xml.sax.SAXParseException exception) throws org.xml.sax.SAXException {
        Debug.logError(exception, "Fatal Error reading XML on line " + exception.getLineNumber() + ", column " + exception.getColumnNumber(), module);
        throw new SAXException("Fatal Error reading XML on line " + exception.getLineNumber() + ", column " + exception.getColumnNumber(), exception);
    }

    public void warning(org.xml.sax.SAXParseException exception) throws org.xml.sax.SAXException {
        Debug.logWarning(exception, "Warning reading XML on line " + exception.getLineNumber() + ", column " + exception.getColumnNumber(), module);
    }
}
