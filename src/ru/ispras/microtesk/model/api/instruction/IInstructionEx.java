/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * IInstruction.java, Nov 21, 2012 3:43:47 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.api.instruction;

import ru.ispras.microtesk.model.api.metadata.IMetaInstruction;

public interface IInstructionEx extends IInstruction
{
    /**
     * Returns meta information on the specified instruction.
     * 
     * @return Meta data describing the given instruction.
     */

    public IMetaInstruction getMetaData();
}
