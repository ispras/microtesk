/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: Workspace.java,v 1.13 2008/08/08 10:51:35 kamkin Exp $
 */

package com.unitesk.testfusion.gui;

import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.*;

/**
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class Workspace extends JPanel
{
    public static final long serialVersionUID = 0;

    protected Panel panel;
    protected Console console;
    protected StatusBar status;
    protected JPanel buttonPanel;
    
    public Workspace(final GUI frame)
    {
        super(new GridLayout(1, 1));

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        
        buttonPanel = new JPanel();
        
        panel = new Panel(frame);
        JScrollPane panelView = new JScrollPane(panel);

        console = new Console(frame);
        status = new StatusBar(frame);

        JPanel topPanel = new JPanel();
        BoxLayout topLayout = new BoxLayout(topPanel, BoxLayout.Y_AXIS);
        topPanel.setLayout(topLayout);
        
        topPanel.add(panelView);
        topPanel.add(status);
        
        Dimension statusSize = new Dimension(status.getMaximumSize().width, status.getMinimumSize().height);
        
        status.setMaximumSize(statusSize);
        
        splitPane.setTopComponent(topPanel);
        splitPane.setBottomComponent(console);

        splitPane.setDividerLocation(600);
        splitPane.setBorder(BorderFactory.createEmptyBorder());

        add(splitPane);
        
        setBorder(BorderFactory.createEmptyBorder());         
    }
    
    public Panel getPanel()
    {
        return panel;
    }
    
    public Console getConsole()
    {
        return console;
    }
    
    public StatusBar getInnerStatusBar()
    {
    	return status;
    }
    
    public JPanel getButtonPanel()
    {
    	return buttonPanel;
    }
}
