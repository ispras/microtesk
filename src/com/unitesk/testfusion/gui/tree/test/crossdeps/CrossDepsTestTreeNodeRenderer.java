/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: CrossDepsTestTreeNodeRenderer.java,v 1.4 2008/12/25 13:47:27 kozlov Exp $
 */

package com.unitesk.testfusion.gui.tree.test.crossdeps;

import java.awt.Component;
import java.awt.Font;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.TreeCellRenderer;

import com.unitesk.testfusion.core.config.CrossDependencyListConfig;
import com.unitesk.testfusion.core.config.SectionConfig;
import com.unitesk.testfusion.core.config.SectionListConfig;
import com.unitesk.testfusion.core.config.TestConfig;
import com.unitesk.testfusion.gui.tree.CheckBoxTreeCell;
import com.unitesk.testfusion.gui.tree.LabelTreeCell;
import com.unitesk.testfusion.gui.tree.TreeLabel;
import com.unitesk.testfusion.gui.tree.test.TestTreeNode;

/**
 * Tree node renderer for cross dependency test tree node.
 * 
 * @author <a href="mailto:kozlov@ispras.ru">Kirill Kozlov</a>
 */
public class CrossDepsTestTreeNodeRenderer implements TreeCellRenderer 
{
    /** Tree cell with label and checkbox. */
    protected CheckBoxTreeCell checkCell;
    
    /** Tree cell only with label. */
    protected LabelTreeCell labelCell;
    
    /** Node with section configuration for configure options. */
    protected TestTreeNode dependedNode;
    
    /** Current cross dependency list configuration. */
    protected CrossDependencyListConfig crossDeps;
    
    /** Cross dependency test tree. */
    protected CrossDepsTestTree crossTree;

    public CrossDepsTestTreeNodeRenderer(CrossDepsTestTree tree, TestTreeNode node, 
            CrossDependencyListConfig crossDeps) 
    {
        this.crossTree = tree;
        this.dependedNode = node;
        this.crossDeps = crossDeps;
        
        this.checkCell = new CheckBoxTreeCell(); 
        this.labelCell = new LabelTreeCell();
    }
    
    /**
     * Sets parameters for the specified tree label.
     * 
     * @param <code>label</code> tree label for set parametrs.
     * 
     * @param <code>node</code> tree node of the specified tree label.
     * 
     * @param <code>font</code> the font.
     * 
     * @param <code>text</code> the text for of the specified tree label.
     * 
     * @param <code>expanded</code> is specified tree label shown expanded.
     * 
     * @param <code>isSelected</code> is specified tree label shown selected.
     *  
     * @param <code>hasFocus</code> is specified tree label shown focused.
     */
    protected void configureTreeLabel(TreeLabel label, TestTreeNode node, Font font,
            String text, boolean expanded, boolean isSelected, boolean hasFocus)
    {
        ClassLoader loader = getClass().getClassLoader();
        
        if(node.isTestNode())
            { label.setIcon(new ImageIcon(loader.getResource("img/chip.gif"))); }
        else if(expanded) 
            { label.setIcon(UIManager.getIcon("Tree.openIcon")); } 
        else
            { label.setIcon(UIManager.getIcon("Tree.closedIcon")); }
    
        label.setFont(font);
        label.setText(text);
        label.setSelected(isSelected);
        label.setFocus(hasFocus);
    }
    
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean isSelected, 
        boolean expanded, boolean leaf, int row, boolean hasFocus) 
    { 
        String text = tree.convertValueToText(value, isSelected, expanded, leaf, row, hasFocus);
        TestTreeNode node = (TestTreeNode)value;
        
        // depended node
        if (node == dependedNode)
        {
            TreeLabel label = labelCell.getTreeLabel();
            configureTreeLabel(label, node,
                    tree.getFont(), text, expanded, isSelected, hasFocus);
            
            label.setFont(new Font("Arial", Font.ITALIC, 12));
            label.setText(label.getText() + " - Current Section");
            
            return labelCell;
        }
        
        // forbidden nodes
        if (crossTree.isForbiddenNode(node)) 
        {
            configureTreeLabel(labelCell.getTreeLabel(), node,
                    tree.getFont(), text, expanded, isSelected, hasFocus);
            
            labelCell.setEnabled(false);
            
            return labelCell; 
        }
        
        // busy nodes
        if (crossTree.isBusyNode(node))
        {
            TreeLabel label = labelCell.getTreeLabel();
            configureTreeLabel(label, node,
                    tree.getFont(), text, expanded, isSelected, hasFocus);
            
            // TODO : mark out this kind of nodes
            
            return labelCell; 
        }
        
        // available nodes
        configureTreeLabel(checkCell.getTreeLabel(), node,
                tree.getFont(), text, expanded, isSelected, hasFocus);
        
        checkCell.setEnabled(!node.isHasAnyDependentAncestor(crossDeps) &&
                !node.isHasAnyDependentDescendant(crossDeps));
        
        checkCell.getCheckBox().setSelected(isDependOnIt(node));
        
        return checkCell;
    }
    
    /**
     * Returns if dependedNode depends on the specified node.
     * 
     * @param <code>node</code> the tree node.
     * 
     * @return <code>true</code> if dependedNode depends on the specified node
     *         or <code>false</code> otherwise.
     */
    protected boolean isDependOnIt(TestTreeNode node)
    {
        SectionListConfig currentConfig = (SectionListConfig)node.getConfig();
        
        if (currentConfig instanceof TestConfig)
            return false;
        
        SectionConfig section = (SectionConfig)currentConfig;
        
        return crossDeps.isContainCrossDependency(section);
    }
}
