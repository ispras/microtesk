/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: SequenceTemplateIteratorDialog.java,v 1.2 2008/11/20 13:05:56 kozlov Exp $
 */

package com.unitesk.testfusion.gui.dialog.options;

import javax.swing.JPanel;

import com.unitesk.testfusion.core.config.OptionsConfig;
import com.unitesk.testfusion.core.config.template.SequenceTemplateIteratorConfig;
import com.unitesk.testfusion.core.config.template.SingleTemplateIteratorConfig;
import com.unitesk.testfusion.core.config.walker.ConfigWalker;
import com.unitesk.testfusion.gui.GUI;
import com.unitesk.testfusion.gui.dialog.OptionsDialog;
import com.unitesk.testfusion.gui.dialog.options.panel.iterator.SequenceTemplateIteratorPanel;
import com.unitesk.testfusion.gui.dialog.options.panel.iterator.SingleTemplateIteratorPanel;
import com.unitesk.testfusion.gui.dialog.options.visitor.GetSpecificPositionVisitor;

/**
 * @author <a href="mailto:vorobyev@ispras.ru">Dmitry Vorobyev</a>
 */
public class SequenceTemplateIteratorDialog extends SingleTemplateIteratorDialog 
{
	private static final long serialVersionUID = 0L;

	public SequenceTemplateIteratorDialog(GUI frame, OptionsDialog dialog, JPanel panel) 
	{
		super(frame, dialog, panel);
	}
	
	protected void setTemplateSize()
	{
		if(!(dialog instanceof SingleTemplateIteratorDialog))
			{ throw new IllegalStateException("dialog is not instanceof SequenceIteratorDialog"); }
	
		// Get max position
		GetSpecificPositionVisitor specificVisitor = new GetSpecificPositionVisitor(GetSpecificPositionVisitor.MAX_POSITION);
		ConfigWalker specificWalker = new ConfigWalker(sectionConfig, specificVisitor);
		specificWalker.process();
		
		int maxPosition = specificVisitor.getSpecificPosition();
		
		OptionsConfig optionsConfig = frame.getSection().getOptions();
	
		SequenceTemplateIteratorConfig sequenceIterator = (SequenceTemplateIteratorConfig)optionsConfig.getTemplateIterator(OptionsConfig.SEQUENCE_TEMPLATE_ITERATOR);
        
		SingleTemplateIteratorConfig singleIterator     = (SingleTemplateIteratorConfig)optionsConfig.getTemplateIterator(OptionsConfig.SINGLE_TEMPLATE_ITERATOR);

		SequenceTemplateIteratorPanel sequencePanel = (SequenceTemplateIteratorPanel)panel; 
		SingleTemplateIteratorPanel singlePanel = 
            optionsDialog.getTemplateIteratorTabPanel().getSingleTemplateIteratorPanel();
		
		if(sequenceIterator.getTemplateSize() < maxPosition)
		{
			// Set position into single iterator
			sequencePanel.setSizeValue(maxPosition);
		}

		if(singleIterator.getTemplateSize() < maxPosition)
		{
			// Set position into sequence iterator
			singlePanel.setSizeValue(maxPosition);
		}
	}
}