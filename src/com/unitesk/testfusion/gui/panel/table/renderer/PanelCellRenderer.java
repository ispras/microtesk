/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: PanelCellRenderer.java,v 1.9 2008/08/28 11:31:36 vorobyev Exp $
 */

package com.unitesk.testfusion.gui.panel.table.renderer;

import java.awt.*;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;

import com.unitesk.testfusion.core.config.*;
import com.unitesk.testfusion.gui.ColorManager;
import com.unitesk.testfusion.gui.panel.table.TableModel;

/**
 * @author <a href="mailto:vorobyev@ispras.ru">Dmitry Vorobyev</a>
 */
public class PanelCellRenderer extends DefaultTableCellRenderer
{
	private static final long serialVersionUID = 0L;

	protected TableModel model;
	protected Component cell;
	
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
		       boolean hasFocus, int row, int column) 
	{
		cell = super.getTableCellRendererComponent
                         (table, value, isSelected, hasFocus, row, column);
		
		model = (TableModel)table.getModel();
		
		Config config = model.getConfigValue(model.getRow(row));

		int col = model.getColumnValue(column);
		
		JLabel label = (JLabel)cell;
		
		if(col == TableModel.INSTRUCTIONS_COLUMN ||
		   col == TableModel.SITUATIONS_COLUMN)
		{
			label.setHorizontalAlignment(JLabel.LEFT);
		}
		else
		{
			label.setHorizontalAlignment(JLabel.LEFT);
		}

		/* Test renderer */
		if(config instanceof TestConfig)
		{
			setUnselectedColor(isSelected, ColorManager.DEFAULT_TEST_COLOR);
		}
		/* Section renderer */
		else if(config instanceof SectionConfig)
		{
			setUnselectedColor(isSelected, ColorManager.DEFAULT_SECTION_COLOR);
		}
		/* Group renderer */
		else if(config instanceof GroupConfig)
		{
			setUnselectedColor(isSelected, ColorManager.DEFAULT_GROUP_COLOR);
		}
		/* Instruction renderer */
		else if(config instanceof InstructionConfig)
		{
			setUnselectedColor(isSelected, ColorManager.DEFAULT_INSTRUCTION_COLOR);
		}
		/* Situation renderer */
		else if(config instanceof SituationConfig)
		{
			setUnselectedColor(isSelected, ColorManager.DEFAULT_SITUATION_COLOR);
		}
		
		return cell;
	}
	
	protected void setUnselectedColor(boolean isSelected, Color color)
	{
		if(!isSelected) { cell.setBackground(color); }
	}
}
