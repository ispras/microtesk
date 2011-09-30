/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: TestTreeNodeRenderer.java,v 1.3 2008/12/18 14:01:32 kozlov Exp $
 */

package com.unitesk.testfusion.gui.tree.test;

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.TreeCellRenderer;

import com.unitesk.testfusion.gui.tree.LabelTreeCell;
import com.unitesk.testfusion.gui.tree.TreeLabel;

/**
 * @author <a href="mailto:kozlov@ispras.ru">Kirill Kozlov</a>
 */
public class TestTreeNodeRenderer implements TreeCellRenderer 
{
    protected LabelTreeCell cell;

    public TestTreeNodeRenderer() 
    {
        cell = new LabelTreeCell();
    }

    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean isSelected, 
        boolean expanded, boolean leaf, int row, boolean hasFocus) 
    { 
        ClassLoader loader = getClass().getClassLoader(); 
        
        String stringValue = tree.convertValueToText(value, isSelected, expanded, leaf, row, hasFocus);
        TestTreeNode node = (TestTreeNode)value;
        
        cell.setEnabled(tree.isEnabled());
        
        TreeLabel label = cell.getTreeLabel();
        
        label.setEnabled(tree.isEnabled());

        label.setFont(tree.getFont());
        label.setText(stringValue);
        label.setSelected(isSelected);
        label.setFocus(hasFocus);
        
        if(node.isTestNode())
            { label.setIcon(new ImageIcon(loader.getResource("img/chip.gif"))); }
        else if(expanded) 
            { label.setIcon(UIManager.getIcon("Tree.openIcon")); } 
        else
            { label.setIcon(UIManager.getIcon("Tree.closedIcon")); }
        
        return cell;
    }
}
