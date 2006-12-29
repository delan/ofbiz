/*
 *
 * Copyright 2001-2006 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package org.ofbiz.party.content;

import org.ofbiz.content.content.ContentWrapper;
import org.ofbiz.content.content.ContentWorker;
import org.ofbiz.base.util.cache.UtilCache;
import org.ofbiz.base.util.*;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.GenericDelegator;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.entity.model.ModelUtil;
import org.ofbiz.entity.model.ModelEntity;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.io.Writer;
import java.io.IOException;
import java.io.StringWriter;

import javolution.util.FastList;
import javolution.util.FastMap;

/**
 * WorkEffortContentWrapper; gets work effort content for display
 */
public class PartyContentWrapper implements ContentWrapper {

    public static final String module = PartyContentWrapper.class.getName();
    public static final String CACHE_KEY_SEPARATOR = "::";

    public static UtilCache workEffortContentCache = new UtilCache("workeffort.content.rendered", true);

    protected GenericValue party;
    protected Locale locale;
    protected String mimeTypeId;

    public PartyContentWrapper(GenericValue party, Locale locale, String mimeTypeId) {
        this.party = party;
        this.locale = locale;
        this.mimeTypeId = mimeTypeId;
    }

    public PartyContentWrapper(GenericValue party, HttpServletRequest request) {
        this.party = party;
        this.locale = UtilHttp.getLocale(request);
        this.mimeTypeId = "text/html";
    }

    // interface implementation
    public String get(String contentTypeId) {
        return getPartyContentAsText(party, contentTypeId, locale, mimeTypeId, party.getDelegator());
    }

    public List getList(String contentTypeId) {
        try {
            return getPartyContentTextList(party, contentTypeId, locale, mimeTypeId, party.getDelegator());
        } catch (Exception e) {
            Debug.logError(e, module);
            return null;
        }
    }

    public String getContent(String contentId) {
        return getPartyContentAsText(party, contentId, null, locale, mimeTypeId, party.getDelegator());
    }

    // static methods
    public static String getPartyContentAsText(GenericValue party, String partyContentId, HttpServletRequest request) {
        return getPartyContentAsText(party, partyContentId, UtilHttp.getLocale(request), "text/html", party.getDelegator());
    }

    public static String getPartyContentAsText(GenericValue party, String partyContentId, Locale locale) {
        return getPartyContentAsText(party, partyContentId, locale, null, null);
    }

    public static String getPartyContentAsText(GenericValue party, String partyContentTypeId,
            Locale locale, String mimeTypeId, GenericDelegator delegator) {
        return getPartyContentAsText(party, null, partyContentTypeId, locale, mimeTypeId, delegator);
    }

    public static String getPartyContentAsText(GenericValue party, String contentId, String partyContentTypeId,
            Locale locale, String mimeTypeId, GenericDelegator delegator) {
        if (party == null) {
            return null;
        }

        String candidateFieldName = ModelUtil.dbNameToVarName(partyContentTypeId);
        String cacheKey;
        if (contentId != null) {
            cacheKey = contentId + CACHE_KEY_SEPARATOR + locale + CACHE_KEY_SEPARATOR + mimeTypeId +
                    CACHE_KEY_SEPARATOR + party.get("partyId");
        } else {
            cacheKey = partyContentTypeId + CACHE_KEY_SEPARATOR + locale + CACHE_KEY_SEPARATOR + mimeTypeId +
                    CACHE_KEY_SEPARATOR + party.get("partyId");
        }

        try {
            if (workEffortContentCache.get(cacheKey) != null) {
                return (String) workEffortContentCache.get(cacheKey);
            }

            Writer outWriter = new StringWriter();
            getPartyContentAsText(contentId, party.getString("partyId"), party, partyContentTypeId, locale, mimeTypeId, delegator, outWriter);

            String outString = outWriter.toString();
            if (outString.length() > 0) {
                if (workEffortContentCache != null) {
                    workEffortContentCache.put(cacheKey, outString);
                }
                return outString;
            } else {
                String candidateOut = party.getModelEntity().isField(candidateFieldName) ? party.getString(candidateFieldName): "";
                return candidateOut == null ? "" : candidateOut;
            }
        } catch (GeneralException e) {
            Debug.logError(e, "Error rendering PartyContent, inserting empty String", module);
            String candidateOut = party.getModelEntity().isField(candidateFieldName) ? party.getString(candidateFieldName): "";
            return candidateOut == null? "" : candidateOut;
        } catch (IOException e) {
            Debug.logError(e, "Error rendering PartyContent, inserting empty String", module);
            String candidateOut = party.getModelEntity().isField(candidateFieldName) ? party.getString(candidateFieldName): "";
            return candidateOut == null? "" : candidateOut;
        }
    }

    public static void getPartyContentAsText(String contentId, String partyId, GenericValue party, String partyContentTypeId, Locale locale, String mimeTypeId, GenericDelegator delegator, Writer outWriter) throws GeneralException, IOException {
        if (partyId == null && party != null) {
            partyId = party.getString("partyId");
        }

        if (delegator == null && party != null) {
            delegator = party.getDelegator();
        }

        if (UtilValidate.isEmpty(mimeTypeId)) {
            mimeTypeId = "text/html";
        }

        if (delegator == null) {
            throw new GeneralRuntimeException("Unable to find a delegator to use!");
        }

        if (partyContentTypeId != null) {
            String candidateFieldName = ModelUtil.dbNameToVarName(partyContentTypeId);

            // first check for a person field
            ModelEntity partyPersonModel = delegator.getModelEntity("PartyAndPerson");
            if (partyPersonModel != null && partyPersonModel.isField(candidateFieldName)) {
                if (party == null) {
                    party = delegator.findByPrimaryKeyCache("PartyAndPerson", UtilMisc.toMap("partyId", partyId));
                }
                if (party != null) {
                    String candidateValue = party.getString(candidateFieldName);
                    if (UtilValidate.isNotEmpty(candidateValue)) {
                        outWriter.write(candidateValue);
                        return;
                    }
                }
            }

            // next check for group field
            ModelEntity partyGroupModel = delegator.getModelEntity("PartyAndGroup");
            if (partyGroupModel != null && partyGroupModel.isField(candidateFieldName)) {
                if (party == null) {
                    party = delegator.findByPrimaryKeyCache("PartyAndGroup", UtilMisc.toMap("partyId", partyId));
                }
                if (party != null) {
                    String candidateValue = party.getString(candidateFieldName);
                    if (UtilValidate.isNotEmpty(candidateValue)) {
                        outWriter.write(candidateValue);
                        return;
                    }
                }
            }
        }

        // otherwise a content field
        GenericValue partyContent;
        if (contentId != null) {
            partyContent = delegator.findByPrimaryKeyCache("PartyContent", UtilMisc.toMap("partyId", partyId, "contentId", contentId));
        } else {
            List partyContentList = delegator.findByAndCache("PartyContent", UtilMisc.toMap("partyId", partyId, "contentPurposeEnumId", partyContentTypeId), UtilMisc.toList("-fromDate"));
            partyContentList = EntityUtil.filterByDate(partyContentList);
            partyContent = EntityUtil.getFirst(partyContentList);
        }
        if (partyContent != null) {
            // when rendering the product content, always include the Product and ProductContent records that this comes from
            Map inContext = FastMap.newInstance();
            inContext.put("party", party);
            inContext.put("partyContent", partyContent);
            ContentWorker.renderContentAsText(delegator, partyContent.getString("contentId"), outWriter, inContext, null, locale, mimeTypeId);
        }
    }

    public static List getPartyContentTextList(GenericValue party, String partyContentTypeId, Locale locale, String mimeTypeId, GenericDelegator delegator) throws GeneralException, IOException {
        List partyContentList = delegator.findByAndCache("PartyContent", UtilMisc.toMap("partyId", party.getString("partyId"), "contentPurposeEnumId", partyContentTypeId), UtilMisc.toList("-fromDate"));
        partyContentList = EntityUtil.filterByDate(partyContentList);

        List contentList = FastList.newInstance();
        if (partyContentList != null) {
            Iterator i = partyContentList.iterator();
            while (i.hasNext()) {
                GenericValue partyContent = (GenericValue) i.next();
                StringWriter outWriter = new StringWriter();
                Map inContext = FastMap.newInstance();
                inContext.put("party", party);
                inContext.put("partyContent", partyContent);
                ContentWorker.renderContentAsText(delegator, partyContent.getString("contentId"), outWriter, inContext, null, locale, mimeTypeId);
                contentList.add(outWriter.toString());
            }
        }

        return contentList;
    }

    public static PartyContentWrapper makePartyContentWrapper(GenericValue party, HttpServletRequest request) {
        return new PartyContentWrapper(party, request);
    }
}
