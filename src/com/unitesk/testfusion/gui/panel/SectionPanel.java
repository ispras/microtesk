/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: SectionPanel.java,v 1.7 2008/08/25 13:41:50 vorobyev Exp $
 */

package com.unitesk.testfusion.gui.panel;

import com.unitesk.testfusion.core.config.*;
import com.unitesk.testfusion.gui.GUI;
import com.unitesk.testfusion.gui.panel.table.TablePanel;

/**
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class SectionPanel extends TablePanel 
{
    public static final long serialVersionUID = 0;

    public SectionPanel(GUI frame)
    {
        super(frame);
    }
    
    public void show(ProcessorConfig processor)
    {
        super.show(processor);
        
        setHeader("Processor", processor);
    }
    
    public void show(Config config)
    {
    	show((ProcessorConfig)config);
    }
}