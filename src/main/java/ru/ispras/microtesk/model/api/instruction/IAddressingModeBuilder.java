/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * IAddressingModeBuilder.java, Nov 6, 2012 3:30:30 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.api.instruction;

import ru.ispras.microtesk.model.api.exception.ConfigurationException;

/**
 * The IAddressingModeBuilder interface allows providing an addressing mode 
 * object that is represents an argument of the specified instruction with
 * necessary argument values.
 * 
 * @author Andrei Tatarnikov
 */

public interface IAddressingModeBuilder
{
    /**
     * Initializes the specified addressing mode argument with a textual value 
     * (parsing will be performed by the model library). 
     * 
     * @param name Mode argument name.
     * @param value Mode argument value text.
     */

    public IAddressingModeBuilder setArgumentValue(String name, String value) throws ConfigurationException;

    /**
     * Initializes the specified addressing mode argument with
     * an integer value.
     * 
     * @param name Mode argument name.
     * @param value Mode argument integer value.
     */

    public IAddressingModeBuilder setArgumentValue(String name, Integer value) throws ConfigurationException;

    /**
     * Returns an addressing mode object created by the builder.
     * 
     * @return The addressing mode object.
     * @throws ConfigurationException Exception that informs of an 
     * error that occurs on attempt to build an addressing mode object
     * due to incorrect configuration.
     */

    public IAddressingMode getProduct() throws ConfigurationException;
}
