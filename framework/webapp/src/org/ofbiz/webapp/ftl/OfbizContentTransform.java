/*
 * Copyright 2001-2006 The Apache Software Foundation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.ofbiz.webapp.ftl;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ofbiz.webapp.taglib.ContentUrlTag;

import freemarker.core.Environment;
import freemarker.ext.beans.BeanModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateTransformModel;

/**
 * OfbizContentTransform - Freemarker Transform for content links
 */
public class OfbizContentTransform implements TemplateTransformModel {
        
        public Writer getWriter(final Writer out, Map args) {              
            final StringBuffer buf = new StringBuffer();
            return new Writer(out) {
                public void write(char cbuf[], int off, int len) {
                    buf.append(cbuf, off, len);
                }

                public void flush() throws IOException {
                    out.flush();
                }

                public void close() throws IOException {  
                    try {                              
                        Environment env = Environment.getCurrentEnvironment();
                        BeanModel req = (BeanModel)env.getVariable("request");
                        BeanModel res = (BeanModel) env.getVariable("response");
                        HttpServletRequest request = (HttpServletRequest) req.getWrappedObject();
                        HttpServletResponse response = (HttpServletResponse) res.getWrappedObject();
                        ServletContext ctx = (ServletContext) request.getAttribute("servletContext");
                        
                        // make the link
                        StringBuffer newURL = new StringBuffer();
                        ContentUrlTag.appendContentPrefix(request, newURL);
                        newURL.append(buf.toString());                                        
                        out.write(newURL.toString());
                    } catch (TemplateModelException e) {
                        throw new IOException(e.getMessage());
                    }
                }
            };
        }
    }
