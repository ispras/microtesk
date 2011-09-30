/* 
 * Copyright (c) 2007 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: PseudoException.java,v 1.2 2008/08/15 10:08:59 kamkin Exp $
 */

package com.unitesk.testfusion.core.exception;

/**
 * Abstract class that represents a pseudo-exception. Pseudo-exception is not
 * a real exception that can occure during the exception of an instruction, it
 * is a special facility for expressing the result of instruction execution that
 * is not stored in output registers.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public abstract class PseudoException extends ProcessorException
{
    /** Default constructor. */
    public PseudoException()
    {
        super("");
    }
    
    /**
     * Constructor.
     * 
     * @param <code>name</code> the name of the pseudo-exception.
     */
    public PseudoException(String name)
    {
        super(name);
    }
}
