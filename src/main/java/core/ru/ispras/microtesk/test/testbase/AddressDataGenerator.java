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
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.settings.GeneratorSettings;
import ru.ispras.microtesk.settings.MemorySettings;
import ru.ispras.microtesk.settings.RegionSettings;
import ru.ispras.microtesk.test.TestEngine;
import ru.ispras.microtesk.utils.BigIntegerUtils;
import ru.ispras.testbase.TestBaseQuery;
import ru.ispras.testbase.TestData;
import ru.ispras.testbase.TestDataProvider;
import ru.ispras.testbase.generator.DataGenerator;
import ru.ispras.testbase.stub.Utils;

/**
 * {@link AddressDataGenerator} randomly generates an address from a given memory region.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class AddressDataGenerator implements DataGenerator {
  /** Instruction operand for the address base. */
  public static final String PARAM_ADDRESS_BASE = "base";
  /** Instruction operand for the address offset (optional). */
  public static final String PARAM_ADDRESS_OFFSET = "offset";
  /** Memory region to choose an address from (ignored if {@code PARAM_ADDRESS_VALUE} is set). */
  public static final String PARAM_MEMORY_REGION = "region";
  /** Size of a data block being accessed (ignored if {@code PARAM_ADDRESS_VALUE} is set). */
  public static final String PARAM_BLOCK_SIZE = "size";
  /** Address to be set (optional). */
  public static final String PARAM_ADDRESS_VALUE = "address";

  @Override
  public boolean isSuitable(final TestBaseQuery query) {
    InvariantChecks.checkNotNull(query);

    final Object base = Utils.getParameter(query, PARAM_ADDRESS_BASE);
    final Object region = Utils.getParameter(query, PARAM_MEMORY_REGION);
    final Object size = Utils.getParameter(query, PARAM_BLOCK_SIZE);
    final Object address = Utils.getParameter(query, PARAM_ADDRESS_VALUE);

    if (base == null || ((region == null || size == null) && address == null)) {
      return false;
    }

    if (address == null) {
      final GeneratorSettings generatorSettings = TestEngine.getGeneratorSettings();
      if (generatorSettings == null) {
        return false;
      }

      final MemorySettings memorySettings = generatorSettings.getMemory();
      if (memorySettings == null) {
        return false;
      }

      final RegionSettings regionSettings = memorySettings.getRegion(region.toString());
      if (regionSettings == null) {
        return false;
      }

      return regionSettings.isEnabled();
    }

    return true;
  }

  @Override
  public TestDataProvider generate(final TestBaseQuery query) {
    final Object base = Utils.getParameter(query, PARAM_ADDRESS_BASE);
    final Object offset = Utils.getParameter(query, PARAM_ADDRESS_OFFSET);
    final Object region = Utils.getParameter(query, PARAM_MEMORY_REGION);
    final Object size = Utils.getParameter(query, PARAM_BLOCK_SIZE);
    final Object address = Utils.getParameter(query, PARAM_ADDRESS_VALUE);

    final BigInteger addressValue;

    if (address != null) {
      InvariantChecks.checkTrue(address instanceof Number, "Address is of incorrect type");

      addressValue = BigIntegerUtils.valueOfUnsignedLong(((Number) address).longValue());
    } else {
      InvariantChecks.checkTrue(size instanceof Number, "Size is of incorrect type");

      final GeneratorSettings generatorSettings = TestEngine.getGeneratorSettings();
      final MemorySettings memorySettings = generatorSettings.getMemory();
      final RegionSettings regionSettings = memorySettings.getRegion(region.toString());

      final BigInteger min = BigIntegerUtils.valueOfUnsignedLong(regionSettings.getStartAddress());
      final BigInteger max = BigIntegerUtils.valueOfUnsignedLong(regionSettings.getEndAddress());

      final BigInteger mask = BigInteger.valueOf(((Number) size).longValue() - 1);
      final BigInteger random = Randomizer.get().nextBigIntegerRange(min, max);
      final BigInteger aligned = random.andNot(mask);

      addressValue = aligned;
    }

    final Map<String, Node> unknowns = Utils.extractUnknown(query);
    final Map<String, Node> bindings = new LinkedHashMap<>();

    for (final Map.Entry<String, Node> entry : unknowns.entrySet()) {
      final String name = entry.getKey();
      final DataType type = entry.getValue().getDataType();

      if (name.equals(base)) {
        final BitVector data = BitVector.valueOf(addressValue, type.getSize());
        bindings.put(name, NodeValue.newBitVector(data));
      } else if (offset != null && name.equals(offset)) {
        final BitVector data = BitVector.valueOf(0, type.getSize());
        bindings.put(name, NodeValue.newBitVector(data));
      }
    }

    return TestDataProvider.singleton(new TestData(bindings));
  }
}
