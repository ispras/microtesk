/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * IInstruction.java, Apr 30, 2013 12:24:38 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.api.instruction;

import ru.ispras.microtesk.model.api.exception.ConfigurationException;
import ru.ispras.microtesk.model.api.situation.ISituation;

/**
 * The IInstruction interface is a base interface for all generated instructions.
 * 
 * @author Andrei Tatarnikov
 */

public interface IInstruction
{
    /**
     * Returns the name of the given instruction.
     * 
     * @return Instruction name.
     */

    public String getName();

    /**
     * Returns a builder that allows setting up an instruction call.
     * 
     * @return A builder for an instruction call. 
     */

    public IInstructionCallBuilderEx createCallBuilder();

    /**
     * Creates a new object of the specified test situation.
     * 
     * @param name Name of the test situation.
     * @return A new instance of the specified test situation.
     * @throws ConfigurationException An exception is thrown if no 
     * such situation is associated with the given instruction. 
     */

    public ISituation createSituation(String name) throws ConfigurationException;
}
