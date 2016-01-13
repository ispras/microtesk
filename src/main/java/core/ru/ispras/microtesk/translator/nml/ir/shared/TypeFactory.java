/*
 * Copyright 2012-2014 ISP RAS (http://www.ispras.ru)
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

import ru.ispras.microtesk.model.api.data.floatx.Precision;
import ru.ispras.microtesk.translator.antlrex.SemanticException;
import ru.ispras.microtesk.translator.antlrex.Where;
import ru.ispras.microtesk.translator.nml.antlrex.WalkerContext;
import ru.ispras.microtesk.translator.nml.antlrex.WalkerFactoryBase;
import ru.ispras.microtesk.translator.nml.ir.expression.Expr;

public final class TypeFactory extends WalkerFactoryBase {
  public TypeFactory(final WalkerContext context) {
    super(context);
  }

  public Type newAlias(
      final Where where,
      final String name) throws SemanticException {
    final Type ref = getIR().getTypes().get(name);
    if (null == ref) {
      raiseError(where, String.format("Undefined type: %s.", name));
    }

    return ref.alias(name);
  }

  public Type newInt(
      final Where where,
      final Expr bitSize) throws SemanticException {
    return Type.INT(bitSize.integerValue());
  }

  public Type newCard(
      final Where where,
      final Expr bitSize) throws SemanticException {
    return Type.CARD(bitSize.integerValue());
  }

  public Type newFloat(
      final Where where,
      final Expr fractionBitSize,
      final Expr exponentBitSize) throws SemanticException {

    final int fractionSize = fractionBitSize.integerValue();
    final int exponentSize = exponentBitSize.integerValue();

    final Precision precision = Precision.find(fractionSize, exponentSize);
    if (null == precision) {
      raiseError(where, String.format(
          "Unsupported floating-point format: float(%d, %d)",
          fractionSize,
          exponentSize
          ));
    }

    return Type.FLOAT(fractionSize, exponentSize);
  }
}
