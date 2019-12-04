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

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.model.memory.MemoryAllocator;
import ru.ispras.microtesk.options.Option;
import ru.ispras.microtesk.options.Options;

import java.math.BigInteger;

public class DirectiveAlign extends Directive {
  protected final Options options;
  protected final BigInteger alignment;
  protected final BigInteger alignmentInBytes;

  DirectiveAlign(
      final Options options,
      final BigInteger alignment,
      final BigInteger alignmentInBytes) {
    InvariantChecks.checkNotNull(options);
    InvariantChecks.checkNotNull(alignment);
    InvariantChecks.checkNotNull(alignmentInBytes);

    this.options = options;
    this.alignment = alignment;
    this.alignmentInBytes = alignmentInBytes;
  }

  @Override
  public Kind getKind() {
    return Kind.ALIGN;
  }

  @Override
  public String getText() {
    return String.format(options.getValueAsString(Option.ALIGN_FORMAT), alignment);
  }

  @Override
  public BigInteger apply(final BigInteger currentAddress, final MemoryAllocator allocator) {
    return MemoryAllocator.alignAddress(currentAddress, alignmentInBytes.intValue());
  }

  @Override
  public String toString() {
    return String.format("%s %s %d bytes",
        getText(), options.getValueAsString(Option.COMMENT_TOKEN), alignmentInBytes);
  }
}
