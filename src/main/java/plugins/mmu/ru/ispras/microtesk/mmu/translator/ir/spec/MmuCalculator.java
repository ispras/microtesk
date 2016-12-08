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

package ru.ispras.microtesk.mmu.translator.ir.spec;

import java.math.BigInteger;
import java.util.Collections;
import java.util.Map;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.basis.solver.integer.IntegerField;
import ru.ispras.microtesk.basis.solver.integer.IntegerRange;
import ru.ispras.microtesk.basis.solver.integer.IntegerVariable;

/**
 * {@link MmuCalculator} implements an expr calculator.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class MmuCalculator {
  private MmuCalculator() {}

  /**
   * Evaluates the expr.
   * 
   * <p>The empty expr is evaluated to zero.</p>
   * 
   * @param expr the expr to be calculated.
   * @param values the values of the variables.
   * @param check the flag indicating whether to fail if no value is found for some variable. 
   * @return the expr value.
   */
  public static BigInteger eval(
      final MmuExpression expr,
      final Map<IntegerVariable, BigInteger> values,
      final boolean check) {
    InvariantChecks.checkNotNull(expr);
    InvariantChecks.checkNotNull(values);

    BigInteger result = BigInteger.ZERO;
    int offset = 0;

    for (final IntegerField field : expr.getTerms()) {
      final IntegerVariable variable = field.getVariable();
      final BigInteger value = variable.isDefined() ? variable.getValue() : values.get(variable);

      if (value == null && !check) {
        return null;
      }

      InvariantChecks.checkNotNull(value);

      final int fieldWidth = field.getWidth();
      final BigInteger fieldValue =
          value.shiftRight(field.getLoIndex()).mod(BigInteger.ONE.shiftLeft(fieldWidth));

      result = result.or(fieldValue.shiftLeft(offset));
      offset += fieldWidth;
    }

    return result;
  }

  public static BigInteger eval(
      final MmuExpression expr,
      final IntegerVariable variable,
      final BigInteger value,
      final boolean check) {
    InvariantChecks.checkNotNull(expr);
    InvariantChecks.checkNotNull(variable);
    InvariantChecks.checkNotNull(value);

    return eval(
        expr, Collections.<IntegerVariable, BigInteger>singletonMap(variable, value), check);
  }

  public static BigInteger eval(
      final MmuExpression expr,
      final boolean check) {
    InvariantChecks.checkNotNull(expr);

    return eval(expr, Collections.<IntegerVariable, BigInteger>emptyMap(), check);
  }

  public static Boolean eval(final MmuConditionAtom atom) {
    InvariantChecks.checkNotNull(atom);

    switch (atom.getType()) {
      case EQ_EXPR_CONST:
      case IN_EXPR_RANGE:
        final MmuExpression expr = atom.getLhsExpr();
        final BigInteger value = eval(expr, false);

        if (value == null) {
          return null;
        }

        final IntegerRange range = atom.getRhsRange();
        return range.contains(value) != atom.isNegated();

      default:
        return null;
    }
  }

  public static Boolean eval(final MmuCondition cond) {
    InvariantChecks.checkNotNull(cond);

    final boolean initialResultValue = (cond.getType() == MmuCondition.Type.AND);
    boolean result = initialResultValue;

    for (final MmuConditionAtom atom : cond.getAtoms()) {
      final Boolean value = eval(atom);

      if (value == null) {
        return null;
      }

      result = (cond.getType() == MmuCondition.Type.AND ? (result & value) : (result | value));

      if (result != initialResultValue) {
        break;
      }
    }

    return result;
  }
}
