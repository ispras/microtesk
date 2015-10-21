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

package ru.ispras.microtesk.mmu.test.template;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import ru.ispras.fortress.randomizer.Variate;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.basis.solver.integer.IntegerConstraint;
import ru.ispras.microtesk.basis.solver.integer.IntegerDomainConstraint;
import ru.ispras.microtesk.basis.solver.integer.IntegerVariable;
import ru.ispras.microtesk.mmu.MmuPlugin;
import ru.ispras.microtesk.mmu.basis.BufferAccessEvent;
import ru.ispras.microtesk.mmu.basis.BufferEventConstraint;
import ru.ispras.microtesk.mmu.basis.MemoryAccessConstraints;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuBuffer;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuSubsystem;
import ru.ispras.microtesk.test.GenerationAbortedException;

/**
 * The {@link ConstraintBuilder} class is used by test templates to
 * build memory-related constraints from Ruby code.
 * 
 * @author <a href="mailto:andrewt@ispras.ru">Andrei Tatarnikov</a>
 */

public final class ConstraintBuilder {
  private final MmuSubsystem spec;
  private final MemoryAccessConstraints.Builder builder;

  private ConstraintBuilder() {
    this.spec = MmuPlugin.getSpecification();
    this.builder = new MemoryAccessConstraints.Builder();
  }

  public MemoryAccessConstraints build() {
    return builder.build();
  }

  public void addEq(final String variableName, final BigInteger value) {
    InvariantChecks.checkNotNull(variableName);
    InvariantChecks.checkNotNull(value);

    final IntegerVariable variable = getVariable(variableName);
    final IntegerConstraint<IntegerVariable> constraint =
        new IntegerDomainConstraint<>(variable, value);

    builder.addConstraint(constraint);
  }

  public void addDomRange(
      final String variableName,
      final BigInteger min,
      final BigInteger max) {
    InvariantChecks.checkNotNull(variableName);
    InvariantChecks.checkNotNull(min);
    InvariantChecks.checkNotNull(max);
    InvariantChecks.checkGreaterOrEq(max, min);

    final Set<BigInteger> values = new LinkedHashSet<>();
    for (BigInteger value = min;
         value.compareTo(max) <= 0;
         value = value.add(BigInteger.ONE)) {
      values.add(value);
    }

    final IntegerVariable variable = getVariable(variableName);
    final IntegerConstraint<IntegerVariable> constraint =
        new IntegerDomainConstraint<>(variable, null, values);

    builder.addConstraint(constraint);
  }

  public void addDomCollection(
      final String variableName,
      final Collection<BigInteger> values) {
    InvariantChecks.checkNotNull(variableName);
    InvariantChecks.checkNotNull(values);

    final IntegerVariable variable = getVariable(variableName);
    final IntegerConstraint<IntegerVariable> constraint =
        new IntegerDomainConstraint<>(variable, null, new LinkedHashSet<>(values));

    builder.addConstraint(constraint);
  }

  public void addDistribution(
      final String variableName,
      final Variate<?> distribution) {
    InvariantChecks.checkNotNull(variableName);
    InvariantChecks.checkNotNull(distribution);

    final IntegerVariable variable = getVariable(variableName);
    final Set<BigInteger> values = extractValues(distribution);

    final IntegerConstraint<IntegerVariable> constraint =
        new IntegerDomainConstraint<>(variable, null, values);

    builder.addConstraint(constraint);
  }

  public void addHit(final String bufferName) {
    InvariantChecks.checkNotNull(bufferName);

    final MmuBuffer buffer = getBuffer(bufferName);
    final BufferEventConstraint constraint =
        new BufferEventConstraint(buffer, BufferAccessEvent.HIT);

    builder.addConstraint(constraint);
  }

  public void addMiss(final String bufferName) {
    InvariantChecks.checkNotNull(bufferName);

    final MmuBuffer buffer = getBuffer(bufferName);
    final BufferEventConstraint constraint =
        new BufferEventConstraint(buffer, BufferAccessEvent.MISS);

    builder.addConstraint(constraint);
  }

  public void addEvent(final String bufferName, final int hitBias, final int missBias) {
    InvariantChecks.checkNotNull(bufferName);

    final List<BufferAccessEvent> events = new ArrayList<>(2);
    if (0 != hitBias) {
      events.add(BufferAccessEvent.HIT);
    }

    if (0 != missBias) {
      events.add(BufferAccessEvent.MISS);
    }

    final Set<BufferAccessEvent> eventSet = events.isEmpty() ?
        EnumSet.noneOf(BufferAccessEvent.class) : EnumSet.copyOf(events);

    final MmuBuffer buffer = getBuffer(bufferName);
    final BufferEventConstraint constraint =
        new BufferEventConstraint(buffer, eventSet);

    builder.addConstraint(constraint);
  }

  private IntegerVariable getVariable(final String name) {
    final IntegerVariable variable = spec.getVariable(name);
    if (null == variable) {
      throw new GenerationAbortedException(String.format(
          "Invalid test template: variable %s is not defined in the MMU model.", name));
    }

    return variable;
  }

  private MmuBuffer getBuffer(final String name) {
    final MmuBuffer buffer = spec.getBuffer(name);
    if (null == buffer) {
      throw new GenerationAbortedException(String.format(
          "Invalid test template: buffer %s is not defined in the MMU model.", name));
    }

    return buffer;
  }

  private Set<BigInteger> extractValues(final Variate<?> variate) {
    final ValueExtractor extractor = new ValueExtractor();
    extractor.visit(variate);
    return extractor.getValues();
  }
}
