/*
 * Copyright 2016 ISP RAS (http://www.ispras.ru)
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

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.randomizer.Randomizer;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.model.data.Type;

/**
 * The {@link DataGenerator} class is responsible for generating data values
 * of the specified type using a set of provided methods.
 *
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
abstract class DataGenerator {
  private DataGenerator() {}

  public static DataGenerator newInstance(final String methodName, final Type type) {
    InvariantChecks.checkNotNull(methodName);
    InvariantChecks.checkNotNull(type);

    if ("zero".equalsIgnoreCase(methodName)) {
      return new Zero(type);
    }

    if ("random".equalsIgnoreCase(methodName)) {
      return new Random(type);
    }

    throw new IllegalArgumentException(
        "Unknown data generation method: " + methodName);
  }

  public abstract BitVector nextData();

  private static final class Random extends DataGenerator {
    private final Type type;

    public Random(final Type type) {
      this.type = type;
    }

    @Override
    public BitVector nextData() {
      final BitVector data = BitVector.newEmpty(type.getBitSize());
      Randomizer.get().fill(data);
      return data;
    }
  }

  private static final class Zero extends DataGenerator {
    private final BitVector data;

    public Zero(final Type type) {
      this.data = BitVector.newEmpty(type.getBitSize());
    }

    @Override
    public BitVector nextData() {
      return data;
    }
  }
}
