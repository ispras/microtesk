/* 
 * Copyright (c) 2007 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: SingleSituation.java,v 1.5 2009/07/09 14:48:07 kamkin Exp $
 */

package com.unitesk.testfusion.core.situation;

/**
 * Class that represents single-value test situation. 
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public abstract class SingleSituation extends Situation
{
	private boolean hasValue = true; 

    /** Default constructor. */
    public SingleSituation()
    {
        super("single");
    }

    /**
     * Constructor.
     * 
     * @param <code>name</code> the name of the situation.
     */
    public SingleSituation(String name)
    {
        super(name);
    }

    /**
     * Constructor.
     * 
     * @param <code>name</code> the name of the situation.
     *
     * @param <code>text</code> the description of the situation.
     */
    public SingleSituation(String name, String text)
    {
        super(name, text);
    }
    
    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to situation object.
     */
    protected SingleSituation(SingleSituation r)
    {
        super(r);
        
        this.hasValue = r.hasValue;
    }
        
    /** Initializes the iterator. */
    @Override
	public void init()
	{
		hasValue = true;
	}
	
    /**
     * Checks if the iterator is not exhausted (the test situation is available).
     * 
     * @return <code>true</code> if the iterator is not exhausted;
     *         <code>false</code> otherwise.
     */
    @Override
	public boolean hasValue()
	{
		return hasValue;
	}
	
    /** Makes iteration. */
    @Override
	public void next()
	{
        hasValue = false;
	}
	
    /** Stops the iterator. */
    @Override
    public void stop()
    {
        hasValue = false;
    }
}
