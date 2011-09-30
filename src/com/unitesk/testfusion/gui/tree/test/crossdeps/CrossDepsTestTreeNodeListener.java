/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: CrossDepsTestTreeNodeListener.java,v 1.5 2010/01/14 16:54:40 vorobyev Exp $
 */

package com.unitesk.testfusion.gui.tree.test.crossdeps;

import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JCheckBox;
import javax.swing.tree.TreePath;

import com.unitesk.testfusion.core.config.CrossDependencyListConfig;
import com.unitesk.testfusion.core.config.SectionConfig;
import com.unitesk.testfusion.gui.GUI;
import com.unitesk.testfusion.gui.tree.test.TestTreeNode;

/**
 * Listener for cross dependency test tree.
 * 
 * @author <a href="mailto:kozlov@ispras.ru">Kirill Kozlov</a>
 */
public class CrossDepsTestTreeNodeListener extends MouseAdapter
{
    protected int button;
    protected int clickCount;
    protected int checkBoxWidth;
    
    protected TreePath path;
    protected int row;
    protected boolean check;
    
    protected GUI frame;
    
    protected CrossDepsTestTree tree; 
    
    protected int x;
    protected int y;
    protected Object component;

    public CrossDepsTestTreeNodeListener(final GUI frame, CrossDepsTestTree tree) 
    {
        this.frame = frame;
        this.tree = tree;
        
        checkBoxWidth = new JCheckBox().getPreferredSize().width;
    }
    
    public void processMouseClick()
    {
        if(path == null)
            { return; }
    
        if(button == MouseEvent.BUTTON1)
        {
            if(check)
            {
                if(clickCount == 1)
                    { processSingleClick_CheckBox_LeftButton(); }
                else
                    { processDoubleClick_CheckBox_LeftButton(); }
            }
        }
    }

    public void clickTree(MouseEvent event)
    {
    }

    protected void processSingleClick_CheckBox_LeftButton()
    {
        checkNode();
    }
    
    protected void processDoubleClick_CheckBox_LeftButton()
    {
        checkNode();
    }
    
    protected boolean clickOnCheckBox(MouseEvent e, TreePath path)
    {
        Rectangle bounds = tree.getPathBounds(path);
        
        if(path == null)
            { return false; }
            
        if(tree.getComponentOrientation().isLeftToRight())
            { return e.getX() < bounds.x + checkBoxWidth; }
        else
            { return e.getX() > bounds.x + bounds.width - checkBoxWidth; }
    }
    
    protected void checkNode()
    {
        TestTreeNode node = (TestTreeNode)path.getLastPathComponent();
        
        CrossDependencyListConfig crossDeps = tree.getCrossDependencies();
        
        if (tree.isFreeNode(node) && 
                !node.isHasAnyDependentAncestor(crossDeps) &&
                !node.isHasAnyDependentDescendant(crossDeps))
        {
            SectionConfig section = (SectionConfig)node.getConfig();
            
            if (!crossDeps.isContainCrossDependency(section))
            {
                crossDeps.addCrossDependency(section);
            }
            else 
            {
                int choice = frame.showConfirmYesNoWarningDialog(
                        "Do you want to delete this cross dependency?",
                        "Delete Cross Dependency");
                
                if (choice == GUI.YES_OPTION)
                    { crossDeps.removeCrossDependency(section); }
            }
        }
    }
    
    public void mouseClicked(MouseEvent e) 
    {
        if (tree.isEnabled())
        {
            x = e.getX();
            y = e.getY();
            
            // eventPopupMenu = e.getSource().equals(frame.getSectionTree());
            // component = e.getComponent();
            
            row  = tree.getRowForLocation(x, y);
            path = tree.getPathForRow(row);
            check = clickOnCheckBox(e, path);

            button = e.getButton();
            clickCount = e.getClickCount();
            processMouseClick();
        }
    }
}
