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

package ru.ispras.microtesk.test.sequence.iterator;

/**
 * This class implements an array iterator.
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class ArrayIterator<T> implements IBoundedIterator<T>
{
    // The iterated array.
    private T[] array;
    // The current index.
    private int index;
    // The flag that refrects availability of the value.
    private boolean hasValue;
	
    /**
     * Constructs an array iterator.
     * 
     * @param array the array to be iterated.
     */
    public ArrayIterator(final T[] array)
    {	
        this.array = array;
    }
    
    @Override
    public void init()
    {
        index = 0;
        hasValue = (array != null && array.length > 0);
    }
    
    @Override
    public boolean hasValue()
    {
        return hasValue;
    }

    @Override
    public T value()
    {
        return array[index];
    }
    
    @Override
    public void next()
    {
        if(index == array.length - 1)
            { hasValue = false; }
        else
            { index++; }
    }

    @Override
    public int size()
    {
        return array.length;
    }
}
