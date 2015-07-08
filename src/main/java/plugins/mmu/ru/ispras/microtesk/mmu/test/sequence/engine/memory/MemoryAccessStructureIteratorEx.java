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

package ru.ispras.microtesk.mmu.test.sequence.engine.memory;

import java.util.Collection;
import java.util.List;

import ru.ispras.fortress.util.InvariantChecks;
import ru.ispras.microtesk.basis.Classifier;
import ru.ispras.microtesk.mmu.translator.ir.spec.MmuSubsystem;
import ru.ispras.testbase.knowledge.iterator.CollectionIterator;
import ru.ispras.testbase.knowledge.iterator.Iterator;
import ru.ispras.testbase.knowledge.iterator.ProductIterator;
import ru.ispras.testbase.knowledge.iterator.RandomValueIterator;

/**
 * {@link MemoryAccessStructureIteratorEx} implements an iterator of memory access structures.
 * 
 * @author <a href="mailto:kamkin@ispras.ru">Alexander Kamkin</a>
 */
public final class MemoryAccessStructureIteratorEx implements Iterator<MemoryAccessStructure> {
  private final MmuSubsystem memory;
  private final Classifier<MemoryAccess> classifier;

  private Iterator<List<MemoryAccessType>> typesIterator;
  private Iterator<MemoryAccessStructure> structureIterator;

  private boolean hasValue;

  /**
   * Constructs an iterator of memory access structures.
   * 
   * @param memory the memory subsystem specification.
   * @param size the number of memory accesses in a structure.
   * @param memoryAccessTypes the list of memory access types.
   * @param randomDataType the data type randomization option.
   * @param classifier the memory access classification policy.
   */
  public MemoryAccessStructureIteratorEx(
      final MmuSubsystem memory,
      final List<Collection<MemoryAccessType>> accessTypes,
      final boolean randomDataType,
      final Classifier<MemoryAccess> classifier) {
    InvariantChecks.checkNotNull(memory);
    InvariantChecks.checkNotNull(accessTypes);
    InvariantChecks.checkNotEmpty(accessTypes);
    InvariantChecks.checkNotNull(classifier);

    this.memory = memory;
    this.classifier = classifier;

    ProductIterator<MemoryAccessType> typesIterator = new ProductIterator<>();
    for (final Collection<MemoryAccessType> accessTypeCollection : accessTypes) {
      typesIterator.registerIterator(randomDataType ?
          new RandomValueIterator<MemoryAccessType>(accessTypeCollection) :
          new CollectionIterator<>(accessTypeCollection));
    }
    this.typesIterator = typesIterator;

    init();
  }

  @Override
  public void init() {
    initTypes();
    initStructure();

    hasValue = true;
  }

  @Override
  public boolean hasValue() {
    return hasValue;
  }

  @Override
  public MemoryAccessStructure value() {
    return structureIterator.value();
  }

  @Override
  public void next() {
    if (nextStructure()) {
      return;
    }
    if (nextTypes()) {
      initStructure();
      return;
    }

    hasValue = false;
  }

  @Override
  public void stop() {
    hasValue = false;
  }

  @Override
  public MemoryAccessStructureIteratorEx clone() {
    throw new UnsupportedOperationException();
  }

  private void initTypes() {
    typesIterator.init();
  }

  private boolean nextTypes() {
    if (typesIterator.hasValue()) {
      typesIterator.next();
    }
    return typesIterator.hasValue();
  }

  private void initStructure() {
    structureIterator =
        new MemoryAccessStructureIterator(memory, typesIterator.value(), classifier);
    structureIterator.init();
  }

  private boolean nextStructure() {
    if (structureIterator.hasValue()) {
      structureIterator.next();
    }
    return structureIterator.hasValue();
  }
}
