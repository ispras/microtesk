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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.basis.solver.integer.IntegerVariable;
import ru.ispras.microtesk.mmu.MmuPlugin;
import ru.ispras.microtesk.mmu.basis.BufferAccessEvent;
import ru.ispras.microtesk.mmu.basis.MemoryAccessContext;
import ru.ispras.microtesk.mmu.basis.MemoryAccessStack;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuAddressInstance;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuBuffer;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuBufferAccess;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuEntry;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuSubsystem;
import ru.ispras.microtesk.model.api.ConfigurationException;
import ru.ispras.microtesk.test.TestSequence;
import ru.ispras.microtesk.test.sequence.engine.Adapter;
import ru.ispras.microtesk.test.sequence.engine.AdapterResult;
import ru.ispras.microtesk.test.sequence.engine.EngineContext;
import ru.ispras.microtesk.test.sequence.engine.utils.AddressingModeWrapper;
import ru.ispras.microtesk.test.sequence.engine.utils.EngineUtils;
import ru.ispras.microtesk.test.template.BlockId;
import ru.ispras.microtesk.test.template.BufferPreparator;
import ru.ispras.microtesk.test.template.BufferPreparatorStore;
import ru.ispras.microtesk.test.template.Call;
import ru.ispras.microtesk.test.template.ConcreteCall;
import ru.ispras.microtesk.test.template.DataDirectiveFactory;
import ru.ispras.microtesk.test.template.DataSectionBuilder;
import ru.ispras.microtesk.test.template.DataSectionBuilder.DataValueBuilder;
import ru.ispras.microtesk.test.template.Primitive;
import ru.ispras.microtesk.test.template.Situation;
import ru.ispras.microtesk.test.testbase.AddressDataGenerator;

/**
 * {@link MemoryAdapter} implements adapter of {@link MemorySolution}.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class MemoryAdapter implements Adapter<MemorySolution> {

  static final MemoryEngine.ParamPreparator PARAM_PREPARATOR = MemoryEngine.PARAM_PREPARATOR;
  private boolean isStaticPreparator = PARAM_PREPARATOR.getDefaultValue();

  private final Set<BigInteger> entriesInDataSection = new HashSet<>();

  @Override
  public Class<MemorySolution> getSolutionClass() {
    return MemorySolution.class;
  }

  @Override
  public void configure(final Map<String, Object> attributes) {
    InvariantChecks.checkNotNull(attributes);
    isStaticPreparator = PARAM_PREPARATOR.parse(attributes.get(PARAM_PREPARATOR.getName()));
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
    // Load addresses and data into the registers.
    builder.addToPrologue(prepareAddresses(engineContext, abstractSequence, solution));

    // Convert the abstract sequence into the concrete one.
    builder.add(prepareSequence(engineContext, abstractSequence));

    final TestSequence sequence = builder.build();
    return new AdapterResult(sequence);
  }

  @Override
  public void onStartProgram() {
    entriesInDataSection.clear();
  }

  @Override
  public void onEndProgram() {
    Logger.debug("Allocated entries: %s", entriesInDataSection);
  }

  private List<ConcreteCall> prepareEntries(
      final EngineContext engineContext,
      final MemorySolution solution) {
    InvariantChecks.checkNotNull(engineContext);
    InvariantChecks.checkNotNull(solution);

    final List<ConcreteCall> sequence = new ArrayList<>(); 
    final Map<MmuBufferAccess, Map<BigInteger, EntryObject>> entries = solution.getEntries();

    for (final Map.Entry<MmuBufferAccess, Map<BigInteger, EntryObject>> entry : entries.entrySet()) {
      final MmuBufferAccess bufferAccess = entry.getKey();
      sequence.addAll(prepareEntries(bufferAccess, engineContext, solution, entriesInDataSection));
    }

    return sequence;
  }

  private List<ConcreteCall> prepareEntries(
      final MmuBufferAccess bufferAccess,
      final EngineContext engineContext,
      final MemorySolution solution,
      final Set<BigInteger> entriesInDataSection) {
    InvariantChecks.checkNotNull(bufferAccess);
    InvariantChecks.checkNotNull(engineContext);
    InvariantChecks.checkNotNull(solution);
    InvariantChecks.checkNotNull(entriesInDataSection);

    final MmuBuffer buffer = bufferAccess.getBuffer();

    final BlockId blockId = new BlockId();
    final DataDirectiveFactory dataDirectiveFactory = engineContext.getDataDirectiveFactory();
    InvariantChecks.checkNotNull(dataDirectiveFactory);

    final List<ConcreteCall> preparation = new ArrayList<>();

    final Map<BigInteger, EntryObject> entries = solution.getEntries(bufferAccess);
    InvariantChecks.checkNotNull(entries);

    for (final Map.Entry<BigInteger, EntryObject> entry : entries.entrySet()) {
      final BigInteger index = entry.getKey();
      final MmuEntry data = entry.getValue().getEntry();
      final BigInteger bufferAccessAddress = data.getAddress();

      Logger.debug("Entry preparation: index=0x%s, address=0x%s",
          index.toString(16), bufferAccessAddress.toString(16));

      final BitVector addressValue = BitVector.valueOf(index, Long.SIZE);
      final Map<String, BitVector> entryFieldValues = new LinkedHashMap<>();

      for (final IntegerVariable field : data.getVariables()) {
        final String entryFieldName = field.getName();
        final BigInteger entryFieldValue = data.getValue(field);

        entryFieldValues.put(entryFieldName, BitVector.valueOf(entryFieldValue, field.getWidth()));
      }

      final boolean isMemoryMapped = buffer.getKind() == MmuBuffer.Kind.MEMORY;
      final boolean isEntryInDataSection = entriesInDataSection.contains(bufferAccessAddress);

      final MemoryAccessContext context = bufferAccess.getContext();
      final int bufferAccessId = context.getBufferAccessId(buffer);

      final String level = context.getMemoryAccessStack().isEmpty()
          ? String.format("%d", bufferAccessId)
          : String.format("%d.%d", context.getMemoryAccessStack().size(), bufferAccessId);

      final String comment = String.format("%s(%s)=%s", buffer.getName(), level, data);

      if (isMemoryMapped && isStaticPreparator && !isEntryInDataSection) {
        Logger.debug("Entries in data section: %s", entriesInDataSection);

        final DataSectionBuilder dataSectionBuilder = new DataSectionBuilder(
            blockId, dataDirectiveFactory, true /* Global section */, false /* Same file */);

        dataSectionBuilder.setVirtualAddress(bufferAccessAddress);
        dataSectionBuilder.addComment(comment);

        final List<BitVector> fieldValues = new ArrayList<>(entryFieldValues.values());
        Collections.reverse(fieldValues);

        final BitVector entryValue =
            BitVector.newMapping(fieldValues.toArray(new BitVector[fieldValues.size()]));

        final int sizeInBits = entryValue.getBitSize();
        InvariantChecks.checkTrue((sizeInBits & 0x3) == 0);

        final int maxItemSizeInBits = dataDirectiveFactory.getMaxTypeBitSize();

        int itemSizeInBits = 8;
        while (itemSizeInBits < sizeInBits && itemSizeInBits < maxItemSizeInBits) {
          itemSizeInBits <<= 1;
        }

        final DataValueBuilder dataValueBuilder =
            dataSectionBuilder.addDataValuesForSize(itemSizeInBits);

        for (int i = 0; i < sizeInBits; i += itemSizeInBits) {
          final BitVector item = entryValue.field(i, i + itemSizeInBits - 1);
          dataValueBuilder.add(item.bigIntegerValue());

          // Static buffer initialization.
          entriesInDataSection.add(bufferAccessAddress.add(BigInteger.valueOf(i >>> 3)));
        }

        dataValueBuilder.build();

        final Call abstractCall = Call.newData(dataSectionBuilder.build());
        final ConcreteCall concreteCall = new ConcreteCall(abstractCall);

        preparation.add(concreteCall);
      } else {
        // Dynamic buffer initialization.
        final List<ConcreteCall> initializer = prepareBuffer(
            buffer, engineContext, addressValue, entryFieldValues);
        InvariantChecks.checkNotNull(initializer);

        if (!initializer.isEmpty()) {
          preparation.add(ConcreteCall.newLine());
          preparation.add(ConcreteCall.newComment(comment));

          preparation.addAll(initializer);
        }
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
    final Set<AddressingModeWrapper> initializedModes = new HashSet<>();

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
        preparation.add(ConcreteCall.newComment(String.format("Initializing Instruction %d:", i)));

        for (final String comment : getComments(addressObject)) {
          preparation.add(ConcreteCall.newComment(comment));
        }

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

    final MmuSubsystem memory = MmuPlugin.getSpecification();

    final Primitive primitive = abstractCall.getRootOperation();
    InvariantChecks.checkNotNull(primitive, "Primitive is null");

    final Situation situation = primitive.getSituation();
    InvariantChecks.checkNotNull(situation, "Situation is null");

    final Map<String, Object> attributes = situation.getAttributes();
    InvariantChecks.checkNotNull(attributes, "Attributes map is null");

    // Specify the situation's parameter (address value).
    final Map<String, Object> newAttributes = new HashMap<>(attributes);
    newAttributes.put(AddressDataGenerator.PARAM_ADDRESS_VALUE,
        addressObject.getAddress(memory.getVirtualAddress()));

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

  private String getMemoryAccessComment(final AddressObject addressObject) {
    InvariantChecks.checkNotNull(addressObject);

    final MmuSubsystem memory = MmuPlugin.getSpecification();
    final MmuAddressInstance virtualAddressType = memory.getVirtualAddress();
    final BigInteger virtualAddressValue = addressObject.getAddress(virtualAddressType);

    return String.format("%s[0x%s]", memory.getName(), virtualAddressValue.toString(16));
  }

  private String getBufferAccessComment(
      final AddressObject addressObject,
      final MmuBufferAccess bufferAccess) {
    InvariantChecks.checkNotNull(addressObject);
    InvariantChecks.checkNotNull(bufferAccess);

    final StringBuilder builder = new StringBuilder();

    final MmuBuffer buffer = bufferAccess.getBuffer();
    final BufferAccessEvent event = bufferAccess.getEvent();

    final MemoryAccessContext context = bufferAccess.getContext();
    final MemoryAccessStack stack = context.getMemoryAccessStack();

    final MmuAddressInstance addressType = bufferAccess.getAddress();
    final BigInteger addressValue = addressObject.getAddress(addressType);

    for (int i = 0; i <= stack.size(); i++) {
      builder.append("  ");
    }

    // The address may be undefined for buffer writes.
    builder.append(String.format("%-5s %s[%s]", event, buffer.getName(),
        addressValue != null ? String.format("0x%s", addressValue.toString(16)) : "<unknown>"));

    final EntryObject entryObject = addressObject.getEntry(bufferAccess);

    if (entryObject != null) {
      builder.append(String.format("=%s", entryObject.getEntry()));
    }

    return builder.toString();
  }

  private Collection<String> getComments(final AddressObject addressObject) {
    InvariantChecks.checkNotNull(addressObject);

    final Collection<String> comments = new ArrayList<>();
    comments.add(getMemoryAccessComment(addressObject));

    for (final MmuBufferAccess bufferAccess : addressObject.getAccess().getPath().getBufferAccesses()) {
      comments.add(getBufferAccessComment(addressObject, bufferAccess));
    }

    return comments;
  }
}
