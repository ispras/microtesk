/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: GUI.java,v 1.87 2009/07/09 14:48:11 kamkin Exp $
 */

package com.unitesk.testfusion.gui;

import java.awt.BorderLayout;

import java.lang.Object;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import javax.swing.JOptionPane; 

import com.unitesk.testfusion.core.config.ProcessorConfig;

import com.unitesk.testfusion.core.config.Config;
import com.unitesk.testfusion.core.config.SectionConfig;
import com.unitesk.testfusion.core.config.SectionListConfig;
import com.unitesk.testfusion.core.config.SelectionConfig;
import com.unitesk.testfusion.core.config.TestConfig;
import com.unitesk.testfusion.core.config.TestSuiteConfig;
import com.unitesk.testfusion.gui.dialog.SectionNameDialog;
import com.unitesk.testfusion.gui.tree.DepthFirstTreeWalker;
import com.unitesk.testfusion.gui.tree.TreeNodeSelector;
import com.unitesk.testfusion.gui.tree.section.SectionTree;
import com.unitesk.testfusion.gui.tree.test.TestTree;

/**
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class GUI extends JFrame
{
    public static final long serialVersionUID = 0;

    public static final String APPLICATION_NAME = "MicroTESK";
    public static final String EXTENSION = "mt";
    
    public static final boolean SYSTEM_LOOK      = true;
    public static final int     DEFAULT_WIDTH    = 800;
    public static final int     DEFAULT_HIGH     = 600;
    public static final int     YES_OPTION       = JOptionPane.YES_OPTION;
    public static final int     NO_OPTION        = JOptionPane.NO_OPTION;
    public static final int     CANCEL_OPTION    = JOptionPane.CANCEL_OPTION;
    public static final int     OK_OPTION        = JOptionPane.OK_OPTION;
    public static final int     CLOSED_OPTION    = JOptionPane.CLOSED_OPTION;
    
    protected MenuBar   menu;
    protected ToolBar   tool;
    protected View      view;
    protected StatusBar status;
    
    protected final TestConfig  defaultConfig;
    protected final GUISettings defaultSettings;
    
    protected TestConfig config;
    
    /** Current selected section. */
    protected SectionConfig currentSection;
    
    protected GUISettings settings;
    protected TestSuiteConfig testSuite;

    protected HistoryManager history; 
    
    /** Shows is current test has changes. */
    protected boolean isTestHasChanges;
    
    public GUI(TestConfig config, GUISettings settings, TestSuiteConfig testSuite)
    {
        defaultConfig = config;
        defaultSettings = settings;
        
        this.config = defaultConfig.clone();
        
        // Remove all sections from default configuration
        while(defaultConfig.countSection() > 0)
        {
            SectionConfig section = defaultConfig.getSection(0);
            defaultConfig.removeSection(section);
        }
        
        this.settings = defaultSettings.clone();
        
        ProcessorConfig processor = getConfig().getProcessor();
        
        if(processor != null && !processor.isEmpty())
        {
            // If current configuration has no sections, add default section.
            if(this.config.countSection() == 0)
                { this.config.registerSection(defaultConfig.createSection("template")); }
        }
        
        currentSection = null;
        
        this.testSuite = testSuite;
        
        history = new HistoryManager(settings.getHistorySize());
        
        isTestHasChanges = false;
        
        ClassLoader loader = getClass().getClassLoader(); 

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        menu = new MenuBar(this);
        setJMenuBar(menu);
        
        tool = new ToolBar(this);
        add(tool, BorderLayout.PAGE_START);
        
        view = new View(this);
        add(view);
        
        status = new StatusBar(this);
        add(status, BorderLayout.PAGE_END);
        
        setResizable(true);
        setSize(DEFAULT_WIDTH, DEFAULT_HIGH);
        
        setIconImage(new ImageIcon(
                loader.getResource("img/chip.gif")).getImage());
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        
        enableBackAction(false);
        enableForwardAction(false);

        // TODO:
        enableRunAction(false);
        enableStopAction(false);
        
        if(testSuite.isUndefined())
        	{ enableOpenTestAction(false); }
        
        if(processor == null || processor.isEmpty())
        {
            enableNewTestAction(false);
            enableOpenTestAction(false);
            enableSwitchWorkspaceAction(false);
            enableSaveTestAction(false);
            enableStopAction(false);
            enableOptionsAction(false);
            enableSettingsAction(false);
            enableShowAction(false);
            
            getTestTree().setEnabled(false);
            getTestTree().setSelectionPath(null);
        }
        else
            { showConfig(this.config.getFirstLeaf()); }
        
        updateTitle();
    }
    
    public GUI(TestConfig config)
    {
        this(config, new GUISettings(), new TestSuiteConfig());
    }
    
    public TestConfig getDefaultConfig()
    {
        return defaultConfig;
    } 
    
    public TestConfig getConfig()
    {
        return config;
    }
    
    public void setConfig(TestConfig config)
    {
        this.config = config;
    }
    
    public TestSuiteConfig getTestSuite()
    {
        return testSuite;
    }
    
    public void setTestSuite(TestSuiteConfig testSuite)
    {
        this.testSuite = testSuite;
    }
    
    /**
     * Returns the current section.
     * 
     * @return the current section.
     */
    public SectionConfig getSection()
    {
        return currentSection;
    }
    
    /**
     * Sets current section.
     * 
     * @param  <code>section</code> the section to be set.
     */
    public void setCurrentSection(SectionConfig section)
    {
        currentSection = section;
    }
    
    public GUISettings getDefaultSettings()
    {
        return defaultSettings;
    }

    public GUISettings getSettings()
    {
        return settings;
    }
    
    public void setSettings(GUISettings settings)
    {
        this.settings = settings;
    }
    
    public HistoryManager getHistory()
    {
        return history;
    }
    
    public void setTestHasChanges(boolean f)
    {
        isTestHasChanges = f;
    }
    
    public void setTestHasChanges()
    {
        isTestHasChanges = true;
    }
    
    public boolean isTestHasChanges()
    {
        return isTestHasChanges; 
    }
    
    //***************************************************************
    // Getters
    //***************************************************************
    
    public MenuBar getMenu()
    {
        return menu;
    }
    
    public ToolBar getTool()
    {
        return tool;
    }
    
    public View getView()
    {
        return view;
    }
    
    public SectionTree getSectionTree()
    {
        return view.getSectionTree();
    }
    
    public TestTree getTestTree()
    {
        return view.getTestTree();
    }
    
    public Panel getPanel()
    {
        return view.getPanel();
    }
    
    public Console getConsole()
    {
        return view.getConsole();
    }
    
    public StatusBar getStatus()
    {
        return status;
    }
    
    //***************************************************************
    // Config Change Handler
    //***************************************************************
    
    public void updateConfig()
    {
        getSectionTree().updateConfig();
        
        // update console size
        getConsole().setMaxSize(getSettings().getConsoleSize());
        
        // update history size
        getHistory().setMaxSize(getSettings().getHistorySize());
    }
    
    //***************************************************************
    // Enable Actions
    //***************************************************************

    public void enableNewTestAction(boolean enabled)
    {
        JButton newButton = tool.getNewButton();
        newButton.setEnabled(enabled);
        
        JMenuItem newTestMenuItem = menu.getNewTestMenuItem();
        newTestMenuItem.setEnabled(enabled);
    }
    
    public void enableOpenTestAction(boolean enabled)
    {
    	JButton openButton = tool.getOpenButton();
        openButton.setEnabled(enabled);
    	
        JMenuItem openTestMenuItem = menu.getOpenTestMenuItem();
        openTestMenuItem.setEnabled(enabled);
    }
    
    public void enableSaveTestAction(boolean enabled)
    {
        JButton saveButton = tool.getSaveButton();
        saveButton.setEnabled(enabled);
        
        JMenuItem saveTestMenuItem = menu.getSaveTestMenuItem();
        saveTestMenuItem.setEnabled(enabled);
        
        JMenuItem saveTestAsMenuItem = menu.getSaveTestAsMenuItem();
        saveTestAsMenuItem.setEnabled(enabled);
    }
    
    public void enableSwitchWorkspaceAction(boolean enabled)
    {
        JMenuItem switchWorkspaceMenuItem = menu.getSwitchWorkspaceMenuItem();
        switchWorkspaceMenuItem.setEnabled(enabled);
    }
    
    public void enableRunAction(boolean enabled)
    {
        JButton runButton = tool.getRunButton();
        runButton.setEnabled(enabled);
        
        JMenuItem runMenuItem = menu.getRunMenuItem();
        runMenuItem.setEnabled(enabled);
    }
    
    public void enableStopAction(boolean enabled)
    {
        JButton stopButton = tool.getStopButton();
        stopButton.setEnabled(enabled);        

        JMenuItem stopMenuItem = menu.getStopMenuItem();
        stopMenuItem.setEnabled(enabled);
    }
    
    public void enableOptionsAction(boolean enabled)
    {
        JButton optionsButton = tool.getOptionsButton();
        optionsButton.setEnabled(enabled);
        
        JMenuItem optionsMenuItem = menu.getOptionsMenuItem();
        optionsMenuItem.setEnabled(enabled);
    }
    
    public void enableSettingsAction(boolean enabled)
    {
        JButton settingsButton = tool.getSettingsButton();
        settingsButton.setEnabled(enabled);
        
        JMenuItem settingsMenuItem = menu.getSettingsMenuItem();
        settingsMenuItem.setEnabled(enabled);
    }
    
    public void enableShowSectionAction(boolean enabled)
    {
        JMenuItem sectionMenuItem = menu.getSectionMenuItem();
        JButton sectionButton = tool.getSectionButton();
        
        sectionMenuItem.setEnabled(enabled);
        sectionButton.setEnabled(enabled);
    }

    public void enableShowAction(boolean enabled)
    {
        JMenuItem sectionMenuItem = menu.getSectionMenuItem();
        JButton sectionButton = tool.getSectionButton();
        sectionMenuItem.setEnabled(enabled);
        sectionButton.setEnabled(enabled);
        
        JMenuItem testMenuItem = menu.getTestMenuItem();
        JButton testButton = tool.getTestButton();
        testMenuItem.setEnabled(enabled);
        testButton.setEnabled(enabled);
        
        JMenuItem testSuiteMenuItem = menu.getTestSuiteMenuItem();
        JButton testSuiteButton = tool.getTestSuiteButton();
        testSuiteMenuItem.setEnabled(enabled);
        testSuiteButton.setEnabled(enabled);
    }
    
    public void enableBackAction(boolean enabled)
    {
        JButton backButton = tool.getBackButton();
        backButton.setEnabled(enabled);
    }
    
    public void enableForwardAction(boolean enabled)
    {
        JButton forwardButton = tool.getForwardButton();
        forwardButton.setEnabled(enabled);
    }
    
    protected void enableLeafSectionRelatedElems(boolean enable)
    {
        enableShowSectionAction(enable);
        
        getSectionTree().setEnabled(enable);
    }
    
    //***************************************************************
    // Show Config Actions
    //***************************************************************
    
    /**
     * Changes GUI when config was selected for showing.
     * 
     *  @param <code>config</code> the selected config.
     */
    public void showConfig(Config config)
    {
        showConfig(config, true);
    }
    
    /**
     * Changes GUI when config was selected for showing.
     * 
     *  @param <code>config</code> the selected config.
     *  
     *  @param <code>addHistory<code> is need to add action to history.
     */
    public void showConfig(Config config, boolean addHistory)
    {
        if (config instanceof TestConfig)
            { showTest((TestConfig)config, addHistory); }
        else if (config instanceof SectionConfig)
            { showSection((SectionConfig)config, addHistory); }
        else if (config instanceof SelectionConfig)
        {
            showSelectionConfig((SelectionConfig)config, addHistory);
        }
        else 
        {
            getPanel().showPanel(config, addHistory);
            getPanel().update();
        }
    }
    
    /**
     * Changes GUI when selection config was selected for showing.
     * 
     *  @param <code>config</code> the selected selection config.
     */
    public void showSelectionConfig(SelectionConfig config, boolean addHistory)
    {
        getPanel().showPanel(config, addHistory);
        getPanel().update();
        
        SectionTree tree = getSectionTree();
        TreeNodeSelector selector = new TreeNodeSelector(config);
        DepthFirstTreeWalker walker = 
            new DepthFirstTreeWalker(tree, selector, false);
        
        walker.process();
    }
    
    /**
     * Changes GUI when test was selected for showing.
     * 
     *  @param <code>test</code> the selected test.
     */
    public void showTest(TestConfig test, boolean addHistory)
    {
        setCurrentSection(null);
        showSectionListConfig(test, addHistory);
        
        enableLeafSectionRelatedElems(false);
        getSectionTree().updateConfig();
    }
    
    /**
     * Changes GUI when some section was selected for showing.
     * 
     *  @param <code>section</code> the selected section.
     */
    public void showSection(SectionConfig section, boolean addHistory)
    {
        setCurrentSection(section);
        showSectionListConfig(section, addHistory);
        
        enableLeafSectionRelatedElems(section.isLeaf());
        
        if (section.isLeaf())
            getSectionTree().selectRoot();
        
        getSectionTree().updateConfig();
    }
    
    /**
     * Selects corresponding node in test tree and show corresponding
     * panel when a section list config was selected in GUI.
     * 
     *  @param <code>config</code> the selected section list config.
     */
    protected void showSectionListConfig(SectionListConfig config,
            boolean addHistory)
    {
        // select corresponding node in test tree
        TestTree testTree = getTestTree();
        TreeNodeSelector selector = new TreeNodeSelector(config);
        DepthFirstTreeWalker walker = 
            new DepthFirstTreeWalker(testTree, selector, true);
        
        walker.process();
        
        // show corresponding panel
        getPanel().showPanel(config, addHistory);
    }
    
    /**
     * Updates the titles of the application.
     * 
     * @param section
     */
    public void updateTitle()
    {
        TestConfig test = getConfig();
        ProcessorConfig processor = test.getProcessor();
        SectionConfig section = getSection();
        
        String testName = test.getName();
        
        String processorName = processor.getName();
        String sectionName = (section == null) ? 
                testName : testName + "." + section.getFullName();
        
        sectionName = (isTestHasChanges) ? sectionName + " *" : sectionName; 
        
        setTitle(GUI.APPLICATION_NAME + " - " + 
                processorName + " Test Program Generator - " + sectionName);
    }
    
    // **************************************************************
    // Show Dialogs Methods
    // **************************************************************
    
    /**
     * Show dialog for set section name.
     * 
     * @param <code>section</code> the section for set name.
     * 
     * @return is dialog was closed by Ok button pressing.
     */
    public boolean showSectionNameDialog(SectionConfig section,
            SectionListConfig parent)
    {
        SectionNameDialog dialog = 
            new SectionNameDialog(this, section, parent);
        
        dialog.setVisible(true);
        
        return dialog.isOkPressed();
    }
    
    public void showWarningMessage(Object message, String title)
    {
    	JOptionPane.showMessageDialog(this, message,
                APPLICATION_NAME + " - " + title,
                JOptionPane.WARNING_MESSAGE);
    }
    
    public void showErrorMessage(Object message, String title)
    {
    	JOptionPane.showMessageDialog(this, message,
                APPLICATION_NAME + " - " + title,
                JOptionPane.ERROR_MESSAGE);
    }
    
    public void showInformationMessage(Object message, String title)
    {
    	JOptionPane.showMessageDialog(this, message,
                APPLICATION_NAME + " - " + title,
                JOptionPane.INFORMATION_MESSAGE);
    }
    
    public int showConfirmYesNoWarningDialog(Object message, String title)
    {
    	return JOptionPane.showConfirmDialog(this, message,
                APPLICATION_NAME + " - " + title, 
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
    }
    
    //***************************************************************
    // Run Application
    //***************************************************************

    public static TestConfig createNewTestConfig()
    {
        return new TestConfig();
    }

    public static void createAndShowGUI(TestConfig config)
    {
        if(GUI.SYSTEM_LOOK)
        {
            try
            {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }

        GUI frame = new GUI(config);
        
        frame.setVisible(true);
    }

    public static void main(String[] args)
    {
        SwingUtilities.invokeLater
        (
            new Runnable()
            {
                public void run() { createAndShowGUI(createNewTestConfig()); }
            }
        );
    }
}