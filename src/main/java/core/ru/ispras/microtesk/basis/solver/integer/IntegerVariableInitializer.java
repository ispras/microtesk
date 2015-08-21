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

package ru.ispras.microtesk.basis.solver.integer;

import java.math.BigInteger;

import ru.ispras.fortress.randomizer.Randomizer;
import ru.ispras.fortress.util.BitUtils;
import ru.ispras.fortress.util.InvariantChecks;

/**
 * {@link IntegerVariableInitializer} defines strategies for initializing integer variables.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public enum IntegerVariableInitializer {
  ZEROS() {
    @Override
    public BigInteger getValue(final IntegerVariable variable) {
      InvariantChecks.checkNotNull(variable);
      return BigInteger.ZERO;
    }
  },
  UNITS() {
    @Override
    public BigInteger getValue(final IntegerVariable variable) {
      InvariantChecks.checkNotNull(variable);
      return BitUtils.getBigIntegerMask(variable.getWidth());
    }
  },
  RANDOM() {
    @Override
    public BigInteger getValue(final IntegerVariable variable) {
      InvariantChecks.checkNotNull(variable);
      return Randomizer.get().nextBigIntegerField(variable.getWidth(), false);
    }
  };

  public abstract BigInteger getValue(final IntegerVariable variable);
}

