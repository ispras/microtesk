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

import java.util.EnumSet;
import java.util.Map;

import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeOperation;
import ru.ispras.fortress.expression.NodeValue;
import ru.ispras.fortress.expression.NodeVariable;
import ru.ispras.fortress.expression.StandardOperation;

import ru.ispras.microtesk.translator.mmu.ir.AttributeRef;
import ru.ispras.microtesk.translator.mmu.ir.FieldRef;
import ru.ispras.microtesk.translator.mmu.ir.Variable;
import ru.ispras.microtesk.translator.mmu.spec.MmuExpression;
import ru.ispras.microtesk.translator.mmu.spec.basis.IntegerField;
import ru.ispras.microtesk.translator.mmu.spec.basis.IntegerVariable;

public final class AtomExtractor {
  private final VariableTracker variables;

  AtomExtractor(VariableTracker variables) {
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
      final Variable variable = (Variable) userData;

      
    } else if (userData instanceof FieldRef) {
      
    } else if (userData instanceof AttributeRef) {
      
    } else {
      // Unexpected! Error!
    }

    final VariableTracker.Status status = variables.checkDefined(expr.getName());
    if (VariableTracker.Status.UNDEFINED == status) {
      throw new IllegalArgumentException("Undefined variable: " + expr);
    }

    if (VariableTracker.Status.VARIABLE == status) {
      final IntegerVariable variable = variables.getVariable(expr.getName());
      return Atom.newVariable(variable);
    }

    final Map<String, IntegerVariable> group = variables.getGroup(expr.getName());
    final MmuExpression concat = MmuExpression.RCATX(group.values());

    return Atom.newConcat(concat);
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
