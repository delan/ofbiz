package org.ofbiz.core.entity.model;

import javax.xml.parsers.DocumentBuilder; 
import javax.xml.parsers.DocumentBuilderFactory;  
import javax.xml.parsers.FactoryConfigurationError;  
import javax.xml.parsers.ParserConfigurationException;
 
import org.xml.sax.SAXException;  
import org.xml.sax.SAXParseException;  

import java.io.File;
import java.io.IOException;
import java.util.*;

import org.w3c.dom.Document;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.ofbiz.core.util.*;

/**
 * <p><b>Title:</b> Generic Entity - Entity Definition Reader
 * <p><b>Description:</b> Describes an Entity and acts as the base for all entity description data used in the code templates.
 * <p>Copyright (c) 2001 The Open For Business Project - www.ofbiz.org
 *
 * <p>Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * <p>The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * <p>THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT
 * OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR
 * THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * @author David E. Jones
 * @created May 15, 2001
 * @version 1.0
 */

public class ModelFieldTypeReader
{
  public static Map readers = new Hashtable();
  
  public Map fieldTypeCache = null;
  
  public int numEntities = 0;
  public int numFields = 0;
  public int numRelations = 0;

  public String modelName;
  public String fieldTypeFileName;
  public String entityFileName;

  public static ModelFieldTypeReader getModelFieldTypeReader(String helperName)
  {
    String tempModelName = UtilProperties.getPropertyValue("servers", helperName + ".field.type.reader");
    ModelFieldTypeReader reader = (ModelFieldTypeReader)readers.get(tempModelName);
    if(reader == null) //don't want to block here
    {
      synchronized(ModelFieldTypeReader.class) 
      { 
        //must check if null again as one of the blocked threads can still enter
        reader = (ModelFieldTypeReader)readers.get(tempModelName);
        if(reader == null)
        {
          reader = new ModelFieldTypeReader(tempModelName);
          readers.put(tempModelName, reader);
        }
      }
    }
    return reader;
  }
  
  public ModelFieldTypeReader(String modelName)
  {
    this.modelName = modelName;
    fieldTypeFileName = UtilProperties.getPropertyValue("servers", modelName + ".xml.field.type");
    
    //preload caches...
    getFieldTypeCache();
  }
  
  public Map getFieldTypeCache()
  {
    if(fieldTypeCache == null) //don't want to block here
    {
      synchronized(ModelFieldTypeReader.class) 
      { 
        //must check if null again as one of the blocked threads can still enter 
        if(fieldTypeCache == null) //now it's safe
        {
          fieldTypeCache = new HashMap();

          UtilTimer utilTimer = new UtilTimer();
          utilTimer.timerString("Before getDocument");
          Document document = getDocument(fieldTypeFileName);
          if(document == null) { fieldTypeCache = null; return null; }
          
          utilTimer.timerString("Before getDocumentElement");
          Element docElement = document.getDocumentElement();
          if(docElement == null) { fieldTypeCache = null; return null; }
          docElement.normalize();

          Node curChild = docElement.getFirstChild();
          
          int i=0;
          if(curChild != null)
          {
            utilTimer.timerString("Before start of field type loop");
            do
            {
              if(curChild.getNodeType() == Node.ELEMENT_NODE && "field-type-def".equals(curChild.getNodeName()))
              {
                i++;
                //utilTimer.timerString("Start loop -- " + i + " --");
                Element curFieldType = (Element)curChild;
                String fieldTypeName = checkNull(curFieldType.getAttribute("type"), "[No type name]");
                //utilTimer.timerString("  After fieldTypeName -- " + i + " --");
                ModelFieldType fieldType = createModelFieldType(curFieldType, docElement, null);
                //utilTimer.timerString("  After createModelFieldType -- " + i + " --");
                if(fieldType != null) 
                {
                  fieldTypeCache.put(fieldTypeName, fieldType);
                  //utilTimer.timerString("  After fieldTypeCache.put -- " + i + " --");
                  Debug.logInfo("-- getModelFieldType: #" + i + " Created fieldType: " + fieldTypeName);
                }
                else { Debug.logWarning("-- -- ENTITYGEN ERROR:getModelFieldType: Could not create fieldType for fieldTypeName: " + fieldTypeName); }
                
              }
            } while((curChild = curChild.getNextSibling()) != null);
          }
          else Debug.logWarning("No child nodes found.");
          utilTimer.timerString("FINISHED - Total Field Types: " + i + " FINISHED");
        }
      }
    }
    return fieldTypeCache;
  }
  
  /** Creates a Collection with all of the ModelFieldType names
   * @return A Collection of ModelFieldType names
   */  
  public Collection getFieldTypeNames()
  {
    Map ftc = getFieldTypeCache();
    return ftc.keySet();
  }

  /** Creates a Collection with all of the ModelFieldTypes
   * @return A Collection of ModelFieldTypes
   */  
  public Collection getFieldTypes()
  {
    Map ftc = getFieldTypeCache();
    return ftc.values();
  }

  /** Gets an FieldType object based on a definition from the specified XML FieldType descriptor file.
   * @param fieldTypeName The fieldTypeName of the FieldType definition to use.
   * @return An FieldType object describing the specified fieldType of the specified descriptor file.
   */    
  public ModelFieldType getModelFieldType(String fieldTypeName)
  {
    Map ftc = getFieldTypeCache();
    if(ftc != null) return (ModelFieldType)ftc.get(fieldTypeName);
    else return null;
  }

  ModelFieldType createModelFieldType(Element fieldTypeElement, Element docElement, UtilTimer utilTimer)
  {
    if(fieldTypeElement == null) return null;

    ModelFieldType field = new ModelFieldType();
    field.type = checkNull(fieldTypeElement.getAttribute("type"));
    field.javaType = checkNull(fieldTypeElement.getAttribute("java-type"));
    field.sqlType = checkNull(fieldTypeElement.getAttribute("sql-type"));

    NodeList validateList = fieldTypeElement.getElementsByTagName("validate");
    for(int i=0; i<validateList.getLength(); i++)
    {
      Element element = (Element)validateList.item(i);
      field.validators.add(checkNull(element.getAttribute("name")));
    }
    
    return field;
  }
  
  String childElementValue(Element element, String childElementName)
  {
    if(element == null || childElementName == null) return null;
    //get the value of the first element with the given name
    Node node = element.getFirstChild();
    if(node != null)
    {
      do
      {
        if(node.getNodeType() == Node.ELEMENT_NODE && childElementName.equals(node.getNodeName()))
        {
          Element childElement = (Element)node;
          return elementValue(childElement);
        }
      } while((node = node.getNextSibling()) != null);
    }
    return null;
  }  

  String elementValue(Element element)
  {
    Node textNode = element.getFirstChild();
    if(textNode == null) return null;
    //should be of type text
    return textNode.getNodeValue();
  }  

  String checkNull(String string)
  {
    if(string != null) return string;
    else return "";
  }
  
  String checkNull(String string1, String string2)
  {
    if(string1 != null) return string1;
    else if(string2 != null) return string2;
    else return "";
  }
  String checkNull(String string1, String string2, String string3)
  {
    if(string1 != null) return string1;
    else if(string2 != null) return string2;
    else if(string3 != null) return string3;
    else return "";
  }
  
  Document getDocument(String filename)
  {
    Document document = null;
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    //factory.setValidating(true);   
    //factory.setNamespaceAware(true);
    try 
    {
      //if(documentCache.containsKey(filename + ":document")) document = (Document)documentCache.get(filename + ":document");
      //else {
        DocumentBuilder builder = factory.newDocumentBuilder();
        document = builder.parse(new File(filename));
        //documentCache.put(filename + ":document", document);
      //}
    } 
    catch (SAXException sxe) 
    {
      // Error generated during parsing)
      Exception  x = sxe;
      if(sxe.getException() != null) x = sxe.getException();
      x.printStackTrace();
    } 
    catch(ParserConfigurationException pce) 
    {
      // Parser with specified options can't be built
      pce.printStackTrace();
    } 
    catch(IOException ioe) { ioe.printStackTrace(); }
    
    return document;
  }  
}
