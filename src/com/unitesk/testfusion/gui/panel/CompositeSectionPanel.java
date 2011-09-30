/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: CompositeSectionPanel.java,v 1.8 2009/05/15 16:14:23 vorobyev Exp $
 */

package com.unitesk.testfusion.gui.panel;

import java.util.HashMap;

import com.unitesk.testfusion.core.config.*;
import com.unitesk.testfusion.gui.GUI;
import com.unitesk.testfusion.gui.panel.table.*;
import com.unitesk.testfusion.gui.panel.table.buttons.TestButtonsPanel;
import com.unitesk.testfusion.gui.panel.table.menu.TestRightClickMenu;

/**
 * @author <a href="mailto:vorobyev@ispras.ru">Dmitry Vorobyev</a>
 */
public class CompositeSectionPanel extends TablePanel
{
    public static final long serialVersionUID = 0;

    public CompositeSectionPanel(GUI frame)
    {
        super(frame);
        // Set columns for Composite Section panel 
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
    
    public void show(SectionConfig processor)
    {
    	// Register Button Panel
        buttonPanel.registerElement(new TestButtonsPanel());
        
        // Register menu
        menu.registerElement(new TestRightClickMenu());
    	
    	super.show(processor);
        
        setHeader("Section", processor);
    }
    
    public void show(Config config)
    {
        show((SectionConfig)config);
    }
}