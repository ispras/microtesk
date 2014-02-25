/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * IAddressingModeBuilderEx.java, Nov 27, 2012 1:09:32 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.api.simnml.instruction;

import ru.ispras.microtesk.model.api.exception.ConfigurationException;

import ru.ispras.microtesk.model.api.instruction.IAddressingModeBuilder;

/**
 * The IAddressingModeBuilderEx interface is a Sim-nML specific extension
 * of the IAddressingModeBuilder interface that provides a possibility to
 * get the constructed addressing mode object.   
 * 
 * @author Andrei Tatarnikov
 */

public interface IAddressingModeBuilderEx extends IAddressingModeBuilder
{
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
