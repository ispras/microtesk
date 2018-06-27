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
import ru.ispras.microtesk.test.template.Primitive;
import ru.ispras.microtesk.test.template.UnknownImmediateValue;

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

  public void exclude(final Primitive primitive) {
    InvariantChecks.checkNotNull(primitive);

    final String name = primitive.getName();
    if (!isAddressingMode(primitive)) {
      throw new GenerationAbortedException(name + " is not an addressing mode.");
    }

    final Set<Integer> excludedValues;
    if (excluded.containsKey(name)) {
      excludedValues = excluded.get(name);
    } else {
      excludedValues = new LinkedHashSet<>();
      excluded.put(name, excludedValues);
    }

    for (final Argument arg : primitive.getArguments().values()) {
      final BigInteger value = arg.getImmediateValue();
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
      if (isPrimitive(arg)) {
        useInitializedModes((Primitive) arg.getValue());
        continue;
      }

      if (isFixedImmediateValue(arg) && isAddressingMode(primitive)) {
        use(primitive.getName(), arg.getImmediateValue());
      }
    }
  }

  private void allocateUninitializedModes(final Primitive primitive, final boolean isWrite) {
    for (final Argument arg : primitive.getArguments().values()) {
      if (isPrimitive(arg)) {
        allocateUninitializedModes((Primitive) arg.getValue(), arg.getMode().isOut());
        continue;
      }

      if (isUnknownImmediateValue(arg) && isAddressingMode(primitive)) {
        final UnknownImmediateValue unknownValue = (UnknownImmediateValue) arg.getValue();
        final int value = allocate(primitive.getName(), isWrite, unknownValue.getAllocationData());
        unknownValue.setValue(BigInteger.valueOf(value));
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

    final AllocationTable<Integer, ?> allocationTable = allocationTables.get(primitive.getName());
    InvariantChecks.checkNotNull(allocationTable);

    if (isFreeAll) {
      allocationTable.reset();
      return;
    }

    for (final Argument arg : primitive.getArguments().values()) {
      if (isFixedImmediateValue(arg)) {
        allocationTable.free(arg.getImmediateValue().intValue());
      }
    }
  }

  private static boolean isAddressingMode(final Primitive primitive) {
    InvariantChecks.checkNotNull(primitive);
    return Primitive.Kind.MODE == primitive.getKind();
  }

  private static boolean isPrimitive(final Argument argument) {
    return argument.getValue() instanceof Primitive;
  }

  private static boolean isFixedImmediateValue(final Argument argument) {
    if (!argument.isImmediate()) {
      return false;
    }

    if (argument.getValue() instanceof UnknownImmediateValue) {
      return ((UnknownImmediateValue) argument.getValue()).isValueSet();
    }

    return true;
  }

  private static boolean isUnknownImmediateValue(final Argument argument) {
    if (!argument.isImmediate()) {
      return false;
    }

    if (argument.getValue() instanceof UnknownImmediateValue) {
      return !((UnknownImmediateValue) argument.getValue()).isValueSet();
    }

    return false;
  }
}
