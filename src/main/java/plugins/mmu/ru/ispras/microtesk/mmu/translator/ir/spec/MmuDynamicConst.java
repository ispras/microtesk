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

package ru.ispras.microtesk.mmu.translator.ir.spec;

import java.math.BigInteger;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.fortress.util.Value;
import ru.ispras.microtesk.basis.solver.integer.IntegerVariable;

public final class MmuDynamicConst extends IntegerVariable {
  private final Value<?> value;

  public MmuDynamicConst(
      final String name,
      final int width,
      final Value<?> value) {
    super(name, width);

    InvariantChecks.checkNotNull(value);
    this.value = value;
  }

  @Override
  public BigInteger getValue() {
    final Object object = value.value();
    final BigInteger result;

    if (object instanceof BigInteger) {
      result = (BigInteger) object;
    } else if (object instanceof BitVector) {
      result = ((BitVector) object).bigIntegerValue(false);
    } else if (object instanceof Boolean) {
      result = ((Boolean) object) ? BigInteger.ONE : BigInteger.ZERO;
    } else {
      throw new ClassCastException(
          "Type " + object.getClass().getName() + " in not supported for constants.");
    }

    return result;
  }
}
