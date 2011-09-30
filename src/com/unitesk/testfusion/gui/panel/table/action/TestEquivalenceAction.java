/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: TestEquivalenceAction.java,v 1.10 2008/09/01 12:33:20 vorobyev Exp $
 */

package com.unitesk.testfusion.gui.panel.table.action;

import java.util.HashSet;

import javax.swing.JTable;

import com.unitesk.testfusion.core.config.*;
import com.unitesk.testfusion.core.config.walker.ConfigWalker;
import com.unitesk.testfusion.gui.panel.table.TableModel;
import com.unitesk.testfusion.gui.panel.table.TablePanel;
import com.unitesk.testfusion.gui.panel.table.visitor.SetTestForEquivalenceVisitor;

/**
 * @author <a href="mailto:vorobyev@ispras.ru">Dmitry Vorobyev</a>
 */
public class TestEquivalenceAction extends AbstractTableAction
{
	public TestEquivalenceAction(TablePanel panel, TableModel model, JTable table) 
	{
		super(panel, model, table);
	}

	public void executeAction() 
	{
    	int selectedRows[] = table.getSelectedRows();
    	
    	HashSet<String> set = new HashSet<String>();
    	
    	for(int i = 0; i < selectedRows.length; i++)
    	{ 
			// Get model index from sorter 
    		int modelRow = table.getRowSorter().convertRowIndexToModel(selectedRows[i]);
    		
    		Config config = model.getConfigValue(modelRow);
    		
    		if(config instanceof InstructionConfig)
    		{
    			String equivalenceClass = ((InstructionConfig)config).getEquivalenceClass(); 

    			// Add value into Set
    			if(!set.contains(equivalenceClass))
    				{ set.add(equivalenceClass); }
    		}
    		else if(config instanceof GroupConfig)
    		{
    			GroupConfig groupConfig = (GroupConfig)config;
    			
    			Object[] equivalenceClasses = groupConfig.getEquivalenceClasses().toArray();
    			
    			for(int j = 0; j < equivalenceClasses.length; j++)
    			{
        			String equivalenceClass = (String)equivalenceClasses[j]; 

        			// Add value into Set
        			if(!set.contains(equivalenceClass))
        				{ set.add(equivalenceClass); }
    			}
    		}
    	}
    	
    	Config config = panel.getConfig();
    	
    	SetTestForEquivalenceVisitor visitor = new SetTestForEquivalenceVisitor(set);
    	ConfigWalker walker = new ConfigWalker(config, visitor, ConfigWalker.VISIT_ALL);
    	walker.process();

    	updateColumnModel(TableModel.TEST_COLUMN);
    	
    	// Update objects that dependence from column test
    	panel.updateTestDependenceObjects();
	}

	public boolean isEnabledAction() 
	{
		if(!model.hasColumnValue(TableModel.EQUIVALENCE_COLUMN))
			{ return false; }
		
		int selectedRows[] = table.getSelectedRows(); 
		
		for(int i = 0; i < selectedRows.length; i++)
		{
			// Get model index from sorter 
    		int modelRow = table.getRowSorter().convertRowIndexToModel(selectedRows[i]);
    		
    		Config config = model.getConfigValue(modelRow);
    		
    		if(config instanceof GroupConfig)
    		{
    			GroupConfig groupConfig = (GroupConfig)config;
    			
    			return !groupConfig.getEquivalenceClasses().isEmpty();
			}
    		else if(config instanceof InstructionConfig)
    		{
    			InstructionConfig instructionConfig = (InstructionConfig)config;
    			
    			return (instructionConfig.getEquivalenceClass() != null) && 
    			       (!instructionConfig.equals(TableModel.SINGLE_EQUIVALENCE_CLASS));
    		}
		}
		
		return false;
	}

}
