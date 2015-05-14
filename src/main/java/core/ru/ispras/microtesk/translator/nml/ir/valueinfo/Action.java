/*
 * Copyright 2014 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.translator.nml.ir.valueinfo;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.translator.nml.ir.expression.Operands;

abstract class Action {
  private final Class<?> type;
  private final int operands;

  public Action(Class<?> type, Operands operands) {
    InvariantChecks.checkNotNull(type);
    InvariantChecks.checkNotNull(operands);

    this.type = type;
    this.operands = operands.count();
  }

  public final Class<?> getType() {
    return type;
  }

  public final int getOperands() {
    return operands;
  }
}


abstract class UnaryAction extends Action {
  public UnaryAction(Class<?> type) {
    super(type, Operands.UNARY);
  }

  public abstract Object calculate(Object value);
}


abstract class BinaryAction extends Action {
  public BinaryAction(Class<?> type) {
    super(type, Operands.BINARY);
  }

  public abstract Object calculate(Object left, Object right);
}
