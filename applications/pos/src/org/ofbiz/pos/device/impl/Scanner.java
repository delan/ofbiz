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
package org.ofbiz.pos.device.impl;

import jpos.JposException;
import jpos.ScannerConst;

import org.ofbiz.base.util.Debug;
import org.ofbiz.pos.adaptor.DataEventAdaptor;
import org.ofbiz.pos.device.GenericDevice;
import org.ofbiz.pos.screen.PosScreen;
import org.ofbiz.pos.event.MenuEvents;

/**
 *
 * @author     <a href="mailto:jaz@ofbiz.org">Andy Zeneski</a>
 * @version    $Rev$
 * @since      3.2
 */
public class Scanner extends GenericDevice {

    public static final String module = Scanner.class.getName();

    protected String deviceName = null;
    protected int timeout = -1;

    public Scanner(String deviceName, int timeout) {
        super(deviceName, timeout);
        this.control = new jpos.Scanner();
    }

    protected void initialize() throws JposException {
        Debug.logInfo("Scanner [" + control.getPhysicalDeviceName() + "] Claimed : " + control.getClaimed(), module);
        final jpos.Scanner scanner = (jpos.Scanner) control;

        // tell the driver to decode the scanned data
        scanner.setDecodeData(true);

        // create the new listner
        scanner.addDataListener(new DataEventAdaptor() {

            public void dataOccurred(jpos.events.DataEvent event) {
                byte[] scanData = null;
                int dataType = ScannerConst.SCAN_SDT_UNKNOWN;

                try {
                    dataType = scanner.getScanDataType();
                    scanData = scanner.getScanDataLabel();
                    if (scanData == null || scanData.length == 0) {
                        Debug.logWarning("Scanner driver does not support decoding data; the raw result is used instead", module);
                        scanData = scanner.getScanData();
                    }
                    
                    scanner.clearInput();
                } catch (jpos.JposException e) {
                    Debug.logError(e, module);
                }

                processScanData(scanData, dataType);
            }
        });
    }

    protected void processScanData(byte[] data, int dataType) {
        if (data != null) {
            // make sure we are on the main POS screen
            if (!"main/pospanel".equals(PosScreen.currentScreen.getName())) {
                PosScreen.currentScreen.showPage("pospanel");
            }
            
            // we can add some type checking here if needed (i.e. type of barcode; type of SKU, etc)
            if (dataType == ScannerConst.SCAN_SDT_UNKNOWN) {
                Debug.logWarning("Scanner type checking problems - check scanner driver", module);
            }

            // stuff the data to the Input component
            PosScreen.currentScreen.getInput().clearInput();
            PosScreen.currentScreen.getInput().appendString(new String(data));

            // call the ENTER event
            //this.callEnter();
            MenuEvents.addItem(PosScreen.currentScreen, null);
        }
    }
}

