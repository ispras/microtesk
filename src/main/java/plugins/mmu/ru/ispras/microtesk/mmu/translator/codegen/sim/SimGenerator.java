/*
 * Copyright 2015-2018 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.mmu.translator.codegen.sim;

import ru.ispras.castle.codegen.FileGenerator;
import ru.ispras.fortress.util.InvariantChecks;

import ru.ispras.microtesk.mmu.translator.codegen.spec.MemoryControlFlowExplorer;
import ru.ispras.microtesk.mmu.translator.ir.Address;
import ru.ispras.microtesk.mmu.translator.ir.Buffer;
import ru.ispras.microtesk.mmu.translator.ir.Callable;
import ru.ispras.microtesk.mmu.translator.ir.Constant;
import ru.ispras.microtesk.mmu.translator.ir.Ir;
import ru.ispras.microtesk.mmu.translator.ir.Memory;
import ru.ispras.microtesk.mmu.translator.ir.Operation;
import ru.ispras.microtesk.mmu.translator.ir.Segment;
import ru.ispras.microtesk.mmu.translator.ir.Type;
import ru.ispras.microtesk.mmu.translator.ir.Var;
import ru.ispras.microtesk.options.Options;
import ru.ispras.microtesk.translator.Translator;
import ru.ispras.microtesk.translator.TranslatorHandler;

import java.io.IOException;
import java.util.Map;

public final class SimGenerator implements TranslatorHandler<Ir> {
  private final Translator<Ir> translator;

  public SimGenerator(final Translator<Ir> translator) {
    InvariantChecks.checkNotNull(translator);
    this.translator = translator;
  }

  private String getOutDir() {
    return translator.getOutDir() + "/src/java";
  }

  @Override
  public void processIr(final Ir ir) {
    processIr(ir, new Options());
  }

  @Override
  public void processIr(final Ir ir, final Options opts) {
    InvariantChecks.checkNotNull(ir);

    final SimGeneratorFactory factory =
        new SimGeneratorFactory(getOutDir(), ir.getModelName(), opts);

    try {
      final Map<String, Memory> memories = ir.getMemories();
      InvariantChecks.checkTrue(memories.size() == 1, "Only one mmu definition is allowed.");

      final Memory memory = memories.values().iterator().next();
      final MemoryControlFlowExplorer flowExplorer = new MemoryControlFlowExplorer(memory);
      final Buffer targetBuffer = flowExplorer.getTargetBuffer();

      processExterns(ir, factory);
      processConstants(ir, factory);
      processStructs(ir, factory);
      processAddresses(ir, factory);
      processOperations(ir, factory);
      processFunctions(ir, factory);
      processBuffers(ir, targetBuffer, factory);
      processSegments(ir, factory);
      processMemories(ir, factory);
      processModel(ir, targetBuffer, factory);

    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void processExterns(
      final Ir ir,
      final SimGeneratorFactory factory) throws IOException {
    final Map<String, Var> externs = ir.getExterns();
    if (!externs.isEmpty()) {
      final FileGenerator fileGenerator = factory.newExternGenerator(externs);
      fileGenerator.generate();
    }
  }

  private void processConstants(
      final Ir ir,
      final SimGeneratorFactory factory) throws IOException {
    for (final Constant constant : ir.getConstants().values()) {
      if (!constant.isValue()) {
        final FileGenerator fileGenerator = factory.newConstantGenerator(constant);
        fileGenerator.generate();
      }
    }
  }

  private void processStructs(
      final Ir ir,
      final SimGeneratorFactory factory) throws IOException {
    for (final Type type : ir.getTypes().values()) {
      if (!ir.getAddresses().containsKey(type.getId())
          && !ir.getBuffers().containsKey(type.getId().replaceAll(".Entry", ""))) {
        final FileGenerator fileGenerator = factory.newStructGenerator(type);
        fileGenerator.generate();
      }
    }
  }

  private void processAddresses(
      final Ir ir,
      final SimGeneratorFactory factory) throws IOException {
    for (final Address address : ir.getAddresses().values()) {
      final FileGenerator fileGenerator = factory.newAddressGenerator(address);
      fileGenerator.generate();
    }
  }

  private void processOperations(
      final Ir ir,
      final SimGeneratorFactory factory) throws IOException {
    for (final Operation operation : ir.getOperations().values()) {
      final FileGenerator fileGenerator = factory.newOperationGenerator(operation);
      fileGenerator.generate();
    }
  }

  private void processFunctions(
      final Ir ir,
      final SimGeneratorFactory factory) throws IOException {
    for (final Callable func : ir.getFunctions().values()) {
      final FileGenerator fileGenerator = factory.newFunctionGenerator(ir, func);
      fileGenerator.generate();
    }
  }

  private void processBuffers(
      final Ir ir,
      final Buffer targetBuffer,
      final SimGeneratorFactory factory) throws IOException {
    for (final Buffer buffer : ir.getBuffers().values()) {
      final boolean isTargetBuffer = buffer.equals(targetBuffer);
      final FileGenerator fileGenerator = factory.newBufferGenerator(ir, buffer, isTargetBuffer);
      fileGenerator.generate();
    }
  }

  private void processSegments(
      final Ir ir,
      final SimGeneratorFactory factory) throws IOException {
    for (final Segment segment : ir.getSegments().values()) {
      final FileGenerator fileGenerator = factory.newSegmentGenerator(segment);
      fileGenerator.generate();
    }
  }

  private void processMemories(
      final Ir ir,
      final SimGeneratorFactory factory) throws IOException {
    for (final Memory memory : ir.getMemories().values()) {
      final FileGenerator fileGenerator = factory.newMemoryGenerator(memory);
      fileGenerator.generate();
    }
  }

  private void processModel(
      final Ir ir,
      final Buffer targetBuffer,
      final SimGeneratorFactory factory) throws IOException {
    final FileGenerator fileGenerator = factory.newModelGenerator(ir, targetBuffer);
    fileGenerator.generate();
  }
}
