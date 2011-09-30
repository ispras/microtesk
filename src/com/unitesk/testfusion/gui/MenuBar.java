/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: MenuBar.java,v 1.44 2008/09/19 08:16:35 kamkin Exp $
 */

package com.unitesk.testfusion.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import com.unitesk.testfusion.gui.action.*;
import com.unitesk.testfusion.gui.dialog.*;

/**
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class MenuBar extends JMenuBar
{
    public static final long serialVersionUID = 0;

    protected JMenu fileMenu;
    protected JMenuItem newTestMenuItem;
    protected JMenuItem openTestMenuItem;
    protected JMenuItem saveTestMenuItem;
    protected JMenuItem saveTestAsMenuItem;
    protected JMenuItem switchWorkspaceMenuItem;
    protected JMenuItem exitMenuItem;
    
    protected JMenu generationMenu;
    protected JMenuItem runMenuItem;
    protected JMenuItem stopMenuItem;
    protected JMenuItem optionsMenuItem;
    protected JMenuItem settingsMenuItem;

    protected JMenu windowMenu;
    protected JMenuItem showSectionMenuItem;
    protected JMenuItem showTestMenuItem;
    protected JMenuItem showTestSuiteMenuItem;

    protected JMenu helpMenu;
    protected JMenuItem helpMenuItem;
    protected JMenuItem aboutMenuItem;

    public MenuBar(final GUI frame)
    {
        //**********************************************************************************************
        // File menu 
        //**********************************************************************************************
        fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);

        newTestMenuItem = new JMenuItem("New Test");
        newTestMenuItem.setMnemonic(KeyEvent.VK_N);
        newTestMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
        newTestMenuItem.addActionListener(new NewTestAction(frame));
        fileMenu.add(newTestMenuItem);
        
        openTestMenuItem = new JMenuItem("Open Test...");
        openTestMenuItem.setMnemonic(KeyEvent.VK_O);
        openTestMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
        openTestMenuItem.addActionListener(new OpenTestAction(frame));
        fileMenu.add(openTestMenuItem);
        
        fileMenu.addSeparator();
        
        saveTestMenuItem = new JMenuItem("Save");
        saveTestMenuItem.setMnemonic(KeyEvent.VK_S);
        saveTestMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
        saveTestMenuItem.addActionListener(new SaveTestAction(frame, false));
        fileMenu.add(saveTestMenuItem);
        
        saveTestAsMenuItem = new JMenuItem("Save As...");
        saveTestAsMenuItem.addActionListener(new SaveTestAction(frame, true));
        fileMenu.add(saveTestAsMenuItem);
        
        fileMenu.addSeparator();
        
        switchWorkspaceMenuItem = new JMenuItem("Switch Workspace...");
        switchWorkspaceMenuItem.addActionListener(new SwitchWorkspaceAction(frame));
        fileMenu.add(switchWorkspaceMenuItem);
        
        fileMenu.addSeparator();
        
        exitMenuItem = new JMenuItem("Exit");
        exitMenuItem.setMnemonic(KeyEvent.VK_ESCAPE);
        exitMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
        exitMenuItem.addActionListener
        (
            new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                    { frame.dispose(); }
            }
        );
        
        fileMenu.add(exitMenuItem);

        add(fileMenu);

        //**********************************************************************************************
        // Generation menu 
        //**********************************************************************************************
        generationMenu = new JMenu("Generation");
        generationMenu.setMnemonic(KeyEvent.VK_G);
        
        runMenuItem = new JMenuItem("Run");
        runMenuItem.setMnemonic(KeyEvent.VK_X);
        runMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK));
        runMenuItem.addActionListener(new RunAction(frame));
        
        generationMenu.add(runMenuItem);

        stopMenuItem = new JMenuItem("Terminate");
        stopMenuItem.setMnemonic(KeyEvent.VK_C);
        stopMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
        stopMenuItem.addActionListener(new StopAction(frame));

        generationMenu.add(stopMenuItem);
        
        generationMenu.addSeparator();
        
        optionsMenuItem = new JMenuItem("Options");
        optionsMenuItem.setMnemonic(KeyEvent.VK_O);
        optionsMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.ALT_MASK));
        optionsMenuItem.addActionListener(new OptionsAction(frame));

        generationMenu.add(optionsMenuItem);

        settingsMenuItem = new JMenuItem("Settings");
        settingsMenuItem.setMnemonic(KeyEvent.VK_S);
        settingsMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.ALT_MASK));
        settingsMenuItem.addActionListener(new SettingsAction(frame));
        
        generationMenu.add(settingsMenuItem);
        
        add(generationMenu);

        //**********************************************************************************************
        // Window menu 
        //**********************************************************************************************
        windowMenu = new JMenu("Window");
        windowMenu.setMnemonic(KeyEvent.VK_W);

        showSectionMenuItem = new JMenuItem("Show Section");
        showSectionMenuItem.setMnemonic(KeyEvent.VK_S);
        showSectionMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, ActionEvent.CTRL_MASK));
        showSectionMenuItem.addActionListener(new ShowSectionAction(frame));

        windowMenu.add(showSectionMenuItem);
        
        windowMenu.addSeparator();
        
        showTestMenuItem = new JMenuItem("Show Test");
        showTestMenuItem.setMnemonic(KeyEvent.VK_T);
        showTestMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, ActionEvent.CTRL_MASK));
        showTestMenuItem.addActionListener(new ShowTestAction(frame));

        windowMenu.add(showTestMenuItem);
        
        windowMenu.addSeparator();

        showTestSuiteMenuItem = new JMenuItem("Show Workspace");
        showTestSuiteMenuItem.setMnemonic(KeyEvent.VK_W);
        showTestSuiteMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_3, ActionEvent.CTRL_MASK));
        showTestSuiteMenuItem.addActionListener(new ShowTestSuiteAction(frame));
        
        windowMenu.add(showTestSuiteMenuItem);
        
        add(windowMenu);
        
        //**********************************************************************************************
        // Help menu 
        //**********************************************************************************************
        helpMenu = new JMenu("Help");
        helpMenu.setMnemonic(KeyEvent.VK_H);

        helpMenuItem = new JMenuItem("Documentation");
        helpMenuItem.setMnemonic(KeyEvent.VK_F1);
        helpMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
        helpMenuItem.addActionListener(new DocAction(frame));

        helpMenu.add(helpMenuItem);
        
        helpMenu.addSeparator();
        
        aboutMenuItem = new JMenuItem("About " + GUI.APPLICATION_NAME);
        aboutMenuItem.setMnemonic(KeyEvent.VK_A);
        aboutMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.ALT_MASK));
        aboutMenuItem.addActionListener
        (
            new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    AboutDialog dialog = new AboutDialog(frame);
                    
                    dialog.setVisible(true);
                }
            }
        );
        
        helpMenu.add(aboutMenuItem);
        
        add(helpMenu);
    }
    
    //**********************************************************************************************
    // File menu 
    //**********************************************************************************************
    public JMenu getFileMenu()                    { return fileMenu; }
    public JMenuItem getNewTestMenuItem()         { return newTestMenuItem; }
    public JMenuItem getOpenTestMenuItem()        { return openTestMenuItem; }
    public JMenuItem getSwitchWorkspaceMenuItem() { return switchWorkspaceMenuItem; }
    public JMenuItem getSaveTestMenuItem()        { return saveTestMenuItem; }
    public JMenuItem getSaveTestAsMenuItem()      { return saveTestAsMenuItem; }
    public JMenuItem getExitMenuItem()            { return exitMenuItem; }
    public JMenu getGenerationMenu()              { return generationMenu; }
    public JMenuItem getRunMenuItem()             { return runMenuItem; }
    public JMenuItem getStopMenuItem()            { return stopMenuItem; }
    public JMenuItem getOptionsMenuItem()         { return optionsMenuItem; }
    public JMenuItem getSettingsMenuItem()        { return settingsMenuItem; }
    public JMenu getHelpMenu()                    { return helpMenu; }
    public JMenuItem getDocMenuItem()             { return helpMenuItem; }
    public JMenuItem getAboutMenuItem()           { return aboutMenuItem; }
    public JMenu getWindowMenu()                  { return windowMenu; }
    public JMenuItem getSectionMenuItem()         { return showSectionMenuItem; }
    public JMenuItem getTestMenuItem()            { return showTestMenuItem; }
    public JMenuItem getTestSuiteMenuItem()       { return showTestSuiteMenuItem; }
}