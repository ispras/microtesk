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

package ru.ispras.microtesk.tools.templgen;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.SysUtils;
import ru.ispras.microtesk.model.Model;
import ru.ispras.microtesk.model.metadata.MetaModel;
import ru.ispras.microtesk.options.Options;
import ru.ispras.microtesk.tools.templgen.printers.RubyTemplatePrinter;
import ru.ispras.microtesk.tools.templgen.templates.GroupTemplate;
import ru.ispras.microtesk.tools.templgen.templates.SimpleTemplate;

public final class TemplateGenerator {
  public static boolean generate(final Options options, final String modelName) {
    InvariantChecks.checkNotNull(options);
    InvariantChecks.checkNotNull(modelName);

    final Model model = loadModel(modelName);
    final MetaModel metaModel = model.getMetaData();

    boolean generatedResult = true;

    SimpleTemplate simpleTemplate =
        new SimpleTemplate(metaModel, new RubyTemplatePrinter(SimpleTemplate.SIMPLE_TEMPLATE_NAME));
    generatedResult = generatedResult & simpleTemplate.generate();

    GroupTemplate groupTemplate =
        new GroupTemplate(metaModel, new RubyTemplatePrinter(GroupTemplate.GROUP_TEMPLATE_NAME));
    generatedResult = generatedResult & groupTemplate.generate();

    return generatedResult;
  }

  private static Model loadModel(final String modelName) {
    try {
      return SysUtils.loadModel(modelName);
    } catch (final Exception e) {
      Logger.error("Failed to load the %s model. Reason: %s.", modelName, e.getMessage());
      return null;
    }
  }
}
