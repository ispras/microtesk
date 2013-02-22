/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * IInstructionCallBuilder.java, Nov 6, 2012 3:27:03 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.api.instruction;

import ru.ispras.microtesk.model.api.exception.ConfigurationException;

/**
 * The IInstructionCallBuilder interface provide methods for initializing
 * the executing context of some specified instruction.    
 * 
 * @author Andrei Tatarnikov
 */

public interface IInstructionCallBuilder
{
    /**
     * Returns a builder for an instruction argument (the argument
     * should be specified in the instruction description).
     * 
     * @param name The argument name.
     * @return An argument builder object.
     */
    
    public IArgumentBuilder getArgumentBuilder(String name) throws ConfigurationException;
    
    /**
     * Returns a builder for test situation to be associated with 
     * the current instruction call.
     * 
     * @param name The situation name.
     * @return A test situation builder object.
     */
    
    public ISituationBuilder getSituationBuilder(String name);
}
