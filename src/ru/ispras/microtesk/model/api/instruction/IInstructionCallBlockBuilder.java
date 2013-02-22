/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * IInstructionCallBlockBuilder.java, Nov 15, 2012 10:52:35 AM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.api.instruction;

import ru.ispras.microtesk.model.api.exception.ConfigurationException;

/**
 * The IInstructionCallBlockBuilder interface provides methods for
 * setting up a block of instruction calls.
 * 
 * @author Andrei Tatarnikov
 */

public interface IInstructionCallBlockBuilder
{
    /**
     * Returns a builder for the instruction to be added to
     * the block of instruction calls. 
     * 
     * @param name The name of the instruction.
     * @return Instruction call builder object.
     */
    
    public IInstructionCallBuilder addCall(String name) throws ConfigurationException;
    
    /**
     * Returns an object that describes created and initialized
     * block of instruction calls.  
     * 
     * @return Block of instruction calls.
     */
    
    public IInstructionCallBlock getCallBlock() throws ConfigurationException;
}
