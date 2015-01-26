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

package ru.ispras.microtesk.translator.mmu.ir;

import static ru.ispras.fortress.util.InvariantChecks.checkNotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public final class Format {
  public static final Format EMPTY = new Format(Collections.<String, Field>emptyMap());

  private final Map<String, Field> fields;

  Format(Map<String, Field> fields) {
    checkNotNull(fields);
    this.fields = fields;
  }

  public Collection<Field> getFields() {
    return Collections.unmodifiableCollection(fields.values());
  }

  public Field getField(String name) {
    checkNotNull(name);
    return fields.get(name);
  }
}
