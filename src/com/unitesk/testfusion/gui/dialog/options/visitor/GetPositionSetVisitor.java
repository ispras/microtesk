/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: GetPositionSetVisitor.java,v 1.3 2008/09/19 08:27:28 vorobyev Exp $
 */

package com.unitesk.testfusion.gui.dialog.options.visitor;

import java.util.*;

import com.unitesk.testfusion.core.config.InstructionConfig;
import com.unitesk.testfusion.core.config.walker.ConfigEmptyVisitor;

/**
 * @author <a href="mailto:vorobyev@ispras.ru">Dmitry Vorobyev</a>
 */
public class GetPositionSetVisitor extends ConfigEmptyVisitor
{
	protected HashMap<Integer, HashSet<InstructionConfig>> positionMap = new HashMap<Integer, HashSet<InstructionConfig>>();

	public final static int ALL_POSITIONS = -1;
	
	public void onInstruction(InstructionConfig instruction) 
	{
		HashSet<Integer> positionSet = instruction.getPositions(); 
		
		if(instruction.isSelected())
		{
			if(!positionSet.isEmpty())
			{ 
				for(Integer position: positionSet)
				{
					HashSet<InstructionConfig> instructionConfigs;
					
					if(positionMap.containsKey(position))
					{ 
						instructionConfigs = positionMap.get(position); 
					}
					else
					{ 
						instructionConfigs = new HashSet<InstructionConfig>();
						positionMap.put(position, instructionConfigs);
					}
					
					instructionConfigs.add(instruction);
				}
			}
			else
			{
				HashSet<InstructionConfig> instructionConfigs;

				// All positions
				if(positionMap.containsKey(ALL_POSITIONS))
				{
					instructionConfigs = positionMap.get(ALL_POSITIONS);
				}
				else
				{
					instructionConfigs = new HashSet<InstructionConfig>();
					positionMap.put(ALL_POSITIONS, instructionConfigs);
				}
				
				instructionConfigs.add(instruction);
			}
		}
	}
	
	public HashMap<Integer, HashSet<InstructionConfig>> getPositionMap()
	{
		return positionMap;
	}
}