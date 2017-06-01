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

package ru.ispras.microtesk.mmu.test.engine.memory.iterator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.Logger;
import ru.ispras.microtesk.mmu.MmuPlugin;
import ru.ispras.microtesk.mmu.basis.MemoryAccessType;
import ru.ispras.microtesk.mmu.test.engine.memory.BufferDependency;
import ru.ispras.microtesk.mmu.test.engine.memory.CoverageExtractor;
import ru.ispras.microtesk.mmu.test.engine.memory.MemoryAccess;
import ru.ispras.microtesk.mmu.test.engine.memory.MemoryAccessChooser;
import ru.ispras.microtesk.mmu.test.engine.memory.MemoryEngineUtils;
import ru.ispras.microtesk.mmu.test.engine.memory.MemoryGraphAbstraction;
import ru.ispras.microtesk.mmu.test.template.MemoryAccessConstraints;
import ru.ispras.testbase.knowledge.iterator.Iterator;

/**
 * {@link MemoryAccessIterator} implements an iterator of memory accesses.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class MemoryAccessIterator implements Iterator<List<MemoryAccess>> {
  private static final boolean CHECK_STRUCTURE = false;

  public enum Mode {
    RANDOM() {
      @Override
      public Iterator<List<MemoryAccess>> getAccessIterator(
          final List<Collection<MemoryAccessChooser>> accessChoosers) {
        return new MemoryAccessIteratorRandom(accessChoosers);
      }

      @Override
      public MemoryDependencyIterator getDependencyIterator(
          final MemoryAccess access1,
          final MemoryAccess access2) {
        return new MemoryDependencyIteratorRandom(access1, access2);
      }
    },

    EXHAUSTIVE() {
      @Override
      public Iterator<List<MemoryAccess>> getAccessIterator(
          final List<Collection<MemoryAccessChooser>> accessChoosers) {
        return new MemoryAccessIteratorExhaustive(accessChoosers);
      }

      @Override
      public MemoryDependencyIterator getDependencyIterator(
          final MemoryAccess access1,
          final MemoryAccess access2) {
        return new MemoryDependencyIteratorExhaustive(access1, access2);
      }
    };

    public abstract Iterator<List<MemoryAccess>> getAccessIterator(
        final List<Collection<MemoryAccessChooser>> accessChoosers);

    public abstract MemoryDependencyIterator getDependencyIterator(
        final MemoryAccess access1,
        final MemoryAccess access2);
  }

  private final List<MemoryAccessType> accessTypes;

  private final Iterator<List<MemoryAccess>> accessIterator;
  private final MemoryDependencyIterator[][] dependencyIterators;

  private final Mode mode;

  private boolean hasValue;
  private boolean enoughDependencies;

  private List<MemoryAccess> accesses;
  private BufferDependency[][] dependencies;

  public MemoryAccessIterator(
      final MemoryGraphAbstraction abstraction,
      final List<MemoryAccessType> accessTypes,
      final List<MemoryAccessConstraints> accessConstraints,
      final MemoryAccessConstraints globalConstraints,
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

    final List<Collection<MemoryAccessChooser>> accessChoosers = new ArrayList<>(size);

    int index = 0;
    for (final MemoryAccessType accessType : accessTypes) {
      final MemoryAccessConstraints constraints =
          MemoryAccessConstraints.merge(
              globalConstraints,
              accessConstraints.get(index)
          );

      final List<MemoryAccessChooser> choosers =
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
    this.dependencyIterators = new MemoryDependencyIterator[size][size];
    this.dependencies = new BufferDependency[size][size];
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
  public List<MemoryAccess> value() {
    for (int j = 0; j < accesses.size(); j++) {
      final MemoryAccess access = accesses.get(j);
      access.setDependencies(dependencies[j]);
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
  public MemoryAccessIterator clone() {
    throw new UnsupportedOperationException();
  }

  private boolean checkStructure() {
    if (CHECK_STRUCTURE) {
      final List<MemoryAccess> structure = value();
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
      final MemoryAccess access1 = accesses.get(i);

      for (int j = i + 1; j < size; j++) {
        final MemoryAccess access2 = accesses.get(j);

        dependencyIterators[i][j] = mode.getDependencyIterator(access1, access2);
        dependencyIterators[i][j].init();

        if (!dependencyIterators[i][j].hasValue()) {
          return false;
        }
      }
    }

    enoughDependencies = false;

    assignDependencies();
    return true;
  }

  private boolean nextDependencies() {
    if (enoughDependencies) {
      return false;
    }

    final int size = accessTypes.size();

    for (int i = 0; i < size - 1; i++) {
      for (int j = i + 1; j < size; j++) {
        final MemoryDependencyIterator iterator = dependencyIterators[i][j];

        if (iterator.hasValue()) {
          iterator.next();

          if (iterator.hasValue()) {
            assignDependencies();
            return true;
          }

          iterator.init();
        }
      }
    }

    return false;
  }

  private void assignDependencies() {
    final int size = accessTypes.size();

    for (int i = 0; i < size - 1; i++) {
      for (int j = i + 1; j < size; j++) {
        dependencies[i][j] = dependencyIterators[i][j].value();
      }
    }
  }

  private boolean initAccesses() {
    accessIterator.init();

    if (accessIterator.hasValue()) {
      accesses = accessIterator.value();

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
