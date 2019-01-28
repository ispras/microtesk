/*
 * Copyright 2017-2018 ISP RAS (http://www.ispras.ru)
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

import ru.ispras.microtesk.model.metadata.MetaModel;
import ru.ispras.microtesk.model.metadata.MetaOperation;
import ru.ispras.microtesk.tools.templgen.printers.TemplatePrinter;

import java.util.HashSet;
import java.util.Set;

public final class GroupTemplate extends GeneratedTemplate {
  public static final String GROUP_TEMPLATE_NAME = "Group";

  public GroupTemplate(final MetaModel metaModel, final TemplatePrinter printer,
      final Set<String> ignoredInstructions) {
    super(metaModel, printer, ignoredInstructions);
  }

  private void printSequence(final Set<TemplateOperation> operationsGroup) {
    templatePrinter.startSequence("sequence {");

    for (TemplateOperation operation : operationsGroup) {
      templatePrinter.addString("");
      operation.printOperationBlock(templatePrinter);
    }

    templatePrinter.closeSequence("}.run");
  }

  private void printIterate(final Set<TemplateOperation> operationsGroup) {
    templatePrinter.startSequence("iterate {");

    for (TemplateOperation operation : operationsGroup) {
      templatePrinter.addString("");
      // operation.printOperationBlock(templatePrinter);
      if (null != operation.getPreCommand() && !operation.getPreCommand().isEmpty()) {
        templatePrinter.startSequence("sequence {");
        operation.printOperationBlock(templatePrinter);
        templatePrinter.closeSequence("}");
      } else {
        operation.printOperation(templatePrinter);
      }
    }

    templatePrinter.closeSequence("}");
  }

  @Override
  public boolean generate() {
    templatePrinter.templateBegin();
    templatePrinter.addString("set_default_allocator FREE");
    templatePrinter.addString("");

    Set<TemplateOperation> branchSet = new HashSet<TemplateOperation>();
    Set<TemplateOperation> storeSet = new HashSet<TemplateOperation>();
    Set<TemplateOperation> loadSet = new HashSet<TemplateOperation>();
    Set<TemplateOperation> arithmeticSet = new HashSet<TemplateOperation>();

    final Iterable<MetaOperation> operationsIterator = templateMetaModel.getOperations();
    for (MetaOperation operation : operationsIterator) {
      if (operation.hasRootShortcuts() && !ignoredInstructions.contains(operation.getName())) {
        TemplateOperation templateOperation = new TemplateOperation(operation, templatePrinter);
        if (templateOperation.isBranchOperation()) {
          branchSet.add(templateOperation);
        }
        if (templateOperation.isStoreOperation()) {
          storeSet.add(templateOperation);
        }
        if (templateOperation.isLoadOperation()) {
          loadSet.add(templateOperation);
        }
        if (templateOperation.isArithmeticOperation()) {
          arithmeticSet.add(templateOperation);
        }
      }
    }

    templatePrinter.addComment(" BRANCH_OPERATIONS");
    printSequence(branchSet);

    templatePrinter.addString("");
    templatePrinter.startBlock();
    templatePrinter.addComment(" STORE_OPERATIONS");
    printIterate(storeSet);
    templatePrinter.addComment(" LOAD_OPERATIONS");
    printIterate(loadSet);
    templatePrinter.addComment(" ARITHMETIC_OPERATIONS");
    printIterate(arithmeticSet);
    templatePrinter.closeBlock();

    templatePrinter.templateEnd();
    templatePrinter.templateClose();

    return true;
  }
}
