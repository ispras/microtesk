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

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.basis.solver.integer.IntegerField;
import ru.ispras.microtesk.basis.solver.integer.IntegerRange;
import ru.ispras.microtesk.basis.solver.integer.IntegerVariable;
import ru.ispras.microtesk.utils.function.Function;

/**
 * {@link MmuCalculator} implements an expression calculator.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class MmuCalculator {
  private MmuCalculator() {}

  /**
   * Evaluates the expression.
   * 
   * <p>The empty expression is evaluated to zero.</p>
   * 
   * @param expr the expression to be calculated.
   * @param getValue the function that returns a variable's value.
   * @param check the flag indicating whether to fail if no value is found for some variable. 
   * @return the expression's value.
   */
  public static BigInteger eval(
      final MmuExpression expr,
      final Function<IntegerVariable, BigInteger> getValue,
      final boolean check) {
    InvariantChecks.checkNotNull(expr);
    InvariantChecks.checkNotNull(getValue);

    BigInteger result = BigInteger.ZERO;
    int offset = 0;

    for (final IntegerField field : expr.getTerms()) {
      final IntegerVariable variable = field.getVariable();
      final BigInteger value = variable.isDefined() ? variable.getValue() : getValue.apply(variable);

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

  public static Boolean eval(
      final MmuConditionAtom atom,
      final Function<IntegerVariable, BigInteger> getValue) {
    InvariantChecks.checkNotNull(atom);
    InvariantChecks.checkNotNull(getValue);

    switch (atom.getType()) {
      case EQ_EXPR_CONST:
      case IN_EXPR_RANGE:
        final MmuExpression expr = atom.getLhsExpr();
        final BigInteger value = eval(expr, getValue, false);

        if (value == null) {
          return null;
        }

        final IntegerRange range = atom.getRhsRange();
        return range.contains(value) != atom.isNegated();

      default:
        return null;
    }
  }

  public static Boolean eval(
      final MmuCondition cond,
      final Function<IntegerVariable, BigInteger> getValue) {
    InvariantChecks.checkNotNull(cond);
    InvariantChecks.checkNotNull(getValue);

    final boolean initialResultValue = (cond.getType() == MmuCondition.Type.AND);
    boolean result = initialResultValue;

    for (final MmuConditionAtom atom : cond.getAtoms()) {
      final Boolean value = eval(atom, getValue);

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
