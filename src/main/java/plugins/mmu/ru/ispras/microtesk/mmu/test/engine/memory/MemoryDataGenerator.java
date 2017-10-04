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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.ispras.fortress.data.Data;
import ru.ispras.fortress.data.Variable;
import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeOperation;
import ru.ispras.fortress.expression.NodeValue;
import ru.ispras.fortress.randomizer.Randomizer;
import ru.ispras.fortress.transformer.ValueProvider;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.fortress.util.Pair;
import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.basis.solver.integer.IntegerConstraint;
import ru.ispras.microtesk.basis.solver.integer.IntegerEqualConstraint;
import ru.ispras.microtesk.basis.solver.integer.VariableInitializer;
import ru.ispras.microtesk.mmu.MmuPlugin;
import ru.ispras.microtesk.mmu.basis.MemoryAccessContext;
import ru.ispras.microtesk.mmu.basis.MemoryAccessType;
import ru.ispras.microtesk.mmu.basis.MemoryDataType;
import ru.ispras.microtesk.mmu.model.api.BufferObserver;
import ru.ispras.microtesk.mmu.model.api.MmuModel;
import ru.ispras.microtesk.mmu.test.template.AccessConstraints;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuAddressInstance;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuBuffer;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuBufferAccess;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuEntry;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuSubsystem;
import ru.ispras.microtesk.settings.GeneratorSettings;
import ru.ispras.microtesk.settings.RegionSettings;
import ru.ispras.microtesk.test.engine.AbstractSequence;
import ru.ispras.microtesk.test.template.AbstractCall;
import ru.ispras.microtesk.test.template.Primitive;
import ru.ispras.microtesk.test.template.Situation;
import ru.ispras.microtesk.utils.FortressUtils;
import ru.ispras.testbase.TestBaseContext;
import ru.ispras.testbase.TestBaseQuery;
import ru.ispras.testbase.TestData;
import ru.ispras.testbase.TestDataProvider;
import ru.ispras.testbase.generator.DataGenerator;

/**
 * {@link MemoryDataGenerator} implements a solver of memory-related constraints (hit, miss, etc.).
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class MemoryDataGenerator implements DataGenerator {
  public static final String SEQUENCE = "AbstractSequence";
  public static final String CONSTRAINT = String.format("%s.%s", MemoryEngine.ID, "constraint");
  public static final String SOLUTION = String.format("%s.%s", MemoryEngine.ID, "solution");

  @Override
  public boolean isSuitable(final TestBaseQuery query) {
    InvariantChecks.checkNotNull(query);

    final Map<String, Object> context = query.getContext();
    final Object situationId = context.get(TestBaseContext.TESTCASE);

    Logger.debug("MemoryDataGenerator.isSuitable: %s == %s", MemoryEngine.ID, situationId);
    return MemoryEngine.ID.equals(situationId);
  }

  @Override
  public TestDataProvider generate(final TestBaseQuery query) {
    InvariantChecks.checkNotNull(query);

    final MmuSubsystem memory = MmuPlugin.getSpecification();

    final Map<String, Object> context = query.getContext();
    final AbstractSequence abstractSequence = (AbstractSequence) context.get(SEQUENCE);
    InvariantChecks.checkNotNull(abstractSequence);

    final Map<String, Object> parameters = query.getParameters();
    final Access access = (Access) parameters.get(CONSTRAINT);
    InvariantChecks.checkNotNull(access);

    AddressObject solution = new AddressObject(access);
    //final List<AddressObject> solutions = null; // FIXME:

    final BufferUnitedDependency dependency = access.getUnitedDependency();
    Logger.debug("Solve: %s, %s", access, dependency);

    // Generate a virtual address value.
    final MemoryAccessType accessType = access.getType();
    final MemoryDataType dataType = accessType.getDataType();

    final MmuAddressInstance virtAddrType = memory.getVirtualAddress();
    final BigInteger virtAddrValue = getRandomAddress(dataType);
    solution.setAddress(virtAddrType, virtAddrValue);

    final Variable dataVariable = memory.getDataVariable();
    final BigInteger dataValue = getRandomValue(dataVariable);
    solution.setData(dataVariable, dataValue);

    final Collection<Node> conditions = new ArrayList<>();
    final AccessConstraints accessConstraints = access.getConstraints();

    accessConstraints.randomize();

    final Collection<IntegerConstraint> constraints =
        accessConstraints.getVariateConstraints();

    // Refine the addresses (in particular, assign the intermediate addresses).
    Map<Variable, BigInteger> values = refineAddr(solution, conditions, constraints);

    if (values == null) {
      Logger.debug("Infeasible path: %s", access);
      return TestDataProvider.empty();
    }

    // Assign the tag, index and offset according to the dependencies.
    conditions.addAll(getHazardConditions(access, abstractSequence));

    // Try to refine the address object.
    if (!conditions.isEmpty()) {
      final AddressObject refinedAddrObject = new AddressObject(access);
      refinedAddrObject.setAddress(virtAddrType, virtAddrValue);
      refinedAddrObject.setData(dataVariable, dataValue);

      final Map<Variable, BigInteger> refinedValues =
          refineAddr(refinedAddrObject, conditions, constraints);

      if (refinedValues != null) {
        solution = refinedAddrObject;
        values = refinedValues;
      }
    }

    final AccessPath path = access.getPath();

    // Synthesize HIT, MISS, and REPLACE conditions.
    final int numberOfConditions = conditions.size();

    for (final MmuBufferAccess bufferAccess : path.getBufferChecks()) {
      final MmuBuffer buffer = bufferAccess.getBuffer();
      final Set<?> tagEqualRelation = dependency.getTagEqualRelation(bufferAccess);

      if (tagEqualRelation.isEmpty()) {
        final MmuAddressInstance addrType = bufferAccess.getAddress();
        final BigInteger addrValue = solution.getAddress(addrType);

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

      final Map<Variable, BigInteger> refinedValues =
          refineAddr(refinedAddrObject, conditions, constraints);

      if (refinedValues != null) {
        solution = refinedAddrObject;
        values = refinedValues;
      }
    }

    // Allocate entries in non-replaceable buffers.
    for (final MmuBufferAccess bufferAccess : path.getBufferReads()) {
      if (!bufferAccess.getBuffer().isReplaceable()) {
        final EntryObject entryObject = allocateEntry(access, solution, abstractSequence, bufferAccess);
        InvariantChecks.checkNotNull(entryObject);

        fillEntry(solution, bufferAccess, entryObject, values);
      }
    }

    final TestData testData = new TestData(
        MemoryEngine.ID,
        Collections.<String, Object>singletonMap(SOLUTION, solution)
    );

    return TestDataProvider.singleton(testData);
  }

  private EntryObject allocateEntry(
      final Access access,
      final AddressObject solution,
      final AbstractSequence abstractSequence,
      final MmuBufferAccess bufferAccess) {
    final BufferUnitedDependency dependency = access.getUnitedDependency();
    final EntryObject entryObject = solution.getEntry(bufferAccess);

    if (entryObject != null) {
      return entryObject;
    }

    final Set<Pair<Integer, BufferHazard.Instance>> tagEqualRelation =
        dependency.getTagEqualRelation(bufferAccess);

    if (!tagEqualRelation.isEmpty()) {
      final int i = tagEqualRelation.iterator().next().first;
      final AbstractCall prevAbstractCall = abstractSequence.getSequence().get(i);
      final AddressObject prevAddrObject = getAddressObject(prevAbstractCall);
      final EntryObject prevEntryObject = prevAddrObject.getEntry(bufferAccess);

      solution.setEntry(bufferAccess, prevEntryObject);
      return prevEntryObject;
    }

    // Allocate new entry.
    final MmuBuffer buffer = bufferAccess.getBuffer();
    final BigInteger newEntryId = allocateEntryId(buffer, false);
    final MmuEntry newEntry = new MmuEntry(buffer.getFields());
    final EntryObject newEntryObject = new EntryObject(newEntryId, newEntry);

    solution.setEntry(bufferAccess, newEntryObject);

    return newEntryObject;
  }

  private void fillEntry(
      final AddressObject solution,
      final MmuBufferAccess bufferAccess,
      final EntryObject entryObject,
      final Map<Variable, BigInteger> values) {

    final MmuEntry entry = entryObject.getEntry();

    final String bufferAccessId = bufferAccess.getId();
    final MemoryAccessContext context = bufferAccess.getContext();

    // Set the entry fields.
    entry.setValid(true);
    entry.setAddress(solution.getAddress(bufferAccess));

    Logger.debug("Fill entry: values=%s", values);

    for (final Variable field : entry.getVariables()) {
      final Variable fieldInstance = context.getInstance(bufferAccessId, field);
      Logger.debug("Fill entry: fieldInstance=%s", fieldInstance);

      // If an entry field is not used in the path, it remains unchanged.
      if (values.containsKey(fieldInstance) && !entry.isValid(field)) {
        final BigInteger fieldValue = values.get(fieldInstance);
        entry.setValue(field, fieldValue, true);
      }
    }
  }

  private Node getHazardCondition(
      final AbstractSequence abstractSequence,
      final int i,
      final BufferHazard.Instance hazard) {
    final MmuBufferAccess bufferAccess1 = hazard.getPrimaryAccess();
    final MmuBufferAccess bufferAccess2 = hazard.getSecondaryAccess();

    final MmuBuffer buffer1 = bufferAccess1.getBuffer();
    final MmuBuffer buffer2 = bufferAccess2.getBuffer();
    InvariantChecks.checkTrue(buffer1 == buffer2);

    final NodeOperation condition = (NodeOperation) hazard.getCondition();
    final List<Node> atoms = condition.getOperands();
    InvariantChecks.checkTrue(atoms.size() == 1);

    final NodeOperation equality = (NodeOperation) atoms.iterator().next();
    final Node expression = equality.getOperand(0);

    final AbstractCall abstractCall1 = abstractSequence.getSequence().get(i);
    final AddressObject addressObject1 = getAddressObject(abstractCall1);
    final BigInteger addrValue1 = addressObject1.getAddress(bufferAccess1);

    final String instanceId2 = bufferAccess2.getId();
    final MemoryAccessContext context2 = bufferAccess2.getContext();

    final Node lhs = context2.getInstance(instanceId2, expression);
    final BigInteger rhs = FortressUtils.evaluate(
        expression,
        new ValueProvider() {
          @Override
          public Data getVariableValue(final Variable variable) {
            return Data.newBitVector(addrValue1, variable.getType().getSize());
          }
        });

    return new NodeOperation(equality.getOperationId(), lhs, FortressUtils.makeNodeValueInteger(rhs));
  }

  private Collection<Node> getHazardConditions(
      final Access access,
      final AbstractSequence abstractSequence) {
    final AccessPath path = access.getPath();
    final BufferUnitedDependency dependency = access.getUnitedDependency();

    final Collection<Node> conditions = new ArrayList<>();

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
            conditions.add(getHazardCondition(abstractSequence, pair.first, pair.second));
            break; // Enough.
          }

          // INDEX[j] == INDEX[i] && TAG[j] != TAG[i].
          for (final Pair<Integer, BufferHazard.Instance> pair : tagNotEqualRelation) {
            conditions.add(getHazardCondition(abstractSequence, pair.first, pair.second));
          }
        } else {
          // INDEX[j] == INDEX[i].
          for (final Pair<Integer, BufferHazard.Instance> pair : indexEqualRelation) {
            conditions.add(getHazardCondition(abstractSequence, pair.first, pair.second));
            break; // Enough.
          }
        }
      }

      final Set<Pair<Integer, BufferHazard.Instance>> indexNotEqualRelation =
          dependency.getIndexNotEqualRelation(bufferAccess);

      // INDEX-NOT-EQUAL constraints.
      for (final Pair<Integer, BufferHazard.Instance> pair : indexNotEqualRelation) {
        conditions.add(getHazardCondition(abstractSequence, pair.first, pair.second));
      }
    }

    return conditions;
  }

  private Node getIndexCondition(
      final MmuBufferAccess bufferAccess,
      final BigInteger addressWithoutTag) {
    final Node lhs = bufferAccess.getIndexExpression();
    final BigInteger rhs = bufferAccess.getBuffer().getIndex(addressWithoutTag);

    return FortressUtils.makeNodeEqual(lhs, FortressUtils.makeNodeValueInteger(rhs));
  }

  private Node getHitCondition(
      final MmuBufferAccess bufferAccess,
      final BigInteger addressWithoutTag) {
    final MmuModel model = MmuPlugin.getMmuModel();

    final MmuBuffer buffer = bufferAccess.getBuffer();
    final BufferObserver bufferObserver = model.getBufferObserver(buffer.getName());

    final int bitSize = bufferAccess.getAddress().getBitSize();
    final BitVector index = BitVector.valueOf(buffer.getIndex(addressWithoutTag), bitSize);

    final List<Node> atoms = new ArrayList<>();
    final Node lhs = bufferAccess.getTagExpression();

    // TAG == TAG[0] || ... || TAG == TAG[w-1].
    for (int i = 0; i < buffer.getWays(); i++) {
      final BitVector way = BitVector.valueOf(i, bitSize);
      final Pair<BitVector, BitVector> taggedData = bufferObserver.seeData(index, way);

      if (taggedData != null) {
        final BigInteger address = taggedData.first.bigIntegerValue();
        final BigInteger rhs = buffer.getTag(address);

        atoms.add(FortressUtils.makeNodeEqual(lhs, FortressUtils.makeNodeValueInteger(rhs)));
      }
    }

    return !atoms.isEmpty() ? FortressUtils.makeNodeOr(atoms) : NodeValue.newBoolean(true);
  }

  private Node getMissCondition(
      final MmuBufferAccess bufferAccess,
      final BigInteger addressWithoutTag) {
    final MmuModel model = MmuPlugin.getMmuModel();

    final MmuBuffer buffer = bufferAccess.getBuffer();
    final BufferObserver bufferObserver = model.getBufferObserver(buffer.getName());

    final int bitSize = bufferAccess.getAddress().getBitSize();
    final BitVector index = BitVector.valueOf(buffer.getIndex(addressWithoutTag), bitSize);

    final List<Node> atoms = new ArrayList<>();
    final Node lhs = bufferAccess.getTagExpression();

    // TAG != TAG[0] && ... && TAG != TAG[w-1].
    for (int i = 0; i < buffer.getWays(); i++) {
      final BitVector way = BitVector.valueOf(i, bitSize);
      final Pair<BitVector, BitVector> taggedData = bufferObserver.seeData(index, way);

      if (taggedData != null) {
        final BigInteger address = taggedData.first.bigIntegerValue();
        final BigInteger rhs = buffer.getTag(address);

        atoms.add(FortressUtils.makeNodeNotEqual(lhs, FortressUtils.makeNodeValueInteger(rhs)));
      }
    }

    return FortressUtils.makeNodeAnd(atoms);
  }

  private Node getReplaceCondition(
      final MmuBufferAccess bufferAccess,
      final BigInteger addressWithoutTag) {
    // FIXME:
    return getMissCondition(bufferAccess, addressWithoutTag);
  }

  private BigInteger getRandomAddress(final MemoryDataType dataType) {
    final MmuSubsystem memory = MmuPlugin.getSpecification();

    final GeneratorSettings settings = GeneratorSettings.get();
    final RegionSettings region = settings.getMemory().getRegion(memory.getName());

    final BigInteger address =
        Randomizer.get().nextBigIntegerRange(region.getMin(), region.getMax());

    return dataType.align(address);
  }

  private BigInteger getRandomValue(final Variable variable) {
    return Randomizer.get().nextBigIntegerField(variable.getType().getSize(), false);
  }

  private BigInteger allocateEntryId(final MmuBuffer buffer, final boolean peek) {
    final EntryIdAllocator entryIdAllocator = new EntryIdAllocator(GeneratorSettings.get());

    final BigInteger entryId = entryIdAllocator.allocate(buffer, peek, null);
    Logger.debug("Allocate entry: buffer=%s, entryId=%s", buffer, entryId.toString(16));

    return entryId;
  }

  private Map<Variable, BigInteger> refineAddr(
      final AddressObject addressObject,
      final Collection<Node> conditions,
      final Collection<IntegerConstraint> constraints) {

    Logger.debug("Refine address: conditions=%s", conditions);

    final Collection<IntegerConstraint> allConstraints = new ArrayList<>(constraints);

    // Fix known values of the data.
    final Map<Variable, BigInteger> data = addressObject.getData();
    for (final Map.Entry<Variable, BigInteger> entry : data.entrySet()) {
      final Variable variable = entry.getKey();
      final BigInteger value = entry.getValue();

      allConstraints.add(
          new IntegerEqualConstraint(FortressUtils.makeNodeVariable(variable), value));
    }

    // Fix known values of the addresses.
    final Map<MmuAddressInstance, BigInteger> addresses = addressObject.getAddresses();
    for (final Map.Entry<MmuAddressInstance, BigInteger> entry : addresses.entrySet()) {
      final Variable variable = entry.getKey().getVariable();
      final BigInteger value = entry.getValue();

      allConstraints.add(
          new IntegerEqualConstraint(FortressUtils.makeNodeVariable(variable), value));
    }

    Logger.debug("Constraints for refinement: %s", allConstraints);

    final Map<Variable, BigInteger> values =
        MemoryEngineUtils.generateData(
            addressObject.getAccess(),
            conditions,
            allConstraints,
            VariableInitializer.RANDOM
        );

    // Cannot correct the address values.
    if (values == null) {
      Logger.debug("Cannot refine the address values");
      return null;
    }

    final AccessPath path = addressObject.getAccess().getPath();
    final Collection<MmuBufferAccess> bufferAccesses = path.getBufferAccesses();

    Logger.debug("Buffer checks and reads: %s", bufferAccesses);

    // Set the intermediate addresses used along the memory access path.
    for (final MmuBufferAccess bufferAccess : bufferAccesses) {
      final MmuAddressInstance addrType = bufferAccess.getAddress();
      final Variable addrVar = addrType.getVariable(); 
      final BigInteger addrValue = values.get(addrVar);

      if (addrValue != null) {
        Logger.debug("Refine address: %s=0x%s", addrType, addrValue.toString(16));
        addressObject.setAddress(addrType, addrValue);
      }
    }

    // Get the virtual address value.
    final MmuSubsystem memory = MmuPlugin.getSpecification();
    final MmuAddressInstance addrType = memory.getVirtualAddress();
    final Variable addrVar = addrType.getVariable(); 
    final BigInteger addrValue = values.get(addrVar);
    InvariantChecks.checkNotNull(addrValue, "Cannot obtain the virtual address value");

    Logger.debug("Refine address: %s=0x%s", addrType, addrValue.toString(16));
    addressObject.setAddress(addrType, addrValue);

    return values;
  }

  private AddressObject getAddressObject(final AbstractCall abstractCall) {
    final Primitive primitive = abstractCall.getRootOperation();
    final Situation situation = primitive.getSituation();
    final TestData testData = situation.getTestData();
    InvariantChecks.checkNotNull(testData);

    return (AddressObject) testData.getBindings().get(MemoryDataGenerator.SOLUTION);
  }
}
