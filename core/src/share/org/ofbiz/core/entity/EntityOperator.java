package org.ofbiz.core.entity;

/**
 * <p><b>Title:</b> EntityOperator
 * <p><b>Description:</b> Encapsulates operations between entities and entity fields. This is a immutable class.
 * <p>Copyright (c) 2001 The Open For Business Project - www.ofbiz.org
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
 *
 *@author     <a href='mailto:chris_maurer@altavista.com'>Chris Maurer</a>
 *@author     <a href="mailto:jonesde@ofbiz.org">David E. Jones</a>
 *@created    Mon Nov 5, 2001
 *@version    1.0
 */
public class EntityOperator {
  public static final EntityOperator EQUALS = new EntityOperator(1);
  public static final EntityOperator NOT_EQUAL = new EntityOperator(2);
  public static final EntityOperator LESS_THAN = new EntityOperator(3);
  public static final EntityOperator GREATER_THAN = new EntityOperator(4);
  public static final EntityOperator LESS_THAN_EQUAL_TO = new EntityOperator(5);
  public static final EntityOperator GREATER_THAN_EQUAL_TO = new EntityOperator(6);
  public static final EntityOperator IN = new EntityOperator(7);
  public static final EntityOperator BETWEEN = new EntityOperator(8);
  public static final EntityOperator NOT = new EntityOperator(9);
  public static final EntityOperator AND = new EntityOperator(10);
  public static final EntityOperator OR = new EntityOperator(11);
  
  public static final String[] operatorStrings = { "", " = ", " <> ", " < ", " > ", " <= ", " >= ", " IN ", " BETWEEN ", " NOT ", " AND ", " OR " };
  
  private String codeString = "";
  
  public EntityOperator(int code) { putCode(code); }
  
  public String getCode() { return codeString; }

  private void putCode(int code) {
    if(code <= 11 && code >= 1) { this.codeString = operatorStrings[code]; }
    throw new IllegalArgumentException("Code " + code + " is not a valid Entity Operator code.");
  }
}
