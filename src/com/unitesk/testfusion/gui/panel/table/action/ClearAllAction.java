/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: ClearAllAction.java,v 1.5 2008/09/01 12:33:20 vorobyev Exp $
 */

package com.unitesk.testfusion.gui.panel.table.action;

import javax.swing.JTable;

import com.unitesk.testfusion.gui.panel.table.*;

/**
 * @author <a href="mailto:vorobyev@ispras.ru">Dmitry Vorobyev</a>
 */
public class ClearAllAction extends AbstractTableAction
{
	public ClearAllAction(TablePanel panel, TableModel model, JTable table) 
	{
		super(panel, model, table);
		this.panel = panel;
	}

	public void executeAction() 
	{
		table.getSelectionModel().removeSelectionInterval(0, table.getRowCount() - 1);
	}

	public boolean isEnabledAction() 
	{
		return super.isEnabledAction() && table.getRowCount() > 0;
	}
}