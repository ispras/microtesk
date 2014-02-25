/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * IMetaLocationStore.java, Nov 15, 2012 12:43:50 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.api.metadata;

/**
 * The IMetaLocationStore interface is a base interface for object describing
 * memory resources of the processor (as registers and memory store locations). 
 * 
 * @author Andrei Tatarnikov
 */

public interface IMetaLocationStore
{
    /**
     * Returns the name of the resource.
     * 
     * @return Memory resource name.
     */
    
    public String getName();
    
    /**
     * Returns the count of items in the memory store.
     * 
     * @return Memory store item count.
     */
    
    public int getCount();
}
