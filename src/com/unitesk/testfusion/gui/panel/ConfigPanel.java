/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: ConfigPanel.java,v 1.23 2009/05/19 11:51:16 vorobyev Exp $
 */

package com.unitesk.testfusion.gui.panel;

import java.awt.*;

import javax.swing.*;

import com.unitesk.testfusion.core.config.Config;
import com.unitesk.testfusion.core.util.Utils;
import com.unitesk.testfusion.gui.*;

/**
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public abstract class ConfigPanel extends JPanel
{
    public static final long serialVersionUID = 0;

    protected JLabel label;
    protected JComponent component;
    protected StatusBar innerStatus;
    protected Config config;
    protected JPanel buttonPanel; 
    protected GUI frame;
    
    public ConfigPanel(GUI frame, String header)
    {
        super(new BorderLayout());
        
        label = new JLabel(header);
        label.setFont(new Font("Arial", Font.BOLD | Font.ITALIC, 16));
        label.setHorizontalAlignment(JLabel.CENTER);
        
        JPanel headerPanel = new JPanel();
        headerPanel.add(label);
        
        add(headerPanel, BorderLayout.NORTH);
        
        component = createCustomComponent();
        
        this.frame = frame;
        
        if(component != null)
        {
        	add(component, BorderLayout.CENTER);
        }
        
        setBorder(BorderFactory.createEmptyBorder());         
    }
    
    public ConfigPanel(GUI frame)
    {
        this(frame, "");
    }
    
    public void setHeader(String prefix, Config config)
    {
        String name = config.getName();
        String fullName = config.getFullName();
        
        StringBuffer buffer = new StringBuffer(prefix);
        
        buffer.append(" ");
        buffer.append(name.toUpperCase());

        if(!Utils.isNullOrEmpty(fullName) && !name.equals(fullName))
        {
            buffer.append(" (");
            buffer.append(fullName);
            buffer.append(")");
        }
        
        label.setText(buffer.toString());
    }
    
    public JComponent createCustomComponent()
    {
        return null;
    }
    
    public void update()
    {
    	Workspace workspace = frame.getView().getWorkspace();
    	
    	// Update inner status bar
    	innerStatus = workspace.getInnerStatusBar();
    	innerStatus.update(config);
    	innerStatus.repaint();
    	
    	// Update external status bar
    	StatusBar statusBar = frame.getStatus();
    	statusBar.update(frame.getConfig());
    	statusBar.repaint();
    	
		// Enabling of Run button
    	frame.enableRunAction(statusBar.situation_select > 0);
    }
    
    public void show(Config config)
    {
    	this.config = config;
        
        View view = frame.getView();
        
        if(view != null)
        {
        	// Update inner status bar
        	Workspace workspace = view.getWorkspace();
        	innerStatus = workspace.getInnerStatusBar();
        	innerStatus.update(config);
        	
        	// Update external status bar
        	StatusBar statusBar = frame.getStatus();
        	statusBar.update(frame.getConfig());
        	statusBar.repaint();
        	
    		// Enabling of Run button
        	frame.enableRunAction(statusBar.situation_select > 0);
        }
    }
}