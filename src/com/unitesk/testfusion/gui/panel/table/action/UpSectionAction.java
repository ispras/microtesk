/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: UpSectionAction.java,v 1.6 2008/09/01 12:33:20 vorobyev Exp $
 */

package com.unitesk.testfusion.gui.panel.table.action;

import java.util.*;

import javax.swing.JTable;

import com.unitesk.testfusion.core.config.*;
import com.unitesk.testfusion.gui.action.MoveSectionAction;
import com.unitesk.testfusion.gui.panel.table.*;
import com.unitesk.testfusion.gui.tree.test.TestTree;

/**
 * @author <a href="mailto:vorobyev@ispras.ru">Dmitry Vorobyev</a>
 */
public class UpSectionAction extends AbstractTableAction
{
	public static void changeRows(int firstRow, int secondRow, SectionListConfig config, TablePanel panel)
	{
		SectionConfig firstConfig = config.getSection(firstRow);
		SectionConfig secondConfig = config.getSection(secondRow);

		int firstIndex = config.getIndex(firstConfig);
		int secondIndex = config.getIndex(secondConfig);
		
		// Remove config
		config.removeSection(firstConfig);
		// Register section
		config.registerSection(secondIndex, firstConfig);
		
		config.removeSection(secondConfig);
		config.registerSection(firstIndex, secondConfig);
		
		panel.getModel().updateModel(config);
		
		TestTree tree = panel.getFrame().getTestTree();
		
		tree.update();
	}

	public UpSectionAction(TablePanel panel, TableModel model, JTable table) 
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

        // Making up
        for(int j = 0; j < sortedRows.length; j++)
        {
        	int row = sortedRows[j];

        	if(row != 0)
        	{
        		// Change rows
        		SectionConfig firstRowConfig = (SectionConfig)panel.getModel().getConfigValue(row);
        		SectionConfig secondRowConfig = (SectionConfig)panel.getModel().getConfigValue(row - 1);
        			
        		// Change places of configurations
        		MoveSectionAction actionUp = new MoveSectionAction(panel.getFrame(), firstRowConfig, row - 1);
        		MoveSectionAction actionDown = new MoveSectionAction(panel.getFrame(), secondRowConfig, row);
        		
        		// Change rows data
        		actionUp.execute();
        		actionDown.execute();
        	}
        }

        // Update model
        panel.getModel().updateModel(config);
        
        panel.repaint();
        panel.revalidate();

        // Select upped rows
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