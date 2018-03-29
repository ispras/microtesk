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

package ru.ispras.microtesk.test.template;

import ru.ispras.fortress.util.InvariantChecks;

import java.util.Arrays;

/**
 * The {@link NumericLabelFactory} is responsible for constructing numeric labels and
 * references to them. It track relative order of labels with the name number and
 * marks it with the reference number attribute.
 *
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
final class NumericLabelFactory {
  private final int[] referenceNumbers;

  public NumericLabelFactory() {
    this.referenceNumbers = new int[10];
    Arrays.fill(referenceNumbers, 0);
  }

  public Label newLabel(final int index, final BlockId blockId) {
    InvariantChecks.checkBounds(index, referenceNumbers.length);
    InvariantChecks.checkNotNull(blockId);

    final String name = Integer.toString(index);
    final Label label = new Label(name, blockId, false, true);

    final int referenceNumber = referenceNumbers[index];
    label.setReferenceNumber(referenceNumber);

    referenceNumbers[index] = referenceNumber + 1;
    return label;
  }

  public LabelValue newLabelRef(final int index, final BlockId blockId, final boolean forward) {
    InvariantChecks.checkBounds(index, referenceNumbers.length);
    InvariantChecks.checkNotNull(blockId);

    final String name = Integer.toString(index);
    final Label label = new Label(name, blockId, false, true);

    final int referenceNumber = referenceNumbers[index];
    if (forward) {
      label.setReferenceNumber(referenceNumber);
    } else {
      if (referenceNumber <= 0) {
        throw new IllegalArgumentException(
            String.format("Label '%d' is not defined and cannot be referenced as '%<db'.", index));
      }
      label.setReferenceNumber(referenceNumber - 1);
    }

    final LabelValue labelValue = LabelValue.newUnknown(label);
    labelValue.setSuffix(forward ? "f" : "b");

    return labelValue;
  }
}
