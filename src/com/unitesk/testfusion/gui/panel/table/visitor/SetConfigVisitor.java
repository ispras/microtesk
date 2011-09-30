/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: SetConfigVisitor.java,v 1.4 2008/08/19 11:54:36 vorobyev Exp $
 */

package com.unitesk.testfusion.gui.panel.table.visitor;

import java.util.HashMap;

import com.unitesk.testfusion.core.config.*;
import com.unitesk.testfusion.core.config.walker.ConfigEmptyVisitor;

/**
 * @author <a href="mailto:vorobyev@ispras.ru">Dmitry Vorobyev</a>
 */
public class SetConfigVisitor extends ConfigEmptyVisitor
{
	public HashMap<Object, Config> configMap;

	public SetConfigVisitor(HashMap<Object, Config> configMap)
	{
		this.configMap = configMap;
	}

	public void onProcessor(ProcessorConfig processor) 
	{
		/* Add Section into HashMap */
		configMap.put(processor.getName(), processor);
	}
	
	public void onGroup(GroupConfig group) 
	{
		/* Add group into HashMap */
		configMap.put(group.getName(), group);
	}

	public void onInstruction(InstructionConfig instruction) 
	{
		/* Add Instruction into HashMap */
		configMap.put(instruction.getName(), instruction);
	}

	public void onSituation(SituationConfig situation) 
	{
		/* Add Situation into HashMap */
		configMap.put(situation.getName(), situation);
	}
}
