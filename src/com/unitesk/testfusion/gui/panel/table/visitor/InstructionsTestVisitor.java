/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: InstructionsTestVisitor.java,v 1.4 2008/08/12 15:04:03 vorobyev Exp $
 */

package com.unitesk.testfusion.gui.panel.table.visitor;

import com.unitesk.testfusion.core.config.*;
import com.unitesk.testfusion.core.config.walker.ConfigEmptyVisitor;

/**
 * @author <a href="mailto:vorobyev@ispras.ru">Dmitry Vorobyev</a>
 */
public class InstructionsTestVisitor extends ConfigEmptyVisitor
{
	public static int ALL_NODES              = 0;
	public static int ALL_EXCEPT_EMPTY_NODES = 1;
	
	protected boolean isAllInstructionsTesting = true;
	protected int mode = -1;
	
	public InstructionsTestVisitor()
	{
		mode = ALL_NODES;
	}
	
	public InstructionsTestVisitor(int mode)
	{
		this.mode = mode;
	}
	
	public void onStart(Config config) 
	{
		isAllInstructionsTesting = true;
	}
	
	public void onTest(TestConfig test) {}
    public void onEnd() {}
	public void onGroup(GroupConfig group) {} 
	public void onSection(SectionConfig section) {}
    public void onProcessor(ProcessorConfig processor) {}

    public void onInstruction(InstructionConfig instruction) 
	{
    	if(mode == -1)
    		{ throw new IllegalStateException("Mode is invalid"); }
    	
		if(!instruction.isSelected())
		{
			if(mode == ALL_NODES || (mode == ALL_EXCEPT_EMPTY_NODES && !instruction.isEmpty()) )
				{ isAllInstructionsTesting = false;	}
		}
	}

	public void onSituation(SituationConfig situation) 
	{
    	if(mode == -1)
    		{ throw new IllegalStateException("Mode is invalid"); }

		if(!situation.isSelected())
		{
			if(mode == ALL_NODES || (mode == ALL_EXCEPT_EMPTY_NODES && !situation.isEmpty()) )
				{ isAllInstructionsTesting = false;	}
		}
	}
	
	public boolean isAllInstructionsTesting()
	{
		return isAllInstructionsTesting;
	}
}
