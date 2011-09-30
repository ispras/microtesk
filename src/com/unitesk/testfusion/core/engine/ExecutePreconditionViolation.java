/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: ExecutePreconditionViolation.java,v 1.2 2008/08/26 10:00:26 kamkin Exp $
 */

package com.unitesk.testfusion.core.engine;

import com.unitesk.testfusion.core.model.Instruction;

/**
 * Exception of precondition violation of the <code>execute()</code> method
 * of <code>Instruction</code>.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class ExecutePreconditionViolation extends RuntimeException
{
    public static final long serialVersionUID = 0L;
    
    /** Default constructor. */
    public ExecutePreconditionViolation() {}
    
    /**
     * Constructor.
     * 
     * @param <code>message</code> the message.
     */
    public ExecutePreconditionViolation(String message)
    {
        super(message);
    }
    
    /**
     * Constructor.
     * 
     * @param <code>instruction</code> the instruction.
     */
    public ExecutePreconditionViolation(Instruction instruction)
    {
        this("Precondition violation for " + instruction);
    }
}
