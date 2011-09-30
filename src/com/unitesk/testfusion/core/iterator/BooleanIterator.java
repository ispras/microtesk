/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: BooleanIterator.java,v 1.7 2009/08/06 13:04:24 kamkin Exp $
 */

package com.unitesk.testfusion.core.iterator;

/**
 * Iterator of boolean values {<code>false</code>, </code>true</code>}.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class BooleanIterator implements Iterator
{
    /** Current value. */
	protected boolean value = false;
    
    /** Flag that refrects availability of the value. */
	protected boolean hasValue = true;

    /** Default constructor. */
    public BooleanIterator() {}
    
    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to boolean iterator object.
     */
    protected BooleanIterator(BooleanIterator r)
    {
        value = r.value;
        hasValue = r.hasValue;
    }
    
    /** Initializes the iterator. */
	public void init()
	{
		value = false;
		hasValue = true;
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
     * Sets the current value of the iterator.
     * 
     * @param <code>value</code> the current value.
     */
    public void setValue(boolean value)
    {
        this.value = value;
    }
    
    /**
     * Returns the current value of iterator.
     * 
     * @return the current value of iterator.
     */
	public boolean booleanValue()
	{
		return value;
	}
	
    /**
     * Returns the current value of iterator in the form of <code>Boolean</code>
     * object.
     * 
     * @return the current value of iterator in the form of <code>Boolean</code>
     *         object.
     */
	public Object value()
	{
		return new Boolean(value);
	}

    /** Makes the iteration. */
	public void next()
	{
		if(value)
			{ hasValue = false; }
		
		value = true;
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
        return Boolean.toString(value);
    }

    /**
     * Returns a copy of the iterator.
     * 
     * @return a copy of the iterator.
     */
    public BooleanIterator clone()
    {
        return new BooleanIterator(this);
    }
}
