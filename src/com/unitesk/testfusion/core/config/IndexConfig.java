/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: IndexConfig.java,v 1.7 2009/07/08 08:25:55 kamkin Exp $
 */

package com.unitesk.testfusion.core.config;

/**
 * Abstract class for representing MicroTESK configuration or its part,
 * which has property "index".
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public abstract class IndexConfig extends Config
{
    /** Index value. */
    protected int index;
    
    /** Number of indexed items. */
    protected int size;
    
    /**
     * Constructor.
     * 
     * @param <code>name</code> the name of the configuration.

     * @param <code>index</code> the initial value of index.
     * 
     * @param <code>size</code> the number of indexed items.
     */
    public IndexConfig(String name, int index, int size)
    {
        super(name);
        
        this.index = index;
        this.size  = size;
    }
    
    /**
     * Constructor. Initial value of index is zero.
     * 
     * @param <code>name</code> the name of the configuration.

     * @param <code>size</code> the number of indexed items.
     */
    public IndexConfig(String name, int size)
    {
        this(name, 0, size);
    }

    /**
     * Constructor.
     * 
     * @param <code>index</code> the initial value of index.
     * 
     * @param <code>size</code> the number of indexed items.
     */
    public IndexConfig(int index, int size)
    {
        this.index = index;
        this.size  = size;
    }
    
    /**
     * Constructor. Initial value of index is zero.
     * 
     * @param <code>size</code> the number of indexed items.
     */
    public IndexConfig(int size)
    {
        this(0, size);
    }
    
    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to index configuration object.
     */
    protected IndexConfig(IndexConfig r)
    {
        super(r);
        
        index = r.index;
        size  = r.size;
    }
    
    /**
     * Returns the value of the index.
     * 
     * @return the value of the index.
     */
    public int getIndex()
    {
        return index;
    }
    
    /**
     * Sets the value of the index.
     * 
     * @param <code>index</code> the new value of the index.
     */
    public void setIndex(int index)
    {
        this.index = index;
    }
    
    /**
     * Returns the number of indexed items.
     * 
     * @return the number of indexed items.
     */
    public int size()
    {
        return size;
    }
}
