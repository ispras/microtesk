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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.mmu.basis.DataType;
import ru.ispras.microtesk.mmu.basis.MemoryAccessType;
import ru.ispras.microtesk.mmu.basis.MemoryOperation;
import ru.ispras.microtesk.mmu.test.template.AccessConstraints;
import ru.ispras.microtesk.test.engine.AbstractSequence;
import ru.ispras.microtesk.test.engine.Engine;
import ru.ispras.microtesk.test.engine.EngineContext;
import ru.ispras.microtesk.test.engine.EngineParameter;
import ru.ispras.microtesk.test.engine.SequenceSelector;
import ru.ispras.microtesk.test.template.AbstractCall;
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
  public static final String PATH = "path";

  final static class ParamAbstraction extends EngineParameter<GraphAbstraction> {
    ParamAbstraction() {
      super("classifier",
          new EngineParameter.Option<>("buffer-access", GraphAbstraction.BUFFER_ACCESS),
          new EngineParameter.Option<>("trivial", GraphAbstraction.TRIVIAL),
          new EngineParameter.Option<>("universal", GraphAbstraction.UNIVERSAL));
    }
  }

  final static class ParamPreparator extends EngineParameter<Boolean> {
    ParamPreparator() {
      super("preparator",
          new EngineParameter.Option<>("static", Boolean.TRUE),
          new EngineParameter.Option<>("dynamic", Boolean.FALSE));
    }
  }

  final static class ParamIterator extends EngineParameter<AccessesIterator.Mode> {
    ParamIterator() {
      super("iterator",
          new EngineParameter.Option<>("static", AccessesIterator.Mode.RANDOM),
          new EngineParameter.Option<>("dynamic", AccessesIterator.Mode.EXHAUSTIVE));
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

  private static MemoryOperation getOperation(final AbstractCall abstractCall) {
    for (final Primitive primitive : abstractCall.getCommands()) {
      if (primitive.isLoad()) {
        return MemoryOperation.LOAD;
      }
      if (primitive.isStore()) {
        return MemoryOperation.STORE;
      }
    }

    return MemoryOperation.NONE;
  }

  private static int getBlockSize(final AbstractCall abstractCall) {
    for (final Primitive primitive : abstractCall.getCommands()) {
      if (primitive.isLoad() || primitive.isStore()) {
        return primitive.getBlockSize();
      }
    }

    return -1;
  }

  private static AccessConstraints getConstraints(final AbstractCall abstractCall) {
    final Primitive primitive = abstractCall.getRootOperation();
    final Situation situation = primitive.getSituation();

    if (situation != null) {
      final Object attribute = situation.getAttribute(PATH);

      if (attribute != null && attribute instanceof AccessConstraints) {
        return (AccessConstraints) attribute;
      }
    }

    return AccessConstraints.EMPTY;
  }

  private static void setAccess(final AbstractCall abstractCall, final Access access) {
    final Primitive primitive = abstractCall.getRootOperation();

    final Situation oldSituation = primitive.getSituation();
    final Map<String, Object> oldAttributes = oldSituation.getAttributes();

    final Map<String, Object> newAttributes = new LinkedHashMap<>(oldAttributes);
    newAttributes.put(MemoryDataGenerator.CONSTRAINT, access);
    final Situation newSituation = new Situation(MemoryEngine.ID, newAttributes);

    primitive.setSituation(newSituation);
  }

  private GraphAbstraction abstraction = PARAM_ABSTRACTION.getDefaultValue();
  private boolean preparator = PARAM_PREPARATOR.getDefaultValue();
  private AccessesIterator.Mode iterator = PARAM_ITERATOR.getDefaultValue();
  private int recursionLimit = PARAM_RECURSION_LIMIT.getDefaultValue();
  private int count = PARAM_COUNT.getDefaultValue();

  private final SequenceSelector sequenceSelector = new SequenceSelector(ID);

  @Override
  public String getId() {
    return ID;
  }

  @Override
  public SequenceSelector getSequenceSelector() {
    return sequenceSelector;
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
  public Iterator<AbstractSequence> solve(
      final EngineContext engineContext, final AbstractSequence abstractSequence) {
    InvariantChecks.checkNotNull(engineContext);
    InvariantChecks.checkNotNull(abstractSequence);

    final AccessConstraints globalConstraints = AccessConstraints.EMPTY;
    Logger.debug("Global memory constraints: %s", globalConstraints);

    final List<MemoryAccessType> accessTypes = new ArrayList<>();
    final List<AccessConstraints> accessConstraints = new ArrayList<>();

    for (final AbstractCall abstractCall : abstractSequence.getSequence()) {
      final MemoryOperation operation = getOperation(abstractCall);

      final int blockSizeInBits = operation != MemoryOperation.NONE
          ? getBlockSize(abstractCall)
          : (1 << 3) /* Does not matter */;
      InvariantChecks.checkTrue((blockSizeInBits & 7) == 0);

      final int blockSizeInBytes = blockSizeInBits >>> 3;
      accessTypes.add(new MemoryAccessType(operation, DataType.type(blockSizeInBytes)));

      final AccessConstraints constraints = operation != MemoryOperation.NONE
          ? getConstraints(abstractCall)
          : AccessConstraints.EMPTY;

      accessConstraints.add(constraints);
    }

    Logger.debug("Memory access types: %s", accessTypes);
    Logger.debug("Memory access constraints: %s", accessConstraints);

    final Iterator<List<Access>> accessIterator =
        new AccessesIterator(
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

          @Override
          public void init() {
            accessIterator.init();
          }

          @Override
          public boolean hasValue() {
            return accessIterator.hasValue() && (count == -1 || i < count);
          }

          @Override
          public AbstractSequence value() {
            final List<AbstractCall> abstractCalls = abstractSequence.getSequence();
            final List<Access> accesses = accessIterator.value();

            for (int i = 0; i < abstractCalls.size(); i++) {
              final AbstractCall abstractCall = abstractCalls.get(i);
              final Access access = accesses.get(i);

              setAccess(abstractCall, access);
            }

            return abstractSequence;
          }

          @Override
          public void next() {
            accessIterator.next();
            i++;
          }

          @Override
          public void stop() {
            accessIterator.stop();
          }

          @Override
          public Iterator<AbstractSequence> clone() {
            throw new UnsupportedOperationException();
          }
      };

    return solutionIterator;
  }

  @Override
  public void onStartProgram() {}

  @Override
  public void onEndProgram() {}
}
