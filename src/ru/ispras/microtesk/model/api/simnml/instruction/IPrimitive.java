/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * IPrimitive.java, Nov 2, 2012 2:36:58 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.api.simnml.instruction;

/**
 * The IPrimitive interface is a base interface for Op (specifies an operation)
 * and Mode (specifies an addressing mode) Sim-nML primitives.
 * 
 * @author Andrei Tatarnikov
 */

public interface IPrimitive
{
    /**
     * Returns textual representation of the specified primitive.
     * 
     * @return Text value.
     */
    
    public String syntax();
    
    /**
     * Returns binary representation of the specified primitive. 
     * 
     * @return Binary text.
     */
    
    public String image();
       
    /**
     * Runs the action code associated with the primitive.
     */
    
    public void action(); 
}
