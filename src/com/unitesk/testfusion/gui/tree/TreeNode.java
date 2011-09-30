/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: TreeNode.java,v 1.13 2008/12/13 14:51:24 kozlov Exp $
 */

package com.unitesk.testfusion.gui.tree;

import javax.swing.tree.DefaultMutableTreeNode;

import com.unitesk.testfusion.core.config.Config;

/**
 * Class for representing tree node.
 * @author <a href="mailto:kozlov@ispras.ru">Kirill Kozlov</a>
 */
public class TreeNode extends DefaultMutableTreeNode
{
    public static final long serialVersionUID = 0;
    
    /** Node's identifier, shows type of a node. */
    protected String nodeID;
    
    /** Node's configuration. */
    protected Config config;

    /**
     * Base constructor.
     * 
     * @param <code>nodeID</code> the node's identifier. 
     * 
     * @param <code>config</code> the node's configuration.
     *  
     * @param <code>allowsChildren</code> if true, the node is
     *        allowed to have child nodes -- otherwise, it is 
     *        always a leaf node.
     */
    public TreeNode(String nodeID, Config config, boolean allowsChildren)
    {
        super(allowsChildren);
        
        this.nodeID = nodeID;
        this.config = config;
    }

    /**
     * Returns the node's identifier.
     * 
     * @return the node's identifier.
     */
    public String getNodeID()
    {
        return nodeID;
    }

    /**
     * Returns the node's configuration. 
     * 
     * @return the node's configuration.
     */
    public Config getConfig()
    {
        return config;
    }
    
    /**
     * Returns if specified node lays lower that this node in tree. 
     *  
     * @param <code>node</code> the tree node.
     * 
     * @return <code>true</code> if specified node lays lower that this node
     *         in tree or <code>false</code> otherwise.
     */
    public boolean isLowerNode(TreeNode node)
    {
        int level = getLevel();
        int anotherLevel = node.getLevel();
        
        TreeNode ancestor = this;
        TreeNode anotherAncestor = node;
        
        // get ancestors of our nodes, which have equal levels 
        if (level > anotherLevel)
        {
            for (int i = 0; i < level - anotherLevel; i++)
                { ancestor = (TreeNode)ancestor.getParent(); }
        }
        else if (level < anotherLevel)
        {
            for (int i = 0; i < anotherLevel - level; i++)
                { anotherAncestor = (TreeNode)anotherAncestor.getParent(); }
        }
        
        // get ancestors of our nodes, which have common parent
        while (!ancestor.isNodeSibling(anotherAncestor))
        {
            ancestor = (TreeNode)ancestor.getParent();
            anotherAncestor = (TreeNode)anotherAncestor.getParent();
        }
        
        TreeNode commonParent = (TreeNode)ancestor.getParent();
        
        int index = commonParent.getIndex(ancestor); 
        int anotherIndex = commonParent.getIndex(anotherAncestor);
        
        return  index < anotherIndex; 
    }
    
    /**
     * Returns tree node with specified configuration. Search begins from
     * node's children.
     * 
     * @param <code>config</code> the specified configuration.
     * 
     * @return the tree node with specified configuration if this such
     *         node exists, or null otherwise. 
     */
    public TreeNode getNodeFromChild(Config config)
    {
        for (int i = 0; i < getChildCount(); i++)
        {
            Config currentConfig = ((TreeNode)getChildAt(i)).getConfig(); 
            if (config.equals(currentConfig))
                return (TreeNode)getChildAt(i);
        }
        
        for (int i = 0; i < getChildCount(); i++)
        {
            TreeNode findedNode = 
                ((TreeNode)getChildAt(i)).getNodeFromChild(config);
            if (findedNode != null)
                return findedNode;
        }
        
        return null;
    }
    
    public String toString()
    {
        return config != null ? config.getName() : "<Unknown>";
    }
    
}
