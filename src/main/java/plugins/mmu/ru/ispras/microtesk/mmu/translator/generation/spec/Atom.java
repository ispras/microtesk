/*
 * Copyright 2015-2018 ISP RAS (http://www.ispras.ru)
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

import ru.ispras.fortress.data.Variable;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.mmu.translator.ir.Var;

import java.math.BigInteger;

public final class Atom {
  public static enum Kind {
    VALUE    (BigInteger.class, false),
    VARIABLE (Variable.class, false),
    FIELD    (Node.class, false),
    GROUP    (Var.class, true),
    CONCAT   (Node.class, false);

    private final Class<?> objectClass;
    private final boolean isStruct;

    private Kind(final Class<?> objectClass, final boolean isStruct) {
      this.objectClass = objectClass;
      this.isStruct = isStruct;
    }

    public Class<?> getObjectClass() {
      return objectClass;
    }

    public boolean isStruct() {
      return isStruct;
    }
  }

  public static Atom newValue(final BigInteger value) {
    return new Atom(Kind.VALUE, value);
  }

  public static Atom newVariable(final Variable variable) {
    return new Atom(Kind.VARIABLE, variable);
  }

  public static Atom newGroup(final Var group) {
    return new Atom(Kind.GROUP, group);
  }

  public static Atom newField(final Node extract) {
    return new Atom(Kind.FIELD, extract);
  }

  public static Atom newConcat(final Node concat) {
    return new Atom(Kind.CONCAT, concat);
  }

  private final Kind kind;
  private final Object object;

  public Atom(final Kind kind, final Object object) {
    InvariantChecks.checkNotNull(kind);
    InvariantChecks.checkNotNull(object);

    if (!kind.getObjectClass().isAssignableFrom(object.getClass())) {
      throw new IllegalArgumentException(
          String.format("%s is expected,%s is found",
              kind.getObjectClass().getName(), object.getClass().getName()));
    }

    this.kind = kind;
    this.object = object;
  }

  public Kind getKind() {
    return kind;
  }

  public Object getObject() {
    return object;
  }

  @Override
  public String toString() {
    return String.format("%s: %s", kind, object);
  }
}
