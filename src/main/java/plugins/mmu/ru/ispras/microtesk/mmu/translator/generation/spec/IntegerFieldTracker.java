/*
 * Copyright 2015-2017 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.mmu.translator.generation.spec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ru.ispras.fortress.data.Variable;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.Nodes;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.utils.FortressUtils;

/**
 * This class is to track fields in a variable by exclusion.
 *
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */
final class IntegerFieldTracker {
  private final Variable variable;
  private List<Node> fields;

  private static boolean inField(final Node field, final int index) {
    return FortressUtils.getLowerBit(field) <= index && index <= FortressUtils.getUpperBit(field);
  }

  public IntegerFieldTracker(final Variable variable) {
    InvariantChecks.checkNotNull(variable);

    this.variable = variable;
    this.fields = Collections.singletonList(Nodes.BVEXTRACT(variable));
  }

  public void exclude(final int lo, final int hi) {
    if (fields.isEmpty()) {
      return;
    }

    if (hi < lo) {
      exclude(hi, lo);
      return;
    }

    final List<Node> newFields = new ArrayList<>();
    for (final Node field : fields) {
      final boolean isLoInField = inField(field, lo);
      final boolean isHiInField = inField(field, hi);

      if (isLoInField && lo > FortressUtils.getLowerBit(field)) {
        newFields.add(
            Nodes.BVEXTRACT(lo - 1, FortressUtils.getLowerBit(field), variable));
      }

      if (isHiInField && hi < FortressUtils.getUpperBit(field)) {
        newFields.add(
            Nodes.BVEXTRACT(FortressUtils.getUpperBit(field), hi + 1, variable));
      }

      if (!isLoInField && !isHiInField) {
        newFields.add(field);
      }
    }

    fields = newFields;
  }

  public void excludeAll() {
    fields = Collections.emptyList();
  }

  public List<Node> getFields() {
    return Collections.unmodifiableList(fields);
  }

  @Override
  public String toString() {
    return String.format("%s(%d), available fields: %s",
        variable, variable.getType().getSize(), fields);
  }
}
