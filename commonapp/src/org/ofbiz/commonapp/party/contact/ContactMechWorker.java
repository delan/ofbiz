/*
 * $Id$
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
package org.ofbiz.commonapp.party.contact;

import java.util.*;
import javax.servlet.*;
import javax.servlet.jsp.*;

import org.ofbiz.core.entity.*;
import org.ofbiz.core.util.*;

/**
 * Worker methods for Contact Mechanisms
 *
 * @author     <a href="mailto:jonesde@ofbiz.org">David E. Jones</a>
 * @version    $Revision$
 * @since      2.0
 */
public class ContactMechWorker {
    public static void getPartyContactMechValueMaps(PageContext pageContext, String partyId, boolean showOld, String partyContactMechValueMapsAttr) {
        GenericDelegator delegator = (GenericDelegator) pageContext.getRequest().getAttribute("delegator");
        List partyContactMechValueMaps = getPartyContactMechValueMaps(delegator, partyId, showOld);
        if (partyContactMechValueMaps.size() > 0) {
            pageContext.setAttribute(partyContactMechValueMapsAttr, partyContactMechValueMaps);
        }
    }
    
    public static List getPartyContactMechValueMaps(GenericDelegator delegator, String partyId, boolean showOld) {
       return getPartyContactMechValueMaps(delegator, partyId, showOld, null);    
    }
    
    public static List getPartyContactMechValueMaps(GenericDelegator delegator, String partyId, boolean showOld, String contactMechTypeId) {
        List partyContactMechValueMaps = new LinkedList();

        Iterator allPartyContactMechs = null;

        try {
            List tempCol = delegator.findByAnd("PartyContactMech", UtilMisc.toMap("partyId", partyId));
            if(contactMechTypeId != null) {
                List tempColTemp = new LinkedList();
                for(Iterator iterator = tempCol.iterator(); iterator.hasNext();) {
                    GenericValue partyContactMech = (GenericValue) iterator.next();
                    GenericValue contactMech = delegator.getRelatedOne("ContactMech", partyContactMech);
                    if(contactMech != null && contactMechTypeId.equals(contactMech.getString("contactMechTypeId"))) {
                        tempColTemp.add(partyContactMech);
                    }
                        
                }
                tempCol = tempColTemp;
            } 
            if (!showOld) tempCol = EntityUtil.filterByDate(tempCol, true);
            allPartyContactMechs = UtilMisc.toIterator(tempCol);
        } catch (GenericEntityException e) {
            Debug.logWarning(e);
        }

        while (allPartyContactMechs != null && allPartyContactMechs.hasNext()) {
            GenericValue partyContactMech = (GenericValue) allPartyContactMechs.next();
            GenericValue contactMech = null;

            try {
                contactMech = partyContactMech.getRelatedOne("ContactMech");
            } catch (GenericEntityException e) {
                Debug.logWarning(e);
            }
            if (contactMech != null) {
                Map partyContactMechValueMap = new HashMap();

                partyContactMechValueMaps.add(partyContactMechValueMap);
                partyContactMechValueMap.put("contactMech", contactMech);
                partyContactMechValueMap.put("partyContactMech", partyContactMech);

                try {
                    partyContactMechValueMap.put("contactMechType", contactMech.getRelatedOneCache("ContactMechType"));
                } catch (GenericEntityException e) {
                    Debug.logWarning(e);
                }

                try {
                    List partyContactMechPurposes = partyContactMech.getRelated("PartyContactMechPurpose");

                    if (!showOld) partyContactMechPurposes = EntityUtil.filterByDate(partyContactMechPurposes, true);
                    partyContactMechValueMap.put("partyContactMechPurposes", partyContactMechPurposes);
                } catch (GenericEntityException e) {
                    Debug.logWarning(e);
                }

                try {
                    if ("POSTAL_ADDRESS".equals(contactMech.getString("contactMechTypeId"))) {
                        partyContactMechValueMap.put("postalAddress", contactMech.getRelatedOne("PostalAddress"));
                    } else if ("TELECOM_NUMBER".equals(contactMech.getString("contactMechTypeId"))) {
                        partyContactMechValueMap.put("telecomNumber", contactMech.getRelatedOne("TelecomNumber"));
                    }
                } catch (GenericEntityException e) {
                    Debug.logWarning(e);
                }
            }
        }

        return partyContactMechValueMaps;
    }

    public static void getOrderContactMechValueMaps(PageContext pageContext, String orderId, String orderContactMechValueMapsAttr) {
        GenericDelegator delegator = (GenericDelegator) pageContext.getRequest().getAttribute("delegator");
        List maps = getOrderContactMechValueMaps(delegator, orderId);
        if (maps != null && maps.size() > 0) {
            pageContext.setAttribute(orderContactMechValueMapsAttr, maps);
        }
    }
    public static List getOrderContactMechValueMaps(GenericDelegator delegator, String orderId) {        
        List orderContactMechValueMaps = new LinkedList();

        Iterator allOrderContactMechs = null;

        try {
            Collection tempCol = delegator.findByAnd("OrderContactMech", UtilMisc.toMap("orderId", orderId), UtilMisc.toList("contactMechPurposeTypeId"));

            allOrderContactMechs = UtilMisc.toIterator(tempCol);
        } catch (GenericEntityException e) {
            Debug.logWarning(e);
        }

        while (allOrderContactMechs != null && allOrderContactMechs.hasNext()) {
            GenericValue orderContactMech = (GenericValue) allOrderContactMechs.next();
            GenericValue contactMech = null;

            try {
                contactMech = orderContactMech.getRelatedOne("ContactMech");
            } catch (GenericEntityException e) {
                Debug.logWarning(e);
            }
            if (contactMech != null) {
                Map orderContactMechValueMap = new HashMap();

                orderContactMechValueMaps.add(orderContactMechValueMap);
                orderContactMechValueMap.put("contactMech", contactMech);
                orderContactMechValueMap.put("orderContactMech", orderContactMech);

                try {
                    orderContactMechValueMap.put("contactMechType", contactMech.getRelatedOneCache("ContactMechType"));
                } catch (GenericEntityException e) {
                    Debug.logWarning(e);
                }

                try {
                    GenericValue contactMechPurposeType = orderContactMech.getRelatedOne("ContactMechPurposeType");

                    orderContactMechValueMap.put("contactMechPurposeType", contactMechPurposeType);
                } catch (GenericEntityException e) {
                    Debug.logWarning(e);
                }

                try {
                    if ("POSTAL_ADDRESS".equals(contactMech.getString("contactMechTypeId"))) {
                        orderContactMechValueMap.put("postalAddress", contactMech.getRelatedOne("PostalAddress"));
                    } else if ("TELECOM_NUMBER".equals(contactMech.getString("contactMechTypeId"))) {
                        orderContactMechValueMap.put("telecomNumber", contactMech.getRelatedOne("TelecomNumber"));
                    }
                } catch (GenericEntityException e) {
                    Debug.logWarning(e);
                }
            }
        }

        return orderContactMechValueMaps;
    }

    public static Collection getWorkEffortContactMechValueMaps(GenericDelegator delegator, String workEffortId) {
        Collection workEffortContactMechValueMaps = new LinkedList();

        Iterator allWorkEffortContactMechs = null;

        try {
            Collection tempCol = delegator.findByAnd("WorkEffortContactMech", UtilMisc.toMap("workEffortId", workEffortId));
            allWorkEffortContactMechs = UtilMisc.toIterator(tempCol);
        } catch (GenericEntityException e) {
            Debug.logWarning(e);
        }

        while (allWorkEffortContactMechs != null && allWorkEffortContactMechs.hasNext()) {
            GenericValue workEffortContactMech = (GenericValue) allWorkEffortContactMechs.next();
            GenericValue contactMech = null;

            try {
                contactMech = workEffortContactMech.getRelatedOne("ContactMech");
            } catch (GenericEntityException e) {
                Debug.logWarning(e);
            }
            if (contactMech != null) {
                Map workEffortContactMechValueMap = new HashMap();

                workEffortContactMechValueMaps.add(workEffortContactMechValueMap);
                workEffortContactMechValueMap.put("contactMech", contactMech);
                workEffortContactMechValueMap.put("workEffortContactMech", workEffortContactMech);

                try {
                    workEffortContactMechValueMap.put("contactMechType", contactMech.getRelatedOneCache("ContactMechType"));
                } catch (GenericEntityException e) {
                    Debug.logWarning(e);
                }

                try {
                    if ("POSTAL_ADDRESS".equals(contactMech.getString("contactMechTypeId"))) {
                        workEffortContactMechValueMap.put("postalAddress", contactMech.getRelatedOne("PostalAddress"));
                    } else if ("TELECOM_NUMBER".equals(contactMech.getString("contactMechTypeId"))) {
                        workEffortContactMechValueMap.put("telecomNumber", contactMech.getRelatedOne("TelecomNumber"));
                    }
                } catch (GenericEntityException e) {
                    Debug.logWarning(e);
                }
            }
        }

        return workEffortContactMechValueMaps.size() > 0 ? workEffortContactMechValueMaps : null;
    }
    
    /** TO BE REMOVED (DEJ 20030221): This method was for use in a JSP and when they are removed this can be removed as well rather than being maintained, should be removed when eCommerce and party mgr and possible other places are converted to FTL */
    public static void getContactMechAndRelated(PageContext pageContext, String partyId, String contactMechAttr, String contactMechIdAttr,
        String partyContactMechAttr, String partyContactMechPurposesAttr, String contactMechTypeIdAttr, String contactMechTypeAttr, String purposeTypesAttr,
        String postalAddressAttr, String telecomNumberAttr, String requestNameAttr, String donePageAttr, String tryEntityAttr, String contactMechTypesAttr) {

        ServletRequest request = pageContext.getRequest();
        GenericDelegator delegator = (GenericDelegator) pageContext.getRequest().getAttribute("delegator");

        boolean tryEntity = true;

        if (request.getAttribute(SiteDefs.ERROR_MESSAGE) != null) tryEntity = false;
        if ("true".equals(request.getParameter("tryEntity"))) tryEntity = true;

        String donePage = request.getParameter("DONE_PAGE");

        if (donePage == null) donePage = (String) request.getAttribute("DONE_PAGE");
        if (donePage == null || donePage.length() <= 0) donePage = "viewprofile";
        pageContext.setAttribute(donePageAttr, donePage);

        String contactMechTypeId = request.getParameter("preContactMechTypeId");

        if (contactMechTypeId == null) contactMechTypeId = (String) request.getAttribute("preContactMechTypeId");
        if (contactMechTypeId != null)
            tryEntity = false;

        String contactMechId = request.getParameter("contactMechId");

        if (request.getAttribute("contactMechId") != null)
            contactMechId = (String) request.getAttribute("contactMechId");

        GenericValue contactMech = null;

        if (contactMechId != null) {
            pageContext.setAttribute(contactMechIdAttr, contactMechId);

            // try to find a PartyContactMech with a valid date range
            List partyContactMechs = null;

            try {
                partyContactMechs = EntityUtil.filterByDate(delegator.findByAnd("PartyContactMech", UtilMisc.toMap("partyId", partyId, "contactMechId", contactMechId)), true);
            } catch (GenericEntityException e) {
                Debug.logWarning(e);
            }

            GenericValue partyContactMech = EntityUtil.getFirst(partyContactMechs);

            if (partyContactMech != null) {
                pageContext.setAttribute(partyContactMechAttr, partyContactMech);

                Collection partyContactMechPurposes = null;

                try {
                    partyContactMechPurposes = EntityUtil.filterByDate(partyContactMech.getRelated("PartyContactMechPurpose"), true);
                } catch (GenericEntityException e) {
                    Debug.logWarning(e);
                }
                if (partyContactMechPurposes != null && partyContactMechPurposes.size() > 0)
                    pageContext.setAttribute(partyContactMechPurposesAttr, partyContactMechPurposes);
            }

            try {
                contactMech = delegator.findByPrimaryKey("ContactMech", UtilMisc.toMap("contactMechId", contactMechId));
            } catch (GenericEntityException e) {
                Debug.logWarning(e);
            }

            if (contactMech != null) {
                pageContext.setAttribute(contactMechAttr, contactMech);
                contactMechTypeId = contactMech.getString("contactMechTypeId");
            }
        }

        if (contactMechTypeId != null) {
            pageContext.setAttribute(contactMechTypeIdAttr, contactMechTypeId);

            try {
                GenericValue contactMechType = delegator.findByPrimaryKey("ContactMechType", UtilMisc.toMap("contactMechTypeId", contactMechTypeId));

                if (contactMechType != null)
                    pageContext.setAttribute(contactMechTypeAttr, contactMechType);
            } catch (GenericEntityException e) {
                Debug.logWarning(e);
            }

            Collection purposeTypes = new LinkedList();
            Iterator typePurposes = null;

            try {
                typePurposes = UtilMisc.toIterator(delegator.findByAnd("ContactMechTypePurpose", UtilMisc.toMap("contactMechTypeId", contactMechTypeId)));
            } catch (GenericEntityException e) {
                Debug.logWarning(e);
            }
            while (typePurposes != null && typePurposes.hasNext()) {
                GenericValue contactMechTypePurpose = (GenericValue) typePurposes.next();
                GenericValue contactMechPurposeType = null;

                try {
                    contactMechPurposeType = contactMechTypePurpose.getRelatedOne("ContactMechPurposeType");
                } catch (GenericEntityException e) {
                    Debug.logWarning(e);
                }
                if (contactMechPurposeType != null) {
                    purposeTypes.add(contactMechPurposeType);
                }
            }
            if (purposeTypes.size() > 0)
                pageContext.setAttribute(purposeTypesAttr, purposeTypes);
        }

        String requestName;

        if (contactMech == null) {
            // create
            if ("POSTAL_ADDRESS".equals(contactMechTypeId)) {
                if (request.getParameter("contactMechPurposeTypeId") != null || request.getAttribute("contactMechPurposeTypeId") != null) {
                    requestName = "createPostalAddressAndPurpose";
                } else {
                    requestName = "createPostalAddress";
                }
            } else if ("TELECOM_NUMBER".equals(contactMechTypeId)) {
                requestName = "createTelecomNumber";
            } else if ("EMAIL_ADDRESS".equals(contactMechTypeId)) {
                requestName = "createEmailAddress";
            } else {
                requestName = "createContactMech";
            }
        } else {
            // update
            if ("POSTAL_ADDRESS".equals(contactMechTypeId)) {
                requestName = "updatePostalAddress";
            } else if ("TELECOM_NUMBER".equals(contactMechTypeId)) {
                requestName = "updateTelecomNumber";
            } else if ("EMAIL_ADDRESS".equals(contactMechTypeId)) {
                requestName = "updateEmailAddress";
            } else {
                requestName = "updateContactMech";
            }
        }
        pageContext.setAttribute(requestNameAttr, requestName);

        if ("POSTAL_ADDRESS".equals(contactMechTypeId)) {
            GenericValue postalAddress = null;

            try {
                if (contactMech != null) postalAddress = contactMech.getRelatedOne("PostalAddress");
            } catch (GenericEntityException e) {
                Debug.logWarning(e);
            }
            if (postalAddress != null) pageContext.setAttribute(postalAddressAttr, postalAddress);
        } else if ("TELECOM_NUMBER".equals(contactMechTypeId)) {
            GenericValue telecomNumber = null;

            try {
                if (contactMech != null) telecomNumber = contactMech.getRelatedOne("TelecomNumber");
            } catch (GenericEntityException e) {
                Debug.logWarning(e);
            }
            if (telecomNumber != null) pageContext.setAttribute(telecomNumberAttr, telecomNumber);
        }

        if ("true".equals(request.getParameter("useValues"))) tryEntity = true;
        pageContext.setAttribute(tryEntityAttr, new Boolean(tryEntity));

        try {
            Collection contactMechTypes = delegator.findAllCache("ContactMechType", null);

            if (contactMechTypes != null) {
                pageContext.setAttribute(contactMechTypesAttr, contactMechTypes);
            }
        } catch (GenericEntityException e) {
            Debug.logWarning(e);
        }
    }

    public static void getContactMechAndRelated(ServletRequest request, String partyId, Map target) {
        GenericDelegator delegator = (GenericDelegator) request.getAttribute("delegator");

        boolean tryEntity = true;
        if (request.getAttribute(SiteDefs.ERROR_MESSAGE) != null) tryEntity = false;
        if ("true".equals(request.getParameter("tryEntity"))) tryEntity = true;

        String donePage = request.getParameter("DONE_PAGE");
        if (donePage == null) donePage = (String) request.getAttribute("DONE_PAGE");
        if (donePage == null || donePage.length() <= 0) donePage = "viewprofile";
        target.put("donePage", donePage);

        String contactMechTypeId = request.getParameter("preContactMechTypeId");

        if (contactMechTypeId == null) contactMechTypeId = (String) request.getAttribute("preContactMechTypeId");
        if (contactMechTypeId != null)
            tryEntity = false;

        String contactMechId = request.getParameter("contactMechId");

        if (request.getAttribute("contactMechId") != null)
            contactMechId = (String) request.getAttribute("contactMechId");

        GenericValue contactMech = null;

        if (contactMechId != null) {
            target.put("contactMechId", contactMechId);

            // try to find a PartyContactMech with a valid date range
            List partyContactMechs = null;

            try {
                partyContactMechs = EntityUtil.filterByDate(delegator.findByAnd("PartyContactMech", UtilMisc.toMap("partyId", partyId, "contactMechId", contactMechId)), true);
            } catch (GenericEntityException e) {
                Debug.logWarning(e);
            }

            GenericValue partyContactMech = EntityUtil.getFirst(partyContactMechs);

            if (partyContactMech != null) {
                target.put("partyContactMech", partyContactMech);

                Collection partyContactMechPurposes = null;

                try {
                    partyContactMechPurposes = EntityUtil.filterByDate(partyContactMech.getRelated("PartyContactMechPurpose"), true);
                } catch (GenericEntityException e) {
                    Debug.logWarning(e);
                }
                if (partyContactMechPurposes != null && partyContactMechPurposes.size() > 0)
                    target.put("partyContactMechPurposes", partyContactMechPurposes);
            }

            try {
                contactMech = delegator.findByPrimaryKey("ContactMech", UtilMisc.toMap("contactMechId", contactMechId));
            } catch (GenericEntityException e) {
                Debug.logWarning(e);
            }

            if (contactMech != null) {
                target.put("contactMech", contactMech);
                contactMechTypeId = contactMech.getString("contactMechTypeId");
            }
        }

        if (contactMechTypeId != null) {
            target.put("contactMechTypeId", contactMechTypeId);

            try {
                GenericValue contactMechType = delegator.findByPrimaryKey("ContactMechType", UtilMisc.toMap("contactMechTypeId", contactMechTypeId));

                if (contactMechType != null)
                    target.put("contactMechType", contactMechType);
            } catch (GenericEntityException e) {
                Debug.logWarning(e);
            }

            Collection purposeTypes = new LinkedList();
            Iterator typePurposes = null;

            try {
                typePurposes = UtilMisc.toIterator(delegator.findByAnd("ContactMechTypePurpose", UtilMisc.toMap("contactMechTypeId", contactMechTypeId)));
            } catch (GenericEntityException e) {
                Debug.logWarning(e);
            }
            while (typePurposes != null && typePurposes.hasNext()) {
                GenericValue contactMechTypePurpose = (GenericValue) typePurposes.next();
                GenericValue contactMechPurposeType = null;

                try {
                    contactMechPurposeType = contactMechTypePurpose.getRelatedOne("ContactMechPurposeType");
                } catch (GenericEntityException e) {
                    Debug.logWarning(e);
                }
                if (contactMechPurposeType != null) {
                    purposeTypes.add(contactMechPurposeType);
                }
            }
            if (purposeTypes.size() > 0)
                target.put("purposeTypes", purposeTypes);
        }

        String requestName;

        if (contactMech == null) {
            // create
            if ("POSTAL_ADDRESS".equals(contactMechTypeId)) {
                if (request.getParameter("contactMechPurposeTypeId") != null || request.getAttribute("contactMechPurposeTypeId") != null) {
                    requestName = "createPostalAddressAndPurpose";
                } else {
                    requestName = "createPostalAddress";
                }
            } else if ("TELECOM_NUMBER".equals(contactMechTypeId)) {
                requestName = "createTelecomNumber";
            } else if ("EMAIL_ADDRESS".equals(contactMechTypeId)) {
                requestName = "createEmailAddress";
            } else {
                requestName = "createContactMech";
            }
        } else {
            // update
            if ("POSTAL_ADDRESS".equals(contactMechTypeId)) {
                requestName = "updatePostalAddress";
            } else if ("TELECOM_NUMBER".equals(contactMechTypeId)) {
                requestName = "updateTelecomNumber";
            } else if ("EMAIL_ADDRESS".equals(contactMechTypeId)) {
                requestName = "updateEmailAddress";
            } else {
                requestName = "updateContactMech";
            }
        }
        target.put("requestName", requestName);

        if ("POSTAL_ADDRESS".equals(contactMechTypeId)) {
            GenericValue postalAddress = null;

            try {
                if (contactMech != null) postalAddress = contactMech.getRelatedOne("PostalAddress");
            } catch (GenericEntityException e) {
                Debug.logWarning(e);
            }
            if (postalAddress != null) target.put("postalAddress", postalAddress);
        } else if ("TELECOM_NUMBER".equals(contactMechTypeId)) {
            GenericValue telecomNumber = null;

            try {
                if (contactMech != null) telecomNumber = contactMech.getRelatedOne("TelecomNumber");
            } catch (GenericEntityException e) {
                Debug.logWarning(e);
            }
            if (telecomNumber != null) target.put("telecomNumber", telecomNumber);
        }

        if ("true".equals(request.getParameter("useValues"))) tryEntity = true;
        target.put("tryEntity", new Boolean(tryEntity));

        try {
            Collection contactMechTypes = delegator.findAllCache("ContactMechType", null);

            if (contactMechTypes != null) {
                target.put("contactMechTypes", contactMechTypes);
            }
        } catch (GenericEntityException e) {
            Debug.logWarning(e);
        }
    }

    /** TO BE REMOVED (DEJ 20030221): This method was for use in a JSP and when they are removed this can be removed as well rather than being maintained, should be removed when eCommerce and party mgr and possible other places are converted to FTL */
    public static void getPartyPostalAddresses(PageContext pageContext, String partyId, String curContactMechId, String postalAddressInfosAttr) {
        ServletRequest request = pageContext.getRequest();
        List postalAddressInfos = getPartyPostalAddresses(request, partyId, curContactMechId);
        if (postalAddressInfos.size() > 0) {
            pageContext.setAttribute(postalAddressInfosAttr, postalAddressInfos);
        }
    }
    
    public static List getPartyPostalAddresses(ServletRequest request, String partyId, String curContactMechId) {
        GenericDelegator delegator = (GenericDelegator) request.getAttribute("delegator");
        List postalAddressInfos = new LinkedList();

        Iterator allPartyContactMechs = null;

        try {
            allPartyContactMechs = UtilMisc.toIterator(EntityUtil.filterByDate(delegator.findByAnd("PartyContactMech", UtilMisc.toMap("partyId", partyId)), true));
        } catch (GenericEntityException e) {
            Debug.logWarning(e);
        }
        while (allPartyContactMechs != null && allPartyContactMechs.hasNext()) {
            GenericValue partyContactMech = (GenericValue) allPartyContactMechs.next();
            GenericValue contactMech = null;

            try {
                contactMech = partyContactMech.getRelatedOne("ContactMech");
            } catch (GenericEntityException e) {
                Debug.logWarning(e);
            }
            if (contactMech != null && "POSTAL_ADDRESS".equals(contactMech.getString("contactMechTypeId")) && !contactMech.getString("contactMechId").equals(curContactMechId)) {
                Map postalAddressInfo = new HashMap();

                postalAddressInfos.add(postalAddressInfo);
                postalAddressInfo.put("contactMech", contactMech);
                postalAddressInfo.put("partyContactMech", partyContactMech);

                try {
                    GenericValue postalAddress = contactMech.getRelatedOne("PostalAddress");
                    postalAddressInfo.put("postalAddress", postalAddress);
                } catch (GenericEntityException e) {
                    Debug.logWarning(e);
                }

                try {
                    List partyContactMechPurposes = EntityUtil.filterByDate(partyContactMech.getRelated("PartyContactMechPurpose"), true);
                    postalAddressInfo.put("partyContactMechPurposes", partyContactMechPurposes);
                } catch (GenericEntityException e) {
                    Debug.logWarning(e);
                }
            }
        }

        return postalAddressInfos;
    }

    /** TO BE REMOVED (DEJ 20030221): This method was for use in a JSP and when they are removed this can be removed as well rather than being maintained, should be removed when eCommerce and party mgr and possible other places are converted to FTL */
    public static void getCurrentPostalAddress(PageContext pageContext, String partyId, String curContactMechId,
            String curPartyContactMechAttr, String curContactMechAttr, String curPostalAddressAttr, String curPartyContactMechPurposesAttr) {
        ServletRequest request = pageContext.getRequest();
        Map results = getCurrentPostalAddress(request, partyId, curContactMechId);
        if (results.get("curPartyContactMech") != null) pageContext.setAttribute(curPartyContactMechAttr, results.get("curPartyContactMech"));
        if (results.get("curContactMech") != null) pageContext.setAttribute(curContactMechAttr, results.get("curContactMech"));
        if (results.get("curPostalAddress") != null) pageContext.setAttribute(curPostalAddressAttr, results.get("curPostalAddress"));
        if (results.get("curPartyContactMechPurposes") != null) pageContext.setAttribute(curPartyContactMechPurposesAttr, results.get("curPartyContactMechPurposes"));
    }
    public static Map getCurrentPostalAddress(ServletRequest request, String partyId, String curContactMechId) {
        GenericDelegator delegator = (GenericDelegator) request.getAttribute("delegator");
        Map results = new HashMap();
        
        if (curContactMechId != null) {
            List partyContactMechs = null;

            try {
                partyContactMechs = EntityUtil.filterByDate(delegator.findByAnd("PartyContactMech", UtilMisc.toMap("partyId", partyId, "contactMechId", curContactMechId)), true);
            } catch (GenericEntityException e) {
                Debug.logWarning(e);
            }
            GenericValue curPartyContactMech = EntityUtil.getFirst(partyContactMechs);
            results.put("curPartyContactMech", curPartyContactMech);

            GenericValue curContactMech = null;
            if (curPartyContactMech != null) {
                try {
                    curContactMech = curPartyContactMech.getRelatedOne("ContactMech");
                } catch (GenericEntityException e) {
                    Debug.logWarning(e);
                }

                Collection curPartyContactMechPurposes = null;
                try {
                    curPartyContactMechPurposes = EntityUtil.filterByDate(curPartyContactMech.getRelated("PartyContactMechPurpose"), true);
                } catch (GenericEntityException e) {
                    Debug.logWarning(e);
                }
                results.put("curPartyContactMechPurposes", curPartyContactMechPurposes);
            }
            results.put("curContactMech", curContactMech);

            GenericValue curPostalAddress = null;
            if (curContactMech != null) {
                try {
                    curPostalAddress = curContactMech.getRelatedOne("PostalAddress");
                } catch (GenericEntityException e) {
                    Debug.logWarning(e);
                }
            }

            results.put("curPostalAddress", curPostalAddress);
        }
        return results;
    }    
}
