/*
 * $Id: Journal.java,v 1.3 2004/08/15 21:26:41 ajzeneski Exp $
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
package org.ofbiz.pos.component;

import net.xoetrope.swing.XTable;
import net.xoetrope.xui.XPage;
import net.xoetrope.xui.data.XModel;

import org.ofbiz.base.util.Debug;
import org.ofbiz.pos.PosTransaction;
import org.ofbiz.pos.screen.PosScreen;

/**
 * 
 * @author     <a href="mailto:jaz@ofbiz.org">Andy Zeneski</a>
 * @version    $Revision: 1.3 $
 * @since      3.1
 */
public class Journal {

    public static final String module = Journal.class.getName();

    private static String[] field = { "sku", "desc", "qty", "price" };
    private static String[] name = { "SKU", "ITEM", "QTY", "AMT" };
    private static int[] width = { 100, 170, 60, 80};

    protected XTable jtable = null;
    protected String style = null;

    public Journal(XPage page) {
        this.jtable = (XTable) page.findComponent("jtable");

        // set the table as selectable
        jtable.setInteractiveTable(true);
        jtable.setFocusable(false);

        // set the styles
        jtable.setBorderStyle("journalBorder");
        jtable.setHeaderStyle("journalHeader");
        jtable.setStyle("journalData");
        jtable.setSelectedStyle("journalSelected");

        // initialize the journal table header
        XModel jmodel = createModel();
        if (jmodel != null) {
            this.appendEmpty(jmodel);
            jtable.setModel(jmodel);

            for (int i = 0; i < width.length; i++) {
                jtable.setColWidth(i, width[i]);
            }
        }
        jtable.setSelectedRow(0);
    }

    public String getSelectedSku() {
        XModel jmodel = (XModel) XModel.getInstance().get("journal/items");
        Debug.log("Selected Index : " + jtable.getSelectedRow(), module);
        XModel model = jmodel.get(jtable.getSelectedRow() + 1);
        return model.getValueAsString("sku");
    }
    
    public void selectNext() {
        jtable.next();
    }

    public void selectPrevious() {
        jtable.prev();
    }

    public void focus() {
        if (jtable.isEnabled()) {
            jtable.requestFocus();
        }
    }

    public void setLock(boolean lock) {
        jtable.setInteractiveTable(!lock);
        jtable.setFocusable(!lock);
        jtable.setVisible(!lock);
        jtable.setEnabled(!lock);
    }

    public void refresh(PosScreen pos) {
        if (!jtable.isEnabled()) {
            // no point in refreshing when we are locked;
            // we will auto-refresh when unlocked
            return;
        }

        PosTransaction tx = PosTransaction.getCurrentTx(pos.getSession());
        XModel jmodel = this.createModel();
        if (!tx.isEmpty()) {
            tx.appendItemDataModel(jmodel);
            this.appendEmpty(jmodel);
            tx.appendTotalDataModel(jmodel);
            if (tx.selectedPayments() > 0) {
                this.appendEmpty(jmodel);
                tx.appendPaymentDataModel(jmodel);
            }
        } else {
            this.appendEmpty(jmodel);
        }

        // make sure we are at the last item in the journal
        jtable.setSelectedRow(0);

        try {
            jtable.repaint();
        } catch (ArrayIndexOutOfBoundsException e) {
            // bug in XUI causes this; ignore for now
            // it has been reported and will be fixed soon
        }
        Debug.log("Selected Row : " + jtable.getSelectedRow(), module);
    }

    private XModel createModel() {
        XModel jmodel = (XModel) XModel.getInstance().get("journal/items");

        // clear the list
        jmodel.clear();

        if (field.length == 0) {
            return null;
        }

        // create the header
        XModel headerNode = appendNode(jmodel, "th", "", "");
        for (int i = 0 ; i < field.length; i++) {
            appendNode(headerNode, "td", field[i], name[i]);
        }

        return jmodel;
    }

    private void appendEmpty(XModel jmodel) {
        XModel headerNode = appendNode(jmodel, "tr", "", "");
        for (int i = 0 ; i < field.length; i++) {
            appendNode(headerNode, "td", field[i], "");
        }
    }

    public static XModel appendNode(XModel node, String tag, String id, String value) {
        XModel newNode = (XModel) node.append(id);
        newNode.setTagName(tag);
        if (value != null) {
            newNode.set(value);
        }
        return newNode;
    }
}
