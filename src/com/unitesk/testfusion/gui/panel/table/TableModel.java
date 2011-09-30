/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: TableModel.java,v 1.59 2009/05/15 16:14:25 vorobyev Exp $
 */

package com.unitesk.testfusion.gui.panel.table;

import java.io.File;
import java.util.HashMap;

import javax.swing.JTable;
import javax.swing.event.TableModelListener;
import javax.swing.table.*;

import com.unitesk.testfusion.core.config.*;
import com.unitesk.testfusion.core.config.walker.ConfigWalker;
import com.unitesk.testfusion.gui.*;
import com.unitesk.testfusion.gui.action.*;
import com.unitesk.testfusion.gui.dialog.ConfigDialogs;
import com.unitesk.testfusion.gui.panel.table.utils.TableSelectedInfo;
import com.unitesk.testfusion.gui.panel.table.visitor.*;
import com.unitesk.testfusion.gui.tree.section.SectionTree;

/**
 * @author <a href="mailto:vorobyev@ispras.ru">Dmitry Vorobyev</a>
 */
public class TableModel extends DefaultTableModel
{
	public final static int TEST_COLUMN         = 0;
	public final static int NAME_COLUMN         = 1;
	public final static int EQUIVALENCE_COLUMN  = 2;
	public final static int SECTIONS_COLUMN     = 3;
	public final static int INSTRUCTIONS_COLUMN = 4;
	public final static int SITUATIONS_COLUMN   = 5;
    public final static int DESCRIPTION_COLUMN  = 6;
    
    public final static String SINGLE_EQUIVALENCE_CLASS = "";
        
	private static final long serialVersionUID = 0L;
	
	public HashMap<Integer, Config> numberRowMap = new HashMap<Integer, Config>();
	
	/** Current frame. */
	protected GUI frame;
	
	/** Panel configuration. */
	protected Config config;
	
	/** Hash that show such columns as are present in table. */
	protected HashMap<Integer, Integer> columnEnabled = null; 
	
	/** Hash that show names of such columns that present in table. */
	protected HashMap<Integer, String> columnNamesHash;
	
	/** Object give methods that allow to get statistical information about 
	  * test configuration. */
	protected TableSelectedInfo selectedInfo;
	
	/** Flag that defines if table is sortable. */
	protected boolean tableSortable = true;
	
	/** Main table of configuration. */
	public JTable table;
	
	/** Constructor. */
	public TableModel(GUI frame, Config config, JTable table)
	{
		this(frame, config, table, null, null);
	}

	/** Extended constructor. */
	public TableModel(GUI frame, Config config, JTable table, HashMap<Integer, Integer> columnHash)
	{
		this(frame, config, table, columnHash, null);
	}
	
	/** Extended constructor. */
	public TableModel(GUI frame, Config config, JTable table, HashMap<Integer, Integer> columnHash, HashMap<Integer, String> columnNamesHash)
	{
		super();
		this.frame  = frame;
		this.config = config;
		this.table = table;
		
		this.selectedInfo = new TableSelectedInfo(config);
		
		if(columnHash == null)
		{
			columnEnabled = new HashMap<Integer, Integer>();
		
			/* Set default: columns 0-4 are enabled */
			columnEnabled.put(0, TEST_COLUMN);
			columnEnabled.put(1, NAME_COLUMN);
			columnEnabled.put(2, EQUIVALENCE_COLUMN);
			columnEnabled.put(3, INSTRUCTIONS_COLUMN);
			columnEnabled.put(4, SITUATIONS_COLUMN);
		}
		else
            { columnEnabled = columnHash; }

		if(columnNamesHash == null)
		{
			columnNamesHash = new HashMap<Integer, String>();
			
		    columnNamesHash.put(TEST_COLUMN, "Test");
		    columnNamesHash.put(NAME_COLUMN, "Subgroup or Instruction");
		    columnNamesHash.put(EQUIVALENCE_COLUMN, "Equivalence Class");
		    columnNamesHash.put(INSTRUCTIONS_COLUMN, "Instructions");
		    columnNamesHash.put(SITUATIONS_COLUMN, "Situations");
		    columnNamesHash.put(DESCRIPTION_COLUMN, "Description");
		    columnNamesHash.put(SECTIONS_COLUMN, "Sections");
		}
		else
            { this.columnNamesHash = columnNamesHash; }
		
		// Add Columns into Table
		int columnCount = columnEnabled.size();
		for(int i = 0; i < columnCount; i++)
			{ addColumn(columnNamesHash.get(getColumnValue(i))); }
		
		addTableRows(config, numberRowMap);
	}

	/** Method that define class of corresponding cell.
	 * 
	 *  @param  <code>column</code> column of corresponding cell.
	 *  
	 *  @return the class of corresponding cell.
	 */
    @SuppressWarnings("unchecked")
    public Class getColumnClass(int column) 
    {
        return getValueAt(0, column).getClass();
    }
 
    /** Method that set with propagation the same class of equivalence for 
     *  selected rows of panel. 
     *  
     *  @param <code>name</code> the name of equivalence class.
     */
	public void setEquivalenceClass(String name)
	{
		if(!hasColumnValue(EQUIVALENCE_COLUMN))
			{ return; }

		for(int i = 0; i < getRowCount(); i++)
		{
			Boolean selected = this.isSelectedRow(i); 
			
			if(selected) 
			{
				Config configValue = getConfigValue(table.getRowSorter().convertRowIndexToModel(i));

				// Set Equivalence Class in Config
				setDataEquivalenceClass(configValue, name);
			}
		}
		
		// Update Model from Config
	    updateTableEquivalenceModel();
	}
	
	/** Method that set equivalence class with propagation for given 
	 *  configuration. 
	 * 
	 *  @param <code>config</code> the given configuration.
	 * 
	 *  @param <code>equivalenceClass</code> the given equivalence class.
	 */
    public void setDataEquivalenceClass(Config config, String equivalenceClass)
    {
    	SetEquivalenceVisitor visitor = new SetEquivalenceVisitor(equivalenceClass);
    	ConfigWalker walker = new ConfigWalker(config, visitor, ConfigWalker.VISIT_ALL);
    	walker.process();
    }

    /**
     * Method that update model from for given configuration.
     * 
     * @param <code>config</code> the given configuration.
     */
    public void updateModel(Config config)
    {
    	TableModelListener listeners[]= getListeners(TableModelListener.class);

    	// Remove Table Model listener
    	this.listenerList.remove((TableModelListener.class), listeners[0]);
    	
    	// Remove all rows
    	while(getRowCount() > 0)
    		{ removeRow(0); }

    	this.numberRowMap = new HashMap<Integer, Config>();
    	
    	// Add rows with new Configuration
    	addTableRows(this, config, numberRowMap);

    	// Recover Table Model listener
    	this.listenerList.add((TableModelListener.class), listeners[0]);
    }
    
    public void addTableRows(Config config)
    {
    	addTableRows(config, numberRowMap);
    }
    
    public void addTableRows(Config config, HashMap<Integer, Config> numberRowMap)
    {
    	addTableRows(this, config, numberRowMap);
    }
    
    /** Method that add rows for given configuration. */
    public void addTableRows(TableModel model, Config config, HashMap<Integer, Config> numberRowMap)
	{
		AddRowVisitor visitor = new AddRowVisitor(frame, this, config, numberRowMap);
		ConfigWalker walker = new ConfigWalker(config, visitor, ConfigWalker.VISIT_IMMEDIATE_CHILDREN);
		walker.process();
	}
	
	/** Method that allow to show selected row configuration by mouse 
	 *  clicks. */
    public void showCell(int row, int column, boolean oneClick)
	{
		if(!oneClick)
		{
			Config config = getConfigValue(getRow(row));
			
			if(config instanceof TestConfig)
			{
	        	TestSuiteConfig testSuiteConfig = frame.getTestSuite();
				String testSuitePath = testSuiteConfig.toString();
				String filename = testSuitePath + "/" + config.getName() + "." + GUI.EXTENSION;
				
				// Get file state
				File file = new File(filename);
			    
				int fileStatus = ConfigDialogs.readFile(frame, file);
			    
			    if(fileStatus == ConfigDialogs.FILE_HAS_READ)
			    {
			    	OpenTestAction openTestAction = new OpenTestAction(frame);

			    	// Update configuration for tree
			    	openTestAction.updateConfig();
			    }
		    
			    return;
		    }
			else if(config instanceof SectionConfig)
			{
				SectionConfig section = (SectionConfig)config;
				
				frame.showConfig(config);
				
				// Section has no subsections
				if(section.isLeaf())
				{
					// Set Section
					config = section.getProcessor();
				}
				
				return;
			}
			else if(config.isEmpty())
				{ throw new IllegalStateException("Configuration is empty"); }
		
			frame.showConfig(config);
		}
	}
   	
    //**********************************************************************************************
    // Update methods, using when model is actual, config is not actual. 
    //**********************************************************************************************

	public void updateColumnConfig()
	{
		// Update realize by using Tree update
		updateTreeStateConfig();
	}

	/** Method that update test configuration and then update tree. */
	public void updateTreeStateConfig()
	{
		for(int row = 0; row < getRowCount(); row++)
		{
			int rowSorted = table.getRowSorter().convertRowIndexToModel(getRow(row));
			SelectionConfig config = (SelectionConfig)getConfigValue(rowSorted);
			
			Boolean underTest = (Boolean)getRealValueAt(rowSorted, TEST_COLUMN ); 
			config.setSelectedWithPropagation(underTest);
			
			SectionTree tree = frame.getSectionTree();
			tree.update();
		}
	}
	
    //**********************************************************************************************
    // Update functions: Model is not actual, Config is actual 
    //**********************************************************************************************

	/** Method that update cells from configuration. */
	public void updateColumnModel(int columnNumber)
	{
		switch(columnNumber)
		{
		case EQUIVALENCE_COLUMN:
			if(hasColumnValue(EQUIVALENCE_COLUMN) )
				{ updateTableEquivalenceModel(); }
			break;
		case TEST_COLUMN:
			if(hasColumnValue(TEST_COLUMN))
				{ updateTableTestModel(); }
			break;
		case INSTRUCTIONS_COLUMN:
			if(hasColumnValue(INSTRUCTIONS_COLUMN) )
				{ updateTableInstructionModel(); }
			break;
		case SITUATIONS_COLUMN:
			if(hasColumnValue(SITUATIONS_COLUMN) )
			 	{ updateTableSituationModel(); }
			break;
		}
	}

	public void updateTableTestModel()
	{
		for(int i = 0; i < getRowCount(); i++)
		{
    		// Get model index from sorter
			// TODO:
    		int modelRow = getRow(i); 
			SelectionConfig config = (SelectionConfig)getConfigValue(modelRow);

			// Precondition
			if(config == null) { throw new IllegalStateException("config is null"); }
			
			setRealValueAt(config.isSelected(), i, TEST_COLUMN );
		}
	}
	
	public void updateTableInstructionModel()
	{
		// Set Instruction class for instructions
		for(int i = 0; i < getRowCount(); i++)
		{
			Config currentConfig = getConfigValue(getRow(i));
			
			int column; 
			
			String info;

			if(currentConfig instanceof GroupConfig && hasColumnValue(INSTRUCTIONS_COLUMN))
			{
				// Update Group Info
				column = INSTRUCTIONS_COLUMN;
				info = selectedInfo.getInfo(currentConfig, TableSelectedInfo.INSTRUCTION_INFO);
				setRealValueAt(info, i, column);
			}
		}
	}
	
	public void updateTableSituationModel()
	{
		// Set Situation class for instructions
		for(int i = 0; i < getRowCount(); i++)
		{
			Config currentConfig = getConfigValue(getRow(i));
			
			int column; 
			
			String info;
			
			if(currentConfig instanceof GroupConfig || currentConfig instanceof InstructionConfig)
			{ 
				if(hasColumnValue(SITUATIONS_COLUMN))
				{
					// Update Instruction Info
					column = SITUATIONS_COLUMN; 
					info = selectedInfo.getInfo(currentConfig, TableSelectedInfo.SITUATION_INFO);
					
					setRealValueAt(info, i, column);
				}
			}
		}
	}
	
	// Function update model column from configuration map
	public void updateTableEquivalenceModel()
	{
		// At first: Set equivalence class for instruction row
		for(int i = 0; i < getRowCount(); i++)
		{
			SelectionConfig config = (SelectionConfig)getConfigValue(getRow(i));
			
			if(config instanceof InstructionConfig)
			{
				InstructionConfig instructionConfig = (InstructionConfig)config;
				
				String equivalenceClass = instructionConfig.getEquivalenceClass();
				
				// Transformation for view
				if(equivalenceClass == null) { equivalenceClass = SINGLE_EQUIVALENCE_CLASS; }
				setRealValueAt(equivalenceClass, i, EQUIVALENCE_COLUMN);
			}
		}

		// At Second: Set equivalence class for group row
		for(int j = 0; j < getRowCount(); j++)
		{
			// Get row configuration
			SelectionConfig config = (SelectionConfig)getConfigValue(getRow(j));
			
			if(config instanceof GroupConfig)
			{
				GroupConfig groupConfig = (GroupConfig)config;
				
				// Get equivalence class for group by first instruction
				Object[] equivalenceClasses = groupConfig.getEquivalenceClasses().toArray(); 
				
				if(equivalenceClasses.length != 1)
				{
					setRealValueAt(SINGLE_EQUIVALENCE_CLASS, j, EQUIVALENCE_COLUMN);
					continue;
				}
					
				String equivalenceClass = (String)equivalenceClasses[0];
				
				// Define if equivalence class is only class
				InstructionsEquivalenceVisitor visitor = new InstructionsEquivalenceVisitor(equivalenceClass);
				ConfigWalker walker = new ConfigWalker(config, visitor, ConfigWalker.VISIT_ALL);
				walker.process();
				
				// Set equivalence class into group row
				if( visitor.isEquivalence())
				    { setRealValueAt(equivalenceClass, j, EQUIVALENCE_COLUMN); }
				else
					{ setRealValueAt(SINGLE_EQUIVALENCE_CLASS, j, EQUIVALENCE_COLUMN); }
			}			
		}
	}
	
    public void initColumnSizes(JTable table)
    {
    	TableColumn column = null;

    	for (int i = 0; i < getColumnCount(); i++) 
    	{
    	    column = table.getColumnModel().getColumn(i);
    	    
    	    if(hasColumnValue(TableModel.TEST_COLUMN) && i == getColumnKey(TableModel.TEST_COLUMN) ) 
    	    	{ column.setMaxWidth(50); } 
    	    else if ((hasColumnValue(TableModel.INSTRUCTIONS_COLUMN) && i == getColumnKey(TableModel.INSTRUCTIONS_COLUMN)) ||
       	             (hasColumnValue(TableModel.SITUATIONS_COLUMN) && i == getColumnKey(TableModel.SITUATIONS_COLUMN)) )
	    		{ column.setMaxWidth(75); }
    	    else if(hasColumnKey(i))
	    		{ column.setPreferredWidth(200); }
    	}
    }
	
    //**********************************************************************************************
    // Methods for working with table values
    //**********************************************************************************************
	public Object getRealValueAt(int row, int value)
	{
		return super.getValueAt(row, getColumnKey(value) );
	}

	public void setRealValueAt(Object value, int row, int hashValue)
	{
		super.setValueAt(value, row, getColumnKey(hashValue) );
        fireTableCellUpdated(row, getColumnKey(hashValue));
	}
	
	public Object getValueAt(int row, int value)
	{
		return super.getValueAt(row, value );
	}
	
    public void setValueAt(Object value, int row, int hashValue) 
    {
    	TableModelListener list[] = listenerList.getListeners(TableModelListener.class);
    	TableModelListener listener = list[0];

    	super.setValueAt(value, row, hashValue);

    	listenerList.remove(TableModelListener.class, listener);

    	fireTableCellUpdated(row, hashValue);

    	listenerList.add(TableModelListener.class, listener);
    }

	public void setRealValueAt_sorter(Object value, int row, int hashValue)
	{
		int rowSorted = table.getRowSorter().convertRowIndexToModel(row);
		
		super.setValueAt(value, rowSorted, getColumnKey(hashValue) );
        fireTableCellUpdated(rowSorted, getColumnKey(hashValue));
	}
	
    public Integer getRowKey(Config configValue)
    {
    	if(!numberRowMap.containsValue(configValue))
    		{ throw new IllegalStateException("configMap doesn't contain that row"); }
    	
    	for(int i = 0; i < getRowCount(); i++)
		{
    		/* Running over all of the rows */
    		Integer row = getRow(i);
    		Config config = numberRowMap.get(row);

    		if(config == configValue) { return row; }
		}
    	
    	throw new IllegalStateException("Can't find row");
    }
    
    public Config getConfigValue(Integer rowNumber)
    {
    	if(!numberRowMap.containsKey(rowNumber))
    		{ throw new IllegalStateException("configMap doesn't contain that key"); }
    	
    	return numberRowMap.get(rowNumber);
    }
    
	public int getColumnValue(int key)
	{
		if(!hasColumnKey(key))
			{ throw new IllegalStateException("column=" + key); }
		
		return columnEnabled.get(key);
	}
	
	public int getColumnKey(int value)
	{
		for(int i = 0; i < getColumnCount(); i++)
		{
			if(hasColumnKey(i) && (getColumnValue(i) == value))
				{ return i; } 
		}
		
		throw new IllegalStateException("value=" + value + " hash=" + columnEnabled.toString());
	}
	
	public boolean hasColumnKey(int key)
	{
		return columnEnabled.containsKey(key);
	}

	public Integer getRow(int row)
	{
		return row;
	}

	public boolean hasColumnValue(int columnValue)
	{
		return columnEnabled.containsValue(columnValue);
	}
	
	public boolean isSelectedRow(int row)
	{
		int selectedRows[] = table.getSelectedRows();
		
		for(int i = 0; i < selectedRows.length; i++)
		{
			if(row == selectedRows[i])
				{ return true; }
		}
		
		return false;
	}
	
	public boolean isSelectedCell(int row, int column)
	{
		int selectedRows[]    = table.getSelectedRows();
		int selectedColumns[] = table.getSelectedColumns();
		
		for(int i = 0; i < selectedColumns.length; i++)
		{
			if(column == selectedColumns[i])
			{
				for(int j = 0; j < selectedRows.length; j++)
				{
					if(row == selectedRows[j])
						{ return true; }
				}
			}
		}
		
		return false;
	}
	
	public void unselectTable()
	{
		table.getSelectionModel().clearSelection();
	}
	
	public int countOfSelectedInstructions()
	{
		return table.getSelectedRows().length;
	}
}