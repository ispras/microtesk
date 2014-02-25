/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * IMetaModel.java, Nov 2, 2012 3:14:27 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.api.metadata;

/**
 * The IMetaModel interface provides methods to query information
 * on the ISA. This includes the list of instructions, the list of memory
 * resources (registers, memory) and the list of test situations (behavioral
 * properties of the instructions).   
 * 
 * @author Andrei Tatarnikov
 */

public interface IMetaModel
{
    /**
     * Returns an iterator for the collection of instructions. 
     * 
     * @return An Iterable object.
     */

    public Iterable<IMetaInstruction> getInstructions();

    /**
     * Returns an iterator for the collection of registers.
     * 
     * @return An Iterable object.
     */

    public Iterable<IMetaLocationStore> getRegisters();

    /**
     * Returns an iterator for the collection of memory store locations.
     * 
     * @return An Iterable object.
     */

    public Iterable<IMetaLocationStore> getMemoryStores();
}
