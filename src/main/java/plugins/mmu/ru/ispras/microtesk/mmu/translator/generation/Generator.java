/*
 * Copyright 2015 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.mmu.translator.generation;

import java.io.IOException;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.mmu.translator.ir.Address;
import ru.ispras.microtesk.mmu.translator.ir.Buffer;
import ru.ispras.microtesk.mmu.translator.ir.Ir;
import ru.ispras.microtesk.mmu.translator.ir.Memory;
import ru.ispras.microtesk.mmu.translator.ir.Segment;
import ru.ispras.microtesk.mmu.translator.ir.Type;
import ru.ispras.microtesk.translator.Translator;
import ru.ispras.microtesk.translator.TranslatorHandler;
import ru.ispras.microtesk.translator.generation.FileGenerator;

public final class Generator implements TranslatorHandler<Ir> {
  private final Translator<Ir> translator;

  public Generator(final Translator<Ir> translator) {
    InvariantChecks.checkNotNull(translator);
    this.translator = translator;
  }

  private String getOutDir() {
    return translator.getOutDir() + "/src/java";
  }

  @Override
  public void processIr(final Ir ir) {
    InvariantChecks.checkNotNull(ir);

    final GeneratorFactory factory =
        new GeneratorFactory(getOutDir(), ir.getModelName());

    try {
      processStructs(ir, factory);
      processAddresses(ir, factory);
      /*processBuffers(ir, factory);
      processSegments(ir, factory);
      processMemories(ir, factory);
      processModel(ir, factory);*/
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void processStructs(final Ir ir, final GeneratorFactory factory) throws IOException {
    for (final Type type : ir.getTypes().values()) {
      if (!ir.getAddresses().containsKey(type.getId())) {
        final FileGenerator fileGenerator = factory.newStructGenerator(type);
        fileGenerator.generate();
      }
    }
  }

  private void processAddresses(final Ir ir, final GeneratorFactory factory) throws IOException {
    for (final Address address : ir.getAddresses().values()) {
      final FileGenerator fileGenerator = factory.newAddressGenerator(address);
      fileGenerator.generate();
    }
  }

  private void processBuffers(final Ir ir, final GeneratorFactory factory) throws IOException {
    for (final Buffer buffer : ir.getBuffers().values()) {
      final FileGenerator fileGenerator = factory.newBufferGenerator(buffer);
      fileGenerator.generate();
    }
  }

  private void processSegments(final Ir ir, final GeneratorFactory factory) throws IOException {
    for (final Segment segment : ir.getSegments().values()) {
      final FileGenerator fileGenerator = factory.newSegmentGenerator(segment);
      fileGenerator.generate();
    }
  }

  private void processMemories(final Ir ir, final GeneratorFactory factory) throws IOException {
    for (final Memory memory : ir.getMemories().values()) {
      final FileGenerator fileGenerator = factory.newMemoryGenerator(ir, memory);
      fileGenerator.generate();
    }
  }

  private void processModel(final Ir ir, final GeneratorFactory factory) throws IOException {
    final FileGenerator fileGenerator = factory.newModelGenerator(ir);
    fileGenerator.generate();
  }
}
