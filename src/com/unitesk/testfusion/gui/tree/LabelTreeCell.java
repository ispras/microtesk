/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: LabelTreeCell.java,v 1.2 2008/12/25 13:47:25 kozlov Exp $
 */

package com.unitesk.testfusion.gui.tree;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;

import com.unitesk.testfusion.gui.tree.TreeLabel;

/**
 * Tree cell with only one label.
 * 
 * @author <a href="mailto:protsenko@ispras.ru">Alexander Protsenko</a>
 * @author <a href="mailto:kozlov@ispras.ru">Kirill Kozlov</a>
 */
public class LabelTreeCell extends JPanel
{
    private static final long serialVersionUID = 0;

    protected TreeLabel label;

    public LabelTreeCell() 
    {
        setLayout(null);
        
        add(label = new TreeLabel());
        
        label.setForeground(UIManager.getColor("Tree.textForeground"));
        
        setTransferHandler(new TransferHandler("dragEnabled"));
    }

    public TreeLabel getTreeLabel()
    {
        return label;
    }
    
    public Dimension getPreferredSize() 
    {
        return label.getPreferredSize();
    }

    public void doLayout() 
    {
        Dimension d_label = label.getPreferredSize();
       
        label.setLocation(0, 0);
        label.setBounds(0, -1, d_label.width, d_label.height);
     }

    public void setBackground(Color color) 
    {
        if(color instanceof ColorUIResource)
            { color = null; }
        
        super.setBackground(color);
    }
    
    public void setEnabled(boolean enable)
    {
        label.setEnabled(enable);
    }
}
