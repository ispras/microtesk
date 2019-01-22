/*
 * Copyright 2007-2019 ISP RAS (http://www.ispras.ru)
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

import java.math.BigInteger;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import ru.ispras.microtesk.test.template.Situation;
import ru.ispras.microtesk.test.template.UnknownImmediateValue;
import ru.ispras.microtesk.test.template.Value;

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

  private final Map<String, AllocationTable<Integer>> allocationTables = new HashMap<>();
  private final Exclusions excluded = new Exclusions();
  private final Dependencies dependencies = new Dependencies();

  private AllocatorEngine(final AllocationSettings allocation) {
    InvariantChecks.checkNotNull(allocation);

    if (allocation != null) {
      for (final ModeSettings mode : allocation.getModes()) {
        final RangeSettings range = mode.getRange();

        if (range != null) {
          final StrategySettings strategy = mode.getStrategy();
          final Map<String, String> attributes = strategy != null
              ? strategy.getAttributes()
              : Collections.<String, String>emptyMap();

          final String track = attributes.get("track");
          final String anBias = attributes.get("free-bias");
          final String aaBias = attributes.get("used-bias");
          final String rnBias = attributes.get("read-free-bias");
          final String raBias = attributes.get("read-used-bias");
          final String rrBias = attributes.get("read-read-bias");
          final String rwBias = attributes.get("read-write-bias");
          final String wnBias = attributes.get("write-free-bias");
          final String waBias = attributes.get("write-used-bias");
          final String wrBias = attributes.get("write-read-bias");
          final String wwBias = attributes.get("write-write-bias");

          final String rn = rnBias != null ? rnBias : anBias;
          final String ra = raBias != null ? raBias : aaBias;
          final String rr = rrBias;
          final String rw = rwBias;
          final String wn = wnBias != null ? wnBias : anBias;
          final String wa = waBias != null ? waBias : aaBias;
          final String wr = wrBias;
          final String ww = wwBias;

          final Map<ResourceOperation, Integer> readAfterRate = new EnumMap<>(ResourceOperation.class);
          final Map<ResourceOperation, Integer> writeAfterRate = new EnumMap<>(ResourceOperation.class);

          readAfterRate.put(ResourceOperation.NOP,    rn != null ? Integer.parseInt(rn) : 0);
          readAfterRate.put(ResourceOperation.ANY,    ra != null ? Integer.parseInt(ra) : 0);
          readAfterRate.put(ResourceOperation.READ,   rr != null ? Integer.parseInt(rr) : 0);
          readAfterRate.put(ResourceOperation.WRITE,  rw != null ? Integer.parseInt(rw) : 0);
          writeAfterRate.put(ResourceOperation.NOP,   wn != null ? Integer.parseInt(wn) : 0);
          writeAfterRate.put(ResourceOperation.ANY,   wa != null ? Integer.parseInt(wa) : 0);
          writeAfterRate.put(ResourceOperation.READ,  wr != null ? Integer.parseInt(wr) : 0);
          writeAfterRate.put(ResourceOperation.WRITE, ww != null ? Integer.parseInt(ww) : 0);

          final AllocationData<Integer> allocationData = new AllocationData<Integer>(
              strategy != null ? strategy.getAllocator() : Allocator.RANDOM,
              range.getValues(),
              Collections.<Integer>emptySet(),
              track != null ? Integer.parseInt(track) : -1,
              readAfterRate,
              writeAfterRate,
              false);

          final AllocationTable<Integer> allocationTable =
              new AllocationTable<>(allocationData, range.getValues());
          allocationTables.put(mode.getName(), allocationTable);
        }
      }
    }
  }

  public void reset() {
    for (final AllocationTable<Integer> allocationTable : allocationTables.values()) {
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

      // Get block-level constraints (they may include allocation constraints).
      final Map<String, Situation> constraints = call.getBlockConstraints();

      if (call.isExecutable()) {
        final Primitive primitive = call.getRootOperation();
        allocateUnknownValues(primitive, constraints, ResourceOperation.NOP);
      } else if (call.isPreparatorCall()) {
        final Primitive primitive = call.getPreparatorReference().getTarget();
        allocateUnknownValues(primitive, constraints, ResourceOperation.WRITE);
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
            String.format("Unsupported action kind: %s", allocatorAction.getKind()));
    }
  }

  private int allocate(
      final String mode,
      final ResourceOperation operation,
      final AllocationData<Value> allocationData) {
    InvariantChecks.checkNotNull(mode);
    InvariantChecks.checkNotNull(allocationData);

    final AllocationTable<Integer> allocationTable = allocationTables.get(mode);
    InvariantChecks.checkNotNull(allocationTable);

    // The default allocation data associated with the given mode.
    final AllocationData<Integer> defaultAllocationData = allocationTable.getAllocationData();

    try {
      if (null != allocationData) {
        // Evaluate lazy values: Value to Integer.
        final AllocationData<Integer> evaluatedData = new AllocationData<>(
            allocationData.getAllocator() != null
              ? allocationData.getAllocator()
              : defaultAllocationData.getAllocator(),
            AllocatorUtils.toIntegerSet(allocationData.getRetain()),
            AllocatorUtils.toIntegerSet(allocationData.getExclude()),
            allocationData.getTrack(),
            allocationData.getReadAfterRate(),
            allocationData.getWriteAfterRate(),
            false);

        allocationTable.setAllocationData(evaluatedData);
      }

      final Set<Integer> globalExclude = excluded.getExcludedIndexes(mode);
      final Set<Integer> localExclude = AllocatorUtils.toIntegerSet(allocationData.getExclude());

      // Global excludes apply only to writes.
      final Set<Integer> exclude = (operation == ResourceOperation.WRITE)
          ? CollectionUtils.uniteSets(globalExclude, localExclude) : localExclude;

      final Set<Integer> retain = AllocatorUtils.toIntegerSet(allocationData.getRetain());

      final Map<ResourceOperation, Integer> rate = (operation == ResourceOperation.WRITE)
          ? allocationData.getWriteAfterRate() : allocationData.getReadAfterRate();

      return allocationTable.allocate(operation, exclude, retain, rate);
    } catch (final Exception e) {
      e.printStackTrace(); // TODO:
      throw new GenerationAbortedException(String.format(
          "Failed to allocate %s using %s. Reason: %s.",
          mode,
          allocationTable.getAllocationData(),
          e.getMessage())
          );
    } finally {
      allocationTable.setAllocationData(defaultAllocationData);
    }
  }

  @SuppressWarnings("unchecked")
  private void allocateUnknownValues(
      final Primitive primitive,
      final Map<String, Situation> constraints,
      final ResourceOperation operation) {
    for (final Argument argument : primitive.getArguments().values()) {
      if (AllocatorUtils.isPrimitive(argument)) {
        final Primitive innerPrimitive = (Primitive) argument.getValue();
        final ResourceOperation innerOperation = argument.getMode().isOut()
            ? ResourceOperation.WRITE : ResourceOperation.READ;

        allocateUnknownValues(innerPrimitive, constraints, innerOperation);
        continue;
      }

      if (AllocatorUtils.isUnknownValue(argument) && AllocatorUtils.isAddressingMode(primitive)) {
        final String modeName = primitive.getName();
        final UnknownImmediateValue unknownValue = (UnknownImmediateValue) argument.getValue();

        // Take into account the block-level allocation constraints.
        final Situation constraint = constraints.get(modeName.toLowerCase());

        final AllocationData<Value> allocationData;

        if (unknownValue.getAllocationData().isSpecified()) {
          allocationData = unknownValue.getAllocationData();
        } else if (constraint != null && constraint.getKind() == Situation.Kind.ALLOCATION) {
          allocationData = (AllocationData<Value>) constraint.getAttribute("allocation");
        } else {
          allocationData = unknownValue.getAllocationData();
        }

        final int index = allocate(modeName, operation, allocationData);
        unknownValue.setValue(BigInteger.valueOf(index));

        if (allocationData.isReserved()) {
          excluded.setExcluded(modeName, index, true);
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
    final boolean isExclude = (count != 0);

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
        final ResourceOperation operation = argument.getMode().isOut() ?
            ResourceOperation.WRITE : ResourceOperation.READ;

        final AllocationTable<Integer> allocationTable = allocationTables.get(name);
        if (allocationTable != null && allocationTable.exists(value.intValue())) {
          allocationTable.use(operation, value.intValue());
        }
      }
    }
  }

  private void freeValues(final Primitive primitive, final boolean isFreeAll) {
    InvariantChecks.checkTrue(AllocatorUtils.isAddressingMode(primitive));

    final AllocationTable<Integer> allocationTable = allocationTables.get(primitive.getName());
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
