/**
 * GenericBoundaryInfo.java	Java 1.3.0 Fri Apr 27 15:06:28 EDT 2001
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
import org.ofbiz.wrappers.xml.xgen.ClassDecl;
import org.ofbiz.wrappers.xml.IClassDeclaration;

public class GenericBoundaryInfo implements IGenericBoundaryInfo
  {
  public Hashtable _Attributes = new Hashtable();
  
  public static IClassDeclaration getStaticDXMLInfo()
    {
    return ClassDecl.find( "org.ofbiz.designer.networkdesign.GenericBoundaryInfo" );
    }
  
  public IClassDeclaration getDXMLInfo()
    {
    return getStaticDXMLInfo();
    }

  // element Attributes
  
  public String getAttribute( String name )
    {
    String value = (String) _Attributes.get( name );

    if( value != null ) 
      return value;

    return null;
    }
  
  public Hashtable getAttributes()
    {
    Hashtable clone = (Hashtable) _Attributes.clone();

    return clone;
    }
  
  public void setAttribute( String name, String value )
    {
    _Attributes.put( name, value );
    }
  
  public String removeAttribute( String name )
    {
    return (String) _Attributes.remove( name );
    }
  
  public String getReceiverportAttribute()
    {
    return getAttribute( "receiverport" );
    }
  
  public void setReceiverportAttribute( String value )
    {
    setAttribute( "receiverport", value );
    }
  
  public String removeReceiverportAttribute()
    {
    return removeAttribute( "receiverport" );
    }
  
  public String getOptionstringAttribute()
    {
    return getAttribute( "optionstring" );
    }
  
  public void setOptionstringAttribute( String value )
    {
    setAttribute( "optionstring", value );
    }
  
  public String removeOptionstringAttribute()
    {
    return removeAttribute( "optionstring" );
    }
  
  public String getReceiverhostAttribute()
    {
    return getAttribute( "receiverhost" );
    }
  
  public void setReceiverhostAttribute( String value )
    {
    setAttribute( "receiverhost", value );
    }
  
  public String removeReceiverhostAttribute()
    {
    return removeAttribute( "receiverhost" );
    }
  }