/*
 * $Id$
 *
 * Copyright (c) 2001, 2002 The Open For Business Project - www.ofbiz.org
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
package org.ofbiz.service;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.transaction.InvalidTransactionException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.ofbiz.base.config.GenericConfigException;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.base.util.UtilXml;
import org.ofbiz.entity.GenericDelegator;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.transaction.DebugXaResource;
import org.ofbiz.entity.transaction.GenericTransactionException;
import org.ofbiz.entity.transaction.TransactionFactory;
import org.ofbiz.entity.transaction.TransactionUtil;
import org.ofbiz.security.Security;
import org.ofbiz.security.SecurityConfigurationException;
import org.ofbiz.security.SecurityFactory;
import org.ofbiz.service.config.ServiceConfigUtil;
import org.ofbiz.service.eca.ServiceEcaUtil;
import org.ofbiz.service.engine.GenericEngine;
import org.ofbiz.service.engine.GenericEngineFactory;
import org.ofbiz.service.group.ServiceGroupReader;
import org.ofbiz.service.jms.JmsListenerFactory;
import org.ofbiz.service.job.JobManager;
import org.ofbiz.service.job.JobManagerException;
import org.w3c.dom.Element;

/**
 * Global Service Dispatcher
 *
 * @author     <a href="mailto:jaz@ofbiz.org">Andy Zeneski</a>
 * @version    $Rev$
 * @since      2.0
 */
public class ServiceDispatcher {

    public static final String module = ServiceDispatcher.class.getName();

    protected static Map dispatchers = new HashMap();
    protected static boolean enableJM = true;
    protected static boolean enableJMS = true;
    protected static boolean enableSvcs = true;

    protected GenericDelegator delegator = null;
    protected GenericEngineFactory factory = null;
    protected Security security = null;
    protected Map localContext = null;
    protected JobManager jm = null;
    protected JmsListenerFactory jlf = null;

    public ServiceDispatcher(GenericDelegator delegator) {
        Debug.logInfo("[ServiceDispatcher] : Creating new instance.", module);
        factory = new GenericEngineFactory(this);
        ServiceGroupReader.readConfig();
        ServiceEcaUtil.readConfig();

        this.delegator = delegator;
        this.localContext = new HashMap();
        if (delegator != null) {
            try {
                this.security = SecurityFactory.getInstance(delegator);
            } catch (SecurityConfigurationException e) {
                Debug.logError(e, "[ServiceDispatcher.init] : No instance of security imeplemtation found.", module);
            }
        }

        // make sure we haven't disabled these features from running
        if (enableJM) {
            this.jm = new JobManager(this.delegator);
        }

        if (enableJMS) {
            this.jlf = new JmsListenerFactory(this);
        }

        if (enableSvcs) {
            this.runStartupServices();
        }
    }

    /**
     * Returns a pre-registered instance of the ServiceDispatcher associated with this delegator.
     * @param delegator the local delegator
     * @return A reference to this global ServiceDispatcher
     */
    public static ServiceDispatcher getInstance(String name, GenericDelegator delegator) {
        ServiceDispatcher sd = getInstance(null, null, delegator);

        if (!sd.containsContext(name)) {
            return null;
        }
        return sd;
    }

    /**
     * Returns an instance of the ServiceDispatcher associated with this delegator and registers the loader.
     * @param name the local dispatcher
     * @param context the context of the local dispatcher
     * @param delegator the local delegator
     * @return A reference to this global ServiceDispatcher
     */
    public static ServiceDispatcher getInstance(String name, DispatchContext context, GenericDelegator delegator) {
        ServiceDispatcher sd = null;

        String dispatcherKey = delegator != null ? delegator.getDelegatorName() : "null";
        sd = (ServiceDispatcher) dispatchers.get(dispatcherKey);
        if (sd == null) {
            synchronized (ServiceDispatcher.class) {
                if (Debug.verboseOn()) Debug.logVerbose("[ServiceDispatcher.getInstance] : No instance found (" + delegator.getDelegatorName() + ").", module);
                sd = (ServiceDispatcher) dispatchers.get(dispatcherKey);
                if (sd == null) {
                    sd = new ServiceDispatcher(delegator);
                    dispatchers.put(dispatcherKey, sd);
                }
            }
        }
        if (name != null && context != null) {
            sd.register(name, context);
        }
        return sd;
    }

    /**
     * Registers the loader with this ServiceDispatcher
     * @param name the local dispatcher
     * @param context the context of the local dispatcher
     */
    public void register(String name, DispatchContext context) {
        if (Debug.infoOn()) Debug.logInfo("[ServiceDispatcher.register] : Registered dispatcher: " + context.getName(), module);
        this.localContext.put(name, context);
    }

    /**
     * De-Registers the loader with this ServiceDispatcher
     * @param local the LocalDispatcher to de-register
     */
    public void deregister(LocalDispatcher local) {
        if (Debug.infoOn()) Debug.logInfo("[ServiceDispatcher.deregister] : De-Registering dispatcher: " + local.getName(), module);
        localContext.remove(local.getName());
         if (localContext.size() == 1) { // 1 == the JMSDispatcher
             try {
                 this.shutdown();
             } catch (GenericServiceException e) {
                 Debug.logError(e, "Trouble shutting down ServiceDispatcher!", module);
             }
         }
    }

    /**
     * Run the service synchronously and return the result.
     * @param localName Name of the context to use.
     * @param service Service model object.
     * @param context Map of name, value pairs composing the context.
     * @return Map of name, value pairs composing the result.
     * @throws ServiceAuthException
     * @throws ServiceValidationException
     * @throws GenericServiceException
     */
    public Map runSync(String localName, ModelService service, Map context) throws ServiceAuthException, ServiceValidationException, GenericServiceException {
        return runSync(localName, service, context, true);
    }

    /**
     * Run the service synchronously and IGNORE the result.
     * @param localName Name of the context to use.
     * @param service Service model object.
     * @param context Map of name, value pairs composing the context.
     * @throws ServiceAuthException
     * @throws ServiceValidationException
     * @throws GenericServiceException
     */
    public void runSyncIgnore(String localName, ModelService service, Map context) throws ServiceAuthException, ServiceValidationException, GenericServiceException {
        runSync(localName, service, context, false);
    }

    /**
     * Run the service synchronously and return the result.
     * @param localName Name of the context to use.
     * @param modelService Service model object.
     * @param context Map of name, value pairs composing the context.
     * @param validateOut Validate OUT parameters
     * @return Map of name, value pairs composing the result.
     * @throws ServiceAuthException
     * @throws ServiceValidationException
     * @throws GenericServiceException
     */
    public Map runSync(String localName, ModelService modelService, Map context, boolean validateOut) throws ServiceAuthException, ServiceValidationException, GenericServiceException {
        boolean debugging = checkDebug(modelService, 1, true);
        if (Debug.verboseOn()) {
            Debug.logVerbose("[ServiceDispatcher.runSync] : invoking service " + modelService.name + " [" + modelService.location +
                "/" + modelService.invoke + "] (" + modelService.engineName + ")", module);
        }

        // setup the result map
        Map result = new HashMap();
        boolean isFailure = false;
        boolean isError = false;

        // check the locale
        this.checkLocale(context);

        // for isolated transactions
        TransactionManager tm = TransactionFactory.getTransactionManager();
        Transaction parentTransaction = null;

        // start the transaction
        boolean beganTrans = false;
        if (modelService.useTransaction) {
            try {
                beganTrans = TransactionUtil.begin(modelService.transactionTimeout);
            } catch (GenericTransactionException te) {
                throw new GenericServiceException("Cannot start the transaction.", te.getNested());
            }

            // isolate the transaction if defined
            if (modelService.requireNewTransaction && !beganTrans) {
                try {
                    parentTransaction = tm.suspend();
                } catch (SystemException se) {
                    Debug.logError(se, "Problems suspending current transaction", module);
                    throw new GenericServiceException("Problems suspending transaction, see logs");
                }

                // now start a new transaction
                try {
                    beganTrans = TransactionUtil.begin(modelService.transactionTimeout);
                } catch (GenericTransactionException gte) {
                    throw new GenericServiceException("Cannot start the transaction.", gte.getNested());
                }
            }
        }

        // XAResource debugging
        if (beganTrans && TransactionUtil.debugResources) {
            DebugXaResource dxa = new DebugXaResource(modelService.name);
            try {
                dxa.enlist();
            } catch (Exception e) {
                Debug.logError(e, module);
            }
        }

        // needed for events
        DispatchContext ctx = (DispatchContext) localContext.get(localName);

        try {
            // get eventMap once for all calls for speed, don't do event calls if it is null
            Map eventMap = ServiceEcaUtil.getServiceEventMap(modelService.name);

            // setup global transaction ECA listeners
            if (eventMap != null) ServiceEcaUtil.evalRules(modelService.name, eventMap, "global-rollback", ctx, context, result, false, false);
            if (eventMap != null) ServiceEcaUtil.evalRules(modelService.name, eventMap, "global-commit", ctx, context, result, false, false);

            // pre-auth ECA
            if (eventMap != null) ServiceEcaUtil.evalRules(modelService.name, eventMap, "auth", ctx, context, result, false, false);

            context = checkAuth(localName, context, modelService);
            Object userLogin = context.get("userLogin");

            if (modelService.auth && userLogin == null) {
                throw new ServiceAuthException("User authorization is required for this service: " + modelService.name + modelService.debugInfo());
            }

            // setup the engine
            GenericEngine engine = getGenericEngine(modelService.engineName);

            // pre-validate ECA
            if (eventMap != null) ServiceEcaUtil.evalRules(modelService.name, eventMap, "in-validate", ctx, context, result, false, false);

            // check for pre-validate failure/erros
            isFailure = ModelService.RESPOND_FAIL.equals(result.get(ModelService.RESPONSE_MESSAGE));
            isError = ModelService.RESPOND_ERROR.equals(result.get(ModelService.RESPONSE_MESSAGE));

            // validate the context
            if (modelService.validate && !isError && !isFailure) {
                try {
                    modelService.validate(context, ModelService.IN_PARAM);
                } catch (ServiceValidationException e) {
                    Debug.logError(e, "Incoming context (in runSync : " + modelService.name + ") does not match expected requirements", module);
                    throw e;
                }
            }

            // pre-invoke ECA
            if (eventMap != null) ServiceEcaUtil.evalRules(modelService.name, eventMap, "invoke", ctx, context, result, false, false);

            // check for pre-invoke failure/erros
            isFailure = ModelService.RESPOND_FAIL.equals(result.get(ModelService.RESPONSE_MESSAGE));
            isError = ModelService.RESPOND_ERROR.equals(result.get(ModelService.RESPONSE_MESSAGE));

            // ===== invoke the service =====
            if (!isError && !isFailure) {
                Map invokeResult = engine.runSync(localName, modelService, context);
                if (invokeResult != null) {                    
                    result.putAll(invokeResult);
                } else {
                    Debug.logWarning("Service (in runSync : " + modelService.name + ") returns null result", module);
                }
            }

            // re-check the errors/failures
            isFailure = ModelService.RESPOND_FAIL.equals(result.get(ModelService.RESPONSE_MESSAGE));
            isError = ModelService.RESPOND_ERROR.equals(result.get(ModelService.RESPONSE_MESSAGE));            

            // create a new context with the results to pass to ECA services; necessary because caller may reuse this context
            Map ecaContext = new HashMap(context);

            // copy all results: don't worry parameters that aren't allowed won't be passed to the ECA services
            ecaContext.putAll(result);

            // validate the result
            if (modelService.validate && validateOut) {
                // pre-out-validate ECA
                if (eventMap != null) ServiceEcaUtil.evalRules(modelService.name, eventMap, "out-validate", ctx, ecaContext, result, isError, isFailure);
                try {
                    modelService.validate(result, ModelService.OUT_PARAM);
                } catch (ServiceValidationException e) {
                    Debug.logError(e, "Outgoing result (in runSync : " + modelService.name + ") does not match expected requirements", module);
                    throw e;
                }
            }

            // pre-commit ECA
            if (eventMap != null) ServiceEcaUtil.evalRules(modelService.name, eventMap, "commit", ctx, ecaContext, result, isError, isFailure);

            // check for failure and log on info level; this is used for debugging
            if (isFailure) {
                Debug.logWarning("Service Failure [" + modelService.name + "]: " + ServiceUtil.getErrorMessage(result), module);
            }

            // if there was an error, rollback transaction, otherwise commit
            if (isError) {
                // try to log the error
                Debug.logError("Service Error [" + modelService.name + "]: " + ServiceUtil.getErrorMessage(result), module);

                // rollback the transaction
                try {
                    TransactionUtil.rollback(beganTrans);
                } catch (GenericTransactionException e) {
                    Debug.logError(e, "Could not rollback transaction", module);
                }
            } else {
                // commit the transaction
                try {
                    TransactionUtil.commit(beganTrans);
                } catch (GenericTransactionException e) {
                    Debug.logError(e, "Could not commit transaction", module);
                    throw new GenericServiceException("Commit transaction failed");
                }
            }

            // resume the parent transaction
            if (parentTransaction != null) {
                try {
                    tm.resume(parentTransaction);
                } catch (InvalidTransactionException ite) {
                    Debug.logWarning(ite, "Invalid transaction, not resumed", module);
                } catch (IllegalStateException ise) {
                    Debug.logError(ise, "Trouble resuming parent transaction", module);
                    throw new GenericServiceException("Resume transaction exception, see logs");
                } catch (SystemException se) {
                    Debug.logError(se, "Trouble resuming parent transaction", module);
                    throw new GenericServiceException("Resume transaction exception, see logs");
                }
            }

            // pre-return ECA
            if (eventMap != null) ServiceEcaUtil.evalRules(modelService.name, eventMap, "return", ctx, ecaContext, result, isError, isFailure);

            checkDebug(modelService, 0, debugging);
            return result;
        } catch (Throwable t) {
            Debug.logError(t, "Service [" + modelService.name + "] threw an unexpected exception/error", module);
            try {
                TransactionUtil.rollback(beganTrans);
            } catch (GenericTransactionException te) {
                Debug.logError(te, "Cannot rollback transaction", module);
            }
            checkDebug(modelService, 0, debugging);
            if (t instanceof ServiceAuthException) {
                throw (ServiceAuthException) t;
            } else if (t instanceof ServiceValidationException) {
                throw (ServiceValidationException) t;
            } else if (t instanceof GenericServiceException) {
                throw (GenericServiceException) t;
            } else {
                throw new GenericServiceException("Service [" + modelService.name + "] Failed" + modelService.debugInfo() , t);
            }
        }
    }

    /**
     * Run the service asynchronously, passing an instance of GenericRequester that will receive the result.
     * @param localName Name of the context to use.
     * @param service Service model object.
     * @param context Map of name, value pairs composing the context.
     * @param requester Object implementing GenericRequester interface which will receive the result.
     * @param persist True for store/run; False for run.
     * @throws ServiceAuthException
     * @throws ServiceValidationException
     * @throws GenericServiceException
     */
    public void runAsync(String localName, ModelService service, Map context, GenericRequester requester, boolean persist) throws ServiceAuthException, ServiceValidationException, GenericServiceException {
        boolean debugging = checkDebug(service, 1, true);
        if (Debug.verboseOn()) {
            Debug.logVerbose("[ServiceDispatcher.runAsync] : prepareing service " + service.name + " [" + service.location + "/" + service.invoke +
                "] (" + service.engineName + ")", module);
        }

        // setup the result map
        Map result = new HashMap();
        boolean isFailure = false;
        boolean isError = false;

        // check the locale
        this.checkLocale(context);

        // for isolated transactions
        TransactionManager tm = TransactionFactory.getTransactionManager();
        Transaction parentTransaction = null;

        // start the transaction
        boolean beganTrans = false;
        if (service.useTransaction) {
            try {
                beganTrans = TransactionUtil.begin(service.transactionTimeout);
            } catch (GenericTransactionException te) {
                throw new GenericServiceException("Cannot start the transaction.", te.getNested());
            }

            // isolate the transaction if defined
            if (service.requireNewTransaction && !beganTrans) {
                try {
                    parentTransaction = tm.suspend();
                } catch (SystemException se) {
                    Debug.logError(se, "Problems suspending current transaction", module);
                    throw new GenericServiceException("Problems suspending transaction, see logs");
                }

                // now start a new transaction
                try {
                    beganTrans = TransactionUtil.begin(service.transactionTimeout);
                } catch (GenericTransactionException gte) {
                    throw new GenericServiceException("Cannot start the transaction.", gte.getNested());
                }
            }
        }

        // XAResource debugging
        if (beganTrans && TransactionUtil.debugResources) {
            DebugXaResource dxa = new DebugXaResource(service.name);
            try {
                dxa.enlist();
            } catch (Exception e) {
                Debug.logError(e, module);
            }
        }

        // needed for events
        DispatchContext ctx = (DispatchContext) localContext.get(localName);

        try {
            // get eventMap once for all calls for speed, don't do event calls if it is null
            Map eventMap = ServiceEcaUtil.getServiceEventMap(service.name);

            // pre-auth ECA
            if (eventMap != null) ServiceEcaUtil.evalRules(service.name, eventMap, "auth", ctx, context, result, isError, isFailure);

            context = checkAuth(localName, context, service);
            Object userLogin = context.get("userLogin");

            if (service.auth && userLogin == null)
                throw new ServiceAuthException("User authorization is required for this service: " + service.name + service.debugInfo());

            // setup the engine
            GenericEngine engine = getGenericEngine(service.engineName);

            // pre-validate ECA
            if (eventMap != null) ServiceEcaUtil.evalRules(service.name, eventMap, "in-validate", ctx, context, result, isError, isFailure);

            // check for pre-validate failure/erros
            isFailure = ModelService.RESPOND_FAIL.equals(result.get(ModelService.RESPONSE_MESSAGE));
            isError = ModelService.RESPOND_ERROR.equals(result.get(ModelService.RESPONSE_MESSAGE));

            // validate the context
            if (service.validate && !isError && !isFailure) {
                try {
                    service.validate(context, ModelService.IN_PARAM);
                } catch (ServiceValidationException e) {
                    Debug.logError(e, "Incoming service context (in runAsync: " + service.name + ") does not match expected requirements", module);
                    throw e;
                }
            }

            // run the service
            if (!isError && !isFailure) {
                if (requester != null) {
                    engine.runAsync(localName, service, context, requester, persist);
                } else {
                    engine.runAsync(localName, service, context, persist);
                }
            }

            // always try to commit the transaction since we don't know in this case if its was an error or not
            try {
                TransactionUtil.commit(beganTrans);
            } catch (GenericTransactionException e) {
                Debug.logError(e, "Could not commit transaction", module);
                throw new GenericServiceException("Commit transaction failed");
            }

            // resume the parent transaction
            if (parentTransaction != null) {
                try {
                    tm.resume(parentTransaction);
                } catch (InvalidTransactionException ite) {
                    Debug.logWarning(ite, "Invalid transaction, not resumed", module);
                } catch (IllegalStateException ise) {
                    Debug.logError(ise, "Trouble resuming parent transaction", module);
                    throw new GenericServiceException("Resume transaction exception, see logs");
                } catch (SystemException se) {
                    Debug.logError(se, "Trouble resuming parent transaction", module);
                    throw new GenericServiceException("Resume transaction exception, see logs");
                }
            }

            checkDebug(service, 0, debugging);
        } catch (Throwable t) {
            Debug.logError(t, "Service [" + service.name + "] threw an unexpected exception/error", module);
            try {
                TransactionUtil.rollback(beganTrans);
            } catch (GenericTransactionException te) {
                Debug.logError(te, "Cannot rollback transaction", module);
            }
            checkDebug(service, 0, debugging);
            if (t instanceof ServiceAuthException) {
                throw (ServiceAuthException) t;
            } else if (t instanceof ServiceValidationException) {
                throw (ServiceValidationException) t;
            } else if (t instanceof GenericServiceException) {
                throw (GenericServiceException) t;
            } else {
                throw new GenericServiceException("Service [" + service.name + "] Failed" + service.debugInfo() , t);
            }
        }
    }

    /**
     * Run the service asynchronously and IGNORE the result.
     * @param localName Name of the context to use.
     * @param service Service model object.
     * @param context Map of name, value pairs composing the context.
     * @param persist True for store/run; False for run.
     * @throws ServiceAuthException
     * @throws ServiceValidationException
     * @throws GenericServiceException
     */
    public void runAsync(String localName, ModelService service, Map context, boolean persist) throws ServiceAuthException, ServiceValidationException, GenericServiceException {
        this.runAsync(localName, service, context, null, persist);
    }

    /**
     * Gets the GenericEngine instance that corresponds to the given name
     * @param engineName Name of the engine
     * @return GenericEngine instance that corresponds to the engineName
     */
    public GenericEngine getGenericEngine(String engineName) throws GenericServiceException {
        return factory.getGenericEngine(engineName);
    }

    /**
     * Gets the JobManager associated with this dispatcher
     * @return JobManager that is associated with this dispatcher
     */
    public JobManager getJobManager() {
        return this.jm;
    }

    /**
     * Gets the JmsListenerFactory which holds the message listeners.
     * @return JmsListenerFactory
     */
    public JmsListenerFactory getJMSListenerFactory() {
        return this.jlf;
    }

    /**
     * Gets the GenericDelegator associated with this dispatcher
     * @return GenericDelegator associated with this dispatcher
     */
    public GenericDelegator getDelegator() {
        return this.delegator;
    }

    /**
     * Gets the Security object associated with this dispatcher
     * @return Security object associated with this dispatcher
     */
    public Security getSecurity() {
        return this.security;
    }

    /**
     * Gets the local context from a name
     * @param name of the context to find.
     */
    public DispatchContext getLocalContext(String name) {
        return (DispatchContext) localContext.get(name);
    }

    /**
     * Gets the local dispatcher from a name
     * @param name of the LocalDispatcher to find.
     * @return LocalDispatcher matching the loader name
     */
    public LocalDispatcher getLocalDispatcher(String name) {
        return ((DispatchContext) localContext.get(name)).getDispatcher();
    }

    /**
     * Test if this dispatcher instance contains the local context.
     * @param name of the local context
     * @return true if the local context is found in this dispatcher.
     */
    public boolean containsContext(String name) {
        return localContext.containsKey(name);
    }

    protected void shutdown() throws GenericServiceException {
        Debug.logImportant("Shutting down the service engine...", module);
        // shutdown JMS listeners
        jlf.closeListeners();
        // shutdown the job scheduler
        jm.finalize();
    }

    // checks if parameters were passed for authentication
    private Map checkAuth(String localName, Map context, ModelService origService) throws ServiceAuthException, GenericServiceException {
        String service = ServiceConfigUtil.getElementAttr("authorization", "service-name");

        if (service == null) {
            throw new GenericServiceException("No Authentication Service Defined");
        }
        if (service.equals(origService.name)) {
            // manually calling the auth service, don't continue...
            return context;
        }

        if (context.containsKey("login.username")) {
            // check for a username/password, if there log the user in and make the userLogin object
            String username = (String) context.get("login.username");

            if (context.containsKey("login.password")) {
                String password = (String) context.get("login.password");

                context.put("userLogin", getLoginObject(service, localName, username, password, (Locale) context.get("locale")));
                context.remove("login.password");
            } else {
                context.put("userLogin", getLoginObject(service, localName, username, null, (Locale) context.get("locale")));
            }
            context.remove("login.username");
        } else {
            // if a userLogin object is there, make sure the given username/password exists in our local database
            GenericValue userLogin = (GenericValue) context.get("userLogin");

            if (userLogin != null) {
                GenericValue newUserLogin = getLoginObject(service, localName, userLogin.getString("userLoginId"), userLogin.getString("currentPassword"), (Locale) context.get("locale"));

                if (newUserLogin == null) {
                    // uh oh, couldn't validate that one...
                    // we'll have to remove it from the incoming context which will cause an auth error later if auth is required
                    context.remove("userLogin");
                }
            }
        }

        // evaluate permissions for the service or throw exception if fail.
        DispatchContext dctx = this.getLocalContext(localName);
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        if (!origService.evalPermissions(dctx.getSecurity(), userLogin)) {
            throw new ServiceAuthException("You do not have permission to invoke this service");
        }

        return context;
    }

    // gets a value object from name/password pair
    private GenericValue getLoginObject(String service, String localName, String username, String password, Locale locale) throws GenericServiceException {
        Map context = UtilMisc.toMap("login.username", username, "login.password", password, "isServiceAuth", new Boolean(true), "locale", locale);

        if (Debug.verboseOn()) Debug.logVerbose("[ServiceDispathcer.authenticate] : Invoking UserLogin Service", module);

        // get the dispatch context and service model
        DispatchContext dctx = getLocalContext(localName);
        ModelService model = dctx.getModelService(service);

        // get the service engine
        GenericEngine engine = getGenericEngine(model.engineName);

        // invoke the service and get the UserLogin value object
        Map result = engine.runSync(localName, model, context);
        GenericValue value = (GenericValue) result.get("userLogin");

        return value;
    }

    // checks the locale object in the context
    private void checkLocale(Map context) {
        Object locale = context.get("locale");
        Locale newLocale = null;

        if (locale != null) {
            if (locale instanceof Locale) {
                return;
            } else if (locale instanceof String) {
                // en_US = lang_COUNTRY
                newLocale = UtilMisc.parseLocale((String) locale);
            }
        }

        if (newLocale == null) {
            newLocale = Locale.getDefault();
        }
        context.put("locale", newLocale);
    }

    // mode 1 = beginning (turn on) mode 0 = end (turn off)
    private boolean checkDebug(ModelService model, int mode, boolean enable) {
        boolean debugOn = Debug.verboseOn();
        switch (mode) {
            case 0:
                if (model.debug && enable && debugOn) {
                    // turn it off
                    Debug.set(Debug.VERBOSE, false);
                    Debug.logInfo("Verbose logging turned OFF", module);
                    return true;
                }
                break;
            case 1:
                if (model.debug && enable && !debugOn) {
                    // turn it on
                    Debug.set(Debug.VERBOSE, true);
                    Debug.logInfo("Verbose logging turned ON", module);
                    return true;
                }
                break;
            default:
                Debug.logError("Invalid mode for checkDebug should be (0 or 1)", module);
        }
        return false;
    }

    // run startup services
    private synchronized int runStartupServices() {
        if (jm == null) return 0;

        Element root = null;
        try {
            root = ServiceConfigUtil.getXmlRootElement();
        } catch (GenericConfigException e) {
            Debug.logError(e, module);
            return 0;
        }

        int servicesScheduled = 0;
        List startupServices = UtilXml.childElementList(root, "startup-service");
        if (startupServices != null && startupServices.size() > 0) {
            Iterator i = startupServices.iterator();
            while (i.hasNext()) {
                Element ss = (Element) i.next();
                String serviceName = ss.getAttribute("name");
                String runtimeDataId = ss.getAttribute("runtime-data-id");
                String delayStr = ss.getAttribute("runtime-delay");
                String sendToPool = ss.getAttribute("run-in-pool");
                if (UtilValidate.isEmpty(sendToPool)) {
                    sendToPool = ServiceConfigUtil.getSendPool();
                }

                long runtimeDelay = 0;
                try {
                    runtimeDelay = Long.parseLong(delayStr);
                } catch (Exception e) {
                    Debug.logError(e, "Unable to parse runtime-delay value; using 0", module);
                    runtimeDelay = 0;
                }

                // current time + 1 sec delay + extended delay
                long runtime = System.currentTimeMillis() + 1000 + runtimeDelay;
                try {
                    jm.schedule(sendToPool, serviceName, runtimeDataId, runtime);
                } catch (JobManagerException e) {
                    Debug.logError(e, "Unable to schedule service [" + serviceName + "]", module);
                }
            }
        }

        return servicesScheduled;
    }

    /**
     * Enabled/Disables the Job Manager/Scheduler globally
     * (this will not effect any dispatchers already running)
     * @param enable
     */
    public static void enableJM(boolean enable) {
        ServiceDispatcher.enableJM = enable;
    }

    /**
     * Enabled/Disables the JMS listeners globally
     * (this will not effect any dispatchers already running)
     * @param enable
     */
    public static void enableJMS(boolean enable) {
        ServiceDispatcher.enableJMS = enable;
    }

    /**
     * Enabled/Disables the startup services globally
     * (this will not effect any dispatchers already running)
     * @param enable
     */
    public static void enableSvcs(boolean enable) {
        ServiceDispatcher.enableSvcs = enable;
    }

}
