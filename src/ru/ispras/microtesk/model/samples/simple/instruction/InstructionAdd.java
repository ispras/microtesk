/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * InstructionAdd.java, Nov 26, 2012 3:23:27 PM Andrei Tatarnikov
 */

package ru.ispras.microtesk.model.samples.simple.instruction;

import ru.ispras.microtesk.model.api.exception.ConfigurationException;

import ru.ispras.microtesk.model.api.instruction.*;
import ru.ispras.microtesk.model.api.simnml.instruction.*;
import ru.ispras.microtesk.model.samples.simple.mode.*;
import ru.ispras.microtesk.model.samples.simple.op.*;

public class InstructionAdd extends InstructionBase
{
    public static final String NAME = "Add";

    public static final ParamDecl[] PARAMS = new ParamDecl[] 
    {
        new ParamDecl("op1", OPRNDL.INFO),
        new ParamDecl("op2", OPRNDR.INFO)
    };

    public InstructionAdd()
    { 
        super(NAME, PARAMS);
    }

    @Override
    public IInstructionCallBuilderEx createCallBuilder()
    {
        return new Builder();
    }

    private class Builder extends CallBuilderBase
    {
        public Builder()
        {
            super(PARAMS);
        }

        @Override
        public IInstructionCall getCall() throws ConfigurationException
        {
            final IAddressingMode op1 = getArgument("op1");
            final IAddressingMode op2 = getArgument("op2");

            return new InstructionCall(
                new Instruction(
                    new Arith_Mem_Inst(new Add(), op1, op2)
                )
           );
        }
    }
}
