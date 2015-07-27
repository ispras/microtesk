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

package ru.ispras.microtesk.mmu.translator.ir.spec.builder;

import static ru.ispras.fortress.util.InvariantChecks.checkNotNull;
import static ru.ispras.microtesk.mmu.translator.ir.spec.builder.ScopeStorage.dotConc;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeOperation;
import ru.ispras.fortress.expression.NodeValue;
import ru.ispras.fortress.expression.NodeVariable;
import ru.ispras.fortress.expression.StandardOperation;
import ru.ispras.microtesk.basis.solver.integer.IntegerField;
import ru.ispras.microtesk.basis.solver.integer.IntegerVariable;
import ru.ispras.microtesk.mmu.translator.ir.AbstractStorage;
import ru.ispras.microtesk.mmu.translator.ir.AttributeRef;
import ru.ispras.microtesk.mmu.translator.ir.Variable;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuExpression;

final class AtomExtractor {
  private final String prefix;
  private final IntegerVariableTracker variables;

  AtomExtractor(String prefix, IntegerVariableTracker variables) {
    checkNotNull(prefix);
    checkNotNull(variables);

    this.prefix = prefix;
    this.variables = variables;
  }

  public Atom extract(Node expr) {
    checkNotNull(expr);
    switch(expr.getKind()) {
      case VALUE:
        return Atom.newValue(((NodeValue) expr).getInteger());

      case VARIABLE:
        return extract((NodeVariable) expr);

      case OPERATION:
        return extract((NodeOperation) expr);

      default:
        throw new IllegalArgumentException("Unsupported node kind: " + expr.getKind());
    }
  }

  private Atom extract(NodeVariable expr) {
    final Object userData = expr.getUserData();
    if (userData instanceof Variable) {
      return extractFromVariable((Variable) userData);
    } else if (userData instanceof AttributeRef) {
      return extractFromAttributeRef((AttributeRef) userData);
    } else {
      throw new IllegalArgumentException("Illegal user data attribute: " + userData);
    }
  }

  private Atom extract(NodeOperation expr) {
    final Enum<?> op = expr.getOperationId();
    if (StandardOperation.BVEXTRACT == op) {
      return extractFromBitField(expr);
    } else if (StandardOperation.BVCONCAT == op) {
      return extractFromBitConcat(expr);
    } else {
      throw new IllegalArgumentException(String.format("Unsupported operator %s in %s", op, expr));
    }
  }

  private Atom extractFromVariable(final Variable var) {
    final Variable source;
    if (variables.isGlobal(var)) {
      source = var;
    } else {
      source = variables.getOrCreate(dotConc(prefix, var.getName()), var);
    }
    if (source.isStruct()) {
      return Atom.newGroup(source);
    }
    return Atom.newVariable(variables.get(source));
  }

  private Atom extractFromAttributeRef(AttributeRef attrRef) {
    final String groupName = attrRef.getTarget().getId();
    final String attrName = attrRef.getAttribute().getId();

    if (attrName.equals(AbstractStorage.READ_ATTR_NAME) || 
        attrName.equals(AbstractStorage.WRITE_ATTR_NAME)) {
      final Variable output = attrRef.getTarget().getDataArg();
      if (variables.isGlobal(output)) {
        return Atom.newGroup(output);
      }
      final Variable group =
          variables.getOrCreate(dotConc(prefix, output.getName()), output);

      return Atom.newGroup(group);
    }

    // TODO: Handle hit
    throw new UnsupportedOperationException("Cannot parse: " + attrRef);
  }

  private Atom extractFromBitField(NodeOperation expr) {
    if (expr.getOperandCount() != 3) {
      throw new IllegalStateException("Wrong operand count for " + expr);
    }

    final Atom lo = extract(expr.getOperand(0));
    if (lo.getKind() != Atom.Kind.VALUE) {
      throw new IllegalStateException("Low bound is not a constant value: " + expr);
    }

    final Atom hi = extract(expr.getOperand(1));
    if (hi.getKind() != Atom.Kind.VALUE) {
      throw new IllegalStateException("Hi bound is not a constant value: " + expr);
    }

    final Atom var = extract(expr.getOperand(2));
    if (var.getKind() != Atom.Kind.VARIABLE) {
      throw new IllegalStateException("Source is not a single variable: " + expr);
    }

    final int intLo = ((BigInteger) lo.getObject()).intValue();
    final int intHi = ((BigInteger) hi.getObject()).intValue();
    final IntegerVariable intVar = (IntegerVariable) var.getObject();

    final IntegerField field = new IntegerField(intVar, intLo, intHi);
    return Atom.newField(field);
  }

  private Atom extractFromBitConcat(NodeOperation expr) {
    final List<IntegerField> concat = new ArrayList<>();

    for (Node operand : expr.getOperands()) {
      final Atom atom = extract(operand);
      switch (atom.getKind()) {
        case VARIABLE: {
          final IntegerVariable intVar = (IntegerVariable) atom.getObject();
          concat.add(new IntegerField(intVar));
          break;
        }

        case FIELD: {
          final IntegerField intField = (IntegerField) atom.getObject();
          concat.add(intField);
          break;
        }

        default:
          throw new IllegalStateException(
              operand + " cannot be used in a concatenation expression.");
      }
    }

    return Atom.newConcat(MmuExpression.rcat(concat));
  }
}
