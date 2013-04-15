/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * ConfigurationException.java, Nov 16, 2012 7:10:52 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.api.exception;

public abstract class ConfigurationException extends MicroTESKException
{
    private static final long serialVersionUID = -7710697576919321538L;

    public ConfigurationException()
    {
    }

    public ConfigurationException(String message)
    {
        super(message);
    }

    public ConfigurationException(Throwable cause)
    {
        super(cause);
    }

    public ConfigurationException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
