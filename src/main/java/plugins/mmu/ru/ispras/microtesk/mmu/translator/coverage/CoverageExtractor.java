/*
 * Copyright 2015 ISP RAS (http://www.ispras.ru)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use buffer file
 * except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package ru.ispras.microtesk.mmu.translator.coverage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.mmu.basis.MemoryAccessConstraints;
import ru.ispras.microtesk.mmu.basis.MemoryAccessType;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryHazard;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuAddressInstance;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuBuffer;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuSubsystem;

/**
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class CoverageExtractor {
  private CoverageExtractor() {}

  private static final CoverageExtractor instance = new CoverageExtractor();

  public static CoverageExtractor get() {
    return instance;
  }

  private final Map<MmuAddressInstance, Collection<MemoryHazard>> addressHazards = new HashMap<>();

  public Collection<MemoryHazard> getHazards(final MmuAddressInstance address) {
    InvariantChecks.checkNotNull(address);

    Collection<MemoryHazard> coverage = addressHazards.get(address);
    if (coverage == null) {
      final AddressCoverageExtractor extractor = new AddressCoverageExtractor(address);
      addressHazards.put(address, coverage = extractor.getHazards());
    }

    return coverage;
  }

  private final Map<MmuBuffer, Collection<MemoryHazard>> bufferHazards = new HashMap<>();

  public Collection<MemoryHazard> getHazards(final MmuBuffer buffer) {
    InvariantChecks.checkNotNull(buffer);

    Collection<MemoryHazard> coverage = bufferHazards.get(buffer);
    if (coverage == null) {
      final BufferCoverageExtractor extractor = new BufferCoverageExtractor(buffer);
      bufferHazards.put(buffer, coverage = extractor.getHazards());
    }

    return coverage;
  }

  private final Map<MmuSubsystem, Collection<MemoryHazard>> memoryHazards = new HashMap<>();

  public Collection<MemoryHazard> getHazards(final MmuSubsystem memory) {
    InvariantChecks.checkNotNull(memory);

    Collection<MemoryHazard> coverage = memoryHazards.get(memory);
    if (coverage == null) {
      coverage = new ArrayList<>();
      for (final MmuBuffer device : memory.getBuffers()) {
        coverage.addAll(getHazards(device));
      }
      memoryHazards.put(memory, coverage);
    }

    return coverage;
  }

  public MemoryAccessPathChooser getPathChooser(
      final MmuSubsystem memory,
      final MemoryGraphAbstraction abstraction,
      final MemoryAccessType type,
      final MemoryAccessConstraints constraints,
      final boolean discardEmptyTrajectories) {
    InvariantChecks.checkNotNull(memory);
    InvariantChecks.checkNotNull(abstraction);
    InvariantChecks.checkNotNull(type);
    InvariantChecks.checkNotNull(constraints);
  
    final MemoryTrajectoryExtractor extractor = new MemoryTrajectoryExtractor(memory);
    final MemoryTrajectoryExtractor.Result result = extractor.apply(abstraction);
  
    final MemoryAccessPathChooser chooser = new MemoryAccessPathChooser(
        memory, result.getTrajectories(), result.getGraph(), type, constraints, discardEmptyTrajectories);
  
    return chooser;
  }

  public List<MemoryAccessPathChooser> getPathChoosers(
      final MmuSubsystem memory,
      final MemoryGraphAbstraction abstraction,
      final MemoryAccessType type,
      final MemoryAccessConstraints constraints,
      final boolean discardEmptyTrajectories) {
    InvariantChecks.checkNotNull(memory);
    InvariantChecks.checkNotNull(abstraction);
    InvariantChecks.checkNotNull(type);
    InvariantChecks.checkNotNull(constraints);

    final MemoryTrajectoryExtractor extractor = new MemoryTrajectoryExtractor(memory);
    final MemoryTrajectoryExtractor.Result result = extractor.apply(abstraction);

    final List<MemoryAccessPathChooser> choosers = new ArrayList<>();

    for (final Collection<Object> trajectory : result.getTrajectories()) {
      if (discardEmptyTrajectories && trajectory.isEmpty()) {
        continue;
      }

      choosers.add(
          new MemoryAccessPathChooser(memory, trajectory, result.getGraph(), type, constraints));
    }

    return choosers;
  }
}

