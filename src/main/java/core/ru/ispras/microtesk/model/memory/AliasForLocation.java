/*
 * Copyright 2015-2017 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.model.memory;

import java.math.BigInteger;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.model.data.Data;
import ru.ispras.microtesk.model.data.Type;

/**
 * The {@link AliasForLocation} class implements a memory storage which
 * is an alias for some location.
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
final class AliasForLocation extends Memory {
  private final Location source;

  public AliasForLocation(
      final Kind kind,
      final String name,
      final Type type,
      final BigInteger length,
      final Location source) {
    super(kind, name, type, length, true);
    InvariantChecks.checkNotNull(source);

    final int totalBitSize = type.getBitSize() * length.intValue();
    if (source.getBitSize() != totalBitSize) {
      throw new IllegalArgumentException();
    }

    this.source = source;
  }

  @Override
  public Location access(final int index) {
    InvariantChecks.checkBounds(index, getLength().intValue());

    final int locationBitSize = getType().getBitSize();
    final int start = locationBitSize * index;
    final int end = start + locationBitSize - 1;

    final Location bitField = source.bitField(start, end);
    return bitField.castTo(getType().getTypeId());
  }

  @Override
  public Location access(final long address) {
    return access((int) address);
  }

  @Override
  public Location access(final BigInteger address) {
    return access(address.intValue());
  }

  @Override
  public Location access(final Data address) {
    return access(address.getRawData().intValue());
  }

  @Override
  public Memory copy() {
    throw new UnsupportedOperationException("Copying of aliases is not supported.");
  }

  @Override
  public void reset() {
    // Does not work for aliases (and should not be called)
  }
}
