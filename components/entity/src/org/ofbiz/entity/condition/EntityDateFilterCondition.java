/*
 * $Id: EntityDateFilterCondition.java,v 1.1 2003/11/05 12:08:00 jonesde Exp $
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
package org.ofbiz.entity.condition;

import java.util.List;
import java.sql.Timestamp;

import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.entity.GenericEntity;
import org.ofbiz.entity.GenericModelException;
import org.ofbiz.entity.model.ModelEntity;

public class EntityDateFilterCondition extends EntityCondition {

    protected String fromDateName;
    protected String thruDateName;

    public EntityDateFilterCondition(String fromDateName, String thruDateName) {
        this.fromDateName = fromDateName;
        this.thruDateName = thruDateName;
    }

    public String makeWhereString(ModelEntity modelEntity, List entityConditionParams) {
        EntityCondition condition = makeCondition();
        return condition.makeWhereString(modelEntity, entityConditionParams);
    }

    public void checkCondition(ModelEntity modelEntity) throws GenericModelException {
        EntityCondition condition = makeCondition();
        condition.checkCondition(modelEntity);
    }

    public boolean entityMatches(GenericEntity entity) {    
        EntityCondition condition = makeCondition();
        return condition.entityMatches(entity);
    }

    protected EntityCondition makeCondition() {
        return makeCondition(UtilDateTime.nowTimestamp(), fromDateName, thruDateName);
    }

    public static EntityExpr makeCondition(Timestamp moment, String fromDateName, String thruDateName) {
        return new EntityExpr(
            new EntityExpr(
                new EntityExpr( thruDateName, EntityOperator.EQUALS, null ),
                EntityOperator.OR,
                new EntityExpr( thruDateName, EntityOperator.GREATER_THAN, moment )
            ),
            EntityOperator.AND,
            new EntityExpr(
                new EntityExpr( fromDateName, EntityOperator.EQUALS, null ),
                EntityOperator.OR,
                new EntityExpr( fromDateName, EntityOperator.LESS_THAN_EQUAL_TO, moment )
            )
       );
    }
}
