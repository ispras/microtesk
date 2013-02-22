/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * OPRNDL.java, Nov 20, 2012 1:20:44 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.samples.simple.mode;

import ru.ispras.microtesk.model.api.simnml.instruction.AddressingMode;

/*
    mode OPRNDL = MEM | REG | IREG
*/

public abstract class OPRNDL extends AddressingMode 
{
    public static final String NAME = "OPRNDL";
    
    public static final IInfo INFO = new InfoOrRule(NAME,
        MEM.INFO,
        REG.INFO,
        IREG.INFO
    );     
}
