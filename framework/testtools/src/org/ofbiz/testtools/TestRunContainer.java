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
package org.ofbiz.testtools;

import javolution.util.FastMap;
import junit.framework.*;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.optional.junit.JUnitTest;
import org.apache.tools.ant.taskdefs.optional.junit.XMLJUnitResultFormatter;
import org.ofbiz.base.container.Container;
import org.ofbiz.base.container.ContainerException;
import org.ofbiz.base.util.Debug;

import java.io.*;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A Container implementation to run the tests configured through this testtools stuff.
 */
public class TestRunContainer implements Container {

    public static final String module = TestRunContainer.class.getName();
    public static final String logFile = "runtime/logs/tests-junit.xml";

    protected String configFile = null;
    protected String component = null;
    protected String testCase = null;
    protected String outFile = null;
    protected String logLevel = null;

    /**
     * @see org.ofbiz.base.container.Container#init(java.lang.String[], java.lang.String)
     */
    public void init(String[] args, String configFile) {
        this.configFile = configFile;
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                String argument = args[i];
                // arguments can prefix w/ a '-'. Just strip them off
                if (argument.startsWith("-")) {
                    int subIdx = 1;
                    if (argument.startsWith("--")) {
                        subIdx = 2;
                    }
                    argument = argument.substring(subIdx);
                }

                // parse the arguments
                if (argument.indexOf("=") != -1) {
                    String argumentName = argument.substring(0, argument.indexOf("="));
                    String argumentVal = argument.substring(argument.indexOf("=") + 1);

                    if ("component".equalsIgnoreCase(argumentName)) {
                        this.component = argumentVal;
                    }
                    if ("case".equalsIgnoreCase(argumentName)) {
                        this.testCase = argumentVal;
                    }
                    if ("results".equalsIgnoreCase(argumentName)) {
                        this.outFile = argumentVal;
                    }
                    if ("loglevel".equalsIgnoreCase(argumentName)) {
                        this.logLevel = argumentVal;
                    }
                }
            }
        }
    }

    public boolean start() throws ContainerException {
        // configure log4j output logging
        if (logLevel != null) {
            int llevel = Debug.getLevelFromString(logLevel);

            for (int v = 0; v < 9; v++) {
                if (v < llevel) {
                    Debug.set(v, false);
                } else {
                    Debug.set(v, true);
                }
            }
        }

        // configure xml output
        if (outFile == null) {
            outFile = logFile;
        }

        JunitXmlListener xml;
        try {
            xml = new JunitXmlListener(new FileOutputStream(outFile));
        } catch (FileNotFoundException e) {
            throw new ContainerException(e);
        }

        TestResult results = new TestResult();
        results.addListener(new JunitListener());
        results.addListener(xml);

        // get the tests to run
        JunitSuiteWrapper jsWrapper = new JunitSuiteWrapper(component, testCase);
        if (jsWrapper.getAllTestList().size() == 0) {
            throw new ContainerException("No tests found (" + component + " / " + testCase + ")");
        }

        List testSuites = jsWrapper.makeTestSuites();
        Iterator i = testSuites.iterator();
        while (i.hasNext()) {
            TestSuite suite = (TestSuite) i.next();
            JUnitTest test = new JUnitTest();
            test.setName(suite.getName());
            xml.startTestSuite(test);

            // run the tests
            suite.run(results);
            xml.endTestSuite(test);            
        }
       
        // dispay the results
        Debug.log("[JUNIT] Pass: " + results.wasSuccessful() + " | # Tests: " + results.runCount() + " | # Failed: " +
                results.failureCount() + " # Errors: " + results.errorCount(), module);
        if (Debug.importantOn()) {
            Debug.log("[JUNIT] ----------------------------- ERRORS ----------------------------- [JUNIT]", module);
            Enumeration err = results.errors();
            if (!err.hasMoreElements()) {
                Debug.log("None");
            } else {
                while (err.hasMoreElements()) {
                    Debug.log("--> " + err.nextElement(), module);
                }
            }
            Debug.log("[JUNIT] ------------------------------------------------------------------ [JUNIT]", module);
            Debug.log("[JUNIT] ---------------------------- FAILURES ---------------------------- [JUNIT]", module);
            Enumeration fail = results.failures();
            if (!fail.hasMoreElements()) {
                Debug.log("None");
            } else {
                while (fail.hasMoreElements()) {
                    Debug.log("--> " + fail.nextElement(), module);
                }
            }
            Debug.log("[JUNIT] ------------------------------------------------------------------ [JUNIT]", module);
        }

        return true;
    }

    public void stop() throws ContainerException {        
    }

    class JunitXmlListener extends XMLJUnitResultFormatter {

        Map<String, Long> startTimes = FastMap.newInstance();

        public JunitXmlListener(OutputStream out) {
            this.setOutput(out);            
        }

        public void startTestSuite(JUnitTest suite) {
            startTimes.put(suite.getName(), System.currentTimeMillis());
            super.startTestSuite(suite);
        }

        public void endTestSuite(JUnitTest suite) throws BuildException {
            long startTime = startTimes.get(suite.getName());
            suite.setRunTime((System.currentTimeMillis() - startTime));
            super.endTestSuite(suite);
        }
    }

    class JunitListener implements TestListener {

        public void addError(Test test, Throwable throwable) {
            Debug.logWarning(throwable, "[JUNIT (error)] - " + test.getClass().getName() + " : " + throwable.toString(), module);
        }

        public void addFailure(Test test, AssertionFailedError assertionFailedError) {
            Debug.logWarning("[JUNIT (failure)] - " + test.getClass().getName() + " : " + assertionFailedError.getMessage(), module);
        }

        public void endTest(Test test) {
            //Debug.logInfo("[JUNIT] : " + test.getClass().getName() + " finished.", module);
        }

        public void startTest(Test test) {
           //Debug.logInfo("[JUNIT] : " + test.getClass().getName() + " starting...", module);
        }
    }
}
