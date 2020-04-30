/*
 * Copyright 2017-2020 ISP RAS (http://www.ispras.ru)
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

import org.sat4j.core.VecInt;
import org.sat4j.specs.IVecInt;

import ru.ispras.fortress.util.InvariantChecks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

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
    private final Collection<IntArray> clauses = new ArrayList<>();

    public Builder() {}

    public Builder(final Builder rhs) {
      clauses.addAll(rhs.clauses);
    }

    public final void add(final IntArray clause) {
      InvariantChecks.checkNotNull(clause);
      this.clauses.add(clause);
    }

    public final void addAll(final Collection<IntArray> clauses) {
      InvariantChecks.checkNotNull(clauses);
      this.clauses.addAll(clauses);
    }

    public Sat4jFormula build() {
      final Collection<IVecInt> sat4jClauses = new ArrayList<>(clauses.size());

      for (final IntArray clause : clauses) {
        final VecInt sat4jClause = new VecInt(clause.toArray());

        sat4jClause.shrinkTo(clause.length());
        sat4jClauses.add(sat4jClause);
      }
      return new Sat4jFormula(sat4jClauses);
    }
  }

  /** Collection of clauses. */
  private final Collection<IVecInt> clauses;

  public Sat4jFormula(final Collection<IVecInt> clauses) {
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

  public Collection<IVecInt> getClauses() {
    return clauses;
  }

  @Override
  public String toString() {
    final StringBuilder builder = new StringBuilder();

    boolean delimiter = false;
    for (final IVecInt clause : clauses) {
      if (delimiter) {
        builder.append(" & ");
      }

      delimiter = true;

      builder.append("(");
      builder.append(clause);
      builder.append(")");
    }

    return builder.toString();
  }
}
