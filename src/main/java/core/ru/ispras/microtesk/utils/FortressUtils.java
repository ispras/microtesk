/*
 * Copyright 2014-2015 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.utils;

import static ru.ispras.fortress.util.InvariantChecks.checkNotNull;
import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeValue;

public final class FortressUtils {
  private FortressUtils() {}

  public static int extractInt(final Node expr) {
    checkConstantValue(expr);
    final NodeValue value = (NodeValue) expr;

    switch (value.getDataTypeId()) {
      case LOGIC_INTEGER:
        return value.getInteger().intValue();

      case BIT_VECTOR:
        return value.getBitVector().intValue();

      default:
        throw new IllegalStateException(String.format("%s cannot be converted to int", value));
    }
  }

  public static BitVector extractBitVector(final Node expr) {
    checkConstantValue(expr);
    return ((NodeValue) expr).getBitVector();
  }

  private static void checkConstantValue(final Node expr) {
    checkNotNull(expr);
    if (expr.getKind() != Node.Kind.VALUE) {
      throw new IllegalStateException(String.format("%s is not a constant value.", expr));
    }
  }
}
