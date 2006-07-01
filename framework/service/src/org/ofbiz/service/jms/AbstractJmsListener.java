/*
 * $Id: AbstractJmsListener.java 5462 2005-08-05 18:35:48Z jonesde $
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
package org.ofbiz.service.jms;

import java.util.Map;

import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.ObjectType;
import org.ofbiz.entity.serialize.XmlSerializer;
import org.ofbiz.service.*;

/**
 * AbstractJmsListener
 *
 * @author     <a href="mailto:jaz@ofbiz.org">Andy Zeneski</a>
 * @version    $Rev$
 * @since      2.0
 */
public abstract class AbstractJmsListener implements GenericMessageListener, ExceptionListener {

    public static final String module = AbstractJmsListener.class.getName();

    protected LocalDispatcher dispatcher;
    protected boolean isConnected = false;

    /**
     * Initializes the LocalDispatcher for this service listener.
     * @param dispatcher
     */
    protected AbstractJmsListener(ServiceDispatcher dispatcher) {
        DispatchContext dctx = new DispatchContext("JMSDispatcher", null, this.getClass().getClassLoader(), null);
        this.dispatcher = new GenericDispatcher(dctx, dispatcher);
    }

    /**
     * Runs the service defined in the MapMessage
     * @param message
     * @return Map
     */
    protected Map runService(MapMessage message) {
        Map context = null;
        String serviceName = null;
        String xmlContext = null;

        try {
            serviceName = message.getString("serviceName");
            xmlContext = message.getString("serviceContext");
            if (serviceName == null || xmlContext == null) {
                Debug.logError("Message received is not an OFB service message. Ignored!", module);
                return null;
            }

            Object o = XmlSerializer.deserialize(xmlContext, dispatcher.getDelegator());

            if (Debug.verboseOn()) Debug.logVerbose("De-Serialized Context --> " + o, module);
            if (ObjectType.instanceOf(o, "java.util.Map"))
                context = (Map) o;
        } catch (JMSException je) {
            Debug.logError(je, "Problems reading message.", module);
        } catch (Exception e) {
            Debug.logError(e, "Problems deserializing the service context.", module);
        }

        try {
            ModelService model = dispatcher.getDispatchContext().getModelService(serviceName);
            if (!model.export) {
                Debug.logWarning("Attempt to invoke a non-exported service: " + serviceName, module);
                return null;
            }
        } catch (GenericServiceException e) {
            Debug.logError(e, "Unable to get ModelService for service : " + serviceName, module);
        }

        if (Debug.verboseOn()) Debug.logVerbose("Running service: " + serviceName, module);
        
        Map result = null;
        if (context != null) {
            try {
                result = dispatcher.runSync(serviceName, context);
            } catch (GenericServiceException gse) {
                Debug.logError(gse, "Problems with service invocation.", module);
            }
        }
        return result;
    }

    /**
     * Receives the MapMessage and processes the service. 
     * @see javax.jms.MessageListener#onMessage(Message)
     */
    public void onMessage(Message message) {
        MapMessage mapMessage = null;

        if (Debug.verboseOn()) Debug.logVerbose("JMS Message Received --> " + message, module);
        
        if (message instanceof MapMessage) {
            mapMessage = (MapMessage) message;
        } else {
            Debug.logError("Received message is not a MapMessage!", module);
            return;
        }
        runService(mapMessage);
    }

    /**
     * On exception try to re-establish connection to the JMS server.
     * @see javax.jms.ExceptionListener#onException(JMSException)
     */
    public void onException(JMSException je) {
        this.setConnected(false);
        Debug.logError(je, "JMS connection exception", module);
        while (!isConnected()) {
            try {
                this.refresh();
            } catch (GenericServiceException e) {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException ie) {}
                continue;
            }
        }
    }

    /**
     *
     * @see org.ofbiz.service.jms.GenericMessageListener#refresh()
     */
    public void refresh() throws GenericServiceException {
        this.close();
        this.load();
    }

    /**
     * 
     * @see org.ofbiz.service.jms.GenericMessageListener#isConnected()
     */
    public boolean isConnected() {
        return this.isConnected;
    }

    /**
     * Setter method for the connected field.
     * @param connected
     */
    protected void setConnected(boolean connected) {
        this.isConnected = connected;
    }

}
