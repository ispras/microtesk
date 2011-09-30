/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: AddSectionTableAction.java,v 1.6 2008/09/01 12:33:20 vorobyev Exp $
 */

package com.unitesk.testfusion.gui.panel.table.action;

import javax.swing.JTable;

import com.unitesk.testfusion.core.config.*;
import com.unitesk.testfusion.gui.action.AddSectionAction;
import com.unitesk.testfusion.gui.panel.table.*;

/**
 * @author <a href="mailto:vorobyev@ispras.ru">Dmitry Vorobyev</a>
 */
public class AddSectionTableAction extends AbstractTableAction
{
	public AddSectionTableAction(TablePanel panel, TableModel model, JTable table) 
	{
		super(panel, model, table);
	}

	public void executeAction() 
	{
		Config config = panel.getConfig();
		AddSectionAction action = new AddSectionAction(panel.getFrame(), (SectionListConfig)config);
		action.execute();
	}
}