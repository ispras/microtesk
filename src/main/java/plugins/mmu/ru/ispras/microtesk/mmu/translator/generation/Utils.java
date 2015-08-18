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

import java.util.List;

import ru.ispras.fortress.util.InvariantChecks;

public final class Utils {
  private Utils() {}

  public static String listToString(final List<String> list) {
    return listToString(list, '.');
  }

  public static String listToString(final List<String> list, final char ch) {
    InvariantChecks.checkNotNull(list);

    final StringBuilder sb = new StringBuilder();
    for (final String string : list) {
      if (sb.length() != 0) {
        sb.append(ch);
      }
      sb.append(string);
    }
    return sb.toString();
  }
}
