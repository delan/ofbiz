package org.ofbiz.core.minilang;

import java.net.*;
import java.text.*;
import java.util.*;
import java.lang.reflect.*;

import org.w3c.dom.*;
import org.apache.oro.text.regex.*;
import org.ofbiz.core.util.*;

/**
 * <p><b>Title:</b> SimpleMapProcessor Mini Language
 * <p><b>Description:</b> None
 * <p>Copyright (c) 2001 The Open For Business Project - www.ofbiz.org
 *
 * <p>Permission is hereby granted, free of charge, to any person obtaining a
 *  copy of this software and associated documentation files (the "Software"),
 *  to deal in the Software without restriction, including without limitation
 *  the rights to use, copy, modify, merge, publish, distribute, sublicense,
 *  and/or sell copies of the Software, and to permit persons to whom the
 *  Software is furnished to do so, subject to the following conditions:
 *
 * <p>The above copyright notice and this permission notice shall be included
 *  in all copies or substantial portions of the Software.
 *
 * <p>THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 *  OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 *  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 *  IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 *  CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT
 *  OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR
 *  THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 *@author <a href="mailto:jonesde@ofbiz.org">David E. Jones</a>
 *@created    December 29, 2001
 *@version    1.0
 */
public class SimpleMapProcessor {
    protected static UtilCache simpleMapProcessors = new UtilCache("SimpleMapProcessors", 0, 0);
    
    public static void runSimpleMapProcessor(String xmlResource, Map inMap, Map results, List messages) throws MiniLangException {
        runSimpleMapProcessor(xmlResource, inMap, results, messages, null);
    }

    public static void runSimpleMapProcessor(String xmlResource, Map inMap, Map results, List messages, Class contextClass) throws MiniLangException {
        URL xmlURL = UtilURL.fromResource(contextClass, xmlResource);
        if (xmlURL == null) {
            throw new MiniLangException("Could not find SimpleMapProcessor XML document in resource: " + xmlResource);
        }
                    
        runSimpleMapProcessor(xmlURL, inMap, results, messages, contextClass);
    }

    public static void runSimpleMapProcessor(URL xmlURL, Map inMap, Map results, List messages, Class contextClass) throws MiniLangException {
        List simpleMapProcesses = getSimpleMapProcesses(xmlURL);
        if (simpleMapProcesses != null && simpleMapProcesses.size() > 0) {
            Iterator strPrsIter = simpleMapProcesses.iterator();
            while (strPrsIter.hasNext()) {
                SimpleMapProcess simpleMapProcess = (SimpleMapProcess) strPrsIter.next();
                simpleMapProcess.exec(inMap, results, messages, contextClass);
            }
        }
    }

    protected static List getSimpleMapProcesses(URL xmlURL) throws MiniLangException {
        List simpleMapProcesses = (List) simpleMapProcessors.get(xmlURL);
        if (simpleMapProcesses == null) {
            synchronized (SimpleMapProcessor.class) {
                simpleMapProcesses = (List) simpleMapProcessors.get(xmlURL);
                if (simpleMapProcesses == null) {
                    simpleMapProcesses = new LinkedList();
                    
                    //read in the file
                    Document document = null;
                    try {
                        document = UtilXml.readXmlDocument(xmlURL, true);
                    } catch (java.io.IOException e) {
                        throw new MiniLangException("Could not read XML file", e);
                    } catch (org.xml.sax.SAXException e) {
                        throw new MiniLangException("Could not parse XML file", e);
                    } catch (javax.xml.parsers.ParserConfigurationException e) {
                        throw new MiniLangException("XML parser not setup correctly", e);
                    }
                    
                    if (document == null) {
                        throw new MiniLangException("Could not find SimpleMapProcessor XML document: " + xmlURL.toString());
                    }
                    
                    Element rootElement = document.getDocumentElement();
                    List simpleMapProcessElements = UtilXml.childElementList(rootElement, "process");
                    
                    Iterator strProcIter = simpleMapProcessElements.iterator();
                    while (strProcIter.hasNext()) {
                        Element simpleMapProcessElement = (Element) strProcIter.next();
                        SimpleMapProcessor.SimpleMapProcess strProc = new SimpleMapProcessor.SimpleMapProcess(simpleMapProcessElement);
                        simpleMapProcesses.add(strProc);
                    }
                    
                    //put it in the cache
                    simpleMapProcessors.put(xmlURL, simpleMapProcesses);
                }
            }
        }
        
        return simpleMapProcesses;
    }
    
    /** A complete string process for a given field; contains multiple string operations */
    public static class SimpleMapProcess {
        List simpleMapOperations = new LinkedList();
        String field = "";
        
        public SimpleMapProcess(Element simpleMapProcessElement) {
            this.field = simpleMapProcessElement.getAttribute("field");
            readOperations(simpleMapProcessElement);
        }
        
        public String getFieldName() {
            return field;
        }
        
        public void exec(Map inMap, Map results, List messages, Class contextClass) {
            Iterator strOpsIter = simpleMapOperations.iterator();
            while (strOpsIter.hasNext()) {
                SimpleMapOperation simpleMapOperation = (SimpleMapOperation) strOpsIter.next();
                simpleMapOperation.exec(inMap, results, messages, contextClass);
            }
        }
        
        void readOperations(Element simpleMapProcessElement) {
            List operationElements = UtilXml.childElementList(simpleMapProcessElement, null);
            if (operationElements != null && operationElements.size() > 0) {
                Iterator operElemIter = operationElements.iterator();
                while (operElemIter.hasNext()) {
                    Element curOperElem = (Element) operElemIter.next();
                    String nodeName = curOperElem.getNodeName();
                    if ("validate-method".equals(nodeName)) {
                        simpleMapOperations.add(new SimpleMapProcessor.ValidateMethod(curOperElem, this));
                    } else if ("compare".equals(nodeName)) {
                        simpleMapOperations.add(new SimpleMapProcessor.Compare(curOperElem, this));
                    } else if ("compare-field".equals(nodeName)) {
                        simpleMapOperations.add(new SimpleMapProcessor.CompareField(curOperElem, this));
                    } else if ("regexp".equals(nodeName)) {
                        simpleMapOperations.add(new SimpleMapProcessor.Regexp(curOperElem, this));
                    } else if ("not-empty".equals(nodeName)) {
                        simpleMapOperations.add(new SimpleMapProcessor.NotEmpty(curOperElem, this));
                    } else if ("copy".equals(nodeName)) {
                        simpleMapOperations.add(new SimpleMapProcessor.Copy(curOperElem, this));
                    } else if ("convert".equals(nodeName)) {
                        simpleMapOperations.add(new SimpleMapProcessor.Convert(curOperElem, this));
                    } else {
                        Debug.logWarning("[SimpleMapProcessor.SimpleMapProcess.readOperations] Operation element \"" + nodeName + "\" no recognized");
                    }
                }
            }
            
        }
    }
    
    /** A single string operation, does the specified operation on the given field */
    public static abstract class SimpleMapOperation {
        String message = null;
        String propertyResource = null;
        boolean isProperty = false;
        SimpleMapProcess simpleMapProcess;
        String fieldName;
        
        public SimpleMapOperation(Element element, SimpleMapProcess simpleMapProcess) {
            Element failMessage = UtilXml.firstChildElement(element, "fail-message");
            Element failProperty = UtilXml.firstChildElement(element, "fail-property");
            if (failMessage != null) {
                this.message = failMessage.getAttribute("message");
                this.isProperty = false;
            } else if (failProperty != null) {
                this.propertyResource = failProperty.getAttribute("resource");
                this.message = failProperty.getAttribute("property");
                this.isProperty = true;
            }
            
            this.simpleMapProcess = simpleMapProcess;
            this.fieldName = simpleMapProcess.getFieldName();
        }
        
        public abstract void exec(Map inMap, Map results, List messages, Class contextClass);
        
        public void addMessage(List messages, Class contextClass) {
            if (!isProperty && message != null) {
                messages.add(message);
                //Debug.logInfo("[SimpleMapOperation.addMessage] Adding message: " + message);
            } else if (isProperty && propertyResource != null && message != null) {
                String propMsg = UtilProperties.getPropertyValue(UtilURL.fromResource(contextClass, propertyResource), message);
                messages.add(propMsg);
                //Debug.logInfo("[SimpleMapOperation.addMessage] Adding property message: " + propMsg);
            } else {
                messages.add("String Processing error occurred, but no message was found, sorry.");
                //Debug.logInfo("[SimpleMapOperation.addMessage] ERROR: No message found");
            }
        }
    }
    
    /* ==================================================================== */
    /* All of the SimpleMapOperations...
    /* ==================================================================== */

    /** A string operation that calls a validation method */
    public static class ValidateMethod extends SimpleMapOperation {
        String methodName;
        String className;
        
        public ValidateMethod(Element element, SimpleMapProcess simpleMapProcess) {
            super(element, simpleMapProcess);
            this.methodName = element.getAttribute("method");
            this.className = element.getAttribute("class");
        }
        
        public void exec(Map inMap, Map results, List messages, Class contextClass) {
            String fieldValue = (java.lang.String) inMap.get(fieldName);
            
            Class[] paramTypes = new Class[] {String.class};
            Object[] params = new Object[] {fieldValue};

            Class valClass;
            try {
                valClass = contextClass.forName(className);
            } catch(ClassNotFoundException cnfe) {
                String msg = "Could not find validation class: " + className;
                messages.add(msg);
                Debug.logError("[ValidateMethod.exec] " + msg);
                return;
            }

            Method valMethod;
            try {
                valMethod = valClass.getMethod(methodName, paramTypes);
            } catch(NoSuchMethodException cnfe) {
                String msg = "Could not find validation method: " + methodName + " of class " + className;
                messages.add(msg);
                Debug.logError("[ValidateMethod.exec] " + msg);
                return;
            }

            Boolean resultBool = Boolean.FALSE;
            try {
                resultBool = (Boolean)valMethod.invoke(null,params);
            } catch(Exception e) {
                String msg = "Error in validation method " + methodName + " of class " + className + ": " + e.getMessage();
                messages.add(msg);
                Debug.logError("[ValidateMethod.exec] " + msg);
                return;
            }

            if(!resultBool.booleanValue()) {
                addMessage(messages, contextClass);
            }
        }
    }

    public static abstract class BaseCompare extends SimpleMapOperation {
        String operator;
        String type;
        String format;

        public BaseCompare(Element element, SimpleMapProcess simpleMapProcess) {
            super(element, simpleMapProcess);
            this.operator = element.getAttribute("operator");
            this.type = element.getAttribute("type");
            this.format = element.getAttribute("format");
            if (this.format == null || this.format.length() == 0) {
                if ("Date".equals(type)) {
                    this.format = "yyyy-MM-dd";
                } else if ("Time".equals(type)) {
                    this.format = "HH:mm:ss";
                } else if ("Timestamp".equals(type)) {
                    this.format = "yyyy-MM-dd HH:mm:ss";
                }
            }
        }
        
        public void doCompare(Object value1, Object value2, List messages, Class contextClass) {
            //Debug.logInfo("[BaseCompare.doCompare] Comparing value1: \"" + value1 + "\", value2:\"" + value2 + "\"");
            
            int result = 0;
            
            Object convertedValue1 = null;
            try {
                convertedValue1 = ObjectType.simpleTypeConvert(value1, type, format);
            } catch (GeneralException e) {
                messages.add("Could not convert value1 for comparison: " + e.getMessage());
                return;
            }

            Object convertedValue2 = null;
            try {
                convertedValue2 = ObjectType.simpleTypeConvert(value2, type, format);
            } catch (GeneralException e) {
                messages.add("Could not convert value2 for comparison: " + e.getMessage());
                return;
            }
            
            if (convertedValue1 == null) {
                return;
            }
            if (convertedValue2 == null) {
                return;
            }
            
            if ("contains".equals(operator)) {
                if (!"String".equals(type)) {
                    messages.add("Error in string-processor file: cannot do a contains compare with a non-String type");
                    return;
                }
                
                String str1 = (String) convertedValue1;
                String str2 = (String) convertedValue2;
                if (str1.indexOf(str2) < 0)
                    addMessage(messages, contextClass);
            }
            
            if ("String".equals(type)) {
                String str1 = (String) convertedValue1;
                String str2 = (String) convertedValue2;
                if (str1.length() == 0 || str2.length() == 0)
                    return;
                result = str1.compareTo(str2);
            } else if ("Double".equals(type) || "Float".equals(type) || "Long".equals(type) || "Integer".equals(type)) {
                Number tempNum = (Number) convertedValue1;
                double value1Double = tempNum.doubleValue();

                tempNum = (Number) convertedValue2;
                double value2Double = tempNum.doubleValue();

                if (value1Double < value2Double)
                    result = -1;
                else if (value1Double < value2Double)
                    result = 1;
                else
                    result = 0;
            } else if ("Date".equals(type)) {
                java.sql.Date value1Date = (java.sql.Date) convertedValue1;
                java.sql.Date value2Date = (java.sql.Date) convertedValue2;
                result = value1Date.compareTo(value2Date);
            } else if ("Time".equals(type)) {
                java.sql.Time value1Time = (java.sql.Time) convertedValue1;
                java.sql.Time value2Time = (java.sql.Time) convertedValue2;
                result = value1Time.compareTo(value2Time);
            } else if ("Timestamp".equals(type)) {
                java.sql.Timestamp value1Timestamp= (java.sql.Timestamp) convertedValue1;
                java.sql.Timestamp value2Timestamp = (java.sql.Timestamp) convertedValue2;
                result = value1Timestamp.compareTo(value2Timestamp);
            } else {
                messages.add("Type \"" + type + "\" specified for compare not supported.");
            }
            
            //Debug.logInfo("[BaseCompare.doCompare] Got Compare result: " + result + ", operator: " + operator);
            if ("less".equals(operator)) {
                if (result >= 0)
                    addMessage(messages, contextClass);
            } else if ("greater".equals(operator)) {
                if (result <= 0)
                    addMessage(messages, contextClass);
            } else if ("less-equals".equals(operator)) {
                if (result > 0)
                    addMessage(messages, contextClass);
            } else if ("greater-equals".equals(operator)) {
                if (result < 0)
                    addMessage(messages, contextClass);
            } else if ("equals".equals(operator)) {
                if (result != 0)
                    addMessage(messages, contextClass);
            } else if ("not-equals".equals(operator)) {
                if (result == 0)
                    addMessage(messages, contextClass);
            } else {
                messages.add("Specified compare operator \"" + operator + "\" not known.");
            }
        }
    }
    
    public static class Compare extends BaseCompare {
        String value;
        
        public Compare(Element element, SimpleMapProcess simpleMapProcess) {
            super(element, simpleMapProcess);
            this.value = element.getAttribute("value");
        }
        
        public void exec(Map inMap, Map results, List messages, Class contextClass) {
            Object fieldValue = inMap.get(fieldName);
            
            doCompare(fieldValue, value, messages, contextClass);
        }
    }

    public static class CompareField extends BaseCompare {
        String compareName;
        
        public CompareField(Element element, SimpleMapProcess simpleMapProcess) {
            super(element, simpleMapProcess);
            this.compareName = element.getAttribute("field");
        }
        
        public void exec(Map inMap, Map results, List messages, Class contextClass) {
            Object compareValue = inMap.get(compareName);
            Object fieldValue = inMap.get(fieldName);
            
            doCompare(fieldValue, compareValue, messages, contextClass);
        }
    }

    public static class Regexp extends SimpleMapOperation {
        static PatternMatcher matcher = new Perl5Matcher();
        static PatternCompiler compiler = new Perl5Compiler();
        Pattern pattern = null;
        String expr;
        
        public Regexp(Element element, SimpleMapProcess simpleMapProcess) {
            super(element, simpleMapProcess);
            expr = element.getAttribute("expr");
            try {
                pattern = compiler.compile(expr);
            } catch (MalformedPatternException e) {
                Debug.logError(e);
            }
        }
        
        public void exec(Map inMap, Map results, List messages, Class contextClass) {
            String fieldValue = (java.lang.String) inMap.get(fieldName);
            
            if (pattern == null) {
                messages.add("Could not compile regular expression \"" + expr + "\" for validation");
                return;
            }
            
            if (!matcher.matches(fieldValue, pattern)) {
                addMessage(messages, contextClass);
            }
        }
    }

    public static class NotEmpty extends SimpleMapOperation {
        public NotEmpty(Element element, SimpleMapProcess simpleMapProcess) {
            super(element, simpleMapProcess);
        }
        
        public void exec(Map inMap, Map results, List messages, Class contextClass) {
            String fieldValue = (java.lang.String) inMap.get(fieldName);
            
            if (!UtilValidate.isNotEmpty(fieldValue)) {
                addMessage(messages, contextClass);
            }
        }
    }

    public static class Copy extends SimpleMapOperation {
        boolean replace = true;
        String toField;
        
        public Copy(Element element, SimpleMapProcess simpleMapProcess) {
            super(element, simpleMapProcess);
            toField = element.getAttribute("to-field");
            if (this.toField == null || this.toField.length() == 0) {
                this.toField = this.fieldName;
            }
            
            replace = "true".equals(element.getAttribute("replace"));
        }
        
        public void exec(Map inMap, Map results, List messages, Class contextClass) {
            Object fieldValue = inMap.get(fieldName);
            
            if (fieldValue == null)
                return;
            
            if (replace) {
                results.put(toField, fieldValue);
                //Debug.logInfo("[SimpleMapProcessor.Copy.exec] Copied \"" + fieldValue + "\" to field \"" + toField + "\"");
            } else {
                if (results.containsKey(toField)) {
                    //do nothing
                } else {
                    results.put(toField, fieldValue);
                    //Debug.logInfo("[SimpleMapProcessor.Copy.exec] Copied \"" + fieldValue + "\" to field \"" + toField + "\"");
                }
            }
        }
    }

    public static class Convert extends SimpleMapOperation {
        String toField;
        String type;
        boolean replace = true;
        String format;
        
        public Convert(Element element, SimpleMapProcess simpleMapProcess) {
            super(element, simpleMapProcess);
            this.toField = element.getAttribute("to-field");
            if (this.toField == null || this.toField.length() == 0) {
                this.toField = this.fieldName;
            }
            
            this.type = element.getAttribute("type");
            this.replace = "true".equals(element.getAttribute("replace"));

            this.format = element.getAttribute("format");
        }
        
        public void exec(Map inMap, Map results, List messages, Class contextClass) {
            Object fieldObject = inMap.get(fieldName);
            
            if (fieldObject == null) {
                return;
            }
            
            if (fieldObject instanceof java.lang.String) {
                if (((String)fieldObject).length() == 0)
                    return;
            }
            
            Object convertedObject = null;
            try {
                convertedObject = ObjectType.simpleTypeConvert(fieldObject, type, format);
            } catch (GeneralException e) {
                messages.add(e.getMessage());
                return;
            }
            
            if (convertedObject == null)
                return;
            
            if (replace) {
                results.put(toField, convertedObject);
                //Debug.logInfo("[SimpleMapProcessor.Converted.exec] Put converted value \"" + convertedObject + "\" in field \"" + toField + "\"");
            } else {
                if (results.containsKey(toField)) {
                    //do nothing
                } else {
                    results.put(toField, convertedObject);
                    //Debug.logInfo("[SimpleMapProcessor.Converted.exec] Put converted value \"" + convertedObject + "\" in field \"" + toField + "\"");
                }
            }
        }
    }
}
