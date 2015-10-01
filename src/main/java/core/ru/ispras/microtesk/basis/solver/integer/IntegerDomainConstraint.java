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
import java.util.Collections;
import java.util.LinkedHashSet;
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

  private final IntegerFormula<V> formula;

  public IntegerDomainConstraint(
      final Type type,
      final V variable,
      final Set<BigInteger> domain,
      final Set<BigInteger> values) {
    InvariantChecks.checkNotNull(type);
    InvariantChecks.checkNotNull(variable);
    InvariantChecks.checkNotNull(values);
    InvariantChecks.checkNotEmpty(values);
    // Parameter {@code domain} can be null.

    this.type = type;
    this.variable = variable;
    this.values = values;

    final boolean inverse = (domain != null) && (domain.size() < 2 * values.size());

    final Type effectiveType;
    final Set<BigInteger> effectiveValues;

    if (inverse) {
      effectiveType = type == Type.RETAIN ? Type.EXCLUDE : Type.RETAIN;
      effectiveValues = new LinkedHashSet<>(domain);
      effectiveValues.removeAll(values);
    } else {
      effectiveType = type;
      effectiveValues = values;
    }

    // Construct the constraint formula.
    this.formula = new IntegerFormula<>();

    final IntegerClause<V> clause = new IntegerClause<>(
        effectiveType == Type.RETAIN ? IntegerClause.Type.OR : IntegerClause.Type.AND);

    for (final BigInteger value : effectiveValues) {
      clause.addEquation(variable, value, effectiveType == Type.RETAIN);
    }

    this.formula.addClause(clause);
  }

  public IntegerDomainConstraint(
      final V variable,
      final Set<BigInteger> domain,
      final Set<BigInteger> values) {
    this(Type.RETAIN, variable, domain, values);
  }

  public IntegerDomainConstraint(
      final V variable,
      final BigInteger value) {
    this(variable, null, Collections.singleton(value));
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
    return formula;
  }

  @Override
  public String toString() {
    return formula.toString();
  }
}
