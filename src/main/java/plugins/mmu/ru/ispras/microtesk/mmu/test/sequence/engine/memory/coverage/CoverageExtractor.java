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

package ru.ispras.microtesk.mmu.test.sequence.engine.memory.coverage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.mmu.basis.MemoryAccessConstraints;
import ru.ispras.microtesk.mmu.basis.MemoryAccessType;
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

  private final Map<MemoryAccessType, MemoryTrajectoryExtractor.Result> trajectories = new HashMap<>(); 
  private final Map<MemoryAccessType, List<MemoryAccessPathChooser>> pathChoosers = new HashMap<>();

  private MemoryTrajectoryExtractor.Result getTrajectories(
      final MmuSubsystem memory,
      final MemoryGraphAbstraction abstraction,
      final MemoryAccessType type) {

    final MemoryTrajectoryExtractor.Result cachedResult = trajectories.get(type);

    if (cachedResult != null) {
      return cachedResult;
    }

    final MemoryTrajectoryExtractor extractor = new MemoryTrajectoryExtractor(memory);
    final MemoryTrajectoryExtractor.Result result = extractor.apply(type, abstraction);

    trajectories.put(type, result);

    return result;
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

    final MemoryTrajectoryExtractor.Result result = getTrajectories(memory, abstraction, type);
    final MemoryAccessPathChooser chooser = new MemoryAccessPathChooser(
        memory,
        result.getTrajectories(),
        result.getGraph(),
        type,
        constraints,
        discardEmptyTrajectories);
  
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

    // Only unconstrained path choosers are cached.
    if (constraints.isEmpty()) {
      final List<MemoryAccessPathChooser> cachedChoosers = pathChoosers.get(type);

      if (cachedChoosers != null) {
        return cachedChoosers;
      }
    }

    final MemoryTrajectoryExtractor.Result result = getTrajectories(memory, abstraction, type);
    final List<MemoryAccessPathChooser> choosers = new ArrayList<>();

    for (final List<Object> trajectory : result.getTrajectories()) {
      if (discardEmptyTrajectories && trajectory.isEmpty()) {
        continue;
      }

      choosers.add(new MemoryAccessPathChooser(
          memory, trajectory, result.getGraph(), type, constraints));
    }

    if (constraints.isEmpty()) {
      pathChoosers.put(type, choosers);
    }

    return choosers;
  }
}

