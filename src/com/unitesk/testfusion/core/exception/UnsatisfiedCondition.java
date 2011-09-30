/* 
 * Copyright (c) 2007 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: UnsatisfiedCondition.java,v 1.2 2008/08/15 10:08:59 kamkin Exp $
 */

package com.unitesk.testfusion.core.exception;

/**
 * Pseudo-exception that represents unsatisfied condition in conditional jumps,
 * moves, and other types of instructions.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class UnsatisfiedCondition extends PseudoException
{
    /** Default constructor. */
    public UnsatisfiedCondition()
    {
        super("Unsatisfied Condition");
    }
}
