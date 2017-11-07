/*
 * Copyright 2017 ISP RAS (http://www.ispras.ru)
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

/**
 * @author <a href="mailto:protsenko@ispras.ru">Alexander Protsenko</a>
 */

import java.util.ArrayList;
import java.util.Iterator;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.model.metadata.MetaModel;
import ru.ispras.microtesk.model.metadata.MetaOperation;
import ru.ispras.microtesk.tools.templgen.printers.TemplatePrinter;

public class GroupTemplate extends GeneratedTemplate {
  public static final String GROUP_TEMPLATE_NAME = "group";

  private static final int BRANCH_OPERATIONS = 1;
  private static final int STORE_OPERATIONS = 2;
  private static final int LOAD_OPERATIONS = 3;
  private static final int ARITHMETIC_OPERATIONS = 4;

  private final MetaModel templateMetaModel;
  private final TemplatePrinter templatePrinter;

  public GroupTemplate(final MetaModel metaModel, final TemplatePrinter printer) {
    InvariantChecks.checkNotNull(metaModel);

    this.templateMetaModel = metaModel;

    this.templatePrinter = printer;
  }

  protected static ArrayList<MetaOperation> getGroups(Iterable<MetaOperation> operations,
      int groupType) {

    ArrayList<MetaOperation> groupOperations = new ArrayList<MetaOperation>();

    for (MetaOperation operation : operations) {

      switch (groupType) {
        case BRANCH_OPERATIONS:
          if (TemplateUtils.isBranchOperation(operation))
            groupOperations.add(operation);
          break;
        case STORE_OPERATIONS:
          if (operation.isStore())
            groupOperations.add(operation);
          break;
        case LOAD_OPERATIONS:
          if (operation.isLoad())
            groupOperations.add(operation);
          break;
        case ARITHMETIC_OPERATIONS:
          int argumentsNumber = TemplateUtils.getArgumentsNumber(operation.getArguments());
          if (// getArgumentNumbers(operation.getArguments(), IsaPrimitiveKind.MODE) ==
              // argumentsNumber &&
          !TemplateUtils.isBranchOperation(operation) && argumentsNumber == 3 && !operation.isLoad()
              && !operation.isStore())
            groupOperations.add(operation);
          break;
        default:
          //
          break;
      }
    }

    return groupOperations;
  }

  private void printSequence(int operationsGroup) {
    Iterable<MetaOperation> operationsIterator = templateMetaModel.getOperations();

    ArrayList<MetaOperation> operationGroups = getGroups(operationsIterator, operationsGroup);

    Iterator<MetaOperation> iteratorGroup = operationGroups.iterator();

    templatePrinter.startSequence("sequence {");

    while (iteratorGroup.hasNext()) {
      MetaOperation operation = iteratorGroup.next();
      if (operation.hasRootShortcuts())
        printMetaOperation(templatePrinter, operation);
    }

    templatePrinter.closeSequence("}.run");
  }

  private void printIterate(int operationsGroup) {
    Iterable<MetaOperation> operationsIterator = templateMetaModel.getOperations();

    ArrayList<MetaOperation> operationGroups = getGroups(operationsIterator, operationsGroup);

    Iterator<MetaOperation> iteratorGroup = operationGroups.iterator();

    templatePrinter.startSequence("iterate {");

    while (iteratorGroup.hasNext()) {
      MetaOperation operation = iteratorGroup.next();
      if (operation.hasRootShortcuts())
        printMetaOperation(templatePrinter, operation);
    }

    templatePrinter.closeSequence("}");
  }

  @Override
  public boolean generate() {
    templatePrinter.templateBegin();

    templatePrinter.addComment(" BRANCH_OPERATIONS");
    printSequence(BRANCH_OPERATIONS);

    templatePrinter.startBlock();

    templatePrinter.addComment(" STORE_OPERATIONS");
    printIterate(STORE_OPERATIONS);

    templatePrinter.addComment(" LOAD_OPERATIONS");
    printIterate(LOAD_OPERATIONS);

    templatePrinter.addComment(" ARITHMETIC_OPERATIONS");
    printIterate(ARITHMETIC_OPERATIONS);

    templatePrinter.closeBlock();

    templatePrinter.templateEnd();
    templatePrinter.templateClose();

    return true;
  }

}
