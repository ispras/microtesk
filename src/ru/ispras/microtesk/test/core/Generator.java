/*
 * Copyright 2013 ISP RAS (http://www.ispras.ru), UniTESK Lab (http://www.unitesk.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.ispras.microtesk.test.core;

import java.util.List;

import ru.ispras.microtesk.test.core.combinator.Combinator;
import ru.ispras.microtesk.test.core.compositor.Compositor;
import ru.ispras.microtesk.test.core.iterator.CollectionIterator;
import ru.ispras.microtesk.test.core.iterator.IIterator;

/**
 * This class implements the test sequence generator.
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class Generator<T> implements IIterator<Sequence<T>>
{
    /// The combinator used by the generator (it produces different combinations of the sequences).
    private Combinator<Sequence<T>> combinator;
    /// The compositor used by the generator (it merges several sequences into one).
    private Compositor<T> compositor;

    private List<IIterator<Sequence<T>>> iterators;

    /**
     * Constructs a test sequence generator.
     *
     * @param combinator the combinator.
     * @param compositor the compositor.
     */

    public Generator(
        final Combinator<Sequence<T>> combinator,
        final Compositor<T> compositor,
        final List<IIterator<Sequence<T>>> iterators
        )
    {
        this.combinator = combinator;
        this.compositor = compositor;
        this.iterators  = iterators;
    }

    @Override
    public void init()
    {
        combinator.removeIterators();
        combinator.addIterators(iterators);

        combinator.init();
    }
    
    @Override
    public boolean hasValue()
    {
        return combinator.hasValue();
    }
    
    @Override
    public Sequence<T> value()
    {
        final List<Sequence<T>> combination = combinator.value();
        
        compositor.removeIterators();
        for(final Sequence<T> sequence : combination)
        {
            compositor.addIterator(new CollectionIterator<T>(sequence));
        }

        final Sequence<T> result = new Sequence<T>();
        for(compositor.init(); compositor.hasValue(); compositor.next())
        {
            result.add(compositor.value());
        }

        return result;
    }

    @Override
    public void next()
    {
        combinator.next();
    }
}
