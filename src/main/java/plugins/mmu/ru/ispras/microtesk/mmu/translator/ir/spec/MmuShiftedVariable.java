/*
 * Copyright 2017 ISP RAS (http://www.ispras.ru)
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
import ru.ispras.fortress.data.types.bitvector.BitVectorMath;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.basis.solver.integer.IntegerVariable;

public final class MmuShiftedVariable extends IntegerVariable {
  private final IntegerVariable variable;
  private final IntegerVariable shift;
  private final boolean left;

  public static MmuShiftedVariable right(
      final IntegerVariable variable,
      final IntegerVariable shift) {
    return new MmuShiftedVariable(variable, shift, false);
  }

  public static MmuShiftedVariable left(
      final IntegerVariable variable,
      final IntegerVariable shift) {
    return new MmuShiftedVariable(variable, shift, true);
  }

  public MmuShiftedVariable(
      final IntegerVariable variable,
      final IntegerVariable shift,
      final boolean left) {
    super(null != variable ? variable.getWidth() : 0, null);

    InvariantChecks.checkNotNull(variable);
    InvariantChecks.checkNotNull(shift);

    this.variable = variable;
    this.shift = shift;
    this.left = left;
  }

  public IntegerVariable getVariable() {
    return variable;
  }

  public IntegerVariable getShift() {
    return shift;
  }

  public boolean isLeft() {
    return left;
  }

  @Override
  public BigInteger getValue() {
    final BitVector value = BitVector.valueOf(variable.getValue(), variable.getWidth());

    if (left) {
      BitVectorMath.shl(value, shift.getValue());
    } else {
      BitVectorMath.lshr(value, shift.getValue());
    }

    return value.bigIntegerValue(false);
  }
}
