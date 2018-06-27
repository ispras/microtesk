/*
 * Copyright 2007-2018 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.test.engine.allocator;

import ru.ispras.fortress.util.CollectionUtils;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.settings.AllocationSettings;
import ru.ispras.microtesk.settings.ModeSettings;
import ru.ispras.microtesk.settings.RangeSettings;
import ru.ispras.microtesk.settings.StrategySettings;
import ru.ispras.microtesk.test.GenerationAbortedException;
import ru.ispras.microtesk.test.template.AbstractCall;
import ru.ispras.microtesk.test.template.Argument;
import ru.ispras.microtesk.test.template.LabelValue;
import ru.ispras.microtesk.test.template.LazyValue;
import ru.ispras.microtesk.test.template.Primitive;
import ru.ispras.microtesk.test.template.RandomValue;
import ru.ispras.microtesk.test.template.UnknownImmediateValue;
import ru.ispras.microtesk.test.template.Value;

import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * {@code AllocatorEngine} allocates addressing modes for a given abstract sequence.
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class AllocatorEngine {
  private static AllocatorEngine instance = null;

  public static void init(final AllocationSettings allocation) {
    instance = new AllocatorEngine(allocation);
  }

  public static AllocatorEngine get() {
    return instance;
  }

  private final Map<String, AllocationTable<Integer, ?>> allocationTables = new HashMap<>();
  private final Map<String, Set<Integer>> excluded = new HashMap<>();

  private AllocatorEngine(final AllocationSettings allocation) {
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

  public void exclude(final Primitive mode) {
    InvariantChecks.checkNotNull(mode);

    final String name = mode.getName();
    if (mode.getKind() != Primitive.Kind.MODE) {
      throw new GenerationAbortedException(name + " is not an addressing mode.");
    }

    final Set<Integer> excludedValues;
    if (excluded.containsKey(name)) {
      excludedValues = excluded.get(name);
    } else {
      excludedValues = new LinkedHashSet<>();
      excluded.put(name, excludedValues);
    }

    for (final Argument arg : mode.getArguments().values()) {
      final BigInteger value;
      if (arg.getValue() instanceof BigInteger) {
        value = (BigInteger) arg.getValue();
      } else if (arg.getValue() instanceof Value) {
        value = ((Value) arg.getValue()).getValue();
      } else {
        throw new IllegalArgumentException("Illegal argument type: " + arg);
      }
      excludedValues.add(value.intValue());
    }
  }

  public void allocate(final List<AbstractCall> sequence, final boolean markExplicitAsUsed) {
    InvariantChecks.checkNotNull(sequence);

    reset();

    if (markExplicitAsUsed) {
      // Phase 1: mark all explicitly initialized addressing modes as 'used'.
      for (final AbstractCall call : sequence) {
        if (call.isExecutable()) {
          final Primitive primitive = call.getRootOperation();
          useInitializedModes(primitive);
        } else if (call.isPreparatorCall()) {
          final Primitive primitive = call.getPreparatorReference().getTarget();
          useInitializedModes(primitive);
        }
      }
    }

    // Phase 2: allocate the uninitialized addressing modes.
    for (final AbstractCall call : sequence) {
      if (call.isModeToFree()) {
        final Primitive primitive = call.getModeToFree();
        freeInitializedMode(primitive, call.isFreeAllModes());
      }

      if (call.isExecutable()) {
        final Primitive primitive = call.getRootOperation();
        allocateUninitializedModes(primitive, false);
      } else if (call.isPreparatorCall()) {
        final Primitive primitive = call.getPreparatorReference().getTarget();
        allocateUninitializedModes(primitive, false);
      }
    }
  }

  private int allocate(
      final String mode,
      final boolean isWrite,
      final AllocationData allocationData) {
    InvariantChecks.checkNotNull(mode);
    InvariantChecks.checkNotNull(allocationData);

    final AllocationTable<Integer, ?> allocationTable = allocationTables.get(mode);
    InvariantChecks.checkNotNull(allocationTable);

    final Allocator defaultAllocator = allocationTable.getAllocator();
    try {
      if (null != allocationData.getAllocator()) {
        allocationTable.setAllocator(allocationData.getAllocator());
      }

      final Set<Integer> globalExclude = excluded.containsKey(mode) ?
          excluded.get(mode) : Collections.<Integer>emptySet();

      final Set<Integer> localExclude = null != allocationData.getExclude() ?
          AllocatorUtils.toValueSet(allocationData.getExclude()) : Collections.<Integer>emptySet();

      // Global excludes apply only to writes.
      final Set<Integer> unitedExclude = isWrite ?
          CollectionUtils.uniteSets(globalExclude, localExclude) : localExclude;

      return unitedExclude.isEmpty()
          ? allocationTable.allocate()
          : allocationTable.allocate(unitedExclude);
    } finally {
      allocationTable.setAllocator(defaultAllocator);
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

  private void allocateUninitializedModes(final Primitive primitive, final boolean isWrite) {
    for (final Argument arg : primitive.getArguments().values()) {
      if (arg.getValue() instanceof Primitive) {
        allocateUninitializedModes((Primitive) arg.getValue(), arg.getMode().isOut());
        continue;
      }

      if (isAddressingMode(primitive) && arg.getValue() instanceof UnknownImmediateValue) {
        final UnknownImmediateValue unknownValue = (UnknownImmediateValue) arg.getValue();
        if (!unknownValue.isValueSet()) {
          final AllocationData allocationData = unknownValue.getAllocationData();
          final int value = allocate(primitive.getName(), isWrite, allocationData);
          unknownValue.setValue(BigInteger.valueOf(value));
        }
      }
    }
  }

  private void use(final String mode, final BigInteger value) {
    final AllocationTable<Integer, ?> allocationTable = allocationTables.get(mode);

    if (allocationTable != null && allocationTable.exists(value.intValue())) {
      allocationTable.use(value.intValue());
    }
  }

  private void freeInitializedMode(final Primitive primitive, final boolean isFreeAll) {
    InvariantChecks.checkTrue(isAddressingMode(primitive));

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
      } else if (arg.getValue() instanceof UnknownImmediateValue) {
        final UnknownImmediateValue value = (UnknownImmediateValue) arg.getValue();
        if (value.isValueSet()) {
          allocationTable.free(value.getValue().intValue());
        }
      } else if (arg.getValue() instanceof Value) {
        final BigInteger value = ((Value) arg.getValue()).getValue();
        allocationTable.free(value.intValue());
      }
    }
  }

  private static boolean isAddressingMode(final Primitive primitive) {
    InvariantChecks.checkNotNull(primitive);
    return Primitive.Kind.MODE == primitive.getKind();
  }
}
