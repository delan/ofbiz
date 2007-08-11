/*******************************************************************************
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
 *******************************************************************************/
package org.ofbiz.webapp.event;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilHttp;
import org.ofbiz.base.util.UtilProperties;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.transaction.GenericTransactionException;
import org.ofbiz.entity.transaction.TransactionUtil;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ModelParam;
import org.ofbiz.service.ModelService;
import org.ofbiz.service.ServiceAuthException;
import org.ofbiz.service.ServiceUtil;
import org.ofbiz.service.ServiceValidationException;
import org.ofbiz.webapp.control.RequestHandler;
import org.ofbiz.webapp.control.RequestManager;

/**
 * ServiceMultiEventHandler - Event handler for running a service multiple times; for bulk forms
 */
public class ServiceMultiEventHandler implements EventHandler {

    public static final String module = ServiceMultiEventHandler.class.getName();

    public static final String SYNC = "sync";
    public static final String ASYNC = "async";
    private RequestManager requestManager;

    /**
     * @see org.ofbiz.webapp.event.EventHandler#init(javax.servlet.ServletContext)
     */
    public void init(ServletContext context) throws EventHandlerException {

        // Provide a means to check the controller for event tag attributes at execution time
        this.requestManager = new RequestManager(context);
    }

    /**
     * @see org.ofbiz.webapp.event.EventHandler#invoke(java.lang.String, java.lang.String, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public String invoke(String eventPath, String eventMethod, HttpServletRequest request, HttpServletResponse response) throws EventHandlerException {
        // TODO: consider changing this to use the new UtilHttp.parseMultiFormData method
        
        // make sure we have a valid reference to the Service Engine
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        if (dispatcher == null) {
            throw new EventHandlerException("The local service dispatcher is null");
        }
        DispatchContext dctx = dispatcher.getDispatchContext();
        if (dctx == null) {
            throw new EventHandlerException("Dispatch context cannot be found");
        }

        // get the details for the service(s) to call
        String mode = SYNC;
        String serviceName = null;

        if (eventPath == null || eventPath.length() == 0) {
            mode = SYNC;
        } else {
            mode = eventPath;
        }

        // we only support SYNC mode in this handler
        if (!SYNC.equals(mode)) {
            throw new EventHandlerException("Async mode is not supported");
        }

        // nake sure we have a defined service to call
        serviceName = eventMethod;
        if (serviceName == null) {
            throw new EventHandlerException("Service name (eventMethod) cannot be null");
        }
        if (Debug.verboseOn()) Debug.logVerbose("[Set mode/service]: " + mode + "/" + serviceName, module);

        // some needed info for when running the service
        Locale locale = UtilHttp.getLocale(request);
        TimeZone timeZone = UtilHttp.getTimeZone(request);
        HttpSession session = request.getSession();
        GenericValue userLogin = (GenericValue) session.getAttribute("userLogin");

        // get the service model to generate context(s)
        ModelService modelService = null;

        try {
            modelService = dctx.getModelService(serviceName);
        } catch (GenericServiceException e) {
            throw new EventHandlerException("Problems getting the service model", e);
        }

        if (modelService == null) {
            throw new EventHandlerException("Problems getting the service model");
        }

        if (Debug.verboseOn()) Debug.logVerbose("[Processing]: SERVICE Event", module);
        if (Debug.verboseOn()) Debug.logVerbose("[Using delegator]: " + dispatcher.getDelegator().getDelegatorName(), module);

        // check if we are using per row submit
        boolean useRowSubmit = request.getParameter("_useRowSubmit") == null ? false :
                "Y".equalsIgnoreCase(request.getParameter("_useRowSubmit"));

        // check if we are to also look in a global scope (no delimiter)
        boolean checkGlobalScope = request.getParameter("_checkGlobalScope") == null ? true :
                !"N".equalsIgnoreCase(request.getParameter("_checkGlobalScope"));

        // get the number of rows
        String rowCountField = request.getParameter("_rowCount");
        if (rowCountField == null) {
            throw new EventHandlerException("Required field _rowCount is missing");
        }

        int rowCount = 0; // parsed int value
        try {
            rowCount = Integer.parseInt(rowCountField);
        } catch (NumberFormatException e) {
            throw new EventHandlerException("Invalid value for _rowCount");
        }
        if (rowCount < 1) {
            throw new EventHandlerException("No rows to process");
        }

        // some default message settings
        String errorPrefixStr = UtilProperties.getMessage("DefaultMessages", "service.error.prefix", locale);
        String errorSuffixStr = UtilProperties.getMessage("DefaultMessages", "service.error.suffix", locale);
        String messagePrefixStr = UtilProperties.getMessage("DefaultMessages", "service.message.prefix", locale);
        String messageSuffixStr = UtilProperties.getMessage("DefaultMessages", "service.message.suffix", locale);

        // prepare the error message and success message lists
        List errorMessages = FastList.newInstance();
        List successMessages = FastList.newInstance();

        // Check the global-transaction attribute of the event from the controller to see if the
        //  event should be wrapped in a transaction
        String requestUri = RequestHandler.getRequestUri(request.getPathInfo());
        boolean eventGlobalTransaction = requestManager.getEventGlobalTransaction(requestUri);

        // big try/finally to make sure commit or rollback are run
        boolean beganTrans = false;
        String returnString = null;
        try {

            if (eventGlobalTransaction) {
                // start the global transaction
                try {
                    beganTrans = TransactionUtil.begin(modelService.transactionTimeout * rowCount);
                } catch (GenericTransactionException e) {
                    throw new EventHandlerException("Problem starting multi-service global transaction", e);
                }
            }

            // now loop throw the rows and prepare/invoke the service for each
            for (int i = 0; i < rowCount; i++) {
                String curSuffix = UtilHttp.MULTI_ROW_DELIMITER + i;
                boolean rowSelected = request.getParameter(UtilHttp.ROW_SUBMIT_PREFIX + i) == null ? false :
                        "Y".equalsIgnoreCase(request.getParameter(UtilHttp.ROW_SUBMIT_PREFIX + i));

                // make sure we are to process this row
                if (useRowSubmit && !rowSelected) {
                    continue;
                }

                // build the context
                Map serviceContext = FastMap.newInstance();
                List modelParmInList = modelService.getInModelParamList();
                Iterator modelParmInIter = modelParmInList.iterator();
                while (modelParmInIter.hasNext()) {
                    ModelParam modelParam = (ModelParam) modelParmInIter.next();
                    String paramName = modelParam.name;
                    
                    // Debug.logInfo("In ServiceMultiEventHandler processing input parameter [" + modelParam.name + (modelParam.optional?"(optional):":"(required):") + modelParam.mode + "] for service [" + serviceName + "]", module);

                    // don't include userLogin, that's taken care of below
                    if ("userLogin".equals(paramName)) continue;
                    // don't include locale, that is also taken care of below
                    if ("locale".equals(paramName)) continue;
                    // don't include timeZone, that is also taken care of below
                    if ("timeZone".equals(paramName)) continue;

                    Object value = null;
                    if (modelParam.stringMapPrefix != null && modelParam.stringMapPrefix.length() > 0) {
                        Map paramMap = UtilHttp.makeParamMapWithPrefix(request, modelParam.stringMapPrefix, curSuffix);
                        value = paramMap;
                    } else if (modelParam.stringListSuffix != null && modelParam.stringListSuffix.length() > 0) {
                        List paramList = UtilHttp.makeParamListWithSuffix(request, modelParam.stringListSuffix, null);
                        value = paramList;
                    } else {
                        // check attributes; do this before parameters so that attribute which can be changed by code can override parameters which can't
                        value = request.getAttribute(paramName + curSuffix);

                        // first check for request parameters
                        if (value == null) {
                            String[] paramArr = request.getParameterValues(paramName + curSuffix);
                            if (paramArr != null) {
                                if (paramArr.length > 1) {
                                    value = Arrays.asList(paramArr);
                                } else {
                                    value = paramArr[0];
                                }
                            }
                        }

                        // if the parameter wasn't passed and no other value found, check the session
                        if (value == null) {
                            value = session.getAttribute(paramName + curSuffix);
                        }

                        // now check global scope
                        if (value == null) {
                            if (checkGlobalScope) {
                                String[] gParamArr = request.getParameterValues(paramName);
                                if (gParamArr != null) {
                                    if (gParamArr.length > 1) {
                                        value = Arrays.asList(gParamArr);
                                    } else {
                                        value = gParamArr[0];
                                    }
                                }                            
                                if (value == null) {
                                    value = request.getAttribute(paramName);
                                }
                                if (value == null) {
                                    value = session.getAttribute(paramName);
                                }
                            }
                        }

                        if (value == null) {
                            // still null, give up for this one
                            continue;
                        }

                        if (value instanceof String && ((String) value).length() == 0) {
                            // interpreting empty fields as null values for each in back end handling...
                            value = null;
                        }
                    }
                    // set even if null so that values will get nulled in the db later on
                    serviceContext.put(paramName, value);

                    // Debug.logInfo("In ServiceMultiEventHandler got value [" + value + "] for input parameter [" + paramName + "] for service [" + serviceName + "]", module);
                }

                // get only the parameters for this service - converted to proper type
                serviceContext = modelService.makeValid(serviceContext, ModelService.IN_PARAM, true, null, timeZone, locale);

                // include the UserLogin value object
                if (userLogin != null) {
                    serviceContext.put("userLogin", userLogin);
                }

                // include the Locale object
                if (locale != null) {
                    serviceContext.put("locale", locale);
                }

                // include the TimeZone object
                if (timeZone != null) {
                    serviceContext.put("timeZone", timeZone);
                }

                // Debug.logInfo("ready to call " + serviceName + " with context " + serviceContext, module);

                // invoke the service
                Map result = null;
                try {
                    result = dispatcher.runSync(serviceName, serviceContext);
                } catch (ServiceAuthException e) {
                    // not logging since the service engine already did
                    errorMessages.add(messagePrefixStr + "Service invocation error on row (" + i +"): " + e.getNonNestedMessage());
                } catch (ServiceValidationException e) {
                    // not logging since the service engine already did
                    request.setAttribute("serviceValidationException", e);
                    List errors = e.getMessageList();
                    if (errors != null) {
                        Iterator erri = errors.iterator();
                        while (erri.hasNext()) {
                            errorMessages.add("Service invocation error on row (" + i + "): " + erri.next());
                        }
                    } else {
                        errorMessages.add(messagePrefixStr + "Service invocation error on row (" + i +"): " + e.getNonNestedMessage());
                    }
                } catch (GenericServiceException e) {
                    Debug.logError(e, "Service invocation error", module);
                    errorMessages.add(messagePrefixStr + "Service invocation error on row (" + i +"): " + e.getNested() + messageSuffixStr);
                }

                if (result == null) {
                    returnString = ModelService.RESPOND_SUCCESS;
                } else {
                    // check for an error message
                    String errorMessage = ServiceUtil.makeErrorMessage(result, messagePrefixStr, messageSuffixStr, "", "");
                    if (UtilValidate.isNotEmpty(errorMessage)) {
                        errorMessages.add(errorMessage);
                    }
    
                    // get the success messages
                    if (!UtilValidate.isEmpty((String)result.get(ModelService.SUCCESS_MESSAGE))) {
                        String newSuccessMessage = (String)result.get(ModelService.SUCCESS_MESSAGE);
                        if (!successMessages.contains(newSuccessMessage)) {
                            successMessages.add(newSuccessMessage);
                        }
                    }
                    if (!UtilValidate.isEmpty((List)result.get(ModelService.SUCCESS_MESSAGE_LIST))) {
                        List newSuccessMessages = (List)result.get(ModelService.SUCCESS_MESSAGE_LIST);
                        for (int j = 0; j < newSuccessMessages.size(); j++) {
                            String newSuccessMessage = (String)newSuccessMessages.get(j);
                            if (!successMessages.contains(newSuccessMessage)) {
                                successMessages.add(newSuccessMessage);
                            }
                        }
                    }
                }
                // set the results in the request
                if ((result != null) && (result.entrySet() != null)) {
                    Iterator rmei = result.entrySet().iterator();
                    while (rmei.hasNext()) {
                        Map.Entry rme = (Map.Entry) rmei.next();
                        String resultKey = (String) rme.getKey();
                        Object resultValue = rme.getValue();

                        if (resultKey != null && !ModelService.RESPONSE_MESSAGE.equals(resultKey) && !ModelService.ERROR_MESSAGE.equals(resultKey) &&
                                !ModelService.ERROR_MESSAGE_LIST.equals(resultKey) && !ModelService.ERROR_MESSAGE_MAP.equals(resultKey) &&
                                !ModelService.SUCCESS_MESSAGE.equals(resultKey) && !ModelService.SUCCESS_MESSAGE_LIST.equals(resultKey)) {
                            request.setAttribute(resultKey, resultValue);
                        }
                    }
                }
            }
        } finally {
            if (errorMessages.size() > 0) {
                if (eventGlobalTransaction) {
                    // rollback the global transaction
                    try {
                        TransactionUtil.rollback(beganTrans, "Error in multi-service event handling: " + errorMessages.toString(), null);
                    } catch (GenericTransactionException e) {
                        Debug.logError(e, "Could not rollback multi-service global transaction", module);
                    }
                }
                errorMessages.add(0, errorPrefixStr);
                errorMessages.add(errorSuffixStr);
                StringBuffer errorBuf = new StringBuffer();
                Iterator ei = errorMessages.iterator();
                while (ei.hasNext()) {
                    String em = (String) ei.next();
                    errorBuf.append(em + "\n");
                }
                request.setAttribute("_ERROR_MESSAGE_", errorBuf.toString());
                returnString = "error";
            } else {
                if (eventGlobalTransaction) {
                    // commit the global transaction
                    try {
                        TransactionUtil.commit(beganTrans);
                    } catch (GenericTransactionException e) {
                        Debug.logError(e, "Could not commit multi-service global transaction", module);
                        throw new EventHandlerException("Commit multi-service global transaction failed");
                    }
                }
                if (successMessages.size() > 0) {
                    request.setAttribute("_EVENT_MESSAGE_LIST_", successMessages);
                }
                returnString = "success";
            }
        }
        
        return returnString;
    }
}
