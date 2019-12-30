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

  public static <T> List<? extends T> tailOf(
      final List<? extends T> list, final int index) {
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
