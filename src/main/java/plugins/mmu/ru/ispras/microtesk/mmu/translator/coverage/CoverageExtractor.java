/*
 * Copyright 2015 ISP RAS (http://www.ispras.ru)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use buffer file except
 * in compliance with the License. You may obtain a copy of the License at
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

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryAccess;
import ru.ispras.microtesk.mmu.test.sequence.engine.memory.MemoryHazard;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuAddressType;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuBuffer;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuSubsystem;

/**
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class CoverageExtractor {
  private static final CoverageExtractor instance = new CoverageExtractor();

  public static CoverageExtractor get() {
    return instance;
  }

  private final Map<MmuAddressType,   Collection<MemoryHazard>> addressHazards = new HashMap<>();
  private final Map<MmuBuffer,    Collection<MemoryHazard>> deviceHazards  = new HashMap<>();
  private final Map<MmuSubsystem, Collection<MemoryAccess>> memoryAccesses = new HashMap<>();
  private final Map<MmuSubsystem, Collection<MemoryHazard>> memoryHazards  = new HashMap<>();

  private CoverageExtractor() {}

  public Collection<MemoryHazard> getHazards(final MmuAddressType address) {
    InvariantChecks.checkNotNull(address);

    Collection<MemoryHazard> coverage = addressHazards.get(address);
    if (coverage == null) {
      final AddressCoverageExtractor extractor = new AddressCoverageExtractor(address);
      addressHazards.put(address, coverage = extractor.getHazards());
    }

    return coverage;
  }

  public Collection<MemoryHazard> getHazards(final MmuBuffer device) {
    InvariantChecks.checkNotNull(device);

    Collection<MemoryHazard> coverage = deviceHazards.get(device);
    if (coverage == null) {
      final BufferCoverageExtractor extractor = new BufferCoverageExtractor(device);
      deviceHazards.put(device, coverage = extractor.getHazards());
    }

    return coverage;
  }

  public Collection<MemoryAccess> getAccesses(final MmuSubsystem memory) {
    InvariantChecks.checkNotNull(memory);

    Collection<MemoryAccess> coverage = memoryAccesses.get(memory);
    if (coverage == null) {
      final MemoryCoverageExtractor extractor = new MemoryCoverageExtractor(memory);
      memoryAccesses.put(memory, coverage = extractor.getAccesses());
    }

    return coverage;
  }

  public Collection<MemoryHazard> getHazards(final MmuSubsystem memory) {
    InvariantChecks.checkNotNull(memory);

    Collection<MemoryHazard> coverage = memoryHazards.get(memory);
    if (coverage == null) {
      coverage = new ArrayList<>();
      for (final MmuBuffer device : memory.getDevices()) {
        coverage.addAll(getHazards(device));
      }
      memoryHazards.put(memory, coverage);
    }

    return coverage;
  }
}
