/*
 * Copyright 2012-2015 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.model.samples.simple.op;

import static ru.ispras.microtesk.model.samples.simple.shared.Shared.DEST;
import static ru.ispras.microtesk.model.samples.simple.shared.Shared.SRC2;

import java.util.Map;

import ru.ispras.microtesk.model.api.ArgumentMode;
import ru.ispras.microtesk.model.api.instruction.IAddressingMode;
import ru.ispras.microtesk.model.api.instruction.IOperation;
import ru.ispras.microtesk.model.api.instruction.Operation;
import ru.ispras.microtesk.model.samples.simple.mode.OPRNDL;
import ru.ispras.microtesk.model.samples.simple.mode.OPRNDR;

/*
    op Mov()
    syntax = "mov"
    image  = "10"
    action = {
                 DEST = SRC2; 
             }
*/

public final class Mov extends Operation {
  private static class Info extends InfoAndRule {
    Info() {
      super(
          Mov.class,
          Mov.class.getSimpleName(),
          false,
          new ParamDecls(),
          false,
          false,
          false,
          false,
          false,
          0,
          new Shortcuts()
              .addShortcut(new Info_Instruction(), "#root")
          );
     }

     @Override
     public IOperation create(final Map<String, Object> args) {
      return new Mov();
    }
  }

  // A short way to instantiate the operation with together with parent operations.
  private static class Info_Instruction extends InfoAndRule {
    Info_Instruction() {
      super(
          Instruction.class, 
          "Mov",
          true,
          new ParamDecls()
              .declareParam("op1", ArgumentMode.NA, OPRNDL.INFO)
              .declareParam("op2", ArgumentMode.NA, OPRNDR.INFO),
          false,
          false,
          false,
          false,
          false,
          0
      );
    }

    @Override
    public IOperation create(final Map<String, Object> args) {
      final IAddressingMode op1 = (IAddressingMode) getArgument("op1", args);
      final IAddressingMode op2 = (IAddressingMode) getArgument("op2", args);

      return new Instruction(
          new Arith_Mem_Inst(
              new Mov(),
              op1,
              op2
          )
      );
    }
  }

  public static final IInfo INFO = new Info();

  public Mov() {}

  @Override 
  public String syntax() { return "mov"; }

  @Override
  public String image() { return "10"; }

  @Override
  public void action() {
    DEST.access().assign(SRC2.access());
  }
}
