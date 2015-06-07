/*
 * Copyright 2012-2014 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.model.api.data.operations;

import java.math.BigInteger;

import ru.ispras.fortress.data.types.Radix;
import ru.ispras.fortress.data.types.bitvector.BitVector;

import ru.ispras.microtesk.model.api.data.Data;
import ru.ispras.microtesk.model.api.data.IValueConverter;
import ru.ispras.microtesk.model.api.type.Type;

public class IntCardConverter implements IValueConverter {
  @Override
  public Data fromLong(Type type, long value) {
    final BitVector rawData = BitVector.valueOf(value, type.getBitSize());
    return new Data(rawData, type);
  }

  @Override
  public long toLong(Data data) {
    assert data.getRawData().getBitSize() <= Long.SIZE;
    return data.getRawData().longValue();
  }

  @Override
  public Data fromInt(Type type, int value) {
    final BitVector rawData = BitVector.valueOf(value, type.getBitSize());
    return new Data(rawData, type);
  }

  @Override
  public int toInt(Data data) {
    assert data.getRawData().getBitSize() <= Integer.SIZE;
    return data.getRawData().intValue();
  }

  @Override
  public Data fromString(Type type, String value, Radix radix) {
    assert false : "NOT IMPLEMENTED";
    return null;
  }

  @Override
  public Data fromBigInteger(Type type, BigInteger value) {
    final BitVector rawData = BitVector.valueOf(value, type.getBitSize());
    return new Data(rawData, type);
  }
}
