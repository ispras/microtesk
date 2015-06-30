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

package ru.ispras.microtesk.translator.mmu.ir.spec;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.test.sequence.solver.IntegerField;
import ru.ispras.microtesk.test.sequence.solver.IntegerVariable;

/**
 * {@link MmuCalculator} implements an expression calculator.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class MmuCalculator {

  /**
   * Evaluates the expression. The empty expression is evaluated to zero.
   * 
   * @param expression the expression to be calculated.
   * @param values the values of the variables.
   * @return the expression value.
   * @throws IllegalArgumentException if {@code expression} or {@code values} is null.
   */
  public static BigInteger eval(final MmuExpression expression,
      final Map<IntegerVariable, BigInteger> values) {
    InvariantChecks.checkNotNull(expression);
    InvariantChecks.checkNotNull(values);

    BigInteger result = BigInteger.ZERO;
    int offset = 0;

    for (final IntegerField field : expression.getTerms()) {
      final BigInteger value = values.get(field.getVariable());
      InvariantChecks.checkNotNull(value);

      final int fieldWidth = field.getWidth();
      final BigInteger fieldValue =
          value.shiftRight(field.getLoIndex()).mod(BigInteger.ONE.shiftLeft(fieldWidth));

      result = result.or(fieldValue.shiftLeft(offset));
      offset += fieldWidth;
    }

    return result;
  }

  /**
   * Evaluates the single-variable expression. The empty expression is evaluated to zero.
   * 
   * @param expression the expression to be calculated.
   * @param variable the variable.
   * @param value the value of the variable.
   * @return the expression value.
   * @throws IllegalArgumentException if some parameters are null.
   */
  public static BigInteger eval(final MmuExpression expression, final IntegerVariable variable,
      final BigInteger value) {
    InvariantChecks.checkNotNull(expression);
    InvariantChecks.checkNotNull(variable);
    InvariantChecks.checkNotNull(value);

    final Map<IntegerVariable, BigInteger> values = new HashMap<>();
    values.put(variable, value);

    return eval(expression, values);
  }
}
