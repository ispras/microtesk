/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: View.java,v 1.12 2008/08/18 12:05:57 kozlov Exp $
 */

package com.unitesk.testfusion.gui;

import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import com.unitesk.testfusion.gui.tree.section.SectionTree;
import com.unitesk.testfusion.gui.tree.test.TestTree;

/**
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class View extends JPanel
{
    public static final long serialVersionUID = 0;

    protected SectionTree sectionTree;
    protected TestTree testTree;
    protected Workspace workspace;
    
    public View(final GUI frame)
    {
        super(new GridLayout(1, 0));

        sectionTree = new SectionTree(frame);
        testTree = new TestTree(frame);
        
        workspace = new Workspace(frame);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

        splitPane.setLeftComponent(createTreesViewPanel());
        splitPane.setRightComponent(workspace);

        splitPane.setDividerLocation(300); 
        splitPane.setBorder(BorderFactory.createEmptyBorder());
        
        add(splitPane);

        setBorder(BorderFactory.createEmptyBorder());
    }
    
    protected JPanel createTreesViewPanel()
    {
    	JPanel panel = new JPanel(new GridLayout(0, 1));
    	
    	JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    	
    	splitPane.setTopComponent(new JScrollPane(sectionTree));
    	splitPane.setBottomComponent(new JScrollPane(testTree));
    	
    	splitPane.setDividerLocation(600);
    	
    	splitPane.setBorder(BorderFactory.createEmptyBorder());
        
        panel.add(splitPane);

        panel.setBorder(BorderFactory.createEmptyBorder());
        
        return panel;
    }
    
    public SectionTree getSectionTree()
    {
        return sectionTree;
    }
    
    public TestTree getTestTree()
    {
        return testTree;
    }
    
    public Workspace getWorkspace()
    {
        return workspace;
    }
    
    public Panel getPanel()
    {
        return workspace.getPanel();
    }
    
    public Console getConsole()
    {
        return workspace.getConsole();
    }
}

