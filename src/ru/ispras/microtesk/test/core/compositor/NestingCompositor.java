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

package ru.ispras.microtesk.test.core.compositor;

import java.util.Stack;

import ru.ispras.microtesk.test.core.compositor.internal.IteratorEntry;
import ru.ispras.microtesk.test.core.iterator.IBoundedIterator;
import ru.ispras.microtesk.test.core.iterator.IIterator;

/**
 * This class implements the nesting composition of iterators.
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class NestingCompositor<T> extends BaseCompositor<T>
{
    /// The stack of iterators.
    private Stack<IteratorEntry<T>> stack = new Stack<IteratorEntry<T>>();

    @Override
    protected void onInit()
    {
        stack.clear();
        
        if(iterators.size() > 0)
            { stack.push(new IteratorEntry<T>(iterators.get(0))); }
    }

    @Override
    public void onNext()
    {
        stack.peek().index++;
    }

    @Override
    protected IIterator<T> choose()
    {
        while(!stack.isEmpty())
        {
            IteratorEntry<T> entry = stack.peek();

            if(entry.index == entry.point && !entry.done)
            {
                if(stack.size() < iterators.size())
                {
                    entry.done = true;        
                    stack.push(new IteratorEntry<T>(iterators.get(stack.size())));

                    continue;
                }
            }

            if(entry.iterator.hasValue())
                { return entry.iterator; }
            else
                { stack.pop(); }
        }

        return null;
    }
}

