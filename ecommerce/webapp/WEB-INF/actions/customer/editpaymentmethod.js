/*
 *  Copyright (c) 2003 The Open For Business Project - www.ofbiz.org
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a 
 *  copy of this software and associated documentation files (the "Software"), 
 *  to deal in the Software without restriction, including without limitation 
 *  the rights to use, copy, modify, merge, publish, distribute, sublicense, 
 *  and/or sell copies of the Software, and to permit persons to whom the 
 *  Software is furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included 
 *  in all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS 
 *  OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
 *  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
 *  IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY 
 *  CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT 
 *  OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR 
 *  THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 *@author     David E. Jones
 *@version    1.0
 */

importClass(Packages.java.util.HashMap);
importClass(Packages.org.ofbiz.core.util.SiteDefs);
importClass(Packages.org.ofbiz.core.util.UtilHttp);
importClass(Packages.org.ofbiz.commonapp.accounting.payment.PaymentWorker);
importClass(Packages.org.ofbiz.commonapp.party.contact.ContactMechWorker);

var userLogin = session.getAttribute("userLogin");
var security = request.getAttribute("security");

var paymentResults = PaymentWorker.getPaymentMethodAndRelated(request, userLogin.getString("partyId")) 
//returns the following: "paymentMethod", "creditCard", "eftAccount", "paymentMethodId", "curContactMechId", "donePage", "tryEntity"
context.put("paymentMethod", paymentResults.get("paymentMethod"));
context.put("creditCard", paymentResults.get("creditCard"));
context.put("eftAccount", paymentResults.get("eftAccount"));
context.put("paymentMethodId", paymentResults.get("paymentMethodId"));
context.put("curContactMechId", paymentResults.get("curContactMechId"));
context.put("donePage", paymentResults.get("donePage"));
context.put("tryEntity", paymentResults.get("tryEntity"));

var curPostalAddressResults = ContactMechWorker.getCurrentPostalAddress(request, userLogin.getString("partyId"), paymentResults.get("curContactMechId")); 
//returns the following: "curPartyContactMech", "curContactMech", "curPostalAddress", "curPartyContactMechPurposes"
context.put("curPartyContactMech", curPostalAddressResults.get("curPartyContactMech"));
context.put("curContactMech", curPostalAddressResults.get("curContactMech"));
context.put("curPostalAddress", curPostalAddressResults.get("curPostalAddress"));
context.put("curPartyContactMechPurposes", curPostalAddressResults.get("curPartyContactMechPurposes"));

var postalAddressInfos = ContactMechWorker.getPartyPostalAddresses(request, userLogin.getString("partyId"), paymentResults.get("curContactMechId"));
context.put("postalAddressInfos", postalAddressInfos);

//prepare "Data" maps for filling form input boxes
var parameterMap = UtilHttp.getParameterMap(request);
var tryEntity = paymentResults.get("tryEntity");

var creditCardData = paymentResults.get("creditCard");
if (!tryEntity) creditCardData = parameterMap;
if (creditCardData == null) creditCardData = new HashMap();
context.put("creditCardData", creditCardData);

var eftAccountData = paymentResults.get("eftAccount");
if (!tryEntity) eftAccountData = parameterMap;
if (eftAccountData == null) eftAccountData = new HashMap();
context.put("eftAccountData", eftAccountData);

//prepare security flag
if (!security.hasEntityPermission("PARTYMGR", "_VIEW", session) && (context.get("creditCard") != null || context.get("eftAccount") != null) && context.get("paymentMethod") != null && !userLogin.get("partyId").equals((context.get("paymentMethod")).get("partyId"))) {
    context.put("canNotView", true);
} else {
    context.put("canNotView", false);
}

