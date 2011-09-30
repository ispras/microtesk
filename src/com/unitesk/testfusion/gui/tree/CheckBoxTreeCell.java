/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: CheckBoxTreeCell.java,v 1.2 2008/12/25 13:47:25 kozlov Exp $
 */

package com.unitesk.testfusion.gui.tree;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;

import com.unitesk.testfusion.gui.tree.TreeLabel;


/**
 * Tree cell with checkbox and label.
 * 
 * @author <a href="mailto:protsenko@ispras.ru">Alexander Protsenko</a>
 * @author <a href="mailto:kozlov@ispras.ru">Kirill Kozlov</a>
 */
public class CheckBoxTreeCell extends JPanel 
{
    private static final long serialVersionUID = 0;

    protected JCheckBox check;
    protected TreeLabel label;

    public CheckBoxTreeCell() 
    {
        setLayout(null);
        
        add(check = new JCheckBox());
        add(label = new TreeLabel());
        
        check.setBackground(UIManager.getColor("Tree.textBackground"));
        label.setForeground(UIManager.getColor("Tree.textForeground"));
    }

    public JCheckBox getCheckBox()
    {
        return check;
    }
    
    public TreeLabel getTreeLabel()
    {
        return label;
    }
    
    public Dimension getPreferredSize() 
    {
        Dimension d_check = check.getPreferredSize();
        Dimension d_label = label.getPreferredSize();
    
        return new Dimension(d_check.width + d_label.width,
            (d_check.height < d_label.height ? d_label.height : d_check.height));
    }

    public void doLayout() 
    {
        Dimension d_check = check.getPreferredSize();
        Dimension d_label = label.getPreferredSize();
        
        int y_check = 0;
        int y_label = 0;
       
        if(d_check.height < d_label.height) 
            { y_check = (d_label.height - d_check.height) / 2; } 
        else 
            { y_label = (d_check.height - d_label.height) / 2; }
        
        check.setLocation(0, y_check);
        check.setBounds(0, y_check - 1, d_check.width, d_check.height);
        
        label.setLocation(d_check.width, y_label);
        label.setBounds(d_check.width, y_label - 1, d_label.width, d_label.height);
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
        check.setEnabled(enable);
    }
}
