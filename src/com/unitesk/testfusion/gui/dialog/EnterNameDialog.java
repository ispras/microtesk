/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: EnterNameDialog.java,v 1.9 2008/11/12 12:26:03 kozlov Exp $
 */

package com.unitesk.testfusion.gui.dialog;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.unitesk.testfusion.core.util.Utils;
import com.unitesk.testfusion.gui.GUI;
import com.unitesk.testfusion.gui.textfield.NonEmptyTextField;

import static com.unitesk.testfusion.gui.Layout.*;

/**
 * Abstract class for dialog with one non-empty text field. Allow to set name
 * for one parameter.
 * 
 * @author <a href="mailto:vorobyev@ispras.ru">Dmitry Vorobyev</a>
 * @author <a href="mailto:kozlov@ispras.ru">Kirill Kozlov</a>
 */
public abstract class EnterNameDialog extends NonEmptyTextFieldDialog
{
    private static final long serialVersionUID = 0L;
    
    /** Default name for OK button. */
    public final static String DIALOG_OK_BUTTON_NAME = "OK";
    
    /** Default name for Cancel button. */
    public final static String DIALOG_CANCEL_BUTTON_NAME = "Cancel";
    
    /** Non-empty text field. */
    protected NonEmptyTextField textField;

    /**
     * Actions, which are implemented in OK button listener.
     */
    protected abstract void actionsInOkButtonListener();
    
    /**
     * Base constructor.
     * 
     * @param <code>frame</code> the parent frame.
     * 
     * @param <code>dialogName</code> the dialog name.
     * 
     * @param <code>labelName</code> the parameter name, which will be 
     *        reflected as label.
     */
    public EnterNameDialog(GUI frame, String dialogName, String labelName)
    {
        super(frame);
        
        final EnterNameDialog dialog = this;
        
        setTitle(GUI.APPLICATION_NAME + " - " + dialogName);

        setModal(true);
        setLocation();
        
        JPanel panel = new JPanel();
        GridBagLayout gridBagLayout = new GridBagLayout();
        panel.setLayout(gridBagLayout);
                
        GridBagConstraints constraints;
        Insets insets;
        
        //*********************************************************************
        // TextField panel 
        //*********************************************************************
        JLabel label = new JLabel(labelName + ": ");
        
        constraints = getGridBagConstraints(GridBagConstraints.NORTHWEST, 
                GridBagConstraints.NONE, 0, 0, 0.0, 0.5);

        gridBagLayout.setConstraints(label, constraints);
        panel.add(label, constraints);
        
        textField = new NonEmptyTextField(20);
        textField.addEmptyTextFieldListener(this);
        
        insets = new Insets(0, SPACE_BETWEEN_RELATIVE_COMPONENT, 0, 0);
        constraints = getGridBagConstraints(GridBagConstraints.NORTH, 
                GridBagConstraints.HORIZONTAL, 1, 0, insets, 1.0, 0.5);

        gridBagLayout.setConstraints(textField, constraints);
        panel.add(textField, constraints);
        
        //*********************************************************************
        // Ok/Cancel panel 
        //*********************************************************************
        ActionListener okButtonListener = new ActionListener()
        {
            public void actionPerformed(ActionEvent event) 
            {
                actionsInOkButtonListener();
            }
        };
        
        ActionListener cancelButtonListener = new ActionListener()
        {
            public void actionPerformed(ActionEvent event) 
            {
                dialog.dispose();
            }
        };

        add(createDialogMainPanel(
                panel, okButtonListener, cancelButtonListener)); 

        String text = textField.getText();

        // init OK button enable
        if(text.equals("")) 
        {
            emptyFields = 1;
            okButton.setEnabled(false);
        }

        Dimension newSize = panel.getPreferredSize();
        newSize.height += 100; 
       
        // Ratio: 4 / 1.5
        newSize.width = (int) ((4 * newSize.height) / 1.5);
        
        // Set size of Dialog
        setSize(newSize);
        
        // Set dialog unresizable
        setResizable(false);
        
        // Set Location after size: order is important
        setLocation();
    }
    
    /**
     * Sets default text to non-empty text field and selects it.
     * 
     * @param <code>text</code> the string, which be set to text field.
     */
    protected void setDefaultText(String text)
    {
        if(!Utils.isNullOrEmpty(text))
        {
            textField.setText(text);
            textField.setCaretPosition(textField.getDocument().getLength());
            textField.selectAll();
        }
    }
    
    /**
     * Returns the non-empty text field.
     * 
     * @return the non-empty text field.
     */
    public NonEmptyTextField getTextField()
    {
        return textField;
    }
}
