
package org.ofbiz.commonapp.product.feature;

import java.rmi.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.math.*;
import org.ofbiz.commonapp.security.*;
import org.ofbiz.core.util.*;

/**
 * <p><b>Title:</b> Feature Data Object Entity
 * <p><b>Description:</b> None
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
 *@author     David E. Jones
 *@created    Fri Jul 27 01:18:28 MDT 2001
 *@version    1.0
 */

public class FeatureDataObjectWebEvent
{
  /** An HTTP WebEvent handler that updates a FeatureDataObject entity
   *
   * @param request The HTTP request object for the current JSP or Servlet request.
   * @param response The HTTP response object for the current JSP or Servlet request.
   * @return Returns a String specifying the outcome state of the event. This is used to decide which event to run next or which view to display. If null no event is run nor view displayed, allowing the event to call a forward on a RequestDispatcher.
   * @exception javax.servlet.ServletException Standard J2EE Servlet Exception
   * @exception java.rmi.RemoteException Standard RMI Remote Exception
   * @exception java.io.IOException Standard IO Exception
   */
  public static String updateFeatureDataObject(HttpServletRequest request, HttpServletResponse response) throws javax.servlet.ServletException, java.rmi.RemoteException, java.io.IOException
  {
    // a little check to reprocessing the web event in error cases - would cause infinate loop
    if(request.getAttribute("ERROR_MESSAGE") != null) return "success";
    if(request.getSession().getAttribute("ERROR_MESSAGE") != null) return "success";    
    String errMsg = "";
    
    String updateMode = request.getParameter("UPDATE_MODE");
    if(updateMode == null || updateMode.length() <= 0)
    {
      request.getSession().setAttribute("ERROR_MESSAGE", "updateFeatureDataObject: Update Mode was not specified, but is required.");
      Debug.logWarning("updateFeatureDataObject: Update Mode was not specified, but is required.");
    }
    
    //check permissions before moving on...
    if(!Security.hasEntityPermission("FEATURE_DATA_OBJECT", "_" + updateMode, request.getSession()))
    {
      request.getSession().setAttribute("ERROR_MESSAGE", "You do not have sufficient permissions to "+ updateMode + " FeatureDataObject (FEATURE_DATA_OBJECT_" + updateMode + " or FEATURE_DATA_OBJECT_ADMIN needed).");
      return "success";
    }

    //get the primary key parameters...
  
    String dataObjectId = request.getParameter("FEATURE_DATA_OBJECT_DATA_OBJECT_ID");  
    String productFeatureId = request.getParameter("FEATURE_DATA_OBJECT_PRODUCT_FEATURE_ID");  

  

    //if this is a delete, do that before getting all of the non-pk parameters and validating them
    if(updateMode.equals("DELETE"))
    {
      //Remove associated/dependent entries from other tables here
      //Delete actual FeatureDataObject last, just in case database is set up to do a cascading delete, caches won't get cleared
      FeatureDataObjectHelper.removeByPrimaryKey(dataObjectId, productFeatureId);
      return "success";
    }

    //get the non-primary key parameters
  

  

    //if the updateMode is CREATE, check to see if an entity with the specified primary key already exists
    if(updateMode.compareTo("CREATE") == 0)
      if(FeatureDataObjectHelper.findByPrimaryKey(dataObjectId, productFeatureId) != null) errMsg = errMsg + "<li>FeatureDataObject already exists with DATA_OBJECT_ID, PRODUCT_FEATURE_ID:" + dataObjectId + ", " + productFeatureId + "; please change.";

    //Validate parameters...
  
    if(!UtilValidate.isNotEmpty(dataObjectId)) errMsg = errMsg + "<li>DATA_OBJECT_ID isNotEmpty failed: " + UtilValidate.isNotEmptyMsg;
    if(!UtilValidate.isNotEmpty(productFeatureId)) errMsg = errMsg + "<li>PRODUCT_FEATURE_ID isNotEmpty failed: " + UtilValidate.isNotEmptyMsg;

    if(errMsg.length() > 0)
    {
      errMsg = "<br><b>The following error(s) occured:</b><ul>" + errMsg + "</ul>";
      request.setAttribute("ERROR_MESSAGE", errMsg);
      return "error";
    }

    if(updateMode.equals("CREATE"))
    {
      FeatureDataObject featureDataObject = FeatureDataObjectHelper.create(dataObjectId, productFeatureId);
      if(featureDataObject == null)
      {
        request.getSession().setAttribute("ERROR_MESSAGE", "Creation of FeatureDataObject failed. DATA_OBJECT_ID, PRODUCT_FEATURE_ID: " + dataObjectId + ", " + productFeatureId);
        return "success";
      }
    }
    else if(updateMode.equals("UPDATE"))
    {
      FeatureDataObject featureDataObject = FeatureDataObjectHelper.update(dataObjectId, productFeatureId);
      if(featureDataObject == null)
      {
        request.getSession().setAttribute("ERROR_MESSAGE", "Update of FeatureDataObject failed. DATA_OBJECT_ID, PRODUCT_FEATURE_ID: " + dataObjectId + ", " + productFeatureId);
        return "success";
      }
    }
    else
    {
      request.getSession().setAttribute("ERROR_MESSAGE", "updateFeatureDataObject: Update Mode specified (" + updateMode + ") was not valid.");
      Debug.logWarning("updateFeatureDataObject: Update Mode specified (" + updateMode + ") was not valid.");
    }

    return "success";
  }
}
