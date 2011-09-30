/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: TestTreePopupMenu.java,v 1.12 2008/12/04 12:44:34 kozlov Exp $
 */

package com.unitesk.testfusion.gui.tree.test;

import java.awt.event.MouseListener;

import javax.swing.JMenuItem;

import com.unitesk.testfusion.core.config.SectionConfig;
import com.unitesk.testfusion.core.config.SectionListConfig;
import com.unitesk.testfusion.gui.GUI;
import com.unitesk.testfusion.gui.action.AddSectionAction;
import com.unitesk.testfusion.gui.action.OptionsAction;
import com.unitesk.testfusion.gui.action.RemoveSectionAction;
import com.unitesk.testfusion.gui.action.RenameSectionAction;
import com.unitesk.testfusion.gui.action.SettingsAction;
import com.unitesk.testfusion.gui.tree.TreePopupMenu;

/**
 * @author <a href="mailto:kozlov@ispras.ru">Kirill Kozlov</a>
 */
public class TestTreePopupMenu extends TreePopupMenu implements MouseListener
{
	private static final long serialVersionUID = 0L;

    protected SectionListConfig config;
    protected TestTreeNode node;
    
    public final static String SHOW_PANEL     = "Show";
    public final static String OPTIONS        = "Options...";
    public final static String SETTINGS       = "Settings...";
    public final static String ADD_SECTION    = "Add Section";
    public final static String REMOVE_SECTION = "Remove";
    public final static String RENAME_SECTION = "Rename";
    
	protected JMenuItem menuItem0;
    protected JMenuItem menuItem1;
    protected JMenuItem menuItem2;
    protected JMenuItem menuItem3;
    protected JMenuItem menuItem4;
	
	protected final int ITEMS_COUNT;
	
	public TestTreePopupMenu(GUI frame, TestTreeNode node, boolean isRoot) 
	{
        super(frame);
        
        this.node = node;
        this.config = node.getConfig();
		this.itemsCount = 0;
        
        if (isRoot)
        {
            ITEMS_COUNT = 4;
            menuItems = new JMenuItem[ITEMS_COUNT];
            registerItem(SHOW_PANEL,  menuItem0, true);
            registerItem(OPTIONS,  menuItem1, false);
            registerItem(SETTINGS,  menuItem2, true);
            registerItem(ADD_SECTION,  menuItem3, false);
        }
        else if (((SectionConfig)config).isLeaf())
        {
            ITEMS_COUNT = 5;
            menuItems = new JMenuItem[ITEMS_COUNT];
            registerItem(SHOW_PANEL,  menuItem0, true);
            registerItem(OPTIONS,  menuItem1, true);
            registerItem(RENAME_SECTION,  menuItem2, true);
            registerItem(ADD_SECTION,  menuItem3, false);
            registerItem(REMOVE_SECTION,  menuItem4, false);
        }
        else
        {
            ITEMS_COUNT = 5;
            menuItems = new JMenuItem[ITEMS_COUNT];
            registerItem(SHOW_PANEL,  menuItem0, true);
            registerItem(OPTIONS,  menuItem1, true);
            registerItem(RENAME_SECTION,  menuItem2, true);
            registerItem(ADD_SECTION,  menuItem3, false);
            registerItem(REMOVE_SECTION,  menuItem4, false);
        }
        
		setActivationOfItems();
	}
    
    protected void executeAction(String action)
    {
        TestTree tree = frame.getTestTree();
        
        if(SHOW_PANEL == action)
            { tree.selectNode(node); }
        else if (OPTIONS == action)
            { showOptions(); }
        else if (SETTINGS == action)
            { showSettings(); }
        else if (ADD_SECTION == action)
            { addNewSection(); }
        else if (RENAME_SECTION == action)
            { renameSection(); }
        else if (REMOVE_SECTION == action)
            { removeSection(); }
    }
    
    protected void showSettings()
    {
        SettingsAction action = new SettingsAction(frame);
        action.execute();
    }
    
    protected void showOptions()
    {
        OptionsAction action = new OptionsAction(frame);
        action.execute(config);
    }
    
    protected void renameSection()
    {
        RenameSectionAction action = new RenameSectionAction(frame, node);
        action.execute();
    }
    
    protected void addNewSection()
    {
        AddSectionAction action = new AddSectionAction(frame, node);
        action.execute();
    }
    
    protected void removeSection()
    {
        RemoveSectionAction action = new RemoveSectionAction(frame, node);
        action.execute();
    }
}

