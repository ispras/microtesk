/*
 * Copyright 2012-2014 ISP RAS (http://www.ispras.ru)
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

import java.io.IOException;

import ru.ispras.microtesk.translator.generation.IClassGenerator;
import ru.ispras.microtesk.translator.nml.ir.IR;
import ru.ispras.microtesk.translator.nml.ir.primitive.Primitive;
import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveAND;
import ru.ispras.microtesk.translator.nml.ir.primitive.PrimitiveOR;

public final class Generator {
  private final GeneratorFactory factory;
  private final IR ir;

  public Generator(String outDir, String modelName, String specFileName, IR ir) {
    this.factory = new GeneratorFactory(outDir, modelName, specFileName);
    this.ir = ir;
  }

  public void generate() {
    try {
      generateShared();
      generateModes();
      generateOps();
      generateModel();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void generateModel() throws IOException {
    final IClassGenerator model = factory.createModelGenerator(ir);
    model.generate();
  }

  private void generateShared() throws IOException {
    final IClassGenerator shared = factory.createSharedGenerator(ir);
    shared.generate();
  }

  private void generateModes() throws IOException {
    for (Primitive m : ir.getModes().values()) {
      final IClassGenerator mode = m.isOrRule() ? 
        factory.createAddressingModeOr((PrimitiveOR) m) :
        factory.createAddressingMode((PrimitiveAND) m);

      mode.generate();
    }
  }

  private void generateOps() throws IOException {
    for (Primitive o : ir.getOps().values()) {
      final IClassGenerator op = o.isOrRule() ? 
        factory.createOperationOr((PrimitiveOR) o) :
        factory.createOperation((PrimitiveAND) o);

      op.generate();
    }
  }
}
