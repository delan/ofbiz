/*
 * $Id: WrapSubContentCacheTransform.java 4256 2005-01-14 03:19:50Z jonesde $
 *
 *  Copyright (c) 2001-2004 The Open For Business Project - www.ofbiz.org
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
package org.ofbiz.content.webapp.ftl;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.GeneralException;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.base.util.template.FreeMarkerWorker;
import org.ofbiz.content.content.ContentWorker;
import org.ofbiz.entity.GenericDelegator;
import org.ofbiz.entity.GenericValue;

import freemarker.core.Environment;
import freemarker.template.TemplateTransformModel;

//import com.clarkware.profiler.Profiler;
/**
 * WrapSubContentCacheTransform - Freemarker Transform for URLs (links)
 * 
 * This is an interactive FreeMarker tranform that allows the user to modify the contents that are placed within it.
 * 
 * @author <a href="mailto:byersa@automationgroups.com">Al Byers</a>
 * @version $Rev$
 * @since 3.0
 */
public class WrapSubContentCacheTransform implements TemplateTransformModel {

    public static final String module = WrapSubContentCacheTransform.class.getName();
    public static final String [] upSaveKeyNames = {"globalNodeTrail"};
    public static final String [] saveKeyNames = {"contentId", "subContentId", "subDataResourceTypeId", "mimeTypeId", "whenMap", "locale",  "wrapTemplateId", "encloseWrapText", "nullThruDatesOnly"};
    
    /**
     * A wrapper for the FreeMarkerWorker version.
     */
    public static Object getWrappedObject(String varName, Environment env) {
        return FreeMarkerWorker.getWrappedObject(varName, env);
    }

    public static String getArg(Map args, String key, Environment env) {
        return FreeMarkerWorker.getArg(args, key, env);
    }

    public static String getArg(Map args, String key, Map ctx) {
        return FreeMarkerWorker.getArg(args, key, ctx);
    }

    public Writer getWriter(final Writer out, Map args) {
        final StringBuffer buf = new StringBuffer();
        final Environment env = Environment.getCurrentEnvironment();
        Map envContext = (Map) FreeMarkerWorker.getWrappedObject("context", env);
        final Map templateCtx;
        if (envContext == null) {
            templateCtx = FreeMarkerWorker.createEnvironmentMap(env);
        } else {
            templateCtx = envContext;
        }
        final GenericDelegator delegator = (GenericDelegator) FreeMarkerWorker.getWrappedObject("delegator", env);
        final HttpServletRequest request = (HttpServletRequest) FreeMarkerWorker.getWrappedObject("request", env);
        FreeMarkerWorker.getSiteParameters(request, templateCtx);
        final Map savedValuesUp = new HashMap();
        FreeMarkerWorker.saveContextValues(templateCtx, upSaveKeyNames, savedValuesUp);
        //if (Debug.infoOn()) Debug.logInfo("in Wrap(0a), savedValuesUp ." + savedValuesUp , module);
        FreeMarkerWorker.overrideWithArgs(templateCtx, args);
        //if (Debug.infoOn()) Debug.logInfo("in Wrap(0b), savedValuesUp ." + savedValuesUp , module);
        final String wrapTemplateId = (String)templateCtx.get("wrapTemplateId");
        //if (Debug.infoOn()) Debug.logInfo("in WrapSubContent, wrapTemplateId(1):" + wrapTemplateId, module);
        final GenericValue userLogin = (GenericValue) FreeMarkerWorker.getWrappedObject("userLogin", env);
        List trail = (List)templateCtx.get("globalNodeTrail");
                //if (Debug.infoOn()) Debug.logInfo("in WrapSubContent, trail(0):" + trail, "");
        String contentAssocPredicateId = (String)templateCtx.get("contentAssocPredicateId");
        String strNullThruDatesOnly = (String)templateCtx.get("nullThruDatesOnly");
        Boolean nullThruDatesOnly = (strNullThruDatesOnly != null && strNullThruDatesOnly.equalsIgnoreCase("true")) ? new Boolean(true) :new Boolean(false);
        GenericValue val = null;
        try {
            val = ContentWorker.getCurrentContent(delegator, trail, userLogin, templateCtx, nullThruDatesOnly, contentAssocPredicateId);
        } catch(GeneralException e) {
            throw new RuntimeException("Error getting current content. " + e.toString());
        }
                //if (Debug.infoOn()) Debug.logInfo("in WrapSubContent, trail(1):" + trail, "");
        final GenericValue view = val;

        String dataResourceId = null;
        try {
            dataResourceId = (String) view.get("drDataResourceId");
        } catch (Exception e) {
            dataResourceId = (String) view.get("dataResourceId");
        }
        String subContentIdSub = (String) view.get("contentId");
        // This order is taken so that the dataResourceType can be overridden in the transform arguments.
        String subDataResourceTypeId = (String)templateCtx.get("subDataResourceTypeId");
        if (UtilValidate.isEmpty(subDataResourceTypeId)) {
            try {
                subDataResourceTypeId = (String) view.get("drDataResourceTypeId");
            } catch (Exception e) {
                // view may be "Content"
            }
            // TODO: If this value is still empty then it is probably necessary to get a value from
            // the parent context. But it will already have one and it is the same context that is
            // being passed.
        }
        final Map savedValues = new HashMap();
        FreeMarkerWorker.saveContextValues(templateCtx, saveKeyNames, savedValues);
        //if (Debug.infoOn()) Debug.logInfo("in Wrap(1), savedValues ." + savedValues , module);
        // This order is taken so that the mimeType can be overridden in the transform arguments.
        String mimeTypeId = ContentWorker.getMimeTypeId(delegator, view, templateCtx);
        templateCtx.put("drDataResourceId", dataResourceId);
        templateCtx.put("mimeTypeId", mimeTypeId);
        templateCtx.put("dataResourceId", dataResourceId);
        templateCtx.put("subContentIdSub", subContentIdSub);
        templateCtx.put("subDataResourceTypeId", subDataResourceTypeId);
        templateCtx.put("wrapTemplateId", null); // Not something to pass on

        return new Writer(out) {

            public void write(char cbuf[], int off, int len) {
                buf.append(cbuf, off, len);
            }

            public void flush() throws IOException {
                out.flush();
            }

            public void close() throws IOException {
                FreeMarkerWorker.reloadValues(templateCtx, savedValues, env);
                //if (Debug.infoOn()) Debug.logInfo("in Wrap(2), savedValues ." + savedValues , module);
                String wrappedContent = buf.toString();
                
                //if (view != null && Debug.infoOn()) Debug.logInfo("in WrapSubContent, view(2):" + view.get("contentId"), module);
                //if (Debug.infoOn()) Debug.logInfo("in WrapSubContent, wrappedContent:" + wrappedContent, module);
                //if (Debug.infoOn()) Debug.logInfo("in WrapSubContent, wrapTemplateId(2):" + wrapTemplateId, module);
                if (UtilValidate.isNotEmpty(wrapTemplateId)) {
                    templateCtx.put("wrappedContent", wrappedContent);
                    
                    //Map templateRoot = FreeMarkerWorker.createEnvironmentMap(env);
                    Map templateRoot = null;
                    Map templateRootTemplate = (Map)templateCtx.get("templateRootTemplate");
                    if (templateRootTemplate == null) {
                        Map templateRootTmp = FreeMarkerWorker.createEnvironmentMap(env);
                        templateRoot = new HashMap(templateRootTmp);
                        templateCtx.put("templateRootTemplate", templateRootTmp);
                    } else {
                        templateRoot = new HashMap(templateRootTemplate);
                    }
                    
                    templateRoot.put("context", templateCtx);
                    
                    String mimeTypeId = (String)templateCtx.get("mimeTypeId");
                    Locale locale = null;
                    try {
                        //if (Debug.infoOn()) Debug.logInfo("in Edit(0), before calling renderContentAsText ." , module);
                        ContentWorker.renderContentAsTextCache(delegator, wrapTemplateId, out, templateRoot, null, locale, mimeTypeId);
                        //if (Debug.infoOn()) Debug.logInfo("in Edit(0), after calling renderContentAsText ." , module);
                    } catch (IOException e) {
                        Debug.logError(e, "Error rendering content" + e.getMessage(), module);
                        throw new IOException("Error rendering content" + e.toString());
                    } catch (GeneralException e2) {
                        Debug.logError(e2, "Error rendering content" + e2.getMessage(), module);
                        throw new IOException("Error rendering content" + e2.toString());
                    }
                        
                FreeMarkerWorker.reloadValues(templateCtx, savedValuesUp, env);
                        //if (Debug.infoOn()) Debug.logInfo("in Wrap(2), savedValuesUp ." + savedValuesUp , module);
                }
            }
        };
    }
}
