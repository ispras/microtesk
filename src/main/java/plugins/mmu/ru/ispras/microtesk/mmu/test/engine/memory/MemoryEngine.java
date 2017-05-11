/*
 * Copyright 2006-2017 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.mmu.test.engine.memory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.basis.solver.Solver;
import ru.ispras.microtesk.basis.solver.SolverResult;
import ru.ispras.microtesk.mmu.basis.DataType;
import ru.ispras.microtesk.mmu.basis.MemoryAccessType;
import ru.ispras.microtesk.mmu.basis.MemoryOperation;
import ru.ispras.microtesk.mmu.test.engine.memory.coverage.MemoryGraphAbstraction;
import ru.ispras.microtesk.mmu.test.engine.memory.iterator.MemoryAccessStructureIterator;
import ru.ispras.microtesk.mmu.test.template.MemoryAccessConstraints;
import ru.ispras.microtesk.test.engine.Engine;
import ru.ispras.microtesk.test.engine.EngineContext;
import ru.ispras.microtesk.test.engine.EngineParameter;
import ru.ispras.microtesk.test.engine.EngineResult;
import ru.ispras.microtesk.test.template.Call;
import ru.ispras.microtesk.test.template.Primitive;
import ru.ispras.microtesk.test.template.Situation;
import ru.ispras.testbase.knowledge.iterator.Iterator;

/**
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class MemoryEngine implements Engine<MemorySolution> {
  public static final String ID = "memory";

  final static class ParamAbstraction extends EngineParameter<MemoryGraphAbstraction> {
    ParamAbstraction() {
      super("classifier",
          new EngineParameter.Option<>("buffer-access", MemoryGraphAbstraction.BUFFER_ACCESS),
          new EngineParameter.Option<>("trivial", MemoryGraphAbstraction.TRIVIAL),
          new EngineParameter.Option<>("universal", MemoryGraphAbstraction.UNIVERSAL));
    }
  }

  final static class ParamPreparator extends EngineParameter<Boolean> {
    ParamPreparator() {
      super("preparator",
          new EngineParameter.Option<>("static", Boolean.TRUE),
          new EngineParameter.Option<>("dynamic", Boolean.FALSE));
    }
  }

  final static class ParamIterator extends EngineParameter<MemoryAccessStructureIterator.Mode> {
    ParamIterator() {
      super("iterator",
          new EngineParameter.Option<>("static", MemoryAccessStructureIterator.Mode.RANDOM),
          new EngineParameter.Option<>("dynamic", MemoryAccessStructureIterator.Mode.EXHAUSTIVE));
    }
  }

  final static class ParamRecursionLimit extends EngineParameter<Integer> {
    ParamRecursionLimit() {
      super("recursion-limit");
    }

    @Override
    public Integer getValue(final Object option) {
      final Number number = (option instanceof Number)
          ? (Number) option : Integer.parseInt(option.toString(), 10);

      return number.intValue();
    }

    @Override
    public Integer getDefaultValue() {
      return 1;
    }
  }

  final static class ParamCount extends EngineParameter<Integer> {
    ParamCount() {
      super("count");
    }

    @Override
    public Integer getValue(final Object option) {
      final Number number = (option instanceof Number)
          ? (Number) option : Integer.parseInt(option.toString(), 10);

      return number.intValue();
    }

    @Override
    public Integer getDefaultValue() {
      return 1;
    }
  }

  static final ParamAbstraction PARAM_ABSTRACTION = new ParamAbstraction();
  static final ParamPreparator PARAM_PREPARATOR = new ParamPreparator();
  static final ParamIterator PARAM_ITERATOR = new ParamIterator();
  static final ParamRecursionLimit PARAM_RECURSION_LIMIT = new ParamRecursionLimit();
  static final ParamCount PARAM_COUNT = new ParamCount();

  public static boolean isMemoryAccessWithSituation(final Call abstractCall) {
    InvariantChecks.checkNotNull(abstractCall);

    if (!abstractCall.isLoad() && !abstractCall.isStore()) {
      return false;
    }

    final Primitive primitive = abstractCall.getRootOperation();
    InvariantChecks.checkNotNull(primitive, "Primitive is null");

    final Situation situation = primitive.getSituation();
    return situation != null;
  }

  public static MemoryAccessConstraints getMemoryAccessConstraints(final Call abstractCall) {
    if (!isMemoryAccessWithSituation(abstractCall)) {
      return MemoryAccessConstraints.EMPTY;
    }

    final Primitive primitive = abstractCall.getRootOperation();
    final Situation situation = primitive.getSituation();

    final Object attribute = situation.getAttribute("path");
    if (null == attribute) {
      return MemoryAccessConstraints.EMPTY;
    }

    if (attribute instanceof MemoryAccessConstraints) {
      return (MemoryAccessConstraints) attribute;
    }

    Logger.warning("Unexpected format of the path attribute of a test situation: %s", attribute);
    return MemoryAccessConstraints.EMPTY;
  }

  private MemoryGraphAbstraction abstraction = PARAM_ABSTRACTION.getDefaultValue();
  private boolean preparator = PARAM_PREPARATOR.getDefaultValue();
  private MemoryAccessStructureIterator.Mode iterator = PARAM_ITERATOR.getDefaultValue();
  private int recursionLimit = PARAM_RECURSION_LIMIT.getDefaultValue();
  private int count = PARAM_COUNT.getDefaultValue();

  @Override
  public Class<MemorySolution> getSolutionClass() {
    return MemorySolution.class;
  }

  @Override
  public void configure(final Map<String, Object> attributes) {
    InvariantChecks.checkNotNull(attributes);

    abstraction = PARAM_ABSTRACTION.parse(attributes.get(PARAM_ABSTRACTION.getName()));
    preparator = PARAM_PREPARATOR.parse(attributes.get(PARAM_PREPARATOR.getName()));
    iterator = PARAM_ITERATOR.parse(attributes.get(PARAM_ITERATOR.getName()));
    recursionLimit = PARAM_RECURSION_LIMIT.parse(attributes.get(PARAM_RECURSION_LIMIT.getName()));
    count = PARAM_COUNT.parse(attributes.get(PARAM_COUNT.getName()));

    Logger.debug("Memory engine configuration: %s=%s, %s=%b, %s=%s, %s=%d, %s=%d",
        PARAM_ABSTRACTION, abstraction,
        PARAM_PREPARATOR, preparator,
        PARAM_ITERATOR, iterator,
        PARAM_RECURSION_LIMIT, recursionLimit,
        PARAM_COUNT, count);

    InvariantChecks.checkTrue(count == -1 || count >= 0);
  }

  @Override
  public EngineResult<MemorySolution> solve(
      final EngineContext engineContext, final List<Call> abstractSequence) {
    InvariantChecks.checkNotNull(engineContext);
    InvariantChecks.checkNotNull(abstractSequence);

    final MemoryAccessConstraints globalConstraints = MemoryAccessConstraints.EMPTY;
    Logger.debug("Global memory constraints: %s", globalConstraints);

    final List<MemoryAccessType> accessTypes = new ArrayList<>();
    final List<MemoryAccessConstraints> accessConstraints = new ArrayList<>();

    for (final Call abstractCall : abstractSequence) {
      if(!isMemoryAccessWithSituation(abstractCall)) {
        // Skip non-memory-access instructions and memory-accesses instructions without situations.
        continue;
      }

      final MemoryOperation operation =
          abstractCall.isLoad() ? MemoryOperation.LOAD : MemoryOperation.STORE;

      final int blockSizeInBits = abstractCall.getBlockSize();
      InvariantChecks.checkTrue((blockSizeInBits & 7) == 0);

      final int blockSizeInBytes = blockSizeInBits >>> 3;
      accessTypes.add(new MemoryAccessType(operation, DataType.type(blockSizeInBytes)));

      final MemoryAccessConstraints constraints = getMemoryAccessConstraints(abstractCall);
      accessConstraints.add(constraints);
    }

    Logger.debug("Creating memory access iterator: %s", accessTypes);
    Logger.debug("Memory access constraints: %s", accessConstraints);

    final Iterator<MemoryAccessStructure> structureIterator =
        new MemoryAccessStructureIterator(
            abstraction,
            accessTypes,
            accessConstraints,
            globalConstraints,
            recursionLimit,
            iterator);

    final Iterator<MemorySolution> solutionIterator =
        new Iterator<MemorySolution>() {
          private int i = 0;
          private MemorySolution solution = null;

          private MemorySolution getSolution() {
            while (structureIterator.hasValue()) {
              final MemoryAccessStructure structure = structureIterator.value();
              final MemorySolver solver = new MemorySolver(structure);
              final SolverResult<MemorySolution> result = solver.solve(Solver.Mode.MAP);
              InvariantChecks.checkNotNull(result);

              if (result.getStatus() == SolverResult.Status.SAT) {
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
            return solution != null && (count == -1 || i < count);
          }

          @Override
          public MemorySolution value() {
            return solution;
          }

          @Override
          public void next() {
            // Randomize.
            /*
            globalConstraints.randomize();
            for (final MemoryAccessConstraints constraints : accessConstraints) {
              constraints.randomize();
            }
            */

            structureIterator.next();
            solution = getSolution();

            if (solution == null && count != -1 && i < count) {
              structureIterator.init();
              solution = getSolution();
            }

            if (solution != null) {
              i++;
            }
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

    return new EngineResult<MemorySolution>(solutionIterator);
  }

  @Override
  public void onStartProgram() {
    // TODO
  }

  @Override
  public void onEndProgram() {
    // TODO
  }
}
