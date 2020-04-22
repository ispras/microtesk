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

package ru.ispras.microtesk.test.template;

import ru.ispras.castle.util.Logger;
import ru.ispras.fortress.randomizer.Randomizer;
import ru.ispras.fortress.util.InvariantChecks;

import ru.ispras.microtesk.model.memory.MemoryAccessMode;
import ru.ispras.microtesk.settings.GeneratorSettings;
import ru.ispras.microtesk.settings.MemorySettings;
import ru.ispras.microtesk.settings.RegionSettings;
import ru.ispras.microtesk.test.GenerationAbortedException;
import ru.ispras.microtesk.test.LabelManager;

import java.math.BigInteger;
import ru.ispras.microtesk.utils.BigIntegerUtils;

public final class MemoryObjectBuilder {
  private final LabelManager memoryMap;
  private final GeneratorSettings settings;
  private final int size;

  private String name;
  private MemoryAccessMode mode;

  private BigInteger va = null;
  private boolean isVaLabel = false;
  private BigInteger pa = null;
  private BigInteger data = null;

  protected MemoryObjectBuilder(
      final int size,
      final LabelManager memoryMap,
      final GeneratorSettings settings) {
    InvariantChecks.checkGreaterThanZero(size);
    InvariantChecks.checkNotNull(memoryMap);
    InvariantChecks.checkNotNull(settings);

    if (!isPowOf2(size)) {
      throw new IllegalArgumentException(String.format(
          "Size (%d) must be a power of two!", size));
    }

    this.memoryMap = memoryMap;
    this.settings = settings;
    this.size = size;

    this.name = null;
    this.mode = new MemoryAccessMode(true, false, false);
  }

  private boolean isPowOf2(final int value) {
    return 0 != value ? 0 == (value & (value - 1)) : false;
  }

  public void setName(final String name) {
    InvariantChecks.checkNotNull(name);
    this.name = name;
  }

  public void setMode(final String rwx) {
    InvariantChecks.checkNotNull(rwx);
    this.mode = new MemoryAccessMode(rwx);
  }

  public void setVa(final BigInteger address) {
    InvariantChecks.checkNotNull(address);
    this.va = address;
  }

  public void setVa(final BigInteger startAddress, final BigInteger endAddress) {
    this.va = randomAlignedAddressFromRange(startAddress, endAddress, size);
  }

  public void setVa(final String labelName) {
    InvariantChecks.checkNotNull(labelName);

    final LabelManager.Target target =
        memoryMap.resolve(Label.newLabel(labelName, new BlockId()));

    if (null == target) {
      throw new GenerationAbortedException(
          String.format("The %s label is not defined.", labelName));
    }

    this.va = BigIntegerUtils.asUnsigned(target.getAddress());
    this.isVaLabel = true;
  }

  public void setPa(final BigInteger address) {
    InvariantChecks.checkNotNull(address);
    this.pa = address;
  }

  public void setPa(final BigInteger startAddress, final BigInteger endAddress) {
    this.pa = randomAlignedAddressFromRange(startAddress, endAddress, size);
  }

  public void setPa(final String regionName) {
    InvariantChecks.checkNotNull(regionName);

    final MemorySettings memory = settings.getMemory();
    if (null == memory) {
      throw new IllegalStateException("Memory settings are not defined.");
    }

    final RegionSettings region = memory.getRegion(regionName);
    if (null == region) {
      throw new IllegalArgumentException(String.format(
          "Settings for memory region '%s' are not defined.", regionName));
    }

    final BigInteger startAddress = region.getStartAddress();
    final BigInteger endAddress = region.getEndAddress();

    setPa(startAddress, endAddress);
  }

  private static BigInteger randomAlignedAddressFromRange(
      final BigInteger startAddress,
      final BigInteger endAddress,
      final int itemSize) {
    InvariantChecks.checkNotNull(startAddress);
    InvariantChecks.checkNotNull(endAddress);
    InvariantChecks.checkGreaterThan(endAddress, startAddress);

    final long rangeSize = endAddress.subtract(startAddress).longValue();
    final long itemCount = rangeSize / itemSize;

    final long itemIndex = Randomizer.get().nextLongRange(0, itemCount - 1);

    final BigInteger offset =
        BigInteger.valueOf(itemSize).multiply(BigInteger.valueOf(itemIndex));

    return startAddress.add(offset);
  }

  public void setData(final BigInteger data) {
    InvariantChecks.checkNotNull(data);
    this.data = data;
  }

  public MemoryObject build() {
    checkInitialized("va", va != null);
    checkInitialized("pa", isVaLabel || (!isVaLabel && pa != null));

    final MemoryObject result =
        new MemoryObject(name, size, mode, va, pa);

    if (null != data) {
      result.setData(data);
    }

    Logger.debug(result.toString());
    return result;
  }

  private static void checkInitialized(final String id, final boolean state) {
    if (!state) {
      throw new IllegalStateException(
          String.format("%s is not initialized", id));
    }
  }
}
