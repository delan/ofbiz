/*
 * $Id: BOMServices.java,v 1.1 2003/11/26 10:16:39 jacopo Exp $
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
package org.ofbiz.manufacturing.bom;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Date;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.GenericDelegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.security.Security;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;

/**
 * ContentServices Class
 *
 * @author     <a href="mailto:byersa@automationgroups.com">Al Byers</a>
 * @version    $Revision: 1.1 $
 * @since      2.2
 *
 *
 */
public class BOMServices {

    public static final String module = BOMServices.class.getName();

    public static Map getMaxDepth(DispatchContext dctx, Map context) {

        Map result = new HashMap();
        Security security = dctx.getSecurity();
        GenericDelegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        String productId = (String) context.get("productId");
        String fromDateStr = (String) context.get("fromDate");
        String bomType = (String) context.get("bomType");
        
        Date fromDate = null;
        if (UtilValidate.isNotEmpty(fromDateStr)) {
            try {
                fromDate = Timestamp.valueOf(fromDateStr);
            } catch (Exception e) {
            }
        }
        if (fromDate == null) {
            fromDate = new Date();
        }
        
        Integer depth = null;
        try {
            depth = new Integer(BOMHelper.getMaxDepth(productId, bomType, fromDate, delegator));
        } catch(GenericEntityException gee) {
            return ServiceUtil.returnError("Error running max depth algorithm: " + gee.getMessage());
        }

        result.put("depth", depth);

        return result;
    }

    public static Map searchDuplicatedAncestor(DispatchContext dctx, Map context) {

        Map result = new HashMap();
        Security security = dctx.getSecurity();
        GenericDelegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        String productId = (String) context.get("productId");
        String productIdKey = (String) context.get("productIdKey");
        String fromDateStr = (String) context.get("fromDate");
        String bomType = (String) context.get("bomType");
        
        Date fromDate = null;
        if (UtilValidate.isNotEmpty(fromDateStr)) {
            try {
                fromDate = Timestamp.valueOf(fromDateStr);
            } catch (Exception e) {
            }
        }
        if (fromDate == null) {
            fromDate = new Date();
        }
        
        GenericValue duplicatedProductAssoc = null;
        try {
            duplicatedProductAssoc = BOMHelper.searchDuplicatedAncestor(productId, productIdKey, bomType, fromDate, delegator);
        } catch(GenericEntityException gee) {
            return ServiceUtil.returnError("Error running duplicated ancestor search: " + gee.getMessage());
        }

        result.put("duplicatedProductAssoc", duplicatedProductAssoc);

        return result;
    }

    public static Map getItemConfigurationTree(DispatchContext dctx, Map context) {

        Map result = new HashMap();
        Security security = dctx.getSecurity();
        GenericDelegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        String productId = (String) context.get("productId");
        String fromDateStr = (String) context.get("fromDate");
        String bomType = (String) context.get("bomType");
        Boolean explosion = (Boolean) context.get("explosion");
        if (explosion == null) {
            explosion = new Boolean(true);
        }
        
        Date fromDate = null;
        if (UtilValidate.isNotEmpty(fromDateStr)) {
            try {
                fromDate = Timestamp.valueOf(fromDateStr);
            } catch (Exception e) {
            }
        }
        if (fromDate == null) {
            fromDate = new Date();
        }
        
        ItemConfigurationTree tree = null;
        try {
            tree = new ItemConfigurationTree(productId, bomType, fromDate, explosion.booleanValue(), delegator);
        } catch(GenericEntityException gee) {
            return ServiceUtil.returnError("Error creating bill of materials tree: " + gee.getMessage());
        }

        result.put("tree", tree);

        return result;
    }

}


