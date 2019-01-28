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

import java.util.Set;

import ru.ispras.microtesk.model.metadata.MetaModel;
import ru.ispras.microtesk.model.metadata.MetaOperation;
import ru.ispras.microtesk.tools.templgen.printers.TemplatePrinter;

/**
 * @author <a href="mailto:protsenko@ispras.ru">Alexander Protsenko</a>
 */

public final class BoundaryValuesTemplate extends GeneratedTemplate {
  public static final String BOUNDARY_TEMPLATE_NAME = "Boundary";

  public BoundaryValuesTemplate(MetaModel metaModel, TemplatePrinter printer,
      final Set<String> ignoredInstructions) {
    super(metaModel, printer, ignoredInstructions);
  }

  private void printSequence(final TemplateOperation templateOperation) {
    templatePrinter.addString("");
    templatePrinter.startSequence("sequence {");
    templatePrinter.addAlignedText(templateOperation.getCommand());
    templatePrinter.addText(" do testdata('boundary') end");
    templatePrinter.addString("");
    templatePrinter.closeSequence("}.run");
  }

  @Override
  public boolean generate() {
    templatePrinter.templateBegin();
    templatePrinter.addString("set_default_allocator FREE");
    templatePrinter.addString("");
    templatePrinter.addComment("Only arithmetic operations");

    final Iterable<MetaOperation> operationsIterator = templateMetaModel.getOperations();
    for (MetaOperation operation : operationsIterator) {
      if (operation.hasRootShortcuts() && !ignoredInstructions.contains(operation.getName())) {
        TemplateOperation templateOperation = new TemplateOperation(operation, templatePrinter);
        if (templateOperation.isArithmeticOperation()) {
          printSequence(templateOperation);
        }
      }
    }

    templatePrinter.templateEnd();
    templatePrinter.templateClose();
    return true;
  }
}
