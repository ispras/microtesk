/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: TablePanel.java,v 1.84 2009/05/15 16:14:25 vorobyev Exp $
 */

package com.unitesk.testfusion.gui.panel.table;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.plaf.basic.BasicTableUI;
import javax.swing.table.*;

import com.unitesk.testfusion.core.config.*;
import com.unitesk.testfusion.core.config.walker.ConfigWalker;
import com.unitesk.testfusion.gui.*;
import com.unitesk.testfusion.gui.panel.ConfigPanel;
import com.unitesk.testfusion.gui.panel.table.action.*;
import com.unitesk.testfusion.gui.panel.table.buttons.*;
import com.unitesk.testfusion.gui.panel.table.editor.EquivalenceEditor;
import com.unitesk.testfusion.gui.panel.table.listener.*;
import com.unitesk.testfusion.gui.panel.table.menu.*;
import com.unitesk.testfusion.gui.panel.table.renderer.*;
import com.unitesk.testfusion.gui.panel.table.visitor.*;
import com.unitesk.testfusion.gui.tree.section.SectionTree;

/**
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 * @author <a href="mailto:vorobyev@ispras.ru">Dmitry Vorobyev</a>
 */
public class TablePanel extends ConfigPanel implements ListSelectionListener
{
    public static final long serialVersionUID = 0;

    public final static String DIVIDE_LABEL_NAME = "Test Coverage";
	
    /** Current Frame. */
    protected GUI frame; 

    /** Main panel that contains table and table contol buttons. */
    protected JPanel tablePanel;
    
    /** Used to display main table. */
    protected JTable table;

    /** Current panel. */
    protected TablePanel panel = this;
    
    /** Model of table. */
    protected TableModel model;
   
    /** Panel that provides scrollable view of table. */
    protected JScrollPane tableScrollPanel;
    
    /** Label that position between main table and table of operands. */
    protected JLabel divideLabel;
    protected JPanel divideLabelPanel;
    
    /** Panel that contains table of operands (for instruction panel). */
    protected OperandTablePanel operandTablePanel;
    
    /** Variable that indicates if table of operands exists. */
    protected boolean operandTableEnabled = false;
    
    /** Hash that contains column order for the table. */
    protected HashMap<Integer, Integer> columnHash = null;
	
	/** Hash that contains column Names for the table. */
	protected HashMap<Integer, String> columnNamesHash = null;

    /** Flag that show if Equivalence column is editing now. */ 
    protected boolean editingByTextField = false;
    
    /** Changeable menu that show by right mouse click. */
    protected TablePanelElement menu = new TablePanelElement(PopupMenu.class);

    /** Changeable panel that contains control buttons for current table. */
    protected TablePanelElement buttonPanel = new TablePanelElement(ButtonsPanel.class);
 
    /** Changeable TablePanelModelListeners that implements TableModelListener interface. */
    protected TablePanelElement tableModelListener = new TablePanelElement(TablePanelModelListener.class);
    
    /** Changeable table UI. */
    protected TablePanelElement tableUI = new TablePanelElement(BasicTableUI.class);
    
    public TablePanel(GUI frame)
    {
        super(frame);
        this.frame = frame;
    }
    
    public void setColumnHash(HashMap<Integer, Integer> columnHash)
    {
    	this.columnHash = columnHash; 
    }
    
    public void setColumnNamesHash(HashMap<Integer, String> columnNamesHash)
    {
    	this.columnNamesHash = columnNamesHash;
    }
    
    public JComponent createCustomComponent()
    {
    	tablePanel = new JPanel();
    	
    	return tablePanel;
    }

	public void show(Config config)
	{
		super.show(config);
    	this.config = config;
    	
    	// Clear panel
    	tablePanel.removeAll();

    	// Set new layout for model
    	tablePanel.setLayout(new BoxLayout(tablePanel, BoxLayout.Y_AXIS));

		table = new JTable();
        
		// Create new model for new group
    	model = new TableModel(frame, config, table, columnHash, columnNamesHash)
    	{
			private static final long serialVersionUID = 0L;

			public boolean isCellEditable(int row, int col)
	        {
	        	return panel.isCellEditable(row, col);
	        }
    	};

    	// Set model for table
    	table.setModel(model);
    	
    	// Register all default objects
		registerDefaultObjects();
    	
		// Set UI for table
		table.setUI((BasicTableUI)tableUI.getObject());
    	
    	if(isTableSortable() && (model.getRowCount() != 0) )
    		{ table.setAutoCreateRowSorter(true); }

		model.initColumnSizes(table);

		// Set table focusable
		table.setFocusable(true);

		table.setOpaque(true);
        
        // Cell is selectable
    	table.setRowSelectionAllowed(true);

        setCellRenderers();
        setCellEditors();
        
        // Create components
        tableScrollPanel = new JScrollPane(table);

        setScrollPanelSize();
        
        if(operandTableEnabled)
            { tablePanel.add(getDivideLabelPanel()); }
        
        // ButtonPanel
        ButtonsPanel buttonPanelObject = (ButtonsPanel)buttonPanel.getObject();
        buttonPanelObject.setFocusable(false);
        buttonPanelObject.removeAll();

        // Set params for Button panel and add buttons into panel
        buttonPanelObject.setParams(this);
        
        tablePanel.setMinimumSize(new Dimension(300, 300));
        
        tablePanel.add(tableScrollPanel);
    	tablePanel.add(new JLabel(" "));
        
        tablePanel.add(buttonPanelObject);
        tablePanel.add(new JPanel());
        
        // Update
        model.updateColumnModel(TableModel.TEST_COLUMN);
       	model.updateColumnModel(TableModel.SITUATIONS_COLUMN); 
        model.updateColumnModel(TableModel.INSTRUCTIONS_COLUMN);
        model.updateColumnModel(TableModel.EQUIVALENCE_COLUMN);

        setListeners();
        
        tablePanel.revalidate();
        tablePanel.repaint();
    }
	
	public void registerDefaultObjects()
	{
    	// Register menu
		if(!menu.isRegistered())
    		{ menu.registerElement(new DefaultRightClickMenu()); }
    	
		if(!buttonPanel.isRegistered())
			{ buttonPanel.registerElement(new DefaultButtonsPanel()); }
		
		if(!tableModelListener.isRegistered())
			{ tableModelListener.registerElement(new DefaultTablePanelModelListener()); }

		// Do not change table UI
		if(!tableUI.isRegistered())
		    { tableUI.registerElement(table.getUI()); }
		
		// TODO: Register else elements
	}
	
	public boolean isCellEditable(int row, int col)
    {
    	if(model.hasColumnValue(TableModel.EQUIVALENCE_COLUMN) &&
    	   col == model.getColumnKey(TableModel.EQUIVALENCE_COLUMN) ) 
        	{ return true; } 

    	return false;
    }
	
    public void setScrollPanelSize()
    {
        // Set sizes
        int preferredWidth = tableScrollPanel.getMinimumSize().width;
        int minimumHeight = 23;
        int preferredHeight = minimumHeight + table.getPreferredSize().height; 
        tableScrollPanel.setPreferredSize( new Dimension(preferredWidth, preferredHeight) );
 }
    
    //**********************************************************************************************
    // Cells renderers and editors
    //**********************************************************************************************
    protected void setCellRenderers()
    {
        // Set renderers
		PanelCellRenderer renderer = new PanelCellRenderer();
		table.setDefaultRenderer(Object.class, renderer);
		
		if(model.hasColumnValue(TableModel.TEST_COLUMN))
		{
			int underTestIndex = model.getColumnKey(TableModel.TEST_COLUMN); 
			TableColumn underTestColumn = table.getColumnModel().getColumn(underTestIndex);
	        underTestColumn.setCellRenderer(new UnderTestRenderer());
		}
		
        if(model.hasColumnValue(TableModel.EQUIVALENCE_COLUMN))
        {
			int equivalenceIndex  = model.getColumnKey(TableModel.EQUIVALENCE_COLUMN);
	        TableColumn equivalenceColumn  = table.getColumnModel().getColumn(equivalenceIndex);
	        equivalenceColumn.setCellRenderer(new EquivalenceRenderer(model));
        }
        
        // Add operandPanel to main Panel
        if(operandTableEnabled)
        {
        	if(!(config instanceof InstructionConfig)) { throw new IllegalStateException(); }
        	tablePanel.add(getOperandTable((InstructionConfig)config));
        	tablePanel.add(new JLabel(" "));
        }
    }

    protected void setCellEditors()
    {
    	if(model.hasColumnValue(TableModel.EQUIVALENCE_COLUMN))
    	{
    		int equivalenceIndex = model.getColumnKey(TableModel.EQUIVALENCE_COLUMN);
    		TableColumn equivalenceColumn = table.getColumnModel().getColumn(equivalenceIndex);
    		equivalenceColumn.setCellEditor(new EquivalenceEditor(this, new JTextField()) );
    	}
    }
    
    //**********************************************************************************************
    // Mouse handlers
    //**********************************************************************************************
	public class TableMouseListener implements MouseListener
	{
	    public void mouseClicked(MouseEvent event) 
		{
	      	if(event.getSource().equals(table))
	        {
	      		clickJTable(event);
	        }
		}
		
	    public void mouseEntered(MouseEvent arg0) 
		{
		}
	
		public void mouseExited(MouseEvent arg0) 
		{
		}
	
		public void mousePressed(MouseEvent arg0) 
		{
		}
	
		public void mouseReleased(MouseEvent arg0) 
		{
		}
	}
	
	public void clickJTable(MouseEvent event)
	{
		boolean oneClick = false;
		int rowIndex = table.rowAtPoint(new Point(event.getX(),    event.getY()));
        int colIndex = table.columnAtPoint(new Point(event.getX(), event.getY()));
        
        // Get real row and column indexes
        int row    = table.convertRowIndexToModel(rowIndex);
        int column = table.convertColumnIndexToModel(colIndex);
        
        if((event.getClickCount() % 2) == 1)
			{ oneClick = true; }
        
        // Left button handling
        if(event.getButton() == MouseEvent.BUTTON1)
        {
        	if(model.hasColumnValue(TableModel.TEST_COLUMN) &&
        	   column == model.getColumnKey(TableModel.TEST_COLUMN))
	        	{ testClickHandler(row, column, oneClick); }
	        else if(!model.isCellEditable(row, column))
	        	{ model.showCell(row, column, oneClick); }
        }
        // Right button handling
        else if(event.getButton() == MouseEvent.BUTTON3)
        {
        	// Create popup menu
        	PopupMenu menuObject = (PopupMenu)menu.getObject();
        	
        	// Clear menu
       		menuObject.removeAll();
       		menuObject.registerParams(this);
        	
        	menuObject.show(event.getComponent(),
                    event.getX(), event.getY());
        }
	}
	
    public void testClickHandler(int row, int column, boolean oneClick)
	{
    	// Disable selection listener 
    	enableListeners(false, ListSelectionListener.class);

    	String classEquiv;
    	
    	if((model.hasColumnValue(TableModel.EQUIVALENCE_COLUMN)) && 
    	   (classEquiv = (String)model.getRealValueAt(row, TableModel.EQUIVALENCE_COLUMN)).equals(TableModel.SINGLE_EQUIVALENCE_CLASS)		
    	)
    	{
       		// Set data
       		TestAction testAction = new TestAction(panel, model, table);
       		testAction.executeAndUpdate();
    	}
    	else if(oneClick)
	    {
    		// Set data
    		TestAction testAction = new TestAction(panel, model, table);
    		testAction.executeAndUpdate();
	    }
    	else
		{
    		if(model.hasColumnValue(TableModel.EQUIVALENCE_COLUMN))
    		{
    			classEquiv = (String)model.getRealValueAt(row, TableModel.EQUIVALENCE_COLUMN );
    			
    			boolean flag = true;
				  
				// If all rows has equiv Equivalence Class 
				for(int i = 0; i < model.getRowCount(); ++i)
				{
					if(classEquiv.equals((String)model.getRealValueAt(i, TableModel.EQUIVALENCE_COLUMN )) )
					{
						if(i == row) { continue; }
						  
						if(!(Boolean)model.getValueAt(i, column))
							{ flag = false; break; }
					}
				}

	    		for(int j = 0; j < model.getRowCount(); ++j)
	    		{
	    			String tmp = (String)model.getRealValueAt(j, TableModel.EQUIVALENCE_COLUMN ); 
	    	        
	    			if(classEquiv.equals(tmp))
	    	        {
	    				int modelRow = j;
	    				
                    	Config rowConfig = model.getConfigValue(modelRow);

	    	        	// Cutting down test selection to one focused instruction 
	                    if(flag)
	                    {
	                    	
	                		SetTestVisitor visitor = new SetTestVisitor(j == row);
	                    	ConfigWalker walker = new ConfigWalker(rowConfig, visitor, ConfigWalker.VISIT_ALL);    		
	                    	walker.process();
	    			    }
	                    // Test selection for all instruction with equiv Equivalence Class
	    				else
	                    {
	                		SetTestVisitor visitor = new SetTestVisitor(true);
	                    	ConfigWalker walker = new ConfigWalker(rowConfig, visitor, ConfigWalker.VISIT_ALL);    		
	                    	walker.process();
	    				}
	    	        }
	            }
    		}
        }

    	// Update model
		model.updateColumnModel(TableModel.TEST_COLUMN);
		model.updateColumnModel(TableModel.EQUIVALENCE_COLUMN);
		updateTestDependenceObjects();
		
		// Enable List Selection listener
		enableListeners(true, ListSelectionListener.class);
	}
	    
	/** Handler of selection event for ListSelectionListener. */
	public void valueChanged(ListSelectionEvent arg0) 
	{
		updateActionDependenceObjects();
	}

    /** Update that calls when Tree updates */ 
	public void update()
	{
		model.updateModel(config);
		
		if(model.hasColumnValue(TableModel.TEST_COLUMN))
		{
			// Update Test Column from Tree
			model.updateTableTestModel();
		}
        
        // Change size of table
        panel.setScrollPanelSize();
        
        panel.revalidate();
        panel.repaint();
        
    	// Update inner status bar, extern status bar and run button
    	super.update();
	}

	//**********************************************************************************************
    // Table Action dependence functions
    //**********************************************************************************************
	
	/** Update objects that dependence of Table Actions. */
	public void updateActionDependenceObjects()
	{
		ButtonsPanel buttonPanelObject = (ButtonsPanel)buttonPanel.getObject();
		buttonPanelObject.setActivationOfButtons();
	}
	
	public void updateColumnDependenceObjects(int columnNumber)
	{
		switch(columnNumber)
		{
		case TableModel.TEST_COLUMN:
			updateTestDependenceObjects();
			break;
		}
	}
	
	/** Method that updates dependence objects from Configuration. */
	public void updateTestDependenceObjects()
	{
		// Update tree from Configuration
		SectionTree tree = frame.getSectionTree();
		tree.update();

		// Update inner, external status bars and run button from configuration
		super.update();

		// Update selected info state from Configuration
		model.updateTableInstructionModel();
		model.updateTableSituationModel();
	}
	
	//**********************************************************************************************
    // Function for work with listeners
	//**********************************************************************************************
    
	/** Method that enable or disable listeners of given class for TablePanel. 
	 * 
	 * @param <code>enabled</code> 
	 * if <code>true</code> enable listeners;
	 * <code>false</code> disable listeners.
	 * 
	 * @param <code>listenerClass</code> 
	 * class of listeners that will be disable.
	 * */
	public void enableListeners(boolean enabled, Class listenerClass)
    {
    	if(listenerClass.equals(ListSelectionListener.class))
    	{
    		//TODO: for all listeners
			if(enabled)
			{
				table.getSelectionModel().removeListSelectionListener(this);
				table.getSelectionModel().addListSelectionListener(this);
			}
			else
			{
				table.getSelectionModel().removeListSelectionListener(this);
			}
    	}
    	else if(listenerClass.equals(TableModelListener.class))
    	{
    		//TODO: for all listeners
    		
    		TableModelListener listener = (TableModelListener)tableModelListener.getObject(); 
    		if(enabled)
    			{ model.addTableModelListener(listener); }
    		else
    			{ model.removeTableModelListener(listener);	}
    	}
    	else { throw new IllegalArgumentException("Class " + listenerClass + " is not defined"); }
	}
	
	/** Method that set listeners for panel, model and table. */
	public void setListeners()
	{
        table.getSelectionModel().addListSelectionListener(this);
        
        // Add mouse listeners
        TableMouseListener mouseListener = new TableMouseListener(); 
        addMouseListener(mouseListener);
        table.addMouseListener(mouseListener);
        
        if(operandTablePanel != null)
        	{ operandTablePanel.addMouseListener(mouseListener); };

        enableListeners(true, ListSelectionListener.class);

        if(!tableModelListener.isRegistered()) 
        	{ throw new IllegalStateException("TableModelListener is not registered"); }

        TablePanelModelListener tableModelListenerObject = (TablePanelModelListener)tableModelListener.getObject();
        tableModelListenerObject.registerParams(this);

        // Set default model listener
        table.getModel().addTableModelListener(tableModelListenerObject);
	}
    
	//**********************************************************************************************
    // Getters functions
    //**********************************************************************************************
	public Config getConfig()
	{
		return config;
	}
	
    /** Function shows if equivalence column is editing by textfield now. */
	public boolean isEditingByTextField()
	{
		return editingByTextField;
	}
	
    public JPanel getDivideLabelPanel()
    {
    	divideLabelPanel = new JPanel();  
    	
    	divideLabel = new JLabel(DIVIDE_LABEL_NAME);
    	divideLabel.setFont(new Font("Arial", Font.BOLD | Font.ITALIC, 16));
        divideLabel.setHorizontalAlignment(JLabel.CENTER);
        
        divideLabelPanel.add(divideLabel);
    	
    	return divideLabelPanel; 
    }

    public Component getOperandTable(InstructionConfig instruction)
    {
        this.operandTablePanel = new OperandTablePanel(instruction);
    	
    	return operandTablePanel;
    }
    
    /** Method show if table is sortable.
     * 
     *  @return <code>true</code> if table is sortable;
     *          <code>false</code> anyway.
     *  */
    public boolean isTableSortable()
    {
    	return model.tableSortable;
    }
    
    /** Method that return frame. */
    public GUI getFrame()
    {
    	return frame;
    }
    
    /** Method that return model. */
    public TableModel getModel()
    {
    	return model;
    }
    
    /** Method that return table. */
    public JTable getTable()
    {
    	return table;
    }
    
	//**********************************************************************************************
    // Setters functions
    //**********************************************************************************************
	
    /** Method set variable that show if equivalence column is editing by textfield now. */
	public void setEditingByTextField(boolean isEditing)
	{
		editingByTextField = isEditing;
	}
	
	/** Method that enables operand table. */
	public void setOperandTableEnabled(boolean enabled)
	{
		this.operandTableEnabled = enabled;	
	}

	public void setTableSortable(boolean sortable)
	{
		model.tableSortable = sortable;
	}
}