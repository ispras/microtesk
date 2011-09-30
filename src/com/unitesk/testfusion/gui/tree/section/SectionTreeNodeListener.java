/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: SectionTreeNodeListener.java,v 1.4 2008/10/03 10:41:04 vorobyev Exp $
 */

package com.unitesk.testfusion.gui.tree.section;

import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JCheckBox;
import javax.swing.Timer;
import javax.swing.tree.TreePath;

import com.unitesk.testfusion.gui.GUI;
import com.unitesk.testfusion.gui.Panel;
import com.unitesk.testfusion.gui.action.TestSelectionConfigAction;

/**
 * @author <a href="mailto:protsenko@ispras.ru">Alexander Protsenko</a>
 */
public class SectionTreeNodeListener extends MouseAdapter
{
    protected int button;
    protected int clickCount;
    protected int checkBoxWidth;
    protected Timer timer;
    
    protected TreePath path;
    protected int row;
    protected boolean check;
    
    protected final int delay = 0;
    
    protected GUI frame;
    
    protected SectionTreePopupMenu menu;
    
    protected boolean eventPopupMenu;
    protected int x;
    protected int y;
    protected Object component;

    public SectionTreeNodeListener(final GUI frame) 
    {
        this.frame = frame;
        
        ActionListener listener = new ActionListener()
        {
            public void actionPerformed(ActionEvent evt) 
            {
                if(path == null || !frame.getSectionTree().isEnabled())
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
                    else
                    {
                        if(clickCount == 1)
                            { processSingleClick_TreeLabel_LeftButton(); }
                        else
                            { processDoubleClick_TreeLabel_LeftButton(); }
                    }
                }
                else
                {
                    if(check)
                    {
                        if(clickCount == 1)
                            { processSingleClick_CheckBox_RightButton(); }
                        else
                            { processDoubleClick_CheckBox_RightButton(); }
                    }
                    else
                    {
                        if(clickCount == 1)
                            { processSingleClick_TreeLabel_RightButton(evt); }
                        else
                            { processDoubleClick_TreeLabel_RightButton(); }
                    }                    
                }
            }
        };
        
        timer = new Timer(delay, listener);
        timer.setRepeats(false);
        checkBoxWidth = new JCheckBox().getPreferredSize().width;
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
    
    protected void processSingleClick_TreeLabel_LeftButton()
    {
        showPanel();
    }
    
    protected void processDoubleClick_TreeLabel_LeftButton()
    {
        showPanel();
    }
    
    protected void processSingleClick_CheckBox_RightButton()
    {
    }
    
    protected void processDoubleClick_CheckBox_RightButton()
    {
    }
    
    protected void processSingleClick_TreeLabel_RightButton(ActionEvent evt)
    {
        showPopupMenu(evt);
    }
    
    protected void processDoubleClick_TreeLabel_RightButton()
    {
    }
        
    protected void showPopupMenu(ActionEvent evt)
    {
        if((button == MouseEvent.BUTTON3) && eventPopupMenu)
        {    
            SectionTreeNode node = (SectionTreeNode)path.getLastPathComponent();

            if(node.isEnabled())
            {
                // Create popup menu
                menu = new SectionTreePopupMenu(frame, node);
                
                menu.show((Component) component, x, y);
            }
        }
    }
    
    protected boolean clickOnCheckBox(MouseEvent e, TreePath path)
    {
        SectionTree tree = frame.getSectionTree();
        Rectangle bounds = tree.getPathBounds(path);
        
        if(path == null)
            { return false; }
            
        if(tree.getComponentOrientation().isLeftToRight())
            { return e.getX() < bounds.x + checkBoxWidth; }
        else
            { return e.getX() > bounds.x + bounds.width - checkBoxWidth; }
    }
    
    protected void showPanel()
    {
        Panel panel = frame.getPanel();
        SectionTreeNode node = (SectionTreeNode)path.getLastPathComponent();
        
        panel.showPanel(node.getConfig());
    }
    
    protected void checkNode()
    {
        SectionTreeNode node = (SectionTreeNode)path.getLastPathComponent();
        
        TestSelectionConfigAction action = 
            new TestSelectionConfigAction(frame, node.getConfig());
        
        action.execute();
    }
    
    public void mouseClicked(MouseEvent e) 
    {
        SectionTree tree = frame.getSectionTree();
        if (tree.isEnabled())
        {
            x = e.getX();
            y = e.getY();
            
            eventPopupMenu = e.getSource().equals(frame.getSectionTree());
            component = e.getComponent();
            
            row  = tree.getRowForLocation(x, y);
            path = tree.getPathForRow(row);
            check = clickOnCheckBox(e, path);

            button = e.getButton();
            clickCount = e.getClickCount();
            timer.start();
        }
    }
}
