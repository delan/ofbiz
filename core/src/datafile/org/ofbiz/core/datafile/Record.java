/*
 * $Id$
 *
 * Copyright (c) 2001, 2002 The Open For Business Project - www.ofbiz.org
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
package org.ofbiz.core.datafile;

import java.util.*;
import java.text.*;
import java.io.*;

/**
 * Record
 *
 * @author     <a href="mailto:jonesde@ofbiz.org">David E. Jones</a>
 * @version    $Revision$
 * @since      2.0
 */
public class Record implements Serializable {

    /** Contains a map with field data by name */
    protected Map fields;

    /** Contains the name of the record definition */
    protected String recordName;

    /** Contains the definition for the record */
    protected transient ModelRecord modelRecord;

    protected Record parentRecord = null;
    protected List childRecords = new ArrayList();

    /** Creates new Record */
    protected Record(ModelRecord modelRecord) {
        if (modelRecord == null)
            throw new IllegalArgumentException("Cannont create a Record with a null modelRecord parameter");
        this.recordName = modelRecord.name;
        this.modelRecord = modelRecord;
        this.fields = new HashMap();
    }

    /** Creates new Record from existing Map */
    protected Record(ModelRecord modelRecord, Map fields) {
        if (modelRecord == null)
            throw new IllegalArgumentException("Cannont create a Record with a null modelRecord parameter");
        this.recordName = modelRecord.name;
        this.modelRecord = modelRecord;
        this.fields = (fields == null ? new HashMap() : new HashMap(fields));
    }

    public String getRecordName() {
        return recordName;
    }

    public ModelRecord getModelRecord() {
        if (modelRecord == null) {
            throw new IllegalStateException("[Record.getModelRecord] could not find modelRecord for recordName " + recordName);
        }
        return modelRecord;
    }

    public Object get(String name) {
        if (getModelRecord().getModelField(name) == null) {
            throw new IllegalArgumentException("[Record.get] \"" + name + "\" is not a field of " + recordName);
            // Debug.logWarning("[GenericRecord.get] \"" + name + "\" is not a field of " + recordName + ", but getting anyway...");
        }
        return fields.get(name);
    }

    public String getString(String name) {
        Object object = get(name);

        if (object == null)
            return null;
        if (object instanceof java.lang.String)
            return (String) object;
        else
            return object.toString();
    }

    public java.sql.Timestamp getTimestamp(String name) {
        return (java.sql.Timestamp) get(name);
    }

    public java.sql.Time getTime(String name) {
        return (java.sql.Time) get(name);
    }

    public java.sql.Date getDate(String name) {
        return (java.sql.Date) get(name);
    }

    public Integer getInteger(String name) {
        return (Integer) get(name);
    }

    public Long getLong(String name) {
        return (Long) get(name);
    }

    public Float getFloat(String name) {
        return (Float) get(name);
    }

    public Double getDouble(String name) {
        return (Double) get(name);
    }

    /** Sets the named field to the passed value, even if the value is null
     * @param name The field name to set
     * @param value The value to set
     */
    public void set(String name, Object value) {
        set(name, value, true);
    }

    /** Sets the named field to the passed value. If value is null, it is only
     *  set if the setIfNull parameter is true.
     * @param name The field name to set
     * @param value The value to set
     * @param setIfNull Specifies whether or not to set the value if it is null
     */
    public synchronized void set(String name, Object value, boolean setIfNull) {
        if (getModelRecord().getModelField(name) == null) {
            throw new IllegalArgumentException("[Record.set] \"" + name + "\" is not a field of " + recordName);
            // Debug.logWarning("[GenericRecord.set] \"" + name + "\" is not a field of " + recordName + ", but setting anyway...");
        }
        if (value != null || setIfNull) {
            if (value instanceof Boolean) {
                value = ((Boolean) value).booleanValue() ? "Y" : "N";
            }
            fields.put(name, value);
        }
    }

    /** Sets the named field to the passed value, converting the value from a String to the corrent type using <code>Type.valueOf()</code>
     * @param name The field name to set
     * @param value The String value to convert and set
     */
    public void setString(String name, String value) throws ParseException {
        if (name == null || value == null || value.equals(""))
            return;
        ModelField field = getModelRecord().getModelField(name);

        if (field == null)
            set(name, value); // this will get an error in the set() method...

        // if the string is all spaces ignore
        boolean nonSpace = false;

        for (int i = 0; i < value.length(); i++) {
            if (value.charAt(i) != ' ') {
                nonSpace = true;
                break;
            }
        }
        if (!nonSpace)
            return;

        // if (Debug.verboseOn()) Debug.logVerbose("Value: " + value);

        String fieldType = field.type;

        // first the custom types that need to be parsed
        if (fieldType.equals("CustomTimestamp")) {
            // this custom type will take a string a parse according to date formatting
            // string then put the result in a java.sql.Timestamp
            // a common timestamp format for flat files is with no separators: yyyyMMddHHmmss
            SimpleDateFormat sdf = new SimpleDateFormat(field.format);
            java.util.Date tempDate = sdf.parse(value);
            java.sql.Timestamp timestamp = new java.sql.Timestamp(tempDate.getTime());

            set(name, timestamp);
        } else if (fieldType.equals("CustomDate")) {
            // a common date only format for flat files is with no separators: yyyyMMdd or MMddyyyy
            SimpleDateFormat sdf = new SimpleDateFormat(field.format);
            java.util.Date tempDate = sdf.parse(value);
            java.sql.Date date = new java.sql.Date(tempDate.getTime());

            set(name, date);
        } else if (fieldType.equals("CustomTime")) {
            // a common time only format for flat files is with no separators: HHmmss
            SimpleDateFormat sdf = new SimpleDateFormat(field.format);
            java.util.Date tempDate = sdf.parse(value);
            java.sql.Time time = new java.sql.Time(tempDate.getTime());

            set(name, time);
        } else if (fieldType.equals("FixedPointDouble")) {
            // this custom type will parse a fixed point number according to the number
            // of decimal places in the formatting string then place it in a Double
            NumberFormat nf = NumberFormat.getNumberInstance();
            Number tempNum = nf.parse(value);
            double number = tempNum.doubleValue();
            double decimalPlaces = Double.parseDouble(field.format);
            double divisor = Math.pow(10.0, decimalPlaces);

            number = number / divisor;
            set(name, new Double(number));
        } // standard types
        else if (fieldType.equals("java.lang.String") || fieldType.equals("String"))
            set(name, value);
        else if (fieldType.equals("java.sql.Timestamp") || fieldType.equals("Timestamp"))
            set(name, java.sql.Timestamp.valueOf(value));
        else if (fieldType.equals("java.sql.Time") || fieldType.equals("Time"))
            set(name, java.sql.Time.valueOf(value));
        else if (fieldType.equals("java.sql.Date") || fieldType.equals("Date"))
            set(name, java.sql.Date.valueOf(value));
        else if (fieldType.equals("java.lang.Integer") || fieldType.equals("Integer"))
            set(name, Integer.valueOf(value));
        else if (fieldType.equals("java.lang.Long") || fieldType.equals("Long"))
            set(name, Long.valueOf(value));
        else if (fieldType.equals("java.lang.Float") || fieldType.equals("Float"))
            set(name, Float.valueOf(value));
        else if (fieldType.equals("java.lang.Double") || fieldType.equals("Double"))
            set(name, Double.valueOf(value));
        else {
            throw new IllegalArgumentException("Field type " + fieldType + " not currently supported. Sorry.");
        }
    }

    public String getFixedString(String name) {
        if (name == null)
            return null;
        if (getModelRecord() == null)
            throw new IllegalArgumentException("Could not find modelrecord for field named \"" + name + "\"");
        ModelField field = getModelRecord().getModelField(name);

        if (field == null)
            throw new IllegalArgumentException("Could not find model for field named \"" + name + "\"");

        Object value = get(name);

        if (value == null) {
            return null;
        }

        String fieldType = field.type;
        String str = null;

        // first the custom types that need to be parsed
        if (fieldType.equals("CustomTimestamp")) {
            // a common timestamp format for flat files is with no separators: yyyyMMddHHmmss
            SimpleDateFormat sdf = new SimpleDateFormat(field.format);
            java.sql.Timestamp timestamp = (java.sql.Timestamp) value;

            str = sdf.format(new Date(timestamp.getTime()));
        } else if (fieldType.equals("CustomDate")) {
            // a common date only format for flat files is with no separators: yyyyMMdd or MMddyyyy
            SimpleDateFormat sdf = new SimpleDateFormat(field.format);
            java.sql.Date date = (java.sql.Date) value;

            str = sdf.format(new Date(date.getTime()));
        } else if (fieldType.equals("CustomTime")) {
            // a common time only format for flat files is with no separators: HHmmss
            SimpleDateFormat sdf = new SimpleDateFormat(field.format);
            java.sql.Time time = (java.sql.Time) value;

            str = sdf.format(new Date(time.getTime()));
        } else if (fieldType.equals("FixedPointDouble")) {
            // this custom type will parse a fixed point number according to the number
            // of decimal places in the formatting string then place it in a Double
            NumberFormat nf = NumberFormat.getNumberInstance();
            double decimalPlaces = Double.parseDouble(field.format);
            double multiplier = Math.pow(10.0, decimalPlaces);
            double dnum = multiplier * ((Double) value).doubleValue();
            long number = Math.round(dnum);

            str = padFrontZeros(Long.toString(number), field.length);
            // if (Debug.infoOn()) Debug.logInfo("[Record.getFixedString] FixedPointDouble: multiplier=" + multiplier + ", value=" + value + ", dnum=" + dnum + ", number=" + number + ", str=" + str);
        } // standard types
        else if (fieldType.equals("java.lang.String") || fieldType.equals("String"))
            str = value.toString();
        else if (fieldType.equals("java.sql.Timestamp") || fieldType.equals("Timestamp"))
            str = value.toString();
        else if (fieldType.equals("java.sql.Time") || fieldType.equals("Time"))
            str = value.toString();
        else if (fieldType.equals("java.sql.Date") || fieldType.equals("Date"))
            str = value.toString();
        // for all numbers, pad front with zeros if field length is specified
        else if (fieldType.equals("java.lang.Integer") || fieldType.equals("Integer"))
            str = padFrontZeros(value.toString(), field.length);
        else if (fieldType.equals("java.lang.Long") || fieldType.equals("Long"))
            str = padFrontZeros(value.toString(), field.length);
        else if (fieldType.equals("java.lang.Float") || fieldType.equals("Float"))
            str = padFrontZeros(value.toString(), field.length);
        else if (fieldType.equals("java.lang.Double") || fieldType.equals("Double"))
            str = padFrontZeros(value.toString(), field.length);
        else {
            throw new IllegalArgumentException("Field type " + fieldType + " not currently supported. Sorry.");
        }

        if (str != null && field.length > 0 && str.length() < field.length) {
            // pad the end with spaces
            StringBuffer strBuf = new StringBuffer(str);

            while (strBuf.length() < field.length)
                strBuf.append(' ');
            str = strBuf.toString();
        }
        return str;
    }

    public String writeLineString(ModelDataFile modelDataFile) throws DataFileException {
        ModelRecord modelRecord = getModelRecord();
        boolean isFixedRecord = ModelDataFile.SEP_FIXED_RECORD.equals(modelDataFile.separatorStyle);
        boolean isFixedLength = ModelDataFile.SEP_FIXED_LENGTH.equals(modelDataFile.separatorStyle);
        boolean isDelimited = ModelDataFile.SEP_DELIMITED.equals(modelDataFile.separatorStyle);

        StringBuffer lineBuf = new StringBuffer();

        for (int f = 0; f < modelRecord.fields.size(); f++) {
            ModelField modelField = (ModelField) modelRecord.fields.get(f);
            String data = this.getFixedString(modelField.name);

            // if field is null (not set) then assume we want to pad the field
            char PAD_CHAR = ' ';

            if (data == null) {
                StringBuffer sb = new StringBuffer("");

                for (int i = 0; i < modelField.length; i++)
                    sb.append(PAD_CHAR);
                data = new String(sb);
            }

            // Pad the record
            if (isFixedRecord) {
                while (modelField.position > lineBuf.length())
                    lineBuf.append(" ");
            }
            // if (Debug.infoOn()) Debug.logInfo("Field: " + modelField.name + " Position: " + modelField.position + " BufLen: " + lineBuf.length());

            // if (Debug.infoOn()) Debug.logInfo("Got data \"" + data + "\" for field " + modelField.name + " in record " + modelRecord.name);
            if (modelField.length > 0 && data.length() != modelField.length)
                throw new DataFileException("Got field length " + data.length() + " but expected field length is " + modelField.length + " for field \"" +
                        modelField.name + "\" of record \"" + modelRecord.name + "\" data is: \"" + data + "\"");

            lineBuf.append(data);
            if (isDelimited)
                lineBuf.append(modelDataFile.delimiter);
        }
        if ((isFixedRecord || isFixedLength) && modelDataFile.recordLength > 0 && lineBuf.length() != modelDataFile.recordLength)
            throw new DataFileException("Got record length " + lineBuf.length() + " but expected record length is " + modelDataFile.recordLength +
                    " for record \"" + modelRecord.name + "\" data line is: \"" + lineBuf + "\"");

        // for convenience, insert the type-code in where it is looked for, if exists
        if (modelRecord.tcPosition > 0 && modelRecord.typeCode.length() > 0) {
            lineBuf.replace(modelRecord.tcPosition, modelRecord.tcPosition + modelRecord.tcLength, modelRecord.typeCode);
        }

        if (isFixedLength || isDelimited)
            lineBuf.append('\n');

        return lineBuf.toString();
    }

    String padFrontZeros(String str, int totalLength) {
        if (totalLength > 0 && str.length() < totalLength) {
            // pad the front with zeros
            StringBuffer zeros = new StringBuffer();
            int numZeros = totalLength - str.length();

            for (int i = 0; i < numZeros; i++)
                zeros.append('0');
            zeros.append(str);
            return zeros.toString();
        } else
            return str;
    }

    public Record getParentRecord() {
        return parentRecord;
    }

    public List getChildRecords() {
        return childRecords;
    }

    public void addChildRecord(Record record) {
        childRecords.add(record);
    }

    /** Creates new Record
     * @param modelRecord
     * @throws DataFileException Exception thown for various errors, generally has a nested exception
     * @return
     */
    public static Record createRecord(ModelRecord modelRecord) throws DataFileException {
        Record record = new Record(modelRecord);

        return record;
    }

    /** Creates new Record from existing fields Map
     * @param modelRecord
     * @param fields
     * @throws DataFileException Exception thown for various errors, generally has a nested exception
     * @return
     */
    public static Record createRecord(ModelRecord modelRecord, Map fields) throws DataFileException {
        Record record = new Record(modelRecord, fields);

        return record;
    }

    /**
     * @param line
     * @param lineNum
     * @param modelRecord
     * @throws DataFileException Exception thown for various errors, generally has a nested exception
     * @return
     */
    public static Record createRecord(String line, int lineNum, ModelRecord modelRecord) throws DataFileException {
        Record record = new Record(modelRecord);

        for (int i = 0; i < modelRecord.fields.size(); i++) {
            ModelField modelField = (ModelField) modelRecord.fields.get(i);
            String strVal = null;

            try {
                strVal = line.substring(modelField.position, modelField.position + modelField.length);
            } catch (IndexOutOfBoundsException ioobe) {
                throw new DataFileException("Field " + modelField.name + " from " + modelField.position +
                        " for " + modelField.length + " chars could not be read from a line (" + lineNum + ") with only " +
                        line.length() + " chars.", ioobe);
            }
            try {
                record.setString(modelField.name, strVal);
            } catch (java.text.ParseException e) {
                throw new DataFileException("Could not parse field " + modelField.name + ", format string \"" + modelField.format + "\" with value " + strVal +
                        " on line " + lineNum, e);
            } catch (java.lang.NumberFormatException e) {
                throw new DataFileException("Number not valid for field " + modelField.name + ", format string \"" + modelField.format + "\" with value " +
                        strVal + " on line " + lineNum, e);
            }
        }
        return record;
    }
}

