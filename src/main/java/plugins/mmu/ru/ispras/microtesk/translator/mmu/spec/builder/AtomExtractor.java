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

import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeOperation;
import ru.ispras.fortress.expression.NodeValue;
import ru.ispras.fortress.expression.NodeVariable;
import ru.ispras.fortress.expression.StandardOperation;

import ru.ispras.microtesk.translator.mmu.ir.AttributeRef;
import ru.ispras.microtesk.translator.mmu.ir.FieldRef;
import ru.ispras.microtesk.translator.mmu.ir.Variable;
import ru.ispras.microtesk.translator.mmu.spec.basis.IntegerField;
import ru.ispras.microtesk.translator.mmu.spec.basis.IntegerVariable;

final class AtomExtractor {
  private final IntegerVariableTracker variables;

  AtomExtractor(IntegerVariableTracker variables) {
    checkNotNull(variables);
    this.variables = variables;
  }

  public Atom extract(Node expr) {
    checkNotNull(expr);

    switch(expr.getKind()) {
      case VALUE:
        return Atom.newValue(((NodeValue) expr).getInteger());

      case VARIABLE:
        return processVariable((NodeVariable) expr);

      case OPERATION:
        return processOperation((NodeOperation) expr);

      default:
        throw new IllegalArgumentException("Unsupported node kind: " + expr.getKind());
    }
  }

  private Atom processVariable(NodeVariable expr) {
    final Object userData = expr.getUserData();
    if (userData instanceof Variable) {
      // TODO
    } else if (userData instanceof FieldRef) {
      // TODO
    } else if (userData instanceof AttributeRef) {
      // TODO
    } else {
      // Unexpected! Error!
    }

    final IntegerVariableTracker.Status status = variables.checkDefined(expr.getName());
    if (IntegerVariableTracker.Status.UNDEFINED == status) {
      throw new IllegalArgumentException("Undefined variable: " + expr);
    }

    if (IntegerVariableTracker.Status.VARIABLE == status) {
      final IntegerVariable variable = variables.getVariable(expr.getName());
      return Atom.newVariable(variable);
    }

    final IntegerVariableGroup group = variables.getGroup(expr.getName());
    return Atom.newGroup(group);
  }

  private Atom processOperation(NodeOperation expr) {
    final Enum<?> operator = expr.getOperationId();
    if (StandardOperation.BVEXTRACT != operator && 
        StandardOperation.BVCONCAT  != operator) {
      throw new IllegalArgumentException("Unsupported operator: " + operator);
    }

    if (operator == StandardOperation.BVEXTRACT) {
      final int lo = ((NodeValue) expr.getOperand(0)).getInteger().intValue();
      final int hi = ((NodeValue) expr.getOperand(1)).getInteger().intValue();
      final NodeVariable nodeVar = (NodeVariable) expr.getOperand(2);

      final IntegerVariable intVar = variables.getVariable(nodeVar.getName());
      if (null == intVar) {
        throw new IllegalArgumentException("Undefined variable: " + nodeVar);
      }

      final IntegerField field = new IntegerField(intVar, lo, hi);
      return Atom.newField(field);
    }

    // TODO: operator == StandardOperation.BVCONCAT
    return null;
  }


 }
