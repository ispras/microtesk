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
import ru.ispras.microtesk.model.api.memory.MemoryAccessMode;

public final class MemoryObject {
  private final String name;
  private final int size;
  private final MemoryAccessMode mode;
  private final BigInteger va;
  private final BigInteger pa;
  private BigInteger data;

  protected MemoryObject(
      final String name,
      final int size,
      final MemoryAccessMode mode,
      final BigInteger va,
      final BigInteger pa) {
    InvariantChecks.checkGreaterThanZero(size);
    InvariantChecks.checkNotNull(mode);
    InvariantChecks.checkNotNull(va);

    this.name = name;
    this.size = size;
    this.mode = mode;
    this.va = va;
    this.pa = pa;
    this.data = null;
  }

  public String getName() {
    return name;
  }

  // Data size in bytes
  public int getSize() {
    return size;
  }

  public MemoryAccessMode getMode() {
    return mode;
  }

  public BigInteger getVa() {
    return va;
  }

  public BigInteger getPa() {
    return pa;
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
        "MemoryObject %s[size=%d, mode=%s, va=%s, pa=%s, data=%s]",
        name != null ? name : "",
        size,
        mode,
        toHexString(va),
        toHexString(pa),
        toHexString(data)
        );
  }

  private static String toHexString(final BigInteger value) {
    return value != null ? String.format("0x%x", value) : "null";
  }
}
