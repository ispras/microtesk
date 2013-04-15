/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * UndeclaredException.java, Apr 15, 2013 11:32:12 AM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.api.exception.config;

import ru.ispras.microtesk.model.api.exception.ConfigurationException;

public class UndeclaredException extends ConfigurationException
{
    private static final long serialVersionUID = -4197185882652716717L;

    public UndeclaredException(String message)
    {
        super(message);
    }
}
