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
package org.ofbiz.pos.component;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import javax.swing.border.Border;

import net.xoetrope.swing.XButton;
import net.xoetrope.xui.XProjectManager;
import net.xoetrope.xui.style.XStyle;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilValidate;

/**
 * 
 * @author     <a href="mailto:jaz@ofbiz.org">Andy Zeneski</a>
 * @version    $Rev$
 * @since      3.1
 */
public class PosButtonWrapper {

    public static final String module = PosButtonWrapper.class.getName();
    private static final String prefix = "<html><center><font color=\"${FCOLOR}\">";
    private static final String suffix = "</font></center></html>";

    private static final String disabledAll = "buttonDisabled";
    private static final String enabledMenu = "posButton";
    private static final String enabledNum = "numButton";
    private static final String enabledSku = "skuButton";
    private static final String enabledAlt = "altButton";

    protected XStyle disabledStyle = null;
    protected XStyle enabledStyle = null;
    protected XButton xbutton = null;

    protected boolean isEnabled = true;
    protected String origText = null;
    protected String name = null;

    public PosButtonWrapper(XButton button) {
        this.xbutton = button;
        this.name = xbutton.getName();
        this.origText = xbutton.getText();

        // load the disabled style
        this.disabledStyle = XProjectManager.getStyleManager().getStyle(disabledAll);
        if (this.disabledStyle == null) {
            Debug.logError("ERROR: The disabled button style \"buttonDisabled\" was not found!", module);
        }

        /// load the enabled style
        if (name != null && name.startsWith("num")) {
            this.enabledStyle = XProjectManager.getStyleManager().getStyle(enabledNum);
            if (this.enabledStyle == null) {
                Debug.logError("ERROR: The enabled button style \""+ enabledNum + "\" was not found!", module);
            }
        } else if (name != null && name.startsWith("SKU.")) {
            this.enabledStyle = XProjectManager.getStyleManager().getStyle(enabledSku);
            if (this.enabledStyle == null) {
                Debug.logError("ERROR: The enabled button style \""+ enabledSku + "\" was not found!", module);
            }
        } else if (name != null && name.startsWith("ALT.")) {
            this.enabledStyle = XProjectManager.getStyleManager().getStyle(enabledAlt);
            if (this.enabledStyle == null) {
                Debug.logError("ERROR: The enabled button style \""+ enabledAlt + "\" was not found!", module);
            }
        } else {
            this.enabledStyle = XProjectManager.getStyleManager().getStyle(enabledMenu);
            if (this.enabledStyle == null) {
                Debug.logError("ERROR: The enabled button style \""+ enabledMenu + "\" was not found!", module);
            }
        }

        // wrap the text in HTML and set colors
        try {
            this.updateText();
        } catch (Throwable t) {
            Debug.logError(t, module);
        }
    }

    public void setEnabled(boolean enable) {
        this.isEnabled = enable;
        this.updateText();
        xbutton.setEnabled(enable);
    }

    public void updateText() {
        // no text to output; nothing to do
        if (UtilValidate.isEmpty(this.origText)) {
            return;
        }

        StringBuffer newContent = new StringBuffer();
        XStyle style = null;
        if (this.isEnabled) {
            style = enabledStyle;
        } else {
            style = disabledStyle;
        }

        // get the hex color for the current style
        String fcolor = Integer.toHexString(style.getStyleAsColor(XStyle.COLOR_FORE).getRGB() & 0x00ffffff);        
        xbutton.setBackground(style.getStyleAsColor(XStyle.COLOR_BACK));

        // add the # for the HTML color
        if (fcolor.equals("0")) {
            fcolor = "#000000";
        } else {
            fcolor = "#" + fcolor;
        }

        // get the additional styles
        boolean isItalic = false;
        boolean isBold = false;
        if (style.getStyleAsInt(XStyle.FONT_WEIGHT) > 0) {
            isBold = true;
        }
        if (style.getStyleAsInt(XStyle.FONT_ITALIC) > 0) {
            isItalic = true;
        }

        // open with prefix (opening tags)
        newContent.append(prefix.replaceAll("\\$\\{FCOLOR\\}", fcolor));

        // add in additional styles
        if (isBold) {
            newContent.append("<b>");
        }
        if (isItalic) {
            newContent.append("<i>");
        }

        // the actual wrapped text
        newContent.append(wrapText(origText, "<BR>", 0));


        // close the additional styles
        if (isItalic) {
            newContent.append("</i>");
        }
        if (isBold) {
            newContent.append("</b>");
        }

        // append the suffix (closing tags)
        newContent.append(suffix);

        // set the button font
        Font font = xbutton.getFont().deriveFont(Font.PLAIN);
        xbutton.setFont(font);

        // update the button text
        xbutton.setRolloverEnabled(false);
        xbutton.setText(newContent.toString());

        //Debug.log("Button [" + name + "] = " + xbutton.getText(), module);
    }

    public String wrapText(String text, String newLine, int padding) {
        // nothing to do
        if (UtilValidate.isEmpty(text)) {
            return "";
        }

        FontMetrics fm = xbutton.getFontMetrics(xbutton.getFont());
        Graphics g = xbutton.getGraphics();
        Border b = xbutton.getBorder();
        StringBuffer buf = new StringBuffer();

        int leftBorder = b.getBorderInsets(xbutton).left;
        int rightBorder = b.getBorderInsets(xbutton).right;
        int topBorder = b.getBorderInsets(xbutton).top;
        int bottomBorder = b.getBorderInsets(xbutton).bottom;

        int padWidth = ((int) (fm.getStringBounds((new char[] {(char) 32}), 0, 1, g).getWidth()) * padding);
        int butWidth = (((xbutton.getSize().width) - leftBorder - rightBorder) - padWidth);
        int maxLines = ((xbutton.getSize().height - topBorder - bottomBorder) / fm.getHeight());
        int strWidth = (int) fm.getStringBounds(text, g).getWidth();

        if (strWidth <= butWidth) {
            return text;
        }

        // if not we need to wrap the text
        int lineNumber = 0;
        while (text.length() > 0) {
            int thisPosition = this.getMaxStrIndex(fm, g, text, butWidth);
            int space = -1;
            String line = null;

            // the end of the string has been reached
            if (thisPosition == text.length()) {
                line = text;
                buf.append(line);
                text = new String();
                break;
            }

            line = text.substring(0, thisPosition);
            space = line.lastIndexOf(32); // last space

            // we can only wrap if :
            // 1) we have a space available
            // 2) the next character is a space
            if (space == -1 && ((int)text.charAt(thisPosition)) != 32) {
                buf.append(text);
                break;
            } else {
                if (space != -1) {
                    // we found a space; use that location to wrap
                    thisPosition = space;
                    line = line.substring(0, thisPosition);

                    // move forward one to trim off the space
                    thisPosition++;
                }

                // increment the line counter; we've added a line
                lineNumber++;

                // make sure we don't have too many lines;
                // if so, trim it down ...
                if (lineNumber >= maxLines) {
                    int dotWidth = (int) fm.getStringBounds("...", g).getWidth();
                    int maxLineIdx = this.getMaxStrIndex(fm, g, text, (butWidth - dotWidth));
                    line = text.substring(0, maxLineIdx);
                    buf.append(line);
                    buf.append("...");
                    break;
                } else {
                    text = text.substring(thisPosition);
                    buf.append(line);
                    buf.append(newLine);
                }
            }
        }

        return buf.toString();
    }

    private int getMaxStrIndex(FontMetrics fm, Graphics g, String str, int width) {
        for (int i = str.length(); i > 0; i--) {
            double widthTest = fm.getStringBounds(str.substring(0, i), g).getWidth();
            if (widthTest < width) {
                return i;
            }
        }
        return str.length();
    }
}
