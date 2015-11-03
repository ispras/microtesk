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

package ru.ispras.microtesk.mmu.translator.ir;

import ru.ispras.fortress.data.DataType;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.util.InvariantChecks;

public final class Constant {
  private final String id;
  private final Node expression;
  private final Node variable;

  public Constant(final String id, final Node expression) {
    InvariantChecks.checkNotNull(id);
    InvariantChecks.checkNotNull(expression);

    this.id = id; 
    this.expression = expression;

    if (!isValue()) {
      final DataType dataType = expression.getDataType();
      final Variable variable = new Variable(id, new Type(dataType.getSize()));
      this.variable = variable.getNode();
    } else {
      this.variable = null;
    }
  }

  public String getId() {
    return id;
  }

  public Node getExpression() {
    return expression;
  }

  public Node getVariable() {
    return variable;
  }

  public boolean isValue() {
    return expression.getKind() == Node.Kind.VALUE;
  }

  @Override
  public String toString() {
    return String.format(
        "%s: %s = %s",
        id,
        expression.getDataType(),
        expression
        );
  }
}
