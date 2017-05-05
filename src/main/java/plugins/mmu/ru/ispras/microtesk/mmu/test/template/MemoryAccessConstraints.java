/*
 * Copyright 2015-2017 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.mmu.test.template;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.basis.solver.integer.IntegerConstraint;
import ru.ispras.microtesk.basis.solver.integer.IntegerDomainConstraint;
import ru.ispras.microtesk.basis.solver.integer.IntegerEqualConstraint;
import ru.ispras.microtesk.basis.solver.integer.IntegerField;
import ru.ispras.microtesk.basis.solver.integer.IntegerVariable;

/**
 * The {@link MemoryAccessConstraints} class holds constraints related to memory
 * accesses. There are two categories of constraints: (1) constraints on variable
 * values and (2) constraints on memory access events. Each is stored in a separate
 * collection.
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */

public final class MemoryAccessConstraints {
  public static final MemoryAccessConstraints EMPTY = new MemoryAccessConstraints();

  public static final MemoryAccessConstraints compose(
      final Collection<MemoryAccessConstraints> collection) {
    InvariantChecks.checkNotNull(collection);

    final Builder builder = new Builder();

    for (final MemoryAccessConstraints constraints : collection) {
      builder.addConstraints(constraints);
    }

    return builder.build();
  }

  public static final class Builder {
    private final List<VariableConstraint> variableConstraints;
    private final List<BufferEventConstraint> bufferEventConstraints;

    public Builder() {
      this.variableConstraints = new ArrayList<>();
      this.bufferEventConstraints = new ArrayList<>(); 
    }

    public void addConstraint(final VariableConstraint constrant) {
      InvariantChecks.checkNotNull(constrant);
      variableConstraints.add(constrant);
    }

    public void addConstraint(final BufferEventConstraint constraint) {
      InvariantChecks.checkNotNull(constraint);
      bufferEventConstraints.add(constraint);
    }

    public void addConstraints(final MemoryAccessConstraints constraints) {
      InvariantChecks.checkNotNull(constraints);

      this.variableConstraints.addAll(constraints.getVariableConstraints());
      this.bufferEventConstraints.addAll(constraints.getBufferEventConstraints());
    }

    public MemoryAccessConstraints build() {
      return new MemoryAccessConstraints(variableConstraints, bufferEventConstraints);
    }
  }

  private final Collection<VariableConstraint> variableConstraints;
  private final Collection<BufferEventConstraint> bufferEventConstraints;

  public MemoryAccessConstraints() {
    this.variableConstraints = Collections.<VariableConstraint>emptyList();
    this.bufferEventConstraints = Collections.<BufferEventConstraint>emptyList();
  }

  public MemoryAccessConstraints(
      final Collection<VariableConstraint> variableConstraints,
      final Collection<BufferEventConstraint> bufferEventConstraints) {
    InvariantChecks.checkNotNull(variableConstraints);
    InvariantChecks.checkNotNull(bufferEventConstraints);

    this.variableConstraints = Collections.unmodifiableCollection(variableConstraints);
    this.bufferEventConstraints = Collections.unmodifiableCollection(bufferEventConstraints);
  }

  public static MemoryAccessConstraints merge(
      final MemoryAccessConstraints lhs,
      final MemoryAccessConstraints rhs) {
    InvariantChecks.checkNotNull(lhs);
    InvariantChecks.checkNotNull(rhs);

    if (lhs.isEmpty()) {
      return rhs;
    }

    if (rhs.isEmpty()) {
      return lhs;
    }

    final Collection<VariableConstraint> variableConstraints = new ArrayList<>();
    final Collection<BufferEventConstraint> bufferEventConstraints = new ArrayList<>();

    variableConstraints.addAll(lhs.variableConstraints);
    variableConstraints.addAll(rhs.variableConstraints);

    bufferEventConstraints.addAll(lhs.bufferEventConstraints);
    bufferEventConstraints.addAll(rhs.bufferEventConstraints);

    return new MemoryAccessConstraints(variableConstraints, bufferEventConstraints);
  }

  public boolean isEmpty() {
    return variableConstraints.isEmpty() && bufferEventConstraints.isEmpty();
  }

  public Collection<VariableConstraint> getVariableConstraints() {
    return variableConstraints;
  }

  public Collection<BufferEventConstraint> getBufferEventConstraints() {
    return bufferEventConstraints;
  }

  private Collection<IntegerConstraint<IntegerField>> generalConstraints = null;
  private Collection<IntegerConstraint<IntegerField>> variateConstraints = null;

  public Collection<IntegerConstraint<IntegerField>> getGeneralConstraints() {
    if (generalConstraints != null) {
      return generalConstraints;
    }

    generalConstraints = new ArrayList<>();

    for (final VariableConstraint variableConstraint : variableConstraints) {
      final IntegerVariable variable = variableConstraint.getVariable();
      final Set<BigInteger> values = variableConstraint.getValues();

      if ((1 << variable.getWidth()) != values.size()) {
        generalConstraints.add(new IntegerDomainConstraint<IntegerField>(variable.field(), values));
      }
    }

    Logger.debug("General constraints: %s", generalConstraints);
    return generalConstraints;
  }

  public Collection<IntegerConstraint<IntegerField>> getVariateConstraints() {
    if (variateConstraints != null) {
      return variateConstraints;
    }

    variateConstraints = new ArrayList<>();

    for (final VariableConstraint variableConstraint : variableConstraints) {
      final IntegerVariable variable = variableConstraint.getVariable();
      final BigInteger value = variableConstraint.getVariate().getValue();

      variateConstraints.add(new IntegerEqualConstraint<IntegerField>(variable.field(), value));
    }

    Logger.debug("Variate constraints: %s", variateConstraints);
    return variateConstraints;
  }

  public void randomize() {
    variateConstraints = null;
  }

  @Override
  public String toString() {
    return String.format("MemoryAccessConstraints [integers=%s, buffer_events=%s]",
        variableConstraints,
        bufferEventConstraints);
  }
}
