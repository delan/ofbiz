/*
 * $Id$
 *
 * Copyright (c) 2004 The Open For Business Project - www.ofbiz.org
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
package org.ofbiz.pos.event;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.GeneralException;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.pos.PosTransaction;
import org.ofbiz.pos.component.Input;
import org.ofbiz.pos.component.Output;
import org.ofbiz.pos.screen.PosScreen;

/**
 * 
 * @author     <a href="mailto:jaz@ofbiz.org">Andy Zeneski</a>
 * @version    $Rev$
 * @since      3.1
 */
public class PaymentEvents {

    public static final String module = PaymentEvents.class.getName();

    public static void payCash(PosScreen pos) {
        PosTransaction trans = PosTransaction.getCurrentTx(pos.getSession());
        try {
            double amount = processAmount(trans, pos, null);
            Debug.log("Processing [Cash] Amount : " + amount, module);

            // add the payment
            trans.addPayment("CASH", amount);
        } catch (GeneralException e) {
            // errors handled
        }

        pos.refresh();
    }

    public static void payCredit(PosScreen pos) {
        PosTransaction trans = PosTransaction.getCurrentTx(pos.getSession());
        Input input = pos.getInput();
        String[] msrInfo = input.getFunction("MSRINFO");
        String[] crtInfo = input.getFunction("CREDIT");

        if (crtInfo == null) {
            input.setFunction("CREDIT");
            pos.getOutput().print(Output.CREDNO);
        } else {
            Debug.log("Credit Func Info : " + crtInfo[1], module);
            if (msrInfo == null) {
                if (UtilValidate.isCreditCard(input.value())) {
                    input.setFunction("MSRINFO");
                    pos.getOutput().print(Output.CREDEX);
                } else {
                    input.clear();
                    pos.showDialog("main/dialog/error/invalidcardnumber");                    
                }
            } else {
                String msrInfoStr = msrInfo[1];
                if (UtilValidate.isNotEmpty(input.value())) {
                    if (UtilValidate.isNotEmpty(msrInfoStr)) {
                        msrInfoStr = msrInfoStr + "|" + input.value();
                    } else {
                        msrInfoStr = input.value();
                    }
                }
                input.setFunction("MSRINFO", msrInfoStr);
                String[] msrInfoArr = msrInfoStr.split("\\|");
                int allInfo = msrInfoArr.length;
                String firstName = null;
                String lastName = null;
                switch (allInfo) {
                    case 4:
                        lastName = msrInfoArr[3];
                    case 3:
                        firstName = msrInfoArr[2];
                    case 2: // card number & exp date found
                        double amount = 0;
                        try {
                            amount = processAmount(trans, pos, crtInfo[1]);
                            Debug.log("Processing Credit Card Amount : " + amount, module);
                        } catch (GeneralException e) {
                        }

                        String cardNumber = msrInfoArr[0];
                        String expDate = msrInfoArr[1];
                        String pmId = trans.makeCreditCardVo(cardNumber, expDate, firstName, lastName);
                        if (pmId != null) {
                            trans.addPayment(pmId, amount);
                        }
                        pos.refresh();
                        break;
                    case 1: // card number only found
                        pos.getOutput().print(Output.CREDEX);
                        break;
                    default:
                        Debug.log("Hit the default switch case [" + allInfo + "] refreshing.", module);
                        input.clearFunction("MSRINFO");
                        pos.getOutput().print(Output.CREDNO);
                        break;
                }
            }
        }        
    }

    public static void clearAllPayments(PosScreen pos) {
        PosTransaction trans = PosTransaction.getCurrentTx(pos.getSession());
        trans.clearPayments();
        pos.getInput().clear();
        pos.refresh();
    }

    public static void processSale(PosScreen pos) {
        PosTransaction trans = PosTransaction.getCurrentTx(pos.getSession());
        if (trans.getTotalDue() > 0) {
            pos.showDialog("main/dialog/error/notenoughfunds");
        } else {
            // manual locks (not secured; will be unlocked on clear)
            pos.getInput().setLock(true);
            pos.getButtons().setLock(true);
            pos.getInput().clear();
            pos.getInput().setFunction("PAID");

            // display change
            pos.refresh();

            // process the order
            try {
                trans.processSale();
            } catch (GeneralException e) {
                pos.showDialog("main/dialog/error/testerror");
            }
        }
    }

    private static double processAmount(PosTransaction trans, PosScreen pos, String amountStr) throws GeneralException {
        Input input = pos.getInput();

        if (input.isFunctionSet("TOTAL")) {
            String amtStr = amountStr != null ? amountStr : input.value();
            double amount;
            if (UtilValidate.isNotEmpty(amtStr)) {
                try {
                    amount = Double.parseDouble(amtStr);
                } catch (NumberFormatException e) {
                    Debug.logError("Invalid number for amount : " + amtStr, module);
                    pos.getOutput().print("Invalid Amount!");
                    input.clearInput();
                    throw new GeneralException();
                }
                amount = amount / 100; // convert to decimal
                Debug.log("Set amount / 100 : " + amount, module);
            } else {
                Debug.log("Amount is empty; assumption is full amount : " + trans.getTotalDue(), module);
                amount = trans.getTotalDue();
            }
            return amount;
        } else {
            Debug.log("TOTAL function NOT set", module);
            throw new GeneralException();
        }
    }
}
