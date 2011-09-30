/* Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: SectionNode.java,v 1.1 2008/08/18 13:22:11 kozlov Exp $
 */

package com.unitesk.testfusion.gui.tree.test;

import com.unitesk.testfusion.core.config.SectionConfig; 

/**
 * @author <a href="mailto:kozlov@ispras.ru">Kirill Kozlov</a>
 */
public class SectionNode extends TestTreeNode 
{
	public static final long serialVersionUID = 0;
    
    public static final String NODE_ID = "SECTION_NODE";
    
    protected SectionConfig section;
    
    public SectionNode(SectionConfig section)
    {
        super(NODE_ID, section);
        
        this.section = section;
        
        for(int i = 0; i < section.countSection(); i++)
        {
        	add(new SectionNode(section.getSection(i)));
        }
    }
}
