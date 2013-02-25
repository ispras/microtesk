/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * IMetaLocationReference.java, Feb 25, 2013 9:38:07 AM PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.api.metadata;

/**
 * The IMetaLocationReference interface provides information on location
 * reference using a built-in alias (for example, one of common aliases is PC).
 * 
 * @author Andrei Tatarnikov
 */

public interface IMetaLocationReference
{
    /**
     * Returns the name of the location store (register file or memory line)
     * that holds the given location (the location we refer to).
     * 
     * @return Name of the location store.
     */

    public String getStoreName();

    /**
     * Returns the index of the given location in the location store (register file
     * or memory line).
     * 
     * @return Index of the given location.
     */

    public int getIndex();
}
