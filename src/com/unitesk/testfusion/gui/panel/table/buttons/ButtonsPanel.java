/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: ButtonsPanel.java,v 1.7 2008/09/13 12:28:33 vorobyev Exp $
 */

package com.unitesk.testfusion.gui.panel.table.buttons;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.HashMap;

import javax.swing.*;

import com.unitesk.testfusion.gui.GUI;
import com.unitesk.testfusion.gui.panel.table.TableModel;
import com.unitesk.testfusion.gui.panel.table.TablePanel;
import com.unitesk.testfusion.gui.panel.table.action.AbstractTableAction;

/**
 * @author <a href="mailto:vorobyev@ispras.ru">Dmitry Vorobyev</a>
 */
public abstract class ButtonsPanel extends JPanel implements ActionListener
{
	private static final long serialVersionUID = 0L;

	protected HashMap<String, JButton> buttons = new HashMap <String, JButton>();
	
	protected HashMap<String, AbstractTableAction> actions = new HashMap<String, AbstractTableAction>();
	
    protected JTable table;
    protected TablePanel panel;
    protected TableModel model;
    protected GUI frame;

	public abstract void locateButtons();
    public abstract void registerButtons();

    public static void setMaxPreferredButtonSize(Collection<JButton> collection)
    {
    	Object buttonObjects[] = (collection.toArray());
    	
    	Dimension max;
    	int i;
    	
    	for(i = 1, max = ((JButton)buttonObjects[0]).getPreferredSize(); i < buttonObjects.length; i++)
    	{
    		Dimension size = ((JButton)buttonObjects[i]).getPreferredSize();
    		
    		if(size.getWidth() >  max.getWidth() && size.getWidth() >= max.getHeight()) 
    			{ max = size; } 
    	}

    	for(int j = 0; j < buttonObjects.length; j++)
    		{ ((JButton)buttonObjects[j]).setPreferredSize(max); }
    }
    
    public ButtonsPanel()
    {
    }
    
	public ButtonsPanel(TablePanel panel)
	{
		setParams(panel);
	}
	
	public void setParams(TablePanel panel)
	{
		this.panel = panel;
		
		this.table = panel.getTable();
		this.model = panel.getModel(); 
		this.frame = panel.getFrame();
		
		// Register buttons and correspond actions
		registerButtons();
		
        // Set buttons sizes
        setMaxPreferredButtonSize(buttons.values());

        // Locate buttons into panel
        locateButtons();
        
        // Add Listeners for buttons
        addListeners();
     
        // Set activation of buttons
        setActivationOfButtons();
	}
	
	protected void registerButton(String name, AbstractTableAction action)
	{
		JButton button = new JButton(name);
		
		buttons.put(name, button);
		actions.put(name, action);
	}
	
	public void addListeners()
	{
		Object names[] = buttons.keySet().toArray();
		 
		// Set action listeners for all buttons
		for(int i = 0; i < names.length; i++)
		{
			String buttonName = (String)names[i];
			
			// Get Button
			JButton button = buttons.get(buttonName);

			button.addActionListener(this);
		}
	}
	
	public void setActivationOfButtons()
	{
		// Get names  
		Object names[] = buttons.keySet().toArray();
		 
		for(int i = 0; i < names.length; i++)
		{
			String buttonName = (String)names[i];
			
			// Get Button
			JButton button = buttons.get(buttonName);
			
			// Get Action
			AbstractTableAction action = actions.get(buttonName);
			
			boolean isEnabled = (action != null) && action.isEnabledAction();
			
			// Set button enabling
			button.setEnabled(isEnabled);
		}
	}
	
	public JButton getButton(String name)
	{
		return buttons.get(name);
	}

    // Action Handler
	public void actionPerformed(ActionEvent event) 
	{
		String actionCommand = event.getActionCommand();
		AbstractTableAction action = actions.get(actionCommand);
		action.executeAndUpdate();
	}
}