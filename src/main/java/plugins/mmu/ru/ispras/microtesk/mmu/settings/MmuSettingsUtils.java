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

package ru.ispras.microtesk.mmu.settings;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.basis.solver.integer.IntegerConstraint;
import ru.ispras.microtesk.basis.solver.integer.IntegerDomainConstraint;
import ru.ispras.microtesk.basis.solver.integer.IntegerField;
import ru.ispras.microtesk.basis.solver.integer.IntegerVariable;
import ru.ispras.microtesk.mmu.MmuPlugin;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuSubsystem;
import ru.ispras.microtesk.settings.AbstractSettings;
import ru.ispras.microtesk.settings.GeneratorSettings;

/**
 * {@link MmuSettingsUtils} implements utilities for handing MMU settings.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class MmuSettingsUtils extends GeneratorSettings {
  private MmuSettingsUtils() {}

  private static Collection<IntegerConstraint<IntegerField>> constraints = null;

  public static Collection<IntegerConstraint<IntegerField>> getConstraints(
      final GeneratorSettings settings) {
    InvariantChecks.checkNotNull(settings);

    if (constraints != null) {
      return constraints;
    }

    constraints = new ArrayList<>();

    final Collection<AbstractSettings> integerValuesSettings =
        settings.get(IntegerValuesSettings.TAG);

    if (integerValuesSettings != null) {
      for (final AbstractSettings section : integerValuesSettings) {
        final IntegerConstraint<IntegerField> constraint =
            getConstraint((IntegerValuesSettings) section);

        if (constraint != null) {
          constraints.add(constraint);
        }
      }
    }

    final Collection<AbstractSettings> booleanValuesSettings =
        settings.get(BooleanValuesSettings.TAG);

    if (booleanValuesSettings != null) {
      for (final AbstractSettings section : booleanValuesSettings) {
        final IntegerConstraint<IntegerField> constraint =
            getConstraint((BooleanValuesSettings) section);

        if (constraint != null) {
          constraints.add(constraint);
        }
      }
    }

    return constraints;
  }

  /**
   * Returns the constraint corresponding to the values settings or {@code null} if no constraint is
   * specified (the constraint is identical to TRUE).
   * 
   * @param settings the values settings.
   * @return the constraint or {@code null}.
   */
  public static IntegerConstraint<IntegerField> getConstraint(
      final IntegerValuesSettings settings) {
    InvariantChecks.checkNotNull(settings);

    final MmuSubsystem memory = MmuPlugin.getSpecification();

    final IntegerVariable variable = memory.getVariable(settings.getName());
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

    return new IntegerDomainConstraint<IntegerField>(
        include.isEmpty() ?
            IntegerDomainConstraint.Type.EXCLUDE :
            IntegerDomainConstraint.Type.RETAIN,
        new IntegerField(variable),
        domain,
        include.isEmpty() ?
            exclude :
            include); 
  }

  /**
   * Returns the constraint corresponding to the values settings or {@code null} if no constraint is
   * specified (the constraint is identical to TRUE).
   * 
   * @param settings the values settings.
   * @return the constraint or {@code null}.
   */
  public static IntegerConstraint<IntegerField> getConstraint(
      final BooleanValuesSettings settings) {
    InvariantChecks.checkNotNull(settings);

    final MmuSubsystem memory = MmuPlugin.getSpecification();

    final IntegerVariable variable = memory.getVariable(settings.getName());
    InvariantChecks.checkNotNull(variable);

    final Set<Boolean> booleanValues = settings.getValues();
    InvariantChecks.checkTrue(booleanValues != null && !booleanValues.isEmpty());

    if (booleanValues.size() == 2) {
      return null /* TRUE */;
    }

    final Set<BigInteger> values = new LinkedHashSet<>();

    for (final boolean value : booleanValues) {
      values.add(value ? BigInteger.ONE : BigInteger.ZERO);
    }

    return new IntegerDomainConstraint<IntegerField>(
        IntegerDomainConstraint.Type.RETAIN,
        new IntegerField(variable),
        null,
        values); 
  }
}
