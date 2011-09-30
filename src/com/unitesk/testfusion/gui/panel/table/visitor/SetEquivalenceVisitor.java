/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: SetEquivalenceVisitor.java,v 1.4 2008/08/19 11:54:36 vorobyev Exp $
 */

package com.unitesk.testfusion.gui.panel.table.visitor;

import com.unitesk.testfusion.core.config.*;
import com.unitesk.testfusion.core.config.walker.ConfigEmptyVisitor;

/**
 * @author <a href="mailto:vorobyev@ispras.ru">Dmitry Vorobyev</a>
 */
public class SetEquivalenceVisitor extends ConfigEmptyVisitor
{
	public String equivalenceName;
	
	public SetEquivalenceVisitor(String equivalenceName)
	{
		this.equivalenceName = equivalenceName;
	}

	public void onInstruction(InstructionConfig instruction) 
	{
		if(!instruction.isEmpty())
			{ instruction.setEquivalenceClass(equivalenceName); }
	}
}
