package com.unitesk.testfusion.gui.tree.section;

import javax.swing.tree.TreePath;

import com.unitesk.testfusion.gui.tree.TreeNode;

public class TreeNodeExpander implements SectionTreeNodeVisitor
{
    public static final int EXPAND_NONE        = 0;
    public static final int EXPAND_PROCESSOR   = 1;
    public static final int EXPAND_GROUP       = 2;
    public static final int EXPAND_INSTRUCTION = 3;

    public static final int EXPAND_ALL = EXPAND_INSTRUCTION;

    protected int mode;
    
    public TreeNodeExpander(int mode)
    {
        this.mode = mode;
    }
    
    public TreeNodeExpander()
    {
        this(EXPAND_ALL);
    }
    
    protected void expand(SectionTree tree, TreeNode node)
    {
        tree.expandPath(new TreePath(node.getPath()));
    }

    public void onProcessor(SectionTree tree, ProcessorNode section)
    {
        if(mode >= EXPAND_PROCESSOR)
            { expand(tree, section); }
    }

    public void onGroup(SectionTree tree, GroupNode group)
    {
        if(mode >= EXPAND_GROUP)
            { expand(tree, group); }
    }

    public void onInstruction(SectionTree tree, InstructionNode instruction)
    {
        if(mode >= EXPAND_INSTRUCTION)
            { expand(tree, instruction); }
    }

    public void onSituation(SectionTree tree, SituationNode situation)
    {
        expand(tree, situation);
    }
}
