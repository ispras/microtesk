/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: TableSelectedInfo.java,v 1.5 2008/08/13 15:02:55 vorobyev Exp $
 */

package com.unitesk.testfusion.gui.panel.table.utils;

import com.unitesk.testfusion.core.config.*;
import com.unitesk.testfusion.core.config.walker.*;

/**
 * @author <a href="mailto:vorobyev@ispras.ru">Dmitry Vorobyev</a>
 */
public class TableSelectedInfo 
{
	Config totalConfig;
	ConfigWalker walker;
	ConfigCounter counterVisitor;
	
	public final static int SITUATION_INFO = 0;
	public final static int INSTRUCTION_INFO = 1;
	
	protected final static boolean PRINT_PERCENT = false;
	
	public TableSelectedInfo(Config totalConfig)
	{
		this.counterVisitor = new ConfigCounter();
		
		ConfigCounter totalCounter = new ConfigCounter();
		ConfigWalker totalWalker = new ConfigWalker(totalConfig, totalCounter, ConfigWalker.VISIT_ALL);
		totalWalker.process();
	}
	
	public String getInfo(Config currentConfig, int infoType)
	{
		walker = new ConfigWalker(currentConfig, counterVisitor, ConfigWalker.VISIT_ALL);
		walker.process();

		if(currentConfig instanceof SectionConfig ||
           currentConfig instanceof GroupConfig)
		{
			if(infoType == INSTRUCTION_INFO)
			{
		        int instruction_select  = counterVisitor.countSelectedInstruction();
		        int instruction_count   = counterVisitor.countInstruction();
		        int instruction_percent = instruction_count == 0 ? 100 : (100 * instruction_select) / instruction_count; 
	
		        String instruction_info = "  " + instruction_select + "/" + instruction_count; 
		        
		        if(PRINT_PERCENT)
		        	{ instruction_info += " (" + instruction_percent + "%)"; }
		        
		        return instruction_info;
			}
		}
		
		if(currentConfig instanceof SectionConfig ||
		   currentConfig instanceof GroupConfig || 
		   currentConfig instanceof InstructionConfig)
		{
	        if(infoType == SITUATION_INFO)
	        {
	        	int situation_select  = counterVisitor.countSelectedSituation();
	        	int situation_count   = counterVisitor.countSituation();
	        	int situation_percent = situation_count == 0 ? 100 : (100 * situation_select) / situation_count; 
	        	
	        	String situation_info = "  " + situation_select + "/" + situation_count; 
	        	
	        	if(PRINT_PERCENT)
	        		{ situation_info += " (" + situation_percent + "%)"; }
	        	
	        	return situation_info;
	        }
		}
		
		throw new IllegalArgumentException();
	}
}
