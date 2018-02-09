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

import ru.ispras.fortress.data.DataType;
import ru.ispras.fortress.data.DataTypeId;
import ru.ispras.fortress.data.Variable;
import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeOperation;
import ru.ispras.fortress.expression.NodeValue;
import ru.ispras.fortress.expression.NodeVariable;
import ru.ispras.fortress.expression.StandardOperation;
import ru.ispras.fortress.util.InvariantChecks;

import ru.ispras.microtesk.mmu.translator.ir.AbstractStorage;
import ru.ispras.microtesk.mmu.translator.ir.AttributeRef;
import ru.ispras.microtesk.mmu.translator.ir.Constant;
import ru.ispras.microtesk.mmu.translator.ir.Var;

import java.math.BigInteger;

public final class AtomExtractor {
  private AtomExtractor() {}

  public static Atom extract(final Node node) {
    InvariantChecks.checkNotNull(node);

    final ExprTransformer transformer = new ExprTransformer();
    final Node expr = transformer.transform(node);

    switch (expr.getKind()) {
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

  static Atom extract(final NodeValue node) {
    final BigInteger value;

    if (node.isType(DataTypeId.LOGIC_INTEGER)) {
      value = node.getInteger();
    } else if (node.isType(DataTypeId.LOGIC_BOOLEAN)) {
      value = BitVector.valueOf(node.getBoolean()).bigIntegerValue(false);
    } else {
      value = node.getBitVector().bigIntegerValue(false);
    }

    return Atom.newValue(value);
  }

  static Atom extract(final NodeVariable expr) {
    final Object userData = expr.getUserData();
    if (userData instanceof Var) {
      return extractFromVariable((Var) userData);
    } else if (userData instanceof AttributeRef) {
      return extractFromAttributeRef((AttributeRef) userData);
    } else if (userData instanceof Constant) {
      if (expr.isType(DataTypeId.BIT_VECTOR)) {
        return Atom.newVariable(
            new Variable(expr.getName(), expr.getDataType()));
      } else if (expr.isType(DataTypeId.LOGIC_INTEGER)) {
        return Atom.newVariable(
            new Variable(expr.getName(), /* arbitrary positive value */ DataType.bitVector(64)));
      } else {
        throw new IllegalArgumentException("Illegal variable type: " + expr.getDataType());
      }
    } else {
      throw new IllegalArgumentException("Illegal user data attribute: " + userData);
    }
  }

  static Atom extract(final NodeOperation expr) {
    final Enum<?> op = expr.getOperationId();
    if (StandardOperation.BVEXTRACT == op) {
      return Atom.newField(expr);
    } else if (StandardOperation.BVCONCAT == op) {
      return Atom.newConcat(expr);
    } else {
      throw new IllegalArgumentException(String.format("Unsupported operator %s in %s", op, expr));
    }
  }

  static Atom extractFromVariable(final Var var) {
    if (var.isStruct()) {
      return Atom.newGroup(var);
    } else {
      return Atom.newVariable(new Variable(var.getName(), DataType.bitVector(var.getBitSize())));
    }
  }

  static Atom extractFromAttributeRef(final AttributeRef attrRef) {
    final String attrName = attrRef.getAttribute().getId();

    if (attrName.equals(AbstractStorage.READ_ATTR_NAME)
        || attrName.equals(AbstractStorage.WRITE_ATTR_NAME)) {
      final Var output = attrRef.getTarget().getDataArg();
      return Atom.newGroup(output);
    }

    // TODO: Handle hit
    throw new UnsupportedOperationException("Cannot parse: " + attrRef);
  }
}
