/*
 * Copyright (c) 2014 ISPRAS (www.ispras.ru)
 * 
 * Institute for System Programming of Russian Academy of Sciences
 * 
 * 25 Alexander Solzhenitsyn st. Moscow 109004 Russia
 * 
 * All rights reserved.
 * 
 * FortressUtils.java, Oct 7, 2014 6:16:40 PM Andrei Tatarnikov
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

import ru.ispras.fortress.data.Data;
import ru.ispras.fortress.data.DataTypeId;
import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeValue;

public final class FortressUtils {
  private FortressUtils() {}

  public static int extractInt(Node value) {
    if (null == value) {
      throw new NullPointerException();
    }

    if (value.getKind() != Node.Kind.VALUE) {
      throw new IllegalStateException(String.format("%s is not a constant value.", value));
    }

    final Data data = ((NodeValue) value).getData();
    final int intValue;

    switch (data.getType().getTypeId()) {
      case LOGIC_INTEGER:
        intValue = ((Integer) data.getValue()).intValue();
        break;

      case BIT_VECTOR:
        intValue = ((BitVector) data.getValue()).intValue();
        break;

      default:
        throw new IllegalStateException(String.format("%s cannot be converted to int", data));
    }

    return intValue;
  }

  public static BitVector extractBitVector(Node value) {
    if (null == value) {
      throw new NullPointerException();
    }

    if (value.getKind() != Node.Kind.VALUE) {
      throw new IllegalStateException(String.format("%s is not a constant value.", value));
    }

    final Data data = ((NodeValue) value).getData();
    if (data.getType().getTypeId() != DataTypeId.BIT_VECTOR) {
      throw new IllegalStateException(String.format("%s is not a bit vector.", value));
    }

    return (BitVector) data.getValue();
  }
}
