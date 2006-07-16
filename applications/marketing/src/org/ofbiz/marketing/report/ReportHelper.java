/*
 *
 * Copyright 2001-2006 The Apache Software Foundation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.ofbiz.marketing.report;

import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.Debug;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.marketing.tracking.TrackingCodeEvents;

import java.util.*;

/**
 * Marketing Report Helper
 *
 * @author <a href="mailto:mujinsong@gmail.com">Mu Jinsong</a>
 * @author <a href="mailto:sichen@opensourcestrategies.com">Si Chen</a>
 * @version $Rev:  6395
 * @since 3.2
 */
public class ReportHelper {

    public static final String module = ReportHelper.class.getName();
    
/**
 * Calculate conversion rates based on a List of visits and orders.  Designed to be used for reporting on 
 * tracking code or marketing campaigns
 * @param visits
 * @param orders
 * @param keyFieldName - name of key field for visits and orders Lists, ie "trackingCodeId" or "marketingCampaignId"
 * @return a List of Maps with keys (${keyFieldName}, visits - # visits, orders - # orders, orderAmount - total amount of orders,
 * conversionRate - # orders/# visits
 */
    public static List calcConversionRates(List visits, List orders, String keyFieldName) {
        List conversionRates = new ArrayList();
        
        // loop through all the visits
        for (Iterator vit = visits.iterator(); vit.hasNext(); ) {
            GenericValue visit = (GenericValue) vit.next();
            Map reportValue = new HashMap();
            reportValue.put(keyFieldName, visit.getString(keyFieldName));
            reportValue.put("visits", visit.getLong("visitId")); // actually # of visits
            
            // find the matching entry in orders for the given key field
            List ordersForThisKey = EntityUtil.filterByAnd(orders, UtilMisc.toMap(keyFieldName, visit.getString(keyFieldName)));
            
            // if there are matching orders, then calculate orders, order amount, and conversion rate
            if ((ordersForThisKey != null) && (ordersForThisKey.size() > 0)) {
                // note: there should be only one line of order stats per key, so .get(0) should work
                GenericValue orderValue = (GenericValue) ordersForThisKey.get(0);

                reportValue.put("orders", orderValue.getLong("orderId")); // # of orders
                if (orderValue.getDouble("grandTotal") == null) {
                    reportValue.put("orderAmount", new Double(0));                    
                } else {
                    reportValue.put("orderAmount", orderValue.getDouble("grandTotal")); 
                }
                if ((orderValue.getLong("orderId") == null) || (visit.getLong("visitId") == null) || 
                    (visit.getLong("visitId").intValue() == 0)) {
                    reportValue.put("conversionRate", new Double(0));
                } else {
                    reportValue.put("conversionRate", new Double(orderValue.getLong("orderId").doubleValue() / visit.getLong("visitId").doubleValue()));    
                }
            } else {
                // no matching orders - all those values are zeroes
                reportValue.put("orders", new Long(0));
                reportValue.put("orderAmount", new Double(0));
                reportValue.put("conversionRate", new Double(0));
            }
            
            conversionRates.add(reportValue);
        }
        
        return conversionRates;
    }
}




