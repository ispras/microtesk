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

package ru.ispras.microtesk.basis.solver.integer;

import java.math.BigInteger;
import java.util.Set;

import ru.ispras.fortress.util.InvariantChecks;

/**
 * {@link IntegerDomainConstraint} class represents a simple constraint.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class IntegerDomainConstraint<V> implements IntegerConstraint<V> {
  /**
   * {@link Type} contains domain constraint types.
   */
  public static enum Type {
    RETAIN,
    EXCLUDE
  }

  private final Type type;
  private final V variable;
  private final Set<BigInteger> values;

  public IntegerDomainConstraint(
      final Type type,
      final V variable,
      final Set<BigInteger> values) {
    InvariantChecks.checkNotNull(type);
    InvariantChecks.checkNotNull(variable);
    InvariantChecks.checkNotNull(values);

    this.type = type;
    this.variable = variable;
    this.values = values;
  }

  public Type getType() {
    return type;
  }

  public V getVariable() {
    return variable;
  }

  public Set<BigInteger> getValues() {
    return values;
  }

  @Override
  public IntegerFormula<V> getFormula() {
    final IntegerFormula<V> formula = new IntegerFormula<>();

    final IntegerClause<V> clause = new IntegerClause<>(
        type == Type.RETAIN ? IntegerClause.Type.OR : IntegerClause.Type.AND);

    for (final BigInteger value : values) {
      clause.addEquation(variable, value, type == Type.RETAIN);
    }

    formula.addClause(clause);

    return formula;
  }
}
