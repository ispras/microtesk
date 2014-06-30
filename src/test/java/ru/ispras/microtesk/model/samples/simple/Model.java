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

package ru.ispras.microtesk.model.samples.simple;

import ru.ispras.microtesk.model.api.debug.MetaModelPrinter;
import ru.ispras.microtesk.model.api.debug.ModelStatePrinter;
import ru.ispras.microtesk.model.api.ProcessorModel;
import ru.ispras.microtesk.model.samples.simple.instruction.ISA;
import ru.ispras.microtesk.test.data.IInitializerGenerator;
import ru.ispras.microtesk.model.api.instruction.IAddressingMode;
import ru.ispras.microtesk.model.api.instruction.IOperation;
import ru.ispras.microtesk.model.samples.simple.mode.*;
import ru.ispras.microtesk.model.samples.simple.op.*;

import static ru.ispras.microtesk.model.samples.simple.shared.Shared.*;

public final class Model extends ProcessorModel
{
    private static final IAddressingMode.IInfo[] __MODES = new IAddressingMode.IInfo[]
    { 
        IMM.INFO,
        IREG.INFO,
        MEM.INFO,
        REG.INFO
    };

    private static final IOperation.IInfo[] __OPS = new IOperation.IInfo[]
    {
        Add.INFO,
        Sub.INFO,
        Mov.INFO,
        Arith_Mem_Inst.INFO,
        Instruction.INFO
    };

    private static final IInitializerGenerator[] __INITIALIZERS = {};

    public Model()
    {
        super(
            new ISA(),
            __MODES,
            __OPS,
            __REGISTERS,
            __MEMORY,
            __LABELS,
            __STATUSES,
            __RESETTER
        );
    }

    @Override
    public IInitializerGenerator[] getInitializers()
    {
        return __INITIALIZERS;
    }

    public static void printInformation()
    {
        final Model model = new Model();

        final MetaModelPrinter metaModelPrinter = new MetaModelPrinter(model.getMetaData());
        metaModelPrinter.printAll();

        final ModelStatePrinter modelStatePrinter = new ModelStatePrinter(model);
        modelStatePrinter.printRegisters();
    }

    public static void main(String[] args)
    {
        printInformation();
    }
}
