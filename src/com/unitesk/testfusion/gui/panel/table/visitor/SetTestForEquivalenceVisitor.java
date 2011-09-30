/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: SetTestForEquivalenceVisitor.java,v 1.4 2008/08/19 11:54:36 vorobyev Exp $
 */

package com.unitesk.testfusion.gui.panel.table.visitor;

import java.util.HashSet;

import com.unitesk.testfusion.core.config.*;
import com.unitesk.testfusion.core.config.walker.ConfigEmptyVisitor;

/**
 * @author <a href="mailto:vorobyev@ispras.ru">Dmitry Vorobyev</a>
 */
public class SetTestForEquivalenceVisitor extends ConfigEmptyVisitor
{
	protected HashSet<String> equivalenceClasses;
	
	public SetTestForEquivalenceVisitor(HashSet<String> equivalenceClasses)
	{
		this.equivalenceClasses = equivalenceClasses;
	}
	
	public void onInstruction(InstructionConfig instruction)
	{
		String instructionClass = instruction.getEquivalenceClass();
		
		if(equivalenceClasses.contains(instructionClass))
		{
			instruction.setSelectedWithPropagation();
		}
	}
}
