/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: Layout.java,v 1.1 2008/11/12 09:15:16 kozlov Exp $
 */

package com.unitesk.testfusion.gui;

import java.awt.GridBagConstraints;
import java.awt.Insets;

/**
 * Class contains constants,which define space between elements of GUI.
 * 
 * @author <a href="mailto:kozlov@ispras.ru">Kirill Kozlov</a>
 */
public final class Layout 
{
    /** Size of interval from border of the dialog, window, etc. */
    public static final int SPACE_FROM_BORDER = 12;
    
    /** Size of interval between relative components. */
    public static final int SPACE_BETWEEN_RELATIVE_COMPONENT = 5;
    
    /** Size of interval between different components. */
    public static final int SPACE_BETWEEN_DIFFERENT_COMPONENT = 12;
    
    /** Size of horizontal interval between components. */
    public static final int SPACE_HORIZONTAL_SEPARATION_ELEMENTS = 11;
    
    public static final GridBagConstraints getGridBagConstraints(int anchor, int fill, 
            int gridheight, int gridwidth, int gridx, int gridy, Insets insets,
            int ipadx, int ipady, double weightx,  double weighty)
    {
        GridBagConstraints constraints =  new GridBagConstraints();

        constraints.anchor = anchor; 
        constraints.fill   = fill;  
        constraints.gridheight = gridheight; 
        constraints.gridwidth  = gridwidth; 
        constraints.gridx = gridx; 
        constraints.gridy = gridy; 
        constraints.insets = insets;
        constraints.ipadx = ipadx;
        constraints.ipady = ipady;
        constraints.weightx = weightx;
        constraints.weighty = weighty;

        return constraints;
    }
    
    public static final GridBagConstraints getGridBagConstraints(int anchor, int fill,
            int gridheight, int gridwidth, int gridx, int gridy, Insets insets,
            double weightx,  double weighty)
    {
        return getGridBagConstraints(anchor, fill, gridheight, gridwidth,
                gridx, gridy, insets, 0, 0, weightx,  weighty);
    }
    
    public static final GridBagConstraints getGridBagConstraints(int anchor, int fill,
            int gridx, int gridy, Insets insets,
            double weightx, double weighty)
    {
        return getGridBagConstraints(anchor, fill, 1, 1, gridx, gridy, insets,
                weightx,  weighty);
    }
    
    public static final GridBagConstraints getGridBagConstraints(int anchor, int fill,
            int gridx, int gridy, double weightx,  double weighty)
    {
        return getGridBagConstraints(anchor, fill, gridx, gridy, 
                new Insets(0, 0, 0, 0), weightx,  weighty);
    }
}
