/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: GetInstructionConfigVisitor.java,v 1.1 2008/09/12 14:06:56 vorobyev Exp $
 */

package com.unitesk.testfusion.gui.dialog.options.visitor;

import java.util.HashSet;

import com.unitesk.testfusion.core.config.InstructionConfig;
import com.unitesk.testfusion.core.config.walker.ConfigEmptyVisitor;

/**
 * @author <a href="mailto:vorobyev@ispras.ru">Dmitry Vorobyev</a>
 */
public class GetInstructionConfigVisitor extends ConfigEmptyVisitor
{
	protected HashSet<InstructionConfig> set = new HashSet<InstructionConfig>();
	
	public void onInstruction(InstructionConfig instruction) 
	{
		set.add(instruction);
	}
	
	public HashSet<InstructionConfig> getInstructionConfigSet()
	{
		return set;
	}
}