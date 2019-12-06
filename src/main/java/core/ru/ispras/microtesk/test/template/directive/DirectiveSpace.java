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
import ru.ispras.microtesk.options.Options;

import java.math.BigInteger;

public class DirectiveSpace extends Directive {
  protected final String text;
  protected final BitVector data;
  protected final int length;

  DirectiveSpace(final Options options, final String text, final BitVector data, final int length) {
    super(options);

    InvariantChecks.checkNotNull(text);
    InvariantChecks.checkNotNull(data);
    InvariantChecks.checkGreaterThanZero(length);
    this.text = text;
    this.data = data;
    this.length = length;
  }

  @Override
  public Kind getKind() {
    return Kind.DATA;
  }

  @Override
  public String getText() {
    return String.format("%s %d", text, length);
  }

  @Override
  public BigInteger apply(final BigInteger currentAddress, final MemoryAllocator allocator) {
    return allocator.allocate(currentAddress, data, length).second;
  }
}
