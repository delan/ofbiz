/*
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

import java.util.*;

import org.ofbiz.core.util.*;

/**
 * Generic Service Model Class
 *
 *@author     <a href="mailto:jaz@zsolv.com">Andy Zeneski</a>
 *@author     <a href="mailto:jonesde@ofbiz.org">David E. Jones</a>
 *@created    October 20, 2001
 *@version    1.0
 */
public class ModelService {

    public static final String module = ModelService.class.getName();

    public static final String OUT_PARAM = "OUT";
    public static final String IN_PARAM = "IN";

    public static final String RESPONSE_MESSAGE = "responseMessage";
    public static final String RESPOND_SUCCESS = "success";
    public static final String RESPOND_ERROR = "error";
    public static final String ERROR_MESSAGE = "errorMessage";
    public static final String ERROR_MESSAGE_LIST = "errorMessageList";
    public static final String SUCCESS_MESSAGE = "successMessage";
    public static final String SUCCESS_MESSAGE_LIST = "successMessageList";

    /** The name of this service */
    public String name;

    /** The description of this service */
    public String description;

    /** The name of the engine from engine.properties */
    public String engineName;

    /** The namespace of this service */
    public String nameSpace;

    /** The package name or location of this service */
    public String location;

    /** The method or function to invoke for this service */
    public String invoke;

    /** Does this service require authorization */
    public boolean auth;

    /** Can this service be exported via RPC, RMI, SOAP, etc */
    public boolean export;

    /** Validate the context info for this service */
    public boolean validate;

    /** Context Information, a list of parameters used by the service, contains ModelParam objects */
    protected Map contextInfo = new HashMap();
    /** Context Information, a list of parameters used by the service, contains ModelParam objects */
    protected List contextParamList = new LinkedList();

    /** Gets the ModelParam by name
     * @param name The name of the parameter to get
     * @return ModelParam object with the specified name
     */
    public ModelParam getParam(String name) {
        if (contextInfo.containsKey(name))
            return (ModelParam) contextInfo.get(name);
        return null;
    }
    
    /** Adds a parameter definition to this service; puts on list in order added
     * then sorts by order if specified.
     */
    public void addParam(ModelParam param) {
        contextInfo.put(param.name, param);
        contextParamList.add(param);
    }

    public List getAllParamNames() {
        List nameList = new LinkedList();
        Iterator i = this.contextParamList.iterator();
        while (i.hasNext()) {
            ModelParam p = (ModelParam) i.next();
            nameList.add(p.name);
        }
        return nameList;
    }
    
    public List getInParamNames() {
        List nameList = new LinkedList();
        Iterator i = this.contextParamList.iterator();
        while (i.hasNext()) {
            ModelParam p = (ModelParam) i.next();
            //don't include OUT parameters in this list, only IN and INOUT
            if ("OUT".equals(p.mode)) continue;
            nameList.add(p.name);
        }
        return nameList;
    }
    
    /** Validates a Map against the IN or OUT parameter information
     * @param test The Map object to test
     * @param mode Test either mode IN or mode OUT
     * @return true if the validation is successful
     */
    public void validate(Map test, String mode) throws ServiceValidationException {
        Map requiredInfo = new HashMap();
        Map optionalInfo = new HashMap();

        Debug.logVerbose("[ModelService.validate] : Validating context - " + test, module);

        // do not validate results with errors
        if (mode.equals(OUT_PARAM) && test != null && test.containsKey(RESPONSE_MESSAGE) &&
                test.get(RESPONSE_MESSAGE).equals(RESPOND_ERROR)) {
            Debug.logVerbose("[ModelService.validate] : response was an error, not validating.", module);
            return;
        }

        // get the info values
        Collection values = contextInfo.values();

        Iterator i = values.iterator();
        while (i.hasNext()) {
            ModelParam p = (ModelParam) i.next();
            if (p.mode.equals("INOUT") || p.mode.equals(mode)) {
                if (!p.optional)
                    requiredInfo.put(p.name, p.type);
                else
                    optionalInfo.put(p.name, p.type);
            }
        }

        // get the test values
        Map requiredTest = new HashMap();
        Map optionalTest = new HashMap();
        if (test == null)
            test = new HashMap();

        requiredTest.putAll(test);
        if (requiredTest != null) {
            List keyList = new ArrayList(requiredTest.keySet());
            Iterator t = keyList.iterator();
            while (t.hasNext()) {
                Object key = t.next();
                Object value = requiredTest.get(key);
                if (!requiredInfo.containsKey(key)) {
                    requiredTest.remove(key);
                    optionalTest.put(key, value);
                }
            }
        }

        Debug.logVerbose("[ModelService.validate] : (" + mode + ") Required - " +
                         requiredTest.size() + " / " + requiredInfo.size(), module);
        Debug.logVerbose("[ModelService.validate] : (" + mode + ") Optional - " +
                         optionalTest.size() + " / " + optionalInfo.size(), module);

        try {
            validate(requiredInfo, requiredTest, true);
            validate(optionalInfo, optionalTest, false);
        } catch (ServiceValidationException e) {
            Debug.logError("[ModelService.validate] : (" + mode + ") Required test error: " + e.toString(), module);
            throw e;
        }
    }

    /** Validates a map of name, object types to a map of name, objects
     * @param info The map of name, object types
     * @param test The map to test its value types.
     * @param reverse Test the maps in reverse.
     * @returns true if validation is successful
     */
    public static void validate(Map info, Map test, boolean reverse) throws ServiceValidationException {
        if (info == null || test == null)
            throw new ServiceValidationException("Cannot validate NULL maps");

        // * Validate keys first
        Set testSet = test.keySet();
        Set keySet = info.keySet();

        // Quick check for sizes
        if (info.size() == 0 && test.size() == 0)
            return;
        // This is to see if the test set contains all from the info set (reverse)
        if (reverse && !testSet.containsAll(keySet)) {
            Set missing = new TreeSet(keySet);
            missing.removeAll(testSet);
            String missingStr = "";
            Iterator iter = missing.iterator();
            while (iter.hasNext()) {
                missingStr += (String) iter.next();
                if (iter.hasNext()) {
                    missingStr += ", ";
                }
            }

            throw new ServiceValidationException("The following required parameters are missing: " + missingStr);
        }
        // This is to see if the info set contains all from the test set
        if (!keySet.containsAll(testSet)) {
            Set extra = new TreeSet(testSet);
            extra.removeAll(keySet);
            String extraStr = "";
            Iterator iter = extra.iterator();
            while (iter.hasNext()) {
                extraStr += (String) iter.next();
                if (iter.hasNext()) {
                    extraStr += ", ";
                }
            }
            throw new ServiceValidationException("Unknown paramters found: " + extraStr);
        }

        // * Validate types next
        // Warning - the class types MUST be accessible to this classloader
        String LANG_PACKAGE = "java.lang."; // We will test both the raw value and this + raw value
        String SQL_PACKAGE = "java.sql.";   // We will test both the raw value and this + raw value

        Iterator i = testSet.iterator();
        while (i.hasNext()) {
            Object key = i.next();
            Object testObject = test.get(key);
            String infoType = (String) info.get(key);
            Class infoClass = null;
            try {
                infoClass = ObjectType.loadClass(infoType);
            } catch (SecurityException se1) {
                throw new ServiceValidationException("Problems with classloader: sercurity exception (" +
                        se1.getMessage() + ")");
            } catch (ClassNotFoundException e1) {
                try {
                    infoClass = ObjectType.loadClass(LANG_PACKAGE + infoType);
                } catch (SecurityException se2) {
                    throw new ServiceValidationException("Problems with classloader: sercurity exception (" +
                            se2.getMessage() + ")");
                } catch (ClassNotFoundException e2) {
                    try {
                        infoClass = ObjectType.loadClass(SQL_PACKAGE + infoType);
                    } catch (SecurityException se3) {
                        throw new ServiceValidationException("Problems with classloader: sercurity exception (" +
                                se3.getMessage() + ")");
                    } catch (ClassNotFoundException e3) {
                        throw new ServiceValidationException("Cannot find and load the class of type: " + infoType +
                                " or of type: " + LANG_PACKAGE + infoType + " or of type: " + SQL_PACKAGE + infoType +
                                ":  (" + e3.getMessage() + ")");
                    }
                }
            }

            if (infoClass == null)
                throw new ServiceValidationException("Illegal type found in info map (could not load class for specified type)");

            if (!ObjectType.instanceOf(testObject, infoClass)) {
                String testType = testObject == null ? "null" : testObject.getClass().getName();
                throw new ServiceValidationException("Type check failed for field " + key + "; expected type is " +
                        infoType + "; actual type is: " + testType);
            }
        }
    }

    /**
     * Gets the parameter names of the specified mode (IN/OUT/INOUT). The 
     * parameters will be returned in the order specified in the file.
     * Note: IN and OUT will also contains INOUT parameters.
     * @param mode The mode (IN/OUT/INOUT)
     * @param optional True if to include optional parameters
     * @return List of parameter names
     */
    public List getParameterNames(String mode, boolean optional) {
        List names = new ArrayList();
        if (!"IN".equals(mode) && !"OUT".equals(mode) && !"INOUT".equals(mode)) {
            return names;
        }
        if (contextInfo.size() == 0) {
            return names;
        }
        Iterator i = contextParamList.iterator();
        while (i.hasNext()) {
            ModelParam param = (ModelParam) i.next();
            if (param.mode.equals("INOUT") || param.mode.equals(mode)) {
                if (optional || (!optional && !param.optional)) {
                    names.add(name);
                }
            }
        }
        return names;
    }

    /**
     * Creates a new Map based from an existing map with just valid parameters
     * @param source The source map
     * @param mode The mode which to build the new map
     * @returns Map a new Map of only valid parameters
     */
    public Map makeValid(Map source, String mode) {
        Map target = new HashMap();
        if (source == null) {
            return target;
        }
        if (!"IN".equals(mode) && !"OUT".equals(mode) && !"INOUT".equals(mode)) {
            return target;
        }
        if (contextInfo.size() == 0) {
            return target;
        }
        List names = getParameterNames(mode, true);
        Iterator i = names.iterator();
        while (i.hasNext()) {
            Object key = i.next();
            if (source.containsKey(key)) {
                target.put(key, source.get(key));
            }
        }
        return target;
    }

    /**
     * Gets a list of required IN parameters in sequence.
     * @return A list of required IN parameters in the order which they were defined.
     */
     public List getInParameterSequence(Map source) {
        List target = new LinkedList();
        if (source == null) {
            return target;
        }
        if (contextInfo == null || contextInfo.size() == 0) {
            return target;
        }
        Iterator i = this.contextParamList.iterator();
        while (i.hasNext()) {
            ModelParam p = (ModelParam) i.next();
            //don't include OUT parameters in this list, only IN and INOUT
            if ("OUT".equals(p.mode)) continue;
            
            Object srcObject = source.get(p.name);
            if (srcObject != null) {
                target.add(srcObject);
            }
        }
        return target;
    }
     
    /** Returns a list of ModelParam objects in the order they were defined when 
     * the service was created.
     */
    public List getModelParamList() {
        return new LinkedList(this.contextParamList);
    }
     
    /** Returns a list of ModelParam objects in the order they were defined when 
     * the service was created.
     */
    public List getInModelParamList() {
        List inList = new LinkedList();
        Iterator i = this.contextParamList.iterator();
        while (i.hasNext()) {
            ModelParam p = (ModelParam) i.next();
            //don't include OUT parameters in this list, only IN and INOUT
            if ("OUT".equals(p.mode)) continue;
            inList.add(p);
        }
        return inList;
    }
}
