/*
 * Copyright 2012-2018 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.translator.nml.generation;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.codegen.FileGenerator;
import ru.ispras.microtesk.translator.Translator;
import ru.ispras.microtesk.translator.TranslatorHandler;
import ru.ispras.microtesk.translator.nml.ir.Ir;
import ru.ispras.microtesk.translator.nml.ir.primitive.Primitive;
import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveAND;
import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveOR;

import java.io.IOException;

public final class Generator implements TranslatorHandler<Ir> {
  private final Translator<Ir> translator;
  private Ir ir;
  private GeneratorFactory factory;

  public Generator(final Translator<Ir> translator) {
    InvariantChecks.checkNotNull(translator);
    this.translator = translator;
  }

  @Override
  public void processIr(final Ir ir) {
    InvariantChecks.checkNotNull(ir);
    this.ir = ir;

    this.factory = new GeneratorFactory(
        translator.getOutDir() + "/src/java", ir.getModelName());

    generate();
  }

  private void generate() {
    try {
      generateTypes();
      generateProcessingElement();
      generateTemporaryVariables();
      generateModes();
      generateOps();
      generateModel();
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void generateModel() throws IOException {
    final FileGenerator model = factory.createModelGenerator(ir);
    model.generate();
  }

  private void generateTypes() throws IOException {
    final FileGenerator types = factory.createTypesGenerator(ir);
    types.generate();
  }

  private void generateProcessingElement() throws IOException {
    final FileGenerator core = factory.createPEGenerator(ir);
    core.generate();
  }

  private void generateTemporaryVariables() throws IOException {
    final FileGenerator core = factory.createTempVarGenerator(ir);
    core.generate();
  }

  private void generateModes() throws IOException {
    for (final Primitive m : ir.getModes().values()) {
      final FileGenerator mode = m.isOrRule()
          ? factory.createAddressingModeOr((PrimitiveOR) m)
          : factory.createAddressingMode((PrimitiveAND) m);

      mode.generate();
    }
  }

  private void generateOps() throws IOException {
    for (final Primitive o : ir.getOps().values()) {
      final FileGenerator op = o.isOrRule()
          ? factory.createOperationOr((PrimitiveOR) o)
          : factory.createOperation((PrimitiveAND) o);

      op.generate();
    }
  }
}
