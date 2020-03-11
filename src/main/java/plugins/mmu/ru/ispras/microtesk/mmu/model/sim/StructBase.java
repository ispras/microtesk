/*
 * Copyright 2012-2020 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.mmu.model.sim;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.util.InvariantChecks;

/**
 * {@link StructBase} is an abstract base class for buffer entries.
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public abstract class StructBase<T> implements Struct<T> {
  private BitVector entry = null;

  @Override
  public abstract T newStruct(BitVector value);

  @Override
  public final BitVector asBitVector() {
    return entry;
  }

  protected void setEntry(final BitVector entry) {
    InvariantChecks.checkTrue(this.entry == null && entry != null);
    this.entry = entry;
  }

  public final void assign(final BitVector entry) {
    // If entry is of different size, it will be truncated or zero extended.
    this.entry.assign(entry);
  }
}
