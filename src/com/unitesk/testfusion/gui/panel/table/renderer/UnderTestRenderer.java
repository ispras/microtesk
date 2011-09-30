/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: UnderTestRenderer.java,v 1.6 2008/08/13 10:50:24 vorobyev Exp $
 */

package com.unitesk.testfusion.gui.panel.table.renderer;

import java.awt.Component;

import javax.swing.*;

import com.unitesk.testfusion.core.config.*;
import com.unitesk.testfusion.gui.panel.table.TableModel;

/**
 * @author <a href="mailto:vorobyev@ispras.ru">Dmitry Vorobyev</a>
 */
public class UnderTestRenderer extends PanelCellRenderer 
{
	private static final long serialVersionUID = 0L;

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
		       boolean hasFocus, int row, int column) 
	{
		Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

		JPanel rendererComponent = new JPanel();
		rendererComponent.setLayout(new BoxLayout(rendererComponent, BoxLayout.LINE_AXIS));
		
		ClassLoader loader = getClass().getClassLoader(); 
		
		JCheckBox checkbox = new JCheckBox();
		checkbox.setHorizontalAlignment(JCheckBox.CENTER);
		
		model = (TableModel) table.getModel();
		
		Config config = model.getConfigValue(table.getRowSorter().convertRowIndexToModel(model.getRow(row))); 
		
		JLabel label = new JLabel();
		
		// Instruction renderer
		if(config instanceof InstructionConfig)
		{
			label.setIcon(new ImageIcon(loader.getResource("img/instruction.gif")));
		}
		// Group renderer
		else if(config instanceof GroupConfig)
		{
			label.setIcon(UIManager.getIcon("Tree.openIcon"));
		}
		
		label.setHorizontalAlignment(JLabel.CENTER);

		boolean marked = (Boolean) value;
		
		checkbox.setBackground(cell.getBackground());
		rendererComponent.setBackground(cell.getBackground());
		
		if(marked) 
			{ checkbox.setSelected(true); }
		
		// Checkbox handle
		checkbox.setEnabled(!config.isEmpty());
		
		rendererComponent.add(checkbox);
		rendererComponent.add(label);
		
		return rendererComponent;
	}
}

