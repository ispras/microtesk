/* 
 * Copyright (c) 2007 ISPRAS
 *
 * Institute for System Programming of Russian Academy of Sciences
 * 25 B.Kommunisticheskaya st. Moscow 109004 Russia
 *
 * All rights reserved.
 *
 * $Id: Register.java,v 1.5 2009/04/09 08:38:29 kamkin Exp $
 */

package com.unitesk.testfusion.core.model.register;

import com.unitesk.testfusion.core.model.OperandType;
import com.unitesk.testfusion.core.model.ResetableObject;

/**
 * Interface of microprocessor register.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public interface Register extends ResetableObject
{
    /**
     * Returns the operand type.
     * 
     * @return the operand type.
     */
    public OperandType getRegisterType();
    
    /**
     * Checks if the register is undefined.
     * 
     * @return <code>true</code> if the register is undefined;
     *         <code>false</code> otherwise.
     */
    public boolean isUnpredictable();
    
    /**
     * Sets undefined/defined status of the register.
     * 
     * @param <code>unpredictable</code> undefined/defined status of the register. 
     */
    public void setUnpredictable(boolean unpredictable);
    
    /**
     * Returns the register value.
     * 
     * @return the register value.
     */
    public boolean getBooleanValue();

    /**
     * Sets the register value.
     * 
     * @param <code>value</code> the register value.
     */
    public void setBooleanValue(boolean value);
    
    /**
     * Returns the register value.
     * 
     * @param <code>value</code> the register value.
     */
    public byte getByteValue();
    
    /**
     * Sets the register value.
     * 
     * @param <code>value</code> the register value.
     */
    public void setByteValue(byte value);

    /**
     * Returns the register value.
     * 
     * @param <code>value</code> the register value.
     */
    public short getShortValue();
    
    /**
     * Sets the register value.
     * 
     * @param <code>value</code> the register value.
     */
    public void setShortValue(short value);
    
    /**
     * Returns the register value.
     * 
     * @return the register value.
     */
    public int getIntegerValue();

    /**
     * Sets the register value.
     * 
     * @param <code>value</code> the register value.
     */
    public void setIntegerValue(int value);
    
    /**
     * Returns the register value.
     * 
     * @return the register value.
     */
    public long getLongValue();
    
    /**
     * Sets the register value.
     * 
     * @param <code>value</code> the register value.
     */
    public void setLongValue(long value);
    
    /**
     * Returns the register value.
     * 
     * @return the register value.
     */
    public float getSingleValue();

    /**
     * Sets the register value.
     * 
     * @param <code>value</code> the register value.
     */
    public void setSingleValue(float value);
    
    /**
     * Returns the register value.
     * 
     * @return the register value.
     */
    public double getDoubleValue();
    
    /**
     * Sets the register value.
     * 
     * @param <code>value</code> the register value.
     */
    public void setDoubleValue(double value);

    /** Returns a copy of the register. */
    public Register clone();
}
