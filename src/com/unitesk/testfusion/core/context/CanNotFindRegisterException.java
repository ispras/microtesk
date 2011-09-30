/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: CanNotFindRegisterException.java,v 1.2 2008/08/22 14:57:30 kamkin Exp $
 */

package com.unitesk.testfusion.core.context;

import com.unitesk.testfusion.core.model.OperandType;

/**
 * This exception is thrown when context does not contain a register of a
 * requested type.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class CanNotFindRegisterException extends RuntimeException
{
    public static final long serialVersionUID = 0L;
    
    /** Default constructor. */
    public CanNotFindRegisterException() {}
    
    /**
     * Constructor.
     * 
     * @param <code>message</code> the error message.
     */
    public CanNotFindRegisterException(String message)
    {
        super(message);
    }
    
    /**
     * Constructor.
     * 
     * @param <code>type</code> the register type.
     */
    public CanNotFindRegisterException(OperandType type)
    {
        this("Can not find register of type " + type.getName());
    }
}
