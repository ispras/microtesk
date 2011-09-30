/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: ColorManager.java,v 1.6 2008/08/28 11:31:29 vorobyev Exp $
 */

package com.unitesk.testfusion.gui;

import java.awt.Color;

/**
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class ColorManager
{
	public static final Color DEFAULT_TEST_COLOR         = new Color(250, 255, 180);
	public static final Color DEFAULT_SECTION_COLOR      = new Color(250, 255, 210);
	public static final Color DEFAULT_GROUP_COLOR        = new Color(225, 255, 235);
    public static final Color DEFAULT_INSTRUCTION_COLOR  = new Color(225, 235, 255);
    public static final Color DEFAULT_SITUATION_COLOR    = new Color(255, 225, 235);
    public static final Color DEFAULT_OPERAND_COLOR      = new Color(225, 235, 255);
    
    public static Color zerba(Color base, int index)
    {
        int r = base.getRed();
        int g = base.getGreen();
        int b = base.getBlue();

        final double factor = 1.2;
        
        if((index & 0x1) != 0)
        {
            r /= factor;
            g /= factor;
            b /= factor;
        }
        
        return new Color(r, g, b);
    }
}
