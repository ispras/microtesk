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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.fortress.util.Pair;

public class DataSection {
  private final List<LabelValue> labelValues;
  private final List<DataDirective> directives;

  private final boolean global;
  private final boolean separateFile;

  protected DataSection(
      final List<LabelValue> labelValues,
      final List<DataDirective> directives,
      final boolean global,
      final boolean separateFile) {
    InvariantChecks.checkNotNull(directives);

    this.labelValues = Collections.unmodifiableList(labelValues);
    this.directives = Collections.unmodifiableList(directives);

    this.global = global;
    this.separateFile = separateFile;
  }

  protected DataSection(final DataSection other) {
    InvariantChecks.checkNotNull(other);

    this.labelValues = copyAllLabelValues(other.labelValues);
    this.directives = copyAllDirectives(other.directives);

    this.global = other.global;
    this.separateFile = other.separateFile;
  }

  private static List<LabelValue> copyAllLabelValues(final List<LabelValue> labelValues) {
    InvariantChecks.checkNotNull(labelValues);

    if (labelValues.isEmpty()) {
      return Collections.emptyList();
    }

    final List<LabelValue> result = new ArrayList<>(labelValues.size());
    for (final LabelValue labelValue : labelValues) {
      result.add(new LabelValue(labelValue));
    }

    return result;
  }

  private static List<DataDirective> copyAllDirectives(final List<DataDirective> directives) {
    InvariantChecks.checkNotNull(directives);

    if (directives.isEmpty()) {
      return Collections.emptyList();
    }

    final List<DataDirective> result = new ArrayList<>(directives.size());
    for (final DataDirective directive : directives) {
      result.add(directive.copy());
    }

    return result;
  }

  public List<Label> getLabels() {
    final List<Label> result = new ArrayList<>(labelValues.size());

    for (final LabelValue labelValue : labelValues) {
      final Label label = labelValue.getLabel();
      InvariantChecks.checkNotNull(label);
      result.add(label);
    }

    return result;
  }

  public List<Pair<Label, BigInteger>> getLabelAddresses() {
    final List<Pair<Label, BigInteger>> result = new ArrayList<>(labelValues.size());

    for (final LabelValue labelValue : labelValues) {
      final Label label = labelValue.getLabel();
      InvariantChecks.checkNotNull(label);

      final BigInteger address = labelValue.getAddress();
      InvariantChecks.checkNotNull(address);

      result.add(new Pair<>(label, address));
    }

    return result;
  }

  public List<DataDirective> getDirectives() {
    return directives;
  }

  public boolean isGlobal() {
    return global;
  }

  public boolean isSeparateFile() {
    return separateFile;
  }
}
