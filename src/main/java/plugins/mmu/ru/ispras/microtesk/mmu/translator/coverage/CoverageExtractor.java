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
import java.util.Map;
import java.util.Set;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.mmu.basis.BufferAccessEvent;
import ru.ispras.microtesk.mmu.basis.BufferEventConstraint;
import ru.ispras.microtesk.mmu.basis.MemoryAccessConstraints;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryAccessPath;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryAccessType;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryHazard;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuAddressType;
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

  private final Map<MmuAddressType, Collection<MemoryHazard>> addressHazards = new HashMap<>();
  private final Map<MmuBuffer, Collection<MemoryHazard>> bufferHazards = new HashMap<>();
  private final Map<MmuSubsystem, Collection<MemoryHazard>> memoryHazards = new HashMap<>();

  private final Map<MmuSubsystem, Map<MemoryAccessType, Collection<MemoryAccessPath>>> enabledPaths =
      new HashMap<>();

  private final Map<MmuSubsystem, Map<MmuBuffer, Collection<MemoryAccessPath>>> normalPaths =
      new HashMap<>();

  public Collection<MemoryHazard> getHazards(final MmuAddressType address) {
    InvariantChecks.checkNotNull(address);

    Collection<MemoryHazard> coverage = addressHazards.get(address);
    if (coverage == null) {
      final AddressCoverageExtractor extractor = new AddressCoverageExtractor(address);
      addressHazards.put(address, coverage = extractor.getHazards());
    }

    return coverage;
  }

  public Collection<MemoryHazard> getHazards(final MmuBuffer buffer) {
    InvariantChecks.checkNotNull(buffer);

    Collection<MemoryHazard> coverage = bufferHazards.get(buffer);
    if (coverage == null) {
      final BufferCoverageExtractor extractor = new BufferCoverageExtractor(buffer);
      bufferHazards.put(buffer, coverage = extractor.getHazards());
    }

    return coverage;
  }

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

  public Collection<MemoryAccessPath> getEnabledPaths(
      final MmuSubsystem memory,
      final MemoryAccessType type,
      final MemoryAccessConstraints constraints) {
    InvariantChecks.checkNotNull(memory);
    InvariantChecks.checkNotNull(type);
    // Parameter {@code constraints} can be null.

    Map<MemoryAccessType, Collection<MemoryAccessPath>> typeToPaths = enabledPaths.get(memory);
    if (typeToPaths == null) {
      enabledPaths.put(memory,
          typeToPaths = new HashMap<MemoryAccessType, Collection<MemoryAccessPath>>());
    }

    Collection<MemoryAccessPath> paths = typeToPaths.get(type);
    if (paths == null) {
      final MemoryCoverageExtractor extractor = new MemoryCoverageExtractor(memory);
      typeToPaths.put(type, paths = extractor.getPaths(type));
    }

    return getEnabledPaths(memory, paths, constraints);
  }

  public Collection<MemoryAccessPath> getNormalPaths(
      final MmuSubsystem memory,
      final MmuBuffer buffer,
      final MemoryAccessConstraints constraints) {
    InvariantChecks.checkNotNull(memory);
    InvariantChecks.checkNotNull(buffer);
    // Parameter {@code constraints} can be null.

    Map<MmuBuffer, Collection<MemoryAccessPath>> bufferToPaths = normalPaths.get(memory);
    if (bufferToPaths == null) {
      normalPaths.put(memory,
          bufferToPaths = new HashMap<MmuBuffer, Collection<MemoryAccessPath>>());
    }

    Collection<MemoryAccessPath> paths = bufferToPaths.get(buffer);
    if (paths == null) {
      final MemoryCoverageExtractor extractor = new MemoryCoverageExtractor(memory);
      final Collection<MemoryAccessPath> allPaths = extractor.getPaths(null);
      final Collection<MemoryAccessPath> enabledPaths =
          getEnabledPaths(memory, allPaths, constraints);

      paths = new ArrayList<>();
      for (final MemoryAccessPath path : enabledPaths) {
        if (path.contains(buffer) && path.contains(memory.getTargetBuffer())) {
          paths.add(path);
        }
      }

      bufferToPaths.put(buffer, paths);
    }

    return paths;
  }

  private static boolean isEnabledPath(
      final MmuSubsystem memory,
      final MemoryAccessPath path,
      final MemoryAccessConstraints constraints) {
    InvariantChecks.checkNotNull(memory);
    InvariantChecks.checkNotNull(path);
    InvariantChecks.checkNotNull(constraints);

    if (memory.getRegions().isEmpty() && path.getSegments().isEmpty()
        || !memory.getRegions().isEmpty() && path.getRegions().isEmpty()) {
      return false;
    }

    final Collection<BufferEventConstraint> bufferEventsConstraints =
        constraints.getBufferEvents();

    if (bufferEventsConstraints != null) {
      for (final BufferEventConstraint constraint : bufferEventsConstraints) {
        final MmuBuffer buffer = constraint.getBuffer();
        InvariantChecks.checkNotNull(buffer);

        final Set<BufferAccessEvent> events = constraint.getEvents();
        InvariantChecks.checkNotNull(events);

        if (path.contains(buffer) && !events.contains(path.getEvent(buffer))) {
          return false;
        }
      }
    }

    return true;
  }

  private static Collection<MemoryAccessPath> getEnabledPaths(
      final MmuSubsystem memory,
      final Collection<MemoryAccessPath> paths,
      final MemoryAccessConstraints constraints) {
    InvariantChecks.checkNotNull(memory);
    InvariantChecks.checkNotNull(paths);
    // Parameter {@code settings} can be null.

    if (constraints == null) {
      return paths;
    }

    final Collection<MemoryAccessPath> enabledPaths = new ArrayList<>(paths.size());

    for (final MemoryAccessPath path : paths) {
      if (isEnabledPath(memory, path, constraints)) {
        enabledPaths.add(path);
      }
    }

    return enabledPaths;
  }
}
