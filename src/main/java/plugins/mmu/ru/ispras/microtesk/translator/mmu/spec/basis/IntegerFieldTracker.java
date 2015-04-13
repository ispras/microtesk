/*
 * Copyright 2015 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.translator.mmu.spec.basis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import ru.ispras.fortress.util.InvariantChecks;

/**
 * This class is to track fields in a variable by exclusion.
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */

public final class IntegerFieldTracker {
  private final IntegerVariable variable;
  private List<IntegerField> fields;

  private static boolean inField(final IntegerField field, final int index) {
    return field.getLoIndex() <= index && index <= field.getHiIndex();
  }

  public IntegerFieldTracker(final IntegerVariable variable) {
    InvariantChecks.checkNotNull(variable);

    this.variable = variable;
    this.fields = Collections.singletonList(new IntegerField(variable));
  }

  public void exclude(final int lo, final int hi) {
    if (fields.isEmpty()) {
      return;
    }

    if (hi < lo) {
      exclude(hi, lo);
      return;
    }

    final List<IntegerField> newFields = new ArrayList<>();
    for (IntegerField field : fields) {
      final boolean isLoInField = inField(field, lo);
      final boolean isHiInField = inField(field, hi);

      if (isLoInField && lo > field.getLoIndex()) {
        newFields.add(new IntegerField(variable, field.getLoIndex(), lo - 1));
      }

      if (isHiInField && hi < field.getHiIndex()) {
        newFields.add(new IntegerField(variable, hi + 1, field.getHiIndex()));
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

  public List<IntegerField> getFields() {
    return Collections.unmodifiableList(fields);
  }

  @Override
  public String toString() {
    return String.format("%s(%d), available fields: %s",
        variable, variable.getWidth(), fields);
  }
}
