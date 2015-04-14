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

import ru.ispras.fortress.expression.Node;
import ru.ispras.microtesk.translator.mmu.spec.MmuExpression;
import ru.ispras.microtesk.translator.mmu.spec.basis.IntegerField;
import ru.ispras.microtesk.translator.mmu.spec.basis.IntegerVariable;

public final class AtomConverter {
  public static class Atom {
    public final AtomKind kind;
    public final Object object;

    private Atom(AtomKind kind, Object object) {
      this.kind = kind;
      this.object = object;
    }
  }

  public static enum AtomKind {
    VALUE    (BigInteger.class),
    VARIABLE (IntegerVariable.class),
    FIELD    (IntegerField.class),
    CONCAT   (MmuExpression.class);

    private final Class<?> objectClass;
    private AtomKind(Class<?> objectClass) {
      this.objectClass = objectClass;
    }

    public Class<?> getObjectClass() {
      return objectClass;
    }
  }

  private final VariableTracker variables;

  private AtomConverter(VariableTracker variables) {
    checkNotNull(variables);
    this.variables = variables;
  }

  public Atom convert(Node expr) {
    checkNotNull(expr);
    return null;
  }
 }
