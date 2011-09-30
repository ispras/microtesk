/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: OperandTypeMismatchException.java,v 1.2 2008/08/26 12:17:50 kamkin Exp $
 */

package com.unitesk.testfusion.core.engine;

import com.unitesk.testfusion.core.model.Operand;

/**
 * Operand type mismatch exception.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class OperandTypeMismatchException extends RuntimeException
{
    public static final long serialVersionUID = 0L;
    
    /** Default constructor. */
    public OperandTypeMismatchException() {}
    
    /**
     * Constructor.
     * 
     * @param <code>message</code> the message.
     */
    public OperandTypeMismatchException(String message)
    {
        super(message);
    }
    
    /**
     * Constructor.
     * 
     * @param <code>operand</code> the operand type.
     */
    public OperandTypeMismatchException(Operand operand)
    {
        this(operand.toString());
    }
}
