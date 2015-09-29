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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.ispras.fortress.randomizer.Randomizer;
import ru.ispras.fortress.util.BitUtils;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.Logger;
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
import ru.ispras.microtesk.mmu.settings.MmuSettingsUtils;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryAccess;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryAccessPath;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryAccessStructure;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryAccessType;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryEngineUtils;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryUnitedDependency;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryUnitedHazard;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.allocator.AddressAllocator;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.allocator.EntryIdAllocator;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.filter.FilterAccessThenMiss;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.loader.AddressAndEntry;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.loader.Load;
import ru.ispras.microtesk.mmu.translator.coverage.CoverageExtractor;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuAddressType;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuBuffer;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuEntry;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuExpression;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuSegment;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuSubsystem;
import ru.ispras.microtesk.settings.AccessSettings;
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
  /** Memory access structures being processed. */
  private final MemoryAccessStructure structure;

  private final Map<MmuAddressType, Predicate<Long>> hitCheckers;
  private final Collection<IntegerConstraint<IntegerField>> constraints;

  private final long pageMask;
  private final DataType alignType;

  private final AddressAllocator addressAllocator;
  private final EntryIdAllocator entryIdAllocator;

  private final GeneratorSettings settings;

  /** Given a buffer, maps indices to sets of tags to be explicitly loaded into the buffer. */
  private final Map<MmuBuffer, Map<Long, Set<Long>>> bufferHitTags = new LinkedHashMap<>();
  /** Given a buffer, contains indices for which replacing sequences have been constructed. */
  private final Map<MmuBuffer, Set<Long>> bufferReplacedIndices = new LinkedHashMap<>();
  /** Given an access index, contains the buffers having been processed. */
  private final Map<Integer, Set<MmuBuffer>> handledBuffers = new LinkedHashMap<>();

  /** Contains a reference to the memory subsystem specification. */
  private final MmuSubsystem memory = MmuPlugin.getSpecification();

  /** Current solution. */
  private MemorySolution solution;

  public MemorySolver(
      final MemoryAccessStructure structure,
      final MemoryEngineContext context,
      final AddressAllocator addressAllocator,
      final EntryIdAllocator entryIdAllocator,
      final long pageMask,
      final DataType alignType,
      final GeneratorSettings settings) {
    InvariantChecks.checkNotNull(memory);
    InvariantChecks.checkNotNull(structure);
    InvariantChecks.checkNotNull(context);
    InvariantChecks.checkNotNull(addressAllocator);
    InvariantChecks.checkNotNull(entryIdAllocator);
    InvariantChecks.checkNotNull(settings);

    this.structure = structure;

    this.addressAllocator = addressAllocator;
    this.entryIdAllocator = entryIdAllocator;

    this.hitCheckers = context.getHitCheckers();
    this.constraints = MmuSettingsUtils.getConstraints(settings);

    this.pageMask = pageMask;
    this.alignType = alignType;

    this.settings = settings;
  }

  @Override
  public SolverResult<MemorySolution> solve() {
    solution = new MemorySolution(structure);

    SolverResult<MemorySolution> result = null;

    // Construct address objects.
    for (int j = 0; j < structure.size(); j++) {
      result = solve(j);

      if (result.getStatus() != SolverResult.Status.SAT) {
        return result;
      }
    }

    // Correct the address objects.
    for (int j = 0; j < structure.size(); j++) {
      result = correct(j);

      if (result.getStatus() != SolverResult.Status.SAT) {
        return result;
      }
    }

    // Fill the allocated entries.
    for (int j = 0; j < structure.size(); j++) {
      result = fill(j);

      if (result.getStatus() != SolverResult.Status.SAT) {
        return result;
      }
    }

    return result;
  }

  private RegionSettings chooseRegion() {
    final Set<RegionSettings> regions = new HashSet<>();

    for (final RegionSettings region : settings.getMemory().getRegions()) {
      if (region.isEnabled() && region.getType() == RegionSettings.Type.DATA) {
        regions.add(region);
      }
    }

    return Randomizer.get().choose(regions);
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
    final AddressObject addrObject = solution.getAddressObject(j);

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
    final long oldAddress = addrObject.getAddress(addrType);

    if (!maxDataType.isAligned(oldAddress)) {
      final long newAddress = maxDataType.align(oldAddress);
      addrObject.setAddress(addrType, newAddress);
    }

    return new SolverResult<>(solution);
  }

  /**
   * Solves the HIT constraint.
   * 
   * @param j the access index.
   * @param buffer the buffer under scrutiny.
   * @return the partial solution.
   */
  private SolverResult<MemorySolution> solveHitConstraint(final int j, final MmuBuffer buffer) {
    final AddressObject addrObject = solution.getAddressObject(j);
    final MmuAddressType addrType = buffer.getAddress();

    final long address = addrObject.getAddress(addrType);
    final long tag = buffer.getTag(address);

    final Set<Long> hitTags = getHitTags(buffer, address);

    // Check whether the preparation loading has been already scheduled.
    if (hitTags.contains(tag)) {
      // Doing the same thing twice is redundant.
      return new SolverResult<>(solution);
    }

    final MemoryUnitedDependency dependency = structure.getUnitedDependency(j);
    final Set<Integer> tagEqualRelation = dependency.getTagEqualRelation(buffer);

    // Check whether the previous instructions load the data into the buffer.
    if (!tagEqualRelation.isEmpty()) {
      // Preparation is not required.
      return new SolverResult<>(solution);
    }

    final Set<Integer> tagReplacedRelation = dependency.getTagReplacedRelation(buffer);

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
    // DO NOT CHANGE OFFSET: there are buffers, in which offset bits have special meaning, e.g. 
    // in the MIPS TLB, VA[12] chooses between EntryLo0 and EntryLo1.
    final List<Long> sequence = new ArrayList<>();
    sequence.add(address);

    solution.getLoader().addAddresses(buffer, BufferAccessEvent.HIT, address, sequence);

    // Loading data into the buffer may load them into the previous buffers.
    final MemoryAccess access = structure.getAccess(j);
    final MemoryAccessPath path = access.getPath();

    // TODO:
    final List<MmuBuffer> buffers = new ArrayList<>(path.getBuffers());

    // Scan the buffers of the same address type in reverse order.
    boolean found = false;
    for (int i = buffers.size() - 1; i >= 0; i--) {
      final MmuBuffer prevDevice = buffers.get(i);

      if (!found) {
        found = (prevDevice == buffer);
        continue;
      }

      if (prevDevice.getAddress() != buffer.getAddress()) {
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
   * @param buffer the buffer under scrutiny.
   * @return the partial solution.
   */
  private SolverResult<MemorySolution> solveMissConstraint(final int j, final MmuBuffer buffer) {
    final MemoryUnitedDependency dependency = structure.getUnitedDependency(j);

    if (!FilterAccessThenMiss.test(buffer, dependency)) {
      return new SolverResult<>(String.format("Miss constraint violation for buffer %s", buffer));
    }

    final AddressObject addrObject = solution.getAddressObject(j);
    final MmuAddressType addrType = buffer.getAddress();

    final long address = addrObject.getAddress(addrType);
    final long tag = buffer.getTag(address);
    final long index = buffer.getIndex(address);

    final Set<Long> hitTags = getHitTags(buffer, address);

    if (hitTags.contains(tag)) {
      // Replacement does not make sense, because data will be loaded anyway.
      return new SolverResult<>(solution);
    }

    final Set<Integer> tagReplacedRelation = dependency.getTagReplacedRelation(buffer);
    final Set<Long> replacedIndices = getReplacedIndices(buffer);

    // It is enough to use one replacing sequence for all test case instructions.
    if (!replacedIndices.contains(index)
        && (mayBeHit(j, buffer) || !tagReplacedRelation.isEmpty())) {
      final List<AddressAndEntry> sequence = new ArrayList<>();

      for (int i = 0; i < buffer.getWays(); i++) {
        final AddressAndEntry evictingAddressAndEntry =
            allocateAddrMissTagAndParentEntry(buffer, address, chooseRegion(), false);

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
   * @param j the access index.
   * @param buffer the buffer under scrutiny.
   * @return the partial solution.
   */
  private SolverResult<MemorySolution> solveEntryConstraint(final int j, final MmuBuffer buffer) {
    final AddressObject addrObject = solution.getAddressObject(j);
    final MemoryUnitedDependency dependency = structure.getUnitedDependency(j);

    final Set<Integer> tagEqualRelation = dependency.getTagEqualRelation(buffer);

    if (!tagEqualRelation.isEmpty()) {
      final int i = tagEqualRelation.iterator().next();
      final AddressObject prevAddrObject = solution.getAddressObject(i);

      // Instruction uses the same entry of the buffer (the map contains one entry).
      final Map<Long, EntryObject> entries = prevAddrObject.getEntries(buffer);
      // Set the reference to the entry (filling is done when all dependencies are resolved).
      addrObject.setEntries(buffer, entries);
    } else {
      // Check whether there are tag replace constraints.
      boolean tagReplaced = false;

      for (final MmuBuffer child : buffer.getChildren()) {
        if (!dependency.getTagReplacedRelation(child).isEmpty()) {
          tagReplaced = true;
          break;
        }
      }

      if (!tagReplaced) {
        if (addrObject.getEntries(buffer) == null || addrObject.getEntries(buffer).isEmpty()) {
          // Allocate an entry of the buffer.
          final Long bufferEntryId = allocateEntryId(buffer, false);
          final MmuEntry bufferEntry = new MmuEntry(buffer.getFields());
  
          if (bufferEntryId == null || bufferEntry == null) {
            return new SolverResult<>(
                String.format("Cannot allocate an entry for buffer %s", buffer));
          }

          // Filling the entry with appropriate data is done when all dependencies are resolved.
          final EntryObject entryObject = new EntryObject(bufferEntryId, bufferEntry);

          addrObject.addEntry(buffer, entryObject);
          solution.addEntry(buffer, entryObject);
        }
      }
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
    final AddressObject addrObject = solution.getAddressObject(j);

    final MemoryUnitedDependency dependency = structure.getUnitedDependency(j);
    final Set<Integer> addrEqualRelation = dependency.getAddrEqualRelation(addrType);

    // The instruction uses the same address as one of the previous instructions.
    if (!addrEqualRelation.isEmpty()) {
      final int i = addrEqualRelation.iterator().next();
      final AddressObject prevAddrObject = solution.getAddressObject(i);

      final long newAddress = prevAddrObject.getAddress(addrType);

      // Copy the address from the previous instruction.
      addrObject.setAddress(addrType, newAddress);
    }

    return new SolverResult<>(solution);
  }

  /**
   * Solves the INDEX-EQUAL constraint ({@code INDEX[j] == INDEX[i]}).
   * 
   * @param j the access index.
   * @param buffer the buffer under scrutiny.
   * @return the partial solution.
   */
  private SolverResult<MemorySolution> solveIndexEqualConstraint(
      final int j, final MmuBuffer buffer) {
    final AddressObject addrObject = solution.getAddressObject(j);
    final MmuAddressType addrType = buffer.getAddress();

    final MemoryUnitedDependency dependency = structure.getUnitedDependency(j);
    final Set<Integer> indexEqualRelation = dependency.getIndexEqualRelation(buffer);

    if (!indexEqualRelation.isEmpty()) {
      final int i = indexEqualRelation.iterator().next();
      final AddressObject prevAddrObject = solution.getAddressObject(i);

      final long oldTag = buffer.getTag(addrObject.getAddress(addrType));
      final long oldIndex = buffer.getIndex(addrObject.getAddress(addrType));
      final long newIndex = buffer.getIndex(prevAddrObject.getAddress(addrType));
      final long oldOffset = buffer.getOffset(addrObject.getAddress(addrType));

      // Copy the index from the previous instruction.
      final long newAddress = buffer.getAddress(oldTag, newIndex, oldOffset);

      // If the index has changed, allocate a new tag.
      final long newTag;

      if (newIndex != oldIndex) {
        final AddressAndEntry allocated =
            allocateAddrMissTagAndParentEntry(buffer, newAddress, chooseRegion(), false);

        newTag = buffer.getTag(allocated.address);
      } else {
        newTag = oldTag;
      }

      addrObject.setAddress(addrType, buffer.getAddress(newTag, newIndex, oldOffset));
    }

    return new SolverResult<>(solution);
  }

  /**
   * Solves the TAG-EQUAL constraint ({@code INDEX[j] == INDEX[i] && TAG[j] == TAG[i]}).
   * 
   * @param j the access index.
   * @param buffer the buffer under scrutiny.
   * @return the partial solution.
   */
  private SolverResult<MemorySolution> solveTagEqualConstraint(
      final int j, final MmuBuffer buffer) {
    final AddressObject addrObject = solution.getAddressObject(j);
    final MmuAddressType addrType = buffer.getAddress();

    final MemoryUnitedDependency dependency = structure.getUnitedDependency(j);
    final Set<Integer> tagEqualRelation = dependency.getTagEqualRelation(buffer);

    // Instruction uses the same tag and the same index as one of the previous instructions.
    if (!tagEqualRelation.isEmpty()) {
      final int i = tagEqualRelation.iterator().next();
      final AddressObject prevAddrObject = solution.getAddressObject(i);

      // Copy the tag and the index from the previous instruction.
      final long newTag = buffer.getTag(prevAddrObject.getAddress(addrType));
      final long newIndex = buffer.getIndex(prevAddrObject.getAddress(addrType));
      final long oldOffset = buffer.getOffset(addrObject.getAddress(addrType));

      addrObject.setAddress(addrType, buffer.getAddress(newTag, newIndex, oldOffset));
    }

    return new SolverResult<>(solution);
  }

  /**
   * Predicts replacements in the buffer (buffer) up to the {@code j} access and solve the
   * corresponding constraints.
   * 
   * @param j the access index.
   * @param buffer the buffer under scrutiny.
   * @return the partial solution.
   */
  private SolverResult<MemorySolution> solveTagReplacedConstraints(
      final int j, final MmuBuffer buffer) {
    final MmuAddressType addrType = buffer.getAddress();

    final BufferStateTracker<Long> stateTracker = new BufferStateTracker<>(
        buffer.getSets(), buffer.getWays(), buffer.getAddressView());

    // Maps access indices to the replaced tags.
    final Map<Integer, Long> replacedTags =
        track(stateTracker, solution.getLoader().prepareLoads(addrType));

    for (int i = 0; i <= j; i++) {
      final MemoryAccess access = structure.getAccess(i);
      final MemoryAccessPath path = access.getPath();
      final MemoryUnitedDependency dependency = structure.getUnitedDependency(i);
      final AddressObject addrObject = solution.getAddressObject(i);

      final long address = addrObject.getAddress(addrType);
      final long index = buffer.getIndex(address);
      final long offset = buffer.getOffset(address);

      // Check the buffer access condition.
      if (path.contains(buffer) && buffer.checkGuard(access)) {
        final Long replacedTag = stateTracker.track(address);

        if (replacedTag != null) {
          replacedTags.put(i, replacedTag);
        }
      }

      // Satisfy the TAG-REPLACED constraint.
      final Set<Integer> tagReplacedRelation = dependency.getTagReplacedRelation(buffer);

      if (!tagReplacedRelation.isEmpty()) {
        final int dependsOn = tagReplacedRelation.iterator().next();
        final Long replacedTag = replacedTags.get(dependsOn);

        if (replacedTag == null) {
          return new SolverResult<>(String.format("Replace constraint violation for %s", buffer));
        }

        addrObject.setAddress(addrType, buffer.getAddress(replacedTag, index, offset));

        if (buffer.isView()) { 
          final MmuBuffer parent = buffer.getParent();
          InvariantChecks.checkTrue(parent != null && !parent.isReplaceable());

          // Search for the entry in the parent buffer.
          boolean entryFound = false;
          for (final EntryObject entryObject : solution.getEntries(parent).values()) {
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
              addrObject.addEntry(parent, entryObject);

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
   * @param j the access index.
   * @param buffer the buffer under scrutiny.
   * @return the partial solution.
   */
  private SolverResult<MemorySolution> solveBufferConstraint(
      final int j, final MmuBuffer buffer) {
    // Do nothing if the buffer has been already handled.
    Set<MmuBuffer> handledBuffersForExecution = handledBuffers.get(j);

    if (handledBuffersForExecution == null) {
      handledBuffers.put(j, handledBuffersForExecution = new LinkedHashSet<>());
    } else if (handledBuffersForExecution.contains(buffer)) {
      return new SolverResult<MemorySolution>(solution);
    }

    handledBuffersForExecution.add(buffer);

    final MemoryAccess access = structure.getAccess(j);
    final MemoryAccessPath path = access.getPath();

    // If the buffer access event is null, the situation is considered to be a hit.
    // The event is null, if the buffer is a parent of some view and is not in the access. 
    final BufferAccessEvent realEvent = path.getEvent(buffer);
    final BufferAccessEvent usedEvent = realEvent == null ? BufferAccessEvent.HIT : realEvent;

    // The buffer is a view of another buffer (e.g., DTLB is a view of JTLB).
    if (buffer.isView()) {
      solveBufferConstraint(j, buffer.getParent());
    }

    final boolean canBeAccessed =
        // The parent access event is a hit or null, but not a miss.
        !buffer.isView() || path.getEvent(buffer.getParent()) != BufferAccessEvent.MISS;

    if (canBeAccessed && buffer.checkGuard(access)) {
      SolverResult<MemorySolution> result = null;

      if (buffer.isReplaceable()) {
        // Construct a sequence of addresses to be accessed.
        if (usedEvent == BufferAccessEvent.HIT) {
          result = solveHitConstraint(j, buffer);
        } else {
          result = solveMissConstraint(j, buffer);
        }

        if (result.getStatus() != SolverResult.Status.UNSAT) {
          result = solveTagReplacedConstraints(j, buffer);
        }
      } else {
        // Construct a set of entries to be written to the buffer.
        if (usedEvent == BufferAccessEvent.HIT) {
          result = solveEntryConstraint(j, buffer);
        } else {
          // Do nothing: the constraint is satisfied by tag allocators.
        }
      }

      if (result != null && result.getStatus() == SolverResult.Status.UNSAT) {
        return result;
      }
    } else {
      if (path.getEvent(buffer) == BufferAccessEvent.HIT) {
        return new SolverResult<>(String.format("Constraint violation for buffer %s", buffer));
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

    // Construct the initial address object for the access.
    final AddressObject addrObject = constructAddr(access, true);
    solution.setAddressObject(j, addrObject);

    // Align the addresses.
    for (final MmuAddressType addrType : addrObject.getAddresses().keySet()) {
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
        final long addr = addrObject.getAddress(addrType);

        if (!addrObject.getType().getDataType().isAligned(addr)) {
          throw new IllegalStateException(
              String.format("Unaligned address after solving AddrEqual constraints: %x", addr));
        }
      } else {
        final Map<MmuBuffer, MemoryUnitedHazard> bufferHazards =
            dependency.getDeviceHazards(addrType);

        for (Map.Entry<MmuBuffer, MemoryUnitedHazard> bufferEntry : bufferHazards.entrySet()) {
          final MmuBuffer bufferType = bufferEntry.getKey();
          final Set<Integer> tagEqualRelation = dependency.getTagEqualRelation(bufferType);
          final Set<Integer> indexEqualRelation = dependency.getIndexEqualRelation(bufferType);

          if (!tagEqualRelation.isEmpty()) {
            solveTagEqualConstraint(j, bufferType);
          } else if (!indexEqualRelation.isEmpty()) {
            solveIndexEqualConstraint(j, bufferType);
          }
        }
      }
    }

    // Solve the hit and miss constraints for the buffers as well as the replace dependencies.
    for (final MmuBuffer buffer : path.getBuffers()) {
      final SolverResult<MemorySolution> result = solveBufferConstraint(j, buffer);

      if (result.getStatus() == SolverResult.Status.UNSAT) {
        return result;
      }
    }

    return new SolverResult<MemorySolution>(solution);
  }

  private SolverResult<MemorySolution> correct(final int j) {
    final MemoryAccess access = structure.getAccess(j);
    final MemoryAccessPath path = access.getPath();
    final AddressObject addrObject = solution.getAddressObject(j);

    // Satisfying dependencies may lead to changing the memory access path.
    if (correctAddr(addrObject)) {
      final Collection<MemoryAccessPath> paths =
          CoverageExtractor.get().getEnabledPaths(memory, access.getType(), settings);
      final Collection<MemoryAccessPath> variants =
          MemoryEngineUtils.getFeasibleSimilarPaths(path, paths, constraints);

      boolean pathFound = false;

      if (!(pathFound = refineAddr(path, addrObject, true))) {
        for (final MemoryAccessPath variant : variants) {
          if (path == variant) {
            continue;
          }

          if (pathFound = refineAddr(variant, addrObject, true)) {
            // Update the memory access path in the address object.
            addrObject.setPath(variant);
            break;
          }
        }
      }

      if (!pathFound) {
        return new SolverResult<MemorySolution>(
            String.format("No feasible variant found: %s", variants));
      }
    }

    return new SolverResult<MemorySolution>(solution);
  }

  private SolverResult<MemorySolution> fill(final int j) {
    final AddressObject addrObject = solution.getAddressObject(j);
    final Map<MmuBuffer, Map<Long, EntryObject>> pathEntries = addrObject.getEntries();

    for (final MmuBuffer buffer : pathEntries.keySet()) {
      for (final EntryObject entryObject : pathEntries.get(buffer).values()) {
        // Fill the entry according to the path constraints.
        final MmuEntry entry = entryObject.getEntry();
        fillEntry(addrObject, entry);
      }
    }

    return new SolverResult<MemorySolution>(solution);
  }

  /**
   * Returns the set of tags to be explicitly loaded into the buffer to cause the hits.
   * 
   * @param buffer the MMU buffer (buffer).
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
   * @param buffer the MMU buffer (buffer).
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
   * @param j the access index.
   * @param buffer the MMU buffer (buffer).
   * @return {@code false} if a hit is infeasible; {@code true} if a hit is possible.
   */
  private boolean mayBeHit(final int j, final MmuBuffer buffer) {
    final MmuAddressType addrType = buffer.getAddress();

    // TODO: This check can be optimized.
    final MemoryAccess access = structure.getAccess(j);
    final MemoryAccessPath path = access.getPath();

    // TODO: This is not accurate if addrType = VA, prevAddrType = PA. 
    for (final MmuAddressType prevAddrType : path.getAddresses()) {
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

  private long allocateAddr(
      final MmuAddressType addrType,
      final Range<Long> region,
      final boolean peek) {
    InvariantChecks.checkNotNull(addrType);
    Logger.debug("Allocate address: %s", addrType);

    final long significantBitsMask = addressAllocator.getSignificatBitsMask(addrType);
    Logger.debug("Significant bits: %x", significantBitsMask);

    final long insignificantBits = Randomizer.get().nextLong()
        & ~significantBitsMask
        & BitUtils.getLongMask(addrType.getWidth());

    return allocateAddr(addrType, insignificantBits, region, peek);
  }

  private long allocateAddr(
      final MmuAddressType addrType,
      final long partialAddress, // Offset
      final Range<Long> region,
      final boolean peek) {
    InvariantChecks.checkNotNull(addrType);
    Logger.debug("Partial address: %x", partialAddress);

    final Predicate<Long> hitChecker = hitCheckers.get(addrType);

    while (true) {
      final long address =
          addressAllocator.allocateAddress(addrType, partialAddress, region, peek);

      if (hitChecker == null || !hitChecker.test(address)) {
        Logger.debug("Allocated address: %x", address);
        return address;
      }
    }
  }

  /**
   * Allocates a tag for a replaceable buffer (e.g., a cache unit).
   * 
   * <p>It takes an address (a partial address with initialized index) and returns a tag such that
   * it does not belong to the buffer set (the set is determined by the index defined in the
   * address) and was not returned previously for that set.</p>
   */
  private long allocateAddrMissTag(
      final MmuBuffer buffer,
      final long partialAddress, // Index and offset
      final Range<Long> region,
      final boolean peek) {
    InvariantChecks.checkNotNull(buffer);

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
      final MmuBuffer buffer,
      final long partialAddress, // Index and offset
      final Range<Long> region,
      final boolean peek) {
    InvariantChecks.checkNotNull(buffer);

    // The buffer is not a view of another buffer.
    if (!buffer.isView()) {
      return new AddressAndEntry(allocateAddrMissTag(buffer, partialAddress, region, peek));
    }

    // The buffer is a view of a non-replaceable buffer.
    final MmuBuffer parent = buffer.getParent();
    InvariantChecks.checkTrue(parent != null && !parent.isReplaceable());

    // Allocate a unique entry in the parent buffer.
    final Long id = allocateEntryId(parent, false);
    InvariantChecks.checkNotNull(id);

    final MemoryAccessType normalType = MemoryAccessType.LOAD(DataType.BYTE);

    // Select normal paths that affect the parent buffer.
    final Collection<MemoryAccessPath> normalPaths =
        CoverageExtractor.get().getNormalPaths(memory, parent, settings);
    // The normal paths satisfying the user-defined constraints are of greater priority.
    final Collection<MemoryAccessPath> bestPaths =
        MemoryEngineUtils.getFeasiblePaths(normalPaths, constraints);

    final MemoryAccessPath normalPath = Randomizer.get().choose(
        bestPaths.isEmpty() ? normalPaths : bestPaths);

    final MemoryAccess normalAccess = MemoryAccess.create(normalType, normalPath);
    InvariantChecks.checkNotNull(normalAccess);

    // Construct a valid address object.
    final AddressObject normalAddrObject = constructAddr(normalAccess, !bestPaths.isEmpty());

    // Construct the corresponding entry.
    final MmuEntry entry = new MmuEntry(parent.getFields());
    fillEntry(normalAddrObject, entry);

    final EntryObject entryObject = new EntryObject(id, entry);
    solution.addEntry(parent, entryObject);

    return new AddressAndEntry(normalAddrObject.getAddress(buffer.getAddress()), entryObject);
  }

  private long allocateEntryId(final MmuBuffer buffer, final boolean peek) {
    return entryIdAllocator.allocate(buffer, peek, null);
  }

  private AddressObject constructAddr(final MemoryAccess access, final boolean applyConstraints) {
    InvariantChecks.checkNotNull(access);

    final MmuSubsystem memory = MmuPlugin.getSpecification();

    final MmuAddressType vaType = memory.getVirtualAddress();
    final MmuAddressType paType = memory.getPhysicalAddress();

    final AddressObject addrObject = new AddressObject(access);

    final RegionSettings region = access.getRegion(); // PA constraint.
    final MmuSegment segment = access.getSegment();   // VA constraint.

    // Allocate physical and virtual addresses.
    long pa = allocateAddr(paType, region, false);
    long va = segment.isMapped() ? allocateAddr(vaType, 0, segment, false) : segment.getVa(pa);

    // The address should be aligned not to cause the exception.
    pa = addrObject.getType().getDataType().align(pa);

    // Align the addresses if the option is set.
    if (alignType != null) {
      pa = alignType.align(pa);
    }

    addrObject.setAddress(vaType, va);
    addrObject.setAddress(paType, pa);

    final boolean done = refineAddr(access.getPath(), addrObject, applyConstraints);
    InvariantChecks.checkTrue(done, String.format("Infeasible path=%s", access.getPath()));

    return addrObject;
  }

  /**
   * Returns {@code true} if success.
   */
  private boolean refineAddr(
      final MemoryAccessPath path,
      final AddressObject addrObject,
      final boolean applyConstraints) {
    InvariantChecks.checkNotNull(path);
    InvariantChecks.checkNotNull(addrObject);

    correctOffset(addrObject);

    final MmuAddressType vaType = memory.getVirtualAddress();
    InvariantChecks.checkNotNull(vaType, "Virtual address type is null");

    final MmuAddressType paType = memory.getPhysicalAddress();
    InvariantChecks.checkNotNull(paType, "Physical address type is null");

    final IntegerVariable vaVar = vaType.getVariable();
    final IntegerVariable paVar = paType.getVariable();

    long va = addrObject.getAddress(vaType);
    long pa = addrObject.getAddress(paType);
    Logger.debug("Refine address: VA=%x, PA=%x", va, pa);

    // Fix the address tags, indices and page offsets.
    final Map<IntegerField, BigInteger> knownValues = new LinkedHashMap<>();

    for (final MmuBuffer buffer : path.getBuffers()) {
      final MmuAddressType addrType = buffer.getAddress();
      final long address = addrObject.getAddress(addrType);

      final MmuExpression tagExpr = buffer.getTagExpression();
      InvariantChecks.checkNotNull(tagExpr, "Tag expression is null");

      final long tag = buffer.getTag(address);
      knownValues.putAll(
          IntegerField.split(tagExpr.getTerms(), BigIntegerUtils.valueOfUnsignedLong(tag)));

      final MmuExpression indexExpr = buffer.getIndexExpression();
      InvariantChecks.checkNotNull(indexExpr, "Index expression is null");

      final long index = buffer.getIndex(address);
      knownValues.putAll(
          IntegerField.split(indexExpr.getTerms(), BigIntegerUtils.valueOfUnsignedLong(index)));
    }

    final BigInteger pageMaskValue = BigIntegerUtils.valueOfUnsignedLong(pageMask);
    knownValues.put(IntegerField.create(vaVar, pageMaskValue),
        BigIntegerUtils.valueOfUnsignedLong(pa & pageMask));
    knownValues.put(IntegerField.create(paVar, pageMaskValue),
        BigIntegerUtils.valueOfUnsignedLong(pa & pageMask));

    final Collection<IntegerConstraint<IntegerField>> constraints = applyConstraints ?
        new ArrayList<>(this.constraints) : new ArrayList<IntegerConstraint<IntegerField>>();

    for (final Map.Entry<IntegerField, BigInteger> entry : knownValues.entrySet()) {
      final IntegerField field = entry.getKey();
      final BigInteger value = entry.getValue();

      constraints.add(new IntegerDomainConstraint<IntegerField>(field, value));
    }

    // It is important to fill unused fields with zeros.
    Map<IntegerVariable, BigInteger> values = MemoryEngineUtils.generateData(
        path, constraints, IntegerVariableInitializer.ZEROS);

    if (values == null) {
      return false;
    }

    // Correct the address values.
    va |= values.get(vaVar).longValue();
    pa |= values.get(paVar).longValue();

    addrObject.setAddress(vaType, va);
    addrObject.setAddress(paType, pa);

    // Set the attributes to be used in an adapter (if required).
    addrObject.clearAttrs();

    for (final Map.Entry<IntegerVariable, BigInteger> attribute : values.entrySet()) {
      final IntegerVariable key = attribute.getKey();
      final BigInteger value = attribute.getValue();

      if (!key.equals(vaVar) && !key.equals(paVar)) {
        addrObject.setAttrValue(key, value.longValue());
      }
    }

    return true;
  }

  private void correctOffset(final AddressObject addrObject) {
    InvariantChecks.checkNotNull(addrObject);

    final MmuAddressType vaType = memory.getVirtualAddress();
    final MmuAddressType paType = memory.getPhysicalAddress();

    final long va = addrObject.getAddress(vaType);
    final long pa = addrObject.getAddress(paType);

    addrObject.setAddress(vaType, (va & ~pageMask) | (pa & pageMask));
  }

  /**
   * Avoids inconsistencies that may appear during the constraint solving.
   * 
   * Returns {@code true} if the address has been corrected.
   */
  private boolean correctAddr(final AddressObject addrObject) {
    InvariantChecks.checkNotNull(addrObject);

    correctOffset(addrObject);

    final MmuAddressType vaType = memory.getVirtualAddress();
    final MmuAddressType paType = memory.getPhysicalAddress();

    long va = addrObject.getAddress(vaType);
    long pa = addrObject.getAddress(paType);

    boolean addressCorrected = false;
    boolean regionFound = false;

    for (final RegionSettings region : settings.getMemory().getRegions()) {
      if (!region.isEnabled() || region.getType() != RegionSettings.Type.DATA) {
        continue;
      }

      if (region.checkAddress(pa)) {
        boolean vaOk = false;

        final Collection<MmuSegment> segments = new ArrayList<>();

        for (final AccessSettings regionAccess : region.getAccesses()) {
          final MmuSegment segment = memory.getSegment(regionAccess.getSegment());
          InvariantChecks.checkNotNull(segment);

          if (segment.checkVa(va)) {
            // The segment may remain unchanged (it can represent the physical address).
            if (segment.isMapped()) {
              // The virtual address may remain unchanged.
              vaOk = true;
            } else {
              // The virtual address should be recalculated.
              segments.clear();
              segments.add(segment);
            }
            break;
          }

          // It is assumed that mapped segments cover the entire physical memory.
          // The goal is to choose an appropriate unmapped segment. 
          if(!segment.isMapped()) {
            segments.add(segment);
          }
        }

        if (!vaOk) {
          // The virtual address should be recalculated.
          final MmuSegment segment = Randomizer.get().choose(segments);

          // An adapter should take into account additional attributes (e.g., CP in XKPHYS).
          va = segment.checkVa(va) ? segment.getVa(pa, segment.getRest(va)) : segment.getVa(pa);
          addrObject.setAddress(vaType, va);

          addressCorrected = true;
        }

        regionFound = true;
        break;
      }
    }

    InvariantChecks.checkTrue(regionFound);
    return addressCorrected;
  }

  /**
   * Fills the given entry with appropriate data produced on the basis of the memory access and
   * the address object.
   * 
   * @param addrObject the address object.
   * @param entry the entry to be filled.
   */
  private void fillEntry(final AddressObject addrObject, final MmuEntry entry) {
    InvariantChecks.checkNotNull(addrObject);
    InvariantChecks.checkNotNull(entry);

    final Map<MmuAddressType, Long> addresses = addrObject.getAddresses();
    final MemoryAccessPath path = addrObject.getPath();

    // Fix the known values of the addresses.
    final Collection<IntegerConstraint<IntegerField>> constraints =
        new ArrayList<>(this.constraints);

    for (final Map.Entry<MmuAddressType, Long> addrEntry : addresses.entrySet()) {
      final MmuAddressType addrType = addrEntry.getKey();
      final long address = addrEntry.getValue();

      constraints.add(new IntegerDomainConstraint<IntegerField>(
          new IntegerField(addrType.getVariable()), BigIntegerUtils.valueOfUnsignedLong(address)));
    }

    // Use the effective memory access path to generate test data.
    final Map<IntegerVariable, BigInteger> values = MemoryEngineUtils.generateData(
        path, constraints, IntegerVariableInitializer.ZEROS /* IntegerVariableInitializer.RANDOM */);
    InvariantChecks.checkTrue(values != null && !values.isEmpty());

    // Set the entry fields.
    entry.setValid(true);
    for (final IntegerVariable field : entry.getVariables()) {
      // If an entry field is not used in the path it remains unchanged.
      if (values.containsKey(field) && (!entry.isValid(field) || path.contains(field))) {
        entry.setValue(field, values.get(field), path.contains(field));
      }
    }
  }
}
