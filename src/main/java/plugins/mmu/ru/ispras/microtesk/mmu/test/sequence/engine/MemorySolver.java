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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.ispras.fortress.randomizer.Randomizer;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.basis.Solver;
import ru.ispras.microtesk.basis.SolverResult;
import ru.ispras.microtesk.mmu.basis.BufferAccessEvent;
import ru.ispras.microtesk.mmu.basis.BufferStateTracker;
import ru.ispras.microtesk.mmu.basis.DataType;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryAccess;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryAccessPath;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryAccessStructure;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryUnitedDependency;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryUnitedHazard;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.filter.FilterAccessThenMiss;
import ru.ispras.microtesk.mmu.translator.MmuTranslator;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuAddressType;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuBuffer;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuSubsystem;
import ru.ispras.microtesk.utils.function.BiConsumer;
import ru.ispras.microtesk.utils.function.Function;
import ru.ispras.microtesk.utils.function.Supplier;
import ru.ispras.microtesk.utils.function.TriConsumer;
import ru.ispras.microtesk.utils.function.UnaryOperator;

/**
 * {@link MemorySolver} implements a solver of memory-related constraints (hit, miss, etc.)
 * specified in a memory access structure.
 * 
 * <p>The input is a memory access structure (an object of {@link MemoryAccessStructure});
 * the output is a solution (an object of {@link MemorySolution}).</p>
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class MemorySolver implements Solver<MemorySolution> {
  /**
   * Refers to the test data constructor.
   * 
   * <p>A test data constructor is a user-defined function that maps a memory access (an object
   * of {@link MemoryAccess}) to the test data (an object of {@link AddressObject}).</p>
   */
  private final Function<MemoryAccess, AddressObject> testDataConstructor;

  /**
   * Refers to the test data corrector.
   * 
   * <p>A test data corrector is a user-defined function that corrects inconsistencies in test data
   * (an object of {@link AddressObject}) after solving the constraints.</p>
   */
  private final BiConsumer<MemoryAccess, AddressObject> testDataCorrector;

  /**
   * Given a replaceable device (e.g. a cache unit), contains the tag allocator.
   * 
   * <p>A tag allocator is a user-defined function (with a side effect) that takes an address and
   * returns a tag such that is does not belong to the device set (the set is determined by the
   * address) and was not returned previously for that set.</p>
   */
  private final Map<MmuBuffer, UnaryOperator<Long>> tagAllocators;

  /**
   * Given a non-replaceable device, contains the entry id allocator.
   * 
   * <p>An entry id allocator is a user-defined function (with a side effect) that takes an
   * address and returns an id (internal address) that was not returned previously.</p>
   */
  private final Map<MmuBuffer, UnaryOperator<Long>> entryIdAllocators;

  /**
   * Given a non-replaceable device, contains the entry constructor.
   * 
   * <p>An entry constructor is a user-defined function that creates a new device entry.</p>
   */
  private final Map<MmuBuffer, Supplier<Object>> entryConstructors;

  /**
   * Given a non-replaceable device, contains the entry provider.
   * 
   * <p>An entry provider is a user-defined function that fills a given entry with appropriate data
   * (the data are produced on the basis the memory access and the test data).</p>
   */
  private final Map<MmuBuffer, TriConsumer<MemoryAccess, AddressObject, Object>> entryProviders;

  /** Given a device, maps indices to sets of tags to be explicitly loaded into the device. */
  private final Map<MmuBuffer, Map<Long, Set<Long>>> deviceHitTags = new LinkedHashMap<>();

  /** Given a device, contains indices for which replacing sequences have been constructed. */
  private final Map<MmuBuffer, Set<Long>> deviceReplacedIndices = new LinkedHashMap<>();

  /** Given an access index, contains the devices having been processed. */
  private final Map<Integer, Set<MmuBuffer>> handledDevices = new LinkedHashMap<>();

  private final MmuSubsystem memory = MmuTranslator.getSpecification();
  private final MemoryAccessStructure structure;

  private MemorySolution solution;

  public MemorySolution getCurrentSolution() {
    return solution;
  }

  /**
   * Constructs a solver for the given memory access structure.
   * 
   * @param structure the memory access structure.
   * @param testDataConstructor the test data constructor.
   * @param testDataCorrector the test data corrector.
   * @param tagAllocators the tag allocators.
   * @param entryIdAllocators the entry id allocators.
   * @param entryConstructors the entry constructors.
   * @param entryProviders the entry providers.
   * @throws IllegalArgumentException if some parameters are null.
   */
  public MemorySolver(
      final MemoryAccessStructure structure,
      final Function<MemoryAccess, AddressObject> testDataConstructor,
      final BiConsumer<MemoryAccess, AddressObject> testDataCorrector,
      final Map<MmuBuffer, UnaryOperator<Long>> tagAllocators,
      final Map<MmuBuffer, UnaryOperator<Long>> entryIdAllocators,
      final Map<MmuBuffer, Supplier<Object>> entryConstructors,
      final Map<MmuBuffer, TriConsumer<MemoryAccess, AddressObject, Object>> entryProviders) {

    InvariantChecks.checkNotNull(memory);
    InvariantChecks.checkNotNull(testDataConstructor);
    InvariantChecks.checkNotNull(tagAllocators);
    InvariantChecks.checkNotNull(entryIdAllocators);
    InvariantChecks.checkNotNull(entryConstructors);
    InvariantChecks.checkNotNull(entryProviders);

    this.structure = structure;

    this.testDataConstructor = testDataConstructor;
    this.testDataCorrector = testDataCorrector;
    this.tagAllocators = tagAllocators;
    this.entryIdAllocators = entryIdAllocators;
    this.entryConstructors = entryConstructors;
    this.entryProviders = entryProviders;
  }

  @Override
  public SolverResult<MemorySolution> solve() {
    solution = new MemorySolution(structure);

    SolverResult<MemorySolution> result = null;
    for (int j = 0; j < structure.size(); j++) {
      result = solve(j);

      if (result.getStatus() == SolverResult.Status.UNSAT) {
        break;
      }
    }

    return result;
  }

  /**
   * Solves the address alignment constraint (aligns the address according to the data type).
   * 
   * <p>The approach works only if the address equality relation is transitively closed.</p>
   * 
   * @param j the access index.
   * @param addrType the address type to be aligned.
   * @return the solution.
   */
  private SolverResult<MemorySolution> solveAlignConstraint(
      final int j, final MmuAddressType addrType) {

    final MemoryAccess access = structure.getAccess(j);
    final AddressObject testData = solution.getTestData(j);

    DataType maxDataType = access.getType().getDataType();

    // Get the maximal data type among the dependent instructions.
    for (int k = j + 1; k < solution.size(); k++) {
      final MemoryAccess nextAccess = structure.getAccess(k);
      final MemoryUnitedDependency nextDependency = structure.getUnitedDependency(k);
      final Set<Integer> addrEqualRelation = nextDependency.getAddrEqualRelation(addrType);

      if (addrEqualRelation.contains(j)) {
        final DataType dataType = nextAccess.getType().getDataType();
        if (maxDataType.size() < dataType.size()) {
          maxDataType = dataType;
        }
      }
    }

    // Checks whether the address is unaligned.
    final long oldAddress = testData.getAddress(addrType);

    if (!maxDataType.isAligned(oldAddress)) {
      final long newAddress = maxDataType.align(oldAddress);
      testData.setAddress(addrType, newAddress);
    }

    return new SolverResult<>(solution);
  }

  /**
   * Solves the HIT constraint.
   * 
   * @param j the access index.
   * @param device the device under scrutiny.
   * @return the partial solution.
   */
  private SolverResult<MemorySolution> solveHitConstraint(final int j, final MmuBuffer device) {
    final AddressObject testData = solution.getTestData(j);
    final MmuAddressType addrType = device.getAddress();

    final long address = testData.getAddress(addrType);
    final long tag = device.getTag(address);

    final Set<Long> hitTags = getHitTags(device, address);

    // Check whether the preparation loading has been already scheduled.
    if (hitTags.contains(tag)) {
      // Doing the same thing twice is redundant.
      return new SolverResult<>(solution);
    }

    final MemoryUnitedDependency dependency = structure.getUnitedDependency(j);
    final Set<Integer> tagEqualRelation = dependency.getTagEqualRelation(device);

    // Check whether the previous instructions load the data into the buffer.
    if (!tagEqualRelation.isEmpty()) {
      // Preparation is not required.
      return new SolverResult<>(solution);
    }

    final Set<Integer> tagReplacedRelation = dependency.getTagReplacedRelation(device);

    // Check whether there is a tag-replaced dependency.
    if (!tagReplacedRelation.isEmpty()) {
      // Ignore the hit constraint.
      return new SolverResult<>(solution);
    }

    // Check whether loading the data corrupts the preparation code.
    if (hitTags.size() >= device.getWays()) {
      // Loading the data will cause other useful data to be replaced.
      return new SolverResult<>(String.format("Hit constraint violation for device %s", device));
    }

    // Update the set of hit tags.
    hitTags.add(tag);

    // Add a memory access to cause a HIT.
    // DO NOT CHANGE OFFSET: there are devices, in which offset bits have special meaning, e.g. 
    // in the MIPS TLB, VA[12] chooses between EntryLo0 and EntryLo1.
    final List<Long> sequence = new ArrayList<>();
    sequence.add(address);

    solution.getLoader().addLoads(device, BufferAccessEvent.HIT, address, sequence);

    // Loading data into the buffer may load them into the previous buffers.
    final MemoryAccess access = structure.getAccess(j);
    final MemoryAccessPath path = access.getPath();
    final List<MmuBuffer> devices = path.getBuffers();

    // Scan the devices of the same address type in reverse order.
    boolean found = false;
    for (int i = devices.size() - 1; i >= 0; i--) {
      final MmuBuffer prevDevice = devices.get(i);

      if (!found) {
        found = (prevDevice == device);
        continue;
      }

      if (prevDevice.getAddress() != device.getAddress()) {
        continue;
      }

      if (path.getEvent(prevDevice) == BufferAccessEvent.MISS) {
        final SolverResult<MemorySolution> result = solveMissConstraint(j, prevDevice);

        if (result.getStatus() == SolverResult.Status.UNSAT) {
          return result;
        }
      }
    }

    return new SolverResult<>(solution);
  }

  /**
   * Solves the MISS constraint.
   * 
   * @param j the access index.
   * @param device the device under scrutiny.
   * @return the partial solution.
   */
  private SolverResult<MemorySolution> solveMissConstraint(final int j, final MmuBuffer device) {
    final MemoryUnitedDependency dependency = structure.getUnitedDependency(j);

    if (!FilterAccessThenMiss.test(device, dependency)) {
      return new SolverResult<>(String.format("Miss constraint violation for device %s", device));
    }

    final AddressObject testData = solution.getTestData(j);
    final MmuAddressType addrType = device.getAddress();
    final UnaryOperator<Long> tagAllocator = tagAllocators.get(device);

    final long address = testData.getAddress(addrType);
    final long tag = device.getTag(address);
    final long index = device.getIndex(address);

    final Set<Long> hitTags = getHitTags(device, address);

    if (hitTags.contains(tag)) {
      // Replacement does not make sense, because data will be loaded anyway.
      return new SolverResult<>(solution);
    }

    final Set<Integer> tagReplacedRelation = dependency.getTagReplacedRelation(device);
    final Set<Long> replacedIndices = getReplacedIndices(device);

    // It is enough to use one replacing sequence for all test case instructions.
    if (!replacedIndices.contains(index) &&
        (mayBeHit(j, device) || !tagReplacedRelation.isEmpty())) {
      final List<Long> sequence = new ArrayList<>();

      for (int i = 0; i < device.getWays(); i++) {
        final Long evictingTag = tagAllocator.apply(/* Full address */ address);

        if (evictingTag == null) {
          return new SolverResult<>(
              String.format("Cannot allocate a replacing tag for device %s", device));
        }

        // Address offset is randomized.
        final long replacingOffset =
            device.getOffset(Randomizer.get().nextLong());
        final long replacingAddress =
            device.getAddress(evictingTag, index, replacingOffset);

        sequence.add(replacingAddress);
      }

      solution.getLoader().addLoads(device, BufferAccessEvent.MISS, address, sequence);

      replacedIndices.add(index);
    }

    return new SolverResult<>(solution);
  }

  /**
   * Solves the HIT constraint for the given non-replaceable device.
   * 
   * @param j the access index.
   * @param device the device under scrutiny.
   * @return the partial solution.
   */
  private SolverResult<MemorySolution> solveEntryConstraint(final int j, final MmuBuffer device) {
    final AddressObject testData = solution.getTestData(j);
    final MemoryAccess access = structure.getAccess(j);
    final MemoryUnitedDependency dependency = structure.getUnitedDependency(j);
    final MmuAddressType addrType = device.getAddress();

    final long address = testData.getAddress(addrType);

    final Set<Integer> tagEqualRelation = dependency.getTagEqualRelation(device);

    if (!tagEqualRelation.isEmpty()) {
      final int i = tagEqualRelation.iterator().next();
      final AddressObject prevTestData = solution.getTestData(i);

      // Instruction uses the same entry of the device.
      final Map<Long, Object> entries = prevTestData.getEntries(device);

      // Update the entry (the map contains one entry).
      for (final Object entry : entries.values()) {
        entryProviders.get(device).accept(access, testData, entry);
      }

      testData.setEntries(device, entries);
    }

    if (testData.getEntries(device) == null || testData.getEntries(device).isEmpty()) {
      final UnaryOperator<Long> entryIdAllocator =
          entryIdAllocators.get(device);
      final Supplier<Object> entryConstructor =
          entryConstructors.get(device);
      final TriConsumer<MemoryAccess, AddressObject, Object> entryProvider =
          entryProviders.get(device);

      final Long deviceEntryId = entryIdAllocator.apply(address);
      final Object deviceEntry = entryConstructor.get();

      if (deviceEntryId == null || deviceEntry == null) {
        return new SolverResult<>(String.format("Cannot allocate an entry for device %s", device));
      }

      // Filling the entry with appropriate data.
      entryProvider.accept(access, testData, deviceEntry);

      testData.addEntry(device, deviceEntryId, deviceEntry);
      solution.addEntry(device, deviceEntryId, deviceEntry);
    }

    return new SolverResult<>(solution);
  }

  /**
   * Solves the ADDR-EQUAL constraint ({@code ADDR[j] == ADDR[i]}).
   * 
   * @param j the access index.
   * @param addrType the address type.
   * @return the partial solution.
   */
  private SolverResult<MemorySolution> solveAddrEqualConstraint(
      final int j, final MmuAddressType addrType) {
    final AddressObject testData = solution.getTestData(j);

    final MemoryUnitedDependency dependency = structure.getUnitedDependency(j);
    final Set<Integer> addrEqualRelation = dependency.getAddrEqualRelation(addrType);

    // The instruction uses the same address as one of the previous instructions.
    if (!addrEqualRelation.isEmpty()) {
      final int i = addrEqualRelation.iterator().next();
      final AddressObject prevTestData = solution.getTestData(i);

      final long newAddress = prevTestData.getAddress(addrType);

      // Copy the address from the previous instruction.
      testData.setAddress(addrType, newAddress);
    }

    return new SolverResult<>(solution);
  }

  /**
   * Solves the INDEX-EQUAL constraint ({@code INDEX[j] == INDEX[i]}).
   * 
   * @param j the access index.
   * @param device the device under scrutiny.
   * @return the partial solution.
   */
  private SolverResult<MemorySolution> solveIndexEqualConstraint(
      final int j, final MmuBuffer device) {
    final AddressObject testData = solution.getTestData(j);
    final MmuAddressType addrType = device.getAddress();

    final MemoryUnitedDependency dependency = structure.getUnitedDependency(j);
    final Set<Integer> indexEqualRelation = dependency.getIndexEqualRelation(device);

    if (!indexEqualRelation.isEmpty()) {
      final int i = indexEqualRelation.iterator().next();
      final AddressObject prevTestData = solution.getTestData(i);

      final UnaryOperator<Long> tagAllocator = tagAllocators.get(device);
  
      final long oldTag = device.getTag(testData.getAddress(addrType));
      final long oldIndex = device.getIndex(testData.getAddress(addrType));
      final long newIndex = device.getIndex(prevTestData.getAddress(addrType));
      final long oldOffset = device.getOffset(testData.getAddress(addrType));

      // Copy the index from the previous instruction.
      final long newAddress = device.getAddress(oldTag, newIndex, oldOffset);

      // If the index has changed, allocate a new tag.
      final long newTag = newIndex != oldIndex ? tagAllocator.apply(newAddress) : oldTag;

      testData.setAddress(addrType, device.getAddress(newTag, newIndex, oldOffset));
    }

    return new SolverResult<>(solution);
  }

  /**
   * Solves the TAG-EQUAL constraint ({@code INDEX[j] == INDEX[i] && TAG[j] == TAG[i]}).
   * 
   * @param j the access index.
   * @param device the device under scrutiny.
   * @return the partial solution.
   */
  private SolverResult<MemorySolution> solveTagEqualConstraint(
      final int j, final MmuBuffer device) {
    final AddressObject testData = solution.getTestData(j);
    final MmuAddressType addrType = device.getAddress();

    final MemoryUnitedDependency dependency = structure.getUnitedDependency(j);
    final Set<Integer> tagEqualRelation = dependency.getTagEqualRelation(device);

    // Instruction uses the same tag and the same index as one of the previous instructions.
    if (!tagEqualRelation.isEmpty()) {
      final int i = tagEqualRelation.iterator().next();
      final AddressObject prevTestData = solution.getTestData(i);

      // Copy the tag and the index from the previous instruction.
      final long newTag = device.getTag(prevTestData.getAddress(addrType));
      final long newIndex = device.getIndex(prevTestData.getAddress(addrType));
      final long oldOffset = device.getOffset(testData.getAddress(addrType));

      testData.setAddress(addrType, device.getAddress(newTag, newIndex, oldOffset));
    }

    return new SolverResult<>(solution);
  }

  /**
   * Predicts replacements in the device (buffer) up to the {@code j} access and solve the
   * corresponding constraints.
   * 
   * @param j the access index.
   * @param device the device under scrutiny.
   * @return the partial solution.
   */
  private SolverResult<MemorySolution> solveTagReplacedConstraints(
      final int j, final MmuBuffer device) {
    final MmuAddressType addrType = device.getAddress();

    // TODO: int -> long
    final BufferStateTracker<Long> stateTracker = new BufferStateTracker<>(
        (int) device.getSets(), (int) device.getWays(), device.getAddressView());

    stateTracker.access(solution.getLoader().prepareLoads(addrType));

    // Maps access indices to the replaced tags.
    final Map<Integer, Long> replacedTags = new LinkedHashMap<>();

    for (int i = 0; i <= j; i++) {
      final MemoryAccess access = structure.getAccess(i);
      final MemoryAccessPath path = access.getPath();
      final MemoryUnitedDependency dependency = structure.getUnitedDependency(i);
      final AddressObject testData = solution.getTestData(i);

      final long address = testData.getAddress(addrType);
      final long index = device.getIndex(address);
      final long offset = device.getOffset(address);

      // Check the device access condition.
      if (path.contains(device) && device.checkGuard(access)) {
        final Long replacedTag = stateTracker.access(address);

        if (replacedTag != null) {
          replacedTags.put(i, replacedTag);
        }
      }

      // Satisfy the TAG-REPLACED constraint.
      final Set<Integer> tagReplacedRelation = dependency.getTagReplacedRelation(device);

      if (!tagReplacedRelation.isEmpty()) {
        final int dependsOn = tagReplacedRelation.iterator().next();
        final Long replacedTag = replacedTags.get(dependsOn);

        if (replacedTag == null) {
          return new SolverResult<>(String.format("Replace constraint violation for %s", device));
        }

        testData.setAddress(addrType, device.getAddress(replacedTag, index, offset));
      }

      // TAG-NOT-REPLACED constraints are satisfied AUTOMATICALLY.
    }

    return new SolverResult<>(solution);
  }

  /**
   * Solve hit/miss constraints specified for the given device.
   * 
   * @param j the access index.
   * @param device the device under scrutiny.
   * @return the partial solution.
   */
  private SolverResult<MemorySolution> solveDeviceConstraint(
      final int j, final MmuBuffer device) {
    // Do nothing if the device has been already handled.
    Set<MmuBuffer> handledDevicesForExecution = handledDevices.get(j);

    if (handledDevicesForExecution == null) {
      handledDevices.put(j, handledDevicesForExecution = new LinkedHashSet<>());
    } else if (handledDevicesForExecution.contains(device)) {
      return new SolverResult<MemorySolution>(solution);
    }

    handledDevicesForExecution.add(device);

    final MemoryAccess access = structure.getAccess(j);
    final MemoryAccessPath path = access.getPath();

    // If the buffer access event is null, the situation is considered to be a hit.
    // The event is null, if the device is a parent of some view and is not in the access. 
    final BufferAccessEvent realEvent = path.getEvent(device);
    final BufferAccessEvent usedEvent = realEvent == null ? BufferAccessEvent.HIT : realEvent;

    // The device is a view of another device (e.g., DTLB is a view of JTLB).
    if (device.isView()) {
      solveDeviceConstraint(j, device.getParent());
    }

    final boolean canBeAccessed =
        // The parent access event is a hit or null, but not a miss.
        !device.isView() || path.getEvent(device.getParent()) != BufferAccessEvent.MISS;

    if (canBeAccessed && device.checkGuard(access)) {
      SolverResult<MemorySolution> result = null;

      if (device.isReplaceable()) {
        // Construct a sequence of addresses to be accessed.
        if (usedEvent == BufferAccessEvent.HIT) {
          result = solveHitConstraint(j, device);
        } else {
          result = solveMissConstraint(j, device);
        }

        if (result.getStatus() != SolverResult.Status.UNSAT) {
          result = solveTagReplacedConstraints(j, device);
        }
      } else {
        // Construct a set of entries to be written to the device.
        if (usedEvent == BufferAccessEvent.HIT) {
          result = solveEntryConstraint(j, device);
        } else {
          // Do nothing: the constraint is satisfied by tag allocators.
        }
      }

      if (result != null && result.getStatus() == SolverResult.Status.UNSAT) {
        return result;
      }
    } else {
      if (path.getEvent(device) == BufferAccessEvent.HIT) {
        return new SolverResult<>(String.format("Constraint violation for device %s", device));
      }
    }

    return new SolverResult<MemorySolution>(solution);
  }

  /**
   * Handles the given instruction call (access) of the memory access structure.
   * 
   * @param j the access index.
   * @return the partial solution.
   */
  private SolverResult<MemorySolution> solve(final int j) {
    final MemoryAccess access = structure.getAccess(j);
    final MemoryAccessPath path = access.getPath();

    final MemoryUnitedDependency dependency = structure.getUnitedDependency(j);

    // Construct initial test data for the access.
    final AddressObject testData = testDataConstructor.apply(access);
    solution.setTestData(j, testData);

    // Align the addresses.
    for (final MmuAddressType addrType : testData.getAddresses().keySet()) {
      solveAlignConstraint(j, addrType);
    }

    // Assign the tag, index and offset according to the dependencies.
    final Map<MmuAddressType, MemoryUnitedHazard> addrHazards = dependency.getAddrHazards();

    for (final Map.Entry<MmuAddressType, MemoryUnitedHazard> addrEntry : addrHazards.entrySet()) {
      final MmuAddressType addrType = addrEntry.getKey();
      final Set<Integer> addrEqualRelation = dependency.getAddrEqualRelation(addrType);

      if (!addrEqualRelation.isEmpty()) {
        solveAddrEqualConstraint(j, addrType);

        // Paranoid check.
        final long addr = testData.getAddress(addrType);

        if (!testData.getType().getDataType().isAligned(addr)) {
          throw new IllegalStateException(
              String.format("Unaligned address after solving AddrEqual constraints: %x", addr));
        }
      } else {
        final Map<MmuBuffer, MemoryUnitedHazard> deviceHazards = dependency.getDeviceHazards(addrType);

        for (Map.Entry<MmuBuffer, MemoryUnitedHazard> deviceEntry : deviceHazards.entrySet()) {
          final MmuBuffer deviceType = deviceEntry.getKey();
          final Set<Integer> tagEqualRelation = dependency.getTagEqualRelation(deviceType);
          final Set<Integer> indexEqualRelation = dependency.getIndexEqualRelation(deviceType);

          if (!tagEqualRelation.isEmpty()) {
            solveTagEqualConstraint(j, deviceType);
          } else if (!indexEqualRelation.isEmpty()) {
            solveIndexEqualConstraint(j, deviceType);
          }
        }
      }
    }

    // Solve the hit and miss constraints for the devices.
    final List<MmuBuffer> devices = path.getBuffers();

    for (final MmuBuffer device : devices) {
      final SolverResult<MemorySolution> result = solveDeviceConstraint(j, device);

      if (result.getStatus() == SolverResult.Status.UNSAT) {
        return result;
      }
    }

    // Correct the solution.
    testDataCorrector.accept(access, testData);

    return new SolverResult<MemorySolution>(solution);
  }

  /**
   * Returns the set of tags to be explicitly loaded into the device to cause the hits.
   * 
   * @param device the MMU device (buffer).
   * @param address the address.
   * @return the set of tags.
   */
  private Set<Long> getHitTags(final MmuBuffer device, final long address) {
    final long index = device.getIndex(address);

    Map<Long, Set<Long>> hitIndices = deviceHitTags.get(device);
    if (hitIndices == null) {
      deviceHitTags.put(device, hitIndices = new LinkedHashMap<>());
    }

    Set<Long> hitTags = hitIndices.get(index);
    if (hitTags == null) {
      hitIndices.put(index, hitTags = new LinkedHashSet<>());
    }

    return hitTags;
  }

  /**
   * Returns the indices for which replacing sequences have been constructed.
   * 
   * @param device the MMU device (buffer).
   * @return the set of indices.
   */
  private Set<Long> getReplacedIndices(final MmuBuffer device) {
    Set<Long> replacedIndices = deviceReplacedIndices.get(device);
    if (replacedIndices == null) {
      deviceReplacedIndices.put(device, replacedIndices = new LinkedHashSet<>());
    }

    return replacedIndices;
  }

  /**
   * Checks whether a hit into the given device is possible for the given access.
   * 
   * @param j the access index.
   * @param device the MMU device (buffer).
   * @return {@code false} if a hit is infeasible; {@code true} if a hit is possible.
   */
  private boolean mayBeHit(final int j, final MmuBuffer device) {
    final MmuAddressType addrType = device.getAddress();

    // TODO: This check can be optimized.
    final MemoryAccess access = structure.getAccess(j);
    final MemoryAccessPath path = access.getPath();
    final List<MmuAddressType> addresses = path.getAddresses();

    // TODO: This is not accurate if addrType = VA, prevAddrType = PA. 
    for (final MmuAddressType prevAddrType : addresses) {
      if (prevAddrType != addrType) {
        if (!solution.getLoader().prepareLoads(prevAddrType).isEmpty()) {
          // Possible HIT.
          return true;
        }
      }
    }

    final AddressObject testData = solution.getTestData(j);

    final long address = testData.getAddress(addrType);
    final long tag = device.getTag(address);
    final long index = device.getIndex(address);

    for (final long loadedAddress : solution.getLoader().prepareLoads(addrType)) {
      final long loadedTag = device.getTag(loadedAddress);
      final long loadedIndex = device.getIndex(loadedAddress);

      if (loadedIndex == index && loadedTag == tag) {
        // Possibly HIT.
        return true;
      }
    }

    // Definitely MISS.
    return false;
  }
}
