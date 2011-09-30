/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: TestButtonsPanel.java,v 1.7 2008/08/20 14:50:24 vorobyev Exp $
 */

package com.unitesk.testfusion.gui.panel.table.buttons;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JPanel;

import com.unitesk.testfusion.gui.panel.table.TablePanel;
import com.unitesk.testfusion.gui.panel.table.action.*;

/**
 * @author <a href="mailto:vorobyev@ispras.ru">Dmitry Vorobyev</a>
 */
public class TestButtonsPanel extends ButtonsPanel
{
	private static final long serialVersionUID = 0L;
	
    protected JPanel buttonPanel1;
    protected JPanel buttonPanel2;
    protected JPanel buttonPanel3;
	
    public TestButtonsPanel()
    {
    	super();
    }
    
	public TestButtonsPanel(TablePanel panel) 
	{
		super(panel);
	}
	
	public void registerButtons() 
	{
		registerButton(TableAction.NEW_SECTION_ACTION, new AddSectionTableAction(panel, model, table));
		registerButton(TableAction.REMOVE_SECTION_ACTION, new RemoveSectionTableAction(panel, model, table));
		
		registerButton(TableAction.UP_ACTION, new UpSectionAction(panel, model, table));
		registerButton(TableAction.DOWN_ACTION, new DownSectionAction(panel, model, table));
		
		registerButton(TableAction.SELECT_ALL_ACTION, new SelectAllAction(panel, model, table));
		registerButton(TableAction.CLEAR_ALL_ACTION,  new ClearAllAction(panel, model, table));
	}
	
	public void locateButtons() 
	{
        GridBagLayout layout = new GridBagLayout();
        setLayout(layout);
        
        GridBagConstraints c = new GridBagConstraints();

        buttonPanel1 = new JPanel();
        
        buttonPanel1.add(getButton(TableAction.NEW_SECTION_ACTION));
        buttonPanel1.add(getButton(TableAction.REMOVE_SECTION_ACTION));
        
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.1;
        c.weighty = 1.0;
        layout.setConstraints(buttonPanel1, c);
        add(buttonPanel1);
        
        buttonPanel2 = new JPanel();
        buttonPanel2.add(getButton(TableAction.UP_ACTION));
        buttonPanel2.add(getButton(TableAction.DOWN_ACTION));

        c.gridx = 1;
        c.gridy = 0;
        c.anchor = GridBagConstraints.NORTH;
        c.fill = GridBagConstraints.HORIZONTAL;
        layout.setConstraints(buttonPanel2, c);
        add(buttonPanel2);
        
        buttonPanel3 = new JPanel();
        buttonPanel3.add(getButton(TableAction.SELECT_ALL_ACTION));
        buttonPanel3.add(getButton(TableAction.CLEAR_ALL_ACTION));

        c.gridx = 2;
        c.gridy = 0;
        c.anchor = GridBagConstraints.NORTHEAST;
        c.fill = GridBagConstraints.HORIZONTAL;
        layout.setConstraints(buttonPanel3, c);
        add(buttonPanel3);
	}
}