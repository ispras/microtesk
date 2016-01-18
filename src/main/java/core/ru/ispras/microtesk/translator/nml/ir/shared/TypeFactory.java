/*
 * Copyright 2012-2016 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.translator.nml.ir.shared;

import ru.ispras.fortress.data.DataTypeId;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeValue;
import ru.ispras.microtesk.model.api.data.floatx.Precision;
import ru.ispras.microtesk.translator.antlrex.SemanticException;
import ru.ispras.microtesk.translator.antlrex.symbols.Where;
import ru.ispras.microtesk.translator.nml.antlrex.WalkerContext;
import ru.ispras.microtesk.translator.nml.antlrex.WalkerFactoryBase;
import ru.ispras.microtesk.translator.nml.ir.expression.Expr;

public final class TypeFactory extends WalkerFactoryBase {
  public TypeFactory(final WalkerContext context) {
    super(context);
  }

  public Type newAlias(final Where where, final String name) throws SemanticException {
    final Type ref = getIR().getTypes().get(name);
    if (null == ref) {
      raiseError(where, String.format("Undefined type: %s.", name));
    }

    return ref.alias(name);
  }

  public Type newInt(final Where where, final Expr bitSize) throws SemanticException {
    return Type.INT(getIntegerValue(where, bitSize));
  }

  public Type newCard(final Where where, final Expr bitSize) throws SemanticException {
    return Type.CARD(getIntegerValue(where, bitSize));
  }

  public Type newFloat(
      final Where where,
      final Expr fractionBitSize,
      final Expr exponentBitSize) throws SemanticException {

    final int fracBitSize = getIntegerValue(where, fractionBitSize);
    final int expBitSize = getIntegerValue(where, exponentBitSize);

    final Precision precision = Precision.find(fracBitSize, expBitSize);
    if (null == precision) {
      raiseError(where, String.format(
          "Unsupported floating-point format: float(%d, %d)",
          fracBitSize,
          expBitSize
          ));
    }

    return Type.FLOAT(fracBitSize, expBitSize);
  }

  private int getIntegerValue(final Where where, final Expr expr) throws SemanticException {
    final Node node = expr.getNode();
    if (node.getKind() != Node.Kind.VALUE || !node.isType(DataTypeId.LOGIC_INTEGER)) {
      raiseError(where, "Statically calculated integer expression is expected.");
    }

    return ((NodeValue) expr.getNode()).getInteger().intValue();
  }
}
