/*
 * Copyright 2016 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.basis.solver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import ru.ispras.fortress.util.InvariantChecks;

/**
 * {@link BiasedConstraints} represents a set of biased constraints.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class BiasedConstraints<T> {

  @SafeVarargs
  public static <C> BiasedConstraints<C> HARD(final C... constraints) {
    InvariantChecks.checkNotNull(constraints);

    final Builder<C> builder = new Builder<>();

    for (final C constraint : constraints) {
      builder.add(Bias.HARD, constraint);
    }

    return builder.build();
  }

  @SafeVarargs
  public static <C> BiasedConstraints<C> SOFT(final C... constraints) {
    InvariantChecks.checkNotNull(constraints);

    final Builder<C> builder = new Builder<>();

    for (final C constraint : constraints) {
      builder.add(Bias.SOFT, constraint);
    }

    return builder.build();
  }

  public static final class Builder<T> {
    private final SortedMap<Bias, Collection<T>> biasedConstraints = new TreeMap<>();

    public Builder() {}

    public void add(final Bias bias, final T constraint) {
      InvariantChecks.checkNotNull(bias);
      InvariantChecks.checkNotNull(constraint);

      Collection<T> constraints = biasedConstraints.get(bias);
      if (constraints == null) {
        biasedConstraints.put(bias, constraints = new ArrayList<>());
      }

      constraints.add(constraint);
    }

    public void add(final T constraint) {
      add(Bias.HARD, constraint);
    }

    public BiasedConstraints<T> build() {
      return new BiasedConstraints<>(biasedConstraints);
    }
  }

  public static final class Entry<T> {
    private final Bias bias;
    private final T constraint;

    public Entry(final Bias bias, final T constraint) {
      InvariantChecks.checkNotNull(bias);
      InvariantChecks.checkNotNull(constraint);

      this.bias = bias;
      this.constraint = constraint;
    }

    public Bias getBias() {
      return bias;
    }

    public T getConstraint() {
      return constraint;
    }
  }

  private final SortedMap<Bias, Collection<T>> biasedConstraints;

  public BiasedConstraints(final SortedMap<Bias, Collection<T>> biasedConstraints) {
    InvariantChecks.checkNotNull(biasedConstraints);
    this.biasedConstraints = biasedConstraints;
  }

  public boolean isEmpty() {
    return biasedConstraints.isEmpty();
  }

  public Collection<T> getHard() {
    final Collection<T> result = biasedConstraints.get(Bias.HARD);
    return result != null ? result : Collections.<T>emptyList();
  }

  public Collection<T> getSoft() {
    final Collection<T> result = new ArrayList<>();

    for (final Map.Entry<Bias, Collection<T>> entry : biasedConstraints.entrySet()) {
      final Bias bias = entry.getKey();

      if (!bias.isHard()) {
        result.addAll(entry.getValue());
      }
    }

    return result;
  }

  public Collection<T> getAll() {
    final Collection<T> result = new ArrayList<>();

    for (final Map.Entry<Bias, Collection<T>> entry : biasedConstraints.entrySet()) {
      result.addAll(entry.getValue());
    }

    return result;
  }

  public Collection<Entry<T>> getHistogram() {
    final Collection<Entry<T>> result = new ArrayList<>();

    for (final Map.Entry<Bias, Collection<T>> entry : biasedConstraints.entrySet()) {
      final Bias bias = entry.getKey();

      for (final T constraint : entry.getValue()) {
        result.add(new Entry<T>(bias, constraint));
      }
    }

    return result;
  }
}
