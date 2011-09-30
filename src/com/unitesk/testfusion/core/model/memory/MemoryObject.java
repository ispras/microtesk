/* 
 * Copyright (c) 2008 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: MemoryObject.java,v 1.5 2009/08/13 15:54:16 kamkin Exp $
 */

package com.unitesk.testfusion.core.model.memory;

/**
 * Abstract class that represents an object allocated in the memory.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public abstract class MemoryObject
{
    /** Address of the object. */
    protected Address address = new Address();
    
    /** Size of the object. */
    protected int size;
    
    /** Default constructor. */
    public MemoryObject() {}

    /**
     * Constructor.
     * 
     * @param <code>address</code> the address of the object.
     */
    public MemoryObject(Address address)
    {
        this.address = address;
    }
    
    /**
     * Constructor.
     * 
     * @param <code>va</code> the virtual address of the object.
     * 
     * @param <code>pa</code> the physical address of the object.
     */
    public MemoryObject(long va, long pa)
    {
        this(new Address(va, pa));
    }

    /**
     * Constructor.
     * 
     * @param <code>size</code> the size of the object.
     */
    public MemoryObject(int size)
    {
        this.size = size;
    }

    /**
     * Constructor.
     * 
     * @param <code>address</code> the address of the object.
     * 
     * @param <code>size</code> the size of the object.
     */
    public MemoryObject(Address address, int size)
    {
        this(address);
        
        this.size = size;
    }
    
    /**
     * Constructor.
     * 
     * @param <code>va</code> the virtual address of the object.
     * 
     * @param <code>pa</code> the physical address of the object.
     * 
     * @param <code>size</code> the size of the object.
     */
    public MemoryObject(long va, long pa, int size)
    {
        this(new Address(va, pa));
        
        this.size = size;
    }
    
    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to memory object.
     */
    public MemoryObject(MemoryObject r)
    {
        address = r.address.clone();
        size = r.size;
    }
    
    /**
     * Returns the address of the object.
     * 
     * @return the address of the object.
     */
    public Address getAddress()
    {
        return address;
    }
    
    /**
     * Sets the address of the object.
     * 
     * @param <code>address</code> the address of the object.
     */
    public void setAddress(Address address)
    {
        this.address = address;
    }

    /**
     * Inrements the virtual and physical addresses of the object.
     * 
     * @param <code>offset</code> the increment value.
     */
    public void incrementAddress(long offset)
    {
        address.increment(offset);
    }

    /**
     * Decrements the virtual and physical addresses of the object.
     * 
     * @param <code>offset</code> the decrement value.
     */
    public void decrementAddress(long offset)
    {
        address.decrement(offset);
    }
    
    /**
     * Returns the virtual address of the object.
     * 
     * @return the virtual address of the object.
     */
    public long getVirtualAddress()
    {
        return address.va;
    }
    
    /**
     * Sets the virtual address of the object.
     * 
     * @param <code>va</code> the virtual address of the object.
     */
    public void setVirtualAddress(long va)
    {
        address.va = va;
    }
    
    /**
     * Returns the physical address of the object.
     * 
     * @return the physical address of the object.
     */
    public long getPhysicalAddress()
    {
        return address.pa;
    }
    
    /**
     * Sets the physical address of the object.
     * 
     * @param <code>pa</code> the physical address of the object.
     */
    public void setPhysicalAddress(long pa)
    {
        address.pa = pa;
    }
    
    /**
     * Returns the size of the object.
     *  
     * @return the size of the object.
     */
    public int getSize()
    {
        return size;
    }
    
    /**
     * Sets the size of the object.
     * 
     * @return the size of the object.
     */
    public void setSize(int size)
    {
        this.size = size;
    }
}
