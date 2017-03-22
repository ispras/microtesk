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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.basis.solver.Solver;
import ru.ispras.microtesk.basis.solver.SolverResult;
import ru.ispras.microtesk.mmu.MmuPlugin;
import ru.ispras.microtesk.mmu.basis.DataType;
import ru.ispras.microtesk.mmu.basis.MemoryAccessConstraints;
import ru.ispras.microtesk.mmu.basis.MemoryAccessType;
import ru.ispras.microtesk.mmu.basis.MemoryOperation;
import ru.ispras.microtesk.mmu.model.api.BufferObserver;
import ru.ispras.microtesk.mmu.model.api.MmuModel;
import ru.ispras.microtesk.mmu.settings.MmuSettingsUtils;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.allocator.AddressAllocator;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.allocator.EntryIdAllocator;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.coverage.CoverageExtractor;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.coverage.MemoryAccessPathChooser;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.coverage.MemoryGraphAbstraction;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.iterator.MemoryAccessStructureIterator;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuAddressInstance;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuBuffer;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuSubsystem;
import ru.ispras.microtesk.settings.GeneratorSettings;
import ru.ispras.microtesk.settings.RegionSettings;
import ru.ispras.microtesk.test.sequence.engine.Engine;
import ru.ispras.microtesk.test.sequence.engine.EngineContext;
import ru.ispras.microtesk.test.sequence.engine.EngineParameter;
import ru.ispras.microtesk.test.sequence.engine.EngineResult;
import ru.ispras.microtesk.test.template.Call;
import ru.ispras.microtesk.test.template.Primitive;
import ru.ispras.microtesk.test.template.Situation;
import ru.ispras.microtesk.utils.Range;
import ru.ispras.microtesk.utils.function.Predicate;
import ru.ispras.testbase.knowledge.iterator.Iterator;

/**
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class MemoryEngine implements Engine<MemorySolution> {
  public static final String ID = "memory";

  final static class ParamAbstraction extends EngineParameter<MemoryGraphAbstraction> {
    ParamAbstraction() {
      super("classifier",
          new EngineParameter.Option<>("buffer-access", MemoryGraphAbstraction.BUFFER_ACCESS),
          new EngineParameter.Option<>("trivial", MemoryGraphAbstraction.TRIVIAL),
          new EngineParameter.Option<>("universal", MemoryGraphAbstraction.UNIVERSAL));
    }
  }

  final static class ParamPreparator extends EngineParameter<Boolean> {
    ParamPreparator() {
      super("preparator",
          new EngineParameter.Option<>("static", Boolean.TRUE),
          new EngineParameter.Option<>("dynamic", Boolean.FALSE));
    }
  }

  final static class ParamIterator extends EngineParameter<MemoryAccessStructureIterator.Mode> {
    ParamIterator() {
      super("iterator",
          new EngineParameter.Option<>("static", MemoryAccessStructureIterator.Mode.RANDOM),
          new EngineParameter.Option<>("dynamic", MemoryAccessStructureIterator.Mode.EXHAUSTIVE));
    }
  }

  final static class ParamCount extends EngineParameter<Integer> {
    ParamCount() {
      super("count");
    }

    @Override
    public Integer getValue(final Object option) {
      final Number number = (option instanceof Number)
          ? (Number) option : Integer.parseInt(option.toString(), 10);

      return number.intValue();
    }

    @Override
    public Integer getDefaultValue() {
      return 1;
    }
  }

  final static class ParamPageMask extends EngineParameter<Long> {
    ParamPageMask() {
      super("page_mask");
    }

    @Override
    public Long getValue(final Object option) {
      final Number number = (option instanceof Number)
          ? (Number) option : Long.parseLong(option.toString(), 16);

      return number.longValue();
    }

    @Override
    public Long getDefaultValue() {
      // 4KB pages.
      return 0x0fffL;
    }
  }

  final static class ParamAlign extends EngineParameter<DataType> {
    ParamAlign() {
      super("align");
    }

    @Override
    public DataType getValue(final Object option) {
      final Number number = (option instanceof Number)
          ? (Number) option : Integer.parseInt(option.toString(), 10);

      return DataType.type(number.intValue());
    }

    @Override
    public DataType getDefaultValue() {
      return null;
    }
  }

  static final ParamAbstraction PARAM_ABSTRACTION = new ParamAbstraction();
  static final ParamPreparator PARAM_PREPARATOR = new ParamPreparator();
  static final ParamIterator PARAM_ITERATOR = new ParamIterator();
  static final ParamCount PARAM_COUNT = new ParamCount();
  static final ParamPageMask PARAM_PAGE_MASK = new ParamPageMask();
  static final ParamAlign PARAM_ALIGN = new ParamAlign();

  public static boolean isMemoryAccessWithSituation(final Call abstractCall) {
    InvariantChecks.checkNotNull(abstractCall);

    if (!abstractCall.isLoad() && !abstractCall.isStore()) {
      return false;
    }

    final Primitive primitive = abstractCall.getRootOperation();
    InvariantChecks.checkNotNull(primitive, "Primitive is null");

    final Situation situation = primitive.getSituation();
    return situation != null;
  }

  public static MemoryAccessConstraints getMemoryAccessConstraints(final Call abstractCall) {
    if (!isMemoryAccessWithSituation(abstractCall)) {
      return null;
    }

    final Primitive primitive = abstractCall.getRootOperation();
    final Situation situation = primitive.getSituation();

    final Object attribute = situation.getAttribute("path");
    if (null == attribute) {
      return null;
    }

    if (attribute instanceof MemoryAccessConstraints) {
      return (MemoryAccessConstraints) attribute;
    }

    Logger.warning("Unexpected format of the path attribute of a test situation: %s", attribute);
    return null;
  }

  private MemoryGraphAbstraction abstraction = PARAM_ABSTRACTION.getDefaultValue();
  private boolean preparator = PARAM_PREPARATOR.getDefaultValue();
  private MemoryAccessStructureIterator.Mode iterator = PARAM_ITERATOR.getDefaultValue();
  private int count = PARAM_COUNT.getDefaultValue();
  private long pageMask = PARAM_PAGE_MASK.getDefaultValue();
  private DataType align = PARAM_ALIGN.getDefaultValue();

  private AddressAllocator addressAllocator;
  private EntryIdAllocator entryIdAllocator;

  private Map<MmuAddressInstance, Predicate<Long>> hitCheckers;
  private MemoryAccessPathChooser normalPathChooser;

  @Override
  public Class<MemorySolution> getSolutionClass() {
    return MemorySolution.class;
  }

  @Override
  public void configure(final Map<String, Object> attributes) {
    InvariantChecks.checkNotNull(attributes);

    abstraction = PARAM_ABSTRACTION.parse(attributes.get(PARAM_ABSTRACTION.getName()));
    preparator = PARAM_PREPARATOR.parse(attributes.get(PARAM_PREPARATOR.getName()));
    iterator = PARAM_ITERATOR.parse(attributes.get(PARAM_ITERATOR.getName()));
    count = PARAM_COUNT.parse(attributes.get(PARAM_COUNT.getName()));
    pageMask = PARAM_PAGE_MASK.parse(attributes.get(PARAM_PAGE_MASK.getName()));
    align = PARAM_ALIGN.parse(attributes.get(PARAM_ALIGN.getName()));

    Logger.debug("Memory engine configuration: %s=%s, %s=%b, %s=%s, %s=%d, %s=0x%x, %s=%s",
        PARAM_ABSTRACTION, abstraction,
        PARAM_PREPARATOR, preparator,
        PARAM_ITERATOR, iterator,
        PARAM_COUNT, count,
        PARAM_PAGE_MASK, pageMask,
        PARAM_ALIGN, align);
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
    final Map<MmuAddressInstance, Collection<? extends Range<Long>>> addressToRegions = new HashMap<>();
    final Collection<RegionSettings> regions = new ArrayList<>();

    for (final RegionSettings region : settings.getMemory().getRegions()) {
      if (region.isEnabled() && region.getType() == RegionSettings.Type.DATA) {
        regions.add(region);
      }
    }

    Logger.debug("Memory engine: regions=%s", regions);

    final MmuSubsystem memory = MmuPlugin.getSpecification();

    for (final MmuAddressInstance addrType : memory.getSortedListOfAddresses()) {
      if (addrType.equals(memory.getVirtualAddress())) {
        addressToRegions.put(addrType, memory.getSegments());
      } else if (addrType.equals(memory.getPhysicalAddress())) {
        addressToRegions.put(addrType, regions);
      } else {
        addressToRegions.put(addrType, Collections.<RegionSettings>emptyList());
      }
    }

    this.addressAllocator = new AddressAllocator(addressToRegions);
    this.entryIdAllocator = new EntryIdAllocator(settings);

    final Map<MmuAddressInstance, Predicate<Long>> hitCheckers = new LinkedHashMap<>();

    for (final MmuAddressInstance addressType : memory.getSortedListOfAddresses()) {
      hitCheckers.put(addressType, new Predicate<Long>() {
        @Override
        public boolean test(final Long address) {
          final MmuModel model = MmuPlugin.getMmuModel();
          final MmuSubsystem memory = MmuPlugin.getSpecification();

          for (final MmuBuffer buffer : memory.getSortedListOfBuffers()) {
            if (!buffer.isFake()
                && buffer.getAddress().equals(addressType)
                && buffer != memory.getTargetBuffer()) {
              Logger.debug("Hit checker: %s", buffer);

              final BufferObserver observer = model.getBufferObserver(buffer.getName());

              if (observer.isHit(BitVector.valueOf(address, addressType.getWidth()))) {
                return true;
              }
            }
          }

          return false;
        }
      });
    }

    this.hitCheckers = hitCheckers;

    this.normalPathChooser = CoverageExtractor.get().getPathChooser(
        memory,
        MemoryGraphAbstraction.TARGET_BUFFER_ACCESS,
        MemoryAccessType.LOAD(DataType.BYTE),
        new MemoryAccessConstraints.Builder().build(),
        true);

    return new EngineResult<MemorySolution>(solutionIterator);
  }

  public Collection<Long> getAllAddresses(
      final MmuAddressInstance addressType, final RegionSettings region) {
    InvariantChecks.checkNotNull(addressType);
    InvariantChecks.checkNotNull(region);

    return addressAllocator.getAllAddresses(addressType, region);
  }

  private Iterator<MemoryAccessStructure> getStructureIterator(
      final EngineContext engineContext,
      final List<Call> abstractSequence) {
    InvariantChecks.checkNotNull(engineContext);
    InvariantChecks.checkNotNull(abstractSequence);

    final List<MemoryAccessType> accessTypes = new ArrayList<>();
    final List<MemoryAccessConstraints> accessConstraints = new ArrayList<>();

    for (final Call abstractCall : abstractSequence) {
      if(!isMemoryAccessWithSituation(abstractCall)) {
        // Skip non memory access instructions and memory accesses instructions without situations.
        continue;
      }

      final MemoryOperation operation =
          abstractCall.isLoad() ? MemoryOperation.LOAD : MemoryOperation.STORE;

      final MemoryAccessConstraints constraints =
          getMemoryAccessConstraints(abstractCall);

      final int blockSizeInBits = abstractCall.getBlockSize();
      InvariantChecks.checkTrue((blockSizeInBits & 7) == 0);

      final int blockSizeInBytes = blockSizeInBits >>> 3;
      accessTypes.add(new MemoryAccessType(operation, DataType.type(blockSizeInBytes)));

      accessConstraints.add(constraints);
    }

    Logger.debug("Creating memory access iterator: %s", accessTypes);
    Logger.debug("Memory access constraints: %s", accessConstraints);

    final GeneratorSettings settings = engineContext.getSettings();
    final MemoryAccessConstraints globalConstraints = null != settings ?
        MmuSettingsUtils.getConstraints(MmuPlugin.getSpecification(), settings) : null;

    return new MemoryAccessStructureIterator(
        abstraction, accessTypes, accessConstraints, globalConstraints, iterator, count);
  }

  private Iterator<MemorySolution> getSolutionIterator(
      final EngineContext engineContext,
      final Iterator<MemoryAccessStructure> structureIterator) {
    InvariantChecks.checkNotNull(engineContext);
    InvariantChecks.checkNotNull(structureIterator);

    return new Iterator<MemorySolution>() {
      private MemorySolution solution = null;

      private MemorySolution getSolution() {
        while (structureIterator.hasValue()) {
          final MemoryAccessStructure structure = structureIterator.value();

          // Reset the allocation tables before solving the constraint.
          addressAllocator.reset();
          entryIdAllocator.reset();

          final MemorySolver solver = new MemorySolver(
              structure,
              addressAllocator,
              entryIdAllocator,
              hitCheckers,
              normalPathChooser,
              pageMask,
              align,
              engineContext.getSettings());

          final SolverResult<MemorySolution> result = solver.solve(Solver.Mode.MAP);
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

  @Override
  public void onStartProgram() {
    // TODO
  }

  @Override
  public void onEndProgram() {
    // TODO
  }
}
