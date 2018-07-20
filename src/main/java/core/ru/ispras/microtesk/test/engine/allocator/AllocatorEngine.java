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
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * {@code AllocatorEngine} allocates addressing modes for a given abstract sequence.
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
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
  private final Exclusions excluded = new Exclusions();
  private final Dependencies dependencies = new Dependencies();

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
      dependencies.reset();
    }
  }

  public void exclude(final Primitive primitive) {
    excluded.setExcluded(primitive, true);
  }

  public void allocate(
      final List<AbstractCall> sequence,
      final boolean markExplicitAsUsed,
      final boolean reserveDependencies) {
    InvariantChecks.checkNotNull(sequence);

    reset();

    // Phase 1 (optional): mark all explicitly initialized values as 'used'.
    if (markExplicitAsUsed) {
      for (final AbstractCall call : sequence) {
        if (call.isExecutable()) {
          final Primitive primitive = call.getRootOperation();
          useFixedValues(primitive);
        } else if (call.isPreparatorCall()) {
          final Primitive primitive = call.getPreparatorReference().getTarget();
          useFixedValues(primitive);
        }
      }
    }

    // Phase 2 (optional): initialize dependencies.
    if (reserveDependencies) {
      dependencies.init(sequence);
    }

    // Phase 3: allocate the uninitialized addressing modes.
    for (final AbstractCall call : sequence) {
      if (call.isAllocatorAction()) {
        processAllocatorAction(call.getAllocatorAction());
      }

      if (call.isExecutable()) {
        final Primitive primitive = call.getRootOperation();
        allocateUnknownValues(primitive, false);
      } else if (call.isPreparatorCall()) {
        final Primitive primitive = call.getPreparatorReference().getTarget();
        allocateUnknownValues(primitive, true);
      }
    }
  }

  private void processAllocatorAction(final AllocatorAction allocatorAction) {
    InvariantChecks.checkNotNull(allocatorAction);

    final Primitive primitive = allocatorAction.getPrimitive();
    final boolean value = allocatorAction.getValue();

    switch (allocatorAction.getKind()) {
      case FREE:
        InvariantChecks.checkTrue(value);
        freeValues(primitive, allocatorAction.isApplyToAll());
        break;

      case RESERVED:
        excluded.setExcluded(primitive, value);
        break;

      default:
        throw new IllegalArgumentException(
            "Unsupported action kind: " + allocatorAction.getKind());
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

      final Set<Integer> globalExclude = excluded.getExcludedIndexes(mode);
      final Set<Integer> localExclude = AllocatorUtils.toValueSet(allocationData.getExclude());

      // Global excludes apply only to writes.
      final Set<Integer> exclude =
          isWrite ? CollectionUtils.uniteSets(globalExclude, localExclude) : localExclude;

      final Set<Integer> retain =
          AllocatorUtils.toValueSet(allocationData.getRetain());

      return allocationTable.allocate(exclude, retain);
    } catch (final Exception e) {
      throw new GenerationAbortedException(String.format(
          "Failed to allocate %s using %s. Reason: %s.",
          mode,
          allocationTable.getAllocator(),
          e.getMessage())
          );
    } finally {
      allocationTable.setAllocator(defaultAllocator);
    }
  }

  private void allocateUnknownValues(final Primitive primitive, final boolean isWrite) {
    for (final Argument argument : primitive.getArguments().values()) {
      if (AllocatorUtils.isPrimitive(argument)) {
        allocateUnknownValues((Primitive) argument.getValue(), argument.getMode().isOut());
        continue;
      }

      if (AllocatorUtils.isUnknownValue(argument) && AllocatorUtils.isAddressingMode(primitive)) {
        final UnknownImmediateValue unknownValue = (UnknownImmediateValue) argument.getValue();
        final AllocationData allocationData = unknownValue.getAllocationData();

        final int index = allocate(primitive.getName(), isWrite, allocationData);
        unknownValue.setValue(BigInteger.valueOf(index));

        if (allocationData.isReserved()) {
          excluded.setExcluded(primitive.getName(), index, true);
        }
      }
    }

    if (AllocatorUtils.isAddressingMode(primitive)) {
      processDependencies(primitive);
    }
  }

  private void processDependencies(final Primitive primitive) {
    if (!dependencies.contains(primitive)) {
      return;
    }

    final int count = dependencies.getReferenceCount(primitive);
    final boolean isExclude = count != 0;

    excluded.setExcluded(primitive, isExclude);
    dependencies.release(primitive);
  }

  private void useFixedValues(final Primitive primitive) {
    for (final Argument argument : primitive.getArguments().values()) {
      if (AllocatorUtils.isPrimitive(argument)) {
        useFixedValues((Primitive) argument.getValue());
        continue;
      }

      if (AllocatorUtils.isFixedValue(argument) && AllocatorUtils.isAddressingMode(primitive)) {
        final String name = primitive.getName();
        final BigInteger value = argument.getImmediateValue();

        final AllocationTable<Integer, ?> allocationTable = allocationTables.get(name);
        if (allocationTable != null && allocationTable.exists(value.intValue())) {
          allocationTable.use(value.intValue());
        }
      }
    }
  }

  private void freeValues(final Primitive primitive, final boolean isFreeAll) {
    InvariantChecks.checkTrue(AllocatorUtils.isAddressingMode(primitive));

    final AllocationTable<Integer, ?> allocationTable = allocationTables.get(primitive.getName());
    InvariantChecks.checkNotNull(allocationTable);

    if (isFreeAll) {
      allocationTable.reset();
      return;
    }

    InvariantChecks.checkTrue(primitive.getArguments().size() == 1);
    for (final Argument argument : primitive.getArguments().values()) {
      if (AllocatorUtils.isFixedValue(argument)) {
        allocationTable.free(argument.getImmediateValue().intValue());
      }
    }
  }
}
