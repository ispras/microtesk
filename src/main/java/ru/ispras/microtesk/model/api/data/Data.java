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

package ru.ispras.microtesk.model.api.data;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.microtesk.model.api.type.Type;

public final class Data {
  private final BitVector rawData;
  private final Type type;

  public Data(BitVector rawData, Type type) {
    checkNotNull(rawData);
    checkNotNull(type);

    if (rawData.getBitSize() != type.getBitSize()) {
      throw new IllegalArgumentException(String.format(
          "Wrong data size: %d, expected: %d", rawData.getBitSize(), type.getBitSize()));
    }

    this.rawData = rawData;
    this.type = type;
  }

  public Data(Data data) {
    checkNotNull(data);

    this.rawData = data.getRawData().copy();
    this.type = data.getType();
  }

  public Data(Type type) {
    checkNotNull(type);

    this.rawData = BitVector.newEmpty(type.getBitSize());
    this.type = type;
  }

  public BitVector getRawData() {
    return rawData;
  }

  public Type getType() {
    return type;
  }

  private static void checkNotNull(Object o) {
    if (null == o) {
      throw new NullPointerException();
    }
  }
}
