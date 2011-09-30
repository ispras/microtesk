/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: EquivalenceClassDialog.java,v 1.26 2008/08/20 14:59:40 kozlov Exp $
 */

package com.unitesk.testfusion.gui.dialog;

import javax.swing.JTable;

import com.unitesk.testfusion.gui.GUI;
import com.unitesk.testfusion.gui.panel.table.*;
import com.unitesk.testfusion.gui.panel.table.utils.EquivalenceUtils;
import com.unitesk.testfusion.gui.panel.table.utils.
    EquivalenceUtils.InstructionEquivalence;

/**
 * Dialog for set equivalence class name for instructions and groups.
 * 
 * @author <a href="mailto:vorobyev@ispras.ru">Dmitry Vorobyev</a>
 * @author <a href="mailto:kozlov@ispras.ru">Kirill Kozlov</a>
 */
public class EquivalenceClassDialog extends EnterNameDialog
{
	private static final long serialVersionUID = 0L;

    /** Table model. */
	protected TableModel model;
    
    /**
     * Base constructor.
     * 
     * @param <code>frame</code> the parent frame.
     * 
     * @param <code>model</code> the table model.
     * 
     * @param <code>table</code> the table. 
     */
	public EquivalenceClassDialog(GUI frame, final TableModel model,
            JTable table)
    {
        super(frame, "Equivalence Class", "Equivalence Class");
        
        this.model = model;
        
        EquivalenceUtils equivalenceUtils = new EquivalenceUtils(model, table);
        InstructionEquivalence instructionEquivalence = 
            equivalenceUtils.getInstructionEquivalence();
        
        String defaultName = instructionEquivalence.getEquivalenceName();
        
        setDefaultText(defaultName);
    }
    
    /**
     * Actions, which will be implemented in OK button listener.
     */
    protected void actionsInOkButtonListener()
    {
        String text = textField.getText();
        
        model.setEquivalenceClass(text); 
        
        dispose();
    }
}