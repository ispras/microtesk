/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: AddRowVisitor.java,v 1.16 2008/11/10 12:58:18 kozlov Exp $
 */

package com.unitesk.testfusion.gui.panel.table.visitor;

import java.util.HashMap;

import com.unitesk.testfusion.core.config.*;
import com.unitesk.testfusion.core.config.walker.*;
import com.unitesk.testfusion.gui.GUI;
import com.unitesk.testfusion.gui.panel.table.TableModel;
import com.unitesk.testfusion.gui.panel.table.utils.*;

/**
 * @author <a href="mailto:vorobyev@ispras.ru">Dmitry Vorobyev</a>
 */
public class AddRowVisitor extends ConfigEmptyVisitor
{
	protected TableModel model;
	protected Config config;
	protected HashMap<Integer, Config> numberConfigMap;
	protected TableSelectedInfo selectedInfo;
	protected GUI frame;
	
	public AddRowVisitor(GUI frame, TableModel model, Config config, HashMap<Integer, Config> numberConfigMap)
	{
		this.frame = frame;
		this.model = model;
		this.config = config;
		this.numberConfigMap = numberConfigMap;
		this.selectedInfo = new TableSelectedInfo(config);
	}
	
	public void onStart(Config config)  {}
    public void onEnd() {}
	
    public void onProcessor(ProcessorConfig processor) {}

    public void onTest(TestConfig test) 
    {
    	if(!config.equals(test))
    		{ addTestRow(test); }
    }
    
    public void onSection(SectionConfig section) 
    {
    	if(!config.equals(section))
    		{ addSectionRow(section); }
    }
    
	public void onGroup(GroupConfig group) 
	{
		if(!config.equals(group))
			{ addGroupRow(group); }
	}

	public void onInstruction(InstructionConfig instruction) 
	{
		if(!config.equals(instruction))
			{ addInstructionRow(instruction); }
	}

	public void onSituation(SituationConfig situation) 
	{
		if(!config.equals(situation))
			{ addSituationRow(situation); }
	}

	public void addTestRow(TestConfig testConfig)
	{
		int columnCount = model.getColumnCount();
		
		Object[] row = new Object[columnCount];
		
		for(int j = 0; j < columnCount; j++)
		{
			int column = model.getColumnValue(j);
			
			// Add section row
			switch(column)
			{
			case TableModel.NAME_COLUMN:
				row[j] = new String(testConfig.getName()); break;
			case TableModel.DESCRIPTION_COLUMN:
				TestSuiteConfig testSuiteConfig = frame.getTestSuite(); 
				String filename = testSuiteConfig.toString() + "/" + testConfig.toString() + "." + GUI.EXTENSION; 
				
				String description;
				
				try
				{
					InputOutputUtils.TestFeatures testFeatures = new InputOutputUtils.TestFeatures(); 
					description = testFeatures.getTestDescription(filename);
				}
				catch(Exception exception)
				{
					//exception.printStackTrace();
					description = "[problems occured in reading of description]";
				}
				
				row[j] = new String(description); break;
			
			default: throw new IllegalStateException("Illegal column type:" + column);
			}			
		}
	
		addRow(row, testConfig);
	}
	
	public void addSectionRow(SectionConfig sectionConfig)
	{
		int columnCount = model.getColumnCount();
		
		Object[] row = new Object[columnCount];
		
		for(int j = 0; j < columnCount; j++)
		{
			int column = model.getColumnValue(j);
			
			// Add section row
			switch(column)
			{
			case TableModel.NAME_COLUMN:
				row[j] = new String(sectionConfig.getName()); break;
			case TableModel.INSTRUCTIONS_COLUMN:
				row[j] = new String(selectedInfo.getInfo(sectionConfig, TableSelectedInfo.INSTRUCTION_INFO)); break; 
			case TableModel.SITUATIONS_COLUMN:
				row[j] = new String(selectedInfo.getInfo(sectionConfig, TableSelectedInfo.SITUATION_INFO)); break;				
			default: throw new IllegalStateException("Illegal column type:" + column);
			}
		}
	
		addRow(row, sectionConfig);
	}
	
	public void addGroupRow(GroupConfig groupConfig)
	{
		int columnCount = model.getColumnCount();
		
		Object[] row = new Object[columnCount];
		
		for(int j = 0; j < columnCount; j++)
		{
			int column = model.getColumnValue(j); 

			// Add group row
			switch(column)
			{
			case TableModel.TEST_COLUMN:
				row[j] = new Boolean(false); break;
			case TableModel.NAME_COLUMN:
				row[j] = new String(groupConfig.getName()); break;
			// Equivalence Class is Empty - it should be updated after table creation
			case TableModel.EQUIVALENCE_COLUMN:
				row[j] = new String(""); break;
			case TableModel.INSTRUCTIONS_COLUMN:
				row[j] = new String(selectedInfo.getInfo(groupConfig, TableSelectedInfo.INSTRUCTION_INFO)); break;
			case TableModel.SITUATIONS_COLUMN:
				row[j] = new String(selectedInfo.getInfo(groupConfig, TableSelectedInfo.SITUATION_INFO)); break;
			default: throw new IllegalStateException("Illegal column type:" + column);
			}
		}
		addRow(row, groupConfig);
	}
	
	public void addInstructionRow(InstructionConfig instructionConfig)
	{
		String equivalenceClass = instructionConfig.getEquivalenceClass();

		int columnCount = model.getColumnCount();
		
		if(equivalenceClass == null)
			{ equivalenceClass = new String(TableModel.SINGLE_EQUIVALENCE_CLASS); }

		Object[] row = new Object[columnCount];

		for(int j = 0; j < columnCount; j++)
		{
			int column = model.getColumnValue(j); 

			// Add instruction row
			switch(column)
			{
			case TableModel.TEST_COLUMN:
				row[j] = new Boolean(instructionConfig.isSelected()); break;
			case TableModel.NAME_COLUMN:
				row[j] = new String(instructionConfig.getName()); break;
			case TableModel.EQUIVALENCE_COLUMN:
				row[j] = new String(equivalenceClass); break;
			case TableModel.SITUATIONS_COLUMN:
				row[j] = new String(selectedInfo.getInfo(instructionConfig, TableSelectedInfo.SITUATION_INFO)); break;
			default: throw new IllegalStateException("Illegal column type:" + column);
			}
		}

		addRow(row, instructionConfig);
	}
	
	public void addSituationRow(SituationConfig situationConfig)
	{
		int columnCount = model.getColumnCount();
		
		Object[] row = new Object[columnCount];
		
		for(int j = 0; j < columnCount; j++)
		{
			int column = model.getColumnValue(j);
			
			// Add situation row
			switch(column)
			{
			case TableModel.TEST_COLUMN:
				row[j] = new Boolean(situationConfig.isSelected()); break;
			case TableModel.NAME_COLUMN:
				row[j] = new String(situationConfig.getName()); break;
			case TableModel.DESCRIPTION_COLUMN:
				row[j] = new String(situationConfig.getSituation().getText()); break;
			default: throw new IllegalStateException("Illegal column type:" + column);
			}
		}
		
		addRow(row, situationConfig);
	}
	
	public void addRow(Object[] row, Config config)
	{
		model.addRow(row);

		numberConfigMap.put(numberConfigMap.size(), config);
	}
}