/*
 * Copyright 2016 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.translator.nml.ir.analysis;

public final class MemoryAccessStatus {
  public static final MemoryAccessStatus NO =
      new MemoryAccessStatus(false, false, 0);

  private boolean load;
  private boolean store;
  private int blockSize;

  public MemoryAccessStatus(
      final boolean load,
      final boolean store,
      final int blockSize) {
    this.load = load;
    this.store = store;
    this.blockSize = blockSize;
  }

  public boolean isLoad() { return load; }
  public boolean isStore() { return store; }
  public int getBlockSize() { return blockSize; }

  public MemoryAccessStatus merge(final MemoryAccessStatus other) {
    return new MemoryAccessStatus(
        this.load  || other.load,
        this.store || other.store,
        Math.max(this.blockSize, other.blockSize)
        );
  }

  @Override
  public String toString() {
    return String.format(
        "MemoryAccessStatus [isLoad=%s, isStore=%s, blockSize=%s]",
        load,
        store,
        blockSize
        );
  }
}
