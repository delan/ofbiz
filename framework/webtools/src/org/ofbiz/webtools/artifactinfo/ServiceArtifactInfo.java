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
package org.ofbiz.webtools.artifactinfo;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javolution.util.FastList;
import javolution.util.FastMap;
import javolution.util.FastSet;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.GeneralException;
import org.ofbiz.base.util.UtilFormatOut;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.minilang.MiniLangException;
import org.ofbiz.minilang.SimpleMethod;
import org.ofbiz.minilang.method.callops.CallSimpleMethod;
import org.ofbiz.service.ModelParam;
import org.ofbiz.service.ModelService;
import org.ofbiz.service.eca.ServiceEcaRule;
import org.ofbiz.service.eca.ServiceEcaUtil;

/**
 *
 */
public class ServiceArtifactInfo {
    public static final String module = ServiceArtifactInfo.class.getName();
    
    protected ArtifactInfoFactory aif;
    protected ModelService modelService;
    protected String displayPrefix = null;
    
    Set<EntityArtifactInfo> entitiesUsedByThisService = FastSet.newInstance();
    Set<ServiceArtifactInfo> servicesCalledByThisService = FastSet.newInstance();
    Set<ServiceEcaArtifactInfo> serviceEcasTriggeredByThisService = FastSet.newInstance();
    
    public ServiceArtifactInfo(String serviceName, ArtifactInfoFactory aif) throws GeneralException {
        this.aif = aif;
        this.modelService = this.aif.getModelService(serviceName);
    }
    
    /**
     * This must be called after creation from the ArtifactInfoFactory after this class has been put into the global Map in order to avoid recursive initialization
     * 
     * @throws GeneralException
     */
    public void populateAll() throws GeneralException {
        this.populateUsedEntities();
        this.populateCalledServices();
        this.populateTriggeredServiceEcas();
    }
    
    protected void populateUsedEntities() throws GeneralException {
        // populate entitiesUsedByThisService and for each the reverse-associate cache in the aif
        if ("simple".equals(this.modelService.engineName)) {
            // we can do something with this!
            SimpleMethod simpleMethodToCall = null;
            try {
                Map<String, SimpleMethod> simpleMethods = SimpleMethod.getSimpleMethods(this.modelService.location, null);
                simpleMethodToCall = (SimpleMethod) simpleMethods.get(this.modelService.invoke);
            } catch (MiniLangException e) {
                Debug.logWarning("Error getting Simple-method [" + this.modelService.invoke + "] in [" + this.modelService.location + "] referenced in service [" + this.modelService.name + "]: " + e.toString(), module);
            }
            if (simpleMethodToCall == null) {
                Debug.logWarning("Simple-method [" + this.modelService.invoke + "] in [" + this.modelService.location + "] referenced in service [" + this.modelService.name + "] not found", module);
                return;
            }
            
            Set<String> allEntityNameSet = simpleMethodToCall.getAllEntityNamesUsed();
            for (String entityName: allEntityNameSet) {
                if (entityName.contains("${")) {
                    continue;
                }
                if (!aif.getEntityModelReader().getEntityNames().contains(entityName)) {
                    Debug.logWarning("Entity [" + entityName + "] reference in service [" + this.modelService.name + "] does not exist!", module);
                    continue;
                }
                
                // the forward reference
                this.entitiesUsedByThisService.add(aif.getEntityArtifactInfo(entityName));
                // the reverse reference
                UtilMisc.addToSetInMap(this, aif.allServiceInfosReferringToEntityName, entityName);
                
            }
        } else if ("java".equals(this.modelService.engineName)) {
            // TODO: can't do anything about this :( ...YET! :)
        }
    }
    
    protected void populateCalledServices() throws GeneralException {
        // populate servicesCalledByThisService and for each the reverse-associate cache in the aif
        if ("simple".equals(this.modelService.engineName)) {
            // we can do something with this!
            SimpleMethod simpleMethodToCall = null;
            try {
                Map<String, SimpleMethod> simpleMethods = SimpleMethod.getSimpleMethods(this.modelService.location, null);
                simpleMethodToCall = (SimpleMethod) simpleMethods.get(this.modelService.invoke);
            } catch (MiniLangException e) {
                Debug.logWarning("Error getting Simple-method [" + this.modelService.invoke + "] in [" + this.modelService.location + "] referenced in service [" + this.modelService.name + "]: " + e.toString(), module);
            }
            if (simpleMethodToCall == null) {
                Debug.logWarning("Simple-method [" + this.modelService.invoke + "] in [" + this.modelService.location + "] referenced in service [" + this.modelService.name + "] not found", module);
                return;
            }
            
            Set<String> allServiceNameSet = simpleMethodToCall.getAllServiceNamesCalled();
            for (String serviceName: allServiceNameSet) {
                if (serviceName.contains("${")) {
                    continue;
                }
                if (!aif.getDispatchContext().getAllServiceNames().contains(serviceName)) {
                    Debug.logWarning("Service [" + serviceName + "] reference in service [" + this.modelService.name + "] does not exist!", module);
                    continue;
                }
                
                // the forward reference
                this.servicesCalledByThisService.add(aif.getServiceArtifactInfo(serviceName));
                // the reverse reference
                UtilMisc.addToSetInMap(this, aif.allServiceInfosReferringToServiceName, serviceName);
            }
        } else if ("java".equals(this.modelService.engineName)) {
            // TODO: can't do anything about this :( ...YET! :)
        }
    }
    
    protected void populateTriggeredServiceEcas() throws GeneralException {
        // populate serviceEcasTriggeredByThisService and for each the reverse-associate cache in the aif
        Map<String, List<ServiceEcaRule>> serviceEventMap = ServiceEcaUtil.getServiceEventMap(this.modelService.name);
        if (serviceEventMap == null) return;
        for (List<ServiceEcaRule> ecaRuleList: serviceEventMap.values()) {
            for (ServiceEcaRule ecaRule: ecaRuleList) {
                this.serviceEcasTriggeredByThisService.add(aif.getServiceEcaArtifactInfo(ecaRule));
                // the reverse reference
                UtilMisc.addToSetInMap(this, aif.allServiceInfosReferringToServiceEcaRule, ecaRule);
            }
        }
    }
    
    public ModelService getModelService() {
        return this.modelService;
    }
    
    public void setDisplayPrefix(String displayPrefix) {
        this.displayPrefix = displayPrefix;
    }
    
    public String getDisplayPrefixedName() {
        return (this.displayPrefix != null ? this.displayPrefix : "") + this.modelService.name;
    }
    
    public Set<EntityArtifactInfo> getEntitiesUsedByService() {
        return this.entitiesUsedByThisService;
    }
    
    public Set<ServiceArtifactInfo> getServicesCallingService() {
        return aif.allServiceInfosReferringToServiceName.get(this.modelService.name);
    }
    
    public Set<ServiceArtifactInfo> getServicesCalledByService() {
        return this.servicesCalledByThisService;
    }
    
    public Set<ServiceArtifactInfo> getServicesCalledByServiceEcas() {
        Set<ServiceArtifactInfo> serviceList = FastSet.newInstance();
        // TODO: implement this
        return serviceList;
    }
    
    public Set<ServiceEcaArtifactInfo> getServiceEcaRulesTriggeredByService() {
        return this.serviceEcasTriggeredByThisService;
    }
    
    public Set<ServiceArtifactInfo> getServicesCallingServiceByEcas() {
        Set<ServiceArtifactInfo> serviceList = FastSet.newInstance();
        // TODO: implement this
        return serviceList;
    }
    
    public Set<ServiceEcaArtifactInfo> getServiceEcaRulesCallingService() {
        return aif.allServiceEcaInfosReferringToServiceName.get(this.modelService.name);
    }
    
    public Set<FormWidgetArtifactInfo> getFormsCallingService() {
        Set<FormWidgetArtifactInfo> formSet = FastSet.newInstance();
        // TODO: implement this
        return formSet;
    }
    
    public Set<FormWidgetArtifactInfo> getFormsBasedOnService() {
        Set<FormWidgetArtifactInfo> formSet = FastSet.newInstance();
        // TODO: implement this
        return formSet;
    }
    
    public Set<ScreenWidgetArtifactInfo> getScreensCallingService() {
        Set<ScreenWidgetArtifactInfo> screenSet = FastSet.newInstance();
        // TODO: implement this
        return screenSet;
    }
    
    public Set getRequestsWithEventCallingService() {
        Set requestSet = FastSet.newInstance();
        // TODO: implement this
        return requestSet;
    }
    
    public void writeServiceCallGraphEoModel(String eomodeldFullPath) throws GeneralException, FileNotFoundException, UnsupportedEncodingException {
        boolean useMoreDetailedNames = true;
        
        Debug.logInfo("Writing Service Call Graph EO Model for service [" + this.modelService.name + "] to [" + eomodeldFullPath + "]", module);
        
        Set<String> allDiagramEntitiesWithPrefixes = FastSet.newInstance();
        List<ServiceArtifactInfo> allServiceList = FastList.newInstance(); 
        List<ServiceEcaArtifactInfo> allServiceEcaList = FastList.newInstance();
        
        // all services that call this service
        Set<ServiceArtifactInfo> callingServiceList = this.getServicesCallingService();
        if (callingServiceList != null) {
            // set the prefix and add to the all list
            for (ServiceArtifactInfo callingService: callingServiceList) {
                callingService.setDisplayPrefix("Calling_");
                allDiagramEntitiesWithPrefixes.add(callingService.getDisplayPrefixedName());
                allServiceList.add(callingService);
            }
        }
        
        // all services this service calls
        Set<ServiceArtifactInfo> calledServiceList = this.getServicesCalledByService();
        
        for (ServiceArtifactInfo calledService: calledServiceList) {
            calledService.setDisplayPrefix("Called_");
            allDiagramEntitiesWithPrefixes.add(calledService.getDisplayPrefixedName());
            allServiceList.add(calledService);
        }
        
        // all SECAs and triggering services that call this service as an action
        Set<ServiceEcaArtifactInfo> callingServiceEcaSet = this.getServiceEcaRulesCallingService();
        if (callingServiceEcaSet != null) {
            for (ServiceEcaArtifactInfo callingServiceEca: callingServiceEcaSet) {
                callingServiceEca.setDisplayPrefix("Triggering_");
                allDiagramEntitiesWithPrefixes.add(callingServiceEca.getDisplayPrefixedName());
                allServiceEcaList.add(callingServiceEca);
            }
        }

        // all SECAs and corresponding services triggered by this service
        Set<ServiceEcaArtifactInfo> calledServiceEcaSet = this.getServiceEcaRulesTriggeredByService();
        
        for (ServiceEcaArtifactInfo calledServiceEca: calledServiceEcaSet) {
            calledServiceEca.setDisplayPrefix("Called_");
            allDiagramEntitiesWithPrefixes.add(calledServiceEca.getDisplayPrefixedName());
            allServiceEcaList.add(calledServiceEca);
        }

        // write index.eomodeld file
        Map<String, Object> indexEoModelMap = FastMap.newInstance();
        indexEoModelMap.put("EOModelVersion", "\"2.1\"");
        List<Map<String, Object>> entitiesMapList = FastList.newInstance();
        indexEoModelMap.put("entities", entitiesMapList);
        for (String entityName: allDiagramEntitiesWithPrefixes) {
            Map<String, Object> entitiesMap = FastMap.newInstance();
            entitiesMapList.add(entitiesMap);
            entitiesMap.put("className", "EOGenericRecord");
            entitiesMap.put("name", entityName);
        }
        UtilFormatOut.writePlistFile(indexEoModelMap, eomodeldFullPath, "index.eomodeld");
        
        // write this service description file
        Map<String, Object> thisServiceEoModelMap = createEoModelMap(allServiceList, allServiceEcaList, useMoreDetailedNames);
        UtilFormatOut.writePlistFile(thisServiceEoModelMap, eomodeldFullPath, this.getDisplayPrefixedName() + ".plist");

        // write service description files
        for (ServiceArtifactInfo callingService: callingServiceList) {
            Map<String, Object> serviceEoModelMap = callingService.createEoModelMap(UtilMisc.toList(this), null, useMoreDetailedNames);
            UtilFormatOut.writePlistFile(serviceEoModelMap, eomodeldFullPath, callingService.getDisplayPrefixedName() + ".plist");
        }
        for (ServiceArtifactInfo calledService: calledServiceList) {
            Map<String, Object> serviceEoModelMap = calledService.createEoModelMap(UtilMisc.toList(this), null, useMoreDetailedNames);
            UtilFormatOut.writePlistFile(serviceEoModelMap, eomodeldFullPath, calledService.getDisplayPrefixedName() + ".plist");
        }
        
        // write SECA description files
        if (callingServiceEcaSet != null) {
            for (ServiceEcaArtifactInfo callingServiceEca: callingServiceEcaSet) {
                // add List<ServiceArtifactInfo> for services that trigger this eca rule
                Set<ServiceArtifactInfo> ecaCallingServiceSet = callingServiceEca.getServicesTriggeringServiceEca();
                for (ServiceArtifactInfo ecaCallingService: ecaCallingServiceSet) {
                    ecaCallingService.setDisplayPrefix("Triggering:");
                }
                ecaCallingServiceSet.add(this);
                
                Map<String, Object> serviceEcaEoModelMap = callingServiceEca.createEoModelMap(ecaCallingServiceSet, useMoreDetailedNames);
                UtilFormatOut.writePlistFile(serviceEcaEoModelMap, eomodeldFullPath, callingServiceEca.getDisplayPrefixedName() + ".plist");
            }
        }
        for (ServiceEcaArtifactInfo calledServiceEca: calledServiceEcaSet) {
            // add List<ServiceArtifactInfo> for services this eca rule calls in action
            Set<ServiceArtifactInfo> ecaCalledServiceSet = calledServiceEca.getServicesCalledByServiceEcaActions();
            for (ServiceArtifactInfo ecaCalledService: ecaCalledServiceSet) {
                ecaCalledService.setDisplayPrefix("Called:");
            }
            ecaCalledServiceSet.add(this);
            
            Map<String, Object> serviceEcaEoModelMap = calledServiceEca.createEoModelMap(ecaCalledServiceSet, useMoreDetailedNames);
            UtilFormatOut.writePlistFile(serviceEcaEoModelMap, eomodeldFullPath, calledServiceEca.getDisplayPrefixedName() + ".plist");
        }
    }

    public Map<String, Object> createEoModelMap(List<ServiceArtifactInfo> relatedServiceList, List<ServiceEcaArtifactInfo> relatedServiceEcaList, boolean useMoreDetailedNames) {
        if (relatedServiceList == null) relatedServiceList = FastList.newInstance();
        if (relatedServiceEcaList == null) relatedServiceEcaList = FastList.newInstance();
        Map<String, Object> topLevelMap = FastMap.newInstance();

        topLevelMap.put("name", this.getDisplayPrefixedName());
        topLevelMap.put("className", "EOGenericRecord");

        // for classProperties add attribute names AND relationship names to get a nice, complete chart
        List<String> classPropertiesList = FastList.newInstance();
        topLevelMap.put("classProperties", classPropertiesList);
        for (ModelParam param: this.modelService.getModelParamList()) {
            if (useMoreDetailedNames) {
                classPropertiesList.add(param.getShortDisplayDescription());
            } else {
                classPropertiesList.add(param.name);
            }
        }
        for (ServiceArtifactInfo sai: relatedServiceList) {
            classPropertiesList.add(sai.getDisplayPrefixedName());
        }
        for (ServiceEcaArtifactInfo seai: relatedServiceEcaList) {
            classPropertiesList.add(seai.getDisplayPrefixedName());
        }
        
        // attributes
        List<Map<String, Object>> attributesList = FastList.newInstance();
        topLevelMap.put("attributes", attributesList);
        for (ModelParam param: this.modelService.getModelParamList()) {
            Map<String, Object> attributeMap = FastMap.newInstance();
            attributesList.add(attributeMap);
            
            if (useMoreDetailedNames) {
                attributeMap.put("name", param.getShortDisplayDescription());
            } else {
                attributeMap.put("name", param.name);
            }
            attributeMap.put("valueClassName", param.type);
            attributeMap.put("externalType", param.type);
        }
        
        // relationships
        List<Map<String, Object>> relationshipsMapList = FastList.newInstance();
        
        for (ServiceArtifactInfo sai: relatedServiceList) {
            Map<String, Object> relationshipMap = FastMap.newInstance();
            relationshipsMapList.add(relationshipMap);
            
            relationshipMap.put("name", sai.getDisplayPrefixedName());
            relationshipMap.put("destination", sai.getDisplayPrefixedName());
            
            // not sure if we can use these, or need them, for this type of diagram
            //relationshipMap.put("isToMany", "N");
            //relationshipMap.put("joinSemantic", "EOInnerJoin");
            //relationshipMap.put("joins", joinsMapList);
            //joinsMap.put("sourceAttribute", keyMap.getFieldName());
            //joinsMap.put("destinationAttribute", keyMap.getRelFieldName());
        }
        for (ServiceEcaArtifactInfo seai: relatedServiceEcaList) {
            Map<String, Object> relationshipMap = FastMap.newInstance();
            relationshipsMapList.add(relationshipMap);
            
            relationshipMap.put("name", seai.getDisplayPrefixedName());
            relationshipMap.put("destination", seai.getDisplayPrefixedName());
        }
        
        if (relationshipsMapList.size() > 0) {
            topLevelMap.put("relationships", relationshipsMapList);
        }
        
        return topLevelMap;
    }
    
    public boolean equals(Object obj) {
        if (obj instanceof ServiceArtifactInfo) {
            return this.modelService.name.equals(((ServiceArtifactInfo) obj).modelService.name);
        } else {
            return false;
        }
    }
}
