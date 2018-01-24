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
import ru.ispras.microtesk.tools.templgen.printers.TemplatePrinter;

public abstract class GeneratedTemplate implements BaseTemplate {
  protected final MetaModel templateMetaModel;
  protected final TemplatePrinter templatePrinter;

  /**
   * Constructs a template generator.
   *
   * @param metaModel model of the microprocessor.
   * @param printer printer for the template.
   */
  public GeneratedTemplate(final MetaModel metaModel, final TemplatePrinter printer) {
    InvariantChecks.checkNotNull(metaModel);
    InvariantChecks.checkNotNull(printer);

    this.templateMetaModel = metaModel;

    this.templatePrinter = printer;
  }

  /**
   * Generates the template and output it in the format of the specified printer.
   */
  public abstract boolean generate();
}
