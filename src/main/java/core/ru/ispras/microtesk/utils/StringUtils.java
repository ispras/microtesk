/*
 * Copyright 2016 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.utils;

import java.util.Collection;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.fortress.util.Pair;

/**
 * The {@link StringUtils} class provides utility methods to deal with strings.
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
public final class StringUtils {
  private StringUtils() {}

  public static interface Converter<T> {
    String toString(T o);
  }

  public static <T> String toString(
      final Collection<T> list,
      final String sep) {
    return toString(list, sep, null);
  }

  public static <T> String toString(
      final Collection<T> list,
      final String sep,
      final Converter<T> converter) {
    InvariantChecks.checkNotNull(list);
    InvariantChecks.checkNotNull(sep);

    final StringBuilder sb = new StringBuilder();
    for (final T item : list) {
      if (sb.length() != 0) {
        sb.append(sep);
      }
      sb.append(
          null != converter ? converter.toString(item) : item.toString());
    }

    return sb.toString();
  }

  public static Pair<String, String> splitOnLast(final String str, final char c) {
    InvariantChecks.checkNotNull(str);
    return splitOnIndex(str, str.lastIndexOf(c));
  }

  public static Pair<String, String> splitOnFirst(final String str, final char c) {
    InvariantChecks.checkNotNull(str);
    return splitOnIndex(str, str.indexOf(c));
  }

  private static Pair<String, String> splitOnIndex(final String str, final int index) {
    if (index < 0) {
      return new Pair<>("", str);
    }
    return new Pair<>(str.substring(0, index), str.substring(index + 1));
  }
}
