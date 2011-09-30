/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: CustomizeCrossDependencyDialog.java,v 1.1 2008/12/09 12:37:40 kozlov Exp $
 */

package com.unitesk.testfusion.gui.dialog.options;

import java.awt.event.*;

import javax.swing.*;

import com.unitesk.testfusion.gui.GUI;
import com.unitesk.testfusion.gui.dialog.Dialog;
import com.unitesk.testfusion.gui.dialog.options.panel.*;

import com.unitesk.testfusion.core.config.*;

/**
 * Class for options dialog.
 * 
 * @author <a href="mailto:kozlov@ispras.ru">Kirill Kozlov</a>
 */
public class CustomizeCrossDependencyDialog extends Dialog 
{
    public static final long serialVersionUID = 0;
    
    /*
     * Names of options tab panels.
     */
    protected static final String REGISTER_DEP_TAB = "Register Dependencies";
    protected static final String CUSTOM_DEP_TAB = "Custom Dependencies";
    
    /** GUI frame. */
    protected GUI frame;
    
    protected CrossDependencyConfig cross;
    
    protected DependencyListConfig deps;
    
    /** Tabbed panel, the main element of options dialog. */
    protected JTabbedPane tabPanel;
    
    /*
     * Options tab panels.
     */
    protected RegisterDependenciesPanel registerDepPanel;
    protected CustomDependenciesPanel   customDepPanel;
    
    /**
     * Constructor.
     * 
     * @param <code>frame</code> the GUI frame.
     * 
     * @param <code>config</code> the configuration, whose options are used
     *        in dialog.
     */
    public CustomizeCrossDependencyDialog(final GUI frame, 
            final CrossDependencyConfig config)
    {
        super(frame);
        
        final CustomizeCrossDependencyDialog dialog = this;
        
        this.frame = frame;
        this.cross = config;
        this.deps = cross.getDependencies().clone();
        
        // ititialize common parameters
        setTitle(GUI.APPLICATION_NAME + " - Cross Dependency Customize");
        setSize(WIDTH, HEIGHT);
        setResizable(false);
        setModal(true);
        setLocation();
        
        // create listeners
        ActionListener cancelListener = new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                dialog.dispose();
            }
        };
        
        ActionListener okListener = new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                registerDepPanel.saveRegisterDependenciesTab();
                customDepPanel.saveCustomDependenciesTab();
                
                cross.setDependencies(deps);

                okPressed = true;
                dialog.dispose();
            }
        };

        // create tab panel
        tabPanel = new JTabbedPane(JTabbedPane.TOP,
                JTabbedPane.SCROLL_TAB_LAYOUT);
        
        tabPanel.addTab(REGISTER_DEP_TAB, 
                registerDepPanel = new RegisterDependenciesPanel(deps));
        tabPanel.addTab(CUSTOM_DEP_TAB, 
                customDepPanel = new CustomDependenciesPanel(deps));

        add(createDialogMainPanel(tabPanel, okListener, cancelListener));
    }
    
    //*************************************************************************    
    // Getters
    //*************************************************************************

    public DependencyListConfig getCurrentDependencies()
    {
        return deps;
    }
}