/*
 * $Id: EntityJoinOperator.java,v 1.3 2004/07/07 00:15:24 doogie Exp $
 *
 *  Copyright (c) 2002 The Open For Business Project - www.ofbiz.org
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

package org.ofbiz.entity.condition;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ofbiz.entity.GenericDelegator;
import org.ofbiz.entity.GenericEntity;
import org.ofbiz.entity.GenericModelException;
import org.ofbiz.entity.config.EntityConfigUtil;
import org.ofbiz.entity.model.ModelEntity;

/**
 * Encapsulates operations between entities and entity fields. This is a immutable class.
 *
 *@author     <a href='mailto:chris_maurer@altavista.com'>Chris Maurer</a>
 *@author     <a href="mailto:jonesde@ofbiz.org">David E. Jones</a>
 *@author     <a href="mailto:jaz@jflow.net">Andy Zeneski</a>
 *@created    Nov 5, 2001
 *@version    1.0
 */
public class EntityJoinOperator extends EntityOperator {

    protected boolean shortCircuitValue;

    protected EntityJoinOperator(int id, String code, boolean shortCircuitValue) {
        super(id, code);
        this.shortCircuitValue = shortCircuitValue;
    }

    public void addSqlValue(StringBuffer sql, ModelEntity modelEntity, List entityConditionParams, Object lhs, Object rhs) {
        sql.append('(');
        sql.append(((EntityCondition) lhs).makeWhereString(modelEntity, entityConditionParams));
        sql.append(") ");
        sql.append(getCode());
        sql.append(" (");
        if (rhs instanceof EntityCondition) {
            sql.append(((EntityCondition) rhs).makeWhereString(modelEntity, entityConditionParams));
        } else {
            addValue(sql, null, rhs, entityConditionParams);
        }
        sql.append(')');
    }

    public void addSqlValue(StringBuffer sql, ModelEntity modelEntity, List entityConditionParams, List conditionList) {
        if (conditionList != null && conditionList.size() > 0) {
            sql.append('(');
            for (int i = 0; i < conditionList.size(); i++) {
                if (i != 0) sql.append(' ').append(getCode()).append(' ');
                EntityCondition condition = (EntityCondition) conditionList.get(i);
                sql.append(condition.makeWhereString(modelEntity, entityConditionParams));
            }
            sql.append(')');
        }
    }

    public boolean entityMatches(GenericEntity entity, Object lhs, Object rhs) {
        return entityMatches(entity, (EntityCondition) lhs, (EntityCondition) rhs);
    }

    public boolean entityMatches(GenericEntity entity, EntityCondition lhs, EntityCondition rhs) {
        if (lhs.entityMatches(entity)) return shortCircuitValue;
        if (rhs.entityMatches(entity)) return shortCircuitValue;
        return !shortCircuitValue;
    }

    public boolean entityMatches(GenericEntity entity, List conditionList) {
        return mapMatches(entity.getDelegator(), entity, conditionList);
    }

    public boolean mapMatches(GenericDelegator delegator, Map map, Object lhs, Object rhs) {
        if (((EntityCondition) lhs).mapMatches(delegator, map)) return shortCircuitValue;
        if (((EntityCondition) rhs).mapMatches(delegator, map)) return shortCircuitValue;
        return !shortCircuitValue;
    }

    public boolean mapMatches(GenericDelegator delegator, Map map, List conditionList) {
        if (conditionList != null && conditionList.size() > 0) {
            for (int i = 0; i < conditionList.size(); i++) {
                EntityCondition condition = (EntityCondition) conditionList.get(i);
                if (condition.mapMatches(delegator, map) == shortCircuitValue) return shortCircuitValue;
            }
        }
        return !shortCircuitValue;
    }

    public void validateSql(ModelEntity modelEntity, Object lhs, Object rhs) throws GenericModelException {
        validateSql(modelEntity, (EntityCondition) lhs, (EntityCondition) rhs);
    }

    public void validateSql(ModelEntity modelEntity, EntityCondition lhs, EntityCondition rhs) throws GenericModelException {
        lhs.checkCondition(modelEntity);
        rhs.checkCondition(modelEntity);
    }

    public void validateSql(ModelEntity modelEntity, List conditionList) throws GenericModelException {
        if (conditionList == null && conditionList.size() == 0)
            throw new GenericModelException("Empty list for joining");
        for (int i = 0; i < conditionList.size(); i++) {
            EntityCondition condition = (EntityCondition) conditionList.get(i);
            condition.checkCondition(modelEntity);
        }
    }
}
