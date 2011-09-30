/* 
 * Copyright (c) 2007-2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: DirectMappedCache.java,v 1.5 2008/08/15 09:37:59 kamkin Exp $
 */

package com.unitesk.testfusion.core.model.cache;

import com.unitesk.testfusion.core.model.Processor;

/**
 * Abstract class <code>DirrectMappedCache</code> represents direct-mapped cache memory.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public abstract class DirectMappedCache extends Cache
{
    /** Number of rows. */
    protected int rowNumber;
    
    /** Size of rows. */
    protected int rowSize;
    
    /** Set of rows. */
    protected CacheRow cache[];
    
    /**
     * Constructor.
     * 
     * @param <code>processor</code> the processor object.
     * 
     * @param <code>rowNumber</code> the number of rows.
     * 
     * @param <code>rowSize</code> the size of rows.
     */
    public DirectMappedCache(Processor processor, int rowNumber, int rowSize)
    {
        super(processor);
        
        this.rowNumber = rowNumber;
        this.rowSize = rowSize;
    
        this.cache = new CacheRow[rowNumber];
        
        for(int i = 0; i < rowNumber; i++)
            { this.cache[i] = new CacheRow(rowSize); }
    }
    
    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference of 
     */
    protected DirectMappedCache(DirectMappedCache r)
    {
        super(r.getProcessor());
        
        rowNumber = r.rowNumber;
        rowSize = r.rowSize;
        
        for(int i = 0; i < rowNumber; i++)
            { cache[i] = new CacheRow(r.cache[i]); }
    }
    
    /**
     * Returns the number of sections. Always return one.
     * 
     * @return <code>1</code>.
     */
    public int getSectionNumber()
    {
        return 1;
    }

    /**
     * Returns the number of rows.
     * 
     * @return the number of rows.
     */
    public int getRowNumber()
    {
        return rowNumber;
    }
    
    /**
     * Return the size of a row.
     * 
     * @return the size of a row.
     */
    public int getRowSize()
    {
        return rowSize;
    }

    /**
     * Checks if the cache containts data located at the given physical address.
     * 
     * @param  <code>physicalAddress</code> the physical address of data.
     * 
     * @return <code>true</code> if the cache contains data located at the given
     *         physical address; <code>false</code> otherwise.
     */
    public boolean contains(long physicalAddress)
    {
        long tag = getTag(physicalAddress);
        int  row = getRow(physicalAddress);
        
        return cache[row].tag == tag;
    }
    
    /**
     * Reads the data, located at the given physical address, from the cache. It is
     * assumed that the cache containts these data.
     * 
     * @param  <code>physicalAddress</code> the physical address of the data.
     * 
     * @return the data at the given physical address.
     */
    public byte[] read(long physicalAddress)
    {
        return contains(physicalAddress) ?
            getData(0x0, getRow(physicalAddress)) : null;
    }
    
    /**
     * Writes the data, assiciated with the given physical address, to the cache.
     *  
     * @param <code>physicalAddress</code> the physical address of the data.
     * 
     * @param <code>data</code> the data to be written.
     */
    public void write(long physicalAddress, byte data[])
    {
        long tag = getTag(physicalAddress);
        int  row = getRow(physicalAddress);

        setTag(row, tag);
    }

    /**
     * Returns the tag stored at the given row.
     * 
     * @param  <code>row</code> the row.
     * 
     * @return the tag stored in the given section at the given row.
     */
    public long getTag(int row)
    {
        return cache[row].tag;
    }
    
    /**
     * Returns the tag stored in the given section at the given row.
     * 
     * @param  <code>section</code> the section (it is not taken into
     *         consideration).
     * 
     * @param  <code>row</code> the row.
     * 
     * @return the tag stored in the given section at the given row.
     */
    public long getTag(int section, int row)
    {
        return getTag(row);
    }

    /**
     * Sets the tag at the given row.
     * 
     * @param <code>row</code> the row.
     * 
     * @param <code>tag</code> the tag to be written.
     */
    public void setTag(int row, long tag)
    {
        cache[row].tag = tag;
    }

    /**
     * Sets the tag in the given section at the given row.
     * 
     * @param <code>section</code> the section (it is not taken into
     *        consideration).
     * 
     * @param <code>row</code> the row.
     * 
     * @param <code>tag</code> the tag to be written.
     */
    public void setTag(int section, int row, long tag)
    {
        setTag(row, tag);
    }

    /**
     * Returns the data stored at the given row.
     * 
     * @param  <code>row</code> the row.
     * 
     * @return the data stored in the given section at the given row.
     */
    public byte[] getData(int row)
    {
        return cache[row].data;
    }
    
    /**
     * Returns the data stored in the given section at the given row.
     * 
     * @param  <code>section</code> the section (it is not taken into
     *         consideration).
     * 
     * @param  <code>row</code> the row.
     * 
     * @return the data stored in the given section at the given row.
     */
    public byte[] getData(int section, int row)
    {
        return getData(row);
    }

    /**
     * Sets the data at the given row.
     * 
     * @param <code>row</code> the row.
     * 
     * @param <code>data</code> the data to be written.
     */
    public void setData(int row, byte data[])
    {
        if(data == null)
            { return; }
        
        if(data.length != rowSize)
            { throw new IllegalArgumentException(); }

        for(int i = 0; i < rowSize; i++)
            { cache[row].data[i] = data[i]; }
    }
    
    /**
     * Sets the data in the given section at the given row.
     * 
     * @param <code>section</code> the section (it is not taken into
     *        consideration).
     * 
     * @param <code>row</code> the row.
     * 
     * @param <code>data</code> the data to be written.
     */
    public void setData(int section, int row, byte data[])
    {
        setData(row, data);
    }

    /** Resets the state of the cache. */
    public void reset()
    {
        for(int i = 0; i < rowNumber; i++)
            { cache[i].reset(); }
    }
}
