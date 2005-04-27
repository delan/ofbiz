/*
 * $Id: FlexibleStringExpander.java 3102 2004-08-20 21:45:02Z jaz $
 *
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
 */
package org.ofbiz.base.util;

/**
 * <p/>
 * <p>
 * Parses input to strip ignorable characters, deal with nulls, etc.
 * </p>
 * <br/>
 * <br/>
 * Created on Oct 2, 2004 by dustin
 */
public class UtilParse {

    /**
     * return nulls for empty strings, as the entity engine can deal with nulls. This will provide blanks
     * in fields where doubles display. Blank meaning null, vs. 0 which means 0
     * @param doubleString
     * @return
     */
    public static Double parseDoubleForEntity(String doubleString) throws NumberFormatException {
        if (doubleString == null) {
            return null;
        }
        doubleString = doubleString.trim();
        doubleString = doubleString.replaceAll(",", "");
        if (doubleString.length() < 1) {
            return null;
        }
        return new Double(doubleString);
    }
}
