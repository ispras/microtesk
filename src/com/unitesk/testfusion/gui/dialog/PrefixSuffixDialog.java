/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: PrefixSuffixDialog.java,v 1.7 2008/10/15 11:48:05 kozlov Exp $
 */

package com.unitesk.testfusion.gui.dialog;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Font;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTabbedPane;

import com.unitesk.testfusion.gui.GUI;

import com.unitesk.testfusion.core.model.Program;

/**
 * Class for dialog, which shows prefixes and suffixes of test program and
 * test actions.
 * 
 * @author <a href="mailto:kozlov@ispras.ru">Kirill Kozlov</a>
 */
public class PrefixSuffixDialog extends Dialog 
{
	public static final long serialVersionUID = 0;
	
	protected static final int DEFAULT_WIDTH = 300;
	protected static final int DEFAULT_HEIGHT= 400;
	
	protected static final String DEFAULT_FONT    = "Courier New";
	protected static final int DEFAULT_FONT_STYLE = 0;
	protected static final int DEFAULT_FONT_SIZE  = 12; 
	
	/** Program for target processor. */
	Program targetProcProgram;
	
	/** Program for control processor. */
	Program controlProcProgram;
	
    /**
     * Constructor.
     * 
     * @param <code>frame</code> the GUI frame.
     * 
     * @param <code>targetProcProgram</code> the program for target processor.
     *   
     * @param <code>controlProcProgram</code> the program for control 
     *        processor.
     * 
     * @param <code>dialogName</code> the dialog's name.
     */
	public PrefixSuffixDialog(GUI frame, Program targetProcProgram,
            Program controlProcProgram, String dialogName)
	{
		super(frame);
		
		this.targetProcProgram  = targetProcProgram;
		this.controlProcProgram = controlProcProgram;
		
		final PrefixSuffixDialog dialog = this;
		
		setTitle(GUI.APPLICATION_NAME + " - " + dialogName);
        setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        setResizable(false);
        setModal(true);
        setLocation();
        
        ActionListener okListener = new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                okPressed = true;
            	dialog.dispose();
            }
        };
        
        JTabbedPane panel = new JTabbedPane(JTabbedPane.TOP, 
                JTabbedPane.SCROLL_TAB_LAYOUT);
        
        panel.addTab("Target Test", (targetProcProgram == null) ? 
                null : createProgramViewPanel(targetProcProgram));
        
        panel.addTab("Control Test",(controlProcProgram == null) ?
                null : createProgramViewPanel(controlProcProgram));
        
        // set tab disable if corresponding program is null
        panel.setEnabledAt(0, targetProcProgram != null);
        panel.setEnabledAt(1, controlProcProgram != null);
        
        /*
         * set Control Test as current tab, when target program is null 
         * and control program is not null. Set Target Test as current tab
         * in all others cases.
         */ 
        panel.setSelectedIndex(
                (targetProcProgram == null && controlProcProgram != null) ? 
                        1 : 0);
        
        add(createDialogMainPanel(panel, okListener));
	}
	
    
    /**
     * Returns scroll panel with text area, which contains program's
     * assembler code.
     * 
     * @param <code>program</code> the program.
     * 
     * @return scroll panel with text area, which contains program's
     * assembler code.
     */
	protected JScrollPane createProgramViewPanel(Program program)
	{
		JTextArea textArea = new JTextArea();
		textArea.setFont(new Font(DEFAULT_FONT, DEFAULT_FONT_STYLE,
                DEFAULT_FONT_SIZE));
		
		textArea.setEditable(false);
		
		for (int i = 0; i< program.countInstruction(); i++)
		{
			textArea.append(program.getInstruction(i).toString() + "\n");
		}
        
        /*
         * help to show the start position of the text area, without this
         * operator vertical scrollbar of scrollpane is located in 
         * bottom state.
         */  
        textArea.setCaretPosition(0);
        
        return new JScrollPane(textArea);
	}
}
