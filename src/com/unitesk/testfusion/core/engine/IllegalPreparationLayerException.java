/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: IllegalPreparationLayerException.java,v 1.2 2008/08/26 12:17:50 kamkin Exp $
 */

package com.unitesk.testfusion.core.engine;

/**
 * Illegal preparation layer exception.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class IllegalPreparationLayerException extends RuntimeException
{
    public static final long serialVersionUID = 0L;
    
    /** Default constructor. */
    public IllegalPreparationLayerException() {}
    
    /**
     * Constructor.
     * 
     * @param <code>message</code> the message.
     */
    public IllegalPreparationLayerException(String message)
    {
        super(message);
    }
}
