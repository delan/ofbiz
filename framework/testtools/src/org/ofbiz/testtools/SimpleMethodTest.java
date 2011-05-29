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

import java.util.List;
import java.util.Locale;
import java.util.Map;

import junit.framework.AssertionFailedError;
import junit.framework.TestResult;

import org.ofbiz.base.util.UtilGenerics;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.minilang.MiniLangException;
import org.ofbiz.minilang.SimpleMethod;
import org.ofbiz.service.ModelService;
import org.ofbiz.service.testtools.OFBizTestCase;
import org.w3c.dom.Element;

public class SimpleMethodTest extends OFBizTestCase {

    public static final String module = ServiceTest.class.getName();

    protected String methodLocation;
    protected String methodName;

    /**
     * Tests of Simple Method
     * @param caseName test case name
     * @param mainElement DOM main element 
     */
    public SimpleMethodTest(String caseName, Element mainElement) {
        this(caseName, mainElement.getAttribute("location"), mainElement.getAttribute("name"));
    }

    public SimpleMethodTest(String caseName, String methodLocation, String methodName) {
        super(caseName);
        this.methodLocation = methodLocation;
        this.methodName = methodName;
    }

    @Override
    public int countTestCases() {
        return 1;
    }

    @Override
    public void run(TestResult result) {
        result.startTest(this);

        try {

            Map<String, Object> serviceResult = SimpleMethod.runSimpleService(methodLocation, methodName, dispatcher.getDispatchContext(),
                    UtilMisc.toMap("test", this, "testResult", result, "locale", Locale.getDefault()));

            // do something with the errorMessage
            String errorMessage = (String) serviceResult.get(ModelService.ERROR_MESSAGE);
            if (UtilValidate.isNotEmpty(errorMessage)) {
                result.addFailure(this, new AssertionFailedError(errorMessage));
            }

            // do something with the errorMessageList
            List<Object> errorMessageList = UtilGenerics.cast(serviceResult.get(ModelService.ERROR_MESSAGE_LIST));
            if (UtilValidate.isNotEmpty(errorMessageList)) {
                for (Object message: errorMessageList) {
                    result.addFailure(this, new AssertionFailedError(message.toString()));
                }
            }

            // do something with the errorMessageMap
            Map<String, Object> errorMessageMap = UtilGenerics.cast(serviceResult.get(ModelService.ERROR_MESSAGE_MAP));
            if (!UtilValidate.isEmpty(errorMessageMap)) {
                for (Map.Entry<String, Object> entry: errorMessageMap.entrySet()) {
                    result.addFailure(this, new AssertionFailedError(entry.getKey() + ": " + entry.getValue()));
                }
            }

        } catch (MiniLangException e) {
            result.addError(this, e);
        }

        result.endTest(this);
    }
}
