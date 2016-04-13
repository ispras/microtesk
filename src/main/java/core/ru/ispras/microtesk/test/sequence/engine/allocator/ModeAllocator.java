/*
 * Copyright 2007-2015 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.test.sequence.engine.allocator;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.settings.AllocationSettings;
import ru.ispras.microtesk.settings.ModeSettings;
import ru.ispras.microtesk.settings.RangeSettings;
import ru.ispras.microtesk.settings.StrategySettings;
import ru.ispras.microtesk.test.template.Argument;
import ru.ispras.microtesk.test.template.Call;
import ru.ispras.microtesk.test.template.LabelValue;
import ru.ispras.microtesk.test.template.LazyValue;
import ru.ispras.microtesk.test.template.Primitive;
import ru.ispras.microtesk.test.template.RandomValue;
import ru.ispras.microtesk.test.template.UnknownImmediateValue;
import ru.ispras.microtesk.test.template.Value;

/**
 * {@code ModeAllocator} allocates addressing modes for a given abstract sequence.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class ModeAllocator {
  private static ModeAllocator instance = null;

  public static void init(final AllocationSettings allocation) {
    instance = new ModeAllocator(allocation);
  }

  public static ModeAllocator get() {
    return instance;
  }

  private Map<String, AllocationTable<Integer, ?>> allocationTables = new HashMap<>();

  private ModeAllocator(final AllocationSettings allocation) {
    InvariantChecks.checkNotNull(allocation);
    if (allocation != null) {
      for (final ModeSettings mode : allocation.getModes()) {
        final StrategySettings strategy = mode.getStrategy();
  
        final AllocationStrategy allocationStrategy =
            strategy != null ? strategy.getStrategy() : AllocationStrategyId.RANDOM;
        final Map<String, String> allocationAttributes =
            strategy != null ? strategy.getAttributes() : null;

        final RangeSettings range = mode.getRange();
        if (range != null) {
          final AllocationTable<Integer, ?> allocationTable =
              new AllocationTable<>(allocationStrategy, allocationAttributes, range.getValues());
          allocationTables.put(mode.getName(), allocationTable);
        }
      }
    }
  }

  public void reset() {
    for (final AllocationTable<Integer, ?> allocationTable : allocationTables.values()) {
      allocationTable.reset();
    }
  }

  public void allocate(final List<Call> sequence) {
    InvariantChecks.checkNotNull(sequence);

    reset();

    // Phase 1: mark the initialized addressing modes as 'used'.
    for (final Call call : sequence) {
      if (call.isExecutable()) {
        final Primitive primitive = call.getRootOperation();
        useInitializedModes(primitive);
      }
    }

    // Phase 2: allocate the uninitialized addressing modes.
    for (final Call call : sequence) {
      if (call.isModeToFree()) {
        final Primitive primitive = call.getModeToFree();
        freeInitializedMode(primitive, call.isFreeAllModes());
      }

      if (call.isExecutable()) {
        final Primitive primitive = call.getRootOperation();
        allocateUninitializedModes(primitive);
      }
    }
  }

  private void useInitializedModes(final Primitive primitive) {
    for (final Argument arg : primitive.getArguments().values()) {
      switch (arg.getKind()) {
        case IMM:
          final BigInteger integer = (BigInteger) arg.getValue();
          if (primitive.getKind() == Primitive.Kind.MODE) {
            use(primitive.getName(), integer);
          }
          break;
        case IMM_RANDOM:
          final RandomValue randomValue = (RandomValue) arg.getValue();
          if (primitive.getKind() == Primitive.Kind.MODE) {
            use(primitive.getName(), randomValue.getValue());
          }
          break;
        case IMM_UNKNOWN:
          final UnknownImmediateValue unknownValue = (UnknownImmediateValue) arg.getValue();
          if (primitive.getKind() == Primitive.Kind.MODE && unknownValue.isValueSet()) {
            use(primitive.getName(), unknownValue.getValue());
          }
          break;
        case IMM_LAZY:
          final LazyValue lazyValue = (LazyValue) arg.getValue();
          if (primitive.getKind() == Primitive.Kind.MODE) {
            use(primitive.getName(), lazyValue.getValue());
          }
          break;

        case LABEL:
          final LabelValue labelValue = (LabelValue) arg.getValue();
          if (primitive.getKind() == Primitive.Kind.MODE) {
            use(primitive.getName(), labelValue.getValue());
          }
          break;

        default:
          useInitializedModes((Primitive) arg.getValue());
          break;
      }
    }
  }

  private void allocateUninitializedModes(final Primitive primitive) {
    for (final Argument arg : primitive.getArguments().values()) {
      switch (arg.getKind()) {
        case IMM:
        case IMM_RANDOM:
        case IMM_LAZY:
        case LABEL:
          break;
        case IMM_UNKNOWN:
          final UnknownImmediateValue unknownValue = (UnknownImmediateValue) arg.getValue();
          if (primitive.getKind() == Primitive.Kind.MODE && !unknownValue.isValueSet()) {
            final int value = allocate(
                primitive.getName(), unknownValue.getAllocator(), unknownValue.getExclude());
            unknownValue.setValue(BigInteger.valueOf(value));
          }
          break;
        default:
          allocateUninitializedModes((Primitive) arg.getValue());
          break;
      }
    }
  }

  private void use(final String mode, final BigInteger value) {
    final AllocationTable<Integer, ?> allocationTable = allocationTables.get(mode);

    if (allocationTable != null && allocationTable.exists(value.intValue())) {
      allocationTable.use(value.intValue());
    }
  }

  private int allocate(final String mode, final Allocator allocator, final List<Value> exclude) {
    final AllocationTable<Integer, ?> allocationTable = allocationTables.get(mode);
    InvariantChecks.checkNotNull(allocationTable);

    if (null == allocator) {
      return allocationTable.allocate();
    }

    final Allocator defaultAllocator = allocationTable.getAllocator();
    try {
      allocationTable.setAllocator(allocator);
      return null == exclude ? allocationTable.allocate() :
                               allocationTable.allocate(toValueSet(exclude));
    } finally {
      allocationTable.setAllocator(defaultAllocator);
    }
  }

  private void freeInitializedMode(final Primitive primitive, final boolean isFreeAll) {
    InvariantChecks.checkNotNull(primitive);
    InvariantChecks.checkTrue(primitive.getKind() == Primitive.Kind.MODE);

    final String mode = primitive.getName();
    final AllocationTable<Integer, ?> allocationTable = allocationTables.get(mode);
    InvariantChecks.checkNotNull(allocationTable);

    if (isFreeAll) {
      allocationTable.reset();
      return;
    }

    for (final Argument arg : primitive.getArguments().values()) {
      if (arg.getValue() instanceof BigInteger) {
        final BigInteger value = (BigInteger) arg.getValue();
        allocationTable.free(value.intValue());
      } else if (arg.getValue() instanceof Value) {
        final BigInteger value = ((Value) arg.getValue()).getValue();
        allocationTable.free(value.intValue());
      }
    }
  }

  private static Set<Integer> toValueSet(final List<Value> values) {
    InvariantChecks.checkNotNull(values);

    final Set<Integer> result = new LinkedHashSet<>();
    for (final Value value : values) {
      result.add(value.getValue().intValue());
    }

    return result;
  }
}
