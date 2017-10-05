/*
 * Copyright 2017 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.basis.solver.bitvector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.sat4j.core.Vec;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;

import ru.ispras.fortress.util.InvariantChecks;

/**
 * {@link Sat4jFormula} represents a SAT4J formula, which is a set of clauses.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class Sat4jFormula {
  /**
   * {@link Builder} is a {@link Sat4jFormula} builder.
   */
  public static final class Builder {
    /** Collection of vectors of clauses. */
    private final Collection<IVec<IVecInt>> clauses = new ArrayList<>();

    public Builder() {}

    public Builder(final Builder rhs) {
      clauses.addAll(rhs.clauses);
    }

    public final void addClause(final IVecInt clause) {
      InvariantChecks.checkNotNull(clause);

      final IVec<IVecInt> vector = new Vec<>(new IVecInt[] { clause });
      this.clauses.add(vector);
    }

    public final void addAllClauses(final IVec<IVecInt> clauses) {
      InvariantChecks.checkNotNull(clauses);
      this.clauses.add(clauses);
    }

    public final void addAllClauses(final Collection<IVec<IVecInt>> clauses) {
      InvariantChecks.checkNotNull(clauses);
      this.clauses.addAll(clauses);
    }

    public Sat4jFormula build() {
      return new Sat4jFormula(clauses);
    }
  }

  /** Collection of vectors of clauses. */
  private final Collection<IVec<IVecInt>> clauses;

  public Sat4jFormula(final Collection<IVec<IVecInt>> clauses) {
    InvariantChecks.checkNotNull(clauses);
    this.clauses = Collections.unmodifiableCollection(clauses);
  }

  public Sat4jFormula(final Sat4jFormula rhs) {
    this(rhs.clauses);
  }

  public boolean isEmpty() {
    return clauses.isEmpty();
  }

  public int size() {
    return clauses.size();
  }

  public Collection<IVec<IVecInt>> getClauses() {
    return clauses;
  }

  @Override
  public boolean equals(final Object o) {
    if (o == this) {
      return true;
    }

    if (o == null || !(o instanceof Sat4jFormula)) {
      return false;
    }

    final Sat4jFormula r = (Sat4jFormula) o;

    return clauses.equals(r.clauses);
  }

  @Override
  public int hashCode() {
    return clauses.hashCode();
  }

  @Override
  public String toString() {
    final StringBuilder builder = new StringBuilder();

    boolean delimiter = false;
    for (final IVec<IVecInt> vector : clauses) {
      for (int i = 0; i < vector.size(); i++) {
        final IVecInt clause = vector.get(i);

        if (delimiter) {
          builder.append(" & ");
        }

        delimiter = true;

        builder.append("(");
        builder.append(clause);
        builder.append(")");
      }
    }

    return builder.toString();
  }
}
