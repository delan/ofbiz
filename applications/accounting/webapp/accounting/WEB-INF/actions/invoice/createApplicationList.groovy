/*
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
 */

import java.util.*;
import java.math.BigDecimal;
import org.ofbiz.entity.*;
import org.ofbiz.base.util.*;
import org.ofbiz.base.util.collections.*;
import org.ofbiz.accounting.invoice.*;
import org.ofbiz.accounting.payment.*;
import java.text.DateFormat;
import java.text.*;
import java.text.NumberFormat;

int decimals = UtilNumber.getBigDecimalScale("invoice.decimals");
int rounding = UtilNumber.getBigDecimalRoundingMode("invoice.rounding");
ZERO = BigDecimal.ZERO;

invoiceId = request.getParameter("invoiceId");
if (!invoiceId) invoiceId = context.invoiceId;
invoice = delegator.findByPrimaryKey("Invoice", [invoiceId : invoiceId]);
tabButtonItem = context.tabButtonItem;

invoiceItems = [];  // to pass back to the screeen with payment applications added
if (invoice) {    
    // retrieve related applications with null itemnumber
    invoiceAppl = null;  
    invoiceAppls = delegator.findByAnd("PaymentApplication", [invoiceId : invoiceId, invoiceItemSeqId : null]);
    invoiceAppls.each { invoiceAppl ->
        itemmap = [:];
        itemmap.invoiceId = invoiceId;
        itemmap.invoiceItemSeqId = invoiceAppl.getString("invoiceItemSeqId"));
        itemmap.total = InvoiceWorker.getInvoiceTotalBd(invoice).doubleValue();
        itemmap.paymentApplicationId = invoiceAppl.paymentApplicationId;
        itemmap.paymentId = invoiceAppl.paymentId;
        itemmap.billingAccountId = invoiceAppl.billingAccountId;
        itemmap.taxAuthGeoId = invoiceAppl.taxAuthGeoId;
        itemmap.amountToApply = invoiceAppl.amountApplied;
        itemmap.amountApplied = invoiceAppl.amountApplied;
        invoiceItems.add(itemmap);
    }

	
	// retrieve related applications with an existing itemnumber
    invoice.getRelated("InvoiceItem").each { item ->
        BigDecimal itemTotal = null;
        if (item.amount != null) {
              if (item.quantity == null || item.getBigDecimal("quantity").compareTo(ZERO) == 0) {
                  itemTotal = item.getBigDecimal("amount");
              } else {
                  itemTotal = item.getBigDecimal("amount").multiply(item.getBigDecimal("quantity"));
              }
        }

        // get relation payment applications for every item(can be more than 1 per item number)
        paymentApplications = item.getRelated("PaymentApplication");
        if (paymentApplications) {
              paymentApplications.each { paymentApplication ->
                  itemmap = [:];
                  itemmap.putAll(item);
                  itemmap.total = NumberFormat.getInstance(locale).format(itemTotal);
                  itemmap.paymentApplicationId = paymentApplication.paymentApplicationId;
                  itemmap.paymentId = paymentApplication.paymentId;
                  itemmap.toPaymentId = paymentApplication.toPaymentId;
                  itemmap.amountApplied = paymentApplication.getBigDecimal("amountApplied");
                  itemmap.amountToApply = paymentApplication.getBigDecimal("amountApplied");
                  itemmap.billingAccountId = paymentApplication.billingAccountId;
                  itemmap.taxAuthGeoId = paymentApplication.taxAuthGeoId;
                  invoiceItems.add(itemmap);
              }
        }

/*
        // create an extra line for input when not completely applied but not in the overview 
        if (tabButtonItem.equals("invoiceOverview") != true && 
                      (paymentApplications == null || paymentApplications.size() == 0 
                      || (applied < itemTotal && appliedAmount < invoiceAmount)))    {
                  Map itemmap = new HashMap();
                  itemmap.putAll(item);
                  itemmap.put("total",itemTotal);
                  itemmap.put("paymentApplicationId","");
                  itemmap.put("paymentId","");
                  itemmap.put("amountToApply", NumberFormat.getNumberInstance(locale).format(itemTotal - applied));
                  itemmap.put("billingAccountId","");
                  itemmap.put("taxAuthGeoId","");
                  invoiceItems.add(itemmap);
        }
*/
    }
	context.invoice = invoice;
	context.invoiceId = invoiceId;
}

if(invoiceItems) context.put("invoiceApplications",invoiceItems);
