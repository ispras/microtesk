/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: TestSuiteRightClickMenu.java,v 1.1 2008/08/18 13:20:27 vorobyev Exp $
 */

package com.unitesk.testfusion.gui.panel.table.menu;

import com.unitesk.testfusion.gui.panel.table.TablePanel;
import com.unitesk.testfusion.gui.panel.table.action.*;

/**
 * @author <a href="mailto:vorobyev@ispras.ru">Dmitry Vorobyev</a>
 */
public class TestSuiteRightClickMenu extends PopupMenu
{
	private static final long serialVersionUID = 0L;
	
	public TestSuiteRightClickMenu()
	{
	}
	
	public TestSuiteRightClickMenu(TablePanel panel) 
	{
		super();
		registerParams(panel);
	}
	
	public void registerParams(TablePanel panel)
	{
		super.registerParams(panel);
		
		registerItem(TableAction.NEW_TEST_ACTION, new AddTestAction(panel, model, table));
		registerItem(TableAction.REMOVE_TEST_ACTION, new RemoveTestAction(panel, model, table));
		registerSeparator();
		
		registerItem(TableAction.SELECT_ALL_ACTION, new SelectAllAction(panel, model, table));
		registerItem(TableAction.CLEAR_ALL_ACTION, new ClearAllAction(panel, model, table));
		
		setActivationOfItems();
	}
}
