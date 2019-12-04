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

import java.math.BigInteger;

public final class DirectiveAsciiStrings extends Directive {
  private final String ztermStrText;
  private final String nztermStrText;
  private final boolean zeroTerm;
  private final String[] strings;

  DirectiveAsciiStrings(
      final String ztermStrText,
      final String nztermStrText,
      final boolean zeroTerm,
      final String[] strings) {
    InvariantChecks.checkTrue(zeroTerm ? ztermStrText != null : nztermStrText != null);
    InvariantChecks.checkNotEmpty(strings);

    this.ztermStrText = ztermStrText;
    this.nztermStrText = nztermStrText;
    this.zeroTerm = zeroTerm;
    this.strings = strings;
  }

  @Override
  public Kind getKind() {
    return Kind.DATA;
  }

  @Override
  public String getText() {
    final StringBuilder sb = new StringBuilder(zeroTerm ? ztermStrText : nztermStrText);
    for (int index = 0; index < strings.length; index++) {
      if (index > 0) {
        sb.append(',');
      }
      sb.append(String.format(" \"%s\"", strings[index]));
    }
    return sb.toString();
  }

  @Override
  public BigInteger apply(final BigInteger currentAddress, final MemoryAllocator allocator) {
    BigInteger current = currentAddress;

    for (int index = 0; index < strings.length; index++) {
      current = allocator.allocateAsciiString(current, strings[index], zeroTerm).second;
    }

    return current;
  }
}

