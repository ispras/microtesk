/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: TestSuitePanel.java,v 1.22 2008/08/29 11:27:55 vorobyev Exp $
 */

package com.unitesk.testfusion.gui.panel;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.HashMap;

import javax.swing.*;

import com.unitesk.testfusion.core.config.*;
import com.unitesk.testfusion.gui.GUI;
import com.unitesk.testfusion.gui.action.SwitchWorkspaceAction;
import com.unitesk.testfusion.gui.panel.table.*;
import com.unitesk.testfusion.gui.panel.table.buttons.TestSuiteButtonsPanel;
import com.unitesk.testfusion.gui.panel.table.listener.TableSuitePanelModelListener;
import com.unitesk.testfusion.gui.panel.table.menu.TestSuiteRightClickMenu;

/**
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 * @author <a href="mailto:vorobyev@ispras.ru">Dmitry Vorobyev</a>
 */
public class TestSuitePanel extends TablePanel
{
    public static final long serialVersionUID = 0;
    
    public TestSuitePanel(GUI frame)
    {
        super(frame);
        
        // Set columns 
		HashMap<Integer, Integer> columnHash = new HashMap<Integer, Integer>();
		columnHash.put(0, TableModel.NAME_COLUMN);
		columnHash.put(1, TableModel.DESCRIPTION_COLUMN);
		setColumnHash(columnHash);
		
		// Set column names
		HashMap<Integer, String> columnNamesHash = new HashMap<Integer, String>();
	    columnNamesHash.put(TableModel.NAME_COLUMN, "Test");
	    columnNamesHash.put(TableModel.DESCRIPTION_COLUMN, "Description");
		setColumnNamesHash(columnNamesHash);
		
		// Handling of F5 Key
		ActionMap actionMap = getActionMap();
		InputMap inputMap = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), "released");
		actionMap.put("released", new AbstractAction()
		{
			private static final long serialVersionUID = 0L;

			public void actionPerformed(ActionEvent ignored)
            	{ SwitchWorkspaceAction.updateSelectedWorkspace(panel.getFrame()); }
		});
    } 
    
    public void show(TestSuiteConfig testSuite)
    {
        // Register Button Panel
        buttonPanel.registerElement(new TestSuiteButtonsPanel());
        
        // Register menu
        menu.registerElement(new TestSuiteRightClickMenu());

        tableModelListener.registerElement(new TableSuitePanelModelListener() );

        SwitchWorkspaceAction.updateWorkspaceConfiguration(panel.getFrame());
        
    	super.show(testSuite);
        setHeader("Workspace", testSuite);
    }
    
	public boolean isCellEditable(int row, int col)
    {
		return (col != model.getColumnKey(TableModel.DESCRIPTION_COLUMN));
    }
    
    public void show(Config config)
    {
        show((TestSuiteConfig)config);
    }
}