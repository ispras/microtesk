/*
 * Copyright 2013-2015 ISP RAS (http://www.ispras.ru)
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

import static ru.ispras.fortress.util.InvariantChecks.checkNotNull; 

import ru.ispras.microtesk.model.api.instruction.AddressingModeImm;

public final class Argument {
  public static enum Kind {
    IMM (Integer.class, true),
    IMM_RANDOM (RandomValue.class, true),
    IMM_UNKNOWN (UnknownImmediateValue.class, true),
    IMM_LAZY (LazyValue.class, true),
    MODE (Primitive.class, false),
    OP (Primitive.class, false);

    private static final String ILLEGAL_CLASS = "%s is illegal value class, %s is expected.";

    private final Class<?> vc;
    private final boolean isImmediate;

    private Kind(Class<?> valueClass, boolean isImmediate) {
      if (isImmediate) {
        if (!(Number.class.isAssignableFrom(valueClass) 
           || Value.class.isAssignableFrom(valueClass))) {
          throw new IllegalArgumentException(valueClass.getSimpleName() +
              " must implement Value or Number to be used to store immediate values.");
        }
      }

      this.vc = valueClass;
      this.isImmediate = isImmediate;
    }

    private void checkClass(Class<?> c) {
      if (!vc.isAssignableFrom(c)) {
        throw new IllegalArgumentException(String.format(
          ILLEGAL_CLASS, c.getSimpleName(),vc.getSimpleName()));
      }
    }

    private final boolean isImmediate() {
      return isImmediate;
    }
  }

  private final String name;
  private final Kind kind;
  private final Object value;

  Argument(String name, Kind kind, Object value) {
    checkNotNull(name);
    checkNotNull(kind);
    checkNotNull(value);

    kind.checkClass(value.getClass());

    this.name = name;
    this.kind = kind;
    this.value = value;
  }

  public boolean isImmediate() {
    return kind.isImmediate();
  }

  public int getImmediateValue() {
    if (!isImmediate()) {
      throw new UnsupportedOperationException(String.format(
          "%s(%s) is not an immediate argument.", name, value.getClass().getSimpleName()));
    }

    if (value instanceof Number) {
      return ((Number) value).intValue();
    }

    return ((Value) value).getValue();
  }

  public String getName() {
    return name;
  }

  public Kind getKind() {
    return kind;
  }

  public Object getValue() {
    return value;
  }

  public String getTypeName() {
    return isImmediate() ? AddressingModeImm.NAME : ((Primitive) value).getTypeName();
  }
}
