/*
 * Copyright (c) 2013 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * Call.java, Apr 30, 2013 1:12:59 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.test.block;

import ru.ispras.microtesk.model.api.instruction.IInstruction;

public class Call implements Entry
{
    private IInstruction instruction;

    public Call(IInstruction instruction)
    {
        this.instruction = instruction; 
    }

    public void dummy()
    {
        //instruction.createCallBuilder().
    }
    
    //private final String instruction;
    // instruction name
    // arguments 
    // situations
    // other attributes
}
