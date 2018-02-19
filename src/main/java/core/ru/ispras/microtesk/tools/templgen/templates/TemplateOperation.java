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

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.model.ArgumentMode;
import ru.ispras.microtesk.model.IsaPrimitiveKind;
import ru.ispras.microtesk.model.metadata.MetaArgument;
import ru.ispras.microtesk.model.metadata.MetaOperation;
import ru.ispras.microtesk.tools.templgen.printers.TemplatePrinter;

import java.util.Collection;
import java.util.Iterator;

/**
 * @author <a href="mailto:protsenko@ispras.ru">Alexander Protsenko</a>
 */
public class TemplateOperation {
  private static final int JUMP_REG = 3;

  private final String name; // Operation name
  private final boolean branch;
  private final boolean store;
  private final boolean load;
  private final boolean arithmetic;
  private final String command;

  private String branchLabel;
  private String regTitle;
  private String preCommand;
  private String[] postCommand;

  TemplateOperation(final MetaOperation operation, final TemplatePrinter templatePrinter) {
    printMetaOperation(operation);

    name = templatePrinter.formattingOperation(operation.getName());

    // TODO: branch
    branch = (name.startsWith("b") || name.startsWith("j")) ? Boolean.TRUE : Boolean.FALSE;

    load = (operation.isLoad()) ? Boolean.TRUE : Boolean.FALSE;
    store = (operation.isStore()) ? Boolean.TRUE : Boolean.FALSE;
    arithmetic = (!branch && getArgumentsNumber(operation.getArguments()) == 3 && !load && !store)
        ? Boolean.TRUE
        : Boolean.FALSE;

    if (branch) {
      branchLabel = String.format(":%s_label", name);
      regTitle = getLastArgument(operation.getArguments(), IsaPrimitiveKind.MODE);

      if (regTitle != "JUMP_LABEL" && regTitle != "BRANCH_LABEL" && regTitle != "BRANCH_LABEL_M2"
          && regTitle != "JUMP_LABEL_M2") {
        System.out.println(regTitle);
        preCommand = prepareReg(regTitle);
      } else {
        preCommand = "";
      }

      postCommand = new String[3];
      postCommand[0] = String.format("nop");
      postCommand[1] = String.format("label " + branchLabel);
      postCommand[2] = String.format("nop");
    }

    command = setCommand(operation.getArguments(), name);
  }

  private static void printMetaOperation(MetaOperation operation) {
    System.out.format("getTypeName: %s \n", operation.toString());
    System.out.format("Operation: %s \n", operation.getName());
    System.out.format("getTypeName: %s \n", operation.getTypeName());
    System.out.format("getDataType: %s \n", operation.getDataType());
    System.out.format("hasRootShortcuts: %s \n", operation.hasRootShortcuts());
    System.out.format("isBranch: %s \n", operation.isBranch());
    System.out.format("isConditionalBranch: %s \n", operation.isConditionalBranch());
    System.out.format("isLoad: %s \n", operation.isLoad());
    System.out.format("isRoot: %s \n", operation.isRoot());
    System.out.format("isStore: %s \n", operation.isStore());
    System.out.format("getTypeName: %s \n", operation);

    Iterable<MetaArgument> arguments = operation.getArguments();

    for (MetaArgument argument : arguments) {
      System.out.format("getName: %s \n", argument.getName());
      System.out.format("getMode: %s \n", argument.getMode());
      System.out.format("getDataType: %s \n", argument.getDataType());
      System.out.format("getKind: %s \n", argument.getKind());
      System.out.format("getTypeNames: %s \n", argument.getTypeNames());
      System.out.format("toString: %s \n\n", argument.toString());
    }

    System.out.format("\n");
  }

  private String setCommand(final Iterable<MetaArgument> arguments, final String name) {
    String tempCommand = name + " ";

    boolean commaIndicator = false;
    // for (MetaArgument argument : arguments) {
    for (Iterator<MetaArgument> iterator = arguments.iterator(); iterator.hasNext();) {
      MetaArgument argument = (MetaArgument) iterator.next();

      Collection<String> tempTypes = argument.getTypeNames();
      if (commaIndicator) {
        tempCommand += ", ";
      }

      if ((argument.getKind() == IsaPrimitiveKind.MODE)) {
        boolean printLabel = false;
        //for (String tempType : tempTypes) {
        if (tempTypes != null) {System.out.println("!!!!!!!!!!!!!!!             " + tempTypes.size());}
        Object[] strArray = (Object[]) tempTypes.toArray();
        String tempType = (String) strArray[0];
          if (tempType == "BRANCH_IMM" || tempType == "BRANCH_LABEL" || tempType == "JUMP_IMM"
              || tempType == "JUMP_LABEL" || tempType == "BRANCH_LABEL_M2"
              || tempType == "JUMP_LABEL_M2") {
            if (!printLabel) {
              tempCommand += branchLabel;
              printLabel = true;
            }
          } else {
            if (argument.getMode() == ArgumentMode.IN && branch
                && getArgumentNumbers(arguments, IsaPrimitiveKind.IMM) == 0
                && !iterator.hasNext()) {
              tempCommand += String.format("%s(%x)", tempType.toLowerCase(), JUMP_REG);
            } else {
              tempCommand += String.format("%s(_)", tempType.toLowerCase());
            }
          }
        //}
      }

      if (argument.getKind() == IsaPrimitiveKind.IMM) {
        if (!branch) {
          /*
           * tempCommand += String.format("rand(%s, %s)", 0, (long) Math.pow(2,
           * argument.getDataType().getBitSize()) - 1);
           */
          tempCommand += String.format("_");
        } else {
          tempCommand += branchLabel;
        }
      }
      commaIndicator = true;
    }

    return tempCommand;
  }

  private static String getLastArgument(final Iterable<MetaArgument> arguments,
      final IsaPrimitiveKind type) {
    for (Iterator<MetaArgument> iterator = arguments.iterator(); iterator.hasNext();) {
      MetaArgument argument = (MetaArgument) iterator.next();

      if (!iterator.hasNext()) {
        Collection<String> tempTypes = argument.getTypeNames();

        if (argument.getKind() == type) {
          for (String tempType : tempTypes) {
            return tempType;
          }
        }
      }
    }
    return null;
  }

  private static int getArgumentNumbers(final Iterable<MetaArgument> arguments,
      final IsaPrimitiveKind type) {
    InvariantChecks.checkNotNull(arguments);
    InvariantChecks.checkNotNull(type);
    int argumentNumbers = 0;
    for (MetaArgument argument : arguments) {
      if (argument.getKind() == type) {
        argumentNumbers++;
      }
    }
    return argumentNumbers;
  }

  private static int getArgumentsNumber(Iterable<MetaArgument> arguments) {
    if (arguments instanceof Collection<?>) {
      return ((Collection<?>) arguments).size();
    }
    return 0;
  }

  private String prepareReg(String regType) {
    return String.format("la %s(%x), %s", regType, JUMP_REG, branchLabel);
    // TODO prepare REG(1), get_address_of(:j_label)
  }

  /**
   * Returns the command syntax.
   *
   * @return command syntax.
   */
  public String getCommand() {
    return command;
  }

  public String getPreCommand() {
    return preCommand;
  }

  public String[] getPostCommand() {
    return postCommand;
  }

  public boolean isBranchOperation() {
    return branch;
  }

  public boolean isStoreOperation() {
    return store;
  }

  public boolean isLoadOperation() {
    return load;
  }

  public boolean isArithmeticOperation() {
    return arithmetic;
  }

  /**
   * Prints operation to the {@code TemplatePrinter}.
   *
   * @param templatePrinter the templates printer.
   */
  public void printOperationBlock(final TemplatePrinter templatePrinter) {
    if (branch) {
      String tempPreCommand = this.getPreCommand();
      if (tempPreCommand != null && !tempPreCommand.isEmpty()) {
        templatePrinter.addString(tempPreCommand);
      }
    }
    templatePrinter.addString(this.getCommand());
    if (branch) {
      String[] postCommand = this.getPostCommand();

      for (int i = 0; i < postCommand.length; i++) {
        templatePrinter.addString(postCommand[i]);
      }
    }
  }
}
