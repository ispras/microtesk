/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: CrossDepsTestTree.java,v 1.5 2008/12/25 13:47:27 kozlov Exp $
 */

package com.unitesk.testfusion.gui.tree.test.crossdeps;

import java.util.Collection;
import java.util.HashSet;

import com.unitesk.testfusion.core.config.CrossDependencyListConfig;
import com.unitesk.testfusion.core.config.OptionsConfig;
import com.unitesk.testfusion.core.config.SectionConfig;
import com.unitesk.testfusion.core.config.SectionListConfig;
import com.unitesk.testfusion.gui.GUI;
import com.unitesk.testfusion.gui.event.ConfigChangedEvent;
import com.unitesk.testfusion.gui.event.ConfigChangedListener;
import com.unitesk.testfusion.gui.tree.test.BasicTestTree;
import com.unitesk.testfusion.gui.tree.test.TestTreeNode;

/**
 * Class for tree, which is used for working with cross-dependency.
 * 
 * @author <a href="mailto:kozlov@ispras.ru">Kirill Kozlov</a>
 */
public class CrossDepsTestTree extends BasicTestTree 
    implements ConfigChangedListener
{
    public static final long serialVersionUID = 0;
    
    /** Section configuration for configure options. */ 
    protected SectionConfig section;
    
    /** Current options. */
    protected OptionsConfig options;
    
    /** Node in tree with section configuration for configure options. */  
    protected TestTreeNode dependedNode;
    
    /** dependedNode never can't depends on these nodes. */
    protected Collection<TestTreeNode> forbiddenNodes = 
        new HashSet<TestTreeNode>();
    
    /** 
     * dependedNode can't depends on these nodes. If it depends on them, 
     * it'll be a conflict. 
     * */
    protected Collection<TestTreeNode> busyNodes = 
        new HashSet<TestTreeNode>(); 
    
    /**
     * Base constructor.
     * 
     * @param <code>frame</code> the parent frame.
     */
    public CrossDepsTestTree(GUI frame, SectionConfig section, 
            OptionsConfig options)
    {
        super(frame);
        
        this.section = section;
        this.options = options;
        
        updateConfig();
        
        dependedNode = (TestTreeNode)getNode(section);
        
        setCellRenderer(new CrossDepsTestTreeNodeRenderer(this,
                dependedNode, options.getCrossDependencies()));
        
        addMouseListener(new CrossDepsTestTreeNodeListener(frame, this));
        
        //setSelectionModel(null);
        
        expand();
        
        initForbiddenNodes();
        
        initBusyNodes();
    }
    
    public TestTreeNode getDependedNode()
    {
        return (TestTreeNode)getNode(section);
    }
    
    public CrossDependencyListConfig getCrossDependencies()
    {
        return options.getCrossDependencies();
    }
    
    public void configChanged(ConfigChangedEvent event)
    {
        update();
    }
    
    protected void initForbiddenNodes()
    {
        // add depended node
        forbiddenNodes.add(dependedNode);
        
        // add all ancestors of the depended node 
        forbiddenNodes.addAll(dependedNode.getAllAncestors());
        
        // add all descendants of the depended node
        forbiddenNodes.addAll(dependedNode.getAllDescendants());

        // add all descendants of the depended node
        forbiddenNodes.addAll(dependedNode.getAllLowerNodes());
    }
    
    protected void initBusyNodes()
    {
        Collection<TestTreeNode> descendants = dependedNode.getAllDescendants();
        
        addBusyNode(descendants);
        
        Collection<TestTreeNode> ancestors = dependedNode.getAllAncestors();
        
        addBusyNode(ancestors);
    }
    
    protected void addBusyNode(Collection<TestTreeNode> nodes)
    {
        for (TestTreeNode oneNode : nodes)
        {
            SectionListConfig sectionList = oneNode.getConfig();
            
            CrossDependencyListConfig crossDeps = 
                sectionList.getOptions().getCrossDependencies();
            
            for (int i = 0; i < crossDeps.countCrossDependencies(); i++)
            {
                SectionConfig dependOn = 
                    crossDeps.getCrossDependency(i).getDependsOnSection();
                
                TestTreeNode node = (TestTreeNode)getNode(dependOn); 
                
                busyNodes.add(node);
                
                busyNodes.addAll(node.getAllAncestors());
                
                busyNodes.addAll(node.getAllDescendants());
            }
        }
    }
    
    public boolean isForbiddenNode(TestTreeNode node)
    {
        return forbiddenNodes.contains(node);
    }
    
    public boolean isBusyNode(TestTreeNode node)
    {
        return busyNodes.contains(node);
    }
    
    public boolean isFreeNode(TestTreeNode node)
    {
        return !forbiddenNodes.contains(node) && !busyNodes.contains(node); 
    }
}
