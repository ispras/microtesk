/*
 * Copyright 2018 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.translator.nml.codegen.whyml;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.codegen.FileGenerator;
import ru.ispras.microtesk.codegen.FileGeneratorStringTemplate;
import ru.ispras.microtesk.codegen.StringTemplateBuilder;
import ru.ispras.microtesk.translator.Translator;
import ru.ispras.microtesk.translator.TranslatorHandler;
import ru.ispras.microtesk.translator.generation.PackageInfo;
import ru.ispras.microtesk.translator.nml.ir.Ir;
import ru.ispras.microtesk.translator.nml.ir.primitive.Primitive;
import ru.ispras.microtesk.translator.nml.ir.primitive.Primitive.Modifier;

import java.io.IOException;

public final class WhymlGenerator implements TranslatorHandler<Ir> {
  private final Translator<Ir> translator;
  private Ir ir;

  public WhymlGenerator(final Translator<Ir> translator) {
    InvariantChecks.checkNotNull(translator);

    this.translator = translator;
    this.ir = null;
  }

  @Override
  public void processIr(final Ir ir) {
    InvariantChecks.checkNotNull(ir);
    this.ir = ir;

    generateState();
    generateAddressingModes();
    generateOperations();
  }

  private void generateState() {
    InvariantChecks.checkNotNull(ir);
    generateFile(StbState.FILE_NAME, new StbState(ir));
  }

  private void generateAddressingModes() {
    for (final Primitive primitive : ir.getModes().values()) {
      if (!primitive.isOrRule() &&
          primitive.getModifier() != Modifier.PSEUDO &&
          primitive.getModifier() != Modifier.LABEL) {
        generateFile(primitive.getName(), new StbPrimitive(primitive));
      }
    }
  }

  private void generateOperations() {
    for (final Primitive primitive : ir.getOps().values()) {
      if (!primitive.isOrRule() &&
          primitive.getModifier() != Modifier.PSEUDO &&
          primitive.getModifier() != Modifier.LABEL) {
        generateFile(primitive.getName(), new StbPrimitive(primitive));
      }
    }
  }

  private void generateFile(
      final String className,
      final StringTemplateBuilder templateBuilder) {
    final String[] templateGroups =
        new String[] { PackageInfo.COMMON_TEMPLATE_DIR + "Whyml.stg" };

    final FileGenerator generator =
        new FileGeneratorStringTemplate(getFileName(className), templateGroups, templateBuilder);

    try {
      generator.generate();
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  private String getFileName(final String name) {
    InvariantChecks.checkNotNull(ir);
    return String.format("%s/%s/%s.mlw", getOutDir(), getModelName(), name.toLowerCase());
  }

  private String getOutDir() {
    return translator.getOutDir() + "/src/why3";
  }

  private String getModelName() {
    InvariantChecks.checkNotNull(ir);
    return ir.getModelName();
  }
}
