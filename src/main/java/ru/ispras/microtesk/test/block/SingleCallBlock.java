/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * SingleBlock.java, May 6, 2013 3:04:52 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.test.block;

import ru.ispras.microtesk.test.core.Sequence;
import ru.ispras.microtesk.test.core.iterator.IIterator;
import ru.ispras.microtesk.test.core.iterator.SingleValueIterator;

final class SingleCallBlock implements Block
{
    private final IIterator<Sequence<AbstractCall>> iterator;

    public SingleCallBlock(AbstractCall call)
    {
        final Sequence<AbstractCall> sequence = new Sequence<AbstractCall>();
        sequence.add(call);

        this.iterator =
           new SingleValueIterator<Sequence<AbstractCall>>(sequence);
    }

    @Override
    public IIterator<Sequence<AbstractCall>> getIterator()
    {
        return iterator;
    }
}
