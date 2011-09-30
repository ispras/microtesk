/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: EquivalenceUtils.java,v 1.2 2008/08/12 11:05:44 vorobyev Exp $
 */

package com.unitesk.testfusion.gui.panel.table.utils;

import javax.swing.JTable;

import com.unitesk.testfusion.core.config.*;
import com.unitesk.testfusion.core.config.walker.ConfigWalker;
import com.unitesk.testfusion.gui.panel.table.TableModel;
import com.unitesk.testfusion.gui.panel.table.visitor.InstructionsEquivalenceVisitor;

/**
 * @author <a href="mailto:vorobyev@ispras.ru">Dmitry Vorobyev</a>
 */
public class EquivalenceUtils 
{
	protected InstructionEquivalence instructionEquivalence;
	
	public EquivalenceUtils(TableModel model, JTable table)
	{
		instructionEquivalence = new InstructionEquivalence(model, table);  
	}
	
	public InstructionEquivalence getInstructionEquivalence()
	{
		return instructionEquivalence;
	}
	
	public class InstructionEquivalence
	{
		protected TableModel model;
		protected JTable table;
		protected String equivalenceName; 
		protected boolean allInstructionsEqual;
		
		public InstructionEquivalence(TableModel model, JTable table)
		{
			this.model = model;
			this.table = table;
			
			allInstructionsEqual = true;
			
			for(int i = 0; i < table.getSelectedRowCount(); i++)
			{
				int selectedRows[] = table.getSelectedRows();
				
				Config rowConfig = model.getConfigValue(selectedRows[i]);
				
				if(i == 0)
				{
					if(rowConfig instanceof InstructionConfig)
					{
						equivalenceName = ((InstructionConfig)rowConfig).getEquivalenceClass();
					}
					else if(rowConfig instanceof GroupConfig)
					{
						Object classNames[] = ((GroupConfig)rowConfig).getEquivalenceClasses().toArray();
						
						if(classNames.length != 1)
							{ allInstructionsEqual = false; return; }
						
						equivalenceName = (String)classNames[0];
					}
					else
					    { allInstructionsEqual = false; return; }
				}
				
				InstructionsEquivalenceVisitor visitor = new InstructionsEquivalenceVisitor(equivalenceName);
				ConfigWalker walker = new ConfigWalker(rowConfig, visitor, ConfigWalker.VISIT_ALL);
				walker.process();
				
				allInstructionsEqual &= visitor.isEquivalence();
				
				if(!allInstructionsEqual)
				{
					equivalenceName = null;
					
					return; 
				}
			}
		}
		
		public boolean isAllInstructionsEqual()
		{
			return allInstructionsEqual;
		}
		
		public String getEquivalenceName()
		{
			return equivalenceName;
		}
	}
}
