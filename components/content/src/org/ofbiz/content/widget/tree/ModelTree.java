/*
 * $Id: ModelTree.java,v 1.5 2004/08/09 23:52:22 jonesde Exp $
 *
 * Copyright (c) 2004 The Open For Business Project - www.ofbiz.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT
 * OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR
 * THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.ofbiz.content.widget.tree;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilFormatOut;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.base.util.UtilXml;
import org.ofbiz.base.util.collections.MapStack;
import org.ofbiz.base.util.string.FlexibleStringExpander;
import org.ofbiz.content.widget.screen.ModelScreen;
import org.ofbiz.content.widget.screen.ScreenFactory;
import org.ofbiz.content.widget.screen.ScreenStringRenderer;
import org.ofbiz.entity.GenericDelegator;
import org.ofbiz.entity.GenericPK;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.service.LocalDispatcher;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

//import com.clarkware.profiler.Profiler;

/**
 * Widget Library - Tree model class
 *
 * @author     <a href="mailto:byersa@automationgroups.com">Al Byers</a>
 * @version    $Revision: 1.5 $
 * @since      3.1
 */
public class ModelTree {

    public static final String module = ModelTree.class.getName();

    protected String name;
    protected String rootNodeName;
    protected String renderStyle;
    protected List nodeList = new ArrayList();
    protected Map nodeMap = new HashMap();
    protected GenericDelegator delegator;
    protected LocalDispatcher dispatcher;
    
// ===== CONSTRUCTORS =====
    /** Default Constructor */

    /** XML Constructor */
    public ModelTree(Element treeElement, GenericDelegator delegator, LocalDispatcher dispatcher) {

        this.name = treeElement.getAttribute("name");
        this.rootNodeName = treeElement.getAttribute("root-node-name");
        this.renderStyle = UtilFormatOut.checkEmpty(treeElement.getAttribute("render-style"), "simple");
        this.delegator = delegator;
        this.dispatcher = dispatcher;

        List nodeElements = UtilXml.childElementList(treeElement, "node");
        Iterator nodeElementIter = nodeElements.iterator();
        while (nodeElementIter.hasNext()) {
            Element nodeElementEntry = (Element) nodeElementIter.next();
            ModelNode node = new ModelNode(nodeElementEntry, this);
            String nodeName = node.getName();
            nodeList.add(node);
            nodeMap.put(nodeName,node);
        }
    
        if (nodeList.size() == 0) {
            throw new IllegalArgumentException("No node elements found for the tree definition with name: " + this.name);
        }

    }
    
    public String getName() {
        return name;
    }

    public String getRootNodeName() {
        return rootNodeName;
    }


    /**
     * Renders this tree to a String, i.e. in a text format, as defined with the
     * TreeStringRenderer implementation.
     *
     * @param writer The Writer that the tree text will be written to
     * @param context Map containing the tree context; the following are
     *   reserved words in this context: parameters (Map), isError (Boolean),
     *   itemIndex (Integer, for lists only, otherwise null), bshInterpreter,
     *   treeName (String, optional alternate name for tree, defaults to the
     *   value of the name attribute)
     * @param treeStringRenderer An implementation of the TreeStringRenderer
     *   interface that is responsible for the actual text generation for
     *   different tree elements; implementing your own makes it possible to
     *   use the same tree definitions for many types of tree UIs
     */
    public void renderTreeString(StringBuffer buf, Map context, TreeStringRenderer treeStringRenderer) {
        ModelNode node = (ModelNode)nodeMap.get(rootNodeName);
        StringWriter writer = new StringWriter();
        try {
            node.renderNodeString(writer, context, treeStringRenderer, 0, true);
            buf.append(writer.toString());
        } catch (IOException e2) {
                String errMsg = "Error rendering included label with name [" + name + "] : " + e2.toString();
                Debug.logError(e2, errMsg, module);
                throw new RuntimeException(errMsg);
        }
//        try {
//            FileOutputStream fw = new FileOutputStream(new File("/usr/local/agi/ofbiz/hot-deploy/ofbizdoc/misc/profile.data"));
//            Profiler.print(fw);
//            fw.close();
//        } catch (IOException e) {
//           Debug.logError("[PROFILER] " + e.getMessage(),"");
//        }


    }

    public LocalDispatcher getDispatcher() {
        return this.dispatcher;
    }

    public GenericDelegator getDelegator() {
        return this.delegator;
    }

    public String getRenderStyle() {
        return this.renderStyle;
    }
    

    public static class ModelNode {

        protected String screenName;
        protected String screenLocation;
        protected Label label;
        protected Link link;
        protected Image image;
        protected List subNodeList = new ArrayList();
        protected List actions = new ArrayList();
        protected String name;
        protected ModelTree modelTree;
        protected List subNodeValues = new ArrayList();
        protected String expandCollapseStyle;

        public ModelNode() {}

        public ModelNode(Element nodeElement, ModelTree modelTree) {

            this.modelTree = modelTree;
            this.name = nodeElement.getAttribute("name");
            this.expandCollapseStyle = nodeElement.getAttribute("expand-collapse-style");
    
            Element actionElement = UtilXml.firstChildElement(nodeElement, "entity-one");
            if (actionElement != null) {
               actions.add(new ModelTreeAction.EntityOne(modelTree, actionElement));
            }
        
            actionElement = UtilXml.firstChildElement(nodeElement, "service");
            if (actionElement != null) {
                actions.add(new ModelTreeAction.Service(modelTree, actionElement));
            }
        
            Element screenElement = UtilXml.firstChildElement(nodeElement, "screen");
            if (screenElement != null) {
                this.screenName =  screenElement.getAttribute("screen-name");
                this.screenLocation =  screenElement.getAttribute("screen-location");
            }
            
            Element labelElement = UtilXml.firstChildElement(nodeElement, "label");
            if (labelElement != null) {
                this.label = new Label(labelElement);
            }
    
            Element linkElement = UtilXml.firstChildElement(nodeElement, "link");
            if (linkElement != null) {
                this.link = new Link(linkElement);
            }
            
            Element imageElement = UtilXml.firstChildElement(nodeElement, "image");
            if (imageElement != null) {
                this.image = new Image(imageElement);
            }
    
            if (screenElement == null && labelElement == null && linkElement == null) {
                throw new IllegalArgumentException("Neither 'screen' nor 'label' nor 'link' found for the node definition with name: " + this.name);
            }

            List subNodeElements = UtilXml.childElementList(nodeElement, "sub-node");
            Iterator subNodeElementIter = subNodeElements.iterator();
            while (subNodeElementIter.hasNext()) {
                Element subNodeElementEntry = (Element) subNodeElementIter.next();
                ModelSubNode subNode = new ModelSubNode(subNodeElementEntry, this);
                subNodeList.add(subNode);
            }
    
            
        }
    
        public void renderNodeString(Writer writer, Map context, TreeStringRenderer treeStringRenderer, int depth, boolean isLast) throws IOException {

            subNodeValues = new ArrayList();
            if (Debug.infoOn()) Debug.logInfo(" renderNodeString, contentId:" + context.get("contentId"), module);
            context.put("processChildren", new Boolean(true));
            ModelTreeAction.runSubActions(this.actions, context);
            treeStringRenderer.renderNodeBegin( writer, context, this, depth, isLast);
                    //if (Debug.infoOn()) Debug.logInfo(" context:" + context.entrySet(), module);
         
            try {
                if ( screenName != null && screenLocation != null) {
                    ScreenStringRenderer screenStringRenderer = treeStringRenderer.getScreenStringRenderer(context);
                    ModelScreen modelScreen = ScreenFactory.getScreenFromLocation(screenLocation, screenName);
                    modelScreen.renderScreenString(writer, context, screenStringRenderer);

                }
                if ( label != null) {
                    label.renderLabelString(writer, context, treeStringRenderer);
                }
                if ( link != null) {
                    link.renderLinkString(writer, context, treeStringRenderer);
                }
                Boolean processChildren = (Boolean)context.get("processChildren");
                    if (Debug.infoOn()) Debug.logInfo(" processChildren:" + processChildren, module);
                if (processChildren.booleanValue()) {
                    List vals = getChildren(context);
                    Iterator nodeIter = vals.iterator();
                    while (nodeIter.hasNext()) {
                        Object [] arr = (Object [])nodeIter.next();
                        ModelNode node = (ModelNode)arr[0];
                        GenericValue val = (GenericValue)arr[1];
                        GenericPK pk = val.getPrimaryKey();
                        if (Debug.infoOn()) Debug.logInfo(" pk:" + pk, module);
                        Map newContext = ((MapStack) context).standAloneChildStack();
                        newContext.putAll(pk);
                        if (Debug.infoOn()) Debug.logInfo(" newContext.contentId:" + newContext.get("contentId"), module);
                        boolean lastNode = !nodeIter.hasNext(); 
                        node.renderNodeString(writer, newContext, treeStringRenderer, depth + 1, lastNode);
                    }
                }
            } catch(SAXException e) {
                String errMsg = "Error rendering included label with name [" + name + "] : " + e.toString();
                Debug.logError(e, errMsg, module);
                throw new RuntimeException(errMsg);
            } catch(ParserConfigurationException e3) {
                String errMsg = "Error rendering included label with name [" + name + "] : " + e3.toString();
                Debug.logError(e3, errMsg, module);
                throw new RuntimeException(errMsg);
            } catch(IOException e2) {
                String errMsg = "Error rendering included label with name [" + name + "] : " + e2.toString();
                Debug.logError(e2, errMsg, module);
                throw new RuntimeException(errMsg);
            }


            // TODO: the item below needs to be moved to a TreeStringRenderer class, shouldn't be in the TreeStringRenderer
            treeStringRenderer.renderNodeEnd(writer, context,  this);
        }

        public boolean hasChildren(Map context) {

             boolean hasChildren = false;
                 if (Debug.infoOn()) Debug.logInfo(" subNodeList:" + subNodeList, module);
             Iterator nodeIter = subNodeList.iterator();
             while (nodeIter.hasNext()) {
                 ModelSubNode subNode = (ModelSubNode)nodeIter.next();
                 String nodeName = subNode.getNodeName();
                 ModelNode node = (ModelNode)modelTree.nodeMap.get(nodeName);
                 List subNodeActions = subNode.getActions();
                 if (Debug.infoOn()) Debug.logInfo(" context.currentValue:" + context.get("currentValue"), module);
                 ModelTreeAction.runSubActions(subNodeActions, context);
                 List dataFound = (List)context.get("dataFound");
                 if (Debug.infoOn()) Debug.logInfo(" dataFound:" + dataFound, module);
                 Iterator dataIter =  dataFound.iterator();
                 while (dataIter != null && dataIter.hasNext()) {
                     GenericValue val = (GenericValue)dataIter.next();
                     Object [] arr = {node,val};
                     subNodeValues.add(arr);
                     hasChildren = true;
                 }
                 
             }
             return hasChildren;
        }

        public List getChildren(Map context) {
      
            return new ArrayList(subNodeValues);
        }

        public String getName() {
            return name;
        }

        public String getExpandCollapseStyle() {
            return expandCollapseStyle;
        }
    
        public static class ModelSubNode {
    
            protected ModelNode rootNode;
            protected ModelNode subNode;
            protected String nodeName;
            protected List actions = new ArrayList();
            protected List outFieldMaps;
    
            public ModelSubNode() {}
    
            public ModelSubNode(Element nodeElement, ModelNode modelNode) {
    
                this.rootNode = modelNode;
                this.nodeName = nodeElement.getAttribute("node-name");
        
                Element actionElement = UtilXml.firstChildElement(nodeElement, "entity-and");
                if (actionElement != null) {
                   actions.add(new ModelTreeAction.EntityAnd(modelNode.modelTree, actionElement));
                }
            
                actionElement = UtilXml.firstChildElement(nodeElement, "service");
                if (actionElement != null) {
                    actions.add(new ModelTreeAction.Service(modelNode.modelTree, actionElement));
                }
        
            }
        
            public String getNodeName() {
                return nodeName;
            }
    
            public List getActions() {
                return actions;
            }
    
        }
    
        public static class Label {
            protected FlexibleStringExpander textExdr;
            
            protected FlexibleStringExpander idExdr;
            protected FlexibleStringExpander styleExdr;
            
            public Label( Element labelElement) {
    
                // put the text attribute first, then the pcdata under the element, if both are there of course
                String textAttr = UtilFormatOut.checkNull(labelElement.getAttribute("text"));
                String pcdata = UtilFormatOut.checkNull(UtilXml.elementValue(labelElement));
                this.textExdr = new FlexibleStringExpander(textAttr + pcdata);
    
                this.idExdr = new FlexibleStringExpander(labelElement.getAttribute("id"));
                this.styleExdr = new FlexibleStringExpander(labelElement.getAttribute("style"));
            }
    
            public void renderLabelString(Writer writer, Map context, TreeStringRenderer treeStringRenderer) {
                try {
                    treeStringRenderer.renderLabel(writer, context, this);
                } catch (IOException e) {
                    String errMsg = "Error rendering label with id [" + getId(context) + "]: " + e.toString();
                    Debug.logError(e, errMsg, module);
                    throw new RuntimeException(errMsg);
                }
            }
            
            public String getText(Map context) {
                return this.textExdr.expandString(context);
            }
            
            public String getId(Map context) {
                return this.idExdr.expandString(context);
            }
            
            public String getStyle(Map context) {
                return this.styleExdr.expandString(context);
            }
        }

    
        public static class Link {
            protected FlexibleStringExpander textExdr;
            protected FlexibleStringExpander idExdr;
            protected FlexibleStringExpander styleExdr;
            protected FlexibleStringExpander targetExdr;
            protected FlexibleStringExpander targetWindowExdr;
            protected FlexibleStringExpander prefixExdr;
            protected Image image;
            protected String urlMode = "ofbiz";
            protected boolean fullPath = false;
            protected boolean secure = false;
            protected boolean encode = false;
            
            public Link() {
                setText(null);
                setId(null);
                setStyle(null);
                setTarget(null);
                setTargetWindow(null);
                setPrefix(null);
                setUrlMode(null);
                setFullPath(null);
                setSecure(null);
                setEncode(null);
            }

            public Link( Element linkElement) {
    
                setText(linkElement.getAttribute("text"));
                setId(linkElement.getAttribute("id"));
                setStyle(linkElement.getAttribute("style"));
                setTarget(linkElement.getAttribute("target"));
                setTargetWindow(linkElement.getAttribute("target-window"));
                setPrefix(linkElement.getAttribute("prefix"));
                setUrlMode(linkElement.getAttribute("url-mode"));
                setFullPath(linkElement.getAttribute("full-path"));
                setSecure(linkElement.getAttribute("secure"));
                setEncode(linkElement.getAttribute("encode"));
                Element imageElement = UtilXml.firstChildElement(linkElement, "image");
                if (imageElement != null) {
                    this.image = new Image(imageElement);
                }
    
            }
    
            public void renderLinkString(Writer writer, Map context, TreeStringRenderer treeStringRenderer) {
                try {
                    treeStringRenderer.renderLink(writer, context, this);
                } catch (IOException e) {
                    String errMsg = "Error rendering link with id [" + getId(context) + "]: " + e.toString();
                    Debug.logError(e, errMsg, module);
                    throw new RuntimeException(errMsg);
                }
            }
            
            public String getText(Map context) {
                return this.textExdr.expandString(context);
            }
            
            public String getId(Map context) {
                return this.idExdr.expandString(context);
            }
            
            public String getStyle(Map context) {
                return this.styleExdr.expandString(context);
            }
            
            public String getTarget(Map context) {
                return this.targetExdr.expandString(context);
            }
            
            public String getTargetWindow(Map context) {
                return this.targetWindowExdr.expandString(context);
            }
            
            public String getUrlMode() {
                return this.urlMode;
            }
            
            public String getPrefix(Map context) {
                return this.prefixExdr.expandString(context);
            }
            
            public boolean getFullPath() {
                return this.fullPath;
            }
            
            public boolean getSecure() {
                return this.secure;
            }
            
            public boolean getEncode() {
                return this.encode;
            }
            
            public Image getImage() {
                return this.image;
            }

            public void setText( String val ) {
                String textAttr = UtilFormatOut.checkNull(val);
                this.textExdr = new FlexibleStringExpander(textAttr);
            }
            public void setId( String val ) {
                this.idExdr = new FlexibleStringExpander(val);
            }
            public void setStyle( String val ) {
                this.styleExdr = new FlexibleStringExpander(val);
            }
            public void setTarget( String val ) {
                this.targetExdr = new FlexibleStringExpander(val);
            }
            public void setTargetWindow( String val ) {
                this.targetWindowExdr = new FlexibleStringExpander(val);
            }
            public void setPrefix( String val ) {
                this.prefixExdr = new FlexibleStringExpander(val);
            }
            public void setUrlMode( String val ) {
                if (UtilValidate.isEmpty(val))
                    this.urlMode = "ofbiz";
                else
                    this.urlMode = val;
            }
            public void setFullPath( String val ) {
                String sFullPath = val;
                if (sFullPath != null && sFullPath.equalsIgnoreCase("true"))
                    this.fullPath = true;
                else
                    this.fullPath = false;
            }

            public void setSecure( String val ) {
                String sSecure = val;
                if (sSecure != null && sSecure.equalsIgnoreCase("true"))
                    this.secure = true;
                else
                    this.secure = false;
            }

            public void setEncode( String val ) {
                String sEncode = val;
                if (sEncode != null && sEncode.equalsIgnoreCase("true"))
                    this.encode = true;
                else
                    this.encode = false;
            }
            public void setImage( Image img ) {
                this.image = img;
            }
                
        }

        public static class Image {

            protected FlexibleStringExpander srcExdr;
            protected FlexibleStringExpander idExdr;
            protected FlexibleStringExpander styleExdr;
            protected FlexibleStringExpander widthExdr;
            protected FlexibleStringExpander heightExdr;
            protected FlexibleStringExpander borderExdr;
            protected String urlMode;
            
            public Image() {

                setSrc(null);
                setId(null);
                setStyle(null);
                setWidth(null);
                setHeight(null);
                setBorder("0");
                setUrlMode(null);
            }

            public Image( Element imageElement) {
    
                setSrc(imageElement.getAttribute("src"));
                setId(imageElement.getAttribute("id"));
                setStyle(imageElement.getAttribute("style"));
                setWidth(imageElement.getAttribute("width"));
                setHeight(imageElement.getAttribute("height"));
                setBorder(UtilFormatOut.checkEmpty(imageElement.getAttribute("border"), "0"));
                setUrlMode(UtilFormatOut.checkEmpty(imageElement.getAttribute("url-mode"), "content"));
    
            }
    
            public void renderImageString(Writer writer, Map context, TreeStringRenderer treeStringRenderer) {
                try {
                    treeStringRenderer.renderImage(writer, context, this);
                } catch (IOException e) {
                    String errMsg = "Error rendering image with id [" + getId(context) + "]: " + e.toString();
                    Debug.logError(e, errMsg, module);
                    throw new RuntimeException(errMsg);
                }
            }
            
            public String getSrc(Map context) {
                return this.srcExdr.expandString(context);
            }
            
            public String getId(Map context) {
                return this.idExdr.expandString(context);
            }
            
            public String getStyle(Map context) {
                return this.styleExdr.expandString(context);
            }

            public String getWidth(Map context) {
                return this.widthExdr.expandString(context);
            }

            public String getHeight(Map context) {
                return this.heightExdr.expandString(context);
            }

            public String getBorder(Map context) {
                return this.borderExdr.expandString(context);
            }
            
            public String getUrlMode() {
                return this.urlMode;
            }
            
            public void setSrc( String val ) {
                String textAttr = UtilFormatOut.checkNull(val);
                this.srcExdr = new FlexibleStringExpander(textAttr);
            }
            public void setId( String val ) {
                this.idExdr = new FlexibleStringExpander(val);
            }
            public void setStyle( String val ) {
                this.styleExdr = new FlexibleStringExpander(val);
            }
            public void setWidth( String val ) {
                this.widthExdr = new FlexibleStringExpander(val);
            }
            public void setHeight( String val ) {
                this.heightExdr = new FlexibleStringExpander(val);
            }
            public void setBorder( String val ) {
                this.borderExdr = new FlexibleStringExpander(val);
            }
            public void setUrlMode( String val ) {
                if (UtilValidate.isEmpty(val))
                    this.urlMode = "content";
                else
                    this.urlMode = val;
            }
                
        }

    }
}

