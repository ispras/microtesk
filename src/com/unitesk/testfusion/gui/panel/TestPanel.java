/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: TestPanel.java,v 1.16 2008/08/28 10:31:58 vorobyev Exp $
 */

package com.unitesk.testfusion.gui.panel;

import java.util.HashMap;

import com.unitesk.testfusion.core.config.*;
import com.unitesk.testfusion.gui.GUI;
import com.unitesk.testfusion.gui.panel.table.*;
import com.unitesk.testfusion.gui.panel.table.buttons.TestButtonsPanel;
import com.unitesk.testfusion.gui.panel.table.menu.TestRightClickMenu;

/**
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class TestPanel extends TablePanel
{
    public static final long serialVersionUID = 0;
    
    public TestPanel(GUI frame)
    {
        super(frame);
        
        // Set columns for Test panel 
		HashMap<Integer, Integer> columnHash = new HashMap<Integer, Integer>();
		columnHash.put(0, TableModel.NAME_COLUMN);
		columnHash.put(1, TableModel.INSTRUCTIONS_COLUMN);
		columnHash.put(2, TableModel.SITUATIONS_COLUMN);
		setColumnHash(columnHash);
		
		// Set column names
		HashMap<Integer, String> columnNamesHash = new HashMap<Integer, String>();
		columnNamesHash.put(TableModel.NAME_COLUMN, "Test");
	    columnNamesHash.put(TableModel.INSTRUCTIONS_COLUMN, "Instructions");
		columnNamesHash.put(TableModel.SITUATIONS_COLUMN, "Situations");
	    setColumnNamesHash(columnNamesHash);
    }
    
    public void show(TestConfig test)
    {
        // Register Button Panel
        buttonPanel.registerElement(new TestButtonsPanel());
        
        // Register menu
        menu.registerElement(new TestRightClickMenu());

        // Register UI
        tableUI.registerElement(new DragDropRowTableUI(this));
        
    	super.show(test);
        
        setHeader("Test", test);
    }
    
    public void show(Config config)
    {
        show((TestConfig)config);
    }
}