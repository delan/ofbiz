/*
 *  Copyright (c) 2001 The Open For Business Project - www.ofbiz.org
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

package org.ofbiz.core.entity;

import java.sql.*;
import java.util.*;
import org.ofbiz.core.entity.jdbc.*;
import org.ofbiz.core.entity.model.*;
import org.ofbiz.core.util.*;

/**
 * Generic Entity Cursor List Iterator for Handling Cursored DB Results
 *
 *@author     <a href="mailto:jonesde@ofbiz.org">David E. Jones</a>
 *@created    July 12, 2002
 *@version    1.0
 */
public class EntityListIterator implements ListIterator {

    protected SQLProcessor sqlp;
    protected ResultSet resultSet;
    protected ModelEntity modelEntity;
    protected List selectFields;
    protected ModelFieldTypeReader modelFieldTypeReader;
    protected boolean closed = false;
    protected boolean haveMadeValue = false;
    
    public EntityListIterator(SQLProcessor sqlp, ModelEntity modelEntity, List selectFields, ModelFieldTypeReader modelFieldTypeReader) {
        this.sqlp = sqlp;
        this.resultSet = sqlp.getResultSet();
        this.modelEntity = modelEntity;
        this.selectFields = selectFields;
        this.modelFieldTypeReader = modelFieldTypeReader;
    }

    /** Sets the cursor position to just after the last result so that previous() will return the last result */
    public void afterLast() throws GenericEntityException {
        try {
            resultSet.afterLast();
        } catch (SQLException e) {
            throw new GenericEntityException("Error setting the cursor to afterLast", e);
        }
    }
    
    /** Sets the cursor position to just before the first result so that next() will return the first result */
    public void beforeFirst() throws GenericEntityException {
        try {
            resultSet.beforeFirst();
        } catch (SQLException e) {
            throw new GenericEntityException("Error setting the cursor to beforeFirst", e);
        }
    }
    
    public void close() throws GenericEntityException {
        if (closed) throw new GenericResultSetClosedException("This EntityListIterator has been closed, this operation cannot be performed");
        
        sqlp.close();
        closed = true;
    }
    
    /** NOTE: Calling this method does return the current value, but so does calling next() or previous(), so calling one of those AND this method will cause the value to be created twice */
    public GenericValue currentGenericValue() throws GenericEntityException {
        if (closed) throw new GenericResultSetClosedException("This EntityListIterator has been closed, this operation cannot be performed");
        
        GenericValue value = new GenericValue(modelEntity);

        for (int j = 0; j < selectFields.size(); j++) {
            ModelField curField = (ModelField) selectFields.get(j);

            SqlJdbcUtil.getValue(resultSet, j + 1, curField, value, modelFieldTypeReader);
        }

        value.modified = false;
        this.haveMadeValue = true;
        return value;
    }
    
    public int currentIndex() throws GenericEntityException {
        if (closed) throw new GenericResultSetClosedException("This EntityListIterator has been closed, this operation cannot be performed");
        
        try {
            return resultSet.getRow();
        } catch (SQLException e) {
            throw new GenericEntityException("Error getting the current index", e);
        }
    }
    
    /** PLEASE NOTE: Because of the nature of the JDBC ResultSet interface this method can be very inefficient; it is much better to just use next() until it returns null */
    public boolean hasNext() {
        try {
            if (resultSet.isLast() || resultSet.isAfterLast()) {
                return false;
            } else {
                //do a quick game to see if the resultSet is empty:
                // if we are not in the first or beforeFirst positions and we haven't made any values yet, the result set is empty so return false
                if (!haveMadeValue && !resultSet.isBeforeFirst() && !resultSet.isFirst()) {
                    return false;
                } else {
                    return true;
                }
            }
        } catch (SQLException e) {
            throw new GeneralRuntimeException("Error while checking to see if this is the last result", e);
        }
    }
    
    /** PLEASE NOTE: Because of the nature of the JDBC ResultSet interface this method can be very inefficient; it is much better to just use previous() until it returns null */
    public boolean hasPrevious() {
        try {
            if (resultSet.isFirst() || resultSet.isBeforeFirst()) {
                return false;
            } else {
                //do a quick game to see if the resultSet is empty:
                // in this case it's easy, if we haven't made any values yet, just return false
                if (!haveMadeValue) {
                    return false;
                } else {
                    return true;
                }
            }
        } catch (SQLException e) {
            throw new GeneralRuntimeException("Error while checking to see if this is the first result", e);
        }
    }

    /** Moves the cursor to the next position and returns the GenericValue object for that position; if there is no next, returns null */
    public Object next() {
        try {
            if (resultSet.next()) {
                return currentGenericValue();
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new GeneralRuntimeException("Error getting the next result", e);
        } catch (GenericEntityException e) {
            throw new GeneralRuntimeException("Error creating GenericValue", e);
        }
    }
    
    /** Returns the index of the next result, but does not guarantee that there will be a next result */
    public int nextIndex() {
        try {
            return currentIndex() + 1;
        } catch (GenericEntityException e) {
            throw new GeneralRuntimeException(e.getNonNestedMessage(), e.getNested());
        }
    }
    
    /** Moves the cursor to the previous position and returns the GenericValue object for that position; if there is no previous, returns null */
    public Object previous() {
        try {
            if (resultSet.previous()) {
                return currentGenericValue();
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new GeneralRuntimeException("Error getting the previous result", e);
        } catch (GenericEntityException e) {
            throw new GeneralRuntimeException("Error creating GenericValue", e);
        }
    }
    
    /** Returns the index of the previous result, but does not guarantee that there will be a previous result */
    public int previousIndex() {
        try {
            return currentIndex() - 1;
        } catch (GenericEntityException e) {
            throw new GeneralRuntimeException("Error getting the current index", e);
        }
    }
    
    public void setFetchSize(int rows) throws GenericEntityException {
        try {
            resultSet.setFetchSize(rows);
        } catch (SQLException e) {
            throw new GenericEntityException("Error getting the next result", e);
        }
    }
    
    public Collection getCompleteCollection() throws GenericEntityException {
        try {
            //if the resultSet has been moved forward at all, move back to the beginning
            if (!resultSet.isBeforeFirst()) {
                resultSet.beforeFirst();
            }
            Collection collection = new LinkedList();
            Object nextValue = null;
            while ((nextValue = this.next()) != null) {
                collection.add(nextValue);
            }
            return collection;
        } catch (SQLException e) {
            throw new GeneralRuntimeException("Error getting results", e);
        } catch (GeneralRuntimeException e) {
            throw new GenericEntityException(e.getNonNestedMessage(), e.getNested());
        }
    }
    
    public void add(Object obj) {
        throw new GeneralRuntimeException("CursorListIterator currently only supports read-only access");
    }
    
    public void remove() {
        throw new GeneralRuntimeException("CursorListIterator currently only supports read-only access");
    }
    
    public void set(Object obj) {
        throw new GeneralRuntimeException("CursorListIterator currently only supports read-only access");
    }
}
