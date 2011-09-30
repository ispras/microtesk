/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: FactorizeAction.java,v 1.15 2008/09/01 12:33:20 vorobyev Exp $
 */

package com.unitesk.testfusion.gui.panel.table.action;

import javax.swing.JTable;
import javax.swing.event.ListSelectionListener;

import com.unitesk.testfusion.core.config.*;
import com.unitesk.testfusion.gui.GUI;
import com.unitesk.testfusion.gui.dialog.EquivalenceClassDialog;
import com.unitesk.testfusion.gui.panel.table.*;
import com.unitesk.testfusion.gui.panel.table.utils.SelectionUtils;

/**
 * @author <a href="mailto:vorobyev@ispras.ru">Dmitry Vorobyev</a>
 */
public class FactorizeAction extends AbstractTableAction 
{
	protected GUI frame;

	public FactorizeAction(GUI frame, TablePanel panel, TableModel model, JTable table) 
	{
		super(panel, model, table);
		this.frame = frame;
	}
	
	public void executeAction() 
	{
		// Disable List Selection Listeners 
		panel.enableListeners(false, ListSelectionListener.class);
		
        if(model.countOfSelectedInstructions() == 0)
        	{ throw new IllegalStateException("Count of selected classes should be even one"); }
        else
        {
			// Create and set up the dialog window.
        	final EquivalenceClassDialog dialogFrame = new EquivalenceClassDialog(frame, model, table);
	       
        	dialogFrame.setVisible(true);
        }
        
        // Update configuration for Equivalence Column 
        updateColumnConfig(TableModel.EQUIVALENCE_COLUMN);

		// Enable List Selection Listeners
		panel.enableListeners(true, ListSelectionListener.class);
	}

	public boolean isEnabledAction() 
	{
		Config config = panel.getConfig();
		
		return ( !SelectionUtils.isAllSelectedRowsEmpty(model) &&
			     ( ((config instanceof ProcessorConfig) || (config instanceof GroupConfig)) && 
				     table.getSelectedRowCount() > 0 && !panel.isEditingByTextField()) );
	}
}