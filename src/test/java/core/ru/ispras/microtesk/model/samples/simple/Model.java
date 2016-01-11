/*
 * Copyright 2012-2016 ISP RAS (http://www.ispras.ru)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package ru.ispras.microtesk.model.samples.simple;

import static ru.ispras.microtesk.model.samples.simple.shared.Shared.__LABELS;
import static ru.ispras.microtesk.model.samples.simple.shared.Shared.__MEMORY;
import static ru.ispras.microtesk.model.samples.simple.shared.Shared.__REGISTERS;
import static ru.ispras.microtesk.model.samples.simple.shared.Shared.__RESETTER;
import static ru.ispras.microtesk.model.samples.simple.shared.Shared.__STATUSES;

import ru.ispras.microtesk.model.api.ProcessorModel;
import ru.ispras.microtesk.model.api.metadata.MetaModelPrinter;
import ru.ispras.microtesk.model.api.state.ModelStatePrinter;
import ru.ispras.microtesk.model.api.instruction.IAddressingMode;
import ru.ispras.microtesk.model.api.instruction.IOperation;
import ru.ispras.microtesk.model.samples.simple.mode.IMM;
import ru.ispras.microtesk.model.samples.simple.mode.IREG;
import ru.ispras.microtesk.model.samples.simple.mode.MEM;
import ru.ispras.microtesk.model.samples.simple.mode.OPRNDL;
import ru.ispras.microtesk.model.samples.simple.mode.OPRNDR;
import ru.ispras.microtesk.model.samples.simple.mode.REG;
import ru.ispras.microtesk.model.samples.simple.op.Add;
import ru.ispras.microtesk.model.samples.simple.op.Add_sub_mov;
import ru.ispras.microtesk.model.samples.simple.op.Arith_Mem_Inst;
import ru.ispras.microtesk.model.samples.simple.op.Instruction;
import ru.ispras.microtesk.model.samples.simple.op.Mov;
import ru.ispras.microtesk.model.samples.simple.op.Sub;

public final class Model extends ProcessorModel {
  public static final String NAME = "simple";

  private static final IAddressingMode.IInfo[] __MODES = new IAddressingMode.IInfo[] {
    IMM.INFO,
    IREG.INFO,
    MEM.INFO,
    REG.INFO
  };

  private static final IAddressingMode.IInfo[] __MODE_GROUPS = new IAddressingMode.IInfo[] {
    OPRNDL.INFO,
    OPRNDR.INFO
  };

  private static final IOperation.IInfo[] __OPS = new IOperation.IInfo[] {
    Add.INFO,
    Sub.INFO,
    Mov.INFO,
    Arith_Mem_Inst.INFO,
    Instruction.INFO
  };

  private static final IOperation.IInfo[] __OP_GROUPS = new IOperation.IInfo[] {
    Add_sub_mov.INFO
  };

  public Model() {
    super(
        NAME,
        __MODES,
        __MODE_GROUPS,
        __OPS,
        __OP_GROUPS,
        __REGISTERS,
        __MEMORY,
        __LABELS,
        __STATUSES,
        __RESETTER
        );
  }

  public static void printInformation() {
    final Model model = new Model();

    final MetaModelPrinter metaModelPrinter = new MetaModelPrinter(model.getMetaData());
    metaModelPrinter.printAll();

    final ModelStatePrinter modelStatePrinter = new ModelStatePrinter(model);
    modelStatePrinter.printRegisters();
  }

  public static void main(String[] args) {
    printInformation();
  }
}
