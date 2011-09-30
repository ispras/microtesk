/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: SingleTemplateIteratorAction.java,v 1.1 2008/09/12 14:06:46 vorobyev Exp $
 */

package com.unitesk.testfusion.gui.dialog.options.action;

import java.util.HashSet;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import com.unitesk.testfusion.gui.dialog.options.SingleTemplateIteratorTable;

/**
 * @author <a href="mailto:vorobyev@ispras.ru">Dmitry Vorobyev</a>
 */
public abstract class SingleTemplateIteratorAction 
{
	protected SingleTemplateIteratorTable table;
	protected DefaultTableModel model;
	protected JPanel mainPanel;

	public abstract void execute();
	public abstract boolean isEnabled();

	public HashSet<Object> relatedObjects = new HashSet<Object>();
	
	public SingleTemplateIteratorAction(SingleTemplateIteratorTable table, DefaultTableModel model, JPanel mainPanel)
	{
		this.table = table;
		this.model = model;
		this.mainPanel = mainPanel;
	}
	
	public void relateObject(Object object)
	{
		relatedObjects.add(object);
	}
}
