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

import java.math.BigInteger;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.utils.SharedObject;

final class OperatorValueBinary extends SharedObject<OperatorValueBinary> implements Value {
  public interface Operator {
    String getText();
    BigInteger apply(final BigInteger operand1, final BigInteger operand2);
  }

  private final Operator operator;
  private final Value operand1;
  private final Value operand2;

  protected OperatorValueBinary(
      final Operator operator,
      final Value operand1,
      final Value operand2) {
    InvariantChecks.checkNotNull(operator);
    InvariantChecks.checkNotNull(operand1);
    InvariantChecks.checkNotNull(operand2);

    this.operator = operator;
    this.operand1 = operand1;
    this.operand2 = operand2;
  }

  private OperatorValueBinary(final OperatorValueBinary other) {
    super(other);

    this.operator = other.operator;

    this.operand1 = other.operand1 instanceof SharedObject
        ? (Value)((SharedObject<?>) other.operand1).getCopy()
        : other.operand1.copy();

    this.operand2 = other.operand2 instanceof SharedObject
        ? (Value)((SharedObject<?>) other.operand2).getCopy()
        : other.operand2.copy();
  }

  @Override
  public OperatorValueBinary newCopy() {
    return new OperatorValueBinary(this);
  }

  @Override
  public Value copy() {
    return newCopy();
  }

  @Override
  public BigInteger getValue() {
    return operator.apply(operand1.getValue(), operand2.getValue());
  }

  @Override
  public String toString() {
    return String.format("%s %s %s", operand1.toString(), operator.getText(), operand2.toString());
  }
}
