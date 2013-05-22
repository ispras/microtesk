/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * ConstraintSolverException.java, May 22, 2013 6:00:32 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.api.exception.config;

import ru.ispras.microtesk.model.api.exception.ConfigurationException;

public final class ConstraintSolverException extends ConfigurationException
{
    private static final long serialVersionUID = -3577098499365476766L;

    public ConstraintSolverException(String message)
    {
        super(message);
    }
}
