/*
 * $Id$
 * $Log$
 * Revision 1.5  2002/01/25 12:15:35  jonesde
 * A few changes to handle create postal address and purpose at the same time, small cleanups
 *
 * Revision 1.4  2002/01/24 11:53:37  jonesde
 * Moved party specific worker from party/contact to party/party
 *
 * Revision 1.3  2002/01/24 11:11:20  jonesde
 * Added workers for viewprofile
 *
 * Revision 1.2  2002/01/24 05:14:12  jonesde
 * Added credit card info related workers
 *
 * Revision 1.1  2002/01/23 10:22:03  jonesde
 * Major refactoring of contact mech stuff, more things working too
 *
 *
 */
package org.ofbiz.commonapp.party.contact;

import java.util.*;
import javax.servlet.*;
import javax.servlet.jsp.*;

import org.ofbiz.core.entity.*;
import org.ofbiz.core.util.*;

/**
 * <p><b>Title:</b> Worker methods for Contact Mechanisms
 * <p><b>Description:</b> None
 * <p>Copyright (c) 2001 The Open For Business Project (www.ofbiz.org) and repected authors.
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
 *@author <a href="mailto:jonesde@ofbiz.org">David E. Jones</a>
 *@version 1.0
 *@created January 22, 2002
 */
public class ContactMechWorker {
    public static void getPartyCreditCardInfos(PageContext pageContext, String partyId, boolean showOld, String creditCardInfosAttr) {
        GenericDelegator delegator = (GenericDelegator) pageContext.getRequest().getAttribute("delegator");
        try {
            Collection creditCardInfos = delegator.findByAnd("CreditCardInfo", UtilMisc.toMap("partyId", partyId));
            if (!showOld) creditCardInfos = EntityUtil.filterByDate(creditCardInfos);
            if (creditCardInfos != null)
                pageContext.setAttribute(creditCardInfosAttr, creditCardInfos);
        } catch (GenericEntityException e) {
            Debug.logWarning(e);
        }
    }
    
    public static void getPartyContactMechValueMaps(PageContext pageContext, String partyId, boolean showOld, String partyContactMechValueMapsAttr) {
        GenericDelegator delegator = (GenericDelegator) pageContext.getRequest().getAttribute("delegator");
        Collection partyContactMechValueMaps = new LinkedList();
        
        Iterator allPartyContactMechs = null;
        try {
            Collection tempCol = delegator.findByAnd("PartyContactMech", UtilMisc.toMap("partyId", partyId));
            if (!showOld) tempCol = EntityUtil.filterByDate(tempCol);
            allPartyContactMechs = UtilMisc.toIterator(tempCol);
        } catch (GenericEntityException e) {
            Debug.logWarning(e);
        }

        while(allPartyContactMechs != null && allPartyContactMechs.hasNext()) {
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
                    Collection partyContactMechPurposes = partyContactMech.getRelated("PartyContactMechPurpose");
                    if (!showOld) partyContactMechPurposes = EntityUtil.filterByDate(partyContactMechPurposes);
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
        
        if (partyContactMechValueMaps.size() > 0) {
            pageContext.setAttribute(partyContactMechValueMapsAttr, partyContactMechValueMaps);
        }
    }

    public static void getContactMechAndRelated(PageContext pageContext, String partyId, String contactMechAttr, String contactMechIdAttr,
            String partyContactMechAttr, String partyContactMechPurposesAttr, String contactMechTypeIdAttr, String contactMechTypeAttr, String purposeTypesAttr,
            String postalAddressAttr, String telecomNumberAttr, String requestNameAttr, String donePageAttr, String tryEntityAttr, String contactMechTypesAttr) {

        ServletRequest request = pageContext.getRequest();
        GenericDelegator delegator = (GenericDelegator) pageContext.getRequest().getAttribute("delegator");
        
        boolean tryEntity = true;
        if (request.getAttribute(SiteDefs.ERROR_MESSAGE) != null) tryEntity = false;
        if ("true".equals(request.getParameter("tryEntity"))) tryEntity = true;

        String donePage = request.getParameter("DONE_PAGE");
        if (donePage == null || donePage.length() <= 0) donePage="viewprofile";
        pageContext.setAttribute(donePageAttr, donePage);

        String contactMechTypeId = null;
        if (request.getParameter("preContactMechTypeId") != null) {
            contactMechTypeId = request.getParameter("preContactMechTypeId");
            tryEntity = false;
        }
        
        String contactMechId = request.getParameter("contactMechId");
        if (request.getAttribute("contactMechId") != null)
            contactMechId = (String) request.getAttribute("contactMechId");

        GenericValue contactMech = null;
        if (contactMechId != null) {
            pageContext.setAttribute(contactMechIdAttr, contactMechId);

            //try to find a PartyContactMech with a valid date range
            Collection partyContactMechs = null;
            try {
                partyContactMechs = EntityUtil.filterByDate(delegator.findByAnd("PartyContactMech", UtilMisc.toMap("partyId", partyId, "contactMechId", contactMechId)));
            } catch (GenericEntityException e) {
                Debug.logWarning(e);
            }

            GenericValue partyContactMech = EntityUtil.getFirst(partyContactMechs);
            if (partyContactMech != null) {
                pageContext.setAttribute(partyContactMechAttr, partyContactMech);

                Collection partyContactMechPurposes = null;
                try {
                    partyContactMechPurposes = EntityUtil.filterByDate(partyContactMech.getRelated("PartyContactMechPurpose"));
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
            //create
            if ("POSTAL_ADDRESS".equals(contactMechTypeId)) {
                if (request.getParameter("contactMechPurposeTypeId") != null) {
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
            //update
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
    
    public static void getCreditCardInfoAndRelated(PageContext pageContext, String partyId, 
            String creditCardAttr, String creditCardIdAttr, String curContactMechIdAttr, 
            String curPartyContactMechAttr, String curContactMechAttr, String curPostalAddressAttr, 
            String curPartyContactMechPurposesAttr, String donePageAttr, String tryEntityAttr) {

        ServletRequest request = pageContext.getRequest();
        GenericDelegator delegator = (GenericDelegator) pageContext.getRequest().getAttribute("delegator");

        boolean tryEntity = true;
        if(request.getAttribute(SiteDefs.ERROR_MESSAGE) != null)
            tryEntity = false;

        String donePage = request.getParameter("DONE_PAGE");
        if (donePage == null || donePage.length() <= 0)
            donePage = "viewprofile";
        pageContext.setAttribute(donePageAttr, donePage);

        String creditCardId = request.getParameter("creditCardId");
        if (request.getAttribute("creditCardId") != null)
            creditCardId = (String)request.getAttribute("creditCardId");
        if (creditCardId != null)
            pageContext.setAttribute(creditCardIdAttr, creditCardId);

        GenericValue creditCard = null;
        if (UtilValidate.isNotEmpty(creditCardId)) {
            try {
                creditCard = delegator.findByPrimaryKey("CreditCardInfo", UtilMisc.toMap("creditCardId", creditCardId));
            } catch (GenericEntityException e) {
                Debug.logWarning(e);
            }
        }
        if (creditCard != null)
            pageContext.setAttribute(creditCardAttr, creditCard);
        else
            tryEntity = false;


        String curContactMechId = UtilFormatOut.checkNull(tryEntity?creditCard.getString("contactMechId"):request.getParameter("contactMechId"));
        if (curContactMechId != null) {
            pageContext.setAttribute(curContactMechIdAttr, curContactMechId);
            
            Collection partyContactMechs = null;
            try {
                partyContactMechs = EntityUtil.filterByDate(delegator.findByAnd("PartyContactMech", UtilMisc.toMap("partyId", partyId, "contactMechId", curContactMechId)));
            } catch (GenericEntityException e) {
                Debug.logWarning(e);
            }
            GenericValue curPartyContactMech = EntityUtil.getFirst(partyContactMechs);

            GenericValue curContactMech = null;
            if (curPartyContactMech != null) {
                pageContext.setAttribute(curPartyContactMechAttr, curPartyContactMech);
                try {
                    curContactMech = curPartyContactMech.getRelatedOne("ContactMech");
                } catch (GenericEntityException e) {
                    Debug.logWarning(e);
                }

                Collection curPartyContactMechPurposes = null;
                try {
                    curPartyContactMechPurposes = EntityUtil.filterByDate(curPartyContactMech.getRelated("PartyContactMechPurpose"));
                } catch (GenericEntityException e) {
                    Debug.logWarning(e);
                }
                if (curPartyContactMechPurposes != null && curPartyContactMechPurposes.size() > 0) {
                    pageContext.setAttribute(curPartyContactMechPurposesAttr, curPartyContactMechPurposes);
                }
            }

            GenericValue curPostalAddress = null;
            if (curContactMech != null) {
                pageContext.setAttribute(curContactMechAttr, curContactMech);
                try {
                    curPostalAddress = curContactMech.getRelatedOne("PostalAddress");
                } catch (GenericEntityException e) {
                    Debug.logWarning(e);
                }
            }

            if (curPostalAddress != null) {
                pageContext.setAttribute(curPostalAddressAttr, curPostalAddress);
            }
        }

        pageContext.setAttribute(tryEntityAttr, new Boolean(tryEntity));
    }
    
    public static void getPartyPostalAddresses(PageContext pageContext, String partyId, String curContactMechId, String postalAddressInfosAttr) {
        GenericDelegator delegator = (GenericDelegator) pageContext.getRequest().getAttribute("delegator");
        Collection postalAddressInfos = new LinkedList();
        
        Iterator allPartyContactMechs = null;
        try {
            allPartyContactMechs = UtilMisc.toIterator(EntityUtil.filterByDate(delegator.findByAnd("PartyContactMech", UtilMisc.toMap("partyId", partyId))));
        } catch (GenericEntityException e) {
            Debug.logWarning(e);
        }
        while(allPartyContactMechs != null && allPartyContactMechs.hasNext()) {
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
                    Collection partyContactMechPurposes = EntityUtil.filterByDate(partyContactMech.getRelated("PartyContactMechPurpose"));
                    postalAddressInfo.put("partyContactMechPurposes", partyContactMechPurposes);
                } catch (GenericEntityException e) {
                    Debug.logWarning(e);
                }
            }
        }
        
        if (postalAddressInfos.size() > 0) {
            pageContext.setAttribute(postalAddressInfosAttr, postalAddressInfos);
        }
    }
}
