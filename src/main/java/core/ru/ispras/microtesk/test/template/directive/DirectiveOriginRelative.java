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

public final class DirectiveOriginRelative extends Directive {
  private final BigInteger delta;
  private BigInteger origin;

  DirectiveOriginRelative(final Options options, final BigInteger delta) {
    this(options, delta, null);
  }

  DirectiveOriginRelative(final Options options, final BigInteger delta, final BigInteger origin) {
    super(options);

    InvariantChecks.checkNotNull(delta);
    this.delta = delta;
    this.origin = origin;
  }

  @Override
  public Kind getKind() {
    return Kind.ORIGIN;
  }

  @Override
  public String getText() {
    return origin != null
        ? String.format(options.getValueAsString(Option.ORIGIN_FORMAT), origin)
        : String.format(options.getValueAsString(Option.ORIGIN_FORMAT) + " (relative)", delta);
  }

  @Override
  public BigInteger apply(final BigInteger currentAddress, final MemoryAllocator allocator) {
    // Offset relative to the current address.
    final BigInteger address = currentAddress.add(delta);

    origin = address.subtract(allocator.getBaseAddress());
    InvariantChecks.checkTrue(origin.compareTo(BigInteger.ZERO) >= 0,
        String.format("Negative origin: %d (base %x)",
            origin.longValue(), allocator.getBaseAddress().longValue()));

    return address;
  }

  @Override
  public Directive copy() {
    return new DirectiveOriginRelative(options, delta, origin);
  }
}
