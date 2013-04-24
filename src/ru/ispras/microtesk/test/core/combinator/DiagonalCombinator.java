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

package ru.ispras.microtesk.test.core.combinator;

import java.util.HashSet;
import java.util.Set;

import ru.ispras.microtesk.test.core.iterator.IIterator;

/**
 * This class implements the diagonal combinator of iterators.
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class DiagonalCombinator<T> extends BaseCombinator<T>
{
    /// The set of exhausted iterators.
    private Set<Integer> exhausted = new HashSet<Integer>();

    @Override
    public void onInit()
    {
        exhausted.clear();
    }

    @Override
    public T getValue(int i)
    {
        IIterator<T> iterator = iterators.get(i);
        
        return iterator.hasValue() ? iterator.value() : null;
    }
    
    @Override
	public boolean doNext()
	{
		for(int i = 0; i < iterators.size(); i++)
		{
			IIterator<T> iterator = iterators.get(i);

            iterator.next();
                
            if(!iterator.hasValue())
            {
                exhausted.add(i);
                
                if(exhausted.size() < iterators.size())
                    { iterator.init(); }
                else
                    { return false; }
            }
		}
        
        return true;
	}
}