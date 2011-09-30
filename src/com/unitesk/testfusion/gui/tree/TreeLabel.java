/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: TreeLabel.java,v 1.4 2008/12/13 14:51:24 kozlov Exp $
 */

package com.unitesk.testfusion.gui.tree;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JLabel;
import javax.swing.Icon;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;

import com.unitesk.testfusion.core.util.Utils;

/**
 * Tree label for tree's node. Tree label contains icon and text.
 * 
 * @author <a href="mailto:protsenko@ispras.ru">Alexander Protsenko</a>
 */
public class TreeLabel extends JLabel
{
	private static final long serialVersionUID = 0;
    
    /** is label selected */
    protected boolean isSelected;
    
    /** is label focused */
    protected boolean hasFocus;
    
    public void setBackground(Color color) 
    {
        if(color instanceof ColorUIResource)
            { color = null; }
        
        super.setBackground(color);
    }

    public boolean isShowing()
    {
        return true;
    }
    
    public void paint(Graphics g) 
    {
        if(!Utils.isNullOrEmpty(getText())) 
        {
            if(isSelected) 
            { 
                g.setColor(UIManager.getColor("Tree.selectionBackground")); 

                setForeground(UIManager.getColor("Tree.textBackground"));
            }     
            else
                { g.setColor(UIManager.getColor("Tree.textBackground")); }
      
            int imageOffset = 0;
            Dimension d = getPreferredSize();
            Icon currentI = getIcon();
            
            if(currentI != null) 
                { imageOffset = currentI.getIconWidth() + Math.max(0, getIconTextGap() - 1); }
            
            g.fillRect(imageOffset, 0, d.width - 1 - imageOffset, d.height);
            
            if(hasFocus) 
            {
                g.setColor(UIManager.getColor("Tree.selectionBorderColor"));
                
                setForeground(UIManager.getColor("Tree.textBackground"));
            }
            else
            {
                if(!isSelected)
                    {setForeground(UIManager.getColor("Tree.textForeground"));}
            }
        }
        
        super.paint(g);
    }
    
    public Dimension getPreferredSize() 
    {
        Dimension retDimension = super.getPreferredSize();
        
        if(retDimension != null) 
            { retDimension = new Dimension(retDimension.width + 3, retDimension.height); }
        
        return retDimension;
    }

    /**
     * Sets label selection state.
     * @param isSelected a boolean value, true is the label is selected.
     */
    public void setSelected(boolean isSelected) 
    {
        this.isSelected = isSelected;
    }
    /**
     * Sets label focus state.
     * @param hasFocus a boolean value, true if the label is focused.
     */
    public void setFocus(boolean hasFocus) 
    {
        this.hasFocus = hasFocus;
    }

}
