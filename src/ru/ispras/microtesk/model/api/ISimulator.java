/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * ISimulator.java, Nov 6, 2012 3:10:25 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.api;

import ru.ispras.microtesk.model.api.instruction.IInstructionCallBlockBuilder;

/**
 * The ISimulator interface provides the possibility to run instructions
 * located in the target processor model and request their textual
 * representation (with corresponding arguments). Also, it allows getting
 * information on the current state of the model.  
 * 
 * @author Andrei Tatarnikov
 */

@Deprecated
public interface ISimulator
{
    /**
     * Creates a builder that will help set up a block of an instruction calls. 
     *
     * @return A builder for an instruction call object.
     */

    public IInstructionCallBlockBuilder createCallBlock();
}
