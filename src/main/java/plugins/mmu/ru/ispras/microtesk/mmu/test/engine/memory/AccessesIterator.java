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

package ru.ispras.microtesk.mmu.test.engine.memory;

import ru.ispras.castle.util.Logger;
import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.mmu.MmuPlugin;
import ru.ispras.microtesk.mmu.basis.MemoryAccessType;
import ru.ispras.microtesk.mmu.test.template.AccessConstraints;
import ru.ispras.testbase.knowledge.iterator.Iterator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * {@link AccessesIterator} implements an iterator of memory accesses.
 *
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class AccessesIterator implements Iterator<List<Access>> {
  private static final boolean CHECK_STRUCTURE = false;

  public enum Mode {
    RANDOM() {
      @Override
      public Iterator<List<Access>> getAccessIterator(
          final List<Collection<AccessChooser>> accessChoosers) {
        return new AccessesIteratorRandom(accessChoosers);
      }

      @Override
      public DependencyIterator getDependencyIterator(
          final Access access1,
          final Access access2) {
        return new DependencyIteratorRandom(access1, access2);
      }
    },

    EXHAUSTIVE() {
      @Override
      public Iterator<List<Access>> getAccessIterator(
          final List<Collection<AccessChooser>> accessChoosers) {
        return new AccessesIteratorExhaustive(accessChoosers);
      }

      @Override
      public DependencyIterator getDependencyIterator(
          final Access access1,
          final Access access2) {
        return new DependencyIteratorExhaustive(access1, access2);
      }
    };

    public abstract Iterator<List<Access>> getAccessIterator(
        final List<Collection<AccessChooser>> accessChoosers);

    public abstract DependencyIterator getDependencyIterator(
        final Access access1,
        final Access access2);
  }

  private final List<MemoryAccessType> accessTypes;

  private final Iterator<List<Access>> accessIterator;
  private final DependencyIterator[][] dependencyIterators;

  private final Mode mode;

  private boolean hasValue;
  private boolean enoughDependencies;

  private List<Access> accesses;

  public AccessesIterator(
      final GraphAbstraction abstraction,
      final List<MemoryAccessType> accessTypes,
      final List<AccessConstraints> accessConstraints,
      final AccessConstraints globalConstraints,
      final int recursionLimit,
      final Mode mode) {
    InvariantChecks.checkNotNull(abstraction);
    InvariantChecks.checkNotNull(accessTypes);
    InvariantChecks.checkNotNull(accessConstraints);
    InvariantChecks.checkNotNull(globalConstraints);
    InvariantChecks.checkNotNull(mode);
    InvariantChecks.checkNotEmpty(accessTypes);
    InvariantChecks.checkTrue(accessTypes.size() == accessConstraints.size());

    final int size = accessTypes.size();

    this.accessTypes = accessTypes;
    this.mode = mode;

    final List<Collection<AccessChooser>> accessChoosers = new ArrayList<>(size);

    int index = 0;
    for (final MemoryAccessType accessType : accessTypes) {
      final AccessConstraints constraints =
          AccessConstraints.merge(
              globalConstraints,
              accessConstraints.get(index)
          );

      final List<AccessChooser> choosers =
          CoverageExtractor.get().getPathChoosers(
              MmuPlugin.getSpecification(),
              abstraction,
              accessType,
              constraints,
              recursionLimit,
              false
          );

      InvariantChecks.checkTrue(choosers != null && !choosers.isEmpty());
      Logger.debug("Classifying memory access paths: %s %d classes", accessType, choosers.size());

      accessChoosers.add(choosers);
      index++;
    }

    this.accessIterator = mode.getAccessIterator(accessChoosers);
    this.dependencyIterators = new DependencyIterator[size][size];
  }

  @Override
  public void init() {
    hasValue = initAccesses();

    while (hasValue && !checkStructure()) {
      Logger.debug("Inconsistent memory access structure");
      hasValue = nextStructure();
    }
  }

  @Override
  public boolean hasValue() {
    return hasValue;
  }

  @Override
  public List<Access> value() {
    final int size = accesses.size();

    for (int j = 0; j < size; j++) {
      final Access access = accesses.get(j);

      access.clearDependencies();
      for (int i = 0; i < j; i++) {
        final BufferDependency dependency = dependencyIterators[i][j].value();

        if (dependency != null) {
          access.setDependency(i, dependency);
        }
      }
    }

    return accesses;
  }

  @Override
  public void next() {
    while (nextStructure()) {
      if (checkStructure()) {
        enoughDependencies = (mode == Mode.RANDOM);
        break;
      }
    }
  }

  @Override
  public void stop() {
    hasValue = false;
  }

  @Override
  public AccessesIterator clone() {
    throw new UnsupportedOperationException();
  }

  private boolean checkStructure() {
    if (CHECK_STRUCTURE) {
      final List<Access> structure = value();
      return MemoryEngineUtils.isFeasibleStructure(structure);
    }

    return true;
  }

  private boolean nextStructure() {
    if (nextDependencies()) {
      return true;
    }

    if (nextAccesses()) {
      return true;
    }

    hasValue = false;
    return false;
  }

  private boolean initDependencies() {
    final int size = accessTypes.size();

    for (int i = 0; i < size - 1; i++) {
      final Access access1 = accesses.get(i);

      for (int j = i + 1; j < size; j++) {
        final Access access2 = accesses.get(j);

        dependencyIterators[i][j] = mode.getDependencyIterator(access1, access2);
        dependencyIterators[i][j].init();

        if (!dependencyIterators[i][j].hasValue()) {
          return false;
        }
      }
    }

    enoughDependencies = false;

    return true;
  }

  private boolean nextDependencies() {
    if (enoughDependencies) {
      return false;
    }

    final int size = accessTypes.size();

    for (int i = 0; i < size - 1; i++) {
      for (int j = i + 1; j < size; j++) {
        final DependencyIterator iterator = dependencyIterators[i][j];

        if (iterator.hasValue()) {
          iterator.next();

          if (iterator.hasValue()) {
            return true;
          }

          iterator.init();
        }
      }
    }

    return false;
  }

  private boolean initAccesses() {
    accessIterator.init();

    if (accessIterator.hasValue()) {
      accesses = accessIterator.value();

      for (final Access access : accesses) {
        access.clearDependencies();
      }

      if (initDependencies()) {
        return true;
      }
    }

    return false;
  }

  private boolean nextAccesses() {
    accessIterator.next();

    while (accessIterator.hasValue()) {
      accesses = accessIterator.value();

      if (initDependencies()) {
        return true;
      }

      accessIterator.next();
    }

    return false;
  }
}
