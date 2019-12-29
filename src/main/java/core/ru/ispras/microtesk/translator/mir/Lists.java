package ru.ispras.microtesk.translator.mir;

import java.util.List;

final class Lists {
  public static <T> T lastOf(final List<? extends T> list) {
    return list.get(list.size() - 1);
  }

  public static <T> T removeLast(final List<? extends T> list) {
    return list.remove(list.size() - 1);
  }

  public static <T> List<T> removeLastN(final List<? extends T> list, final int n) {
    final List<? extends T> tail = list.subList(list.size() - n, list.size());
    final List<T> items = new java.util.ArrayList<T>(tail);
    tail.clear();

    return items;
  }

  private Lists() { }
}
