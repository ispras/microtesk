/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * Arith_Mem_Inst.java, Nov 20, 2012 1:31:25 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.samples.simple.op;

import ru.ispras.microtesk.model.api.simnml.instruction.IAddressingMode;
import ru.ispras.microtesk.model.api.simnml.instruction.IOperation;
import ru.ispras.microtesk.model.api.simnml.instruction.Operation;
import ru.ispras.microtesk.model.api.data.DataEngine;
import ru.ispras.microtesk.model.api.data.EOperatorID;
import ru.ispras.microtesk.model.samples.simple.mode.OPRNDL;
import ru.ispras.microtesk.model.samples.simple.mode.OPRNDR;

import static ru.ispras.microtesk.model.samples.simple.shared.Shared.*;

/*
    op arith_mem_inst(y: Add_sub_mov, op1: OPRND, op2: OPRND)
    uses   = y.uses
    syntax = format("%s %s %s", y.syntax, op1.syntax, op2.syntax)
    image  = format("%s %s 00%s", y.image, op1.image, op2.image)
    action = {
                 SRC1 = op1;
                 SRC2 = op2;
                 y.action;
                 op1 = DEST;
                 PC = PC + 2; 
             }
*/

public class Arith_Mem_Inst extends Operation
{
    public static final IInfo INFO = new Info(Arith_Mem_Inst.class, Arith_Mem_Inst.class.getSimpleName());

    private static final IOperation.IInfo        yINFO = Add_sub_mov.INFO;
    private static final IAddressingMode.IInfo op1INFO = OPRNDL.INFO;
    private static final IAddressingMode.IInfo op2INFO = OPRNDR.INFO;

    private IOperation        y; 
    private IAddressingMode op1;
    private IAddressingMode op2;

    public Arith_Mem_Inst(IOperation y, IAddressingMode op1, IAddressingMode op2)
    {
        assert yINFO.isSupported(y);
        assert op1INFO.isSupported(op1);
        assert op2INFO.isSupported(op2);

        this.y   = y;
        this.op1 = op1;
        this.op2 = op2;
    }

    @Override
    public String syntax() 
    {
        return String.format("%s %s %s", y.syntax(), op1.syntax(), op2.syntax()); 
    }

    @Override
    public String image()
    {
        // TODO: NOT SUPPORTED
        // image  = format("%s %s 00%s", y.image, op1.image, op2.image)
        return null;
    }

    /*
    SRC1 = op1;
    SRC2 = op2;
    y.action;
    op1 = DEST;
    PC = PC + 2;
    */

    @Override
    public void action()
    {
        SRC1.access().assign(op1.access());
        SRC2.access().assign(op2.access());

        y.action();

        op1.access().assign(DEST.access());

        PC.access().store(
            DataEngine.execute(
                EOperatorID.PLUS, PC.access().load(), DataEngine.valueOf(PC.getType(), 2)
            )
        );
    }
}
