/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: AbstractTemplateIteratorPanel.java,v 1.1 2008/11/20 13:05:33 kozlov Exp $
 */

package com.unitesk.testfusion.gui.dialog.options.panel.iterator;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.unitesk.testfusion.core.config.OptionsConfig;
import com.unitesk.testfusion.gui.dialog.OptionsDialog;

import static com.unitesk.testfusion.gui.Layout.*;

/**
 * Class for panel with a some template iterator parameters. Panel contains 
 * lines, each of which can contains label with text field or button. 
 * 
 * @author <a href="mailto:kozlov@ispras.ru">Kirill Kozlov</a>
 */
public class AbstractTemplateIteratorPanel extends JPanel
{
    public static final long serialVersionUID = 0;
    
    public static final int TEXT_FIELD_SIZE = 3;
    
    /** Current options configuration. */
    protected OptionsConfig currentOptions;
    
    /** Number of current line on panel. */
    protected int currentLine;
    
    /**
     * Constructor.
     * 
     * @param <code>dialog</code> the options dialog.
     */
    protected AbstractTemplateIteratorPanel(OptionsDialog dialog)
    {
        currentOptions = dialog.getCurrentOptionsConfig();
        
        setLayout(new GridBagLayout());
    }
    
    /**
     * Adds the label and the text field on the panel.
     * 
     * @param <code>labelText</code> the label's text.
     * 
     * @param <code>textField</code> the text field.
     * 
     * @param <code>lastInGroup</code> shows, if this elements are the last in
     *        group of the same elemets. In other words, if the next element in 
     *        the panel is going to be not label with text field.
     */
    protected void addLabelAndTextField(String labelText, JTextField textField,
            boolean lastInGroup)
    {
        Insets insets = new Insets(0, SPACE_FROM_BORDER,
                (lastInGroup ? SPACE_BETWEEN_DIFFERENT_COMPONENT :
                    SPACE_BETWEEN_RELATIVE_COMPONENT), 0);
        
        GridBagConstraints constraints = getGridBagConstraints(
                GridBagConstraints.EAST, GridBagConstraints.NONE, 0,
                currentLine, insets, 1.0, 1.0);
        
        add(new JLabel(labelText), constraints);
        
        insets = new Insets(0, SPACE_HORIZONTAL_SEPARATION_ELEMENTS,
                (lastInGroup ? SPACE_BETWEEN_DIFFERENT_COMPONENT :
                    SPACE_BETWEEN_RELATIVE_COMPONENT), 
                    SPACE_FROM_BORDER);
        
        constraints = getGridBagConstraints(GridBagConstraints.WEST,
                GridBagConstraints.NONE, 1, currentLine, insets, 0.0, 1.0);
        
        add(textField, constraints);
        
        currentLine++;
    }
    
    /**
     * Adds the button on the panel.
     * 
     * @param <code>button</code> the button.
     * 
     * @param <code>lastInGroup</code> shows, if this elements are the last in
     *        group of the same elemets. In other words, if the next element in 
     *        the panel is going to be not button.
     */
    protected void addButton(JButton button, boolean lastInGroup)
    {
        Insets insets = new Insets(0, SPACE_FROM_BORDER,
                (lastInGroup ? SPACE_BETWEEN_DIFFERENT_COMPONENT :
                    SPACE_BETWEEN_RELATIVE_COMPONENT),
                    SPACE_FROM_BORDER);
        
        GridBagConstraints constraints = getGridBagConstraints(
                GridBagConstraints.CENTER, GridBagConstraints.NONE, 1, 2, 0,
                currentLine, insets, 1.0, 1.0);
        
        add(button, constraints);
        
        currentLine++;
    }
}
