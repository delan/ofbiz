/*
 * $Id$
 *
 *  Copyright (c) 2001, 2002 The Open For Business Project - www.ofbiz.org
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a
 *  copy of this software and associated documentation files (the "Software"),
 *  to deal in the Software without restriction, including without limitation
 *  the rights to use, copy, modify, merge, publish, distribute, sublicense,
 *  and/or sell copies of the Software, and to permit persons to whom the
 *  Software is furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included
 *  in all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 *  OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 *  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 *  IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 *  CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT
 *  OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR
 *  THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.ofbiz.service.group;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilXml;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.ModelService;
import org.ofbiz.service.ServiceDispatcher;
import org.w3c.dom.Element;

/**
 * GroupModel.java
 * 
 * @author     <a href="mailto:jaz@ofbiz.org">Andy Zeneski</a>
 * @version    $Rev:$
 * @since      2.0
 */
public class GroupModel {
    
    public static final String module = GroupModel.class.getName();
    
    private String groupName, sendMode;    
    private List services;
    private int lastServiceRan;
    
    /**
     * Constructor using DOM Element
     * @param group DOM element for the group
     */
    public GroupModel(Element group) {
        this.lastServiceRan = -1;
        this.services = new LinkedList();
        List serviceList = UtilXml.childElementList(group, "service");  
        Iterator i = serviceList.iterator();
        while (i.hasNext()) {
            Element service = (Element) i.next();
            services.add(new GroupServiceModel(service));
        }
        this.groupName = group.getAttribute("name");
        this.sendMode = group.getAttribute("send-mode");        
        if (Debug.verboseOn()) Debug.logVerbose("Created Service Group Model --> " + this, module);       
    }
    
    /**
     * Basic Constructor
     * @param groupName Name of the group
     * @param sendMode Mode used (see DTD)
     * @param services List of GroupServiceModel objects
     */
    public GroupModel(String groupName, String sendMode, List services) {
        this.lastServiceRan = -1;
        this.groupName = groupName;
        this.sendMode = sendMode;
        this.services = services;
    }
    
    /**
     * Getter for group name
     * @return String
     */
    public String getGroupName() {
        return this.groupName;
    }
    
    /**
     * Getter for send mode
     * @return String
     */
    public String getSendMode() {
        return this.sendMode;
    }
    
    /**
     * Returns a list of services in this group
     * @return List
     */
    public List getServices() {
        return this.services;
    }
    
    /**
     * Invokes the group of services in order defined
     * @param dispatcher ServiceDispatcher used for invocation
     * @param localName Name of the LocalDispatcher (namespace)
     * @param context Full parameter context (combined for all services)
     * @return Map Result Map
     * @throws GenericServiceException
     */
    public Map run(ServiceDispatcher dispatcher, String localName, Map context) throws GenericServiceException {
        if (this.getSendMode().equals("all")) {
            return runAll(dispatcher, localName, context);
        } else if (this.getSendMode().equals("round-robin")) {
            return runIndex(dispatcher, localName, context, (++lastServiceRan % services.size()));   
        } else if (this.getSendMode().equals("random")) {
            int randomIndex = (int) (Math.random() * (double) (services.size())); 
            return runIndex(dispatcher, localName, context, randomIndex);
        } else if (this.getSendMode().equals("first-available")) {
            return runOne(dispatcher, localName, context);  
        } else if (this.getSendMode().equals("none")) {
            return new HashMap();                                 
        } else { 
            throw new GenericServiceException("This mode is not currently supported");
        }
    }
    
    /**     
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuffer str = new StringBuffer();
        str.append(getGroupName());
        str.append("::");
        str.append(getSendMode());
        str.append("::");        
        str.append(getServices());
        return str.toString();
    }
    
    private Map runAll(ServiceDispatcher dispatcher, String localName, Map context) throws GenericServiceException {
        Map runContext = new HashMap(context);
        Map result = new HashMap();
        Iterator i = services.iterator();
        while (i.hasNext()) {
            GroupServiceModel model = (GroupServiceModel) i.next();
            if (Debug.verboseOn()) Debug.logVerbose("Using Context: " + runContext, module);
            Map thisResult = model.invoke(dispatcher, localName, runContext);
            if (Debug.verboseOn()) Debug.logVerbose("Result: " + thisResult, module);
            
            // make sure we didn't fail
            if (((String) thisResult.get(ModelService.RESPONSE_MESSAGE)).equals(ModelService.RESPOND_ERROR)) {
                Debug.logError("Grouped service [" + model.getName() + "] failed.", module);
                return thisResult;
            }
            
            result.putAll(thisResult);
            if (model.resultToContext()) {
                runContext.putAll(thisResult);
                Debug.logVerbose("Added result(s) to context.", module);
            }
        }
        return result;
    }
    
    private Map runIndex(ServiceDispatcher dispatcher, String localName, Map context, int index) throws GenericServiceException {
        GroupServiceModel model = (GroupServiceModel) services.get(index);
        return model.invoke(dispatcher, localName, context);
    } 
    
    private Map runOne(ServiceDispatcher dispatcher, String localName, Map context) throws GenericServiceException {      
        Map result = null;        
        Iterator i = services.iterator();
        while (i.hasNext() && result != null) {
            GroupServiceModel model = (GroupServiceModel) i.next();
            try {
                result = model.invoke(dispatcher, localName, context);
            } catch (GenericServiceException e) {
                Debug.logError("Service: " + model + " failed.", module);
            }
        }
        if (result == null) {
            throw new GenericServiceException("All services failed to run; none available.");
        }
        return result;
    }            
}
