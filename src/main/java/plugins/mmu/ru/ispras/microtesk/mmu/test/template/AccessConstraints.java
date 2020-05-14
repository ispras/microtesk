/*
 * Copyright 2015-2018 ISP RAS (http://www.ispras.ru)
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

import ru.ispras.castle.util.Logger;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeValue;
import ru.ispras.fortress.expression.Nodes;
import ru.ispras.fortress.util.InvariantChecks;

import ru.ispras.microtesk.basis.solver.bitvector.Restriction;
import ru.ispras.microtesk.settings.RegionSettings;
import ru.ispras.microtesk.utils.BigIntegerUtils;
import ru.ispras.microtesk.utils.FortressUtils;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * The {@link AccessConstraints} class holds constraints related to memory accesses.
 *
 * <p>
 * There are two categories of constraints: (1) constraints on variable values and
 * (2) constraints on memory access events. Each is stored in a separate collection.
 * </p>
 *
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */

public final class AccessConstraints {
  public static final AccessConstraints EMPTY = new AccessConstraints();

  public static final AccessConstraints compose(
      final Collection<AccessConstraints> collection) {
    InvariantChecks.checkNotNull(collection);

    final Builder builder = new Builder();

    for (final AccessConstraints constraints : collection) {
      builder.addConstraints(constraints);
    }

    return builder.build();
  }

  public static final class Builder {
    private RegionSettings region;

    private final List<VariableConstraint> variableConstraints;
    private final List<BufferEventConstraint> bufferEventConstraints;

    public Builder() {
      this.variableConstraints = new ArrayList<>();
      this.bufferEventConstraints = new ArrayList<>();
    }

    public void setRegion(final RegionSettings region) {
      this.region = region;
    }

    public void addConstraint(final VariableConstraint constrant) {
      InvariantChecks.checkNotNull(constrant);
      variableConstraints.add(constrant);
    }

    public void addConstraint(final BufferEventConstraint constraint) {
      InvariantChecks.checkNotNull(constraint);
      bufferEventConstraints.add(constraint);
    }

    public void addConstraints(final AccessConstraints constraints) {
      InvariantChecks.checkNotNull(constraints);

      this.variableConstraints.addAll(constraints.getVariableConstraints());
      this.bufferEventConstraints.addAll(constraints.getBufferEventConstraints());
    }

    public AccessConstraints build() {
      return new AccessConstraints(region, variableConstraints, bufferEventConstraints);
    }
  }

  private final RegionSettings region;
  private final Collection<VariableConstraint> variableConstraints;
  private final Collection<BufferEventConstraint> bufferEventConstraints;

  public AccessConstraints() {
    this.region = null;
    this.variableConstraints = Collections.<VariableConstraint>emptyList();
    this.bufferEventConstraints = Collections.<BufferEventConstraint>emptyList();
  }

  public AccessConstraints(
      final RegionSettings region,
      final Collection<VariableConstraint> variableConstraints,
      final Collection<BufferEventConstraint> bufferEventConstraints) {
    // The region parameter can be null.
    InvariantChecks.checkNotNull(variableConstraints);
    InvariantChecks.checkNotNull(bufferEventConstraints);

    this.region = region;
    this.variableConstraints = Collections.unmodifiableCollection(variableConstraints);
    this.bufferEventConstraints = Collections.unmodifiableCollection(bufferEventConstraints);
  }

  public static AccessConstraints merge(
      final AccessConstraints lhs,
      final AccessConstraints rhs) {
    InvariantChecks.checkNotNull(lhs);
    InvariantChecks.checkNotNull(rhs);

    if (lhs.isEmpty()) {
      return rhs;
    }

    if (rhs.isEmpty()) {
      return lhs;
    }

    final RegionSettings region = (lhs.region != null ? lhs.region : rhs.region);

    final Collection<VariableConstraint> variableConstraints = new ArrayList<>();
    final Collection<BufferEventConstraint> bufferEventConstraints = new ArrayList<>();

    variableConstraints.addAll(lhs.variableConstraints);
    variableConstraints.addAll(rhs.variableConstraints);

    bufferEventConstraints.addAll(lhs.bufferEventConstraints);
    bufferEventConstraints.addAll(rhs.bufferEventConstraints);

    return new AccessConstraints(region, variableConstraints, bufferEventConstraints);
  }

  public boolean isEmpty() {
    return variableConstraints.isEmpty() && bufferEventConstraints.isEmpty();
  }

  public RegionSettings getRegion() {
    return region;
  }

  public Collection<VariableConstraint> getVariableConstraints() {
    return variableConstraints;
  }

  public Collection<BufferEventConstraint> getBufferEventConstraints() {
    return bufferEventConstraints;
  }

  private Collection<Node> generalConstraints = null;
  private Collection<Node> variateConstraints = null;

  public Collection<Node> getGeneralConstraints() {
    if (generalConstraints != null) {
      return generalConstraints;
    }

    generalConstraints = new ArrayList<>();

    for (final VariableConstraint variableConstraint : variableConstraints) {
      final Node variable = variableConstraint.getVariable();
      final Set<BigInteger> values = variableConstraint.getValues();

      if ((1 << FortressUtils.getBitSize(variable)) != values.size()) {
        generalConstraints.add(
            Restriction.domain(
                variable,
                BigIntegerUtils.toBvSet(values, FortressUtils.getBitSize(variable)))
        );
      }
    }

    Logger.debug("General constraints: %s", generalConstraints);
    return generalConstraints;
  }

  public Collection<Node> getVariateConstraints() {
    if (variateConstraints != null) {
      return variateConstraints;
    }

    variateConstraints = new ArrayList<>();

    for (final VariableConstraint variableConstraint : variableConstraints) {
      final Node variable = variableConstraint.getVariable();
      final BigInteger value = variableConstraint.getVariate().getValue();

      variateConstraints.add(
          Nodes.eq(
              variable,
              NodeValue.newBitVector(value, FortressUtils.getBitSize(variable))
          )
      );
    }

    Logger.debug("Variate constraints: %s", variateConstraints);
    return variateConstraints;
  }

  public void randomize() {
    variateConstraints = null;
  }

  @Override
  public String toString() {
    return String.format("MemoryAccessConstraints [%s, %s, %s]",
        region,
        variableConstraints,
        bufferEventConstraints);
  }
}
