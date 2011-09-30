/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: DownSectionAction.java,v 1.6 2008/09/01 12:33:20 vorobyev Exp $
 */

package com.unitesk.testfusion.gui.panel.table.action;

import java.util.*;

import javax.swing.JTable;

import com.unitesk.testfusion.core.config.*;
import com.unitesk.testfusion.gui.action.MoveSectionAction;
import com.unitesk.testfusion.gui.panel.table.*;

/**
 * @author <a href="mailto:vorobyev@ispras.ru">Dmitry Vorobyev</a>
 */
public class DownSectionAction extends AbstractTableAction
{
	public DownSectionAction(TablePanel panel, TableModel model, JTable table) 
	{
		super(panel, model, table);
	}

	public void executeAction() 
	{
		//TODO: use TestTree!
        Config config = panel.getConfig();
		
        if(!(config instanceof SectionListConfig))
        	{ throw new IllegalStateException("config is not instanceof SectionListConfig"); }
        
        int selectedRows[] = table.getSelectedRows();
        int sortedRows[] = new int[selectedRows.length];

        HashSet<Config> rowConfigs = new HashSet<Config>();
        
        // Get Real indexes
        for(int i = 0; i < selectedRows.length; i++)
        {
        	int upNumber;
        	
        	// Sorter is available
        	if(table.getRowSorter() != null)
        		{ upNumber = table.getRowSorter().convertRowIndexToModel(selectedRows[i]); }
        	else
        		{ upNumber = selectedRows[i]; }
        	
   			sortedRows[i] = upNumber;
   			
   			rowConfigs.add(model.getConfigValue(upNumber));
        }	

        // Sort real indexes
        Arrays.sort(sortedRows);

        // Making down
        for(int j = sortedRows.length - 1; j >= 0 ;j--)
        {
        	int row = sortedRows[j];

        	if(row != (table.getRowCount() - 1))
        	{
        		// Change rows
        		SectionConfig firstRowConfig = (SectionConfig)panel.getModel().getConfigValue(row);
        		SectionConfig secondRowConfig = (SectionConfig)panel.getModel().getConfigValue(row + 1);
        			
        		// Change places of configurations
        		MoveSectionAction actionUp = new MoveSectionAction(panel.getFrame(), firstRowConfig, row + 1);
        		MoveSectionAction actionDown = new MoveSectionAction(panel.getFrame(), secondRowConfig, row);
        		
        		// Change rows data
        		actionDown.execute();
        		actionUp.execute();
        	}
        }
        
        // Update model
        panel.getModel().updateModel(config);
        
        panel.repaint();
        panel.revalidate();
        
        // Select downed rows
        for(int k = 0; k < model.getRowCount(); k++)
        {
        	Config rowConfig = model.getConfigValue(k); 
        	
        	if(rowConfigs.contains(rowConfig))
        		{ table.getSelectionModel().addSelectionInterval(k, k);	}
        }
	}
	
	public boolean isEnabledAction()
	{
		return super.isEnabledAction() && (table.getSelectedRowCount() > 0);
	}
}