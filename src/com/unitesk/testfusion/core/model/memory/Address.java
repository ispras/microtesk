/* 
 * Copyright (c) 2007 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: Address.java,v 1.4 2009/08/13 15:54:16 kamkin Exp $
 */

package com.unitesk.testfusion.core.model.memory;

/**
 * Address of data or instruction in the memory.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class Address
{
    /** Virtual address. */
    public long va = 0L;
    
    /** Physical address. */
    public long pa = 0L;

    /**
     * Returns a string representation of the address.
     * 
     * @param  <code>va</code> the virtual address.
     * 
     * @param  <code>pa</code> the physical address.
     * 
     * @return a string representation of the address.
     */
    public static String toString(long va, long pa)
    {
        return "VA=0x" + Long.toHexString(va) + ", PA=0x" + Long.toHexString(pa); 
    }
    
    /** Default constructor. Virtual and physical addresses are zero. */
    public Address() {}
    
    /**
     * Constructor.
     * 
     * @param  <code>va</code> the virtual address.
     * 
     * @param  <code>pa</code> the physical address.
     */
    public Address(long va, long pa)
    {
        this.va = va;
        this.pa = pa;
    }

    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to address object.
     */
    protected Address(Address r)
    {
        va = r.va;
        pa = r.pa;
    }
    
    /**
     * Returns a string representation of the address.
     * 
     * @return a string representation of the address.
     */
    public String toString()
    {
        return toString(va, pa); 
    }
    
    /**
     * Increments the virtual and physical addresses.
     * 
     * @param <code>offset</code> the increment value.
     */
    public void increment(long offset)
    {
        va += offset;
        pa += offset;
    }

    /**
     * Decrements the virtual and physical addresses.
     * 
     * @param <code>offset</code> the increment value.
     */
    public void decrement(long offset)
    {
        va -= offset;
        pa -= offset;
    }
    
    /**
     * Returns a copy of the address.
     * 
     * @return a copy of the address.
     */
    public Address clone()
    {
        return new Address(this);
    }
}
