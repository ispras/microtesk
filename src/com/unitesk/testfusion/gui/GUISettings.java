/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: GUISettings.java,v 1.6 2008/08/15 07:25:29 kamkin Exp $
 */

package com.unitesk.testfusion.gui;

/**
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class GUISettings
{
	public static final int DEFAULT_CONSOLE_SIZE = 1024;
	public static final int DEFAULT_HISTORY_SIZE = 128;
	
	public static final int MAX_CONSOLE_SIZE = Integer.MAX_VALUE;
	public static final int MAX_HISTORY_SIZE = Integer.MAX_VALUE;
	
    protected int consoleSize = DEFAULT_CONSOLE_SIZE;
    protected int historySize = DEFAULT_HISTORY_SIZE;
    
    public GUISettings() {}
    
    public GUISettings(GUISettings r)
    {
        consoleSize = r.consoleSize;
        historySize = r.historySize;
    }
    
    public int getConsoleSize()
    {
        return consoleSize;
    }
    
    public void setConsoleSize(int consoleSize)
    {
        this.consoleSize = consoleSize;
    }
    
    public int getHistorySize()
    {
        return historySize;
    }
    
    public void setHistorySize(int historySize)
    {
        this.historySize = historySize;
    }
    
    public GUISettings clone()
    {
        return new GUISettings(this);
    }
}
