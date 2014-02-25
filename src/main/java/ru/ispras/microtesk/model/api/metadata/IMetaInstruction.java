/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * IMetaInstruction.java, Nov 2, 2012 3:59:46 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.api.metadata;

/**
 * The IMetaInstruction interface provides methods to request information
 * on the current instruction.
 * 
 * @author Andrei Tatarnikov
 */

public interface IMetaInstruction
{
    /**
     * Returns the instruction name.
     * 
     * @return The instruction name.
     */
    
    public String getName();
    
    /**
     * Returns an Iterable object for the collection of instruction arguments. 
     * 
     * @return Iterable object.
     */
    
    public Iterable<IMetaArgument> getArguments();
    
    /**
     * Returns an iterator for the collection of test situations.
     * 
     * @return An Iterable object.
     */

    public Iterable<IMetaSituation> getSituations();
}
