/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: TablePanelElement.java,v 1.1 2008/08/25 13:42:26 vorobyev Exp $
 */

package com.unitesk.testfusion.gui.panel.table;

/**
 * @author <a href="mailto:vorobyev@ispras.ru">Dmitry Vorobyev</a>
 */
public class TablePanelElement 
{
	protected boolean isRegistered = false;
	protected Object element;
	protected Class elementClass;
	
	public TablePanelElement(Class elementClass)
	{
		this.elementClass = elementClass;
	}
	
	public void registerElement(Object element)
	{
		if(!elementClass.isInstance(element))
			{ throw new IllegalStateException("element.getClass()=" + element.getClass() + " " + 
					                          "elementClass=" + elementClass); }
		
		this.element = element;
		
		setRegistered(true);
	}
	
	public void setRegistered(boolean isRegistered)
	{
		this.isRegistered = isRegistered;
	}
	
	public Object getObject()
	{
		if(element == null) { throw new IllegalStateException(); }
		
		return element;
	}
	
	public boolean isRegistered()
	{
		return isRegistered;
	}
}
