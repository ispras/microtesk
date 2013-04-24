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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ru.ispras.microtesk.test.core.iterator.IIterator;
import ru.ispras.microtesk.test.core.randomizer.Randomizer;

/**
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class RandomCombinator<T> extends BaseCombinator<T>
{
    /// The set of exhausted iterators.
    private Map<Integer, ArrayList<T>> caches = new HashMap<Integer, ArrayList<T>>();
    /// The list of current values.
    private Map<Integer, T> values = new HashMap<Integer, T>();

    /// The set of exhausted iterators.
    private Set<Integer> exhausted = new HashSet<Integer>();

    @Override
    public void onInit()
    {
        caches.clear();
        values.clear();

        exhausted.clear();
        
        for(int i = 0; i < iterators.size(); i++)
        {
			IIterator<T> iterator = iterators.get(i);

            if(iterator.hasValue())
                { addValue(i, iterator.value()); }
        }
    }

    @Override
    public T getValue(int i)
    {
        return values.get(i);
    }
    
    @Override
	public boolean doNext()
	{
		for(int i = 0; i < iterators.size(); i++)
		{
			IIterator<T> iterator = iterators.get(i);
            
            if(iterator.hasValue() && (caches.get(i).isEmpty() || Randomizer.get().nextBoolean()))
            {
                iterator.next();
                
                if(iterator.hasValue())
                {
                    addValue(i, iterator.value());                    
                    continue;
                }
                else
                {
                    exhausted.add(i);
                    
                    if(exhausted.size() == iterators.size())
                        { return false; }
                }
            }
                
            values.put(i, Randomizer.get().choose(caches.get(i)));                    
		}
        
        return true;
	}
    
    /// Adds the iterator value into the cache.
    private void addValue(int i, final T value)
    {
        ArrayList<T> trace = caches.containsKey(i) ? caches.get(i) : new ArrayList<T>();

        trace.add(value);

        caches.put(i, trace);
        values.put(i, value);
    }
}