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

package ru.ispras.microtesk.test.core.iterator;

import java.util.Collection;
import java.util.Iterator;

/**
 * This class implements a collection iterator.
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class CollectionIterator<T> implements IBoundedIterator<T>
{
    // The collection being iterator.
    private Collection<T> collection;
    // The collection iterator.
    private Iterator<T> iterator;
    // The flag that refrects availability of the value.
	private boolean hasValue;    
    // The current value.
    private T value;
	
    /**
     * Constructs a collection iterator.
     * 
     * @param collection the collection to be iterated.
     */
	public CollectionIterator(final Collection<T> collection)
	{	
		this.collection = collection;
	}
    
    @Override
	public void init()
	{
        iterator = collection.iterator();
		hasValue = iterator.hasNext();
		value    = iterator.next();
	}
    
    @Override
	public boolean hasValue()
	{
		return hasValue;
	}

    @Override
    public T value()
    {
        return value;
    }
    
    @Override
	public void next()
	{
		hasValue = iterator.hasNext();
		value    = iterator.next();
	}

    @Override
    public int size()
    {
        return collection.size();
    }
}