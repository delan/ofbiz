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
import java.math.BigDecimal;
import org.ofbiz.order.order.OrderReadHelper;
import org.ofbiz.base.util.UtilMisc;

orderId = context.orderId;
orderPaymentPreferenceId = context.orderPaymentPreferenceId;

if ((!orderId) || (!orderPaymentPreferenceId)) return;

if (orderId) {
   orderHeader = delegator.findOne("OrderHeader", [orderId : orderId], false);
   context.orderHeader = orderHeader;
}

if (orderHeader) {
   orh = new OrderReadHelper(orderHeader);
   context.orh = orh;
   context.overrideAmount = orh.getOrderGrandTotal();
}

if (orderPaymentPreferenceId) {
   orderPaymentPreference = delegator.findOne("OrderPaymentPreference", [orderPaymentPreferenceId : orderPaymentPreferenceId], false);
   context.orderPaymentPreference = orderPaymentPreference;
}

if (orderPaymentPreference) {
   paymentMethodType = orderPaymentPreference.getRelatedOneCache("PaymentMethodType");
   context.paymentMethodType = paymentMethodType;
   context.overrideAmount = orderPaymentPreference.getBigDecimal("maxAmount");
}
