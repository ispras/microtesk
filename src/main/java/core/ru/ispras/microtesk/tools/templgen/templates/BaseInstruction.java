/*
 * Copyright 2018 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.tools.templgen.templates;

import java.util.Collection;

import ru.ispras.microtesk.model.metadata.MetaArgument;
import ru.ispras.microtesk.model.metadata.MetaOperation;
import ru.ispras.microtesk.tools.templgen.printers.TemplatePrinter;

/**
 * The {@link AutoGenInstruction} abstract class for instructions used in the template auto generation.
 *
 * @author <a href="mailto:protsenko@ispras.ru">Alexander Protsenko</a>
 */

public abstract class BaseInstruction implements AutoGenInstruction {
  private final String name; // Operation name

  private final boolean branch;
  private final boolean jump;
  private final boolean store;
  private final boolean load;
  private final boolean arithmetic;
  private String command;

  /**
   * Constructs a templegen instruction.
   *
   * @param operation operation.
   * @param templatePrinter printer for the template.
   */
  private final TemplatePrinter templatePrinter;
  BaseInstruction(final MetaOperation operation, final TemplatePrinter templatePrinter) {
    //this.initLabelsSet();
    this.templatePrinter = templatePrinter;
    name = this.templatePrinter.formattingOperation(operation.getName());

    branch = operation.isConditionalBranch();// || ((name.startsWith("b") || name.startsWith("j")) && (getArgumentsNumber(operation.getArguments()) > 0));
    jump = operation.isBranch() && !branch;

    load = (operation.isLoad()) ? Boolean.TRUE : Boolean.FALSE;
    store = (operation.isStore()) ? Boolean.TRUE : Boolean.FALSE;
    arithmetic =
        (!branch && !jump && getArgumentsNumber(operation.getArguments()) == 3 && !load && !store)
            ? Boolean.TRUE
            : Boolean.FALSE;

  }  //public final boolean branch;

  public boolean isBranchOperation() {
    return branch || jump;
  }

  public String getName() {
    return name;
  }

  protected static int getArgumentsNumber(final Iterable<MetaArgument> arguments) {
    if (arguments instanceof Collection<?>) {
      return ((Collection<?>) arguments).size();
    }
    return 0;
  }

}
