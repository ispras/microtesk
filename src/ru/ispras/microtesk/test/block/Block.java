/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * Block.java, Apr 29, 2013 5:19:47 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.test.block;

import java.util.ArrayList;
import java.util.List;

import ru.ispras.microtesk.test.core.ECombinator;
import ru.ispras.microtesk.test.core.ECompositor;
import ru.ispras.microtesk.test.core.iterator.CollectionIterator;
import ru.ispras.microtesk.test.core.iterator.IIterator;
import ru.ispras.microtesk.test.core.iterator.SingleValueIterator;

public class Block implements Entry
{
    //private final ECombinator combinator;
    //private final ECompositor compositor;
    
    private final List<Entry> entries;
    private final IIterator<Entry> iterator;

    public Block()
    {
        this.entries  = new ArrayList<Entry>();
        this.iterator = new CollectionIterator<Entry>(entries);
    }

    public Block(Call call)
    {
        this.entries  = null;
        this.iterator = new SingleValueIterator<Entry>(call);
    }

    public void addEntry(Entry entry)
    {
        entries.add(entry);
    }

    public IIterator<Entry> getIterator()
    {
        return iterator;
    }
}
