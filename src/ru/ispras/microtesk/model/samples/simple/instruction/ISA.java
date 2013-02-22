/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * ISA.java, Nov 8, 2012 4:12:07 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.samples.simple.instruction;

import ru.ispras.microtesk.model.api.instruction.IInstruction;
import ru.ispras.microtesk.model.api.instruction.InstructionSet;

public class ISA extends InstructionSet
{
    private static final IInstruction[] ENTRIES = new IInstruction[] 
    {
        new InstructionAdd(),
        new InstructionSub(),
        new InstructionMov()
    };

    public ISA()
    {
        super(ENTRIES);
    }
}
