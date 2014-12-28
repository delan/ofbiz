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
package org.ofbiz.widget.screen;

import java.util.Map;

import org.ofbiz.base.util.UtilGenerics;
import org.ofbiz.base.util.string.FlexibleStringExpander;
import org.ofbiz.widget.ModelWidget;
import org.ofbiz.widget.ModelWidgetCondition;
import org.w3c.dom.Element;

/**
 * Models the &lt;condition&gt; element.
 * 
 * @see <code>widget-screen.xsd</code>
 */
@SuppressWarnings("serial")
public class ModelScreenCondition extends ModelWidgetCondition {

    /*
     * ----------------------------------------------------------------------- *
     *                     DEVELOPERS PLEASE READ
     * ----------------------------------------------------------------------- *
     * 
     * This model is intended to be a read-only data structure that represents
     * an XML element. Outside of object construction, the class should not
     * have any behaviors.
     * 
     * Instances of this class will be shared by multiple threads - therefore
     * it is immutable. DO NOT CHANGE THE OBJECT'S STATE AT RUN TIME!
     * 
     */

    public static final String module = ModelScreenCondition.class.getName();
    public static final ConditionFactory SCREEN_CONDITION_FACTORY = new ScreenConditionFactory();

    public ModelScreenCondition(ModelScreen modelScreen, Element conditionElement) {
        super(SCREEN_CONDITION_FACTORY, modelScreen, conditionElement);
    }

    public static class IfEmptySection extends ModelWidgetCondition implements Condition {
        private final FlexibleStringExpander sectionExdr;

        private IfEmptySection(ConditionFactory factory, ModelWidget modelWidget, Element condElement) {
            super (factory, modelWidget, condElement);
            this.sectionExdr = FlexibleStringExpander.getInstance(condElement.getAttribute("section-name"));
        }

        @Override
        public boolean eval(Map<String, Object> context) {
            Map<String, Object> sectionsMap = UtilGenerics.toMap(context.get("sections"));
            return !sectionsMap.containsKey(this.sectionExdr.expandString(context));
        }
    }

    public static class ScreenConditionFactory extends DefaultConditionFactory {

        @Override
        public Condition newInstance(ModelWidget modelWidget, Element conditionElement) {
            if (conditionElement == null) {
                return DefaultConditionFactory.TRUE;
            }
            if ("if-empty-section".equals(conditionElement.getNodeName())) {
                return new IfEmptySection(this, modelWidget, conditionElement);
            } else {
                return super.newInstance(modelWidget,conditionElement);
            }
        }
    }
}
