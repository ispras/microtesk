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

package ru.ispras.microtesk.test;

import ru.ispras.fortress.util.InvariantChecks;

import java.util.Arrays;

public final class NumericLabelTracker {
  private final int[] referenceNumbers;
  private final int[] referenceNumbersCache;

  public NumericLabelTracker() {
    this.referenceNumbers = new int[10];
    this.referenceNumbersCache = new int[10];
    reset();
  }

  public NumericLabelTracker(final NumericLabelTracker other) {
    this();

    InvariantChecks.checkNotNull(other);
    System.arraycopy(
        other.referenceNumbers,0,
        this.referenceNumbers,0,
        this.referenceNumbers.length
    );
  }

  public void reset() {
    Arrays.fill(referenceNumbers, 0);
    Arrays.fill(referenceNumbersCache, 0);
  }

  public void save() {
    for (int index = 0; index < referenceNumbers.length; index++) {
      referenceNumbersCache[index] = referenceNumbers[index];
    }
  }

  public void restore() {
    for (int index = 0; index < referenceNumbers.length; index++) {
      referenceNumbers[index] = referenceNumbersCache[index];
    }
  }

  public int nextReferenceNumber(final String labelIndexText) {
    final int index = Integer.parseInt(labelIndexText, 10);
    InvariantChecks.checkBounds(index, referenceNumbers.length);

    final int referenceNumber = referenceNumbers[index];
    referenceNumbers[index] = referenceNumber + 1;

    return referenceNumber;
  }

  public int getReferenceNumber(final String labelIndexText, final boolean forward) {
    final int index = Integer.parseInt(labelIndexText, 10);
    InvariantChecks.checkBounds(index, referenceNumbers.length);

    final int referenceNumber = referenceNumbers[index];

    if (forward) {
      return referenceNumber;
    }

    if (referenceNumber <= 0) {
      throw new IllegalArgumentException(
          String.format("Label '%d' is not defined and cannot be referenced as '%<db'.", index));
    }

    return referenceNumber - 1;
  }

  @Override
  public String toString() {
    return Arrays.toString(referenceNumbers);
  }
}
