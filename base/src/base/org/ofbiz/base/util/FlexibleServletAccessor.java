/*
 * $Id: FlexibleServletAccessor.java,v 1.1 2003/08/17 06:06:13 ajzeneski Exp $
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

import java.util.List;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Used to flexibly access Map values, supporting the "." (dot) syntax for
 * accessing sub-map values and the "[]" (square bracket) syntax for accessing
 * list elements. See individual Map operations for more information.
 *
 * @author     <a href="mailto:jonesde@ofbiz.org">David E. Jones</a>
 * @version    $Revision: 1.1 $
 * @since      2.1
 */
public class FlexibleServletAccessor {

    protected String name;
    protected String attributeName;
    protected FlexibleMapAccessor fma;
    protected boolean needsExpand;
    protected boolean empty;

    public FlexibleServletAccessor(String name) {
        init(name);
    }
    
    public FlexibleServletAccessor(String name, String defaultName) {
        if (name == null || name.length() == 0) {
            init(defaultName);
        } else {
            init(name);
        }
    }
    
    protected void init(String name) {
        this.name = name;
        if (name == null || name.length() == 0) {
            empty = true;
            needsExpand = false;
            fma = new FlexibleMapAccessor(name);
            attributeName = name;
        } else {
            empty = false;
            int openPos = name.indexOf("${");
            if (openPos != -1 && name.indexOf("}", openPos) != -1) {
                fma = null;
                attributeName = null;
                needsExpand = true;
            } else {
                int dotIndex = name.indexOf('.');
                if (dotIndex != -1) {
                    attributeName = name.substring(0, dotIndex);
                    fma = new FlexibleMapAccessor(name.substring(dotIndex+1));
                } else {
                    attributeName = name;
                    fma = null;
                }
                
                needsExpand = false;
            }
        }
    }
    
    public boolean isEmpty() {
        return this.empty;
    }

    /** Based on name get from ServletRequest or from List in ServletRequest */
    public Object get(ServletRequest request, Map expandContext) {
        AttributeAccessor aa = new AttributeAccessor(name, expandContext, this.attributeName, this.fma, this.needsExpand);
        return aa.get(request);
    }

    /** Based on name get from HttpSession or from List in HttpSession */
    public Object get(HttpSession session, Map expandContext) {
        AttributeAccessor aa = new AttributeAccessor(name, expandContext, this.attributeName, this.fma, this.needsExpand);
        return aa.get(session);
    }

    /** Based on name put in ServletRequest or from List in ServletRequest;
     * If the brackets for a list are empty the value will be appended to the list,
     * otherwise the value will be set in the position of the number in the brackets.
     * If a "+" (plus sign) is included inside the square brackets before the index 
     * number the value will inserted/added at that point instead of set at the point.
     */
    public void put(ServletRequest request, Object value, Map expandContext) {
        AttributeAccessor aa = new AttributeAccessor(name, expandContext, this.attributeName, this.fma, this.needsExpand);
        aa.put(request, value);
    }
    
    /** Based on name put in HttpSession or from List in HttpSession;
     * If the brackets for a list are empty the value will be appended to the list,
     * otherwise the value will be set in the position of the number in the brackets.
     * If a "+" (plus sign) is included inside the square brackets before the index 
     * number the value will inserted/added at that point instead of set at the point.
     */
    public void put(HttpSession session, Object value, Map expandContext) {
        AttributeAccessor aa = new AttributeAccessor(name, expandContext, this.attributeName, this.fma, this.needsExpand);
        aa.put(session, value);
    }
    
    /** Based on name remove from ServletRequest or from List in ServletRequest */
    public Object remove(ServletRequest request, Map expandContext) {
        AttributeAccessor aa = new AttributeAccessor(name, expandContext, this.attributeName, this.fma, this.needsExpand);
        return aa.remove(request);
    }
    
    /** Based on name remove from HttpSession or from List in HttpSession */
    public Object remove(HttpSession session, Map expandContext) {
        AttributeAccessor aa = new AttributeAccessor(name, expandContext, this.attributeName, this.fma, this.needsExpand);
        return aa.remove(session);
    }
    
    /** The equals and hashCode methods are imnplemented just case this object is ever accidently used as a Map key */    
    public int hashCode() {
        return this.name.hashCode();
    }

    /** The equals and hashCode methods are imnplemented just case this object is ever accidently used as a Map key */    
    public boolean equals(Object obj) {
        if (obj instanceof FlexibleServletAccessor) {
            FlexibleServletAccessor flexibleServletAccessor = (FlexibleServletAccessor) obj;
            if (this.name == null) {
                return flexibleServletAccessor.name == null;
            }
            return this.name.equals(flexibleServletAccessor.name);
        } else {
            String str = (String) obj;
            if (this.name == null) {
                return str == null;
            }
            return this.name.equals(str);
        }
    }

    /** To be used for a string representation of the accessor, returns the original name. */    
    public String toString() {
        return this.name;
    }
    
    protected static class AttributeAccessor {
        protected Map expandContext;
        protected String attributeName;
        protected FlexibleMapAccessor fma;
        protected boolean isListReference;
        protected boolean isAddAtIndex;
        protected boolean isAddAtEnd;
        protected int listIndex;
        protected int openBrace;
        protected int closeBrace;
        
        public AttributeAccessor(String origName, Map expandContext, String defAttributeName, FlexibleMapAccessor defFma, boolean needsExpand) {
            attributeName = defAttributeName;
            fma = defFma;
            
            if (needsExpand) {
                String name = FlexibleStringExpander.expandString(origName, expandContext);
                int dotIndex = name.indexOf('.');
                if (dotIndex != -1) {
                    attributeName = name.substring(0, dotIndex);
                    fma = new FlexibleMapAccessor(name.substring(dotIndex+1));
                } else {
                    attributeName = name;
                    fma = null;
                }
            }

            isListReference = false;
            isAddAtIndex = false;
            isAddAtEnd = false;
            listIndex = -1;
            openBrace = attributeName.indexOf('[');
            closeBrace = (openBrace == -1 ? -1 : attributeName.indexOf(']', openBrace));
            if (openBrace != -1 && closeBrace != -1) {
                String liStr = attributeName.substring(openBrace+1, closeBrace);
                //if brackets are empty, append to list
                if (liStr.length() == 0) {
                    isAddAtEnd = true;
                } else {
                    if (liStr.charAt(0) == '+') {
                        liStr = liStr.substring(1);
                        listIndex = Integer.parseInt(liStr);
                        isAddAtIndex = true;
                    } else {
                        listIndex = Integer.parseInt(liStr);
                    }
                }
                attributeName = attributeName.substring(0, openBrace);
                isListReference = true;
            }
        
        }

        public Object get(ServletRequest request) {
            Object theValue = null;
            if (isListReference) {
                List lst = (List) request.getAttribute(attributeName);
                theValue = lst.get(listIndex);
            } else {
                theValue = request.getAttribute(attributeName);
            }

            if (fma != null) {
                return fma.get((Map) theValue);
            } else {
                return theValue;
            }
        }

        public Object get(HttpSession session) {
            Object theValue = null;
            if (isListReference) {
                List lst = (List) session.getAttribute(attributeName);
                theValue = lst.get(listIndex);
            } else {
                theValue = session.getAttribute(attributeName);
            }

            if (fma != null) {
                return fma.get((Map) theValue);
            } else {
                return theValue;
            }
        }

        protected void putInList(List lst, Object value) {
            //if brackets are empty, append to list
            if (isAddAtEnd) {
                lst.add(value);
            } else {
                if (isAddAtIndex) {
                    lst.add(listIndex, value);
                } else {
                    lst.set(listIndex, value);
                }
            }
        }
        
        public void put(ServletRequest request, Object value) {
            if (fma == null) {
                if (isListReference) {
                    List lst = (List) request.getAttribute(attributeName);
                    putInList(lst, value);
                } else {
                    request.setAttribute(attributeName, value);
                }
            } else {
                Object theObj = request.getAttribute(attributeName);
                if (isListReference) {
                    List lst = (List) theObj;
                    fma.put((Map) lst.get(listIndex), value);
                } else {
                    fma.put((Map) theObj, value);
                }
            }
        }
        
        public void put(HttpSession session, Object value) {
            if (fma == null) {
                if (isListReference) {
                    List lst = (List) session.getAttribute(attributeName);
                    putInList(lst, value);
                } else {
                    session.setAttribute(attributeName, value);
                }
            } else {
                Object theObj = session.getAttribute(attributeName);
                if (isListReference) {
                    List lst = (List) theObj;
                    fma.put((Map) lst.get(listIndex), value);
                } else {
                    fma.put((Map) theObj, value);
                }
            }
        }

        public Object remove(ServletRequest request) {
            if (fma != null) {
                Object theObj = request.getAttribute(attributeName);
                if (isListReference) {
                    List lst = (List) theObj;
                    return fma.remove((Map) lst.get(listIndex));
                } else {
                    return fma.remove((Map) theObj);
                }
            } else {
                if (isListReference) {
                    List lst = (List) request.getAttribute(attributeName);
                    return lst.remove(listIndex);
                } else {
                    Object theValue = request.getAttribute(attributeName);
                    request.removeAttribute(attributeName);
                    return theValue;
                }
            }
        }

        public Object remove(HttpSession session) {
            if (fma != null) {
                Object theObj = session.getAttribute(attributeName);
                if (isListReference) {
                    List lst = (List) theObj;
                    return fma.remove((Map) lst.get(listIndex));
                } else {
                    return fma.remove((Map) theObj);
                }
            } else {
                if (isListReference) {
                    List lst = (List) session.getAttribute(attributeName);
                    return lst.remove(listIndex);
                } else {
                    Object theValue = session.getAttribute(attributeName);
                    session.removeAttribute(attributeName);
                    return theValue;
                }
            }
        }
    }
}
