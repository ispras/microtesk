/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: SectionTreePopupMenu.java,v 1.5 2008/08/29 13:37:50 kozlov Exp $
 */

package com.unitesk.testfusion.gui.tree.section;

import java.awt.event.*;

import javax.swing.JMenuItem;
import javax.swing.tree.TreePath;

import com.unitesk.testfusion.gui.GUI;
import com.unitesk.testfusion.gui.Panel;
import com.unitesk.testfusion.gui.action.TestSelectionConfigAction;
import com.unitesk.testfusion.gui.tree.TreePopupMenu;

/**
 * @author <a href="mailto:protsenko@ispras.ru">Alexander Protsenko</a>
 * @author <a href="mailto:kozlov@ispras.ru">Kirill Kozlov</a>
 */
public class SectionTreePopupMenu extends TreePopupMenu 
    implements MouseListener
{
    private static final long serialVersionUID = 0L;

    public final static String SHOW_PANEL    = "Show";
    public final static String SELECT_ITEM   = "Test";
    public final static String UNSELECT_ITEM = "Do Not Test";
    
    protected SectionTreeNode node;
    
    protected JMenuItem menuItem0;
    protected JMenuItem menuItem1;
    
    protected int ITEMS_COUNT = 2;
    
    public SectionTreePopupMenu(GUI frame, SectionTreeNode node) 
    {
        super(frame);
        this.node = node;
        
        menuItems = new JMenuItem[ITEMS_COUNT];
        
        registerItem(SHOW_PANEL,  menuItem0, true);
        if( node.isSelected())
            { registerItem(UNSELECT_ITEM, menuItem1, false); }
        else
            { registerItem(SELECT_ITEM, menuItem1, false); }
        
        setActivationOfItems();
    }
    
    protected void executeAction(String action)
    {
        if(SHOW_PANEL == action)
            { showPanel(); }
        else if((SELECT_ITEM == action) || (UNSELECT_ITEM == action))
            { checkNode(); } 
    }
    
    protected void showPanel()
    {
        Panel panel = frame.getPanel();
        
        panel.showPanel(node.getConfig());
        
        SectionTree tree = frame.getSectionTree();
        tree.setSelectionPath(new TreePath(node.getPath()));
    }
    
    protected void checkNode()
    {
        TestSelectionConfigAction action = 
            new TestSelectionConfigAction(frame, node);
        
        action.execute();
    }
}
