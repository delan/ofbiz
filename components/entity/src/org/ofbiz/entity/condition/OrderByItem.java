/*
 * $Id: OrderByItem.java,v 1.4 2004/07/23 17:02:10 doogie Exp $
 *
 * <p>Copyright (c) 2004 The Open For Business Project - www.ofbiz.org
 *
 * <p>Permission is hereby granted, free of charge, to any person obtaining a
 *  copy of this software and associated documentation files (the "Software"),
 *  to deal in the Software without restriction, including without limitation
 *  the rights to use, copy, modify, merge, publish, distribute, sublicense,
 *  and/or sell copies of the Software, and to permit persons to whom the
 *  Software is furnished to do so, subject to the following conditions:
 *
 * <p>The above copyright notice and this permission notice shall be included
 *  in all copies or substantial portions of the Software.
 *
 * <p>THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 *  OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 *  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 *  IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 *  CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT
 *  OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR
 *  THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package org.ofbiz.entity.condition;

import java.util.Comparator;

import org.ofbiz.entity.GenericEntity;
import org.ofbiz.entity.GenericModelException;
import org.ofbiz.entity.config.DatasourceInfo;
import org.ofbiz.entity.model.ModelEntity;

public class OrderByItem implements Comparator {
    public static final int DEFAULT = 0;
    public static final int UPPER   = 1;
    public static final int LOWER   = 2;

    protected boolean descending;
    protected EntityConditionValue value;

    public OrderByItem(EntityConditionValue value) {
        this.value = value;
    }

    public OrderByItem(EntityConditionValue value, boolean descending) {
        this(value);
        this.descending = descending;
    }

    public EntityConditionValue getValue() {
        return value;
    }

    public boolean getDescending() {
        return descending;
    }

    public static final OrderByItem parse(Object obj) {
        if (obj instanceof String) {
            return parse((String) obj);
        } else if (obj instanceof EntityConditionValue) {
            return new OrderByItem((EntityConditionValue) obj, false);
        } else if (obj instanceof OrderByItem) {
            return (OrderByItem) obj;
        } else {
            throw new IllegalArgumentException("unknown orderBy item: " + obj);
        }
    }

    public static final OrderByItem parse(String text) {
        text = text.trim();
        int startIndex = 0, endIndex = text.length();
        boolean descending;
        int caseSensitivity;
        if (text.endsWith(" DESC")) {
            descending = true;
            endIndex -= 5;
        } else if (text.endsWith(" ASC")) {
            descending = false;
            endIndex -= 4;
        } else if (text.startsWith("-")) {
            descending = true;
            startIndex++;
        } else if (text.startsWith("+")) {
            descending = false;
            startIndex++;
        } else {
            descending = false;
        }

        if (startIndex != 0 || endIndex != text.length()) {
            text = text.substring(startIndex, endIndex);
            startIndex = 0;
            endIndex = text.length();
        }

        if (text.endsWith(")")) {
            String upperText = text.toUpperCase();
            endIndex--;
            if (upperText.startsWith("UPPER(")) {
                caseSensitivity = UPPER;
                startIndex = 6;
            } else if (upperText.startsWith("LOWER(")) {
                caseSensitivity = LOWER;
                startIndex = 6;
            } else {
                caseSensitivity = DEFAULT;
            }
        } else {
            caseSensitivity = DEFAULT;
        }

        if (startIndex != 0 || endIndex != text.length()) {
            text = text.substring(startIndex, endIndex);
            startIndex = 0;
            endIndex = text.length();
        }
        EntityConditionValue value = new EntityFieldValue(text);
        switch (caseSensitivity) {
            case UPPER:
                value = new EntityFunction.UPPER(value);
                break;
            case LOWER:
                value = new EntityFunction.LOWER(value);
                break;
        }
        return new OrderByItem(value, descending);
    }

    public int compare(java.lang.Object obj1, java.lang.Object obj2) {
        return compare((GenericEntity) obj1, (GenericEntity) obj2);
    }
        
    public void checkOrderBy(ModelEntity modelEntity) throws GenericModelException {
        value.validateSql(modelEntity);
    }

    public int compare(GenericEntity obj1, GenericEntity obj2) {
        Object value1 = value.getValue(obj1);
        Object value2 = value.getValue(obj2);

        int result;
        // null is defined as the largest possible value
        if (value1 == null) {
            result = value2 == null ? 0 : 1;
        } else if (value2 == null) {
            result = value1 == null ? 0 : -1;
        } else {
            result = ((Comparable) value1).compareTo(value2);
        }
        // if (Debug.infoOn()) Debug.logInfo("[OrderByComparator.compareAsc] Result is " + result + " for [" + value + "] and [" + value2 + "]", module);
        return descending ? -result : result;
    }

    public String makeOrderByString(ModelEntity modelEntity, boolean includeTablenamePrefix, DatasourceInfo datasourceInfo) {
        StringBuffer sb = new StringBuffer();
        makeOrderByString(sb, modelEntity, includeTablenamePrefix, datasourceInfo);
        return sb.toString();
    }

    public void makeOrderByString(StringBuffer sb, ModelEntity modelEntity, boolean includeTablenamePrefix, DatasourceInfo datasourceInfo) {
        getValue().addSqlValue(sb, modelEntity, null, includeTablenamePrefix, datasourceInfo);
        sb.append(descending ? " DESC" : " ASC");
    }

    public boolean equals(java.lang.Object obj) {
        if (!(obj instanceof OrderByItem)) return false;
        OrderByItem that = (OrderByItem) obj;

        return getValue().equals(that.getValue()) && getDescending() == that.getDescending();
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(getValue());
        sb.append(descending ? " DESC" : " ASC");
        return sb.toString();
    }
}
