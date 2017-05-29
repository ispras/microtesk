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
import ru.ispras.microtesk.mmu.test.engine.memory.iterator.MemoryAccessIterator;
import ru.ispras.microtesk.mmu.test.template.MemoryAccessConstraints;
import ru.ispras.microtesk.test.engine.Engine;
import ru.ispras.microtesk.test.engine.EngineContext;
import ru.ispras.microtesk.test.engine.EngineParameter;
import ru.ispras.microtesk.test.engine.EngineResult;
import ru.ispras.microtesk.test.template.AbstractCall;
import ru.ispras.microtesk.test.template.AbstractSequence;
import ru.ispras.microtesk.test.template.Primitive;
import ru.ispras.microtesk.test.template.Situation;
import ru.ispras.testbase.knowledge.iterator.Iterator;

/**
 * {@link MemoryEngine} implements a test engine for memory management units (MMU).
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class MemoryEngine implements Engine {
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

  final static class ParamIterator extends EngineParameter<MemoryAccessIterator.Mode> {
    ParamIterator() {
      super("iterator",
          new EngineParameter.Option<>("static", MemoryAccessIterator.Mode.RANDOM),
          new EngineParameter.Option<>("dynamic", MemoryAccessIterator.Mode.EXHAUSTIVE));
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

  static boolean isLoad(final AbstractCall abstractCall) {
    for (final Primitive primitive : abstractCall.getCommands()) {
      if (primitive.isLoad()) {
        return true;
      }
    }

    return false;
  }

  static boolean isStore(final AbstractCall abstractCall) {
    for (final Primitive primitive : abstractCall.getCommands()) {
      if (primitive.isStore()) {
        return true;
      }
    }

    return false;
  }

  static int getBlockSize(final AbstractCall abstractCall) {
    for (final Primitive primitive : abstractCall.getCommands()) {
      if (primitive.isLoad() || primitive.isStore()) {
        return primitive.getBlockSize();
      }
    }

    return -1;
  }

  static boolean isSuitable(final AbstractCall abstractCall) {
    return isLoad(abstractCall) || isStore(abstractCall);
  }

  static MemoryAccessConstraints getConstraints(final AbstractCall abstractCall) {
    final Primitive primitive = abstractCall.getRootOperation();
    final Situation situation = primitive.getSituation();

    if (situation != null) {
      final Object attribute = situation.getAttribute("path");

      if (attribute != null && attribute instanceof MemoryAccessConstraints) {
        return (MemoryAccessConstraints) attribute;
      }
    }

    return MemoryAccessConstraints.EMPTY;
  }

  static AddressObject getAddressObject(final AbstractCall abstractCall) {
    final Map<String, Object> attributes = abstractCall.getAttributes();
    return (AddressObject) attributes.get("addressObject");
  }

  static void setAddressObject(final AbstractCall abstractCall, final AddressObject addressObject) {
    final Map<String, Object> attributes = abstractCall.getAttributes();
    attributes.put("addressObject", addressObject);
  }

  private MemoryGraphAbstraction abstraction = PARAM_ABSTRACTION.getDefaultValue();
  private boolean preparator = PARAM_PREPARATOR.getDefaultValue();
  private MemoryAccessIterator.Mode iterator = PARAM_ITERATOR.getDefaultValue();
  private int recursionLimit = PARAM_RECURSION_LIMIT.getDefaultValue();
  private int count = PARAM_COUNT.getDefaultValue();

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
  public EngineResult solve(
      final EngineContext engineContext, final AbstractSequence abstractSequence) {
    InvariantChecks.checkNotNull(engineContext);
    InvariantChecks.checkNotNull(abstractSequence);

    final MemoryAccessConstraints globalConstraints = MemoryAccessConstraints.EMPTY;
    Logger.debug("Global memory constraints: %s", globalConstraints);

    final List<MemoryAccessType> accessTypes = new ArrayList<>();
    final List<MemoryAccessConstraints> accessConstraints = new ArrayList<>();

    for (final AbstractCall abstractCall : abstractSequence.getSequence()) {
      if(!isSuitable(abstractCall)) {
        continue;
      }

      final MemoryOperation operation = isLoad(abstractCall)
          ? MemoryOperation.LOAD
          : MemoryOperation.STORE;

      final int blockSizeInBits = getBlockSize(abstractCall);
      InvariantChecks.checkTrue((blockSizeInBits & 7) == 0);

      final int blockSizeInBytes = blockSizeInBits >>> 3;
      accessTypes.add(new MemoryAccessType(operation, DataType.type(blockSizeInBytes)));

      final MemoryAccessConstraints constraints = getConstraints(abstractCall);
      accessConstraints.add(constraints);
    }

    Logger.debug("Memory access types: %s", accessTypes);
    Logger.debug("Memory access constraints: %s", accessConstraints);

    final Iterator<List<MemoryAccess>> accessIterator =
        new MemoryAccessIterator(
            abstraction,
            accessTypes,
            accessConstraints,
            globalConstraints,
            recursionLimit,
            iterator
        );

    final Iterator<AbstractSequence> solutionIterator =
        new Iterator<AbstractSequence>() {
          private int i = 0;
          private List<AddressObject> solutions = null;

          private List<AddressObject> getSolution() {
            final List<AddressObject> solutions = new ArrayList<>();

            while (accessIterator.hasValue()) {
              final List<MemoryAccess> accesses = accessIterator.value();

              solutions.clear();
              for (final MemoryAccess access : accesses) {
                final MemorySolver solver = new MemorySolver(solutions, access);
                final SolverResult<AddressObject> result = solver.solve(Solver.Mode.MAP);

                if (result.getStatus() == SolverResult.Status.SAT) {
                  solutions.add(result.getResult());
                } else {
                  solutions.clear();
                  break;
                }
              }

              if (!solutions.isEmpty()) {
                // Constructed.
                break;
              }

              accessIterator.next();
            }

            return accessIterator.hasValue() ? solutions : null;
          }

          @Override
          public void init() {
            accessIterator.init();
            solutions = getSolution();
          }

          @Override
          public boolean hasValue() {
            return solutions != null && (count == -1 || i < count);
          }

          @Override
          public AbstractSequence value() {
            for (int i = 0; i < abstractSequence.size(); i++) {
              final AbstractCall abstractCall = abstractSequence.getSequence().get(i);
              final AddressObject addressObject = solutions.get(i);

              setAddressObject(abstractCall, addressObject);
            }

            return abstractSequence;
          }

          @Override
          public void next() {
            accessIterator.next();
            solutions = getSolution();

            if (solutions == null && count != -1 && i < count) {
              accessIterator.init();
              solutions = getSolution();
            }

            if (solutions != null) {
              i++;
            }
          }

          @Override
          public void stop() {
            solutions = null;
          }

          @Override
          public Iterator<AbstractSequence> clone() {
            throw new UnsupportedOperationException();
          }
      };

    return new EngineResult(solutionIterator);
  }

  @Override
  public void onStartProgram() {}

  @Override
  public void onEndProgram() {}
}
