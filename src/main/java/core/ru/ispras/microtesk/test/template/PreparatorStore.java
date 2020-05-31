/*
 * Copyright 2014-2018 ISP RAS (http://www.ispras.ru)
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

package ru.ispras.microtesk.test.template;

import ru.ispras.fortress.data.types.bitvector.BitVector;
import ru.ispras.fortress.util.InvariantChecks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class PreparatorStore implements CodeBlockCollection<Preparator> {
  private static class PreparatorGroup {
    private Preparator defaultPreparator;
    private final List<Preparator> preparators;

    private PreparatorGroup() {
      this.defaultPreparator = null;
      this.preparators = new ArrayList<>();
    }

    public Preparator getDefault() {
      return defaultPreparator;
    }

    public Preparator setDefault(final Preparator preparator) {
      final Preparator oldPreparator = this.defaultPreparator;
      this.defaultPreparator = preparator;
      return oldPreparator;
    }

    public Preparator addPreparator(final Preparator preparator) {
      for (int index = 0; index < preparators.size(); ++index) {
        final Preparator oldPreparator = preparators.get(index);
        if (oldPreparator.equals(preparator)) {
          preparators.set(index, preparator);
          return oldPreparator;
        }
      }

      this.preparators.add(preparator);
      return null;
    }

    public List<Preparator> getPreparators() {
      return preparators;
    }
  }

  private final Map<String, PreparatorGroup> preparatorGroups;
  private final Map<String, PreparatorGroup> comparatorGroups;

  public PreparatorStore() {
    this.preparatorGroups = new HashMap<>();
    this.comparatorGroups = new HashMap<>();
  }

  @Override
  public void add(Preparator p) {
    addPreparator(p);
  }

  public Preparator addPreparator(final Preparator preparator) {
    InvariantChecks.checkNotNull(preparator);
    return addPrerator(
        preparator.isComparator() ? comparatorGroups : preparatorGroups,
        preparator
        );
  }

  public Preparator getPreparator(
      final Primitive targetMode,
      final BitVector data,
      final String preparatorName) {
    return getPreparator(preparatorGroups, targetMode, data, preparatorName);
  }

  private static Preparator getPreparator(
      final Map<String, PreparatorGroup> preparatorGroups,
      final Primitive targetMode,
      final BitVector data,
      final String preparatorName) {
    InvariantChecks.checkNotNull(targetMode);
    InvariantChecks.checkNotNull(data);

    final String name = targetMode.getName();
    final PreparatorGroup group = preparatorGroups.get(name);

    if (null == group) {
      return null;
    }

    for (final Preparator preparator : group.getPreparators()) {
      if (preparator.isMatch(targetMode, data, preparatorName)) {
        return preparator;
      }
    }

    return group.getDefault();
  }

  public Preparator getComparator(
      final Primitive targetMode,
      final BitVector data,
      final String preparatorName) {
    return getPreparator(comparatorGroups, targetMode, data, preparatorName);
  }

  private static Preparator addPrerator(
      final Map<String, PreparatorGroup> preparatorGroups,
      final Preparator preparator) {
    final String name = preparator.getTargetName();
    PreparatorGroup group = preparatorGroups.get(name);

    if (null == group) {
      group = new PreparatorGroup();
      preparatorGroups.put(name, group);
    }

    return preparator.isDefault()
        ? group.setDefault(preparator) : group.addPreparator(preparator);
  }
}
