/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: ToolBar.java,v 1.34 2008/08/06 06:22:07 kamkin Exp $
 */

package com.unitesk.testfusion.gui;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JToolBar;
import javax.swing.UIManager;

import com.unitesk.testfusion.gui.action.*;

/**
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class ToolBar extends JToolBar
{
    public static final long serialVersionUID = 0;
    
    protected JButton newButton;
    protected JButton openButton;
    protected JButton saveButton;
    protected JButton runButton;
    protected JButton stopButton;
    protected JButton optionsButton;
    protected JButton settingsButton;
    protected JButton docButton;
    protected JButton sectionButton;
    protected JButton testButton;
    protected JButton testSuiteButton;
    protected JButton backButton;
    protected JButton forwardButton;
    
    public ToolBar(GUI frame)
    {
        super(GUI.APPLICATION_NAME + " Toolbar");
        
        addButtons(frame);
        
        setBorder(BorderFactory.createBevelBorder(0));        
    }
    
    protected void addButtons(final GUI frame)
    {
        ClassLoader loader = getClass().getClassLoader(); 

        newButton = new JButton(new ImageIcon(loader.getResource("img/new.gif")));
        newButton.setToolTipText("New " + GUI.APPLICATION_NAME + " Test");
        newButton.addActionListener(new NewTestAction(frame));
        add(newButton);
        
        openButton = new JButton(UIManager.getIcon("Tree.openIcon"));
        openButton.setToolTipText("Open " + GUI.APPLICATION_NAME + " Test");
        openButton.addActionListener(new OpenTestAction(frame));
        add(openButton);
        
        saveButton = new JButton(new ImageIcon(loader.getResource("img/save.gif")));
        saveButton.setToolTipText("Save " + GUI.APPLICATION_NAME + " Test");
        saveButton.addActionListener(new SaveTestAction(frame, false));
        add(saveButton);
        
        addSeparator();
        
        runButton = new JButton(new ImageIcon(loader.getResource("img/run.gif")));
        runButton.setToolTipText("Run Generation");
        runButton.addActionListener(new RunAction(frame));
        add(runButton);

        stopButton = new JButton(new ImageIcon(loader.getResource("img/stop.gif")));
        stopButton.setToolTipText("Terminate Generation");
        stopButton.addActionListener(new StopAction(frame));
        add(stopButton);
        
        addSeparator();
        
        optionsButton = new JButton(new ImageIcon(loader.getResource("img/options.gif")));
        optionsButton.setToolTipText(GUI.APPLICATION_NAME + " Options");
        optionsButton.addActionListener(new OptionsAction(frame));
        add(optionsButton);
        
        settingsButton = new JButton(new ImageIcon(loader.getResource("img/settings.gif")));
        settingsButton.setToolTipText(GUI.APPLICATION_NAME + " Settings");
        settingsButton.addActionListener(new SettingsAction(frame));
        add(settingsButton);

        addSeparator();
        
        docButton = new JButton(new ImageIcon(loader.getResource("img/doc.gif")));
        docButton.setToolTipText(GUI.APPLICATION_NAME + " Documentation");
        docButton.addActionListener(new DocAction(frame));
        add(docButton);

        addSeparator();
        
        sectionButton = new JButton(new ImageIcon(loader.getResource("img/section.gif")));
        sectionButton.setToolTipText(GUI.APPLICATION_NAME + " Show Section");
        sectionButton.addActionListener(new ShowSectionAction(frame));
        add(sectionButton);

        testButton = new JButton(new ImageIcon(loader.getResource("img/test.gif")));
        testButton.setToolTipText(GUI.APPLICATION_NAME + " Show Test");
        testButton.addActionListener(new ShowTestAction(frame));
        add(testButton);
        
        testSuiteButton = new JButton(new ImageIcon(loader.getResource("img/testsuite.gif")));
        testSuiteButton.setToolTipText(GUI.APPLICATION_NAME + " Show Workspace");
        testSuiteButton.addActionListener(new ShowTestSuiteAction(frame));
        add(testSuiteButton);

        addSeparator();

        backButton = new JButton(new ImageIcon(loader.getResource("img/back.gif")));
        backButton.addActionListener(new BackAction(frame));
        add(backButton);
        
        forwardButton = new JButton(new ImageIcon(loader.getResource("img/forward.gif")));
        forwardButton.addActionListener(new ForwardAction(frame));
        add(forwardButton);
    }
    
    public JButton getNewButton()       { return newButton; }
    public JButton getOpenButton()      { return openButton; }
    public JButton getSaveButton()      { return saveButton; }
    public JButton getRunButton()       { return runButton; }
    public JButton getStopButton()      { return stopButton; }
    public JButton getOptionsButton()   { return optionsButton; }
    public JButton getSettingsButton()  { return settingsButton; }
    public JButton getDocButton()       { return docButton; }
    public JButton getSectionButton()   { return sectionButton; }
    public JButton getTestButton()      { return testButton; }
    public JButton getTestSuiteButton() { return testSuiteButton; }
    public JButton getBackButton()      { return backButton; }
    public JButton getForwardButton()   { return forwardButton; }
}