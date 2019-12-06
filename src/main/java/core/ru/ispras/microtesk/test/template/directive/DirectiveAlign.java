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
import ru.ispras.microtesk.model.memory.MemoryAllocator;
import ru.ispras.microtesk.options.Option;
import ru.ispras.microtesk.options.Options;

import java.math.BigInteger;

public class DirectiveAlign extends Directive {
  protected final int alignment;
  protected final int alignmentInBytes;
  protected final int fillWith;

  DirectiveAlign(
      final Options options,
      final int alignment,
      final int alignmentInBytes,
      final int fillWith) {
    super(options);

    this.alignment = alignment;
    this.alignmentInBytes = alignmentInBytes;
    this.fillWith = fillWith;
  }

  @Override
  public Kind getKind() {
    return Kind.ALIGN;
  }

  @Override
  public String getText() {
    return fillWith == -1
      ? String.format(options.getValueAsString(Option.ALIGN_FORMAT), alignment)
      : String.format(options.getValueAsString(Option.ALIGN_FORMAT2), alignment, fillWith);
  }

  @Override
  public BigInteger apply(final BigInteger currentAddress, final MemoryAllocator allocator) {
    final BigInteger alignedAddress = MemoryAllocator.alignAddress(currentAddress, alignmentInBytes);

    if (fillWith != -1) {
      final BigInteger delta = alignedAddress.subtract(currentAddress);

      if (!delta.equals(BigInteger.ZERO)) {
        allocator.allocate(
            currentAddress,
            BitVector.valueOf(fillWith, allocator.getAddressableUnitBitSize()),
            delta.intValue());
      }
    }

    return alignedAddress;
  }

  @Override
  public String toString() {
    return String.format("%s %s %d bytes",
        getText(), options.getValueAsString(Option.COMMENT_TOKEN), alignmentInBytes);
  }
}
