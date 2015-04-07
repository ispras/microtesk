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

package ru.ispras.microtesk.translator.mmu.spec.builder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import ru.ispras.fortress.util.InvariantChecks;

/**
 * This class is to track fields of a data structure by exclusion.
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */

public final class FieldTracker {
  private final int bitSize;
  private List<Field> fields;

  public FieldTracker(int bitSize) {
    InvariantChecks.checkGreaterThanZero(bitSize);

    this.bitSize = bitSize;
    this.fields = new ArrayList<>();
    this.fields.add(new Field(0, bitSize - 1));
  }

  public void exclude(int min, int max) {
    InvariantChecks.checkBounds(min, bitSize);
    InvariantChecks.checkBounds(max, bitSize);

    if (fields.isEmpty()) {
      return;
    }

    final List<Field> newFields = new ArrayList<>();
    for (Field field : fields) {
      if (field.inField(min) && min > 0) {
        newFields.add(new Field(field.min, min-1));
      }

      if (field.inField(max) && max < field.max) {
        newFields.add(new Field(max+1, field.max));
      }

      if (!field.inField(min) && !field.inField(max)) {
        newFields.add(field);
      }
    }

    fields = newFields;
  }

  public void excludeAll() {
    fields = Collections.emptyList();
  }

  public List<Field> getFields() {
    return Collections.unmodifiableList(fields);
  }

  @Override
  public String toString() {
    return String.format("From <0..%d> are left: %s", bitSize - 1, fields);
  }
}
