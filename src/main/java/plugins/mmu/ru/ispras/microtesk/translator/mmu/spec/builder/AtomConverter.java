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
import java.util.Map;

import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeOperation;
import ru.ispras.fortress.expression.NodeValue;
import ru.ispras.fortress.expression.NodeVariable;
import ru.ispras.fortress.expression.StandardOperation;
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

    @Override
    public String toString() {
      return String.format("%s: %s", kind, object);
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

  AtomConverter(VariableTracker variables) {
    checkNotNull(variables);
    this.variables = variables;
  }

  public Atom convert(Node expr) {
    checkNotNull(expr);

    switch(expr.getKind()) {
      case VALUE:
        return processValue((NodeValue) expr);

      case VARIABLE:
        return processVariable((NodeVariable) expr);

      case OPERATION:
        return processOperation((NodeOperation) expr);

      default:
        throw new IllegalArgumentException("Unsupported node kind: " + expr.getKind());
    }
  }

  private Atom processOperation(NodeOperation expr) {
    final Enum<?> operator = expr.getOperationId();
    if (StandardOperation.BVEXTRACT != operator && StandardOperation.BVCONCAT != operator) {
      throw new IllegalArgumentException("Unsupported operator: " + operator);
    }

    if (operator == StandardOperation.BVEXTRACT) {
      
    }

    // TODO Auto-generated method stub
    return null;
  }

  private Atom processValue(NodeValue value) {
    return new Atom(AtomKind.VALUE, value.getInteger());
  }

  private Atom processVariable(NodeVariable expr) {
    final VariableTracker.Status status = variables.checkDefined(expr.getName());
    if (VariableTracker.Status.UNDEFINED == status) {
      throw new IllegalArgumentException("Undefined variable: " + expr);
    }

    if (VariableTracker.Status.VARIABLE == status) {
      final IntegerVariable variable = variables.getVariable(expr.getName());
      return new Atom(AtomKind.VARIABLE, variable);
    }

    final Map<String, IntegerVariable> group = variables.getGroup(expr.getName());
    final MmuExpression concat = MmuExpression.RCATX(group.values());

    return new Atom(AtomKind.CONCAT, concat);
  }
 }
