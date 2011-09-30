/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: TablePanelModelListener.java,v 1.4 2008/08/25 14:55:35 vorobyev Exp $
 */

package com.unitesk.testfusion.gui.panel.table.listener;

import javax.swing.event.TableModelListener;

import com.unitesk.testfusion.gui.panel.table.TablePanel;

/**
 * @author <a href="mailto:vorobyev@ispras.ru">Dmitry Vorobyev</a>
 */
public abstract class TablePanelModelListener implements TableModelListener 
{
	protected TablePanel panel;
	
	public TablePanelModelListener()
	{
	}

	public TablePanelModelListener(TablePanel panel)
	{
		registerParams(panel);
	} 
	
	public void registerParams(TablePanel panel)
	{
		this.panel = panel;
	}
}
