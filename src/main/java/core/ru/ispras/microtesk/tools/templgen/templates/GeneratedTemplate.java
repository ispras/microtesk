package ru.ispras.microtesk.tools.templgen.templates;

import java.util.Collection;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.model.ArgumentMode;
import ru.ispras.microtesk.model.IsaPrimitiveKind;
import ru.ispras.microtesk.model.metadata.MetaArgument;
import ru.ispras.microtesk.model.metadata.MetaOperation;
import ru.ispras.microtesk.tools.templgen.printers.TemplatePrinter;

public abstract class GeneratedTemplate implements BaseTemplate {
  private static final int JUMP_REG = 3;
  private static final int NOT_JUMP = -1;

  public abstract boolean generate();

  protected static int getArgumentNumbers(Iterable<MetaArgument> arguments, IsaPrimitiveKind type) {
    InvariantChecks.checkNotNull(arguments);
    InvariantChecks.checkNotNull(type);
    int argumentNumbers = 0;
    for (MetaArgument argument : arguments) {
      if (argument.getKind() == type)
      {
        argumentNumbers ++;
      }
    }
    return argumentNumbers;
  }

  private static String getLastArgument(Iterable<MetaArgument> arguments, IsaPrimitiveKind type) {
    for (MetaArgument argument : arguments) {
      Collection<String> tempTypes = argument.getTypeNames();

      if (argument.getKind() == type)
        for (String tempType : tempTypes) {
          return tempType;
        }
    }    
    return null;
  }

  private static String createPreparatorFor(String regType, int regNumber, String jumpLabel) {
    return String.format("la %s(%x), %s", regType, regNumber, jumpLabel);
    // TODO prepare REG(1), get_address_of(:j_label)
  }

  protected static void printBranchOperation(TemplatePrinter templatePrinter,
      MetaOperation operation) {
    String branchLabel = String.format(":%s_label", operation.getName());

    // Get arguments list
    Iterable<MetaArgument> arguments = operation.getArguments();

    // Conditions for use preparator
    if (getArgumentNumbers(arguments, IsaPrimitiveKind.IMM) == 0 & getArgumentNumbers(arguments, IsaPrimitiveKind.MODE) > 0) {
      String regTitle = getLastArgument(arguments, IsaPrimitiveKind.MODE);
      
      templatePrinter.addString(createPreparatorFor(regTitle, JUMP_REG, branchLabel));
      templatePrinter.addString("nop");
    }

    // Print operation name
    templatePrinter.addOperation(operation.getName());

//    System.out.format("(size: %s)", TemplateGeneratorUtils.getArgumentsNumber(arguments));

    printOperationArguments(templatePrinter, arguments, branchLabel, JUMP_REG);
    
    templatePrinter.addString("");

    templatePrinter.addString("nop");
    templatePrinter.addString("label " + branchLabel + "");
    templatePrinter.addString("nop");

  }

  protected static void printOperation(TemplatePrinter templatePrinter,
      MetaOperation operation) {

    // Get arguments list
    Iterable<MetaArgument> arguments = operation.getArguments();

    // Print operation name
    templatePrinter.addOperation(operation.getName());

    printOperationArguments(templatePrinter, arguments);

    templatePrinter.addString("");
  }

  private static void printOperationArguments(TemplatePrinter templatePrinter,
      Iterable<MetaArgument> arguments) {
    printOperationArguments(templatePrinter, arguments, null, NOT_JUMP);
  }

  private static void printOperationArguments(TemplatePrinter templatePrinter,
      Iterable<MetaArgument> arguments, String label, int jumpReg) {
    boolean commaIndicator = false;
    for (MetaArgument argument : arguments) {
      Collection<String> tempTypes = argument.getTypeNames();
      if (commaIndicator)
        templatePrinter.addText(", ");

      if (argument.getKind() == IsaPrimitiveKind.MODE)
        for (String tempType : tempTypes) {
          //if (jumpReg == NOT_JUMP || getArgumentNumbers(arguments, IsaPrimitiveKind.IMM) > 0)
         //   templatePrinter.addText(String.format("%s(_)", tempType.toLowerCase()));
          if (argument.getMode() == ArgumentMode.IN && jumpReg != NOT_JUMP && getArgumentNumbers(arguments, IsaPrimitiveKind.IMM) == 0)
            templatePrinter.addText(String.format("%s(%x)", tempType.toLowerCase(), jumpReg));
          else
            templatePrinter.addText(String.format("%s(_)", tempType.toLowerCase()));
        }

      if (argument.getKind() == IsaPrimitiveKind.IMM) {
        // TODO:
        if (label == null)
        templatePrinter.addText(String.format("rand(%s, %s)", 0,
            (long) Math.pow(2, argument.getDataType().getBitSize() / 2) - 1));
        else
          templatePrinter.addText(label);
      }
      commaIndicator = true;

     // System.out.format("%s \n", argument.toString());
    }
  }

  protected static void printMetaOperation(final TemplatePrinter templatePrinter, final MetaOperation operation) {
   // System.out.format("isBranch = %s, isConditionalBranch = %s, isLoad = %s, isStore = %s\n",
    //    operation.isBranch(), operation.isConditionalBranch(), operation.isLoad(), operation.isStore());

    if (TemplateUtils.isBranchOperation(operation)) {
      printBranchOperation(templatePrinter, operation);
    } else {
      printOperation(templatePrinter, operation);
    }

    System.out.println();
  }
}
