/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: SectionTree.java,v 1.12 2008/12/25 12:07:12 kozlov Exp $
 */

package com.unitesk.testfusion.gui.tree.section;

import javax.swing.tree.DefaultTreeModel;

import com.unitesk.testfusion.gui.GUI;
import com.unitesk.testfusion.gui.tree.DepthFirstTreeWalker;
import com.unitesk.testfusion.gui.tree.SelectionListener;
import com.unitesk.testfusion.gui.tree.Tree;
import com.unitesk.testfusion.gui.tree.section.ProcessorNode;

import com.unitesk.testfusion.core.config.SectionConfig;

/**
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 * @author <a href="mailto:kozlov@ispras.ru">Kirill Kozlov</a>
 */
public class SectionTree extends Tree
{
    public static final long serialVersionUID = 0;

    public SectionTree(GUI frame)
    {
        super(frame);

        updateConfig();

        setCellRenderer(new SectionTreeNodeRenderer());
        addMouseListener(new SectionTreeNodeListener(frame));
        
        addKeyListener(new SelectionListener(frame, this));
    }
    
    public void expand()
    {
        TreeNodeExpander expander = new TreeNodeExpander(TreeNodeExpander.EXPAND_GROUP);
        SectionTreeVisitorAdapter adapter = new SectionTreeVisitorAdapter(expander); 
        DepthFirstTreeWalker walker = new DepthFirstTreeWalker(this, adapter);
        
        walker.process();
    }

    public void updateConfig()
    {
        SectionConfig currentSection = frame.getSection();
        
        if(currentSection == null || !currentSection.isLeaf())
        {
            setEnabled(false);
            setModel(new DefaultTreeModel(new ProcessorNode(frame.getConfig().getProcessor())));
        }
        else
        {
            setEnabled(true);
            setModel(new DefaultTreeModel(new ProcessorNode(frame.getSection().getProcessor())));
            selectRoot();
        }
        
        expand();
        update();
    }
    
    /**
     * Actions for changing node state. Set node selected for test
     * or not.
     * 
     * @param <code>node</code> the node for changing stste
     */
    public void testNode(SectionTreeNode node) 
    {
        DefaultTreeModel model = (DefaultTreeModel)getModel();
        
        model.nodeChanged(node);
        
        revalidate();
        repaint();
    }
}
