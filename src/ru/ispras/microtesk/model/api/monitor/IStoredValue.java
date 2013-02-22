/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * IStoredValue.java, Nov 9, 2012 11:36:59 AM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.api.monitor;

import java.math.BigInteger;

/**
 * The IStoredValue interface provides access to the value
 * stored in the specified location. 
 * 
 * @author Andrei Tatarnikov
 */

public interface IStoredValue
{
    /**
     * Returns the size of the location in bits.
     * 
     * @return Size in bits.
     */
    
    public int getBitSize();
    
    /**
     * Returns the value stored in the location packed in a BigInteger object.
     * 
     * @return Value packed in a BigInteger object.
     */

    public BigInteger getValue();
    
    /**
     * Returns textual representation of stored data (a string of 0 and 1 characters).
     * 
     * @return Binary string.
     */

    public String toBinString();
}
