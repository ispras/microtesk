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
import ru.ispras.microtesk.model.metadata.MetaModel;
import ru.ispras.microtesk.model.metadata.MetaOperation;
import ru.ispras.microtesk.tools.templgen.printers.TemplatePrinter;

import java.util.Collection;
import java.util.Iterator;

/**
 * @author <a href="mailto:protsenko@ispras.ru">Alexander Protsenko</a>
 */
public class TemplateOperation {
  private static final int JUMP_REG = 3;
  private static final int DATA_REG = 5;

  private final String name; // Operation name
  private final boolean branch;
  private final boolean jump;
  private final boolean store;
  private final boolean load;
  private final boolean arithmetic;
  private final String command;
  private final TemplatePrinter templatePrinter;

  private String branchLabel;
  private String regTitle;
  private String preCommand;
  private String[] postCommand;

  private final MetaModel metaModel;
  //private Set<String> jumpLabelsSet;

  TemplateOperation(final MetaOperation operation, final TemplatePrinter templatePrinter, final MetaModel metaModel) {
    this.metaModel = metaModel;
    this.templatePrinter = templatePrinter;
    name = this.templatePrinter.formattingOperation(operation.getName());

    branch = operation.isConditionalBranch() || ((name.startsWith("b") || name.startsWith("j")) && (getArgumentsNumber(operation.getArguments()) > 0));
    jump = operation.isBranch() && !branch;

   //if (branch || jump) {
      //TemplatesUtils.printMetaOperation(operation);
      //printMetaOperation(operation);
      /*System.out.println(operation.getName());
      System.out.print(branch);
      System.out.print(jump);
      System.out.println();*/
   // }

    load = (operation.isLoad()) ? Boolean.TRUE : Boolean.FALSE;
    store = (operation.isStore()) ? Boolean.TRUE : Boolean.FALSE;
    arithmetic =
        (!branch && !jump && getArgumentsNumber(operation.getArguments()) == 3 && !load && !store)
            ? Boolean.TRUE
            : Boolean.FALSE;

    if (branch || jump) {
      //System.out.println(name);
      branchLabel = String.format(":%s_label", name);
      regTitle = getLastArgument(operation.getArguments(), IsaPrimitiveKind.MODE);
      //System.out.println(regTitle);

      // TODO: only for Jalr riscv: || null != regTitle
      if (null != regTitle && !metaModel.getAddressingMode(regTitle).isLabel()) {//!jumpLabelsSet.contains(regTitle)) {
        // System.out.println(regTitle);
        // TODO:
        preCommand = prepareReg(regTitle);
      } else if (jump) {
        int temp = getArgumentNumbers(operation.getArguments(), IsaPrimitiveKind.MODE);

        if (temp > 1) {
          MetaArgument tempArg = null;
          tempArg = getArgument(operation.getArguments(), IsaPrimitiveKind.MODE, temp - 1);

          if (null != tempArg) {
            Collection<String> tempTypes = tempArg.getTypeNames();

            for (String tempType : tempTypes) {
              preCommand = prepareReg(tempType);

              break;
            }
          }
        }

      } else {
        preCommand = "";
      }
      postCommand = new String[3];
      postCommand[0] = String.format("nop");
      postCommand[1] = String.format("label " + branchLabel);
      postCommand[2] = String.format("nop");
    }

    if (load || store) {
      int temp = getArgumentNumbers(operation.getArguments(), IsaPrimitiveKind.MODE);

      MetaArgument tempArg = null;
      if (temp > 1) {
        tempArg = getArgument(operation.getArguments(), IsaPrimitiveKind.MODE, temp);
      }

      if (null != tempArg) {
        Collection<String> tempTypes = tempArg.getTypeNames();

        for (String tempType : tempTypes) {
          preCommand = prepareReg(tempType, DATA_REG, templatePrinter.getDataLabel());
          // TODO
          break;
        }
      }
    }

    command = setCommand(operation.getArguments(), name);
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
//        System.out.println("MetaArgument" + argument);

        boolean printLabel = false;
        // for (String tempType : tempTypes) {
        // if (tempTypes != null) {System.out.println("!!!!!!!!!!!!!!! " + tempTypes.size());}
        Object[] strArray = (Object[]) tempTypes.toArray();
        String tempType = (String) strArray[0];

        boolean jumpLabelsSet = false;
        //tempTypes.size()
        for (String s : tempTypes) {
//          System.out.println(metaModel.getAddressingMode(s).isLabel());
          if (metaModel.getAddressingMode(s).isLabel()) {
            jumpLabelsSet = true;
            break;
          }
        }
//        for (MetaArgument argument1 : arguments) {
          //System.out.println("MetaArgument" + argument1.getKind());
//        }

        if (jumpLabelsSet) {
          if (!printLabel) {
            if (null != branchLabel) {
              if (jump && getArgumentNumbers(arguments, IsaPrimitiveKind.MODE) > 2) {
                tempCommand += String.format("0");
                //System.out.println("jump " + jump);
              } else {
                tempCommand += branchLabel;
              }
            } else {
              tempCommand += "_";
            }
            printLabel = true;
          }
        } else {
          if ((argument.getMode() == ArgumentMode.IN && branch
              && getArgumentNumbers(arguments, IsaPrimitiveKind.IMM) == 0 && !iterator.hasNext())
              || (argument.getMode() == ArgumentMode.IN && jump)) {
            tempCommand += String.format("%s(%x)", tempType.toLowerCase(), JUMP_REG);
          } else if (tempTypes.size() > 1) {
            tempCommand += String.format("%s()", tempType.toLowerCase());
          } else if ((load || store) && argument.getMode() == ArgumentMode.IN) {
            tempCommand += String.format("%s(%s)", tempType.toLowerCase(), DATA_REG);
          } else {
            tempCommand += String.format("%s(_)", tempType.toLowerCase());
          }
        }
        // }
      }

      if (argument.getKind() == IsaPrimitiveKind.IMM) {
//        System.out.println("MetaArgument Not Mode" + argument);

        if (load || store) {
          tempCommand += this.templatePrinter.getDataLabel();
        } else if (!branch) {
          // tempCommand += String.format("rand(%s, %s)", 0, (long) Math.pow(2,
          // argument.getDataType().getBitSize()) - 1);
          tempCommand += String.format("_");
        } else {
          // System.out.println("Temp");
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

  private static MetaArgument getArgument(final Iterable<MetaArgument> arguments,
      final IsaPrimitiveKind type, final int number) {
    // TODO; del
    InvariantChecks.checkNotNull(arguments);
    InvariantChecks.checkNotNull(type);
    int argumentNumbers = 0;
    for (MetaArgument argument : arguments) {
      if (argument.getKind() == type) {
        argumentNumbers++;
      }
      if (number == argumentNumbers) {
        return argument;
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

  private static int getArgumentsNumber(final Iterable<MetaArgument> arguments) {
    if (arguments instanceof Collection<?>) {
      return ((Collection<?>) arguments).size();
    }
    return 0;
  }

  private String prepareReg(final String regType) {
    InvariantChecks.checkNotNull(regType);
    return String.format("la %s(%x), %s", regType.toLowerCase(), JUMP_REG, branchLabel);
    // TODO prepare REG(1), get_address_of(:j_label)
  }

  private String prepareReg(final String regType, final int number, final String label) {
    InvariantChecks.checkNotNull(regType);
    return String.format("la %s(%x), %s", regType.toLowerCase(), number, label);
    // return String.format("prepare %s(%x), get_address_of(%s)", regType.toLowerCase(), number,
    // label);
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
    return branch || jump;
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
   * Prints operation (with pre and post commands) to the {@code TemplatePrinter}.
   *
   * @param templatePrinter the templates printer.
   */
  public void printOperationBlock(final TemplatePrinter templatePrinter) {
    if (branch || jump || store || load) {
      String tempPreCommand = this.getPreCommand();
      if (tempPreCommand != null && !tempPreCommand.isEmpty()) {
        templatePrinter.addString(tempPreCommand);
      }
    }
    templatePrinter.addString(this.getCommand());
    if (branch || jump) {
      String[] postCommand = this.getPostCommand();

      for (int i = 0; i < postCommand.length; i++) {
        templatePrinter.addString(postCommand[i]);
      }
    }
  }

  /**
   * Prints operation to the {@code TemplatePrinter}.
   *
   * @param templatePrinter the templates printer.
   */
  public void printOperation(final TemplatePrinter templatePrinter) {
    templatePrinter.addString(this.getCommand());
  }
}
