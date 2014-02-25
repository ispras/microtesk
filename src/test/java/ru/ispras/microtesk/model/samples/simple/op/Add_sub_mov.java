/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * Add_sub_mov.java, Nov 20, 2012 3:34:50 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.samples.simple.op;

import ru.ispras.microtesk.model.api.simnml.instruction.Operation;

/*
   Add_sub_mov = Add | Sub | Mov
 */

public abstract class Add_sub_mov extends Operation
{
    public static final IInfo INFO = new InfoOrRule(Add_sub_mov.class.getSimpleName(),
        Add.INFO,
        Sub.INFO,
        Mov.INFO
    );
}
