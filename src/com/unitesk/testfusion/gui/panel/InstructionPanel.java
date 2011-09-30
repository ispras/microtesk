/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: InstructionPanel.java,v 1.16 2008/07/02 12:07:41 vorobyev Exp $
 */

package com.unitesk.testfusion.gui.panel;

import java.util.HashMap;

import javax.swing.*;

import com.unitesk.testfusion.core.config.*;
import com.unitesk.testfusion.gui.GUI;
import com.unitesk.testfusion.gui.panel.table.*;

/**
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 * @author <a href="mailto:vorobyev@ispras.ru">Dmitry Vorobyev</a>
 */
public class InstructionPanel extends TablePanel
{
    public static final long serialVersionUID = 0;
    
    public InstructionPanel(GUI frame)
    {
        super(frame);
        
        HashMap<Integer, Integer> columnHash     = new HashMap<Integer, Integer>();
		HashMap<Integer, String> columnNamesHash = new HashMap<Integer, String>(); 
		
		columnHash.put(0, TableModel.TEST_COLUMN);
		columnHash.put(1, TableModel.NAME_COLUMN);
		columnHash.put(2, TableModel.DESCRIPTION_COLUMN);
		
		setColumnHash(columnHash);

	    columnNamesHash.put(TableModel.TEST_COLUMN, "Test");
	    columnNamesHash.put(TableModel.NAME_COLUMN, "Situations");
	    columnNamesHash.put(TableModel.DESCRIPTION_COLUMN, "Description");
	    
		setColumnNamesHash(columnNamesHash);
		
		setOperandTableEnabled(true);
    }
    
    public JComponent createCustomComponent()
    {
        return super.createCustomComponent();
    }
    
    public void show(InstructionConfig instruction)
    {
        setHeader("Instruction", instruction);
 
        super.show(instruction);		
    }
    
    public void show(Config config)
    {
        show((InstructionConfig)config);
    }
}
