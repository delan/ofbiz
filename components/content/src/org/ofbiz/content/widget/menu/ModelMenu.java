/*
 * $Id$
 *
 * Copyright (c) 2003 The Open For Business Project - www.ofbiz.org
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
package org.ofbiz.content.widget.menu;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.ofbiz.base.util.BshUtil;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.base.util.UtilXml;
import org.ofbiz.base.util.string.FlexibleStringExpander;
import org.ofbiz.entity.GenericDelegator;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.content.content.EntityPermissionChecker;
import org.w3c.dom.Element;

import bsh.EvalError;
import bsh.Interpreter;

/**
 * Widget Library - Menu model class
 *
 * @author     <a href="mailto:jonesde@ofbiz.org">David E. Jones</a>
 * @author     <a href="mailto:byersa@automationgroups.com">Al Byers</a>
 * @version    $Rev$
 * @since      2.2
 */
public class ModelMenu {

    public static final String module = ModelMenu.class.getName();

    protected GenericDelegator delegator;
    protected LocalDispatcher dispatcher;

    protected String name;
    protected String type;
    protected String target;
    protected String title;
    protected String tooltip;
    protected String defaultEntityName;
    protected String defaultTitleStyle;
    protected String defaultWidgetStyle;
    protected String defaultTooltipStyle;
    protected String defaultSelectedStyle;
    protected String defaultMenuItemName;
    protected String currentMenuItemName;
    protected String defaultPermissionOperation;
    protected String defaultPermissionEntityAction;
    protected FlexibleStringExpander defaultAssociatedContentId;
    protected String defaultPermissionStatusId;
    protected String defaultPrivilegeEnumId;
    protected String orientation = "horizontal";
    protected String menuWidth;
    protected String defaultCellWidth;
    protected Boolean defaultHideIfSelected;
    protected String defaultDisabledTitleStyle;
    protected String selectedMenuItemContextFieldName;

    /** This List will contain one copy of each item for each item name in the order
     * they were encountered in the service, entity, or menu definition; item definitions
     * with constraints will also be in this list but may appear multiple times for the same
     * item name.
     *
     * When rendering the menu the order in this list should be following and it should not be
     * necessary to use the Map. The Map is used when loading the menu definition to keep the
     * list clean and implement the override features for item definitions.
     */
    protected List menuItemList = new LinkedList();

    /** This Map is keyed with the item name and has a ModelMenuItem for the value; items
     * with conditions will not be put in this Map so item definition overrides for items
     * with conditions is not possible.
     */
    protected Map menuItemMap = new HashMap();

    
   // ===== CONSTRUCTORS =====
    /** Default Constructor */
    public ModelMenu() {}

    /** XML Constructor */
    public ModelMenu(Element menuElement, GenericDelegator delegator, LocalDispatcher dispatcher) {
        this.delegator = delegator;
        this.dispatcher = dispatcher;

        // check if there is a parent menu to inherit from
        String parentResource = menuElement.getAttribute("extends-resource");
        String parentMenu = menuElement.getAttribute("extends");
        //TODO: Modify this to allow for extending a menu with the same name but different resource
        if (parentMenu.length() > 0 && !parentMenu.equals(menuElement.getAttribute("name"))) {
            ModelMenu parent = null;
            // check if we have a resource name (part of the string before the ?)
            if (parentResource.length() > 0) {
                try {
                    parent = MenuFactory.getMenuFromClass(parentResource, parentMenu, delegator, dispatcher);
                } catch (Exception e) {
                    Debug.logError(e, "Failed to load parent menu definition '" + parentMenu + "' at resource '" + parentResource + "'", module);
                }
            } else {
                // try to find a menu definition in the same file
                Element rootElement = menuElement.getOwnerDocument().getDocumentElement();
                List menuElements = UtilXml.childElementList(rootElement, "menu");
                //Uncomment below to add support for abstract menus
                //menuElements.addAll(UtilXml.childElementList(rootElement, "abstract-menu"));
                Iterator menuElementIter = menuElements.iterator();
                while (menuElementIter.hasNext()) {
                    Element menuElementEntry = (Element) menuElementIter.next();
                    if (menuElementEntry.getAttribute("name").equals(parentMenu)) {
                        parent = new ModelMenu(menuElementEntry, delegator, dispatcher);
                        break;
                    }
                }
                if (parent == null) {
                    Debug.logError("Failed to find parent menu defenition '" + parentMenu + "' in same document.", module);
                }
            }

            if (parent != null) {
                this.type = parent.type;
                this.target = parent.target;
                this.title = parent.title;
                this.tooltip = parent.tooltip;
                this.tooltip = parent.tooltip;
                this.defaultEntityName = parent.defaultEntityName;
                this.defaultTitleStyle = parent.defaultTitleStyle;
                this.defaultSelectedStyle = parent.defaultSelectedStyle;
                this.defaultWidgetStyle = parent.defaultWidgetStyle;
                this.defaultTooltipStyle = parent.defaultTooltipStyle;
                this.defaultMenuItemName = parent.defaultMenuItemName;
                this.menuItemList = parent.menuItemList;
                this.menuItemMap = parent.menuItemMap;
                this.defaultPermissionOperation = parent.defaultPermissionOperation;
                this.defaultPermissionEntityAction = parent.defaultPermissionEntityAction;
                this.defaultAssociatedContentId = parent.defaultAssociatedContentId;
                this.defaultPermissionStatusId = parent.defaultPermissionStatusId;
                this.defaultPrivilegeEnumId = parent.defaultPrivilegeEnumId;
                this.defaultHideIfSelected = parent.defaultHideIfSelected;
                this.orientation = parent.orientation;
                this.menuWidth = parent.menuWidth;
                this.defaultCellWidth = parent.defaultCellWidth;
                this.defaultDisabledTitleStyle = parent.defaultDisabledTitleStyle;
            }
        }

        this.name = menuElement.getAttribute("name");
        if (this.type == null || menuElement.hasAttribute("type"))
            this.type = menuElement.getAttribute("type");
        if (this.target == null || menuElement.hasAttribute("target"))
            this.target = menuElement.getAttribute("target");
        if (this.title == null || menuElement.hasAttribute("title"))
            this.title = menuElement.getAttribute("title");
        if (this.tooltip == null || menuElement.hasAttribute("tooltip"))
            this.tooltip = menuElement.getAttribute("tooltip");
        if (this.defaultEntityName == null || menuElement.hasAttribute("default-entity-name"))
            this.defaultEntityName = menuElement.getAttribute("default-entity-name");
        if (this.defaultTitleStyle == null || menuElement.hasAttribute("default-title-style"))
            this.defaultTitleStyle = menuElement.getAttribute("default-title-style");
        if (this.defaultSelectedStyle == null || menuElement.hasAttribute("default-selected-style"))
            this.defaultSelectedStyle = menuElement.getAttribute("default-selected-style");
        if (this.defaultWidgetStyle == null || menuElement.hasAttribute("default-widget-style"))
            this.defaultWidgetStyle = menuElement.getAttribute("default-widget-style");
        if (this.defaultTooltipStyle == null || menuElement.hasAttribute("default-tooltip-style"))
            this.defaultTooltipStyle = menuElement.getAttribute("default-tooltip-style");
        if (this.defaultMenuItemName == null || menuElement.hasAttribute("default-menu-item-name"))
            this.defaultMenuItemName = menuElement.getAttribute("default-menu-item-name");
        if (this.defaultPermissionOperation == null || menuElement.hasAttribute("default-permission-operation"))
            this.defaultPermissionOperation = menuElement.getAttribute("default-permission-operation");
        if (this.defaultPermissionEntityAction == null || menuElement.hasAttribute("default-permission-entity-action"))
            this.defaultPermissionEntityAction = menuElement.getAttribute("default-permission-entity-action");
        if (this.defaultPermissionStatusId == null || menuElement.hasAttribute("defaultPermissionStatusId"))
            this.defaultPermissionStatusId = menuElement.getAttribute("default-permission-status-id");
        if (this.defaultPrivilegeEnumId == null || menuElement.hasAttribute("defaultPrivilegeEnumId"))
            this.defaultPrivilegeEnumId = menuElement.getAttribute("default-privilege-enum-id");
        if (this.defaultAssociatedContentId == null || menuElement.hasAttribute("defaultAssociatedContentId"))
            this.setDefaultAssociatedContentId( menuElement.getAttribute("default-associated-content-id"));
        if (this.orientation == null || menuElement.hasAttribute("orientation"))
            this.orientation = menuElement.getAttribute("orientation");
        if (this.menuWidth == null || menuElement.hasAttribute("menu-width"))
            this.menuWidth = menuElement.getAttribute("menu-width");
        if (this.defaultCellWidth == null || menuElement.hasAttribute("default-cell-width"))
            this.defaultCellWidth = menuElement.getAttribute("default-cell-width");
        if (menuElement.hasAttribute("default-hide-if-selected")) {
            String val = menuElement.getAttribute("default-hide-if-selected");
                //Debug.logInfo("in ModelMenu, hideIfSelected, val:" + val, module);
            if (val != null && val.equalsIgnoreCase("true"))
                defaultHideIfSelected = new Boolean(true);
            else 
                defaultHideIfSelected = new Boolean(false);
        }
        if (this.defaultDisabledTitleStyle == null || menuElement.hasAttribute("default-disabled-title-style"))
            this.defaultDisabledTitleStyle = menuElement.getAttribute("default-disabled-title-style");
        if (this.selectedMenuItemContextFieldName == null || menuElement.hasAttribute("selected-menuitem-context-field-name"))
            this.selectedMenuItemContextFieldName = menuElement.getAttribute("selected-menuitem-context-field-name");


        // read in add item defs, add/override one by one using the menuItemList and menuItemMap
        List itemElements = UtilXml.childElementList(menuElement, "menu-item");
        Iterator itemElementIter = itemElements.iterator();
        while (itemElementIter.hasNext()) {
            Element itemElement = (Element) itemElementIter.next();
            ModelMenuItem modelMenuItem = new ModelMenuItem(itemElement, this);
            modelMenuItem = this.addUpdateMenuItem(modelMenuItem);
        }
    }
    /**
     * add/override modelMenuItem using the menuItemList and menuItemMap
     *
     * @return The same ModelMenuItem, or if merged with an existing item, the existing item.
     */
    public ModelMenuItem addUpdateMenuItem(ModelMenuItem modelMenuItem) {

            // not a conditional item, see if a named item exists in Map
            ModelMenuItem existingMenuItem = (ModelMenuItem) this.menuItemMap.get(modelMenuItem.getName());
            if (existingMenuItem != null) {
                // does exist, update the item by doing a merge/override
                existingMenuItem.mergeOverrideModelMenuItem(modelMenuItem);
                return existingMenuItem;
            } else {
                // does not exist, add to List and Map
                this.menuItemList.add(modelMenuItem);
                this.menuItemMap.put(modelMenuItem.getName(), modelMenuItem);
                return modelMenuItem;
            }
    }

    public ModelMenuItem getModelMenuItemByName(String name) {
            ModelMenuItem existingMenuItem = (ModelMenuItem) this.menuItemMap.get(name);
            return existingMenuItem;
    }

    public ModelMenuItem getModelMenuItemByContentId(String contentId, Map context) {

        ModelMenuItem existingMenuItem = null;
        if (UtilValidate.isEmpty(contentId))
            return existingMenuItem;
        Iterator iter = menuItemList.iterator();
        while (iter.hasNext()) {
            ModelMenuItem mi = (ModelMenuItem) iter.next();
            String assocContentId = mi.getAssociatedContentId(context);
            if (contentId.equals(assocContentId)) {
                existingMenuItem = mi;
                break;
            }
        }
            return existingMenuItem;
    }

    /**
     * Renders this menu to a String, i.e. in a text format, as defined with the
     * MenuStringRenderer implementation.
     *
     * @param buffer The StringBuffer that the menu text will be written to
     * @param context Map containing the menu context; the following are
     *   reserved words in this context: parameters (Map), isError (Boolean),
     *   itemIndex (Integer, for lists only, otherwise null), bshInterpreter,
     *   menuName (String, optional alternate name for menu, defaults to the
     *   value of the name attribute)
     * @param menuStringRenderer An implementation of the MenuStringRenderer
     *   interface that is responsible for the actual text generation for
     *   different menu elements; implementing you own makes it possible to
     *   use the same menu definitions for many types of menu UIs
     */
    public void renderMenuString(StringBuffer buffer, Map context, MenuStringRenderer menuStringRenderer) {
    	
    	boolean passed = true;

            Debug.logInfo("in ModelMenu, name:" + this.getName(), module);
        if (passed) {
	        if ("simple".equals(this.type)) {
	            this.renderSimpleMenuString(buffer, context, menuStringRenderer);
	        } else {
	            throw new IllegalArgumentException("The type " + this.getType() + " is not supported for menu with name " + this.getName());
	        }
        }
            //Debug.logInfo("in ModelMenu, buffer:" + buffer.toString(), module);
    }

    public void renderSimpleMenuString(StringBuffer buffer, Map context, MenuStringRenderer menuStringRenderer) {
        Iterator menuItemIter = null;

        Set alreadyRendered = new TreeSet();

        // render menu open
        menuStringRenderer.renderMenuOpen(buffer, context, this);

        // render formatting wrapper open
        menuStringRenderer.renderFormatSimpleWrapperOpen(buffer, context, this);

            //Debug.logInfo("in ModelMenu, menuItemList:" + menuItemList, module);
        // render each menuItem row, except hidden & ignored rows
        menuStringRenderer.renderFormatSimpleWrapperRows(buffer, context, this);

        // render formatting wrapper close
        menuStringRenderer.renderFormatSimpleWrapperClose(buffer, context, this);

        // render menu close
        menuStringRenderer.renderMenuClose(buffer, context, this);
    }


    public LocalDispatcher getDispacher() {
        return this.dispatcher;
    }

    public GenericDelegator getDelegator() {
        return this.delegator;
    }

    /**
     * @return
     */
    public String getDefaultEntityName() {
        return this.defaultEntityName;
    }


    /**
     * @return
     */
    public String getDefaultTitleStyle() {
        return this.defaultTitleStyle;
    }

    /**
     * @return
     */
    public String getDefaultDisabledTitleStyle() {
        return this.defaultDisabledTitleStyle;
    }

    /**
     * @return
     */
    public String getDefaultSelectedStyle() {
        return this.defaultSelectedStyle;
    }

    /**
     * @return
     */
    public String getDefaultWidgetStyle() {
        return this.defaultWidgetStyle;
    }

    /**
     * @return
     */
    public String getDefaultTooltipStyle() {
        return this.defaultTooltipStyle;
    }

    /**
     * @return
     */
    public String getDefaultMenuItemName() {
        return this.defaultMenuItemName;
    }

    /**
     * @return
     */
    public String getSelectedMenuItemContextFieldName() {
        return this.selectedMenuItemContextFieldName;
    }

    /**
     * @return
     */
    public String getCurrentMenuItemName() {
        if (UtilValidate.isNotEmpty(this.currentMenuItemName))
            return this.currentMenuItemName;
        else
            return this.defaultMenuItemName;
    }


    /**
     * @return
     */
    public String getName() {
        return this.name;
    }

    public String getCurrentMenuName(Map context) {
        return this.name;
    }


    /**
     * @return
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * @return
     */
    public String getTooltip() {
        return this.tooltip;
    }

    /**
     * @return
     */
    public String getType() {
        return this.type;
    }

    public Interpreter getBshInterpreter(Map context) throws EvalError {
        Interpreter bsh = (Interpreter) context.get("bshInterpreter");
        if (bsh == null) {
            bsh = BshUtil.makeInterpreter(context);
            context.put("bshInterpreter", bsh);
        }
        return bsh;
    }

    /**
     * @param string
     */
    public void setDefaultEntityName(String string) {
        this.defaultEntityName = string;
    }


    /**
     * @param string
     */
    public void setDefaultTitleStyle(String string) {
        this.defaultTitleStyle = string;
    }

    /**
     * @param string
     */
    public void setDefaultSelectedStyle(String string) {
        this.defaultSelectedStyle = string;
    }

    /**
     * @param string
     */
    public void setDefaultWidgetStyle(String string) {
        this.defaultWidgetStyle = string;
    }

    /**
     * @param string
     */
    public void setDefaultTooltipStyle(String string) {
        this.defaultTooltipStyle = string;
    }

    /**
     * @param string
     */
    public void setDefaultMenuItemName(String string) {
        this.defaultMenuItemName = string;
    }

    /**
     * @param string
     */
    public void setCurrentMenuItemName(String string) {
        this.currentMenuItemName = string;
    }


    /**
     * @param string
     */
    public void setName(String string) {
        this.name = string;
    }

    /**
     * @param string
     */
    public void setTarget(String string) {
        this.target = string;
    }

    /**
     * @param string
     */
    public void setTitle(String string) {
        this.title = string;
    }

    /**
     * @param string
     */
    public void setTooltip(String string) {
        this.tooltip = string;
    }

    /**
     * @param string
     */
    public void setType(String string) {
        this.type = string;
    }

    /**
     * @param string
     */
    public void setDefaultAssociatedContentId(String string) {
        this.defaultAssociatedContentId = new FlexibleStringExpander(string);
    }

    /**
     * @return
     */
    public String getDefaultAssociatedContentId(Map context) {
        return defaultAssociatedContentId.expandString(context);
    }

    /**
     * @param string
     */
    public void setDefaultPermissionOperation(String string) {
        this.defaultPermissionOperation = string;
    }

    /**
     * @return
     */
    public String getDefaultPermissionStatusId() {
        return this.defaultPermissionStatusId;
    }

    /**
     * @param string
     */
    public void setDefaultPermissionStatusId(String string) {
        this.defaultPermissionStatusId = string;
    }

    /**
     * @param string
     */
    public void setDefaultPrivilegeEnumId(String string) {
        this.defaultPrivilegeEnumId = string;
    }

    /**
     * @return
     */
    public String getDefaultPrivilegeEnumId() {
        return this.defaultPrivilegeEnumId;
    }

    /**
     * @param string
     */
    public void setOrientation(String string) {
        this.orientation = string;
    }

    /**
     * @return
     */
    public String getOrientation() {
        return this.orientation;
    }

    /**
     * @param string
     */
    public void setMenuWidth(String string) {
        this.menuWidth = string;
    }

    /**
     * @return
     */
    public String getMenuWidth() {
        return this.menuWidth;
    }

    /**
     * @param string
     */
    public void setDefaultCellWidth(String string) {
        this.defaultCellWidth = string;
    }

    /**
     * @return
     */
    public String getDefaultCellWidth() {
        return this.defaultCellWidth;
    }

    /**
     * @return
     */
    public String getDefaultPermissionOperation() {
        return this.defaultPermissionOperation;
    }

    /**
     * @param string
     */
    public void setDefaultPermissionEntityAction(String string) {
        this.defaultPermissionEntityAction = string;
    }

    /**
     * @return
     */
    public String getDefaultPermissionEntityAction() {
        return this.defaultPermissionEntityAction;
    }

    /**
     * @param boolean
     */
    public void setDefaultHideIfSelected(Boolean val) {
        this.defaultHideIfSelected = val;
    }

    /**
     * @return
     */
    public Boolean getDefaultHideIfSelected() {
        return this.defaultHideIfSelected;
    }

    public ModelMenuItem getCurrentMenuItem() {
        
        ModelMenuItem currentMenuItem = (ModelMenuItem)menuItemMap.get(this.currentMenuItemName);
        if (currentMenuItem == null) {
            currentMenuItem = (ModelMenuItem)menuItemMap.get(this.defaultMenuItemName);
            if (currentMenuItem == null && menuItemList.size() > 0) {
                currentMenuItem = (ModelMenuItem)menuItemList.get(0);
            }
        }
        return currentMenuItem;
    }

    public List getMenuItemList() {
        return menuItemList;
    }

    public void dump(StringBuffer buffer ) {
        buffer.append("ModelMenu:" 
            + "\n name=" + this.name
            + "\n type=" + this.type
            + "\n target=" + this.target
            + "\n title=" + this.title
            + "\n tooltip=" + this.tooltip
            + "\n defaultEntityName=" + this.defaultEntityName
            + "\n defaultTitleStyle=" + this.defaultTitleStyle
            + "\n defaultWidgetStyle=" + this.defaultWidgetStyle
            + "\n defaultTooltipStyle=" + this.defaultTooltipStyle
            + "\n defaultSelectedStyle=" + this.defaultSelectedStyle
            + "\n defaultMenuItemName=" + this.defaultMenuItemName
            + "\n currentMenuItemName=" + this.currentMenuItemName
            + "\n\n");
     
        Iterator iter = menuItemList.iterator();
        while (iter.hasNext()) {
            ModelMenuItem menuItem = (ModelMenuItem)iter.next();
            menuItem.dump(buffer);
        }
            
        return;
    }

}
