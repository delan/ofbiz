/*
 * $Id: DatasourceInfo.java,v 1.2 2004/08/09 23:52:23 jonesde Exp $
 *
 * Copyright (c) 2001-2004 The Open For Business Project - www.ofbiz.org
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
package org.ofbiz.entity.config;

import java.util.LinkedList;
import java.util.List;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilXml;
import org.w3c.dom.Element;

/**
 * Misc. utility method for dealing with the entityengine.xml file
 *
 * @author     <a href="mailto:jonesde@ofbiz.org">David E. Jones</a> 
 * @author     <a href="mailto:jaz@ofbiz.org">Andy Zeneski</a> 
 * @version    $Revision: 1.2 $
 * @since      2.0
 */
public class DatasourceInfo {
    public static final String module = DatasourceInfo.class.getName();

    public String name;
    public String helperClass;
    public String fieldTypeName;
    public List sqlLoadPaths = new LinkedList();
    public List readDatas = new LinkedList();
    public Element datasourceElement;
    
    public static final int TYPE_JNDI_JDBC = 1;        
    public static final int TYPE_INLINE_JDBC = 2;
    public static final int TYPE_TYREX_DATA_SOURCE = 3;
    public static final int TYPE_OTHER = 4;
            
    public Element jndiJdbcElement;
    public Element tyrexDataSourceElement;
    public Element inlineJdbcElement;

    public String schemaName = null;
    public boolean useSchemas = true;
    public boolean checkOnStart = true;
    public boolean addMissingOnStart = false;
    public boolean useFks = true;
    public boolean useFkIndices = true;
    public boolean checkForeignKeysOnStart = false;
    public boolean checkFkIndicesOnStart = false;
    public boolean usePkConstraintNames = true;
    public int constraintNameClipLength = 30;
    public boolean useProxyCursor = false;
    public String cursorName = "p_cursor";
    public int resultFetchSize = -1;
    public String fkStyle = null;
    public boolean useFkInitiallyDeferred = true;
    public boolean useIndices = true;
    public boolean checkIndicesOnStart = false;
    public String joinStyle = null;
    public boolean aliasViews = true;
    public boolean alwaysUseConstraintKeyword = false;

    public DatasourceInfo(Element element) {
        this.name = element.getAttribute("name");
        this.helperClass = element.getAttribute("helper-class");
        this.fieldTypeName = element.getAttribute("field-type-name");

        sqlLoadPaths = UtilXml.childElementList(element, "sql-load-path");
        readDatas = UtilXml.childElementList(element, "read-data");
        datasourceElement = element;

        if (datasourceElement == null) {
            Debug.logWarning("datasource def not found with name " + this.name + ", using default for schema-name (none)", module);
            Debug.logWarning("datasource def not found with name " + this.name + ", using default for use-schemas (true)", module);
            Debug.logWarning("datasource def not found with name " + this.name + ", using default for check-on-start (true)", module);
            Debug.logWarning("datasource def not found with name " + this.name + ", using default for add-missing-on-start (false)", module);
            Debug.logWarning("datasource def not found with name " + this.name + ", using default for use-foreign-keys (true)", module);
            Debug.logWarning("datasource def not found with name " + this.name + ", using default use-foreign-key-indices (true)", module);
            Debug.logWarning("datasource def not found with name " + this.name + ", using default for check-fks-on-start (false)", module);
            Debug.logWarning("datasource def not found with name " + this.name + ", using default for check-fk-indices-on-start (false)", module);
            Debug.logWarning("datasource def not found with name " + this.name + ", using default for use-pk-constraint-names (true)", module);
            Debug.logWarning("datasource def not found with name " + this.name + ", using default for constraint-name-clip-length (30)", module);
            Debug.logWarning("datasource def not found with name " + this.name + ", using default for fk-style (name_constraint)", module);
            Debug.logWarning("datasource def not found with name " + this.name + ", using default for use-fk-initially-deferred (true)", module);
            Debug.logWarning("datasource def not found with name " + this.name + ", using default for use-indices (true)", module);
            Debug.logWarning("datasource def not found with name " + this.name + ", using default for check-indices-on-start (false)", module);
            Debug.logWarning("datasource def not found with name " + this.name + ", using default for join-style (ansi)", module);
            Debug.logWarning("datasource def not found with name " + this.name + ", using default for always-use-constraint-keyword (false)", module);
        } else {
            schemaName = datasourceElement.getAttribute("schema-name");
            // anything but false is true
            useSchemas = !"false".equals(datasourceElement.getAttribute("use-schemas"));
            // anything but false is true
            checkOnStart = !"false".equals(datasourceElement.getAttribute("check-on-start"));
            // anything but true is false
            addMissingOnStart = "true".equals(datasourceElement.getAttribute("add-missing-on-start"));
            // anything but false is true
            useFks = !"false".equals(datasourceElement.getAttribute("use-foreign-keys"));
            // anything but false is true
            useFkIndices = !"false".equals(datasourceElement.getAttribute("use-foreign-key-indices"));
            // anything but true is false
            checkForeignKeysOnStart = "true".equals(datasourceElement.getAttribute("check-fks-on-start"));
            // anything but true is false
            checkFkIndicesOnStart = "true".equals(datasourceElement.getAttribute("check-fk-indices-on-start"));
            // anything but false is true
            usePkConstraintNames = !"false".equals(datasourceElement.getAttribute("use-pk-constraint-names"));
            try {
                constraintNameClipLength = Integer.parseInt(datasourceElement.getAttribute("constraint-name-clip-length"));
            } catch (Exception e) {
                Debug.logError("Could not parse constraint-name-clip-length value for datasource with name " + this.name + ", using default value of 30", module);
            }
            useProxyCursor = "true".equalsIgnoreCase(datasourceElement.getAttribute("use-proxy-cursor"));
            cursorName = datasourceElement.getAttribute("proxy-cursor-name");
            try {
                resultFetchSize = Integer.parseInt(datasourceElement.getAttribute("result-fetch-size"));
            } catch (Exception e) {
                Debug.logWarning("Could not parse result-fetch-size value for datasource with name " + this.name + ", using JDBC driver default value", module);
            }
            fkStyle = datasourceElement.getAttribute("fk-style");
            // anything but true is false
            useFkInitiallyDeferred = "true".equals(datasourceElement.getAttribute("use-fk-initially-deferred"));
            // anything but false is true
            useIndices = !"false".equals(datasourceElement.getAttribute("use-indices"));
            // anything but true is false
            checkIndicesOnStart = "true".equals(datasourceElement.getAttribute("check-indices-on-start"));
            joinStyle = datasourceElement.getAttribute("join-style");
            aliasViews = !"false".equals(datasourceElement.getAttribute("alias-view-columns"));
            // anything but true is false
            alwaysUseConstraintKeyword = "true".equals(datasourceElement.getAttribute("always-use-constraint-keyword"));
        }
        if (fkStyle == null || fkStyle.length() == 0) fkStyle = "name_constraint";
        if (joinStyle == null || joinStyle.length() == 0) joinStyle = "ansi";

        jndiJdbcElement = UtilXml.firstChildElement(datasourceElement, "jndi-jdbc");
        tyrexDataSourceElement = UtilXml.firstChildElement(datasourceElement, "tyrex-dataSource");
        inlineJdbcElement = UtilXml.firstChildElement(datasourceElement, "inline-jdbc");
    }
}
