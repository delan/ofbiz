/*
 * $Id$
 * $Log$
 *
 */

package org.ofbiz.commonapp.product.category;

import java.util.*;
import javax.servlet.jsp.PageContext;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletRequest;

import org.ofbiz.core.util.*;
import org.ofbiz.core.entity.*;

/**
 * <p><b>Title:</b> CategoryWorker.java
 * <p><b>Description:</b> Helper class to reduce code in JSPs.
 * <p>Copyright (c) 2001 The Open For Business Project and repected authors.
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
 * @author <a href="mailto:jaz@zsolv.com">Andy Zeneski</a>
 * @author <a href="mailto:jonesde@ofbiz.org">David E. Jones</a>
 * @version 1.0
 * Created on August 23, 2001, 7:58 PM
 */
public class CategoryWorker {
  
  public static void getRelatedProducts(PageContext pageContext, String attributeName) {
    getRelatedProducts(pageContext,attributeName,null);
  }
  
  /**
   * Puts the following into the pageContext attribute list with a prefix, if specified:
   *  productList, categoryId, viewIndex, viewSize, lowIndex, highIndex, listSize
   * Puts the following into the session attribute list:
   *  CACHE_SEARCH_RESULTS, CACHE_SEARCH_RESULTS_NAME
   *@param pageContext The pageContext of the calling JSP
   *@param attributePrefix A prefix to put on each attribute name in the pageContext
   *@param parentId The ID of the parent category
   */
  public static void getRelatedProducts(PageContext pageContext, String attributePrefix, String parentId) {
    GenericDelegator delegator = (GenericDelegator)pageContext.getServletContext().getAttribute("delegator");
    ServletRequest request = pageContext.getRequest();
    if(attributePrefix == null) attributePrefix = "";
    
    int viewIndex = 0;
    try { viewIndex = Integer.valueOf((String)pageContext.getRequest().getParameter("VIEW_INDEX")).intValue(); }
    catch (Exception e) { viewIndex = 0; }
    
    int viewSize = 10;
    try { viewSize = Integer.valueOf((String)pageContext.getRequest().getParameter("VIEW_SIZE")).intValue(); }
    catch (Exception e) { viewSize = 10; }
    
    if(parentId == null)
      parentId = UtilFormatOut.checkNull(request.getParameter("catalog_id"), request.getParameter("CATALOG_ID"),
              request.getParameter("category_id"), request.getParameter("CATEGORY_ID"));
    if(parentId == null || parentId.length() <= 0) return;
    
    String curFindString = "ProductCategoryByParentId:" + parentId;
    
    ArrayList prodCatMembers = (ArrayList)pageContext.getSession().getAttribute("CACHE_SEARCH_RESULTS");
    String resultArrayName = (String)pageContext.getSession().getAttribute("CACHE_SEARCH_RESULTS_NAME");
    
    if(prodCatMembers == null || resultArrayName == null || !curFindString.equals(resultArrayName) || viewIndex == 0) {
      //since cache is invalid, should not use prodCatMembers
      prodCatMembers = null;
      
      Debug.logInfo("-=-=-=-=- Current Array not found in session, getting new one...");
      Debug.logInfo("-=-=-=-=- curFindString:" + curFindString + " resultArrayName:" + resultArrayName);
      
      GenericValue category = null;
      try { category = delegator.findByPrimaryKeyCache("ProductCategory",UtilMisc.toMap("productCategoryId",parentId)); }
      catch(GenericEntityException e) { Debug.logWarning(e.getMessage()); category = null; }
      
      if(category != null) {
        Collection prodCatMemberCol = null;
        try { prodCatMemberCol = category.getRelated("ProductCategoryMember"); }
        catch(GenericEntityException e) { Debug.logWarning(e.getMessage()); prodCatMemberCol = null; }
        if(prodCatMemberCol != null) prodCatMembers = new ArrayList(prodCatMemberCol);
      } 
      
      if(prodCatMembers != null) {
        //XXX should be synchronized from multiple clients thrashing eachother (?) - how?
        pageContext.getSession().setAttribute("CACHE_SEARCH_RESULTS", prodCatMembers);
        pageContext.getSession().setAttribute("CACHE_SEARCH_RESULTS_NAME", curFindString);
      }
    }
    
    int lowIndex = viewIndex*viewSize+1;
    int highIndex = (viewIndex+1)*viewSize;
    int listSize = 0;
    if(prodCatMembers!=null) listSize = prodCatMembers.size();
    if(listSize<highIndex) highIndex=listSize;
    
    ArrayList someProducts = new ArrayList();
    if(prodCatMembers != null) {
      for(int ind=lowIndex; ind<=highIndex; ind++) {
        GenericValue prodCatMember = (GenericValue)prodCatMembers.get(ind-1);
        GenericValue prod = null;
        try { prod = delegator.findByPrimaryKeyCache("Product", UtilMisc.toMap("productId", prodCatMember.get("productId"))); }
        catch(GenericEntityException e) { Debug.logWarning(e.getMessage()); prod = null; }
        if(prod != null) someProducts.add(prod);
      }
    }
    
    pageContext.setAttribute(attributePrefix + "viewIndex", new Integer(viewIndex));
    pageContext.setAttribute(attributePrefix + "viewSize", new Integer(viewSize));
    pageContext.setAttribute(attributePrefix + "lowIndex", new Integer(lowIndex));
    pageContext.setAttribute(attributePrefix + "highIndex", new Integer(highIndex));
    pageContext.setAttribute(attributePrefix + "listSize", new Integer(listSize));
    pageContext.setAttribute(attributePrefix + "categoryId", parentId);
    if(someProducts.size() > 0) pageContext.setAttribute(attributePrefix + "productList",someProducts);
  }
  
  public static void getRelatedCategories(PageContext pageContext, String attributeName) {
    ServletRequest request = pageContext.getRequest();
    String requestId = null;
    requestId = UtilFormatOut.checkNull(request.getParameter("catalog_id"), request.getParameter("CATALOG_ID"),
                                        request.getParameter("category_id"), request.getParameter("CATEGORY_ID"));
    if(requestId.equals("")) return;
    Debug.logInfo("[CatalogHelper.getRelatedCategories] RequestID: " + requestId);
    getRelatedCategories(pageContext,attributeName,requestId);
  }
  
  public static void getRelatedCategories(PageContext pageContext, String attributeName, String parentId) {
    ArrayList categories = new ArrayList();
    ServletRequest request = pageContext.getRequest();
    
    Debug.logInfo("[CatalogHelper.getRelatedCategories] ParentID: " + parentId);
    
    GenericDelegator delegator = (GenericDelegator)pageContext.getServletContext().getAttribute("delegator");
    Collection rollups = null;
    try { rollups = delegator.findByAndCache("ProductCategoryRollup",UtilMisc.toMap("parentProductCategoryId",parentId),null); }
    catch(GenericEntityException e) { Debug.logWarning(e.getMessage()); rollups = null; }
    if(rollups != null && rollups.size() > 0) {
      //Debug.log("Rollup size: " + rollups.size());
      Iterator ri = rollups.iterator();
      while ( ri.hasNext() ) {
        GenericValue parent = (GenericValue) ri.next();
        //Debug.log("Adding child of: " + parent.getString("parentProductCategoryId"));
        GenericValue cv = null;
        try { cv = parent.getRelatedOneCache("CurrentProductCategory"); }
        catch(GenericEntityException e) { Debug.logWarning(e.getMessage()); cv = null; }
        if(cv != null) categories.add(cv);
      }
    }
    
    if(categories.size() > 0) pageContext.setAttribute(attributeName,categories);
  }
  
  public static void setTrail(PageContext pageContext, String currentCategory) {
    String previousCategory = pageContext.getRequest().getParameter("pcategory");
    Debug.logInfo("[CatalogHelper.setTrail] Start: previousCategory=" + previousCategory + " currentCategory=" + currentCategory);

    //if there is no current category, just return and do nothing to that the last settings will stay
    if(currentCategory == null || currentCategory.length() <= 0) return;
    
    //always get the last crumb list
    ArrayList crumb = getTrail(pageContext);
    if(crumb == null) crumb = new ArrayList();
        
    //if no previous category was specified, check to see if currentCategory is in the list
    if(previousCategory == null || previousCategory.length() <= 0) {
      if(crumb.contains(currentCategory)) {
        //if cur category is in crumb, remove everything after it and return
        int cindex = crumb.lastIndexOf(currentCategory);
        if(cindex < (crumb.size() - 1)) {
          for(int i = crumb.size() -1; i>cindex; i--) {
            String deadCat = (String)crumb.remove(i);
            Debug.logInfo("[CatalogHelper.setTrail] Removed after current category index: " + i + " catname: " + deadCat);
          }
        }
        return;
      }
      else {
        //current category is not in the list, and no previous category was specified, go back to the beginning
        previousCategory = "TOP";
        crumb.clear();
        crumb.add(previousCategory);
        Debug.logInfo("[CatalogHelper.setTrail] Starting new list, added previousCategory: " + previousCategory);
      }
    }
    
    if(!crumb.contains(previousCategory)) {
      //previous category was NOT in the list, ERROR, start over
      Debug.logInfo("[CatalogHelper.setTrail] ERROR: previousCategory (" + previousCategory + ") was not in the crumb list, position is lost, starting over with TOP");
      previousCategory = "TOP";
      crumb.clear();
      crumb.add(previousCategory);
    }
    else {    
      //remove all categories after the previous category, preparing for adding the current category
      int index = crumb.indexOf(previousCategory);
      if(index < (crumb.size() - 1)) {
        for(int i = crumb.size() -1; i>index; i--) {
          String deadCat = (String)crumb.remove(i);
          Debug.logInfo("[CatalogHelper.setTrail] Removed after previous category index: " + i + " catname: " + deadCat);
        }
      }
    }
    
    //add the current category to the end of the list
    crumb.add(currentCategory);
    Debug.logInfo("[CatalogHelper.setTrail] Continuing list: Added currentCategory: " + currentCategory);
    setTrail(pageContext, crumb);
  }
  
  public static ArrayList getTrail(PageContext pageContext) {
    HttpSession session = pageContext.getSession();
    ArrayList crumb = (ArrayList)session.getAttribute("_BREAD_CRUMB_TRAIL_");
    return crumb;
  }
  
  public static void setTrail(PageContext pageContext, ArrayList crumb) {
    HttpSession session = pageContext.getSession();
    session.setAttribute("_BREAD_CRUMB_TRAIL_", crumb);
  }
  
  public static boolean checkTrailItem(PageContext pageContext,String category) {
    ArrayList crumb = getTrail(pageContext);
    if(crumb != null && crumb.contains(category)) return true;
    else return false;
  }

  public static String lastTrailItem(PageContext pageContext) {
    ArrayList crumb = getTrail(pageContext);
    if(crumb != null && crumb.size() > 0) return (String)crumb.get(crumb.size()-1);
    else return null;
  }
}
