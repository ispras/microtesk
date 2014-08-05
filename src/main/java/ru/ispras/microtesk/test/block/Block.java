/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * Block.java, May 6, 2013 1:32:37 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.test.block;

import java.util.Collections;
import java.util.Map;

import ru.ispras.microtesk.test.sequence.Sequence;
import ru.ispras.microtesk.test.sequence.iterator.IIterator;

public final class Block
{
    private final IIterator<Sequence<AbstractCall>> iterator;
    private final Map<String, Object> attributes;
    
    public Block(
        IIterator<Sequence<AbstractCall>> iterator,
        Map<String, Object> attributes
        )
    {
        if (null == iterator)
            throw new NullPointerException();

        if (null == attributes)
            throw new NullPointerException();

        this.iterator = iterator;
        this.attributes = attributes;
    }
    
    public Block(IIterator<Sequence<AbstractCall>> iterator)
    {
        this(iterator, Collections.<String, Object>emptyMap());
    }

    public IIterator<Sequence<AbstractCall>> getIterator()
    {
        return iterator;
    }
    
    public Object getAttaribute(String name)
    {
        return attributes.get(name);
    }
}
