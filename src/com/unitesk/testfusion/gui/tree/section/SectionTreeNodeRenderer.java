/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: SectionTreeNodeRenderer.java,v 1.5 2008/12/18 14:31:09 kozlov Exp $
 */

package com.unitesk.testfusion.gui.tree.section;

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.TreeCellRenderer;

import com.unitesk.testfusion.gui.tree.CheckBoxTreeCell;
import com.unitesk.testfusion.gui.tree.TreeLabel;


/**
 * @author <a href="mailto:protsenko@ispras.ru">Alexander Protsenko</a>
 * @author <a href="mailto:kozlov@ispras.ru">Kirill Kozlov</a>
 */
public class SectionTreeNodeRenderer implements TreeCellRenderer 
{
    protected CheckBoxTreeCell cell;

    public SectionTreeNodeRenderer() 
    {
        cell = new CheckBoxTreeCell(); 
    }

    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean isSelected, 
        boolean expanded, boolean leaf, int row, boolean hasFocus) 
    { 
        ClassLoader loader = getClass().getClassLoader(); 
        
        String stringValue = tree.convertValueToText(value, isSelected, expanded, leaf, row, hasFocus);
        SectionTreeNode node = (SectionTreeNode)value;
        
        JCheckBox check = cell.getCheckBox();
        TreeLabel label = cell.getTreeLabel();
        
        //cell.setEnabled(node.isEnabled());
        
        check.setEnabled(node.isEnabled() && tree.isEnabled());
        check.setSelected(node.isSelected());
        
        //label.setEnabled(tree.isEnabled());
        label.setFont(tree.getFont());
        label.setText(stringValue);
        label.setSelected(isSelected);
        label.setFocus(hasFocus);
        
        if(node.isProcessorNode())
            { label.setIcon(new ImageIcon(loader.getResource("img/chip.gif"))); }
        else if(node.isInstructionNode())
            { label.setIcon(new ImageIcon(loader.getResource("img/instruction.gif"))); }
        else if(leaf) 
            { label.setIcon(UIManager.getIcon("Tree.leafIcon")); } 
        else if(expanded) 
            { label.setIcon(UIManager.getIcon("Tree.openIcon")); } 
        else
            { label.setIcon(UIManager.getIcon("Tree.closedIcon")); }
        
        return cell;
    }
}
