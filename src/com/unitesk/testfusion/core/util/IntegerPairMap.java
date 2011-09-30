/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: IntegerPairMap.java,v 1.1 2008/09/01 11:09:17 kamkin Exp $
 */

package com.unitesk.testfusion.core.util;

import java.util.HashMap;

/**
 * Class <code>IntegerPairMap</code> implements integer-pair to object map.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class IntegerPairMap<T>
{
    public static final long serialVersionUID = 0;
    
    protected HashMap<Long, T> map = new HashMap<Long, T>();

    /** Default constructor. */
    public IntegerPairMap() {}
    
    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to integer-pair to object map.
     */
    @SuppressWarnings("unchecked")
    protected IntegerPairMap(IntegerPairMap r)
    {
        map = (HashMap<Long, T>)r.map.clone();
    }
    
    /**
     * Puts the object at the given position.
     * 
     * @param <code>i</code> the first part of the position.
     * 
     * @param <code>j</code> the second part of the position.
     * 
     * @param <code>obj</code> the object to be added.
     */
    public void put(int i, int j, T obj)
    {
        map.put(new Long(((long)i << 32) | j), obj);
    }
    
    /**
     * Returns the object at the given position.
     * 
     * @param  <code>i</code> the first part of the position.
     * 
     * @param  <code>j</code> the second part of the position.
     * 
     * @return the object at the given position.
     */
    public T get(int i, int j)
    {
        return map.get(new Long((long)i << 32) | j);
    }
    
    /** Clears the map. */
    public void clear()
    {
        map.clear();
    }
    
    /**
     * Returns a copy of the map.
     * 
     * @return a copy of the map.
     */
    public IntegerPairMap<T> clone()
    {
        return new IntegerPairMap<T>(this);
    }
}
