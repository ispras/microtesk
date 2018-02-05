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

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.microtesk.model.metadata.MetaModel;
import ru.ispras.microtesk.model.metadata.MetaOperation;
import ru.ispras.microtesk.tools.templgen.printers.TemplatePrinter;
import ru.ispras.testbase.knowledge.iterator.CollectionIterator;

/**
 * @author <a href="mailto:protsenko@ispras.ru">Alexander Protsenko</a>
 */

public final class BoundaryValuesTemplate extends GeneratedTemplate {
  public static final String BOUNDARY_TEMPLATE_NAME = "boundary";

  public BoundaryValuesTemplate(MetaModel metaModel, TemplatePrinter printer) {
    super(metaModel, printer);
  }

  private void printSequence(final TemplateOperation templateOperation) {
    templatePrinter.startSequence("iterate {");
    templatePrinter.addString(templateOperation.getCommand());
    /*CollectionIterator<BitVector> iterator =
        ru.ispras.testbase.knowledge.iterator.BoundaryValueIterator.getBoundaryIterator(32);
    String iteratorValues = " ";
    boolean tabs = false;
    for (iterator.init(); iterator.hasValue(); iterator.next()) {
      BitVector value = iterator.value();
      if (tabs) {
        iteratorValues += ", ";
      }
      iteratorValues += value + "(" + Long.toHexString(value.longValue()) + ")";
      tabs = true;
    }*/
    //templatePrinter.addComment(iteratorValues);
    templatePrinter.closeSequence("}");
  }

  @Override
  public boolean generate() {
    templatePrinter.templateBegin();
    templatePrinter.startBlock();
    templatePrinter.addComment(" ARITHMETIC_OPERATIONS");

    final Iterable<MetaOperation> operationsIterator = templateMetaModel.getOperations();
    for (MetaOperation operation : operationsIterator) {
      if (operation.hasRootShortcuts()) {
        TemplateOperation templateOperation = new TemplateOperation(operation, templatePrinter);
        if (templateOperation.isArithmeticOperation()) {
          printSequence(templateOperation);
        }
      }
    }

    templatePrinter.closeBlock();
    templatePrinter.templateEnd();
    templatePrinter.templateClose();
    return true;
  }
}
