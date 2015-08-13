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
import java.util.LinkedHashSet;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.mmu.translator.MmuTranslator;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuGuard;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuSegment;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuSubsystem;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuTransition;
import ru.ispras.microtesk.settings.GeneratorSettings;
import ru.ispras.microtesk.settings.RegionSettings;

/**
 * {@link MemoryAccess} describes an execution path of a memory access instruction.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class MemoryAccess {
  private final MemoryAccessType type;
  private final MemoryAccessPath path;

  private final Collection<RegionSettings> regions = new LinkedHashSet<>();
  private final Collection<MmuSegment> segments = new LinkedHashSet<>();

  public MemoryAccess(
      final MemoryAccessType type,
      final MemoryAccessPath path,
      final GeneratorSettings settings) {
    InvariantChecks.checkNotNull(type);
    InvariantChecks.checkNotNull(path);
    // Parameter {@code settings} can be null.

    this.type = type;
    this.path = path;

    if (settings != null) {
      for (final RegionSettings region : settings.getMemory().getRegions()) {
        if (region.isEnabled() && region.getType() == RegionSettings.Type.DATA) {
          regions.add(region);
        }
      }
    }

    final MmuSubsystem memory = MmuTranslator.getSpecification();
    for (final MmuSegment segment : memory.getSegments()) {
      segments.add(segment);
    }

    for (final MmuTransition transition : path.getTransitions()) {
      final MmuGuard guard = transition.getGuard();

      if (guard != null) {
        final Collection<String> guardRegionNames = guard.getRegions(); 
        final Collection<MmuSegment> guardSegments = guard.getSegments();

        final Collection<RegionSettings> guardRegions =
            settings != null && guardRegionNames != null ? new ArrayList<RegionSettings>() : null;

        if (settings != null && guardRegionNames != null) {
          for (final String regionName : guardRegionNames) {
            guardRegions.add(settings.getMemory().getRegion(regionName));
          }
        }

        if (guardRegions != null) {
          regions.retainAll(guardRegions);
        }

        if (guardSegments != null) {
          segments.retainAll(guardSegments);
        }
      }
    }
  }

  public MemoryAccessType getType() {
    return type;
  }

  public MemoryAccessPath getPath() {
    return path;
  }

  public Collection<RegionSettings> getRegions() {
    return regions;
  }

  public Collection<MmuSegment> getSegments() {
    return segments;
  }

  @Override
  public String toString() {
    return String.format("%s, %s", type, path);
  }
}
