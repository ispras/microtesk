/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: GetSpecificPositionVisitor.java,v 1.2 2008/10/01 14:17:47 vorobyev Exp $
 */

package com.unitesk.testfusion.gui.dialog.options.visitor;

import java.util.HashSet;

import com.unitesk.testfusion.core.config.InstructionConfig;
import com.unitesk.testfusion.core.config.walker.ConfigEmptyVisitor;

/**
 * @author <a href="mailto:vorobyev@ispras.ru">Dmitry Vorobyev</a>
 */
public class GetSpecificPositionVisitor extends ConfigEmptyVisitor
{
	public final static int MAX_POSITION = 0;
	
	public int specificPosition = 0;
	public int specific;
	
	public GetSpecificPositionVisitor()
	{
		specific = MAX_POSITION;
	}
	
	public GetSpecificPositionVisitor(int specific)
	{
		this.specific = specific;

		// TODO: Add check
	}
	
	public void onInstruction(InstructionConfig instruction)
	{
		HashSet<Integer> positions = instruction.getPositions();

		if(specific == MAX_POSITION)
		{
			for(int position: positions)
			{
				if(position > specificPosition)
				{
				    specificPosition = position;
				}
			}
		}
	}
	
	public int getSpecificPosition()
	{
		return specificPosition;
	}
}