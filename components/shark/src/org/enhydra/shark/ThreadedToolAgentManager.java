/*
 * $Id: ThreadedToolAgentManager.java,v 1.1 2004/04/22 15:40:55 ajzeneski Exp $
 *
 */
package org.enhydra.shark;

import java.util.List;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;

import org.enhydra.shark.api.internal.working.ToolAgentManager;
import org.enhydra.shark.api.internal.working.WfActivityInternal;
import org.enhydra.shark.api.internal.working.CallbackUtilities;
import org.enhydra.shark.api.internal.working.WfProcessInternal;
import org.enhydra.shark.api.internal.toolagent.AppParameter;
import org.enhydra.shark.api.internal.toolagent.SessionHandle;
import org.enhydra.shark.api.internal.toolagent.ToolAgent;
import org.enhydra.shark.api.internal.toolagent.ConnectFailed;
import org.enhydra.shark.api.internal.mappersistence.MappingManager;
import org.enhydra.shark.api.internal.mappersistence.ApplicationMap;
import org.enhydra.shark.api.SharkTransaction;
import org.enhydra.shark.api.RootException;
import org.enhydra.shark.api.TransactionException;
import org.enhydra.shark.api.MappingTransaction;
import org.enhydra.shark.api.client.wfbase.BaseException;
import org.enhydra.shark.api.client.wfmodel.InvalidData;
import org.enhydra.shark.api.client.wfmodel.UpdateNotAllowed;
import org.enhydra.shark.api.client.wfmodel.CannotComplete;
import org.enhydra.shark.xpdl.elements.Tool;
import org.enhydra.shark.xpdl.elements.WorkflowProcess;
import org.enhydra.shark.xpdl.elements.Application;
import org.enhydra.shark.xpdl.elements.ExtendedAttributes;
import org.enhydra.shark.xpdl.elements.ActualParameters;
import org.enhydra.shark.xpdl.elements.FormalParameters;
import org.enhydra.shark.xpdl.elements.FormalParameter;
import org.enhydra.shark.xpdl.elements.ActualParameter;
import org.enhydra.shark.xpdl.XMLComplexChoice;
import org.enhydra.shark.xpdl.XMLComplexElement;
import org.enhydra.shark.utilities.XPDLUtilities;

/**
 * @author     <a href="mailto:jaz@ofbiz.org">Andy Zeneski</a>
 * @version    $Revision: 1.1 $
 * @since      Mar 30, 2004
 */
public class ThreadedToolAgentManager implements ToolAgentManager {

    private final static String TOOL_AGENT_PREFIX = "ToolAgent.";
    private final static String DEFAULT_TOOL_AGENT = "DefaultToolAgent";
    private final static long APP_STATUS_INVALID = -1;
    private static List toolMonitors = new ArrayList();

    private String defaultToolAgentClassName = null;
    private List toolAgents = null;
    private CallbackUtilities cus = null;

    ThreadedToolAgentManager() {
        this.cus = SharkEngineManager.getInstance().getCallbackUtilities();
        createToolAgentList();
    }

    public void executeActivity(SharkTransaction t, WfActivityInternal act) throws RootException {
        ThreadedToolAgentManager.toolMonitors.add(new ToolRunnerManager(t, act));
    }

    /**
     * Returns all tool agents registered at nameserver.
     */
    public String[] getDefinedToolAgents() {
        String[] ata = new String[toolAgents.size()];
        toolAgents.toArray(ata);
        return ata;
    }

    private void createToolAgentList() {
        this.toolAgents = new ArrayList();
        String taName = null;
        String className = null;
        Properties props = cus.getProperties();

        Iterator it = props.entrySet().iterator();
        while (it.hasNext()) {
            try {
                Map.Entry me = (Map.Entry) it.next();
                taName = me.getKey().toString();
                if (taName.startsWith(TOOL_AGENT_PREFIX)) {
                    taName = taName.substring(TOOL_AGENT_PREFIX.length());
                    className = me.getValue().toString();
                    toolAgents.add(className);
                }
            } catch (Throwable ex) {
                //ex.printStackTrace();
                cus.error("ToolAgentManager -> Creation of Tool Agent " + taName + " from clas " + className + " failed !!!");
            }
        }

        // setting default tool agent
        try {
            defaultToolAgentClassName = (String) props.get(DEFAULT_TOOL_AGENT);

        } catch (Throwable ex) {
            cus.error("ToolAgentManager -> Creation of Default Tool Agent failed !!!");
        }
    }

    public static int howManyActivitiesRunning() {
        return ThreadedToolAgentManager.toolMonitors.size();
    }

    private synchronized static void removeToolMonitor(ToolRunnerManager monitor) {
        ThreadedToolAgentManager.toolMonitors.remove(monitor);
    }

    class ToolContext {

        private Tool tool;
        private Application app;
        private ApplicationMap appMap;
        private String packageId;
        private String processId;
        private String activityId;
        private String resource;
        private AppParameter[] params;

        public ToolContext(Tool tool, Application app, ApplicationMap appMap, String pkgId,
                String procId, String actId, String resource, AppParameter[] params) {
            this.tool = tool;
            this.app = app;
            this.appMap = appMap;
            this.packageId = pkgId;
            this.processId = procId;
            this.activityId = actId;
            this.resource = resource;
            this.params = params;
        }

        public Tool getTool() {
            return tool;
        }

        public Application getApplication() {
            return app;
        }

        public ApplicationMap getApplicationMap() {
            return appMap;
        }

        public String getPackageId() {
            return packageId;
        }

        public String getProcessId() {
            return processId;
        }

        public String getActivityId() {
            return activityId;
        }

        public String getActivityResource() {
            return resource;
        }

        public AppParameter[] getParameters() {
            return params;
        }
    }

    class ToolRunnerManager implements Runnable {

        private String packageKey;
        private String processKey;
        private String activityKey;
        private String resource;
        private List tools;
        private Map context;

        private Map toolResults;
        private List runners;
        private Thread thread;
        private boolean isRunning = false;

        ToolRunnerManager(SharkTransaction trans, WfActivityInternal activity) throws BaseException {
            this.packageKey = activity.container(trans).package_id(trans);
            this.processKey = activity.container(trans).key(trans);
            this.packageKey = activity.container(trans).package_id(trans);
            this.activityKey = activity.key(trans);
            this.resource = activity.getResourceUsername(trans);
            this.context = activity.process_context(trans);
            this.tools = this.getTools(trans, activity);
            this.toolResults = new HashMap();
            this.runners = new ArrayList();

            // start the thread
            thread = new Thread(this);
            thread.setDaemon(false);
            thread.setName(this.getClass().getName());
            thread.start();
        }

        private Collection getToolObjs(SharkTransaction trans, WfActivityInternal activity) throws BaseException {
            WfProcessInternal pr = activity.container(trans);
            WorkflowProcess wp = SharkUtilities.getWorkflowProcess(pr.manager(trans).package_id(trans),
                    pr.manager(trans).process_definition_id(trans));
            return SharkUtilities.getActivityDefinition(trans, activity, wp).getTools().toCollection();
        }

        private List getTools(SharkTransaction trans, WfActivityInternal activity) throws BaseException {
            Collection tools = getToolObjs(trans, activity);

            List toolList = null;
            if (tools != null) {
                toolList = new ArrayList();
                Iterator i = tools.iterator();
                while (i.hasNext()) {
                    Tool tool = (Tool) i.next();
                    String toolId = tool.getID();

                    Application app = (Application) ((XMLComplexChoice) tool.get("Application")).getChoosen();
                    ApplicationMap appMap = null;
                    try {
                        appMap = getApplicationMap(app, toolId);
                    } catch (Exception e) {
                        throw new BaseException(e);
                    }

                    AppParameter[] params = null;
                    try {
                        params = makeParameters(trans, tool, app, packageKey);
                    } catch (Exception e) {
                        throw new BaseException(e);
                    }
                    ToolContext ctx = new ToolContext(tool, app, appMap, packageKey, processKey, activityKey, resource, params);
                    toolList.add(ctx);
                }
            }
            return toolList;
        }

        private AppParameter[] makeParameters(SharkTransaction transaction, Tool tool, Application app, String packageId) throws Exception {
            // build up the parameters
            List parameters = new ArrayList();

            // the extended attributes are always the first parameter passed to tool agent
            String appPStr = XPDLUtilities.getExtendedAttributesString((ExtendedAttributes) app.get("ExtendedAttributes"));
            AppParameter param = new AppParameter("ExtendedAttributes", "ExtendedAttributes", AppParameter.MODE_IN, appPStr, String.class);
            parameters.add(param);

            ActualParameters aps = (ActualParameters) tool.get("ActualParameters");
            FormalParameters fps = (FormalParameters) ((XMLComplexChoice) app.get("Choice")).getChoosen();
            Map m = SharkUtilities.createContextMap(transaction, context, aps, fps, packageId);

            Iterator itFps = fps.toCollection().iterator();
            Iterator itAps = aps.toCollection().iterator();
            while (itFps.hasNext() && itAps.hasNext()) {
                FormalParameter fp = (FormalParameter) itFps.next();
                ActualParameter ap = (ActualParameter) itAps.next();
                String fpMode = fp.get("Mode").toValue().toString();
                String fpId = fp.getID();
                Object paramVal = m.get(fpId);

                // JAWE's CLASSES DataField and FormalParameter RETURNS ITs
                // Id ATTRIBUTE WHEN METHOD toString() is CALLED (when calling
                // ap.toValue().toString(), it can be called toString() method of
                // these two classes)
                param = new AppParameter(ap.toValue().toString(), fpId, fpMode, paramVal, SharkUtilities.getJavaClass(fp));
                parameters.add(param);
            }

            return (AppParameter[]) parameters.toArray(new AppParameter[parameters.size()]);
        }

        private ApplicationMap getApplicationMap(Application app, String applicationId) throws Exception {
            // find mapped procedure - but we can also live without mapping
            // manager (but we can't without ToolAgentFactory
            MappingManager mm = SharkEngineManager.getInstance().getMapPersistenceManager();
            ApplicationMap tad = null;
            if (mm != null) {
                XMLComplexElement cOwn = app.getCollection().getOwner();
                boolean isProcessApp = (cOwn instanceof WorkflowProcess);
                MappingTransaction t = null;
                try {
                    t = SharkUtilities.createMappingTransaction();
                    tad =
                            SharkEngineManager.
                            getInstance().
                            getMapPersistenceManager().getApplicationMappings().
                            getApplicationMap(t,
                                    app.getPackage().get("Id").toString(),
                                    ((isProcessApp) ? cOwn.get("Id").toString() : null),
                                    applicationId);
                } catch (RootException e) {
                    throw e;
                } finally {
                    SharkUtilities.releaseMappingTransaction(t);
                }
            }

            return tad;
        }

        public void run() {
            this.isRunning = true;

            // start the tools
            Iterator ti = tools.iterator();
            while (ti.hasNext()) {
                ToolContext tool = (ToolContext) ti.next();
                runners.add(new ToolRunner(tool));
            }

            // monitor the tools
            while (isRunning) {
                // check tool status
                Iterator ri = runners.iterator();
                while (ri.hasNext()) {
                    ToolRunner runner = (ToolRunner) ri.next();
                    if (!runner.isRunning()) {
                        // check for errors
                        Throwable toolError = runner.getError();
                        if (toolError != null) {
                            // handle tool error
                        } else {
                            // get the results
                            Map thisResult = runner.getResults();
                            if (thisResult != null) {
                                toolResults.putAll(thisResult);
                            }
                        }
                        // remove the runner from the waiting list
                        ri.remove();
                    }
                }

                // sleep for a while then run again
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                }
            }

            // all tools finished - need a transaction and internal activity object
            SharkTransaction trans = null;
            WfActivityInternal act = null;

            try {
                trans = SharkUtilities.createTransaction();
                act = SharkUtilities.getActivity(trans, processKey, activityKey);
                act.set_process_context(trans, toolResults);
                act.finish(trans);
            } catch (TransactionException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (BaseException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (UpdateNotAllowed e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (InvalidData e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (CannotComplete e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

            // clear out the reference in parent class
            ThreadedToolAgentManager.removeToolMonitor(this);
        }

        class ToolRunner implements Runnable {

            private Thread thread;
            private ToolContext tool;

            private boolean isRunning = false;
            private Throwable error = null;
            private Map results = null;

            ToolRunner(ToolContext tool) {
                this.tool = tool;

                // run the tool
                thread = new Thread(this);
                thread.setDaemon(false);
                thread.setName(this.getClass().getName());
                thread.start();
            }

            public void run() {
                this.isRunning = true;
                try {
                    this.results = this.runTool(null);
                } catch (Exception e) {
                    this.error = e;
                }
                this.isRunning = false;
            }

            public boolean isRunning() {
                return this.isRunning;
            }

            public Throwable getError() {
                return this.error;
            }

            public Map getResults() {
                return this.results;
            }

            private Map runTool(SharkTransaction transaction) throws Exception {
                // get the application to run
                ApplicationMap tad = tool.getApplicationMap();

                // connect to the tool
                String tacn = (tad != null) ? tad.getToolAgentClassName() : defaultToolAgentClassName;
                String uname = (tad != null) ? tad.getUsername() : "";
                String pwd = (tad != null) ? tad.getPassword() : "";
                String appN = (tad != null) ? tad.getApplicationName() : "";
                Integer appM = (tad != null) ? tad.getApplicationMode() : null;
                ToolAgent ta = SharkEngineManager.getInstance().getToolAgentFactory().createToolAgent(transaction, tacn);

                SessionHandle shandle = null;
                // try to connect to the tool agent
                try {
                    shandle = ta.connect(transaction, uname, pwd, cus.getProperty("enginename", "imaobihostrezube"), "");
                } catch (ConnectFailed cf) {
                    cus.error("Activity[" + tool.getActivityId() + "] - connection to Tool agent " + tacn + " failed !");
                    throw cf;
                }

                String assId = SharkUtilities.createAssignmentKey(tool.getActivityId(), resource);

                // invoke the application
                ta.invokeApplication(transaction, shandle.getHandle(), appN, tool.getProcessId(), assId, tool.getParameters(), appM);

                // check the status
                long appStatus = ta.requestAppStatus(transaction, shandle.getHandle(), tool.getProcessId(), assId, tool.getParameters());
                if (appStatus == APP_STATUS_INVALID) {
                    ta.disconnect(transaction, shandle);
                    throw new Exception();
                }
                ta.disconnect(transaction, shandle);

                // return the result parameters
                AppParameter[] returnValues = tool.getParameters();
                Map newData = new HashMap();
                for (int i = 0; i < returnValues.length; i++) {
                    if (returnValues[i].the_mode.equals(AppParameter.MODE_OUT) ||
                            returnValues[i].the_mode.equals(AppParameter.MODE_INOUT)) {
                        String name = returnValues[i].the_actual_name;
                        Object value = returnValues[i].the_value;
                        newData.put(name, value);
                    }
                }

                return newData;
            }
        }
    }
}
