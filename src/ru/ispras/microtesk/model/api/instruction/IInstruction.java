/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * IInstruction.java, Nov 21, 2012 3:43:47 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.api.instruction;

import ru.ispras.microtesk.model.api.metadata.IMetaInstruction;

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
     * Returns meta information on the specified instruction.
     * 
     * @return Meta data describing the given instruction.
     */

    public IMetaInstruction getMetaData();
}
