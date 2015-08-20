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

package ru.ispras.microtesk.mmu.translator.generation;

import java.math.BigInteger;
import java.util.Collection;

import ru.ispras.fortress.util.InvariantChecks;

public final class Utils {
  private Utils() {}

  public static String toString(final Collection<String> list) {
    return toString(list, ".");
  }

  public static String toString(final Collection<String> list, final String sep) {
    InvariantChecks.checkNotNull(list);

    final StringBuilder sb = new StringBuilder();
    for (final String string : list) {
      if (sb.length() != 0) {
        sb.append(sep);
      }
      sb.append(string);
    }

    return sb.toString();
  }

  public String bigIntegerToString(final BigInteger value) {
    InvariantChecks.checkNotNull(value);
    return String.format("new BigInteger(\"%d\", 10)", value);
  }
}
