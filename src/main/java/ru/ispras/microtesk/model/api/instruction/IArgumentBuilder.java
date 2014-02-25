/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * IArgumentBuilder.java, Nov 6, 2012 3:30:37 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.api.instruction;

import ru.ispras.microtesk.model.api.exception.ConfigurationException;

/**
 * The IArgumentBuilder interface provides methods that help set up 
 * the specified instruction argument.
 * 
 * @author Andrei Tatarnikov
 */

public interface IArgumentBuilder
{
    /**
     * Returns a builder for an addressing mode argument
     * (applicable for arguments that represent addressing mode). 
     * 
     * @param name The name of the addressing mode.
     * @return An addressing mode builder.
     */

    public IAddressingModeBuilder getModeBuilder(String name) throws ConfigurationException;
}
