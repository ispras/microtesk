/*
 * Copyright 2014-2018 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.tools.microft;

import ru.ispras.fortress.util.Pair;

import java.util.Iterator;

public class IterablePair <T, U> implements Iterable<Pair<T, U>> {
  public static <T, U> IterablePair<T, U> create(
    final Iterable<? extends T> lhs,
    final Iterable<? extends U> rhs) {
    return new IterablePair<T, U>(lhs, rhs);
  }

  @Override
  public Iterator<Pair<T, U>> iterator() {
    return new PairIterator<T, U>(lhs.iterator(), rhs.iterator());
  }

  private final Iterable<? extends T> lhs;
  private final Iterable<? extends U> rhs;

  private IterablePair(final Iterable<? extends T> lhs, final Iterable<? extends U> rhs) {
    this.lhs = lhs;
    this.rhs = rhs;
  }

  private static class PairIterator <T, U> implements Iterator<Pair<T, U>> {
    private final Iterator<? extends T> lhs;
    private final Iterator<? extends U> rhs;

    PairIterator(final Iterator<? extends T> lhs, final Iterator<? extends U> rhs) {
      this.lhs = lhs;
      this.rhs = rhs;
    }

    @Override
    public boolean hasNext() {
      return lhs.hasNext() && rhs.hasNext();
    }

    @Override
    public Pair<T, U> next() {
      return new Pair<T, U>(lhs.next(), rhs.next());
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }
  }
}
