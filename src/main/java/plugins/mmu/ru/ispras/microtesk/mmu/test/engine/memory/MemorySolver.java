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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.randomizer.Randomizer;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.fortress.util.Pair;
import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.basis.solver.Solver;
import ru.ispras.microtesk.basis.solver.SolverResult;
import ru.ispras.microtesk.basis.solver.integer.IntegerConstraint;
import ru.ispras.microtesk.basis.solver.integer.IntegerEqualConstraint;
import ru.ispras.microtesk.basis.solver.integer.IntegerField;
import ru.ispras.microtesk.basis.solver.integer.IntegerVariable;
import ru.ispras.microtesk.basis.solver.integer.IntegerVariableInitializer;
import ru.ispras.microtesk.mmu.MmuPlugin;
import ru.ispras.microtesk.mmu.basis.DataType;
import ru.ispras.microtesk.mmu.basis.MemoryAccessContext;
import ru.ispras.microtesk.mmu.basis.MemoryAccessType;
import ru.ispras.microtesk.mmu.model.api.BufferObserver;
import ru.ispras.microtesk.mmu.model.api.MmuModel;
import ru.ispras.microtesk.mmu.test.engine.memory.allocator.EntryIdAllocator;
import ru.ispras.microtesk.mmu.test.template.MemoryAccessConstraints;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuAddressInstance;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuBuffer;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuBufferAccess;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuCalculator;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuCondition;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuConditionAtom;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuEntry;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuExpression;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuSubsystem;
import ru.ispras.microtesk.settings.GeneratorSettings;
import ru.ispras.microtesk.settings.RegionSettings;
import ru.ispras.microtesk.utils.function.Function;

/**
 * {@link MemorySolver} implements a solver of memory-related constraints (hit, miss, etc.)
 * specified in a memory access structure.
 * 
 * <p>
 * The input is a memory access structure; the output is a solution.
 * </p>
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class MemorySolver implements Solver<MemorySolution> {
  /** Symbolic model of the memory subsystem. */
  private final MmuSubsystem memory = MmuPlugin.getSpecification();
  /** Executable model of the memory subsystem. */
  private final MmuModel model = MmuPlugin.getMmuModel();

  /** Memory access structure being processed. */
  private final List<MemoryAccess> structure;

  private final EntryIdAllocator entryIdAllocator = new EntryIdAllocator(GeneratorSettings.get());

  /** Current solution. */
  private MemorySolution solution;

  public MemorySolver(final List<MemoryAccess> structure) {
    InvariantChecks.checkNotNull(structure);
    this.structure = structure;
  }

  @Override
  public SolverResult<MemorySolution> solve(final Mode mode) {
    solution = new MemorySolution(structure);
    SolverResult<MemorySolution> result = null;

    for (int j = 0; j < structure.size(); j++) {
      result = solve(j);

      if (result.getStatus() != SolverResult.Status.SAT) {
        Logger.debug("Solve[%d]: UNSAT", j);
        return result;
      }
    }

    Logger.debug("Solve: SAT");
    return result;
  }

  private EntryObject allocateEntry(
      final int j,
      final MmuBufferAccess bufferAccess) {
    final AddressObject addrObject = solution.getAddressObject(j);
    final MemoryAccess access = structure.get(j);
    final BufferUnitedDependency dependency = access.getUnitedDependency();

    final EntryObject entryObject = addrObject.getEntry(bufferAccess);

    if (entryObject != null) {
      return entryObject;
    }

    final Set<Pair<Integer, BufferHazard.Instance>> tagEqualRelation =
        dependency.getTagEqualRelation(bufferAccess);

    if (!tagEqualRelation.isEmpty()) {
      final int i = tagEqualRelation.iterator().next().first;
      final AddressObject prevAddrObject = solution.getAddressObject(i);
      final EntryObject prevEntryObject = prevAddrObject.getEntry(bufferAccess);

      addrObject.setEntry(bufferAccess, prevEntryObject);
      return prevEntryObject;
    }

    // Allocate new entry.
    final MmuBuffer buffer = bufferAccess.getBuffer();
    final BigInteger newEntryId = allocateEntryId(buffer, false);
    final MmuEntry newEntry = new MmuEntry(buffer.getFields());
    final EntryObject newEntryObject = new EntryObject(newEntryId, newEntry);

    addrObject.setEntry(bufferAccess, newEntryObject);
    solution.addEntry(bufferAccess, newEntryObject);

    return newEntryObject;
  }

  private void fillEntry(
      final int j,
      final MmuBufferAccess bufferAccess,
      final EntryObject entryObject,
      final Map<IntegerVariable, BigInteger> values) {

    final AddressObject addrObject = solution.getAddressObject(j);
    final MmuEntry entry = entryObject.getEntry();

    final String bufferAccessId = bufferAccess.getId();
    final MemoryAccessContext context = bufferAccess.getContext();

    // Set the entry fields.
    entry.setValid(true);
    entry.setAddress(addrObject.getAddress(bufferAccess));

    Logger.debug("Fill entry: values=%s", values);

    for (final IntegerVariable field : entry.getVariables()) {
      final IntegerVariable fieldInstance = context.getInstance(bufferAccessId, field);
      Logger.debug("Fill entry: fieldInstance=%s", fieldInstance);

      // If an entry field is not used in the path, it remains unchanged.
      if (values.containsKey(fieldInstance) && !entry.isValid(field)) {
        final BigInteger fieldValue = values.get(fieldInstance);
        entry.setValue(field, fieldValue, true);
      }
    }
  }

  private MmuCondition getHazardCondition(
      final int i,
      final int j,
      final BufferHazard.Instance hazard) {

    final MmuBufferAccess bufferAccess1 = hazard.getPrimaryAccess();
    final MmuBufferAccess bufferAccess2 = hazard.getSecondaryAccess();

    final MmuBuffer buffer1 = bufferAccess1.getBuffer();
    final MmuBuffer buffer2 = bufferAccess2.getBuffer();
    InvariantChecks.checkTrue(buffer1 == buffer2);

    final Collection<MmuConditionAtom> atoms = hazard.getCondition().getAtoms();
    InvariantChecks.checkTrue(atoms.size() == 1);

    final MmuConditionAtom atom = atoms.iterator().next();
    final MmuExpression expression = atom.getLhsExpr();

    final AddressObject addrObject1 = solution.getAddressObject(i);
    final BigInteger addrValue1 = addrObject1.getAddress(bufferAccess1);

    final String instanceId2 = bufferAccess2.getId();
    final MemoryAccessContext context2 = bufferAccess2.getContext();

    final MmuExpression lhs = expression.getInstance(instanceId2, context2);
    final BigInteger rhs = MmuCalculator.eval(
        expression,
        new Function<IntegerVariable, BigInteger>() {
          @Override
          public BigInteger apply(final IntegerVariable variable) {
            return addrValue1;
          }
        },
        true);

    final MmuCondition condition = atom.isNegated()
        ? MmuCondition.neq(lhs, rhs)
        : MmuCondition.eq(lhs, rhs);

    Logger.debug("Hazard: %s, condition: %s", hazard, condition);
    return condition;
  }

  private Collection<MmuCondition> getHazardConditions(final int j) {
    final MemoryAccess access = structure.get(j);
    final MemoryAccessPath path = access.getPath();
    final BufferUnitedDependency dependency = access.getUnitedDependency();

    final Collection<MmuCondition> conditions = new ArrayList<>();

    for (final MmuBufferAccess bufferAccess : path.getBufferReads()) {
      final Set<Pair<Integer, BufferHazard.Instance>> indexEqualRelation =
          dependency.getIndexEqualRelation(bufferAccess);

      if (!indexEqualRelation.isEmpty()) {
        final Set<Pair<Integer, BufferHazard.Instance>> tagEqualRelation =
            dependency.getTagEqualRelation(bufferAccess);
        final Set<Pair<Integer, BufferHazard.Instance>> tagNotEqualRelation =
            dependency.getTagNotEqualRelation(bufferAccess);

        if (!tagEqualRelation.isEmpty() || !tagNotEqualRelation.isEmpty()) {
          // INDEX[j] == INDEX[i] && TAG[j] == TAG[i].
          for (final Pair<Integer, BufferHazard.Instance> pair : tagEqualRelation) {
            conditions.add(getHazardCondition(pair.first, j, pair.second));
            break; // Enough.
          }

          // INDEX[j] == INDEX[i] && TAG[j] != TAG[i].
          for (final Pair<Integer, BufferHazard.Instance> pair : tagNotEqualRelation) {
            conditions.add(getHazardCondition(pair.first, j, pair.second));
          }
        } else {
          // INDEX[j] == INDEX[i].
          for (final Pair<Integer, BufferHazard.Instance> pair : indexEqualRelation) {
            conditions.add(getHazardCondition(pair.first, j, pair.second));
            break; // Enough.
          }
        }
      }

      final Set<Pair<Integer, BufferHazard.Instance>> indexNotEqualRelation =
          dependency.getIndexNotEqualRelation(bufferAccess);

      // INDEX-NOT-EQUAL constraints.
      for (final Pair<Integer, BufferHazard.Instance> pair : indexNotEqualRelation) {
        conditions.add(getHazardCondition(pair.first, j, pair.second));
      }
    }

    return conditions;
  }

  private MmuCondition getIndexCondition(
      final MmuBufferAccess bufferAccess,
      final BigInteger addressWithoutTag) {
    final MmuExpression lhs = bufferAccess.getIndexExpression();
    final BigInteger rhs = bufferAccess.getBuffer().getIndex(addressWithoutTag);

    return MmuCondition.eq(lhs, rhs);
  }

  private MmuCondition getHitCondition(
      final MmuBufferAccess bufferAccess,
      final BigInteger addressWithoutTag) {
    final MmuBuffer buffer = bufferAccess.getBuffer();
    final BufferObserver bufferObserver = model.getBufferObserver(buffer.getName());

    final int bitSize = bufferAccess.getAddress().getBitSize();
    final BitVector index = BitVector.valueOf(buffer.getIndex(addressWithoutTag), bitSize);

    final Collection<MmuConditionAtom> atoms = new ArrayList<>();
    final MmuExpression lhs = bufferAccess.getTagExpression();

    // TAG == TAG[0] || ... || TAG == TAG[w-1].
    for (int i = 0; i < buffer.getWays(); i++) {
      final BitVector way = BitVector.valueOf(i, bitSize);
      final Pair<BitVector, BitVector> taggedData = bufferObserver.seeData(index, way);

      if (taggedData != null) {
        final BigInteger address = taggedData.first.bigIntegerValue();
        final BigInteger rhs = buffer.getTag(address);

        atoms.add(MmuConditionAtom.eq(lhs, rhs));
      }
    }

    return !atoms.isEmpty() ? MmuCondition.or(atoms) : MmuCondition.TRUE;
  }

  private MmuCondition getMissCondition(
      final MmuBufferAccess bufferAccess,
      final BigInteger addressWithoutTag) {
    final MmuBuffer buffer = bufferAccess.getBuffer();
    final BufferObserver bufferObserver = model.getBufferObserver(buffer.getName());

    final int bitSize = bufferAccess.getAddress().getBitSize();
    final BitVector index = BitVector.valueOf(buffer.getIndex(addressWithoutTag), bitSize);

    final Collection<MmuConditionAtom> atoms = new ArrayList<>();
    final MmuExpression lhs = bufferAccess.getTagExpression();

    // TAG != TAG[0] && ... && TAG != TAG[w-1].
    for (int i = 0; i < buffer.getWays(); i++) {
      final BitVector way = BitVector.valueOf(i, bitSize);
      final Pair<BitVector, BitVector> taggedData = bufferObserver.seeData(index, way);

      if (taggedData != null) {
        final BigInteger address = taggedData.first.bigIntegerValue();
        final BigInteger rhs = buffer.getTag(address);

        atoms.add(MmuConditionAtom.neq(lhs, rhs));
      }
    }

    return MmuCondition.and(atoms);
  }

  private MmuCondition getReplaceCondition(
      final MmuBufferAccess bufferAccess,
      final BigInteger addressWithoutTag) {
    // FIXME:
    return getMissCondition(bufferAccess, addressWithoutTag);
  }

  private BigInteger getRandomAddress(final DataType dataType) {
    final GeneratorSettings settings = GeneratorSettings.get();
    final RegionSettings region = settings.getMemory().getRegion(memory.getName());

    final BigInteger address =
        Randomizer.get().nextBigIntegerRange(region.getMin(), region.getMax());

    return dataType.align(address);
  }

  private BigInteger getRandomValue(final IntegerVariable variable) {
    return Randomizer.get().nextBigIntegerField(variable.getWidth(), false);
  }

  /**
   * Handles the given instruction call (access) of the memory access structure.
   * 
   * @param j the memory access index.
   * @return the partial solution.
   */
  private SolverResult<MemorySolution> solve(final int j) {
    final MemoryAccess access = structure.get(j);
    final BufferUnitedDependency dependency = access.getUnitedDependency();

    Logger.debug("Solve[%d]: %s, %s", j, access, dependency);

    AddressObject addrObject = new AddressObject(access);
    solution.setAddressObject(j, addrObject);

    // Generate a virtual address value.
    final MemoryAccessType accessType = access.getType();
    final DataType dataType = accessType.getDataType();

    final MmuAddressInstance virtAddrType = memory.getVirtualAddress();
    final BigInteger virtAddrValue = getRandomAddress(dataType);
    addrObject.setAddress(virtAddrType, virtAddrValue);

    final IntegerVariable dataVariable = memory.getDataVariable();
    final BigInteger dataValue = getRandomValue(dataVariable);
    addrObject.setData(dataVariable, dataValue);

    final Collection<MmuCondition> conditions = new ArrayList<>();
    final MemoryAccessConstraints accessConstraints = access.getConstraints();

    accessConstraints.randomize();

    final Collection<IntegerConstraint<IntegerField>> constraints =
        accessConstraints.getVariateConstraints();

    // Refine the addresses (in particular, assign the intermediate addresses).
    Map<IntegerVariable, BigInteger> values = refineAddr(addrObject, conditions, constraints);

    if (values == null) {
      final String warning = String.format("Infeasible path: %s", access);

      Logger.debug(warning);
      return new SolverResult<>(warning);
    }

    // Assign the tag, index and offset according to the dependencies.
    conditions.addAll(getHazardConditions(j));

    // Try to refine the address object.
    if (!conditions.isEmpty()) {
      final AddressObject refinedAddrObject = new AddressObject(access);
      refinedAddrObject.setAddress(virtAddrType, virtAddrValue);
      refinedAddrObject.setData(dataVariable, dataValue);

      final Map<IntegerVariable, BigInteger> refinedValues =
          refineAddr(refinedAddrObject, conditions, constraints);

      if (refinedValues != null) {
        solution.setAddressObject(j, refinedAddrObject);

        addrObject = refinedAddrObject;
        values = refinedValues;
      }
    }

    final MemoryAccessPath path = access.getPath();

    // Synthesize HIT, MISS, and REPLACE conditions.
    final int numberOfConditions = conditions.size();

    for (final MmuBufferAccess bufferAccess : path.getBufferChecks()) {
      final MmuBuffer buffer = bufferAccess.getBuffer();
      final Set<?> tagEqualRelation = dependency.getTagEqualRelation(bufferAccess);

      if (tagEqualRelation.isEmpty()) {
        final MmuAddressInstance addrType = bufferAccess.getAddress();
        final BigInteger addrValue = addrObject.getAddress(addrType);

        if (addrValue == null) {
          // The address has been already processed.
          continue;
        }

        switch (bufferAccess.getEvent()) {
          case HIT:
            // Add more constraints.
            conditions.add(getIndexCondition(bufferAccess, addrValue));
            conditions.add(getHitCondition(bufferAccess, addrValue));
            break;

          case MISS:
            // Add more constraints.
            conditions.add(getIndexCondition(bufferAccess, addrValue));

            if (!buffer.isReplaceable() || Randomizer.get().nextBoolean()) {
              conditions.add(getMissCondition(bufferAccess, addrValue));
            } else {
              conditions.add(getReplaceCondition(bufferAccess, addrValue));
            }

            break;

          default:
            InvariantChecks.checkTrue(false);
            break;
        }
      } // If there are no tag equalities.
    } // For each buffer access.

    // Try to refine the address object.
    if (conditions.size() > numberOfConditions) {
      final AddressObject refinedAddrObject = new AddressObject(access);
      refinedAddrObject.setAddress(virtAddrType, virtAddrValue);
      refinedAddrObject.setData(dataVariable, dataValue);

      final Map<IntegerVariable, BigInteger> refinedValues =
          refineAddr(refinedAddrObject, conditions, constraints);

      if (refinedValues != null) {
        solution.setAddressObject(j, refinedAddrObject);

        addrObject = refinedAddrObject;
        values = refinedValues;
      }
    }

    // Allocate entries in non-replaceable buffers.
    for (final MmuBufferAccess bufferAccess : path.getBufferReads()) {
      if (!bufferAccess.getBuffer().isReplaceable()) {
        final EntryObject entryObject = allocateEntry(j, bufferAccess);
        InvariantChecks.checkNotNull(entryObject);

        fillEntry(j, bufferAccess, entryObject, values);
      }
    }

    return new SolverResult<MemorySolution>(solution);
  }

  private BigInteger allocateEntryId(final MmuBuffer buffer, final boolean peek) {
    final BigInteger entryId = entryIdAllocator.allocate(buffer, peek, null);
    Logger.debug("Allocate entry: buffer=%s, entryId=%s", buffer, entryId.toString(16));

    return entryId;
  }

  private Map<IntegerVariable, BigInteger> refineAddr(
      final AddressObject addrObject,
      final Collection<MmuCondition> conditions,
      final Collection<IntegerConstraint<IntegerField>> constraints) {

    Logger.debug("Refine address: conditions=%s", conditions);

    final Collection<IntegerConstraint<IntegerField>> allConstraints = new ArrayList<>(constraints);

    // Fix known values of the data.
    final Map<IntegerVariable, BigInteger> data = addrObject.getData();
    for (final Map.Entry<IntegerVariable, BigInteger> entry : data.entrySet()) {
      final IntegerVariable variable = entry.getKey();
      final BigInteger value = entry.getValue();

      allConstraints.add(new IntegerEqualConstraint<IntegerField>(variable.field(), value));
    }

    // Fix known values of the addresses.
    final Map<MmuAddressInstance, BigInteger> addresses = addrObject.getAddresses();
    for (final Map.Entry<MmuAddressInstance, BigInteger> entry : addresses.entrySet()) {
      final IntegerVariable variable = entry.getKey().getVariable();
      final BigInteger value = entry.getValue();

      allConstraints.add(new IntegerEqualConstraint<IntegerField>(variable.field(), value));
    }

    Logger.debug("Constraints for refinement: %s", allConstraints);

    final Map<IntegerVariable, BigInteger> values =
        MemoryEngineUtils.generateData(
            addrObject.getAccess(),
            conditions,
            allConstraints,
            IntegerVariableInitializer.RANDOM
        );

    // Cannot correct the address values.
    if (values == null) {
      Logger.debug("Cannot refine the address values");
      return null;
    }

    final MemoryAccessPath path = addrObject.getAccess().getPath();
    final Collection<MmuBufferAccess> bufferAccesses = path.getBufferAccesses();

    Logger.debug("Buffer checks and reads: %s", bufferAccesses);

    // Set the intermediate addresses used along the memory access path.
    for (final MmuBufferAccess bufferAccess : bufferAccesses) {
      final MmuAddressInstance addrType = bufferAccess.getAddress();
      final IntegerVariable addrVar = addrType.getVariable(); 
      final BigInteger addrValue = values.get(addrVar);

      if (addrValue != null) {
        Logger.debug("Refine address: %s=0x%s", addrType, addrValue.toString(16));
        addrObject.setAddress(addrType, addrValue);
      }
    }

    // Get the virtual address value.
    final MmuAddressInstance addrType = memory.getVirtualAddress();
    final IntegerVariable addrVar = addrType.getVariable(); 
    final BigInteger addrValue = values.get(addrVar);
    InvariantChecks.checkNotNull(addrValue, "Cannot obtain the virtual address value");

    Logger.debug("Refine address: %s=0x%s", addrType, addrValue.toString(16));
    addrObject.setAddress(addrType, addrValue);

    return values;
  }
}
