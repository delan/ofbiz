/**
 * ISyncRealization.java	Java 1.3.0 Fri Apr 27 15:06:28 EDT 2001
 *
 * Copyright 1999 by ObjectSpace, Inc.,
 * 14850 Quorum Dr., Dallas, TX, 75240 U.S.A.
 * All rights reserved.
 * 
 * This software is the confidential and proprietary information
 * of ObjectSpace, Inc. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with ObjectSpace.
 */

package org.ofbiz.designer.networkdesign;

import java.util.Hashtable;

public interface ISyncRealization extends org.ofbiz.wrappers.xml.IDXMLInterface ,org.ofbiz.wrappers.xml.IAttributeContainer
  {

  // element Attributes
  public String getSyncTypeAttribute();
  public void setSyncTypeAttribute( String value );
  public String removeSyncTypeAttribute();
  public String getPartnerAttribute();
  public void setPartnerAttribute( String value );
  public String removePartnerAttribute();

  // element PCDATA
  public String getPCDATA();
  public void setPCDATA( String arg0 );
  }