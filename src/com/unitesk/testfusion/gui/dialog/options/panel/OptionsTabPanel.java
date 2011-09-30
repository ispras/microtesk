/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: OptionsTabPanel.java,v 1.3 2008/12/04 12:44:31 kozlov Exp $
 */

package com.unitesk.testfusion.gui.dialog.options.panel;

import static com.unitesk.testfusion.gui.Layout.*;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

/**
 * @author <a href="mailto:kozlov@ispras.ru">Kirill Kozlov</a>
 */
public class OptionsTabPanel extends JPanel 
{
    public static final long serialVersionUID = 0;
    
    protected static int LEFT_PANEL_WIDTH = 170;
    
    protected static Dimension PANEL_LIST_SIZE = 
        new Dimension(LEFT_PANEL_WIDTH, 150);
    
    public static int SPACE_FROM_RIGHT_EDGE = SPACE_FROM_BORDER + 10;
    
    public static final int TEXT_FIELD_SIZE = 3;
    
    //  space for compress right panel
    final int RIGHT_PANEL_DELTA = 10;
    
    // space for align left and right panel component
    final int DELTA = 2;
    
    protected void createTabPanel(String leftPanelName,
            JComponent leftPanelComp, String rightPanelName,
            JComponent rightPanelComp)
    {
        setLayout(new GridBagLayout());
        
        GridBagConstraints constraints;
        Insets insets = new Insets(0, 0, 0, 0);
        
        insets.set(SPACE_FROM_BORDER, SPACE_FROM_BORDER, 0, 0);
        constraints = getGridBagConstraints(GridBagConstraints.NORTHWEST
                , GridBagConstraints.NONE, 0, 0, insets, 0.0, 0.0);
        add(new JLabel(leftPanelName), constraints);
        
        insets.set(SPACE_BETWEEN_RELATIVE_COMPONENT + DELTA, 
                SPACE_FROM_BORDER, SPACE_FROM_BORDER, 
                SPACE_BETWEEN_DIFFERENT_COMPONENT);
        constraints = getGridBagConstraints(GridBagConstraints.NORTHWEST, 
                GridBagConstraints.NONE, 2, 1, 0, 1, insets, 0.0, 1.0);
        leftPanelComp.setPreferredSize(new Dimension(
                LEFT_PANEL_WIDTH, 
                (int)leftPanelComp.getPreferredSize().getHeight()));
        add(leftPanelComp, constraints);
        
        insets.set(0, SPACE_FROM_BORDER + RIGHT_PANEL_DELTA, 
                SPACE_FROM_BORDER, SPACE_FROM_RIGHT_EDGE);
        rightPanelComp.setBorder(new TitledBorder(rightPanelName));
        constraints = getGridBagConstraints(GridBagConstraints.NORTH,
                GridBagConstraints.HORIZONTAL, 1, 2, insets, 1.0, 1.0);
        add(rightPanelComp, constraints);
    }
    
    protected void addBlockToGrigBag(JPanel panel, int line, String labelName,
            JButton button, int spaceFromTop, int spaceFromBottom)
    {
        GridBagConstraints constraints;
        Insets insets = new Insets(0, 0, 0, 0);
        
        insets.set(spaceFromTop, 0, spaceFromBottom,
                SPACE_HORIZONTAL_SEPARATION_ELEMENTS);
        constraints = getGridBagConstraints(GridBagConstraints.EAST, 
                GridBagConstraints.NONE, 0, line, insets, 1.0, 1.0);
        panel.add(new JLabel(labelName), constraints);
        
        insets.set(spaceFromTop, 0, spaceFromBottom, 0);
        constraints = getGridBagConstraints(GridBagConstraints.WEST,
                GridBagConstraints.NONE, 1, line, insets, 1.0, 1.0);
        panel.add(button, constraints);
    }

}
