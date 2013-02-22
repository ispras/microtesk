/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * UnsupportedOperationException.java, Nov 29, 2012 12:06:05 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.api.exception.simulation;

import ru.ispras.microtesk.model.api.exception.SimulationException;

public class UnsupportedOperationException extends SimulationException
{

    private static final long serialVersionUID = -2271633628235423747L;

    public UnsupportedOperationException()
    {
        // TODO Auto-generated constructor stub
    }

    public UnsupportedOperationException(String message)
    {
        super(message);
        // TODO Auto-generated constructor stub
    }

    public UnsupportedOperationException(Throwable cause)
    {
        super(cause);
        // TODO Auto-generated constructor stub
    }

    public UnsupportedOperationException(String message, Throwable cause)
    {
        super(message, cause);
        // TODO Auto-generated constructor stub
    }

}
