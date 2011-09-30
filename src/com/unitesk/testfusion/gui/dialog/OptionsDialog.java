/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: OptionsDialog.java,v 1.77 2008/12/18 14:01:23 kozlov Exp $
 */

package com.unitesk.testfusion.gui.dialog;


import java.awt.event.*;

import javax.swing.*;

import com.unitesk.testfusion.gui.GUI;
import com.unitesk.testfusion.gui.dialog.options.panel.*;

import com.unitesk.testfusion.core.config.*;

/**
 * Class for options dialog.
 * 
 * @author <a href="mailto:kozlov@ispras.ru">Kirill Kozlov</a>
 */
public class OptionsDialog extends NonEmptyTextFieldDialog 
{
    public static final long serialVersionUID = 0;
    
    /*
     * Names of options tab panels.
     */
    protected static final String GENERAL_OPTIONS_TAB = "General Options"; 
    protected static final String REGISTER_DEP_TAB = "Register Dependencies";
    protected static final String CUSTOM_DEP_TAB = "Custom Dependencies";
    protected static final String CROSS_DEP_TAB = "Cross Dependencies";
    protected static final String TEMPLATE_ITER_TAB = "Template Iterator";
    
    /*
     * Types of options dialog
     */
    protected static final int TEST_CONFIG_TYPE             = 0;
    protected static final int NON_LEAF_SECTION_CONFIG_TYPE = 1;
    protected static final int LEAF_SECTION_CONFIG_TYPE     = 2;
    
    /** GUI frame. */
    protected GUI frame;
    
    /** Configuration, which contains options. */
    protected SectionListConfig sectionListConfig;
    
    /** Local copy of options configuration. */
    protected OptionsConfig currentOptions;
    
    /**
     * Type of options dialog. It can possess one of the following values:
     * 
     * {@link #TEST_CONFIG_TYPE} - options for test's configuration.
     * {@link #NON_LEAF_SECTION_CONFIG_TYPE} - options for non-leaf section's
     *                                         configuration.
     * {@link #LEAF_SECTION_CONFIG_TYPE} - options for leaf section's
     *                                     configuration.                                         
     */
    protected int type;
    
    /** Tabbed panel, the main element of options dialog. */
    protected JTabbedPane tabPanel;
    
    /*
     * Options tab panels.
     */
    protected GeneralOptionsPanel       generalOptionsPanel;
    protected RegisterDependenciesPanel registerDepPanel;
    protected CustomDependenciesPanel   customDepPanel;
    protected TemplateIteratorPanel     templateIteratorPanel;
    protected CrossDependenciesPanel    crossDepPanel;
    
    /**
     * Constructor.
     * 
     * @param <code>frame</code> the GUI frame.
     * 
     * @param <code>config</code> the configuration, whose options are used
     *        in dialog.
     */
    public OptionsDialog(final GUI frame, final SectionListConfig config)
    {
        super(frame);
        
        final OptionsDialog optionsDialog = this;
        
        this.frame = frame;
        this.sectionListConfig = config;
        this.currentOptions = config.getOptions().clone();
        
        // find out type of configuration
        if (config instanceof TestConfig)
        {
            type = TEST_CONFIG_TYPE;
            setTitle(GUI.APPLICATION_NAME + " - Options For Test");
        }
        else if(config instanceof SectionConfig)
        {
            SectionConfig section = (SectionConfig)config;
            setTitle(GUI.APPLICATION_NAME + " - Options For Section " + section.getFullName()); 
            
            if (section.isLeaf())
                { type = LEAF_SECTION_CONFIG_TYPE; }
            else
                { type = NON_LEAF_SECTION_CONFIG_TYPE; }
        }
        
        // ititialize common parameters
        setSize(WIDTH, HEIGHT);
        setResizable(false);
        setModal(true);
        setLocation();
        
        // create listeners
        ActionListener cancelListener = new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                optionsDialog.dispose();
            }
        };
        
        ActionListener okListener = null;
        
        // create tab panel
        tabPanel = new JTabbedPane(JTabbedPane.TOP,
                JTabbedPane.SCROLL_TAB_LAYOUT);
        
        switch (type)
        {
            case TEST_CONFIG_TYPE :
            {
                initTestConfigTabPanel();
                okListener = createTestConfigOkListener(optionsDialog);
                break;
            }
            case NON_LEAF_SECTION_CONFIG_TYPE :
            {
                initNonLeafSectionConfigTabPanel();
                okListener = createNonLeafSectionConfigOkListener(
                        optionsDialog);
                break;
            }
            case LEAF_SECTION_CONFIG_TYPE :
            {
                initLeafSectionConfigTabPanel();
                okListener = createLeafSectionConfigOkListener(optionsDialog);
                break;
            }
        }

        add(createDialogMainPanel(tabPanel, okListener, cancelListener));
    }
    
    //*************************************************************************
    // Methods for initialization tab panel.
    //*************************************************************************
    protected void initTestConfigTabPanel()
    {
        DependencyListConfig deps = currentOptions.getDependencies();
        
        tabPanel.addTab(GENERAL_OPTIONS_TAB, 
                generalOptionsPanel = new GeneralOptionsPanel(frame, this));
        tabPanel.addTab(REGISTER_DEP_TAB, 
                registerDepPanel = new RegisterDependenciesPanel(deps));
        tabPanel.addTab(CUSTOM_DEP_TAB, 
                customDepPanel = new CustomDependenciesPanel(deps));
    }
    
    protected void initNonLeafSectionConfigTabPanel()
    {
        DependencyListConfig deps = currentOptions.getDependencies();
        
        tabPanel.addTab(REGISTER_DEP_TAB, 
                registerDepPanel = new RegisterDependenciesPanel(deps));
        tabPanel.addTab(CUSTOM_DEP_TAB, 
                customDepPanel = new CustomDependenciesPanel(deps));
        tabPanel.addTab(CROSS_DEP_TAB, 
                crossDepPanel = new CrossDependenciesPanel(frame, 
                        (SectionConfig)sectionListConfig, currentOptions));
    }
    
    protected void initLeafSectionConfigTabPanel()
    {
        DependencyListConfig deps = currentOptions.getDependencies();
        
        tabPanel.addTab(TEMPLATE_ITER_TAB, 
                templateIteratorPanel = new TemplateIteratorPanel(frame, this));
        tabPanel.addTab(REGISTER_DEP_TAB, 
                registerDepPanel = new RegisterDependenciesPanel(deps));
        tabPanel.addTab(CUSTOM_DEP_TAB, 
                customDepPanel = new CustomDependenciesPanel(deps));
        tabPanel.addTab(CROSS_DEP_TAB, 
                crossDepPanel = new CrossDependenciesPanel(frame, 
                        (SectionConfig)sectionListConfig, currentOptions));
    }
    
    //*************************************************************************
    // Methods for creating OK listeners.
    //*************************************************************************
    
    /**
     * Returns the listener of OK button for test configuration options dialog.
     * 
     * @param <code>dialog</code> the dialog.
     * 
     * @return the listener of OK button for test configuration options dialog.
     */
    protected ActionListener createTestConfigOkListener(
            final OptionsDialog dialog)
    {
        return new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
            	generalOptionsPanel.saveGeneralOptionsTab();
                registerDepPanel.saveRegisterDependenciesTab();
                customDepPanel.saveCustomDependenciesTab();
                
                sectionListConfig.setOptions(currentOptions);

                okPressed = true;
                dialog.dispose();
            }
        };
    }
    
    /**
     * Returns the listener of OK button for non-leaf section configuration 
     * options dialog.
     * 
     * @param <code>dialog</code> the dialog.
     * 
     * @return the listener of OK button for non-leaf section configuration 
     *         options dialog.
     */
    protected ActionListener createNonLeafSectionConfigOkListener(
            final OptionsDialog dialog)
    {
        return new ActionListener()
        {
        	public void actionPerformed(ActionEvent e)
            {
        		registerDepPanel.saveRegisterDependenciesTab();
                customDepPanel.saveCustomDependenciesTab();
                crossDepPanel.saveCrossDependenciesTab();
                
                sectionListConfig.setOptions(currentOptions);

                okPressed = true;
                dialog.dispose();
            }
        };
    }
    
    /**
     * Returns the listener of OK button for leaf section configuration 
     * options dialog.
     * 
     * @param <code>dialog</code> the dialog.
     * 
     * @return the listener of OK button for leaf section configuration 
     *         options dialog.
     */
    protected ActionListener createLeafSectionConfigOkListener(
            final OptionsDialog dialog)
    {
        return new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                String msg = 
                    templateIteratorPanel.checkParametersCorrectness();
                
                if (!msg.equals("")) 
                {
                    frame.showWarningMessage(msg, "Error");
                }
                else
                {
                    templateIteratorPanel.saveTemplateIteratorTab();
                    registerDepPanel.saveRegisterDependenciesTab();
                    customDepPanel.saveCustomDependenciesTab();
                    crossDepPanel.saveCrossDependenciesTab();
                    
                    sectionListConfig.setOptions(currentOptions);

                    okPressed = true;
                    dialog.dispose();
                }
            }
        };
    }
    
    //*************************************************************************    
    // Getters
    //*************************************************************************

    public OptionsConfig getCurrentOptionsConfig()
    {
    	return currentOptions;
    }
    
    public TemplateIteratorPanel getTemplateIteratorTabPanel()
    {
        return templateIteratorPanel;
    }
}