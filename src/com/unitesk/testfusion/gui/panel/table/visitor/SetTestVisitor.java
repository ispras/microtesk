package com.unitesk.testfusion.gui.panel.table.visitor;

import com.unitesk.testfusion.core.config.SituationConfig;
import com.unitesk.testfusion.core.config.walker.ConfigEmptyVisitor;

public class SetTestVisitor extends ConfigEmptyVisitor
{
	protected boolean isSelected;
	
	public SetTestVisitor(boolean isSelected)
	{
		this.isSelected = isSelected;
	}
	
    public void onSituation(SituationConfig situation) 
    {
    	if(!situation.isEmpty())
    		{ situation.setSelectedWithPropagation(isSelected); }
    }
}
