/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * Model.java, Nov 15, 2012 8:23:24 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.samples.simple;

import ru.ispras.microtesk.model.api.simnml.SimnMLProcessorModel;
import ru.ispras.microtesk.model.samples.simple.instruction.ISA;

import static ru.ispras.microtesk.model.samples.simple.shared.Shared.*;

public final class Model extends SimnMLProcessorModel
{
    public Model()
    {
        super(
            new ISA(),
            __REGISTERS,
            __MEMORY
        );
    }
}
