/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: OperandTablePanel.java,v 1.11 2008/08/19 10:18:44 kamkin Exp $
 */

package com.unitesk.testfusion.gui.panel.table;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import com.unitesk.testfusion.core.config.InstructionConfig;
import com.unitesk.testfusion.core.model.Operand;
import com.unitesk.testfusion.gui.panel.table.renderer.OperandCellRenderer;

/**
 * @author <a href="mailto:protsenko@ispras.ru">Alexander Protsenko</a>
 */
public class OperandTablePanel extends JPanel 
{
    private static final long serialVersionUID = 6776155528912039949L;

    public final static String OPERAND_COLUMN_NAME_0 = "Operand";
    public final static String OPERAND_COLUMN_NAME_1 = "Operand Type";
    public final static String OPERAND_COLUMN_NAME_2 = "Content Type"; 
    public final static String OPERAND_COLUMN_NAME_3 = "Input/Output"; 
    
    public final static int OPERAND_COLUMN_VALUE = 4; 
    
    private OperandTableModel model;
    
    JTable table;

    public OperandTablePanel(String[] columnNames, Object[][] data) 
    {
        super(new GridLayout(1,0));

        model = new OperandTableModel(columnNames, data);
        
        table = new JTable(model) ;
        
        Dimension operandScrollSize = table.getPreferredSize(); 

        setBackground(Color.BLUE);
        
        setOpaque(true);
        
        //Create the scroll pane for table
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(operandScrollSize.width, 200));
        
        //Add the scroll pane to this panel.
        add(scrollPane);
    }
    
    public OperandTablePanel(InstructionConfig instruction)
    {
        super(new GridLayout(1,0));

        String[] column = {OPERAND_COLUMN_NAME_0, OPERAND_COLUMN_NAME_1, OPERAND_COLUMN_NAME_2, OPERAND_COLUMN_NAME_3};
       
        int numOperands = instruction.getInstruction().countOperand();
        
        Object[][] data = new Object[numOperands][OPERAND_COLUMN_VALUE];
        Boolean[][] data_fixed = new Boolean[numOperands][1];
        
        model = new OperandTableModel();

        for(int i = 0; i < numOperands; i++)
        {
            // get Operand
            Operand operand = instruction.getInstruction().getOperand(i);

            data[i][0] = operand.getName();
            data[i][1] = operand.getOperandType();
            data[i][2] = operand.getContentType();
            data[i][3] = getInOut(operand.isInput(), operand.isOutput());
            
            data_fixed[i][0] = operand.isFixed();
        }
        
        for(int i = 0; i < column.length; i++)
        {
            Object[] dataColumn = new Object[numOperands];
            
            for(int j = 0; j < numOperands; j++)
            {
                dataColumn[j] = data[j][i];
            }
            
            model.addColumn(column[i], dataColumn);
        }

        table = new JTable(model) ;

        table.setCellSelectionEnabled(false);
        table.setFocusable(false);
        table.setEnabled(false);
        
        OperandCellRenderer renderer = new OperandCellRenderer(data_fixed);
        table.setDefaultRenderer(Object.class, renderer);
        
        //Dimension operandScrollSize = table.getPreferredSize(); 

        //setBackground(Color.BLUE);
        
        setOpaque(true);
        
        //Create the scroll pane for table
        JScrollPane scrollPane = new JScrollPane(table);
        
        int preferredWidth = scrollPane.getMinimumSize().width;
        int preferredHeight = scrollPane.getMinimumSize().height + table.getPreferredSize().height; 
        
        scrollPane.setPreferredSize( new Dimension(preferredWidth, preferredHeight));
        
        //Add the scroll pane to this panel.
        add(scrollPane);
    }
    
    private final static String INPUT = "Input";
    private final static String OUTPUT = "Output";
    
    private String getInOut(boolean used, boolean defined)
    {
        String inOut;
        
        if(used)
        {
            inOut = (defined ? INPUT+"/"+OUTPUT : INPUT);
        }
        else
        {
            inOut = OUTPUT;
        }
        
        return inOut;
    }
    
    public void updateTableStructure()
    {
        model.fireTableStructureChanged();
    }
    
    public void updateTableDate()
    {
        model.fireTableStructureChanged();
        model.fireTableDataChanged();
        model.fireTableDataChanged();
    }
    
    public void addRow(Object[] row)
    {
        model.addRow(row);
    }
    
    public void setValueAt(Object value, int row, int col)
    {
        model.setValueAt(value, row, col);
    }
    
    public Object getValueAt(int row, int col) 
    {
        return model.getValueAt(row, col);
    }
    
    public int getColumnCount() 
    {
        return model.getColumnCount();
    }

    public int getRowCount()
    {
        return model.getRowCount();
    }

    public  String[] columnNames()
    {
        return model.columnNames;
    }

    public  Object[][] date()
    {
        return model.data;
    }
}
