/*
 * Copyright (c) 2012 ISPRAS
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * Sub.java, Nov 20, 2012 1:26:58 PM Andrei Tatarnikov
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

package ru.ispras.microtesk.model.samples.simple.op;

import java.util.Map;

import ru.ispras.microtesk.model.api.instruction.IAddressingMode;
import ru.ispras.microtesk.model.api.instruction.IOperation;
import ru.ispras.microtesk.model.api.instruction.Operation;
import ru.ispras.microtesk.model.api.data.DataEngine;
import ru.ispras.microtesk.model.api.data.EOperatorID;
import ru.ispras.microtesk.model.samples.simple.mode.OPRNDL;
import ru.ispras.microtesk.model.samples.simple.mode.OPRNDR;

import static ru.ispras.microtesk.model.samples.simple.shared.Shared.*;

/*
    op Sub()
    syntax = "sub"
    image  = "01"
    action = {
                 DEST = SRC1 - SRC2; 
             }
*/

public class Sub extends Operation
{
    private static class Info extends InfoAndRule
    {
        Info()
        {
            super(
                Sub.class,
                Sub.class.getSimpleName(),
                new ParamDecls(),
                new Shortcuts()
                    .addShortcut("#root", new Info_Instruction())
            );
        }

        @Override
        public IOperation create(Map<String, Object> args)
        {
            return new Sub();
        }
    }
    
    // A short way to instantiate the operation with together with parent operations.
    private static class Info_Instruction extends InfoAndRule
    {
        Info_Instruction()
        {
            super(
               Instruction.class, 
               "SUB",
               new ParamDecls()
                   .declareParam("op1", OPRNDL.INFO)
                   .declareParam("op2", OPRNDR.INFO)
            );
        }

        @Override
        public IOperation create(Map<String, Object> args)
        {
            final IAddressingMode op1 = (IAddressingMode) getArgument("op1", args);
            final IAddressingMode op2 = (IAddressingMode) getArgument("op2", args);

            return new Instruction(
                new Arith_Mem_Inst(
                    new Sub(),
                    op1,
                    op2
                )
            );
        }
    }

    public static final IInfo INFO = new Info();

    public Sub() {}

    @Override 
    public String syntax() { return "sub"; }

    @Override
    public String image()  { return "01";  }

    @Override
    public void action()
    {
        DEST.access().store(
            DataEngine.execute(
                EOperatorID.MINUS,
                SRC1.access().load(),
                SRC2.access().load()
            )
        );
    }        
}
