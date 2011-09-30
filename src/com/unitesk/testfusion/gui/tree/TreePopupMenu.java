/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: TreePopupMenu.java,v 1.2 2008/08/19 13:51:34 kozlov Exp $
 */

package com.unitesk.testfusion.gui.tree;

import java.awt.event.*;
import java.util.HashMap;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import com.unitesk.testfusion.gui.GUI;

/**
 * @author <a href="mailto:protsenko@ispras.ru">Alexander Protsenko</a>
 * @author <a href="mailto:kozlov@ispras.ru">Kirill Kozlov</a>
 */
public abstract class TreePopupMenu extends JPopupMenu implements MouseListener
{
	private static final long serialVersionUID = 0L;

    protected GUI frame;
    
	protected JMenuItem menuItems[];
	protected int itemsCount;
	
	// Key = Menu Name, Value = Action Name
	protected HashMap<String, String> itemNamesHash = 
        new HashMap<String, String>();
	
	protected TreePopupMenu(GUI frame) 
	{
		this.frame = frame;
		this.itemsCount = 0;
    }
    
	protected void setActivationOfItems()
	{
		// Menu items available when correspond actions are available
		for(int i = 0; i < menuItems.length; i++)
		{
			JMenuItem item = menuItems[i]; 
			
			item.setEnabled(true);
		}
	}
    
	public JMenuItem getMenuItem(String name)
	{
		for(int i = 0; i < menuItems.length; i++)
		{
			String actionName;
			
			if(itemNamesHash.containsKey(name))
				{ actionName = itemNamesHash.get(name);	}
			else
            {
				throw new IllegalStateException(
                        "item " + name + " is not registered");
            }
			
			if(menuItems[i].getText().equals(actionName))
				{ return menuItems[i]; }
		}
		
		throw new IllegalArgumentException("Illegal menu item: " + name);
	}

	protected void registerItem(String actionName, JMenuItem item,
            boolean isSeparate)
	{
		registerItem(actionName, actionName, item, isSeparate);
	}
	
	protected void registerItem(String alternateName, String actionName,
            JMenuItem item, boolean isSeparate)
	{
		// Put alternate Action Name for Menu
		itemNamesHash.put(alternateName, actionName);
		
		// Create item for menu
		item = new JMenuItem(alternateName);

        item.addMouseListener(this);

		// Set items into array
		menuItems[itemsCount] = item;

		// Increment count of items in menu
		itemsCount++;
		
		add(item);
		
		if(isSeparate) 
			{ addSeparator(); }
	}
    
    protected abstract void executeAction(String action); 

    //***************************************************************
    // Mouse Event handlers
    //***************************************************************
    
    public void mousePressed(MouseEvent mouseEvent)
    {
        int button = mouseEvent.getButton();

        if((button == MouseEvent.BUTTON1))
        {    
            JMenuItem jMenuItem = (JMenuItem)mouseEvent.getComponent();
            
            String command = jMenuItem.getText();
            
            if(itemNamesHash.containsKey(command) )
            {
                setVisible(false);
                executeAction(itemNamesHash.get(command));
            }
            else
            {
                throw new IllegalStateException(
                        "item " + command + " is not registered");
            }
                
        }        
    }
    
    public void mouseClicked(MouseEvent e) {}
    
    public void actionPerformed(ActionEvent e) {} 

    public void mouseReleased(MouseEvent arg0) {}

    public void mouseEntered(MouseEvent arg0) {}

    public void mouseExited(MouseEvent arg0) {}
}
