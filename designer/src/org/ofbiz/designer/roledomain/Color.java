/**
 * Color.java	Java 1.3.0 Fri Apr 27 15:06:26 EDT 2001
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

package org.ofbiz.designer.roledomain;

import org.ofbiz.wrappers.xml.xgen.ClassDecl;
import org.ofbiz.wrappers.xml.IClassDeclaration;

public class Color implements IColor
  {
  public String _Number1 = null;
  public String _Number2 = null;
  public String _Number3 = null;
  
  public static IClassDeclaration getStaticDXMLInfo()
    {
    return ClassDecl.find( "org.ofbiz.designer.roledomain.Color" );
    }
  
  public IClassDeclaration getDXMLInfo()
    {
    return getStaticDXMLInfo();
    }

  // element Number1
  
  public String getNumber1()
    {
    return _Number1 == null ? null : _Number1;
    }
  
  public void setNumber1( String arg0 )
    {
    _Number1 = arg0 == null ? null : new String( arg0 );
    }

  // element Number2
  
  public String getNumber2()
    {
    return _Number2 == null ? null : _Number2;
    }
  
  public void setNumber2( String arg0 )
    {
    _Number2 = arg0 == null ? null : new String( arg0 );
    }

  // element Number3
  
  public String getNumber3()
    {
    return _Number3 == null ? null : _Number3;
    }
  
  public void setNumber3( String arg0 )
    {
    _Number3 = arg0 == null ? null : new String( arg0 );
    }
  }