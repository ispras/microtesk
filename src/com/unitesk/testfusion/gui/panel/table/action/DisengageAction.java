/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: DisengageAction.java,v 1.10 2008/09/01 12:33:20 vorobyev Exp $
 */

package com.unitesk.testfusion.gui.panel.table.action;

import javax.swing.JTable;
import javax.swing.event.ListSelectionListener;

import com.unitesk.testfusion.core.config.Config;
import com.unitesk.testfusion.core.config.GroupConfig;
import com.unitesk.testfusion.core.config.ProcessorConfig;
import com.unitesk.testfusion.gui.panel.table.TableModel;
import com.unitesk.testfusion.gui.panel.table.TablePanel;
import com.unitesk.testfusion.gui.panel.table.utils.SelectionUtils;

/**
 * @author <a href="mailto:vorobyev@ispras.ru">Dmitry Vorobyev</a>
 */
public class DisengageAction extends AbstractTableAction
{
	public DisengageAction(TablePanel panel, TableModel model, JTable table) 
	{
		super(panel, model, table);
	}

	public void executeAction() 
	{
		// Disable List Selection Listeners 
		panel.enableListeners(false, ListSelectionListener.class);
		
		for(int i = 0; i < model.getRowCount(); i++)
		{
			model.setEquivalenceClass(null); 
		}
		
		updateColumnConfig(TableModel.EQUIVALENCE_COLUMN);
		
		// Enable List Selection Listeners 
		panel.enableListeners(true, ListSelectionListener.class);
	}

	public boolean isEnabledAction() 
	{
		Config config = panel.getConfig();
		
		return !SelectionUtils.isAllSelectedRowsEmpty(model) &&
		       ( ((config instanceof ProcessorConfig) || (config instanceof GroupConfig)) && 
				   table.getSelectedRowCount() > 0	&& !panel.isEditingByTextField() );
	}
}
