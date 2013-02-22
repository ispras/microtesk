/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * OPRNDR.java, Nov 20, 2012 1:20:44 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.samples.simple.mode;

import ru.ispras.microtesk.model.api.simnml.instruction.AddressingMode;

/*
    mode OPRNDR = OPRNDL | IMM
*/

public abstract class OPRNDR extends AddressingMode 
{
    public static final String NAME = "OPRNDR";
    
    public static final IInfo INFO = new InfoOrRule(NAME,
        OPRNDL.INFO,
        IMM.INFO
    );     
}
