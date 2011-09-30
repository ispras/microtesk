/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: DefaultTablePanelModelListener.java,v 1.2 2008/08/26 12:32:19 vorobyev Exp $
 */

package com.unitesk.testfusion.gui.panel.table.listener;

import javax.swing.event.*;

import com.unitesk.testfusion.core.config.*;
import com.unitesk.testfusion.core.config.walker.ConfigWalker;
import com.unitesk.testfusion.gui.panel.table.*;
import com.unitesk.testfusion.gui.panel.table.visitor.SetEquivalenceVisitor;

/**
 * @author <a href="mailto:vorobyev@ispras.ru">Dmitry Vorobyev</a>
 */
public class DefaultTablePanelModelListener extends TablePanelModelListener
{ 
	public void tableChanged(TableModelEvent e) 
	{
		TableModel model = panel.getModel();

		int row = e.getFirstRow();
        int column = e.getColumn();
        
        Object data = model.getValueAt(row, column);
        
        // Update Equivalence column if it is exists
    	if(model.hasColumnValue(TableModel.EQUIVALENCE_COLUMN) )
    	{
        	// Textfield Equivalence class action handler
	        if(column == model.getColumnKey(TableModel.EQUIVALENCE_COLUMN))
	        {
	            Config rowConfig = model.getConfigValue(model.getRow(row)); 
	            
	            if(rowConfig instanceof GroupConfig)
	            {
	            	GroupConfig groupConfig = (GroupConfig)rowConfig;
	            	
	            	//if(groupConfig.getEquivalenceClasses().isEmpty())
	            	{
						SetEquivalenceVisitor visitor = new SetEquivalenceVisitor((String)data);
						ConfigWalker walker = new ConfigWalker(groupConfig, visitor, ConfigWalker.VISIT_ALL);
						walker.process();
	            	}
	            }
	            else if(rowConfig instanceof InstructionConfig)
	            {
					SetEquivalenceVisitor visitor = new SetEquivalenceVisitor((String)data);
					ConfigWalker walker = new ConfigWalker(rowConfig, visitor, ConfigWalker.VISIT_ALL);
					walker.process();
	            }
	        }
    	}
	}
}
