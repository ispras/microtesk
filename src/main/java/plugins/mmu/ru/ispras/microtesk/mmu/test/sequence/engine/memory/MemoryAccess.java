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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import ru.ispras.fortress.randomizer.Randomizer;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.mmu.translator.MmuTranslator;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuGuard;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuSegment;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuSubsystem;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuTransition;
import ru.ispras.microtesk.settings.AccessSettings;
import ru.ispras.microtesk.settings.GeneratorSettings;
import ru.ispras.microtesk.settings.RegionSettings;

/**
 * {@link MemoryAccess} describes an execution path of a memory access instruction.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class MemoryAccess {
  public static Collection<MmuSegment> getPossibleSegments(final MemoryAccessPath path) {
    InvariantChecks.checkNotNull(path);

    final MmuSubsystem memory = MmuTranslator.getSpecification();
    final Collection<MmuSegment> segments = new LinkedHashSet<>(memory.getSegments());

    for (final MmuTransition transition : path.getTransitions()) {
      final MmuGuard guard = transition.getGuard();

      if (guard != null) {
        final Collection<MmuSegment> guardSegments = guard.getSegments();

        if (guardSegments != null) {
          segments.retainAll(guardSegments);
        }
      }
    }

    return segments;
  }

  public static Map<RegionSettings, Collection<MmuSegment>> getPossibleSegments(
      final MemoryAccessPath path, final GeneratorSettings settings) {
    InvariantChecks.checkNotNull(path);
    InvariantChecks.checkNotNull(settings);

    final MmuSubsystem memory = MmuTranslator.getSpecification();

    final Collection<RegionSettings> regions = new LinkedHashSet<>();
    final Collection<MmuSegment> segments = getPossibleSegments(path);

    for (final RegionSettings region : settings.getMemory().getRegions()) {
      if (region.isEnabled() && region.getType() == RegionSettings.Type.DATA) {
        regions.add(region);
      }
    }

    for (final MmuTransition transition : path.getTransitions()) {
      final MmuGuard guard = transition.getGuard();

      if (guard != null) {
        final Collection<String> guardRegionNames = guard.getRegions(); 
        final Collection<RegionSettings> guardRegions =
            guardRegionNames != null ? new ArrayList<RegionSettings>() : null;

        if (guardRegionNames != null) {
          for (final String regionName : guardRegionNames) {
            guardRegions.add(settings.getMemory().getRegion(regionName));
          }
        }

        if (guardRegions != null) {
          regions.retainAll(guardRegions);
        }
      }
    }

    final Map<RegionSettings, Collection<MmuSegment>> possibleSegments = new LinkedHashMap<>();

    for (final RegionSettings region : regions) {
      final Collection<MmuSegment> regionSegments = new LinkedHashSet<>();

      for (final AccessSettings regionAccess: region.getAccesses()) {
        regionSegments.add(memory.getSegment(regionAccess.getSegment()));
      }

      final Collection<MmuSegment> possibleRegionSegments = new LinkedHashSet<>(segments);
      possibleRegionSegments.retainAll(regionSegments);

      if (!possibleRegionSegments.isEmpty()) {
        possibleSegments.put(region, possibleRegionSegments);
      }
    }

    return possibleSegments;
  }

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
      final MemoryAccessPath path,
      final GeneratorSettings settings) {
    InvariantChecks.checkNotNull(type);
    InvariantChecks.checkNotNull(path);
    // Parameter {@code settings} can be null.

    if (settings == null) {
      final Collection<MmuSegment> segments = getPossibleSegments(path);

      if (!segments.isEmpty()) {
        final MmuSegment segment = Randomizer.get().choose(segments);
        return new MemoryAccess(type, path, null, segment);
      }
    } else {
      final Map<RegionSettings, Collection<MmuSegment>> segments =
          getPossibleSegments(path, settings);

      if (!segments.isEmpty()) {
        final RegionSettings region = Randomizer.get().choose(segments.keySet());
        final MmuSegment segment = Randomizer.get().choose(segments.get(region));
  
        return new MemoryAccess(type, path, region, segment);
      }
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
