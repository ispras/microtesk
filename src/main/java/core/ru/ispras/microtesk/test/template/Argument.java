/*
 * Copyright 2013-2018 ISP RAS (http://www.ispras.ru)
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
import ru.ispras.microtesk.model.ArgumentMode;
import ru.ispras.microtesk.model.Immediate;
import ru.ispras.microtesk.model.data.Type;
import ru.ispras.microtesk.utils.SharedObject;

import java.math.BigInteger;

public final class Argument {
  public enum Kind {
    IMM         (FixedValue.class),
    IMM_RANDOM  (RandomValue.class),
    IMM_BINOP   (OperatorValueBinary.class),
    IMM_UNOP    (OperatorValueUnary.class),
    IMM_UNKNOWN (UnknownImmediateValue.class),
    IMM_LAZY    (LazyValue.class),
    LABEL       (LabelValue.class),
    MODE        (Primitive.class),
    OP          (Primitive.class);

    private final Class<?> vc;

    Kind(final Class<?> valueClass) {
      this.vc = valueClass;
    }

    protected void checkClass(final Class<?> c) {
      if (!vc.isAssignableFrom(c)) {
        throw new IllegalArgumentException(String.format(
            "%s is illegal value class, %s is expected.", c.getSimpleName(),vc.getSimpleName()));
      }
    }
  }

  private final String name;
  private final Kind kind;
  private final Object value;
  private final ArgumentMode mode;
  private final Type type;

  protected Argument(
      final String name,
      final Value value,
      final ArgumentMode mode,
      final Type type) {
    this(name, getKind(value), value, mode, type);
  }

  private static Kind getKind(final Value value) {
    InvariantChecks.checkNotNull(value);

    final Kind kind;
    if (value instanceof FixedValue) {
      kind = Kind.IMM;
    } else if (value instanceof RandomValue) {
      kind = Kind.IMM_RANDOM;
    } else if (value instanceof UnknownImmediateValue) {
      kind = Kind.IMM_UNKNOWN;
    } else if (value instanceof LazyValue) {
      kind = Kind.IMM_LAZY;
    } else if (value instanceof LabelValue) {
      kind = Kind.LABEL;
    } else if (value instanceof OperatorValueBinary) {
      kind = Kind.IMM_BINOP;
    } else if (value instanceof OperatorValueUnary) {
      kind = Kind.IMM_UNOP;
    } else {
      throw new IllegalArgumentException(
          "Unsupported value class: " + value.getClass().getSimpleName());
    }

    return kind;
  }

  protected Argument(
      final String name,
      final Primitive primitive,
      final ArgumentMode mode,
      final Type type) {
    this(name, getKind(primitive), primitive, mode, type);
  }

  private static Kind getKind(final Primitive primitive) {
    InvariantChecks.checkNotNull(primitive);

    final Kind kind;
    if (Primitive.Kind.MODE == primitive.getKind()) {
      kind = Kind.MODE;
    } else if (Primitive.Kind.OP == primitive.getKind()) {
      kind = Kind.OP;
    } else {
      throw new IllegalArgumentException("Unsupported primitive type: " + primitive.getKind());
    }

    return kind;
  }

  private Argument(
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
      InvariantChecks.checkTrue(other.value instanceof FixedValue);
      this.value = other.value;
    }
  }

  public boolean isImmediate() {
    return value instanceof Value;
  }

  public boolean isPrimitive() {
    return value instanceof Primitive;
  }

  public BigInteger getImmediateValue() {
    if (!isImmediate()) {
      throw new UnsupportedOperationException(String.format(
          "%s(%s) is not an immediate argument.", name, value.getClass().getSimpleName()));
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
    return isImmediate() ? Immediate.TYPE_NAME : ((Primitive) value).getTypeName();
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
