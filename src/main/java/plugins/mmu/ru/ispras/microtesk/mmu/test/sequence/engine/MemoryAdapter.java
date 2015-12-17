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
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.basis.solver.integer.IntegerVariable;
import ru.ispras.microtesk.mmu.MmuPlugin;
import ru.ispras.microtesk.mmu.basis.BufferAccessEvent;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.loader.Load;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuAddressType;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuBuffer;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuEntry;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuSubsystem;
import ru.ispras.microtesk.model.api.exception.ConfigurationException;
import ru.ispras.microtesk.test.TestSequence;
import ru.ispras.microtesk.test.sequence.engine.Adapter;
import ru.ispras.microtesk.test.sequence.engine.AdapterResult;
import ru.ispras.microtesk.test.sequence.engine.EngineContext;
import ru.ispras.microtesk.test.sequence.engine.utils.AddressingModeWrapper;
import ru.ispras.microtesk.test.sequence.engine.utils.EngineUtils;
import ru.ispras.microtesk.test.template.BufferPreparator;
import ru.ispras.microtesk.test.template.BufferPreparatorStore;
import ru.ispras.microtesk.test.template.Call;
import ru.ispras.microtesk.test.template.ConcreteCall;
import ru.ispras.microtesk.test.template.Primitive;
import ru.ispras.microtesk.test.template.Situation;
import ru.ispras.microtesk.test.testbase.AddressDataGenerator;

/**
 * {@link MemoryAdapter} implements adapter of {@link MemorySolution}.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class MemoryAdapter implements Adapter<MemorySolution> {
  @Override
  public Class<MemorySolution> getSolutionClass() {
    return MemorySolution.class;
  }

  @Override
  public void configure(final Map<String, Object> attributes) {
    // Do nothing.
  }

  @Override
  public AdapterResult adapt(
      final EngineContext engineContext,
      final List<Call> abstractSequence,
      final MemorySolution solution) {
    InvariantChecks.checkNotNull(engineContext);
    InvariantChecks.checkNotNull(abstractSequence);
    InvariantChecks.checkNotNull(solution);

    final TestSequence.Builder builder = new TestSequence.Builder();

    // Write entries into the non-replaceable buffers.
    builder.addToPrologue(prepareEntries(engineContext, solution));
    // Load data into the replaceable buffers.
    builder.addToPrologue(prepareData(engineContext, solution));
    // Load addresses and data into the registers.
    builder.addToPrologue(prepareAddresses(engineContext, abstractSequence, solution));

    // Convert the abstract sequence into the concrete one.
    builder.add(prepareSequence(engineContext, abstractSequence));

    final TestSequence sequence = builder.build();

    final long baseAddress = engineContext.getAddress();
    final long newAddress = sequence.setAddress(baseAddress);
    engineContext.setAddress(newAddress);

    return new AdapterResult(sequence);
  }

  private List<ConcreteCall> prepareEntries(
      final EngineContext engineContext,
      final MemorySolution solution) {
    InvariantChecks.checkNotNull(engineContext);
    InvariantChecks.checkNotNull(solution);

    final List<ConcreteCall> sequence = new ArrayList<>(); 
    final MmuSubsystem memory = MmuPlugin.getSpecification();

    for (final MmuBuffer buffer : memory.getSortedListOfBuffers()) {
      if (!buffer.isReplaceable()) {
        sequence.addAll(prepareEntries(buffer, engineContext, solution));
      }
    }

    return sequence;
  }

  private List<ConcreteCall> prepareEntries(
      final MmuBuffer buffer,
      final EngineContext engineContext,
      final MemorySolution solution) {
    InvariantChecks.checkNotNull(buffer);
    InvariantChecks.checkNotNull(engineContext);
    InvariantChecks.checkNotNull(solution);

    final List<ConcreteCall> preparation = new ArrayList<>();

    final Map<Long, EntryObject> entries = solution.getEntries(buffer);
    InvariantChecks.checkNotNull(entries);

    for (final Map.Entry<Long, EntryObject> entry : entries.entrySet()) {
      final long index = entry.getKey();
      final MmuEntry data = entry.getValue().getEntry();

      final BitVector addressValue = BitVector.valueOf(index, Long.SIZE);
      final Map<String, BitVector> entryFieldValues = new LinkedHashMap<>();

      for (final IntegerVariable field : data.getVariables()) {
        final String entryFieldName = field.getName();
        final BigInteger entryFieldValue = data.getValue(field);

        entryFieldValues.put(entryFieldName, BitVector.valueOf(entryFieldValue, field.getWidth()));
      }

      final List<ConcreteCall> initializer = prepareBuffer(
          buffer, engineContext, addressValue, entryFieldValues);
      InvariantChecks.checkNotNull(initializer);

      if (!initializer.isEmpty()) {
        preparation.add(ConcreteCall.newLine());
        preparation.add(ConcreteCall.newComment(String.format(
            "Initializing %s: Goal=WRITE[0x%x], Entry=%s", buffer.getName(), index, data)));

        preparation.addAll(initializer);
      }
    }

    return preparation;
  }

  private List<ConcreteCall> prepareData(
      final EngineContext engineContext,
      final MemorySolution solution) {
    InvariantChecks.checkNotNull(engineContext);
    InvariantChecks.checkNotNull(solution);

    final List<ConcreteCall> preparation = new ArrayList<>(); 
    final MmuSubsystem memory = MmuPlugin.getSpecification();

    for (final MmuAddressType addressType : memory.getSortedListOfAddresses()) {
      preparation.addAll(prepareData(addressType, engineContext, solution));
    }

    return preparation;
  }

  private List<ConcreteCall> prepareData(
      final MmuAddressType addressType,
      final EngineContext engineContext,
      final MemorySolution solution) {
    InvariantChecks.checkNotNull(addressType);
    InvariantChecks.checkNotNull(engineContext);
    InvariantChecks.checkNotNull(solution);

    final List<ConcreteCall> preparation = new ArrayList<>();

    final List<Load> loads = solution.getLoader().prepareLoads(addressType);
    InvariantChecks.checkNotNull(loads);

    // Load data into the buffers.
    for (final Load load : loads) {
      final MmuBuffer buffer = load.getBuffer();
      final BufferAccessEvent targetEvent = load.getTargetEvent();

      final long targetAddress = load.getTargetAddress();
      final long targetIndex = buffer.getIndex(targetAddress);
      final long targetTag = buffer.getTag(targetAddress);

      final long currentAddress = load.getAddress();
      final long currentTag = buffer.getTag(currentAddress);

      final BitVector address = BitVector.valueOf(currentAddress, addressType.getWidth());

      final List<ConcreteCall> initializer = prepareBuffer(
          buffer, engineContext, address, Collections.<String, BitVector>emptyMap());
      InvariantChecks.checkNotNull(initializer);

      if (!initializer.isEmpty()) {
        preparation.add(ConcreteCall.newLine());
        preparation.add(ConcreteCall.newComment(String.format(
            "Initializing %s: Goal=%s[0x%x] (Index=0x%x, Tag=0x%x), Address=0x%x (Tag=0x%x)",
            buffer.getName(), targetEvent, targetAddress, targetIndex, targetTag,
            currentAddress, currentTag)));

        preparation.addAll(initializer);
      }
    }

    return preparation;
  }

  private List<ConcreteCall> prepareBuffer(
      final MmuBuffer buffer,
      final EngineContext engineContext,
      final BitVector address,
      final Map<String, BitVector> entry) {
    InvariantChecks.checkNotNull(buffer);
    InvariantChecks.checkNotNull(engineContext);
    InvariantChecks.checkNotNull(address);
    InvariantChecks.checkNotNull(entry);

    final BufferPreparatorStore preparators = engineContext.getBufferPreparators();
    InvariantChecks.checkNotNull(preparators, "Preparator store is null");

    final BufferPreparator preparator = preparators.getPreparatorFor(buffer.getName());
    InvariantChecks.checkNotNull(preparator, "Missing preparator for " + buffer.getName());

    final List<Call> abstractInitializer =
        preparator.makeInitializer(engineContext.getPreparators(), address, entry);
    InvariantChecks.checkNotNull(abstractInitializer, "Abstract initializer is null");

    final List<ConcreteCall> concreteCalls =
        prepareSequence(engineContext, abstractInitializer);

    Logger.debug("Code:");
    for (final ConcreteCall concreteCall : concreteCalls) {
      Logger.debug(concreteCall.getText());
    }
    Logger.debug("");

    return concreteCalls;
  }

  private List<ConcreteCall> prepareSequence(
      final EngineContext engineContext,
      final List<Call> abstractSequence) {
    InvariantChecks.checkNotNull(engineContext);
    InvariantChecks.checkNotNull(abstractSequence);

    try {
      return EngineUtils.makeConcreteCalls(engineContext, abstractSequence);
    } catch (final ConfigurationException e) {
      InvariantChecks.checkTrue(false, e.getMessage());
      return null;
    }
  }

  private List<ConcreteCall> prepareAddresses(
      final EngineContext engineContext,
      final List<Call> abstractSequence,
      final MemorySolution solution) {
    InvariantChecks.checkNotNull(engineContext);
    InvariantChecks.checkNotNull(abstractSequence);
    InvariantChecks.checkNotNull(solution);

    final List<ConcreteCall> preparation = new ArrayList<>();
    final Set<AddressingModeWrapper> initializedModes = new LinkedHashSet<>();

    int i = 0;

    for (final Call abstractCall : abstractSequence) {
      if (!MemoryEngine.isMemoryAccessWithSituation(abstractCall)) {
        continue;
      }

      final AddressObject addressObject = solution.getAddressObject(i);

      final List<ConcreteCall> initializer = prepareAddress(
          engineContext, abstractCall, addressObject, initializedModes);
      InvariantChecks.checkNotNull(initializer);

      Logger.debug("Call preparation: %s", initializer);

      if (!initializer.isEmpty()) {
        preparation.add(ConcreteCall.newLine());
        preparation.add(ConcreteCall.newComment(String.format(
            "Initializing Instruction %d: %s", i, addressObject)));

        preparation.addAll(initializer);
      }

      i++;
    }

    return preparation;
  }

  private List<ConcreteCall> prepareAddress(
      final EngineContext engineContext,
      final Call abstractCall,
      final AddressObject addressObject,
      final Set<AddressingModeWrapper> initializedModes) {
    InvariantChecks.checkNotNull(engineContext);
    InvariantChecks.checkNotNull(abstractCall);
    InvariantChecks.checkNotNull(initializedModes);

    InvariantChecks.checkTrue(MemoryEngine.isMemoryAccessWithSituation(abstractCall));

    final Primitive primitive = abstractCall.getRootOperation();
    InvariantChecks.checkNotNull(primitive, "Primitive is null");

    final Situation situation = primitive.getSituation();
    InvariantChecks.checkNotNull(situation, "Situation is null");

    final Map<String, Object> attributes = situation.getAttributes();
    InvariantChecks.checkNotNull(attributes, "Attributes map is null");

    // Specify the situation's parameter (address value).
    final Map<String, Object> newAttributes = new HashMap<>(attributes);
    newAttributes.put(AddressDataGenerator.PARAM_ADDRESS_VALUE, addressObject.getVirtualAddress());

    final Situation newSituation = new Situation(situation.getName(), newAttributes);

    try {
      final List<Call> abstractInitializer = EngineUtils.makeInitializer(
          engineContext, primitive, newSituation, initializedModes);
      InvariantChecks.checkNotNull(abstractInitializer, "Abstract initializer is null");

      return prepareSequence(engineContext, abstractInitializer);
    } catch (final ConfigurationException e) {
      InvariantChecks.checkTrue(false, e.getMessage());
      return null;
    }
  }
}
