/*
 * Copyright 2020 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.basis.solver.bitvector;

/**
 * {@link IntArray} implements an integer array list.
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class IntArray {
  private static final int DEFAULT_SIZE = 64;
  private static final float GROWTH_FACTOR = 1.25f;

  private int length;
  private int[] array;

  public IntArray() {
    this(DEFAULT_SIZE);
  }

  public IntArray(final int length) {
    this.array = new int[length];
  }

  public IntArray(final int[] items) {
    this.length = items.length;
    this.array = items;
  }

  public boolean isEmpty() {
    return length == 0;
  }

  public int length() {
    return this.length;
  }

  public int[] toArray() {
    return array;
  }

  public int get(final int i) {
    return array[i];
  }

  public void set(final int i, final int item) {
    array[i] = item;
  }

  public int indexOf(final int item) {
    for (int i = 0; i < length; i++) {
      if (array[i] == item) {
        return i;
      }
    }

    return -1;
  }

  public boolean contains(final int item) {
    return indexOf(item) != -1;
  }

  public void add(final int item) {
    ensure(1);
    array[length++] = item;
  }

  public void addAll(final int... items) {
    ensure(items.length);
    System.arraycopy(items, 0, array, length, items.length);
    length += items.length;
  }

  public void clear() {
    length = 0;
  }

  private void ensure(final int numberOfItems) {
    final int targetLength = length + numberOfItems;

    if (targetLength > array.length) {
      final int newLength = Math.max((int) (GROWTH_FACTOR * targetLength), DEFAULT_SIZE);
      final int[] newArray = new int[newLength];

      System.arraycopy(array, 0, newArray, 0, length);
      array = newArray;
    }
  }

  @Override
  public boolean equals(final Object object) {
    if (this == object) {
      return true;
    }

    if (!(object instanceof IntArray)) {
      return false;
    }

    final IntArray other = (IntArray) object;

    if (this.length != other.length) {
      return false;
    }

    for(int i = 0; i < this.length; i++) {
      if (this.array[i] != other.array[i]) {
        return false;
      }
    }

    return true;
  }

  @Override
  public int hashCode() {
    int sum = 0;

    for(int i = 0; i < length; i++) {
      sum = 13 * sum + array[i];
    }

    return 13 * sum + length;
  }

  @Override
  public String toString() {
    final StringBuffer buffer = new StringBuffer();

    boolean comma = false;
    for(int i = 0; i < length; i++) {
      if (comma) {
        buffer.append(", ");
      }
      comma = true;
      buffer.append(array[i]);
    }

    return buffer.toString();
  }
}
