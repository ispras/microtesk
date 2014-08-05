/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * GeneratorSingle.java, May 29, 2013, 11:40:46 PM PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.test.sequence;

import java.util.Iterator;
import java.util.List;
import ru.ispras.microtesk.test.sequence.iterator.IIterator;

public final class GeneratorSingle<T> implements Generator<T>
{
    private final Sequence<T> sequence;
    private boolean hasValue;

    public GeneratorSingle(List<IIterator<Sequence<T>>> iterators)
    {
        assert null != iterators;

        this.sequence = createSingleSequence(iterators);
        this.hasValue = false;
    }

    private static <T> Sequence<T> createSingleSequence(List<IIterator<Sequence<T>>> iterators)
    {
        final Sequence<T> result = new Sequence<T>();

        final Iterator<IIterator<Sequence<T>>> it = iterators.iterator();
        while(it.hasNext())
        {
            final IIterator<Sequence<T>> sequenceIterator = it.next();

            sequenceIterator.init();
            while (sequenceIterator.hasValue())
            {
                result.addAll(sequenceIterator.value());
                sequenceIterator.next();
            }
        }

        return result;
    }

    @Override
    public void init()
    {
        hasValue = true;
    }

    @Override
    public boolean hasValue()
    {
        return hasValue;
    }

    @Override
    public Sequence<T> value()
    {
        assert hasValue;
        return sequence;
    }

    @Override
    public void next()
    {
        hasValue = false;
    }
}
