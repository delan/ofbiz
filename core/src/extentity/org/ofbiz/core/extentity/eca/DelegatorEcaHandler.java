/*
 * $Id$
 *
 * Copyright (c) 2003 The Open For Business Project - www.ofbiz.org
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
package org.ofbiz.core.extentity.eca;

import java.util.*;

import org.ofbiz.core.util.*;
import org.ofbiz.core.service.*;
import org.ofbiz.core.entity.*;
import org.ofbiz.core.entity.eca.*;
import org.ofbiz.core.extentity.*;

/**
 * EntityEcaUtil
 *
 * @author     <a href="mailto:jonesde@ofbiz.org">David E. Jones</a>
 * @author     <a href="mailto:jaz@ofbiz.org">Andy Zeneski</a>
 * @version    $Revision$
 * @since      2.1
 */
public class DelegatorEcaHandler implements EntityEcaHandler {

    public static final String module = DelegatorEcaHandler.class.getName();

    protected GenericDelegator delegator = null;
    protected String delegatorName = null;
    protected String entityEcaReaderName = null;
    protected DispatchContext dctx = null;

    public DelegatorEcaHandler() { }

    public void setDelegator(GenericDelegator delegator) {
        this.delegator = delegator;
        this.delegatorName = delegator.getDelegatorName();
        this.entityEcaReaderName = EntityEcaUtil.getEntityEcaReaderName(this.delegatorName);
        this.dctx = EntityServiceFactory.getDispatchContext(delegator);
    }

    public Map getEntityEventMap(String entityName) {
        Map ecaCache = EntityEcaUtil.readConfig(this.entityEcaReaderName);
        if (ecaCache == null) return null;
        return (Map) ecaCache.get(entityName);
    }

    public void evalRules(String currentOperation, Map eventMap, String event, GenericEntity value, boolean isError) throws GenericEntityException {
        // if the eventMap is passed we save a HashMap lookup, but if not that's okay we'll just look it up now
        if (eventMap == null) eventMap = this.getEntityEventMap(value.getEntityName());
        if (eventMap == null || eventMap.size() == 0) {
            return;
        }

        List rules = (List) eventMap.get(event);
        if (rules == null || rules.size() == 0) {
            return;
        }
        
        Iterator i = rules.iterator();
        if (i.hasNext() && Debug.verboseOn()) Debug.logVerbose("Running ECA (" + event + ").", module);
        while (i.hasNext()) {
            EntityEcaRule eca = (EntityEcaRule) i.next();
            eca.eval(currentOperation, this.dctx, value, isError);
        }
    }
}
