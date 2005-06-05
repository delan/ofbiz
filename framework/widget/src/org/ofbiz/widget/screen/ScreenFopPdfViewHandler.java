/*
 * $Id: FopPdfViewHandler.java 3103 2004-08-20 21:45:49Z jaz $
 *
 * Copyright (c) 2003-2005 The Open For Business Project - www.ofbiz.org
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
package org.ofbiz.widget.screen;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.avalon.framework.logger.Log4JLogger;
import org.apache.avalon.framework.logger.Logger;
import org.apache.fop.apps.Driver;
import org.apache.fop.image.FopImageFactory;
import org.apache.fop.messaging.MessageHandler;
import org.apache.fop.tools.DocumentInputSource;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilXml;
import org.ofbiz.webapp.view.ViewHandlerException;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 * Uses XSL-FO formatted templates to generate PDF views
 * This handler will use JPublish to generate the XSL-FO
 *
 * @author     <a href="mailto:jaz@ofbiz.org">Andy Zeneski</a>
 * @version    $Rev$
 * @since      3.0
 */
public class ScreenFopPdfViewHandler extends ScreenWidgetViewHandler {
    
    public static final String module = ScreenFopPdfViewHandler.class.getName();
    
    /**
     * @see org.ofbiz.content.webapp.view.ViewHandler#render(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public void render(String name, String page, String info, String contentType, String encoding, HttpServletRequest request, HttpServletResponse response) throws ViewHandlerException {
        // render and obtain the XSL-FO 
        Writer writer = new StringWriter();
        try {
            ScreenRenderer screens = new ScreenRenderer(writer, null, htmlScreenRenderer);
            screens.populateContextForRequest(request, response, servletContext);
            screens.render(page);
        } catch (Throwable t) {
            throw new ViewHandlerException("Problems with the response writer/output stream", t);
        }
        if (Debug.verboseOn()) {
           // Debug.logVerbose("XSL-FO : " + writer.toString(), module);
        }
        
        // configure logging for the FOP
        Logger logger = new Log4JLogger(Debug.getLogger(module));
        MessageHandler.setScreenLogger(logger);        
                          
        // load the FOP driver
        Driver driver = new Driver();
        driver.setRenderer(Driver.RENDER_PDF);
        driver.setLogger(logger);
                                        
        /*
        try {
            String buf = writer.toString();
            java.io.FileWriter fw = new java.io.FileWriter(new java.io.File("/tmp/xslfo.out"));
            fw.write(buf.toString());
            fw.close();
        } catch (IOException e) {
            throw new ViewHandlerException("Unable write to browser OutputStream", e);            
        }                             
        */
        // read the XSL-FO XML Document
        Document xslfo = null;
        try {
            xslfo = UtilXml.readXmlDocument(writer.toString());
        } catch (Throwable t) {
            throw new ViewHandlerException("Problems reading the parsed content to XML Document", t);
        }
        
        // create the output stream for the PDF
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        driver.setOutputStream(out);     
                
        // set the input source (XSL-FO) and generate the PDF        
        InputSource is = new DocumentInputSource(xslfo);               
        driver.setInputSource(is);        
        try {
            driver.run();
            FopImageFactory.resetCache();
        } catch (Throwable t) {
            throw new ViewHandlerException("Unable to generate PDF from XSL-FO", t);
        }
                  
        // set the content type and length                    
        response.setContentType("application/pdf");        
        response.setContentLength(out.size());
        
        // write to the browser
        try {
            out.writeTo(response.getOutputStream());
            response.getOutputStream().flush();
        } catch (IOException e) {
            throw new ViewHandlerException("Unable write to browser OutputStream", e);            
        }                             
    }
}
