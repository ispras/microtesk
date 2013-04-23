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

import java.util.List;

import ru.ispras.microtesk.test.core.iterator.IIterator;

/**
 * This class implements concatenation of iterators.
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class ConcatCompositor<T> extends BaseCompositor<T>
{
    private int i;

    public ConcatCompositor()
    {
        super();
    }
    
    public ConcatCompositor(final List<IIterator<T>> iterators)
    {
        super(iterators);
    }

    @Override
    protected void onInit()
    {
        i = 0;
    }
   
    @Override
    protected void onNext()
    {
    }

    @Override
    protected IIterator<T> choose()
    {
        for(; i < iterators.size(); i++)
        {
            if(iterators.get(i).hasValue())
                { return iterators.get(i); }
        }

        return null;        
    }
}
