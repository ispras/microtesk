/*
 * Copyright 2015-2018 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.mmu.test.engine.memory;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.mmu.basis.MemoryAccessType;
import ru.ispras.microtesk.mmu.basis.MemoryOperation;
import ru.ispras.microtesk.mmu.test.template.AccessConstraints;
import ru.ispras.microtesk.mmu.model.spec.MmuSubsystem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class CoverageExtractor {
  private CoverageExtractor() {}

  private static final CoverageExtractor instance = new CoverageExtractor();

  public static CoverageExtractor get() {
    return instance;
  }

  private final Map<MemoryAccessType, TrajectoryExtractor.Result> trajectories = new HashMap<>();
  private final Map<MemoryAccessType, List<AccessChooser>> pathChoosers = new HashMap<>();

  private TrajectoryExtractor.Result getTrajectories(
      final MmuSubsystem memory,
      final GraphAbstraction abstraction,
      final MemoryAccessType type) {

    final TrajectoryExtractor.Result cachedResult = trajectories.get(type);

    if (cachedResult != null) {
      return cachedResult;
    }

    final TrajectoryExtractor.Result result;
    final TrajectoryExtractor extractor = new TrajectoryExtractor(memory);

    if (type.getOperation() != MemoryOperation.NONE) {
      result = extractor.apply(type, abstraction);
    } else {
      result = new TrajectoryExtractor.Result(
          Collections.<List<Object>>singleton(Collections.<Object>emptyList()),
          new Graph()
      );
    }

    trajectories.put(type, result);

    return result;
  }

  public AccessChooser getPathChooser(
      final MmuSubsystem memory,
      final GraphAbstraction abstraction,
      final MemoryAccessType type,
      final AccessConstraints constraints,
      final int recursionLimit,
      final boolean discardEmptyTrajectories) {
    InvariantChecks.checkNotNull(memory);
    InvariantChecks.checkNotNull(abstraction);
    InvariantChecks.checkNotNull(type);
    InvariantChecks.checkNotNull(constraints);

    final TrajectoryExtractor.Result result = getTrajectories(memory, abstraction, type);
    final AccessChooser chooser = new AccessChooser(
        memory,
        result.getTrajectories(),
        result.getGraph(),
        type,
        constraints,
        recursionLimit,
        discardEmptyTrajectories);

    return chooser;
  }

  public List<AccessChooser> getPathChoosers(
      final MmuSubsystem memory,
      final GraphAbstraction abstraction,
      final MemoryAccessType type,
      final AccessConstraints constraints,
      final int recursionLimit,
      final boolean discardEmptyTrajectories) {
    InvariantChecks.checkNotNull(memory);
    InvariantChecks.checkNotNull(abstraction);
    InvariantChecks.checkNotNull(type);
    InvariantChecks.checkNotNull(constraints);

    // Only unconstrained path choosers are cached.
    if (constraints.isEmpty()) {
      final List<AccessChooser> cachedChoosers = pathChoosers.get(type);

      if (cachedChoosers != null) {
        return cachedChoosers;
      }
    }

    final TrajectoryExtractor.Result result = getTrajectories(memory, abstraction, type);
    final List<AccessChooser> choosers = new ArrayList<>();

    for (final List<Object> trajectory : result.getTrajectories()) {
      if (discardEmptyTrajectories && trajectory.isEmpty()) {
        continue;
      }

      choosers.add(
          new AccessChooser(
              memory,
              trajectory,
              result.getGraph(),
              type,
              constraints,
              recursionLimit)
      );
    }

    if (constraints.isEmpty()) {
      pathChoosers.put(type, choosers);
    }

    return choosers;
  }
}

