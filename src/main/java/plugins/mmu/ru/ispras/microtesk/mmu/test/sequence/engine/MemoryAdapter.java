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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.basis.solver.integer.IntegerVariable;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.loader.Load;
import ru.ispras.microtesk.mmu.translator.MmuTranslator;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuAddressType;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuBuffer;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuEntry;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuSubsystem;
import ru.ispras.microtesk.model.api.exception.ConfigurationException;
import ru.ispras.microtesk.test.TestSequence;
import ru.ispras.microtesk.test.sequence.engine.Adapter;
import ru.ispras.microtesk.test.sequence.engine.AdapterResult;
import ru.ispras.microtesk.test.sequence.engine.EngineContext;
import ru.ispras.microtesk.test.sequence.engine.utils.EngineUtils;
import ru.ispras.microtesk.test.template.BufferPreparator;
import ru.ispras.microtesk.test.template.BufferPreparatorStore;
import ru.ispras.microtesk.test.template.Call;
import ru.ispras.microtesk.test.template.ConcreteCall;

/**
 * {@link MemoryAdapter} implements adapter of {@link MemorySolution}.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class MemoryAdapter implements Adapter<MemorySolution> {
  private final MmuSubsystem memory = MmuTranslator.getSpecification();

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

    // TODO: prepare data (ST only).
    // TODO: prepare addresses.

    // Convert the abstract sequence into the concrete one.
    builder.add(prepareSequence(engineContext, abstractSequence));

    return new AdapterResult(builder.build());
  }

  private List<ConcreteCall> prepareEntries(
      final EngineContext engineContext,
      final MemorySolution solution) {
    InvariantChecks.checkNotNull(engineContext);
    InvariantChecks.checkNotNull(solution);

    final List<ConcreteCall> sequence = new ArrayList<>(); 

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

    final Map<Long, MmuEntry> entries = solution.getEntries(buffer);
    InvariantChecks.checkNotNull(entries);

    for (final Map.Entry<Long, MmuEntry> entry : entries.entrySet()) {
      final long index = entry.getKey();
      final MmuEntry data = entry.getValue();

      final BitVector addressValue = BitVector.valueOf(index, Long.SIZE);
      final Map<String, BitVector> entryFieldValues = new LinkedHashMap<>();

      for (final IntegerVariable field : data.getVariables()) {
        final String entryFieldName = field.getName();
        final BigInteger entryFieldValue = data.getValue(field);

        entryFieldValues.put(entryFieldName, BitVector.valueOf(entryFieldValue, field.getWidth()));
      }

      final List<ConcreteCall> initializer = makeInitializer(
          buffer, engineContext, addressValue, entryFieldValues);
      InvariantChecks.checkNotNull(initializer);

      preparation.addAll(initializer);
    }

    return preparation;
  }

  private List<ConcreteCall> prepareData(
      final EngineContext engineContext,
      final MemorySolution solution) {
    InvariantChecks.checkNotNull(engineContext);
    InvariantChecks.checkNotNull(solution);

    final List<ConcreteCall> preparation = new ArrayList<>(); 

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
      final BitVector address = BitVector.valueOf(load.getAddress(), addressType.getWidth());

      final List<ConcreteCall> initializer = makeInitializer(
          load.getBuffer(), engineContext, address, Collections.<String, BitVector>emptyMap());
      InvariantChecks.checkNotNull(initializer);

      preparation.addAll(initializer);
    }

    return preparation;
  }

  private List<ConcreteCall> makeInitializer(
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
    InvariantChecks.checkNotNull(preparator, "Preparator is null");

    final List<Call> abstractInitializer = preparator.makeInitializer(address, entry);
    InvariantChecks.checkNotNull(abstractInitializer, "Abstract initializer is null");

    return prepareSequence(engineContext, abstractInitializer);
  }

  private List<ConcreteCall> prepareSequence(
      final EngineContext engineContext,
      final List<Call> abstractSequence) {
    InvariantChecks.checkNotNull(engineContext);
    InvariantChecks.checkNotNull(abstractSequence);

    try {
      return EngineUtils.makeConcreteCalls(engineContext, abstractSequence);
    } catch (final ConfigurationException e) {
      InvariantChecks.checkTrue(false, "Configuration exception");
      return null;
    }
  }
}
