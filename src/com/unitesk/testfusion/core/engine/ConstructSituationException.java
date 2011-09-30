/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: ConstructSituationException.java,v 1.3 2008/08/26 10:00:26 kamkin Exp $
 */

package com.unitesk.testfusion.core.engine;

import com.unitesk.testfusion.core.situation.Situation;

/**
 * Exception of postcondition violation of the <code>construct()</code> method
 * of <code>Situation</code>.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class ConstructSituationException extends RuntimeException
{
    public static final long serialVersionUID = 0L;
    
    /** Default constructor. */
    public ConstructSituationException() {}
    
    /**
     * Constructor. 
     * 
     * @param <code>message</code> the message.
     */
    public ConstructSituationException(String message)
    {
        super(message);
    }
    
    /**
     * Constructor.
     * 
     * @param <code>situation</code> the situation.
     */
    public ConstructSituationException(Situation situation)
    {
        this("Incorrect construction of test situation " + situation + " for " + situation.getInstruction());
    }
}
