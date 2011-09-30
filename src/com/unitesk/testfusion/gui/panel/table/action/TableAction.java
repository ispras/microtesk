/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: TableAction.java,v 1.8 2008/08/26 12:32:16 vorobyev Exp $
 */

package com.unitesk.testfusion.gui.panel.table.action;

/**
 * @author <a href="mailto:vorobyev@ispras.ru">Dmitry Vorobyev</a>
 */
public interface TableAction 
{
    public final static String TEST_ACTION             = "Test";
    public final static String TEST_EQUIVALENCE_ACTION = "Test Equivalence Class";
    public final static String TEST_ALL_ACTION         = "Test All"; 
   	public final static String TEST_NOTHING_ACTION     = "Nothing";
   	public final static String TEST_NOTHING_BUTTON     = "Test Nothing";
   	
	public final static String FACTORIZE_ACTION = "Factorize";
	public final static String DISENGAGE_ACTION = "Disengage";

    public final static String SELECT_ALL_ACTION         = "Select All";
    public final static String SELECT_EQUIVALENCE_ACTION = "Select Equivalence Class";
    public final static String CLEAR_ALL_ACTION          = "Clear All";

    public final static String NEW_SECTION_ACTION    = "Add Section"; 
    public final static String REMOVE_SECTION_ACTION = "Remove";
    
    public final static String NEW_TEST_ACTION    = "Add Test";
    public final static String REMOVE_TEST_ACTION = "Remove";
    
    public final static String UP_ACTION   = "Up"; 
    public final static String DOWN_ACTION = "Down";
    
	public boolean isEnabledAction();
}