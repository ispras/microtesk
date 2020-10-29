/*
    Copyright 2019 ISP RAS (http://www.ispras.ru)

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/

package ru.ispras.microtesk.translator.mir;

import java.util.Arrays;
import java.util.List;

final class Lists {
  public static <T> T lastOf(final List<? extends T> list) {
    return list.get(list.size() - 1);
  }

  public static <T> T removeLast(final List<? extends T> list) {
    return list.remove(list.size() - 1);
  }

  public static <T> List<T> removeLastN(final List<? extends T> list, final int n) {
    final List<? extends T> tail = tailOf(list, list.size() - n);
    final List<T> items = new java.util.ArrayList<T>(tail);
    tail.clear();

    return items;
  }

  public static <T> List<T> tailOf(final List<T> list, final int index) {
    return list.subList(index, list.size());
  }

  @SafeVarargs
  public static <T> List<T> newList(final T... items) {
    return new java.util.ArrayList<T>(Arrays.asList(items));
  }

  public static <T> void moveAll(
      final List<? super T> dst, final List<? extends T> src) {
    dst.clear();
    dst.addAll(src);
    src.clear();
  }

  private Lists() { }
}
