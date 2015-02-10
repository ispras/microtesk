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

package ru.ispras.microtesk.translator.mmu.ir;

import static ru.ispras.fortress.util.InvariantChecks.checkNotNull;
import static ru.ispras.fortress.util.InvariantChecks.checkGreaterThanZero;

public final class Address {
  private final String id;
  private final int bitSize;

  public Address(String id, int bitSize) {
    checkNotNull(id);
    checkGreaterThanZero(bitSize);

    this.id = id;
    this.bitSize = bitSize;
  }

  public String getId() {
    return id;
  }

  public int getBitSize() {
    return bitSize;
  }

  @Override
  public String toString() {
    return String.format("address %s(%d)", id, bitSize);
  }
}
