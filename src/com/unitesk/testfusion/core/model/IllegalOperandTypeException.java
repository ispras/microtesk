/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: IllegalOperandTypeException.java,v 1.2 2008/08/18 14:45:54 kamkin Exp $
 */

package com.unitesk.testfusion.core.model;

/**
 * Illegal operand type exception.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class IllegalOperandTypeException extends RuntimeException
{
    public static final long serialVersionUID = 0L;
    
    /** Default constructor. */
    public IllegalOperandTypeException() {}
    
    /**
     * Constructor.
     * 
     * @param <code>message</code> the message.
     */
    public IllegalOperandTypeException(String message)
    {
        super(message);
    }
    
    /**
     * Constructor.
     * 
     * @param <code>operand</code> the operand type.
     */
    public IllegalOperandTypeException(OperandType operand)
    {
        this("Illegal operand type " + operand.getName());
    }
}
