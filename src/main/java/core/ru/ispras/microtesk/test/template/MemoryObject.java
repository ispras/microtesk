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

package ru.ispras.microtesk.test.template;

import java.math.BigInteger;
import ru.ispras.fortress.util.InvariantChecks;

public final class MemoryObject {
  private final String name;
  private final BigInteger va;
  private final BigInteger pa;
  private final int size;
  private BigInteger data;

  protected MemoryObject(
      final String name,
      final BigInteger va,
      final BigInteger pa,
      final int size) {
    InvariantChecks.checkNotNull(va);
    InvariantChecks.checkNotNull(pa);
    InvariantChecks.checkGreaterThanZero(size);

    this.name = name;
    this.va = va;
    this.pa = pa;
    this.size = size;
    this.data = null;
  }

  public String getName() {
    return name;
  }

  public BigInteger getVa() {
    return va;
  }

  public BigInteger getPa() {
    return pa;
  }

  // In Bytes
  public int getSize() {
    return size;
  }

  public BigInteger getData() {
    return data;
  }

  public void setData(final BigInteger value) {
    this.data = value;
  }

  @Override
  public String toString() {
    return String.format(
        "MemoryObject %s[va=0x%x, pa=0x%x, size=%d, data=%s]",
        name != null ? name : "",
        va,
        pa,
        size,
        data != null ? String.format("0x%x", data) : "null"
        );
  }
}
