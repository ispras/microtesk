/* 
 * Copyright (c) 2007 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 * 
 * $Id: Cache.java,v 1.4 2008/08/15 09:37:59 kamkin Exp $
 */

package com.unitesk.testfusion.core.model.cache;

import com.unitesk.testfusion.core.model.Module;
import com.unitesk.testfusion.core.model.Processor;
import com.unitesk.testfusion.core.model.ResetableObject;

/**
 * Abstract class that represents microprocessor cache memory.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public abstract class Cache extends Module implements ResetableObject
{
    /** Invalid tag. Each bit of the invalid tag is one. */
    public static final long INVALID_TAG = 0xffffffffffffffffL;

    /**
     * Constructor.
     * 
     * @param <code>processor</code> the processor object.
     */
    public Cache(Processor processor)
    {
        super(processor);
    }

    /**
     * Returns the number of sections (associativity level).
     * 
     * @return the number of sections (associativity level).
     */
    public abstract int getSectionNumber();

    /**
     * Returns the number of rows in each section.
     * 
     * @return the number of rows in each section.
     */
    public abstract int getRowNumber();
    
    /**
     * Returns the number of bytes in each row.
     * 
     * @return the number of bytes in each row.
     */
    public abstract int getRowSize();
    
    /**
     * Calculates the tag for the physical address.
     * 
     * @param  <code>physicalAddress</code> the physical address.
     * 
     * @return the tag of the physical address.
     */
    public abstract long getTag(long physicalAddress);

    /**
     * Calculates the row in the cache for the physical address.
     *  
     * @param  <code>physicalAddress</code> the physical address.
     * 
     * @return the row in the cache for the physical address.
     */
    public abstract int getRow(long physicalAddress);
    
    /**
     * Calculates the position of data in the row for the physical address.
     * 
     * @param  <code>physicalAddress</code> the physical address.
     * 
     * @return the position of data in the row for the physical address.
     */
    public abstract int getPosition(long physicalAddress);

    /**
     * Creates the physical address by tag, row, and position.
     * 
     * @param  <code>tag</code> the tag.
     * 
     * @param  <code>row</code> the row in the cache.
     * 
     * @param  <code>pos</code> the position of data in the row.
     * 
     * @return the created physical address. 
     */
    public abstract long createPhysicalAddress(long tag, int row, int pos);

    /**
     * Checks if the cache containts data located at the given physical address.
     * 
     * @param  <code>physicalAddress</code> the physical address of data.
     * 
     * @return <code>true</code> if the cache contains data located at the given
     *         physical address; <code>false</code> otherwise.
     */
    public abstract boolean contains(long physicalAddress);
    
    /**
     * Reads the data, located at the given physical address, from the cache. It is
     * assumed that the cache containts these data.
     * 
     * @param  <code>physicalAddress</code> the physical address of the data.
     * 
     * @return the data at the given physical address.
     */
    public abstract byte[] read(long physicalAddress);
    
    /**
     * Writes the data, assiciated with the given physical address, to the cache.
     *  
     * @param <code>physicalAddress</code> the physical address of the data.
     * 
     * @param <code>data</code> the data to be written.
     */
    public abstract void write(long physicalAddress, byte data[]);

    /**
     * Returns the tag stored in the given section at the given row.
     * 
     * @param  <code>section</code> the section.
     * 
     * @param  <code>row</code> the row.
     * 
     * @return the tag stored in the given section at the given row.
     */
    public abstract long getTag(int section, int row);
    
    /**
     * Sets the tag in the given section at the given row.
     * 
     * @param <code>section</code> the section.
     * 
     * @param <code>row</code> the row.
     * 
     * @param <code>tag</code> the tag to be written.
     */
    public abstract void setTag(int section, int row, long tag);
    
    /**
     * Returns the data stored in the given section at the given row.
     * 
     * @param  <code>section</code> the section.
     * 
     * @param  <code>row</code> the row.
     * 
     * @return the data stored in the given section at the given row.
     */
    public abstract byte[] getData(int section, int row);
    
    /**
     * Sets the data in the given section at the given row.
     * 
     * @param <code>section</code> the section.
     * 
     * @param <code>row</code> the row.
     * 
     * @param <code>data</code> the data to be written.
     */
    public abstract void setData(int section, int row, byte data[]);

    /** Resets the state of the cache. */
    public abstract void reset();
    
    /**
     * Returns a copy of the cache.
     * 
     * @return a copy of the cache.
     */
    public abstract Cache clone();
}
