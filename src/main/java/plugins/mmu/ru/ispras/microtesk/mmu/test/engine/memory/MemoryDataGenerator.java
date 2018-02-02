/*
 * Copyright 2006-2018 ISP RAS (http://www.ispras.ru)
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

import ru.ispras.fortress.data.Data;
import ru.ispras.fortress.data.Variable;
import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeOperation;
import ru.ispras.fortress.expression.NodeValue;
import ru.ispras.fortress.expression.NodeVariable;
import ru.ispras.fortress.expression.Nodes;
import ru.ispras.fortress.expression.StandardOperation;
import ru.ispras.fortress.randomizer.Randomizer;
import ru.ispras.fortress.transformer.ValueProvider;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.fortress.util.Pair;
import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.basis.solver.bitvector.BitVectorConstraint;
import ru.ispras.microtesk.basis.solver.bitvector.BitVectorVariableInitializer;
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
import ru.ispras.testbase.generator.DataGenerator;
import ru.ispras.testbase.knowledge.iterator.EmptyIterator;
import ru.ispras.testbase.knowledge.iterator.Iterator;
import ru.ispras.testbase.knowledge.iterator.SingleValueIterator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
  public Iterator<TestData> generate(final TestBaseQuery query) {
    InvariantChecks.checkNotNull(query);

    final MmuSubsystem memory = MmuPlugin.getSpecification();
    InvariantChecks.checkNotNull(memory);

    final Map<String, Object> parameters = query.getParameters();

    final AbstractSequence abstractSequence = (AbstractSequence) parameters.get(SEQUENCE);
    InvariantChecks.checkNotNull(abstractSequence);
    Logger.debug("MemoryDataGenerator.generate: %s", abstractSequence.getSequence());

    final Access access = (Access) parameters.get(CONSTRAINT);
    InvariantChecks.checkNotNull(access);
    Logger.debug("MemoryDataGenerator.generate: %s", access);

    // Generate the basic address object constraints.
    final Collection<Node> conditions = new ArrayList<>();
    final Collection<Node> constraints = new ArrayList<>();

    final MemoryAccessType accessType = access.getType();
    final MemoryDataType dataType = accessType.getDataType();

    final MmuAddressInstance addressType = memory.getVirtualAddress();
    constraints.addAll(getAddressConstraints(addressType, dataType));

    final NodeVariable dataVariable = memory.getDataVariable();
    final BitVector dataValue = getRandomValue(dataVariable.getVariable());

    final AccessConstraints accessConstraints = access.getConstraints();
    accessConstraints.randomize();

    constraints.addAll(accessConstraints.getVariateConstraints());

    // Refine the addresses (in particular, assign the intermediate addresses).
    AddressObject addressObject = new AddressObject(access);

    Map<Variable, BitVector> values =
        refineAddress(
          addressObject,
          addressType,
          dataVariable,
          dataValue,
          conditions,
          constraints
        );

    if (values == null) {
      Logger.debug("MemoryDataGenerator.generate: infeasible %s", access);
      return EmptyIterator.<TestData>get();
    }

    // Assign the tag, index and offset according to the dependencies.
    conditions.addAll(getHazardConditions(access, abstractSequence));

    // Try to refine the address object.
    if (!conditions.isEmpty()) {
      final AddressObject refinedObject = new AddressObject(access);

      final Map<Variable, BitVector> refinedValues =
          refineAddress(
            refinedObject,
            addressType,
            dataVariable,
            dataValue,
            conditions,
            constraints
          );

      if (refinedValues != null) {
        addressObject = refinedObject;
        values = refinedValues;
      }
    }

    // Assign the tag, index and offset according to the hit and miss conditions.
    final int previousNumberOfConditions = conditions.size();
    conditions.addAll(getHitMissConditions(access, addressObject));

    // Try to refine the address object.
    if (conditions.size() != previousNumberOfConditions) {
      final AddressObject refinedObject = new AddressObject(access);

      final Map<Variable, BitVector> refinedValues =
          refineAddress(
            refinedObject,
            addressType,
            dataVariable,
            dataValue,
            conditions,
            constraints
          );

      if (refinedValues != null) {
        addressObject = refinedObject;
        values = refinedValues;
      }
    }

    // Allocate entries in the non-replaceable buffers.
    fillEntries(access, addressObject, values, abstractSequence);

    final TestData testData = new TestData(
        MemoryEngine.ID,
        Collections.<String, Object>singletonMap(SOLUTION, addressObject)
    );

    Logger.debug("MemoryDataGenerator.generate: addressObject[%d]=%s",
        System.identityHashCode(addressObject), addressObject);

    return new SingleValueIterator<>(testData);
  }

  private void fillEntries(
      final Access access,
      final AddressObject addressObject,
      final Map<Variable, BitVector> values,
      final AbstractSequence abstractSequence) {
    final AccessPath path = access.getPath();

    for (final MmuBufferAccess bufferAccess : path.getBufferReads()) {
      if (!bufferAccess.getBuffer().isReplaceable()) {
        final EntryObject entryObject = 
            allocateEntry(access, addressObject, abstractSequence, bufferAccess);
        InvariantChecks.checkNotNull(entryObject);

        fillEntry(addressObject, bufferAccess, entryObject, values);
      }
    }
  }

  private EntryObject allocateEntry(
      final Access access,
      final AddressObject addressObject,
      final AbstractSequence abstractSequence,
      final MmuBufferAccess bufferAccess) {
    final BufferUnitedDependency dependency = access.getUnitedDependency();
    final EntryObject entryObject = addressObject.getEntry(bufferAccess);

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

      addressObject.setEntry(bufferAccess, prevEntryObject);
      return prevEntryObject;
    }

    // Allocate new entry.
    final MmuBuffer buffer = bufferAccess.getBuffer();
    final BitVector newEntryId = allocateEntryId(buffer, false);
    final MmuEntry newEntry = new MmuEntry(buffer.getFields());
    final EntryObject newEntryObject = new EntryObject(newEntryId, newEntry);

    addressObject.setEntry(bufferAccess, newEntryObject);

    return newEntryObject;
  }

  private void fillEntry(
      final AddressObject addressObject,
      final MmuBufferAccess bufferAccess,
      final EntryObject entryObject,
      final Map<Variable, BitVector> values) {

    final MmuEntry entry = entryObject.getEntry();

    final String bufferAccessId = bufferAccess.getId();
    final MemoryAccessContext context = bufferAccess.getContext();

    // Set the entry fields.
    entry.setValid(true);
    entry.setAddress(addressObject.getAddress(bufferAccess));

    Logger.debug("Fill entry: values=%s", values);

    for (final NodeVariable field : entry.getVariables()) {
      final NodeVariable fieldInstance = context.getInstance(bufferAccessId, field);
      Logger.debug("Fill entry: fieldInstance=%s", fieldInstance);

      // If an entry field is not used in the path, it remains unchanged.
      if (values.containsKey(fieldInstance.getVariable()) && !entry.isValid(field)) {
        final BitVector fieldValue = values.get(fieldInstance.getVariable());
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

    final AbstractCall abstractCall1 = abstractSequence.getSequence().get(i);
    Logger.debug("Sequence=%s, Sequence[%d]=%s", abstractSequence.getSequence(), i, abstractCall1);

    final AddressObject addressObject1 = getAddressObject(abstractCall1);
    final BitVector addressValue1 = addressObject1.getAddress(bufferAccess1);
    InvariantChecks.checkTrue(addressValue1 != null);

    final String instanceId2 = bufferAccess2.getId();
    final MemoryAccessContext context2 = bufferAccess2.getContext();
    InvariantChecks.checkTrue(context2 != null);

    final NodeOperation condition = (NodeOperation) hazard.getCondition();
    InvariantChecks.checkTrue(condition.getOperationId() == StandardOperation.AND);

    final List<Node> newEquations = new ArrayList<>();

    for (final Node operand : condition.getOperands()) {
      final NodeOperation oldEquation = (NodeOperation) operand;
      InvariantChecks.checkTrue(oldEquation.getOperandCount() == 2);

      final Node oldLhs = oldEquation.getOperand(0);
      final Node oldRhs = oldEquation.getOperand(1);

      final BitVector value = FortressUtils.evaluateBitVector(
          oldRhs,
          new ValueProvider() {
            @Override
            public Data getVariableValue(final Variable variable) {
              return Data.newBitVector(addressValue1);
            }
          });

      final Node newLhs = context2.getInstance(instanceId2, oldLhs);
      final Node newRhs = NodeValue.newBitVector(value);

      newEquations.add(new NodeOperation(oldEquation.getOperationId(), newLhs, newRhs));
    }

    return Nodes.and(newEquations);
  }

  private Collection<Node> getHazardConditions(
      final Access access,
      final AbstractSequence abstractSequence) {
    final Collection<Node> conditions = new ArrayList<>();
    final AccessPath path = access.getPath();
    final BufferUnitedDependency dependency = access.getUnitedDependency();

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

  private Collection<Node> getHitMissConditions(
      final Access access,
      AddressObject addressObject) {
    final Collection<Node> conditions = new ArrayList<>();
    final AccessPath path = access.getPath();
    final BufferUnitedDependency dependency = access.getUnitedDependency();

    for (final MmuBufferAccess bufferAccess : path.getBufferChecks()) {
      if (!bufferAccess.getBuffer().isReplaceable()) {
        // FIXME:
        // This check was introduced to avoid the following problem.
        // Register-mapped buffers' seeData() returns incorrect addresses. 
        continue;
      }

      final Set<?> tagEqualRelation = dependency.getTagEqualRelation(bufferAccess);
      if (!tagEqualRelation.isEmpty()) {
        // Hit/miss conditions are ignored if there is a tag dependency.
        continue;
      }

      final MmuAddressInstance addressType = bufferAccess.getAddress();
      final BitVector addressValue = addressObject.getAddress(addressType);

      if (addressValue == null) {
        // The address has been already processed.
        continue;
      }

      conditions.addAll(getHitMissConditions(bufferAccess, addressValue));
    }

    return conditions;
  }

  private Collection<Node> getHitMissConditions(
      final MmuBufferAccess bufferAccess,
      final BitVector addressValue) {
    final Collection<Node> conditions = new ArrayList<>();
    final MmuBuffer buffer = bufferAccess.getBuffer();

    switch (bufferAccess.getEvent()) {
      case HIT:
        final Node hitIndexCondition = getIndexCondition(bufferAccess, addressValue);
        if (hitIndexCondition != null) {
          conditions.add(hitIndexCondition);
        }

        final Node hitTagCondition = getHitCondition(bufferAccess, addressValue);
        if (hitTagCondition != null) {
          conditions.add(hitTagCondition);
        }

        break;
      case MISS:
        final Node missIndexCondition = getIndexCondition(bufferAccess, addressValue);

        if (missIndexCondition != null) {
          conditions.add(missIndexCondition);
        }

        // There are two types of misses
        if (!buffer.isReplaceable() || Randomizer.get().nextBoolean()) {
          final Node missTagCondition = getMissCondition(bufferAccess, addressValue);
          if (missTagCondition != null) {
            conditions.add(missTagCondition);
          }
        } else {
          final Node replaceTagCondition = getReplaceCondition(bufferAccess, addressValue);
          if (replaceTagCondition != null) {
            conditions.add(replaceTagCondition);
          }
        }

        break;
      default:
        InvariantChecks.checkTrue(false);
        break;
    }

    return conditions;
  }

  private Node getIndexCondition(
      final MmuBufferAccess bufferAccess,
      final BitVector addressWithoutTag) {
    final MmuBuffer buffer = bufferAccess.getBuffer();

    // This workaround is to handle fully associative buffers.
    if (buffer.getIndexExpression().getKind() == Node.Kind.VALUE) {
      return null;
    }

    final Node lhs = bufferAccess.getIndexExpression();
    final BitVector rhs = bufferAccess.getBuffer().getIndex(addressWithoutTag);

    return Nodes.eq(lhs, NodeValue.newBitVector(rhs));
  }

  private Node getHitCondition(
      final MmuBufferAccess bufferAccess,
      final BitVector addressWithoutTag) {
    final MmuModel model = MmuPlugin.getMmuModel();

    final MmuBuffer buffer = bufferAccess.getBuffer();
    final BufferObserver bufferObserver = model.getBufferObserver(buffer.getName());

    final BitVector index = buffer.getIndex(addressWithoutTag);

    final List<Node> atoms = new ArrayList<>();
    final Node lhs = bufferAccess.getTagExpression();

    // TAG == TAG[0] || ... || TAG == TAG[w-1].
    for (int i = 0; i < buffer.getWays(); i++) {
      final BitVector way = BitVector.valueOf(i, Integer.SIZE);
      final Pair<BitVector, BitVector> taggedData = bufferObserver.seeData(index, way);

      if (taggedData != null) {
        final BitVector address = taggedData.first;
        final BitVector rhs = buffer.getTag(address);

        atoms.add(Nodes.eq(lhs, NodeValue.newBitVector(rhs)));
      }
    }

    return !atoms.isEmpty() ? Nodes.or(atoms) : null;
  }

  private Node getMissCondition(
      final MmuBufferAccess bufferAccess,
      final BitVector addressWithoutTag) {
    final MmuModel model = MmuPlugin.getMmuModel();

    final MmuBuffer buffer = bufferAccess.getBuffer();
    final BufferObserver bufferObserver = model.getBufferObserver(buffer.getName());

    final BitVector index = buffer.getIndex(addressWithoutTag);

    final List<Node> atoms = new ArrayList<>();
    final Node lhs = bufferAccess.getTagExpression();

    // TAG != TAG[0] && ... && TAG != TAG[w-1].
    for (int i = 0; i < buffer.getWays(); i++) {
      final BitVector way = BitVector.valueOf(i, Integer.SIZE);
      final Pair<BitVector, BitVector> taggedData = bufferObserver.seeData(index, way);

      if (taggedData != null) {
        final BitVector address = taggedData.first;
        final BitVector rhs = buffer.getTag(address);

        atoms.add(Nodes.noteq(lhs, NodeValue.newBitVector(rhs)));
      }
    }

    return !atoms.isEmpty() ? Nodes.and(atoms) : null;
  }

  private Node getReplaceCondition(
      final MmuBufferAccess bufferAccess,
      final BitVector addressWithoutTag) {
    // FIXME:
    return getMissCondition(bufferAccess, addressWithoutTag);
  }

  private Collection<Node> getAddressConstraints(
      final MmuAddressInstance addressType,
      final MemoryDataType dataType) {
    final Collection<Node> constraints = new ArrayList<>();
    final MmuSubsystem memory = MmuPlugin.getSpecification();

    final GeneratorSettings settings = GeneratorSettings.get();
    final RegionSettings region = settings.getMemory().getRegion(memory.getName());

    final Variable addressVariable = addressType.getVariable().getVariable();

    // Range constraint.
    constraints.add(
        BitVectorConstraint.range(
          addressVariable,
          BitVector.valueOf(region.getMin(), addressType.getBitSize()),
          BitVector.valueOf(region.getMax(), addressType.getBitSize()))
    ); 

    // Alignment constraint (if required).
    if (dataType.getSizeInBytes() > 1) {
      constraints.add(
          Nodes.eq(
              Nodes.bvextract(dataType.getSizeInBytes() - 2, 0, addressVariable),
              NodeValue.newBitVector(0, dataType.getSizeInBytes() - 1)
          )
      );
    }

    return constraints;
  }

  private BitVector getRandomValue(final Variable variable) {
    final BitVector value = BitVector.newEmpty(variable.getType().getSize());
    Randomizer.get().fill(value);

    return value;
  }

  private BitVector allocateEntryId(final MmuBuffer buffer, final boolean peek) {
    final EntryIdAllocator entryIdAllocator = new EntryIdAllocator(GeneratorSettings.get());

    final BitVector entryId = entryIdAllocator.allocate(buffer, peek, null);
    Logger.debug("Allocate entry: buffer=%s, entryId=%s", buffer, entryId.toHexString());

    return entryId;
  }

  private Map<Variable, BitVector> refineAddress(
      final AddressObject addressObject,
      final MmuAddressInstance addressType,
      final NodeVariable dataVariable,
      final BitVector dataValue,
      final Collection<Node> conditions,
      final Collection<Node> constraints) {

    Logger.debug("Refine address: conditions=%s", conditions);
    Logger.debug("Refine address: constraints=%s", constraints);

    addressObject.setData(dataVariable, dataValue);

    final Collection<Node> allConstraints = new ArrayList<>(constraints);

    // Fix known values of the data.
    final Map<NodeVariable, BitVector> data = addressObject.getData();
    for (final Map.Entry<NodeVariable, BitVector> entry : data.entrySet()) {
      final NodeVariable variable = entry.getKey();
      final BitVector value = entry.getValue();

      allConstraints.add(Nodes.eq(variable, NodeValue.newBitVector(value)));
    }

    // Fix known values of the addresses.
    final Map<MmuAddressInstance, BitVector> addresses = addressObject.getAddresses();
    for (final Map.Entry<MmuAddressInstance, BitVector> entry : addresses.entrySet()) {
      final NodeVariable variable = entry.getKey().getVariable();
      final BitVector value = entry.getValue();

      allConstraints.add(Nodes.eq(variable, NodeValue.newBitVector(value)));
    }

    Logger.debug("Constraints for refinement: %s", allConstraints);

    final Map<Variable, BitVector> values =
        MemoryEngineUtils.generateData(
            addressObject.getAccess(),
            conditions,
            allConstraints,
            BitVectorVariableInitializer.RANDOM
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
      final MmuAddressInstance type = bufferAccess.getAddress();
      final BitVector value = values.get(type.getVariable().getVariable());

      if (value != null) {
        Logger.debug("Refine address: %s=0x%s", type, value.toHexString());
        addressObject.setAddress(type, value);
      }
    }

    // Get the virtual address value.
    final BitVector refinedAddressValue = values.get(addressType.getVariable().getVariable());
    InvariantChecks.checkNotNull(refinedAddressValue, "Cannot obtain the virtual address value");

    Logger.debug("Refine address: %s=0x%s", addressType, refinedAddressValue.toHexString());
    addressObject.setAddress(addressType, refinedAddressValue);

    return values;
  }

  private AddressObject getAddressObject(final AbstractCall abstractCall) {
    final Primitive primitive = abstractCall.getRootOperation();
    final Situation situation = primitive.getSituation();
    Logger.debug("MemoryDataGenerator.getAddressObject: situation=%s", situation);

    final TestData testData = situation.getTestData();
    InvariantChecks.checkNotNull(testData);
    Logger.debug("MemoryDataGenerator.getAddressObject: testData=%s", testData);

    return (AddressObject) testData.getBindings().get(MemoryDataGenerator.SOLUTION);
  }
}
