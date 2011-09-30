/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: SingleTemplateIteratorDialog.java,v 1.21 2009/05/21 17:57:05 vorobyev Exp $
 */

package com.unitesk.testfusion.gui.dialog.options;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

import com.unitesk.testfusion.core.config.*;
import com.unitesk.testfusion.core.config.template.*;
import com.unitesk.testfusion.core.config.walker.ConfigWalker;

import com.unitesk.testfusion.gui.GUI;
import com.unitesk.testfusion.gui.action.TestSelectionConfigAction;
import com.unitesk.testfusion.gui.dialog.Dialog;
import com.unitesk.testfusion.gui.dialog.OptionsDialog;
import com.unitesk.testfusion.gui.dialog.options.action.*;
import com.unitesk.testfusion.gui.dialog.options.panel.iterator.SequenceTemplateIteratorPanel;
import com.unitesk.testfusion.gui.dialog.options.panel.iterator.SingleTemplateIteratorPanel;
import com.unitesk.testfusion.gui.dialog.options.visitor.*;
import com.unitesk.testfusion.gui.panel.table.buttons.ButtonsPanel;

import static com.unitesk.testfusion.gui.Layout.*;

/**
 * @author <a href="mailto:vorobyev@ispras.ru">Dmitry Vorobyev</a>
 */
public class SingleTemplateIteratorDialog extends Dialog implements ListSelectionListener
{
	private static final long serialVersionUID = 0L;
	
	public final static int POSITION_COLUMN    = 0;
	public final static int INSTRUCTION_COLUMN = 1;
	public final static String ALL_POSITIONS = "*";
	
	protected JPanel mainPanel = new JPanel();
	
	protected JScrollPane tableScrollPanel;
	protected DefaultTableModel model = new DefaultTableModel();
	
	protected SingleTemplateIteratorTable table;
	
	protected SingleTemplateIteratorButtonsPanel buttonsPanel;
		
	public HashSet<SingleTemplateIteratorAction> actionSet = new HashSet<SingleTemplateIteratorAction>(); 
	public HashSet<InstructionConfig> instructionConfigSet;
	
	public SingleTemplateIteratorDialog dialog = this;
	public SectionConfig sectionConfig;
	
	public GUI frame;
    
    protected OptionsDialog optionsDialog;

	// Instructions that were initiated when dialog creates 
	public HashSet<InstructionConfig> initiatedInstructions = new HashSet<InstructionConfig>();
	
	public JPanel panel;
	
	public SingleTemplateIteratorDialog(GUI frame, OptionsDialog dialog, JPanel panel) 
	{
		super(frame);
	
		this.frame = frame;
        this.optionsDialog = dialog;
		this.panel = panel;

		sectionConfig = frame.getSection();
		
		// Init instruction config set
		GetInstructionConfigVisitor instructionConfigVisitor = new GetInstructionConfigVisitor(); 
		ConfigWalker instructionConfigWalker = new ConfigWalker(sectionConfig, instructionConfigVisitor);
		instructionConfigWalker.process();
		
		instructionConfigSet = instructionConfigVisitor.getInstructionConfigSet();

		// Set dialog modal
		setModal(true);

		// Create and initialize table
		initTable();

		// Add table into panel
		tableScrollPanel = new JScrollPane(table);
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		mainPanel.setLayout(gridBagLayout);

		// Add table panel
        GridBagConstraints constraints = getGridBagConstraints(GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, 0, 0, new Insets(0, 0, 0, 0), 0.1, 0.1);
        gridBagLayout.setConstraints(tableScrollPanel, constraints);
		mainPanel.add(tableScrollPanel);

		// Register actions
		registerActions();
		
		// Add buttons panel
		buttonsPanel = new SingleTemplateIteratorButtonsPanel(this);
		constraints = getGridBagConstraints(GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, 0, 1, new Insets(0, 0, 0, 0), 0, 0); 
		gridBagLayout.setConstraints(buttonsPanel, constraints);
		mainPanel.add(buttonsPanel);

		// Add separator
		JLabel separator = new JLabel();
		separator.setBorder(BorderFactory.createLineBorder(Color.GRAY));
		constraints = getGridBagConstraints(GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 0, 2, new Insets(0, 0, 0, 0), 0.0, 0.0); 
		gridBagLayout.setConstraints(separator, constraints);
		mainPanel.add(separator);
        
        add(createDialogMainPanel(mainPanel, new OkButtonActionListener(), new CancelButtonActionListener()) );
		
		setScrollPanelSize(tableScrollPanel);
		
		// Update objects related from actions  
		updateRelatedObjects();
		
		mainPanel.setBackground(Color.LIGHT_GRAY);
	}

	protected void setTemplateSize()
	{
		if(!(dialog instanceof SingleTemplateIteratorDialog))
			{ throw new IllegalStateException("dialog is not instanceof SequenceIteratorDialog"); }
	
		// Get max position
		GetSpecificPositionVisitor specificVisitor = new GetSpecificPositionVisitor(GetSpecificPositionVisitor.MAX_POSITION);
		ConfigWalker specificWalker = new ConfigWalker(sectionConfig, specificVisitor);
		specificWalker.process();
		
		int maxPosition = specificVisitor.getSpecificPosition();
		
		OptionsConfig optionsConfig = sectionConfig.getOptions();
	
		SequenceTemplateIteratorConfig sequenceIterator = (SequenceTemplateIteratorConfig)optionsConfig.getTemplateIterator(OptionsConfig.SEQUENCE_TEMPLATE_ITERATOR);
		SingleTemplateIteratorConfig singleIterator     = (SingleTemplateIteratorConfig)optionsConfig.getTemplateIterator(OptionsConfig.SINGLE_TEMPLATE_ITERATOR);

		SingleTemplateIteratorPanel singlePanel = (SingleTemplateIteratorPanel)panel; 
		
		if(sequenceIterator.getTemplateSize() < maxPosition)
		{
			// Set position into sequence panel
			SequenceTemplateIteratorPanel sequencePanel = 
                optionsDialog.getTemplateIteratorTabPanel().getSequenceTemplateIteratorPanel();
			sequencePanel.setSizeValue(maxPosition);
		}

		if(singleIterator.getTemplateSize() < maxPosition)
		{
			// Set position into single panel
			singlePanel.setSizeValue(maxPosition);
		}
	}
	
	private void initTable()
	{
		table = new SingleTemplateIteratorTable(instructionConfigSet, this);
		
		table.setModel(model);
		
		table.setUI(new SingleTemplateIteratorTableUI(model));
		
		// Deny column reordering
		table.getTableHeader().setReorderingAllowed(false);
		
		// Add columns
		model.addColumn("Position");
		model.addColumn("Instruction");

		// Init column sizes
		initColumnSizes();
		
		// Get rows
		GetPositionSetVisitor visitor = new GetPositionSetVisitor();
		ConfigWalker walker = new ConfigWalker(sectionConfig, visitor);
		walker.process();
		
		HashMap<Integer, HashSet<InstructionConfig>> positionMap = visitor.getPositionMap();
		
		Set<Integer> positions = positionMap.keySet();

		Object[] positionsArray = positions.toArray();
		
		// Sort positions 
//		Arrays.sort(positionsArray);
		
		int position;

		// Add rows
		for(int i = 0; i < positionsArray.length; ++i)
		{
			position = (Integer)positionsArray[i];
			HashSet<InstructionConfig> instructionSet = positionMap.get(position);

			for(InstructionConfig instruction: instructionSet)
			{ 
				HashSet<Integer> positionsSet = instruction.getPositions();

				if(positionsSet.isEmpty() )
				{
					Object row[] = { ALL_POSITIONS, instruction.toString() };
					model.addRow(row);
				}
				else
				{
					Object[] instructionArray = positionsSet.toArray();
					// TODO: all positions
					Object row[] = {Integer.toString((Integer)instructionArray[0]) , instruction.toString() };
					model.addRow(row);
				}
			}
			
			initiatedInstructions = instructionSet;
		}
		
		table.setOpaque(true);

		// Add listeners
		table.getSelectionModel().addListSelectionListener(this);
	}
	
	public void initColumnSizes()
	{
    	TableColumn column = null;

    	for (int i = 0; i < table.getColumnCount(); i++) 
    	{
    	    column = table.getColumnModel().getColumn(i);
    	    
    	    if(i == POSITION_COLUMN)
    		{
    	    	column.setMaxWidth(75);
    		}
    	}
	}
	
    public void setScrollPanelSize(JScrollPane tableScrollPanel)
    {
        // Set sizes
        int preferredWidth = tableScrollPanel.getMinimumSize().width;
        int minimumHeight = 23;
        int preferredHeight = minimumHeight + table.getPreferredSize().height + 120;
        
        tableScrollPanel.setPreferredSize(new Dimension(preferredWidth, preferredHeight) );
    }
    
    public void registerActions()
    {
    	actionSet.add(new AddAction(table, model, mainPanel));
    	actionSet.add(new RemoveAction(table, model, mainPanel, this));
    	actionSet.add(new MergeAction(table, model, mainPanel));
    	actionSet.add(new SplitAction(table, model, mainPanel));
    }
    
    public void updateRelatedObjects()
    {
    	buttonsPanel.changeEnabled();
    }
    
    public SingleTemplateIteratorAction getAction(Class classType)
    {
    	Iterator<SingleTemplateIteratorAction> iterator = actionSet.iterator(); 
    	
    	while(iterator.hasNext())
    	{
    		SingleTemplateIteratorAction action = iterator.next();
    		
    		if(classType.equals(action.getClass())) { return action; }
    	}
    	
    	return null;
    }
    
    // Selection changes
	public void valueChanged(ListSelectionEvent event) 
	{
		updateRelatedObjects();
	} 
	
	public InstructionConfig getInstructionConfig(String name)
	{
		for(InstructionConfig instruction: instructionConfigSet)
		{
			if((instruction != null) && (name != null) &&
               name.equals(instruction.toString()) )
			{
				return instruction;
			}
		}
		
		return null;
	}
    
	// Getter methods
	public HashSet<InstructionConfig> getRemovedInstructions()
	{
		HashSet<InstructionConfig> removedInstructions = new HashSet<InstructionConfig>();
		
		HashSet<InstructionConfig> resultingInstructions = new HashSet<InstructionConfig>();
		
		// Get set of resulting instructions 
		for(int i = 0; i < model.getRowCount(); i++)
		{
			String name = (String)(model.getValueAt(i, INSTRUCTION_COLUMN));
			InstructionConfig config = dialog.getInstructionConfig(name);
			
			resultingInstructions.add(config);
		}
		
		
		for(InstructionConfig instruction: initiatedInstructions)
		{
			if(!resultingInstructions.contains(instruction))
			{
				removedInstructions.add(instruction);
			}
		}
		
		return removedInstructions;
	}
	
	public class OkButtonActionListener implements ActionListener
	{
		protected boolean checkPrecondition()
		{
			for(int i = 0; i < model.getRowCount(); i++)
			{
				String name = (String)(model.getValueAt(i, INSTRUCTION_COLUMN));
				InstructionConfig config = dialog.getInstructionConfig(name);
				
				if(config == null)
				{
					// Show warning
					frame.showWarningMessage("Instruction \"" + name + "\" not exists", "Error");
					
					return false;
				}
			}
			
			return true;
		}
		
		public void actionPerformed(ActionEvent e) 
		{
			if(!checkPrecondition()) { return; }
			
			// Create new hash map
			HashMap<InstructionConfig, HashSet<Integer>> hashMap = new HashMap<InstructionConfig, HashSet<Integer>>();
			HashSet<InstructionConfig> sharedInstructionSet = new HashSet<InstructionConfig>();
			
			for(int i = 0; i < model.getRowCount(); i++)
			{
				String name = (String)(model.getValueAt(i, INSTRUCTION_COLUMN));
				InstructionConfig config = dialog.getInstructionConfig(name);
				
				if(sharedInstructionSet.contains(config))
					{ continue;	}
				
				int position = 0;
				
				try
				{
					position = Integer.parseInt((String)model.getValueAt(i, POSITION_COLUMN).toString());
				}
				catch(NumberFormatException numberFormatException)
				{
					String positionValue = (String)model.getValueAt(i, POSITION_COLUMN);
					
					if(positionValue.equals(ALL_POSITIONS) || positionValue.equals(""))
					{
						// All positions
						position = -1;
						
						// Add instruction into shared set
						sharedInstructionSet.add(config);
					}
				}
				
				HashSet<Integer> positionSet;

				if(position != -1)
				{
					if(hashMap.containsKey(config))
					{
						// Get old set
						positionSet = hashMap.get(config);
					}
					else
					{
						// Create new set
						positionSet = new HashSet<Integer>();
						
						// Add new set into map
						hashMap.put(config, positionSet);
					}
					
					// Add position
					positionSet.add(position);
				}
				else
				{
					// Put empty set
					hashMap.put(config, new HashSet<Integer>());
				}

				// Set selection for tested instructions
				if(!config.isSelected())
				{
					// Set config selected
					TestSelectionConfigAction testAction = new TestSelectionConfigAction(frame, config);
					testAction.execute();
				}
			}
			
			// Remove selection for deleted instructions
			HashSet<InstructionConfig> removedInstructions = dialog.getRemovedInstructions();
			
			for(InstructionConfig removedInstruction: removedInstructions)
			{
				boolean isRemoved = true;
				for(int row = 0; row < table.getRowCount(); row++)
				{
					InstructionConfig instruction = dialog.getInstructionConfig((String)model.getValueAt(row, INSTRUCTION_COLUMN));
					
					if(instruction == removedInstruction) { isRemoved = false; break; }
				}
				
				// Remove selection
				if(isRemoved)
				{
					// Unset config selected
					TestSelectionConfigAction testAction = new TestSelectionConfigAction(frame, removedInstruction);
					testAction.execute();
				}
			}
			
			// Set position for instructions
			SetPositionSetVisitor visitor = new SetPositionSetVisitor(hashMap);
			ConfigWalker walker = new ConfigWalker(sectionConfig, visitor);
			walker.process();
		
			setTemplateSize();

			dialog.dispose();
		}
	}
	
	public class CancelButtonActionListener implements ActionListener
	{
		public void actionPerformed(ActionEvent arg0) 
		{
			dialog.dispose();
		}
	}
	
    public class SingleTemplateIteratorButtonsPanel extends JPanel implements ActionRelatedObject
    {
		private static final long serialVersionUID = 0L;
		
		protected JButton addButton    = new JButton("Add");
    	protected JButton removeButton = new JButton("Remove");
		protected JButton mergeButton  = new JButton("Merge");
    	protected JButton splitButton  = new JButton("Split");
    	
    	protected HashMap<Object, SingleTemplateIteratorAction> relatedActions = new HashMap<Object, SingleTemplateIteratorAction>();
    	
    	public SingleTemplateIteratorButtonsPanel(SingleTemplateIteratorDialog dialog)
    	{
    		// Set buttons size
    		HashSet<JButton> buttonSet = new HashSet<JButton>();
    		buttonSet.add(addButton);
    		buttonSet.add(removeButton);
    		buttonSet.add(mergeButton);
    		buttonSet.add(splitButton);
    		ButtonsPanel.setMaxPreferredButtonSize(buttonSet);

    		JPanel mainPanel = new JPanel();
    		
    		mainPanel.add(addButton);
    		mainPanel.add(removeButton);
    		mainPanel.add(mergeButton);
    		mainPanel.add(splitButton);
    		
    		add(mainPanel);

    		registerAction(addButton,    dialog.getAction(AddAction.class));
    		registerAction(removeButton, dialog.getAction(RemoveAction.class));
    		registerAction(mergeButton,  dialog.getAction(MergeAction.class));
    		registerAction(splitButton,  dialog.getAction(SplitAction.class));
    		
    		AddButtonListener listener = new AddButtonListener();
    		
    		addButton.addActionListener(listener);
    		removeButton.addActionListener(listener);
    		mergeButton.addActionListener(listener);
    		splitButton.addActionListener(listener);
    	}
    	
		public void changeEnabled() 
		{
			addButton.setEnabled(getAction(addButton).isEnabled());
			removeButton.setEnabled(getAction(removeButton).isEnabled());
			mergeButton.setEnabled(getAction(mergeButton).isEnabled());
			splitButton.setEnabled(getAction(splitButton).isEnabled());
		}
		
		public void registerAction(Object object, SingleTemplateIteratorAction action)
		{
			relatedActions.put(object, action);
		}
		
		public SingleTemplateIteratorAction getAction(Object object)
		{
			return relatedActions.get(object);
		}
		
	    public class AddButtonListener implements ActionListener
	    {
			public void actionPerformed(ActionEvent event) 
			{
				// Execute action
				getAction(event.getSource()).execute();
				
				// Update action related objects
				changeEnabled();
				
				// Update table size
				setScrollPanelSize(tableScrollPanel);
			}
	    }
    }
}