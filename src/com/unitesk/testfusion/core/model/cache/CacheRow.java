package com.unitesk.testfusion.core.model.cache;

import com.unitesk.testfusion.core.model.ResetableObject;

/**
 * Class <code>CacheRow</code> represents a cache row.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class CacheRow implements ResetableObject
{
    /** Size of the row. */
    public int rowSize;
    
    /** Tag stored in the row. */
    public long tag = Cache.INVALID_TAG;
    
    /** Data stored in the row. */
    public byte data[];
    
    /**
     * Constructor.
     * 
     * @param <code>rowSize</code> the size of the row.
     */
    public CacheRow(int rowSize)
    {
        this.rowSize = rowSize;
        this.data = new byte[rowSize];
    }
    
    /**
     * Constructor.
     * 
     * @param <code>tag</code> the tag stored in the row.
     * 
     * @param <code>rowSize</code> the size of the row.
     */
    public CacheRow(long tag, int rowSize)
    {
        this(rowSize);
        
        this.tag = tag;
    }
    
    /**
     * Constructor.
     * 
     * @param <code>tag</code> the tag stored in the row.
     * 
     * @param <code>data</code> the data stored in the row.
     */
    public CacheRow(long tag, byte data[])
    {
        this(tag, data.length);
        
        for(int i = 0; i < rowSize; i++)
            { this.data[i] = data[i]; }
    }
    
    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to cache object to be copied.
     */
    public CacheRow(CacheRow r)
    {
        rowSize = r.rowSize;
        data = new byte[rowSize];
        
        for(int i = 0; i < rowSize; i++)
            { data[i] = r.data[i]; }
    }

    /** Resets the state of the row. */
    public void reset()
    {
        tag = Cache.INVALID_TAG;
    } 
    
    /**
     * Returns a copy of the row.
     * 
     * @return a copy of the row.
     */
    public CacheRow clone()
    {
        return new CacheRow(this);
    }
}
