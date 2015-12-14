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

package ru.ispras.microtesk.mmu.basis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.basis.solver.integer.IntegerConstraint;
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

  public static final class Builder {
    private final List<IntegerConstraint<IntegerVariable>> integerConstraints;
    private final List<BufferEventConstraint> bufferEventConstraints;

    public Builder() {
      this.integerConstraints = new ArrayList<>();
      this.bufferEventConstraints = new ArrayList<>(); 
    }

    public void addConstraint(final IntegerConstraint<IntegerVariable> constrant) {
      InvariantChecks.checkNotNull(constrant);
      integerConstraints.add(constrant);
    }

    public void addConstraint(final BufferEventConstraint constraint) {
      InvariantChecks.checkNotNull(constraint);
      bufferEventConstraints.add(constraint);
    }

    public void addConstraints(final MemoryAccessConstraints constraints) {
      InvariantChecks.checkNotNull(constraints);
      this.integerConstraints.addAll(constraints.getIntegers());
      this.bufferEventConstraints.addAll(constraints.getBufferEvents());
    }

    public MemoryAccessConstraints build() {
      return new MemoryAccessConstraints(integerConstraints, bufferEventConstraints);
    }
  }

  private final List<IntegerConstraint<IntegerVariable>> integerConstraints;
  private final List<BufferEventConstraint> bufferEventConstraints;

  public MemoryAccessConstraints(
      final List<IntegerConstraint<IntegerVariable>> integerConstraints,
      final List<BufferEventConstraint> bufferEventConstraints) {
    InvariantChecks.checkNotNull(integerConstraints);
    InvariantChecks.checkNotNull(bufferEventConstraints);

    this.integerConstraints = Collections.unmodifiableList(integerConstraints);
    this.bufferEventConstraints = Collections.unmodifiableList(bufferEventConstraints);
  }

  public List<IntegerConstraint<IntegerVariable>> getIntegers() {
    return integerConstraints;
  }

  public List<BufferEventConstraint> getBufferEvents() {
    return bufferEventConstraints;
  }

  @Override
  public String toString() {
    return String.format(
        "MemoryAccessConstraints [integers=%s, buffer_events=%s]",
        integerConstraints,
        bufferEventConstraints
        );
  }
}
