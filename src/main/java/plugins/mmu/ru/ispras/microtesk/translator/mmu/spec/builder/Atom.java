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

import static ru.ispras.fortress.util.InvariantChecks.checkNotNull;

import java.math.BigInteger;

import ru.ispras.microtesk.translator.mmu.spec.MmuExpression;
import ru.ispras.microtesk.translator.mmu.spec.basis.IntegerField;
import ru.ispras.microtesk.translator.mmu.spec.basis.IntegerVariable;

public final class Atom {
  public static enum Kind {
    VALUE    (BigInteger.class),
    VARIABLE (IntegerVariable.class),
    FIELD    (IntegerField.class),
    CONCAT   (MmuExpression.class);

    private final Class<?> objectClass;
    private Kind(final Class<?> objectClass) {
      this.objectClass = objectClass;
    }

    public Class<?> getObjectClass() {
      return objectClass;
    }
  }

  public static Atom newValue(final BigInteger value) {
    return new Atom(Kind.VALUE, value);
  }

  public static Atom newVariable(final IntegerVariable variable) {
    return new Atom(Kind.VARIABLE, variable);
  }

  public static Atom newField(final IntegerField field) {
    return new Atom(Kind.FIELD, field);
  }

  public static Atom newConcat(final MmuExpression concat) {
    return new Atom(Kind.CONCAT, concat);
  }

  private final Kind kind;
  private final Object object;

  public Atom(final Kind kind, final Object object) {
    checkNotNull(kind);
    checkNotNull(object);

    if (!object.getClass().equals(kind.getObjectClass())) {
      throw new IllegalArgumentException(
          kind.getObjectClass().getSimpleName() + " is expected.");
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
