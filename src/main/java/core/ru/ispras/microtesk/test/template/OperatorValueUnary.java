/*
 * Copyright 2018 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.test.template;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.utils.SharedObject;

import java.math.BigInteger;

final class OperatorValueUnary extends SharedObject<OperatorValueUnary> implements Value {
  public interface Operator {
    String getText();

    BigInteger apply(final BigInteger operand);
  }

  private final Operator operator;
  private final Value operand;

  protected OperatorValueUnary(final Operator operator, final Value operand) {
    InvariantChecks.checkNotNull(operator);
    InvariantChecks.checkNotNull(operand);

    this.operator = operator;
    this.operand = operand;
  }

  private OperatorValueUnary(final OperatorValueUnary other) {
    super(other);

    this.operator = other.operator;
    this.operand = other.operand instanceof SharedObject
        ? (Value)((SharedObject<?>) other.operand).getCopy()
        : other.operand.copy();
  }

  @Override
  public OperatorValueUnary newCopy() {
    return new OperatorValueUnary(this);
  }

  @Override
  public Value copy() {
    return newCopy();
  }

  @Override
  public BigInteger getValue() {
    return operator.apply(operand.getValue());
  }

  @Override
  public String toString() {
    return String.format("%s %s", operator.getText(), operand.toString());
  }
}
