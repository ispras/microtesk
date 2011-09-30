/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: ProductIterator.java,v 1.5 2009/03/12 16:18:28 kamkin Exp $
 */

package com.unitesk.testfusion.core.iterator;

import java.util.ArrayList;

/**
 * Iterator that makes Cartesian product of arbitrary number of iterators.
 *  
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class ProductIterator implements Iterator
{
    /** List of iterators. */
	protected ArrayList<Iterator> iterators = new ArrayList<Iterator>();
	
    /** Flag that refrects availability of the value. */
	protected boolean hasValue = true;
	
    /** Default constructor. */
	public ProductIterator() {}
    
    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to the iterator object.
     */
	protected ProductIterator(ProductIterator r)
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

    /**
     * Adds an iterator to the list of iterators to be combined
     * .
     * @param <code>iterator</code> an iterator to be added to the list of
     *        iterators.
     */
	public void registerIterator(Iterator iterator)
	{
		iterators.add(iterator);
	}
	
    /** Removes all iterators. */
    public void clear()
    {
        iterators.clear();
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
			
			hasValue &= iterator.hasValue();
		}
	}
    
    /**
     * Returns the number of iterators to be combined.
     * 
     * @return the number of iterators to be combined.
     */
    public int size()
    {
        return iterators.size();
    }

    /**
     * Returns the <code>i</code>-th iterator of the list.
     * 
     * @param  <code>i</code> the index of iterator in the list.
     * 
     * @return the <code>i</code>-th iterator of the list.
     */
	public Iterator iterator(int i)
	{
	    return iterators.get(i);
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

    /**
     * Returns the current value of the <code>i</code>-th iterator.
     * 
     * @param  <code>i</code> the index of iterator.
     * 
     * @return the current value of the <code>i</code>-th iterator.
     */
    public Object value(int i)
    {
        Iterator iterator = iterators.get(i);
        return iterator.value();
    }

    /**
     * Returns the current value of the iterator.
     * 
     * @return the current value of the iterator.
     */
    public ArrayList<Object> value()
    {
        int i, size;
        
        ArrayList<Object> list = new ArrayList<Object>();
        
        size = iterators.size();
        for(i = 0; i < size; i++)
        {
            Iterator iterator = iterators.get(i);
            list.add(iterator.value());
        }
        
        return list;
    }
    
    /** Makes the iteration. */
	public void next()
	{
		for(int i = iterators.size() - 1; i >=0; i--)
		{
			Iterator iterator = (Iterator)iterators.get(i);

			if(iterator.hasValue())
		    {
                iterator.next();
                
                if(iterator.hasValue())
                    { return; }
            }
			
			iterator.init();
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
    public ProductIterator clone()
    {
        return new ProductIterator(this);
    }
}
