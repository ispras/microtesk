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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.ispras.microtesk.mmu.translator.ir.spec.MmuAddress;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuDevice;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuSubsystem;

/**
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class CoverageExtractor {
  private static final CoverageExtractor instance = new CoverageExtractor();

  public static CoverageExtractor get() {
    return instance;
  }

  private Map<MmuAddress, List<MemoryHazard>> addressCoverage = new HashMap<>();
  private Map<MmuDevice, List<MemoryHazard>> deviceCoverage = new HashMap<>();
  private Map<MmuSubsystem, List<MemoryAccess>> memoryCoverage = new HashMap<>();

  private CoverageExtractor() {}

  public List<MemoryHazard> getCoverage(final MmuAddress address) {
    List<MemoryHazard> coverage = addressCoverage.get(address);

    if (coverage != null) {
      return coverage;
    }

    final AddressCoverageExtractor extractor = new AddressCoverageExtractor(address);
    addressCoverage.put(address, coverage = extractor.getHazards());

    return coverage;
  }

  public List<MemoryHazard> getCoverage(final MmuDevice device) {
    List<MemoryHazard> coverage = deviceCoverage.get(device);

    if (coverage != null) {
      return coverage;
    }

    final BufferCoverageExtractor extractor = new BufferCoverageExtractor(device);
    deviceCoverage.put(device, coverage = extractor.getHazards());

    return coverage;
  }

  public List<MemoryAccess> getCoverage(final MmuSubsystem memory) {
    List<MemoryAccess> coverage = memoryCoverage.get(memory);

    if (coverage != null) {
      return coverage;
    }

    final MemoryCoverageExtractor extractor = new MemoryCoverageExtractor(memory);
    memoryCoverage.put(memory, coverage = extractor.getExecutionPaths());

    return coverage;
  }
}
