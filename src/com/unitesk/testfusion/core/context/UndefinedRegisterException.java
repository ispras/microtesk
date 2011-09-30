/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: UndefinedRegisterException.java,v 1.2 2008/08/22 14:57:30 kamkin Exp $
 */

package com.unitesk.testfusion.core.context;

import com.unitesk.testfusion.core.model.register.Register;

/**
 * This exception is thrown when register is not initialized.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class UndefinedRegisterException extends RuntimeException
{
    public static final long serialVersionUID = 0L;
    
    /** Default constructor. */
    public UndefinedRegisterException() {}
    
    /**
     * Constructor.
     * 
     * @param <code>message</code> the error message.
     */
    public UndefinedRegisterException(String message)
    {
        super(message);
    }
    
    /**
     * Constructor.
     * 
     * @param <code>register</code> the register.
     */
    public UndefinedRegisterException(Register register)
    {
        this("Register " + register + " is undefined");
    }
}
