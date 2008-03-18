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

import java.net.URL;
import java.util.Map;
import java.util.Set;

import javolution.util.FastSet;

import org.ofbiz.base.util.UtilObject;

/**
 *
 */
public class ControllerViewArtifactInfo extends ArtifactInfoBase {
    
    protected URL controllerXmlUrl;
    protected String viewUri;
    
    protected Map<String, String> viewInfoMap;
    
    protected Set<ScreenWidgetArtifactInfo> screensCalledByThisView = FastSet.newInstance();
    
    public ControllerViewArtifactInfo(URL controllerXmlUrl, String viewUri, ArtifactInfoFactory aif) {
        super(aif);
        this.controllerXmlUrl = controllerXmlUrl;
        this.viewUri = viewUri;
        
        this.viewInfoMap = aif.getControllerViewInfoMap(controllerXmlUrl, viewUri);
        
        // TODO populate screensCalledByThisView
    }
    
    public URL getControllerXmlUrl() {
        return this.controllerXmlUrl;
    }
    
    public String getViewUri() {
        return this.viewUri;
    }
    
    public String getUniqueId() {
        return this.controllerXmlUrl.toExternalForm() + "#" + this.viewUri;
    }
    
    public boolean equals(Object obj) {
        if (obj instanceof ControllerViewArtifactInfo) {
            ControllerViewArtifactInfo that = (ControllerViewArtifactInfo) obj;
            return UtilObject.equalsHelper(this.controllerXmlUrl, that.controllerXmlUrl) &&
                UtilObject.equalsHelper(this.viewUri, that.viewUri);
        } else {
            return false;
        }
    }
    
    public Set<ControllerRequestArtifactInfo> getRequestsThatThisViewIsResponseTo() {
        return this.aif.allRequestInfosReferringToView.get(this.getUniqueId());
    }
    
    public Set<ScreenWidgetArtifactInfo> getScreensCalledByThisView() {
        return screensCalledByThisView;
    }
}
