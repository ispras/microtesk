/* 
 * Copyright (c) 2007 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: Register32.java,v 1.5 2008/08/19 11:08:55 kamkin Exp $
 */

package com.unitesk.testfusion.core.model.register;

import com.unitesk.testfusion.core.model.OperandType;

/**
 * 32-bit register of microprocessor. 
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class Register32 implements Register
{
    /**
     * Bit field of 32-bit register.
     * 
     * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
     */
    public static class Field
    {
        /** Lower bound of the field. */
        protected int min;
        
        /** Upper bound of the field. */
        protected int max;

        /** Bit mask of the field. */
        protected int mask;

        /**
         * Constructor.
         * 
         * @param <code>min</code> the lower bound of the field.
         * 
         * @param <code>max</code> the upper bound of the field.
         */
        public Field(int min, int max)
        {
            this.min = min;
            this.max = max;

            this.mask = ((0xffffffff << 31 - max) >>> 31 - max + min) << min;
        }

        /**
         * Constructor of 1-bit field.
         * 
         * @param <code>bit</code> the bit position.
         */
        public Field(int bit)
        {
            this(bit, bit);
        }

        /**
         * Returns the lower bound of the field.
         * 
         * @return the lower bound of the field.
         */
        public int getMin()
        {
            return min;
        }

        /**
         * Returns the upper bound of the field.
         * 
         * @return the upper bound of the field.
         */
        public int getMax()
        {
            return max;
        }

        /**
         * Returns the bit mask of the field.
         * 
         * @return the bit mask of the field.
         */
        public int getMask()
        {
            return mask;
        }
    }

    /** Value of the register. */
    protected int value;

    /** Undefined/defined status of the register. */
    protected boolean unpredictable;

    /** Default constructor. The initial value of the register is zero. */
    public Register32()
    {
        this(0);
    }

    /**
     * Constructor.
     * 
     * @param <code>value</code> the value of the register.
     */
    public Register32(int value)
    {
        this.value = value;
        this.unpredictable = false;
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
     * Checks if the register is undefined.
     * 
     * @return <code>true</code> if the register is undefined;
     *         <code>false</code> otherwise.
     */
    public boolean isUnpredictable()
    {
        return unpredictable;
    }
    
    /**
     * Sets undefined/defined status of the register.
     * 
     * @param <code>unpredictable</code> undefined/defined status of the register. 
     */
    public void setUnpredictable(boolean unpredictable)
    {
        this.unpredictable = unpredictable;
    }
    
    /**
     * Returns the register value.
     * 
     * @return the register value.
     */
    public boolean getBooleanValue()
    {
        return value != 0;
    }
    
    /**
     * Sets the register value.
     * 
     * @param <code>value</code> the register value.
     */
    public void setBooleanValue(boolean booleanValue)
    {
        value = booleanValue ? 1 : 0;
    }

    /**
     * Returns the register value.
     * 
     * @param <code>value</code> the register value.
     */
    public byte getByteValue()
    {
        return (byte)value;
    }
    
    /**
     * Sets the register value.
     * 
     * @param <code>value</code> the register value.
     */
    public void setByteValue(byte value)
    {
        this.value = value;
    }

    /**
     * Returns the register value.
     * 
     * @param <code>value</code> the register value.
     */
    public short getShortValue()
    {
        return (short)value;
    }
    
    /**
     * Sets the register value.
     * 
     * @param <code>value</code> the register value.
     */
    public void setShortValue(short value)
    {
        this.value = value;
    }
    
    /**
     * Returns the register value.
     * 
     * @return the register value.
     */
    public int getIntegerValue()
    {
        return value;
    }

    /**
     * Sets the register value.
     * 
     * @param <code>value</code> the register value.
     */
    public void setIntegerValue(int value)
    {
        this.value = value;
    }
    
    /**
     * Returns the register value.
     * 
     * @return the register value.
     */
    public long getLongValue()
    {
        return value;
    }

    /**
     * Sets the register value.
     * 
     * @param <code>value</code> the register value.
     */
    public void setLongValue(long value)
    {
        this.value = (int)(value & 0xffffffffL);
    }
    
    /**
     * Returns the register value.
     * 
     * @return the register value.
     */
    public float getSingleValue()
    {
        return Float.intBitsToFloat(getIntegerValue());
    }
    
    /**
     * Sets the register value.
     * 
     * @param <code>value</code> the register value.
     */
    public void setSingleValue(float value)
    {
        setIntegerValue(Float.floatToRawIntBits(value));
    }
    
    /**
     * Returns the register value.
     * 
     * @return the register value.
     */
    public double getDoubleValue()
    {
        throw new RuntimeException();
    }

    /**
     * Sets the register value.
     * 
     * @param <code>value</code> the register value.
     */
    public void setDoubleValue(double value)
    {
        throw new RuntimeException();
    }
    
    /**
     * Returns the value of the field.
     * 
     * @param  <code>field</code> the field of the register.
     * 
     * @return the value of the field.
     */
    public int getFieldValue(Field field)
    {
        return (value & field.getMask()) >> field.getMin();
    }

    /**
     * Sets the value of the field.
     * 
     * @param <code>field</code> the field of the register.
     * 
     * @param <code>value</code> the value of the field.
     */
    public void setFieldValue(Field field, int value)
    {
        this.value &= ~field.getMask();
        this.value |= (value << field.getMin()) & field.getMask();
    }

    /** Resets the state of the register. */
    public void reset()
    {
        value = 0;
    }
    
    /**
     * Returns a copy of the register.
     * 
     * @return a copy of the register.
     */
    public Register32 clone()
    {
        return new Register32(value);
    }
}
