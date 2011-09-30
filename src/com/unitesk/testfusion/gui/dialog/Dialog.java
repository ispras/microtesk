/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: Dialog.java,v 1.48 2008/11/12 12:26:03 kozlov Exp $
 */

package com.unitesk.testfusion.gui.dialog;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import com.unitesk.testfusion.gui.GUI;

import static com.unitesk.testfusion.gui.Layout.*;

/**
 * Abstract class for all program dialogs.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 * @author <a href="mailto:kozlov@ispras.ru">Kirill Kozlov</a>
 * @author <a href="mailto:protsenko@ispras.ru">Alexander Protsenko</a>
 */
public abstract class Dialog extends JDialog
{
    public static final long serialVersionUID = 0;
   
    /** Default width of dialog's window. */
    protected static final int WIDTH  = 480;
    
    /** Default height of dialog's window. */
    protected static final int HEIGHT = 360;
    
    /** Size of interval from OK Cancel buttons of the dialog. */
    public static final int SPACE_FROM_OK_CANCEL = 17;
    
    /** Default text field size. */
    public static final int DEFAULT_TEXT_FIELD_SIZE = 5;
    
    /** GUI frame. */
    protected GUI frame;
    
    /** Default OK button of the dialog. */
    protected JButton okButton;
    
    /** Default Cancel button of the dialog. */
    protected JButton cancelButton;
    
    /** Is dialog was closed by OK button pressing. */ 
    protected boolean okPressed;

    /**
     * Constructor.
     * 
     * @param <code>frame</code> the parent GUI frame. 
     */
    public Dialog(GUI frame)
    {
        super(frame);
        
        this.frame = frame;
        
        EmptyBorder border = new EmptyBorder(SPACE_FROM_BORDER, 
                SPACE_FROM_BORDER, SPACE_FROM_BORDER, SPACE_FROM_BORDER);  
        getRootPane().setBorder(border);
        
        setIconImage(frame.getIconImage());
    }
    
    /**
     * Creates panel with OK Cancel buttons.
     * 
     * @param <code>okButtonListener</code> the OK button listener.
     * 
     * @param <code>cancelButtonListener</code> the Cancel button listener.
     * 
     * @param <code>needCancel</code> is need Cancel button.
     * 
     * @return panel with OK and Cancel buttons if <code>needCancel</code> is
     *         <code>true</code> or only with OK button if 
     *         <code>needCancel</code> is <code>false</code>.
     */
    protected JPanel createOkCancelPanel(ActionListener okButtonListener, 
            ActionListener cancelButtonListener, boolean needCancel)
    {
        JPanel panel = new JPanel();
        panel.setAlignmentX(JPanel.RIGHT_ALIGNMENT);
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        
        cancelButton = new JButton("Cancel");
        cancelButton.setAlignmentY(Component.BOTTOM_ALIGNMENT);
        if(needCancel)
            { cancelButton.addActionListener(cancelButtonListener); }
        
        okButton = new JButton("OK");
        okButton.setAlignmentY(Component.BOTTOM_ALIGNMENT);
        okButton.setPreferredSize(new Dimension(cancelButton.getPreferredSize()));
        okButton.addActionListener(okButtonListener);
        
        panel.add(Box.createHorizontalGlue());
        panel.add(okButton);
        
        if(needCancel)
        {
            panel.add(Box.createHorizontalStrut(SPACE_BETWEEN_RELATIVE_COMPONENT));
            panel.add(cancelButton);
        }
        
        return panel;
    }
    
    /**
     * Creates main panel of the dialog.
     * 
     * @param <code>mainComponent</code> the main component of the dialog.
     *   
     * @param <code>okButtonListener</code> the OK button listener.
     * 
     * @param <code>cancelButtonListener</code> the Cancel button listener.
     * 
     * @return the panel with <code>mainComponent</code> and 
     *         OK and Cancel buttons.
     */
    protected JPanel createDialogMainPanel(JComponent mainComponent, 
            ActionListener okButtonListener, 
            ActionListener cancelButtonListener)
    {
        return createDialogMainPanel(mainComponent, okButtonListener,
                cancelButtonListener, true);
    }
    
    /**
     * Creates main panel of the dialog.
     * 
     * @param <code>mainComponent</code> the main component of the dialog.
     *   
     * @param <code>okButtonListener</code> the OK button listener.
     * 
     * @return the panel with <code>mainComponent</code> and 
     *         OK button.
     */
    protected JPanel createDialogMainPanel(JComponent mainComponent, 
            ActionListener okButtonListener)
    {
        return createDialogMainPanel(mainComponent, okButtonListener,
                null, false);
    }
    
    /**
     * Creates main panel of the dialog
     * 
     * @param <code>mainComponent</code> the main component of the dialog.
     *   
     * @param <code>okButtonListener</code> the OK button listener.
     * 
     * @param <code>cancelButtonListener</code> the Cancel button listener.
     * 
     * @param <code>needCancel</code> is need Cancel button.
     * 
     * @return the panel with <code>mainComponent</code> and 
     *         OK and Cancel buttons if <code>needCancel</code> is
     *         <code>true</code> or only with OK button if 
     *         <code>needCancel</code> is <code>false</code>.
     */
    protected JPanel createDialogMainPanel(JComponent mainComponent, 
            ActionListener okButtonListener, 
            ActionListener cancelButtonListener, boolean needCancel)
    {
        JPanel panel = new JPanel(new GridBagLayout());
        
        GridBagConstraints constraints =  getGridBagConstraints(
                GridBagConstraints.NORTH, GridBagConstraints.BOTH, 0, 0, 
                new Insets(0, 0, SPACE_FROM_OK_CANCEL, 0), 1.0, 1.0);
        panel.add(mainComponent, constraints);
        
        JPanel okCancelPanel = createOkCancelPanel(okButtonListener, 
                cancelButtonListener, needCancel);
        
        constraints = getGridBagConstraints(GridBagConstraints.SOUTHEAST, 
                GridBagConstraints.HORIZONTAL, 0, 1, 0.0, 0.0);
        panel.add(okCancelPanel, constraints);
        
        ActionListener actionSearch = new ActionListener() 
        {
            public void actionPerformed(ActionEvent e) 
            {
                dispose(); 
            }
        };
        
        KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0); 
        rootPane.registerKeyboardAction(actionSearch , stroke, 
                JComponent.WHEN_IN_FOCUSED_WINDOW);
        
        getRootPane().setDefaultButton(okButton);
        
        return panel;
    }
    
    /**
     * Sets location of the dialog.
     */
    public void setLocation()
    {
        int x, y;
        Point point = frame.getLocation();

        x = point.x;
        y = point.y;

        point.x = x + (frame.getWidth() - getWidth()) / 2;
        point.y = y + (frame.getHeight() - getHeight()) / 2;
        
        setLocation(point);
    }
    
    /**
     * Returns OK button.
     * 
     * @return OK button.
     */
    public JButton getOkButton()
    {
        return okButton;
    }
    
    /**
     * Returns Cancel button.
     * 
     * @return Cancel button.
     */
    public JButton getCancelButton()
    {
        return cancelButton;
    }
    
    /**
     * Returns boolean value, wich shows is dialog was closed by Ok button
     * pressing.
     * 
     * @return boolean value, wich shows is dialog was closed by Ok button
     *         pressing.
     */
    public boolean isOkPressed()
    {
        return okPressed;
    }
}