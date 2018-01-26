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

package ru.ispras.microtesk.mmu.settings;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.expression.Node;
import ru.ispras.fortress.expression.NodeVariable;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.basis.solver.bitvector.BitVectorConstraint;
import ru.ispras.microtesk.mmu.MmuPlugin;
import ru.ispras.microtesk.mmu.basis.BufferAccessEvent;
import ru.ispras.microtesk.mmu.test.template.BufferEventConstraint;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuBuffer;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuSubsystem;
import ru.ispras.microtesk.settings.AbstractSettings;
import ru.ispras.microtesk.settings.GeneratorSettings;
import ru.ispras.microtesk.utils.BigIntegerUtils;

/**
 * {@link MmuSettingsUtils} implements utilities for handing MMU settings.
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class MmuSettingsUtils {
  private MmuSettingsUtils() {}

  public static List<Node> getVariableConstraints() {
    final GeneratorSettings settings = GeneratorSettings.get();
    InvariantChecks.checkNotNull(settings);

    final List<Node> integerConstraints = new ArrayList<>();

    final Collection<AbstractSettings> integerValuesSettings =
        settings.get(IntegerValuesSettings.TAG);

    if (integerValuesSettings != null) {
      for (final AbstractSettings section : integerValuesSettings) {
        final Node constraint =
            getVariableConstraint((IntegerValuesSettings) section);

        if (constraint != null) {
          integerConstraints.add(constraint);
        }
      }
    }

    final Collection<AbstractSettings> booleanValuesSettings =
        settings.get(BooleanValuesSettings.TAG);

    if (booleanValuesSettings != null) {
      for (final AbstractSettings section : booleanValuesSettings) {
        final Node constraint = getVariableConstraint((BooleanValuesSettings) section);

        if (constraint != null) {
          integerConstraints.add(constraint);
        }
      }
    }

    return integerConstraints;
  }

  /**
   * Returns the constraint corresponding to the values settings or {@code null} if no constraint is
   * specified (the constraint is identical to TRUE).
   *
   * @param settings the values settings.
   * @return the constraint or {@code null}.
   */
  public static Node getVariableConstraint(
      final IntegerValuesSettings settings) {
    final MmuSubsystem memory = MmuPlugin.getSpecification();
    InvariantChecks.checkNotNull(memory);

    final NodeVariable variable = memory.getVariable(settings.getName());
    InvariantChecks.checkNotNull(variable);

    final Set<BigInteger> domain = settings.getPossibleValues();
    InvariantChecks.checkNotNull(domain);

    final Set<BigInteger> exclude = settings.getExcludeValues();
    InvariantChecks.checkNotNull(exclude);

    final Set<BigInteger> include = settings.getIncludeValues();
    InvariantChecks.checkNotNull(include);

    InvariantChecks.checkTrue(include.isEmpty() || exclude.isEmpty());

    if (include.isEmpty() && exclude.isEmpty()) {
      return null /* TRUE */;
    }

    final BitVectorConstraint.Kind kind = include.isEmpty()
        ? BitVectorConstraint.Kind.EXCLUDE
        : BitVectorConstraint.Kind.RETAIN;

    final Set<BigInteger> values = include.isEmpty()
        ? exclude
        : include;

    final Set<BitVector> bvDomain =
        BigIntegerUtils.toBitVectorSet(domain, variable.getDataType().getSize());
    final Set<BitVector> bvValues =
        BigIntegerUtils.toBitVectorSet(values, variable.getDataType().getSize());

    return BitVectorConstraint.domain(kind, variable, bvDomain, bvValues);
  }

  /**
   * Returns the constraint corresponding to the values settings or {@code null} if no constraint is
   * specified (the constraint is identical to TRUE).
   *
   * @param settings the values settings.
   * @return the constraint or {@code null}.
   */
  public static Node getVariableConstraint(
      final BooleanValuesSettings settings) {
    InvariantChecks.checkNotNull(settings);

    final MmuSubsystem memory = MmuPlugin.getSpecification();
    InvariantChecks.checkNotNull(memory);

    final NodeVariable variable = memory.getVariable(settings.getName());
    InvariantChecks.checkNotNull(variable);
    InvariantChecks.checkNotNull(variable.getDataType().getSize() == 1);

    final Set<Boolean> booleanValues = settings.getValues();
    InvariantChecks.checkTrue(booleanValues != null && !booleanValues.isEmpty());

    if (booleanValues.size() == 2) {
      return null /* TRUE */;
    }

    final Set<BitVector> values = new LinkedHashSet<>();

    for (final boolean value : booleanValues) {
      values.add(value ? BitVector.valueOf(1, 1) : BitVector.valueOf(0, 1));
    }

    return BitVectorConstraint.domain(BitVectorConstraint.Kind.RETAIN, variable, null, values);
  }

  public static  List<BufferEventConstraint> getBufferConstraints() {
    final MmuSubsystem memory = MmuPlugin.getSpecification();
    InvariantChecks.checkNotNull(memory);

    final GeneratorSettings settings = GeneratorSettings.get();
    InvariantChecks.checkNotNull(settings);

    final List<BufferEventConstraint> bufferEventConstraints = new ArrayList<>();

    final Collection<AbstractSettings> bufferEventsSettings =
        settings.get(BufferEventsSettings.TAG);

    if (bufferEventsSettings != null) {
      for (final AbstractSettings section : bufferEventsSettings) {
        final BufferEventsSettings bufferEventsSection = (BufferEventsSettings) section;

        final MmuBuffer buffer = memory.getBuffer(bufferEventsSection.getName());
        InvariantChecks.checkNotNull(buffer);

        final Set<BufferAccessEvent> events = bufferEventsSection.getValues();
        InvariantChecks.checkNotNull(events);

        final BufferEventConstraint constraint =
            new BufferEventConstraint(buffer, events);

        bufferEventConstraints.add(constraint);
      }
    }

    return bufferEventConstraints;
  }
}
