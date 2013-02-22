/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * MicroTESKException.java, Nov 16, 2012 7:10:45 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.api.exception;

/**
 * The MicroTESKException class is the base abstract class
 * for all exceptions that can occur in a microprocessor model. 
 * 
 * @author Andrei Tatarnikov
 */

public abstract class MicroTESKException extends Exception
{
    private static final long serialVersionUID = -9200888003929598524L;

    public MicroTESKException()
    {
        // TODO Auto-generated constructor stub
    }

    public MicroTESKException(String message)
    {
        super(message);
        // TODO Auto-generated constructor stub
    }

    public MicroTESKException(Throwable cause)
    {
        super(cause);
        // TODO Auto-generated constructor stub
    }

    public MicroTESKException(String message, Throwable cause)
    {
        super(message, cause);
        // TODO Auto-generated constructor stub
    }

}
