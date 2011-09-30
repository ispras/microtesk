/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: GroupPanel.java,v 1.23 2008/08/08 10:51:37 kamkin Exp $
 */

package com.unitesk.testfusion.gui.panel;

import java.util.HashMap;

import com.unitesk.testfusion.core.config.*;
import com.unitesk.testfusion.gui.GUI;
import com.unitesk.testfusion.gui.panel.table.*;

/**
 * @author <a href="mailto:vorobyev@ispras.ru">Dmitry Vorobyev</a>
 */
public class GroupPanel extends TablePanel
{
	private static final long serialVersionUID = 0L;
	
	public GroupPanel(GUI frame) 
	{
		super(frame);

		HashMap<Integer, Integer> columnHash = new HashMap<Integer, Integer>();
		
		columnHash.put(0, TableModel.TEST_COLUMN);
		columnHash.put(1, TableModel.NAME_COLUMN);
		columnHash.put(2, TableModel.EQUIVALENCE_COLUMN);
		columnHash.put(3, TableModel.SITUATIONS_COLUMN);
		
		setColumnHash(columnHash);
	}
	
	public void show(GroupConfig config)
	{
		super.show(config);
		setHeader("Group", config);
	}
	
    public void show(Config config)
    {
    	show((GroupConfig)config);
    }
}
