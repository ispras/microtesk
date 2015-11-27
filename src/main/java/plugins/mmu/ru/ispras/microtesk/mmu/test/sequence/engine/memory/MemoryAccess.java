/*
 * Copyright 2015 ISP RAS (http://www.ispras.ru)
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

import java.util.Collection;
import java.util.Map;

import ru.ispras.fortress.randomizer.Randomizer;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuSegment;
import ru.ispras.microtesk.settings.RegionSettings;

/**
 * {@link MemoryAccess} describes an execution path of a memory access instruction.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class MemoryAccess {
  private final MemoryAccessType type;
  private final MemoryAccessPath path;
  private final RegionSettings region;
  private final MmuSegment segment;

  /**
   * Constructs a memory access.
   * 
   * @param type the memory access type.
   * @param path the memory access path.
   * @param settings the generator settings.
   * @return a memory access or {@code null} if it cannot be constructed
   */
  public static MemoryAccess create(
      final MemoryAccessType type,
      final MemoryAccessPath path) {
    InvariantChecks.checkNotNull(type);
    InvariantChecks.checkNotNull(path);

    final Map<RegionSettings, Collection<MmuSegment>> regions = path.getRegions();

    if (regions.isEmpty()) {
      final Collection<MmuSegment> segments = path.getSegments();

      if (!segments.isEmpty()) {
        final MmuSegment segment = Randomizer.get().choose(segments);
        return new MemoryAccess(type, path, null, segment);
      }
    } else {
      final RegionSettings region = Randomizer.get().choose(regions.keySet());
      final MmuSegment segment = Randomizer.get().choose(regions.get(region));

      return new MemoryAccess(type, path, region, segment);
    }

    return null;
  }

  public MemoryAccess(
      final MemoryAccessType type,
      final MemoryAccessPath path,
      final RegionSettings region,
      final MmuSegment segment) {
    InvariantChecks.checkNotNull(type);
    InvariantChecks.checkNotNull(path);
    InvariantChecks.checkNotNull(segment);
    // Parameter {@code region} can be null.

    this.type = type;
    this.path = path;
    this.region = region;
    this.segment = segment;
  }

  public MemoryAccessType getType() {
    return type;
  }

  public MemoryAccessPath getPath() {
    return path;
  }

  public RegionSettings getRegion() {
    return region;
  }

  public MmuSegment getSegment() {
    return segment;
  }

  @Override
  public String toString() {
    return String.format("%s, %s, %s", type, path,
        (region != null ?
            String.format("%s[%s]", region.getName(), segment.getName()) :
            segment.getName()));
  }
}
