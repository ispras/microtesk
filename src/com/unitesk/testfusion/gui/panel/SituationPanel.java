/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: SituationPanel.java,v 1.6 2008/06/23 14:44:04 vorobyev Exp $
 */

package com.unitesk.testfusion.gui.panel;

import com.unitesk.testfusion.core.config.*;
import com.unitesk.testfusion.gui.GUI;

/**
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class SituationPanel extends ConfigPanel
{
    public static final long serialVersionUID = 0;
    
    public SituationPanel(GUI frame)
    {
        super(frame);
    }
    
    public void show(SituationConfig situation)
    {
        super.show(situation);
        
        setHeader("Situation", situation);
    }
    
    public void show(Config config)
    {
        show((SituationConfig)config);
    }
}
