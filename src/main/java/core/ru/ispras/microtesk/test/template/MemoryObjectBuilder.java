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
import ru.ispras.microtesk.Logger;

public final class MemoryObjectBuilder {
  private final MemoryMap memoryMap;

  private String name = null;
  private BigInteger va = null;
  private BigInteger pa = null;
  private int size = 0;
  private BigInteger data = null;

  protected MemoryObjectBuilder(final MemoryMap memoryMap) {
    InvariantChecks.checkNotNull(memoryMap);
    this.memoryMap = memoryMap;
  }

  public void setName(final String name) {
    InvariantChecks.checkNotNull(name);
    this.name = name;
  }

  public void setVa(final BigInteger address) {
    InvariantChecks.checkNotNull(address);
    this.va = address;
  }

  public void setVa(final BigInteger addressFrom, final BigInteger addressTo) {
    InvariantChecks.checkNotNull(addressFrom);
    InvariantChecks.checkNotNull(addressTo);

    // TODO
  }

  public void setVa(final String labelName) {
    InvariantChecks.checkNotNull(labelName);
    this.va = memoryMap.resolve(labelName);
  }

  public void setPa(final BigInteger address) {
    InvariantChecks.checkNotNull(address);
    this.pa = address;
  }

  public void setPa(final BigInteger addressFrom, final BigInteger addressTo) {
    InvariantChecks.checkNotNull(addressFrom);
    InvariantChecks.checkNotNull(addressTo);

    // TODO
  }

  public void setPa(final String regionName) {
    InvariantChecks.checkNotNull(regionName);
    // TODO
  }

  public void setSize(final int size) {
    InvariantChecks.checkGreaterThanZero(size);
    this.size = size;
  }

  public void setData(final BigInteger data) {
    InvariantChecks.checkNotNull(data);
    this.data = data;
  }

  public MemoryObject build() {
    checkInitialized("va", va != null);
    checkInitialized("size", size > 0);

    final MemoryObject result = new MemoryObject(name, va, pa, size);

    if (null != data) {
      result.setData(data);
    }

    Logger.debug(result.toString());
    return result;
  }

  private static void checkInitialized(final String id, final boolean state) {
    if (!state) {
      throw new IllegalStateException(
          String.format("%s is not initialized", id));
    }
  }
}
