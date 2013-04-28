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

import ru.ispras.microtesk.test.core.combinator.BaseCombinator;
import ru.ispras.microtesk.test.core.compositor.BaseCompositor;
import ru.ispras.microtesk.test.core.internal.CompositeIterator;
import ru.ispras.microtesk.test.core.iterator.IIterator;

/**
 * This class implements the test sequence generator.
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class Generator<T> extends CompositeIterator<Sequence<T>> implements IIterator<Sequence<T>>
{
    /// The combinator used by the generator.
    private BaseCombinator<T> combinator;
    /// The compositor used by the generator.
    private BaseCompositor<T> compositor;

    /**
     * Constructs a test sequence generator.
     *
     * @param combinator the combinator.
     * @param compositor the compositor.
     */
    public Generator(final BaseCombinator<T> combinator, final BaseCompositor<T> compositor)
    {
        this.combinator = combinator;
        this.compositor = compositor;
    }

    @Override
    public void init()
    {
    }
    
    @Override
    public boolean hasValue()
    {
        return false;
    }
    
    @Override
    public Sequence<T> value()
    {
        return null;
    }
    
    @Override
    public void next()
    {
    }
}
