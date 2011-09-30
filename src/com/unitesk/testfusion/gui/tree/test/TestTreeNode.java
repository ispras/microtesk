/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: TestTreeNode.java,v 1.3 2008/12/25 12:07:14 kozlov Exp $
 */

package com.unitesk.testfusion.gui.tree.test;

import java.util.Collection;
import java.util.HashSet;

import com.unitesk.testfusion.core.config.CrossDependencyListConfig;
import com.unitesk.testfusion.core.config.SectionConfig;
import com.unitesk.testfusion.core.config.SectionListConfig;
import com.unitesk.testfusion.gui.tree.TreeNode;

/**
 * @author <a href="mailto:kozlov@ispras.ru">Kirill Kozlov</a>
 */
public class TestTreeNode extends TreeNode
{
	public static final long serialVersionUID = 0;
    
    public final static String TEST_NODE_ID    = TestNode.NODE_ID;
    public final static String SECTION_NODE_ID = SectionNode.NODE_ID;
    
    public TestTreeNode(String nodeID)
    {
        this(nodeID, null);
    }
    
    public TestTreeNode(String nodeID, SectionListConfig config) 
    {
    	this(nodeID, config, true);
    }

    public TestTreeNode(String nodeID, SectionListConfig config, boolean allowsChildren) 
    {
        super(nodeID, config, allowsChildren);
    }
    
    public boolean isTestNode()
    {
        return nodeID.equals(TEST_NODE_ID);
    }
    
    public boolean isSectionNode()
    {
        return nodeID.equals(SECTION_NODE_ID);
    }
    
    /**
     * Returns a collection of all ancestors of the node.
     * Recursive realization.
     *  
     * @return a collection of all ancestors of the node.
     */
    public Collection<TestTreeNode> getAllAncestors()
    {
        Collection<TestTreeNode> ancestors = new HashSet<TestTreeNode>();
        
        TestTreeNode currentParent = (TestTreeNode)getParent();
        
        while (currentParent != null)
        {
            ancestors.add(currentParent);
            
            currentParent = (TestTreeNode)currentParent.getParent();
        }
            
        return ancestors;
    }
    
    /**
     * Returns a collection of all descendants of the node.
     * Recursive realization.
     *  
     * @return a collection of all descendants of the node.
     */
    public Collection<TestTreeNode> getAllDescendants()
    {
        Collection<TestTreeNode> descendants = new HashSet<TestTreeNode>();
        
        if (!isLeaf())
        {
            for (int i = 0; i < getChildCount(); i++)
            {
                TestTreeNode child = (TestTreeNode)getChildAt(i); 
                descendants.add(child);
                
                descendants.addAll(child.getAllDescendants());
            }
        }
            
        return descendants;
    }
    
    /**
     * Returns a collection of all nodes, which lay "under" the the node.
     * Recursive realization.
     * 
     * @return a collection of all nodes, which lay "under" the the node.
     */
    public Collection<TestTreeNode> getAllLowerNodes()
    {
        Collection<TestTreeNode> lower = new HashSet<TestTreeNode>();
        
        TestTreeNode parent = (TestTreeNode)getParent();
        
        if (parent != null)
        {
            for(int i = parent.getIndex(this) + 1; i < parent.getChildCount(); i++)
            {
                TestTreeNode currentChild = (TestTreeNode)parent.getChildAt(i); 
                lower.add(currentChild);
                lower.addAll(currentChild.getAllDescendants());
            }
            
            lower.addAll(parent.getAllLowerNodes());
        }
        
        return lower;
    }
    
    public SectionListConfig getConfig()
    {
        return (SectionListConfig)config;
    }
    
    /**
     * Returns if the node has any ancestor, which has configuration from 
     * the specified cross dependency list configuration. 
     * 
     * @param <code>crossDeps</code> the cross dependency list configuration.
     * 
     * @return <code>true</code> if the node has any ancestor, which has 
     *         configuration from the specified cross dependency list 
     *         configuration or <code>false</code> otherwise.
     */
    public boolean isHasAnyDependentAncestor(CrossDependencyListConfig crossDeps)
    {
        TestTreeNode currentParent = (TestTreeNode)getParent();
        
        while (currentParent != null)
        {
            SectionListConfig config = 
                (SectionListConfig)currentParent.getConfig();
            
            if (config instanceof SectionConfig)
            {
                if (crossDeps.isContainCrossDependency((SectionConfig)config))
                    return true;
            }
            
            currentParent = (TestTreeNode)currentParent.getParent();
        }
        
        return false;
    }
    
    /**
     * Returns if the node has any descendant, which has configuration from 
     * the specified cross dependency list configuration. 
     * 
     * @param <code>crossDeps</code> the cross dependency list configuration.
     * 
     * @return <code>true</code> if the node has any descendant, which has 
     *         configuration from the specified cross dependency list 
     *         configuration or <code>false</code> otherwise.
     */
    public boolean isHasAnyDependentDescendant(CrossDependencyListConfig crossDeps)
    {
        if (isLeaf())
            { return false; }
        
        for (int i = 0; i < getChildCount(); i++)
        {
            TestTreeNode currentChild = (TestTreeNode)getChildAt(i); 
            SectionConfig section = (SectionConfig)currentChild.getConfig();
            
            if (crossDeps.isContainCrossDependency(section))
                return true;
        }
        
        for (int i = 0; i < getChildCount(); i++)
        {
            TestTreeNode currentChild = (TestTreeNode)getChildAt(i);
            if (currentChild.isHasAnyDependentDescendant(crossDeps))
                return true;
        }
        
        return false;
    }
    
    public boolean isEnabled()
    {
        return !config.isEmpty();
    }
}
