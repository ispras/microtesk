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

import java.util.List;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.basis.solver.IntegerField;
import ru.ispras.microtesk.basis.solver.IntegerVariable;

/**
 * {@link MmuAssignment} describes an assignment, i.e. a pair {@code lhs = rhs}, where {@code lhs}
 * is an integer field and {@code rhs} is an expression.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class MmuAssignment {
  /** The field. */
  private final IntegerField field;
  /** The expression. */
  private final MmuExpression expression;

  /**
   * Constructs an assignment.
   * 
   * @param field the field.
   * @param expression the expression or {@code null}.
   * @throws IllegalArgumentException if {@code field} is null.
   */
  public MmuAssignment(final IntegerField field, final MmuExpression expression) {
    InvariantChecks.checkNotNull(field);

    this.field = field;
    this.expression = expression;
  }

  /**
   * Constructs an assignment.
   * 
   * @param variable the left-hand side (LHS).
   * @param expression the right-hand side (RHS).
   * @throws IllegalArgumentException if {@code variable} is null.
   */
  public MmuAssignment(final IntegerVariable variable, final MmuExpression expression) {
    this(new IntegerField(variable), expression);
  }

  /**
   * Constructs an assignment with no right-hand side (RHS).
   * 
   * <p>It is assumed that RHS can be derived from the context.</p>
   * 
   * @param field the left-hand side (LHS).
   */
  public MmuAssignment(final IntegerField field) {
    this(field, null);
  }

  /**
   * Constructs an assignment with no right-hand side (RHS).
   * 
   * It is assumed that RHS can be derived from the context.
   * 
   * @param variable the variable the right-hand side (RHS).
   */
  public MmuAssignment(final IntegerVariable variable) {
    this(new IntegerField(variable), null);
  }

  /**
   * Returns the left-hand side (LHS) of the assignment.
   * 
   * @return LHS.
   */
  public IntegerField getField() {
    return field;
  }

  /**
   * Returns the right-hand side (RHS) of the assignment.
   * 
   * @return RHS.
   */
  public MmuExpression getExpression() {
    return expression;
  }

  @Override
  public String toString() {
    final StringBuilder string = new StringBuilder(field.getVariable().getName());

    string.append("[");
    string.append(field.getLoIndex());
    string.append(":");
    string.append(field.getHiIndex());
    string.append("]: ");

    if (expression == null) {
      string.append("{null}");
    } else {
      final List<IntegerField> terms = expression.getTerms();
      for (final IntegerField term : terms) {
        string.append("{");
        string.append(term);
        string.append("}");
      }
    }

    return string.toString();
  }
}
