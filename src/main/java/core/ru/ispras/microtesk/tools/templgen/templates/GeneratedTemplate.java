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

import java.util.Collections;
import java.util.Set;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.model.metadata.MetaModel;
import ru.ispras.microtesk.tools.templgen.printers.TemplatePrinter;

public abstract class GeneratedTemplate implements BaseTemplate {
  protected final MetaModel templateMetaModel;
  protected final TemplatePrinter templatePrinter;
  protected Set<String> ignoredInstructions;

  /**
   * Constructs a template generator.
   *
   * @param metaModel model of the microprocessor.
   * @param printer printer for the template.
   * @param ignoredInstructions instructions to ignore.
   */
  public GeneratedTemplate(final MetaModel metaModel, final TemplatePrinter printer,
      final Set<String> ignoredInstructions) {
    InvariantChecks.checkNotNull(metaModel);
    InvariantChecks.checkNotNull(printer);
    InvariantChecks.checkNotNull(ignoredInstructions);

    this.templateMetaModel = metaModel;
    this.templatePrinter = printer;
    this.ignoredInstructions = ignoredInstructions;
  }

  /**
   * Constructs a template generator. There are no instructions to ignore.
   *
   * @param metaModel model of the microprocessor.
   * @param printer printer for the template.
   */
  public GeneratedTemplate(final MetaModel metaModel, final TemplatePrinter printer) {
    InvariantChecks.checkNotNull(metaModel);
    InvariantChecks.checkNotNull(printer);

    this.templateMetaModel = metaModel;
    this.templatePrinter = printer;
    this.ignoredInstructions = Collections.emptySet();
  }

  /**
   * Generates the template and output it in the format of the specified printer.
   */
  public abstract boolean generate();


  /**
   * Extracts the information from model for this template.
   */
  protected abstract boolean extract();

  /**
   * Sorts instructions by groups.
   */
  public void sort() {}
}
