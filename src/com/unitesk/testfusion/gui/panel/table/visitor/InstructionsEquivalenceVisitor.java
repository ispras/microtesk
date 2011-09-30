/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: InstructionsEquivalenceVisitor.java,v 1.4 2008/08/19 11:54:36 vorobyev Exp $
 */

package com.unitesk.testfusion.gui.panel.table.visitor;

import com.unitesk.testfusion.core.config.*;
import com.unitesk.testfusion.core.config.walker.*;
import com.unitesk.testfusion.gui.panel.table.TableModel;

/**
 * @author <a href="mailto:vorobyev@ispras.ru">Dmitry Vorobyev</a>
 */
public class InstructionsEquivalenceVisitor extends ConfigEmptyVisitor 
{
	boolean isEquivalence;
	String equivalenceClass;
	
	public InstructionsEquivalenceVisitor(String equivalenceClass)
	{
		this.equivalenceClass = equivalenceClass;
	}
	
	public void onStart(Config config) 
	{
		isEquivalence = true;
	}
	
    public void onInstruction(InstructionConfig instruction) 
	{
		String instructionClass = instruction.getEquivalenceClass(); 
	
		if( instructionClass == null ||
		    instructionClass.equals(TableModel.SINGLE_EQUIVALENCE_CLASS) ||
 	       !instructionClass.equals(equivalenceClass))
			{ isEquivalence = false;  }
	}

	public boolean isEquivalence()
	{
		return isEquivalence;
	}
}
