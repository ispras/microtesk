/*
 * Copyright 2015-2017 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.basis.solver.integer;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.randomizer.Randomizer;
import ru.ispras.fortress.util.InvariantChecks;

/**
 * {@link VariableInitializer} defines strategies for initializing integer variables.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public enum VariableInitializer {
  ZEROS() {
    @Override
    public BitVector getValue(final int width) {
      InvariantChecks.checkGreaterThanZero(width);

      final BitVector value = BitVector.newEmpty(width);
      value.reset();

      return value;
    }
  },
  UNITS() {
    @Override
    public BitVector getValue(final int width) {
      InvariantChecks.checkGreaterThanZero(width);

      final BitVector value = BitVector.newEmpty(width);
      value.setAll();

      return value;
    }
  },
  RANDOM() {
    @Override
    public BitVector getValue(final int width) {
      InvariantChecks.checkGreaterThanZero(width);

      final BitVector value = BitVector.valueOf(
          Randomizer.get().nextBigIntegerField(width, false), width);

      return value;
    }
  };

  public abstract BitVector getValue(final int width);
}

