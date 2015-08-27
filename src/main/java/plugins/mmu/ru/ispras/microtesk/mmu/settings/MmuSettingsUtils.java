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
import ru.ispras.microtesk.mmu.translator.MmuTranslator;
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

  private static final MmuSubsystem memory = MmuTranslator.getSpecification();

  public static Collection<IntegerConstraint<IntegerField>> getConstraints(
      final Collection<AbstractSettings> settings) {
    InvariantChecks.checkNotNull(settings);

    final Collection<IntegerConstraint<IntegerField>> constraints = new ArrayList<>();

    for (final AbstractSettings section : settings) {
      if (section instanceof IntegerValuesSettings) {
        constraints.add(getConstraint((IntegerValuesSettings) section));
      } else if (section instanceof BooleanValuesSettings) {
        constraints.add(getConstraint((BooleanValuesSettings) section));
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

    final IntegerVariable variable = memory.getVariable(settings.getName());
    InvariantChecks.checkNotNull(variable);

    final Set<BigInteger> include = settings.getIncludeValues();
    InvariantChecks.checkNotNull(include);

    final Set<BigInteger> exclude = settings.getExcludeValues();
    InvariantChecks.checkNotNull(exclude);

    InvariantChecks.checkTrue(include.isEmpty() || exclude.isEmpty());

    if (include.isEmpty() && exclude.isEmpty()) {
      return null /* TRUE */;
    }

    return new IntegerDomainConstraint<IntegerField>(
        include.isEmpty() ?
            IntegerDomainConstraint.Type.EXCLUDE :
            IntegerDomainConstraint.Type.RETAIN,
        new IntegerField(variable),
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

    final IntegerVariable variable = memory.getVariable(settings.getName());
    InvariantChecks.checkNotNull(variable);

    final Set<Boolean> values = settings.getValues();
    InvariantChecks.checkTrue(values != null && !values.isEmpty());

    if (values.size() == 2) {
      return null /* TRUE */;
    }

    final Set<BigInteger> domain = new LinkedHashSet<>();

    for (final boolean value : values) {
      domain.add(value ? BigInteger.ONE : BigInteger.ZERO);
    }

    return new IntegerDomainConstraint<IntegerField>(
        IntegerDomainConstraint.Type.RETAIN, new IntegerField(variable), domain); 
  }
}
