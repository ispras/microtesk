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
import java.util.List;

import ru.ispras.microtesk.test.core.iterator.IIterator;

/**
 * This class is a basic combinator of iterators. It takes several iterators
 * and produces different combinations of their results.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public abstract class BaseCombinator<T> implements IIterator<List<T>>
{
    /// Availability of the value.
    private boolean hasValue;
    /// The iterators to be combined.
    protected ArrayList<IIterator<T>> iterators = new ArrayList<IIterator<T>>();

    /**
     * Constructs a compositor with the empty list of iterators.
     */
    public BaseCombinator()
    {
    }
    
    /**
     * Constructs a compositor with the given list of iterators.
     *
     * @param iterators the list of iterators to be composed.
     */
    public BaseCombinator(final List<IIterator<T>> iterators)
    {
        this.iterators.addAll(iterators);
    }
    
    /**
     * Adds the iterator into the compositor's list.
     *
     * @param iterator the iterator to be added to the compositor's list.
     */
    public void addIterator(final IIterator<T> iterator)
    {
        iterators.add(iterator);
    }

    /**
     * Removes the i-th iterator from the compositor's list.
     *
     * @param i the index of the iterator to be removed from the compositor's list.
     */
    public void removeIterator(int i)
    {
        iterators.remove(i);
    }
    
    /**
     * Returns the number of iterators in the compositor's list.
     *
     * @return the size of the compositor's list.
     */
    public int size()
    {
        return iterators.size();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Callbacks that should be overloaded in subclasses
    ///////////////////////////////////////////////////////////////////////////

    /**
     * The callback method called in the <code>init</code> method.
     */
    protected abstract void onInit();
    
    /**
     * The callback method called in the <code>value</code> method.
     *
     * @param i the iterator index.
     * @return the value of the i-th iterator (<code>null</code> if the iterator
     *         has been exhausted, i.e., no value is available).
     */
    protected abstract T getValue(int i);
    
    /**
     * The callback method called in the <code>next</code> method.
     *
     * @return false iff it the combinator has been exhausted.
     */
    protected abstract boolean doNext();

    ///////////////////////////////////////////////////////////////////////////
    // Callback-based implementation of the iterator method
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void init()
    {
        for(IIterator<T> iterator : iterators)
            { iterator.init(); }

        onInit();
        
        hasValue = true;
    }
    
    @Override
    public boolean hasValue()
    {
        if(!hasValue || iterators.isEmpty())
            { return false; }

        for(int i = 0; i < iterators.size(); i++)
        {
            if(getValue(i) == null)
                { return false; }
        }
        
        return true;
    }

    @Override
    public List<T> value()
    {
        List<T> result = new ArrayList<T>(iterators.size());
        
        for(int i = 0; i < iterators.size(); i++)
            { result.set(i, getValue(i)); }
            
        return result;
    }

    @Override
    public void next()
    {
        hasValue = doNext();
    }
}