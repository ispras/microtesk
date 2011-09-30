/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: OperandTableModel.java,v 1.5 2008/07/07 10:26:54 protsenko Exp $
 */

package com.unitesk.testfusion.gui.panel.table;

import javax.swing.table.DefaultTableModel;

/**
 * @author <a href="mailto:protsenko@ispras.ru">Alexander Protsenko</a>
 */
public class OperandTableModel extends DefaultTableModel 
{
    /**
     * serialVersionUID = -4218125184764956384L;
     */
    private static final long serialVersionUID = -4218125184764956384L;
    
     String[] columnNames;
     Object[][] data;
    
    public OperandTableModel(String[] columnNames, Object[][] data)
    {
        super();

        for(int i = 0; i < columnNames.length; i++)
        {
            addColumn(columnNames[i], data[i]);
        }
        
        this.columnNames = columnNames;
        this.data = data;
    }

    public OperandTableModel()
    {
        super();
    }
    
    public Integer getRow(int row)
    {
        return row;
    }
}
