/*
 * Copyright 2018-2020 ISP RAS (http://www.ispras.ru)
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

import java.util.Set;

/**
 * The {@code ArchitectureTemplate} class generates template for architecture validation.
 *
 * @author <a href="mailto:protsenko@ispras.ru">Alexander Protsenko</a>
 */

public class ArchitectureTemplate extends GeneratedTemplate {
  public static final String ARCHITECTURE_TEMPLATE_NAME = "Architecture";

  public ArchitectureTemplate(final MetaModel metaModel, final TemplatePrinter printer,
      final Set<String> ignoredInstructions) {
    super(metaModel, printer, ignoredInstructions);
  }

  @Override
  public boolean generate() {
    templatePrinter.templateBegin();

    final Iterable<MetaOperation> operationsIterator = templateMetaModel.getOperations();

    for (MetaOperation operation : operationsIterator) {
      if (operation.hasRootShortcuts()) {
        /*
         * TemplateOperation templateOperation = new TemplateOperation(operation, templatePrinter);
         * templatePrinter.addString(""); templateOperation.printOperationBlock(templatePrinter);
         */
      }
    }

    templatePrinter.templateEnd();
    templatePrinter.templateClose();

    return true;
  }

  @Override
  protected boolean extract() {
    // TODO Auto-generated method stub
    return false;
  }
}
