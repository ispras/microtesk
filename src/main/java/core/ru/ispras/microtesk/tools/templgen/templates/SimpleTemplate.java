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

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.model.metadata.MetaModel;
import ru.ispras.microtesk.model.metadata.MetaOperation;
import ru.ispras.microtesk.tools.templgen.printers.TemplatePrinter;

/**
 * The {@code SimpleTemplate} class generates simple template of meta model.
 * 
 * @author <a href="mailto:protsenko@ispras.ru">Alexander Protsenko</a>
 */

public class SimpleTemplate extends GeneratedTemplate{
  private final MetaModel templateMetaModel;
  private final TemplatePrinter templatePrinter;

  public SimpleTemplate(final MetaModel metaModel, final TemplatePrinter printer) {
    InvariantChecks.checkNotNull(metaModel);

    this.templateMetaModel = metaModel;

    this.templatePrinter = printer;
  }

  public boolean generate()
  {
    //RubyTemplatePrinter templatePrinter = new RubyTemplatePrinter();
    templatePrinter.templateBegin();
    
    Iterable<MetaOperation> operationsIterator = templateMetaModel.getOperations();
    
    for (MetaOperation operation : operationsIterator) {
      //System.out.format("Operation: %s \n", operation.getName());
      if (operation.hasRootShortcuts()) printMetaOperation(templatePrinter, operation);
    }

    templatePrinter.templateEnd();
    templatePrinter.templateClose();

    //System.out.println(templateMetaModel.getOperationGroups());

    return true;
  }
}
