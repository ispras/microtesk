/*
 * Copyright 2013-2016 ISP RAS (http://www.ispras.ru)
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
import ru.ispras.fortress.util.InvariantChecks;

import ru.ispras.microtesk.model.api.ArgumentMode;
import ru.ispras.microtesk.model.api.data.Type;
import ru.ispras.microtesk.model.api.instruction.Immediate;
import ru.ispras.microtesk.utils.SharedObject;

public final class Argument {
  public static enum Kind {
    IMM         (BigInteger.class, true),
    IMM_RANDOM  (RandomValue.class, true),
    IMM_UNKNOWN (UnknownImmediateValue.class, true),
    IMM_LAZY    (LazyValue.class, true),
    LABEL       (LabelValue.class, true),
    MODE        (Primitive.class, false),
    OP          (Primitive.class, false);

    private final Class<?> vc;
    private final boolean isImmediate;

    private Kind(final Class<?> valueClass, final boolean isImmediate) {
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

    protected void checkClass(final Class<?> c) {
      if (!vc.isAssignableFrom(c)) {
        throw new IllegalArgumentException(String.format(
            "%s is illegal value class, %s is expected.", c.getSimpleName(),vc.getSimpleName()));
      }
    }

    private final boolean isImmediate() {
      return isImmediate;
    }
  }

  private final String name;
  private final Kind kind;
  private final Object value;
  private final ArgumentMode mode;
  private final Type type;

  protected Argument(
      final String name,
      final Kind kind,
      final Object value,
      final ArgumentMode mode,
      final Type type) {
    InvariantChecks.checkNotNull(name);
    InvariantChecks.checkNotNull(kind);
    InvariantChecks.checkNotNull(value);
    InvariantChecks.checkNotNull(mode);

    kind.checkClass(value.getClass());

    this.name = name;
    this.kind = kind;
    this.value = value;
    this.mode = mode;
    this.type = type;
  }

  protected Argument(final Argument other) {
    InvariantChecks.checkNotNull(other);

    this.name = other.name;
    this.kind = other.kind;
    this.mode = other.mode;
    this.type = other.type;

    if (other.value instanceof SharedObject) {
      this.value = ((SharedObject<?>) other.value).getCopy();
    } else {
      InvariantChecks.checkTrue(other.value instanceof BigInteger);
      this.value = other.value;
    }
  }

  public boolean isImmediate() {
    return kind.isImmediate();
  }

  public BigInteger getImmediateValue() {
    if (!isImmediate()) {
      throw new UnsupportedOperationException(String.format(
          "%s(%s) is not an immediate argument.", name, value.getClass().getSimpleName()));
    }

    if (value instanceof BigInteger) {
      return (BigInteger) value;
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
    return isImmediate() ?
        Immediate.TYPE_NAME : ((Primitive) value).getTypeName();
  }

  public ArgumentMode getMode() {
    return mode;
  }

  public Type getType() {
    return type;
  }

  @Override
  public String toString() {
    return String.format(
        "Argument [name=%s, kind=%s, value=%s, mode=%s, type=%s]",
        name,
        kind,
        value,
        mode,
        type
        );
  }
}
