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
import ru.ispras.microtesk.basis.solver.Solver;
import ru.ispras.microtesk.basis.solver.SolverResult;
import ru.ispras.microtesk.mmu.basis.BufferAccessEvent;
import ru.ispras.microtesk.mmu.basis.BufferStateTracker;
import ru.ispras.microtesk.mmu.basis.DataType;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryAccess;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryAccessPath;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryAccessStructure;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryUnitedDependency;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryUnitedHazard;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.filter.FilterAccessThenMiss;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.loader.Load;
import ru.ispras.microtesk.mmu.translator.MmuTranslator;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuAddressType;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuBuffer;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuEntry;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuSubsystem;
import ru.ispras.microtesk.utils.function.BiConsumer;
import ru.ispras.microtesk.utils.function.Function;
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
   * Refers to the address object constructor.
   * 
   * <p>A address object constructor is a user-defined function that maps a memory access (an object
   * of {@link MemoryAccess}) to the address object (an object of {@link AddressObject}).</p>
   */
  private final Function<MemoryAccess, AddressObject> addrObjectConstructors;

  /**
   * Refers to the address object corrector.
   * 
   * <p>A address object corrector is a user-defined function that corrects inconsistencies in
   * address object (an object of {@link AddressObject}) after solving the constraints.</p>
   */
  private final BiConsumer<MemoryAccess, AddressObject> addrObjectCorrectors;

  /**
   * Given a replaceable buffer (e.g., a cache unit), contains the tag allocator.
   * Given a non-replaceable buffer (e.g., a translation table), contains the entry id allocator.
   * 
   * <p>A tag allocator is a user-defined function (with a side effect) that takes an address
   * (a partial address with initialized index) and returns a tag such that is does not belong to
   * the buffer set (the set is determined by the index defined in the address) and was not
   * returned previously for that set.</p>
   * 
   * <p>An entry id allocator is a user-defined function (with a side effect) that takes an
   * address and returns an id (internal address) that was not returned previously.</p>
   */
  private final Map<MmuBuffer, UnaryOperator<Long>> addrAllocators;

  /**
   * Given a non-replaceable buffer, contains the entry provider.
   * 
   * <p>An entry provider is a user-defined function that fills a given entry with appropriate data
   * (the data are produced on the basis the memory access and the address object).</p>
   */
  private final Map<MmuBuffer, TriConsumer<MemoryAccess, AddressObject, MmuEntry>> entryProviders;

  /** Given a buffer, maps indices to sets of tags to be explicitly loaded into the buffer. */
  private final Map<MmuBuffer, Map<Long, Set<Long>>> bufferHitTags = new LinkedHashMap<>();

  /** Given a buffer, contains indices for which replacing sequences have been constructed. */
  private final Map<MmuBuffer, Set<Long>> bufferReplacedIndices = new LinkedHashMap<>();

  /** Given an access index, contains the buffers having been processed. */
  private final Map<Integer, Set<MmuBuffer>> handledBuffers = new LinkedHashMap<>();

  /** Contains a reference to the memory subsystem specification. */
  private final MmuSubsystem memory = MmuTranslator.getSpecification();

  /** Memory access structures being processed. */
  private final MemoryAccessStructure structure;
  /** Current solution. */
  private MemorySolution solution;

  public MemorySolution getCurrentSolution() {
    return solution;
  }

  /**
   * Constructs a solver for the given memory access structure.
   * 
   * @param structure the memory access structure.
   * @param context the memory engine context.
   * @throws IllegalArgumentException if some parameters are null.
   */
  public MemorySolver(
      final MemoryAccessStructure structure, final MemoryEngineContext context) {
    InvariantChecks.checkNotNull(memory);
    InvariantChecks.checkNotNull(structure);
    InvariantChecks.checkNotNull(context);

    this.structure = structure;

    this.addrObjectConstructors = context.getAddrObjectConstructors();
    this.addrObjectCorrectors = context.getAddrObjectCorrectors();
    this.addrAllocators = context.getAddrAllocators();
    this.entryProviders = context.getEntryProviders();
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

    solution.getLoader().addLoads(buffer, BufferAccessEvent.HIT, address, sequence);

    // Loading data into the buffer may load them into the previous buffers.
    final MemoryAccess access = structure.getAccess(j);
    final MemoryAccessPath path = access.getPath();
    final List<MmuBuffer> buffers = path.getBuffers();

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
    final UnaryOperator<Long> tagAllocator = addrAllocators.get(buffer);

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
    if (!replacedIndices.contains(index) &&
        (mayBeHit(j, buffer) || !tagReplacedRelation.isEmpty())) {
      final List<Long> sequence = new ArrayList<>();

      for (int i = 0; i < buffer.getWays(); i++) {
        final Long evictingTag = tagAllocator.apply(/* Full address */ address);

        if (evictingTag == null) {
          return new SolverResult<>(
              String.format("Cannot allocate a replacing tag for buffer %s", buffer));
        }

        // Address offset is randomized.
        final long replacingOffset =
            buffer.getOffset(Randomizer.get().nextLong());
        final long replacingAddress =
            buffer.getAddress(evictingTag, index, replacingOffset);

        sequence.add(replacingAddress);
      }

      solution.getLoader().addLoads(buffer, BufferAccessEvent.MISS, address, sequence);

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
    final MemoryAccess access = structure.getAccess(j);
    final MemoryUnitedDependency dependency = structure.getUnitedDependency(j);
    final MmuAddressType addrType = buffer.getAddress();

    final long address = addrObject.getAddress(addrType);

    final Set<Integer> tagEqualRelation = dependency.getTagEqualRelation(buffer);

    if (!tagEqualRelation.isEmpty()) {
      final int i = tagEqualRelation.iterator().next();
      final AddressObject prevAddrObject = solution.getAddressObject(i);

      // Instruction uses the same entry of the buffer.
      final Map<Long, MmuEntry> entries = prevAddrObject.getEntries(buffer);

      // Update the entry (the map contains one entry).
      for (final MmuEntry entry : entries.values()) {
        entryProviders.get(buffer).accept(access, addrObject, entry);
      }

      addrObject.setEntries(buffer, entries);
    }

    if (addrObject.getEntries(buffer) == null || addrObject.getEntries(buffer).isEmpty()) {
      final UnaryOperator<Long> entryIdAllocator =
          addrAllocators.get(buffer);
      final TriConsumer<MemoryAccess, AddressObject, MmuEntry> entryProvider =
          entryProviders.get(buffer);

      final Long bufferEntryId = entryIdAllocator.apply(address);
      final MmuEntry bufferEntry = new MmuEntry(buffer.getFields());

      if (bufferEntryId == null || bufferEntry == null) {
        return new SolverResult<>(String.format("Cannot allocate an entry for buffer %s", buffer));
      }

      // Filling the entry with appropriate data.
      entryProvider.accept(access, addrObject, bufferEntry);

      addrObject.addEntry(buffer, bufferEntryId, bufferEntry);
      solution.addEntry(buffer, bufferEntryId, bufferEntry);
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

      final UnaryOperator<Long> tagAllocator = addrAllocators.get(buffer);
  
      final long oldTag = buffer.getTag(addrObject.getAddress(addrType));
      final long oldIndex = buffer.getIndex(addrObject.getAddress(addrType));
      final long newIndex = buffer.getIndex(prevAddrObject.getAddress(addrType));
      final long oldOffset = buffer.getOffset(addrObject.getAddress(addrType));

      // Copy the index from the previous instruction.
      final long newAddress = buffer.getAddress(oldTag, newIndex, oldOffset);

      // If the index has changed, allocate a new tag.
      final long newTag = newIndex != oldIndex ? tagAllocator.apply(newAddress) : oldTag;

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
  private SolverResult<MemorySolution> solveDeviceConstraint(
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
      solveDeviceConstraint(j, buffer.getParent());
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
    final AddressObject addrObject = addrObjectConstructors.apply(access);
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

    // Solve the hit and miss constraints for the buffers.
    final List<MmuBuffer> buffers = path.getBuffers();

    for (final MmuBuffer buffer : buffers) {
      final SolverResult<MemorySolution> result = solveDeviceConstraint(j, buffer);

      if (result.getStatus() == SolverResult.Status.UNSAT) {
        return result;
      }
    }

    // Correct the solution.
    addrObjectCorrectors.accept(access, addrObject);

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
}
