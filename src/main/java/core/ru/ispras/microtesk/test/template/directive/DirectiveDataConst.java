/*
 * Copyright 2016-2019 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.test.template.directive;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.model.memory.MemoryAllocator;

import java.math.BigInteger;
import java.util.List;

public final class DirectiveDataConst extends Directive {
  private final String typeText;
  private final List<BitVector> values;

  DirectiveDataConst(
      final String typeText,
      final List<BitVector> values) {
    InvariantChecks.checkNotNull(typeText);
    InvariantChecks.checkNotEmpty(values);

    this.typeText = typeText;
    this.values = values;
  }

  @Override
  public String getText() {
    final StringBuilder sb = new StringBuilder(typeText);

    boolean isFirst = true;
    for (final BitVector value : values) {
      if (isFirst) {
        isFirst = false;
      } else {
        sb.append(',');
      }

      sb.append(" 0x");
      sb.append(value.toHexString());
    }

    return sb.toString();
  }

  @Override
  public BigInteger apply(final BigInteger currentAddress, final MemoryAllocator allocator) {
    BigInteger current = currentAddress;

    for (final BitVector value : values) {
      current = allocator.allocate(current, value).second;
    }

    return current;
  }
}
