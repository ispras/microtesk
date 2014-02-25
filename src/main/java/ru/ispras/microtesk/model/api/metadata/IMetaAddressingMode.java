/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * IMetaAddressingMode.java, Nov 2, 2012 4:31:14 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.api.metadata;

/**
 * The IMetaAddressingMode interface provides information 
 * on the specified addressing mode.
 * 
 * @author Andrei Tatarnikov
 */

public interface IMetaAddressingMode
{
    /**
     * Returns the name of the addressing mode.
     * 
     * @return Mode name.
     */
    
    public String getName();
    
    /**
     * Returns the list of addressing mode argument. 
     * 
     * @return Collection of argument names.
     */
    
    public Iterable<String> getArgumentNames();
}
