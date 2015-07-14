/*
 * Copyright 2006-2015 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.mmu.test.sequence.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.basis.SolverResult;
import ru.ispras.microtesk.mmu.basis.DataType;
import ru.ispras.microtesk.mmu.basis.MemoryOperation;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryAccess;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryAccessStructure;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryAccessStructureIterator;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryAccessType;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.classifier.ClassifierTrivial;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuBuffer;
import ru.ispras.microtesk.test.sequence.engine.Engine;
import ru.ispras.microtesk.test.sequence.engine.EngineContext;
import ru.ispras.microtesk.test.sequence.engine.EngineResult;
import ru.ispras.microtesk.test.template.Call;
import ru.ispras.microtesk.utils.function.BiConsumer;
import ru.ispras.microtesk.utils.function.Function;
import ru.ispras.microtesk.utils.function.Supplier;
import ru.ispras.microtesk.utils.function.TriConsumer;
import ru.ispras.microtesk.utils.function.UnaryOperator;
import ru.ispras.testbase.knowledge.iterator.Iterator;

/**
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class MemoryEngine implements Engine<MemorySolution> {
  private final Iterator<MemoryAccessStructure> iterator;
  private final Function<MemoryAccess, AddressObject> testDataConstructor;
  private final BiConsumer<MemoryAccess, AddressObject> testDataCorrector;
  private final Map<MmuBuffer, UnaryOperator<Long>> tagAllocators;
  private final Map<MmuBuffer, UnaryOperator<Long>> entryIdAllocators;
  private final Map<MmuBuffer, Supplier<Object>> entryConstructors;
  private final Map<MmuBuffer, TriConsumer<MemoryAccess, AddressObject, Object>> entryProviders;

  // TODO: To provide access to the current solution.
  private MemorySolver solver;

  public MemoryEngine(
      final Iterator<MemoryAccessStructure> iterator,
      final Function<MemoryAccess, AddressObject> testDataConstructor,
      final BiConsumer<MemoryAccess, AddressObject> testDataCorrector,
      final Map<MmuBuffer, UnaryOperator<Long>> tagAllocators,
      final Map<MmuBuffer, UnaryOperator<Long>> entryIdAllocators,
      final Map<MmuBuffer, Supplier<Object>> entryConstructors,
      final Map<MmuBuffer, TriConsumer<MemoryAccess, AddressObject, Object>> entryProviders) {
    this.iterator = iterator;
    this.testDataConstructor = testDataConstructor;
    this.testDataCorrector = testDataCorrector;
    this.tagAllocators = tagAllocators;
    this.entryIdAllocators = entryIdAllocators;
    this.entryConstructors = entryConstructors;
    this.entryProviders = entryProviders;
  }

  // TODO:
  public MemoryEngine() {
    this(null, null, null, null, null, null, null);
  }

  public MemorySolution getCurrentSolution() {
    return solver != null ? solver.getCurrentSolution() : null;
  }

  @Override
  public Class<MemorySolution> getSolutionClass() {
    return MemorySolution.class;
  }

  @Override
  public void configure(final Map<String, Object> attributes) {
    // Do nothing.
  }

  @Override
  public EngineResult<MemorySolution> solve(
      final EngineContext engineContext, final List<Call> abstractSequence) {
    final Iterator<MemoryAccessStructure> structureIterator =
        getStructureIterator(abstractSequence);
    final Iterator<MemorySolution> solutionIterator =
        getSolutionIterator(structureIterator);

    return new EngineResult<MemorySolution>(solutionIterator);
  }

  private Iterator<MemoryAccessStructure> getStructureIterator(
      final List<Call> abstractSequence) {
    // TODO: Compatibility with MMU TestGen.
    if (iterator != null) {
      return iterator;
    }

    final List<MemoryAccessType> accessTypes = new ArrayList<>();

    for (final Call abstractCall : abstractSequence) {
      InvariantChecks.checkTrue(abstractCall.isLoad() || abstractCall.isStore());

      final MemoryOperation operation =
          abstractCall.isLoad() ? MemoryOperation.LOAD : MemoryOperation.STORE;

      final int blockSizeInBits = abstractCall.getBlockSize();
      InvariantChecks.checkTrue((blockSizeInBits & 7) == 0);

      accessTypes.add(new MemoryAccessType(operation, DataType.type(blockSizeInBits / 8)));
    }

    return new MemoryAccessStructureIterator(accessTypes, new ClassifierTrivial());
  }

  private Iterator<MemorySolution> getSolutionIterator(
      final Iterator<MemoryAccessStructure> structureIterator) {
    InvariantChecks.checkNotNull(structureIterator);

    return new Iterator<MemorySolution>() {
      private MemorySolution solution = null;

      private MemorySolution getSolution() {
        while (structureIterator.hasValue()) {
          final MemoryAccessStructure structure = structureIterator.value();

          solver = new MemorySolver(
              structure,
              testDataConstructor,
              testDataCorrector,
              tagAllocators,
              entryIdAllocators,
              entryConstructors,
              entryProviders);

          SolverResult<MemorySolution> result = solver.solve();

          if (result != null && result.getStatus() == SolverResult.Status.SAT) {
            solution = result.getResult();
            break;
          }

          structureIterator.next();
        }

        return structureIterator.hasValue() ? solution : null;
      }

      @Override
      public void init() {
        structureIterator.init();
        solution = getSolution();
      }

      @Override
      public boolean hasValue() {
        return solution != null;
      }

      @Override
      public MemorySolution value() {
        return solution;
      }

      @Override
      public void next() {
        structureIterator.next();
        solution = getSolution();
      }

      @Override
      public void stop() {
        solution = null;
      }

      @Override
      public Iterator<MemorySolution> clone() {
        throw new UnsupportedOperationException();
      }
    };
  }
}
