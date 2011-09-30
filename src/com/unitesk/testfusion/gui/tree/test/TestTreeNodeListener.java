/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: TestTreeNodeListener.java,v 1.4 2008/12/29 11:13:13 kozlov Exp $
 */

package com.unitesk.testfusion.gui.tree.test;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Component;

import javax.swing.tree.TreePath;

import com.unitesk.testfusion.gui.GUI;

/**
 * @author <a href="mailto:kozlov@ispras.ru">Kirill Kozlov</a>
 */
public class TestTreeNodeListener extends MouseAdapter
{
    protected int button;
    protected int clickCount;
    
    protected TreePath path;
    
    protected GUI frame;
    
    protected TestTreePopupMenu menu;
    
    protected boolean eventPopupMenu;
    protected int x;
    protected int y;
    protected Object component;

    public TestTreeNodeListener(final GUI frame) 
    {
        this.frame = frame;
    }
    
    public void processMouseClick()
    {
        if(path == null || !frame.getTestTree().isEnabled())
        { return; }
    
        if(button == MouseEvent.BUTTON1)
        {
            if(clickCount == 1)
            {
                processSingleClick_TreeLabel_LeftButton();
            }
            else
            {
                processDoubleClick_TreeLabel_LeftButton();
            }
        }
        else
        {
            if(clickCount == 1)
            {
                processSingleClick_TreeLabel_RightButton();
            }
            else
            {
                processDoubleClick_TreeLabel_RightButton();
            }
        }
    }

    protected void processSingleClick_TreeLabel_LeftButton()
    {
        selectNode();
    }
    
    protected void processDoubleClick_TreeLabel_LeftButton()
    {
        selectNode();
    }
    
    protected void processSingleClick_TreeLabel_RightButton()
    {
    	showPopupMenu();
    }
    
    protected void processDoubleClick_TreeLabel_RightButton() {}
        
    protected void showPopupMenu()
    {
        if((button == MouseEvent.BUTTON3) && eventPopupMenu)
        {    
            // Create popup menu
            TestTreeNode node = (TestTreeNode)path.getLastPathComponent();
            menu = new TestTreePopupMenu(frame, node, node.isRoot());
            
            menu.show((Component) component, x, y);
        }
    }
    
    protected void selectNode()
    {
        TestTree tree = frame.getTestTree();
        tree.selectNode((TestTreeNode)path.getLastPathComponent());
    }
    
    public void mouseClicked(MouseEvent e) 
    {
        x = e.getX();
        y = e.getY();
        
        TestTree tree = frame.getTestTree();
        
        eventPopupMenu = e.getSource().equals(tree);
        component = e.getComponent();
        
        int row  = tree.getRowForLocation(x, y);
        path = tree.getPathForRow(row);

        button = e.getButton();
        clickCount = e.getClickCount();
        
        processMouseClick();
    }
}
