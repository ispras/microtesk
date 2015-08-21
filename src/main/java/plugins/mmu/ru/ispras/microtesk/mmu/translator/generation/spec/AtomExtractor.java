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
import java.util.ArrayList;
import java.util.List;

import ru.ispras.fortress.data.DataTypeId;
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

public final class AtomExtractor {
  private AtomExtractor() {}

  public static Atom extract(final Node expr) {
    checkNotNull(expr);

    switch(expr.getKind()) {
      case VALUE:
        return extract((NodeValue) expr);

      case VARIABLE:
        return extract((NodeVariable) expr);

      case OPERATION:
        return extract((NodeOperation) expr);

      default:
        throw new IllegalArgumentException("Unsupported node kind: " + expr.getKind());
    }
  }

  private static Atom extract(final NodeValue node) {
    final BigInteger value;
    if (node.isType(DataTypeId.LOGIC_INTEGER)) {
      value = node.getInteger();
    } else {
      value = node.getBitVector().bigIntegerValue();
    }
    return Atom.newValue(value);
  }

  private static Atom extract(final NodeVariable expr) {
    final Object userData = expr.getUserData();
    if (userData instanceof Variable) {
      return extractFromVariable((Variable) userData);
    } else if (userData instanceof AttributeRef) {
      return extractFromAttributeRef((AttributeRef) userData);
    } else {
      throw new IllegalArgumentException("Illegal user data attribute: " + userData);
    }
  }

  private static Atom extract(NodeOperation expr) {
    final Enum<?> op = expr.getOperationId();
    if (StandardOperation.BVEXTRACT == op) {
      return extractFromBitField(expr);
    } else if (StandardOperation.BVCONCAT == op) {
      return extractFromBitConcat(expr);
    } else {
      throw new IllegalArgumentException(String.format("Unsupported operator %s in %s", op, expr));
    }
  }

  private static Atom extractFromVariable(final Variable var) {
    if (var.isStruct()) {
      return Atom.newGroup(var);
    } else {
      return Atom.newVariable(new IntegerVariable(var.getName(), var.getBitSize()));
    }
  }

  private static Atom extractFromAttributeRef(final AttributeRef attrRef) {
    final String attrName = attrRef.getAttribute().getId();

    if (attrName.equals(AbstractStorage.READ_ATTR_NAME) || 
        attrName.equals(AbstractStorage.WRITE_ATTR_NAME)) {
      final Variable output = attrRef.getTarget().getDataArg();
      return Atom.newGroup(output);
    }

    // TODO: Handle hit
    throw new UnsupportedOperationException("Cannot parse: " + attrRef);
  }

  private static Atom extractFromBitField(final NodeOperation expr) {
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

  private static Atom extractFromBitConcat(NodeOperation expr) {
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
