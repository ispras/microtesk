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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.basis.classifier.Classifier;
import ru.ispras.microtesk.basis.classifier.ClassifierTrivial;
import ru.ispras.microtesk.basis.classifier.ClassifierUniversal;
import ru.ispras.microtesk.basis.solver.SolverResult;
import ru.ispras.microtesk.mmu.basis.DataType;
import ru.ispras.microtesk.mmu.basis.MemoryOperation;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryAccessPath;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryAccessStructure;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryAccessStructureIterator;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryAccessType;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.allocator.AddressAllocator;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.allocator.EntryIdAllocator;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.classifier.ClassifierEventBased;
import ru.ispras.microtesk.mmu.translator.MmuTranslator;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuAddressType;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuSubsystem;
import ru.ispras.microtesk.settings.GeneratorSettings;
import ru.ispras.microtesk.settings.RegionSettings;
import ru.ispras.microtesk.test.sequence.engine.Engine;
import ru.ispras.microtesk.test.sequence.engine.EngineContext;
import ru.ispras.microtesk.test.sequence.engine.EngineResult;
import ru.ispras.microtesk.test.template.Call;
import ru.ispras.microtesk.utils.Range;
import ru.ispras.testbase.knowledge.iterator.Iterator;

/**
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class MemoryEngine implements Engine<MemorySolution> {
  public static final String ID = "memory";

  public static final String PARAM_CLASSIFIER = "classifier";
  public static final String PARAM_CLASSIFIER_TRIVIAL = "trivial";
  public static final String PARAM_CLASSIFIER_UNIVERSAL = "universal";
  public static final String PARAM_CLASSIFIER_EVENT_BASED = "event-based";
  public static final Classifier<MemoryAccessPath> PARAM_CLASSIFIER_DEFAULT =
      new ClassifierTrivial<MemoryAccessPath>();

  public static final String PARAM_PAGE_MASK = "page_mask";
  public static final long PARAM_PAGE_MASK_DEFAULT = 0x0fff;

  public static final String PARAM_ALIGN = "align";
  public static final DataType PARAM_ALIGN_DEFAULT = null;

  private static Classifier<MemoryAccessPath> getClassifier(final Object value) {
    final String id = value != null ? value.toString() : null;

    if (PARAM_CLASSIFIER_TRIVIAL.equals(id)) {
      return new ClassifierTrivial<MemoryAccessPath>();
    }
    if (PARAM_CLASSIFIER_UNIVERSAL.equals(id)) {
      return new ClassifierUniversal<MemoryAccessPath>();
    }
    if (PARAM_CLASSIFIER_EVENT_BASED.equals(id)) {
      return new ClassifierEventBased();
    }

    return PARAM_CLASSIFIER_DEFAULT;
  }

  private static long getPageMask(final Object value) {
    if (value == null) {
      return PARAM_PAGE_MASK_DEFAULT;
    }

    final Number id =
        value instanceof Number ? (Number) value : Long.parseLong(value.toString(), 16);

    return id.longValue();
  }

  private static DataType getAlign(final Object value) {
    if (value == null) {
      return PARAM_ALIGN_DEFAULT;
    }

    final Number id =
        value instanceof Number ? (Number) value : Long.parseLong(value.toString(), 10);

    return DataType.type(id.intValue());
  }

  private Classifier<MemoryAccessPath> classifier = PARAM_CLASSIFIER_DEFAULT;
  private long pageMask = PARAM_PAGE_MASK_DEFAULT;
  private DataType align = PARAM_ALIGN_DEFAULT;

  private AddressAllocator addressAllocator;
  private EntryIdAllocator entryIdAllocator;

  @Override
  public Class<MemorySolution> getSolutionClass() {
    return MemorySolution.class;
  }

  @Override
  public void configure(final Map<String, Object> attributes) {
    InvariantChecks.checkNotNull(attributes);

    classifier = getClassifier(attributes.get(PARAM_CLASSIFIER));
    pageMask = getPageMask(attributes.get(PARAM_PAGE_MASK));
    align = getAlign(attributes.get(PARAM_ALIGN));
  }

  @Override
  public EngineResult<MemorySolution> solve(
      final EngineContext engineContext, final List<Call> abstractSequence) {
    InvariantChecks.checkNotNull(engineContext);
    InvariantChecks.checkNotNull(abstractSequence);

    final Iterator<MemoryAccessStructure> structureIterator =
        getStructureIterator(engineContext, abstractSequence);

    final Iterator<MemorySolution> solutionIterator =
        getSolutionIterator(engineContext, structureIterator);

    final GeneratorSettings settings = engineContext.getSettings();
    final Map<MmuAddressType, Collection<? extends Range<Long>>> addressToRegions = new HashMap<>();
    final Collection<RegionSettings> regions = new ArrayList<>();

    for (final RegionSettings region : settings.getMemory().getRegions()) {
      if (region.isEnabled() && region.getType() == RegionSettings.Type.DATA) {
        regions.add(region);
      }
    }

    final MmuSubsystem memory = MmuTranslator.getSpecification();

    for (final MmuAddressType addrType : memory.getSortedListOfAddresses()) {
      if (addrType.equals(memory.getVirtualAddress())) {
        addressToRegions.put(addrType, memory.getSegments()); // TODO: memory.getSegments(addrType)
      } else if (addrType.equals(memory.getPhysicalAddress())) {
        addressToRegions.put(addrType, regions);
      } else {
        addressToRegions.put(addrType, Collections.<RegionSettings>emptyList());
      }
    }

    this.addressAllocator = new AddressAllocator(memory, addressToRegions);
    this.entryIdAllocator = new EntryIdAllocator(memory);

    return new EngineResult<MemorySolution>(solutionIterator);
  }

  public Collection<Long> getAllAddresses(
      final MmuAddressType addressType, final RegionSettings region) {
    InvariantChecks.checkNotNull(addressType);
    InvariantChecks.checkNotNull(region);

    return addressAllocator.getAllAddresses(addressType, region);
  }

  private Iterator<MemoryAccessStructure> getStructureIterator(
      final EngineContext engineContext,
      final List<Call> abstractSequence) {
    InvariantChecks.checkNotNull(engineContext);
    InvariantChecks.checkNotNull(abstractSequence);

    final MemoryEngineContext customContext =
        (MemoryEngineContext) engineContext.getCustomContext(ID);

    // TODO: Compatibility with MMU TestGen.
    final Iterator<MemoryAccessStructure> structureIterator =
        customContext != null ? customContext.getStructureIterator() : null;

    if (structureIterator != null) {
      return structureIterator;
    }

    final List<MemoryAccessType> accessTypes = new ArrayList<>();

    for (final Call abstractCall : abstractSequence) {
      InvariantChecks.checkTrue(abstractCall.isLoad() || abstractCall.isStore());

      final MemoryOperation operation =
          abstractCall.isLoad() ? MemoryOperation.LOAD : MemoryOperation.STORE;

      final int blockSizeInBits = abstractCall.getBlockSize();
      InvariantChecks.checkTrue((blockSizeInBits & 7) == 0);

      accessTypes.add(new MemoryAccessType(operation, DataType.type(blockSizeInBits >>> 3)));
    }

    return new MemoryAccessStructureIterator(
        accessTypes, classifier, engineContext.getSettings());
  }

  private Iterator<MemorySolution> getSolutionIterator(
      final EngineContext engineContext,
      final Iterator<MemoryAccessStructure> structureIterator) {
    InvariantChecks.checkNotNull(engineContext);
    InvariantChecks.checkNotNull(structureIterator);

    final MemoryEngineContext customContext =
        (MemoryEngineContext) engineContext.getCustomContext(ID);
    InvariantChecks.checkNotNull(customContext);

    return new Iterator<MemorySolution>() {
      private MemorySolution solution = null;

      private MemorySolution getSolution() {
        while (structureIterator.hasValue()) {
          final MemoryAccessStructure structure = structureIterator.value();

          // Reset the allocation tables before solving the constraint.
          addressAllocator.reset();
          entryIdAllocator.reset();

          final MemorySolver solver = new MemorySolver(
              structure, customContext, addressAllocator, entryIdAllocator,
              pageMask, align, engineContext.getSettings());

          final SolverResult<MemorySolution> result = solver.solve();
          InvariantChecks.checkNotNull(result);

          if (result.getStatus() == SolverResult.Status.SAT) {
            solution = result.getResult();
            break;
          }

          structureIterator.next();
        }

        return structureIterator.hasValue() ? solution : null;
      }

      @Override
      public void init() {
        structureIterator.init();
        solution = getSolution();
      }

      @Override
      public boolean hasValue() {
        return solution != null;
      }

      @Override
      public MemorySolution value() {
        return solution;
      }

      @Override
      public void next() {
        structureIterator.next();
        solution = getSolution();
      }

      @Override
      public void stop() {
        solution = null;
      }

      @Override
      public Iterator<MemorySolution> clone() {
        throw new UnsupportedOperationException();
      }
    };
  }
}
