/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * IInstructionCallBuilderEx.java, Nov 28, 2012 1:12:49 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.api.instruction;

import ru.ispras.microtesk.model.api.exception.ConfigurationException;

/**
 * The IInstructionCallBuilderEx interface is an extension of the
 * IInstructionCallBuilder interface that provides a possibility to
 * get the constructed instruction call object. This interface is
 * user internally while the IInstructionCallBuilder interface is 
 * accessible externally.  
 * 
 * @author Andrei Tatarnikov
 */

public interface IInstructionCallBuilderEx extends IInstructionCallBuilder
{
    /**
     * Returns created and initialized instruction call object.  
     * 
     * @return Instruction call object.
     */

    public IInstructionCall getCall() throws ConfigurationException;
}
