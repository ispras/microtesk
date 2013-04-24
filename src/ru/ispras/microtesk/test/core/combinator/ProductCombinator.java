/*
 * Copyright 2008-2013 ISP RAS (http://www.ispras.ru), UniTESK Lab (http://www.unitesk.com)
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

import ru.ispras.microtesk.test.core.iterator.IIterator;

/**
 * This class implements the product combinator of iterators.
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class ProductCombinator<T> extends BaseCombinator<T>
{
    /// The current iterator index.
    private int i;

    @Override
    public void onInit()
    {
        i = iterators.size() - 1;
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
		for(int j = i; j >=0; j--)
		{
			IIterator<T> iterator = iterators.get(j);

			if(iterator.hasValue())
		    {
                iterator.next();
                
                if(iterator.hasValue())
                    { i = j; return true; }
            }
			
            if(j > 0)
                { iterator.init(); }
		}
        
        return false;
	}
}