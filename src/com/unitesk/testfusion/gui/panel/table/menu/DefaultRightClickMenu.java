/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: DefaultRightClickMenu.java,v 1.3 2008/08/25 13:41:58 vorobyev Exp $
 */

package com.unitesk.testfusion.gui.panel.table.menu;

import com.unitesk.testfusion.gui.panel.table.TablePanel;
import com.unitesk.testfusion.gui.panel.table.action.*;

/**
 * @author <a href="mailto:vorobyev@ispras.ru">Dmitry Vorobyev</a>
 */
public class DefaultRightClickMenu extends PopupMenu
{
	private static final long serialVersionUID = 0L;

	public final String TEST_NOTHING_ACTION_MENU = "Test Nothing";
	
	public DefaultRightClickMenu()
	{
	}
	
	// Register main panel and register items
	public DefaultRightClickMenu(TablePanel panel) 
	{
		super();
		registerParams(panel);
	}
	
	public void registerParams(TablePanel panel)
	{
		super.registerParams(panel);
		
		if(panel == null || model == null || table == null)
			{ throw new IllegalStateException(); }
		
		registerItem(TableAction.TEST_ACTION, new TestAction(panel, model, table));
		registerItem(TableAction.TEST_EQUIVALENCE_ACTION, new TestEquivalenceAction(panel, model, table));
		registerItem(TableAction.TEST_ALL_ACTION, new TestAllAction(panel, model, table));
		registerItem(TEST_NOTHING_ACTION_MENU, new TestNothingAction(panel, model, table));
		registerSeparator();
		
		registerItem(TableAction.FACTORIZE_ACTION, new FactorizeAction(panel.getFrame(), panel, model, table));
		registerItem(TableAction.DISENGAGE_ACTION, new DisengageAction(panel, model, table));
		registerSeparator();
		
		registerItem(TableAction.SELECT_ALL_ACTION, new SelectAllAction(panel, model, table));
		registerItem(TableAction.SELECT_EQUIVALENCE_ACTION, new SelectEquivalenceAction(panel, model, table));
		registerItem(TableAction.CLEAR_ALL_ACTION, new ClearAllAction(panel, model, table));
		
		setActivationOfItems();
	}
}
