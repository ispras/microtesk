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
import ru.ispras.microtesk.test.GenerationAbortedException;

import java.math.BigInteger;

public final class OperatorValueFactory {
  private OperatorValueFactory() {}

  private enum BinaryOperator implements OperatorValueBinary.Operator {
    AND("&") {
      @Override
      public BigInteger apply(final BigInteger operand1, final BigInteger operand2) {
        return operand1.and(operand2);
      }
    },

    OR("|") {
      @Override
      public BigInteger apply(final BigInteger operand1, final BigInteger operand2) {
        return operand1.or(operand2);
      }
    },

    XOR("^") {
      @Override
      public BigInteger apply(final BigInteger operand1, final BigInteger operand2) {
        return operand1.xor(operand2);
      }
    },

    ADD("+") {
      @Override
      public BigInteger apply(final BigInteger operand1, final BigInteger operand2) {
        return operand1.add(operand2);
      }
    },

    SUB("-") {
      @Override
      public BigInteger apply(final BigInteger operand1, final BigInteger operand2) {
        return operand1.subtract(operand2);
      }
    };

    private final String text;

    BinaryOperator(final String text) {
      this.text = text;
    }

    @Override
    public String getText() {
      return text;
    }
  }

  private enum UnaryOperator implements OperatorValueUnary.Operator {
    PLUS("+") {
      @Override
      public BigInteger apply(final BigInteger operand) {
        return operand;
      }
    },

    MINUS("-") {
      @Override
      public BigInteger apply(final BigInteger operand) {
        return operand.negate();
      }
    },

    NOT("~") {
      @Override
      public BigInteger apply(final BigInteger operand) {
        return operand.not();
      }
    };

    private final String text;

    UnaryOperator(final String text) {
      this.text = text;
    }

    @Override
    public String getText() {
      return text;
    }
  }

  public static Value newBinaryOperator(
      final String operatorId,
      final Value operand1,
      final Value operand2) {
    InvariantChecks.checkNotNull(operatorId);
    InvariantChecks.checkNotNull(operand1);
    InvariantChecks.checkNotNull(operand2);

    final BinaryOperator operator = BinaryOperator.valueOf(operatorId);
    checkOperatorNotNull(operator, operatorId);
    return new OperatorValueBinary(operator, operand1, operand2);
  }

  public static Value newBinaryOperator(
      final String operatorId,
      final BigInteger operand1,
      final Value operand2) {
    InvariantChecks.checkNotNull(operand1);
    return newBinaryOperator(operatorId, new FixedValue(operand1), operand2);
  }

  public static Value newBinaryOperator(
      final String operatorId,
      final Value operand1,
      final BigInteger operand2) {
    InvariantChecks.checkNotNull(operand1);
    return newBinaryOperator(operatorId, operand1, new FixedValue(operand2));
  }

  public static Value newUnaryOperator(
      final String operatorId,
      final Value operand) {
    InvariantChecks.checkNotNull(operatorId);
    InvariantChecks.checkNotNull(operand);

    final UnaryOperator operator = UnaryOperator.valueOf(operatorId);
    checkOperatorNotNull(operator, operatorId);
    return new OperatorValueUnary(operator, operand);
  }

  private static void checkOperatorNotNull(final Object operator, final String operatorId) {
    if (null == operator) {
      throw new GenerationAbortedException(
          String.format("The %s operator is not supported.", operatorId));
    }
  }
}
