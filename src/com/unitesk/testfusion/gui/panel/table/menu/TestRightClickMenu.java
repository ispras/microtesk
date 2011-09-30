/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: TestRightClickMenu.java,v 1.2 2008/08/20 14:50:26 vorobyev Exp $
 */

package com.unitesk.testfusion.gui.panel.table.menu;

import com.unitesk.testfusion.gui.panel.table.TablePanel;
import com.unitesk.testfusion.gui.panel.table.action.*;

/**
 * @author <a href="mailto:vorobyev@ispras.ru">Dmitry Vorobyev</a>
 */
public class TestRightClickMenu extends PopupMenu
{
	private static final long serialVersionUID = 0L;
	
	public TestRightClickMenu()
	{
	}
	
	public TestRightClickMenu(TablePanel panel) 
	{
		super();
		registerParams(panel);
	}
	
	public void registerParams(TablePanel panel)
	{
		super.registerParams(panel);
		
		registerItem(TableAction.NEW_SECTION_ACTION, new AddSectionTableAction(panel, model, table));
		registerItem(TableAction.REMOVE_SECTION_ACTION, new RemoveSectionTableAction(panel, model, table));
		registerSeparator();
		
		registerItem(TableAction.UP_ACTION,   new UpSectionAction(panel, model, table));
		registerItem(TableAction.DOWN_ACTION, new DownSectionAction(panel, model, table));
		registerSeparator();
		
		registerItem(TableAction.SELECT_ALL_ACTION, new SelectAllAction(panel, model, table));
		registerItem(TableAction.CLEAR_ALL_ACTION, new ClearAllAction(panel, model, table));
		
		setActivationOfItems();
	}
}