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

package ru.ispras.microtesk.test.testbase;

import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.Map;

import ru.ispras.fortress.data.DataType;
import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeValue;
import ru.ispras.fortress.randomizer.Randomizer;
import ru.ispras.microtesk.settings.GeneratorSettings;
import ru.ispras.microtesk.settings.MemorySettings;
import ru.ispras.microtesk.settings.RegionSettings;
import ru.ispras.microtesk.test.TestEngine;
import ru.ispras.testbase.TestBaseQuery;
import ru.ispras.testbase.TestData;
import ru.ispras.testbase.TestDataProvider;
import ru.ispras.testbase.generator.DataGenerator;
import ru.ispras.testbase.stub.Utils;

/**
 * {@link AddressGenerator} randomly generates an address from a given memory region.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
final class AddressGenerator implements DataGenerator {
  /** Instruction operand for the address base. */
  private static final String PARAM_ADDRESS_BASE = "base";
  /** Instruction operand for the address offset (optional). */
  private static final String PARAM_ADDRESS_OFFSET = "offset";
  /** Memory region to choose an address from. */
  private static final String PARAM_MEMORY_REGION = "region";

  @Override
  public boolean isSuitable(final TestBaseQuery query) {
    final String addressBase = Utils.getParameter(query, PARAM_ADDRESS_BASE).toString();
    final String memoryRegion = Utils.getParameter(query, PARAM_MEMORY_REGION).toString();

    if (addressBase == null || memoryRegion == null) {
      return false;
    }

    final GeneratorSettings generatorSettings = TestEngine.getGeneratorSettings();
    if (generatorSettings == null) {
      return false;
    }

    final MemorySettings memorySettings = generatorSettings.getMemory();
    if (memorySettings == null) {
      return false;
    }

    final RegionSettings regionSettings = memorySettings.getRegion(memoryRegion);
    if (regionSettings == null) {
      return false;
    }

    return regionSettings.isEnabled();
  }

  @Override
  public TestDataProvider generate(final TestBaseQuery query) {
    final Object addressBase = Utils.getParameter(query, PARAM_ADDRESS_BASE);
    final Object addressOffset = Utils.getParameter(query, PARAM_ADDRESS_OFFSET);
    final Object memoryRegion = Utils.getParameter(query, PARAM_MEMORY_REGION);

    final GeneratorSettings generatorSettings = TestEngine.getGeneratorSettings();
    final MemorySettings memorySettings = generatorSettings.getMemory();
    final RegionSettings regionSettings = memorySettings.getRegion(memoryRegion.toString());

    final long min = regionSettings.getStartAddress();
    final long max = regionSettings.getEndAddress();

    final BigInteger startAddress = min >= 0 ? BigInteger.valueOf(min) :
        BigInteger.valueOf(min).add(BigInteger.ONE.shiftLeft(Long.SIZE));

    final BigInteger endAddress = max >= 0 ? BigInteger.valueOf(max) :
      BigInteger.valueOf(max).add(BigInteger.ONE.shiftLeft(Long.SIZE));

    final BigInteger address = Randomizer.get().nextBigIntegerRange(startAddress, endAddress);

    final Map<String, Node> unknowns = Utils.extractUnknown(query);
    final Map<String, Node> bindings = new LinkedHashMap<>();

    for (final Map.Entry<String, Node> entry : unknowns.entrySet()) {
      final String name = entry.getKey();
      final DataType type = entry.getValue().getDataType();

      if (name.equals(addressBase)) {
        final BitVector data = BitVector.valueOf(address, type.getSize());
        bindings.put(name, NodeValue.newBitVector(data));
      } else if (addressOffset != null && name.equals(addressOffset)) {
        final BitVector data = BitVector.valueOf(0, type.getSize());
        bindings.put(name, NodeValue.newBitVector(data));
      }
    }

    return TestDataProvider.singleton(new TestData(bindings));
  }
}
