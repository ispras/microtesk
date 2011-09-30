/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: StatusBar.java,v 1.18 2008/08/05 07:04:09 kamkin Exp $
 */

package com.unitesk.testfusion.gui;

import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.unitesk.testfusion.core.config.Config;
import com.unitesk.testfusion.core.config.SectionConfig;
import com.unitesk.testfusion.core.config.walker.ConfigCounter;
import com.unitesk.testfusion.core.config.walker.ConfigWalker;

/**
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class StatusBar extends JPanel
{
    public static final long serialVersionUID = 0;
    
    protected GUI frame;
    
    protected JLabel info;
    
    public int leaf_group_total;
    public int leaf_group_select;
    
    public int top_group_total;
    public int top_group_select;
    
    public int instruction_total;
    public int instruction_select;
    
    public int situation_total;
    public int situation_select;
    
    public StatusBar(GUI frame)
    {
        super(new GridLayout(1, 1));
        
        this.frame = frame;
        
        info = new JLabel();
        add(info);
        
        setBorder(BorderFactory.createBevelBorder(1));

        update();
    }
    
    public void update(Config config)
    {
    	StringBuffer buffer = new StringBuffer();
        
        ConfigCounter counter = new ConfigCounter();
        ConfigWalker walker = new ConfigWalker(config, counter);
        
        walker.process();
        
        leaf_group_total        = counter.countLeafGroup();
        leaf_group_select       = counter.countSelectedLeafGroup();
        int leaf_group_percent  = leaf_group_total == 0 ? 100 : (100 * leaf_group_select) / leaf_group_total; 
        
        top_group_total         = counter.countTopGroup();
        top_group_select        = counter.countSelectedTopGroup();
        int top_group_percent   = top_group_total == 0 ? 100 : (100 * top_group_select) / top_group_total; 
        
        instruction_total       = counter.countInstruction();
        instruction_select      = counter.countSelectedInstruction();
        int instruction_percent = instruction_total == 0 ? 100 : (100 * instruction_select) / instruction_total; 
        
        situation_total         = counter.countSituation();
        situation_select        = counter.countSelectedSituation();
        int situation_percent   = situation_total == 0 ? 100 : (100 * situation_select) / situation_total; 
        
        buffer.append(" ");
        buffer.append("Top Groups: " + top_group_select + "/" + top_group_total);
        buffer.append(" (" + top_group_percent + "%)    ");
        buffer.append("Leaf Groups: " + leaf_group_select + "/" + leaf_group_total);
        buffer.append(" (" + leaf_group_percent + "%)    ");
        buffer.append("Instructions: " + instruction_select + "/" + instruction_total);
        buffer.append(" (" + instruction_percent + "%)    ");
        buffer.append("Situations: " + situation_select + "/" + situation_total);
        buffer.append(" (" + situation_percent + "%)    ");
        
        info.setText(buffer.toString());
    }
    
    public void update()
    {
        SectionConfig config = frame.getSection();
        update(config);
    }
}
