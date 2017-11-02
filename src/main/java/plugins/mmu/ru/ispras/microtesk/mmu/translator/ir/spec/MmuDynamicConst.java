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
import ru.ispras.fortress.data.Data;
import ru.ispras.fortress.data.DataType;
import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.fortress.util.Value;

/**
 * The {@link MmuDynamicConst} class implements a dynamic constant that describes
 * a bit vector of the specified size. The constant value is read on request from
 * an external provider and is cast to the specified type. 
 *
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public final class MmuDynamicConst implements Value<Data> {
  private final Value<?> value;
  private final int bitSize;

  public MmuDynamicConst(final Value<?> value, final int bitSize) {
    InvariantChecks.checkNotNull(value);

    this.value = value;
    this.bitSize = bitSize;
  }

  @Override
  public Data value() {
    final Object object = value.value();
    return bitSize == DataType.LOGIC_TYPE_SIZE ? toLogicType(object) : toBitVector(object, bitSize);
  }

  private Data toLogicType(final Object object) {
    if (object instanceof BigInteger) {
      return Data.newInteger((BigInteger) object);
    }

    if (object instanceof Boolean) {
      return Data.newBoolean((Boolean) object);
    }

    throw new ClassCastException(String.format(
        "Type conversion from %s to a logic type is not supported.", object.getClass().getName()));
  }

  private static Data toBitVector(final Object object, final int bitSize) {
    if (object instanceof BigInteger) {
      return Data.newBitVector((BigInteger) object, bitSize);
    }

    if (object instanceof BitVector) {
      final BitVector bitVector = (BitVector) object;
      return bitVector.getBitSize() == bitSize ?
          Data.newBitVector(bitVector) :
          Data.newBitVector(bitVector.bigIntegerValue(false), bitSize);
    }

    if (object instanceof Boolean) {
      return Data.newBitVector(((Boolean) object) ? 1 : 0, bitSize);
    }

    throw new ClassCastException(String.format(
        "Type conversion from %s to a bit vector is not supported.", object.getClass().getName()));
  }
}
