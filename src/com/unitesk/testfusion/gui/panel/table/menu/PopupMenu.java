/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: PopupMenu.java,v 1.5 2008/08/28 10:32:09 vorobyev Exp $
 */

package com.unitesk.testfusion.gui.panel.table.menu;

import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import com.unitesk.testfusion.gui.panel.table.*;
import com.unitesk.testfusion.gui.panel.table.action.AbstractTableAction;

/**
 * @author <a href="mailto:vorobyev@ispras.ru">Dmitry Vorobyev</a>
 */
public abstract class PopupMenu extends JPopupMenu implements MouseListener
{
	private static final long serialVersionUID = 0L;

	public HashMap<String, JMenuItem> menuItems = new HashMap<String, JMenuItem>();
	public HashMap<String, AbstractTableAction> menuActions = new HashMap<String, AbstractTableAction>();
	
	protected TablePanel panel;
	protected TableModel model;
	protected JTable table;

	public PopupMenu()
	{
	}
	
	public void registerParams(TablePanel panel)
	{
		this.panel = panel;
		this.model = panel.getModel();
		this.table = panel.getTable();
	}
	
	public void setActivationOfItems()
	{
		Iterator<String> iterator = menuItems.keySet().iterator();
		
		while(iterator.hasNext())
		{
			String itemName = iterator.next();

			JMenuItem item = menuItems.get(itemName);
			AbstractTableAction action = menuActions.get(itemName);
			
			item.setEnabled(action.isEnabledAction());
		}
	}
    
	public void registerItem(String itemName, AbstractTableAction itemAction)
	{
		// Create new menu item
		JMenuItem item = new JMenuItem(itemName);
		
		item.addMouseListener(this);
		
		menuItems.put(itemName, item);
		
		// Put action for menu item
		menuActions.put(itemName, itemAction);
		
		add(item);
	}
	
	public void registerSeparator()
	{
		addSeparator();
	}
	
	//**********************************************************************************************
    // Mouse Event handlers
    //**********************************************************************************************
	public void mouseClicked(MouseEvent arg0)
    {
    }

    public void mousePressed(MouseEvent mouseEvent)
    {
        int button = mouseEvent.getButton();

        if((button == MouseEvent.BUTTON1))
        {    
            JMenuItem jMenuItem = (JMenuItem)mouseEvent.getComponent();
            
            String actionCommand = jMenuItem.getText();
            if(!menuActions.containsKey(actionCommand))
            	{ throw new IllegalStateException("Action for " + actionCommand + " is not registered"); }
            
            AbstractTableAction action = menuActions.get(actionCommand);
            
            // Set visibility to false
            setVisible(false);

            action.executeAndUpdate();
        }
    }

    public void mouseReleased(MouseEvent arg0)
    {
    }

    public void mouseEntered(MouseEvent arg0)
    {
    }

    public void mouseExited(MouseEvent arg0)
    {
    }
}
