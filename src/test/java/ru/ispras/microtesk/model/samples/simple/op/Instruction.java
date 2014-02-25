/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * Instruction.java, Nov 20, 2012 1:35:45 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.samples.simple.op;

import ru.ispras.microtesk.model.api.simnml.instruction.IOperation;
import ru.ispras.microtesk.model.api.simnml.instruction.Operation;

/*
    op Instruction(x: arith_mem_inst)
    syntax  = x.syntax
    image   = x.image
    actions = x.action
*/

public class Instruction extends Operation
{
    public static final IInfo INFO = new Info(Instruction.class, Instruction.class.getSimpleName());
    
    private static final IOperation.IInfo xINFO = Arith_Mem_Inst.INFO;

    private final IOperation x;

    public Instruction(IOperation x)
    {
        assert xINFO.isSupported(x);
        this.x = x;
    }

    @Override
    public String syntax()
    { 
        return x.syntax();
    }

    @Override
    public String image()
    { 
        return x.image();
    }

    @Override
    public void action() 
    { 
        x.action();
    }
}
