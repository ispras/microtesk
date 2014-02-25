/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * ILocationAccessor.java, Nov 9, 2012 11:36:59 AM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.api.memory;

import java.math.BigInteger;

/**
 * The ILocationAccessor interface provides access to data
 * stored in the specified location. 
 * 
 * @author Andrei Tatarnikov
 */

public interface ILocationAccessor
{
    /**
     * Returns the size of the location in bits.
     * 
     * @return Size in bits.
     */
    
    public int getBitSize();
    
    /**
     * Returns textual representation of stored data (a string of 0 and 1 characters).
     * 
     * @return Binary string.
     */

    public String toBinString();
    
    /**
     * Returns the value stored in the location packed in a BigInteger object.
     * 
     * @return Binary data packed in a BigInteger object.
     */

    public BigInteger getValue();
    
    /**
     * Sets the value of the specified location.
     * 
     * @param value Binary data packed in a BigInteger object.
     */
    
    public void setValue(BigInteger value);
}
