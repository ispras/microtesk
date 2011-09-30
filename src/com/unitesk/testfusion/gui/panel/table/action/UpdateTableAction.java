/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: UpdateTableAction.java,v 1.3 2008/09/01 12:33:20 vorobyev Exp $
 */

package com.unitesk.testfusion.gui.panel.table.action;

import javax.swing.JTable;

import com.unitesk.testfusion.core.config.Config;
import com.unitesk.testfusion.gui.panel.table.*;

/**
 * @author <a href="mailto:vorobyev@ispras.ru">Dmitry Vorobyev</a>
 */
public class UpdateTableAction extends AbstractTableAction 
{
	public UpdateTableAction(TablePanel panel, TableModel model, JTable table) 
	{
		super(panel, model, table);
	}

	public void executeAction() 
	{
		Config config = panel.getConfig();
		
        // Update model
        panel.getModel().updateModel(config);
        
        // Change size of table
        panel.setScrollPanelSize();
        
        panel.repaint();
        panel.revalidate();
	}
}
