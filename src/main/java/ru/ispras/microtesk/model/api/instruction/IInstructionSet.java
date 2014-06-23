/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * IInstructionSet.java, Nov 23, 2012 2:46:21 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.api.instruction;

import java.util.Collection;

import ru.ispras.microtesk.model.api.metadata.MetaInstruction;

/**
 * The IInstructionSet interface is a base interfaces for container
 * classes that hold the collection of instructions. 
 * 
 * @author Andrei Tatarnikov
 */

public interface IInstructionSet
{
    /**
     * Checks whether the specified instruction is supported in the ISA.
     * 
     * @param name Instruction name.
     * @return true if the instruction is supported or false if it is not.
     */

    public boolean supportsInstruction(String name);
    
    /**
     * Returns in the instance of the specified instruction. 
     * 
     * @param name Instruction name.
     * @return Instruction object.
     */

    public IInstructionEx getInstruction(String name);
    
    /**
     * Returns meta data for instructions in the instruction set.  
     * 
     * @return A collection of instruction meta data.
     */
    
    public Collection<MetaInstruction> getMetaData();
}
