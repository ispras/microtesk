/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: EquivalenceRenderer.java,v 1.3 2008/08/11 14:12:03 vorobyev Exp $
 */

package com.unitesk.testfusion.gui.panel.table.renderer;

import java.awt.Component;

import javax.swing.JTable;

import com.unitesk.testfusion.core.config.Config;
import com.unitesk.testfusion.core.config.GroupConfig;
import com.unitesk.testfusion.gui.panel.table.TableModel;

/**
 * @author <a href="mailto:vorobyev@ispras.ru">Dmitry Vorobyev</a>
 */
public class EquivalenceRenderer extends PanelCellRenderer
{
	private static final long serialVersionUID = 0L;

	TableModel model;
	
	public EquivalenceRenderer(TableModel model)
	{
		this.model = model;
	}
	
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
		       boolean hasFocus, int row, int column) 
	{
		Config config = model.numberRowMap.get(model.getRow(row));
		
		Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		
		setBackground(cell.getBackground());
		
		if(config instanceof GroupConfig)
		{
			GroupConfig groupConfig = (GroupConfig)config;
			
			setToolTipText(groupConfig.getEquivalenceClasses().toString());
		}
		
		return cell;
	}
}
