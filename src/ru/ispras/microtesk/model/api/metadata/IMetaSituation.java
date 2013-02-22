/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * IMetaSituation.java, Nov 15, 2012 12:51:35 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.api.metadata;

/**
 * The IMetaSituation interface is a base interface for object describing test situations.
 * 
 * @author Andrei Tatarnikov
 */

public interface IMetaSituation
{
    /**
     * Returns the name of the test situation.
     *  
     * @return Situation name.
     */

    public String getName();

    /**
     * Returns in iterator for the collection of test situation arguments
     * (signature of the test situation).
     * 
     * @return
     */

    public Iterable<IMetaArgument> getArguments();

    /**
     * 
     * @return
     */

    public Iterable<IMetaAttribute> getAttributes();
}
