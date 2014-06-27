/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * InstructionSub.java, Nov 26, 2012 3:23:41 PM Andrei Tatarnikov
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package ru.ispras.microtesk.model.samples.simple.instruction;

import ru.ispras.microtesk.model.api.exception.ConfigurationException;

import ru.ispras.microtesk.model.api.instruction.*;
import ru.ispras.microtesk.model.api.situation.ISituation;
import ru.ispras.microtesk.model.samples.simple.mode.*;
import ru.ispras.microtesk.model.samples.simple.op.*;

import static ru.ispras.microtesk.model.samples.simple.shared.Shared.*;

public class InstructionSub extends InstructionBase
{
    public static final String NAME = "Sub";

    public static final ParamDecl[] PARAMS = new ParamDecl[] 
    {
        new ParamDecl("op1", OPRNDL.INFO),
        new ParamDecl("op2", OPRNDR.INFO)
    };

    public static final ISituation.IInfo[] SITUATIONS = new ISituation.IInfo[]
    {
    };

    public InstructionSub()
    { 
        super(NAME, PARAMS, SITUATIONS);
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
        public InstructionCall getCall() throws ConfigurationException
        {
            final IAddressingMode op1 = getArgument("op1");
            final IAddressingMode op2 = getArgument("op2");
            
            return new InstructionCall(
                __RESETTER,
                new Instruction(
                    new Arith_Mem_Inst(new Sub(), op1, op2)
                )
           );
        }
    }
}
