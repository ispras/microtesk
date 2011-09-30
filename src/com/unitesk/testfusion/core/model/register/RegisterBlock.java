/* 
 * Copyright (c) 2007 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: RegisterBlock.java,v 1.4 2009/04/21 13:03:19 kamkin Exp $
 */

package com.unitesk.testfusion.core.model.register;

import java.util.ArrayList;

import com.unitesk.testfusion.core.model.OperandType;

/**
 * Interface of registers' block.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class RegisterBlock<R extends Register> implements Register
{
    /** Block of registers. */
    protected ArrayList<R> block = new ArrayList<R>();

    /**
     * Constructs register pair.
     * 
     * @param <code>first</code> the first register of the pair.
     * 
     * @param <code>second</code> the second register of the pair.
     */
    public RegisterBlock(R first, R second)
    {
        block.add(first);
        block.add(second);
    }
    
    /**
     * Copy constructor.
     * 
     * @param <code>r</code> the reference to register block object.
     */
    @SuppressWarnings("unchecked")
    protected RegisterBlock(RegisterBlock<R> r)
    {
        int i, size;
        
        size = r.block.size();
        block = new ArrayList<R>(size);
        
        for(i = 0; i < size; i++)
        {
            R register = r.block.get(i);
            
            block.add((R)register.clone());
        }
    }
    
    /**
     * Returns the operand type.
     * 
     * @return the operand type.
     */
    public OperandType getRegisterType()
    {
        return OperandType.REGISTER;
    }

    /**
     * Checks if the register block contains the given register.
     * 
     * @param <code>register</code> the register.
     * 
     * @return <code>true</code> if the register block contains the given
     *         register; <code>false</code> otherwise.
     */
    public boolean contains(R register)
    {
        return block.contains(register);
    }
    
    /**
     * Returns the <code>n</code>-th register of the block.
     * 
     * @param <code>n</code> the register number.
     * 
     * @return the <code>n</code>-th register of the block.
     */
    public R getRegister(int n)
    {
        return block.get(n);
    }
    
    /**
     * Sets the <code>n</code>-th register of the block.
     * 
     * @param <code>n</code> the register number.
     */
    public void setRegister(int n, R register)
    {
        if(block.size() <= n)
        {
            while(block.size() < n)
                { block.add(null); }
            
            block.add(register);
        }
        else
        {
            block.remove(n);
            block.add(n, register);
        }
    }
    
    /**
     * Returns the register value.
     * 
     * @param <code>n</code> the register number.
     * 
     * @return the register value.
     */
    public boolean getBooleanValue(int n)
    {
        R register = block.get(n);
        
        return register.getBooleanValue();
    }

    /**
     * Sets the register value.
     *
     * @param <code>n</code> the register number.
     * 
     * @param <code>value</code> the register value.
     */
    public void setBooleanValue(int n, boolean value)
    {
        R register = block.get(n);
        
        register.setBooleanValue(value);
    }

    /**
     * Returns the register value.
     * 
     * @param <code>n</code> the register number.
     * 
     * @return the register value.
     */
    public byte getByteValue(int n)
    {
        R register = block.get(n);
        
        return register.getByteValue();
    }

    /**
     * Sets the register value.
     *
     * @param <code>n</code> the register number.
     * 
     * @param <code>value</code> the register value.
     */
    public void setByteValue(int n, byte value)
    {
        R register = block.get(n);
        
        register.setByteValue(value);
    }

    /**
     * Returns the register value.
     * 
     * @param <code>n</code> the register number.
     * 
     * @return the register value.
     */
    public short getShortValue(int n)
    {
        R register = block.get(n);
        
        return register.getShortValue();
    }

    /**
     * Sets the register value.
     *
     * @param <code>n</code> the register number.
     * 
     * @param <code>value</code> the register value.
     */
    public void setShortValue(int n, short value)
    {
        R register = block.get(n);
        
        register.setShortValue(value);
    }
    
    /**
     * Returns the register value.
     * 
     * @param <code>n</code> the register number.
     * 
     * @return the register value.
     */
    public int getIntegerValue(int n)
    {
        R register = block.get(n);
        
        return register.getIntegerValue();
    }

    /**
     * Sets the register value.
     *
     * @param <code>n</code> the register number.
     * 
     * @param <code>value</code> the register value.
     */
    public void setIntegerValue(int n, int value)
    {
        R register = block.get(n);
        
        register.setIntegerValue(value);
    }

    /**
     * Returns the register value.
     * 
     * @param <code>n</code> the register number.
     * 
     * @return the register value.
     */
    public long getLongValue(int n)
    {
        R register = block.get(n);
        
        return register.getLongValue();
    }

    /**
     * Sets the register value.
     *
     * @param <code>n</code> the register number.
     * 
     * @param <code>value</code> the register value.
     */
    public void setLongValue(int n, long value)
    {
        R register = block.get(n);
        
        register.setLongValue(value);
    }

    /**
     * Returns the register value.
     * 
     * @param <code>n</code> the register number.
     * 
     * @return the register value.
     */
    public float getSingleValue(int n)
    {
        R register = block.get(n);
        
        return register.getSingleValue();
    }

    /**
     * Sets the register value.
     *
     * @param <code>n</code> the register number.
     * 
     * @param <code>value</code> the register value.
     */
    public void setSingleValue(int n, float value)
    {
        R register = block.get(n);
        
        register.setSingleValue(value);
    }

    /**
     * Returns the register value.
     * 
     * @param <code>n</code> the register number.
     * 
     * @return the register value.
     */
    public double getDoubleValue(int n)
    {
        R register = block.get(n);
        
        return register.getDoubleValue();
    }

    /**
     * Sets the register value.
     *
     * @param <code>n</code> the register number.
     * 
     * @param <code>value</code> the register value.
     */
    public void setDoubleValue(int n, double value)
    {
        R register = block.get(n);
        
        register.setDoubleValue(value);
    }

    /**
     * Returns the first register value.
     * 
     * @return the register value.
     */
    public boolean getBooleanValue()
    {
        return getBooleanValue(0);
    }

    /**
     * Sets the first register value.
     *
     * @param <code>value</code> the register value.
     */
    public void setBooleanValue(boolean value)
    {
        setBooleanValue(0, value);
    }

    /**
     * Returns the first register value.
     * 
     * @return the register value.
     */
    public byte getByteValue()
    {
        return getByteValue(0);
    }

    /**
     * Sets the first register value.
     *
     * @param <code>value</code> the register value.
     */
    public void setByteValue(byte value)
    {
        setByteValue(0, value);
    }

    /**
     * Returns the first register value.
     * 
     * @return the register value.
     */
    public short getShortValue()
    {
        return getShortValue(0);
    }

    /**
     * Sets the first register value.
     *
     * @param <code>value</code> the register value.
     */
    public void setShortValue(short value)
    {
        setShortValue(0, value);
    }
    
    /**
     * Returns the first register value.
     * 
     * @return the register value.
     */
    public int getIntegerValue()
    {
        return getIntegerValue(0);
    }

    /**
     * Sets the first register value.
     *
     * @param <code>value</code> the register value.
     */
    public void setIntegerValue(int value)
    {
        setIntegerValue(0, value);
    }

    /**
     * Returns the first register value.
     * 
     * @return the register value.
     */
    public long getLongValue()
    {
        return getLongValue(0);
    }

    /**
     * Sets the first register value.
     *
     * @param <code>value</code> the register value.
     */
    public void setLongValue(long value)
    {
        setLongValue(0, value);
    }

    /**
     * Returns the first register value.
     * 
     * @return the register value.
     */
    public float getSingleValue()
    {
        return getSingleValue(0);
    }

    /**
     * Sets the first register value.
     *
     * @param <code>value</code> the register value.
     */
    public void setSingleValue(float value)
    {
        setSingleValue(0, value);
    }

    /**
     * Returns the first register value.
     * 
     * @return the register value.
     */
    public double getDoubleValue()
    {
        return getDoubleValue();
    }

    /**
     * Sets the register value.
     *
     * @param <code>n</code> the register number.
     * 
     * @param <code>value</code> the register value.
     */
    public void setDoubleValue(double value)
    {
        setDoubleValue(0, value);
    }

    /**
     * Checks if the register block is undefined.
     * 
     * @return <code>true</code> if the register is undefined;
     *         <code>false</code> otherwise.
     */
    public boolean isUnpredictable()
    {
        int i, size;
        
        size = block.size();
        for(i = 0; i < size; i++)
        {
            R register = getRegister(i);
            
            if(register.isUnpredictable())
                { return true; }
        }
        
        return false;
    }

    /**
     * Sets undefined/defined status of the register block.
     * 
     * @param <code>unpredictable</code> undefined/defined status of the
     *        register block. 
     */
    public void setUnpredictable(boolean unpredictable)
    {
        int i, size;
        
        size = block.size();
        for(i = 0; i < size; i++)
        {
            R register = getRegister(i);
            
            register.setUnpredictable(unpredictable);
        }
    }

    /**
     * Returns block size.
     * 
     * @return block size.
     */
    public int size()
    {
        return block.size();
    }
    
    /** Resets the state of the register block. */
    public void reset()
    {
        int i, size;
        
        size = block.size();
        for(i = 0; i < size; i++)
        {
            R register = getRegister(i);
            
            register.reset();
        }
    }

    /**
     * Returns string representation of the register block.
     * 
     * @return string representation of the register block.
     */
    public String toString()
    {
        int i, size;
        
        StringBuffer buffer = new StringBuffer("[");
        
        buffer.append(block.get(0).toString());
        
        size = block.size();
        for(i = 1; i < size; i++)
        {
            buffer.append(", ");
            buffer.append(block.get(i));
        }
        
        buffer.append("]");
        
        return buffer.toString();
    }
    
    /** Returns a copy of the register block. */
    public RegisterBlock<R> clone()
    {
        return new RegisterBlock<R>(this);
    }
}
