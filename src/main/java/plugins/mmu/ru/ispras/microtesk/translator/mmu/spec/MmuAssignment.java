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

package ru.ispras.microtesk.translator.mmu.spec;

import java.util.List;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.translator.mmu.spec.basis.IntegerField;
import ru.ispras.microtesk.translator.mmu.spec.basis.IntegerVariable;

/**
 * This class describes an assignment.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public class MmuAssignment {
  /** The variable. */
  private final IntegerVariable variable;
  /** The expression. */
  private final MmuExpression expression;

  /**
   * Constructs an assignment.
   * 
   * @param variable the variable.
   * @param expression the expression.
   * @throws NullPointerException if {@code variable} is null.
   */
  public MmuAssignment(final IntegerVariable variable, final MmuExpression expression) {
    InvariantChecks.checkNotNull(variable);

    this.variable = variable;
    this.expression = expression;
  }

  /**
   * Constructs an assignment with no right-hand side (RHS). It is assumed that RHS can be derived
   * from context.
   * 
   * @param variable the variable.
   */
  public MmuAssignment(final IntegerVariable variable) {
    this(variable, null);
  }

  /**
   * Returns the variable of the assignment.
   * 
   * @return the variable.
   */
  public IntegerVariable getVariable() {
    return variable;
  }

  /**
   * Returns the expression of the assignment.
   * 
   * @return the expression.
   */
  public MmuExpression getExpression() {
    return expression;
  }

  @Override
  public String toString() {
    final StringBuilder string = new StringBuilder(variable.getName());
    string.append("[");
    string.append(variable.getWidth());
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
