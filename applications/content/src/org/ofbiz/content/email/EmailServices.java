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
 *
 */
package org.ofbiz.content.email;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;
import java.util.Properties;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Calendar;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Message;
import javax.mail.Address;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.xml.parsers.ParserConfigurationException;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.apache.avalon.framework.logger.Log4JLogger;
import org.apache.avalon.framework.logger.Logger;
import org.apache.fop.apps.Driver;
import org.apache.fop.apps.FOPException;
import org.apache.fop.image.FopImageFactory;
import org.apache.fop.messaging.MessageHandler;
import org.apache.fop.tools.DocumentInputSource;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.GeneralException;
import org.ofbiz.base.util.HttpClient;
import org.ofbiz.base.util.HttpClientException;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilProperties;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.base.util.UtilXml;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.collections.MapStack;
import org.ofbiz.base.util.string.FlexibleStringExpander;
import org.ofbiz.entity.GenericDelegator;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;
import org.ofbiz.service.mail.MimeMessageWrapper;
import org.ofbiz.widget.html.HtmlScreenRenderer;
import org.ofbiz.widget.screen.ScreenRenderer;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Email Services
 *
 * @author     <a href="mailto:jaz@ofbiz.org">Andy Zeneski</a>
 * @author     <a href="mailto:jonesde@ofbiz.org">David E. Jones</a>
 * @author     <a href="mailto:h.bakker@antwebsystems.com">Hans Bakker</a>
 * @author     <a href="mailto:byersa@automationgroups.com">Al Byers</a>
 * @since      2.0
 */
public class EmailServices {

    public final static String module = EmailServices.class.getName();

    protected static final HtmlScreenRenderer htmlScreenRenderer = new HtmlScreenRenderer();

    /**
     * Basic JavaMail Service
     *@param ctx The DispatchContext that this service is operating in
     *@param context Map containing the input parameters
     *@return Map with the result of the service, the output parameters
     */
    public static Map sendMail(DispatchContext ctx, Map context) {
          Map results = ServiceUtil.returnSuccess();
        String subject = (String) context.get("subject");
        String partyId = (String) context.get("partyId");
        String body = (String) context.get("body");
        List bodyParts = (List) context.get("bodyParts");
        GenericValue userLogin = (GenericValue) context.get("userLogin");

        results.put("partyId", partyId);
        results.put("subject", subject);
        if (UtilValidate.isNotEmpty(body)) results.put("body", body);
        if (UtilValidate.isNotEmpty(bodyParts)) results.put("bodyParts", bodyParts);
        results.put("userLogin", userLogin);

        // first check to see if sending mail is enabled
        String mailEnabled = UtilProperties.getPropertyValue("general.properties", "mail.notifications.enabled", "N");
        if (!"Y".equalsIgnoreCase(mailEnabled)) {
            // no error; just return as if we already processed
            Debug.logImportant("Mail notifications disabled in general.properties; here is the context with info that would have been sent: " + context, module);
            return results;
        }
        String sendTo = (String) context.get("sendTo");
        String sendCc = (String) context.get("sendCc");
        String sendBcc = (String) context.get("sendBcc");

        // check to see if we should redirect all mail for testing
        String redirectAddress = UtilProperties.getPropertyValue("general.properties", "mail.notifications.redirectTo");
        if (UtilValidate.isNotEmpty(redirectAddress)) {
            String originalRecipients = " [To: " + sendTo + ", Cc: " + sendCc + ", Bcc: " + sendBcc + "]";
            subject = subject + originalRecipients;
            sendTo = redirectAddress;
            sendCc = null;
            sendBcc = null;
        }

        String sendFrom = (String) context.get("sendFrom");
        String sendType = (String) context.get("sendType");
        String sendVia = (String) context.get("sendVia");
        String authUser = (String) context.get("authUser");
        String authPass = (String) context.get("authPass");
        String contentType = (String) context.get("contentType");

        boolean useSmtpAuth = false;

        // define some default
        if (sendType == null || sendType.equals("mail.smtp.host")) {
            sendType = "mail.smtp.host";
            if (sendVia == null || sendVia.length() == 0) {
                sendVia = UtilProperties.getPropertyValue("general.properties", "mail.smtp.relay.host", "localhost");
            }
            if (authUser == null || authUser.length() == 0) {
                authUser = UtilProperties.getPropertyValue("general.properties", "mail.smtp.auth.user");
            }
            if (authPass == null || authPass.length() == 0) {
                authPass = UtilProperties.getPropertyValue("general.properties", "mail.smtp.auth.password");
            }
            if (authUser != null && authUser.length() > 0) {
                useSmtpAuth = true;
            }
        } else if (sendVia == null) {
            return ServiceUtil.returnError("Parameter sendVia is required when sendType is not mail.smtp.host");
        }


        if (contentType == null) {
            contentType = "text/html";
        }

        if (UtilValidate.isNotEmpty(bodyParts)) {
            contentType = "multipart/mixed";
        }
        results.put("contentType", contentType);

        try {
            Properties props = System.getProperties();
            props.put(sendType, sendVia);
            if (useSmtpAuth) {
                props.put("mail.smtp.auth", "true");
            }

            Session session = Session.getInstance(props);

            MimeMessage mail = new MimeMessage(session);
            mail.setFrom(new InternetAddress(sendFrom));
            mail.setSubject(subject);
            mail.addRecipients(Message.RecipientType.TO, sendTo);

            if (UtilValidate.isNotEmpty(sendCc)) {
                mail.addRecipients(Message.RecipientType.CC, sendCc);
            }
            if (UtilValidate.isNotEmpty(sendBcc)) {
                mail.addRecipients(Message.RecipientType.BCC, sendBcc);
            }

            if (UtilValidate.isNotEmpty(bodyParts)) {
                // check for multipart message (with attachments)
                // BodyParts contain a list of Maps items containing content(String) and type(String) of the attachement
                MimeMultipart mp = new MimeMultipart();
                Debug.logInfo(bodyParts.size() + " multiparts found",module);
                Iterator bodyPartIter = bodyParts.iterator();
                while (bodyPartIter.hasNext()) {
                    Map bodyPart = (Map) bodyPartIter.next();
                    Object bodyPartContent = bodyPart.get("content");
                    MimeBodyPart mbp = new MimeBodyPart();

                    if (bodyPartContent instanceof String) {
                        StringDataSource sdr = new StringDataSource((String) bodyPartContent, (String) bodyPart.get("type"));
                        Debug.logInfo("part of type: " + bodyPart.get("type") + " and size: " + bodyPart.get("content").toString().length() , module);
                        mbp.setDataHandler(new DataHandler(sdr));
                    } else if (bodyPartContent instanceof byte[]) {
                        ByteArrayDataSource bads = new ByteArrayDataSource((byte[]) bodyPartContent, (String) bodyPart.get("type"));
                        Debug.logInfo("part of type: " + bodyPart.get("type") + " and size: " + ((byte[]) bodyPartContent).length , module);
                        mbp.setDataHandler(new DataHandler(bads));
                    } else {
                        mbp.setDataHandler(new DataHandler(bodyPartContent, (String) bodyPart.get("type")));
                    }

                    String fileName = (String) bodyPart.get("filename");
                    if (fileName != null) {
                        mbp.setFileName(fileName);
                    }
                    mp.addBodyPart(mbp);
                }
                mail.setContent(mp);
                mail.saveChanges();
            } else {
                // create the singelpart message
                mail.setContent(body, contentType);
                mail.saveChanges();
            }

            Transport trans = session.getTransport("smtp");
            if (!useSmtpAuth) {
                trans.connect();
            } else {
                trans.connect(sendVia, authUser, authPass);
            }
            trans.sendMessage(mail, mail.getAllRecipients());
            trans.close();
        } catch (Exception e) {
            String errMsg = "Cannot send email message to [" + sendTo + "] from [" + sendFrom + "] cc [" + sendCc + "] bcc [" + sendBcc + "] subject [" + subject + "]";
            Debug.logError(e, errMsg, module);
            Debug.logError(e, "Email message that could not be sent to [" + sendTo + "] had context: " + context, module);
            return ServiceUtil.returnError(errMsg);
        }
        return results;
    }

    /**
     * JavaMail Service that gets body content from a URL
     *@param ctx The DispatchContext that this service is operating in
     *@param context Map containing the input parameters
     *@return Map with the result of the service, the output parameters
     */
    public static Map sendMailFromUrl(DispatchContext ctx, Map context) {
        // pretty simple, get the content and then call the sendMail method below
        String bodyUrl = (String) context.remove("bodyUrl");
        Map bodyUrlParameters = (Map) context.remove("bodyUrlParameters");

        URL url = null;

        try {
            url = new URL(bodyUrl);
        } catch (MalformedURLException e) {
            Debug.logWarning(e, module);
            return ServiceUtil.returnError("Malformed URL: " + bodyUrl + "; error was: " + e.toString());
        }

        HttpClient httpClient = new HttpClient(url, bodyUrlParameters);
        String body = null;

        try {
            body = httpClient.post();
        } catch (HttpClientException e) {
            Debug.logWarning(e, module);
            return ServiceUtil.returnError("Error getting content: " + e.toString());
        }

        context.put("body", body);
        Map result = sendMail(ctx, context);

        result.put("body", body);
        return result;
    }

    /**
     * JavaMail Service that gets body content from a Screen Widget
     * defined in the product store record and if available as attachment also.
     *@param dctx The DispatchContext that this service is operating in
     *@param serviceContext Map containing the input parameters
     *@return Map with the result of the service, the output parameters
     */
    public static Map sendMailFromScreen(DispatchContext dctx, Map serviceContext) {
        LocalDispatcher dispatcher = dctx.getDispatcher();
        Locale locale = (Locale) serviceContext.get("locale");
        String webSiteId = (String) serviceContext.remove("webSiteId");
        String bodyText = (String) serviceContext.remove("bodyText");
        String bodyScreenUri = (String) serviceContext.remove("bodyScreenUri");
        String xslfoAttachScreenLocation = (String) serviceContext.remove("xslfoAttachScreenLocation");
        Map bodyParameters = (Map) serviceContext.remove("bodyParameters");
        bodyParameters.put("locale", locale);
        String partyId = (String) bodyParameters.get("partyId");
        NotificationServices.setBaseUrl(dctx.getDelegator(), webSiteId, bodyParameters);

        StringWriter bodyWriter = new StringWriter();

        MapStack screenContext = MapStack.create();
        screenContext.put("locale", locale);
        ScreenRenderer screens = new ScreenRenderer(bodyWriter, screenContext, htmlScreenRenderer);
        screens.populateContextForService(dctx, bodyParameters);
        screenContext.putAll(bodyParameters);

        if (bodyScreenUri != null) {
            try {
                screens.render(bodyScreenUri);
            } catch (GeneralException e) {
                String errMsg = "Error rendering screen for email: " + e.toString();
                Debug.logError(e, errMsg, module);
                return ServiceUtil.returnError(errMsg);
            } catch (IOException e) {
                String errMsg = "Error rendering screen for email: " + e.toString();
                Debug.logError(e, errMsg, module);
                return ServiceUtil.returnError(errMsg);
            } catch (SAXException e) {
                String errMsg = "Error rendering screen for email: " + e.toString();
                Debug.logError(e, errMsg, module);
                return ServiceUtil.returnError(errMsg);
            } catch (ParserConfigurationException e) {
                String errMsg = "Error rendering screen for email: " + e.toString();
                Debug.logError(e, errMsg, module);
                return ServiceUtil.returnError(errMsg);
            }
        }
        
        boolean isMultiPart = false;
        
        // check if attachement screen location passed in
        if (UtilValidate.isNotEmpty(xslfoAttachScreenLocation)) {
            isMultiPart = true;
            // start processing fo pdf attachment
            try {
                Writer writer = new StringWriter();
                MapStack screenContextAtt = MapStack.create();
                // substitute the freemarker variables...
                ScreenRenderer screensAtt = new ScreenRenderer(writer, screenContext, htmlScreenRenderer);
                screensAtt.populateContextForService(dctx, bodyParameters);
                screenContextAtt.putAll(bodyParameters);
                screensAtt.render(xslfoAttachScreenLocation);
                
                /*
                try { // save generated fo file for debugging
                    String buf = writer.toString();
                    java.io.FileWriter fw = new java.io.FileWriter(new java.io.File("/tmp/file1.xml"));
                    fw.write(buf.toString());
                    fw.close();
                } catch (IOException e) {
                    Debug.logError(e, "Couldn't save xsl-fo xml debug file: " + e.toString(), module);
                }
                */
                
                // configure logging for the FOP
                Logger logger = new Log4JLogger(Debug.getLogger(module));
                MessageHandler.setScreenLogger(logger);        
                
                // load the FOP driver
                Driver driver = new Driver();
                driver.setRenderer(Driver.RENDER_PDF);
                driver.setLogger(logger);
                
                // read the XSL-FO XML into the W3 Document
                Document xslfo = UtilXml.readXmlDocument(writer.toString());

                // create the in/output stream for the generation
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                driver.setOutputStream(baos);     
                driver.setInputSource(new DocumentInputSource(xslfo));        
                
                // and generate the PDF
                driver.run();
                FopImageFactory.resetCache();
                baos.flush();
                baos.close();

                /*
                try {    // save generated pdf file for debugging
                    FileOutputStream fos = new FileOutputStream(new java.io.File("/tmp/file2.pdf"));
                    baos.writeTo(fos);
                    fos.close();
                } catch (IOException e) {
                    Debug.logError(e, "Couldn't save xsl-fo pdf debug file: " + e.toString(), module);
                }
                */

                // store in the list of maps for sendmail....
                List bodyParts = FastList.newInstance();
                if (bodyText != null) {
                    bodyText = FlexibleStringExpander.expandString(bodyText, screenContext,  locale);
                    bodyParts.add(UtilMisc.toMap("content", bodyText, "type", "text/html"));
                } else {
                    bodyParts.add(UtilMisc.toMap("content", bodyWriter.toString(), "type", "text/html"));
                }
                bodyParts.add(UtilMisc.toMap("content", baos.toByteArray(), "type", "application/pdf", "filename", "Details.pdf"));
                serviceContext.put("bodyParts", bodyParts);
            } catch (GeneralException ge) {
                String errMsg = "Error rendering PDF attachment for email: " + ge.toString();
                Debug.logError(ge, errMsg, module);
                return ServiceUtil.returnError(errMsg);
            } catch (IOException ie) {
                String errMsg = "Error rendering PDF attachment for email: " + ie.toString();
                Debug.logError(ie, errMsg, module);
                return ServiceUtil.returnError(errMsg);
            } catch (FOPException fe) {
                String errMsg = "Error rendering PDF attachment for email: " + fe.toString();
                Debug.logError(fe, errMsg, module);
                return ServiceUtil.returnError(errMsg);
            } catch (SAXException se) {
                String errMsg = "Error rendering PDF attachment for email: " + se.toString();
                Debug.logError(se, errMsg, module);
                return ServiceUtil.returnError(errMsg);
            } catch (ParserConfigurationException pe) {
                String errMsg = "Error rendering PDF attachment for email: " + pe.toString();
                Debug.logError(pe, errMsg, module);
                return ServiceUtil.returnError(errMsg);
            }
        } else {
            isMultiPart = false;
            // store body and type for single part message in the context.
            if (bodyText != null) {
                bodyText = FlexibleStringExpander.expandString(bodyText, screenContext,  locale);
                serviceContext.put("body", bodyText);
            } else {
                serviceContext.put("body", bodyWriter.toString());
            }
            serviceContext.put("contentType", "text/html");
        }
        
        // also expand the subject at this point, just in case it has the FlexibleStringExpander syntax in it...
        String subject = (String) serviceContext.remove("subject");
        subject = FlexibleStringExpander.expandString(subject, screenContext, locale);
        serviceContext.put("subject", subject);
        serviceContext.put("partyId", partyId);

        if (Debug.verboseOn()) Debug.logVerbose("sendMailFromScreen sendMail context: " + serviceContext, module);
        Map result = ServiceUtil.returnSuccess();
      
        try {
            if (isMultiPart) {
                dispatcher.runSync("sendMailMultiPart", serviceContext);
            } else {
                dispatcher.runSync("sendMail", serviceContext);
            }
        } catch (Exception e) {
            String errMsg = "Error send email :" + e.toString();
            Debug.logError(e, errMsg, module);
            return ServiceUtil.returnError(errMsg);
        } 
        result.put("body", bodyWriter.toString());
        return result;
    }
    
    /**
     * Store email as communication event
     *@param dctx The DispatchContext that this service is operating in
     *@param serviceContext Map containing the input parameters
     *@return Map with the result of the service, the output parameters
     */
    public static Map storeEmailAsCommunication(DispatchContext dctx, Map serviceContext) {
        LocalDispatcher dispatcher = dctx.getDispatcher();
        GenericValue userLogin = (GenericValue) serviceContext.get("userLogin");
        
        String subject = (String) serviceContext.get("subject");
        String body = (String) serviceContext.get("body");
        String partyId = (String) serviceContext.get("partyId");
        String communicationEventId = (String) serviceContext.get("communicationEventId");
        String contentType = (String) serviceContext.get("contentType");
        
        // only create a new communication event if the email is not already associated with one
        if (communicationEventId == null) {
            String partyIdFrom = (String) userLogin.get("partyId");
            Map commEventMap = FastMap.newInstance();
            commEventMap.put("communicationEventTypeId", "EMAIL_COMMUNICATION");
            commEventMap.put("statusId", "COM_COMPLETE");
            commEventMap.put("contactMechTypeId", "EMAIL_ADDRESS");
            commEventMap.put("partyIdFrom", partyIdFrom);
            commEventMap.put("partyIdTo", partyId);
            commEventMap.put("subject", subject);
            commEventMap.put("content", body);
            commEventMap.put("userLogin", userLogin);
            commEventMap.put("contentMimeTypeId", contentType);
            try {
                dispatcher.runSync("createCommunicationEvent", commEventMap);
            } catch (Exception e) {
                Debug.logError(e, "Cannot store email as communication event", module);
                return ServiceUtil.returnError("Cannot store email as communication event; see logs");
            }
        }
        
        return ServiceUtil.returnSuccess();
    }

    /** class to create a file in memory required for sending as an attachment */
    public static class StringDataSource implements DataSource {
        private String contentType;
        private ByteArrayOutputStream contentArray;
        
        public StringDataSource(String content, String contentType) throws IOException {
            this.contentType = contentType;
            contentArray = new ByteArrayOutputStream();
            contentArray.write(content.getBytes("iso-8859-1"));
            contentArray.flush();
            contentArray.close();
        }
        
        public String getContentType() {
            return contentType == null ? "application/octet-stream" : contentType;
        }
 
        public InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream(contentArray.toByteArray());
        }
 
        public String getName() {
            return "stringDatasource";
        }
 
        public OutputStream getOutputStream() throws IOException {
            throw new IOException("Cannot write to this read-only resource");
        }
    }

    /** class to create a file in memory required for sending as an attachment */
    public static class ByteArrayDataSource implements DataSource {
        private String contentType;
        private byte[] contentArray;
        
        public ByteArrayDataSource(byte[] content, String contentType) throws IOException {
            this.contentType = contentType;
            this.contentArray = content;
        }
        
        public String getContentType() {
            return contentType == null ? "application/octet-stream" : contentType;
        }
 
        public InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream(contentArray);
        }
 
        public String getName() {
            return "ByteArrayDataSource";
        }
 
        public OutputStream getOutputStream() throws IOException {
            throw new IOException("Cannot write to this read-only resource");
        }
    }
    
    /*
     * Helper method to retrieve the party information from the first email address of the Address[] specified.
     */
    private static Map getParyInfoFromEmailAddress(Address [] addresses, GenericValue userLogin, LocalDispatcher dispatcher) throws GenericServiceException
    {
    	InternetAddress emailAddress = null;
    	Map map = null;
    	Map result = null;
    	
    	if (addresses.length > 0) {
    		Address addr = addresses[0];
    		if (addr instanceof InternetAddress) {
    			emailAddress = (InternetAddress)addr;
    		}
    	}
    	
    	if (!UtilValidate.isEmpty(emailAddress)) {
    		map = new HashMap();
    		map.put("address", emailAddress.getAddress());
    		map.put("personal", emailAddress.getPersonal());
    		map.put("userLogin", userLogin);
    		result = dispatcher.runSync("findPartyFromEmailAddress", map);    		
    	}    	
    	
    	return result;
    }
    
    /*
     * Calls findPartyFromEmailAddress service and returns a List of the results for the array of addresses 
     */
    private static List buildListOfPartyInfoFromEmailAddresses(Address [] addresses, GenericValue userLogin, LocalDispatcher dispatcher) throws GenericServiceException
    {
        InternetAddress emailAddress = null;
        Address addr = null;
        Map map = null;
        Map result = null;
        List tempResults = new ArrayList();
        
        if (addresses != null) {
            for (int i = 0; i < addresses.length; i++) {
                addr = addresses[i];
                if (addr instanceof InternetAddress) {
                    emailAddress = (InternetAddress)addr;
                    
                    if (!UtilValidate.isEmpty(emailAddress)) {
                        map = new HashMap();
                        map.put("address", emailAddress.getAddress());
                        map.put("personal", emailAddress.getPersonal());
                        map.put("userLogin", userLogin);
                        result = dispatcher.runSync("findPartyFromEmailAddress", map);
                        
                        tempResults.add(result);
                    }
                }    
            }
        }
        
        return tempResults;
    }   
    
    /*
     * Helper method to retrieve a combined list of party information from to, cc, and bcc email addresses
     */
    private static List getListOfParyInfoFromEmailAddresses(Address [] addressesTo, Address [] addressesCC, Address [] addressesBCC, GenericValue userLogin, LocalDispatcher dispatcher) throws GenericServiceException
    {        
        List allResults = new ArrayList();
        
        //Get Party Info for To email addresses
        allResults.addAll(buildListOfPartyInfoFromEmailAddresses(addressesTo, userLogin, dispatcher));        
        
        //Get Party Info for CC email addresses
        allResults.addAll(buildListOfPartyInfoFromEmailAddresses(addressesCC, userLogin, dispatcher));
        
        //Get Party Info for BCC email addresses
        allResults.addAll(buildListOfPartyInfoFromEmailAddresses(addressesBCC, userLogin, dispatcher));
        
        return allResults;
    }
    
    /**
     * This service is the main one for processing incoming emails.
     * 
     * Its only argument is a wrapper for the JavaMail MimeMessage object. 
     * From this object, all the fields, headers and content of the message can be accessed.
     * 
     * The first thing this service does is try to discover the partyId of the message sender
     * by doing a reverse find on the email address. It uses the findPartyFromEmailAddress service to do this.
     * 
     * It then creates a CommunicationEvent entity by calling the createCommunicationEvent service using the appropriate fields from the email and the
     * discovered partyId, if it exists, as the partyIdFrom. Note that it sets the communicationEventTypeId
     * field to AUTO_EMAIL_COMM. This is useful for tracking email generated communications.
     * 
     * The service tries to find appropriate content for inclusion in the CommunicationEvent.content field. 
     * If the contentType of the content starts with "text", the getContent() call returns a string and it is used.
     * If the contentType starts with "multipart", then the "parts" of the content are iterated thru and the first
     * one of mime type, "text/..." is used.
     * 
     * If the contentType has a value of "multipart" then the parts of the content (except the one used in the main 
     * CommunicationEvent.content field) are cycled thru and attached to the CommunicationEvent entity using the 
     * createCommContentDataResource service. This happens in the EmailWorker.addAttachmentsToCommEvent method.
     * 
     * -Al Byers
     * @param dctx
     * @param context
     * @return
     */
    public static Map storeIncomingEmail(DispatchContext dctx, Map context) {
    	
        GenericDelegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        MimeMessageWrapper wrapper = (MimeMessageWrapper) context.get("messageWrapper");
        MimeMessage message = wrapper.getMessage();
        Timestamp nowTimestamp = UtilDateTime.nowTimestamp();
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        String partyIdTo = null;
        String partyIdFrom = null;
        String contentType = null;
        String content = null;
		String communicationEventId = null;
        String contactMechIdFrom = null;
        String contactMechIdTo = null;
        
        Map result = null;
        try {
            String contentTypeRaw = message.getContentType();
            int idx = contentTypeRaw.indexOf(";");
            contentType = contentTypeRaw.substring(0, idx);
            Address [] addressesFrom = message.getFrom();
            Address [] addressesTo = message.getRecipients(MimeMessage.RecipientType.TO);
            Address [] addressesCC = message.getRecipients(MimeMessage.RecipientType.CC);
            Address [] addressesBCC = message.getRecipients(MimeMessage.RecipientType.BCC);            
        	        	
            result = getParyInfoFromEmailAddress(addressesFrom, userLogin, dispatcher);
            partyIdFrom = (String)result.get("partyId");
            contactMechIdFrom = (String)result.get("contactMechId");
            
            List allResults = getListOfParyInfoFromEmailAddresses(addressesTo, addressesCC, addressesBCC, userLogin, dispatcher);
            Iterator itr = allResults.iterator();
            
            //Get the first address from the list - this is the partyIdTo field of the CommunicationEvent
            if ((allResults != null) && (allResults.size() > 0)) {
                Map firstAddressTo = (Map) itr.next();
                
                partyIdTo = (String)firstAddressTo.get("partyId");
                contactMechIdTo = (String)firstAddressTo.get("contactMechId");
            }           
            
            Map commEventMap = new HashMap();
    	    commEventMap.put("communicationEventTypeId", "AUTO_EMAIL_COMM");
    	    commEventMap.put("contactMechTypeId", "EMAIL_ADDRESS");
    	    commEventMap.put("partyIdTo", partyIdTo);
    	    String subject = message.getSubject();
    	    commEventMap.put("subject", subject);
    		
	        commEventMap.put("entryDate", nowTimestamp);
	    	
	        //Set sent and received dates
	        commEventMap.put("datetimeStarted", UtilDateTime.toTimestamp(message.getSentDate()));
	        commEventMap.put("datetimeEnded", UtilDateTime.toTimestamp(message.getReceivedDate()));

    		int contentIndex = -1;
			Multipart multipart = null;
    		if (contentType.startsWith("text")) {
    			content = (String)message.getContent();
        		commEventMap.put("contentMimeTypeId", contentType);
    		} else if (contentType.startsWith("multipart")) {
    			multipart = (Multipart)message.getContent();
    			int multipartCount = multipart.getCount();
    			for (int i=0; i < multipartCount; i++) {
    				Part part = multipart.getBodyPart(i);
    				String thisContentTypeRaw = part.getContentType();
    	            int idx2 = thisContentTypeRaw.indexOf(";");
    	            String thisContentType = thisContentTypeRaw.substring(0, idx2);
    				String disposition = part.getDisposition();
    				
    				// See this case where the disposition of the inline text is null
    				if ((disposition == null) && (i == 0) && thisContentType.startsWith("text")) 
    				{
    					content = (String)part.getContent();
    					if (UtilValidate.isNotEmpty(content)) {
    						contentIndex = i;
    						commEventMap.put("contentMimeTypeId", thisContentType);
    						break;
    					}
   			    	} else if ((disposition != null)
   						 && (disposition.equals(Part.ATTACHMENT) || disposition.equals(Part.INLINE))
   					     && thisContentType.startsWith("text")) 
   			    	{
   			    		content = (String)part.getContent();
   			    		contentIndex = i;
   			    		commEventMap.put("contentMimeTypeId", thisContentType);
   			    		break;
   			    	}
    			}    			
    		}
    		commEventMap.put("content", content);
    		
            // store a note when the to/from emails are not associated with any parties in the system
            String commNote = "";
            if (partyIdFrom != null) {
        		commEventMap.put("partyIdFrom", partyIdFrom);        		
        		commEventMap.put("contactMechIdFrom", contactMechIdFrom);
        		commEventMap.put("contactMechIdTo", contactMechIdTo);
        		commEventMap.put("statusId", "COM_ENTERED");
        	} else {
                // create a task to find party for email
        		commEventMap.put("statusId", "COM_UNKNOWN_PARTY");
        		commNote = "Sent from: " + UtilMisc.toListArray(addressesFrom);
        	}
    		if (partyIdTo == null) {
                commNote += "Sent to: " + UtilMisc.toListArray(addressesTo);
            }
            if (!("".equals(commNote))) {
                commEventMap.put("note", commNote);
            }
            
    		commEventMap.put("userLogin", userLogin);
    		result = dispatcher.runSync("createCommunicationEvent", commEventMap);
    		communicationEventId = (String)result.get("communicationEventId");
    		if (contentType.startsWith("multipart")) {
    			int attachmentCount = EmailWorker.addAttachmentsToCommEvent(message, communicationEventId, contentIndex, dispatcher, userLogin);
    			if (Debug.infoOn()) Debug.logInfo(attachmentCount + " attachments added to CommunicationEvent:" + communicationEventId,module);
    		}
            
            //For all other addresses create a CommunicationEventRole
            while (itr.hasNext()) {
                Map address = (Map) itr.next();
                String partyId = (String)address.get("partyId");
                
                // It's not clear what the "role" of this communication event should be, so we'll just put _NA_
                //Check if "_NA_" role exists for the partyId. If not, then first associate that role with the partyId
                GenericValue partyRole = delegator.findByPrimaryKey("PartyRole", UtilMisc.toMap("partyId", partyId, "roleTypeId", "_NA_"));
                if (partyRole == null) {
                    dispatcher.runSync("createPartyRole", UtilMisc.toMap("partyId", partyId, "roleTypeId", "_NA_", "userLogin", userLogin));
                }
                Map input = UtilMisc.toMap("communicationEventId", communicationEventId, "partyId", partyId, "roleTypeId", "_NA_", "userLogin", userLogin, "contactMechId", (String)address.get("contactMechId"));
                dispatcher.runSync("createCommunicationEventRole", input);
            }
    		
    		Map results = ServiceUtil.returnSuccess();
            results.put("communicationEventId", communicationEventId);
    		return results;
        } catch (MessagingException e) {
            Debug.logError(e, module);
            return ServiceUtil.returnError(e.getMessage());
        } catch (GenericServiceException e) {
            Debug.logError(e, module);
            return ServiceUtil.returnError(e.getMessage());
        } catch (IOException e) {
            Debug.logError(e, module);
            return ServiceUtil.returnError(e.getMessage());
        } catch (Exception e) {
            Debug.logError(e, module);
            return ServiceUtil.returnError(e.getMessage());
        }
    }
}
