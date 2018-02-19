/*
 * Copyright 2013-2018 ISP RAS (http://www.ispras.ru)
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

import ru.ispras.fortress.data.DataType;
import ru.ispras.fortress.expression.NodeValue;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.translator.nml.ir.expr.Expr;
import ru.ispras.microtesk.translator.nml.ir.expr.NodeInfo;

public final class LetConstant {
  public static final LetConstant FLOAT_ROUNDING_MODE =
      new LetConstant("float_rounding_mode", DataType.INTEGER);

  public static final LetConstant FLOAT_EXCEPTION_FLAGS =
      new LetConstant("float_exception_flags", DataType.INTEGER);

  private final String name;
  private final Expr expr;

  LetConstant(final String name, final Expr expr) {
    InvariantChecks.checkNotNull(name);
    InvariantChecks.checkNotNull(expr);

    this.name = name;
    this.expr = expr;
  }

  private LetConstant(final String name, final DataType type) {
    this(name, new Expr(NodeValue.newString(name)));
    expr.setNodeInfo(NodeInfo.newConst(null));
  }

  public String getName() {
    return name;
  }

  public Expr getExpr() {
    return expr;
  }

  @Override
  public String toString() {
    return String.format(
        "LetConstant [name=%s, value=%s]", name, expr.getNode());
  }
}
