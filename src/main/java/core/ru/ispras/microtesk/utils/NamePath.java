/*
 * Copyright 2018 ISP RAS (http://www.ispras.ru)
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

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public final class NamePath implements Iterable<NamePath>, Comparable<NamePath> {
  private static final String DEFAULT_SEPARATOR = ".";

  private final List<String> list;

  private NamePath(final List<String> list) {
    this.list = list;
  }

  public int getNameCount() {
    return list.size();
  }

  public NamePath getName(int index) {
    return NamePath.get(list.get(index));
  }

  public NamePath resolve(final String other) {
    return resolve(this.list, Collections.singletonList(other));
  }

  public NamePath resolve(final NamePath other) {
    return resolve(this.list, other.list);
  }

  public NamePath subpath(final int begin, final int end) {
    return new NamePath(list.subList(begin, end));
  }

  public boolean startsWith(final NamePath other) {
    if (other != null) {
      return startsWith(this.list.iterator(), other.list.iterator());
    }
    return false;
  }

  public boolean endsWith(final NamePath other) {
    if (other != null) {
      return startsWith(new InverseIterator<>(this.list), new InverseIterator<>(other.list));
    }
    return false;
  }

  private static <T> boolean startsWith(final Iterator<? extends T> lhs, final Iterator<? extends T> rhs) {
    boolean eq = true;
    while (lhs.hasNext() && rhs.hasNext() && eq) {
      eq = lhs.next().equals(rhs.next());
    }
    return eq && !rhs.hasNext();
  }

  private static final class InverseIterator<T> implements Iterator<T> {
    final ListIterator<T> it;

    public InverseIterator(final List<T> list) {
      this.it = list.listIterator(list.size());
    }

    @Override
    public boolean hasNext() {
      return it.hasPrevious();
    }

    @Override
    public T next() {
      return it.previous();
    }
  }

  @Override
  public Iterator<NamePath> iterator() {
    return new Iterator<NamePath>() {
      private int i = 0;

      @Override
      public boolean hasNext() {
        return (i < getNameCount());
      }

      @Override
      public NamePath next() {
        return getName(i++);
      }
    };
  }

  @Override
  public int compareTo(final NamePath other) {
    if (other != null) {
      final Iterator<String> lhs = this.list.iterator();
      final Iterator<String> rhs = other.list.iterator();

      int rc = 0;
      while (lhs.hasNext() && rhs.hasNext() && rc == 0) {
        rc = lhs.next().compareTo(rhs.next());
      }
      if (rc == 0 && lhs.hasNext() != rhs.hasNext()) {
        rc = (lhs.hasNext()) ? 1 : -1;
      }
      return rc;
    }
    throw new NullPointerException();
  }

  @Override
  public String toString() {
    return this.toString(DEFAULT_SEPARATOR);
  }

  @Override
  public boolean equals(final Object other) {
    if (other != null && other instanceof NamePath) {
      return this.list.equals(((NamePath) other).list);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return this.list.hashCode();
  }

  public String toString(final String sep) {
    if (list.size() > 1) {
      final Iterator<String> it = list.iterator();
      final StringBuilder builder = new StringBuilder(it.next());
      while (it.hasNext()) {
        builder.append(sep).append(it.next());
      }
      return builder.toString();
    } else if (!list.isEmpty()) {
      return list.get(0);
    }
    return "";
  }

  public static NamePath get(final NamePath head, final NamePath... tail) {
    if (tail.length == 0) {
      return head;
    }
    int size = head.getNameCount();
    for (final NamePath p : tail) {
      size += p.getNameCount();
    }

    final List<String> list = Arrays.asList(new String[size]);
    Collections.copy(list, head.list);

    int index = head.getNameCount();
    for (final NamePath p : tail) {
      Collections.copy(list.subList(index, size), p.list);
      index += p.getNameCount();
    }
    return new NamePath(list);
  }

  public static NamePath get(final NamePath head, final String... tail) {
    if (tail.length == 0) {
      return head;
    }
    return resolve(head.list, Arrays.asList(tail));
  }

  public static NamePath get(final String head, final String... tail) {
    if (tail.length == 0) {
      return new NamePath(Collections.singletonList(head));
    }
    return resolve(Collections.singletonList(head), Arrays.asList(tail));
  }

  private static NamePath resolve(final List<String> lead, final List<String> tail) {
    final List<String> list = Arrays.asList(new String[lead.size() + tail.size()]);
    Collections.copy(list, lead);
    Collections.copy(list.subList(lead.size(), list.size()), tail);

    return new NamePath(list);
  }
}
