/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: SetPositionSetVisitor.java,v 1.2 2008/09/13 11:16:52 vorobyev Exp $
 */

package com.unitesk.testfusion.gui.dialog.options.visitor;

import java.util.*;

import com.unitesk.testfusion.core.config.InstructionConfig;
import com.unitesk.testfusion.core.config.walker.ConfigEmptyVisitor;

/**
 * @author <a href="mailto:vorobyev@ispras.ru">Dmitry Vorobyev</a>
 */
public class SetPositionSetVisitor extends ConfigEmptyVisitor
{
	HashMap<InstructionConfig, HashSet<Integer>> positionMap;
	
	public SetPositionSetVisitor(HashMap<InstructionConfig, HashSet<Integer>> positionMap)
	{
		this.positionMap = positionMap;
	}
	
	public void onInstruction(InstructionConfig instruction)
	{
		if(!positionMap.containsKey(instruction))
			{ clearPositionSet(instruction); } 
		else
			{ setPositionSet(instruction, positionMap.get(instruction)); }
	}
	
	public void clearPositionSet(InstructionConfig instruction)
	{
		HashSet<Integer> set = instruction.getPositions();
		
		// Remove all elements
		set.clear();
	}
	
	public void setPositionSet(InstructionConfig instruction, HashSet<Integer> positionSet)
	{
		// Clear position set
		clearPositionSet(instruction);
		
		for(int position: positionSet)
		{
			// Add new positions
			instruction.registerPosition(position);
		}
	}
}
