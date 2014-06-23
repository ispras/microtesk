/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * IMetaArgument.java, Nov 2, 2012 4:57:43 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.api.metadata;

/**
 * The IMetaArgument interface is a base interface for objects describing instruction arguments.
 * 
 * @author Andrei Tatarnikov
 */

public interface IMetaArgument
{
    /**
     * Returns the name of the argument.
     * 
     * @return Argument name.
     */

    public String getName();

    /**
     * Returns an iterator for the collection of type names associated with the argument.  
     * 
     * @return An Iterable object that refers to the collection of type names
     *         (e.g. addressing mode names).
     */

    public Iterable<String> getTypeNames();
}
