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

package ru.ispras.microtesk.mmu.test.sequence.engine.memory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.ispras.fortress.randomizer.Randomizer;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.fortress.util.Pair;
import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.basis.solver.BiasedConstraints;
import ru.ispras.microtesk.basis.solver.Solver;
import ru.ispras.microtesk.basis.solver.SolverResult;
import ru.ispras.microtesk.basis.solver.integer.IntegerConstraint;
import ru.ispras.microtesk.basis.solver.integer.IntegerDomainConstraint;
import ru.ispras.microtesk.basis.solver.integer.IntegerField;
import ru.ispras.microtesk.basis.solver.integer.IntegerVariable;
import ru.ispras.microtesk.basis.solver.integer.IntegerVariableInitializer;
import ru.ispras.microtesk.mmu.MmuPlugin;
import ru.ispras.microtesk.mmu.basis.BufferAccessEvent;
import ru.ispras.microtesk.mmu.basis.BufferStateTracker;
import ru.ispras.microtesk.mmu.basis.DataType;
import ru.ispras.microtesk.mmu.basis.MemoryAccessConstraints;
import ru.ispras.microtesk.mmu.basis.MemoryAccessType;
import ru.ispras.microtesk.mmu.settings.MmuSettingsUtils;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.allocator.AddressAllocator;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.allocator.EntryIdAllocator;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.coverage.MemoryAccessPathChooser;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.filter.FilterAccessThenMiss;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.loader.AddressAndEntry;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.loader.Load;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuAddressInstance;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuBuffer;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuBufferAccess;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuEntry;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuSubsystem;
import ru.ispras.microtesk.settings.GeneratorSettings;
import ru.ispras.microtesk.settings.RegionSettings;
import ru.ispras.microtesk.utils.BigIntegerUtils;
import ru.ispras.microtesk.utils.Range;
import ru.ispras.microtesk.utils.function.Predicate;

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
  /** Contains a reference to the memory subsystem specification. */
  private final MmuSubsystem memory = MmuPlugin.getSpecification();

  /** Memory access structure being processed. */
  private final MemoryAccessStructure structure;

  private final Map<MmuAddressInstance, Predicate<Long>> hitCheckers;
  private final MemoryAccessPathChooser normalPathChooser;
  private final MemoryAccessConstraints constraints;

  private final DataType alignType;

  private final AddressAllocator addressAllocator;
  private final EntryIdAllocator entryIdAllocator;

  /** Given a buffer, maps indices to sets of tags to be explicitly loaded into the buffer. */
  private final Map<MmuBuffer, Map<Long, Set<Long>>> bufferHitTags = new LinkedHashMap<>();
  /** Given a buffer, contains indices for which replacing sequences have been constructed. */
  private final Map<MmuBuffer, Set<Long>> bufferReplacedIndices = new LinkedHashMap<>();
  /** Given an access index, contains the buffer accesses having been processed. */
  private final Map<Integer, Set<MmuBufferAccess>> handledBufferAccesses = new LinkedHashMap<>();

  /** Current solution. */
  private MemorySolution solution;

  public MemorySolver(
      final MemoryAccessStructure structure,
      final AddressAllocator addressAllocator,
      final EntryIdAllocator entryIdAllocator,
      final Map<MmuAddressInstance, Predicate<Long>> hitCheckers,
      final MemoryAccessPathChooser normalPathChooser,
      final DataType alignType) {
    InvariantChecks.checkNotNull(memory);
    InvariantChecks.checkNotNull(structure);
    InvariantChecks.checkNotNull(addressAllocator);
    InvariantChecks.checkNotNull(entryIdAllocator);
    InvariantChecks.checkNotNull(hitCheckers);
    InvariantChecks.checkNotNull(normalPathChooser);

    this.structure = structure;
    this.addressAllocator = addressAllocator;
    this.entryIdAllocator = entryIdAllocator;
    this.hitCheckers = hitCheckers;
    this.normalPathChooser = normalPathChooser;
    this.constraints = MmuSettingsUtils.getConstraints();
    this.alignType = alignType;
  }

  @Override
  public SolverResult<MemorySolution> solve(final Mode mode) {
    solution = new MemorySolution(structure);

    SolverResult<MemorySolution> result = null;

    // Construct address objects.
    for (int j = 0; j < structure.size(); j++) {
      result = solve(j);

      if (result.getStatus() != SolverResult.Status.SAT) {
        Logger.debug("Solve[%d]: UNSAT", j);
        return result;
      }
    }

    // Fill the allocated entries.
    for (int j = 0; j < structure.size(); j++) {
      result = fill(j);

      if (result.getStatus() != SolverResult.Status.SAT) {
        Logger.debug("Fill[%d]: UNSAT", j);
        return result;
      }
    }

    Logger.debug("Solve: SAT");
    return result;
  }

 
  private RegionSettings chooseRegion() {
    final Set<RegionSettings> regions = new HashSet<>();
    final GeneratorSettings settings = GeneratorSettings.get();

    for (final RegionSettings region : settings.getMemory().getRegions()) {
      if (region.isEnabled() && region.getType() == RegionSettings.Type.DATA) {
        regions.add(region);
      }
    }

    return Randomizer.get().choose(regions);
  }

  /**
   * Solves the HIT constraint.
   * 
   * @param j the memory access index.
   * @param bufferAccess the buffer access under scrutiny.
   * @return the partial solution.
   */
  private SolverResult<MemorySolution> solveHitConstraint(
      final int j,
      final MmuBufferAccess bufferAccess) {

    Logger.debug("Solving the hit constraint for %s", bufferAccess);

    final MmuBuffer buffer = bufferAccess.getBuffer();
    final AddressObject addrObject = solution.getAddressObject(j);
    final MmuAddressInstance addrType = bufferAccess.getAddress();

    final long address = addrObject.getAddress(addrType);
    final long tag = buffer.getTag(address);

    final Set<Long> hitTags = getHitTags(buffer, address);

    // Check whether the preparation loading has been already scheduled.
    if (hitTags.contains(tag)) {
      // Doing the same thing twice is redundant.
      return new SolverResult<>(solution);
    }

    final BufferUnitedDependency dependency = structure.getUnitedDependency(j);
    final Set<Pair<Integer, BufferHazard.Instance>> tagEqualRelation =
        dependency.getTagEqualRelation(bufferAccess);

    // Check whether the previous instructions load the data into the buffer.
    if (!tagEqualRelation.isEmpty()) {
      // Preparation is not required.
      return new SolverResult<>(solution);
    }

    final Set<Pair<Integer, BufferHazard.Instance>> tagReplacedRelation =
        dependency.getTagReplacedRelation(bufferAccess);

    // Check whether there is a tag-replaced dependency.
    if (!tagReplacedRelation.isEmpty()) {
      // Ignore the hit constraint.
      return new SolverResult<>(solution);
    }

    // Check whether loading the data corrupts the preparation code.
    if (hitTags.size() >= buffer.getWays()) {
      // Loading the data will cause other useful data to be replaced.
      return new SolverResult<>(String.format("Hit constraint violation for buffer %s", buffer));
    }

    // Update the set of hit tags.
    hitTags.add(tag);

    // Add a memory access to cause a HIT.
    // DO NOT CHANGE OFFSET: there are buffers, in which offset bits have special meaning,
    // e.g. in the MIPS TLB, VA[12] chooses between EntryLo0 and EntryLo1.
    final List<Long> sequence = new ArrayList<>();
    sequence.add(address);

    solution.getLoader().addAddresses(buffer, BufferAccessEvent.HIT, address, sequence);

    // Loading data into the buffer may load them into the previous buffers.
    final MemoryAccess access = structure.getAccess(j);
    final MemoryAccessPath path = access.getPath();

    // TODO:
    final List<MmuBufferAccess> bufferAccesses = new ArrayList<>(path.getBufferReads());
    Logger.debug("Buffer reads: %s", bufferAccesses);

    // Scan the buffers of the same address type in reverse order.
    boolean found = false;
    for (int i = bufferAccesses.size() - 1; i >= 0; i--) {
      final MmuBufferAccess prevBufferAccess = bufferAccesses.get(i);

      if (!found) {
        found = (prevBufferAccess == bufferAccess);
        continue;
      }

      if (prevBufferAccess.getAddress() != buffer.getAddress()) {
        continue;
      }

      if (prevBufferAccess.getEvent() == BufferAccessEvent.MISS) {
        final SolverResult<MemorySolution> result = solveMissConstraint(j, prevBufferAccess);

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
   * @param j the memory access index.
   * @param bufferAccess the buffer access under scrutiny.
   * @return the partial solution.
   */
  private SolverResult<MemorySolution> solveMissConstraint(
      final int j,
      final MmuBufferAccess bufferAccess) {

    Logger.debug("Solving the miss constraint for %s", bufferAccess);

    final MmuBuffer buffer = bufferAccess.getBuffer();
    final BufferUnitedDependency dependency = structure.getUnitedDependency(j);

    if (!FilterAccessThenMiss.test(bufferAccess, dependency)) {
      return new SolverResult<>(String.format("Miss constraint violation for buffer %s", buffer));
    }

    final AddressObject addrObject = solution.getAddressObject(j);
    final MmuAddressInstance addrType = buffer.getAddress();

    final long address = addrObject.getAddress(addrType);
    final long tag = buffer.getTag(address);
    final long index = buffer.getIndex(address);

    final Set<Long> hitTags = getHitTags(buffer, address);

    if (hitTags.contains(tag)) {
      // Replacement does not make sense, because data will be loaded anyway.
      return new SolverResult<>(solution);
    }

    final Set<Pair<Integer, BufferHazard.Instance>> tagReplacedRelation =
        dependency.getTagReplacedRelation(bufferAccess);

    final Set<Long> replacedIndices = getReplacedIndices(buffer);

    // It is enough to use one replacing sequence for all test case instructions.
    if (!replacedIndices.contains(index)
        && (mayBeHit(j, buffer) || !tagReplacedRelation.isEmpty())) {
      final List<AddressAndEntry> sequence = new ArrayList<>();

      for (int i = 0; i < buffer.getWays(); i++) {
        final AddressAndEntry evictingAddressAndEntry =
            allocateAddrMissTagAndParentEntry(bufferAccess, address, chooseRegion(), false);

        sequence.add(evictingAddressAndEntry);
      }

      solution.getLoader().addAddressesAndEntries(
          buffer, BufferAccessEvent.MISS, address, sequence);
      replacedIndices.add(index);
    }

    return new SolverResult<>(solution);
  }

  /**
   * Solves the HIT constraint for the given non-replaceable buffer.
   * 
   * @param j the memory access index.
   * @param buffer access the buffer access under scrutiny.
   * @return the partial solution.
   */
  private SolverResult<MemorySolution> solveEntryConstraint(
      final int j,
      final MmuBufferAccess bufferAccess) {

    Logger.debug("Solving the entry constraint for %s", bufferAccess);

    final MmuBuffer buffer = bufferAccess.getBuffer();
    final AddressObject addrObject = solution.getAddressObject(j);
    final BufferUnitedDependency dependency = structure.getUnitedDependency(j);

    final Set<Pair<Integer, BufferHazard.Instance>> tagEqualRelation =
        dependency.getTagEqualRelation(bufferAccess);

    if (!tagEqualRelation.isEmpty()) {
      final int i = tagEqualRelation.iterator().next().first;
      final AddressObject prevAddrObject = solution.getAddressObject(i);

      // Instruction uses the same entry of the buffer (the map contains one entry).
      final Map<Long, EntryObject> entries = prevAddrObject.getEntries(bufferAccess);
      // Set the reference to the entry (filling is done when all dependencies are resolved).
      addrObject.setEntries(bufferAccess, entries);
    } else {
      // Check whether there are tag replace constraints.
      boolean tagReplaced = false;

      for (final MmuBufferAccess childAccess : bufferAccess.getChildAccesses()) {
        if (!dependency.getTagReplacedRelation(childAccess).isEmpty()) {
          tagReplaced = true;
          break;
        }
      }

      if (!tagReplaced) {
        if (addrObject.getEntries(bufferAccess) == null
            || addrObject.getEntries(bufferAccess).isEmpty()) {
          // Allocate an entry of the buffer.
          final Long bufferEntryId = allocateEntryId(buffer, false);
          final MmuEntry bufferEntry = new MmuEntry(buffer.getFields());

          if (bufferEntryId == null || bufferEntry == null) {
            return new SolverResult<>(
                String.format("Cannot allocate an entry for buffer %s", buffer));
          }

          // Filling the entry with appropriate data is done when all dependencies are resolved.
          final EntryObject entryObject = new EntryObject(bufferEntryId, bufferEntry);

          addrObject.addEntry(bufferAccess, entryObject);
          solution.addEntry(bufferAccess, entryObject);
        }
      }
    }

    return new SolverResult<>(solution);
  }

  /**
   * Solves the INDEX-EQUAL constraint ({@code INDEX[j] == INDEX[i]}).
   * 
   * @param j the memory access index.
   * @param bufferAccess the buffer access under scrutiny.
   * @return the partial solution.
   */
  private SolverResult<MemorySolution> solveIndexEqualConstraint(
      final int j,
      final MmuBufferAccess bufferAccess) {

    Logger.debug("Solving the index equality constraint for %s", bufferAccess);

    final BufferUnitedDependency dependency = structure.getUnitedDependency(j);
    final Set<Pair<Integer, BufferHazard.Instance>> indexEqualRelation =
        dependency.getIndexEqualRelation(bufferAccess);

    if (!indexEqualRelation.isEmpty()) {
      final Pair<Integer, BufferHazard.Instance> dependsOn = indexEqualRelation.iterator().next();
      final int i = dependsOn.first;
      final BufferHazard.Instance hazard = dependsOn.second;

      final AddressObject addrObject1 = solution.getAddressObject(i);
      final AddressObject addrObject2 = solution.getAddressObject(j);

      final MmuBufferAccess bufferAccess1 = hazard.getPrimaryAccess();
      final MmuBufferAccess bufferAccess2 = bufferAccess;

      final long addrValue1 = addrObject1.getAddress(bufferAccess1);
      final long addrValue2 = addrObject2.getAddress(bufferAccess2);

      final MmuBuffer buffer = bufferAccess.getBuffer();

      final long oldTag = buffer.getTag(addrValue2);
      final long oldIndex = buffer.getIndex(addrValue2);
      final long newIndex = buffer.getIndex(addrValue1);
      final long oldOffset = buffer.getOffset(addrValue2);

      // Copy the index from the previous instruction.
      final long newAddress = buffer.getAddress(oldTag, newIndex, oldOffset);

      // If the index has changed, allocate a new tag.
      final long newTag;

      if (newIndex != oldIndex) {
        final AddressAndEntry allocated = allocateAddrMissTagAndParentEntry(
            bufferAccess, newAddress, chooseRegion(), false);

        newTag = buffer.getTag(allocated.address);
      } else {
        newTag = oldTag;
      }

      addrObject2.setAddress(bufferAccess2, buffer.getAddress(newTag, newIndex, oldOffset));
    }

    return new SolverResult<>(solution);
  }

  /**
   * Solves the TAG-EQUAL constraint ({@code INDEX[j] == INDEX[i] && TAG[j] == TAG[i]}).
   * 
   * @param j the memory access index.
   * @param bufferAccess the buffer access under scrutiny.
   * @return the partial solution.
   */
  private SolverResult<MemorySolution> solveTagEqualConstraint(
      final int j,
      final MmuBufferAccess bufferAccess) {

    Logger.debug("Solving the tag equality constraint for %s", bufferAccess);

    final BufferUnitedDependency dependency = structure.getUnitedDependency(j);
    final Set<Pair<Integer, BufferHazard.Instance>> tagEqualRelation =
        dependency.getTagEqualRelation(bufferAccess);

    // Instruction uses the same tag and the same index as one of the previous instructions.
    if (!tagEqualRelation.isEmpty()) {
      final Pair<Integer, BufferHazard.Instance> dependsOn = tagEqualRelation.iterator().next();
      final int i = dependsOn.first;
      final BufferHazard.Instance hazard = dependsOn.second;

      final AddressObject addrObject1 = solution.getAddressObject(i);
      final AddressObject addrObject2 = solution.getAddressObject(j);

      final MmuBufferAccess bufferAccess1 = hazard.getPrimaryAccess();
      final MmuBufferAccess bufferAccess2 = bufferAccess;

      final long addrValue1 = addrObject1.getAddress(bufferAccess1);
      final long addrValue2 = addrObject2.getAddress(bufferAccess2);

      final MmuBuffer buffer = bufferAccess.getBuffer();

      // Copy the tag and the index from the previous instruction.
      final long newTag = buffer.getTag(addrValue1);
      final long newIndex = buffer.getIndex(addrValue1);
      final long oldOffset = buffer.getOffset(addrValue2);

      addrObject2.setAddress(bufferAccess2, buffer.getAddress(newTag, newIndex, oldOffset));
    }

    return new SolverResult<>(solution);
  }

  /**
   * Predicts replacements in the buffer (buffer) up to the {@code j} access and solve the
   * corresponding constraints.
   * 
   * @param j the memory access index.
   * @param bufferAccess the buffer access under scrutiny.
   * @return the partial solution.
   */
  private SolverResult<MemorySolution> solveTagReplacedConstraints(
      final int j,
      final MmuBufferAccess bufferAccess) {

    Logger.debug("Solving the tag replacement constraint for %s", bufferAccess);

    final MmuBuffer buffer = bufferAccess.getBuffer();
    final MmuAddressInstance addrType = bufferAccess.getAddress();

    final BufferStateTracker<Long> stateTracker = new BufferStateTracker<>(
        buffer.getSets(), buffer.getWays(), buffer.getAddressView());

    // Maps access indices to the replaced tags.
    final Map<Integer, Long> replacedTags =
        track(stateTracker, solution.getLoader().prepareLoads(addrType));

    for (int i = 0; i <= j; i++) {
      final MemoryAccess access = structure.getAccess(i);
      final MemoryAccessPath path = access.getPath();
      final BufferUnitedDependency dependency = structure.getUnitedDependency(i);
      final AddressObject addrObject = solution.getAddressObject(i);

      final long address = addrObject.getAddress(addrType);
      final long index = buffer.getIndex(address);
      final long offset = buffer.getOffset(address);

      // Check the buffer access condition.
      if (path.contains(buffer)) {
        final Long replacedTag = stateTracker.track(address);

        if (replacedTag != null) {
          replacedTags.put(i, replacedTag);
        }
      }

      // Satisfy the TAG-REPLACED constraint.
      final Set<Pair<Integer, BufferHazard.Instance>> tagReplacedRelation =
          dependency.getTagReplacedRelation(bufferAccess);

      if (!tagReplacedRelation.isEmpty()) {
        final int dependsOn = tagReplacedRelation.iterator().next().first;
        final Long replacedTag = replacedTags.get(dependsOn);

        if (replacedTag == null) {
          return new SolverResult<>(String.format("Replace constraint violation for %s", buffer));
        }

        addrObject.setAddress(addrType, buffer.getAddress(replacedTag, index, offset));

        if (buffer.isView()) { 
          final MmuBuffer parent = buffer.getBuffer();
          InvariantChecks.checkTrue(parent != null && !parent.isReplaceable());

          final MmuBufferAccess parentAccess = bufferAccess.getParentAccess();

          // Search for the entry in the parent buffer.
          boolean entryFound = false;
          for (final EntryObject entryObject : solution.getEntries(parentAccess).values()) {
            final long otherAddress;

            if (entryObject.isAuxiliary()) {
              // The entry is written to enable to initialize the buffer.
              final Collection<Load> loads = entryObject.getLoads();
              InvariantChecks.checkNotEmpty(loads);

              final Load load = loads.iterator().next();
              otherAddress = load.getAddress();
            } else {
              // This branch looks strange, because in this case the tag-equal hazard should exist.
              final Collection<AddressObject> addrObjects = entryObject.getAddrObjects();
              InvariantChecks.checkNotNull(addrObjects);

              final AddressObject otherAccess = addrObjects.iterator().next();
              otherAddress = otherAccess.getAddress(addrType);
            }

            final long otherTag = buffer.getTag(otherAddress);
            final long otherIndex = buffer.getIndex(otherAddress);

            if (otherIndex == index && otherTag == replacedTag) {
              // Set the reference to the entry from this address object (to be able to fill it).
              addrObject.addEntry(parentAccess, entryObject);

              entryFound = true;
              break;
            }
          }

          InvariantChecks.checkTrue(entryFound);
        }
      }

      // TAG-NOT-REPLACED constraints are satisfied AUTOMATICALLY.
    }

    return new SolverResult<>(solution);
  }

  /**
   * Solve hit/miss constraints specified for the given buffer.
   * 
   * @param j the memory access index.
   * @param bufferAccess the buffer access under scrutiny.
   * @return the partial solution.
   */
  private SolverResult<MemorySolution> solveBufferConstraint(
      final int j,
      final MmuBufferAccess bufferAccess) {

    Logger.debug("Solving the buffer constraint for %s", bufferAccess);

    // Do nothing if the buffer has been already handled.
    Set<MmuBufferAccess> handledBufferAccessesForExecution = handledBufferAccesses.get(j);

    if (handledBufferAccessesForExecution == null) {
      handledBufferAccesses.put(j, handledBufferAccessesForExecution = new LinkedHashSet<>());
    } else if (handledBufferAccessesForExecution.contains(bufferAccess)) {
      return new SolverResult<MemorySolution>(solution);
    }

    handledBufferAccessesForExecution.add(bufferAccess);

    // If the buffer access event is null, the situation is considered to be a hit.
    // The event is null, if the buffer is a parent of some view and is not in the access. 
    final BufferAccessEvent realEvent = bufferAccess.getEvent();
    final BufferAccessEvent usedEvent = realEvent == BufferAccessEvent.READ ? BufferAccessEvent.HIT : realEvent;

    final MmuBuffer buffer = bufferAccess.getBuffer();

    // The buffer is a view of another buffer (e.g., DTLB is a view of JTLB).
    if (buffer.isView()) {
      solveBufferConstraint(j, bufferAccess.getParentAccess());
    }

    // The parent access event is a hit or null, but not a miss.
    if (!buffer.isView() || bufferAccess.getParentAccess().getEvent() != BufferAccessEvent.MISS) {
      SolverResult<MemorySolution> result = null;

      if (buffer.isFake()) {
        return new SolverResult<MemorySolution>(solution);
      } else if (buffer.isReplaceable()) {
        // Construct a sequence of addresses to be accessed.
        if (usedEvent == BufferAccessEvent.HIT) {
          result = solveHitConstraint(j, bufferAccess);
        } else {
          result = solveMissConstraint(j, bufferAccess);
        }

        if (result.getStatus() != SolverResult.Status.UNSAT) {
          // External buffer accesses only.
          if (bufferAccess.getAddress().equals(buffer.getAddress())) {
            result = solveTagReplacedConstraints(j, bufferAccess);
          }
        }
      } else {
        // Construct a set of entries to be written to the buffer.
        if (usedEvent == BufferAccessEvent.HIT) {
          result = solveEntryConstraint(j, bufferAccess);
        } else {
          // Do nothing: the constraint is satisfied by tag allocators.
        }
      }

      if (result != null && result.getStatus() == SolverResult.Status.UNSAT) {
        return result;
      }
    } else {
      if (bufferAccess.getEvent() == BufferAccessEvent.HIT) {
        return new SolverResult<>(String.format("Constraint violation for buffer %s", buffer));
      }
    }

    return new SolverResult<MemorySolution>(solution);
  }

  /**
   * Handles the given instruction call (access) of the memory access structure.
   * 
   * @param j the memory access index.
   * @return the partial solution.
   */
  private SolverResult<MemorySolution> solve(final int j) {
    final MemoryAccess access = structure.getAccess(j);
    Logger.debug("Solve[%d]: %s", j, access);

    final BufferUnitedDependency dependency = structure.getUnitedDependency(j);

    // Construct the initial address object for the memory access.
    final AddressObject addrObject = new AddressObject(access);

    solution.setAddressObject(j, addrObject);

    // Assign the tag, index and offset according to the dependencies.
    final Map<MmuBufferAccess, BufferUnitedHazard> bufferHazards = dependency.getBufferHazards();

    for (Map.Entry<MmuBufferAccess, BufferUnitedHazard> bufferEntry : bufferHazards.entrySet()) {
      final MmuBufferAccess bufferAccess = bufferEntry.getKey();
      final Set<Pair<Integer, BufferHazard.Instance>> tagEqualRelation =
          dependency.getTagEqualRelation(bufferAccess);
      final Set<Pair<Integer, BufferHazard.Instance>> indexEqualRelation =
          dependency.getIndexEqualRelation(bufferAccess);

      if (!tagEqualRelation.isEmpty()) {
        solveTagEqualConstraint(j, bufferAccess);
      } else if (!indexEqualRelation.isEmpty()) {
        solveIndexEqualConstraint(j, bufferAccess);
      }
    }

    final MemoryAccessPath path = access.getPath();

    // Refine the addresses (in particular, assign the intermediate addresses).
    final boolean hasRefined = refineAddr(access, addrObject, true /* Apply constraints */);
    InvariantChecks.checkTrue(hasRefined, String.format("Infeasible path=%s", path));

    // FIXME: Divide solveBufferConstraint into non-replaceable/replaceable parts.

    // Solve the entry constraints.
    for (final MmuBufferAccess bufferAccess : path.getBufferReads()) {
      if (!bufferAccess.getBuffer().isReplaceable()) {
        final SolverResult<MemorySolution> result = solveBufferConstraint(j, bufferAccess);

        if (result.getStatus() == SolverResult.Status.UNSAT) {
          return result;
        }
      }
    }

    // Solve the hit/miss constraints as well as the replace dependencies.
    for (final MmuBufferAccess bufferAccess : path.getBufferChecks()) {
      if (bufferAccess.getBuffer().isReplaceable()) {
        final SolverResult<MemorySolution> result = solveBufferConstraint(j, bufferAccess);

        if (result.getStatus() == SolverResult.Status.UNSAT) {
          return result;
        }
      }
    }

    return new SolverResult<MemorySolution>(solution);
  }

  private SolverResult<MemorySolution> fill(final int j) {
    final MemoryAccess access = structure.getAccess(j);
    final AddressObject addrObject = solution.getAddressObject(j);
    final Map<MmuBufferAccess, Map<Long, EntryObject>> pathEntries = addrObject.getEntries();

    Logger.debug("Fill[%d]: %s", j, access);

    for (final MmuBufferAccess bufferAccess : pathEntries.keySet()) {
      Logger.debug("Fill[%d]: %s", j, bufferAccess);

      final Collection<EntryObject> entries = pathEntries.get(bufferAccess).values();

      for (final EntryObject entryObject : entries) {
        // Fill the entry according to the path constraints.
        final MmuEntry entry = entryObject.getEntry();
        fillEntry(bufferAccess, addrObject, entry);
      }
    }

    return new SolverResult<MemorySolution>(solution);
  }

  /**
   * Returns the set of tags to be explicitly loaded into the buffer to cause the hits.
   * 
   * @param buffer the memory buffer being accessed.
   * @param address the address.
   * @return the set of tags.
   */
  private Set<Long> getHitTags(final MmuBuffer buffer, final long address) {
    final long index = buffer.getIndex(address);

    Map<Long, Set<Long>> hitIndices = bufferHitTags.get(buffer);
    if (hitIndices == null) {
      bufferHitTags.put(buffer, hitIndices = new LinkedHashMap<>());
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
   * @param buffer the memory buffer being accessed.
   * @return the set of indices.
   */
  private Set<Long> getReplacedIndices(final MmuBuffer buffer) {
    Set<Long> replacedIndices = bufferReplacedIndices.get(buffer);
    if (replacedIndices == null) {
      bufferReplacedIndices.put(buffer, replacedIndices = new LinkedHashSet<>());
    }

    return replacedIndices;
  }

  /**
   * Checks whether a hit into the given buffer is possible for the given access.
   * 
   * @param j the memory access index.
   * @param buffer the memory buffer being accessed.
   * @return {@code false} if a hit is infeasible; {@code true} if a hit is possible.
   */
  private boolean mayBeHit(final int j, final MmuBuffer buffer) {
    final MmuAddressInstance addrType = buffer.getAddress();

    // TODO: This check can be optimized.
    final MemoryAccess access = structure.getAccess(j);
    final MemoryAccessPath path = access.getPath();

    // TODO: This is not accurate if addrType = VA, prevAddrType = PA. 
    for (final MmuAddressInstance prevAddrType : path.getAddressInstances()) {
      if (prevAddrType != addrType) {
        if (!solution.getLoader().prepareLoads(prevAddrType).isEmpty()) {
          // Possible HIT.
          return true;
        }
      }
    }

    final AddressObject addrObject = solution.getAddressObject(j);

    final long address = addrObject.getAddress(addrType);
    final long tag = buffer.getTag(address);
    final long index = buffer.getIndex(address);

    for (final Load load : solution.getLoader().prepareLoads(addrType)) {
      final long loadedAddress = load.getAddress();
      final long loadedTag = buffer.getTag(loadedAddress);
      final long loadedIndex = buffer.getIndex(loadedAddress);

      if (loadedIndex == index && loadedTag == tag) {
        // Possibly HIT.
        return true;
      }
    }

    // Definitely MISS.
    return false;
  }
  

  /**
   * Imitates multiple accesses to the buffer (updates the buffer state).
   * 
   * @param stateTracker the buffer state tracker.
   * @param loads the accesses to the buffer.
   * @return the map of load indices to replaced tags.
   */
  private Map<Integer, Long> track(
      final BufferStateTracker<Long> stateTracker, final List<Load> loads) {
    InvariantChecks.checkNotNull(stateTracker);
    InvariantChecks.checkNotNull(loads);

    final Map<Integer, Long> replacedTags = new LinkedHashMap<>();

    for (int i = 0; i < loads.size(); i++) {
      final Load load = loads.get(i);
      final Long replacedTag = stateTracker.track(load.getAddress());

      if (replacedTag != null) {
        replacedTags.put(i, replacedTag);
      }
    }

    return replacedTags;
  }

  /**
   * Allocates a tag for a replaceable buffer (e.g., a cache unit).
   * 
   * <p>It takes an address (a partial address with initialized index) and returns a tag such that
   * it does not belong to the buffer set (the set is determined by the index defined in the
   * address) and was not returned previously for that set.</p>
   */
  private long allocateAddrMissTag(
      final MmuBufferAccess bufferAccess,
      final long partialAddress, // Index and offset
      final Range<Long> region,
      final boolean peek) {
    final MmuBuffer buffer = bufferAccess.getBuffer();
    final Predicate<Long> hitChecker = hitCheckers.get(buffer.getAddress());

    while (true) {
      final long address = addressAllocator.allocateTag(
          buffer, partialAddress, region, peek, null);

      if (hitChecker == null || !hitChecker.test(address)) {
        return address;
      }
    }
  }

  private AddressAndEntry allocateAddrMissTagAndParentEntry(
      final MmuBufferAccess bufferAccess,
      final long partialAddress, // Index and offset
      final Range<Long> region,
      final boolean peek) {
    final MmuBuffer buffer = bufferAccess.getBuffer();

    // The buffer is not a view of another buffer.
    if (!buffer.isView()) {
      return new AddressAndEntry(allocateAddrMissTag(bufferAccess, partialAddress, region, peek));
    }

    // The buffer is a view of a non-replaceable buffer.
    final MmuBuffer parent = buffer.getParent();
    InvariantChecks.checkTrue(parent != null && !parent.isReplaceable());

    // Allocate a unique entry in the parent buffer.
    final Long id = allocateEntryId(parent, false);
    InvariantChecks.checkNotNull(id);

    final MemoryAccessType normalType = MemoryAccessType.LOAD(DataType.BYTE);

    Logger.debug("Getting normal paths: target=%s, buffer=%s",
        memory.getTargetBuffer(), parent);

    final MemoryAccessPath normalPath =
        normalPathChooser.get(BiasedConstraints.<MemoryAccessConstraints>SOFT(constraints));

    final MemoryAccess normalAccess = MemoryAccess.create(normalType, normalPath);
    InvariantChecks.checkNotNull(normalAccess);

    // Construct a valid address object.
    final AddressObject normalAddrObject = new AddressObject(normalAccess);

    // Refine the addresses (in particular, assign the intermediate addresses).
    final boolean hasRefined = refineAddr(
        MemoryAccess.create(MemoryAccessType.LOAD(DataType.BYTE /* Not important */), normalPath),
        normalAddrObject,
        false /* Do not apply constraints */);
    InvariantChecks.checkTrue(hasRefined, String.format("Infeasible path=%s", normalPath));

    // Construct the corresponding entry.
    final MmuEntry entry = new MmuEntry(parent.getFields());
    fillEntry(bufferAccess.getParentAccess(), normalAddrObject, entry);

    final EntryObject entryObject = new EntryObject(id, entry);
    solution.addEntry(bufferAccess.getParentAccess(), entryObject);

    return new AddressAndEntry(normalAddrObject.getAddress(bufferAccess.getAddress()), entryObject);
  }

  private long allocateEntryId(final MmuBuffer buffer, final boolean peek) {
    final long entryId = entryIdAllocator.allocate(buffer, peek, null);
    Logger.debug("Allocate entry: buffer=%s, entryId=%d", buffer, entryId);

    return entryId;
  }

  private boolean refineAddr(
      final MemoryAccess access,
      final AddressObject addrObject,
      final boolean applyConstraints) {
    final MemoryAccessPath path = access.getPath();

    final MemoryAccessRestrictor restrictor =
        new MemoryAccessRestrictor(
            access.getSegment(),
            access.getRegion(),
            applyConstraints
              ? constraints.getIntegers()
              : Collections.<IntegerConstraint<IntegerField>>emptyList()
        );

    for (final MmuBufferAccess bufferAccess : path.getBufferReads()) {
      restrictor.constrain(bufferAccess);
    }

    final Map<MmuAddressInstance, Long> addresses = addrObject.getAddresses();

    // Fix known values of the addresses.
    for (final Map.Entry<MmuAddressInstance, Long> entry : addresses.entrySet()) {
      final MmuAddressInstance address = entry.getKey();
      final Long value = entry.getValue();

      restrictor.constrain(address, BigIntegerUtils.valueOfUnsignedLong(value));
    }

    Logger.debug("Constraints for refinement: %s", restrictor.getConstraints());

    final Map<IntegerVariable, BigInteger> values =
        MemoryEngineUtils.generateData(
            path,
            restrictor.getConstraints(),
            IntegerVariableInitializer.RANDOM
        );

    // Cannot correct the address values.
    if (values == null) {
      Logger.debug("Cannot refine the address values");
      return false;
    }

    Logger.debug("Buffer reads: %s", path.getBufferReads());

    // Set the intermediate addresses used along the memory access path.
    for (final MmuBufferAccess bufferAccess : path.getBufferReads()) {
      final MmuAddressInstance addrType = bufferAccess.getAddress();
      final IntegerVariable addrVar = addrType.getVariable(); 

      final BigInteger addrValue = values.get(addrVar);
      InvariantChecks.checkNotNull(addrValue,
          String.format("Cannot obtain the address value for %s\n%s", bufferAccess, values));

      Logger.debug("Refine address: %s=0x%x", addrType, addrValue.longValue());
      addrObject.setAddress(addrType, addrValue.longValue());
    }

    return true;
  }

  /**
   * Fills the given entry with appropriate data produced on the basis of the memory access and
   * the address object.
   * 
   * @param bufferAccess the buffer access that uses the entry to be filled.
   * @param addrObject the address object.
   * @param entry the entry to be filled.
   */
  private void fillEntry(
      final MmuBufferAccess bufferAccess,
      final AddressObject addrObject,
      final MmuEntry entry) {
    InvariantChecks.checkNotNull(bufferAccess);
    InvariantChecks.checkNotNull(addrObject);
    InvariantChecks.checkNotNull(entry);

    final Map<MmuAddressInstance, Long> addresses = addrObject.getAddresses();
    final MemoryAccessPath path = addrObject.getPath();

    // Fix the known values of the addresses.
    final Collection<IntegerConstraint<IntegerField>> constraints =
        new ArrayList<>(this.constraints.getIntegers());

    for (final Map.Entry<MmuAddressInstance, Long> addrEntry : addresses.entrySet()) {
      final MmuAddressInstance addrType = addrEntry.getKey();

      final IntegerField variable = new IntegerField(addrType.getVariable());
      final BigInteger value = BigIntegerUtils.valueOfUnsignedLong(addrEntry.getValue());

      Logger.debug("Fill entry: %s=0x%s", variable, value.toString(16));
      constraints.add(new IntegerDomainConstraint<IntegerField>(variable, value));
    }

    Logger.debug("Constraints: %s", constraints);

    // TODO: if there are several buffers, multiple solver invocations are not required.

    // Use the effective memory access path to generate test data.
    final Map<IntegerVariable, BigInteger> values = MemoryEngineUtils.generateData(
        path, constraints, IntegerVariableInitializer.RANDOM);
    InvariantChecks.checkTrue(values != null && !values.isEmpty(), constraints.toString());

    // Set the entry fields.
    entry.setValid(true);
    Logger.debug("Entry address: %s=0x%x", bufferAccess, addrObject.getAddress(bufferAccess));
    entry.setAddress(addrObject.getAddress(bufferAccess));

    for (final IntegerVariable field : entry.getVariables()) {
      // If an entry field is not used in the path, it remains unchanged.
      if (values.containsKey(field) && !entry.isValid(field)) {
        entry.setValue(field, values.get(field), true);
      }
    }
  }
}
