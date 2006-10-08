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
package org.ofbiz.order.thirdparty.taxware;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ofbiz.entity.GenericValue;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.ModelService;

/**
 * TaxwareServices
 */
public class TaxwareServices {

    public static Map calcTax(DispatchContext dctx, Map context) {
        Map result = new HashMap();
        List items = (List) context.get("itemProductList");
        List amnts = (List) context.get("itemAmountList");
        List ishpn = (List) context.get("itemShippingList");
        Double shipping = (Double) context.get("orderShippingAmount");
        GenericValue address = (GenericValue) context.get("shippingAddress");

        if (items.size() != amnts.size()) {
            result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_ERROR);
            result.put(ModelService.ERROR_MESSAGE, "ERROR: Items, Amount, or ItemShipping lists are not valid size.");
            return result;
        }

        try {
            TaxwareUTL utl = new TaxwareUTL();

            utl.setShipping(shipping != null ? shipping.doubleValue() : 0.0);
            utl.setShipAddress(address);
            for (int i = 0; i < items.size(); i++) {
                GenericValue p = (GenericValue) items.get(i);
                Double amount = (Double) amnts.get(i);
                Double ishp = ishpn != null ? (Double) ishpn.get(i) : new Double(0.0);

                utl.addItem(p, amount.doubleValue(), ishp.doubleValue());
            }

            int resp = utl.process();

            if (resp == 0) {
                result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_ERROR);
                result.put(ModelService.ERROR_MESSAGE, "ERROR: No records processed.");
                return result;
            }

            result.put("orderAdjustments", utl.getOrderAdjustments());
            result.put("itemAdjustments", utl.getItemAdjustments());

        } catch (TaxwareException e) {
            e.printStackTrace();
            result.put(ModelService.RESPONSE_MESSAGE, ModelService.RESPOND_ERROR);
            result.put(ModelService.ERROR_MESSAGE, "ERROR: Taxware problem (" + e.getMessage() + ").");
        }

        return result;
    }

    public static Map verifyZip(DispatchContext dctx, Map context) {

        return new HashMap();
    }
}
