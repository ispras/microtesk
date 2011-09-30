/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: DependencyPanel.java,v 1.5 2008/06/19 11:26:57 kamkin Exp $
 */

package com.unitesk.testfusion.gui.panel;

import com.unitesk.testfusion.core.config.Config;
import com.unitesk.testfusion.core.config.DependencyConfig;
import com.unitesk.testfusion.gui.GUI;

/**
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class DependencyPanel extends ConfigPanel
{
    public static final long serialVersionUID = 0;
    
    public DependencyPanel(GUI frame)
    {
        super(frame);
    }
    
    public void show(DependencyConfig dependency)
    {
        super.show(dependency);
        
        setHeader("Dependency", dependency);
    }
    
    public void show(Config config)
    {
        show((DependencyConfig)config);
    }
}
