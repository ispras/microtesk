/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: TestNode.java,v 1.1 2008/08/18 13:22:11 kozlov Exp $
 */

package com.unitesk.testfusion.gui.tree.test;

import com.unitesk.testfusion.core.config.TestConfig; 

/**
 * @author <a href="mailto:kozlov@ispras.ru">Kirill Kozlov</a>
 */
public class TestNode extends TestTreeNode
{
    public static final long serialVersionUID = 0;
    
    public static final String NODE_ID = "TEST_NODE";
    
    protected TestConfig test;
    
    public TestNode(TestConfig test)
    {
        super(NODE_ID, test);
        
        this.test = test;
        
        for(int i = 0; i < test.countSection(); i++)
        {
        	add(new SectionNode(test.getSection(i)));
        }
    }
}
