/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: CrossDependenciesPanel.java,v 1.14 2008/12/25 13:47:24 kozlov Exp $
 */

package com.unitesk.testfusion.gui.dialog.options.panel;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.unitesk.testfusion.core.config.CrossDependencyConfig;
import com.unitesk.testfusion.core.config.CrossDependencyListConfig;
import com.unitesk.testfusion.core.config.OptionsConfig;
import com.unitesk.testfusion.core.config.SectionConfig;
import com.unitesk.testfusion.core.config.SectionListConfig;
import com.unitesk.testfusion.gui.GUI;
import com.unitesk.testfusion.gui.dialog.options.CustomizeCrossDependencyDialog;
import com.unitesk.testfusion.gui.event.ConfigChangedEvent;
import com.unitesk.testfusion.gui.event.ConfigChangedListener;
import com.unitesk.testfusion.gui.tree.TreeNode;
import com.unitesk.testfusion.gui.tree.test.TestTreeNode;
import com.unitesk.testfusion.gui.tree.test.crossdeps.CrossDepsTestTree;

import static com.unitesk.testfusion.gui.Layout.*;

/**
 * Panel for working with cross-dependencies.
 * 
 * @author <a href="mailto:kozlov@ispras.ru">Kirill Kozlov</a>
 */
public class CrossDependenciesPanel extends JPanel 
    implements TreeSelectionListener, ConfigChangedListener
{
    public static final long serialVersionUID = 0;
    
    protected GUI frame;
    
    protected SectionConfig config;
    
    protected OptionsConfig options;
    
    protected CrossDepsTestTree tree;
    
    protected CrossDependencyListConfig crossDeps;
    
    protected JButton newCross;
    protected JButton customize;
    protected JButton remove;
    
    public CrossDependenciesPanel(final GUI frame, 
            final SectionConfig config, final OptionsConfig options) 
    {
        super(new GridBagLayout());
        
        this.config = config;
        this.frame = frame;
        this.options = options;
        this.crossDeps = options.getCrossDependencies();
        
        this.tree = new CrossDepsTestTree(frame, config, options);
        
        TreeSelectionModel treeSelectionModel = tree.getSelectionModel();
        treeSelectionModel.addTreeSelectionListener(this);
        
        JScrollPane scrollTreePane = new JScrollPane(tree);
        
        Insets insets = new Insets(SPACE_FROM_BORDER, 
                SPACE_FROM_BORDER, SPACE_FROM_BORDER, SPACE_FROM_BORDER);
        
        GridBagConstraints constraints = getGridBagConstraints(GridBagConstraints.CENTER, 
                GridBagConstraints.BOTH, 3, 1, 0, 0, insets, 1.0, 1.0); 
        
        add(scrollTreePane, constraints);
        
        insets = new Insets(SPACE_FROM_BORDER, 
                SPACE_FROM_BORDER, SPACE_FROM_BORDER, SPACE_FROM_BORDER);
        
        constraints = getGridBagConstraints(GridBagConstraints.CENTER, 
                GridBagConstraints.NONE, 1, 0, insets, 0.0, 1.0);
        
        newCross = new JButton("New Cross");
        
        newCross.addActionListener
        (
            new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    TreePath path = tree.getSelectionPath();
                    
                    TreeNode node = 
                        (TreeNode)path.getLastPathComponent();
                    
                    SectionConfig config = (SectionConfig)node.getConfig();
                    
                    CrossDependencyListConfig cross = 
                        options.getCrossDependencies();
                    
                    cross.addCrossDependency(config);
                }
            }
        );
        
        add(newCross, constraints);
        
        constraints = getGridBagConstraints(GridBagConstraints.CENTER, 
                GridBagConstraints.NONE, 1, 1, insets, 0.0, 1.0);
        
        customize = new JButton("Customize");
        
        customize.addActionListener
        (
            new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    TreePath path = tree.getSelectionPath();
                    TestTreeNode node = (TestTreeNode)path.getLastPathComponent();
                    
                    SectionConfig section = (SectionConfig)node.getConfig();
                    
                    CrossDependencyConfig cross = 
                        crossDeps.getCrossDependency(section);
                    
                    CustomizeCrossDependencyDialog dialog = 
                        new CustomizeCrossDependencyDialog(frame, cross);
                    
                    dialog.setVisible(true);
                }
            }
        );
        
        add(customize, constraints);
        
        constraints = getGridBagConstraints(GridBagConstraints.CENTER, 
                GridBagConstraints.NONE, 1, 2, insets, 0.0, 1.0);
        
        remove = new JButton("Remove");
        
        remove.addActionListener
        (
            new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    TreePath path = tree.getSelectionPath();
                    TestTreeNode node = (TestTreeNode)path.getLastPathComponent();
                    
                    SectionConfig section = (SectionConfig)node.getConfig();
                    
                    crossDeps.removeCrossDependency(section);
                }
            }
        );
        
        add(remove, constraints);
        
        newCross.setEnabled(false);
        remove.setEnabled(false);
        customize.setEnabled(false);
        
        CrossDependencyListConfig crossDeps = options.getCrossDependencies();
        
        crossDeps.addAddElementToConfigListener(tree);
        crossDeps.addAddElementToConfigListener(this);
    }
    
    /**
     * Saves data in Cross Dependencies tab.
     */
    public void saveCrossDependenciesTab()
    {
        
    }
    
    protected void changeButtonsState()
    {
        TreeSelectionModel treeSelectionModel = 
            (TreeSelectionModel)tree.getSelectionModel();
        
        TreePath path = treeSelectionModel.getSelectionPath();
        TestTreeNode node = (TestTreeNode)path.getLastPathComponent();
        
        SectionListConfig config = node.getConfig();
        
        if (config instanceof SectionConfig)
        {
            SectionConfig section = (SectionConfig)config;
            
            CrossDependencyListConfig crossDeps = options.getCrossDependencies();
            
            boolean isCross = crossDeps.isContainCrossDependency(section);
            
            remove.setEnabled(isCross);
            customize.setEnabled(isCross);
            
            newCross.setEnabled(tree.isFreeNode(node) && 
                    !node.isHasAnyDependentAncestor(crossDeps) && 
                    !node.isHasAnyDependentDescendant(crossDeps) && !isCross);
        }
        else 
        {
            newCross.setEnabled(false);
            remove.setEnabled(false);
            customize.setEnabled(false);
        }
    }
    
    public void valueChanged(TreeSelectionEvent event)
    {
        TreeSelectionModel treeSelectionModel = 
            (TreeSelectionModel)tree.getSelectionModel();
        if (treeSelectionModel.getSelectionPath() != null)
            { changeButtonsState(); }
    }
    
    public void configChanged(ConfigChangedEvent event)
    {
        changeButtonsState();
    }
}
