/*
 * $Id: ProductSearchEvents.java,v 1.3 2004/01/24 18:23:46 jonesde Exp $
 *
 *  Copyright (c) 2001, 2002 The Open For Business Project - www.ofbiz.org
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
package org.ofbiz.product.product;

import java.sql.Timestamp;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.content.stats.VisitHandler;
import org.ofbiz.entity.GenericDelegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.transaction.GenericTransactionException;
import org.ofbiz.entity.transaction.TransactionUtil;
import org.ofbiz.entity.util.EntityListIterator;
import org.ofbiz.product.product.ProductSearch.ProductSearchContext;
import org.ofbiz.product.product.ProductSearch.ResultSortOrder;

/**
 * Product Search Related Events
 *
 * @author     <a href="mailto:jonesde@ofbiz.org">David E. Jones</a>
 * @version    $Revision: 1.3 $
 * @since      3.0
 */
public class ProductSearchEvents {
    
    public static final String module = ProductSearchEvents.class.getName();

    /** Removes the results of a search from the specified category 
     *@param request The HTTPRequest object for the current request
     *@param response The HTTPResponse object for the current request
     *@return String specifying the exit status of this event
     */
    public static String searchRemoveFromCategory(HttpServletRequest request, HttpServletResponse response) {
        GenericDelegator delegator = (GenericDelegator) request.getAttribute("delegator");
        String productCategoryId = request.getParameter("SE_SEARCH_CATEGORY_ID");
       
        EntityListIterator eli = getProductSearchResults(request);
        if (eli == null) {
            request.setAttribute("_ERROR_MESSAGE_", "No results found, probably because there was an error or were no constraints.");
            return "error";
        }
        
        try {
            boolean beganTransaction = TransactionUtil.begin();
            try {
                int numRemoved = 0;
                GenericValue searchResultView = null;
                while ((searchResultView = (GenericValue) eli.next()) != null) {
                    String productId = searchResultView.getString("productId");
                    numRemoved += delegator.removeByAnd("ProductCategoryMember", UtilMisc.toMap("productCategoryId", productCategoryId, "productId", productId )) ;
                }
                eli.close();
                TransactionUtil.commit(beganTransaction);
                request.setAttribute("_EVENT_MESSAGE_", "removed " + numRemoved + " items.");
            } catch (GenericEntityException e) {
                String errMsg = "Error getting search results: " + e.toString();
                Debug.logError(e, errMsg, module);
                request.setAttribute("_ERROR_MESSAGE_", errMsg);
                TransactionUtil.rollback(beganTransaction);
                return "error";
            }
        } catch (GenericTransactionException e) {
            String errMsg = "Error getting search results: " + e.toString();
            Debug.logError(e, errMsg, module);
            request.setAttribute("_ERROR_MESSAGE_", errMsg);
            return "error";
        }
        return "success";
    }
    
   /** Sets the thru date of the results of a search to the specified date for the specified catogory
    *@param request The HTTPRequest object for the current request
    *@param response The HTTPResponse object for the current request
    *@return String specifying the exit status of this event
    */
   public static String searchExpireFromCategory(HttpServletRequest request, HttpServletResponse response) {
       GenericDelegator delegator = (GenericDelegator) request.getAttribute("delegator");
       String productCategoryId = request.getParameter("SE_SEARCH_CATEGORY_ID");
       String thruDateStr = request.getParameter("thruDate");
       
       Timestamp thruDate;
       try {
           thruDate = Timestamp.valueOf(thruDateStr); 
       } catch (RuntimeException e) {
           String errMsg = "The thruDate is not formatted properly: " + e.toString();
           Debug.logError(e, errMsg, module);
           request.setAttribute("_ERROR_MESSAGE_", errMsg);
           return "error";
       }
   
       EntityListIterator eli = getProductSearchResults(request);
       if (eli == null) {
           request.setAttribute("_ERROR_MESSAGE_", "No results found, probably because there was an error or were no constraints.");
           return "error";
       }
        
       try {
           boolean beganTransaction = TransactionUtil.begin();
           try {
                
               GenericValue searchResultView = null;
               int numExpired=0;
               while ((searchResultView = (GenericValue) eli.next()) != null) {
                   String productId = searchResultView.getString("productId");
                   //get all tuples that match product and category
                   List pcmList = delegator.findByAnd("ProductCategoryMember", UtilMisc.toMap("productCategoryId", productCategoryId, "productId", productId ));
                   
                   //set those thrudate to that specificed maybe remove then add new one
                   Iterator pcmListIter=pcmList.iterator();
                   while (pcmListIter.hasNext()) {
                       GenericValue pcm = (GenericValue) pcmListIter.next();
                       if (pcm.get("thruDate") == null) {
                           pcm.set("thruDate", thruDate);
                           pcm.store();
                           numExpired++;
                       }
                   }
               }
               request.setAttribute("_EVENT_MESSAGE_", "Expired " + numExpired + " items.");
               eli.close();
               TransactionUtil.commit(beganTransaction);
           } catch (GenericEntityException e) {
               String errMsg = "Error getting search results: " + e.toString();
               Debug.logError(e, errMsg, module);
               request.setAttribute("_ERROR_MESSAGE_", errMsg);
               TransactionUtil.rollback(beganTransaction);
               return "error";
           }
       } catch (GenericTransactionException e) {
           String errMsg = "Error getting search results: " + e.toString();
           Debug.logError(e, errMsg, module);
           request.setAttribute("_ERROR_MESSAGE_", errMsg);
           return "error";
       }

       return "success";
   }

   /**  Adds the results of a search to the specified catogory
    *@param request The HTTPRequest object for the current request
    *@param response The HTTPResponse object for the current request
    *@return String specifying the exit status of this event
    */
   public static String searchAddToCategory(HttpServletRequest request, HttpServletResponse response) {
       GenericDelegator delegator = (GenericDelegator) request.getAttribute("delegator");
       String productCategoryId = request.getParameter("SE_SEARCH_CATEGORY_ID");
       String fromDateStr = request.getParameter("fromDate");
       Timestamp fromDate = null;

       try {
           fromDate = Timestamp.valueOf(fromDateStr);
        } catch (RuntimeException e) {
            request.setAttribute("_ERROR_MESSAGE_", "The fromDate was not formatted properly: " + e.toString());
            return "error";
        }
       
       EntityListIterator eli = getProductSearchResults(request);
       if (eli == null) {
           request.setAttribute("_ERROR_MESSAGE_", "No results found, probably because there was an error or were no constraints.");
           return "error";
       }
        
       try {
           boolean beganTransaction = TransactionUtil.begin();
           try {
                
               GenericValue searchResultView = null;
               int numAdded=0;
               while ((searchResultView = (GenericValue) eli.next()) != null) {
                   String productId = searchResultView.getString("productId");
                   
                   GenericValue pcm=delegator.makeValue("ProductCategoryMember", null);
                   pcm.set("productCategoryId", productCategoryId);
                   pcm.set("productId", productId);
                   pcm.set("fromDate", fromDate);
                   pcm.create();
                   
                   numAdded++;
               }
              
               request.setAttribute("_EVENT_MESSAGE_", "added "+numAdded+" number of items.");
               eli.close();
               TransactionUtil.commit(beganTransaction);
           } catch (GenericEntityException e) {
               String errMsg = "Error getting search results: " + e.toString();
               Debug.logError(e, errMsg, module);
               request.setAttribute("_ERROR_MESSAGE_", errMsg);
               TransactionUtil.rollback(beganTransaction);
               return "error";
           }
       } catch (GenericTransactionException e) {
           String errMsg = "Error getting search results: " + e.toString();
           Debug.logError(e, errMsg, module);
           request.setAttribute("_ERROR_MESSAGE_", errMsg);
           return "error";
       }

       return "success";
   }
   
   /** Adds a feature to a seach results 
    *@param request The HTTPRequest object for the current request
    *@param response The HTTPResponse object for the current request
    *@return String specifying the exit status of this event
    */
   public static String searchAddFeature(HttpServletRequest request, HttpServletResponse response) {
       GenericDelegator delegator = (GenericDelegator) request.getAttribute("delegator");
       
       String productFeatureId = request.getParameter("productFeatureId");
       String fromDateStr = request.getParameter("fromDate");
       String thruDateStr = request.getParameter("thruDate");
       String amountStr = request.getParameter("amount");
       String sequenceNumStr = request.getParameter("sequenceNum");
       String productFeatureApplTypeId = request.getParameter("productFeatureApplTypeId");
       
       Timestamp thruDate=null;
       Timestamp fromDate=null;
       Double amount=null;
       Long sequenceNum=null;
       try {
           if (UtilValidate.isNotEmpty(fromDateStr)) {
               fromDate = Timestamp.valueOf(fromDateStr);
           }
           if (UtilValidate.isNotEmpty(thruDateStr)) {
               thruDate = Timestamp.valueOf(thruDateStr);
           } 
           if (UtilValidate.isNotEmpty(amountStr)) {
               amount = Double.valueOf(amountStr);
           }
           if (UtilValidate.isNotEmpty(sequenceNumStr)) {
               sequenceNum= Long.valueOf(sequenceNumStr);
           }
       } catch (RuntimeException e) {
           String errMsg = "Error casting data types: " + e.toString();
           Debug.logError(e, errMsg, module);
           request.setAttribute("_ERROR_MESSAGE_", errMsg);
           return "error";
       }
       
       EntityListIterator eli = getProductSearchResults(request);
       if (eli == null) {
           request.setAttribute("_ERROR_MESSAGE_", "No results found, probably because there was an error or were no constraints.");
           return "error";
       }
        
       try {
           boolean beganTransaction = TransactionUtil.begin();
           try {
                
               GenericValue searchResultView = null;
               int numAdded=0;
               while ((searchResultView = (GenericValue) eli.next()) != null) {
                   String productId = searchResultView.getString("productId");
                   GenericValue pfa=delegator.makeValue("ProductFeatureAppl", null);
                   pfa.set("productId", productId);
                   pfa.set("productFeatureId", productFeatureId);
                   pfa.set("fromDate", fromDate);
                   pfa.set("thruDate", thruDate);
                   pfa.set("productFeatureApplTypeId", productFeatureApplTypeId);
                   pfa.set("amount", amount);
                   pfa.set("sequenceNum", sequenceNum);
                   pfa.create();
                   numAdded++;
               }
               request.setAttribute("_EVENT_MESSAGE_", "Added " + numAdded + " features.");
               eli.close();
               TransactionUtil.commit(beganTransaction);
           } catch (GenericEntityException e) {
               String errMsg = "Error getting search results: " + e.toString();
               Debug.logError(e, errMsg, module);
               request.setAttribute("_ERROR_MESSAGE_", errMsg);
               TransactionUtil.rollback(beganTransaction);
               return "error";
           }
       } catch (GenericTransactionException e) {
           String errMsg = "Error getting search results: " + e.toString();
           Debug.logError(e, errMsg, module);
           request.setAttribute("_ERROR_MESSAGE_", errMsg);
           return "error";
       }

       return "success";
   }
    
    public static EntityListIterator getProductSearchResults(HttpServletRequest request) {
        HttpSession session = request.getSession();
        GenericDelegator delegator = (GenericDelegator) request.getAttribute("delegator");
        String visitId = VisitHandler.getVisitId(session);
        
        List productSearchConstraintList = (List) session.getAttribute(ProductSearchSession.PRODUCT_SEARCH_CONSTRAINT_LIST);
        // if no constraints, don't do a search...
        if (productSearchConstraintList != null && productSearchConstraintList.size() > 0) {
            ResultSortOrder resultSortOrder = ProductSearchSession.getResultSortOrder(session);
            ProductSearchContext productSearchContext = new ProductSearchContext(delegator, visitId);
            productSearchContext.addProductSearchConstraints(productSearchConstraintList);
            productSearchContext.setResultSortOrder(resultSortOrder);

            return productSearchContext.doQuery(delegator);
        } else {
            return null;
        }
    }
}
