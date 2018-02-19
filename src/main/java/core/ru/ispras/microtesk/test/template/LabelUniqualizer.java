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

package ru.ispras.microtesk.test.template;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.fortress.util.Pair;
import ru.ispras.microtesk.test.GenerationAbortedException;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class LabelUniqualizer {
  public static final class SeriesId {
    private final int index;

    private SeriesId(int index) {
      this.index = index;
    }
  }

  private static LabelUniqualizer instance = null;

  private final List<Integer> numbers;
  private final Deque<Pair<Integer, Set<String>>> labelScopes;

  private LabelUniqualizer() {
    this.numbers = new ArrayList<>();
    this.labelScopes = new ArrayDeque<>();
  }

  public static LabelUniqualizer get() {
    if (null == instance) {
      instance = new LabelUniqualizer();
    }
    return instance;
  }

  public SeriesId newSeries() {
    numbers.add(0);
    return new SeriesId(numbers.size() - 1);
  }

  public void resetNumbers() {
    InvariantChecks.checkTrue(labelScopes.isEmpty());

    for (int index = 0; index < numbers.size(); index++) {
      numbers.set(index, 0);
    }
  }

  public void pushLabelScope(final SeriesId seriesId) {
    InvariantChecks.checkNotNull(seriesId);

    final int referenceNumber = numbers.get(seriesId.index);
    numbers.set(seriesId.index, referenceNumber + 1);

    labelScopes.push(
        new Pair<>(referenceNumber, (Set<String>) new HashSet<String>()));
  }

  public void popLabelScope() {
    labelScopes.pop();
  }

  public void makeLabelsUnique(final AbstractCall call) {
    InvariantChecks.checkNotNull(call);

    final int referenceNumber = labelScopes.peek().first;
    final Set<String> labelNames = labelScopes.peek().second;

    if (null != call.getData()) {
      setReferenceNumber(call.getData().getLabels(), referenceNumber, labelNames);
    }
    setReferenceNumber(call.getLabels(), referenceNumber, labelNames);

    for (final LabelReference labelRef : call.getLabelReferences()) {
      final Label label = labelRef.getReference();
      if (labelNames.contains(label.getName())) {
        label.setReferenceNumber(referenceNumber);
      }
    }
  }

  private static void setReferenceNumber(
      final List<Label> labels,
      final int referenceNumber,
      final Set<String> labelNames) {
    for (final Label label : labels) {
      label.setReferenceNumber(referenceNumber);
      if (!labelNames.add(label.getName())) {
        throw new GenerationAbortedException(String.format(
            "The %s label is redefined.", label.getName()));
      }
    }
  }
}
