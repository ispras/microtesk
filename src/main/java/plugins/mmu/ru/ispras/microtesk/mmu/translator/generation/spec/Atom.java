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

package ru.ispras.microtesk.mmu.translator.generation.spec;

import static ru.ispras.fortress.util.InvariantChecks.checkNotNull;

import java.math.BigInteger;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.basis.solver.integer.IntegerField;
import ru.ispras.microtesk.basis.solver.integer.IntegerVariable;
import ru.ispras.microtesk.mmu.translator.ir.Variable;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuExpression;

public final class Atom {
  public static enum Kind {
    VALUE    (BigInteger.class, false),
    VARIABLE (IntegerVariable.class, false),
    FIELD    (IntegerField.class, false),
    GROUP    (Variable.class, true),
    CONCAT   (MmuExpression.class, false);

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

  public static Atom newVariable(final IntegerVariable variable) {
    return new Atom(Kind.VARIABLE, variable);
  }

  public static Atom newGroup(final Variable group) {
    return new Atom(Kind.GROUP, group);
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

  public int getWidth() {
    switch (kind) {
      case VARIABLE:
        return ((IntegerVariable) object).getWidth();
      case GROUP:
        return ((Variable) object).getBitSize();
      case FIELD:
        return ((IntegerField) object).getWidth();
      case CONCAT:
        return ((MmuExpression) object).getWidth();
      default:
        InvariantChecks.checkTrue(false);
        return -1;
    }
  }

  @Override
  public String toString() {
    return String.format("%s: %s", kind, object);
  }

  public String getText() {
    switch (kind) {
      case VALUE:
        return Utils.toString((BigInteger) object);

      case VARIABLE:
        return ((IntegerVariable) object).getName().replace('.', '_');

      case FIELD:
        return String.format("%s.field(%d, %d)", 
            ((IntegerField) object).getVariable().getName().replace('.', '_'),
            ((IntegerField) object).getLoIndex(), ((IntegerField) object).getHiIndex());

      case GROUP:
        return ((Variable) object).getName();

      case CONCAT:
        return "MmuExpression.XXX";

      default:
        throw new IllegalStateException(kind.name());
    }
  }
}
