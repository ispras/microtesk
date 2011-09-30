/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: SequenceIterator.java,v 1.4 2008/08/18 08:08:34 kamkin Exp $
 */

package com.unitesk.testfusion.core.iterator;

import java.util.ArrayList;

/**
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class SequenceIterator implements Iterator
{
	protected ArrayList<Iterator> iterators = new ArrayList<Iterator>();
	
    /** Flag that refrects availability of the value. */
	protected boolean hasValue = true;
	
	public SequenceIterator() {}
    
    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to the iterator object.
     */
	protected SequenceIterator(SequenceIterator r)
    {
        int i, size;
        
        hasValue = r.hasValue();
        
        size = r.iterators.size();
        for(i = 0; i < size; i++)
        {
            Iterator iterator = r.iterators.get(i);
            iterators.add(iterator.clone());
        }
    }

	public void registerIterator(Iterator iterator)
	{
		iterators.add(iterator);
	}
	
    /** Initializes the iterator. */
	public void init()
	{
		int i, size;
		
		hasValue = true;
		
		size = iterators.size();
		for(i = 0; i < size; i++)
		{
			Iterator iterator = (Iterator)iterators.get(i);
			iterator.init();
			
			hasValue |= iterator.hasValue();
		}
	}
    
    /**
     * Checks if the iterator is not exhausted (value is available).
     * 
     * @return <code>true</code> if the iterator is not exhausted;
     *         <code>false</code> otherwise.
     */
	public boolean hasValue()
	{
		return hasValue;
	}

    public Object value()
    {
        int i, size;
        
        size = iterators.size();
        for(i = 0; i < size; i++)
        {
            Iterator iterator = iterators.get(i);

            if(iterator.hasValue())
                { return iterator.value(); }
        }
        
        return null;
    }
    
    /** Makes the iteration. */
	public void next()
	{
        int i, j, size;
        
        size = iterators.size();
        for(i = j = 0; i < size; i++)
        {
            Iterator iterator = iterators.get(i);

            if(iterator.hasValue())
            {
                if(j == 0)
                    { iterator.next(); j = 1; }
                else
                    { return; }
            }
        }
        
		hasValue = false;
	}
    
    /** Stops the iterator. */
    public void stop()
    {
        hasValue = false;
    }

    /**
     * Returns a string representation of the current value.
     * 
     * @return a string representation of the current value.
     */
    public String toString()
    {
        return "" + value();
    }

    /**
     * Returns a copy of the iterator.
     * 
     * @return a copy of the iterator.
     */
    public SequenceIterator clone()
    {
        return new SequenceIterator(this);
    }
}
