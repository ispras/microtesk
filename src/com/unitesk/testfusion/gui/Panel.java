/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: Panel.java,v 1.26 2008/08/27 14:09:34 kozlov Exp $
 */

package com.unitesk.testfusion.gui;

import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import com.unitesk.testfusion.core.config.*;
import com.unitesk.testfusion.gui.panel.ConfigPanel;
import com.unitesk.testfusion.gui.panel.DependencyPanel;

import com.unitesk.testfusion.gui.panel.*;

/**
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class Panel extends JPanel
{
    public static final long serialVersionUID = 0;
    
    protected GUI frame;
    
    protected CompositeSectionPanel compositeSectionPanel;
    protected SectionPanel          sectionPanel;
    protected GroupPanel            groupPanel;
    protected InstructionPanel      instructionPanel;
    protected SituationPanel        situationPanel;
    protected DependencyPanel       dependencyPanel;
    protected TestSuitePanel        testSuitePanel;
    protected TestPanel             testPanel;
    
    protected ConfigPanel currentPanel;
    
    public Panel(GUI frame)
    {
        super(new GridLayout(1, 1));
        
        this.frame = frame;
        
        compositeSectionPanel = new CompositeSectionPanel(frame);
        sectionPanel          = new SectionPanel(frame);
        groupPanel            = new GroupPanel(frame);
        instructionPanel      = new InstructionPanel(frame);
        situationPanel        = new SituationPanel(frame);
        dependencyPanel       = new DependencyPanel(frame);
        testSuitePanel        = new TestSuitePanel(frame);
        testPanel             = new TestPanel(frame);
        
        setBorder(BorderFactory.createEmptyBorder());        

        showPanel(frame.getConfig(), false);
    }
    
    protected void hideCurrentPanel()
    {
        if(currentPanel != null)
        {
            currentPanel.setVisible(false);
            remove(currentPanel);
        }
    }
    
    protected void showCurrentPanel(Config config)
    {
        if(currentPanel != null)
        {
            currentPanel.setVisible(true);
            currentPanel.show(config);
            invalidate();
        }
    }
    
    protected void showSectionPanel(SectionConfig section)
    {
        hideCurrentPanel();
        add(currentPanel = compositeSectionPanel);
    	showCurrentPanel(section);
    }
    
    protected void showSectionPanel(ProcessorConfig processor)
    {
        hideCurrentPanel();
        add(currentPanel = sectionPanel);
        showCurrentPanel(processor);
    }
    
    protected void showGroupPanel(GroupConfig group)
    {
        hideCurrentPanel();
        add(currentPanel = groupPanel);
        showCurrentPanel(group);
    }
    
    protected void showInstructionPanel(InstructionConfig instruction)
    {
        hideCurrentPanel();
        add(currentPanel = instructionPanel);
        showCurrentPanel(instruction);
    }
    
    protected void showSituationPanel(SituationConfig situation)
    {
        hideCurrentPanel();
        add(currentPanel = situationPanel);
        showCurrentPanel(situation);
    }
    
    protected void showDependencyPanel(DependencyConfig dependency)
    {
        hideCurrentPanel();
        add(currentPanel = dependencyPanel);
        showCurrentPanel(dependency);
    }
    
    protected void showTestSuitePanel(TestSuiteConfig testSuite)
    {
        hideCurrentPanel();
        add(currentPanel = testSuitePanel);
        showCurrentPanel(testSuite);
    }
    
    protected void showTestPanel(TestConfig test)
    {
        hideCurrentPanel();
        add(currentPanel = testPanel);
        showCurrentPanel(test);
    }

    public void showPanel(Config config, boolean addHistory)
    {
        HistoryManager history = frame.getHistory();

        if(addHistory)
            { history.add(config); }
        
        frame.enableBackAction(!history.isFirst());
        frame.enableForwardAction(!history.isLast());
        
        frame.updateTitle();
        
        if(config instanceof SectionConfig)
        {
            SectionConfig sectionConfig = (SectionConfig)config;
            
            if (sectionConfig.isLeaf())
                { showSectionPanel(sectionConfig.getProcessor()); }
            else
                { showSectionPanel(sectionConfig); }
        }
        if(config instanceof ProcessorConfig)
            { showSectionPanel((ProcessorConfig)config); }
        else if(config instanceof GroupConfig)
            { showGroupPanel((GroupConfig)config); }
        else if(config instanceof InstructionConfig)
            { showInstructionPanel((InstructionConfig)config); }
        else if(config instanceof SituationConfig)
            { showSituationPanel((SituationConfig)config); }
        else if(config instanceof DependencyConfig)
            { showDependencyPanel((DependencyConfig)config); }
        else if(config instanceof TestSuiteConfig)
            { showTestSuitePanel((TestSuiteConfig)config); }
        else if(config instanceof TestConfig)
            { showTestPanel((TestConfig)config); }
    }
    
    public void showPanel(Config config)
    {
        showPanel(config, true);
    }
    
    public void update()
    {
        if(currentPanel != null)
            { currentPanel.update(); }
    }
}
