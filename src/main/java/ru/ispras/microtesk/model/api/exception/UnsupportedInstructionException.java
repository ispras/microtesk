/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * UnsupportedInstructionException.java, Apr 30, 2013 12:31:22 PM Andrei
 * Tatarnikov
 */

package ru.ispras.microtesk.model.api.exception;

import ru.ispras.microtesk.model.api.exception.ConfigurationException;

public final class UnsupportedInstructionException extends ConfigurationException
{
    private static final long serialVersionUID = 67522463236330331L;

    private static final String ERROR_FORMAT = "The %s instruction is not supported in the current ISA.";

    public UnsupportedInstructionException(String name)
    {
        super(String.format(ERROR_FORMAT, name));
    }
}
